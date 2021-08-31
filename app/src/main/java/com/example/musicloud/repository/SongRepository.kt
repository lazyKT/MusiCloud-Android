package com.example.musicloud.repository

import android.util.Log
import androidx.lifecycle.LiveData
import com.example.musicloud.database.Song
import com.example.musicloud.database.SongDAO
import com.example.musicloud.network.MusiCloudApi
import com.example.musicloud.network.SocketManager
import com.example.musicloud.network.SongRequestBody
import kotlinx.coroutines.*
import java.io.InputStream
import java.lang.Exception


private const val DOWNLOAD_READY = "EXISTED"
private const val PROCESS_SONG = "NOT EXISTS"
private const val PROCESS_COMPLETED = "SUCCESS"
private const val RESPONSE_TIMEOUT = "response_timeout"

class SongRepository (private val songDAO: SongDAO) {

    val songs: LiveData<List<Song>> = songDAO.getAllSongs()
    private var socketConnected:Boolean = false

    suspend fun processAndDownloadSongs () {
        withContext (Dispatchers.IO) {
            // read database
            val dlList: List<Song> = songDAO.getDownloadList (false)
            // start processing
            for (song in dlList) {
                song.processing = true
                // download
                fakeDownload()
                song.processing = false
                song.finished = true
                songDAO.update (song)
            }
            // read again
            // start processing if any ..
            // download -> repeat
        }
    }

    private suspend fun fakeDownload () {
        Log.i ("SongRepository", "Starting download ...")
        delay (6000)
        Log.i ("SongRepository", "Finish downloading ...")
    }

    private suspend fun getLastSong(): Song? {
        return songDAO.getLastSong()
    }

    suspend fun insert (parentScope: CoroutineScope, song: Song) {
        parentScope.launch (Dispatchers.IO) {

            /* save song record in ROOM DB */
//            songDAO.insert(song)

            processSong (song)

            /* update the song ROOM Database that the song has finished processing */
            songDAO.finishSongProcessing (finished = true, processing = false, song.songID)
            Log.i ("SongRepository", "SongList Updated: Done!")
        }
    }

    private suspend fun processSong (song: Song) {

        withContext (Dispatchers.IO) {
            val songStatus =
                withContext(Dispatchers.Default) {
                    MusiCloudApi.retrofitService.checkSong(SongRequestBody(url = song.youtubeURL))
                }
            Log.i ("SongRepository", "checkSongStatus = $songStatus")


            when (songStatus.status) {
                PROCESS_SONG -> {
                    doConversion (song.youtubeURL)
                    downloadSong (song.songID)
                }
                DOWNLOAD_READY -> {
                    downloadSong (song.songID)
                }
                RESPONSE_TIMEOUT -> {
                    throw Exception ("Socket Connection TimeOut!")
                }
                else -> throw Exception ("Error! Unknown Song Status!")
            }
        }
    }


    /*
    * Request to server for the song conversion process of the given YouTube URL.
    * Initiate the socket connection to server to get the status updates of conversion process
    * */
    private suspend fun doConversion (ytURL: String) {

        Log.i ("SongRepository", "Start Song Processing...")
        /* do song conversion processing in server */
        val processTask = withContext (Dispatchers.IO) {
            MusiCloudApi.retrofitService.doConversion (SongRequestBody(url = ytURL))
        }
        Log.i ("SongRepository", "Task ID: $processTask")

        val sock = SocketManager()
        socketConnected =
            withContext(Dispatchers.IO) {
                sock.connectSocketEvent()
            }
        Log.i ("SongRepository", "SocketConnected? : $socketConnected")

        if (socketConnected) {
            // do accordingly
            trackConversionProcess (sock, processTask.taskID)
        }
    }


    /* get conversion process updates from the server via socket io connection */
    private suspend fun trackConversionProcess (sock: SocketManager, taskID: String) {
        withContext (Dispatchers.IO) {
            var processStatus = async { sock.trackSongProcess(taskID) }
            Log.i ("SongRepository", "Process Status: ${processStatus.await()}")

            var taskCompleted: Boolean = processStatus.await() == PROCESS_COMPLETED

            while (!taskCompleted) {
                delay (4000L)
                processStatus = async { sock.trackSongProcess(taskID) }
                Log.i ("SongRepository", "Process Status: ${processStatus.await()}")
                taskCompleted = processStatus.await() == PROCESS_COMPLETED
            }

            sock.disconnectSocket()
        }
    }

    /* download songs from server */
    private suspend fun downloadSong (songID: String) {

        val url = "/download/${songID}"

        withContext (Dispatchers.IO) {
            try {
                val songDownloadResponseDeferred = async { MusiCloudApi.retrofitDLApiService.downloadSong (url) }

                val songDownloadResponse = songDownloadResponseDeferred.await()
                Log.i ("SongRepository", "downloadSong():ByteStream -> ${songDownloadResponse.byteStream()}")
                writeToMediaStore (songDownloadResponse.byteStream())
            }
            catch (e: Exception) {
                Log.i ("SongRepository", "downloadSong: ${e.message}")
            }
        }
    }


    private suspend fun writeToMediaStore (stream: InputStream) {

    }
}
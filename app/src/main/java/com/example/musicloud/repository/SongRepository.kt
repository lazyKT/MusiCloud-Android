package com.example.musicloud.repository

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.example.musicloud.database.Song
import com.example.musicloud.database.SongDAO
import com.example.musicloud.network.*
import com.example.musicloud.network.ErrorMessages.genErrorMessage
import kotlinx.coroutines.*
import retrofit2.HttpException
import java.io.IOException
import java.lang.Exception
import java.net.SocketTimeoutException


private const val DOWNLOAD_READY = "EXISTED"
private const val PROCESS_SONG = "NOT EXISTS"
private const val PROCESS_COMPLETED = "SUCCESS"
private const val RESPONSE_OK = "OK"
private const val RESPONSE_ERROR = "ERROR"
private const val RESPONSE_TIMEOUT = "response_timeout"
private val SOCKET_RESPONSES = arrayOf ("SUCCESS", "STARTED", "PROGRESS")

class SongRepository (private val songDAO: SongDAO) {

    val songs: LiveData<List<Song>> = songDAO.getAllSongs()
    private var socketConnected:Boolean = false

    private val _errorMessage = MutableLiveData<String?>()
    val errorMessage: LiveData<String?>
        get() = _errorMessage

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

    suspend fun doProcessAsync (scope: CoroutineScope, song: Song) = scope.async {
        try {
            songDAO.insert (song)
            val songStatus =
                withContext(Dispatchers.IO) {
                    MusiCloudApi.retrofitService.checkSong(SongRequestBody(url = song.youtubeURL))
                }
            Log.i ("SongRepository", "checkSongStatus = $songStatus")

            when (songStatus.status) {
                PROCESS_SONG -> {
                    _errorMessage.value = null
                    doConversion (scope, song.youtubeURL)
                    getSongMetaDataAsync (scope, song.songID).await()
                }
                DOWNLOAD_READY -> {
                    _errorMessage.value = null
                    getSongMetaDataAsync (scope, song.songID).await()
                }
                RESPONSE_TIMEOUT -> {
                    throw SocketTimeoutException ("Socket Connection TimeOut!")
                }
                else -> throw Exception ("Error! Unknown Song Status!")
            }
        }
        catch (exp: Exception) {
            _errorMessage.value = genErrorMessage(exp)
        }
    }

    /*
    * Request to server for the song conversion process of the given YouTube URL.
    * Initiate the socket connection to server to get the status updates of conversion process
    * */
    private suspend fun doConversion (scope: CoroutineScope, ytURL: String) {

        try {
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
                val taskCompleted = trackConversionProcessAsync (scope, sock, processTask.taskID).await()
                Log.i ("SongRepository", "ConversionTask Completed? = $taskCompleted")
            }
            else throw SocketTimeoutException ("Socket Connection Time Out!")
        }
        catch (e: HttpException) {
            _errorMessage.value = genErrorMessage(e)
        }
        catch (e: Exception) {
            _errorMessage.value = genErrorMessage(e)
        }
    }


    /* get conversion process updates from the server via socket io connection */
    private suspend fun trackConversionProcessAsync (scope: CoroutineScope, sock: SocketManager, taskID: String) = scope.async {

        try {
            var processStatus = async { sock.trackSongProcess(taskID) }
            Log.i ("SongRepository", "Process Status: ${processStatus.await()}")

            var taskCompleted: Boolean = processStatus.await() == PROCESS_COMPLETED
            var taskError = false

            while (!taskCompleted) {
                delay (4000L)
                processStatus = async { sock.trackSongProcess(taskID) }
                Log.i ("SongRepository", "Process Status: ${processStatus.await()}")
                taskCompleted = processStatus.await() == PROCESS_COMPLETED
                if (processStatus.await() !in SOCKET_RESPONSES) {
                    taskError = true
                    break
                }
            }

            if (taskError) {
                sock.disconnectSocket()
                throw IOException ("IOException: Something went wrong during the process. Try again!")
            }

            sock.disconnectSocket()

            taskCompleted
        }
        catch (e: SocketTimeoutException) {
            _errorMessage.value = genErrorMessage (e)
        }
        catch (e: Exception) {
            _errorMessage.value = genErrorMessage (e)
        }
    }

    private suspend fun getSongMetaDataAsync (scope: CoroutineScope, songID: String) = scope.async {
        val url = "/download/${songID}"

        try {
            val songDownloadResponseDeferred = async { MusiCloudApi.retrofitDLApiService.downloadSong (url) }

            val songDownloadResponse = songDownloadResponseDeferred.await()

            if (songDownloadResponse.isSuccessful) {
                _errorMessage.value = null
                val responseBody = songDownloadResponse.body()
                responseBody?.byteStream()
            }
            else {
                _errorMessage.value = songDownloadResponse.message()
                throw HttpException (songDownloadResponse)
            }
        }
        catch (e: HttpException) {
            Log.i ("SongRepository", "downloadSong: ${e.code()}: ${e.message}")
            _errorMessage.value = genErrorMessage (e)
        }
        catch (e: Exception) {
            Log.i ("SongRepository", "downloadSong: ${e.message}")
            _errorMessage.value = genErrorMessage(e)
        }
    }
}
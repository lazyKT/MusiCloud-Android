package com.example.musicloud.repository

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.example.musicloud.database.Song
import com.example.musicloud.database.SongDAO
import com.example.musicloud.network.ErrorMessages.genErrorMessage
import com.example.musicloud.network.MusiCloudApi
import com.example.musicloud.network.SocketManager
import com.example.musicloud.network.SongRequestBody
import kotlinx.coroutines.*
import retrofit2.HttpException
import java.io.IOException
import java.net.SocketTimeoutException


private const val DOWNLOAD_READY = "EXISTED"
private const val PROCESS_SONG = "NOT EXISTS"
private const val PROCESS_COMPLETED = "SUCCESS"
private const val RESPONSE_TIMEOUT = "response_timeout"
private val SOCKET_RESPONSES = arrayOf ("SUCCESS", "STARTED", "PROGRESS")

class SongRepository (private val songDAO: SongDAO) {

    val songs = songDAO.getAllSongs()

    private var socketConnected:Boolean = false

    private val _errorMessage = MutableLiveData<String?>()
    val errorMessage: LiveData<String?>
        get() = _errorMessage

    suspend fun processAndDownloadSongsAsync (scope: CoroutineScope, song: Song) =  scope.async {
        try {
            val songStatus =
                withContext(Dispatchers.IO) {
                    MusiCloudApi.retrofitService.checkSong(SongRequestBody(url = song.youtubeURL))
                }

            when (songStatus.status) {
                PROCESS_SONG -> {
                    _errorMessage.value = null
                    val conversionStatus = doConversion (scope, song.youtubeURL)
                    if (conversionStatus == 1)
                        getSongMetaDataAsync (scope, song.songID).await()
                    else throw SocketTimeoutException ("Socket Connection TimeOut!")
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

    suspend fun doProcessAsync (scope: CoroutineScope, song: Song) = scope.async {
        try {
            songDAO.insert (song)
            val songStatus =
                withContext(Dispatchers.IO) {
                    MusiCloudApi.retrofitService.checkSong(SongRequestBody(url = song.youtubeURL))
                }

            when (songStatus.status) {
                PROCESS_SONG -> {
                    _errorMessage.value = null
                    val conversionStatus = doConversion (scope, song.youtubeURL)
                    if (conversionStatus == 1)
                        getSongMetaDataAsync (scope, song.songID).await()
                    else throw SocketTimeoutException ("Socket Connection TimeOut!")
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
    private suspend fun doConversion (scope: CoroutineScope, ytURL: String): Int {

        try {
            /* do song conversion processing in server */
            val processTask = withContext (Dispatchers.IO) {
                MusiCloudApi.retrofitService.doConversion (SongRequestBody(url = ytURL))
            }

            val sock = SocketManager()
            socketConnected = withContext(scope.coroutineContext) {
                sock.connectSocketEvent()
            }
            delay (500L)

            if (socketConnected) {
                // do accordingly
                val taskCompleted = trackConversionProcessAsync (scope, sock, processTask.taskID).await()
                return if(taskCompleted as Boolean) 1 else throw IOException ("Error During Conversion Process.")
            }
            else throw SocketTimeoutException ("Socket Connection Time Out!")
        }
        catch (e: HttpException) {
            _errorMessage.value = genErrorMessage(e)
        }
        catch (e: Exception) {
            _errorMessage.value = genErrorMessage(e)
        }
        return 0
    }


    /* get conversion process updates from the server via socket io connection */
    private suspend fun trackConversionProcessAsync (scope: CoroutineScope, sock: SocketManager, taskID: String) = scope.async {

        try {
            var processStatus = async { sock.trackSongProcess(taskID) }

            var taskCompleted: Boolean = processStatus.await() == PROCESS_COMPLETED
            var taskError = false

            while (!taskCompleted) {
                delay (4000L)
                processStatus = async { sock.trackSongProcess(taskID) }
                taskCompleted = processStatus.await() == PROCESS_COMPLETED
                if (processStatus.await() !in SOCKET_RESPONSES) {
                    taskError = true
                    taskCompleted = false
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
            _errorMessage.value = genErrorMessage (e)
        }
        catch (e: Exception) {
            _errorMessage.value = genErrorMessage(e)
        }
    }
}
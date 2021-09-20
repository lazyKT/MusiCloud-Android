package com.example.musicloud.song

import android.app.Application
import android.content.ContentResolver
import android.content.ContentValues
import android.content.Context.MODE_PRIVATE
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.net.Uri
import android.os.Build
import android.provider.MediaStore
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.example.musicloud.R
import com.example.musicloud.database.Song
import com.example.musicloud.database.SongDAO
import com.example.musicloud.network.ErrorMessages.genErrorMessage
import com.example.musicloud.network.YoutubeSearchProperty
import com.example.musicloud.repository.SongRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.FileNotFoundException
import java.io.IOException
import java.io.InputStream
import java.lang.NullPointerException
import kotlin.system.measureTimeMillis

private const val WIFI = "WIFI"
private const val MOBILE_DATA = "MOBILE_DATA"
private const val NO_CONNECTION = "NO_CONNECTION"

/**
 * Usage of view model prevents the data loss because of configuration changes (screen rotations) or
 * fragment transactions within same activity.
 * The data in the Fragment (UI Class) will be cleared on onStop() stage, however, the data inside view model still survives.
 * Hence, the fragment can still fetch the data from view model when it's re-created.
 * The data inside view model is only cleared when fragment goes into onDestroy() Stage, that is when the host activity is destroyed.
 * With View Models, we can retain the data between the configuration changes and fragment transactions within the same activity,
 */
class SongViewModel (
    private val songDatabase: SongDAO,
    application: Application): AndroidViewModel (application) {

    private val songRepository = SongRepository (songDatabase)

    val processingSongs = songDatabase.getDownloadList (false)

    val errorMessageFromRepository = songRepository.errorMessage

    val songs = songRepository.songs

    var isNewSong: Boolean = false

    private val _errorMessage = MutableLiveData<String> ()
    val errorMessage: LiveData<String> get() = _errorMessage

    private val _dataSaverModeOn = MutableLiveData<Boolean> ()
    val dataSaverModeOn: LiveData<Boolean> get() = _dataSaverModeOn

    private val _userAlert = MutableLiveData<String> ()
    val userAlert: LiveData<String> get() = _userAlert

    init {
        _userAlert.value = null
        processUnfinishedSongs()
        readFromSharedPrefs()
    }

    /**
     * Finish/continue processing on the unfinished songs from previous app session.
     */
    private fun processUnfinishedSongs () {
        try {
            viewModelScope.launch (Dispatchers.IO) {

                val unfinishedSongs = songDatabase.getUnFinishedSongs (finished = false)

                unfinishedSongs.map {
                    val songData = songRepository.processAndDownloadSongsAsync (viewModelScope, it).await()
                    if (songData != null && songData != Unit) {
                        downloadSong (it, songData as InputStream)
                    }
                }
            }
            _errorMessage.value = null
        }
        catch (e: Exception) {
            _errorMessage.value = genErrorMessage (e)
        }
    }


    /**
     * Request the song processing on the server.
     */
    fun startSongProcessing (youtubeSearchProperty: YoutubeSearchProperty) {

        val networkState = getNetworkState()
        Log.i ("SongViewModel", "Current Network State: $networkState")

        when (networkState) {
            WIFI -> {
                requestSongProcessing (youtubeSearchProperty)
            }
            MOBILE_DATA -> {
                if (dataSaverModeOn.value == false) {
                    requestSongProcessing(youtubeSearchProperty)
                }
                else {
                    _userAlert.value = getApplication<Application>().getString (R.string.dataSaverUserAlert)
                }
            }
            NO_CONNECTION -> {
                _errorMessage.value = "You are Offline!"
            }
            else -> Unit
        }

    }

    private fun requestSongProcessing (youtubeSearchProperty: YoutubeSearchProperty) {
        try {
            viewModelScope.launch {
                val newSong = Song(
                    songID = youtubeSearchProperty.videoID,
                    channelTitle = youtubeSearchProperty.channelTitle,
                    thumbnailM = youtubeSearchProperty.thumbnailM,
                    thumbnailS = youtubeSearchProperty.thumbnailS,
                    youtubeURL = youtubeSearchProperty.fullURL,
                    songName = youtubeSearchProperty.title
                )

                val songData = songRepository.doProcessAsync (viewModelScope, newSong).await()
                if (songData != null && songData != Unit) {
                    downloadSong (newSong, songData as InputStream)
                }
            }
            _errorMessage.value = null
        }
        catch (e: Exception) {
            _errorMessage.value = genErrorMessage (e)
        }
    }

    /**
     * Download the song to media store after processing.
     * Upon Successful download, the song will be added to the Exoplayer playlist.
     * */
    private suspend fun downloadSong (song: Song, stream: InputStream) {

        try {
            withContext (Dispatchers.IO) {
                val resolver: ContentResolver = getApplication<Application>().contentResolver

                val audioCollection: Uri =
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                        MediaStore.Audio.Media.getContentUri (MediaStore.VOLUME_EXTERNAL_PRIMARY)
                    }
                    else {
                        MediaStore.Audio.Media.EXTERNAL_CONTENT_URI
                    }

                val songDetails: ContentValues = ContentValues().apply {
                    put (MediaStore.Audio.Media.DISPLAY_NAME, "musicloud:${song.songID}.mp3")
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                        /* IS_PENDING is only available for Android Q and later */
                        put (MediaStore.Audio.Media.IS_PENDING, 1)
                        put (MediaStore.Audio.Media.DOCUMENT_ID, "musicloud:${song.songID}")
                    }
                    put (MediaStore.Audio.Media.IS_MUSIC, 1)
                }

                // store new song inside MediaStore.Audio
                val songLocation = resolver.insert (audioCollection, songDetails)
                var downloadedBytes: Int

                viewModelScope.launch (Dispatchers.IO) {
                    val timeTaken = measureTimeMillis {
                        resolver.openOutputStream(songLocation!!, "w").use {
                            val byteArray = ByteArray(4096)
                            var count = stream.read(byteArray, 0, 4096)
                            downloadedBytes = count
                            while (count != -1) {
                                it?.write(byteArray, 0, count)
                                count = stream.read(byteArray, 0, 4096)
                                downloadedBytes += count
                            }
                            songDetails.put(MediaStore.Audio.Media.SIZE, downloadedBytes)
                        }
                    }
                    Log.i ("SongViewModel", "Download Finish in ${timeTaken/1000} seconds")

                    stream.close()
                    songDetails.clear()
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q)
                        songDetails.put (MediaStore.Audio.Media.IS_PENDING, 0)
                    songDetails.put (MediaStore.Audio.Media.DISPLAY_NAME, "musicloud:${song.songID}.mp3")
                    songDetails.put (MediaStore.Audio.Media.SIZE, downloadedBytes)
                    songDetails.put (MediaStore.Audio.Media.TITLE, song.songName)
                    songDetails.put (MediaStore.Audio.Media.ARTIST, song.channelTitle)
                    resolver.update (songLocation!!, songDetails, null, null)
                    songDatabase.updateFileLocation (songLocation.toString(), song.songID)
                    songDatabase.finishSongProcessing (finished = true, processing = false, songID = song.songID )
                    isNewSong = true
                }

            }
        }
        catch (e: NullPointerException) {
            _errorMessage.value = genErrorMessage (e)
        }
        catch (e: IOException) {
            _errorMessage.value = genErrorMessage (e)
        }
        catch (e: FileNotFoundException) {
            _errorMessage.value = genErrorMessage (e)
        }
    }

    /**
     *  Check whether the song is already exists in MediaStore.Audio before downloading
     *  private fun isSongAlreadyExistedAsync (resolver: ContentResolver, displayName: String) = viewModelScope.async {
        val audioCollection =
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
        MediaStore.Audio.Media.getContentUri (MediaStore.VOLUME_EXTERNAL_PRIMARY)
        }
        else {
        MediaStore.Audio.Media.EXTERNAL_CONTENT_URI
        }
        var count: Int? = 0
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
        Log.i ("SongViewModel", "isSongAlreadyExistedAsync -> Querying ...")
        val projection = arrayOf (MediaStore.Video.Media.DISPLAY_NAME)
        val selection = "${MediaStore.Audio.Media.DISPLAY_NAME} = ?"
        val selectionArgs = arrayOf (displayName)

        val query = resolver.query (
        audioCollection,
        projection,
        selection,
        selectionArgs,
        null
        )

        count = query?.count
     *   query?.close()
     *   }
     *   count
     *   }
     *
     *  */

    /**
     * Enable/disable data saver mode.
     * If the data saver mode is on,
     * one cannot process or download songs unless wifi is connected!
     */
    fun toggleDataSaverMode (enable: Boolean) {
        Log.i ("SongViewModel", "Data Saver Mode: $enable")
        _dataSaverModeOn.value = enable
        saveToSharedPrefs()
    }

    private fun getNetworkState (): String {
        val connectivityManager = getApplication<Application>().getSystemService (ConnectivityManager::class.java)
        val networks = connectivityManager.activeNetwork ?: return NO_CONNECTION
        val activeNetworks = connectivityManager.getNetworkCapabilities (networks) ?: return NO_CONNECTION

        return when {
            activeNetworks.hasTransport (NetworkCapabilities.TRANSPORT_WIFI) -> WIFI
            activeNetworks.hasTransport (NetworkCapabilities.TRANSPORT_CELLULAR) -> MOBILE_DATA
            activeNetworks.hasTransport (NetworkCapabilities.TRANSPORT_ETHERNET) -> MOBILE_DATA
            else -> ""
        }
    }


    /**
     * Read SharedPreferences Storage at the SongViewModel Initialization
     */
    private fun readFromSharedPrefs () {
        val sharedPreferences = getApplication<Application>().getSharedPreferences ("MusiCloud", MODE_PRIVATE)
        val dataSaverMode = sharedPreferences.getBoolean ("DataSaverMode", false)
        _dataSaverModeOn.value = dataSaverMode
    }


    /**
     * save DataSaverMode state in shared preferences storage,
     * so that the user will not lose the state.
     */
    private fun saveToSharedPrefs () {
        val sharedPreferences = getApplication<Application>().getSharedPreferences ("MusiCloud", MODE_PRIVATE)
        val editor = sharedPreferences.edit()
        dataSaverModeOn.value?.let {
            editor.putBoolean ("DataSaverMode", it)
        }
        editor.apply()
    }
}
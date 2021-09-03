package com.example.musicloud.song

import android.app.Application
import android.content.ContentResolver
import android.content.ContentValues
import android.net.Uri
import android.os.Build
import android.provider.MediaStore
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.example.musicloud.database.Song
import com.example.musicloud.database.SongDAO
import com.example.musicloud.network.ErrorMessages.genErrorMessage
import com.example.musicloud.network.YoutubeSearchProperty
import com.example.musicloud.repository.SongRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.FileNotFoundException
import java.io.IOException
import java.io.InputStream
import java.lang.NullPointerException
import kotlin.system.measureTimeMillis


/**
 * Usage of view model prevents the data loss because of configuration changes (screen rotations) or
 * fragment transactions within same activity.
 * The data in the Fragment (UI Class) will be cleared on onStop() stage, however, the data inside view model still survives.
 * Hence, the fragment can still fetch the data from view model when it's re-created.
 * The data inside view model is only cleared when fragment goes into onDestroy() Stage, that is when the host activity is destroyed.
 * With View Models, we can retain the data between the configuration changes and fragment transactions within the same activity,
 */

private const val SONG_ITEM_DETAIL: Int = 1
private const val SONG_ITEM_PLAY: Int = 0

enum class SongFetchStatus { NOTHING, LOADING, SUCCESS, ERROR }

class SongViewModel (
    private val songDatabase: SongDAO,
    application: Application): AndroidViewModel (application) {

    private val songRepository = SongRepository (songDatabase)

    val songs = songRepository.songs

    private val _currentSong = MutableLiveData<Song> ()
    val currentSong: LiveData<Song>
                get() = _currentSong

    private var _navigateToSongDetail = MutableLiveData<Long> ()
    val navigateToSongDetail get() = _navigateToSongDetail

    private val _playing = MutableLiveData<Boolean> ()
    val playing: LiveData<Boolean>
                get() = _playing

    val errorMessage: LiveData<String?>
            get() = songRepository.errorMessage

    val _fetchStatus = MutableLiveData<SongFetchStatus>()
    val fetchStatus: LiveData<SongFetchStatus>
            get() = _fetchStatus

    init {
        _playing.value = false
        _fetchStatus.value = SongFetchStatus.NOTHING
        getSongsFromRepository()
    }

    private fun getSongsFromRepository () {
        viewModelScope.launch {
            try {
                songRepository.processAndDownloadSongs()
            }
            catch (exception: IOException) {
                exception.message?.let { Log.i ("SongViewModel", it) }
            }
        }
    }

    /* on click event on song item inside recyclerview */
    fun onSongClicked (listenerActionType: SongListenerActionType) {
        when (listenerActionType.actionType) {
            SONG_ITEM_DETAIL -> _navigateToSongDetail.value = listenerActionType.songKey
            SONG_ITEM_PLAY -> playSong (listenerActionType.songKey)
        }
    }

    fun onSongDetailNavigated () {
        navigateToSongDetail.value = null
    }

    private fun playSong (id: Long) {
        Log.i ("SongViewModel", "Play the song with id: $id")
        viewModelScope.launch {
            _currentSong.value = songDatabase.get(id)
            _playing.value = true
        }
    }

    fun startSongProcessing (youtubeSearchProperty: YoutubeSearchProperty) {
        Log.i ("SongViewModel", "startSongProcessing()")
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
                Log.i ("SongViewModel", "Data : $songData")
                if (songData != null && songData != Unit) {
                    downloadSong (newSong, songData as InputStream)
                }
            }
        }
        catch (e: Exception) {

        }
    }

    /* write/store song to MediaStore.Audio */
    private suspend fun downloadSong (song: Song, stream: InputStream) {

        withContext (Dispatchers.IO) {
            try {
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

                val isSongExisted = isSongAlreadyExistedAsync (resolver, "musicloud:${song.songID}").await()
                Log.i ("SongViewModel", "isSongExisted: $isSongExisted")

                if (isSongExisted != null && isSongExisted > 0) {
                    // Just Update the fileUri property of song Room Database
                }
                else {
                    // store new song inside MediaStore.Audio
                    val songLocation = resolver.insert (audioCollection, songDetails)
                    var downloadedBytes: Int
                    Log.i ("SongViewModel", "${song.songID}.mp3 will saved at $songLocation")

                    viewModelScope.launch (Dispatchers.IO) {
                        val timeTaken = measureTimeMillis {
                            resolver.openOutputStream(songLocation!!, "w").use {
                                val byteArray = ByteArray(4096)
                                var count = stream.read(byteArray, 0, byteArray.size)
                                downloadedBytes = count
                                while (count != -1) {
                                    it?.write(count)
                                    count = stream.read(byteArray, 0, byteArray.size)
                                    downloadedBytes += count
                                }
                                songDetails.put(MediaStore.Audio.Media.SIZE, downloadedBytes)
                                Log.i ("SongViewModel", "File Write Operation Finished! $downloadedBytes downloaded!")
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
                    }
                }
            }
            catch (e: NullPointerException) {
                genErrorMessage (e)
            }
            catch (e: IOException) {
                genErrorMessage (e)
            }
            catch (e: FileNotFoundException) {
                genErrorMessage (e)
            }
        }
    }

    /* Check whether the song is already exists in MediaStore.Audio before downloading */
    private fun isSongAlreadyExistedAsync (resolver: ContentResolver, displayName: String) = viewModelScope.async {
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
            query?.close()
        }
        count
    }
}
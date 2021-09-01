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
import com.example.musicloud.network.YoutubeSearchProperty
import com.example.musicloud.repository.SongRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.IOException
import java.io.InputStream


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

    init {
        _playing.value = false
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

    fun startSongProcessing (youtubeSearchProperty: YoutubeSearchProperty) {
        Log.i ("SongViewModel", "startSongProcessing()")
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
                downloadSong (newSong.songID, songData as InputStream)
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

    private suspend fun downloadSong (songID: String, stream: InputStream) {
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
                put (MediaStore.Audio.Media.DISPLAY_NAME, "$songID.mp3")
            }

            val songLocation = resolver.insert (audioCollection, songDetails)

            Log.i ("SongViewModel", "$songID.mp3 will saved at $songLocation")
        }
    }
}
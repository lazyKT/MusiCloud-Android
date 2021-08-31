package com.example.musicloud.song

import android.app.Application
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.example.musicloud.database.Song
import com.example.musicloud.database.SongDAO
import com.example.musicloud.network.YoutubeSearchProperty
import com.example.musicloud.repository.SongRepository
import kotlinx.coroutines.launch
import java.io.IOException


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
            songRepository.insert (viewModelScope, newSong)
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
}
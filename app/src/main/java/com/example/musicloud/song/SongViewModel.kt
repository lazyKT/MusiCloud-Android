package com.example.musicloud.song

import android.app.Application
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Transformations
import androidx.lifecycle.viewModelScope
import com.example.musicloud.database.Song
import com.example.musicloud.database.SongDAO
import com.example.musicloud.formatSongs
import kotlinx.coroutines.launch


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
    val database: SongDAO,
    application: Application): AndroidViewModel (application) {

    private val lastSong = MutableLiveData<Song> ()
    val songs = database.getAllSongs()
    val songsString = Transformations.map (songs) { songs ->
        formatSongs (songs, application.resources)
    }

    private var _navigateToSongDetail = MutableLiveData<Long> ()
    val navigateToSongDetail get() = _navigateToSongDetail

    init {
        initLastSong()
    }

    private fun initLastSong () {
        // !!: viewModelScope is a Coroutines Scope
        viewModelScope.launch {
            lastSong.value = getLastSongFromDatabase()
        }
    }

    private suspend fun getLastSongFromDatabase (): Song? {
        var lastSong = database.getLastSong()
        if (lastSong?.finished == true)
            lastSong = null
        return lastSong
    }

    fun startSongProcessing () {
        Log.i ("SongViewModel", "startSongProcessing()")
        viewModelScope.launch {
            val newSong = Song()
            insert (newSong)
            lastSong.value = getLastSongFromDatabase()
        }
    }

    private suspend fun insert (song: Song) {
        database.insert (song)
    }

    fun finishSongProcessing () {
        viewModelScope.launch {
            val currentSong = lastSong.value ?: return@launch
            currentSong.finished = true
            update (currentSong)
        }
    }

    private suspend fun update (song: Song) {
        database.update (song)
    }

    fun deleteAllSongs () {
        viewModelScope.launch {
            clearAll()
            lastSong.value = null
        }
    }

    private suspend fun clearAll () {
        database.deleteAll ()
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

    fun playSong (id: Long) {
        Log.i ("SongViewModel", "Play the song with id: $id")
    }
}
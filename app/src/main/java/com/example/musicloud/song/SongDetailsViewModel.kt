package com.example.musicloud.song

import android.app.Application
import android.net.Uri
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.example.musicloud.database.Song
import com.example.musicloud.database.SongDAO
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext


class SongDetailsViewModel (
    val database: SongDAO,
    val id: String,
    app: Application
        ): AndroidViewModel (app) {


    private val _song = MutableLiveData<Song> ()
    val song: LiveData<Song> get() = _song

    private val _navigateToHomeFragmentAfterDelete = MutableLiveData<String>()
    val navigateToHomeFragmentAfterDelete: LiveData<String>
            get() = _navigateToHomeFragmentAfterDelete

    init {
        Log.i ("SongDetailsViewModel", "Song ID: $id")
        getSongById ()
    }


    private fun getSongById () {
        viewModelScope.launch {
            _song.value = database.getBySongID (id)
        }
    }

    fun deleteSong () {
        viewModelScope.launch {
            deleteSongSuspend()
        }
    }

    /* delete song from user library, Room database and from MediaStore.Audio */
    private suspend fun deleteSongSuspend () {
        try {
            withContext (Dispatchers.IO) {
                Log.i ("SongDetailsViewModel","deleting song: ${song.value?.songName}")
                val resolver = getApplication<Application>().contentResolver
                val uri = Uri.parse (song.value?.localFileURL)
                val numSongRemoved = resolver.delete (uri, null, null)
                Log.i ("SongDetailsViewModel", "${song.value?.songName} has been removed. $numSongRemoved")
            }
        }
        catch (e: SecurityException) {
            Log.i ("SongDetailsViewModel", "Security Exception Occurred! Cannot access to an item.")
        }
        finally {
            song.value?.let { database.delete (it) }
            _navigateToHomeFragmentAfterDelete.value = song.value?.songName
            Log.i ("SongDetailsViewModel", "${song.value?.songName} has been removed from Room Database!")
        }

    }

    fun onNavigatedAfterDeletion () {
        _navigateToHomeFragmentAfterDelete.value = null
    }

}
package com.example.musicloud.song

import android.app.Application
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.example.musicloud.database.Song
import com.example.musicloud.database.SongDAO
import kotlinx.coroutines.launch

class SongDetailsViewModel (
    val database: SongDAO,
    val id: Long,
    app: Application
        ): AndroidViewModel (app) {


    private val _song = MutableLiveData<Song> ()
    val song: LiveData<Song> get() = _song

    init {
        Log.i ("SongDetailsViewModel", "Song ID: $id")
        getSongById ()
    }


    private fun getSongById () {
        viewModelScope.launch {
            _song.value = database.get (id)
        }
    }

}
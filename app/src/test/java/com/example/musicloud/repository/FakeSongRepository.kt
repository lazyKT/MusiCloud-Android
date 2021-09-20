package com.example.musicloud.repository

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.example.musicloud.database.Song
import com.example.musicloud.media.Resource
import java.io.InputStream


/**
 * Fake Repository for testing.
 * Simulation of Real Repository (database, networking)
 */
class FakeSongRepository {

    private val songs = mutableListOf<Song>()

    private val observableSongs = MutableLiveData<List<Song>> (songs)

    private var shouldReturnNetworkError = false

    fun setShouldReturnNetworkError (value: Boolean) {
        shouldReturnNetworkError = value
    }

    private fun refreshSongsLiveData () {
        observableSongs.postValue (songs)
    }

    fun insertSong (song: Song) {
        songs.add (song)
        refreshSongsLiveData()
    }

    fun observeSongList (): LiveData<List<Song>> {
        return observableSongs
    }

    fun removeSong (song: Song) {
        songs.remove (song)
        refreshSongsLiveData()
    }


}
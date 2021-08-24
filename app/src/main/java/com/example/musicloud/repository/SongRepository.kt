package com.example.musicloud.repository

import android.util.Log
import androidx.lifecycle.LiveData
import com.example.musicloud.database.Song
import com.example.musicloud.database.SongDAO
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withContext

class SongRepository (private val songDAO: SongDAO) {

    val songs: LiveData<List<Song>> = songDAO.getAllSongs()

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

    private suspend fun getLastSong(): Song? {
        return songDAO.getLastSong()
    }

    private suspend fun fakeProcessAndDownload (song: Song) {
        Log.i ("SongRepository", "Starting download ...")
        delay (6000)
        Log.i ("SongRepository", "Finish downloading ...")
        songDAO.finishSongProcessing (finished = true, processing = false, song.songID)
    }

    suspend fun insert (song: Song) {
        withContext (Dispatchers.IO) {
            val lastSong = getLastSong()
            /*
            * The below is same as -> song.processing = if (lastSong != null) lastSong.finished else true
            * */
            song.processing = lastSong?.finished ?: true
            songDAO.insert (song)
            // then send network request
            fakeProcessAndDownload (song)
        }
    }

}
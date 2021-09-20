package com.example.musicloud.database

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.example.musicloud.getOrAwaitValue
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runBlockingTest
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import com.google.common.truth.Truth.*
import kotlinx.coroutines.flow.combine
import org.junit.Rule

/**
 * Testing SongDAO methods
 */

@RunWith (AndroidJUnit4::class)
class SongDAOTest {

    @get: Rule
    var instantExecutorRule = InstantTaskExecutorRule ()

    private lateinit var database: SongDatabase
    private lateinit var dao: SongDAO

    /**
     * This function will be executed before every test cases.
     */
    @Before
    fun createDB () {
        /**
         * create a database in Memory, not in storage and notice that the database queries are allowed on Main threads
         */
        database = Room.inMemoryDatabaseBuilder (ApplicationProvider.getApplicationContext(), SongDatabase::class.java)
            .allowMainThreadQueries()
            .build()
        dao = database.songDAO
    }

    /**
     * This function will be executed after every test cases
     */
    @After
    fun closeDB () {
        // close database connection
        database.close()
    }


    @ExperimentalCoroutinesApi
    @Test
    fun insertSong () = runBlockingTest {
        val song = Song (id = 1, songName = "Set Fire to the Rain")
        dao.insert (song)

        // wait for observing life data
        val songs = dao.getAllSongs().getOrAwaitValue ()

        assertThat (songs).contains (song)
    }

    @ExperimentalCoroutinesApi
    @Test
    fun getLastSongs () = runBlockingTest {
        val song = Song (id = 1, songName = "Set Fire to the Rain")
        dao.insert (song)

        val lastSong = dao.getLastSong()
        assertThat (lastSong).isEqualTo (song)
    }

    @ExperimentalCoroutinesApi
    @Test
    fun getUnfinishedSongs () = runBlockingTest {
        val finishedSonList = listOf (
            Song(id = 1, songName = "BlackBird", finished = true),
            Song(id = 2, songName = "BlueBird", finished = true),
            Song(id = 3, songName = "Sakura", finished = true),
            Song(id = 4, songName = "Yell", finished = true),
        )
        val unFinishedSongList = listOf (
            Song(id = 5, songName = "OtherSide", finished = false),
            Song(id = 6, songName = "Scar Tissues", finished = false),
            Song(id = 7, songName = "Californication", finished = false),
            Song(id = 8, songName = "Yell", finished = false),
        )
        val songList =  finishedSonList + unFinishedSongList

        songList.map {
            dao.insert (it)
        }

        val lastDownloadedSong = dao.getLastDownloadedSong (done = true).getOrAwaitValue ()
        assertThat (lastDownloadedSong).isEqualTo (finishedSonList[3])

        val unFinishedSongListFromDB = dao.getUnFinishedSongs (false)
        assertThat (unFinishedSongListFromDB).isEqualTo (unFinishedSongList.reversed())
    }

    @ExperimentalCoroutinesApi
    @Test
    fun finishedSongProcess () = runBlockingTest {
        val songID = "1x3yi5s1"
        val id = 1L
        val song = Song (id = id, songName = "Set Fire to the Rain", songID = songID)
        dao.insert (song)

        dao.finishSongProcessing (finished = true, processing = false, songID = songID)

        val songFromDB = dao.get (id)
        assertThat (songFromDB?.finished).isTrue()
    }

    @ExperimentalCoroutinesApi
    @Test
    fun deleteSong () = runBlockingTest {
        val song = Song (id = 1, songName = "Set Fire to the Rain")
        dao.insert (song)

        dao.delete (song)

        val allSongs = dao.getAllSongs().getOrAwaitValue ()
        assertThat (allSongs).doesNotContain (song)
    }
}
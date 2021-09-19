package com.example.musicloud

import androidx.room.Room
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.example.musicloud.database.Song
import com.example.musicloud.database.SongDAO
import com.example.musicloud.database.SongDatabase
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import java.io.IOException
import java.lang.Exception


@RunWith (AndroidJUnit4::class)
class SongDatabaseTest {

    private lateinit var songDAO: SongDAO
    private lateinit var songDatabase: SongDatabase

    // this function will run during the set up, before the actual test is run
    @Before
    fun createDB () {
        val context = InstrumentationRegistry.getInstrumentation().targetContext
        // inMemoryDatabaseBuilder means that the database will not be saved in the files
        songDatabase = Room.inMemoryDatabaseBuilder (context, SongDatabase::class.java)
            // by default, if we query RoomDB in Main thread we will get an error,
            // this should only be done in testing
            .allowMainThreadQueries()
            .build()
        songDAO = songDatabase.songDAO
    }

    /**
     * The database connection will be opened and created for each test case and
     * the connection is always closed back after every test cases.
     * Since, we have created the database in memory only, the data are not written
     * in storage.
     */


    /* test insertion and fetch last data */
    @Test
    @Throws (Exception::class)
    fun insertAndGetSong () {
        runBlocking {
            val song = Song()
            songDAO.insert (song)
            val lastSongInserted = songDAO.getLastSong()
            assertEquals (lastSongInserted?.songID, -1)
        }
    }

    /* test update song values */
    @Test
    @Throws (Exception::class)
    fun updateAndGetSong () {
        val newSong = Song()
        runBlocking {
            songDAO.insert (newSong)
            val lastSong = songDAO.getLastSong()
            if (lastSong != null) {
                lastSong.songID = "10"
                val updatedSong = songDAO.getLastSong()
                assertEquals (updatedSong?.songID, "10")
            }
        }
    }


    // this will be executed after every test function has been executed
    @After
    @Throws (IOException::class)
    fun closeDB () {
        println ("Closing DB ...")
        songDatabase.close()
    }
}
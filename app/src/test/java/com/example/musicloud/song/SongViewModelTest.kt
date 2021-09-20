package com.example.musicloud.song

import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.example.musicloud.database.SongDAO
import com.example.musicloud.database.SongDatabase
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith


@RunWith (AndroidJUnit4::class)
class SongViewModelTest {

    private lateinit var songViewModel: SongViewModel
    private lateinit var dao: SongDAO
    private lateinit var database: SongDatabase

    @Before
    fun setUp () {
        database = Room.inMemoryDatabaseBuilder (
            ApplicationProvider.getApplicationContext(),
            SongDatabase::class.java
                )
            .allowMainThreadQueries()
            .build()
        dao = database.songDAO
        songViewModel = SongViewModel (dao, ApplicationProvider.getApplicationContext())
    }

    @After
    fun cleanUp () {
        database.close()
    }

    @Test
    fun insertSong () {

    }

}
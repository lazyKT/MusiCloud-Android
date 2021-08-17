package com.example.musicloud.database

import androidx.lifecycle.LiveData
import androidx.room.*

@Dao
interface SongDAO {

    @Insert
    fun insert (song: Song)

    @Update
    fun update (song: Song)

    @Query ("SELECT * FROM song_table WHERE id = :id")
    fun get (id: Int): Song?

    @Query ("SELECT * FROM song_table ORDER BY id DESC")
    fun getAllSongs(): LiveData<List<Song>>

    @Query ("SELECT * FROM song_table ORDER BY id DESC LIMIT 1")
    fun getLastSong(): Song?

    @Delete
    fun delete (song: Song)
}
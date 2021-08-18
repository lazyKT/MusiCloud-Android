package com.example.musicloud.database

import androidx.lifecycle.LiveData
import androidx.room.*

@Dao
interface SongDAO {

    @Insert
    suspend fun insert (song: Song)

    @Update
    suspend fun update (song: Song)

    @Query ("SELECT * FROM song_table WHERE id = :id")
    suspend fun get (id: Int): Song?

    @Query ("SELECT * FROM song_table ORDER BY id DESC")
    fun getAllSongs(): LiveData<List<Song>>

    @Query ("SELECT * FROM song_table ORDER BY id DESC LIMIT 1")
    suspend fun getLastSong(): Song?

    @Delete
    suspend fun delete (song: Song)

    @Query ("DELETE FROM song_table")
    suspend fun deleteAll ()
}
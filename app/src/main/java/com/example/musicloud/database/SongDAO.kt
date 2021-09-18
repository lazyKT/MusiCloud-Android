package com.example.musicloud.database

import androidx.lifecycle.LiveData
import androidx.room.*

@Dao
interface SongDAO {

    @Insert
    suspend fun insert (song: Song)

    @Query ("SELECT * FROM song_table WHERE id = :id")
    suspend fun get (id: Long): Song?

    @Query ("SELECT * FROM song_table WHERE songID = :songID")
    suspend fun getBySongID (songID: String): Song?


    @Query ("SELECT * FROM song_table ORDER BY id DESC")
    fun getAllSongs(): LiveData<List<Song>>

    @Query ("SELECT * FROM song_table ORDER BY id DESC")
    fun getSongs (): List<Song>

    @Query ("SELECT * FROM song_table ORDER BY id DESC LIMIT 1")
    suspend fun getLastSong(): Song?

    @Query ("SELECT * FROM song_table WHERE finished = :finish ORDER BY id")
    suspend fun getDownloadList (finish: Boolean): List<Song>

    @Query ("SELECT * FROM song_table WHERE finished = :done ORDER BY id DESC LIMIT 1")
    fun getLastDownloadedSong (done: Boolean): LiveData<Song>

    @Query ("UPDATE song_table SET finished = :finished, processing = :processing WHERE songID = :songID")
    suspend fun finishSongProcessing (finished: Boolean, processing: Boolean, songID: String)

    @Query ("UPDATE song_table SET localFileURL = :uri WHERE songID = :songID")
    suspend fun updateFileLocation (uri: String, songID: String)

    @Delete
    suspend fun delete (song: Song)

    @Query ("DELETE FROM song_table")
    suspend fun deleteAll ()
}
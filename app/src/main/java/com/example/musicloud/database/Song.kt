package com.example.musicloud.database

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey


@Entity (tableName = "song_table")
data class Song(
    @PrimaryKey (autoGenerate = true)
    var id: Long = 0L,
    @ColumnInfo (name="songID")
    var songID: Int = -1,
    @ColumnInfo (name="userID")
    var userID: Int = -1,
    @ColumnInfo (name="youtubeURL")
    var youtubeURL: String = "",
    @ColumnInfo (name="localFileURL")
    var localFileURL: String = "",
    @ColumnInfo (name="songName")
    var songName: String = "",
    @ColumnInfo (name="finished")
    var finished: Boolean = false,
    @ColumnInfo (name="createdAt")
    var createdAt: Long = System.currentTimeMillis()
        )


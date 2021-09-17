package com.example.musicloud.database

data class SongProcess (
    val songName: String,
    val channelTitle: String,
    val thumbnail: String,
    val status: Int = 0
        )
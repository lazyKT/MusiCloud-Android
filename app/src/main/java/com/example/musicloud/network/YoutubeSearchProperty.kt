package com.example.musicloud.network

import android.os.Parcel
import android.os.Parcelable
import com.squareup.moshi.Json
import kotlinx.parcelize.Parcelize


@Parcelize
data class YoutubeSearchProperty (
    // serialization with Moshi
    @Json (name = "videoId") var videoID: String,
    @Json (name = "title") var title: String,
    @Json (name = "thumbnail_l") var thumbnailL: String,
    @Json (name = "thumbnail_s") var thumbnailS: String,
    @Json (name = "thumbnail_m") var thumbnailM: String,
    @Json (name = "full_url") var fullURL: String,
    @Json (name = "channelId") var channelID: String,
    @Json (name = "channelTitle") var channelTitle: String
        ): Parcelable {
}
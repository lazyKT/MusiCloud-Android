package com.example.musicloud.song

import android.widget.ImageView
import android.widget.TextView
import androidx.databinding.BindingAdapter
import com.example.musicloud.R
import com.example.musicloud.database.Song

/*
* Song Adapter Data Binding static functions
* */

@BindingAdapter ("songNameFormatted")
fun TextView.songNameFormatted (song: Song) {
    text = if (song.songName == "") "Hotel California" else song.songName
}

@BindingAdapter ("songSource")
fun TextView.setSongSource (song: Song) {
    text = if (song.youtubeURL == "") "Eagles" else song.youtubeURL
}

@BindingAdapter ("songThumbnail")
fun ImageView.setSongThumbnail (song: Song) {
    setImageResource (if (song.youtubeURL == "") R.drawable.ic_logo else R.drawable.about_foreground)
}

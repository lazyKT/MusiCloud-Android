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
    text = if (song.youtubeURL == "") "Eagles" else song.channelTitle
}

@BindingAdapter ("channelTitle")
fun TextView.channelTitle (song: Song) {
    text = if (song.channelTitle == "") "Anonymous" else song.channelTitle
}

@BindingAdapter ("playState")
fun ImageView.playState (playing: Boolean) {
    setImageResource (if (playing) R.drawable.ic_pause_foreground else R.drawable.play_foreground)
}

package com.example.musicloud.song

import android.annotation.SuppressLint
import android.view.View
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.TextView
import androidx.databinding.BindingAdapter
import com.example.musicloud.R
import com.example.musicloud.database.Song
import org.w3c.dom.Text

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

@SuppressLint("ResourceAsColor")
@BindingAdapter ("fontColor")
fun fontColor (textView: TextView, songReady: Boolean) {
    textView.setTextColor (if (songReady) R.color.black else R.color.light_gray)
}


@BindingAdapter ("songStatus")
fun bindSongStatus (imageView: ImageView, processing: Boolean) {
    if (processing) {
        imageView.visibility = View.VISIBLE
        imageView.setImageResource (R.drawable.loading_animation)
    }
    else {
        imageView.visibility = View.GONE
    }
}


/* state of song options button */
@BindingAdapter ("showButton")
fun bindImageButton ( imgButton: ImageButton, songReady: Boolean) {
    imgButton.visibility = if (songReady) View.VISIBLE else View.GONE
}


package com.example.musicloud.binding

import android.support.v4.media.session.PlaybackStateCompat
import android.view.View
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.TextView
import androidx.databinding.BindingAdapter
import com.example.musicloud.R
import com.example.musicloud.media.isPlaying
import com.example.musicloud.song.SongFetchStatus

@BindingAdapter ("songFetchStatus")
fun ImageView.setSongFetchStatus (status: SongFetchStatus) {

    when (status) {
        SongFetchStatus.LOADING -> {
            visibility = View.VISIBLE
            setImageResource (R.drawable.loading_animation)
        }
        SongFetchStatus.NOTHING -> {
            visibility = View.VISIBLE
            setImageResource (R.drawable.ic_empty)
        }
        else -> {
            visibility = View.GONE
        }
    }
}

@BindingAdapter ("playbackState")
fun ImageButton.setPlayBackState (isPlaying: Boolean) {
    setImageResource (if (isPlaying) R.drawable.ic_pause_foreground else R.drawable.play_foreground)
}


package com.example.musicloud.binding

import android.view.View
import android.widget.ImageView
import androidx.databinding.BindingAdapter
import com.example.musicloud.R
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

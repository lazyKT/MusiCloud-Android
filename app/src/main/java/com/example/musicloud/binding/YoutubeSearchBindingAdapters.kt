package com.example.musicloud.binding

import android.annotation.SuppressLint
import android.util.Log
import android.view.KeyEvent
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import androidx.core.net.toUri
import androidx.databinding.BindingAdapter
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.Glide.with
import com.bumptech.glide.request.RequestOptions
import com.example.musicloud.GlideApp
import com.example.musicloud.SongGlideModule
import com.example.musicloud.R
import com.example.musicloud.network.YoutubeSearchProperty
import com.example.musicloud.youtubesearch.YoutubeSearchAdapter
import com.example.musicloud.youtubesearch.YoutubeSearchApiStatus


/* image binding into songThumbnail ImageView inside youtube_search_item_view.xml */
@BindingAdapter ("imageUrl")
fun bindImage (imgView: ImageView, imgUrl: String?) {
    imgUrl?.let {
        val imgUri = imgUrl.toUri().buildUpon().scheme("https").build()
        GlideApp.with (imgView.context)
            .load (imgUri)
            .placeholder (R.drawable.loading_animation)
            .error (R.drawable.ic_broken_image_foreground)
            .into (imgView)
    }
}


@BindingAdapter ("youtubeSearchApiStatus")
fun bindStatus (statusImageView: ImageView, status: YoutubeSearchApiStatus?) {
    when (status) {
        YoutubeSearchApiStatus.NOTHING -> {
            statusImageView.visibility = View.VISIBLE
            statusImageView.setImageResource (R.drawable.youtube_search_image)
        }
        YoutubeSearchApiStatus.LOADING -> {
            statusImageView.visibility = View.VISIBLE
            statusImageView.setImageResource (R.drawable.loading_animation)
        }
        YoutubeSearchApiStatus.ERROR -> {
            statusImageView.visibility = View.VISIBLE
            statusImageView.setImageResource (R.drawable.ic_no_connection)
        }
        YoutubeSearchApiStatus.SUCCESS -> {
            statusImageView.visibility = View.GONE
        }
    }
}

@BindingAdapter ("search")
fun bindSearch (editText: EditText, getSearchResult: (String) -> Unit) {
    Log.i ("YoutubeFragment", "bindSearch Method Called!")
    editText.setOnKeyListener (View.OnKeyListener { _, keyCode, event ->
        if (keyCode == KeyEvent.KEYCODE_ENTER && event.action == KeyEvent.ACTION_UP) {
            Log.i ("YoutubeFragment", "EditText Entered!")
            getSearchResult (editText.text.toString())
            return@OnKeyListener true
        }
        false
    })
}


/*
* connect the adapter and recyclerview
* Using a bindingAdapter set the RecyclerView data, causes data binding to observe the LiveData of YoutubeSearchProperty List
* This method will call automatically when YouTubeSearchProperty List changes
* */
@BindingAdapter ("listData")
fun bindRecyclerView (recyclerView: RecyclerView, data: List<YoutubeSearchProperty>?) {
    val adapter = recyclerView.adapter as YoutubeSearchAdapter
    adapter.submitList (data)
}

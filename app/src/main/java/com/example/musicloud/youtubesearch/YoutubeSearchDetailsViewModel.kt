package com.example.musicloud.youtubesearch

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.example.musicloud.network.YoutubeSearchProperty

class YoutubeSearchDetailsViewModel (
    youtubeSearchProperty: YoutubeSearchProperty,
    app: Application
        ): AndroidViewModel (app) {

    private val _searchDetails = MutableLiveData<YoutubeSearchProperty>()
    val searchDetails: LiveData<YoutubeSearchProperty>
                get() = _searchDetails

    init {
        _searchDetails.value = youtubeSearchProperty
    }
}
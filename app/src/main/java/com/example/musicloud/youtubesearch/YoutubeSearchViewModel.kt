package com.example.musicloud.youtubesearch

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.MutableLiveData

class YoutubeSearchViewModel (
    application: Application,
    val youtubeSearchList: List<YoutubeSearch>
        ): AndroidViewModel (application) {

    init {

    }


    override fun onCleared() {
        super.onCleared()

    }

}
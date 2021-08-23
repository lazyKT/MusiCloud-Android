package com.example.musicloud.youtubesearch

import android.app.Application
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.musicloud.network.YoutubeSearchProperty
import java.lang.IllegalArgumentException

class YoutubeSearchDetailsViewModelFactory (
        private val result: YoutubeSearchProperty,
        private val app: Application
        ): ViewModelProvider.Factory {


    @Suppress("unchecked_cast")
    override fun <T : ViewModel?> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom (YoutubeSearchDetailsViewModel::class.java)) {
            return YoutubeSearchDetailsViewModel (result, app) as T
        }
        throw IllegalArgumentException ("Unknown ViewModel Class")
    }
}
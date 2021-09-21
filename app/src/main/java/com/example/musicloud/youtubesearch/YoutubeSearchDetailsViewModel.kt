package com.example.musicloud.youtubesearch

import android.app.Application
import android.content.Intent
import android.net.Uri
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

    /**
     * Open Youtube App to view the video of give url
     */
    fun viewOnYoutube () {
        searchDetails.value?.let {
            val intent = Intent (Intent.ACTION_VIEW, Uri.parse (it.fullURL))
            intent.addFlags (Intent.FLAG_ACTIVITY_NEW_TASK)
            intent.setPackage ("com.google.android.youtube")
            getApplication<Application>().startActivity (intent)
        }
    }
}
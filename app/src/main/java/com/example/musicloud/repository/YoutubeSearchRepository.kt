package com.example.musicloud.repository

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.example.musicloud.network.MusiCloudApi
import com.example.musicloud.youtubesearch.YoutubeSearchApiStatus
import kotlinx.coroutines.*


class YoutubeSearchRepository: BaseYoutubeSearchRepository() {

    private val _status = MutableLiveData<YoutubeSearchApiStatus> ()
    val status: LiveData<YoutubeSearchApiStatus> get() = _status

    override fun getSearchResultAsync (scope: CoroutineScope, filter: String) = scope.async {
        try {

            if (isYouTubeURL(filter)) {
                val ytVideoId = getYouTubeVideoId (filter)
                val url = "/search/$ytVideoId"
                _status.value = YoutubeSearchApiStatus.LOADING
                val searchResult = MusiCloudApi.retrofitService.getYtSearchResultById (url)
                searchResult
            }
            else {
                val queryString: (String) -> String = { it.replace(" ", "+") }
                val url = "/search?filter=${queryString(filter)}&maxResults=10"
                _status.value = YoutubeSearchApiStatus.LOADING
                val searchResults = MusiCloudApi.retrofitService.getYtSearchResults(url)
                searchResults
            }
        }
        catch (e: Exception) {
            throw e
        }
    }


    private fun isYouTubeURL (filter: String): Boolean {
        return filter.contains ("https://www.youtube") || filter.contains ("https://youtu.be")
    }

    private fun getYouTubeVideoId (filter: String): String {
        if (filter.contains(("https://www.youtube")))
            return filter.split ("?v=")[1]
        else
            return filter.split('/')[3]
    }
}
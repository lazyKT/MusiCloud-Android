package com.example.musicloud.repository

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.example.musicloud.network.MusiCloudApi
import com.example.musicloud.youtubesearch.YoutubeSearchApiStatus
import kotlinx.coroutines.*


class YoutubeSearchRepository {

    private val _status = MutableLiveData<YoutubeSearchApiStatus> ()
    val status: LiveData<YoutubeSearchApiStatus> get() = _status

    fun getSearchResultAsync (scope: CoroutineScope, filter: String) = scope.async {
        try {
            val queryString: (String) -> String = { it.replace(" ", "+") }

            val url = "/search?filter=${queryString(filter)}&maxResults=10"

            _status.value = YoutubeSearchApiStatus.LOADING

            val searchResults = MusiCloudApi.retrofitService.getYtSearchResults(url)
            searchResults
        }
        catch (e: Exception) {
            throw e
        }
    }
}
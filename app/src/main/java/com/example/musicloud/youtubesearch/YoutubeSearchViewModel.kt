package com.example.musicloud.youtubesearch

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import retrofit2.Callback

import com.example.musicloud.network.YoutubeSearchApi
import com.example.musicloud.network.YoutubeSearchApiService
import com.example.musicloud.network.YoutubeSearchProperty
import kotlinx.coroutines.launch
import retrofit2.Call
import retrofit2.Response

class YoutubeSearchViewModel: ViewModel () {

    private val _testResult = MutableLiveData<String> ()
    val testResult: LiveData<String> get() = _testResult

    init {
        getSearchResult()
    }

    private fun getSearchResult() {
        val url: String = "/search?filter=saing+htee+saing&maxResults=5"
        viewModelScope.launch {
            try {
                val searchResults = YoutubeSearchApi.retrofitService.getSearchResults(url)
                Log.i ("YoutubeSearchViewModel", "Fetched ${searchResults.size} results!!")
            }
            catch (e: Exception) {
                Log.i ("YoutubeSearchViewModel", "Error: " + e.message)
            }
        }
    }

}
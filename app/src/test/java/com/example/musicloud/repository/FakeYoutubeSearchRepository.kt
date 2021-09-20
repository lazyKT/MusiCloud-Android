package com.example.musicloud.repository

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.example.musicloud.network.YoutubeSearchProperty

class FakeYoutubeSearchRepository {

    private val youtubeLists = listOf(
        YoutubeSearchProperty ("123", "", "", "", "", "", "", ""),
        YoutubeSearchProperty ("231", "", "", "", "", "", "", ""),
        YoutubeSearchProperty ("321", "", "", "", "", "", "", ""),
        YoutubeSearchProperty ("542", "", "", "", "", "", "", ""),
        YoutubeSearchProperty ("123", "", "", "", "", "", "", ""),
        YoutubeSearchProperty ("567", "", "", "", "", "", "", ""),
        YoutubeSearchProperty ("234", "", "", "", "", "", "", ""),
        YoutubeSearchProperty ("321", "", "", "", "", "", "", "")
    )

    fun getSearchResults (filter: String): List<YoutubeSearchProperty> {

        val searchList = mutableListOf<YoutubeSearchProperty>()
        youtubeLists.map { result ->
            if (result.videoID == filter)
                searchList.add (result)
        }
        return searchList
    }

}
package com.example.musicloud.youtubesearch

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.musicloud.network.MusiCloudApi
import com.example.musicloud.network.ErrorMessages.genErrorMessage

import com.example.musicloud.network.YoutubeSearchProperty
import kotlinx.coroutines.launch


/* network request states */
enum class YoutubeSearchApiStatus { NOTHING, LOADING, SUCCESS, ERROR }


class YoutubeSearchViewModel: ViewModel () {

    private val _status = MutableLiveData<YoutubeSearchApiStatus> ()
    val status: LiveData<YoutubeSearchApiStatus> get() = _status

    private val _searchResults = MutableLiveData<List<YoutubeSearchProperty>> ()
    val searchResults: LiveData<List<YoutubeSearchProperty>> get() = _searchResults

    private val _searchQuery = MutableLiveData<String> ()
    val searchQuery: LiveData<String> get() = _searchQuery

    private val _navigateToDetailsPage = MutableLiveData<YoutubeSearchProperty> ()
    val navigateToDetailsPage: LiveData<YoutubeSearchProperty>
                get() = _navigateToDetailsPage

    private val _errorMessage = MutableLiveData<String?>()
    val errorMessage: LiveData<String?>
                    get() = _errorMessage

    init {
        Log.i ("YoutubeSearchViewModel", "Initialised!!!")
        _status.value = YoutubeSearchApiStatus.NOTHING
        _searchQuery.value = ""
        _errorMessage.value = null
    }

    private fun getSearchResult (filter: String) {
        Log.i ("YoutubeSearchViewModel", "searchQuery : ${searchQuery.value}")
        val queryString: (String) -> String = { it.replace(" ", "+") }

        val url = "/search?filter=${queryString(filter)}&maxResults=10"

        _status.value = YoutubeSearchApiStatus.LOADING

        viewModelScope.launch {
            try {
                val searchResults = MusiCloudApi.retrofitService.getYtSearchResults(url)
                _status.value = YoutubeSearchApiStatus.SUCCESS
                _errorMessage.value = null
                if (searchResults.isNotEmpty())
                    _searchResults.value = searchResults
            }
            catch (e: Exception) {
                _status.value = YoutubeSearchApiStatus.ERROR
                Log.i ("YoutubeSearchViewModel", "Error: ${e.message}, " + status.value)
                _searchResults.value = ArrayList()
                _errorMessage.value = genErrorMessage (e)
            }
        }
    }

    val getResult: Function1<String, Unit> = this::getSearchResult


    fun displaySearchResultDetails (result: YoutubeSearchProperty) {
        _navigateToDetailsPage.value = result
    }

    fun displaySearchResultDetailsDone () {
        _navigateToDetailsPage.value = null
    }

    override fun onCleared() {
        Log.i ("YoutubeSearchViewModel", "Data Cleared!")
        super.onCleared()
    }
}
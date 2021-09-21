package com.example.musicloud.youtubesearch

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.musicloud.network.ErrorMessages.genErrorMessage

import com.example.musicloud.network.YoutubeSearchProperty
import com.example.musicloud.repository.BaseYoutubeSearchRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.lang.Exception


/* network request states */
enum class YoutubeSearchApiStatus { NOTHING, LOADING, SUCCESS, ERROR }


class YoutubeSearchViewModel (
    private val repository: BaseYoutubeSearchRepository
        ): ViewModel () {

    private val _status = MutableLiveData<YoutubeSearchApiStatus> ()
    val status: LiveData<YoutubeSearchApiStatus> get() = _status

    private val _searchResults = MutableLiveData <List<YoutubeSearchProperty>> ()
    val searchResults: LiveData<List<YoutubeSearchProperty>> get() = _searchResults

    private val _searchQuery = MutableLiveData<String>()
    val searchQuery: LiveData<String> get() = _searchQuery

    private val _navigateToDetailsPage = MutableLiveData<YoutubeSearchProperty> ()
    val navigateToDetailsPage: LiveData<YoutubeSearchProperty>
                get() = _navigateToDetailsPage

    private val _errorMessage = MutableLiveData<String?>()
    val errorMessage: LiveData<String?>
        get() = _errorMessage

    init {
        _searchQuery.value = ""
        _status.value = YoutubeSearchApiStatus.NOTHING
        _navigateToDetailsPage.value = null
    }

    @Suppress ("unchecked_cast")
    fun getSearchResult (filter: String) {
        _searchResults.value = null
        _status.value = YoutubeSearchApiStatus.LOADING
        viewModelScope.launch (Dispatchers.IO) {
            try {
                val searchResults = repository.getSearchResultAsync (viewModelScope, filter).await() as List<*>
                _status.postValue (YoutubeSearchApiStatus.SUCCESS)
                _searchResults.postValue (searchResults as List<YoutubeSearchProperty>?)
            }
            catch (e: Exception) {
                _status.postValue (YoutubeSearchApiStatus.ERROR)
                _errorMessage.postValue (genErrorMessage(e))
            }
        }
    }

    val getResult: Function1<String, Unit> = this::getSearchResult


    fun displaySearchResultDetails (result: YoutubeSearchProperty) {
        Log.i ("YoutubeSearchViewModel", "displaySearchResultDetails ${result.title}")
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
package com.example.musicloud.youtubesearch

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.musicloud.repository.YoutubeSearchRepository
import java.lang.IllegalArgumentException

class YoutubeSearchViewModelFactory (
    private val repository: YoutubeSearchRepository
): ViewModelProvider.Factory {

    @Suppress ("unchecked_cast")
    override fun <T : ViewModel?> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom (YoutubeSearchViewModel::class.java)) {
            return YoutubeSearchViewModel (repository) as T
        }
        throw IllegalArgumentException ("Unknown ViewModel Class")
    }


}
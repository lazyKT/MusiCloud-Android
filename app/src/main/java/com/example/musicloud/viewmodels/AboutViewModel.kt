package com.example.musicloud.viewmodels

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

class AboutViewModel: ViewModel() {

    private val _aboutText = MutableLiveData<String>()
    val aboutText : LiveData<String> get() = _aboutText

    init {
        _aboutText.value = "A Place Where You Can Keep All of Your Emoticons Together.\n" +
                "With MusiCloud, you will be able to download songs from YouTube and import from Local Storage."
    }

}
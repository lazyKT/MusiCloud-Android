package com.example.musicloud.ViewModelFactories

import android.app.Application
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.musicloud.viewmodels.HelpViewModel
import java.lang.IllegalArgumentException

class HelpViewModelFactory (
    private val application: Application
): ViewModelProvider.Factory {

    @Suppress ("unchecked_cast")
    override fun <T : ViewModel?> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(HelpViewModel::class.java)) {
            return HelpViewModel (application) as T
        }
        else throw IllegalArgumentException ("Unknown View Model Class!")
    }

}
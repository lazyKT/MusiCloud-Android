package com.example.musicloud.song

import android.app.Application
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.musicloud.database.SongDAO
import java.lang.IllegalArgumentException

class SongViewModelFactory (
        private val dataSource: SongDAO,
        private val application: Application): ViewModelProvider.Factory {

        @Suppress ("unchecked_cast")
        override fun <T : ViewModel?> create (modelClass: Class<T>): T {
            if (modelClass.isAssignableFrom (SongViewModel::class.java)) {
                return SongViewModel (dataSource, application) as T
            }
            throw IllegalArgumentException ("Unknown ViewModel Class")
        }

}
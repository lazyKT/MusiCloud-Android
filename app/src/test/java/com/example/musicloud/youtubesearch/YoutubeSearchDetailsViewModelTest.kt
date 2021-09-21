package com.example.musicloud.youtubesearch

import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.example.musicloud.network.YoutubeSearchProperty
import org.junit.Assert.*

import org.junit.Test
import org.junit.runner.RunWith


@RunWith (AndroidJUnit4::class)
class YoutubeSearchDetailsViewModelTest {

    @Test
    fun getSearchDetails_setsNewSearchDetailsEvent() {

        val youtubeSearchProperty = YoutubeSearchProperty ("", "", "", "", "", "", "", "")

        val youtubeSearchDetailsViewModel = YoutubeSearchDetailsViewModel (
            youtubeSearchProperty,
            ApplicationProvider.getApplicationContext()
        )

//        youtubeSearchDetailsViewModel.searchDetails.value = youtubeSearchProperty

    }
}
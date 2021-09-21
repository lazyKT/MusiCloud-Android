package com.example.musicloud.youtubesearch

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.lifecycle.Observer
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.example.musicloud.getOrAwaitValue
import com.example.musicloud.network.YoutubeSearchProperty
import com.example.musicloud.repository.BaseYoutubeSearchRepository
import com.example.musicloud.repository.FakeYoutubeSearchRepository
import com.google.common.truth.Truth.assertThat
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith


@RunWith (AndroidJUnit4::class)
class YoutubeSearchViewModelTest {

    @get: Rule
    var instantExecutorRule = InstantTaskExecutorRule ()

    @Test
    fun setNewSearchList_setsNewSearchListEvent () {

        val property = YoutubeSearchProperty ("123", "", "", "", "", "", "", "")
        val repository = FakeYoutubeSearchRepository () as BaseYoutubeSearchRepository
        val viewModel = YoutubeSearchViewModel (repository)

        val observer = Observer<List<YoutubeSearchProperty>> {}
        try {

            viewModel.searchResults.observeForever (observer)

            viewModel.getSearchResult ("123")

            val value = viewModel.searchResults.getOrAwaitValue()

            assertThat (value).contains(property)
        }
        finally {

        }
    }

}
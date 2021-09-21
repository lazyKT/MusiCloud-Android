package com.example.musicloud.repository

import com.example.musicloud.network.YoutubeSearchProperty
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Deferred

abstract class BaseYoutubeSearchRepository {

    abstract fun getSearchResultAsync (scope: CoroutineScope, filter: String): Deferred<List<YoutubeSearchProperty>>

}
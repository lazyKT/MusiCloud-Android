package com.example.musicloud.youtubesearch

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.musicloud.R

class YoutubeSearchAdapter (
    private val clickListener: YoutubeSearchResultListener
        ): ListAdapter <YoutubeSearch, RecyclerView.ViewHolder> (SearchDiffCallback()) {


    class ViewHolder private constructor (view: View): RecyclerView.ViewHolder (view) {
        companion object {
            fun from (parent: ViewGroup): ViewHolder {
                val layoutInflater = LayoutInflater.from (parent.context)
                val view = layoutInflater.inflate (R.layout.item_text_view, parent, false)
                return ViewHolder (view)
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return ViewHolder.from (parent)
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {

    }

}


/* Search Diff Callbacks and Recycler view data binding */
class SearchDiffCallback: DiffUtil.ItemCallback <YoutubeSearch>() {
    override fun areItemsTheSame(oldItem: YoutubeSearch, newItem: YoutubeSearch): Boolean {
        return oldItem.videoID == newItem.videoID
    }

    override fun areContentsTheSame(oldItem: YoutubeSearch, newItem: YoutubeSearch): Boolean {
        return oldItem == newItem
    }


}


/* on click event on search result item view and option button */
class YoutubeSearchResultListener (val clickListener: (action: YoutubeSearchResultListenerAction) -> Unit) {

    fun onClick (search: YoutubeSearch) = clickListener (YoutubeSearchResultListenerAction (search, 0))

    fun onOptionClick (search: YoutubeSearch) = clickListener (YoutubeSearchResultListenerAction (search, 1))

}

/* ListenAction which defines clicked result and required actions */
data class YoutubeSearchResultListenerAction (
    val search: YoutubeSearch,
    val actionType: Int
        )


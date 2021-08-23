package com.example.musicloud.youtubesearch

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.example.musicloud.databinding.YoutubeSearchItemViewBinding
import com.example.musicloud.network.YoutubeSearchProperty


class YoutubeSearchAdapter (private val onClickListener: OnClickListener):
    ListAdapter<YoutubeSearchProperty, YoutubeSearchAdapter.YoutubeSearchViewHolder>(DiffCallBack) {

    class YoutubeSearchViewHolder (private var binding: YoutubeSearchItemViewBinding): RecyclerView.ViewHolder(binding.root) {
        fun bind (youtubeSearchProperty: YoutubeSearchProperty) {
            binding.property = youtubeSearchProperty
            binding.executePendingBindings()
        }
    }

    override fun onCreateViewHolder (parent: ViewGroup, viewType: Int): YoutubeSearchViewHolder {
        val binding = YoutubeSearchItemViewBinding.inflate (LayoutInflater.from(parent.context))
        return YoutubeSearchViewHolder (binding)
    }

    override fun onBindViewHolder (holder: YoutubeSearchViewHolder, position: Int) {
        val youtubeSearchProperty = getItem (position)
        holder.itemView.setOnClickListener {
            onClickListener.onClick (youtubeSearchProperty)
        }
        holder.bind (youtubeSearchProperty)
    }

    companion object DiffCallBack: DiffUtil.ItemCallback<YoutubeSearchProperty>() {

        override fun areItemsTheSame (oldItem: YoutubeSearchProperty, newItem: YoutubeSearchProperty): Boolean {
            return oldItem == newItem
        }

        override fun areContentsTheSame (oldItem: YoutubeSearchProperty, newItem: YoutubeSearchProperty): Boolean {
            return oldItem.videoID == newItem.videoID
        }
    }

    /* onClick event on Youtube Search Results */
    class OnClickListener (val clickListener: (youtubeSearchResult: YoutubeSearchProperty) -> Unit) {
        fun onClick (result: YoutubeSearchProperty) = clickListener (result)
    }

}


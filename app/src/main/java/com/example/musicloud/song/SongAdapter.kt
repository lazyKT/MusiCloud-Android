package com.example.musicloud.song

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.musicloud.R
import com.example.musicloud.database.Song
import com.example.musicloud.databinding.SongItemViewBinding


class SongAdapter: ListAdapter<Song, SongAdapter.ViewHolder> (SongDiffCallBack()) {

    class ViewHolder private constructor (val binding: SongItemViewBinding): RecyclerView.ViewHolder(binding.root) {

        companion object {
            fun from (parent: ViewGroup): ViewHolder {
                val layoutInflater = LayoutInflater.from (parent.context)
                val binding = SongItemViewBinding.inflate (layoutInflater, parent, false)
                return ViewHolder (binding)
            }
        }

        fun bind(song: Song) {
            binding.song = song
            binding.executePendingBindings() // this call is to execute any pending data bindings right away
        }
    }



    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val song = getItem (position) // getItem(position: Int) method is provided by ListAdapter
        holder.bind (song)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder = ViewHolder.from (parent)
}

/* implementation of DiffUtils Class */
class SongDiffCallBack: DiffUtil.ItemCallback<Song> () {

    override fun areItemsTheSame(oldItem: Song, newItem: Song): Boolean {
        return newItem.id == oldItem.id
    }

    override fun areContentsTheSame(oldItem: Song, newItem: Song): Boolean {
        return oldItem == newItem
    }

}

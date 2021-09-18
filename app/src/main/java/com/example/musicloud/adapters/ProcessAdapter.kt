package com.example.musicloud.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.RequestManager
import com.example.musicloud.database.Song
import com.example.musicloud.databinding.ProcessItemViewBinding
import javax.inject.Inject

class ProcessAdapter @Inject constructor (
    private val glide: RequestManager
): ListAdapter <Song, ProcessAdapter.ProcessViewHolder> (DiffCallBack) {


    class ProcessViewHolder private constructor (val binding: ProcessItemViewBinding)
        : RecyclerView.ViewHolder (binding.root) {

        companion object {
            fun from (parent: ViewGroup): ProcessViewHolder {
                val inflater = LayoutInflater.from (parent.context)
                val binding = ProcessItemViewBinding.inflate (inflater)
                return ProcessViewHolder (binding)
            }
        }

        fun bind (song: Song) {
            binding.song = song
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ProcessViewHolder {
        return ProcessViewHolder.from (parent)
    }

    override fun onBindViewHolder(holder: ProcessViewHolder, position: Int) {
        val processItem = getItem (position)
        holder.bind (processItem)
    }

    companion object DiffCallBack: DiffUtil.ItemCallback <Song> () {

        override fun areItemsTheSame(oldItem: Song, newItem: Song): Boolean {
            return oldItem.songID == newItem.songID
        }

        override fun areContentsTheSame(oldItem: Song, newItem: Song): Boolean {
            return oldItem.songID == newItem.songID && oldItem.finished == newItem.finished
        }

    }

}
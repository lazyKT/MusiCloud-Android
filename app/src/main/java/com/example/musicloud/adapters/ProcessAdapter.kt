package com.example.musicloud.adapters

import android.util.Log
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.AsyncListDiffer
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.RequestManager
import com.example.musicloud.R
import com.example.musicloud.database.Song
import com.example.musicloud.databinding.ProcessItemViewBinding
import javax.inject.Inject

class ProcessAdapter @Inject constructor (
    private val glide: RequestManager
): RecyclerView.Adapter<ProcessAdapter.ProcessViewHolder> () {


    class ProcessViewHolder private constructor (val binding: ProcessItemViewBinding)
        : RecyclerView.ViewHolder (binding.root) {

        companion object {
            fun from (parent: ViewGroup): ProcessViewHolder {
                val inflater = LayoutInflater.from (parent.context)
                val binding = ProcessItemViewBinding.inflate (inflater)
                return ProcessViewHolder (binding)
            }
        }
    }

    private val diffCallBack = object : DiffUtil.ItemCallback <Song> () {

        override fun areItemsTheSame(oldItem: Song, newItem: Song): Boolean {
            return oldItem.songID == newItem.songID
        }

        override fun areContentsTheSame(oldItem: Song, newItem: Song): Boolean {
            return oldItem.songID == newItem.songID && oldItem.finished == newItem.finished
        }

    }

    private val differ: AsyncListDiffer <Song> = AsyncListDiffer (this, diffCallBack)

    var songs: List<Song>
        get() = differ.currentList
        set(value) = differ.submitList (value)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ProcessViewHolder {
        return ProcessViewHolder.from (parent)
    }

    override fun onBindViewHolder(holder: ProcessViewHolder, position: Int) {
        val processItem = songs.get (position)

        holder.binding.apply {
            glide.load (processItem.thumbnailS).into (processThumbnail)
            song = processItem
            Log.i ("ProcessAdapter", processItem.toString())
            songProcessSubtitle.text = processItem.channelTitle
            songProcessTitle.text = processItem.songName

            processStatusImage.setImageResource (R.drawable.loading_animation)
        }
    }

    override fun getItemCount(): Int {
        return songs.size
    }
}
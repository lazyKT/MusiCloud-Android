package com.example.musicloud.song

import android.annotation.SuppressLint
import android.util.Log
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.musicloud.database.Song
import com.example.musicloud.databinding.SongItemViewBinding


class SongAdapter (private val clickListener: SongListener): ListAdapter<Song, SongAdapter.ViewHolder> (SongDiffCallBack()) {

    /* Song Item View Holder */
    class ViewHolder private constructor (val binding: SongItemViewBinding): RecyclerView.ViewHolder(binding.root) {

        companion object {
            fun from (parent: ViewGroup): ViewHolder {
                val layoutInflater = LayoutInflater.from (parent.context)
                val binding = SongItemViewBinding.inflate (layoutInflater, parent, false)
                return ViewHolder (binding)
            }
        }

        fun bind(song: Song, clickListener: SongListener) {
            binding.song = song
            binding.executePendingBindings() // this call is to execute any pending data bindings right away
            binding.clickListener = clickListener
        }
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val song = getItem (position)
        holder.bind (song, clickListener)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder.from(parent)
    }
}

/* implementation of DiffUtils Class */
class SongDiffCallBack: DiffUtil.ItemCallback<Song> () {

    override fun areItemsTheSame(oldItem: Song, newItem: Song): Boolean {
        return newItem == oldItem
    }

    @SuppressLint ("DiffUtilEquals")
    override fun areContentsTheSame(oldItem: Song, newItem: Song): Boolean {
//        Log.i ("SongAdapter", "areContentsTheSame (${newItem.songName} vs ${oldItem.songName}: " +
//                "${oldItem.songID == newItem.songID && oldItem.finished == newItem.finished}")
        return oldItem.songID == newItem.songID && oldItem.finished == newItem.finished
    }

}

/*
* Song Item Click Listener.
* Notice that 'clickListener' is the SongListener's Constructor variable as a 'function' type. (Higher Order Function)
* */
class SongListener (val clickListener: (actionType: SongListenerActionType) -> Unit) {

    /* onClick Event on the SongItemView */
    fun onClick (song: Song) = clickListener (SongListenerActionType (song.id, 0))

    /* onClick Event on the SongOptionButton */
    fun onOptionClick (song: Song) = clickListener (SongListenerActionType (song.id, 1))
}

/* ListenAction which defines clicked song and required actions: play or show details */
data class SongListenerActionType (
    val songKey: Long,
    val actionType: Int
        )



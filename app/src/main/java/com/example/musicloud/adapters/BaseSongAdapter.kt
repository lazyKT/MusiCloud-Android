package com.example.musicloud.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.AsyncListDiffer
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import androidx.viewbinding.ViewBinding
import com.example.musicloud.database.Song
import com.example.musicloud.databinding.MediaItemViewBinding
import java.lang.Exception

private const val SONG_LIST_LAYOUT = 0
private const val SONG_DETAILS_LAYOUT = 1

abstract class BaseSongAdapter (
    private val layoutID: Int
): RecyclerView.Adapter<BaseSongAdapter.SongViewHolder> () {

    class SongViewHolder private constructor (val binding: ViewBinding):
        RecyclerView.ViewHolder (binding.root) {

        companion object {
            fun from (parent: ViewGroup, layoutID: Int): SongViewHolder {
                when (layoutID) {
                    SONG_LIST_LAYOUT -> {
                        val layoutInflater = LayoutInflater.from (parent.context)
                        val binding = MediaItemViewBinding.inflate (layoutInflater, parent, false)
                        return SongViewHolder (binding)
                    }
                    else -> throw Exception ("Invalid Layout ID $layoutID")
                }
            }
        }
    }

    protected val diffCallBack = object : DiffUtil.ItemCallback<Song>() {

        override fun areItemsTheSame(oldItem: Song, newItem: Song): Boolean {
            return oldItem.id == newItem.id
        }

        override fun areContentsTheSame(oldItem: Song, newItem: Song): Boolean {
            return oldItem.hashCode() == newItem.hashCode()
        }
    }

    protected abstract val differ: AsyncListDiffer <Song>

    var songs: List<Song>
        get() = differ.currentList
        set(value) = differ.submitList (value)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SongViewHolder {
        return SongViewHolder.from (parent, layoutID)
    }

    protected var onItemClickListener: ((Song) -> Unit)? = null

    fun setItemClickListener (listener: (Song) -> Unit) {
        onItemClickListener = listener
    }

    override fun getItemCount(): Int {
        return songs.size
    }
}
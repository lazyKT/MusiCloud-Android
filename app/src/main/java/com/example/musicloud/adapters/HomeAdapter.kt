package com.example.musicloud.adapters

import android.util.Log
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.AsyncListDiffer
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.RequestManager
import com.example.musicloud.database.Song
import com.example.musicloud.databinding.MediaItemViewBinding
import javax.inject.Inject


class HomeAdapter @Inject constructor(
    private val glide: RequestManager
): RecyclerView.Adapter<HomeAdapter.SongViewHolder> () {

    class SongViewHolder private constructor (val binding: MediaItemViewBinding):
        RecyclerView.ViewHolder (binding.root) {

        companion object {
            fun from (parent: ViewGroup): SongViewHolder {
                val layoutInflater = LayoutInflater.from (parent.context)
                val binding = MediaItemViewBinding.inflate (layoutInflater, parent, false)
                return SongViewHolder (binding)
            }
        }
    }

    private val diffCallBack = object : DiffUtil.ItemCallback<Song>() {

        override fun areItemsTheSame(oldItem: Song, newItem: Song): Boolean {
            return oldItem.id == newItem.id
        }

        override fun areContentsTheSame(oldItem: Song, newItem: Song): Boolean {
            return oldItem.hashCode() == newItem.hashCode()
        }
    }

    private val differ = AsyncListDiffer (this, diffCallBack)

    var songs: List<Song>
            get() = differ.currentList
            set(value) = differ.submitList (value)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SongViewHolder {
        return SongViewHolder.from (parent)
    }

    override fun onBindViewHolder(holder: SongViewHolder, position: Int) {
        val song = songs[position]
        Log.i ("HomeAdapter", "song: $song")
        holder.binding.apply {

            glide.load(song.thumbnailS).into (songThumbnail)
            songItem = song
            mediaItem.setOnClickListener {
                onItemClickListener?.let { action ->
                    action (song)
                }
            }
        }

    }

    private var onItemClickListener: ((Song) -> Unit)? = null

    fun setOnItemClickListener (listener: (Song) -> Unit) {
        onItemClickListener = listener
    }

    override fun getItemCount(): Int {
        return songs.size
    }
}

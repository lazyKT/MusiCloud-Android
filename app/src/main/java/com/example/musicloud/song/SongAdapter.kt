package com.example.musicloud.song

import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.musicloud.R
import com.example.musicloud.database.Song


class SongAdapter: RecyclerView.Adapter<SongAdapter.ViewHolder>() {

    class ViewHolder (itemView: View): RecyclerView.ViewHolder(itemView) {
        val songName: TextView = itemView.findViewById (R.id.songNameTextView)
        val songSource: TextView = itemView.findViewById (R.id.sourceTextView)
        val songThumbnail: ImageView = itemView.findViewById (R.id.songThumbnail)
    }

    var songs = listOf<Song>()
                    set(value) {
                        field = value
                        notifyDataSetChanged()
                    }

    override fun getItemCount(): Int {
        return songs.size
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val song = songs[position]
        holder.songName.text = song.id.toString()
        holder.songSource.text = song.id.toString()
        holder.songThumbnail.setImageResource (R.drawable.ic_logo)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val layoutInflater = LayoutInflater.from (parent.context)
        val view = layoutInflater.inflate (R.layout.song_item_view, parent, false)

        return ViewHolder(view)
    }
}
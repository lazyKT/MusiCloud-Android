package com.example.musicloud.adapters

import android.util.Log
import androidx.recyclerview.widget.AsyncListDiffer
import com.bumptech.glide.RequestManager
import com.example.musicloud.databinding.MediaItemViewBinding
import java.lang.ClassCastException
import javax.inject.Inject

private const val SONG_LIST_LAYOUT = 0

class SwipeSongAdapter
//    :BaseSongAdapter (SONG_LIST_LAYOUT) {

//    override val differ = AsyncListDiffer (this, diffCallBack)
//
//    override fun onBindViewHolder(holder: SongViewHolder, position: Int) {
//        val song = songs[position]
//        Log.i ("HomeAdapter", "song: $song")
//        try {
//            holder.binding as MediaItemViewBinding
//            holder.binding.apply {
//
//                songItem = song
//                mediaItem.setOnClickListener {
//                    onItemClickListener?.let { action ->
//                        action (song)
//                    }
//                }
//            }
//        }
//        catch (e: ClassCastException) {
//            Log.i ("HomeAdapter", "ClassCaseException: ${e.message}")
//        }
//
//    }
//}

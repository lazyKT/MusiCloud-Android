package com.example.musicloud.song

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.musicloud.R
import com.example.musicloud.database.Song
import com.example.musicloud.databinding.SongItemViewBinding
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.lang.ClassCastException


private const val ITEM_VIEW_TYPE_HEADER = 0
private const val ITEM_VIEW_TYPE_ITEM = 1

private val adapterScope = CoroutineScope (Dispatchers.Default)

class SongAdapter (private val clickListener: SongListener): ListAdapter<DataItem, RecyclerView.ViewHolder> (SongDiffCallBack()) {

    /* Header View Holder */
    class HeaderViewHolder (view: View): RecyclerView.ViewHolder(view) {

        companion object {
            fun from (parent: ViewGroup): HeaderViewHolder {
                val layoutInflater = LayoutInflater.from (parent.context)
                val view = layoutInflater.inflate (R.layout.header_text, parent, false)
                return HeaderViewHolder(view)
            }
        }

    }

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

    /* Update recyclerview */
    fun addHeaderAndSubmitList (list: List<Song>?) {
        adapterScope.launch {
            val songs = when (list) {
                null -> listOf (DataItem.Header)
                else -> listOf (DataItem.Header) + list.map { DataItem.SongItem(it) }
            }
            withContext (Dispatchers.Main) {
                submitList (songs)
            }
        }
    }

    /*
    * get View Type from the View Holder.
    * That viewType will be later used in onCreateViewHolder
    * */
    override fun getItemViewType(position: Int): Int {
        return when (getItem(position)) {
            is DataItem.Header -> ITEM_VIEW_TYPE_HEADER
            is DataItem.SongItem -> ITEM_VIEW_TYPE_ITEM
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        when (holder) {
            /* When onBind, check the view Holder, if the viewHolder is for song items, bind with song_item_view and populate data */
            is ViewHolder -> {
                val songItem = getItem (position) as DataItem.SongItem
                holder.bind (songItem.song, clickListener)
            }
            /* Our Header ViewHolder does not explicitly need a binding, since no live (or dynamic) data involved */
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        /*
        * During onCreateViewHolder, we check the viewType which we defined inside getItemViewType.
        * If 0, just inflate the Header View.
        * Otherwise, inflate both Header and Item View.
        * */
        return when (viewType) {
            ITEM_VIEW_TYPE_ITEM -> ViewHolder.from (parent) // inflate Header View for viewType '0'
            ITEM_VIEW_TYPE_HEADER -> HeaderViewHolder.from (parent) // inflate Song Item View for viewType '1'
            else -> throw ClassCastException ("Unknown viewType: $viewType")
        }
    }
}

/* implementation of DiffUtils Class */
class SongDiffCallBack: DiffUtil.ItemCallback<DataItem> () {

    override fun areItemsTheSame(oldItem: DataItem, newItem: DataItem): Boolean {
        return newItem.id == oldItem.id
    }

    @SuppressLint ("DiffUtilEquals")
    override fun areContentsTheSame(oldItem: DataItem, newItem: DataItem): Boolean {
        return oldItem == newItem
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

/* data item class defined the different types of data items (whether the actual data or the header) */
sealed class DataItem {

    abstract val id: Long

    /* the below two inner classes must implement the abstract id */

    data class SongItem (val song: Song): DataItem() {
        override val id = song.id
    }

    object Header: DataItem() {
        override val id = Long.MIN_VALUE
    }

}


package com.example.musicloud.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.musicloud.database.SongProcess
import com.example.musicloud.databinding.ProcessItemViewBinding

class ProcessAdapter (): ListAdapter <SongProcess, ProcessAdapter.ProcessViewHolder> (DiffCallBack) {


    class ProcessViewHolder private constructor (val binding: ProcessItemViewBinding)
        : RecyclerView.ViewHolder (binding.root) {

        companion object {
            fun from (parent: ViewGroup): ProcessViewHolder {
                val inflater = LayoutInflater.from (parent.context)
                val binding = ProcessItemViewBinding.inflate (inflater)
                return ProcessViewHolder (binding)
            }
        }

        fun bind (process: SongProcess) {
            binding.item = process
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ProcessViewHolder {
        return ProcessViewHolder.from (parent)
    }

    override fun onBindViewHolder(holder: ProcessViewHolder, position: Int) {
        val processItem = getItem (position)
        holder.bind (processItem)
    }

    companion object DiffCallBack: DiffUtil.ItemCallback <SongProcess> () {

        override fun areItemsTheSame(oldItem: SongProcess, newItem: SongProcess): Boolean {
            return oldItem == newItem
        }

        override fun areContentsTheSame(oldItem: SongProcess, newItem: SongProcess): Boolean {
            return oldItem.songName == newItem.songName
        }

    }

}
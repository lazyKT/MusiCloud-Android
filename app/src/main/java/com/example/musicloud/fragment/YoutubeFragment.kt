package com.example.musicloud.fragment

import android.os.Bundle
import android.util.Log
import android.view.KeyEvent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.musicloud.databinding.YoutubeFragmentBinding
import com.example.musicloud.databinding.YoutubeSearchItemViewBinding
import com.example.musicloud.youtubesearch.YoutubeSearchAdapter
import com.example.musicloud.youtubesearch.YoutubeSearchViewModel

class YoutubeFragment: Fragment() {

    private var _binding: YoutubeFragmentBinding? = null
    private val binding get() = _binding!!

    private val youtubeSearchViewModel: YoutubeSearchViewModel by lazy {
        ViewModelProvider(this).get (YoutubeSearchViewModel::class.java)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = YoutubeFragmentBinding.inflate(inflater)
        binding.lifecycleOwner = this
        binding.viewModel = youtubeSearchViewModel
        binding.searchResultList.adapter = YoutubeSearchAdapter()
        binding.searchResultList.layoutManager = LinearLayoutManager (requireActivity())

        return binding.root
    }


    override fun onDestroyView() {
        _binding = null
        super.onDestroyView()
    }

}
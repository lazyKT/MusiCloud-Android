package com.example.musicloud.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.ViewModelProvider
import com.example.musicloud.databinding.YoutubeSearchDetailBinding
import com.example.musicloud.song.SongViewModel
import com.example.musicloud.youtubesearch.YoutubeSearchDetailsViewModel
import com.example.musicloud.youtubesearch.YoutubeSearchDetailsViewModelFactory

class YoutubeSearchDetailsFragment: Fragment() {

    private var _binding: YoutubeSearchDetailBinding? = null
    private val binding: YoutubeSearchDetailBinding
                        get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = YoutubeSearchDetailBinding.inflate (inflater, container, false)
        val application = requireActivity().application
        val youtubeSearchResult = YoutubeSearchDetailsFragmentArgs.fromBundle (requireArguments()).selectedResultDetails

        val viewModelFactory = YoutubeSearchDetailsViewModelFactory (youtubeSearchResult, application)

        val youtubeSearchDetailsViewModel = ViewModelProvider (this, viewModelFactory).get (YoutubeSearchDetailsViewModel::class.java)
        val songViewModel: SongViewModel by activityViewModels ()


        binding.viewModel = youtubeSearchDetailsViewModel
        binding.songViewModel = songViewModel
        binding.lifecycleOwner = viewLifecycleOwner

        return binding.root
    }


    override fun onDestroyView () {
        _binding = null
        super.onDestroyView()
    }

}
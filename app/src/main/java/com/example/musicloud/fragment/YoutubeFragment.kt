package com.example.musicloud.fragment

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.musicloud.databinding.YoutubeFragmentBinding
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
        binding.searchResultList.adapter = YoutubeSearchAdapter (YoutubeSearchAdapter.OnClickListener {
            youtubeSearchViewModel.displaySearchResultDetails (it)
        })
        binding.searchResultList.layoutManager = LinearLayoutManager (requireActivity())

        youtubeSearchViewModel.navigateToDetailsPage.observe (viewLifecycleOwner, Observer {
            if (null != it) {
                this.findNavController().navigate (YoutubeFragmentDirections.actionYoutubeFragmentToYoutubeSearchDetailsFragment(it))
                youtubeSearchViewModel.displaySearchResultDetailsDone()
            }
        })

        return binding.root
    }


    override fun onDestroyView() {
        _binding = null
        super.onDestroyView()
    }

}
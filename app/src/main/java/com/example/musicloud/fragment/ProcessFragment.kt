package com.example.musicloud.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.musicloud.adapters.ProcessAdapter
import com.example.musicloud.databinding.ProcessFragmentBinding
import com.example.musicloud.song.SongViewModel
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class ProcessFragment: Fragment() {

    private var _binding : ProcessFragmentBinding? = null
    private val binding: ProcessFragmentBinding get() = _binding!!

    private val songViewModel: SongViewModel by activityViewModels ()


    @Inject
    lateinit var processAdapter: ProcessAdapter

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        _binding = ProcessFragmentBinding.inflate (inflater, container, false)

        binding.processList.apply {
            adapter = processAdapter
            layoutManager = LinearLayoutManager (requireActivity())
        }

        subscribeObservers()

        return binding.root
    }

    private fun subscribeObservers () {

        songViewModel.songs.observe (viewLifecycleOwner) {
            it?.let {
                processAdapter.songs = it
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

}
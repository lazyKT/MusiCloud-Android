package com.example.musicloud.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import com.example.musicloud.database.SongDAO
import com.example.musicloud.database.SongDatabase
import com.example.musicloud.databinding.SongDetailFragmentBinding
import com.example.musicloud.song.SongDetailsViewModel
import com.example.musicloud.song.SongDetailsViewModelFactory

class SongDetailFragment: Fragment() {

    private var _binding: SongDetailFragmentBinding? = null
    private val binding get() = _binding!!


    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        _binding = SongDetailFragmentBinding.inflate (inflater, container, false)

        val application = requireNotNull (this.requireActivity()).application
        val database: SongDAO = SongDatabase.getInstance(application).songDAO
        val id = SongDetailFragmentArgs.fromBundle (requireArguments()).songKey

        val viewModelFactory = SongDetailsViewModelFactory (database, id, application)
        val viewModel = ViewModelProvider (this, viewModelFactory).get (SongDetailsViewModel::class.java)
        binding.viewModel = viewModel
        binding.lifecycleOwner = viewLifecycleOwner

        viewModel.navigateToHomeFragmentAfterDelete.observe (viewLifecycleOwner, {
            it?.let {
                this.findNavController().navigate (
                            SongDetailFragmentDirections.actionSongDetailFragmentToHomeFragment(it)
                        )
                viewModel.onNavigatedAfterDeletion()
            }
        })

        return binding.root
    }


    override fun onDestroyView() {
        _binding = null
        super.onDestroyView()
    }

}
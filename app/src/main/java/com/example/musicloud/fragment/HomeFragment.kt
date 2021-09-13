package com.example.musicloud.fragment

import android.os.Bundle
import android.util.Log
import android.view.*
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.findNavController
import androidx.navigation.ui.NavigationUI
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.musicloud.R
import com.example.musicloud.adapters.HomeAdapter
import com.example.musicloud.databinding.HomeFragmentBinding
import com.example.musicloud.dialogs.AddNewSongDialog
import com.example.musicloud.media.Status
import com.example.musicloud.viewmodels.HomeViewModel
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject


@AndroidEntryPoint
class HomeFragment: Fragment () {

    private var _binding: HomeFragmentBinding? = null
    private val binding get() = _binding!!

    @Inject
    lateinit var homeAdapter: HomeAdapter

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = HomeFragmentBinding.inflate (inflater, container, false)
        val view = binding.root

        // include options menu
        setHasOptionsMenu (true)

        /* get reference to SongViewModel via the ViewModel Factory */
        val homeViewModel: HomeViewModel = ViewModelProvider (requireActivity()).get (HomeViewModel::class.java)

        binding.lifecycleOwner = viewLifecycleOwner
        binding.viewModel = homeViewModel


        binding.songList.apply {
            adapter = homeAdapter
            layoutManager = LinearLayoutManager (requireActivity())
        }

        homeViewModel.mediaItems.observe (viewLifecycleOwner, { result ->
            when (result.status) {
                Status.SUCCESS -> {
                    Log.i ("HomeFragment", "Songs Loaded!!")
                    result.data?.let { songs ->
                        homeAdapter.songs = songs
                    }
                }
                Status.ERROR -> Unit
                Status.LOADING -> {
                    Log.i ("HomeFragment", "Songs Loading ....")
                    Toast.makeText (requireActivity(), "Songs Loading ...", Toast.LENGTH_SHORT).show()
                }
            }
        })

        homeAdapter.setOnItemClickListener {
            homeViewModel.playOrPauseSong (it)
        }

        return view
    }


    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        super.onCreateOptionsMenu(menu, inflater)
        // inflate the menu layout
        inflater.inflate (R.menu.options_menu, menu)
    }


    override fun onOptionsItemSelected(item: MenuItem): Boolean {

        if (item.itemId == R.id.add_new_song) {
            /* show options of adding new songs to user */
            val dialog = AddNewSongDialog()
            /* used childFragmentManager to share the viewModel between parent fragment and dialog fragment */
            dialog.show (childFragmentManager, dialog.tag)
        }

        return NavigationUI.onNavDestinationSelected(item, requireView().findNavController())
                || super.onOptionsItemSelected(item)
    }

    override fun onDestroyView() {
        Log.i ("SongFragment", "onDestroyView()")
        _binding = null
        super.onDestroyView()
    }

}
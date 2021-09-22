package com.example.musicloud.fragment

import android.os.Bundle
import android.util.Log
import android.view.*
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.navigation.findNavController
import androidx.navigation.fragment.findNavController
import androidx.navigation.ui.NavigationUI
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.musicloud.R
import com.example.musicloud.adapters.HomeAdapter
import com.example.musicloud.databinding.HomeFragmentBinding
import com.example.musicloud.dialogs.AddNewSongDialog
import com.example.musicloud.media.Status
import com.example.musicloud.song.SongViewModel
import com.example.musicloud.viewmodels.HomeViewModel
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject


@AndroidEntryPoint
class HomeFragment: Fragment () {

    private var _binding: HomeFragmentBinding? = null
    private val binding get() = _binding!!

    @Inject
    lateinit var homeAdapter: HomeAdapter

    private val songViewModel: SongViewModel by activityViewModels ()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = HomeFragmentBinding.inflate (inflater, container, false)

        // include options menu
        setHasOptionsMenu (true)

        /* get reference to SongViewModel via the ViewModel Factory */
        val homeViewModel: HomeViewModel by activityViewModels ()

        binding.lifecycleOwner = viewLifecycleOwner
        binding.viewModel = homeViewModel


        binding.songList.apply {
            adapter = homeAdapter
            layoutManager = LinearLayoutManager (requireActivity())
        }

        /**
         * observing song items (playlists) changes,
         * updates UI accordingly.
         */
        homeViewModel.mediaItems.observe (viewLifecycleOwner, { result ->
            when (result.status) {
                Status.SUCCESS -> {
                    /**
                     * Check whether playlist is empty or not.
                     * If empty, show empty icon. Otherwise, playlist!!
                     */
                    if (result.data?.size?.compareTo(0) == 0) {
                        binding.playListStatusImageView.visibility = View.VISIBLE
                        binding.playListStatusImageView.setImageResource (R.drawable.ic_empty)
                    }
                    else {
                        binding.playListStatusImageView.visibility = View.GONE
                    }

                    result.data?.let { songs ->
                        homeAdapter.songs = songs
                    }
                }
                Status.ERROR -> Unit
                Status.LOADING -> {
                    binding.playListStatusImageView.visibility = View.VISIBLE
                    binding.playListStatusImageView.setImageResource (R.drawable.loading_animation)
                }
            }
        })

        homeViewModel.navigateToSongDetailsFragment.observe (viewLifecycleOwner, { songId ->
            if (songId != null) {
                this.findNavController().navigate (HomeFragmentDirections.actionHomeFragmentToSongDetailFragment(songId))
                homeViewModel.onNavigatedToSongDetailsFragment()
            }
        })


        homeAdapter.setItemClickListener {
            homeViewModel.playOrPauseSong (it)
        }

        homeAdapter.setOptionItemClickListener {
            homeViewModel.showSongDetails (it)
        }

        return binding.root
    }


    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        super.onCreateOptionsMenu(menu, inflater)
        // inflate the menu layout
        inflater.inflate (R.menu.options_menu, menu)

        val processItem = menu.findItem (R.id.track_song_process)
        val processActionView = processItem.actionView

        val badgeCount = processActionView.findViewById <TextView> (R.id.badgeCount)

        processActionView.setOnClickListener {
            Log.i ("HomeFragment", "Process Action View Clicked!")
            this.findNavController().navigate (R.id.action_homeFragment_to_processFragment)
        }

        songViewModel.processingSongs.observe (viewLifecycleOwner) { processList ->
            val numOfProcess = processList?.size ?: 0
            if (numOfProcess > 0) {
                badgeCount.text = numOfProcess.toString()
                badgeCount.visibility = View.VISIBLE
            }
            else {
                badgeCount.visibility = View.GONE
            }
        }
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {

        Log.i ("HomeFragment", "onOptionsItemSelected: ${item.itemId}")

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
        _binding = null
        super.onDestroyView()
    }

}
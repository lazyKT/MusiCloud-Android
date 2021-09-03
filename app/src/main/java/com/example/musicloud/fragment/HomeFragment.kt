package com.example.musicloud.fragment

import android.os.Bundle
import android.util.Log
import android.view.*
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.findNavController
import androidx.navigation.fragment.findNavController
import androidx.navigation.ui.NavigationUI
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.musicloud.R
import com.example.musicloud.databinding.HomeFragmentBinding
import com.example.musicloud.dialogs.AddNewSongDialog
import com.example.musicloud.song.SongAdapter
import com.example.musicloud.song.SongListener
import com.example.musicloud.song.SongViewModel
import kotlinx.coroutines.launch


class HomeFragment: Fragment () {

    private var _binding: HomeFragmentBinding? = null
    private val binding get() = _binding!!

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
        val songViewModel: SongViewModel by activityViewModels()

        binding.lifecycleOwner = viewLifecycleOwner
        binding.songViewModel = songViewModel

        val adapter = SongAdapter (SongListener { listenerActionType ->
            /* onClick Event on Song Item inside RecyclerView */
            songViewModel.onSongClicked (listenerActionType)
        })

        binding.songList.adapter = adapter
        binding.songList.layoutManager = LinearLayoutManager (requireActivity())

        songViewModel.songs.observe (viewLifecycleOwner, {
            it?.let {
                Log.i ("SongFragment", "Songs List Mutated! Size: ${it.size}")
                adapter.submitList(it) // submitList () is to tell the ListAdapter that new version of data is available
                songViewModel.setSongListStatus (it.size)
            }
        })

        /* observe the song item clicks */
        songViewModel.navigateToSongDetail.observe (viewLifecycleOwner, { song ->
            song?.let {
                this.findNavController().navigate (
                    HomeFragmentDirections.actionHomeFragmentToSongDetailFragment(song)
                )
                songViewModel.onSongDetailNavigated()
            }
        })

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
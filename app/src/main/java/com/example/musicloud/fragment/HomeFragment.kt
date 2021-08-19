package com.example.musicloud.fragment

import android.os.Bundle
import android.util.Log
import android.view.*
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.findNavController
import androidx.navigation.ui.NavigationUI
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.musicloud.R
import com.example.musicloud.database.SongDatabase
import com.example.musicloud.databinding.HomeFragmentBinding
import com.example.musicloud.dialogs.AddNewSongDialog
import com.example.musicloud.song.SongAdapter
import com.example.musicloud.song.SongViewModel
import com.example.musicloud.song.SongViewModelFactory


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

        val application = requireNotNull (this.activity).application
        val dataSource = SongDatabase.getInstance (application).songDAO
        val viewModelFactory = SongViewModelFactory (dataSource, application)

        /* get reference to SongViewModel via the ViewModel Factory */
        val songViewModel = ViewModelProvider (this, viewModelFactory).get (SongViewModel::class.java)

        binding.lifecycleOwner = viewLifecycleOwner
        binding.songViewModel = songViewModel

        val adapter = SongAdapter ()
        binding.songList.adapter = adapter
        binding.songList.layoutManager = LinearLayoutManager (application)

        songViewModel.songs.observe (viewLifecycleOwner, Observer {
            Log.i ("SongFragment", "RecyclerView Updated!")
            Log.i ("SongFragment", "Total Number of Songs ${adapter.itemCount}")
            it?.let {
                adapter.submitList(it) // submitList () is to tell the ListAdapter that new version of data is available
            }
        })

        adapter.registerAdapterDataObserver (object: RecyclerView.AdapterDataObserver() {
            override fun onChanged() {
                super.onChanged()
                binding.songList.scrollToPosition (0)
            }

            override fun onItemRangeInserted(positionStart: Int, itemCount: Int) {
                super.onItemRangeInserted(positionStart, itemCount)
                binding.songList.scrollToPosition (0)
            }

            override fun onItemRangeChanged(positionStart: Int, itemCount: Int) {
                super.onItemRangeChanged(positionStart, itemCount)
                binding.songList.scrollToPosition (0)
            }

            override fun onItemRangeMoved(fromPosition: Int, toPosition: Int, itemCount: Int) {
                super.onItemRangeMoved(fromPosition, toPosition, itemCount)
                binding.songList.scrollToPosition (0)
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
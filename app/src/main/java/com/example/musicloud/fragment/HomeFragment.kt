package com.example.musicloud.fragment

import android.os.Bundle
import android.view.*
import androidx.fragment.app.Fragment
import androidx.navigation.findNavController
import androidx.navigation.ui.NavigationUI
import com.example.musicloud.R
import com.example.musicloud.databinding.HomeFragmentBinding
import com.example.musicloud.dialogs.AddNewSongDialog

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
            dialog.show (requireActivity().supportFragmentManager, dialog.tag)
        }

        return NavigationUI.onNavDestinationSelected(item, requireView().findNavController())
                || super.onOptionsItemSelected(item)
    }


    override fun onDestroyView() {
        _binding = null
        super.onDestroyView()
    }

}
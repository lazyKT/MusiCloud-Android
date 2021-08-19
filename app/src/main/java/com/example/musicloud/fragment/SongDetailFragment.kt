package com.example.musicloud.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.example.musicloud.database.Song
import com.example.musicloud.databinding.SongDetailFragmentBinding

class SongDetailFragment: Fragment() {

    private var _binding: SongDetailFragmentBinding? = null
    private val binding get() = _binding!!


    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        _binding = SongDetailFragmentBinding.inflate (inflater, container, false)

        return binding.root
    }


    override fun onDestroyView() {
        _binding = null
        super.onDestroyView()
    }

}
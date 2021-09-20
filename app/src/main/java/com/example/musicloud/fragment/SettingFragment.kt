package com.example.musicloud.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import com.example.musicloud.databinding.SettingFragmentBinding
import com.example.musicloud.song.SongViewModel


class SettingFragment: Fragment() {

    private var _binding: SettingFragmentBinding? = null
    private val binding get() = _binding !!

    val songViewModel: SongViewModel by activityViewModels ()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = SettingFragmentBinding.inflate (inflater, container, false)

        binding.dataSaverSwitch.setOnCheckedChangeListener { _, isChecked ->
            songViewModel.toggleDataSaverMode (isChecked)
        }

        songViewModel.dataSaverModeOn.observe (viewLifecycleOwner) {
            binding.dataSaverSwitch.isChecked = it
        }

        return binding.root
    }


    override fun onDestroyView() {
        _binding = null
        super.onDestroyView()
    }

}
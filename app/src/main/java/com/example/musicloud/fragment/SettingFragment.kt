package com.example.musicloud.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.navigation.findNavController
import com.example.musicloud.R
import com.example.musicloud.databinding.SettingFragmentBinding
import kotlin.random.Random

class SettingFragment: Fragment() {

    private var _binding: SettingFragmentBinding? = null
    private val binding get() = _binding !!


    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = SettingFragmentBinding.inflate (inflater, container, false)

        return binding.root
    }


    override fun onDestroyView() {
        _binding = null
        super.onDestroyView()
    }

}
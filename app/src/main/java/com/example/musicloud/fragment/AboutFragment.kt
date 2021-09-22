package com.example.musicloud.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.example.musicloud.databinding.AboutFragmentBinding
import com.example.musicloud.viewmodels.AboutViewModel

class AboutFragment: Fragment() {

    private var _binding: AboutFragmentBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = AboutFragmentBinding.inflate (inflater, container, false)

        val aboutViewModel = ViewModelProvider(this).get (AboutViewModel::class.java)

        aboutViewModel.aboutText.observe (viewLifecycleOwner) {
            binding.aboutTextView.text = it
        }

        return binding.root
    }


    override fun onDestroyView() {
        _binding = null
        super.onDestroyView()
    }

}
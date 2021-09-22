package com.example.musicloud.fragment

import android.app.Application
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.musicloud.ViewModelFactories.HelpViewModelFactory
import com.example.musicloud.adapters.HelpAdapter
import com.example.musicloud.databinding.HelpFragmentBinding
import com.example.musicloud.support.Question
import com.example.musicloud.viewmodels.HelpViewModel

class HelpFragment: Fragment() {

    private var _binding: HelpFragmentBinding? = null
    private val binding: HelpFragmentBinding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = HelpFragmentBinding.inflate (inflater, container, false)

        val application: Application = requireActivity().application
        val helpViewModelFactory = HelpViewModelFactory (application)

        val helpViewModel = ViewModelProvider (this, helpViewModelFactory).get (HelpViewModel::class.java)

        binding.viewModel = helpViewModel

        val helpAdapter = HelpAdapter()

        helpViewModel.questions.observe (viewLifecycleOwner) {
            helpAdapter.questions = it
        }

        binding.questionList.apply {
            adapter = helpAdapter
            layoutManager = LinearLayoutManager (requireActivity())
        }

        return binding.root
    }


    override fun onDestroyView() {
        _binding = null
        super.onDestroyView()
    }

}
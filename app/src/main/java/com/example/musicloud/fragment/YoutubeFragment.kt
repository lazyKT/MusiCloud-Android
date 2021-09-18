package com.example.musicloud.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.musicloud.R
import com.example.musicloud.databinding.YoutubeFragmentBinding
import com.example.musicloud.youtubesearch.YoutubeSearchAdapter
import com.example.musicloud.youtubesearch.YoutubeSearchViewModel
import com.google.android.material.snackbar.Snackbar

class YoutubeFragment: Fragment() {

    private var _binding: YoutubeFragmentBinding? = null
    private val binding get() = _binding!!

    private val youtubeSearchViewModel: YoutubeSearchViewModel by lazy {
        ViewModelProvider(this).get (YoutubeSearchViewModel::class.java)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = YoutubeFragmentBinding.inflate(inflater)
        binding.lifecycleOwner = this
        binding.viewModel = youtubeSearchViewModel

        binding.searchResultList.adapter = YoutubeSearchAdapter (YoutubeSearchAdapter.OnClickListener {
            youtubeSearchViewModel.displaySearchResultDetails (it)
        })
        binding.searchResultList.layoutManager = LinearLayoutManager (requireActivity())

        youtubeSearchViewModel.navigateToDetailsPage.observe (viewLifecycleOwner, {
            if (null != it) {
                this.findNavController().navigate (YoutubeFragmentDirections.actionYoutubeFragmentToYoutubeSearchDetailsFragment(it))
                youtubeSearchViewModel.displaySearchResultDetailsDone()
            }
        })

        youtubeSearchViewModel.errorMessage.observe (viewLifecycleOwner, {
            it?.let {
                Snackbar.make (binding.ytSnackBar, it, Snackbar.LENGTH_INDEFINITE)
                    .setAction (R.string.close) {
//                        it.visibility = View.GONE
                    }
                    .setBackgroundTint (ContextCompat.getColor (requireActivity(), R.color.sky_blue))
                    .setActionTextColor (ContextCompat.getColor (requireActivity(), R.color.whisper))
                    .show()
            }
        })

        return binding.root
    }


    override fun onDestroyView() {
        _binding = null
        super.onDestroyView()
    }

}
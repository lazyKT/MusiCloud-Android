package com.example.musicloud.dialogs

import android.app.Dialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.DialogFragment
import com.example.musicloud.R
import com.example.musicloud.databinding.AddNewSongDialogBinding

class AddNewSongDialog: DialogFragment() {

    private var _binding: AddNewSongDialogBinding? = null
    private val binding get() = _binding!!

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {

        _binding = DataBindingUtil.inflate (LayoutInflater.from(activity), R.layout.add_new_song_dialog, null, false)
        val builder = AlertDialog.Builder (requireActivity())
        builder.setView (binding.root)

        binding.addFromLocalButton.setOnClickListener { v: View ->
            Toast.makeText (requireActivity(), getString(R.string.import_from_file), Toast.LENGTH_SHORT).show()
        }

        binding.addFromYouTubeButton.setOnClickListener {v: View ->
            Toast.makeText (requireActivity(), getString (R.string.download_from_youtube), Toast.LENGTH_SHORT).show()
        }

        return builder.create()
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = AddNewSongDialogBinding.inflate (inflater, container, false)
        return binding.root
    }

    override fun onDestroyView() {
        _binding = null
        super.onDestroyView()
    }

}
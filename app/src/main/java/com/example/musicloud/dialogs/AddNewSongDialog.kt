package com.example.musicloud.dialogs

import android.app.Dialog
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.DialogFragment
import androidx.navigation.fragment.NavHostFragment
import com.example.musicloud.R
import com.example.musicloud.databinding.AddNewSongDialogBinding


class AddNewSongDialog: DialogFragment() {

    private var _binding: AddNewSongDialogBinding? = null
    private val binding get() = _binding!!

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {

        _binding = DataBindingUtil.inflate (LayoutInflater.from(activity), R.layout.add_new_song_dialog, null, false)
        val builder = AlertDialog.Builder (requireActivity())
        builder.setView (binding.root)

        val dialog: AlertDialog = builder.create()

        binding.addFromLocalButton.setOnClickListener {
            Toast.makeText (requireActivity(), "Coming Soon", Toast.LENGTH_SHORT).show()
            dialog.dismiss()
        }

        // go to YouTube View
        binding.addFromYouTubeButton.setOnClickListener {
            dialog.dismiss()
            NavHostFragment.findNavController(this).navigate (R.id.action_homeFragment_to_youtubeFragment)
        }

        dialog.apply {
            window?.setBackgroundDrawable (ColorDrawable(Color.TRANSPARENT)) // set window background to transparent so round corners can be seen
        }

        return dialog
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
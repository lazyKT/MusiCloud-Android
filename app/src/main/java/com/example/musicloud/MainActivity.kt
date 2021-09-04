package com.example.musicloud

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.content.ContextCompat
import androidx.drawerlayout.widget.DrawerLayout
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.NavController
import androidx.navigation.findNavController
import androidx.navigation.ui.NavigationUI
import com.example.musicloud.database.SongDatabase
import com.example.musicloud.databinding.ActivityMainBinding
import com.example.musicloud.song.SongViewModel
import com.example.musicloud.song.SongViewModelFactory
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.snackbar.Snackbar

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private lateinit var drawerLayout: DrawerLayout

    private lateinit var bottomSheetBehavior: BottomSheetBehavior<ConstraintLayout>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val application = requireNotNull (this).application
        val dataSource = SongDatabase.getInstance(application).songDAO
        val viewModelFactory = SongViewModelFactory (dataSource, application)

        val songViewModel = ViewModelProvider(this, viewModelFactory).get (SongViewModel::class.java)

        binding = ActivityMainBinding.inflate (layoutInflater)
        setContentView (binding.root)

        binding.lifecycleOwner = this
        binding.viewModel = songViewModel

        drawerLayout = binding.drawerLayout
        /*
        add up button ('back button' on left side of app bar).
        This button will appear on every screen except the Home screen
        */
        val navController: NavController = this.findNavController (R.id.navHostFragment)

        NavigationUI.setupActionBarWithNavController (this, navController, drawerLayout)
        // add navigation drawer
        NavigationUI.setupWithNavController (binding.navView, navController)

        // display error message
        songViewModel.errorMessage.observe (this, {
            it?.let {
                Snackbar.make (binding.mainSnackBar, it, Snackbar.LENGTH_INDEFINITE)
                    .setAction (R.string.close) {

                    }
                    .setActionTextColor (ContextCompat.getColor (this, R.color.white))
                    .setBackgroundTint (ContextCompat.getColor (this, R.color.coral))
                    .show()
            }
        })

        bottomSheetBehavior = BottomSheetBehavior.from (binding.musicPlayer)

        binding.miniPlayer.setOnClickListener {
            bottomSheetBehavior.state =  BottomSheetBehavior.STATE_EXPANDED
        }

        binding.collapsePlayer.setOnClickListener {
            bottomSheetBehavior.state = BottomSheetBehavior.STATE_COLLAPSED
        }

        bottomSheetBehavior.addBottomSheetCallback (object: BottomSheetBehavior.BottomSheetCallback() {
            override fun onStateChanged(bottomSheet: View, newState: Int) {
                Log.i ("MainActivity", "onStateChanged: $newState")
                when (newState) {
                    BottomSheetBehavior.STATE_COLLAPSED -> {
                        binding.collapsePlayer.visibility = View.GONE
                        binding.miniPlayer.visibility = View.VISIBLE
                        binding.miniPlayer.alpha = 1F
                        binding.fullPlayer.visibility = View.GONE
                    }
                    BottomSheetBehavior.STATE_EXPANDED -> {
                        binding.collapsePlayer.visibility = View.VISIBLE
                        binding.miniPlayer.visibility = View.GONE
                        binding.collapsePlayer.alpha = 1F
                        binding.fullPlayer.visibility = View.VISIBLE
                        binding.fullPlayer.alpha = 1F
                    }
                    BottomSheetBehavior.STATE_DRAGGING -> {
                        binding.collapsePlayer.alpha = 0.3F
                        binding.fullPlayer.alpha = 0.2F
                        binding.fullPlayer.visibility = View.VISIBLE
                    }
                    BottomSheetBehavior.STATE_SETTLING -> {
                        binding.fullPlayer.alpha = 0.5F
                        binding.collapsePlayer.alpha = 0.5F
                        binding.miniPlayer.alpha = 0.1F
                    }
                }
            }

            override fun onSlide(bottomSheet: View, slideOffset: Float) {
//                Log.i ("MainActivity", "onSlide: slideOffset: $slideOffset")
            }

        })
    }

    override fun onSupportNavigateUp(): Boolean {
        val navController: NavController = this.findNavController (R.id.navHostFragment)
        return NavigationUI.navigateUp (navController, drawerLayout)
    }

}
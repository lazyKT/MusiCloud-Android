package com.example.musicloud

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.activity.viewModels
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.drawerlayout.widget.DrawerLayout
import androidx.navigation.NavController
import androidx.navigation.findNavController
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.NavigationUI
import com.bumptech.glide.RequestManager
import com.example.musicloud.database.Song
import com.example.musicloud.databinding.ActivityMainBinding
import com.example.musicloud.media.Status
import com.example.musicloud.media.isPlaying
import com.example.musicloud.viewmodels.HomeViewModel
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.snackbar.Snackbar
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private lateinit var drawerLayout: DrawerLayout

    private lateinit var bottomSheetBehavior: BottomSheetBehavior<ConstraintLayout>

    private val homeViewModel: HomeViewModel by viewModels ()

    private lateinit var currentSong: Song

    @Inject
    lateinit var glide: RequestManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityMainBinding.inflate (layoutInflater)
        setContentView (binding.root)

        binding.viewModel = homeViewModel

        setUpNavigation ()

        setUpBottomSheet ()

        subscribeObservers()
    }

    private fun setUpNavigation () {
        drawerLayout = binding.drawerLayout
        /*
        add up button ('back button' on left side of app bar).
        This button will appear on every screen except the Home screen
        */
        val navHostFragment = supportFragmentManager.findFragmentById (R.id.navHostFragment) as NavHostFragment
        val navController: NavController = navHostFragment.navController

        NavigationUI.setupActionBarWithNavController (this, navController, drawerLayout)
        // add navigation drawer
        NavigationUI.setupWithNavController (binding.navView, navController)
    }

    private fun setUpBottomSheet () {
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
                        showMiniPlayer()
                    }
                    BottomSheetBehavior.STATE_EXPANDED -> {
                        showFullPlayer()
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
                    else -> Unit
                }
            }

            override fun onSlide(bottomSheet: View, slideOffset: Float) {
//                Log.i ("MainActivity", "onSlide: slideOffset: $slideOffset")
            }

        })
    }

    private fun showMiniPlayer () {
        binding.collapsePlayer.visibility = View.GONE
        binding.miniPlayer.visibility = View.VISIBLE
        binding.miniPlayer.alpha = 1F
        binding.fullPlayer.visibility = View.GONE
    }

    private fun showFullPlayer () {
        binding.collapsePlayer.visibility = View.VISIBLE
        binding.miniPlayer.visibility = View.GONE
        binding.collapsePlayer.alpha = 1F
        binding.fullPlayer.visibility = View.VISIBLE
        binding.fullPlayer.alpha = 1F
    }

    private fun subscribeObservers () {

        homeViewModel.currentPlayingSong.observe (this) {
            if (it == null) return@observe

            /* mini bottom sheet player */
            currentSong = homeViewModel.mediaDataCompatToSong (it)!!
            binding.songTitleTextView.text = currentSong.songName
            glide.load (currentSong.thumbnailS).into (binding.songThumbnail)

            /* full player */
            binding.channelTitleTextViewFull.text = currentSong.channelTitle
            binding.songTitleTextViewFull.text = currentSong.songName
            glide.load (currentSong.thumbnailM).into (binding.songThumbnailFull)
        }

        homeViewModel.playbackState.observe (this) {
            /* mini bottom sheet player */
            binding.playPauseButtonMiniPlayer.setImageResource(
                if (it?.isPlaying == true) R.drawable.ic_pause_foreground
                    else R.drawable.play_foreground
            )

            /* full player */
            binding.playPauseButton.setImageResource (
                if (it?.isPlaying == true) R.drawable.ic_pause_foreground
                else R.drawable.ic_play_l_foreground
            )
        }

        homeViewModel.isConnected.observe (this) {
            it?.getContentIfNotHandle()?.let { result ->
                when (result.status) {
                    Status.ERROR -> {
                        // show error snack bar
                        Snackbar.make (
                            binding.mainSnackBar,
                            result.message ?: "An Unknown Error Occurred!",
                            Snackbar.LENGTH_LONG
                        ).show()
                    }
                    else -> Unit
                }
            }
        }

        homeViewModel.networkError.observe (this) {
            it?.getContentIfNotHandle()?.let { result ->
                when (result.status) {
                    Status.ERROR -> {
                        // show error snack bar
                        Snackbar.make (
                            binding.mainSnackBar,
                            result.message ?: "An Unknown Error Occurred!",
                            Snackbar.LENGTH_LONG
                        ).show()
                    }
                    else -> Unit
                }
            }
        }

    }

    override fun onSupportNavigateUp(): Boolean {
        val navController: NavController = this.findNavController (R.id.navHostFragment)
        return NavigationUI.navigateUp (navController, drawerLayout)
    }

}
package com.example.musicloud

import android.app.Application
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.SeekBar
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.drawerlayout.widget.DrawerLayout
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.NavController
import androidx.navigation.findNavController
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.NavigationUI
import com.bumptech.glide.RequestManager
import com.example.musicloud.database.Song
import com.example.musicloud.database.SongDAO
import com.example.musicloud.database.SongDatabase
import com.example.musicloud.databinding.ActivityMainBinding
import com.example.musicloud.media.Status
import com.example.musicloud.media.isPlaying
import com.example.musicloud.song.SongViewModel
import com.example.musicloud.song.SongViewModelFactory
import com.example.musicloud.viewmodels.HomeViewModel
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.snackbar.Snackbar
import dagger.hilt.android.AndroidEntryPoint
import java.text.SimpleDateFormat
import java.util.*
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private lateinit var drawerLayout: DrawerLayout

    private lateinit var bottomSheetBehavior: BottomSheetBehavior<ConstraintLayout>

    private var currentSong: Song? = null
    private var shouldUpdateSeekbar = true

    private lateinit var homeViewModel: HomeViewModel
    private lateinit var songViewModel: SongViewModel

    @Inject
    lateinit var glide: RequestManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityMainBinding.inflate (layoutInflater)
        setContentView (binding.root)

        val application: Application = this.application
        val songDAO: SongDAO = SongDatabase.getInstance (application).songDAO
        val songViewModelFactory = SongViewModelFactory (songDAO, application)

        songViewModel = ViewModelProvider (this, songViewModelFactory).get (SongViewModel::class.java)

        homeViewModel = ViewModelProvider (this).get (HomeViewModel::class.java)

        binding.viewModel = homeViewModel

        /* seekbar onDrag/onChange Event */
        binding.seekBar.setOnSeekBarChangeListener (object : SeekBar.OnSeekBarChangeListener {

            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                if (fromUser) {
                    binding.playbackTime.text = toTimeFormat (progress.toLong())
                }
            }

            override fun onStartTrackingTouch(seekBar: SeekBar?) {
                shouldUpdateSeekbar = false
            }

            override fun onStopTrackingTouch(seekBar: SeekBar?) {
                seekBar?.let {
                    homeViewModel.seekTo (it.progress.toLong())
                    shouldUpdateSeekbar = true
                }
            }

        })

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

    /* Player Bottom Sheet */
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

        songViewModel.numOfSongsAdded.observe (this) {
            songViewModel.getNewlyAddedSongs (it)
        }
        songViewModel.newSongs.observe (this) {
            if (it == null) return@observe
            homeViewModel.addNewlyAddedSongs (it)
        }

        homeViewModel.currentPlayingSong.observe (this) {
            if (it == null) {
                binding.songTitleTextView.text = "--"
                binding.songThumbnailFull.setImageResource (R.drawable.ic_logo)
                binding.songThumbnail.setImageResource (R.drawable.ic_logo)

                /* full player */
                binding.channelTitleTextViewFull.text = "--"
                binding.songTitleTextViewFull.text = "--"
                return@observe
            }

            /* mini bottom sheet player */
            currentSong = homeViewModel.mediaDataCompatToSong (it)
            currentSong?.apply {
                binding.songTitleTextView.text = songName
                glide.load (thumbnailS).into (binding.songThumbnail)

                /* full player */
                binding.channelTitleTextViewFull.text = channelTitle
                binding.songTitleTextViewFull.text = songName
                glide.load (thumbnailM).into (binding.songThumbnailFull)
            }
        }

        homeViewModel.playbackState.observe (this) {
            /* mini bottom sheet player */
            binding.playPauseButtonMiniPlayer.setImageResource(
                if (it?.isPlaying == true) R.drawable.ic_pause_foreground
                    else R.drawable.play_foreground
            )

            /* full player */
            binding.playPauseButton.setImageResource (
                if (it?.isPlaying == true) R.drawable.ic_pause_l_foreground
                else R.drawable.ic_play_l_foreground
            )

            /* update seek bar position */
            binding.seekBar.progress = it?.position?.toInt() ?: 0
        }

        /* update song duration */
        homeViewModel.playbackStateDuration.observe (this) {
            binding.songDuration.text = toTimeFormat (it)
            binding.seekBar.max = it.toInt()
        }

        /* update playback position on Seekbar */
        homeViewModel.playbackStatePosition.observe (this) {
            if (shouldUpdateSeekbar) {
                binding.seekBar.progress = it.toInt()
                binding.playbackTime.text = toTimeFormat (it)
            }
        }

        homeViewModel.isConnected.observe (this) {
            it?.getContentIfNotHandle()?.let { result ->
                when (result.status) {
                    Status.ERROR -> {
                        // show error snack bar
                        showErrorMessage (result.message)
                    }
                    else -> Unit
                }
            }
        }


        /**
         * Observer for Song Network Operations: Song Loading
         */
        homeViewModel.networkError.observe (this) {
            it?.getContentIfNotHandle()?.let { result ->
                when (result.status) {
                    Status.ERROR -> {
                        // show error snack bar
                        showErrorMessage (result.message)
                    }
                    else -> Unit
                }
            }
        }

        /**
         * Observer for Song IO Operations realated to player: Add, Remove
         */
        homeViewModel.ioError.observe (this) {
            it?.getContentIfNotHandle()?.let { result ->
                when (result.status) {
                    Status.ERROR -> {
                       showErrorMessage (result.message)
                    }
                    else -> Unit
                }
            }
        }

        /**
         * Observers for song processes operation
         */
        songViewModel.errorMessageFromRepository.observe (this) {
            if (it == null) return@observe

            showErrorMessage (it)
        }
        songViewModel.errorMessage.observe (this) {
            if (it == null) return@observe

            showErrorMessage (it)
        }

        /**
         * Show Alert
         */
        songViewModel.userAlert.observe (this) { alertMessage ->
            if (alertMessage == null) return@observe
            showErrorMessage (alertMessage)
        }

    }

    private fun toTimeFormat (ms: Long): String {
        val dateFormat = SimpleDateFormat ("mm:ss", Locale.getDefault())
        return dateFormat.format (ms - (30 * 60L * 1000))
    }

    private fun showErrorMessage (message: String?) {
        Snackbar.make (
            binding.mainSnackBar,
            message ?: "An Unknown Error Occurred!",
            Snackbar.LENGTH_INDEFINITE
        )
            .setAction (R.string.cancel) {
            }
            .show()
    }

    override fun onSupportNavigateUp(): Boolean {
        val navController: NavController = this.findNavController (R.id.navHostFragment)
        return NavigationUI.navigateUp (navController, drawerLayout)
    }

}
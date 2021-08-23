package com.example.musicloud

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.ImageButton
import android.widget.Toast
import androidx.constraintlayout.widget.ConstraintLayout
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

        drawerLayout = binding.drawerLayout
        /*
        add up button ('back button' on left side of app bar).
        This button will appear on every screen except the Home screen
        */
        val navController: NavController = this.findNavController (R.id.navHostFragment)

        NavigationUI.setupActionBarWithNavController (this, navController, drawerLayout)
        // add navigation drawer
        NavigationUI.setupWithNavController (binding.navView, navController)

        val bottomSheetView = findViewById<ConstraintLayout> (R.id.musicPlayer)

        bottomSheetBehavior = BottomSheetBehavior.from (bottomSheetView)
        bottomSheetBehavior.state = BottomSheetBehavior.STATE_COLLAPSED

        val playButton: ImageButton = bottomSheetView.findViewById (R.id.playButton)
        playButton.setOnClickListener {_ ->
            Toast.makeText (this, "Play Clicked!", Toast.LENGTH_SHORT).show()
        }

    }

    override fun onSupportNavigateUp(): Boolean {
        val navController: NavController = this.findNavController (R.id.navHostFragment)
        return NavigationUI.navigateUp (navController, drawerLayout)
    }

}
package com.gpetuhov.android.hive

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.NavController
import androidx.navigation.findNavController
import androidx.navigation.ui.setupWithNavController
import kotlinx.android.synthetic.main.activity_main.*

class MainActivity : AppCompatActivity() {

    lateinit var navController: NavController

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // Find NavController
        navController = findNavController(R.id.nav_host)

        // Tie NavHostFragment to bottom navigation bar
        navigationView.setupWithNavController(navController)
    }

    override fun onSupportNavigateUp() = navController.navigateUp()
}

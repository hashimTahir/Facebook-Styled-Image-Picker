package com.hashim.filespickerrunner

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.NavController
import androidx.navigation.fragment.NavHostFragment
import com.hashim.filespickerrunner.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {
    private var hMainBinding: ActivityMainBinding? = null
    private lateinit var hNavHostFragments: NavHostFragment
    private lateinit var hNavController: NavController


    override fun onCreate(savedInstanceState: Bundle?) {

        super.onCreate(savedInstanceState)
        hMainBinding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(hMainBinding?.root)

        hInitNavView()
    }

    private fun hInitNavView() {
        hNavHostFragments = supportFragmentManager
            .findFragmentById(R.id.hMainFragmentContainer)
                as NavHostFragment

        hNavController = hNavHostFragments.navController

        hNavController.setGraph(R.navigation.main_nav)
    }


    override fun onDestroy() {
        super.onDestroy()
        hMainBinding = null
    }
}
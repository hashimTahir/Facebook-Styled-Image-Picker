package com.hashim.filespickerrunner

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.NavController
import androidx.navigation.fragment.NavHostFragment
import com.hashim.filespickerrunner.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {
    private var hMainBinding: ActivityMainBinding? = null
    private var hNavHostFragment: NavHostFragment? = null
    private var hNavController: NavController? = null


    override fun onCreate(savedInstanceState: Bundle?) {

        super.onCreate(savedInstanceState)
        hMainBinding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(hMainBinding?.root)

        hInitNavView()
    }

    private fun hInitNavView() {
        hNavHostFragment = supportFragmentManager
            .findFragmentById(R.id.hMainFragmentContainer)
                as NavHostFragment

        hNavController = hNavHostFragment?.navController

        hNavController?.setGraph(R.navigation.main_nav)
    }

    override fun onBackPressed() {
        if (hNavController?.currentDestination?.id == R.id.hDisplayFragment)
            hNavController?.popBackStack()
        else
            super.onBackPressed()
    }


    override fun onDestroy() {
        super.onDestroy()
        hMainBinding = null
        hNavController = null
        hNavHostFragment = null
    }
}
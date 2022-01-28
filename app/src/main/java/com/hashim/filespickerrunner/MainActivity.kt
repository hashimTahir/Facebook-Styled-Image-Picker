package com.hashim.filespickerrunner

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.hashim.filespicker.gallerymodule.Constants
import com.hashim.filespicker.gallerymodule.activity.GalleryActivity
import com.hashim.filespicker.gallerymodule.data.IntentHolder
import com.hashim.filespickerrunner.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {
    private lateinit var hMainBinding: ActivityMainBinding
    private lateinit var hDisplayAdapter: DisplayAdapter


    private val hGalleryActivityLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            val hRecieviedImagesList =
                result.data?.extras?.getParcelableArrayList<IntentHolder>(Constants.H_IMAGE_LIST_IC)
            hDisplayAdapter.hSetData(hRecieviedImagesList)
        }

    }

    override fun onCreate(savedInstanceState: Bundle?) {

        super.onCreate(savedInstanceState)
        hMainBinding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(hMainBinding.root)

        hInitRecyclerView()

        hMainBinding.hOpenGalleryB.setOnClickListener {
            hGalleryActivityLauncher.launch(
                Intent(
                    this,
                    GalleryActivity::class.java
                )
            )
        }
    }


    private fun hInitRecyclerView() {
        hDisplayAdapter = DisplayAdapter()

        hMainBinding.hDisplayRv.apply {
            layoutManager = LinearLayoutManager(context)
            adapter = hDisplayAdapter
        }

    }
}
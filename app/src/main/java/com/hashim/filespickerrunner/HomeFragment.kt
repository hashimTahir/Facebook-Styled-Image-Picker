package com.hashim.filespickerrunner

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.hashim.filespicker.gallerymodule.Constants
import com.hashim.filespicker.gallerymodule.Constants.Companion.H_GET_IMAGES
import com.hashim.filespicker.gallerymodule.Constants.Companion.H_GET_VIDEOS
import com.hashim.filespicker.gallerymodule.activity.GalleryActivity
import com.hashim.filespicker.gallerymodule.data.IntentHolder
import com.hashim.filespickerrunner.Constants.Companion.H_DATA_IC
import com.hashim.filespickerrunner.databinding.FragmentHomeLayoutBinding

class HomeFragment : Fragment() {

    private var hFragmentHomeLayoutBinding: FragmentHomeLayoutBinding? = null

    private val hGalleryActivityLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            val hIntentHolder1 = result.data?.extras?.getParcelable<IntentHolder>(H_GET_VIDEOS)
            val hIntentHolder2 = result.data?.extras?.getParcelable<IntentHolder>(H_GET_IMAGES)

            val hBundle = Bundle()
            if (hIntentHolder1 != null) {
                hBundle.putParcelable(H_DATA_IC, hIntentHolder1)
            }
            if (hIntentHolder2 != null) {
                hBundle.putParcelable(H_DATA_IC, hIntentHolder2)
            }

            findNavController().navigate(R.id.action_hHomeFragment_to_hDisplayFragment, hBundle)
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        hFragmentHomeLayoutBinding = FragmentHomeLayoutBinding.inflate(
            layoutInflater,
            container,
            false
        )
        return hFragmentHomeLayoutBinding?.root
    }

    override fun onViewCreated(
        view: View,
        savedInstanceState: Bundle?
    ) {


        hFragmentHomeLayoutBinding?.apply {
            hGetImages.setOnClickListener {
                hGalleryActivityLauncher.launch(
                    Intent(
                        requireContext(),
                        GalleryActivity::class.java
                    ).also {
                        it.putExtra(H_GET_IMAGES, "")
                    }
                )
            }
            hGetVideos.setOnClickListener {
                hGalleryActivityLauncher.launch(
                    Intent(
                        requireContext(),
                        GalleryActivity::class.java
                    ).also {
                        it.putExtra(H_GET_VIDEOS, "")
                    }
                )
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        hFragmentHomeLayoutBinding = null
    }
}
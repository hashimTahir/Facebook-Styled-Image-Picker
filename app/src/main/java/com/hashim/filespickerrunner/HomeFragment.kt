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
import com.hashim.filespicker.gallerymodule.FileType
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
            val hIntentHolder = when {
                result.data?.extras?.containsKey(FileType.Images.toString()) == true -> {
                    result.data?.extras?.getParcelable(FileType.Images.toString())
                }
                result.data?.extras?.containsKey(FileType.Audios.toString()) == true -> {
                    result.data?.extras?.getParcelable(FileType.Audios.toString())
                }
                result.data?.extras?.containsKey(FileType.Videos.toString()) == true -> {
                    result.data?.extras?.getParcelable<IntentHolder>(FileType.Videos.toString())
                }
                else -> null
            }

            val hBundle = Bundle()
            hBundle.putParcelable(H_DATA_IC, hIntentHolder)

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
                        it.putExtra(FileType.Images.toString(), "")
                    }
                )
            }
            hGetVideos.setOnClickListener {
                hGalleryActivityLauncher.launch(
                    Intent(
                        requireContext(),
                        GalleryActivity::class.java
                    ).also {
                        it.putExtra(FileType.Videos.toString(), "")
                    }
                )
            }
            hGetAudios.setOnClickListener {
                hGalleryActivityLauncher.launch(
                    Intent(
                        requireContext(),
                        GalleryActivity::class.java
                    ).also {
                        it.putExtra(FileType.Audios.toString(), "")
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
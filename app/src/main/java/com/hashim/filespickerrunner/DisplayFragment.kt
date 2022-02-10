package com.hashim.filespickerrunner

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.hashim.filespicker.gallerymodule.data.ImageIh
import com.hashim.filespicker.gallerymodule.data.IntentHolder
import com.hashim.filespicker.gallerymodule.data.VideoIh
import com.hashim.filespickerrunner.Constants.Companion.H_DATA_IC
import com.hashim.filespickerrunner.databinding.FragmentDisplayLayoutBinding

class DisplayFragment : Fragment() {

    private var hFragmentDisplayLayoutBinding: FragmentDisplayLayoutBinding? = null
    private lateinit var hDisplayAdapter: DisplayAdapter
    private lateinit var hVideoAdapter: VideoAdapter
    private var hIntentHolder: IntentHolder? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        hIntentHolder = arguments?.getParcelable(H_DATA_IC)
    }

    private fun hInitImageAdapter(list: List<ImageIh>) {
        hFragmentDisplayLayoutBinding?.apply {
            hExoPlayerRv.visibility = View.GONE
            hDisplayRv.visibility = View.VISIBLE
        }
        hDisplayAdapter = DisplayAdapter()

        hFragmentDisplayLayoutBinding?.hDisplayRv?.apply {
            layoutManager = LinearLayoutManager(context)
            adapter = hDisplayAdapter
        }
        hDisplayAdapter.hSetData(list)

    }

    private fun hInitVideoAdapter(list: List<VideoIh>) {
        hFragmentDisplayLayoutBinding?.apply {
            hExoPlayerRv.visibility = View.VISIBLE
            hDisplayRv.visibility = View.GONE
        }

        hVideoAdapter = VideoAdapter()

        hFragmentDisplayLayoutBinding?.hExoPlayerRv?.apply {
            layoutManager = LinearLayoutManager(context)
            adapter = hVideoAdapter
        }
        hVideoAdapter.hSetData(list)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        hFragmentDisplayLayoutBinding = FragmentDisplayLayoutBinding.inflate(
            layoutInflater,
            container,
            false
        )
        return hFragmentDisplayLayoutBinding?.root
    }

    override fun onViewCreated(
        view: View,
        savedInstanceState: Bundle?
    ) {
        super.onViewCreated(view, savedInstanceState)

        when {
            hIntentHolder?.hImageList.isNullOrEmpty().not() -> hInitImageAdapter(hIntentHolder?.hImageList!!)
//            hIntentHolder?.hAudioList.isNullOrEmpty().not() -> hInitVideoAdapter(hIntentHolder?.hVideosList!!)
            hIntentHolder?.hVideoList.isNullOrEmpty().not() -> hInitVideoAdapter(hIntentHolder?.hVideoList!!)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        hFragmentDisplayLayoutBinding = null
        hIntentHolder = null
    }
}
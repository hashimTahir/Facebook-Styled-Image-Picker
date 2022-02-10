package com.hashim.filespicker.gallerymodule.fragments.gallery

import android.media.AudioAttributes
import android.media.MediaPlayer
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.net.toUri
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.flowWithLifecycle
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import com.hashim.filespicker.databinding.FragmentGalleryMainBinding
import com.hashim.filespicker.gallerymodule.FileType
import com.hashim.filespicker.gallerymodule.GalleryMainStateView
import com.hashim.filespicker.gallerymodule.GalleryViewModel
import com.hashim.filespicker.gallerymodule.GalleryVs.*
import com.hashim.filespicker.gallerymodule.data.PositionHolder
import kotlinx.coroutines.flow.collectLatest

class GalleryMainFragment : Fragment(), FilesAdapter.FilesAdapterCallbacks {
    private var hFragmentGalleryMainBinding: FragmentGalleryMainBinding? = null
    private val hGalleryViewModel by activityViewModels<GalleryViewModel>()
    private var hFilesAdapter: FilesAdapter? = null
    private var hMediaPlayer: MediaPlayer? = null
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        hFragmentGalleryMainBinding = FragmentGalleryMainBinding.inflate(
            layoutInflater,
            container,
            false
        )
        return hFragmentGalleryMainBinding!!.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        hInitRecyclerView()

        hSubscribeObservers()
    }

    private fun hInitRecyclerView() {
        if (hFilesAdapter == null) {
            hFilesAdapter = FilesAdapter().apply {
                hSetFilesAdapterCallbacks(this@GalleryMainFragment)
            }
        }


        val hLayoutManager = when (hGalleryViewModel.hGetFileType()) {
            FileType.Audios -> LinearLayoutManager(requireActivity())
            else -> GridLayoutManager(requireActivity(), 3)
        }
        hFragmentGalleryMainBinding?.hMainRv?.apply {
            layoutManager = hLayoutManager
            adapter = hFilesAdapter
            itemAnimator = null
        }
    }

    private fun hSubscribeObservers() {

        viewLifecycleOwner.lifecycleScope.launchWhenCreated {
            hGalleryViewModel.hGalleryVsSF.flowWithLifecycle(
                lifecycle,
                Lifecycle.State.STARTED
            ).collectLatest { galleryVs ->
                when (galleryVs) {
                    is OnFilesRetrieved -> hSetupView(onFilesRetrieved = galleryVs)
                    is OnUpdateAdapter -> hFilesAdapter?.hUpdate(galleryVs)
                    is OnViewSetup -> hSetupView(onViewSetup = galleryVs)
                    else -> Unit
                }
            }
        }
    }


    private fun hSetupView(
        onFilesRetrieved: OnFilesRetrieved? = null,
        onViewSetup: OnViewSetup? = null,
    ) {
        onViewSetup?.hCheckImageList?.let {
            hFilesAdapter?.hSetData(it)
        }
        onFilesRetrieved?.hFilesList?.let {
            hFilesAdapter?.hSetData(it)
        }

    }


    override fun onDestroyView() {
        super.onDestroyView()
        hFragmentGalleryMainBinding = null
        hReleaseMediaPlayer()
    }

    override fun hOnUpdateCount(positionHolder: PositionHolder, position: Int) {
        hGalleryViewModel.hOnGalleryMainFragment(
            GalleryMainStateView.OnAddFiles(
                hPostionHolder = positionHolder,
                hPosition = position,
            )
        )
    }

    override fun hOnPlayAudio(hUri: String) {
        hReleaseMediaPlayer()
        hMediaPlayer = MediaPlayer().apply {
            setAudioAttributes(
                AudioAttributes.Builder()
                    .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
                    .setUsage(AudioAttributes.USAGE_MEDIA)
                    .build()
            )
            setDataSource(requireActivity().applicationContext, hUri.toUri())
            prepare()
            start()
        }
    }

    private fun hReleaseMediaPlayer() {
        if (hMediaPlayer != null) {
            hMediaPlayer?.release()
            hMediaPlayer = null
        }
    }

}
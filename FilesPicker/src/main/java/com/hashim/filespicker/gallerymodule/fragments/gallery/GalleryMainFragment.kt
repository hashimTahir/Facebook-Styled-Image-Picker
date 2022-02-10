package com.hashim.filespicker.gallerymodule.fragments.gallery

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.flowWithLifecycle
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import com.hashim.filespicker.databinding.FragmentGalleryMainBinding
import com.hashim.filespicker.gallerymodule.FileType
import com.hashim.filespicker.gallerymodule.GalleryMainStateView.OnAddFiles
import com.hashim.filespicker.gallerymodule.GalleryViewModel
import com.hashim.filespicker.gallerymodule.GalleryVs.*
import kotlinx.coroutines.flow.collectLatest

class GalleryMainFragment : Fragment() {
    private var hFragmentGalleryMainBinding: FragmentGalleryMainBinding? = null
    private val hGalleryViewModel by activityViewModels<GalleryViewModel>()
    private var hFilesAdapter: FilesAdapter? = null
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
            hFilesAdapter = FilesAdapter { hCheckedImage, position ->
                hGalleryViewModel.hOnGalleryMainFragment(
                    OnAddFiles(
                        hPostionHolder = hCheckedImage,
                        hPosition = position,
                    )
                )
            }
        }


        val hLayoutManager = when (hGalleryViewModel.hGetFileType()) {
            FileType.Audios -> LinearLayoutManager(requireActivity())
            else -> GridLayoutManager(requireActivity(), 3)
        }
        hFragmentGalleryMainBinding?.hMainRv?.apply {
            layoutManager = hLayoutManager
            adapter = hFilesAdapter
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
    }

}
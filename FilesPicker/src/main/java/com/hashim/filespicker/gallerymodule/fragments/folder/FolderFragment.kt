package com.hashim.filespicker.gallerymodule.fragments.folder

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.flowWithLifecycle
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.hashim.filespicker.databinding.FragmentFolderBinding
import com.hashim.filespicker.gallerymodule.FolderStateView.OnChangeFolder
import com.hashim.filespicker.gallerymodule.FolderStateView.OnShowFolders
import com.hashim.filespicker.gallerymodule.GalleryViewModel
import com.hashim.filespicker.gallerymodule.GalleryVs.OnFolderChanged
import kotlinx.coroutines.flow.collectLatest

class FolderFragment : Fragment() {

    private var hFragmentFolderBinding: FragmentFolderBinding? = null
    private lateinit var hFolderAdapter: FolderAdapter
    private val hGalleryViewModel by activityViewModels<GalleryViewModel>()


    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        hFragmentFolderBinding = FragmentFolderBinding.inflate(
            inflater,
            container,
            false
        )
        return hFragmentFolderBinding!!.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        hInitRecyclerView()

        hGalleryViewModel.hOnFolderEvents(OnShowFolders)

        hSubscribeObservers()
    }

    private fun hSubscribeObservers() {
        lifecycleScope.launchWhenCreated {

            hGalleryViewModel.hGalleryVsSF.flowWithLifecycle(
                lifecycle,
                Lifecycle.State.STARTED
            )
                .collectLatest { galleryVs ->
                    when (galleryVs) {
                        is OnFolderChanged -> hSetupFolders(galleryVs)
                        else -> Unit
                    }
                }

        }
    }

    private fun hInitRecyclerView() {
        hFolderAdapter = FolderAdapter {
            hGalleryViewModel.hOnFolderEvents(
                OnChangeFolder(
                    hFolder = it
                )
            )
            findNavController().popBackStack()
        }

        hFragmentFolderBinding?.hFolderRv?.apply {
            layoutManager = LinearLayoutManager(context)
            adapter = hFolderAdapter
        }

    }


    private fun hSetupFolders(galleryVs: OnFolderChanged) {
        galleryVs.hFoldersList?.let {
            hFolderAdapter.hSetData(it, galleryVs.hHighListFoldersList)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        hFragmentFolderBinding = null
    }
}
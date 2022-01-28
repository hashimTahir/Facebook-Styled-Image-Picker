package com.hashim.filespicker.gallerymodule

import android.net.Uri
import com.hashim.filespicker.gallerymodule.data.CheckedImage
import com.hashim.filespicker.gallerymodule.data.GalleryFolders
import com.hashim.filespicker.gallerymodule.data.IntentHolder
import com.hashim.filespicker.gallerymodule.data.PositionHolder

sealed class GalleryVs {
    data class OnFilesRetrieved(
        val hFoldersList: List<GalleryFolders>? = null,
        val hImagesList: List<CheckedImage>? = null,
        val hFolderName: String? = null
    ) : GalleryVs()


    data class OnFolderChanged(
        val hFoldersList: List<GalleryFolders>? = null,
        val hHighListFoldersList: List<Long>,
    ) : GalleryVs()

    object StateNone : GalleryVs()

    data class OnViewSetup(
        val hImagesList: List<CheckedImage>? = null,
    ) : GalleryVs()

    data class OnSelectionDone(
        val hSelectedImagesList: List<IntentHolder>
    ) : GalleryVs()

    data class OnUpdateActivity(
        val hShowNextB: Boolean = false
    ) : GalleryVs()

    data class OnUpdateAdapter(
        val hPosition: Int,
        val hPositionsList: List<PositionHolder>?,
    ) : GalleryVs()

    data class OnLaunchCamera(val hPhotoUri: Uri) : GalleryVs()

}


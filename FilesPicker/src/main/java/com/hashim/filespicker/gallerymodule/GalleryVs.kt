package com.hashim.filespicker.gallerymodule

import android.net.Uri
import com.hashim.filespicker.gallerymodule.data.CheckedFile
import com.hashim.filespicker.gallerymodule.data.Folder
import com.hashim.filespicker.gallerymodule.data.IntentHolder
import com.hashim.filespicker.gallerymodule.data.PositionHolder

sealed class GalleryVs {
    data class OnFilesRetrieved(
        val hFoldersList: List<Folder>? = null,
        val hFilesList: List<CheckedFile>? = null,
        val hFolderName: String? = null
    ) : GalleryVs()


    data class OnFolderChanged(
        val hFoldersList: List<Folder>? = null,
        val hHighListFoldersList: List<Long>,
    ) : GalleryVs()

    object StateNone : GalleryVs()

    data class OnViewSetup(
        val hIsMultipleSelected: Boolean = false,
        val hToobarTitle: String? = null,
        val hCheckImageList: List<CheckedFile>?=null,
    ) : GalleryVs()

    data class OnSelectionDone(
        val hIntentHolder: IntentHolder
    ) : GalleryVs()

    data class OnUpdateActivity(
        val hShowNextB: Boolean = false
    ) : GalleryVs()

    data class OnUpdateAdapter(
        val hPosition: Int,
        val hPositionsList: List<PositionHolder>?,
    ) : GalleryVs()

    data class OnLaunchCamera(val hPhotoUri: Uri) : GalleryVs()

    data class OnLoadingOrError(
        val hMessage: String? = null,
        val hShowLoader: Boolean = false
    ) : GalleryVs()


}


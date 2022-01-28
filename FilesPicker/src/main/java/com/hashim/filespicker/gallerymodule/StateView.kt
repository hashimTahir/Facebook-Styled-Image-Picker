package com.hashim.filespicker.gallerymodule

import com.hashim.filespicker.gallerymodule.data.GalleryFolders
import com.hashim.filespicker.gallerymodule.data.PositionHolder

sealed class GalleryStateView {
    data class OnPictureTaken(val hIsSuccessFull: Boolean) : GalleryStateView()

    object OnReloadFiles : GalleryStateView()

    object OnCameraSelected : GalleryStateView()

    object OnMultiSelection : GalleryStateView()

    object OnNextClicked : GalleryStateView()


}

sealed class GalleryMainStateView {

    data class OnAddFiles(
        val hPostionHolder: PositionHolder?,
        val hPosition: Int,

        ) : GalleryMainStateView()

}

sealed class FolderStateView {

    data class OnChangeFolder(
        val hFolder: GalleryFolders,
    ) : FolderStateView()

    object OnShowFolders : FolderStateView()

}
package com.hashim.filespicker.gallerymodule

import android.os.Bundle
import com.hashim.filespicker.gallerymodule.data.Folder
import com.hashim.filespicker.gallerymodule.data.PositionHolder

sealed class GalleryActivitySv {
    data class OnPictureTaken(val hIsSuccessFull: Boolean) : GalleryActivitySv()

    object OnReloadFiles : GalleryActivitySv()

    object OnCameraSelected : GalleryActivitySv()

    object OnMultiSelection : GalleryActivitySv()

    object OnNextClicked : GalleryActivitySv()

    data class OnSetData(
        val hBundle: Bundle? = null
    ) : GalleryActivitySv()


}

sealed class GalleryMainStateView {

    data class OnAddFiles(
        val hPostionHolder: PositionHolder?,
        val hPosition: Int,

        ) : GalleryMainStateView()

}

sealed class FolderStateView {

    data class OnChangeFolder(
        val hFolder: Folder,
    ) : FolderStateView()

    object OnShowFolders : FolderStateView()

}
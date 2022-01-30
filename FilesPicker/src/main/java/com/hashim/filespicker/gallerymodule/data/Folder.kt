package com.hashim.filespicker.gallerymodule.data

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

sealed class Folder {
    var hFolderName: String? = null
    var hFolderId: Long? = null

    @Parcelize
    data class VideoFolder(
        val hVideoItemsList: MutableList<VideoItem> = mutableListOf()
    ) : Folder(), Parcelable


    @Parcelize
    data class VideoItem(
        var hFilePath: String? = null,
        val hUri: String? = null,
        var hFileName: String? = null,
        var hFileSize: String? = null,
        var hFileDuaration: String? = null,
        var hModifiedDate: Long? = null,
        var hFileSizeForOrder: String? = null,
        var hFileDateTime: String? = null
    ) : Parcelable


    @Parcelize
    data class ImageFolder(
        val hImageItemsList: MutableList<ImageItem> = mutableListOf()
    ) : Folder(), Parcelable


    @Parcelize
    data class ImageItem(
        val hItemName: String? = null,
        val hSize: String? = null,
        var hImagePath: String? = null,
        var hImageUri: String? = null,
    ) : Parcelable

}
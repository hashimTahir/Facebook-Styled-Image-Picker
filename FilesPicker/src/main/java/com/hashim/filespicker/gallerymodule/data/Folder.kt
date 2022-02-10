package com.hashim.filespicker.gallerymodule.data

import android.graphics.Bitmap
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
    data class ImageFolder(
        val hImageItemsList: MutableList<ImageItem> = mutableListOf()
    ) : Folder(), Parcelable

    @Parcelize
    data class AudioFolder(
        val hAudioItemsList: MutableList<AudioItem> = mutableListOf()
    ) : Folder(), Parcelable


    @Parcelize
    open class Item(
        var hFileName: String? = null,
        var hFilePath: String? = null,
        var hUri: String? = null,
        var hFileSize: String? = null,
    ) : Parcelable


    @Parcelize
    data class AudioItem(
        val hTitle: String? = null,
        var hMime: String? = null,
        var hDateModified: String? = null,
        val hAlbumArt: Bitmap? = null
    ) : Item(), Parcelable

    @Parcelize
    data class ImageItem(
        var hModifiedDate: Long? = null,
    ) : Item(), Parcelable


    @Parcelize
    data class VideoItem(
        var hFileDuaration: String? = null,
        var hModifiedDate: Long? = null,
        var hFileSizeForOrder: String? = null,
        var hFileDateTime: String? = null
    ) : Item(), Parcelable

}
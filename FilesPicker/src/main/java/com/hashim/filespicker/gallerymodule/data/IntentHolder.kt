package com.hashim.filespicker.gallerymodule.data

import android.graphics.Bitmap
import android.os.Parcelable
import kotlinx.parcelize.Parcelize


@Parcelize
data class IntentHolder(
    var hImageList: List<ImageIh>? = null,
    var hAudioList: List<AudioIh>? = null,
    var hVideoList: List<VideoIh>? = null,
) : Parcelable


@Parcelize
data class AudioIh(
    var hFileName: String? = null,
    var hFilePath: String? = null,
    var hUri: String? = null,
    var hFileSize: String? = null,
    val hTitle: String? = null,
    var hMime: String? = null,
    var hDateModified: String? = null,
    val hAlbumArt: Bitmap? = null
) : Parcelable

@Parcelize
data class ImageIh(
    var hFileName: String? = null,
    var hFilePath: String? = null,
    var hUri: String? = null,
    var hFileSize: String? = null,
    var hModifiedDate: Long? = null,
) : Parcelable


@Parcelize
data class VideoIh(
    var hFileName: String? = null,
    var hFilePath: String? = null,
    var hUri: String? = null,
    var hFileSize: String? = null,
    var hFileDuaration: String? = null,
    var hModifiedDate: Long? = null,
    var hFileSizeForOrder: String? = null,
    var hFileDateTime: String? = null
) : Parcelable
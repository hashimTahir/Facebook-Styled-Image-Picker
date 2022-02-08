package com.hashim.filespicker.gallerymodule.data

import android.os.Parcelable
import kotlinx.parcelize.Parcelize


@Parcelize
data class IntentHolder(
    var hVideosList: List<Folder.VideoItem>? = null,
    var hImageList: List<Folder.ImageItem>? = null,
    var hAudioList: List<Folder.AudioFolder>? = null,
) : Parcelable
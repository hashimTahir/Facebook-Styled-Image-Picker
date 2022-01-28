package com.hashim.filespicker.gallerymodule.data

import android.os.Parcelable
import kotlinx.parcelize.Parcelize


@Parcelize
data class IntentHolder(
    val hImagePath: String? = null,
    val hImageUri: String? = null
) : Parcelable
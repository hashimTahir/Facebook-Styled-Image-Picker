package com.hashim.filespicker.gallerymodule.data

import com.hashim.filespicker.gallerymodule.FileType

data class CheckedFile(
    val hItem: Folder.Item? = null,
    val hIsCheck: Boolean = false,
    val hFileType: FileType? = null
) : Folder.Item()
package com.hashim.filespicker.gallerymodule.data


data class GalleryFolders(
    val hItemName: String? = null,
    val hFolderName: String? = null,
    val hFolderId: Long? = null,
    val hSize: String? = null,
    var hImagePathsList: MutableList<String> = mutableListOf(),
    var hImageUrisList: MutableList<String> = mutableListOf(),
)
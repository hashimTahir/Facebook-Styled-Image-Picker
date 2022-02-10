package com.hashim.filespicker.gallerymodule

import android.net.Uri
import com.hashim.filespicker.gallerymodule.data.*
import java.io.File

fun Folder.AudioItem.hMap(): AudioIh {
    return AudioIh(
        hFileName = hFileName,
        hFilePath = hFilePath,
        hUri = hUri,
        hFileSize = hFileSize,
        hTitle = hTitle,
        hMime = hMime,
        hDateModified = hDateModified,
        hAlbumArt = hAlbumArt
    )

}

fun Folder.VideoItem.hMap(): VideoIh {
    return VideoIh(
        hFileName = hFileName,
        hFilePath = hFilePath,
        hUri = hUri,
        hFileSize = hFileSize,
        hFileDuaration = hFileDuaration,
        hModifiedDate = hModifiedDate,
        hFileSizeForOrder = hFileSizeForOrder,
        hFileDateTime = hFileDateTime
    )
}

fun Folder.ImageItem.hMap(): ImageIh {
    return ImageIh(
        hFileName = hFileName,
        hFilePath = hFilePath,
        hUri = hUri,
        hFileSize = hFileSize,
        hModifiedDate = hModifiedDate,
    )
}


fun Folder.hMapToCheckedItemList(hIsCheck: Boolean, hFileType: FileType): List<CheckedFile> {
    return when (this) {
        is Folder.AudioFolder -> this.hAudioItemsList.map {
            hMapToCheckedFile(it, hIsCheck, hFileType)
        }
        is Folder.ImageFolder -> this.hImageItemsList.map {
            hMapToCheckedFile(it, hIsCheck, hFileType)
        }
        is Folder.VideoFolder -> this.hVideoItemsList.map {
            hMapToCheckedFile(it, hIsCheck, hFileType)
        }
    }
}

private fun hMapToCheckedFile(
    videoItem: Folder.VideoItem, hIsCheck: Boolean, hFileType: FileType
): CheckedFile {
    return CheckedFile(
        hItem = videoItem,
        hIsCheck = hIsCheck,
        hFileType = hFileType
    )
}

private fun hMapToCheckedFile(
    imageItem: Folder.ImageItem,
    hIsCheck: Boolean,
    hFileType: FileType
): CheckedFile {
    return CheckedFile(
        hItem = imageItem,
        hIsCheck = hIsCheck,
        hFileType = hFileType
    )
}


private fun hMapToCheckedFile(
    audioItem: Folder.AudioItem,
    hIsCheck: Boolean,
    hFileType: FileType
): CheckedFile {
    return CheckedFile(
        hItem = audioItem,
        hIsCheck = hIsCheck,
        hFileType = hFileType
    )
}


fun hMapItemToFolder(
    hImageItem: Folder.ImageItem? = null,
    hVideoItem: Folder.VideoItem? = null,
    hAudioItem: Folder.AudioItem? = null,
    hGalleryFolder: Folder? = null,
    uri: Uri? = null,
    hFile: File? = null
): Folder? {
    return when {
        hImageItem != null -> {
            Folder.ImageFolder().apply {
                hFolderId = hGalleryFolder?.hFolderId
                hFolderName = hGalleryFolder?.hFolderName
                hImageItemsList.add(hImageItem)
            }
        }
        hVideoItem != null -> {
            Folder.VideoFolder().apply {
                hFolderId = hGalleryFolder?.hFolderId
                hFolderName = hGalleryFolder?.hFolderName
                hVideoItemsList.add(hVideoItem)
            }
        }
        hAudioItem != null -> {
            Folder.AudioFolder().apply {
                hFolderId = hGalleryFolder?.hFolderId
                hFolderName = hGalleryFolder?.hFolderName
                hAudioItemsList.add(hAudioItem)
            }
        }
        uri != null -> {

            Folder.ImageFolder().apply {
                Folder.ImageItem().apply {
                    hFileName = hFile?.name
                    hFileSize = hFile?.length().toString()
                    hFilePath = hFile?.absolutePath
                    hUri = uri.toString()
                }
                hImageItemsList.add(
                    Folder.ImageItem(

                    )
                )
            }
        }
        else -> {
            null
        }
    }
}


fun Folder.hExtractOnlySelectedFiles(hPosition: Int): Folder {
    return when (this) {
        is Folder.ImageFolder -> {
            val hImageItem = hImageItemsList[hPosition]
            hMapItemToFolder(
                hImageItem = hImageItem,
                hGalleryFolder = this@hExtractOnlySelectedFiles,
            )!!
        }
        is Folder.VideoFolder -> {
            val hVideoItem = hVideoItemsList[hPosition]
            hMapItemToFolder(
                hVideoItem = hVideoItem,
                hGalleryFolder = this@hExtractOnlySelectedFiles,
            )!!

        }
        is Folder.AudioFolder -> {
            val hAudioItem = hAudioItemsList[hPosition]
            hMapItemToFolder(
                hAudioItem = hAudioItem,
                hGalleryFolder = this@hExtractOnlySelectedFiles,
            )!!
        }
    }
}


fun MutableList<Folder>.hMapToOutput(hFileType: FileType): IntentHolder {
    val hIntentHolder = IntentHolder()
    when (hFileType) {
        FileType.Images -> {
            val hImageList = mutableListOf<ImageIh>()
            forEach { hFolerItem ->
                when (hFolerItem) {
                    is Folder.ImageFolder -> hFolerItem.hImageItemsList.map {
                        hImageList.add(it.hMap())
                    }
                    else -> Unit
                }
            }
            hIntentHolder.hImageList = hImageList
        }
        FileType.Videos -> {
            val hVideoList = mutableListOf<VideoIh>()

            forEach { hFolerItem ->
                when (hFolerItem) {
                    is Folder.VideoFolder -> hFolerItem.hVideoItemsList.map {
                        hVideoList.add(it.hMap())
                    }
                    else -> Unit
                }
            }
            hIntentHolder.hVideoList = hVideoList
        }
        FileType.Audios -> {
            val hAudioList = mutableListOf<AudioIh>()
            forEach { hFolerItem ->
                when (hFolerItem) {
                    is Folder.AudioFolder -> hFolerItem.hAudioItemsList.map {
                        hAudioList.add(it.hMap())
                    }
                    else -> Unit
                }
            }
            hIntentHolder.hAudioList = hAudioList
        }
        FileType.None -> Unit
    }
    return hIntentHolder


}





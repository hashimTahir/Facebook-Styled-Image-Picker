package com.hashim.filespicker.gallerymodule

import android.content.ContentUris
import android.content.Context
import android.net.Uri
import android.provider.MediaStore
import com.hashim.filespicker.gallerymodule.data.GalleryFolders
import timber.log.Timber


object GalleryFilesFetcher {
    fun hFetchGalleryFiles(hContext: Context): MutableList<GalleryFolders> {
        val hGalleryMap = HashMap<Long, GalleryFolders>()
        val hProjection = arrayOf(
            MediaStore.Images.Media._ID,
            MediaStore.Images.Media.BUCKET_ID,
            MediaStore.Images.Media.BUCKET_DISPLAY_NAME,
            MediaStore.Images.Media.SIZE,
            MediaStore.Images.Media.DISPLAY_NAME,
            MediaStore.Images.Media.DATA,
        )

        val hImagesUri = MediaStore.Images.Media.EXTERNAL_CONTENT_URI
        try {
            val hContentResolver = hContext.contentResolver
            hContentResolver.query(
                hImagesUri,
                hProjection,
                "",
                null,
                ""
            ).use { hCursor ->
                val hIdColumn = hCursor?.getColumnIndexOrThrow(MediaStore.Images.Media._ID)
                val hBucketIdCol = hCursor?.getColumnIndexOrThrow(MediaStore.Images.Media.BUCKET_ID)
                val hBucketDisplayNameCol = hCursor?.getColumnIndexOrThrow(
                    MediaStore.Images.Media.BUCKET_DISPLAY_NAME
                )
                val hDisplayNameCol = hCursor?.getColumnIndexOrThrow(
                    MediaStore.Images.Media.DISPLAY_NAME
                )
                val hSizeCol = hCursor?.getColumnIndexOrThrow(
                    MediaStore.Images.Media.SIZE
                )

                val hDataCol = hCursor?.getColumnIndexOrThrow(
                    MediaStore.Images.Media.DATA
                )



                while (hCursor?.moveToNext() == true) {
                    val hId = hIdColumn?.let { hCursor.getLong(it) }
                    val hBucketDisplayName = hBucketDisplayNameCol?.let { hCursor.getString(it) }
                    val hBucketId = hBucketIdCol?.let { hCursor.getLong(it) }
                    val hDisplayName = hDisplayNameCol?.let { hCursor.getString(it) }
                    val hSize = hSizeCol?.let { hCursor.getString(it) }
                    val hPath = hDataCol?.let { hCursor.getString(it) }


                    val hContentUri: Uri? = hId?.let {
                        ContentUris.withAppendedId(
                            MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
                            it
                        )
                    }

                    GalleryFolders(
                        hItemName = hDisplayName,
                        hFolderName = hBucketDisplayName,
                        hFolderId = hBucketId,
                        hSize = hSize,
                    ).also { galleryItem ->

                        var hGalleryItem = hGalleryMap[galleryItem.hFolderId]
                        if (hGalleryItem == null) {
                            hGalleryItem = galleryItem
                            hGalleryItem.hImagePathsList.add(hPath.toString())
                            hGalleryItem.hImageUrisList.add(hContentUri.toString())
                            hGalleryMap[galleryItem.hFolderId!!] = hGalleryItem
                        } else {
                            hGalleryItem.hImagePathsList.add(hPath.toString())
                            hGalleryItem.hImageUrisList.add(hContentUri.toString())
                        }
                    }
                }
                hCursor?.close()
            }
        } catch (ex: Exception) {
            Timber.d("Exception ${ex.message}")
        }
        return hGalleryMap.values.toMutableList()
    }
}
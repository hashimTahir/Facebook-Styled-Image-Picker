package com.hashim.filespicker.gallerymodule

import android.content.ContentUris
import android.content.Context
import android.database.Cursor
import android.media.MediaMetadataRetriever
import android.net.Uri
import android.provider.MediaStore
import com.hashim.filespicker.gallerymodule.data.Folder
import timber.log.Timber
import java.io.File
import java.text.SimpleDateFormat
import java.util.*


object GalleryFilesFetcher {
    private val hVideosMap = mutableMapOf<Long, Folder>()
    private val hImagesMap = mutableMapOf<Long, Folder>()


    fun hFetchImages(hContext: Context): List<Folder.ImageFolder> {
        try {
            val hContentResolver = hContext.contentResolver
            hContentResolver.query(
                hGetMainUr(ProjectionType.Image),
                hGetProjection(ProjectionType.Image),
                "",
                null,
                ""
            ).use { hCursor ->
                hMapData(
                    hProjectionType = ProjectionType.Image,
                    hCursor = hCursor,
                    hContext = hContext,
                )
                hCursor?.close()
            }
        } catch (ex: Exception) {
            Timber.d("Exception  ${ex.message}")
        }
        return hImagesMap.values.filterIsInstance<Folder.ImageFolder>()
    }


    fun hFetchVideos(hContext: Context): List<Folder> {
        try {
            val hContentResolver = hContext.contentResolver
            hContentResolver.query(
                hGetMainUr(ProjectionType.Video),
                hGetProjection(ProjectionType.Video),
                "",
                null,
                ""
            ).use { hCursor ->
                hMapData(
                    ProjectionType.Video,
                    hCursor,
                    hContext,
                )
                hCursor?.close()
            }
        } catch (ex: Exception) {
            Timber.d("Exception ${ex.message}")
        }
        return hVideosMap.values.filterIsInstance<Folder.VideoFolder>()
    }

    private fun hMapData(
        hProjectionType: ProjectionType,
        hCursor: Cursor?,
        hContext: Context,
    ) {


        when (hProjectionType) {
            ProjectionType.Image -> hExtractImagesData(hCursor)
            ProjectionType.Video -> hExtractVideosData(hCursor, hContext)
        }
    }

    private fun hExtractVideosData(hCursor: Cursor?, hContext: Context) {

        val hIdColumn = hCursor?.getColumnIndexOrThrow(MediaStore.Video.Media._ID)
        val hBucketIdCol = hCursor?.getColumnIndexOrThrow(MediaStore.Video.Media.BUCKET_ID)
        val hBucketDisplayNameCol = hCursor?.getColumnIndexOrThrow(
            MediaStore.Video.Media.BUCKET_DISPLAY_NAME
        )
        val hDisplayNameCol = hCursor?.getColumnIndexOrThrow(
            MediaStore.Video.Media.DISPLAY_NAME
        )
        val hSizeCol = hCursor?.getColumnIndexOrThrow(
            MediaStore.Video.Media.SIZE
        )

        val hDataCol = hCursor?.getColumnIndexOrThrow(
            MediaStore.Video.Media.DATA
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
                    hGetMainUr(ProjectionType.Video),
                    it
                )
            }


            hPath?.let {
                File(it).apply {
                    val hFileDuration = hGetDuration(hContentUri, hContext)
                    val hLastModifiedData = hFormatDate(this.lastModified())


                    val hFolder = Folder.VideoFolder()
                    hFolder.hFolderName = hBucketDisplayName
                    hFolder.hFolderId = hBucketId

                    hFolder.also { hVideoFolder ->
                        val hCheckVideoFolder = hVideosMap[hVideoFolder.hFolderId!!]
                        if (hCheckVideoFolder == null) {

                            val hCheckedCastedFolder: Folder.VideoFolder = hVideoFolder

                            hCreateVideoItem(
                                hPath,
                                hDisplayName,
                                hSize,
                                hFileDuration,
                                this,
                                hLastModifiedData.toString(),
                                hContentUri
                            ).also { hVideoItem ->
                                hCheckedCastedFolder.hVideoItemsList.add(hVideoItem)
                            }
                            hVideosMap[hVideoFolder.hFolderId!!] = hCheckedCastedFolder
                        } else {
                            val hCheckedCastedFolder: Folder.VideoFolder = hCheckVideoFolder as Folder.VideoFolder
                            hCreateVideoItem(
                                hPath,
                                hDisplayName,
                                hSize,
                                hFileDuration,
                                this,
                                hLastModifiedData.toString(),
                                hContentUri
                            ).also { hVideoItem ->
                                hCheckedCastedFolder.hVideoItemsList.add(hVideoItem)
                            }
                        }
                    }
                }
            }
        }
    }

    private fun hExtractImagesData(hCursor: Cursor?) {

        val hIdColumn = hCursor?.getColumnIndexOrThrow(
            MediaStore.Images.Media._ID
        )
        val hBucketIdCol = hCursor?.getColumnIndexOrThrow(
            MediaStore.Images.Media.BUCKET_ID
        )
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

            val hImageFolder = Folder.ImageFolder()
            hImageFolder.hFolderId = hBucketId
            hImageFolder.hFolderName = hBucketDisplayName
            hImageFolder.also { imageFolder ->

                var hTempFolder: Folder.ImageFolder? = hImagesMap[imageFolder.hFolderId]
                        as Folder.ImageFolder?

                if (hTempFolder == null) {
                    hTempFolder = imageFolder

                    hCreateImageItem(
                        hPath,
                        hDisplayName,
                        hSize,
                        hContentUri
                    ).apply {
                        hTempFolder.hImageItemsList.add(this)
                    }
                    hImagesMap[imageFolder.hFolderId!!] = hTempFolder
                } else {
                    hCreateImageItem(
                        hPath,
                        hDisplayName,
                        hSize,
                        hContentUri
                    ).apply {
                        hTempFolder.hImageItemsList.add(this)
                    }
                }
            }
        }
    }

    private fun hCreateImageItem(
        hPath: String?,
        hDisplayName: String?,
        hSize: String?,
        hContentUri: Uri?
    ): Folder.ImageItem {
        return Folder.ImageItem(
            hItemName = hDisplayName,
            hSize = hSize,
            hImagePath = hPath,
            hImageUri = hContentUri.toString()
        )
    }

    private fun hCreateVideoItem(
        hPath: String?,
        hDisplayName: String?,
        hSize: String?,
        hFileDuration: String?,
        hFile: File,
        hLastModifiedData: String,
        hContentUri: Uri?
    ): Folder.VideoItem {
        return Folder.VideoItem(
            hFilePath = hPath,
            hFileName = hDisplayName,
            hFileSize = hSize,
            hFileDuaration = hFileDuration,
            hModifiedDate = hFile.lastModified(),
            hFileSizeForOrder = hFile.length().toString(),
            hFileDateTime = hLastModifiedData,
            hUri = hContentUri.toString()
        )
    }

    private fun hFormatDate(hLastModified: Long?): String? {
        if (hLastModified != null) {
            val hHrsMinSecsFormat = "dd/MMM/yyyy hh:mm a"
            return SimpleDateFormat(hHrsMinSecsFormat, Locale.getDefault()).format(hLastModified)
        }

        return null
    }

    private fun hGetDuration(hPath: Uri?, context: Context): String? {

        hPath?.let {
            context.contentResolver.openFileDescriptor(it, "r")?.use {
                MediaMetadataRetriever().apply {
                    setDataSource(it.fileDescriptor)
                    val hDurationLong = extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION)?.toLong()
                    val hHrsMinSecsFormat = "HH:mm:ss"
                    return SimpleDateFormat(hHrsMinSecsFormat, Locale.getDefault()).format(hDurationLong)

                }
            }
        }
        return null
    }

}

private fun hGetMainUr(hProjectionType: ProjectionType): Uri {
    return when (hProjectionType) {
        is ProjectionType.Image -> MediaStore.Images.Media.EXTERNAL_CONTENT_URI
        is ProjectionType.Video -> MediaStore.Video.Media.EXTERNAL_CONTENT_URI
    }
}

private fun hGetProjection(hProjectionType: ProjectionType): Array<String> {
    return when (hProjectionType) {
        is ProjectionType.Video -> arrayOf(
            MediaStore.Video.Media._ID,
            MediaStore.Video.Media.BUCKET_ID,
            MediaStore.Video.Media.BUCKET_DISPLAY_NAME,
            MediaStore.Video.Media.SIZE,
            MediaStore.Video.Media.DISPLAY_NAME,
            MediaStore.Video.Media.DATA,
        )
        is ProjectionType.Image -> arrayOf(
            MediaStore.Images.Media._ID,
            MediaStore.Images.Media.BUCKET_ID,
            MediaStore.Images.Media.BUCKET_DISPLAY_NAME,
            MediaStore.Images.Media.SIZE,
            MediaStore.Images.Media.DISPLAY_NAME,
            MediaStore.Images.Media.DATA,
        )

    }
}


sealed class ProjectionType {
    object Image : ProjectionType()
    object Video : ProjectionType()
}

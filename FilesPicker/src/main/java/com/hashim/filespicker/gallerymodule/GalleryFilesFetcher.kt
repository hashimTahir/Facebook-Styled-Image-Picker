package com.hashim.filespicker.gallerymodule

import android.content.ContentUris
import android.content.Context
import android.database.Cursor
import android.media.MediaMetadataRetriever
import android.net.Uri
import android.os.Build
import android.provider.MediaStore
import com.hashim.filespicker.gallerymodule.Constants.Companion.hDateMonthYearHrsMinFormat
import com.hashim.filespicker.gallerymodule.Constants.Companion.hHrsMinSecsFormat
import com.hashim.filespicker.gallerymodule.data.Folder
import timber.log.Timber
import java.io.File
import java.text.SimpleDateFormat
import java.util.*


object GalleryFilesFetcher {
    private val hVideosMap = mutableMapOf<Long, Folder>()
    private val hImagesMap = mutableMapOf<Long, Folder>()
    private val hAudioMap = mutableMapOf<Long, Folder>()


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


    fun hFetchAudios(hContext: Context): List<Folder.AudioFolder> {
        try {
            val hContentResolver = hContext.contentResolver
            hContentResolver.query(
                hGetMainUr(ProjectionType.Audio),
                hGetProjection(ProjectionType.Audio),
                "",
                null,
                ""
            ).use { hCursor ->
                hMapData(
                    hProjectionType = ProjectionType.Audio,
                    hCursor = hCursor,
                    hContext = hContext,
                )
                hCursor?.close()
            }
        } catch (ex: Exception) {
            Timber.d("Exception  ${ex.message}")
        }
        return hAudioMap.values.filterIsInstance<Folder.AudioFolder>()
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
            ProjectionType.Audio -> hExtractAudiosData(hCursor)
        }
    }

    private fun hExtractAudiosData(hCursor: Cursor?) {
        val hIdColumn = hCursor?.getColumnIndexOrThrow(MediaStore.Audio.Media._ID)

        var hBucketIdCol: Int? = null
        var hBucketDisplayNameCol: Int? = null

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            hBucketIdCol = hCursor?.getColumnIndexOrThrow(MediaStore.Audio.Media.BUCKET_ID)
            hBucketDisplayNameCol = hCursor?.getColumnIndexOrThrow(
                MediaStore.Audio.Media.BUCKET_DISPLAY_NAME
            )
        }

        val hDisplayNameCol = hCursor?.getColumnIndexOrThrow(
            MediaStore.Audio.Media.DISPLAY_NAME
        )
        val hSizeCol = hCursor?.getColumnIndexOrThrow(
            MediaStore.Audio.Media.SIZE
        )

        val hPathCol = hCursor?.getColumnIndexOrThrow(
            MediaStore.Audio.Media.DATA
        )
        val hMimeCol = hCursor?.getColumnIndexOrThrow(
            MediaStore.Audio.Media.MIME_TYPE
        )

        val hTitleCol = hCursor?.getColumnIndexOrThrow(
            MediaStore.Audio.Media.TITLE
        )
        val hDateCol = hCursor?.getColumnIndexOrThrow(
            MediaStore.Audio.Media.DATE_MODIFIED
        )

        while (hCursor?.moveToNext() == true) {
            val hId = hIdColumn?.let {
                hCursor.getLong(it)
            }
            val hBucketDisplayName = hBucketDisplayNameCol?.let {
                hCursor.getString(it)
            }
            val hBucketId = hBucketIdCol?.let {
                hCursor.getLong(it)
            }
            val hDisplayName = hDisplayNameCol?.let {
                hCursor.getString(it)
            }
            val hSize = hSizeCol?.let {
                hCursor.getString(it)
            }
            val hPath = hPathCol?.let {
                hCursor.getString(it)
            }
            val hTitle = hTitleCol?.let {
                hCursor.getString(it)
            }
            val hMime = hMimeCol?.let {
                hCursor.getString(it)
            }
            val hDate = hDateCol?.let {
                hCursor.getLong(it)
            }

            val hContentUri: Uri? = hId?.let {
                ContentUris.withAppendedId(
                    hGetMainUr(ProjectionType.Audio),
                    it
                )
            }

            val hFolder = Folder.AudioFolder()
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                hFolder.hFolderName = hBucketDisplayName
                hFolder.hFolderId = hBucketId
            } else {
                hFolder.hFolderName = Constants.H_ALL_FILES
                hFolder.hFolderId = Constants.H_PRE_ANDROID_10_ID
            }

            hCreateAudioItem(
                hUri = hContentUri,
                hDisplayName = hDisplayName,
                hSize = hSize,
                hPath = hPath,
                hTitle = hTitle,
                hMime = hMime,
                hDateModified = hFormatDate(hDate)
            ).also { hAudioItem ->
                val hCheckMapFolder = hAudioMap[hFolder.hFolderId!!]
                if (hCheckMapFolder != null) {
                    (hCheckMapFolder as Folder.AudioFolder).hAudioItemsList.add(hAudioItem)
                } else {
                    hFolder.hAudioItemsList.add(hAudioItem)
                    hAudioMap[hFolder.hFolderId!!] = hFolder
                }
            }


        }
    }

    private fun hCreateAudioItem(
        hUri: Uri?,
        hDisplayName: String?,
        hSize: String?,
        hPath: String?,
        hTitle: String?,
        hMime: String?,
        hDateModified: String?
    ): Folder.AudioItem {
        return Folder.AudioItem(
            hPath = hPath,
            hName = hDisplayName,
            hSize = hSize,
            hUri = hUri.toString(),
            hMime = hMime,
            hDateModified = hDateModified,
            hTitle = hTitle
        )
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

                    hCreateVideoItem(
                        hPath,
                        hDisplayName,
                        hSize,
                        hFileDuration,
                        this,
                        hLastModifiedData.toString(),
                        hContentUri
                    ).also { hVideoItem ->
                        val hCheckMapFolder = hVideosMap[hFolder.hFolderId!!]
                        if (hCheckMapFolder != null) {
                            (hCheckMapFolder as Folder.VideoFolder).hVideoItemsList.add(hVideoItem)
                        } else {
                            hFolder.hVideoItemsList.add(hVideoItem)
                            hVideosMap[hFolder.hFolderId!!] = hFolder
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
                    hGetMainUr(ProjectionType.Image),
                    it
                )
            }

            val hImageFolder = Folder.ImageFolder()
            hImageFolder.hFolderId = hBucketId
            hImageFolder.hFolderName = hBucketDisplayName
            hCreateImageItem(
                hPath,
                hDisplayName,
                hSize,
                hContentUri
            ).also { hFolderItem ->
                val hCheckFolderMap = hImagesMap[hImageFolder.hFolderId!!]
                if (hCheckFolderMap != null) {
                    (hCheckFolderMap as Folder.ImageFolder).hImageItemsList.add(hFolderItem)
                } else {
                    hImageFolder.hImageItemsList.add(hFolderItem)
                    hImagesMap[hImageFolder.hFolderId!!] = hImageFolder
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
            return SimpleDateFormat(hDateMonthYearHrsMinFormat, Locale.getDefault()).format(hLastModified)
        }

        return null
    }

    private fun hGetDuration(hPath: Uri?, context: Context): String? {

        hPath?.let {
            context.contentResolver.openFileDescriptor(it, "r")?.use {
                MediaMetadataRetriever().apply {
                    setDataSource(it.fileDescriptor)
                    val hDurationLong = extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION)?.toLong()
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
        is ProjectionType.Audio -> MediaStore.Audio.Media.EXTERNAL_CONTENT_URI
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
        is ProjectionType.Audio -> {
            val hAudioProjectionList = arrayListOf<String>()
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                hAudioProjectionList.apply {
                    add(MediaStore.Audio.Media.BUCKET_ID)
                    add(MediaStore.Audio.Media.BUCKET_DISPLAY_NAME)
                }
            }
            hAudioProjectionList.apply {
                add(MediaStore.Audio.Media._ID)
                add(MediaStore.Audio.Media.DATA)
                add(MediaStore.Audio.Media.MIME_TYPE)
                add(MediaStore.Audio.Media.DISPLAY_NAME)
                add(MediaStore.Audio.Media.SIZE)
                add(MediaStore.Audio.Media.TITLE)
                add(MediaStore.Audio.Media.DATE_MODIFIED)
            }
            hAudioProjectionList.toTypedArray()
        }
    }
}


sealed class ProjectionType {
    object Image : ProjectionType()
    object Video : ProjectionType()
    object Audio : ProjectionType()
}

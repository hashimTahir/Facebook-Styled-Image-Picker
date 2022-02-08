package com.hashim.filespicker.gallerymodule

import android.app.Application
import android.net.Uri
import android.os.Bundle
import android.os.Environment
import androidx.core.content.FileProvider
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.hashim.filespicker.gallerymodule.FolderStateView.OnChangeFolder
import com.hashim.filespicker.gallerymodule.FolderStateView.OnShowFolders
import com.hashim.filespicker.gallerymodule.GalleryMainStateView.OnAddFiles
import com.hashim.filespicker.gallerymodule.GalleryStateView.*
import com.hashim.filespicker.gallerymodule.GalleryVs.*
import com.hashim.filespicker.gallerymodule.data.CheckedImage
import com.hashim.filespicker.gallerymodule.data.Folder
import com.hashim.filespicker.gallerymodule.data.IntentHolder
import com.hashim.filespicker.gallerymodule.data.PositionHolder
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.io.File
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.*

class GalleryViewModel(
    hApplication: Application
) : AndroidViewModel(hApplication) {

    private val hGalleryVsMSF = MutableStateFlow<GalleryVs>(value = StateNone)
    val hGalleryVsSF = hGalleryVsMSF.asStateFlow()
    private var hIsMultipleSelected = false

    private var hSelectedFolder: Folder? = null
    private var hFolderList = mutableListOf<Folder>()

    private val hUsersSelectedFiles = mutableListOf<Folder>()

    private var hFileType: FileType? = null
    private val hSelectedFoldersPositionsMap = mutableMapOf<Long, MutableMap<Boolean, MutableList<PositionHolder>>>()

    fun hFetchFiles(extras: Bundle?) {

        viewModelScope.launch(Dispatchers.IO) {
            extras?.let {
                hFetchWhichFiles(it)
            }
        }

    }

    private suspend fun hFetchWhichFiles(bundle: Bundle) {
        hEmitLoader(hShowLoader = true)



        hFileType = when {
            bundle.containsKey(FileType.Images.toString()) -> FileType.Images
            bundle.containsKey(FileType.Videos.toString()) -> FileType.Videos
            bundle.containsKey(FileType.Audios.toString()) -> FileType.Audios
            else -> null
        }

        hEmitMultipleSelectionView()

        hFetchFiles()

    }

    private fun hEmitLoader(
        hMessage: String? = null,
        hShowLoader: Boolean = false
    ) {
        hGalleryVsMSF.value = OnLoadingOrError(
            hMessage = hMessage,
            hShowLoader = hShowLoader
        )
    }


    private suspend fun hFetchFiles() {
        val hCheckImageList = when (hFileType) {

            FileType.Audios -> {
                hFolderList = GalleryFilesFetcher.hFetchAudios(getApplication()).toMutableList()
                hSelectedFolder = hFolderList[0]
                (hSelectedFolder as Folder.AudioFolder).hAudioItemsList.map {
                    CheckedImage(
                        hImage = it.hUri.toString(),
                        hIsCheck = hIsMultipleSelected
                    )
                }
            }
            FileType.Images -> {
                hFolderList = GalleryFilesFetcher.hFetchImages(getApplication()).toMutableList()
                hSelectedFolder = hFolderList[0]
                (hSelectedFolder as Folder.ImageFolder).hImageItemsList.map {
                    CheckedImage(
                        hImage = it.hImageUri.toString(),
                        hIsCheck = hIsMultipleSelected
                    )
                }
            }
            FileType.Videos -> {
                hFolderList = GalleryFilesFetcher.hFetchVideos(getApplication()).toMutableList()
                hSelectedFolder = hFolderList[0]
                (hSelectedFolder as Folder.VideoFolder).hVideoItemsList.map {
                    CheckedImage(
                        hImage = it.hUri.toString(),
                        hIsCheck = hIsMultipleSelected
                    )
                }
            }
            else -> null
        }

        if (hFolderList.isNotEmpty()) {
            hGalleryVsMSF.value = OnFilesRetrieved(
                hFoldersList = hFolderList,
                hImagesList = hCheckImageList,
                hFolderName = hSelectedFolder?.hFolderName
            )
        }

        delay(30)

        hEmitLoader()
    }

    private suspend fun hEmitMultipleSelectionView() {
        var hCheckImageList: List<CheckedImage>? = null
        if (hSelectedFolder != null) {
            hCheckImageList = when (hFileType) {

                FileType.Audios -> {
                    hSelectedFolder?.let {
                        (it as Folder.AudioFolder).hAudioItemsList.map {
                            CheckedImage(
                                hImage = it.hUri.toString(),
                                hIsCheck = hIsMultipleSelected
                            )
                        }
                    }

                }
                FileType.Images -> {
                    hSelectedFolder?.let {
                        (it as Folder.ImageFolder).hImageItemsList.map {
                            CheckedImage(
                                hImage = it.hImageUri.toString(),
                                hIsCheck = hIsMultipleSelected
                            )
                        }
                    }

                }
                FileType.Videos -> {
                    hSelectedFolder?.let {
                        (it as Folder.VideoFolder).hVideoItemsList.map {
                            CheckedImage(
                                hImage = it.hUri.toString(),
                                hIsCheck = hIsMultipleSelected
                            )
                        }
                    }
                }
                null -> null
            }
        }


        hGalleryVsMSF.value = OnViewSetup(
            hImagesList = hCheckImageList,
            hIsMultipleSelected = hIsMultipleSelected,
            hToobarTitle = when (hFileType) {
                FileType.Audios -> "Pick Audios"
                FileType.Images -> "Pick Images"
                FileType.Videos -> "Pick Videos"
                null -> null
            }
        )
        hEmitUpdateActivity()

    }


    private suspend fun hSetSelected() {
        hIsMultipleSelected = !hIsMultipleSelected
        if (hIsMultipleSelected.not()) {
            hUsersSelectedFiles.clear()
            hSelectedFoldersPositionsMap.clear()
        }
        hEmitMultipleSelectionView()


    }

    private fun hReloadFiles() {
        val hCheckImageList = when (hFileType) {

            FileType.Audios -> {
                hSelectedFolder?.let {
                    (it as Folder.AudioFolder).hAudioItemsList.map {
                        CheckedImage(
                            hImage = it.hUri.toString(),
                            hIsCheck = hIsMultipleSelected
                        )
                    }
                }
            }
            FileType.Images -> {
                hSelectedFolder?.let {
                    (it as Folder.ImageFolder).hImageItemsList.map {
                        CheckedImage(
                            hImage = it.hImageUri.toString(),
                            hIsCheck = hIsMultipleSelected
                        )
                    }
                }
            }
            FileType.Videos -> {
                hSelectedFolder?.let {
                    (it as Folder.VideoFolder).hVideoItemsList.map {
                        CheckedImage(
                            hImage = it.hUri.toString(),
                            hIsCheck = hIsMultipleSelected
                        )
                    }
                }
            }
            null -> null
        }



        hGalleryVsMSF.value = OnFilesRetrieved(
            hFoldersList = hFolderList,
            hImagesList = hCheckImageList,
            hFolderName = hSelectedFolder?.hFolderName
        )
    }


    fun hOnActivityEvents(galleryStateView: GalleryStateView) {
        when (galleryStateView) {
            OnCameraSelected -> hOnCreateCameraIntent()
            OnMultiSelection -> viewModelScope.launch {
                hSetSelected()
            }
            OnNextClicked -> hFindFilesCloseFragment()
            OnReloadFiles -> hReloadFiles()
            is OnPictureTaken -> hCheckCameraResult(galleryStateView.hIsSuccessFull)
        }


    }

    private fun hCheckCameraResult(hIsSuccessFull: Boolean) {
        when {
            hIsSuccessFull -> {
                hEmitOnSeletionDone()
            }
            else -> {
                hEmitLoader(hMessage = "Error retrieving files from camera")
            }
        }
    }

    private fun hOnCreateCameraIntent() {
        val hPhotoFile: File? = try {
            hCreateImageFile()
        } catch (ex: IOException) {

            null
        }
        hPhotoFile?.also { hFile ->
            val hPhotoUri: Uri = FileProvider.getUriForFile(
                getApplication(),
                "${getApplication<Application>().packageName}.provider",
                hFile
            )

            hCreateFolder(
                hUri = hPhotoUri,
                hFile = hFile
            )?.let {
                hUsersSelectedFiles.add(it)

            }

            hGalleryVsMSF.value = OnLaunchCamera(hPhotoUri)
        }
    }

    private fun hCreateImageFile(): File {
        val hTimeStamp: String = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())
        val hStorageDir: File? =
            getApplication<Application>().getExternalFilesDir(Environment.DIRECTORY_PICTURES)
        return File.createTempFile(
            "JPEG_${hTimeStamp}_",
            ".jpg",
            hStorageDir
        ).apply {
            absolutePath
        }
    }

    private fun hFindFilesCloseFragment() {

        hSelectedFoldersPositionsMap.keys.forEach { hFolderId ->
            hFolderList.find {
                hFolderId == it.hFolderId
            }?.also { hGalleryFolder ->
                hSelectedFoldersPositionsMap[hFolderId]?.get(true)?.forEach { hPositionHolder ->

                    when (hGalleryFolder) {
                        is Folder.ImageFolder -> hGalleryFolder.hImageItemsList[hPositionHolder.hPosition].apply {
                            hCreateFolder(
                                hImageItem = this,
                                hGalleryFolder = hGalleryFolder,
                            )?.let {
                                hUsersSelectedFiles.add(it)
                            }
                        }
                        is Folder.VideoFolder -> hGalleryFolder.hVideoItemsList[hPositionHolder.hPosition].apply {
                            hCreateFolder(
                                hVideoItem = this,
                                hGalleryFolder = hGalleryFolder,
                            )?.let {
                                hUsersSelectedFiles.add(it)
                            }
                        }
                    }
                }
            }
        }
        hEmitOnSeletionDone()
    }

    private fun hCreateFolder(
        hImageItem: Folder.ImageItem? = null,
        hVideoItem: Folder.VideoItem? = null,
        hGalleryFolder: Folder? = null,
        hUri: Uri? = null,
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
            hUri != null -> {
                Folder.ImageFolder().apply {
                    hImageItemsList.add(
                        Folder.ImageItem(
                            hItemName = hFile?.name,
                            hSize = hFile?.length().toString(),
                            hImagePath = hFile?.absolutePath,
                            hImageUri = hUri.toString()
                        )
                    )
                }
            }
            else -> {
                null
            }
        }
    }

    private fun hEmitOnSeletionDone() {
        val hIntentHolder = IntentHolder()
        val hImageList = mutableListOf<Folder.ImageItem>()
        val hVideoList = mutableListOf<Folder.VideoItem>()
        hUsersSelectedFiles.forEach {
            when (it) {
                is Folder.ImageFolder -> hImageList.add(it.hImageItemsList[0])
                is Folder.VideoFolder -> hVideoList.add(it.hVideoItemsList[0])
            }
        }
        hIntentHolder.hImageList = hImageList
        hIntentHolder.hVideosList = hVideoList
        hGalleryVsMSF.value = OnSelectionDone(
            hIntentHolder = hIntentHolder
        )
    }

    fun hOnGalleryMainFragment(galleryMainStateView: GalleryMainStateView) {
        when (galleryMainStateView) {
            is OnAddFiles -> viewModelScope.launch {
                hAddRemoveSelectedFiles(galleryMainStateView)
            }
        }
    }

    private suspend fun hAddRemoveSelectedFiles(onAddFiles: OnAddFiles) {
        hMapFiles(onAddFiles)
        if (hIsMultipleSelected.not()) {
            hFindFilesCloseFragment()
        }
    }

    private suspend fun hMapFiles(onAddMultipleFiles: OnAddFiles) {
        onAddMultipleFiles.apply {
            val hFolderId = hSelectedFolder?.hFolderId
            var hSelectedFolderMap = hSelectedFoldersPositionsMap[hFolderId]

            if (hSelectedFolderMap == null) {
                hSelectedFolderMap = mutableMapOf()
            }

            var hTrueList = hSelectedFolderMap[true]
            var hFalseList = hSelectedFolderMap[false]

            val pair = hNullCheckList(hTrueList, hFalseList)
            hTrueList = pair.first
            hFalseList = pair.second


            if (hTrueList.contains(onAddMultipleFiles.hPostionHolder)) {
                hAddFalseListRemoveTrueList(hPosition, hFalseList, hTrueList)

            } else {
                hPostionHolder?.let {
                    hTrueList.add(
                        PositionHolder(
                            hPosition = hPosition,
                            hTextInt = hGetSize(),
                            hText = hGetSize().toString()
                        )
                    )
                }
            }


            hFalseList = hCleanUpLists(hFalseList, hTrueList)

            hCheckMapsFolderId(
                hSelectedFolderMap,
                hTrueList,
                hFalseList,
                hFolderId
            )


            if (hIsMultipleSelected) {
                hEmitAdapterUpdate(hFolderId)

            }

        }
        if (hIsMultipleSelected) {
            hEmitUpdateActivity()
        }
    }

    private fun hCheckMapsFolderId(
        hSelectedFolderMap: MutableMap<Boolean, MutableList<PositionHolder>>,
        hTrueList: MutableList<PositionHolder>,
        hFalseList: MutableList<PositionHolder>,
        hFolderId: Long?
    ) {
        hSelectedFolderMap[true] = hTrueList
        hSelectedFolderMap[false] = hFalseList

        hSelectedFoldersPositionsMap[hFolderId!!] = hSelectedFolderMap

        if (hTrueList.isEmpty()) {
            hSelectedFoldersPositionsMap.remove(hFolderId)

        }

    }

    private suspend fun hEmitUpdateActivity() {
        delay(30)

        hGalleryVsMSF.value = OnUpdateActivity(
            hShowNextB = hGetSize() > 1
        )
    }

    private fun OnAddFiles.hEmitAdapterUpdate(hFolderId: Long?) {
        val hTempList = hSelectedFoldersPositionsMap[hFolderId]?.get(true)?.toMutableList()
        hTempList?.addAll(hSelectedFoldersPositionsMap[hFolderId]?.get(false)!!)
        hGalleryVsMSF.value = OnUpdateAdapter(
            hPosition,
            hTempList
        )
    }

    private fun hGetSize(): Int {
        var hTotalCount = 1
        hSelectedFoldersPositionsMap.values.forEach {
            it[true]?.apply {
                hTotalCount += size

            }
        }
        return hTotalCount
    }

    private fun hCleanUpLists(
        hFalseList: MutableList<PositionHolder>,
        hTrueList: MutableList<PositionHolder>
    ): MutableList<PositionHolder> {
        var hFalseList1 = hFalseList

        if (hFalseList1.isNotEmpty()) {
            hFalseList1 = hFalseList1.distinct().toMutableList()

            when (hTrueList.isNotEmpty()) {
                true -> {
                    hFalseList1.iterator().also { hFalseIterator ->
                        while (hFalseIterator.hasNext()) {
                            hTrueList.iterator().also { hTrueIterator ->
                                if (hFalseIterator.next().hPosition == hTrueIterator.next().hPosition) {
                                    hFalseIterator.remove()
                                }
                            }
                        }
                    }
                }
                else -> {
                    hFalseList1.clear()
                }
            }
        }

        return hFalseList1
    }

    private fun hNullCheckList(
        hTrueList: MutableList<PositionHolder>?,
        hFalseList: MutableList<PositionHolder>?
    ): Pair<MutableList<PositionHolder>, MutableList<PositionHolder>> {
        var hTrueList1 = hTrueList
        var hFalseList1 = hFalseList

        hTrueList1 = if (hTrueList1 == null) {
            mutableListOf()
        } else {
            hTrueList
        }
        hFalseList1 = if (hFalseList1 == null) {
            mutableListOf()
        } else {
            hFalseList
        }
        return Pair(hTrueList1!!, hFalseList1!!)
    }

    private fun hAddFalseListRemoveTrueList(
        hPosition: Int,
        hFalseList: MutableList<PositionHolder>,
        hTrueList: MutableList<PositionHolder>
    ) {

        val hTrue = hTrueList.find {
            it.hPosition == hPosition
        }

        if (hTrue != null) {
            hFalseList.add(
                PositionHolder(
                    hTextInt = 0,
                    hText = "",
                    hPosition = hTrue.hPosition
                )
            )
            hTrueList.remove(hTrue)

            hSelectedFoldersPositionsMap.values.forEach { hTrueFalseMap ->
                hTrueFalseMap[true]?.filter { hPositionHolder ->
                    hPositionHolder.hTextInt > hTrue.hTextInt
                }?.onEach { positionHolder ->
                    positionHolder.hTextInt = positionHolder.hTextInt.minus(1)
                    positionHolder.hText = positionHolder.hTextInt.toString()
                }
            }
        }
    }

    fun hOnFolderEvents(folderStateView: FolderStateView) {
        when (folderStateView) {
            is OnChangeFolder -> viewModelScope.launch {
                hChangeFolder(folderStateView.hFolder)
            }
            OnShowFolders -> hShowFolders()
        }
    }

    private suspend fun hChangeFolder(hFolder: Folder) {
        hSelectedFolder = hFolder

        var hCheckFileList: List<CheckedImage>? = null
        when (hFileType) {
            FileType.Audios -> {
                hSelectedFolder as Folder.AudioFolder
                hCheckFileList = (hSelectedFolder as Folder.AudioFolder).hAudioItemsList.map {
                    CheckedImage(
                        hImage = it.hUri.toString(),
                        hIsCheck = hIsMultipleSelected
                    )
                }
            }
            FileType.Images -> {
                hCheckFileList = (hSelectedFolder as Folder.ImageFolder).hImageItemsList.map {
                    CheckedImage(
                        hImage = it.hImageUri.toString(),
                        hIsCheck = hIsMultipleSelected
                    )
                }
            }
            FileType.Videos -> {
                hSelectedFolder as Folder.VideoFolder
                hCheckFileList = (hSelectedFolder as Folder.VideoFolder).hVideoItemsList.map {
                    CheckedImage(
                        hImage = it.hUri.toString(),
                        hIsCheck = hIsMultipleSelected
                    )
                }
            }
            else -> Unit
        }

        hGalleryVsMSF.value = OnFilesRetrieved(
            hFoldersList = hFolderList,
            hImagesList = hCheckFileList,
            hFolderName = hSelectedFolder?.hFolderName
        )

        delay(30)


        if (hIsMultipleSelected) {
            val hTempList = hSelectedFoldersPositionsMap[hSelectedFolder?.hFolderId]
                ?.get(true)?.toMutableList()
            hTempList?.addAll(
                hSelectedFoldersPositionsMap[hSelectedFolder?.hFolderId]
                    ?.get(false)!!
            )
            hGalleryVsMSF.value = OnUpdateAdapter(
                0,
                hTempList
            )
        }
    }

    private fun hShowFolders() {
        val hToHighlightFoldersList = mutableListOf<Long>()
        if (hIsMultipleSelected) {
            hSelectedFoldersPositionsMap.keys.forEach { hFolderId ->
                hFolderList.filter { hGalleryFolders ->
                    hGalleryFolders.hFolderId == hFolderId
                }.forEach {
                    it.hFolderId?.let { it1 ->
                        hToHighlightFoldersList.add(it1)
                    }
                }
            }
        }

        hGalleryVsMSF.value = OnFolderChanged(
            hFoldersList = hFolderList,
            hHighListFoldersList = hToHighlightFoldersList
        )
    }

}




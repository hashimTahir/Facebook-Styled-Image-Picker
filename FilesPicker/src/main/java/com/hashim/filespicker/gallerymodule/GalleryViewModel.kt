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
import com.hashim.filespicker.gallerymodule.GalleryActivitySv.*
import com.hashim.filespicker.gallerymodule.GalleryMainStateView.OnAddFiles
import com.hashim.filespicker.gallerymodule.GalleryVs.*
import com.hashim.filespicker.gallerymodule.data.Folder
import com.hashim.filespicker.gallerymodule.data.PositionHolder
import kotlinx.coroutines.CoroutineScope
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
    private var hScope: CoroutineScope = viewModelScope

    private var hFileType: FileType = FileType.None
    private val hSelectedFoldersPositionsMap = mutableMapOf<Long, MutableMap<Boolean, MutableList<PositionHolder>>>()


    fun hOnActivityEvents(galleryActivitySv: GalleryActivitySv) {
        viewModelScope.launch(Dispatchers.IO) {
            when (galleryActivitySv) {
                OnCameraSelected -> hOnCreateCameraIntent()
                OnMultiSelection -> hSetSelected()
                OnNextClicked -> hFindFilesCloseFragment()
                OnReloadFiles -> hReloadFiles()
                is OnPictureTaken -> hCheckCameraResult(galleryActivitySv.hIsSuccessFull)
                is OnSetData -> hCheckInputData(galleryActivitySv.hBundle)
            }
        }
    }


    private fun hCheckInputData(extras: Bundle?) {

        viewModelScope.launch(Dispatchers.IO) {
            extras?.let {
                hFetchWhichFiles(it)
            }
        }

    }

    private fun hFetchWhichFiles(bundle: Bundle) {
        OnLoadingOrError(hShowLoader = true).hEmitValue()

        hFileType = when {
            bundle.containsKey(FileType.Images.toString()) -> FileType.Images
            bundle.containsKey(FileType.Videos.toString()) -> FileType.Videos
            bundle.containsKey(FileType.Audios.toString()) -> FileType.Audios
            else -> FileType.None
        }

        hSetupViews()

        hEmitUpdateActivity()

        hFetchFiles()

    }

    private fun GalleryVs.hEmitValue() {
        hScope.launch {
            delay(20)
            hGalleryVsMSF.value = this@hEmitValue
        }
    }


    private fun hFetchFiles() {
        hFolderList = when (hFileType) {
            FileType.Audios -> GalleryFilesFetcher.hFetchAudios(getApplication()).toMutableList()
            FileType.Images -> GalleryFilesFetcher.hFetchImages(getApplication()).toMutableList()
            FileType.Videos -> GalleryFilesFetcher.hFetchVideos(getApplication()).toMutableList()
            FileType.None -> hFolderList
        }
        hSelectedFolder = hFolderList[0]

        val hCheckImageList = hSelectedFolder?.hMapToCheckedItemList(
            hIsCheck = hIsMultipleSelected,
            hFileType = hFileType
        )

        if (hFolderList.isNotEmpty()) {
            OnFilesRetrieved(
                hFoldersList = hFolderList,
                hFilesList = hCheckImageList,
                hFolderName = hSelectedFolder?.hFolderName
            ).hEmitValue()
        }


        OnLoadingOrError().hEmitValue()
    }

    private fun hSetupViews() {
        val hCheckImageList = hSelectedFolder?.hMapToCheckedItemList(
            hIsCheck = hIsMultipleSelected,
            hFileType = hFileType
        )
        OnViewSetup(
            hIsMultipleSelected = hIsMultipleSelected,
            hToobarTitle = when (hFileType) {
                FileType.Audios -> "Pick Audios"
                FileType.Images -> "Pick Images"
                FileType.Videos -> "Pick Videos"
                FileType.None -> "Pick Images"
            },
            hCheckImageList = hCheckImageList
        ).hEmitValue()
    }


    private fun hSetSelected() {
        hIsMultipleSelected = !hIsMultipleSelected
        if (hIsMultipleSelected.not()) {
            hUsersSelectedFiles.clear()
            hSelectedFoldersPositionsMap.clear()
        }
        hSetupViews()

    }

    private fun hReloadFiles() {
        val hCheckImageList = hSelectedFolder?.hMapToCheckedItemList(
            hIsCheck = hIsMultipleSelected,
            hFileType = hFileType
        )
        OnFilesRetrieved(
            hFoldersList = hFolderList,
            hFilesList = hCheckImageList,
            hFolderName = hSelectedFolder?.hFolderName
        ).hEmitValue()
    }


    private fun hCheckCameraResult(hIsSuccessFull: Boolean) {
        when {
            hIsSuccessFull -> {
                hEmitOnSelectionDone()
            }
            else -> {
                OnLoadingOrError(hMessage = "Error retrieving files from camera").hEmitValue()
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
            hMapItemToFolder(
                uri = hPhotoUri,
                hFile = hFile
            )?.let {
                hUsersSelectedFiles.add(it)
            }

            OnLaunchCamera(hPhotoUri).hEmitValue()
        }
    }

    private fun hCreateImageFile()
            : File {
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
                    hUsersSelectedFiles.add(hGalleryFolder.hExtractOnlySelectedFiles(hPositionHolder.hPosition))
                }
            }
        }
        hEmitOnSelectionDone()
    }


    private fun hEmitOnSelectionDone() {
        OnSelectionDone(
            hIntentHolder = hUsersSelectedFiles.hMapToOutput(hFileType)
        ).hEmitValue()
    }


    fun hOnGalleryMainFragment(galleryMainStateView: GalleryMainStateView) {
        when (galleryMainStateView) {
            is OnAddFiles -> viewModelScope.launch(Dispatchers.IO) {
                hAddRemoveSelectedFiles(galleryMainStateView)
            }
        }
    }

    private fun hAddRemoveSelectedFiles(onAddFiles: OnAddFiles) {
        hMapFiles(onAddFiles)
        if (hIsMultipleSelected.not()) {
            hFindFilesCloseFragment()
        }
    }

    private fun hMapFiles(onAddMultipleFiles: OnAddFiles) {
        onAddMultipleFiles.apply {
            val hFolderId = hSelectedFolder?.hFolderId
            var hSelectedFolderMap = hSelectedFoldersPositionsMap[hFolderId]

            if (hSelectedFolderMap == null) {
                hSelectedFolderMap = mutableMapOf()
            }

            var hTrueList = hSelectedFolderMap[true]
            var hFalseList = hSelectedFolderMap[false]

            val hPair = hNullCheckList(hTrueList, hFalseList)
            hTrueList = hPair.first
            hFalseList = hPair.second


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

    private fun hEmitUpdateActivity() {
        OnUpdateActivity(
            hShowNextB = hGetSize() > 1
        ).hEmitValue()
    }

    private fun OnAddFiles.hEmitAdapterUpdate(hFolderId: Long?) {
        val hTempList = hSelectedFoldersPositionsMap[hFolderId]?.get(true)?.toMutableList()
        hTempList?.addAll(hSelectedFoldersPositionsMap[hFolderId]?.get(false)!!)
        OnUpdateAdapter(
            hPosition,
            hTempList
        ).hEmitValue()
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

    private fun hChangeFolder(hFolder: Folder) {
        hSelectedFolder = hFolder

        val hCheckFileList = hSelectedFolder?.hMapToCheckedItemList(
            hIsCheck = hIsMultipleSelected,
            hFileType = hFileType
        )

        OnFilesRetrieved(
            hFoldersList = hFolderList,
            hFilesList = hCheckFileList,
            hFolderName = hSelectedFolder?.hFolderName
        ).hEmitValue()



        if (hIsMultipleSelected) {
            val hTempList = hSelectedFoldersPositionsMap[hSelectedFolder?.hFolderId]
                ?.get(true)?.toMutableList()
            hTempList?.addAll(
                hSelectedFoldersPositionsMap[hSelectedFolder?.hFolderId]
                    ?.get(false)!!
            )
            OnUpdateAdapter(
                0,
                hTempList
            ).hEmitValue()
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
        OnFolderChanged(
            hFoldersList = hFolderList,
            hHighListFoldersList = hToHighlightFoldersList
        ).hEmitValue()
    }

    fun hGetFileType(): FileType {
        return hFileType
    }

}



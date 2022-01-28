package com.hashim.filespicker.gallerymodule

import android.app.Application
import android.net.Uri
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
import com.hashim.filespicker.gallerymodule.data.GalleryFolders
import com.hashim.filespicker.gallerymodule.data.IntentHolder
import com.hashim.filespicker.gallerymodule.data.PositionHolder
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

    private var hSelectedFolder: GalleryFolders? = null
    private var hFolderList = mutableListOf<GalleryFolders>()

    private val hUsersSelectedFiles = mutableListOf<GalleryFolders>()

    private val hSelectedFoldersPositionsMap = mutableMapOf<Long, MutableMap<Boolean, MutableList<PositionHolder>>>()

    fun hFetchFiles() {

        viewModelScope.launch {
            hEmitMultipleSelectionView()
        }

        hFetchFilesFromGallery()

    }

    private fun hFetchFilesFromGallery() {
        hFolderList = GalleryFilesFetcher.hFetchGalleryFiles(getApplication())

        if (hFolderList.isNotEmpty()) {
            hSelectedFolder = hFolderList[0]
            hGalleryVsMSF.value = OnFilesRetrieved(
                hFoldersList = hFolderList,
                hImagesList = hSelectedFolder?.hImageUrisList?.map {
                    CheckedImage(
                        hImage = it,
                        hIsCheck = hIsMultipleSelected
                    )
                },
                hFolderName = hSelectedFolder?.hFolderName
            )
        }
    }

    private suspend fun hEmitMultipleSelectionView() {
        hGalleryVsMSF.value = OnViewSetup(
            hImagesList = hSelectedFolder?.hImageUrisList?.map {
                CheckedImage(
                    hImage = it,
                    hIsCheck = hIsMultipleSelected
                )
            },
        )
        delay(30)
        hGalleryVsMSF.value = OnUpdateActivity(
            hShowNextB = hSelectedFoldersPositionsMap[hSelectedFolder?.hFolderId]
                ?.get(true)?.toMutableList()
                ?.isNotEmpty() == true
        )

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

        hGalleryVsMSF.value = OnFilesRetrieved(
            hFoldersList = hFolderList,
            hImagesList = hSelectedFolder?.hImageUrisList?.map {
                CheckedImage(
                    hImage = it,
                    hIsCheck = hIsMultipleSelected
                )
            },
            hFolderName = hSelectedFolder?.hFolderName
        )
    }

    private fun hCreateGalleryFolder(
        hGalleryFolders: GalleryFolders? = null,
        hUri: Uri? = null
    ): GalleryFolders? {
        return when {
            hGalleryFolders != null -> GalleryFolders(
                hItemName = hGalleryFolders.hItemName,
                hFolderName = hGalleryFolders.hFolderName,
                hFolderId = hGalleryFolders.hFolderId,
                hSize = hGalleryFolders.hSize,
            )
            hUri != null -> GalleryFolders().also {
                it.hImageUrisList.add(hUri.toString())
            }
            else -> null
        }
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
        if (hIsSuccessFull) {
            hEmitOnSeletionDone()
        } else {
            /*Emit Error message*/
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

            hCreateGalleryFolder(
                hUri = hPhotoUri
            )?.let {
                it.hImagePathsList.add(hFile.absolutePath)
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

                    hCreateGalleryFolder(hGalleryFolder)?.also {
                        it.hImageUrisList = mutableListOf(
                            hGalleryFolder.hImageUrisList[hPositionHolder.hPosition]
                        )
                        it.hImagePathsList = mutableListOf(
                            hGalleryFolder.hImagePathsList[hPositionHolder.hPosition]
                        )
                        hUsersSelectedFiles.add(it)
                    }

                }
            }
        }


        hEmitOnSeletionDone()
    }

    private fun hEmitOnSeletionDone() {
        hGalleryVsMSF.value = OnSelectionDone(
            hSelectedImagesList = hUsersSelectedFiles.map {
                IntentHolder(
                    hImagePath = it.hImagePathsList[0],
                    hImageUri = it.hImageUrisList[0]
                )
            }
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
            hShowNextB = hSelectedFoldersPositionsMap[hSelectedFolder?.hFolderId]?.get(true)?.toMutableList()
                ?.isNotEmpty() == true
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

    private suspend fun hChangeFolder(hFolder: GalleryFolders) {
        hSelectedFolder = hFolder

        hGalleryVsMSF.value = OnFilesRetrieved(
            hFoldersList = hFolderList,
            hImagesList = hSelectedFolder?.hImageUrisList?.map {
                CheckedImage(
                    hImage = it,
                    hIsCheck = hIsMultipleSelected
                )
            },
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




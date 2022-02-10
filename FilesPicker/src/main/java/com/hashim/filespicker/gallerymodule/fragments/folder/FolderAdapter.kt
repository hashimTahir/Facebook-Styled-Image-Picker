package com.hashim.filespicker.gallerymodule.fragments.folder

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.hashim.filespicker.R
import com.hashim.filespicker.databinding.ItemFolderBinding
import com.hashim.filespicker.gallerymodule.data.Folder

class FolderAdapter(
    val hOnFolderClicked: (Folder) -> Unit
) : RecyclerView.Adapter<FolderVh>() {

    private var hFolderList = listOf<Folder>()
    private var hHighLightFolderList = listOf<Long>()


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): FolderVh {
        return FolderVh(
            hItemFolderBinding = ItemFolderBinding.inflate(
                LayoutInflater.from(parent.context),
                parent,
                false
            )
        )
    }

    override fun onBindViewHolder(imageVh: FolderVh, position: Int) {
        val hFolder = hFolderList[position]

        val hFolderFileCount: String
        val hFoldeImageUri: Any?
        when (hFolder) {
            is Folder.ImageFolder -> {
                hFolderFileCount = hFolder.hImageItemsList.size.toString()
                hFoldeImageUri = hFolder.hImageItemsList[0].hUri.toString()

            }
            is Folder.VideoFolder -> {
                hFolderFileCount = hFolder.hVideoItemsList.size.toString()
                hFoldeImageUri = hFolder.hVideoItemsList[0].hUri.toString()
            }
            is Folder.AudioFolder -> {
                hFolderFileCount = hFolder.hAudioItemsList.size.toString()
                hFoldeImageUri = hFolder.hAudioItemsList[0].hAlbumArt
            }
        }


        imageVh.hItemFolderBinding.apply {

            hFoldeImageUri?.let {
                Glide.with(hFolderThumbIv.context)
                    .load(it)
                    .centerCrop()
                    .into(hFolderThumbIv)
            }

            hFolderCountTv.text = hFolderFileCount
            hFolderNameTv.text = hFolder.hFolderName

            if (hHighLightFolderList.contains(hFolder.hFolderId)) {
                root.background = ContextCompat.getDrawable(
                    root.context,
                    R.color.light_gray
                )
            } else {
                root.background = null
            }

            root.setOnClickListener {
                hOnFolderClicked(hFolder)
            }
        }
    }

    override fun getItemCount(): Int {
        return hFolderList.size
    }

    fun hSetData(it: List<Folder>, hHighListFoldersList: List<Long>) {
        hFolderList = it
        hHighLightFolderList = hHighListFoldersList
        notifyDataSetChanged()
    }


}

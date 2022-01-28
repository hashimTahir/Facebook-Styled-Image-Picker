package com.hashim.filespicker.gallerymodule.fragments.folder

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.hashim.filespicker.R
import com.hashim.filespicker.databinding.ItemFolderBinding
import com.hashim.filespicker.gallerymodule.data.GalleryFolders

class FolderAdapter(
    val hOnFolderClicked: (GalleryFolders) -> Unit
) : RecyclerView.Adapter<FolderVh>() {

    private var hFolderList = listOf<GalleryFolders>()
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
        val hFolderItem = hFolderList[position]
        imageVh.hItemFolderBinding.apply {
            Glide.with(hFolderThumbIv.context)
                .load(hFolderItem.hImageUrisList[0])
                .centerCrop()
                .into(hFolderThumbIv)

            hFolderCountTv.text = hFolderItem.hImageUrisList.size.toString()
            hFolderNameTv.text = hFolderItem.hFolderName

            if (hHighLightFolderList.contains(hFolderItem.hFolderId)) {
                root.background = ContextCompat.getDrawable(
                    root.context,
                    R.color.light_gray
                )
            } else {
                root.background = null
            }

            root.setOnClickListener {
                hOnFolderClicked(hFolderItem)
            }
        }
    }

    override fun getItemCount(): Int {
        return hFolderList.size
    }

    fun hSetData(it: List<GalleryFolders>, hHighListFoldersList: List<Long>) {
        hFolderList = it
        hHighLightFolderList = hHighListFoldersList
        notifyDataSetChanged()
    }


}

package com.hashim.filespickerrunner

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.hashim.filespicker.gallerymodule.data.Folder
import com.hashim.filespicker.gallerymodule.data.IntentHolder
import com.hashim.filespickerrunner.databinding.ItemDisplayImageBinding

class DisplayAdapter : RecyclerView.Adapter<DisplayAdapter.DisplayVh>() {


    inner class DisplayVh(
        val hItemDisplayImageBinding: ItemDisplayImageBinding
    ) : RecyclerView.ViewHolder(hItemDisplayImageBinding.root)


    private var hImageList: List<Folder.ImageItem>? = null
    private var hVideoList: List<Folder.VideoItem>? = null


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): DisplayVh {
        return DisplayVh(
            hItemDisplayImageBinding = ItemDisplayImageBinding.inflate(
                LayoutInflater.from(parent.context),
                parent,
                false
            )
        )
    }

    override fun onBindViewHolder(hDisplayVh: DisplayVh, position: Int) {
        val hUri = if (hImageList != null) {
            hImageList!![position].hImageUri
        } else {
            hVideoList!![position].hUri
        }
        hDisplayVh.hItemDisplayImageBinding.apply {
            Glide.with(hDisplayIv.context)
                .load(hUri)
                .centerCrop()
                .into(hDisplayIv)
        }
    }

    override fun getItemCount(): Int {
        return when {
            hImageList != null -> {
                hImageList!!.size
            }
            hVideoList != null -> {
                hVideoList!!.size
            }
            else -> {
                0
            }
        }
    }

    fun hSetData(intentHolder: IntentHolder?) {
        intentHolder?.let {
            hImageList = it.hImageList
            hVideoList = it.hVideosList
            notifyDataSetChanged()
        }
    }


}

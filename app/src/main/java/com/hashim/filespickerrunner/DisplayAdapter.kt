package com.hashim.filespickerrunner

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.hashim.filespicker.gallerymodule.data.ImageIh
import com.hashim.filespickerrunner.databinding.ItemDisplayImageBinding

class DisplayAdapter : RecyclerView.Adapter<DisplayAdapter.DisplayVh>() {


    inner class DisplayVh(
        val hItemDisplayImageBinding: ItemDisplayImageBinding
    ) : RecyclerView.ViewHolder(hItemDisplayImageBinding.root)


    private var hImageList: List<ImageIh> = listOf()


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
        hDisplayVh.hItemDisplayImageBinding.apply {
            Glide.with(hDisplayIv.context)
                .load(hImageList[position].hUri)
                .centerCrop()
                .into(hDisplayIv)
        }
    }

    override fun getItemCount(): Int {
        return hImageList.size
    }

    fun hSetData(imageList: List<ImageIh>) {
        imageList.let {
            hImageList = it
            notifyDataSetChanged()
        }
    }


}

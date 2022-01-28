package com.hashim.filespickerrunner

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.hashim.filespicker.gallerymodule.data.IntentHolder
import com.hashim.filespickerrunner.databinding.ItemDisplayImageBinding

class DisplayAdapter : RecyclerView.Adapter<DisplayAdapter.DisplayVh>() {


    inner class DisplayVh(
        val hItemDisplayImageBinding: ItemDisplayImageBinding
    ) : RecyclerView.ViewHolder(hItemDisplayImageBinding.root)


    private var hDislayList = listOf<IntentHolder>()


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
                .load(hDislayList[position].hImageUri)
                .centerCrop()
                .into(hDisplayIv)
        }
    }

    override fun getItemCount(): Int {
        return hDislayList.size
    }

    fun hSetData(hRecieviedImagesList: List<IntentHolder>?) {
        hRecieviedImagesList?.let {
            hDislayList = hRecieviedImagesList
            notifyDataSetChanged()
        }
    }


}

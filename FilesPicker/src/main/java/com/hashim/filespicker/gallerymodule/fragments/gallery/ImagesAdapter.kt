package com.hashim.filespicker.gallerymodule.fragments.gallery

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.hashim.filespicker.R
import com.hashim.filespicker.databinding.ItemImageBinding
import com.hashim.filespicker.gallerymodule.GalleryVs.OnUpdateAdapter
import com.hashim.filespicker.gallerymodule.data.CheckedImage
import com.hashim.filespicker.gallerymodule.data.PositionHolder

class ImagesAdapter(
    private val hImageAdapterCallback: (
        hPositionHolder: PositionHolder?,
        hPosition: Int,
    ) -> Unit
) : RecyclerView.Adapter<ImageVh>() {


    private var hImageList = listOf<CheckedImage>()
    private var hPositionMap = mutableListOf<PositionHolder>()


    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): ImageVh {
        return ImageVh(
            hItemImageBinding = ItemImageBinding.inflate(
                LayoutInflater.from(parent.context),
                parent,
                false
            )
        )
    }

    override fun onBindViewHolder(
        imageVh: ImageVh,
        position: Int
    ) {
        val hCheckedImage = hImageList[position]
        imageVh.hItemImageBinding.apply {
            Glide.with(hMainIv.context)
                .load(hCheckedImage.hImage)
                .centerCrop()
                .into(hMainIv)


            when (hCheckedImage.hIsCheck) {
                true -> hNumberCb.visibility = View.VISIBLE
                else -> hNumberCb.visibility = View.GONE
            }

            hUpdateCount(hNumberCb, position)

            root.setOnClickListener {
                hImageAdapterCallback(
                    PositionHolder(
                        hText = hNumberCb.text.toString(),
                        hPosition = position,
                        hTextInt = if (hNumberCb.text.toString().isNotEmpty()) {
                            hNumberCb.text.toString().toInt()
                        } else {
                            0
                        }
                    ),
                    position,
                )
            }
        }
    }


    private fun hUpdateCount(
        hNumberCb: TextView,
        position: Int
    ) {
        if (hPositionMap.isNotEmpty()) {

            hPositionMap.find {
                it.hPosition == position
            }?.apply {
                if (hText.isNotEmpty()) {
                    hNumberCb.apply {
                        text = hText
                        setTextColor(
                            ContextCompat.getColor(
                                hNumberCb.context,
                                R.color.white
                            )
                        )
                        background = ContextCompat.getDrawable(
                            hNumberCb.context,
                            R.drawable.circle_colored
                        )
                    }
                } else {
                    hNumberCb.apply {
                        text = hText
                        background = ContextCompat.getDrawable(
                            hNumberCb.context,
                            R.drawable.rounded_corners
                        )
                    }
                }

            }
        } else {
            hNumberCb.text = ""
            hNumberCb.background = ContextCompat.getDrawable(
                hNumberCb.context,
                R.drawable.circle_transparent
            )
        }
    }

    override fun getItemCount(): Int {
        return hImageList.size
    }

    fun hSetData(it: List<CheckedImage>) {
        hImageList = it
        hPositionMap.clear()
        notifyDataSetChanged()
    }


    fun hUpdate(galleryVs: OnUpdateAdapter) {
        if (galleryVs.hPositionsList != null) {
            hPositionMap = galleryVs.hPositionsList.toMutableList()

            if (hPositionMap.isNotEmpty()) {
                hPositionMap.forEach {
                    notifyItemChanged(it.hPosition)
                }
            }


        } else {
            hPositionMap.clear()
            notifyDataSetChanged()
        }


    }

}

package com.hashim.filespickerrunner

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.net.toUri
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.google.android.exoplayer2.MediaItem
import com.hashim.filespicker.gallerymodule.data.Folder
import com.hashim.filespickerrunner.databinding.ItemVideoBinding

class VideoVh(
    val hItemVideoBinding: ItemVideoBinding
) : RecyclerView.ViewHolder(hItemVideoBinding.root) {

    lateinit var hVideoPreview: MediaItem
    fun hBind(hVideoItem: Folder.VideoItem) {
        hItemVideoBinding.root.tag = this

        hVideoPreview = hVideoItem.hUri?.toUri()?.let { MediaItem.fromUri(it) }!!

        hItemVideoBinding.hVideoTitle.text = hVideoItem.hFileName
        Glide.with(hItemVideoBinding.hVideoThumbnail.context).load(hVideoItem.hUri)
            .into(hItemVideoBinding.hVideoThumbnail)
    }

    companion object {
        fun hGetVideoVh(parent: ViewGroup): VideoVh {
            return VideoVh(
                hItemVideoBinding = ItemVideoBinding.inflate(
                    LayoutInflater.from(parent.context),
                    parent,
                    false
                )
            )
        }
    }
}
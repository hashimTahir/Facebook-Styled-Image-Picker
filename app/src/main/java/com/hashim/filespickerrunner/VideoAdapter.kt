package com.hashim.filespickerrunner

import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.hashim.filespicker.gallerymodule.data.Folder

class VideoAdapter : RecyclerView.Adapter<VideoVh>() {


    private var hVideosList = listOf<Folder.VideoItem>()

    override fun getItemCount(): Int = hVideosList.size

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VideoVh {
        return VideoVh.hGetVideoVh(parent)
    }

    override fun onBindViewHolder(holder: VideoVh, position: Int) {
        val element = hVideosList[position]

        holder.hBind(element)
        holder.itemView.setOnClickListener {
        }
    }

    fun hSetData(list: List<Folder.VideoItem>) {
        hVideosList = list
        notifyDataSetChanged()

    }


}
package com.hashim.filespicker.gallerymodule.fragments.gallery

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.hashim.filespicker.R
import com.hashim.filespicker.databinding.ItemAudioBinding
import com.hashim.filespicker.databinding.ItemImageBinding
import com.hashim.filespicker.gallerymodule.FileType
import com.hashim.filespicker.gallerymodule.GalleryVs.OnUpdateAdapter
import com.hashim.filespicker.gallerymodule.data.CheckedFile
import com.hashim.filespicker.gallerymodule.data.Folder
import com.hashim.filespicker.gallerymodule.data.PositionHolder


const val H_AUDIO_VH = 0
const val H_OTHER_VH = 1

class FilesAdapter : RecyclerView.Adapter<RecyclerView.ViewHolder>() {


    private var hFilesList = listOf<CheckedFile>()
    private var hPositionMap = mutableListOf<PositionHolder>()
    private var hFilesAdapterCallbacks: FilesAdapterCallbacks? = null


    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): RecyclerView.ViewHolder {
        return when (viewType) {
            H_AUDIO_VH -> AudioVh(
                hItemAudioBinding = ItemAudioBinding.inflate(
                    LayoutInflater.from(parent.context),
                    parent,
                    false
                )
            )
            else -> FileVh(
                hItemImageBinding = ItemImageBinding.inflate(
                    LayoutInflater.from(parent.context),
                    parent,
                    false
                )
            )
        }

    }

    override fun getItemViewType(position: Int): Int {
        return when (hFilesList[position].hFileType) {
            FileType.Audios -> H_AUDIO_VH
            else -> H_OTHER_VH
        }
    }

    override fun onBindViewHolder(
        viewHodler: RecyclerView.ViewHolder,
        position: Int
    ) {
        val hCheckFile = hFilesList[position]

        when (viewHodler) {
            is AudioVh -> {
                val hAudioItem: Folder.AudioItem = hCheckFile.hItem as Folder.AudioItem
                viewHodler.hItemAudioBinding.apply {
                    Glide.with(hAudioThumbIv.context)
                        .load(hAudioItem.hAlbumArt)
                        .centerCrop()
                        .into(hAudioThumbIv)

                    hAudioNameTv.text = hAudioItem.hTitle


                    when (hCheckFile.hIsCheck) {
                        true -> hNumberCb.visibility = View.VISIBLE
                        else -> hNumberCb.visibility = View.GONE
                    }

                    hUpdateCount(hNumberCb, position)

                    hPlayIv.setOnClickListener {
                        hAudioItem.hUri?.let { uri ->
                            hFilesAdapterCallbacks?.hOnPlayAudio(uri)
                        }
                    }
                    root.setOnClickListener {
                        hFilesAdapterCallbacks?.hOnUpdateCount(
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
            is FileVh -> {
                viewHodler.hItemImageBinding.apply {
                    Glide.with(hMainIv.context)
                        .load(hCheckFile.hItem?.hUri)
                        .centerCrop()
                        .into(hMainIv)

                    when (hCheckFile.hIsCheck) {
                        true -> hNumberCb.visibility = View.VISIBLE
                        else -> hNumberCb.visibility = View.GONE
                    }

                    hUpdateCount(hNumberCb, position)

                    root.setOnClickListener {
                        hFilesAdapterCallbacks?.hOnUpdateCount(
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
        }

    }


    private fun hUpdateCount(
        hNumberCb: TextView,
        position: Int
    ) {
        if (hPositionMap.isNotEmpty()) {
            hPositionMap.find {
                it.hPosition == position
            }.apply {
                if (this != null) {
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
                } else {
                    hResetTextToDefault(hNumberCb)
                }
            }
        } else {
            hResetTextToDefault(hNumberCb)
        }
    }

    private fun hResetTextToDefault(hNumberCb: TextView) {
        hNumberCb.text = ""
        hNumberCb.background = ContextCompat.getDrawable(
            hNumberCb.context,
            R.drawable.circle_transparent
        )
    }

    override fun getItemCount(): Int {
        return hFilesList.size
    }

    fun hSetData(it: List<CheckedFile>) {
        hFilesList = it
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

    fun hSetFilesAdapterCallbacks(filesAdapterCallbacks: FilesAdapterCallbacks) {
        hFilesAdapterCallbacks = filesAdapterCallbacks
    }

    interface FilesAdapterCallbacks {
        fun hOnUpdateCount(
            positionHolder: PositionHolder,
            position: Int
        )

        fun hOnPlayAudio(hUri: String)
    }

}

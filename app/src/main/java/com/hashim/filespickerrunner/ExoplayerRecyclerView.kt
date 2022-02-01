package com.hashim.filespickerrunner


import android.content.Context
import android.util.AttributeSet
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.ProgressBar
import androidx.recyclerview.widget.RecyclerView
import com.google.android.exoplayer2.ExoPlayer
import com.google.android.exoplayer2.Player
import com.google.android.exoplayer2.source.DefaultMediaSourceFactory
import com.google.android.exoplayer2.ui.AspectRatioFrameLayout
import com.google.android.exoplayer2.ui.PlayerView

class ExoplayerRecyclerView : RecyclerView {
    private var hThumbnail: ImageView? = null
    private var hProgressBar: ProgressBar? = null
    private var hViewHolderParent: View? = null
    private var hFrameLayout: FrameLayout? = null
    private lateinit var hVideoSurfaceView: PlayerView

    private var hVideoPlayer: ExoPlayer? = ExoPlayer.Builder(this.context).build()
    private var isVideoViewAdded = false

    constructor(context: Context) : super(context) {
        init(context)
    }

    constructor(context: Context, attrs: AttributeSet?) : super(context, attrs) {
        init(context)
    }

    private fun init(context: Context) {
        hVideoSurfaceView = PlayerView(context)
        hVideoSurfaceView.videoSurfaceView
        hVideoSurfaceView.resizeMode = AspectRatioFrameLayout.RESIZE_MODE_ZOOM

        hVideoSurfaceView.useController = false
        hVideoSurfaceView.player = hVideoPlayer
        hVideoPlayer?.volume = 0f
        hVideoPlayer?.repeatMode = Player.REPEAT_MODE_ONE

        addOnScrollListener(
            object : OnScrollListener() {
                override fun onScrollStateChanged(recyclerView: RecyclerView, newState: Int) {
                    super.onScrollStateChanged(recyclerView, newState)
                    if (newState == SCROLL_STATE_IDLE) {
                        if (hThumbnail != null) {
                            hThumbnail?.visibility = View.VISIBLE
                        }
                    }
                }
            }
        )
        addOnChildAttachStateChangeListener(object : OnChildAttachStateChangeListener {
            override fun onChildViewAttachedToWindow(view: View) {
                view.setOnClickListener { v ->
                    hPlayVideo(v)
                }
            }

            override fun onChildViewDetachedFromWindow(view: View) {
                if (hViewHolderParent != null && hViewHolderParent == view) {
                    hResetVideoView()
                }
            }
        })
        hVideoPlayer?.addListener(object : Player.Listener {
            override fun onPlayerStateChanged(playWhenReady: Boolean, playbackState: Int) {
                when (playbackState) {
                    Player.STATE_BUFFERING -> {
                        hProgressBar?.visibility = View.VISIBLE
                    }
                    Player.STATE_READY -> {
                        hProgressBar?.visibility = View.GONE
                        if (!isVideoViewAdded) {
                            addVideoView()
                        }
                    }
                    else -> {}
                }
            }
        })
    }

    fun hPlayVideo(view: View) {
        if (hViewHolderParent != null && hViewHolderParent == view) {
            return
        } else {
            hProgressBar?.visibility = View.GONE
            hResetVideoView()
        }
        if (!::hVideoSurfaceView.isInitialized) {
            return
        }

        val holder = view.tag as VideoVh

        hThumbnail = holder.hItemVideoBinding.hVideoThumbnail
        hProgressBar = holder.hItemVideoBinding.hVideoProgressbar
        hViewHolderParent = holder.itemView
        hFrameLayout = holder.hItemVideoBinding.hVideoContainer

        hVideoSurfaceView.player = hVideoPlayer
        holder.hVideoPreview.let {
            val videoSource = DefaultMediaSourceFactory(this.context)
                .createMediaSource(it)
            hVideoPlayer?.setMediaSource(videoSource)
            hVideoPlayer?.prepare()
            hVideoPlayer?.playWhenReady = true
        }
    }

    private fun removeVideoView(videoView: PlayerView?) {
        val parent = videoView?.parent as ViewGroup?
        val index = parent?.indexOfChild(videoView)
        if (index != null && index >= 0) {
            parent.removeViewAt(index)
            isVideoViewAdded = false
        }
    }

    private fun addVideoView() {
        hFrameLayout!!.addView(hVideoSurfaceView)
        isVideoViewAdded = true
        hVideoSurfaceView.requestFocus()
        hVideoSurfaceView.visibility = View.VISIBLE
        hVideoSurfaceView.alpha = 1f
        hThumbnail?.visibility = View.GONE
    }

    private fun hResetVideoView() {
        if (isVideoViewAdded) {
            removeVideoView(hVideoSurfaceView)
            hProgressBar?.visibility = View.INVISIBLE
            hVideoSurfaceView.visibility = View.INVISIBLE
            hThumbnail?.visibility = View.VISIBLE
        }
    }

    fun hReleasePlayer() {
        if (hVideoPlayer != null) {
            hVideoPlayer?.release()
            hVideoPlayer = null
        }
        hResetVideoView()
        hViewHolderParent = null
    }

    fun hCreatePlayer() {
        if (hVideoPlayer == null) {
            init(context)
        }
    }
}
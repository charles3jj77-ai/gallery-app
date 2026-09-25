package com.gallery.clean.adapter

import android.view.Gravity
import android.view.ViewGroup
import android.widget.FrameLayout
import android.widget.ImageView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.gallery.clean.model.MediaItem
import com.github.chrisbanes.photoview.PhotoView

class ViewerPagerAdapter(
    private val items: List<MediaItem>,
    private val onVideoClick: (MediaItem) -> Unit
) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    companion object {
        private const val VIEW_TYPE_IMAGE = 0
        private const val VIEW_TYPE_VIDEO = 1
    }

    override fun getItemViewType(position: Int): Int {
        return if (items[position].isVideo) VIEW_TYPE_VIDEO else VIEW_TYPE_IMAGE
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return if (viewType == VIEW_TYPE_IMAGE) {
            val photoView = PhotoView(parent.context).apply {
                layoutParams = ViewGroup.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.MATCH_PARENT
                )
            }
            ImageViewHolder(photoView)
        } else {
            val context = parent.context
            val frameLayout = FrameLayout(context).apply {
                layoutParams = ViewGroup.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.MATCH_PARENT
                )
            }

            val imageView = ImageView(context).apply {
                layoutParams = FrameLayout.LayoutParams(
                    FrameLayout.LayoutParams.MATCH_PARENT,
                    FrameLayout.LayoutParams.MATCH_PARENT
                )
                scaleType = ImageView.ScaleType.FIT_CENTER
            }

            val playIcon = ImageView(context).apply {
                layoutParams = FrameLayout.LayoutParams(
                    ViewGroup.LayoutParams.WRAP_CONTENT,
                    ViewGroup.LayoutParams.WRAP_CONTENT
                ).apply {
                    gravity = Gravity.CENTER
                }
                setImageResource(android.R.drawable.ic_media_play)
            }

            frameLayout.addView(imageView)
            frameLayout.addView(playIcon)
            
            VideoViewHolder(frameLayout, imageView)
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        val item = items[position]
        if (holder is ImageViewHolder) {
            holder.bind(item)
        } else if (holder is VideoViewHolder) {
            holder.bind(item)
        }
    }

    override fun getItemCount(): Int = items.size

    inner class ImageViewHolder(private val photoView: PhotoView) : RecyclerView.ViewHolder(photoView) {
        fun bind(mediaItem: MediaItem) {
            Glide.with(photoView.context)
                .load(mediaItem.uri)
                .into(photoView)
        }
    }

    inner class VideoViewHolder(
        private val container: FrameLayout,
        private val thumbnail: ImageView
    ) : RecyclerView.ViewHolder(container) {
        fun bind(mediaItem: MediaItem) {
            Glide.with(container.context)
                .load(mediaItem.uri)
                .into(thumbnail)

            container.setOnClickListener {
                onVideoClick(mediaItem)
            }
        }
    }
}

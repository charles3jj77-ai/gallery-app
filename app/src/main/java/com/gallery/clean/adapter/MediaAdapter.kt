package com.gallery.clean.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.gallery.clean.databinding.ItemDateHeaderBinding
import com.gallery.clean.databinding.ItemMediaBinding
import com.gallery.clean.model.MediaItem

class MediaAdapter(
    private val onItemClick: (MediaItem, Int) -> Unit
) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    private var items: List<Any> = emptyList()

    companion object {
        private const val VIEW_TYPE_HEADER = 0
        private const val VIEW_TYPE_MEDIA = 1
    }

    fun setItems(items: List<Any>) {
        this.items = items
        notifyDataSetChanged()
    }

    fun getItems(): List<Any> = items

    fun getSpanSize(position: Int): Int {
        return if (getItemViewType(position) == VIEW_TYPE_HEADER) 4 else 1
    }

    override fun getItemViewType(position: Int): Int {
        return if (items[position] is String) VIEW_TYPE_HEADER else VIEW_TYPE_MEDIA
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        return if (viewType == VIEW_TYPE_HEADER) {
            val binding = ItemDateHeaderBinding.inflate(inflater, parent, false)
            HeaderViewHolder(binding)
        } else {
            val binding = ItemMediaBinding.inflate(inflater, parent, false)
            MediaViewHolder(binding)
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        val item = items[position]
        if (holder is HeaderViewHolder && item is String) {
            holder.bind(item)
        } else if (holder is MediaViewHolder && item is MediaItem) {
            holder.bind(item, position)
        }
    }

    override fun getItemCount(): Int = items.size

    inner class HeaderViewHolder(private val binding: ItemDateHeaderBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(date: String) {
            binding.dateHeader.text = date
        }
    }

    inner class MediaViewHolder(private val binding: ItemMediaBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(mediaItem: MediaItem, position: Int) {
            Glide.with(binding.root.context)
                .load(mediaItem.uri)
                .centerCrop()
                .override(300, 300)
                .into(binding.thumbnail)

            if (mediaItem.isVideo) {
                binding.videoOverlay.visibility = View.VISIBLE
                binding.videoDuration.visibility = View.VISIBLE
                binding.videoDuration.text = formatDuration(mediaItem.duration)
            } else {
                binding.videoOverlay.visibility = View.GONE
                binding.videoDuration.visibility = View.GONE
            }

            binding.root.setOnClickListener {
                onItemClick(mediaItem, position)
            }
        }
    }

    private fun formatDuration(ms: Long): String {
        val totalSeconds = ms / 1000
        val minutes = totalSeconds / 60
        val seconds = totalSeconds % 60
        return String.format("%d:%02d", minutes, seconds)
    }
}

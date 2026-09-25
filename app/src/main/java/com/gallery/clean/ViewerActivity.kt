package com.gallery.clean

import android.content.Intent
import android.os.Bundle
import android.view.WindowInsets
import android.view.WindowInsetsController
import androidx.appcompat.app.AppCompatActivity
import androidx.viewpager2.widget.ViewPager2
import com.gallery.clean.adapter.ViewerPagerAdapter
import com.gallery.clean.databinding.ActivityViewerBinding
import com.gallery.clean.model.MediaItem
import java.text.SimpleDateFormat
import java.util.*

class ViewerActivity : AppCompatActivity() {

    private lateinit var binding: ActivityViewerBinding
    private var isFullscreen = false

    companion object {
        var mediaItems: List<MediaItem>? = null
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityViewerBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val items = mediaItems ?: run {
            finish()
            return
        }

        val startPosition = intent.getIntExtra("start_position", 0)

        val adapter = ViewerPagerAdapter(items) { mediaItem ->
            if (mediaItem.isVideo) {
                val intent = Intent(this, VideoPlayerActivity::class.java).apply {
                    data = mediaItem.uri
                }
                startActivity(intent)
            }
        }

        binding.viewPagerViewer.adapter = adapter
        binding.viewPagerViewer.setCurrentItem(startPosition, false)

        binding.btnBack.setOnClickListener {
            finish()
        }

        binding.viewPagerViewer.registerOnPageChangeCallback(object : ViewPager2.OnPageChangeCallback() {
            override fun onPageSelected(position: Int) {
                val item = items[position]
                val dateFormat = SimpleDateFormat("dd MMMM yyyy HH:mm", Locale.FRANCE)
                binding.viewerTitle.text = dateFormat.format(Date(item.dateModified))
            }
        })

        // Initial title
        if (items.isNotEmpty()) {
            val initialFormat = SimpleDateFormat("dd MMMM yyyy HH:mm", Locale.FRANCE)
            binding.viewerTitle.text = initialFormat.format(Date(items[startPosition].dateModified))
        }

        // Toggle full screen on touch (assuming adapter handles touch intercept or we set it globally)
        // For simplicity, we just hide system bars immediately for immersive mode
        hideSystemBars()
    }

    private fun hideSystemBars() {
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.R) {
            window.insetsController?.let { controller ->
                controller.hide(WindowInsets.Type.systemBars())
                controller.systemBarsBehavior = WindowInsetsController.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
            }
        } else {
            @Suppress("DEPRECATION")
            window.decorView.systemUiVisibility = (
                    android.view.View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
                            or android.view.View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                            or android.view.View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                            or android.view.View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                            or android.view.View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                            or android.view.View.SYSTEM_UI_FLAG_FULLSCREEN)
        }
    }
    
    override fun onDestroy() {
        super.onDestroy()
        if (isFinishing) {
            mediaItems = null
        }
    }
}

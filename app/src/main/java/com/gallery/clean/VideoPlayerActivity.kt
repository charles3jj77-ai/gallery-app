package com.gallery.clean

import android.os.Bundle
import android.view.View
import android.widget.MediaController
import androidx.appcompat.app.AppCompatActivity
import com.gallery.clean.databinding.ActivityVideoPlayerBinding

class VideoPlayerActivity : AppCompatActivity() {

    private lateinit var binding: ActivityVideoPlayerBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityVideoPlayerBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val videoUri = intent.data ?: run {
            finish()
            return
        }

        val mediaController = MediaController(this)
        mediaController.setAnchorView(binding.videoView)
        binding.videoView.setMediaController(mediaController)

        binding.videoView.setVideoURI(videoUri)
        binding.loadingProgress.visibility = View.VISIBLE

        binding.videoView.setOnPreparedListener { mp ->
            binding.loadingProgress.visibility = View.GONE
            binding.videoView.start()
        }
        
        binding.videoView.setOnCompletionListener {
            // Optional: finish or loop
        }
        
        binding.videoView.setOnErrorListener { _, _, _ ->
            binding.loadingProgress.visibility = View.GONE
            finish()
            true
        }
    }

    override fun onPause() {
        super.onPause()
        if (binding.videoView.isPlaying) {
            binding.videoView.pause()
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        binding.videoView.stopPlayback()
    }
}

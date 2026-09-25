package com.gallery.clean

import android.content.ContentUris
import android.content.Intent
import android.os.Bundle
import android.provider.MediaStore
import android.view.MenuItem
import android.view.View
import android.widget.LinearLayout
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.gallery.clean.adapter.MediaAdapter
import com.gallery.clean.model.MediaItem
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.text.SimpleDateFormat
import java.util.*

class AlbumDetailActivity : AppCompatActivity() {

    private lateinit var mediaAdapter: MediaAdapter
    private lateinit var recyclerView: RecyclerView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        val bucketId = intent.getStringExtra("bucket_id") ?: return finish()
        val albumName = intent.getStringExtra("album_name") ?: "Album"

        val container = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.MATCH_PARENT
            )
        }

        val toolbar = Toolbar(this).apply {
            title = albumName
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            )
        }
        container.addView(toolbar)
        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        recyclerView = RecyclerView(this).apply {
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.MATCH_PARENT
            )
        }
        container.addView(recyclerView)
        setContentView(container)

        mediaAdapter = MediaAdapter { item, _ ->
            val allMedia = mediaAdapter.getItems().filterIsInstance<MediaItem>()
            val mediaPos = allMedia.indexOf(item)
            ViewerActivity.mediaItems = allMedia
            val intent = Intent(this, ViewerActivity::class.java).apply {
                putExtra("start_position", if (mediaPos >= 0) mediaPos else 0)
            }
            startActivity(intent)
        }

        val gridLayoutManager = GridLayoutManager(this, 4)
        gridLayoutManager.spanSizeLookup = object : GridLayoutManager.SpanSizeLookup() {
            override fun getSpanSize(position: Int): Int {
                return mediaAdapter.getSpanSize(position)
            }
        }
        recyclerView.layoutManager = gridLayoutManager
        recyclerView.adapter = mediaAdapter
        recyclerView.addItemDecoration(object : RecyclerView.ItemDecoration() {
            override fun getItemOffsets(
                outRect: android.graphics.Rect, view: View,
                parent: RecyclerView, state: RecyclerView.State
            ) {
                outRect.set(2, 2, 2, 2)
            }
        })

        loadAlbumMedia(bucketId)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == android.R.id.home) {
            finish()
            return true
        }
        return super.onOptionsItemSelected(item)
    }

    private fun loadAlbumMedia(bucketId: String) {
        lifecycleScope.launch(Dispatchers.IO) {
            val mediaList = mutableListOf<MediaItem>()
            
            // Query Images
            val imageProjection = arrayOf(
                MediaStore.Images.Media._ID,
                MediaStore.Images.Media.DISPLAY_NAME,
                MediaStore.Images.Media.DATE_MODIFIED,
                MediaStore.Images.Media.BUCKET_DISPLAY_NAME
            )
            
            contentResolver.query(
                MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
                imageProjection,
                "${MediaStore.Images.Media.BUCKET_ID} = ?",
                arrayOf(bucketId),
                null
            )?.use { cursor ->
                val idCol = cursor.getColumnIndexOrThrow(MediaStore.Images.Media._ID)
                val nameCol = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DISPLAY_NAME)
                val dateCol = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATE_MODIFIED)
                val bucketCol = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.BUCKET_DISPLAY_NAME)

                while (cursor.moveToNext()) {
                    val id = cursor.getLong(idCol)
                    mediaList.add(
                        MediaItem(
                            id = id,
                            uri = ContentUris.withAppendedId(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, id),
                            displayName = cursor.getString(nameCol) ?: "",
                            dateModified = cursor.getLong(dateCol) * 1000,
                            isVideo = false,
                            bucketName = cursor.getString(bucketCol) ?: "",
                            bucketId = bucketId
                        )
                    )
                }
            }

            // Query Videos
            val videoProjection = arrayOf(
                MediaStore.Video.Media._ID,
                MediaStore.Video.Media.DISPLAY_NAME,
                MediaStore.Video.Media.DATE_MODIFIED,
                MediaStore.Video.Media.DURATION,
                MediaStore.Video.Media.BUCKET_DISPLAY_NAME
            )

            contentResolver.query(
                MediaStore.Video.Media.EXTERNAL_CONTENT_URI,
                videoProjection,
                "${MediaStore.Video.Media.BUCKET_ID} = ?",
                arrayOf(bucketId),
                null
            )?.use { cursor ->
                val idCol = cursor.getColumnIndexOrThrow(MediaStore.Video.Media._ID)
                val nameCol = cursor.getColumnIndexOrThrow(MediaStore.Video.Media.DISPLAY_NAME)
                val dateCol = cursor.getColumnIndexOrThrow(MediaStore.Video.Media.DATE_MODIFIED)
                val durationCol = cursor.getColumnIndexOrThrow(MediaStore.Video.Media.DURATION)
                val bucketCol = cursor.getColumnIndexOrThrow(MediaStore.Video.Media.BUCKET_DISPLAY_NAME)

                while (cursor.moveToNext()) {
                    val id = cursor.getLong(idCol)
                    mediaList.add(
                        MediaItem(
                            id = id,
                            uri = ContentUris.withAppendedId(MediaStore.Video.Media.EXTERNAL_CONTENT_URI, id),
                            displayName = cursor.getString(nameCol) ?: "",
                            dateModified = cursor.getLong(dateCol) * 1000,
                            duration = cursor.getLong(durationCol),
                            isVideo = true,
                            bucketName = cursor.getString(bucketCol) ?: "",
                            bucketId = bucketId
                        )
                    )
                }
            }

            mediaList.sortByDescending { it.dateModified }
            val groupedItems = groupMediaByDate(mediaList)

            withContext(Dispatchers.Main) {
                mediaAdapter.setItems(groupedItems)
            }
        }
    }

    private fun groupMediaByDate(mediaList: List<MediaItem>): List<Any> {
        val result = mutableListOf<Any>()
        val dateFormat = SimpleDateFormat("dd MMMM yyyy", Locale.FRANCE)
        val todayStr = "Aujourd'hui"
        val yesterdayStr = "Hier"

        val calendar = Calendar.getInstance()
        val today = calendar.apply {
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }.timeInMillis

        val yesterday = today - 86400000
        var currentDateHeader: String? = null

        for (item in mediaList) {
            val dateStr = when {
                item.dateModified >= today -> todayStr
                item.dateModified >= yesterday && item.dateModified < today -> yesterdayStr
                else -> dateFormat.format(Date(item.dateModified))
            }

            if (currentDateHeader != dateStr) {
                currentDateHeader = dateStr
                result.add(dateStr)
            }
            result.add(item)
        }
        return result
    }
}

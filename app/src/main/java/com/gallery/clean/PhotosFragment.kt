package com.gallery.clean

import android.Manifest
import android.content.ContentUris
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.provider.MediaStore
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.gallery.clean.adapter.MediaAdapter
import com.gallery.clean.databinding.FragmentPhotosBinding
import com.gallery.clean.model.MediaItem
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.text.SimpleDateFormat
import java.util.*

class PhotosFragment : Fragment() {

    private var _binding: FragmentPhotosBinding? = null
    private val binding get() = _binding!!
    private lateinit var mediaAdapter: MediaAdapter

    private val requestPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        if (permissions.values.all { it }) {
            loadMedia()
        } else {
            showPermissionView()
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentPhotosBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        mediaAdapter = MediaAdapter { item, position ->
            val allMedia = mediaAdapter.getItems().filterIsInstance<MediaItem>()
            val mediaPos = allMedia.indexOf(item)
            ViewerActivity.mediaItems = allMedia
            val intent = Intent(requireContext(), ViewerActivity::class.java).apply {
                putExtra("start_position", if (mediaPos >= 0) mediaPos else 0)
            }
            startActivity(intent)
        }

        val gridLayoutManager = GridLayoutManager(requireContext(), 4)
        gridLayoutManager.spanSizeLookup = object : GridLayoutManager.SpanSizeLookup() {
            override fun getSpanSize(position: Int): Int {
                return mediaAdapter.getSpanSize(position)
            }
        }
        
        binding.recyclerPhotos.layoutManager = gridLayoutManager
        binding.recyclerPhotos.adapter = mediaAdapter
        binding.recyclerPhotos.addItemDecoration(object : RecyclerView.ItemDecoration() {
            override fun getItemOffsets(
                outRect: android.graphics.Rect, view: View,
                parent: RecyclerView, state: RecyclerView.State
            ) {
                outRect.set(2, 2, 2, 2)
            }
        })

        binding.btnGrantPermission.setOnClickListener {
            checkPermissions()
        }

        checkPermissions()
    }

    private fun checkPermissions() {
        val permissions = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            arrayOf(Manifest.permission.READ_MEDIA_IMAGES, Manifest.permission.READ_MEDIA_VIDEO)
        } else {
            arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE)
        }

        val allGranted = permissions.all {
            ContextCompat.checkSelfPermission(requireContext(), it) == PackageManager.PERMISSION_GRANTED
        }

        if (allGranted) {
            binding.permissionView.visibility = View.GONE
            loadMedia()
        } else {
            requestPermissionLauncher.launch(permissions)
        }
    }

    private fun showPermissionView() {
        binding.permissionView.visibility = View.VISIBLE
        binding.emptyView.visibility = View.GONE
        binding.recyclerPhotos.visibility = View.GONE
    }

    private fun loadMedia() {
        binding.permissionView.visibility = View.GONE
        binding.emptyView.visibility = View.GONE
        binding.recyclerPhotos.visibility = View.VISIBLE

        lifecycleScope.launch(Dispatchers.IO) {
            val mediaList = mutableListOf<MediaItem>()
            
            // Query Images
            val imageProjection = arrayOf(
                MediaStore.Images.Media._ID,
                MediaStore.Images.Media.DISPLAY_NAME,
                MediaStore.Images.Media.DATE_MODIFIED,
                MediaStore.Images.Media.BUCKET_DISPLAY_NAME,
                MediaStore.Images.Media.BUCKET_ID
            )
            
            requireContext().contentResolver.query(
                MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
                imageProjection,
                null,
                null,
                null
            )?.use { cursor ->
                val idCol = cursor.getColumnIndexOrThrow(MediaStore.Images.Media._ID)
                val nameCol = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DISPLAY_NAME)
                val dateCol = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATE_MODIFIED)
                val bucketCol = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.BUCKET_DISPLAY_NAME)
                val bucketIdCol = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.BUCKET_ID)

                while (cursor.moveToNext()) {
                    val id = cursor.getLong(idCol)
                    val dateModified = cursor.getLong(dateCol) * 1000 // Convert to ms
                    mediaList.add(
                        MediaItem(
                            id = id,
                            uri = ContentUris.withAppendedId(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, id),
                            displayName = cursor.getString(nameCol) ?: "",
                            dateModified = dateModified,
                            isVideo = false,
                            bucketName = cursor.getString(bucketCol) ?: "",
                            bucketId = cursor.getString(bucketIdCol) ?: ""
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
                MediaStore.Video.Media.BUCKET_DISPLAY_NAME,
                MediaStore.Video.Media.BUCKET_ID
            )

            requireContext().contentResolver.query(
                MediaStore.Video.Media.EXTERNAL_CONTENT_URI,
                videoProjection,
                null,
                null,
                null
            )?.use { cursor ->
                val idCol = cursor.getColumnIndexOrThrow(MediaStore.Video.Media._ID)
                val nameCol = cursor.getColumnIndexOrThrow(MediaStore.Video.Media.DISPLAY_NAME)
                val dateCol = cursor.getColumnIndexOrThrow(MediaStore.Video.Media.DATE_MODIFIED)
                val durationCol = cursor.getColumnIndexOrThrow(MediaStore.Video.Media.DURATION)
                val bucketCol = cursor.getColumnIndexOrThrow(MediaStore.Video.Media.BUCKET_DISPLAY_NAME)
                val bucketIdCol = cursor.getColumnIndexOrThrow(MediaStore.Video.Media.BUCKET_ID)

                while (cursor.moveToNext()) {
                    val id = cursor.getLong(idCol)
                    val dateModified = cursor.getLong(dateCol) * 1000 // Convert to ms
                    mediaList.add(
                        MediaItem(
                            id = id,
                            uri = ContentUris.withAppendedId(MediaStore.Video.Media.EXTERNAL_CONTENT_URI, id),
                            displayName = cursor.getString(nameCol) ?: "",
                            dateModified = dateModified,
                            duration = cursor.getLong(durationCol),
                            isVideo = true,
                            bucketName = cursor.getString(bucketCol) ?: "",
                            bucketId = cursor.getString(bucketIdCol) ?: ""
                        )
                    )
                }
            }

            mediaList.sortByDescending { it.dateModified }

            val groupedItems = groupMediaByDate(mediaList)

            withContext(Dispatchers.Main) {
                if (groupedItems.isEmpty()) {
                    binding.emptyView.visibility = View.VISIBLE
                    binding.recyclerPhotos.visibility = View.GONE
                } else {
                    mediaAdapter.setItems(groupedItems)
                }
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

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}

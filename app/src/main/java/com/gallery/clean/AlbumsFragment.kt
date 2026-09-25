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
import com.gallery.clean.adapter.AlbumAdapter
import com.gallery.clean.databinding.FragmentAlbumsBinding
import com.gallery.clean.model.Album
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class AlbumsFragment : Fragment() {

    private var _binding: FragmentAlbumsBinding? = null
    private val binding get() = _binding!!
    private lateinit var albumAdapter: AlbumAdapter

    private val requestPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        if (permissions.values.all { it }) {
            loadAlbums()
        } else {
            showPermissionView()
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentAlbumsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        albumAdapter = AlbumAdapter { album ->
            val intent = Intent(requireContext(), AlbumDetailActivity::class.java).apply {
                putExtra("album_name", album.name)
                putExtra("bucket_id", album.id)
            }
            startActivity(intent)
        }

        binding.recyclerAlbums.layoutManager = GridLayoutManager(requireContext(), 2)
        binding.recyclerAlbums.adapter = albumAdapter
        binding.recyclerAlbums.addItemDecoration(object : RecyclerView.ItemDecoration() {
            override fun getItemOffsets(
                outRect: android.graphics.Rect, view: View,
                parent: RecyclerView, state: RecyclerView.State
            ) {
                outRect.set(4, 4, 4, 4)
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
            loadAlbums()
        } else {
            requestPermissionLauncher.launch(permissions)
        }
    }

    private fun showPermissionView() {
        binding.permissionView.visibility = View.VISIBLE
        binding.emptyView.visibility = View.GONE
        binding.recyclerAlbums.visibility = View.GONE
    }

    private fun loadAlbums() {
        binding.permissionView.visibility = View.GONE
        binding.emptyView.visibility = View.GONE
        binding.recyclerAlbums.visibility = View.VISIBLE

        lifecycleScope.launch(Dispatchers.IO) {
            val bucketMap = mutableMapOf<String, Album>()

            val projection = arrayOf(
                MediaStore.Images.Media._ID,
                MediaStore.Images.Media.BUCKET_ID,
                MediaStore.Images.Media.BUCKET_DISPLAY_NAME
            )

            // Get Images
            requireContext().contentResolver.query(
                MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
                projection,
                null,
                null,
                "${MediaStore.Images.Media.DATE_MODIFIED} DESC"
            )?.use { cursor ->
                val idCol = cursor.getColumnIndexOrThrow(MediaStore.Images.Media._ID)
                val bucketIdCol = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.BUCKET_ID)
                val bucketNameCol = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.BUCKET_DISPLAY_NAME)

                while (cursor.moveToNext()) {
                    val bucketId = cursor.getString(bucketIdCol) ?: continue
                    val name = cursor.getString(bucketNameCol) ?: "Unknown"
                    if (!bucketMap.containsKey(bucketId)) {
                        val id = cursor.getLong(idCol)
                        val uri = ContentUris.withAppendedId(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, id)
                        bucketMap[bucketId] = Album(bucketId, name, uri, 1)
                    } else {
                        val album = bucketMap[bucketId]!!
                        bucketMap[bucketId] = album.copy(count = album.count + 1)
                    }
                }
            }

            // Get Videos
            val videoProjection = arrayOf(
                MediaStore.Video.Media._ID,
                MediaStore.Video.Media.BUCKET_ID,
                MediaStore.Video.Media.BUCKET_DISPLAY_NAME
            )

            requireContext().contentResolver.query(
                MediaStore.Video.Media.EXTERNAL_CONTENT_URI,
                videoProjection,
                null,
                null,
                "${MediaStore.Video.Media.DATE_MODIFIED} DESC"
            )?.use { cursor ->
                val idCol = cursor.getColumnIndexOrThrow(MediaStore.Video.Media._ID)
                val bucketIdCol = cursor.getColumnIndexOrThrow(MediaStore.Video.Media.BUCKET_ID)
                val bucketNameCol = cursor.getColumnIndexOrThrow(MediaStore.Video.Media.BUCKET_DISPLAY_NAME)

                while (cursor.moveToNext()) {
                    val bucketId = cursor.getString(bucketIdCol) ?: continue
                    val name = cursor.getString(bucketNameCol) ?: "Unknown"
                    if (!bucketMap.containsKey(bucketId)) {
                        val id = cursor.getLong(idCol)
                        val uri = ContentUris.withAppendedId(MediaStore.Video.Media.EXTERNAL_CONTENT_URI, id)
                        bucketMap[bucketId] = Album(bucketId, name, uri, 1)
                    } else {
                        val album = bucketMap[bucketId]!!
                        bucketMap[bucketId] = album.copy(count = album.count + 1)
                    }
                }
            }

            val albumList = bucketMap.values.toList().sortedBy { it.name }

            withContext(Dispatchers.Main) {
                if (albumList.isEmpty()) {
                    binding.emptyView.visibility = View.VISIBLE
                    binding.recyclerAlbums.visibility = View.GONE
                } else {
                    albumAdapter.setAlbums(albumList)
                }
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}

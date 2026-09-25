package com.gallery.clean.model

import android.net.Uri

data class MediaItem(
    val id: Long,
    val uri: Uri,
    val displayName: String,
    val dateModified: Long,  // epoch seconds
    val duration: Long = 0,  // milliseconds, 0 for photos
    val isVideo: Boolean = false,
    val bucketName: String = "",
    val bucketId: String = ""
)

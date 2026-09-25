package com.gallery.clean.model

import android.net.Uri

data class Album(
    val id: String,
    val name: String,
    val coverUri: Uri,
    val count: Int
)

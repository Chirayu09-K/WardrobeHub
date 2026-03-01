package com.example.wardrobehub.repository

import android.content.Context
import android.net.Uri
import kotlinx.coroutines.flow.Flow

interface ImageRepository {
    fun uploadImage(context: Context, imageUri: Uri, callback: (String) -> Unit)
    fun getFileNameFromUri(context: Context, uri: Uri): String?
}
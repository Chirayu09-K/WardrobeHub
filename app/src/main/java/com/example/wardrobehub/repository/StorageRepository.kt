package com.example.wardrobehub.repository

import android.net.Uri
import kotlinx.coroutines.flow.Flow

interface StorageRepository {
    fun uploadImage(userId: String, uri: Uri): Flow<Result<String>>
}
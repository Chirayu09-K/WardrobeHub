package com.example.wardrobehub.repository

import android.content.Context
import android.net.Uri
import android.provider.OpenableColumns
import com.cloudinary.android.MediaManager
import com.cloudinary.android.callback.ErrorInfo
import com.cloudinary.android.callback.UploadCallback

class ImageRepositoryImpl : ImageRepository {
    override fun uploadImage(context: Context, imageUri: Uri, callback: (String) -> Unit) {
        MediaManager.get().upload(imageUri)
            .callback(object : UploadCallback {
                override fun onStart(requestId: String) {}

                override fun onProgress(requestId: String, bytes: Long, totalBytes: Long) {}

                override fun onSuccess(requestId: String, resultData: Map<*, *>) {
                    // Use secure_url for HTTPS
                    val secureUrl = resultData["secure_url"] as? String ?: resultData["url"] as String
                    callback(secureUrl)
                }

                override fun onError(requestId: String, error: ErrorInfo) {
                    callback("")
                }

                override fun onReschedule(requestId: String, error: ErrorInfo) {}
            })
            .dispatch(context)
    }

    override fun getFileNameFromUri(context: Context, uri: Uri): String? {
        var result: String? = null
        if (uri.scheme == "content") {
            val cursor = context.contentResolver.query(uri, null, null, null, null)
            try {
                if (cursor != null && cursor.moveToFirst()) {
                    val columnIndex = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME)
                    if (columnIndex != -1) {
                        result = cursor.getString(columnIndex)
                    }
                }
            } finally {
                cursor?.close()
            }
        }
        if (result == null) {
            result = uri.path
            val cut = result?.lastIndexOf('/')
            if (cut != -1) {
                if (result != null) {
                    result = result.substring(cut!! + 1)
                }
            }
        }
        return result
    }
}
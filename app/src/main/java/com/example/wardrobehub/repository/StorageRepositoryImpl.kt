package com.example.wardrobehub.repository

import android.net.Uri
import com.google.firebase.storage.FirebaseStorage
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import java.util.UUID

class StorageRepositoryImpl : StorageRepository {

    private val storage = FirebaseStorage.getInstance()

    override fun uploadImage(userId: String, uri: Uri): Flow<Result<String>> = callbackFlow {
        val storageRef = storage.reference
        val imageRef = storageRef.child("images/$userId/${UUID.randomUUID()}")

        val uploadTask = imageRef.putFile(uri)

        uploadTask.continueWithTask { task ->
            if (!task.isSuccessful) {
                task.exception?.let {
                    throw it
                }
            }
            imageRef.downloadUrl
        }.addOnCompleteListener { task ->
            if (task.isSuccessful) {
                val downloadUri = task.result
                trySend(Result.success(downloadUri.toString()))
            } else {
                trySend(Result.failure(task.exception ?: Exception("Image upload failed")))
            }
        }
        awaitClose { }
    }
}
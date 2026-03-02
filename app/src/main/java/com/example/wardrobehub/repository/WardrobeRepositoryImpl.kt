package com.example.wardrobehub.repository

import com.example.wardrobehub.model.ClothingItem
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await

class WardrobeRepositoryImpl : WardrobeRepository {

    private val database = FirebaseDatabase.getInstance().getReference("wardrobe")

    override fun getWardrobeItems(userId: String): Flow<Result<List<ClothingItem>>> = callbackFlow {
        val listener = object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val items = snapshot.children.mapNotNull { it.getValue(ClothingItem::class.java) }
                trySend(Result.success(items))
            }

            override fun onCancelled(error: DatabaseError) {
                trySend(Result.failure(error.toException()))
            }
        }
        database.child(userId).addValueEventListener(listener)
        awaitClose { database.child(userId).removeEventListener(listener) }
    }

    override suspend fun addClothingItem(userId: String, item: ClothingItem): Result<Unit> {
        return try {
            database.child(userId).child(item.id).setValue(item).await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun deleteClothingItem(userId: String, itemId: String): Result<Unit> {
        return try {
            database.child(userId).child(itemId).removeValue().await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
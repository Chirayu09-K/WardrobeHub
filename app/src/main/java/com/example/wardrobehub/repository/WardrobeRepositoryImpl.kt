package com.example.wardrobehub.repository

import com.example.wardrobehub.model.ClothingItem
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow

class WardrobeRepositoryImpl : WardrobeRepository {

    private val database = FirebaseDatabase.getInstance().reference

    override fun addClothingItem(userId: String, item: ClothingItem): Flow<Result<Unit>> = callbackFlow {
        val newItemRef = database.child("wardrobe_items").child(userId).push()
        val newItem = item.copy(id = newItemRef.key!!)

        newItemRef.setValue(newItem)
            .addOnSuccessListener {
                trySend(Result.success(Unit))
            }
            .addOnFailureListener {
                trySend(Result.failure(it))
            }
        awaitClose { }
    }

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
        database.child("wardrobe_items").child(userId).addValueEventListener(listener)
        awaitClose { database.child("wardrobe_items").child(userId).removeEventListener(listener) }
    }
}
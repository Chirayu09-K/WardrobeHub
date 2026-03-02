package com.example.wardrobehub.repository

import com.example.wardrobehub.model.ClothingItem
import kotlinx.coroutines.flow.Flow

interface WardrobeRepository {
    fun getWardrobeItems(userId: String): Flow<Result<List<ClothingItem>>>
    suspend fun addClothingItem(userId: String, item: ClothingItem): Result<Unit>
    suspend fun deleteClothingItem(userId: String, itemId: String): Result<Unit>
}
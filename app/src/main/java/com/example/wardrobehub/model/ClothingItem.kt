package com.example.wardrobehub.model

data class ClothingItem(
    val id: String = "",
    val userId: String = "",
    val name: String = "",
    val category: String = "",
    val color: String = "",
    val imageUrl: String? = "",
)
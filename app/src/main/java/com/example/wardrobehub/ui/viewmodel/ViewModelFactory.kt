package com.example.wardrobehub.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.wardrobehub.repository.AuthRepository
import com.example.wardrobehub.repository.ImageRepository
import com.example.wardrobehub.repository.UserRepository
import com.example.wardrobehub.repository.WardrobeRepository

class ViewModelFactory(
    private val authRepository: AuthRepository,
    private val userRepository: UserRepository,
    private val wardrobeRepository: WardrobeRepository,
    private val imageRepository: ImageRepository
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(AuthViewModel::class.java)) {
            return AuthViewModel(authRepository) as T
        }
        if (modelClass.isAssignableFrom(DashboardViewModel::class.java)) {
            return DashboardViewModel(authRepository, userRepository, wardrobeRepository) as T
        }
        if (modelClass.isAssignableFrom(AddItemViewModel::class.java)) {
            return AddItemViewModel(authRepository, wardrobeRepository, imageRepository) as T
        }
        if (modelClass.isAssignableFrom(WardrobeViewModel::class.java)) {
            return WardrobeViewModel(authRepository, wardrobeRepository) as T
        }
        if (modelClass.isAssignableFrom(EditItemViewModel::class.java)) {
            return EditItemViewModel(authRepository, wardrobeRepository, imageRepository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
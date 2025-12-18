package com.example.wardrobehub.ui.viewmodel

import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.wardrobehub.model.ClothingItem
import com.example.wardrobehub.repository.AuthRepository
import com.example.wardrobehub.repository.StorageRepository
import com.example.wardrobehub.repository.WardrobeRepository
import com.example.wardrobehub.ui.auth.AuthUiState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

class AddItemViewModel(
    private val authRepository: AuthRepository,
    private val wardrobeRepository: WardrobeRepository,
    private val storageRepository: StorageRepository
) : ViewModel() {

    private val _addItemState = MutableStateFlow<AuthUiState<Unit>>(AuthUiState.Idle)
    val addItemState: StateFlow<AuthUiState<Unit>> = _addItemState.asStateFlow()

    fun addClothingItem(name: String, category: String, color: String, imageUri: Uri?) {
        val currentUser = authRepository.getCurrentUser()
        if (currentUser == null) {
            _addItemState.value = AuthUiState.Error(Exception("User not logged in"))
            return
        }

        if (name.isBlank() || category.isBlank() || color.isBlank()) {
            _addItemState.value = AuthUiState.Error(Exception("Please fill in all fields"))
            return
        }

        viewModelScope.launch {
            _addItemState.value = AuthUiState.Loading

            try {
                val imageUrl = if (imageUri != null) {
                    val uploadResult = storageRepository.uploadImage(currentUser.uid, imageUri).first()
                    uploadResult.getOrThrow() //Exception if the upload failed.
                } else {
                    "" // No image to upload
                }

                val newItem = ClothingItem(name = name, category = category, color = color, imageUrl = imageUrl)

                //Add the item to the Realtime Database
                wardrobeRepository.addClothingItem(currentUser.uid, newItem).collect {
                    _addItemState.value = if (it.isSuccess) {
                        AuthUiState.Success(Unit)
                    } else {
                        AuthUiState.Error(it.exceptionOrNull() ?: Exception("Failed to save item."))
                    }
                }

            } catch (e: Exception) {
                //Catch exceptions from image upload or database write
                _addItemState.value = AuthUiState.Error(e)
            }
        }
    }

    fun resetState() {
        _addItemState.value = AuthUiState.Idle
    }
}
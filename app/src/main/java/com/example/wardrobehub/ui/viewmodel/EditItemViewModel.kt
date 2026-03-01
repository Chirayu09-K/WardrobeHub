package com.example.wardrobehub.ui.viewmodel

import android.content.Context
import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.wardrobehub.model.ClothingItem
import com.example.wardrobehub.repository.AuthRepository
import com.example.wardrobehub.repository.ImageRepository
import com.example.wardrobehub.repository.WardrobeRepository
import com.example.wardrobehub.utils.AuthUiState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class EditItemViewModel(
    private val authRepository: AuthRepository,
    private val wardrobeRepository: WardrobeRepository,
    private val imageRepository: ImageRepository
) : ViewModel() {

    private val _editItemState = MutableStateFlow<AuthUiState<Unit>>(AuthUiState.Idle)
    val editItemState: StateFlow<AuthUiState<Unit>> = _editItemState.asStateFlow()

    private val _itemState = MutableStateFlow<ClothingItem?>(null)
    val itemState: StateFlow<ClothingItem?> = _itemState.asStateFlow()

    fun getItem(itemId: String) {
        val currentUser = authRepository.getCurrentUser()
        if (currentUser == null) {
            _editItemState.value = AuthUiState.Error(Exception("User not logged in"))
            return
        }

        viewModelScope.launch {
            wardrobeRepository.getWardrobeItems(currentUser.uid).collect { result ->
                if (result.isSuccess) {
                    _itemState.value = result.getOrNull()?.find { it.id == itemId }
                } else {
                    _editItemState.value = AuthUiState.Error(result.exceptionOrNull()!!)
                }
            }
        }
    }

    fun updateClothingItem(context: Context, itemId: String, name: String, category: String, color: String, imageUri: Uri?) {
        val currentUser = authRepository.getCurrentUser()
        if (currentUser == null) {
            _editItemState.value = AuthUiState.Error(Exception("User not logged in"))
            return
        }

        _editItemState.value = AuthUiState.Loading

        if (imageUri != null) {
            imageRepository.uploadImage(context, imageUri) { imageUrl ->
                viewModelScope.launch {
                    val updatedItem = ClothingItem(
                        id = itemId,
                        userId = currentUser.uid,
                        name = name,
                        category = category,
                        color = color,
                        imageUrl = imageUrl
                    )
                    wardrobeRepository.addClothingItem(currentUser.uid, updatedItem).let { addResult ->
                        _editItemState.value = if (addResult.isSuccess) {
                            AuthUiState.Success(Unit)
                        } else {
                            AuthUiState.Error(addResult.exceptionOrNull()!!)
                        }
                    }
                }
            }
        } else {
            viewModelScope.launch {
                val updatedItem = ClothingItem(
                    id = itemId,
                    userId = currentUser.uid,
                    name = name,
                    category = category,
                    color = color,
                    imageUrl = _itemState.value?.imageUrl
                )
                wardrobeRepository.addClothingItem(currentUser.uid, updatedItem).let { addResult ->
                    _editItemState.value = if (addResult.isSuccess) {
                        AuthUiState.Success(Unit)
                    } else {
                        AuthUiState.Error(addResult.exceptionOrNull()!!)
                    }
                }
            }
        }
    }

    fun resetState() {
        _editItemState.value = AuthUiState.Idle
    }
}
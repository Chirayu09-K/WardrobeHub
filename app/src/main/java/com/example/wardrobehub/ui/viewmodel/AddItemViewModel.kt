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
import java.util.UUID

class AddItemViewModel(
    private val authRepository: AuthRepository,
    private val wardrobeRepository: WardrobeRepository,
    private val imageRepository: ImageRepository
) : ViewModel() {

    private val _addItemState = MutableStateFlow<AuthUiState<Unit>>(AuthUiState.Idle)
    val addItemState: StateFlow<AuthUiState<Unit>> = _addItemState.asStateFlow()

    fun addClothingItem(context: Context, name: String, category: String, color: String, imageUri: Uri?) {
        val currentUser = authRepository.getCurrentUser()
        if (currentUser == null) {
            _addItemState.value = AuthUiState.Error(Exception("User not logged in"))
            return
        }

        _addItemState.value = AuthUiState.Loading

        if (imageUri != null) {
            imageRepository.uploadImage(context, imageUri) { imageUrl ->
                viewModelScope.launch {
                    val newItem = ClothingItem(
                        id = UUID.randomUUID().toString(),
                        userId = currentUser.uid,
                        name = name,
                        category = category,
                        color = color,
                        imageUrl = imageUrl
                    )
                    wardrobeRepository.addClothingItem(currentUser.uid, newItem).let { addResult ->
                        _addItemState.value = if (addResult.isSuccess) {
                            AuthUiState.Success(Unit)
                        } else {
                            AuthUiState.Error(addResult.exceptionOrNull()!!)
                        }
                    }
                }
            }
        } else {
            viewModelScope.launch {
                val newItem = ClothingItem(
                    id = UUID.randomUUID().toString(),
                    userId = currentUser.uid,
                    name = name,
                    category = category,
                    color = color
                )
                wardrobeRepository.addClothingItem(currentUser.uid, newItem).let { addResult ->
                    _addItemState.value = if (addResult.isSuccess) {
                        AuthUiState.Success(Unit)
                    } else {
                        AuthUiState.Error(addResult.exceptionOrNull()!!)
                    }
                }
            }
        }
    }

    fun resetState() {
        _addItemState.value = AuthUiState.Idle
    }
}
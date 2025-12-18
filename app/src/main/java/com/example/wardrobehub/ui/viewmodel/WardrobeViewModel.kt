package com.example.wardrobehub.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.wardrobehub.model.ClothingItem
import com.example.wardrobehub.repository.AuthRepository
import com.example.wardrobehub.repository.WardrobeRepository
import com.example.wardrobehub.ui.auth.AuthUiState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class WardrobeViewModel(
    private val authRepository: AuthRepository,
    private val wardrobeRepository: WardrobeRepository
) : ViewModel() {

    private val _wardrobeState = MutableStateFlow<AuthUiState<List<ClothingItem>>>(AuthUiState.Idle)

    private val _selectedCategory = MutableStateFlow<String?>("All")
    val selectedCategory: StateFlow<String?> = _selectedCategory.asStateFlow()

    val filteredWardrobeState: StateFlow<AuthUiState<List<ClothingItem>>> = 
        combine(_wardrobeState, _selectedCategory) { state, category ->
            if (state is AuthUiState.Success) {
                val filteredList = if (category == "All") {
                    state.data
                } else {
                    state.data.filter { it.category == category }
                }
                AuthUiState.Success(filteredList)
            } else {
                state
            }
        }.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = AuthUiState.Idle
        )

    init {
        getWardrobeItems()
    }

    private fun getWardrobeItems() {
        val currentUser = authRepository.getCurrentUser()
        if (currentUser == null) {
            _wardrobeState.value = AuthUiState.Error(Exception("User not logged in"))
            return
        }

        viewModelScope.launch {
            _wardrobeState.value = AuthUiState.Loading
            wardrobeRepository.getWardrobeItems(currentUser.uid).collect {
                _wardrobeState.value = if (it.isSuccess) {
                    AuthUiState.Success(it.getOrNull() ?: emptyList())
                } else {
                    AuthUiState.Error(it.exceptionOrNull()!!)
                }
            }
        }
    }

    fun selectCategory(category: String) {
        _selectedCategory.value = category
    }
}
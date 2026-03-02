package com.example.wardrobehub.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.wardrobehub.model.ClothingItem
import com.example.wardrobehub.repository.AuthRepository
import com.example.wardrobehub.repository.WardrobeRepository
import com.example.wardrobehub.utils.AuthUiState
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

class WardrobeViewModel(
    private val authRepository: AuthRepository,
    private val wardrobeRepository: WardrobeRepository
) : ViewModel() {

    private val _wardrobeState = MutableStateFlow<AuthUiState<List<ClothingItem>>>(AuthUiState.Idle)
    
    private val _selectedCategory = MutableStateFlow<String?>(null)
    val selectedCategory: StateFlow<String?> = _selectedCategory.asStateFlow()

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    val filteredWardrobeState: StateFlow<AuthUiState<List<ClothingItem>>> = 
        combine(
            _wardrobeState,
            _selectedCategory,
            _searchQuery
        ) { state, category, query ->
            when (state) {
                is AuthUiState.Success -> {
                    val items = state.data
                    val filteredItems = items.filter { item ->
                        (category == null || item.category == category) &&
                        (query.isEmpty() || item.name.contains(query, ignoreCase = true) || item.color.contains(query, ignoreCase = true))
                    }
                    AuthUiState.Success(filteredItems)
                }
                else -> state
            }
        }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), AuthUiState.Idle)

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
            wardrobeRepository.getWardrobeItems(currentUser.uid).collect { result ->
                _wardrobeState.value = if (result.isSuccess) {
                    AuthUiState.Success(result.getOrNull() ?: emptyList())
                } else {
                    AuthUiState.Error(result.exceptionOrNull() ?: Exception("Failed to fetch items"))
                }
            }
        }
    }

    fun deleteClothingItem(itemId: String) {
        val currentUser = authRepository.getCurrentUser()
        if (currentUser == null) return

        viewModelScope.launch {
            wardrobeRepository.deleteClothingItem(currentUser.uid, itemId)
        }
    }

    fun selectCategory(category: String?) {
        _selectedCategory.value = category
    }

    fun onSearchQueryChanged(query: String) {
        _searchQuery.value = query
    }
}
package com.example.wardrobehub.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.wardrobehub.model.ClothingItem
import com.example.wardrobehub.model.User
import com.example.wardrobehub.repository.AuthRepository
import com.example.wardrobehub.repository.UserRepository
import com.example.wardrobehub.repository.WardrobeRepository
import com.example.wardrobehub.utils.AuthUiState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class DashboardViewModel(
    private val authRepository: AuthRepository,
    private val userRepository: UserRepository,
    private val wardrobeRepository: WardrobeRepository
) : ViewModel() {

    private val _userState = MutableStateFlow<AuthUiState<User>>(AuthUiState.Idle)
    val userState: StateFlow<AuthUiState<User>> = _userState.asStateFlow()

    private val _wardrobeState = MutableStateFlow<AuthUiState<List<ClothingItem>>>(AuthUiState.Idle)
    val wardrobeState: StateFlow<AuthUiState<List<ClothingItem>>> = _wardrobeState.asStateFlow()

    init {
        getUser()
        getWardrobeItems()
    }

    private fun getUser() {
        viewModelScope.launch {
            _userState.value = AuthUiState.Loading
            val currentUser = authRepository.getCurrentUser()
            if (currentUser != null) {
                userRepository.getUser(currentUser.uid).collect {
                    _userState.value = if (it.isSuccess) {
                        AuthUiState.Success(it.getOrNull()!!)
                    } else {
                        AuthUiState.Error(it.exceptionOrNull()!!)
                    }
                }
            } else {
                _userState.value = AuthUiState.Error(Exception("User not logged in"))
            }
        }
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

    fun logout() {
        authRepository.logout()
    }
}
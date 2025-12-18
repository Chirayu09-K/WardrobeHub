package com.example.wardrobehub.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.wardrobehub.model.User
import com.example.wardrobehub.repository.AuthRepository
import com.example.wardrobehub.repository.UserRepository
import com.example.wardrobehub.ui.auth.AuthUiState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class DashboardViewModel(
    private val authRepository: AuthRepository,
    private val userRepository: UserRepository
) : ViewModel() {

    private val _userState = MutableStateFlow<AuthUiState<User>>(AuthUiState.Idle)
    val userState: StateFlow<AuthUiState<User>> = _userState.asStateFlow()

    init {
        getUser()
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

    fun logout() {
        authRepository.logout()
    }
}
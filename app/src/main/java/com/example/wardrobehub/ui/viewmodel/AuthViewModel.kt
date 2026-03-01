package com.example.wardrobehub.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.wardrobehub.model.User
import com.example.wardrobehub.repository.AuthRepository
import com.example.wardrobehub.utils.AuthUiState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class AuthViewModel(private val repository: AuthRepository) : ViewModel() {

    private val _loginState = MutableStateFlow<AuthUiState<User>>(AuthUiState.Idle)
    val loginState: StateFlow<AuthUiState<User>> = _loginState.asStateFlow()

    private val _registerState = MutableStateFlow<AuthUiState<User>>(AuthUiState.Idle)
    val registerState: StateFlow<AuthUiState<User>> = _registerState.asStateFlow()

    private val _passwordResetState = MutableStateFlow<AuthUiState<Unit>>(AuthUiState.Idle)
    val passwordResetState: StateFlow<AuthUiState<Unit>> = _passwordResetState.asStateFlow()

    fun login(email: String, password: String) {
        viewModelScope.launch {
            _loginState.value = AuthUiState.Loading
            repository.login(email, password).collect { result ->
                _loginState.value = if (result.isSuccess) {
                    AuthUiState.Success(result.getOrNull()!!)
                } else {
                    AuthUiState.Error(result.exceptionOrNull()!!)
                }
            }
        }
    }

    fun register(username: String, email: String, password: String) {
        viewModelScope.launch {
            _registerState.value = AuthUiState.Loading
            repository.register(username, email, password).collect { result ->
                _registerState.value = if (result.isSuccess) {
                    AuthUiState.Success(result.getOrNull()!!)
                } else {
                    AuthUiState.Error(result.exceptionOrNull()!!)
                }
            }
        }
    }

    fun sendPasswordResetEmail(email: String) {
        viewModelScope.launch {
            _passwordResetState.value = AuthUiState.Loading
            repository.sendPasswordResetEmail(email).collect { result ->
                _passwordResetState.value = if (result.isSuccess) {
                    AuthUiState.Success(Unit)
                } else {
                    AuthUiState.Error(result.exceptionOrNull()!!)
                }
            }
        }
    }

    fun resetAuthState() {
        _loginState.value = AuthUiState.Idle
        _registerState.value = AuthUiState.Idle
        _passwordResetState.value = AuthUiState.Idle
    }
}
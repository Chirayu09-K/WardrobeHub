package com.example.wardrobehub.utils

sealed interface AuthUiState<out T> {
    object Idle : AuthUiState<Nothing>
    object Loading : AuthUiState<Nothing>
    data class Success<T>(val data: T) : AuthUiState<T>
    data class Error(val exception: Throwable) : AuthUiState<Nothing>
}
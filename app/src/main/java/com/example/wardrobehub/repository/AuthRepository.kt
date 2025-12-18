package com.example.wardrobehub.repository

import com.example.wardrobehub.model.User
import kotlinx.coroutines.flow.Flow

interface AuthRepository {
    fun login(email: String, password: String): Flow<Result<User>>
    fun register(username: String, email: String, password: String): Flow<Result<User>>
    fun sendPasswordResetEmail(email: String): Flow<Result<Unit>>
    fun logout()
    fun getCurrentUser(): User?
    fun addUserToDatabase(userId: String, model: User): Flow<Result<Unit>>
}
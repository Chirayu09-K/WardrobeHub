package com.example.wardrobehub.repository

import com.example.wardrobehub.model.User
import kotlinx.coroutines.flow.Flow

interface UserRepository {
    fun getUser(uid: String): Flow<Result<User>>
}


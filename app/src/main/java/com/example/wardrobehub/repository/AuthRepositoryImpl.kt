package com.example.wardrobehub.repository

import com.example.wardrobehub.model.User
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow

class AuthRepositoryImpl : AuthRepository {

    private val auth: FirebaseAuth = FirebaseAuth.getInstance()
    private val database = FirebaseDatabase.getInstance().reference

    override fun login(email: String, password: String): Flow<Result<User>> = callbackFlow {
        auth.signInWithEmailAndPassword(email, password)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    val userId = auth.currentUser!!.uid
                    database.child("users").child(userId).get()
                        .addOnSuccessListener { dataSnapshot ->
                            val user = dataSnapshot.getValue(User::class.java)
                            if (user != null) {
                                trySend(Result.success(user))
                            } else {
                                trySend(Result.failure(Exception("User data not found in database.")))
                            }
                        }
                        .addOnFailureListener { 
                            trySend(Result.failure(it))
                        }
                } else {
                    trySend(Result.failure(task.exception ?: Exception("Login failed")))
                }
            }
        awaitClose { }
    }

    override fun register(username: String, email: String, password: String): Flow<Result<User>> = callbackFlow {
        auth.createUserWithEmailAndPassword(email, password)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    val userId = auth.currentUser!!.uid
                    val user = User(uid = userId, username = username, email = email)
                    database.child("users").child(userId).setValue(user)
                        .addOnSuccessListener { 
                            trySend(Result.success(user)) 
                        }
                        .addOnFailureListener { 
                            trySend(Result.failure(it))
                        }
                } else {
                    trySend(Result.failure(task.exception ?: Exception("Registration failed")))
                }
            }
        awaitClose { }
    }

    override fun sendPasswordResetEmail(email: String): Flow<Result<Unit>> = callbackFlow {
        auth.sendPasswordResetEmail(email)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    trySend(Result.success(Unit))
                } else {
                    trySend(Result.failure(task.exception ?: Exception("Failed to send password reset email")))
                }
            }
        awaitClose { }
    }

    override fun logout() {
        auth.signOut()
    }

    override fun getCurrentUser(): User? {
        val firebaseUser = auth.currentUser
        return firebaseUser?.let { User(uid = it.uid, email = it.email!!) }
    }

    override fun addUserToDatabase(userId: String, model: User): Flow<Result<Unit>> = callbackFlow {
        database.child("users").child(userId).setValue(model)
            .addOnSuccessListener { 
                trySend(Result.success(Unit))
            }
            .addOnFailureListener { 
                trySend(Result.failure(it))
            }
        awaitClose { }
    }
}
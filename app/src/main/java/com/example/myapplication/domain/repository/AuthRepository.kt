package com.example.myapplication.domain.repository

import kotlinx.coroutines.flow.Flow

interface AuthRepository {
    val currentUserFlow: Flow<String?> // Returns UserId if logged in, null otherwise
    val currentUserUid: String?
    suspend fun signOut()
    suspend fun deleteAccount()
}

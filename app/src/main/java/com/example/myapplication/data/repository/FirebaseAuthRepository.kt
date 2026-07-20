package com.example.myapplication.data.repository

import com.example.myapplication.domain.repository.AuthRepository
import com.example.myapplication.domain.repository.StreaksRepository
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class FirebaseAuthRepository @Inject constructor(
    private val firebaseAuth: FirebaseAuth,
    private val streaksRepository: StreaksRepository
) : AuthRepository {

    override val currentUserFlow: Flow<String?> = callbackFlow {
        val authStateListener = FirebaseAuth.AuthStateListener { auth ->
            trySend(auth.currentUser?.uid)
        }
        firebaseAuth.addAuthStateListener(authStateListener)
        awaitClose {
            firebaseAuth.removeAuthStateListener(authStateListener)
        }
    }

    override val currentUserUid: String?
        get() = firebaseAuth.currentUser?.uid

    override suspend fun signOut() {
        firebaseAuth.signOut()
    }

    override suspend fun deleteAccount() {
        val user = firebaseAuth.currentUser ?: return
        val uid = user.uid
        
        // 1. Delete user from auth
        user.delete().await()
        
        // 2. Clear user data locally (and potentially remotely if cloud function doesn't handle it)
        streaksRepository.clearAllUserData(uid)
    }
}

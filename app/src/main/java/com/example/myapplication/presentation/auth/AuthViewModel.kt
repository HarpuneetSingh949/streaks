package com.example.myapplication.presentation.auth

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.myapplication.domain.repository.AuthRepository
import com.example.myapplication.domain.repository.StreaksRepository
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class AuthViewModel @Inject constructor(
    private val authRepository: AuthRepository,
    private val streaksRepository: StreaksRepository,
    private val firebaseAuth: FirebaseAuth
) : ViewModel() {

    private val _authState = MutableStateFlow<AuthState>(AuthState.Idle)
    val authState: StateFlow<AuthState> = _authState.asStateFlow()

    init {
        viewModelScope.launch {
            authRepository.currentUserFlow.collect { uid ->
                if (uid != null) {
                    _authState.value = AuthState.Authenticated(uid)
                    // Sync data when authenticated
                    streaksRepository.syncWithRemote(uid)
                } else {
                    _authState.value = AuthState.Unauthenticated
                }
            }
        }
    }

    fun signInWithGoogleToken(idToken: String) {
        _authState.value = AuthState.Loading
        val credential = GoogleAuthProvider.getCredential(idToken, null)
        firebaseAuth.signInWithCredential(credential)
            .addOnCompleteListener { task ->
                if (!task.isSuccessful) {
                    _authState.value = AuthState.Error(task.exception?.message ?: "Sign in failed")
                }
            }
    }

    fun signInAnonymously() {
        _authState.value = AuthState.Loading
        firebaseAuth.signInAnonymously()
            .addOnCompleteListener { task ->
                if (!task.isSuccessful) {
                    _authState.value = AuthState.Error(task.exception?.message ?: "Guest sign in failed")
                }
            }
    }
}

sealed class AuthState {
    object Idle : AuthState()
    object Loading : AuthState()
    data class Authenticated(val userId: String) : AuthState()
    object Unauthenticated : AuthState()
    data class Error(val message: String) : AuthState()
}

package com.example.gun_app_kotlin.ui.screens

import androidx.compose.animation.core.copy
import androidx.lifecycle.ViewModel
import com.example.gun_app_kotlin.network.User
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

// This holds the data for the currently logged in user
data class SessionState(
    val currentUser: User? = null,
    val isJustLoggedIn: Boolean = false // This will be our trigger
)

class SessionViewModel : ViewModel() {
    private val _uiState = MutableStateFlow(SessionState())
    val uiState = _uiState.asStateFlow()

    fun onLoginSuccess(user: User) {
        // --- 2. SET the flag to true on login ---
        _uiState.update { it.copy(currentUser = user, isJustLoggedIn = true) }
    }

    // --- 3. ADD a function to reset the flag ---
    fun onSyncCompleted() {
        _uiState.update { it.copy(isJustLoggedIn = false) }
    }

    fun onLogout() {
        // Reset everything on logout
        _uiState.update { SessionState() }
    }
}

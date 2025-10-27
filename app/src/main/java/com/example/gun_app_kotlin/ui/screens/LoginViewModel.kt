package com.example.gun_app_kotlin.ui.screens

import android.content.Context
import androidx.lifecycle.*
import com.example.gun_app_kotlin.data.AppDatabase
import com.example.gun_app_kotlin.data.LinenRepository
import com.example.gun_app_kotlin.network.ApiClient
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class LoginUiState(
    val username: String = "",
    val password: String = "",
    val isLoading: Boolean = false,
    val loginError: String? = null,
    val loginSuccess: Boolean = false
)

class LoginViewModel(private val repository: LinenRepository, private val sessionViewModel: SessionViewModel) : ViewModel() {

    private val _uiState = MutableStateFlow(LoginUiState())
    val uiState = _uiState.asStateFlow()

    fun onUsernameChange(username: String) {
        _uiState.update { it.copy(username = username) }
    }

    fun onPasswordChange(password: String) {
        _uiState.update { it.copy(password = password) }
    }

    fun attemptLogin() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, loginError = null) }
            val result = repository.loginUser(
                username = _uiState.value.username,
                password = _uiState.value.password
            )

            result.onSuccess { response -> // The result is now a LoginResponse
                // --- THIS IS THE KEY CHANGE ---
                // Save the user data to the shared SessionViewModel
                sessionViewModel.onLoginSuccess(response.user)
                // -----------------------------

                _uiState.update { it.copy(isLoading = false, loginSuccess = true) }

            }.onFailure { exception ->
                _uiState.update { it.copy(isLoading = false, loginError = "Invalid username or password.") }
            }
        }
    }
}

class LoginViewModelFactory(
    private val context: Context,
    private val sessionViewModel: SessionViewModel // <-- ADD THIS
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(LoginViewModel::class.java)) {
            val db = AppDatabase.getDatabase(context)
            val repository = LinenRepository(db.linenDao(), db.batchInDao(), db.batchInDetailDao(), ApiClient.apiService, db.batchUsageDao(), db.batchUsageDetailDao())
            @Suppress("UNCHECKED_CAST")
            // Pass the sessionViewModel to the LoginViewModel constructor
            return LoginViewModel(repository, sessionViewModel) as T // <-- UPDATE THIS
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}

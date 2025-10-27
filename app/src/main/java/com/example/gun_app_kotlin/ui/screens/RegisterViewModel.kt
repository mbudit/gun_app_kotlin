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
import retrofit2.HttpException

data class RegisterUiState(
    val username: String = "",
    val password: String = "",
    val confirmPassword: String = "",
    val name: String = "", // <-- ADD THIS
    val isLoading: Boolean = false,
    val error: String? = null,
    val registrationSuccess: Boolean = false
)

class RegisterViewModel(private val repository: LinenRepository) : ViewModel() {

    private val _uiState = MutableStateFlow(RegisterUiState())
    val uiState = _uiState.asStateFlow()

    fun onNameChange(name: String) { _uiState.update { it.copy(name = name) } }
    fun onUsernameChange(username: String) { _uiState.update { it.copy(username = username) } }
    fun onPasswordChange(password: String) { _uiState.update { it.copy(password = password) } }
    fun onConfirmPasswordChange(confirm: String) { _uiState.update { it.copy(confirmPassword = confirm) } }

    fun attemptRegistration() {
        val state = _uiState.value

        if (state.name.isBlank()) {
            _uiState.update { it.copy(error = "Name cannot be empty.") }
            return
        }
        if (state.password != state.confirmPassword) {
            _uiState.update { it.copy(error = "Passwords do not match.") }
            return
        }
        if (state.password.length < 6) {
            _uiState.update { it.copy(error = "Password must be at least 6 characters long.")}
            return
        }

        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }
            val result = repository.registerUser(state.username, state.password, state.name)
            result.onSuccess {
                _uiState.update { it.copy(isLoading = false, registrationSuccess = true) }
            }.onFailure { e ->
                val errorMessage = if (e is HttpException && e.code() == 409) {
                    "Username already exists."
                } else {
                    "Registration failed. Please try again."
                }
                _uiState.update { it.copy(isLoading = false, error = errorMessage) }
            }
        }
    }
}

class RegisterViewModelFactory(private val context: Context) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(RegisterViewModel::class.java)) {
            val db = AppDatabase.getDatabase(context)
            val repository = LinenRepository(db.linenDao(), db.batchInDao(), db.batchInDetailDao(), ApiClient.apiService, db.batchUsageDao(), db.batchUsageDetailDao())
            @Suppress("UNCHECKED_CAST")
            return RegisterViewModel(repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}

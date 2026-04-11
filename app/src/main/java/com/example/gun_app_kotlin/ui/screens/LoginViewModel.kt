package com.example.gun_app_kotlin.ui.screens

import android.content.Context
import androidx.lifecycle.*
import com.example.gun_app_kotlin.data.AppDatabase
import com.example.gun_app_kotlin.data.LinenRepository
import com.example.gun_app_kotlin.data.ServerConfigManager
import com.example.gun_app_kotlin.network.ApiClient
import com.example.gun_app_kotlin.network.WebSocketManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.net.HttpURLConnection
import java.net.URL

enum class ConnectionStatus { CONNECTED, DISCONNECTED, CHECKING }

data class LoginUiState(
    val username: String = "",
    val password: String = "",
    val isLoading: Boolean = false,
    val loginError: String? = null,
    val loginSuccess: Boolean = false,
    val serverUrl: String = "",
    val connectionStatus: ConnectionStatus = ConnectionStatus.DISCONNECTED,
    val isServerConfigExpanded: Boolean = false
)

class LoginViewModel(
    private val repository: LinenRepository,
    private val sessionViewModel: SessionViewModel
) : ViewModel() {

    private val _uiState = MutableStateFlow(LoginUiState())
    val uiState = _uiState.asStateFlow()

    init {
        // Load the saved server URL
        _uiState.update { it.copy(serverUrl = ServerConfigManager.getServerUrl()) }
        // Auto-check connection on init
        testConnection()
    }

    fun onUsernameChange(username: String) {
        _uiState.update { it.copy(username = username) }
    }

    fun onPasswordChange(password: String) {
        _uiState.update { it.copy(password = password) }
    }

    fun onServerUrlChange(url: String) {
        _uiState.update { it.copy(serverUrl = url) }
    }

    fun toggleServerConfig() {
        _uiState.update { it.copy(isServerConfigExpanded = !it.isServerConfigExpanded) }
    }

    /**
     * Apply the server URL: save it, rebuild ApiClient, reconnect WebSocket,
     * and test the connection.
     */
    fun applyServerUrl() {
        val newUrl = _uiState.value.serverUrl.trim()
        if (newUrl.isEmpty()) return

        ServerConfigManager.setServerUrl(newUrl)
        ApiClient.updateBaseUrl()
        WebSocketManager.reconnectWithNewUrl()
        testConnection()
    }

    /**
     * Test connectivity by making a quick HTTP request to the server.
     */
    fun testConnection() {
        val urlToTest = _uiState.value.serverUrl.trim()
        if (urlToTest.isEmpty()) return

        viewModelScope.launch {
            _uiState.update { it.copy(connectionStatus = ConnectionStatus.CHECKING) }
            val isReachable = withContext(Dispatchers.IO) {
                try {
                    val url = URL("http://$urlToTest/api/linens")
                    val conn = url.openConnection() as HttpURLConnection
                    conn.connectTimeout = 3000
                    conn.readTimeout = 3000
                    conn.requestMethod = "GET"
                    val code = conn.responseCode
                    conn.disconnect()
                    code in 200..599 // Any response means server is reachable
                } catch (e: Exception) {
                    false
                }
            }
            _uiState.update {
                it.copy(
                    connectionStatus = if (isReachable) ConnectionStatus.CONNECTED else ConnectionStatus.DISCONNECTED
                )
            }
        }
    }

    fun attemptLogin() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, loginError = null) }
            val result = repository.loginUser(
                username = _uiState.value.username,
                password = _uiState.value.password
            )

            result.onSuccess { response ->
                sessionViewModel.onLoginSuccess(response.user)
                _uiState.update { it.copy(isLoading = false, loginSuccess = true) }
            }.onFailure { exception ->
                _uiState.update { it.copy(isLoading = false, loginError = "Invalid username or password.") }
            }
        }
    }
}

class LoginViewModelFactory(
    private val context: Context,
    private val sessionViewModel: SessionViewModel
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(LoginViewModel::class.java)) {
            val db = AppDatabase.getDatabase(context)
            val repository = LinenRepository(db.linenDao(), db.batchInDao(), db.batchInDetailDao(), ApiClient.apiService, db.batchUsageDao(), db.batchUsageDetailDao())
            @Suppress("UNCHECKED_CAST")
            return LoginViewModel(repository, sessionViewModel) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}

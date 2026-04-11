package com.example.gun_app_kotlin.ui.screens

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.gun_app_kotlin.data.ServerConfigManager
import com.example.gun_app_kotlin.network.ApiClient
import com.example.gun_app_kotlin.network.WebSocketManager
import com.rscja.deviceapi.RFIDWithUHFUART
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.net.HttpURLConnection
import java.net.URL

data class SettingsState(
    // RFID Reader
    val currentPower: Int = 30,
    val isLoading: Boolean = true,
    val error: String? = null,
    // Server Config
    val serverUrl: String = "",
    val connectionStatus: ConnectionStatus = ConnectionStatus.DISCONNECTED,
    val isServerConfigExpanded: Boolean = true
)

class SettingsViewModel : ViewModel() {

    // Use lateinit for the reader
    private lateinit var rfidReader: RFIDWithUHFUART

    private val _uiState = MutableStateFlow(SettingsState())
    val uiState = _uiState.asStateFlow()

    init {
        // Load saved server URL and auto-check connection
        _uiState.update { it.copy(serverUrl = ServerConfigManager.getServerUrl()) }
        testConnection()
    }

    // ═══════════════════════════════════
    // Server Configuration
    // ═══════════════════════════════════

    fun onServerUrlChange(url: String) {
        _uiState.update { it.copy(serverUrl = url) }
    }

    fun toggleServerConfig() {
        _uiState.update { it.copy(isServerConfigExpanded = !it.isServerConfigExpanded) }
    }

    fun applyServerUrl() {
        val newUrl = _uiState.value.serverUrl.trim()
        if (newUrl.isEmpty()) return

        ServerConfigManager.setServerUrl(newUrl)
        ApiClient.updateBaseUrl()
        WebSocketManager.reconnectWithNewUrl()
        testConnection()
    }

    fun testConnection() {
        viewModelScope.launch {
            _uiState.update { it.copy(connectionStatus = ConnectionStatus.CHECKING) }
            val isReachable = withContext(Dispatchers.IO) {
                try {
                    val url = URL("${ServerConfigManager.getHttpBaseUrl()}api/linens")
                    val conn = url.openConnection() as HttpURLConnection
                    conn.connectTimeout = 3000
                    conn.readTimeout = 3000
                    conn.requestMethod = "GET"
                    val code = conn.responseCode
                    conn.disconnect()
                    code in 200..599
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

    // ═══════════════════════════════════
    // RFID Reader Power Settings
    // ═══════════════════════════════════

    // 1. INIT à la `UHFSetFragment`
    fun init(context: Context) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                // getInstance() can throw an exception, so it's in the try-catch
                rfidReader = RFIDWithUHFUART.getInstance()
                // init() can also fail
                rfidReader.init(context)

                // After a successful init, immediately get the current power
                getPower()

            } catch (e: Exception) { // Catching a broader range of exceptions
                e.printStackTrace()
                _uiState.update { it.copy(isLoading = false, error = "Failed to initialize RFID reader.") }
            }
        }
    }

    // 2. GET POWER logic, translated to Kotlin/Flow
    fun getPower() {
        viewModelScope.launch(Dispatchers.IO) {
            // Ensure reader is initialized before using it
            if (!::rfidReader.isInitialized) return@launch

            val power = rfidReader.power
            if (power > -1) {
                // Update the UI state on the main thread
                withContext(Dispatchers.Main) {
                    _uiState.update { it.copy(currentPower = power, isLoading = false) }
                }
            } else {
                withContext(Dispatchers.Main) {
                    _uiState.update { it.copy(isLoading = false, error = "Failed to read power.") }
                }
            }
        }
    }

    // 3. SET POWER logic, translated to Kotlin/Flow
    fun setPower(power: Int) {
        viewModelScope.launch(Dispatchers.IO) {
            if (!::rfidReader.isInitialized) return@launch

            if (rfidReader.setPower(power)) {
                // If setting power was successful, update our state to match
                withContext(Dispatchers.Main) {
                    _uiState.update { it.copy(currentPower = power) }
                }
            } else {
                withContext(Dispatchers.Main) {
                    _uiState.update { it.copy(error = "Failed to set power.") }
                }
            }
        }
    }

    fun clearError() {
        _uiState.update { it.copy(error = null) }
    }

    // 4. Clean up the reader when the ViewModel is destroyed
    override fun onCleared() {
        super.onCleared()
        // Check if rfidReader has been initialized before trying to free it
        if (::rfidReader.isInitialized) {
            rfidReader.free()
        }
    }
}

// The factory remains the same, it doesn't need to change
class SettingsViewModelFactory(private val context: Context) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(SettingsViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            // We pass the context to the init function, not the constructor
            return SettingsViewModel() as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}

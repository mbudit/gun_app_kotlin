package com.example.gun_app_kotlin.data

import android.content.Context
import android.content.SharedPreferences
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

/**
 * Singleton manager that persists the server URL (IP:Port) to SharedPreferences
 * and exposes it reactively via StateFlow so ApiClient, WebSocketManager,
 * and UI components can all observe changes.
 */
object ServerConfigManager {

    private const val PREFS_NAME = "ServerConfig"
    private const val KEY_SERVER_URL = "server_url"
    private const val DEFAULT_SERVER_URL = "100.108.196.112:5010"

    private lateinit var prefs: SharedPreferences

    private val _serverUrl = MutableStateFlow(DEFAULT_SERVER_URL)
    val serverUrl: StateFlow<String> = _serverUrl.asStateFlow()

    fun init(context: Context) {
        prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        _serverUrl.value = prefs.getString(KEY_SERVER_URL, DEFAULT_SERVER_URL) ?: DEFAULT_SERVER_URL
    }

    fun getServerUrl(): String {
        return _serverUrl.value
    }

    fun setServerUrl(url: String) {
        val trimmed = url.trim()
        if (trimmed.isNotEmpty()) {
            prefs.edit().putString(KEY_SERVER_URL, trimmed).apply()
            _serverUrl.value = trimmed
        }
    }

    fun getHttpBaseUrl(): String {
        return "http://${_serverUrl.value}/"
    }

    fun getWebSocketUrl(): String {
        return "ws://${_serverUrl.value}"
    }
}

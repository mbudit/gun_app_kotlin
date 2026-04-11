package com.example.gun_app_kotlin.network

import com.example.gun_app_kotlin.data.LinenRepository
import com.example.gun_app_kotlin.data.ServerConfigManager
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.Response
import okhttp3.WebSocket
import okhttp3.WebSocketListener
import java.util.concurrent.TimeUnit

object WebSocketManager {
    private const val RECONNECT_INTERVAL_MS = 5000L // 5 seconds

    // --- DEBOUNCING LOGIC ---
    private const val DEBOUNCE_PERIOD_MS = 2000L // 2 seconds
    private var refreshJob: Job? = null
    // ------------------------

    // --- CONNECTION STATE ---
    private val _isConnected = MutableStateFlow(false)
    val isConnected: StateFlow<Boolean> = _isConnected.asStateFlow()
    // -----------------------

    private var webSocket: WebSocket? = null
    private val client = OkHttpClient.Builder()
        .readTimeout(0, TimeUnit.MILLISECONDS) // Important for long-lived connections
        .build()

    // Use a dedicated scope that lives as long as the app
    private val scope = CoroutineScope(Dispatchers.IO + SupervisorJob())

    private lateinit var linenRepository: LinenRepository
    private var shouldReconnect = true

    // This must be called once when the app starts
    fun init(repository: LinenRepository) {
        this.linenRepository = repository
        connect()
    }

    private val webSocketListener = object : WebSocketListener() {
        override fun onOpen(webSocket: WebSocket, response: Response) {
            println("WebSocket: Connection Opened")
            _isConnected.value = true
        }

        override fun onMessage(webSocket: WebSocket, text: String) {
            println("WebSocket: Message Received: $text")

            if (text.contains("data_changed")) {
                // --- START OF NEW DEBOUNCING LOGIC ---
                // Cancel any previously scheduled refresh job.
                refreshJob?.cancel()

                // Schedule a new refresh job to run after the debounce period.
                refreshJob = scope.launch {
                    println("WebSocket: Data change detected. Waiting ${DEBOUNCE_PERIOD_MS}ms for more changes...")
                    delay(DEBOUNCE_PERIOD_MS)

                    // If this code is reached, it means the timer was not reset. Time to refresh!
                    println("WebSocket: Debounce timer finished. Refreshing linens now...")
                    try {
                        linenRepository.refreshLinens()
                        println("WebSocket: Linen refresh completed successfully.")
                    } catch (e: Exception) {
                        println("WebSocket: Error during debounced data refresh: ${e.message}")
                    }
                }
                // --- END OF NEW DEBOUNCING LOGIC ---
            }
        }

        override fun onClosing(webSocket: WebSocket, code: Int, reason: String) {
            webSocket.close(1000, null)
            println("WebSocket: Connection Closing")
            _isConnected.value = false
        }

        override fun onFailure(webSocket: WebSocket, t: Throwable, response: Response?) {
            println("WebSocket: Connection Failed: ${t.message}. Scheduling reconnect.")
            _isConnected.value = false
            if (shouldReconnect) {
                reconnect()
            }
        }
    }

    private fun connect() {
        val wsUrl = ServerConfigManager.getWebSocketUrl()
        println("WebSocket: Attempting to connect to $wsUrl...")
        val request = Request.Builder().url(wsUrl).build()
        webSocket = client.newWebSocket(request, webSocketListener)
    }

    private fun reconnect() {
        scope.launch {
            delay(RECONNECT_INTERVAL_MS)
            if (shouldReconnect) {
                connect()
            }
        }
    }

    /**
     * Call this when the server URL changes. Disconnects the current WebSocket
     * and reconnects using the new URL from ServerConfigManager.
     */
    fun reconnectWithNewUrl() {
        scope.launch {
            // Close existing connection without auto-reconnect
            shouldReconnect = false
            webSocket?.close(1000, "URL changed")
            _isConnected.value = false

            // Small delay to allow cleanup
            delay(500)

            // Reconnect with new URL
            shouldReconnect = true
            connect()
        }
    }

    fun close() {
        shouldReconnect = false
        webSocket?.close(1000, "App destroyed")
        refreshJob?.cancel() // Also cancel any pending job on close
        _isConnected.value = false
    }
}

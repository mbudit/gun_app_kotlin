package com.example.gun_app_kotlin.ui.screens

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.gun_app_kotlin.data.AppDatabase
import com.example.gun_app_kotlin.data.LinenRepository
import com.example.gun_app_kotlin.network.ApiClient
import kotlinx.coroutines.flow.MutableSharedFlow // <-- Import this
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asSharedFlow // <-- Import this
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class HomeState(
    val isSyncing: Boolean = false,
    val isClearing: Boolean = false
)

class HomeViewModel(private val repository: LinenRepository) : ViewModel() {

    private val _uiState = MutableStateFlow(HomeState())
    val uiState = _uiState.asStateFlow()

    // --- ADD THIS SHARED FLOW FOR ONE-TIME EVENTS ---
    private val _syncCompletedEvent = MutableSharedFlow<Unit>()
    val syncCompletedEvent = _syncCompletedEvent.asSharedFlow()
    // ------------------------------------------------

    fun syncData() {
        viewModelScope.launch {
            _uiState.update { it.copy(isSyncing = true) }
            try {
                repository.refreshLinens()
            } catch (e: Exception) {
                e.printStackTrace()
            } finally {
                // When sync is done (success or fail), update state AND send event
                _uiState.update { it.copy(isSyncing = false) }
                _syncCompletedEvent.emit(Unit) // <-- Emit event here
            }
        }
    }

    fun clearData() {
        viewModelScope.launch {
            _uiState.update { it.copy(isClearing = true) }
            repository.clearCache()
            _uiState.update { it.copy(isClearing = false) }
        }
    }
}

class HomeViewModelFactory(private val context: Context) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(HomeViewModel::class.java)) {
            val db = AppDatabase.getDatabase(context)
            val repository = LinenRepository(
                linenDao = db.linenDao(),
                batchInDao = db.batchInDao(),
                batchInDetailDao = db.batchInDetailDao(),
                apiService = ApiClient.apiService,
                batchUsageDao = db.batchUsageDao(),
                batchUsageDetailDao = db.batchUsageDetailDao()
            )
            @Suppress("UNCHECKED_CAST")
            return HomeViewModel(repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}

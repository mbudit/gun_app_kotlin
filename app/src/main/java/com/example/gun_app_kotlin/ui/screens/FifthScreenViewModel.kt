package com.example.gun_app_kotlin.ui.screens

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.gun_app_kotlin.data.AppDatabase
import com.example.gun_app_kotlin.data.BatchUsage
import com.example.gun_app_kotlin.data.LinenRepository
import com.example.gun_app_kotlin.network.ApiClient
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

// A simple data class to hold the combined information for the UI
data class UiBatchUsage(
    val batchUsage: BatchUsage,
    val linenCount: Int
)

// a state holder for the screen's UI state (like syncing) ---
data class FifthScreenState(
    val batchUsages: List<UiBatchUsage> = emptyList(),
    val isSyncing: Boolean = false
)

class FifthScreenViewModel(private val repository: LinenRepository) : ViewModel() {

    private val _uiState = MutableStateFlow(FifthScreenState())
    val uiState: StateFlow<FifthScreenState> = _uiState.asStateFlow()

    init {
        // This flow will now combine the data from two separate queries
        viewModelScope.launch {
            combine(
                repository.getAllBatchUsages(),
                repository.getAllBatchUsageDetails()
            ) { usages, details ->
                // This block runs whenever 'usages' or 'details' table changes.
                val detailsGroupedByBatchId = details.groupBy { it.batchUsageId }
                usages.map { usage ->
                    UiBatchUsage(
                        batchUsage = usage,
                        linenCount = detailsGroupedByBatchId[usage.batchUsageId]?.size ?: 0
                    )
                }
            }.collect { combinedUsages ->
                _uiState.update { it.copy(batchUsages = combinedUsages) }
            }
        }
    }

    fun syncData() {
        viewModelScope.launch {
            _uiState.update { it.copy(isSyncing = true) }
            try {
                repository.refreshLinens()
            } catch (e: Exception) {
                // In a real app, you might want to show an error message
                e.printStackTrace()
            } finally {
                _uiState.update { it.copy(isSyncing = false) }
            }
        }
    }
}

class FifthScreenViewModelFactory(private val context: Context) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(FifthScreenViewModel::class.java)) {
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
            return FifthScreenViewModel(repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}

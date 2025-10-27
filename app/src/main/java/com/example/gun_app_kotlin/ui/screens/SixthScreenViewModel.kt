package com.example.gun_app_kotlin.ui.screens

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.createSavedStateHandle
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.CreationExtras
import com.example.gun_app_kotlin.data.AppDatabase
import com.example.gun_app_kotlin.data.BatchUsageDetail
import com.example.gun_app_kotlin.data.LinenRepository
import com.example.gun_app_kotlin.network.ApiClient
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.combine
import com.example.gun_app_kotlin.data.BatchUsage

data class UiLinenDetail(
    val epc: String,
    val linenType: String
)

class SixthScreenViewModel(
    repository: LinenRepository,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    // Safely get the batchId from the navigation arguments
    private val batchId: String = checkNotNull(savedStateHandle["batchId"])

    // This flow fetches the single BatchUsage object for our header.
    val batchUsage: StateFlow<BatchUsage?> =
        repository.getBatchUsageById(batchId)
            .stateIn(
                scope = viewModelScope,
                started = SharingStarted.WhileSubscribed(5000),
                initialValue = null
            )

    // This flow will observe the database for the details of our specific batchId
    val enrichedDetails: StateFlow<List<UiLinenDetail>> =
        combine(
            repository.getDetailsForBatch(batchId), // Flow 1: Details for this specific batch
            repository.getAllLinens()               // Flow 2: All linens from the 'linens' table
        ) { details, allLinens ->
            // This block executes whenever either Flow 1 or Flow 2 emits new data.

            // Create a quick lookup map of EPC to LinenType for efficiency
            val linenTypeMap = allLinens.associateBy({ it.epc }, { it.linenType })

            // Map over the details and enrich them with the linen type
            details.map { detail ->
                UiLinenDetail(
                    epc = detail.epc,
                    linenType = linenTypeMap[detail.epc] ?: "Unknown EPC" // Fallback text
                )
            }
        }.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    // Companion object for the factory
    companion object {
        val Factory: ViewModelProvider.Factory = object : ViewModelProvider.Factory {
            @Suppress("UNCHECKED_CAST")
            override fun <T : ViewModel> create(
                modelClass: Class<T>,
                extras: CreationExtras
            ): T {
                val application = checkNotNull(extras[ViewModelProvider.AndroidViewModelFactory.APPLICATION_KEY])
                val savedStateHandle = extras.createSavedStateHandle()
                val db = AppDatabase.getDatabase(application)
                val repository = LinenRepository(
                    linenDao = db.linenDao(),
                    batchInDao = db.batchInDao(),
                    batchInDetailDao = db.batchInDetailDao(),
                    apiService = ApiClient.apiService,
                    batchUsageDao = db.batchUsageDao(),
                    batchUsageDetailDao = db.batchUsageDetailDao()
                )
                return SixthScreenViewModel(repository, savedStateHandle) as T
            }
        }
    }
}

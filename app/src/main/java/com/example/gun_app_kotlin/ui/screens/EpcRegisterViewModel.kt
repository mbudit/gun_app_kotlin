package com.example.gun_app_kotlin.ui.screens

import android.content.Context
import android.media.AudioManager
import android.media.SoundPool
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.gun_app_kotlin.R
import com.example.gun_app_kotlin.data.AppDatabase
import com.example.gun_app_kotlin.data.LinenRepository
import com.example.gun_app_kotlin.network.ApiClient
import com.example.gun_app_kotlin.network.LinenRegisterRequest
import com.rscja.deviceapi.RFIDWithUHFUART
import com.rscja.deviceapi.exception.ConfigurationException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.UUID

data class EpcRegisterUiState(
    val scannedTags: Map<String, EnrichedTag> = emptyMap(),
    val isScanning: Boolean = false,
    val isSaving: Boolean = false,
    val showSaveSuccess: Boolean = false,
    val errorMessage: String? = null,
    val skippedCount: Int = 0,
    
    // Form Fields
    val linenType: String = "",
    val linenSizeCategory: String = "MEDIUM",
    val linenMaxCycle: String = "120",
    val linenHeight: String = "",
    val linenWidth: String = "",
    val linenLength: String = "",
    val linenWeight: String = "",
    val linenMaterial: String = "",
    val linenSupplier: String = "",
    val linenBudgetSource: String = "",
    val linenDescription: String = "",
    val operatorUsername: String = ""
)

class EpcRegisterViewModel(
    private val linenRepository: LinenRepository,
    initialOperatorUsername: String
) : ViewModel() {

    private lateinit var rfidReader: RFIDWithUHFUART
    private var soundPool: SoundPool? = null
    private var soundId: Int = 0

    private val _uiState = MutableStateFlow(EpcRegisterUiState(operatorUsername = initialOperatorUsername))
    val uiState = _uiState.asStateFlow()

    fun onFieldChange(field: String, value: String) {
        _uiState.update { state ->
            when (field) {
                "linenType" -> state.copy(linenType = value)
                "linenSizeCategory" -> state.copy(linenSizeCategory = value)
                "linenMaxCycle" -> state.copy(linenMaxCycle = value)
                "linenHeight" -> state.copy(linenHeight = value)
                "linenWidth" -> state.copy(linenWidth = value)
                "linenLength" -> state.copy(linenLength = value)
                "linenWeight" -> state.copy(linenWeight = value)
                "linenMaterial" -> state.copy(linenMaterial = value)
                "linenSupplier" -> state.copy(linenSupplier = value)
                "linenBudgetSource" -> state.copy(linenBudgetSource = value)
                "linenDescription" -> state.copy(linenDescription = value)
                else -> state
            }
        }
    }

    fun init(context: Context) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                rfidReader = RFIDWithUHFUART.getInstance()
                rfidReader.init(context)
                withContext(Dispatchers.Main) {
                    initSound(context)
                }
            } catch (e: Exception) {
                e.printStackTrace()
                _uiState.update { it.copy(errorMessage = "RFID Reader Initialization Failed") }
            }
        }
    }

    private fun initSound(context: Context) {
        soundPool = SoundPool(10, AudioManager.STREAM_MUSIC, 5)
        soundId = soundPool?.load(context, R.raw.barcodebeep, 1) ?: 0
    }

    private fun playSound() {
        soundPool?.play(soundId, 1f, 1f, 1, 0, 1f)
    }

    fun toggleScan() {
        if (_uiState.value.isScanning) {
            stopScan()
        } else {
            startScan()
        }
    }

    fun startScan() {
        if (_uiState.value.isScanning) return
        _uiState.update { it.copy(isScanning = true, errorMessage = null) }
        rfidReader.setInventoryCallback { tagInfo ->
            val epc = tagInfo.epc?.takeIf { it.isNotEmpty() } ?: return@setInventoryCallback
            playSound()
            viewModelScope.launch(Dispatchers.IO) {
                val existingLinen = linenRepository.findLinenByEpc(epc)
                if (existingLinen != null) {
                    _uiState.update { it.copy(skippedCount = it.skippedCount + 1) }
                    return@launch
                }

                _uiState.update { state ->
                    if (state.scannedTags.containsKey(epc)) {
                        val tag = state.scannedTags[epc]!!
                        state.copy(scannedTags = state.scannedTags + (epc to tag.copy(count = tag.count + 1)))
                    } else {
                        state.copy(scannedTags = state.scannedTags + (epc to EnrichedTag(epc, 1, null, null)))
                    }
                }
            }
        }

        viewModelScope.launch(Dispatchers.IO) {
            if (!rfidReader.startInventoryTag()) {
                _uiState.update { it.copy(isScanning = false, errorMessage = "Failed to start scanning") }
            }
        }
    }

    fun stopScan() {
        viewModelScope.launch(Dispatchers.IO) {
            rfidReader.stopInventory()
            _uiState.update { it.copy(isScanning = false) }
        }
    }

    fun reset() {
        _uiState.update { it.copy(scannedTags = emptyMap(), skippedCount = 0) }
    }

    fun registerLinens() {
        val state = _uiState.value
        if (state.scannedTags.isEmpty()) {
            _uiState.update { it.copy(errorMessage = "No tags scanned") }
            return
        }

        viewModelScope.launch {
            _uiState.update { it.copy(isSaving = true, errorMessage = null) }
            try {
                val timestamp = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.US).format(Date())
                val request = LinenRegisterRequest(
                    linenId = "LN-${UUID.randomUUID().toString().take(8)}",
                    linenType = state.linenType,
                    linenHeight = state.linenHeight.toIntOrNull() ?: 0,
                    linenWidth = state.linenWidth.toIntOrNull() ?: 0,
                    linenLength = state.linenLength.toIntOrNull() ?: 0,
                    linenMaxCycle = state.linenMaxCycle.toIntOrNull() ?: 0,
                    linenDescription = state.linenDescription,
                    linenCreatedDate = timestamp,
                    linenSizeCategory = state.linenSizeCategory,
                    linenWeight = state.linenWeight.toDoubleOrNull() ?: 0.0,
                    linenMaterial = state.linenMaterial,
                    linenSupplier = state.linenSupplier,
                    linenBudgetSource = state.linenBudgetSource,
                    operatorUsername = state.operatorUsername,
                    epcList = state.scannedTags.keys.toList()
                )

                withContext(Dispatchers.IO) {
                    linenRepository.registerLinens(request)
                    // Refresh local DB if server returns new linens
                    linenRepository.refreshLinens()
                }

                _uiState.update { 
                    it.copy(
                        isSaving = false, 
                        showSaveSuccess = true, 
                        scannedTags = emptyMap(),
                        skippedCount = 0
                    ) 
                }

            } catch (e: Exception) {
                e.printStackTrace()
                _uiState.update { it.copy(isSaving = false, errorMessage = "Registration failed: ${e.message}") }
            }
        }
    }

    fun onSaveSuccessAcknowledged() {
        _uiState.update { it.copy(showSaveSuccess = false) }
    }

    override fun onCleared() {
        super.onCleared()
        if (::rfidReader.isInitialized) rfidReader.free()
        soundPool?.release()
    }
}

class EpcRegisterViewModelFactory(
    private val context: Context,
    private val initialOperatorUsername: String
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(EpcRegisterViewModel::class.java)) {
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
            return EpcRegisterViewModel(repository, initialOperatorUsername) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}

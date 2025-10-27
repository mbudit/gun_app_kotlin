package com.example.gun_app_kotlin.ui.screens

import android.content.Context
import android.media.AudioManager
import android.media.SoundPool
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.gun_app_kotlin.R
import com.example.gun_app_kotlin.data.AppDatabase
import com.example.gun_app_kotlin.data.LinenItem
import com.example.gun_app_kotlin.data.LinenRepository
import com.example.gun_app_kotlin.network.ApiClient
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

data class BatchUpdateState(
    val scannedTags: Map<String, EnrichedTag> = emptyMap(),
    val isScanning: Boolean = false,
    val selectedLocation: String = "Slow",
    val isSaving: Boolean = false,
    val showSaveSuccess: Boolean = false
)

class ScanViewModel(private val linenRepository: LinenRepository) : ViewModel() {

    private lateinit var rfidReader: RFIDWithUHFUART
    private var soundPool: SoundPool? = null
    private var soundId: Int = 0

    private val _uiState = MutableStateFlow(BatchUpdateState())
    val uiState = _uiState.asStateFlow()

    fun onLocationSelected(newLocation: String) {
        _uiState.update { it.copy(selectedLocation = newLocation) }
    }

    fun saveScannedTags(petugasName: String) {
        viewModelScope.launch {
            _uiState.update { it.copy(isSaving = true, showSaveSuccess = false) }

            val currentState = _uiState.value
            val tagsToSave = currentState.scannedTags.values.toList()

            if (tagsToSave.isNotEmpty()) {
                withContext(Dispatchers.IO) {
                    val groupedByBatchId = tagsToSave
                        .filter { it.batchId != null }
                        .groupBy(
                            { it.batchId!! },
                            { it.epc }
                        )

                    // 2. Loop through each group to call the repository
                    groupedByBatchId.forEach { (batchId, epcsInBatch) ->
                        linenRepository.batchOutItems(
                            batchOutId = batchId,
                            epcs = epcsInBatch,
                            storageType = currentState.selectedLocation, // <-- 3. Get storageType from state
                            petugasName = petugasName // <-- 4. Use the passed-in petugasName
                        )
                    }
                    linenRepository.refreshLinens()
                }
                _uiState.update { it.copy(scannedTags = emptyMap(), showSaveSuccess = true) }
            }
            _uiState.update { it.copy(isSaving = false) }
        }
    }

    fun onSaveSuccessAcknowledged() {
        _uiState.update { it.copy(showSaveSuccess = false) }
    }

    fun init(context: Context) {
        try {
            rfidReader = RFIDWithUHFUART.getInstance()
            viewModelScope.launch(Dispatchers.IO) { rfidReader.init(context) }
            initSound(context)
        } catch (e: ConfigurationException) {
            e.printStackTrace()
        }
    }

    fun toggleScan() {
        if (_uiState.value.isScanning) {
            stopScan()
        } else {
            _uiState.update { it.copy(isScanning = true) }
            startScan()
        }
    }

    private fun startScan() {
        rfidReader.setInventoryCallback { tagInfo ->
            val epc = tagInfo.epc?.takeIf { it.isNotEmpty() } ?: return@setInventoryCallback

            playSound()
            viewModelScope.launch(Dispatchers.IO) {
                _uiState.update { currentState ->
                    val existingTag = currentState.scannedTags[epc]

                    val linenData = existingTag?.linenItem ?: linenRepository.findLinenByEpc(epc)

                    var batchId: String? = null
                    if (existingTag != null) {
                        batchId = existingTag.batchId
                    } else if (linenData != null) {
                        batchId = linenRepository.findBatchIdForLinen(linenData.epc)
                    }

                    val newCount = (existingTag?.count ?: 0) + 1

                    val updatedTag = EnrichedTag(
                        epc = epc,
                        count = newCount,
                        linenItem = linenData,
                        batchId = batchId
                    )

                    currentState.copy(scannedTags = currentState.scannedTags + (epc to updatedTag))
                }
            }
        }

        viewModelScope.launch(Dispatchers.IO) {
            if (!rfidReader.startInventoryTag()) {
                _uiState.update { it.copy(isScanning = false) }
            }
        }
    }


    private fun stopScan() {
        viewModelScope.launch(Dispatchers.IO) {
            rfidReader.stopInventory()
            _uiState.update { it.copy(isScanning = false) }
        }
    }

    fun clearScannedList() {
        _uiState.update { it.copy(scannedTags = emptyMap()) }
    }

    private fun initSound(context: Context) {
        val audioManager = context.getSystemService(Context.AUDIO_SERVICE) as AudioManager
        soundPool = SoundPool(10, AudioManager.STREAM_MUSIC, 5)
        soundId = soundPool?.load(context, R.raw.barcodebeep, 1) ?: 0
    }

    private fun playSound() {
        soundPool?.play(soundId, 1f, 1f, 1, 0, 1f)
    }

    override fun onCleared() {
        super.onCleared()
        if (::rfidReader.isInitialized) {
            rfidReader.free()
        }
        soundPool?.release()
    }
}

class ScanViewModelFactory(private val context: Context) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(ScanViewModel::class.java)) {
            val db = AppDatabase.getDatabase(context)
            // --- UPDATE THIS LINE ---
            val repository = LinenRepository(
                linenDao = db.linenDao(),
                batchInDao = db.batchInDao(),
                batchInDetailDao = db.batchInDetailDao(),
                apiService = ApiClient.apiService,
                batchUsageDao = db.batchUsageDao(),
                batchUsageDetailDao = db.batchUsageDetailDao()
            )
            @Suppress("UNCHECKED_CAST")
            return ScanViewModel(repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}

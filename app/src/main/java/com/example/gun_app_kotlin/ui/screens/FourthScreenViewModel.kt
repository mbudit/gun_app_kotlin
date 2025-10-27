package com.example.gun_app_kotlin.ui.screens

import android.content.Context
import android.media.AudioManager
import android.media.SoundPool
import androidx.compose.ui.text.intl.Locale
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.gun_app_kotlin.R
import com.example.gun_app_kotlin.data.AppDatabase
import com.example.gun_app_kotlin.data.BatchIdManager
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
import kotlin.text.format
import java.text.SimpleDateFormat
import java.util.Date

// State specific to the Fourth Screen
data class SerahTerimaState(
    val scannedTags: Map<String, EnrichedTag> = emptyMap(),
    val isScanning: Boolean = false,
    val petugasName: String = "",
    val lokasiPenerima: String = "", // <-- ADD THIS
    val namaPenerima: String = "",   // <-- ADD THIS
    val isSaving: Boolean = false,
    val showSaveSuccess: Boolean = false
)


class FourthScreenViewModel(
    private val linenRepository: LinenRepository,
    initialPetugasName: String,
    private val batchIdManager: BatchIdManager
) : ViewModel() {

    private lateinit var rfidReader: RFIDWithUHFUART
    private var soundPool: SoundPool? = null
    private var soundId: Int = 0

    private val _uiState = MutableStateFlow(SerahTerimaState(petugasName = initialPetugasName))
    val uiState = _uiState.asStateFlow()

    fun onLokasiPenerimaChanged(lokasi: String) {
        _uiState.update { it.copy(lokasiPenerima = lokasi) }
    }

    fun onNamaPenerimaChanged(nama: String) {
        _uiState.update { it.copy(namaPenerima = nama) }
    }

    fun saveScannedTags() {
        viewModelScope.launch {
            _uiState.update { it.copy(isSaving = true, showSaveSuccess = false) }
            val currentState = _uiState.value

            // Gather all required data
            val epcsToUpdate = currentState.scannedTags.keys.toList()
            val petugas = currentState.petugasName
            val receiverName = currentState.namaPenerima
            val receiverLocation = currentState.lokasiPenerima

            // --- 2. GET THE NEXT UNIQUE BATCH ID FROM THE MANAGER ---
            val newBatchId = batchIdManager.getNextBatchId()

            if (epcsToUpdate.isNotEmpty() && petugas.isNotBlank() && receiverName.isNotBlank() && receiverLocation.isNotBlank()) {
                withContext(Dispatchers.IO) {
                    try {
                        // 3. Call the repository function with the NEWLY generated ID
                        linenRepository.createBatchUsage(
                            batchUsageId = newBatchId, // <-- USE THE NEW ID
                            epcs = epcsToUpdate,
                            petugasName = petugas,
                            receiverName = receiverName,
                            receiverLocation = receiverLocation
                        )
                        linenRepository.refreshLinens()
                    } catch (e: Exception) {
                        e.printStackTrace()
                    }
                }
            }

            // After all IO work is done, update the UI state and reset fields
            _uiState.update {
                it.copy(
                    scannedTags = emptyMap(),
                    lokasiPenerima = "",
                    namaPenerima = "",
                    isSaving = false,
                    showSaveSuccess = true
                )
            }
        }
    }

    fun onSaveSuccessAcknowledged() { _uiState.update { it.copy(showSaveSuccess = false) } }
    fun clearScannedList() { _uiState.update { it.copy(scannedTags = emptyMap()) } }

    fun init(context: Context) {
        try {
            rfidReader = RFIDWithUHFUART.getInstance()
            viewModelScope.launch(Dispatchers.IO) { rfidReader.init(context) }
            initSound(context)
        } catch (e: ConfigurationException) { e.printStackTrace() }
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
                    val newCount = (existingTag?.count ?: 0) + 1
                    val updatedTag = EnrichedTag(epc = epc, count = newCount, linenItem = linenData, batchId = null)
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

    private fun initSound(context: Context) {
        soundPool = SoundPool(10, AudioManager.STREAM_MUSIC, 5)
        soundId = soundPool?.load(context, R.raw.barcodebeep, 1) ?: 0
    }

    private fun playSound() { soundPool?.play(soundId, 1f, 1f, 1, 0, 1f) }

    override fun onCleared() {
        super.onCleared()
        if (::rfidReader.isInitialized) rfidReader.free()
        soundPool?.release()
    }
}

class FourthScreenViewModelFactory(
    private val context: Context,
    private val initialPetugasName: String
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(FourthScreenViewModel::class.java)) {
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
            val batchIdManager = BatchIdManager(context.applicationContext)
            @Suppress("UNCHECKED_CAST")
            return FourthScreenViewModel(repository, initialPetugasName, batchIdManager) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
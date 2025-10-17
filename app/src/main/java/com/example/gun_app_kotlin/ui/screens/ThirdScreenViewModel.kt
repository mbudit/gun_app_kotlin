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

data class StatusUpdateState(
    val scannedTags: Map<String, EnrichedTag> = emptyMap(),
    val isScanning: Boolean = false,
    val petugasName: String = "",
    val isSaving: Boolean = false,
    val showSaveSuccess: Boolean = false
)

class ThirdScreenViewModel(private val linenRepository: LinenRepository) : ViewModel() {

    private lateinit var rfidReader: RFIDWithUHFUART
    private var soundPool: SoundPool? = null
    private var soundId: Int = 0

    private val _uiState = MutableStateFlow(StatusUpdateState())
    val uiState = _uiState.asStateFlow()

    fun onPetugasNameChanged(name: String) {
        _uiState.update { it.copy(petugasName = name) }
    }

    fun saveScannedTags() {
        viewModelScope.launch { // Launch on the main thread
            _uiState.update { it.copy(isSaving = true, showSaveSuccess = false) }

            val epcsToUpdate = _uiState.value.scannedTags.keys.toList()
            val petugas = _uiState.value.petugasName

            // Perform network operations in the IO dispatcher
            withContext(Dispatchers.IO) {
                if (epcsToUpdate.isNotEmpty() && petugas.isNotBlank()) {
                    linenRepository.setIntransitForTags(epcsToUpdate, petugas)
                    linenRepository.refreshLinens()
                }
            }

            // After all IO work is done, update the UI state back on the main thread
            _uiState.update {
                it.copy(
                    scannedTags = emptyMap(),
                    petugasName = "",
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

                    val updatedTag = EnrichedTag(
                        epc = epc,
                        count = newCount,
                        linenItem = linenData, // Storing the whole object is correct
                        batchId = null
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


class ThirdScreenViewModelFactory(private val context: Context) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(ThirdScreenViewModel::class.java)) {
            val db = AppDatabase.getDatabase(context)
            val repository = LinenRepository(
                linenDao = db.linenDao(),
                batchInDao = db.batchInDao(),
                batchInDetailDao = db.batchInDetailDao(),
                apiService = ApiClient.apiService
            )
            @Suppress("UNCHECKED_CAST")
            return ThirdScreenViewModel(repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}

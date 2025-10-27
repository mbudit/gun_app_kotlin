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
import com.rscja.deviceapi.RFIDWithUHFUART
import com.rscja.deviceapi.exception.ConfigurationException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class EpcScanState(
    val scannedEpcs: Set<String> = emptySet(),
    val isScanning: Boolean = false
)

class EpcScanViewModel() : ViewModel() {

    private lateinit var rfidReader: RFIDWithUHFUART
    private var soundPool: SoundPool? = null
    private var soundId: Int = 0

    private val _uiState = MutableStateFlow(EpcScanState())
    val uiState = _uiState.asStateFlow()

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
            _uiState.update { currentState ->
                currentState.copy(scannedEpcs = currentState.scannedEpcs + epc)
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
        _uiState.update { it.copy(scannedEpcs = emptySet()) }
    }

    private fun initSound(context: Context) {
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

// Factory for the new ViewModel
class EpcScanViewModelFactory(private val context: Context) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(EpcScanViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return EpcScanViewModel() as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}

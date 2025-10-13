package com.example.gun_app_kotlin.ui.screens

import android.content.Context
import android.media.AudioManager
import android.media.SoundPool
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.gun_app_kotlin.R // Important: Import your R file
import com.rscja.deviceapi.RFIDWithUHFUART
import com.rscja.deviceapi.entity.UHFTAGInfo
import com.rscja.deviceapi.exception.ConfigurationException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

// Data class to hold the state of our UI
data class ScanScreenState(
    val uniqueTags: Map<String, UHFTAGInfo> = emptyMap(),
    val isScanning: Boolean = false,
    val totalReads: Int = 0
)

class ScanViewModel : ViewModel() {

    private lateinit var rfidReader: RFIDWithUHFUART

    // --- SOUND START: Add SoundPool variables ---
    private var soundPool: SoundPool? = null
    private var soundId: Int = 0
    private var audioManager: AudioManager? = null
    // --- SOUND END ---


    // Private mutable state flow that can be updated from the ViewModel
    private val _uiState = MutableStateFlow(ScanScreenState())
    // Public immutable state flow that the UI can observe
    val uiState = _uiState.asStateFlow()

    // Function to initialize the RFID reader
    fun init(context: Context) {
        try {
            rfidReader = RFIDWithUHFUART.getInstance()
            viewModelScope.launch(Dispatchers.IO) { // Run initialization in the background
                rfidReader.init(context)
            }
            // --- SOUND START: Initialize SoundPool here ---
            initSound(context)
            // --- SOUND END ---
        } catch (e: ConfigurationException) {
            e.printStackTrace()
        }
    }

    // --- SOUND START: Create the initSound function ---
    private fun initSound(context: Context) {
        soundPool = SoundPool(10, AudioManager.STREAM_MUSIC, 5)
        soundId = soundPool?.load(context, R.raw.barcodebeep, 1) ?: 0
        audioManager = context.getSystemService(Context.AUDIO_SERVICE) as AudioManager
    }
    // --- SOUND END ---

    // --- SOUND START: Create a function to play the sound ---
    private fun playSound() {
        val audioMaxVolume = audioManager?.getStreamMaxVolume(AudioManager.STREAM_MUSIC)?.toFloat() ?: 0f
        val audioCurrentVolume = audioManager?.getStreamVolume(AudioManager.STREAM_MUSIC)?.toFloat() ?: 0f
        val volumeRatio = audioCurrentVolume / audioMaxVolume
        soundPool?.play(soundId, volumeRatio, volumeRatio, 1, 0, 1f)
    }
    // --- SOUND END ---

    fun toggleScan() {
        if (_uiState.value.isScanning) {
            stopScan()
        } else {
            startScan()
        }
    }

    private fun startScan() {
        // Clear previous data
        _uiState.update { it.copy(uniqueTags = emptyMap(), totalReads = 0) }

        // This callback is the heart of the scanning process.
        // It runs on a background thread every time a tag is detected.
        rfidReader.setInventoryCallback { tagInfo ->
            // --- SOUND START: Play sound on every successful read ---
            playSound()
            // --- SOUND END ---

            // Use update to safely modify the state
            _uiState.update { currentState ->
                val existingTag = currentState.uniqueTags[tagInfo.epc]
                val newCount = (existingTag?.count ?: 0) + 1
                val updatedTag = tagInfo.apply { count = newCount }

                currentState.copy(
                    uniqueTags = currentState.uniqueTags + (tagInfo.epc to updatedTag),
                    totalReads = currentState.totalReads + 1
                )
            }
        }

        // Start the hardware scan
        viewModelScope.launch(Dispatchers.IO) {
            if (rfidReader.startInventoryTag()) {
                _uiState.update { it.copy(isScanning = true) }
            }
        }
    }

    private fun stopScan() {
        viewModelScope.launch(Dispatchers.IO) {
            rfidReader.stopInventory()
            _uiState.update { it.copy(isScanning = false) }
        }
    }

    // This method is called when the screen is destroyed
    override fun onCleared() {
        super.onCleared()
        if (::rfidReader.isInitialized) {
            rfidReader.free()
        }
        // --- SOUND START: Release SoundPool resources ---
        soundPool?.release()
        soundPool = null
        // --- SOUND END ---
    }
}

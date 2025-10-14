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
import com.rscja.deviceapi.entity.UHFTAGInfo
import com.rscja.deviceapi.exception.ConfigurationException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

// Data classes EnrichedLinenTag and ScanScreenState remain the same
data class EnrichedLinenTag(
    val uhfTagInfo: UHFTAGInfo,
    val linenItem: LinenItem?
)

data class ScanScreenState(
    val scannedItems: Map<String, EnrichedLinenTag> = emptyMap(),
    val isScanning: Boolean = false,
    val totalReads: Int = 0,
    val isSyncing: Boolean = false // To show a sync progress indicator
)

// The ViewModel now takes the repository as a parameter
class ScanViewModel(private val linenRepository: LinenRepository) : ViewModel() {

    private lateinit var rfidReader: RFIDWithUHFUART
    private var soundPool: SoundPool? = null
    private var soundId: Int = 0
    private var audioManager: AudioManager? = null

    private val _uiState = MutableStateFlow(ScanScreenState())
    val uiState = _uiState.asStateFlow()

    init {
        // Automatically sync data when the ViewModel is first created.
        // This ensures the local database has fresh data when the app starts.
        syncData()
    }

    // Public function to trigger a data sync from the UI
    fun syncData() {
        viewModelScope.launch(Dispatchers.IO) {
            _uiState.update { it.copy(isSyncing = true) }
            linenRepository.refreshLinens()
            _uiState.update { it.copy(isSyncing = false) }
        }
    }

    fun init(context: Context) {
        try {
            rfidReader = RFIDWithUHFUART.getInstance()
            viewModelScope.launch(Dispatchers.IO) {
                rfidReader.init(context)
            }
            initSound(context)
        } catch (e: ConfigurationException) {
            e.printStackTrace()
        }
    }

    fun toggleScan() {
        if (_uiState.value.isScanning) {
            stopScan()
        } else {
            startScan()
        }
    }

    private fun startScan() {
        // Clear previous scan data before starting a new session
        _uiState.update { it.copy(scannedItems = emptyMap(), totalReads = 0) }

        // This callback is fired by the hardware for every tag it sees
        rfidReader.setInventoryCallback { tagInfo ->
            playSound()

            // Launch a coroutine to process the tag without blocking the hardware callback
            viewModelScope.launch(Dispatchers.IO) {

                // Get the current state once to work with it
                val currentState = _uiState.value
                val epc = tagInfo.epc
                val existingEnrichedTag = currentState.scannedItems[epc]

                // 1. Look up the linen data from the repository (which queries Room)
                //    We only do this once per unique tag to be efficient.
                val linenData = existingEnrichedTag?.linenItem ?: linenRepository.findLinenByEpc(epc)

                // 2. Determine the new total count for this tag
                val newCount = (existingEnrichedTag?.uhfTagInfo?.count ?: 0) + 1
                tagInfo.count = newCount

                // 3. Create the new/updated enriched object containing both scan and DB info
                val newEnrichedTag = EnrichedLinenTag(
                    uhfTagInfo = tagInfo,
                    linenItem = linenData
                )

                // 4. Update the UI state with the new information
                _uiState.update { state ->
                    state.copy(
                        scannedItems = state.scannedItems + (epc to newEnrichedTag),
                        totalReads = state.totalReads + 1
                    )
                }
            }
        }

        // This part correctly starts the hardware scanner
        viewModelScope.launch(Dispatchers.IO) {
            if (rfidReader.startInventoryTag()) {
                _uiState.update { it.copy(isScanning = true) }
            } else {
                // Optional: Handle case where scanner fails to start
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
        audioManager = context.getSystemService(Context.AUDIO_SERVICE) as AudioManager
    }

    private fun playSound() {
        val audioMaxVolume = audioManager?.getStreamMaxVolume(AudioManager.STREAM_MUSIC)?.toFloat() ?: 0f
        val audioCurrentVolume = audioManager?.getStreamVolume(AudioManager.STREAM_MUSIC)?.toFloat() ?: 0f
        val volumeRatio = audioCurrentVolume / audioMaxVolume
        soundPool?.play(soundId, volumeRatio, volumeRatio, 1, 0, 1f)
    }

    override fun onCleared() {
        super.onCleared()
        if (::rfidReader.isInitialized) {
            rfidReader.free()
        }
        soundPool?.release()
        soundPool = null
    }
}


/**
 * NEW: ViewModel Factory
 * This is crucial. It tells the system how to create a ScanViewModel since it now
 * has a constructor parameter (the repository).
 */
class ScanViewModelFactory(private val context: Context) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(ScanViewModel::class.java)) {
            // This is where we build the real repository instance
            val repository = LinenRepository(
                linenDao = AppDatabase.getDatabase(context).linenDao(),
                apiService = ApiClient.apiService
            )
            @Suppress("UNCHECKED_CAST")
            return ScanViewModel(repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}

package com.example.gun_app_kotlin.ui.screens

import android.view.KeyEvent
import androidx.compose.foundation.focusable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.input.key.onKeyEvent
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel

@OptIn(ExperimentalComposeUiApi::class, ExperimentalMaterial3Api::class)
@Composable
fun ThirdScreen(onNavigateUp: () -> Unit) {
    val viewModel: ThirdScreenViewModel = viewModel(factory = ThirdScreenViewModelFactory(LocalContext.current.applicationContext))
    val context = LocalContext.current
    val uiState by viewModel.uiState.collectAsState()
    val focusRequester = remember { FocusRequester() }
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(uiState.showSaveSuccess) {
        if (uiState.showSaveSuccess) {
            snackbarHostState.showSnackbar("Status updated successfully!")
            viewModel.onSaveSuccessAcknowledged()
        }
    }

    DisposableEffect(Unit) {
        viewModel.init(context)
        onDispose {}
    }

    LaunchedEffect(Unit) {
        focusRequester.requestFocus()
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Linen Keluar") },
                navigationIcon = {
                    IconButton(onClick = onNavigateUp) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, "Navigate back")
                    }
                }
            )
        },
        snackbarHost = { SnackbarHost(hostState = snackbarHostState) }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .focusRequester(focusRequester)
                .focusable()
                .onKeyEvent { event ->
                    if (event.nativeKeyEvent.action == KeyEvent.ACTION_DOWN &&
                        event.nativeKeyEvent.keyCode in listOf(139, 280, 291, 293, 294)
                    ) {
                        focusRequester.requestFocus()
                        viewModel.toggleScan()
                        return@onKeyEvent true
                    }
                    false
                }
        ) {
            // These functions are now called from SharedComposables.kt
            BatchScanHeader(
                isScanning = uiState.isScanning,
                uniqueCount = uiState.scannedTags.size
            )

            OutlinedTextField(
                value = uiState.petugasName,
                onValueChange = { viewModel.onPetugasNameChanged(it) },
                label = { Text("Nama Petugas") },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                singleLine = true,
                enabled = !uiState.isScanning
            )

            BatchListHeader()

            if (uiState.scannedTags.isEmpty()) {
                val message = when {
                    uiState.isScanning -> "Scanning for tags..."
                    uiState.petugasName.isBlank() -> "Masukkan nama petugas untuk memulai."
                    else -> "Tekan trigger untuk memulai scan."
                }
                EmptyState(message = message)
            } else {
                LazyColumn(modifier = Modifier.weight(1f)) {
                    items(
                        items = uiState.scannedTags.values.toList().sortedByDescending { it.count },
                        key = { it.epc }
                    ) { tag ->
                        BatchListItem(tag = tag)
                        HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.2f))
                    }
                }
            }

            SaveButtonBottomBar(
                onSave = { viewModel.saveScannedTags() },
                onClear = { viewModel.clearScannedList() },
                isSaving = uiState.isSaving,
                hasItems = uiState.scannedTags.isNotEmpty() && uiState.petugasName.isNotBlank()
            )
        }
    }
}

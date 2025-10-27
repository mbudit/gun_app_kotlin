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
fun FourthScreen(
    onNavigateUp: () -> Unit,
    sessionViewModel: SessionViewModel = viewModel()
) {
    val context = LocalContext.current
    val sessionState by sessionViewModel.uiState.collectAsState()
    val userName = sessionState.currentUser?.name ?: ""

    val viewModel: FourthScreenViewModel = viewModel(
        factory = FourthScreenViewModelFactory(context.applicationContext, userName)
    )

    val uiState by viewModel.uiState.collectAsState()
    val focusRequester = remember { FocusRequester() }
    val snackbarHostState = remember { SnackbarHostState() }

    // Effects for initialization and showing snackbars
    LaunchedEffect(uiState.showSaveSuccess) {
        if (uiState.showSaveSuccess) {
            snackbarHostState.showSnackbar("Data 'Serah Terima' berhasil disimpan!")
            viewModel.onSaveSuccessAcknowledged()
        }
    }

    DisposableEffect(Unit) {
        viewModel.init(context)
        onDispose { /* Cleanup if needed */ }
    }

    LaunchedEffect(Unit) {
        focusRequester.requestFocus()
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Serah Terima") },
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
                    // Hardware trigger handling
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
            // Reusing the same header components
            BatchScanHeader(
                isScanning = uiState.isScanning,
                userName = userName,
                uniqueCount = uiState.scannedTags.size
            )

            OutlinedTextField(
                value = uiState.lokasiPenerima,
                onValueChange = viewModel::onLokasiPenerimaChanged,
                label = { Text("Lokasi Penerima") },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                singleLine = true,
                enabled = !uiState.isScanning
            )

            OutlinedTextField(
                value = uiState.namaPenerima,
                onValueChange = viewModel::onNamaPenerimaChanged,
                label = { Text("Nama Penerima") },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                singleLine = true,
                enabled = !uiState.isScanning
            )

            BatchListHeaderThirdScreen()

            if (uiState.scannedTags.isEmpty()) {
                val message = when {
                    uiState.isScanning -> "Scanning for tags..."
                    uiState.lokasiPenerima.isBlank() -> "Isi lokasi penerima untuk memulai."
                    uiState.namaPenerima.isBlank() -> "Isi nama penerima untuk memulai."
                    else -> "Tekan trigger untuk memulai scan."
                }
                EmptyState(message = message)
            } else {
                LazyColumn(modifier = Modifier.weight(1f)) {
                    items(
                        items = uiState.scannedTags.values.toList().sortedByDescending { it.count },
                        key = { it.epc }
                    ) { tag ->
                        BatchListItem(tag = tag, showBatchId = false)
                        HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.2f))
                    }
                }
            }

            // Reusing the same bottom bar
            SaveButtonBottomBar(
                onSave = { viewModel.saveScannedTags() },
                onClear = { viewModel.clearScannedList() },
                isSaving = uiState.isSaving,
                hasItems = uiState.scannedTags.isNotEmpty() && uiState.lokasiPenerima.isNotBlank() && uiState.namaPenerima.isNotBlank()
            )
        }
    }
}

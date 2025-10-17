package com.example.gun_app_kotlin.ui.screens

import android.view.KeyEvent
import androidx.compose.foundation.focusable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.input.key.onKeyEvent
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel

@OptIn(ExperimentalComposeUiApi::class, ExperimentalMaterial3Api::class)
@Composable
fun EpcScanScreen(onNavigateUp: () -> Unit) {
    val viewModel: EpcScanViewModel = viewModel(factory = EpcScanViewModelFactory(LocalContext.current.applicationContext))
    val context = LocalContext.current
    val uiState by viewModel.uiState.collectAsState()
    val focusRequester = remember { FocusRequester() }

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
                title = { Text("Identifikasi EPC") },
                navigationIcon = {
                    IconButton(onClick = onNavigateUp) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, "Navigate back")
                    }
                }
            )
        },
        bottomBar = {
            BottomAppBar {
                Spacer(modifier = Modifier.weight(1f))
                Button(
                    onClick = { viewModel.clearScannedList() },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.errorContainer,
                        contentColor = MaterialTheme.colorScheme.onErrorContainer
                    )
                ) {
                    Icon(Icons.Default.Delete, "Clear Icon")
                    Spacer(Modifier.size(ButtonDefaults.IconSpacing))
                    Text("Clear List")
                }
                Spacer(modifier = Modifier.weight(1f))
            }
        }
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
            // Header showing scan status and count
            BatchScanHeader(isScanning = uiState.isScanning, uniqueCount = uiState.scannedEpcs.size)

            if (uiState.scannedEpcs.isEmpty()) {
                val message = if (uiState.isScanning) "Memindai EPC..." else "Tekan trigger untuk memulai scan."
                EmptyState(message = message)
            } else {
                LazyColumn(modifier = Modifier.weight(1f)) {
                    items(uiState.scannedEpcs.toList()) { epc ->
                        Text(
                            text = epc,
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp, vertical = 12.dp),
                            textAlign = TextAlign.Center,
                            style = MaterialTheme.typography.bodyLarge
                        )
                        HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.2f))
                    }
                }
            }
        }
    }
}

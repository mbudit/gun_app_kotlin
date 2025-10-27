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
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
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
fun SecondScreen(
    onNavigateUp: () -> Unit,
    sessionViewModel: SessionViewModel = viewModel(),
    scanViewModel: ScanViewModel = viewModel(factory = ScanViewModelFactory(LocalContext.current.applicationContext))
) {
    val context = LocalContext.current
    val uiState by scanViewModel.uiState.collectAsState()
    val focusRequester = remember { FocusRequester() }
    val snackbarHostState = remember { SnackbarHostState() }
    val sessionState by sessionViewModel.uiState.collectAsState()
    val userName = sessionState.currentUser?.name ?: ""

    LaunchedEffect(uiState.showSaveSuccess) {
        if (uiState.showSaveSuccess) {
            snackbarHostState.showSnackbar(
                message = "Data successfully saved to database!",
                duration = SnackbarDuration.Short
            )
            scanViewModel.onSaveSuccessAcknowledged()
        }
    }

    DisposableEffect(Unit) {
        scanViewModel.init(context)
        onDispose {}
    }

    LaunchedEffect(Unit) {
        focusRequester.requestFocus()
    }

    Scaffold(
        topBar = {
            TopAppBar(title = { Text("Masuk Gudang") },
                navigationIcon = {
                    IconButton(onClick = onNavigateUp) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, "Navigate back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface,
                    titleContentColor = MaterialTheme.colorScheme.onSurface,
                    navigationIconContentColor = MaterialTheme.colorScheme.onSurface
                )
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
                        scanViewModel.toggleScan()
                        return@onKeyEvent true
                    }
                    false
                }
        ) {
            BatchScanHeader(
                isScanning = uiState.isScanning,
                userName = userName, // <-- Pass the name here
                uniqueCount = uiState.scannedTags.size
            )


            StorageLocationDropdown(
                selectedLocation = uiState.selectedLocation,
                onLocationSelected = { scanViewModel.onLocationSelected(it) },
                isEnabled = !uiState.isScanning
            )

            BatchListHeader()

            if (uiState.scannedTags.isEmpty()) {
                val message = if (uiState.isScanning) "Memindai tag..." else "Pilih lokasi gudang dan mulai scan untuk menambahkan tag."
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
                onSave = { scanViewModel.saveScannedTags(userName) }, // <-- Pass the userName here
                onClear = { scanViewModel.clearScannedList() },
                isSaving = uiState.isSaving,
                hasItems = uiState.scannedTags.isNotEmpty()
            )
        }
    }
}

// This is the only composable that is unique to SecondScreen
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StorageLocationDropdown(
    selectedLocation: String,
    onLocationSelected: (String) -> Unit,
    isEnabled: Boolean
) {
    val options = listOf("Slow", "Fast")
    var expanded by remember { mutableStateOf(false) }

    ExposedDropdownMenuBox(
        expanded = expanded,
        onExpandedChange = { if (isEnabled) expanded = !expanded },
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp)
    ) {
        OutlinedTextField(
            value = selectedLocation,
            onValueChange = {},
            readOnly = true,
            label = { Text("Lokasi gudang") },
            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
            modifier = Modifier
                .menuAnchor()
                .fillMaxWidth(),
            enabled = isEnabled
        )
        ExposedDropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false }
        ) {
            options.forEach { selectionOption ->
                DropdownMenuItem(
                    text = { Text(selectionOption) },
                    onClick = {
                        onLocationSelected(selectionOption)
                        expanded = false
                    }
                )
            }
        }
    }
}

package com.example.gun_app_kotlin.ui.screens

import android.view.KeyEvent
import androidx.compose.foundation.focusable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.AppRegistration
import androidx.compose.material.icons.filled.DeleteSweep
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.input.key.onKeyEvent
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel

@OptIn(ExperimentalComposeUiApi::class, ExperimentalMaterial3Api::class)
@Composable
fun EpcRegisterScreen(
    onNavigateUp: () -> Unit,
    sessionViewModel: SessionViewModel = viewModel()
) {
    val context = LocalContext.current
    val sessionState by sessionViewModel.uiState.collectAsState()
    val operatorUsername = sessionState.currentUser?.username ?: ""

    val viewModel: EpcRegisterViewModel = viewModel(
        factory = EpcRegisterViewModelFactory(context.applicationContext, operatorUsername)
    )

    val uiState by viewModel.uiState.collectAsState()
    val focusRequester = remember { FocusRequester() }
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(uiState.showSaveSuccess) {
        if (uiState.showSaveSuccess) {
            snackbarHostState.showSnackbar("Linen batch successfully registered!")
            viewModel.onSaveSuccessAcknowledged()
        }
    }

    LaunchedEffect(uiState.errorMessage) {
        uiState.errorMessage?.let {
            snackbarHostState.showSnackbar(it)
        }
    }

    DisposableEffect(Unit) {
        viewModel.init(context)
        onDispose { }
    }

    LaunchedEffect(Unit) {
        focusRequester.requestFocus()
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("EPC Registration") },
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
                    val triggerKeyCodes = listOf(139, 280, 291, 293, 294)
                    if (event.nativeKeyEvent.keyCode in triggerKeyCodes) {
                        if (event.nativeKeyEvent.action == KeyEvent.ACTION_DOWN) {
                            if (event.nativeKeyEvent.repeatCount == 0) {
                                focusRequester.requestFocus()
                                viewModel.startScan()
                            }
                            return@onKeyEvent true
                        } else if (event.nativeKeyEvent.action == KeyEvent.ACTION_UP) {
                            viewModel.stopScan()
                            return@onKeyEvent true
                        }
                    }
                    false
                }
        ) {
            // Header with operator info and tag counts
            BatchScanHeader(
                isScanning = uiState.isScanning,
                userName = uiState.operatorUsername,
                uniqueCount = uiState.scannedTags.size
            )
            
            if (uiState.skippedCount > 0) {
                Text(
                    text = "${uiState.skippedCount} EPCs already exist and were skipped.",
                    modifier = Modifier.padding(horizontal = 16.dp),
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.labelSmall
                )
            }

            // Tabs for Form vs Scanned Tags
            var selectedTab by remember { mutableStateOf(0) }
            TabRow(selectedTabIndex = selectedTab) {
                Tab(selected = selectedTab == 0, onClick = { selectedTab = 0 }) {
                    Text("Form", modifier = Modifier.padding(16.dp))
                }
                Tab(selected = selectedTab == 1, onClick = { selectedTab = 1 }) {
                    Text("Scanned (${uiState.scannedTags.size})", modifier = Modifier.padding(16.dp))
                }
            }

            Column(modifier = Modifier.weight(1f)) {
                if (selectedTab == 0) {
                    RegistrationForm(uiState, viewModel::onFieldChange)
                } else {
                    ScannedTagsList(uiState.scannedTags, uiState.isScanning)
                }
            }

            RegistrationActions(
                isSaving = uiState.isSaving,
                hasItems = uiState.scannedTags.isNotEmpty(),
                onReset = { viewModel.reset() },
                onRegister = { viewModel.registerLinens() }
            )
        }
    }
}

@Composable
fun RegistrationForm(
    uiState: EpcRegisterUiState,
    onFieldChange: (String, String) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        RegistrationTextField("Linen Type", uiState.linenType) { onFieldChange("linenType", it) }
        
        SizeCategoryDropdown(uiState.linenSizeCategory) { onFieldChange("linenSizeCategory", it) }
        
        MaxCycleDropdown(uiState.linenMaxCycle) { onFieldChange("linenMaxCycle", it) }
        
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            RegistrationTextField("Height (cm)", uiState.linenHeight, Modifier.weight(1f), KeyboardType.Number) { onFieldChange("linenHeight", it) }
            RegistrationTextField("Width (cm)", uiState.linenWidth, Modifier.weight(1f), KeyboardType.Number) { onFieldChange("linenWidth", it) }
            RegistrationTextField("Length (cm)", uiState.linenLength, Modifier.weight(1f), KeyboardType.Number) { onFieldChange("linenLength", it) }
        }
        
        RegistrationTextField("Weight (kg)", uiState.linenWeight, keyboardType = KeyboardType.Decimal) { onFieldChange("linenWeight", it) }
        RegistrationTextField("Material", uiState.linenMaterial) { onFieldChange("linenMaterial", it) }
        RegistrationTextField("Supplier", uiState.linenSupplier) { onFieldChange("linenSupplier", it) }
        RegistrationTextField("Budget Source", uiState.linenBudgetSource) { onFieldChange("linenBudgetSource", it) }
        RegistrationTextField("Description", uiState.linenDescription, singleLine = false) { onFieldChange("linenDescription", it) }
    }
}

@Composable
fun RegistrationTextField(
    label: String,
    value: String,
    modifier: Modifier = Modifier,
    keyboardType: KeyboardType = KeyboardType.Text,
    singleLine: Boolean = true,
    onValueChange: (String) -> Unit
) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        label = { Text(label) },
        modifier = modifier.fillMaxWidth(),
        keyboardOptions = KeyboardOptions(keyboardType = keyboardType),
        singleLine = singleLine
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SizeCategoryDropdown(selected: String, onSelected: (String) -> Unit) {
    var expanded by remember { mutableStateOf(false) }
    val options = listOf("SMALL", "MEDIUM", "LARGE")
    
    ExposedDropdownMenuBox(
        expanded = expanded,
        onExpandedChange = { expanded = !expanded }
    ) {
        OutlinedTextField(
            value = selected,
            onValueChange = {},
            readOnly = true,
            label = { Text("Size Category") },
            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
            modifier = Modifier.menuAnchor().fillMaxWidth()
        )
        ExposedDropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false }
        ) {
            options.forEach { option ->
                DropdownMenuItem(
                    text = { Text(option) },
                    onClick = {
                        onSelected(option)
                        expanded = false
                    }
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MaxCycleDropdown(selected: String, onSelected: (String) -> Unit) {
    var expanded by remember { mutableStateOf(false) }
    val options = listOf("50", "120", "200")
    
    ExposedDropdownMenuBox(
        expanded = expanded,
        onExpandedChange = { expanded = !expanded }
    ) {
        OutlinedTextField(
            value = selected,
            onValueChange = {},
            readOnly = true,
            label = { Text("Max Cycle") },
            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
            modifier = Modifier.menuAnchor().fillMaxWidth()
        )
        ExposedDropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false }
        ) {
            options.forEach { option ->
                DropdownMenuItem(
                    text = { Text(option) },
                    onClick = {
                        onSelected(option)
                        expanded = false
                    }
                )
            }
        }
    }
}

@Composable
fun ScannedTagsList(scannedTags: Map<String, EnrichedTag>, isScanning: Boolean) {
    if (scannedTags.isEmpty()) {
        EmptyState(if (isScanning) "Scanning for new tags..." else "Start scanning to see tags.")
    } else {
        LazyColumn(modifier = Modifier.fillMaxSize()) {
            items(scannedTags.values.toList(), key = { it.epc }) { tag ->
                ListItem(
                    headlineContent = { Text(tag.epc) },
                    supportingContent = { Text("Read Count: ${tag.count}") }
                )
                HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.2f))
            }
        }
    }
}

@Composable
fun RegistrationActions(
    isSaving: Boolean,
    hasItems: Boolean,
    onReset: () -> Unit,
    onRegister: () -> Unit
) {
    Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            OutlinedButton(
                onClick = onReset,
                modifier = Modifier.fillMaxWidth().height(56.dp)
            ) {
                Icon(Icons.Default.DeleteSweep, null)
                Spacer(Modifier.width(8.dp))
                Text("RESET")
            }
        }
        
        Button(
            onClick = onRegister,
            enabled = !isSaving && hasItems,
            modifier = Modifier.fillMaxWidth().height(56.dp)
        ) {
            if (isSaving) {
                CircularProgressIndicator(modifier = Modifier.size(24.dp), strokeWidth = 2.dp, color = MaterialTheme.colorScheme.onPrimary)
            } else {
                Icon(Icons.Default.AppRegistration, null)
                Spacer(Modifier.width(8.dp))
                Text("REGISTER BATCH")
            }
        }
    }
}

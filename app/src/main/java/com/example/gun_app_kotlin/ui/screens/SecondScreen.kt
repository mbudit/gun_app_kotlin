package com.example.gun_app_kotlin.ui.screens

import android.view.KeyEvent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.focusable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.material3.HorizontalDivider
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.key.onKeyEvent
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.gun_app_kotlin.data.LinenItem
import com.example.gun_app_kotlin.ui.theme.GunAppTheme
import com.rscja.deviceapi.entity.UHFTAGInfo

@OptIn(ExperimentalComposeUiApi::class)
@Composable
fun SecondScreen(
    // MODIFIED: Use the factory to create the ViewModel
    scanViewModel: ScanViewModel = viewModel(factory = ScanViewModelFactory(LocalContext.current.applicationContext))
) {
    val context = LocalContext.current
    val uiState by scanViewModel.uiState.collectAsState()
    val focusRequester = remember { FocusRequester() }

    DisposableEffect(Unit) {
        scanViewModel.init(context)
        onDispose {
            // Optional: Add cleanup logic if needed
        }
    }

    LaunchedEffect(Unit) {
        focusRequester.requestFocus()
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .focusRequester(focusRequester)
            .focusable()
            .onKeyEvent { event ->
                if (event.nativeKeyEvent.action == KeyEvent.ACTION_DOWN &&
                    event.nativeKeyEvent.keyCode in listOf(139, 280, 291, 293, 294)
                ) {
                    scanViewModel.toggleScan()
                    return@onKeyEvent true
                }
                false
            }
    ) {
        ScanStatusHeader(
            isScanning = uiState.isScanning,
            uniqueCount = uiState.scannedItems.size, // MODIFIED
            totalReads = uiState.totalReads
        )

        TableHeader() // MODIFIED

        if (uiState.scannedItems.isEmpty()) { // MODIFIED
            EmptyState()
        } else {
            LazyColumn {
                // MODIFIED: Use the new `scannedItems` state object
                items(
                    items = uiState.scannedItems.values.toList().sortedByDescending { it.uhfTagInfo.count },
                    key = { enrichedTag -> enrichedTag.uhfTagInfo.epc }
                ) { enrichedTag ->
                    ListItemView(enrichedTag = enrichedTag) // MODIFIED
                    HorizontalDivider(
                        Modifier,
                        DividerDefaults.Thickness,
                        color = MaterialTheme.colorScheme.outline.copy(alpha = 0.2f)
                    )
                }
            }
        }
    }
}

// --- MODIFIED: TableHeader now shows linen-specific columns ---
@Composable
fun TableHeader() {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(MaterialTheme.colorScheme.surfaceVariant)
            .padding(horizontal = 16.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(text = "Linen Type", modifier = Modifier.weight(3f), fontWeight = FontWeight.Bold)
        Text(text = "Status", modifier = Modifier.weight(3f), fontWeight = FontWeight.Bold)
        Text(text = "Count", modifier = Modifier.weight(1.5f), fontWeight = FontWeight.Bold, textAlign = TextAlign.Center)
        Text(text = "RSSI", modifier = Modifier.weight(1.5f), fontWeight = FontWeight.Bold, textAlign = TextAlign.Center)
    }
}

// --- MODIFIED: ListItemView now displays the rich `EnrichedLinenTag` data ---
@Composable
fun ListItemView(enrichedTag: EnrichedLinenTag) {
    val linenFound = enrichedTag.linenItem != null
    // If linen is found in DB, show its type. Otherwise, show "Unknown Tag".
    val linenType = enrichedTag.linenItem?.linenType ?: "Unknown Tag"
    // If linen is found, show its status. Otherwise, show the raw EPC.
    val statusOrEpc = enrichedTag.linenItem?.status ?: enrichedTag.uhfTagInfo.epc

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Linen Type Column - shows in red if not found in the database
        Text(
            text = linenType,
            modifier = Modifier.weight(3f),
            color = if (linenFound) LocalContentColor.current else Color.Red,
            fontWeight = if (linenFound) FontWeight.Normal else FontWeight.Bold,
            style = MaterialTheme.typography.bodyMedium
        )
        // Status (or EPC) Column
        Text(
            text = statusOrEpc,
            modifier = Modifier.weight(3f),
            style = MaterialTheme.typography.bodySmall,
            color = LocalContentColor.current.copy(alpha = 0.7f), // Slightly dimmed
            maxLines = 2
        )
        // Count Column
        Text(
            text = "x${enrichedTag.uhfTagInfo.count}",
            modifier = Modifier.weight(1.5f),
            textAlign = TextAlign.Center,
            style = MaterialTheme.typography.bodyMedium
        )
        // RSSI Column
        Text(
            text = enrichedTag.uhfTagInfo.rssi,
            modifier = Modifier.weight(1.5f),
            textAlign = TextAlign.Center,
            style = MaterialTheme.typography.bodyMedium
        )
    }
}


// --- UNCHANGED: These helper composables remain the same ---

@Composable
fun ScanStatusHeader(isScanning: Boolean, uniqueCount: Int, totalReads: Int) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        shape = MaterialTheme.shapes.large
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceAround
            ) {
                StatusItem(label = "Status", value = if (isScanning) "Scanning" else "Stopped")
                StatusItem(label = "Unique Tags", value = uniqueCount.toString())
                StatusItem(label = "Total Reads", value = totalReads.toString())
            }
            AnimatedVisibility(visible = isScanning) {
                LinearProgressIndicator(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 16.dp)
                )
            }
        }
    }
}

@Composable
fun StatusItem(label: String, value: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(text = label, style = MaterialTheme.typography.labelMedium)
        Text(text = value, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
    }
}

@Composable
fun EmptyState() {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = "Press the trigger to start scanning for RFID tags.",
            style = MaterialTheme.typography.titleMedium,
            textAlign = TextAlign.Center,
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
        )
    }
}

// --- Preview needs to be updated to work with the new data structure ---
@Preview(showBackground = true)
@Composable
fun SecondScreenPreview() {
    GunAppTheme {
        val knownTag = EnrichedLinenTag(
            uhfTagInfo = UHFTAGInfo().apply { epc = "E280116060000206122C1A20"; count = 12; rssi = "-55" },
            linenItem = LinenItem("E280116060000206122C1A20", "Pillowcase", "In Stock", "")
        )
        val unknownTag = EnrichedLinenTag(
            uhfTagInfo = UHFTAGInfo().apply { epc = "UNKNOWN_EPC_12345"; count = 3; rssi = "-62" },
            linenItem = null // This tag was not found in the database
        )

        Column(Modifier.background(MaterialTheme.colorScheme.background)) {
            ScanStatusHeader(isScanning = true, uniqueCount = 2, totalReads = 15)
            TableHeader()
            ListItemView(enrichedTag = knownTag)
            HorizontalDivider(Modifier, DividerDefaults.Thickness, DividerDefaults.color)
            ListItemView(enrichedTag = unknownTag)
        }
    }
}

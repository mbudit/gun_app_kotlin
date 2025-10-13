package com.example.gun_app_kotlin.ui.screens

import android.view.KeyEvent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.focusable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
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
import com.example.gun_app_kotlin.ui.theme.GunAppTheme
import com.rscja.deviceapi.entity.UHFTAGInfo

@OptIn(ExperimentalComposeUiApi::class)
@Composable
fun SecondScreen(
    scanViewModel: ScanViewModel = viewModel()
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
        // --- MODIFIED: The status header is now a modern Card ---
        ScanStatusHeader(
            isScanning = uiState.isScanning,
            uniqueCount = uiState.uniqueTags.size,
            totalReads = uiState.totalReads
        )

        TableHeader()

        // --- NEW: Show a message when the list is empty ---
        if (uiState.uniqueTags.isEmpty()) {
            EmptyState()
        } else {
            // Display the list of scanned tags
            LazyColumn {
                items(
                    items = uiState.uniqueTags.values.toList().sortedByDescending { it.count },
                    key = { tag -> tag.epc }
                ) { tag ->
                    ListItemView(tagInfo = tag)
                    Divider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.2f))
                }
            }
        }
    }
}

// --- MODIFIED: ScanStatusHeader is now a Card with a progress indicator ---
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
            // --- NEW: Animated progress bar for scanning state ---
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

// --- NEW: Helper composable for status items ---
@Composable
fun StatusItem(label: String, value: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(text = label, style = MaterialTheme.typography.labelMedium)
        Text(text = value, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
    }
}


// --- MODIFIED: TableHeader uses MaterialTheme colors ---
@Composable
fun TableHeader() {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(MaterialTheme.colorScheme.surfaceVariant) // Use a theme color
            .padding(horizontal = 16.dp, vertical = 8.dp)
    ) {
        Text(text = "EPC", modifier = Modifier.weight(5f), fontWeight = FontWeight.Bold)
        Text(text = "Count", modifier = Modifier.weight(2f), fontWeight = FontWeight.Bold, textAlign = TextAlign.Center)
        Text(text = "RSSI", modifier = Modifier.weight(2f), fontWeight = FontWeight.Bold, textAlign = TextAlign.Center)
    }
}

// --- MODIFIED: ListItemView has better spacing and alignment ---
@Composable
fun ListItemView(tagInfo: UHFTAGInfo) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(text = tagInfo.epc, style = MaterialTheme.typography.bodySmall, modifier = Modifier.weight(5f), maxLines = 2)
        Text(text = "x${tagInfo.count}", style = MaterialTheme.typography.bodyMedium, modifier = Modifier.weight(2f), textAlign = TextAlign.Center)
        Text(text = tagInfo.rssi, style = MaterialTheme.typography.bodyMedium, modifier = Modifier.weight(2f), textAlign = TextAlign.Center)
    }
}

// --- NEW: Composable for the empty state ---
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


@Preview(showBackground = true)
@Composable
fun SecondScreenPreview() {
    GunAppTheme {
        Column(Modifier.background(MaterialTheme.colorScheme.background)) {
            ScanStatusHeader(isScanning = true, uniqueCount = 2, totalReads = 15)
            TableHeader()
            ListItemView(tagInfo = UHFTAGInfo().apply {
                epc = "E280116060000206122C1A20"
                count = 12
                rssi = "-55"
            })
            Divider()
            ListItemView(tagInfo = UHFTAGInfo().apply {
                epc = "300833B2DDD9014000000000"
                count = 3
                rssi = "-62"
            })
        }
    }
}

@Preview(showBackground = true, name = "Empty State Preview")
@Composable
fun SecondScreenEmptyPreview() {
    GunAppTheme {
        Column(Modifier.background(MaterialTheme.colorScheme.background)) {
            ScanStatusHeader(isScanning = false, uniqueCount = 0, totalReads = 0)
            TableHeader()
            EmptyState()
        }
    }
}

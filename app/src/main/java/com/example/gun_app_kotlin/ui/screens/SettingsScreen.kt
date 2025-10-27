package com.example.gun_app_kotlin.ui.screens

import android.widget.Toast
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    onNavigateUp: () -> Unit,
    viewModel: SettingsViewModel = viewModel(factory = SettingsViewModelFactory(LocalContext.current.applicationContext))
) {
    val context = LocalContext.current
    val uiState by viewModel.uiState.collectAsState()

    // This state holds the text from the TextField.
    var powerInput by remember { mutableStateOf(uiState.currentPower.toString()) }

    // This effect ensures if the user clears the input and then gets power, the field repopulates.
    LaunchedEffect(uiState.currentPower) {
        if (powerInput != uiState.currentPower.toString()) {
            powerInput = uiState.currentPower.toString()
        }
    }

    // This effect shows toasts for error messages from the ViewModel
    LaunchedEffect(uiState.error) {
        uiState.error?.let {
            Toast.makeText(context, it, Toast.LENGTH_SHORT).show()
            viewModel.clearError()
        }
    }

    DisposableEffect(Unit) {
        viewModel.init(context)
        onDispose { }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Settings") },
                navigationIcon = {
                    IconButton(onClick = onNavigateUp) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, "Navigate back")
                    }
                }
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(16.dp)
        ) {
            if (uiState.isLoading) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator()
                }
            } else {
                ReaderPowerSetting(
                    // Pass the string value for the input field
                    powerValue = powerInput,
                    // Pass the actual current power from the ViewModel for the display text
                    currentPowerDisplay = uiState.currentPower,
                    // The TextField updates the string state
                    onPowerValueChange = { newValue ->
                        // Apply the rules here
                        if (newValue.length <= 2 && newValue.all { it.isDigit() }) {
                            val numericValue = newValue.toIntOrNull()
                            if (numericValue == null || numericValue in 0..30) {
                                powerInput = newValue
                            }
                        }
                    },
                    onGetPowerClicked = {
                        // call init() to refresh
                        viewModel.init(context)
                        Toast.makeText(context, "Reading current power...", Toast.LENGTH_SHORT).show()
                    },
                    onSetPowerClicked = {
                        // Convert the string to an Int before sending to ViewModel
                        val powerToSet = powerInput.toIntOrNull()
                        if (powerToSet != null) {
                            viewModel.setPower(powerToSet)
                            Toast.makeText(context, "Setting power to $powerToSet dBm...", Toast.LENGTH_SHORT).show()
                        } else {
                            Toast.makeText(context, "Please enter a valid number.", Toast.LENGTH_SHORT).show()
                        }
                    }
                )
            }
        }
    }
}


@Composable
fun ReaderPowerSetting(
    powerValue: String,
    currentPowerDisplay: Int, // New parameter for the display text
    onPowerValueChange: (String) -> Unit,
    onGetPowerClicked: () -> Unit,
    onSetPowerClicked: () -> Unit
) {
    Column {
        Text(
            "Reader Power (dBm)",
            style = MaterialTheme.typography.titleMedium,
            modifier = Modifier.padding(bottom = 4.dp)
        )
        Text(
            "Enter a value between 0 and 30. Higher values increase scan distance.",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(bottom = 16.dp)
        )

        // --- NEW ROW TO HOLD TEXTFIELD AND DISPLAY TEXT ---
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            OutlinedTextField(
                value = powerValue,
                onValueChange = onPowerValueChange,
                label = { Text("Set Power (0-30)") },
                modifier = Modifier.weight(1f), // TextField takes up available space
                singleLine = true,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
            )

            // This Text shows the actual current power
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    "Current:",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    text = "$currentPowerDisplay",
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
            }
        }
        // ----------------------------------------------------

        Spacer(modifier = Modifier.height(16.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
//            OutlinedButton(
//                onClick = onGetPowerClicked,
//                modifier = Modifier.weight(1f)
//            ) {
//                Text("Get Power")
//            }

            Button(
                onClick = onSetPowerClicked,
                modifier = Modifier.weight(1f)
            ) {
                Text("Set Power")
            }
        }
    }
}

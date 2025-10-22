package com.example.gun_app_kotlin.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RegisterScreen(
    onNavigateBack: () -> Unit,
    onRegistrationSuccess: () -> Unit,
    registerViewModel: RegisterViewModel = viewModel(factory = RegisterViewModelFactory(LocalContext.current.applicationContext))
) {
    val uiState by registerViewModel.uiState.collectAsState()

    LaunchedEffect(uiState.registrationSuccess) {
        if (uiState.registrationSuccess) {
            onRegistrationSuccess()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Create an Account") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background,
                    scrolledContainerColor = MaterialTheme.colorScheme.background
                )
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(16.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // --- Form Card ---
            Column() {
                AuthTextField(
                    value = uiState.username,
                    onValueChange = registerViewModel::onUsernameChange,
                    label = "Username",
                    leadingIcon = Icons.Default.Person
                )
                Spacer(Modifier.height(16.dp))
                AuthTextField(
                    value = uiState.password,
                    onValueChange = registerViewModel::onPasswordChange,
                    label = "Password",
                    leadingIcon = Icons.Default.Lock,
                    isPasswordToggle = true
                )
                Spacer(Modifier.height(16.dp))
                AuthTextField(
                    value = uiState.confirmPassword,
                    onValueChange = registerViewModel::onConfirmPasswordChange,
                    label = "Confirm Password",
                    leadingIcon = Icons.Default.Lock,
                    isPasswordToggle = true,
                    isError = uiState.error == "Passwords do not match."
                )
            }
            Spacer(Modifier.height(24.dp))

            // --- Error Message ---
            if (uiState.error != null) {
                Text(
                    text = uiState.error!!,
                    color = MaterialTheme.colorScheme.error,
                    textAlign = TextAlign.Center,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 8.dp)
                )
            }

            // --- Register Button ---
            Button(
                onClick = registerViewModel::attemptRegistration,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp),
                enabled = !uiState.isLoading,
                shape = MaterialTheme.shapes.medium
            ) {
                if (uiState.isLoading) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(24.dp),
                        color = MaterialTheme.colorScheme.onPrimary
                    )
                } else {
                    Text("Register")
                }
            }
            Spacer(Modifier.height(16.dp))
        }
    }
}

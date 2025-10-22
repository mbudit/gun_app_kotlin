package com.example.gun_app_kotlin.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.rounded.Lock
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel

@Composable
fun LoginScreen(
    onLoginSuccess: () -> Unit,
    onNavigateToRegister: () -> Unit,
    loginViewModel: LoginViewModel = viewModel(factory = LoginViewModelFactory(LocalContext.current.applicationContext))
) {
    val uiState by loginViewModel.uiState.collectAsState()

    LaunchedEffect(uiState.loginSuccess) {
        if (uiState.loginSuccess) {
            onLoginSuccess()
        }
    }

    Surface(
        modifier = Modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.background
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // --- Header ---
            Icon(
                imageVector = Icons.Rounded.Lock,
                contentDescription = "Lock Icon",
                modifier = Modifier.size(64.dp),
                tint = MaterialTheme.colorScheme.primary
            )
            Spacer(Modifier.height(16.dp))
            Text("Welcome Back!", style = MaterialTheme.typography.headlineLarge)
            Text(
                "Login to access your RFID dashboard",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(Modifier.height(48.dp))

            // --- Form Card ---
            Column() {
                AuthTextField(
                    value = uiState.username,
                    onValueChange = loginViewModel::onUsernameChange,
                    label = "Username",
                    leadingIcon = Icons.Default.Person
                )
                Spacer(Modifier.height(16.dp))
                AuthTextField(
                    value = uiState.password,
                    onValueChange = loginViewModel::onPasswordChange,
                    label = "Password",
                    leadingIcon = Icons.Default.Lock,
                    isPasswordToggle = true
                )
            }

            Spacer(Modifier.height(24.dp))

            // --- Error Message ---
            if (uiState.loginError != null) {
                Text(
                    text = uiState.loginError!!,
                    color = MaterialTheme.colorScheme.error,
                    modifier = Modifier.padding(bottom = 8.dp),
                    textAlign = TextAlign.Center
                )
            }

            // --- Login Button ---
            Button(
                onClick = loginViewModel::attemptLogin,
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
                    Text("Login")
                }
            }
            Spacer(Modifier.height(24.dp))

            // --- Register Navigation ---
            TextButton(onClick = onNavigateToRegister) {
                Text("(KHUSUS ADMIN) Daftar Pengguna")
            }
        }
    }
}

package com.example.gun_app_kotlin.ui.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusDirection
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.gun_app_kotlin.R

// --- Industrial Color Palette (Light Mode) – matching HomeScreen ---
private val PageBackground = Color(0xFFF5F5F7)
private val CardBackground = Color(0xFFFFFFFF)
private val AccentYellow = Color(0xFFF5C518)
private val AccentYellowDark = Color(0xFFD4A90F)
private val TextPrimary = Color(0xFF1A1D23)
private val TextSecondary = Color(0xFF5F6368)
private val DangerRed = Color(0xFFD32F2F)
private val InputBackground = Color(0xFFF0F0F2)

@Composable
fun LoginScreen(
        onLoginSuccess: () -> Unit,
        onNavigateToRegister: () -> Unit,
        sessionViewModel: SessionViewModel = viewModel(),
        loginViewModel: LoginViewModel =
                viewModel(
                        factory =
                                LoginViewModelFactory(
                                        LocalContext.current.applicationContext,
                                        sessionViewModel
                                )
                )
) {
    val uiState by loginViewModel.uiState.collectAsState()
    val focusManager = LocalFocusManager.current
    var passwordVisible by remember { mutableStateOf(false) }

    LaunchedEffect(uiState.loginSuccess) {
        if (uiState.loginSuccess) {
            onLoginSuccess()
        }
    }

    Box(modifier = Modifier.fillMaxSize().background(PageBackground)) {
        Column(
                modifier = Modifier.fillMaxSize().padding(horizontal = 28.dp),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // ── Logo + Branding ──
            Image(
                    painter = painterResource(id = R.drawable.logo_simtech),
                    contentDescription = "Simtech Logo",
                    modifier = Modifier.height(72.dp).padding(bottom = 8.dp),
                    contentScale = ContentScale.Fit
            )

            Spacer(Modifier.height(8.dp))

            Text(
                    text = "RFID SCANNER",
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Black,
                    color = TextPrimary,
                    letterSpacing = 2.sp
            )
            Text(
                    text = "Laundry Operational System",
                    style = MaterialTheme.typography.bodyMedium,
                    color = TextSecondary,
                    letterSpacing = 1.sp
            )

            Spacer(Modifier.height(12.dp))

            // ── Yellow Accent Line ──
            Box(
                    modifier =
                            Modifier.width(60.dp)
                                    .height(3.dp)
                                    .clip(RoundedCornerShape(2.dp))
                                    .background(
                                            Brush.horizontalGradient(
                                                    colors = listOf(AccentYellow, AccentYellowDark)
                                            )
                                    )
            )

            Spacer(Modifier.height(36.dp))

            // ── Login Card ──
            Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = CardBackground),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column(modifier = Modifier.padding(24.dp)) {
                    Text(
                            text = "Masuk",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold,
                            color = TextPrimary
                    )
                    Text(
                            text = "Silakan login untuk melanjutkan",
                            style = MaterialTheme.typography.bodySmall,
                            color = TextSecondary
                    )

                    Spacer(Modifier.height(24.dp))

                    // ── Username Field ──
                    Text(
                            text = "USERNAME",
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.Bold,
                            color = TextSecondary,
                            letterSpacing = 1.sp,
                            modifier = Modifier.padding(bottom = 6.dp)
                    )
                    OutlinedTextField(
                            value = uiState.username,
                            onValueChange = loginViewModel::onUsernameChange,
                            placeholder = {
                                Text("Masukkan username", color = TextSecondary.copy(alpha = 0.5f))
                            },
                            leadingIcon = {
                                Icon(
                                        Icons.Default.Person,
                                        contentDescription = "Username",
                                        tint = AccentYellow
                                )
                            },
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true,
                            shape = RoundedCornerShape(10.dp),
                            colors =
                                    OutlinedTextFieldDefaults.colors(
                                            focusedBorderColor = AccentYellow,
                                            unfocusedBorderColor = Color(0xFFE0E0E0),
                                            focusedContainerColor = Color.Transparent,
                                            unfocusedContainerColor = InputBackground,
                                            cursorColor = AccentYellow
                                    ),
                            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next),
                            keyboardActions =
                                    KeyboardActions(
                                            onNext = { focusManager.moveFocus(FocusDirection.Down) }
                                    )
                    )

                    Spacer(Modifier.height(16.dp))

                    // ── Password Field ──
                    Text(
                            text = "PASSWORD",
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.Bold,
                            color = TextSecondary,
                            letterSpacing = 1.sp,
                            modifier = Modifier.padding(bottom = 6.dp)
                    )
                    OutlinedTextField(
                            value = uiState.password,
                            onValueChange = loginViewModel::onPasswordChange,
                            placeholder = {
                                Text("Masukkan password", color = TextSecondary.copy(alpha = 0.5f))
                            },
                            leadingIcon = {
                                Icon(
                                        Icons.Default.Lock,
                                        contentDescription = "Password",
                                        tint = AccentYellow
                                )
                            },
                            trailingIcon = {
                                IconButton(onClick = { passwordVisible = !passwordVisible }) {
                                    Icon(
                                            imageVector =
                                                    if (passwordVisible) Icons.Default.VisibilityOff
                                                    else Icons.Default.Visibility,
                                            contentDescription =
                                                    if (passwordVisible) "Hide password"
                                                    else "Show password",
                                            tint = TextSecondary
                                    )
                                }
                            },
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true,
                            shape = RoundedCornerShape(10.dp),
                            visualTransformation =
                                    if (passwordVisible) VisualTransformation.None
                                    else PasswordVisualTransformation(),
                            colors =
                                    OutlinedTextFieldDefaults.colors(
                                            focusedBorderColor = AccentYellow,
                                            unfocusedBorderColor = Color(0xFFE0E0E0),
                                            focusedContainerColor = Color.Transparent,
                                            unfocusedContainerColor = InputBackground,
                                            cursorColor = AccentYellow
                                    ),
                            keyboardOptions =
                                    KeyboardOptions(
                                            keyboardType = KeyboardType.Password,
                                            imeAction = ImeAction.Done
                                    ),
                            keyboardActions =
                                    KeyboardActions(
                                            onDone = {
                                                focusManager.clearFocus()
                                                loginViewModel.attemptLogin()
                                            }
                                    )
                    )
                }
            }

            Spacer(Modifier.height(16.dp))

            // ── Error Message ──
            if (uiState.loginError != null) {
                Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(10.dp),
                        colors =
                                CardDefaults.cardColors(
                                        containerColor = DangerRed.copy(alpha = 0.08f)
                                )
                ) {
                    Text(
                            text = uiState.loginError!!,
                            color = DangerRed,
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.Medium,
                            modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp),
                            textAlign = TextAlign.Center
                    )
                }
            }

            Spacer(Modifier.height(24.dp))

            // ── Login Button ──
            Button(
                    onClick = loginViewModel::attemptLogin,
                    modifier = Modifier.fillMaxWidth().height(52.dp),
                    enabled = !uiState.isLoading,
                    shape = RoundedCornerShape(12.dp),
                    colors =
                            ButtonDefaults.buttonColors(
                                    containerColor = AccentYellow,
                                    contentColor = TextPrimary,
                                    disabledContainerColor = AccentYellow.copy(alpha = 0.4f)
                            ),
                    elevation = ButtonDefaults.buttonElevation(defaultElevation = 0.dp)
            ) {
                if (uiState.isLoading) {
                    CircularProgressIndicator(
                            modifier = Modifier.size(22.dp),
                            color = TextPrimary,
                            strokeWidth = 2.5.dp
                    )
                } else {
                    Text(
                            "LOGIN",
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 2.sp,
                            fontSize = 15.sp
                    )
                }
            }

            Spacer(Modifier.height(20.dp))

            // ── Register Link ──
            TextButton(onClick = onNavigateToRegister) {
                Text(
                        "(KHUSUS ADMIN) Daftar Pengguna",
                        color = TextSecondary,
                        style = MaterialTheme.typography.bodySmall
                )
            }

            Spacer(Modifier.height(32.dp))

            // ── Footer ──
            Text(
                    text = "Powered by Simtech",
                    style = MaterialTheme.typography.labelSmall,
                    color = TextSecondary.copy(alpha = 0.4f)
            )
        }
    }
}

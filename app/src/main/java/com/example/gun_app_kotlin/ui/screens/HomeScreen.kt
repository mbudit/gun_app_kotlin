package com.example.gun_app_kotlin.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.automirrored.filled.ExitToApp
import androidx.compose.material.icons.filled.AddCircle
import androidx.compose.material.icons.filled.AssignmentTurnedIn
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Inventory2
import androidx.compose.material.icons.filled.LocalShipping
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.QrCodeScanner
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.SwapHoriz
import androidx.compose.material.icons.filled.Sync
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import com.example.gun_app_kotlin.R
import kotlinx.coroutines.launch

// --- Industrial Color Palette (Light Mode) ---
private val IndustrialDark = Color(0xFFF5F5F7)
private val IndustrialCard = Color(0xFFFFFFFF)
private val IndustrialCardLight = Color(0xFFF0F0F2)
private val AccentYellow = Color(0xFFF5C518)
private val AccentYellowDark = Color(0xFFD4A90F)
private val TextPrimary = Color(0xFF1A1D23)
private val TextSecondary = Color(0xFF5F6368)
private val DividerColor = Color(0xFFE0E0E0)
private val DangerRed = Color(0xFFD32F2F)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
        sessionViewModel: SessionViewModel,
        navController: NavHostController,
        onNavigateToSettings: () -> Unit,
        homeViewModel: HomeViewModel =
                viewModel(factory = HomeViewModelFactory(LocalContext.current.applicationContext))
) {
    val uiState by homeViewModel.uiState.collectAsState()
    val sessionState by sessionViewModel.uiState.collectAsState()
    val currentUser = sessionState.currentUser
    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
    val scope = rememberCoroutineScope()

    // --- EFFECT 1: START THE SYNC ON LOGIN ---
    LaunchedEffect(sessionState.isJustLoggedIn) {
        if (sessionState.isJustLoggedIn) {
            homeViewModel.syncData()
        }
    }

    // --- EFFECT 2: LISTEN FOR SYNC COMPLETION ---
    LaunchedEffect(Unit) {
        homeViewModel.syncCompletedEvent.collect { sessionViewModel.onSyncCompleted() }
    }

    ModalNavigationDrawer(
            drawerState = drawerState,
            drawerContent = {
                ModalDrawerSheet(
                        modifier = Modifier.fillMaxWidth(0.75f),
                        drawerContainerColor = IndustrialDark
                ) {
                    Column(modifier = Modifier.fillMaxHeight()) {
                        // Drawer Header
                        Box(
                                modifier =
                                        Modifier.fillMaxWidth()
                                                .background(
                                                        Brush.verticalGradient(
                                                                colors =
                                                                        listOf(
                                                                                AccentYellow,
                                                                                AccentYellowDark
                                                                        )
                                                        )
                                                )
                                                .padding(24.dp)
                        ) {
                            Column {
                                // Logo in drawer
                                Image(
                                        painter = painterResource(id = R.drawable.logo_simtech),
                                        contentDescription = "Simtech Logo",
                                        modifier =
                                                Modifier.size(48.dp).clip(RoundedCornerShape(8.dp)),
                                        contentScale = ContentScale.Fit
                                )
                                Spacer(Modifier.height(16.dp))
                                if (currentUser != null) {
                                    Text(
                                            text = currentUser.name,
                                            style = MaterialTheme.typography.titleLarge,
                                            fontWeight = FontWeight.Bold,
                                            color = IndustrialDark
                                    )
                                    Text(
                                            text = "@${currentUser.username}",
                                            style = MaterialTheme.typography.bodyMedium,
                                            color = IndustrialDark.copy(alpha = 0.7f)
                                    )
                                } else {
                                    Text(
                                            "RFID Scanner",
                                            style = MaterialTheme.typography.titleLarge,
                                            fontWeight = FontWeight.Bold,
                                            color = IndustrialDark
                                    )
                                }
                            }
                        }

                        Spacer(Modifier.weight(1f))

                        NavigationDrawerItem(
                                icon = {
                                    Icon(
                                            Icons.Default.Settings,
                                            contentDescription = "Settings",
                                            tint = TextSecondary
                                    )
                                },
                                label = { Text("Settings", color = TextPrimary) },
                                selected = false,
                                onClick = {
                                    scope.launch { drawerState.close() }
                                    onNavigateToSettings()
                                },
                                modifier = Modifier.padding(horizontal = 12.dp),
                                colors =
                                        NavigationDrawerItemDefaults.colors(
                                                unselectedContainerColor = Color.Transparent
                                        )
                        )

                        NavigationDrawerItem(
                                icon = {
                                    Icon(
                                            Icons.AutoMirrored.Filled.ExitToApp,
                                            contentDescription = "Logout",
                                            tint = DangerRed
                                    )
                                },
                                label = { Text("Log Out", color = DangerRed) },
                                selected = false,
                                onClick = {
                                    sessionViewModel.onLogout()
                                    navController.navigate("login") {
                                        popUpTo(navController.graph.id) { inclusive = true }
                                    }
                                },
                                modifier = Modifier.padding(horizontal = 12.dp),
                                colors =
                                        NavigationDrawerItemDefaults.colors(
                                                unselectedContainerColor = Color.Transparent
                                        )
                        )
                        Spacer(Modifier.height(16.dp))
                    }
                }
            }
    ) {
        // Main Content
        Box(modifier = Modifier.fillMaxSize().background(IndustrialDark)) {
            Column(modifier = Modifier.fillMaxSize().verticalScroll(rememberScrollState())) {
                // ── Top Bar Area ──
                Row(
                        modifier =
                                Modifier.fillMaxWidth()
                                        .padding(horizontal = 20.dp, vertical = 16.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    IconButton(
                            onClick = {
                                scope.launch {
                                    drawerState.apply { if (isClosed) open() else close() }
                                }
                            }
                    ) {
                        Icon(
                                imageVector = Icons.Default.Person,
                                contentDescription = "User Profile",
                                tint = TextPrimary,
                                modifier = Modifier.size(28.dp)
                        )
                    }

                    // Sync indicator
                    AnimatedVisibility(visible = uiState.isSyncing) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            CircularProgressIndicator(
                                    modifier = Modifier.size(18.dp),
                                    strokeWidth = 2.dp,
                                    color = AccentYellow
                            )
                            Spacer(Modifier.width(8.dp))
                            Text(
                                    "Syncing...",
                                    style = MaterialTheme.typography.labelMedium,
                                    color = AccentYellow
                            )
                        }
                    }
                }

                // ── Hero Section with Logo ──
                Column(
                        modifier =
                                Modifier.fillMaxWidth()
                                        .padding(horizontal = 24.dp)
                                        .padding(top = 8.dp, bottom = 24.dp),
                        horizontalAlignment = Alignment.Start
                ) {
                    Image(
                            painter = painterResource(id = R.drawable.logo_simtech),
                            contentDescription = "Simtech Logo",
                            modifier = Modifier.height(56.dp).padding(bottom = 16.dp),
                            contentScale = ContentScale.Fit
                    )
                    Text(
                            text = "RFID SCANNER",
                            style = MaterialTheme.typography.headlineLarge,
                            fontWeight = FontWeight.Black,
                            color = TextPrimary,
                            letterSpacing = 2.sp
                    )
                    Text(
                            text = "Laundry Operational System",
                            style = MaterialTheme.typography.bodyLarge,
                            color = TextSecondary,
                            letterSpacing = 1.sp
                    )

                    // Greeting bar
                    if (currentUser != null) {
                        Spacer(Modifier.height(16.dp))
                        Row(
                                modifier =
                                        Modifier.fillMaxWidth()
                                                .clip(RoundedCornerShape(8.dp))
                                                .background(IndustrialCard)
                                                .padding(horizontal = 16.dp, vertical = 12.dp),
                                verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                    modifier =
                                            Modifier.size(36.dp)
                                                    .clip(CircleShape)
                                                    .background(AccentYellow),
                                    contentAlignment = Alignment.Center
                            ) {
                                Text(
                                        text = currentUser.name.take(1).uppercase(),
                                        fontWeight = FontWeight.Bold,
                                        color = IndustrialDark,
                                        fontSize = 16.sp
                                )
                            }
                            Spacer(Modifier.width(12.dp))
                            Column {
                                Text(
                                        text = "Halo, ${currentUser.name}",
                                        style = MaterialTheme.typography.bodyLarge,
                                        fontWeight = FontWeight.SemiBold,
                                        color = TextPrimary
                                )
                                Text(
                                        text = "Petugas Laundry",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = TextSecondary
                                )
                            }
                        }
                    }
                }

                // ── Yellow Accent Divider ──
                Box(
                        modifier =
                                Modifier.fillMaxWidth()
                                        .padding(horizontal = 24.dp)
                                        .height(3.dp)
                                        .clip(RoundedCornerShape(2.dp))
                                        .background(
                                                Brush.horizontalGradient(
                                                        colors =
                                                                listOf(
                                                                        AccentYellow,
                                                                        AccentYellowDark,
                                                                        Color.Transparent
                                                                )
                                                )
                                        )
                )

                Spacer(Modifier.height(24.dp))

                // ── Section Label ──
                Text(
                        text = "OPERASI",
                        style = MaterialTheme.typography.labelLarge,
                        color = AccentYellow,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 3.sp,
                        modifier = Modifier.padding(horizontal = 24.dp, vertical = 4.dp)
                )

                Spacer(Modifier.height(12.dp))

                // ── Primary Action Cards ──
                IndustrialMenuCard(
                        icon = Icons.Default.Inventory2,
                        title = "Masuk Gudang",
                        subtitle = "Scan linen masuk ke penyimpanan",
                        accentColor = AccentYellow,
                        onClick = { navController.navigate("second_screen") },
                        enabled = !uiState.isSyncing
                )

                IndustrialMenuCard(
                        icon = Icons.Default.LocalShipping,
                        title = "Keluar Gudang",
                        subtitle = "Scan linen keluar dari penyimpanan",
                        accentColor = Color(0xFF64B5F6),
                        onClick = { navController.navigate("third_screen") },
                        enabled = !uiState.isSyncing
                )

                IndustrialMenuCard(
                        icon = Icons.Default.SwapHoriz,
                        title = "Serah Terima",
                        subtitle = "Penyerahan linen ke penerima",
                        accentColor = Color(0xFF81C784),
                        onClick = { navController.navigate("fourth_screen") },
                        enabled = !uiState.isSyncing
                )

                Spacer(Modifier.height(20.dp))

                // ── Section Label ──
                Text(
                        text = "DATA & TOOLS",
                        style = MaterialTheme.typography.labelLarge,
                        color = TextSecondary,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 3.sp,
                        modifier = Modifier.padding(horizontal = 24.dp, vertical = 4.dp)
                )

                Spacer(Modifier.height(12.dp))

                // ── Secondary Action Cards ──
                IndustrialMenuCard(
                        icon = Icons.Default.AssignmentTurnedIn,
                        title = "List Penerimaan Linen",
                        subtitle = "Riwayat serah terima linen",
                        accentColor = Color(0xFFB39DDB),
                        onClick = { navController.navigate("fifth_screen") },
                        enabled = !uiState.isSyncing
                )

                IndustrialMenuCard(
                        icon = Icons.Default.QrCodeScanner,
                        title = "Identifikasi EPC",
                        subtitle = "Scan dan identifikasi tag RFID",
                        accentColor = Color(0xFF4DD0E1),
                        onClick = { navController.navigate("epc_scan_screen") },
                        enabled = !uiState.isSyncing
                )

                Spacer(Modifier.height(20.dp))

                // ── Danger Zone ──
                Row(
                        modifier =
                                Modifier.fillMaxWidth()
                                        .padding(horizontal = 24.dp)
                                        .clip(RoundedCornerShape(12.dp))
                                        .background(DangerRed.copy(alpha = 0.1f))
                                        .clickable(enabled = !uiState.isSyncing) {
                                            homeViewModel.clearData()
                                        }
                                        .padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                            Icons.Default.Delete,
                            contentDescription = "Clear Cache",
                            tint = DangerRed,
                            modifier = Modifier.size(22.dp)
                    )
                    Spacer(Modifier.width(12.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                                "Clear Cache",
                                style = MaterialTheme.typography.bodyLarge,
                                fontWeight = FontWeight.SemiBold,
                                color = DangerRed
                        )
                        Text(
                                "Hapus semua data lokal",
                                style = MaterialTheme.typography.bodySmall,
                                color = DangerRed.copy(alpha = 0.7f)
                        )
                    }
                }

                Spacer(Modifier.height(32.dp))

                // ── Footer ──
                Text(
                        text = "Powered by Simtech",
                        style = MaterialTheme.typography.labelSmall,
                        color = TextSecondary.copy(alpha = 0.5f),
                        modifier = Modifier.fillMaxWidth().padding(bottom = 24.dp),
                        textAlign = androidx.compose.ui.text.style.TextAlign.Center
                )
            }
        }
    }
}

// ── Reusable Industrial Menu Card ──
@Composable
fun IndustrialMenuCard(
        icon: ImageVector,
        title: String,
        subtitle: String,
        accentColor: Color,
        onClick: () -> Unit,
        enabled: Boolean = true
) {
    val alpha = if (enabled) 1f else 0.5f

    Row(
            modifier =
                    Modifier.fillMaxWidth()
                            .padding(horizontal = 20.dp, vertical = 5.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .background(IndustrialCard.copy(alpha = alpha))
                            .clickable(enabled = enabled, onClick = onClick)
                            .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
    ) {
        // Accent bar + Icon
        Box(
                modifier =
                        Modifier.size(48.dp)
                                .clip(RoundedCornerShape(10.dp))
                                .background(accentColor.copy(alpha = 0.15f)),
                contentAlignment = Alignment.Center
        ) {
            Icon(
                    imageVector = icon,
                    contentDescription = title,
                    tint = accentColor,
                    modifier = Modifier.size(26.dp)
            )
        }

        Spacer(Modifier.width(16.dp))

        Column(modifier = Modifier.weight(1f)) {
            Text(
                    text = title,
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.SemiBold,
                    color = TextPrimary.copy(alpha = alpha)
            )
            Text(
                    text = subtitle,
                    style = MaterialTheme.typography.bodySmall,
                    color = TextSecondary.copy(alpha = alpha)
            )
        }

        Icon(
                imageVector = Icons.Default.ChevronRight,
                contentDescription = "Navigate",
                tint = TextSecondary.copy(alpha = 0.5f),
                modifier = Modifier.size(20.dp)
        )
    }
}

// Keep these composables for backward compat (used nowhere now, but safe to keep)
@Composable
fun InfoCard() {
    OutlinedCard(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp),
            shape = MaterialTheme.shapes.large
    ) {
        Row(
                modifier = Modifier.fillMaxWidth().padding(16.dp),
                horizontalArrangement = Arrangement.spacedBy(16.dp),
                verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                    imageVector = Icons.Default.AddCircle,
                    contentDescription = "Scanner Icon",
                    modifier = Modifier.size(56.dp),
                    tint = MaterialTheme.colorScheme.primary
            )
            Column(
                    modifier = Modifier.weight(1f),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Text(text = "Alat Pemindai RFID", style = MaterialTheme.typography.titleMedium)
                Text(
                        text = "Halo petugas laundry~!",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@Composable
fun GoToScanButton(modifier: Modifier = Modifier, onClick: () -> Unit, isEnabled: Boolean) {
    Button(onClick = onClick, enabled = isEnabled, modifier = modifier.height(56.dp)) {
        Icon(
                Icons.AutoMirrored.Filled.ArrowForward,
                "Arrow Forward Icon",
                modifier = Modifier.size(ButtonDefaults.IconSize)
        )
        Spacer(Modifier.size(ButtonDefaults.IconSpacing))
        Text("Masuk Gudang")
    }
}

@Composable
fun GoToThirdScreenButton(modifier: Modifier = Modifier, onClick: () -> Unit, isEnabled: Boolean) {
    FilledTonalButton(onClick = onClick, enabled = isEnabled, modifier = modifier.height(56.dp)) {
        Icon(
                Icons.AutoMirrored.Filled.ArrowBack,
                "Arrow Forward Icon",
                modifier = Modifier.size(ButtonDefaults.IconSize)
        )
        Spacer(Modifier.size(ButtonDefaults.IconSpacing))
        Text("Keluar Gudang")
    }
}

@Composable
fun SyncDataButton(modifier: Modifier = Modifier, onClick: () -> Unit, isEnabled: Boolean) {
    OutlinedButton(onClick = onClick, enabled = isEnabled, modifier = modifier.height(56.dp)) {
        Icon(Icons.Default.Sync, "Sync Icon", modifier = Modifier.size(ButtonDefaults.IconSize))
        Spacer(Modifier.size(ButtonDefaults.IconSpacing))
        Text("Sinkron Data")
    }
}

@Composable
fun ClearDataButton(modifier: Modifier = Modifier, onClick: () -> Unit, isEnabled: Boolean) {
    Button(
            onClick = onClick,
            enabled = isEnabled,
            modifier = modifier.height(56.dp),
            colors =
                    ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.errorContainer,
                            contentColor = MaterialTheme.colorScheme.onErrorContainer
                    )
    ) {
        Icon(Icons.Default.Delete, "Clear Icon", modifier = Modifier.size(ButtonDefaults.IconSize))
        Spacer(Modifier.size(ButtonDefaults.IconSpacing))
        Text("Clear Cache")
    }
}

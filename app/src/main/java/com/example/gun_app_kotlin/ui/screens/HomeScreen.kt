package com.example.gun_app_kotlin.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ExitToApp
import androidx.compose.material.icons.filled.AddCircle
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.AssignmentTurnedIn
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Sync
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    sessionViewModel: SessionViewModel,
    navController: NavHostController,
    onNavigateToSettings: () -> Unit,
    homeViewModel: HomeViewModel = viewModel(factory = HomeViewModelFactory(LocalContext.current.applicationContext))
) {
    val uiState by homeViewModel.uiState.collectAsState()
    val sessionState by sessionViewModel.uiState.collectAsState()
    val currentUser = sessionState.currentUser
    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
    val scope = rememberCoroutineScope()

    // --- EFFECT 1: START THE SYNC ON LOGIN ---
    LaunchedEffect(sessionState.isJustLoggedIn) {
        if (sessionState.isJustLoggedIn) {
            // ONLY start the sync. Do NOT reset the flag here.
            homeViewModel.syncData()
        }
    }

    // --- EFFECT 2: LISTEN FOR SYNC COMPLETION ---
    LaunchedEffect(Unit) {
        homeViewModel.syncCompletedEvent.collect {
            // When the HomeViewModel tells us it's done, reset the session flag.
            sessionViewModel.onSyncCompleted()
        }
    }

    ModalNavigationDrawer(
        drawerState = drawerState,
        drawerContent = {

            ModalDrawerSheet(
                modifier = Modifier.fillMaxWidth(0.7f) // Or choose a value like 0.6f if 50% is too small
            ) {
                Column(modifier = Modifier.fillMaxHeight()) {
                    if (currentUser != null) {
                        Column(modifier = Modifier.padding(24.dp)) {
                            Text(
                                text = "Halo, ${currentUser.name}!",
                                style = MaterialTheme.typography.titleLarge
                            )
                            Text(
                                text = "@${currentUser.username}",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    } else {
                        Text(
                            "User Menu",
                            style = MaterialTheme.typography.titleMedium,
                            modifier = Modifier.padding(16.dp)
                        )
                    }
                    HorizontalDivider()

                    Spacer(Modifier.weight(1f))

                    NavigationDrawerItem(
                        icon = { Icon(Icons.Default.Settings, contentDescription = "Settings") },
                        label = { Text("Settings") },
                        selected = false,
                        onClick = {
                            scope.launch { drawerState.close() }
                            onNavigateToSettings() // <-- CALL THE NAVIGATION ACTION
                        },
                        modifier = Modifier.padding(NavigationDrawerItemDefaults.ItemPadding)
                    )

                    NavigationDrawerItem(
                        icon = { Icon(Icons.AutoMirrored.Filled.ExitToApp, contentDescription = "Logout") },
                        label = { Text("Log Out") },
                        selected = false,
                        onClick = {
                            // Also clear the session data on logout
                            sessionViewModel.onLogout()
                            navController.navigate("login") {
                                popUpTo(navController.graph.id) {
                                    inclusive = true
                                }
                            }
                        },
                        modifier = Modifier.padding(NavigationDrawerItemDefaults.ItemPadding)
                    )
                    Spacer(Modifier.height(12.dp))
                }
            }
        }
    ) {
        Scaffold(
            topBar = {
                TopAppBar(
                    title = { Text("RFID Scanner") },
                    navigationIcon = {
                        IconButton(onClick = {
                            scope.launch {
                                drawerState.apply {
                                    if (isClosed) open() else close()
                                }
                            }
                        }) {
                            Icon(
                                imageVector = Icons.Default.Person,
                                contentDescription = "User Profile"
                            )
                        }
                    }
                )
            }
        ) { innerPadding ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
                    .padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Box(
                    modifier = Modifier.weight(1f),
                    contentAlignment = Alignment.Center
                ) {
                    InfoCard()
                }

                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    AnimatedVisibility(visible = uiState.isSyncing) {
                        CircularProgressIndicator()
                    }

                    GoToScanButton(
                        modifier = Modifier.fillMaxWidth(),
                        onClick = { navController.navigate("second_screen") },
                        isEnabled = !uiState.isSyncing
                    )

                    GoToThirdScreenButton(
                        modifier = Modifier.fillMaxWidth(),
                        onClick = { navController.navigate("third_screen") },
                        isEnabled = !uiState.isSyncing
                    )

                    Button(
                        onClick = { navController.navigate("fourth_screen") },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(56.dp),
                        enabled = !uiState.isSyncing,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.tertiaryContainer,
                            contentColor = MaterialTheme.colorScheme.onTertiaryContainer
                        )
                    ) {
                        // You can change the icon and text as needed
                        Icon(Icons.Default.Check, "Serah Terima Icon", modifier = Modifier.size(ButtonDefaults.IconSize))
                        Spacer(Modifier.size(ButtonDefaults.IconSpacing))
                        Text("Serah Terima")
                    }

                    Button(
                        onClick = { navController.navigate("fifth_screen") },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(56.dp),
                        enabled = !uiState.isSyncing,
                        // Using different colors to make it stand out
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.primaryContainer,
                            contentColor = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                    ) {
                        Icon(Icons.Default.AssignmentTurnedIn, "Fifth Screen Icon", modifier = Modifier.size(ButtonDefaults.IconSize))
                        Spacer(Modifier.size(ButtonDefaults.IconSpacing))
                        Text("List Penerimaan Linen")
                    }

                    Button(
                        onClick = { navController.navigate("epc_scan_screen") },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(56.dp),
                        enabled = !uiState.isSyncing,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.secondaryContainer,
                            contentColor = MaterialTheme.colorScheme.onSecondaryContainer
                        )
                    ) {
                        Icon(Icons.Default.Info, "Info Icon", modifier = Modifier.size(ButtonDefaults.IconSize))
                        Spacer(Modifier.size(ButtonDefaults.IconSpacing))
                        Text("Identifikasi EPC")
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        SyncDataButton(
                            modifier = Modifier.weight(1f),
                            onClick = { homeViewModel.syncData() },
                            isEnabled = !uiState.isSyncing
                        )

                        ClearDataButton(
                            modifier = Modifier.weight(1f),
                            onClick = { homeViewModel.clearData() },
                            isEnabled = !uiState.isSyncing
                        )
                    }
                }
            }
        }
    }
}


@Composable
fun InfoCard() {
    OutlinedCard(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        shape = MaterialTheme.shapes.large
    )
    {
        Row (
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.spacedBy(16.dp),
            verticalAlignment = androidx . compose . ui . Alignment.CenterVertically
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

                Text(
                    text = "Alat Pemindai RFID",
                    style = MaterialTheme.typography.titleMedium
                )
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
    Button(
        onClick = onClick,
        enabled = isEnabled,
        modifier = modifier.height(56.dp),
    ) {
        Icon(Icons.AutoMirrored.Filled.ArrowForward, "Arrow Forward Icon", modifier = Modifier.size(ButtonDefaults.IconSize))
        Spacer(Modifier.size(ButtonDefaults.IconSpacing))
        Text("Masuk Gudang")
    }
}

@Composable
fun GoToThirdScreenButton(modifier: Modifier = Modifier, onClick: () -> Unit, isEnabled: Boolean) {
    FilledTonalButton(
        onClick = onClick,
        enabled = isEnabled,
        modifier = modifier.height(56.dp),
    ) {
        Icon(Icons.AutoMirrored.Filled.ArrowBack, "Arrow Forward Icon", modifier = Modifier.size(ButtonDefaults.IconSize))
        Spacer(Modifier.size(ButtonDefaults.IconSpacing))
        Text("Keluar Gudang")
    }
}

@Composable
fun SyncDataButton(modifier: Modifier = Modifier, onClick: () -> Unit, isEnabled: Boolean) {
    OutlinedButton(
        onClick = onClick,
        enabled = isEnabled,
        modifier = modifier.height(56.dp),
    ) {
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
        colors = ButtonDefaults.buttonColors(
            containerColor = MaterialTheme.colorScheme.errorContainer,
            contentColor = MaterialTheme.colorScheme.onErrorContainer
        )
    ) {
        Icon(Icons.Default.Delete, "Clear Icon", modifier = Modifier.size(ButtonDefaults.IconSize))
        Spacer(Modifier.size(ButtonDefaults.IconSpacing))
        Text("Clear Cache")
    }
}

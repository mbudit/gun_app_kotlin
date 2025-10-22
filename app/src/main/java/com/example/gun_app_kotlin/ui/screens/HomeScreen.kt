package com.example.gun_app_kotlin.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ExitToApp
import androidx.compose.material.icons.filled.AddCircle
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Sync
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    navController: NavHostController,
    homeViewModel: HomeViewModel = viewModel(factory = HomeViewModelFactory(LocalContext.current.applicationContext))
) {
    val uiState by homeViewModel.uiState.collectAsState()

    // 1. Create state for the drawer (open/closed) and a coroutine scope
    //    to control its animations.
    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
    val scope = rememberCoroutineScope()

    // 2. Use ModalNavigationDrawer, which provides the slide-in drawer behavior.
    ModalNavigationDrawer(
        drawerState = drawerState,
        drawerContent = {
            // --- START OF CHANGES ---

            // 1. Use ModalDrawerSheet with a modifier to set the width to 50%
            ModalDrawerSheet(
                modifier = Modifier.fillMaxWidth(0.5f) // Or choose a value like 0.6f if 50% is too small
            ) {
                // 2. Use a Column to control the layout of items inside the drawer
                Column(modifier = Modifier.fillMaxHeight()) {
                    // You can add a header or other items here if you want
                    Text(
                        "User Menu",
                        style = MaterialTheme.typography.titleMedium,
                        modifier = Modifier.padding(16.dp)
                    )
                    HorizontalDivider()

                    // This spacer takes up all available vertical space, pushing the item below it to the bottom.
                    Spacer(Modifier.weight(1f))

                    // 3. The Logout button is now at the bottom
                    NavigationDrawerItem(
                        icon = { Icon(Icons.AutoMirrored.Filled.ExitToApp, contentDescription = "Logout") },
                        label = { Text("Log Out") },
                        selected = false,
                        onClick = {
                            navController.navigate("login") {
                                popUpTo(navController.graph.id) {
                                    inclusive = true
                                }
                            }
                        },
                        modifier = Modifier.padding(NavigationDrawerItemDefaults.ItemPadding)
                    )

                    // Add some padding at the very bottom
                    Spacer(Modifier.height(12.dp))
                }
            }

            // --- END OF CHANGES ---
        }
    ) {
        // 4. Use Scaffold to easily place the TopAppBar (with the user icon)
        //    and the main screen content.
        Scaffold(
            topBar = {
                TopAppBar(
                    title = { Text("RFID Scanner") },
                    navigationIcon = {
                        // 5. This is the user icon button on the top left.
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
            // This is the original content of your HomeScreen.
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding) // Use padding from the Scaffold
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
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Icon(
                imageVector = Icons.Default.AddCircle,
                contentDescription = "Scanner Icon",
                modifier = Modifier.size(64.dp),
                tint = MaterialTheme.colorScheme.primary
            )
            Text(
                text = "Alat Pemindai RFID",
                style = MaterialTheme.typography.titleLarge
            )
            Text(
                text = "Halo petugas laundry~!",
                style = MaterialTheme.typography.bodyMedium,
                textAlign = TextAlign.Center
            )
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

package com.example.ersteversion

import android.content.res.Configuration
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ExitToApp
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.example.ersteversion.data.AppContainer
import com.example.ersteversion.data.model.NutzerRolle
import com.example.ersteversion.data.model.countUnseenChanges
import com.example.ersteversion.data.repository.ChangeHistoryRepository
import com.example.ersteversion.ui.navigation.AppNavHost
import com.example.ersteversion.ui.navigation.BottomBar
import com.example.ersteversion.ui.navigation.Screen
import com.example.ersteversion.ui.theme.DunkelstesGrün
import com.example.ersteversion.ui.theme.ErsteVersionTheme
import kotlinx.coroutines.launch

// Startpunkt der App. Verwaltet Rollen inkl. deren Auswirkungen auf Top- und Bottombar
// Alle Previews enthalten auch eine Dunkelmodus-Preview. Da dafür aber gegen Ende die Zeit nicht gereicht hat, sehen diese gleich aus wie die Tages-Previews
// Wir haben diese Previews drin gelassen, falls in Zukunft nochmal ein Nachtmodus ergänzt werden sollte.

class MainActivity : ComponentActivity() {
    @OptIn(ExperimentalMaterial3Api::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {

            ErsteVersionTheme {
                val navController = rememberNavController()
                var rolle by rememberSaveable { mutableStateOf<NutzerRolle?>(null) }
                val appContainer = remember { AppContainer(context = applicationContext) }
                val coroutineScope = rememberCoroutineScope()
                var isRoomSelected by remember { mutableStateOf(false) }
                var backAction by remember { mutableStateOf<(() -> Unit)?>(null) }
                val currentBackStackEntry by navController.currentBackStackEntryAsState()
                val showBackArrow = currentBackStackEntry?.destination?.route == Screen.Settings.route
                var startDestinationReady by remember { mutableStateOf(false) }
                var unseenChangesCount by remember { mutableStateOf(0) }
                val snackbarHostState = remember { SnackbarHostState() }

                LaunchedEffect(Unit) {
                    val storedRole = appContainer.authRepository.getStoredRole()
                    if (storedRole != null) {
                        rolle = storedRole
                    }
                    startDestinationReady = true
                }

                LaunchedEffect(rolle) {
                    rolle?.let { r ->
                        try {
                            val repository = ChangeHistoryRepository(appContainer.apiService, r)
                            val changes = repository.getChanges()
                            val seenIds = appContainer.seenChangesDataSource.getSeenIds()
                            unseenChangesCount = countUnseenChanges( changes, seenIds)
                        } catch (e: Exception) {
                            // Zähler bleibt unverändert bei Netzwerkfehler
                        }
                    }
                }

                LaunchedEffect(Unit) {
                    appContainer.sessionExpired.collect {
                        rolle = null
                        navController.navigate(Screen.Login.route) {
                            popUpTo(0)
                        }
                        snackbarHostState.showSnackbar("Sitzung abgelaufen. Bitte erneut anmelden.")
                    }
                }

                if (!startDestinationReady) {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator()
                    } // Kurzer Ladebildschirm, während geprüft wird, ob ein Token existiert

                    return@ErsteVersionTheme
                }


                Scaffold(
                    snackbarHost = { SnackbarHost(snackbarHostState) },
                    topBar = {
                        val image = painterResource(R.drawable.studo_logo_lighttext_h100)
                        CenterAlignedTopAppBar(
                            colors = TopAppBarDefaults.topAppBarColors(
                                containerColor = MaterialTheme.colorScheme.primary,
                                titleContentColor = MaterialTheme.colorScheme.onPrimary,
                            ),
                            navigationIcon = {
                                if (isRoomSelected) {
                                    IconButton(onClick = {
                                        backAction?.invoke()
                                        isRoomSelected = false
                                    }) {
                                        Icon(
                                            Icons.AutoMirrored.Filled.ArrowBack,
                                            contentDescription = stringResource(R.string.zurueck),
                                            tint = DunkelstesGrün
                                        )
                                    }
                                } else if (showBackArrow) {
                                    IconButton(onClick = {
                                        navController.popBackStack()
                                    }) {
                                        Icon(Icons.AutoMirrored.Filled.ArrowBack,
                                            contentDescription = stringResource(R.string.zurueck),
                                            tint = DunkelstesGrün
                                        )
                                    }
                                }
                            },
                            title = {
                                Image(
                                    painter = image,
                                    contentDescription = stringResource(R.string.studo_logo),
                                    modifier = Modifier.height(32.dp)
                                )
                            },
                            actions = {
                                if (rolle == null) {
                                    IconButton(onClick = {
                                        navController.navigate(Screen.Settings.route) {
                                            launchSingleTop = true
                                        }
                                    }) {
                                        Icon(Icons.Default.Settings,
                                            contentDescription = stringResource(R.string.einstellungen),
                                            tint = DunkelstesGrün
                                        )
                                    }
                                }
                                if (rolle != null) {
                                    IconButton(onClick = {
                                        coroutineScope.launch {
                                            appContainer.authRepository.logout()
                                            rolle = null
                                            navController.navigate(Screen.Login.route) {
                                                popUpTo(0)
                                            }
                                        }
                                    }) {
                                        Icon(Icons.AutoMirrored.Filled.ExitToApp,
                                            contentDescription = stringResource(R.string.logout),
                                            tint = DunkelstesGrün
                                        )
                                    }
                                }
                            }
                        )
                    },
                    bottomBar = {
                        rolle?.let { aktuelleRolle ->
                            BottomBar(navController = navController, rolle = aktuelleRolle, badgeCount = unseenChangesCount )
                        }
                    }
                ) { innerPadding ->
                    AppNavHost(
                        navController = navController,
                        onLoginSuccess = { neueRolle -> rolle = neueRolle },
                        appContainer = appContainer,
                        onRoomSelectedInRoomPlan = { selected -> isRoomSelected = selected },
                        onRegisterBackAction = { action -> backAction = action },
                        initialRole = rolle,
                        modifier = Modifier.padding(innerPadding),
                        onChangesViewed = { unseenChangesCount = 0 }
                    )
                }
            }
        }
    }
}

@Preview(showBackground = true, heightDp = 800, name = "Tagmodus", uiMode = Configuration.UI_MODE_NIGHT_NO)
@Preview(showBackground = true, heightDp = 800, name = "Nachtmodus", uiMode = Configuration.UI_MODE_NIGHT_YES)
@OptIn(ExperimentalMaterial3Api::class)
@Preview(showBackground = true, name = "TopBar mit Zurück-Pfeil")
@Composable
private fun TopBarWithBackPreview() {
    ErsteVersionTheme {
        val image = painterResource(R.drawable.studo_logo_lighttext_h100)
        CenterAlignedTopAppBar(
            colors = TopAppBarDefaults.topAppBarColors(
                containerColor = MaterialTheme.colorScheme.primary,
                titleContentColor = MaterialTheme.colorScheme.onPrimary,
            ),
            navigationIcon = {
                IconButton(onClick = {}) {
                    Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Zurück")
                }
            },
            title = {
                Image(painter = image, contentDescription = "STudo Logo", modifier = Modifier.height(32.dp))
            },
            actions = {
                IconButton(onClick = {}) {
                    Icon(Icons.AutoMirrored.Filled.ExitToApp, contentDescription = "Logout")
                }
            }
        )
    }
}
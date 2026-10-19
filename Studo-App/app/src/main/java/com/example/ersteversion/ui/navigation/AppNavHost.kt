package com.example.ersteversion.ui.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.example.ersteversion.data.AppContainer
import com.example.ersteversion.data.model.NutzerRolle
import com.example.ersteversion.ui.changehistory.ChangeHistoryRoute
import com.example.ersteversion.ui.login.LoginRoute
import com.example.ersteversion.ui.roomplan.RoomPlanRoute
import com.example.ersteversion.ui.timetable.TimetableRoute
import androidx.compose.runtime.getValue
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import com.example.ersteversion.ui.settings.SettingsRoute

// Zentrale Navigationsstruktur der App

@Composable
fun AppNavHost(
    navController: NavHostController,
    onLoginSuccess: (NutzerRolle) -> Unit,
    appContainer: AppContainer,
    onRoomSelectedInRoomPlan: (Boolean) -> Unit,
    onRegisterBackAction: ((() -> Unit)) -> Unit,
    initialRole: NutzerRolle?,
    modifier: Modifier = Modifier,
    onChangesViewed: () -> Unit
) {
    var aktuelleRolle by rememberSaveable { mutableStateOf(initialRole ?: NutzerRolle.SCHUELER) }
    val startDestination = if (initialRole != null) Screen.Timetable.route else Screen.Login.route


    NavHost(navController, startDestination = startDestination, modifier = modifier) {
        composable(Screen.Login.route) {
            LoginRoute(appContainer = appContainer, onLoginSuccess = { rolle ->
                aktuelleRolle = rolle
                onLoginSuccess(rolle)
                navController.navigate(Screen.Timetable.route) {
                    popUpTo(Screen.Login.route) { inclusive = true }
                }
            })
        }
        composable(Screen.Timetable.route) {
            TimetableRoute(rolle = aktuelleRolle, appContainer = appContainer)
        }
        composable(Screen.RoomPlan.route) {
            RoomPlanRoute(
                appContainer = appContainer,
                onRoomSelectedChange = { selected -> onRoomSelectedInRoomPlan(selected) },
                registerBackAction = onRegisterBackAction
                )
        }
        composable(Screen.ChangeHistory.route) {
            ChangeHistoryRoute( role = aktuelleRolle, appContainer = appContainer, onChangesViewed = onChangesViewed )
        }
        composable(Screen.Settings.route) {
            SettingsRoute(appContainer = appContainer)
        }
    }
}

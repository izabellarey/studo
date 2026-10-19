package com.example.ersteversion.ui.navigation

import android.content.res.Configuration
import androidx.compose.runtime.getValue
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.List
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material3.Badge
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.example.ersteversion.R
import com.example.ersteversion.data.model.NutzerRolle
import com.example.ersteversion.ui.theme.DunkelstesGrün

// Untere Navigationsleiste

@Composable
fun BottomBar(navController: NavController, rolle: NutzerRolle, badgeCount: Int = 0) {
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination

    NavigationBar(
        containerColor = MaterialTheme.colorScheme.primary,
        contentColor = MaterialTheme.colorScheme.onPrimary,
        tonalElevation = 0.dp
    ) {
        NavigationBarItem(
            selected = currentRoute?.hierarchy?.any { it.route == Screen.Timetable.route } == true,
            onClick = {
                navController.navigate(Screen.Timetable.route) {
                    popUpTo(Screen.Timetable.route) { inclusive = true }
                }
            },
            icon = {
                Icon(
                    Icons.Default.DateRange,
                    contentDescription = stringResource(R.string.stundenplan)
                )
            },
            label = { Text(text = stringResource(R.string.stundenplan), color = Color.White) },
            colors = NavigationBarItemDefaults.colors(
                indicatorColor = Color.White,
                selectedIconColor = DunkelstesGrün,
                selectedTextColor = DunkelstesGrün,
                unselectedIconColor = Color.White,
                unselectedTextColor = Color.White
            )
        )

        if (rolle == NutzerRolle.LEHRKRAFT) {
            NavigationBarItem(
                selected = currentRoute?.hierarchy?.any { it.route == Screen.RoomPlan.route } == true,
                onClick = {
                    navController.navigate(Screen.RoomPlan.route) {
                        popUpTo(Screen.Timetable.route)
                    }
                },
                icon = {
                    Icon(
                        Icons.Default.List,
                        contentDescription = stringResource(R.string.raueme)
                    )
                },
                label = { Text(text = stringResource(R.string.raueme), color = Color.White) },
                colors = NavigationBarItemDefaults.colors(
                    indicatorColor = Color.White,
                    selectedIconColor = DunkelstesGrün,
                    selectedTextColor = DunkelstesGrün,
                    unselectedIconColor = Color.White,
                    unselectedTextColor = Color.White
                )
            )
        }

        NavigationBarItem(
            selected = currentRoute?.hierarchy?.any { it.route == Screen.ChangeHistory.route } == true,
            onClick = {
                navController.navigate(Screen.ChangeHistory.route) {
                    popUpTo(Screen.ChangeHistory.route) { inclusive = true }
                }
            },
            icon = {
                BadgedBox(badge = {
                    if (badgeCount > 0) {
                        Badge { Text(badgeCount.toString()) }
                    }
                }) {
                    Icon(
                        Icons.Default.Notifications,
                        contentDescription = stringResource(R.string.aenderungen)
                    )
                }
            },
            label = { Text(text = stringResource(R.string.aenderungen), color = Color.White) },
            colors = NavigationBarItemDefaults.colors(
                indicatorColor = Color.White,
                selectedIconColor = DunkelstesGrün,
                selectedTextColor = DunkelstesGrün,
                unselectedIconColor = Color.White,
                unselectedTextColor = Color.White
            )
        )
    }
}

@Preview(showBackground = true, name = "Tagmodus Lehrer", uiMode = Configuration.UI_MODE_NIGHT_NO)
@Preview(showBackground = true, name = "Nachtmodus Lehrer", uiMode = Configuration.UI_MODE_NIGHT_YES)
@Preview(showBackground = true, name = "Lehrkraft")
@Composable
private fun BottomBarLehrkraftPreview() {
    val navController = rememberNavController()
    BottomBar(navController = navController, rolle = NutzerRolle.LEHRKRAFT,)
}

@Preview(showBackground = true, name = "Tagmodus Schüler", uiMode = Configuration.UI_MODE_NIGHT_NO)
@Preview(showBackground = true, name = "Nachtmodus Schüler", uiMode = Configuration.UI_MODE_NIGHT_YES)
@Preview(showBackground = true, name = "Schüler")
@Composable
private fun BottomBarSchuelerPreview() {
    val navController = rememberNavController()
    BottomBar(navController = navController, rolle = NutzerRolle.SCHUELER,)
}
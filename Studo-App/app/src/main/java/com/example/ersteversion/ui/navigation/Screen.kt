package com.example.ersteversion.ui.navigation

sealed class Screen(val route: String) {
    object Login: Screen("login")
    object Timetable: Screen("timetable")
    object RoomPlan: Screen("roomplan")
    object ChangeHistory: Screen("changehistory")
    object Settings : Screen("Settings")
}
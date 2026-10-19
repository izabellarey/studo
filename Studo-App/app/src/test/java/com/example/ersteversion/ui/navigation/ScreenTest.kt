package com.example.ersteversion.ui.navigation

import org.junit.Assert.assertEquals
import org.junit.Test

class ScreenTest {
    @Test
    fun `Routen haben erwartete Werte`() {
        assertEquals("login", Screen.Login.route)
        assertEquals("timetable", Screen.Timetable.route)
        assertEquals("roomplan", Screen.RoomPlan.route)
        assertEquals("changehistory", Screen.ChangeHistory.route)
        assertEquals("Settings", Screen.Settings.route)
    }
}
package com.example.ersteversion.ui.navigation

import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.navigation.compose.rememberNavController
import com.example.ersteversion.data.model.NutzerRolle
import org.junit.Rule
import org.junit.Test

class BottomBarUiTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    @Test
    fun lehrkraft_siehtRaeumeTab() {
        composeTestRule.setContent {
            val navController = rememberNavController()
            BottomBar(navController = navController, rolle = NutzerRolle.LEHRKRAFT)
        }
        composeTestRule.onNodeWithText("Räume").assertExists()
    }

    @Test
    fun schueler_siehtKeinenRaeumeTab() {
        composeTestRule.setContent {
            val navController = rememberNavController()
            BottomBar(navController = navController, rolle = NutzerRolle.SCHUELER)
        }
        composeTestRule.onNodeWithText("Räume").assertDoesNotExist()
    }

    @Test
    fun beideRollen_sehenStundenplanTab() {
        composeTestRule.setContent {
            val navController = rememberNavController()
            BottomBar(navController = navController, rolle = NutzerRolle.SCHUELER)
        }
        composeTestRule.onNodeWithText("Stundenplan").assertExists()
    }

    @Test
    fun badgeCount_wirdAngezeigt_wennGroesserAlsNull() {
        composeTestRule.setContent {
            val navController = rememberNavController()
            BottomBar(navController = navController, rolle = NutzerRolle.LEHRKRAFT, badgeCount = 3)
        }
        composeTestRule.onNode(hasText("3"), useUnmergedTree = true).assertExists()
    }

    @Test
    fun badgeCount_wirdNichtAngezeigt_wennNull() {
        composeTestRule.setContent {
            val navController = rememberNavController()
            BottomBar(navController = navController, rolle = NutzerRolle.LEHRKRAFT, badgeCount = 0)
        }
        composeTestRule.onNode(hasText("0"), useUnmergedTree = true).assertDoesNotExist()
    }
}
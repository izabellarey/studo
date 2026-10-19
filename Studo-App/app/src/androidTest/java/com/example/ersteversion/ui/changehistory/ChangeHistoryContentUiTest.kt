package com.example.ersteversion.ui.changehistory

import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import com.example.ersteversion.data.model.Change
import org.junit.Rule
import org.junit.Test

class ChangeHistoryContentUiTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    @Test
    fun leereListe_zeigtHinweistext() {
        composeTestRule.setContent {
            ChangeHistoryContent(uiState = ChangeHistoryUiState(changes = emptyList()))
        }
        composeTestRule.onNodeWithText("Keine Änderungen vorhanden.").assertExists()
    }

    @Test
    fun changeTitel_wirdAngezeigt() {
        composeTestRule.setContent {
            ChangeHistoryContent(
                uiState = ChangeHistoryUiState(
                    changes = listOf(
                        Change(1, "AUSFALL", "Sport fällt aus", "Beschreibung", false)
                    )
                )
            )
        }
        composeTestRule.onNodeWithText("Sport fällt aus").assertExists()
    }
}
package com.example.ersteversion.ui.roomplan

import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollTo
import androidx.compose.ui.test.performScrollToIndex
import com.example.ersteversion.data.model.Room
import com.example.ersteversion.data.model.RoomCategory
import org.junit.Rule
import org.junit.Test

class RoomPlanContentUiTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    @Test
    fun kategorienUebersicht_zeigtRaumNamen() {
        composeTestRule.setContent {
            RoomPlanContent(
                uiState = RoomPlanUiState(
                    categories = listOf(
                        RoomCategory(id = "1", name = "Hauptgebäude", rooms = listOf(Room("101", "101")))
                    )
                ),
                onRoomClick = {},
                onBackClick = {}
            )
        }
        composeTestRule.onNodeWithText("101").assertExists()
        composeTestRule.onNodeWithText("Hauptgebäude").assertExists()
    }

    @Test
    fun klickAufRaum_ruftCallbackMitRichtigemRaumAuf() {
        var geklickterRaum: Room? = null
        val raum = Room("101", "101")
        composeTestRule.setContent {
            RoomPlanContent(
                uiState = RoomPlanUiState(
                    categories = listOf(RoomCategory(id = "1", name = "Hauptgebäude", rooms = listOf(raum)))
                ),
                onRoomClick = { geklickterRaum = it },
                onBackClick = {}
            )
        }
        composeTestRule.onNodeWithText("101").performClick()
        assert(geklickterRaum == raum)
    }

    @Test
    fun vieleKategorien_letzteIstErreichbarDurchScrollen() {
        val vieleKategorien = (1..15).map { i ->
            RoomCategory(id = "$i", name = "Kategorie $i", rooms = listOf(Room("$i", "Raum$i")))
        }
        composeTestRule.setContent {
            RoomPlanContent(
                uiState = RoomPlanUiState(categories = vieleKategorien),
                onRoomClick = {},
                onBackClick = {}
            )
        }
        composeTestRule.onNodeWithTag("categoryList").performScrollToIndex(14)
        composeTestRule.onNodeWithText("Raum15").assertExists()
    }
}
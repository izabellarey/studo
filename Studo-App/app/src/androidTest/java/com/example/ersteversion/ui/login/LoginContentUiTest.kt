package com.example.ersteversion.ui.login

import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTextInput
import org.junit.Rule
import org.junit.Test

class LoginContentUiTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    @Test
    fun loginButton_zeigtLadeindikator_wennIsLoadingTrue() {
        composeTestRule.setContent {
            LoginContent(
                uiState = LoginUiState(isLoading = true),
                onUsernameChange = {},
                onPasswordChange = {},
                onLoginClick = {}
            )
        }
        composeTestRule.onNodeWithText("Login").assertDoesNotExist()
    }

    @Test
    fun fehlermeldung_wirdAngezeigt_wennErrorMessageGesetzt() {
        composeTestRule.setContent {
            LoginContent(
                uiState = LoginUiState(errorMessage = "Benutzername oder Passwort falsch."),
                onUsernameChange = {},
                onPasswordChange = {},
                onLoginClick = {}
            )
        }
        composeTestRule.onNodeWithText("Benutzername oder Passwort falsch.").assertExists()
    }

    @Test
    fun loginButton_klick_ruftCallbackAuf() {
        var clicked = false
        composeTestRule.setContent {
            LoginContent(
                uiState = LoginUiState(),
                onUsernameChange = {},
                onPasswordChange = {},
                onLoginClick = { clicked = true }
            )
        }
        composeTestRule.onNodeWithText("Login").performClick()
        assert(clicked)
    }

    @Test
    fun eingabe_wirdWeitergegeben() {
        var eingegebenerName = ""
        composeTestRule.setContent {
            LoginContent(
                uiState = LoginUiState(),
                onUsernameChange = { eingegebenerName = it },
                onPasswordChange = {},
                onLoginClick = {}
            )
        }
        composeTestRule.onNodeWithText("Nutzername").performTextInput("web")
        assert(eingegebenerName == "web")
    }
}
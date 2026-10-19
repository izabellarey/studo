package com.example.ersteversion.ui.login

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.example.ersteversion.data.AppContainer
import com.example.ersteversion.data.model.NutzerRolle

@Composable
fun LoginRoute(
    appContainer: AppContainer,
    onLoginSuccess: (NutzerRolle) -> Unit
) {
    val factory = viewModelFactory {
        initializer {
            LoginViewModel(appContainer.authRepository)
        }
    }
    val viewModel: LoginViewModel = viewModel(factory = factory)
    val uiState by viewModel.uiState.collectAsState()

    LoginContent(
        uiState = uiState,
        onUsernameChange = viewModel::onUsernameChange,
        onPasswordChange = viewModel::onPasswordChange,
        onLoginClick = { viewModel.login(onSuccess = onLoginSuccess) }
    )
}
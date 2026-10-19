package com.example.ersteversion.ui.settings

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.example.ersteversion.data.AppContainer

@Composable
fun SettingsRoute(appContainer: AppContainer) {
    val factory = viewModelFactory {
        initializer {
            SettingsViewModel(appContainer.serverUrlDataSource)
        }
    }
    val viewModel: SettingsViewModel = viewModel(factory = factory)
    val uiState by viewModel.uiState.collectAsState()

    SettingsContent(
        uiState = uiState,
        onUrlChange = viewModel::onUrlChange,
        onSaveClick = { viewModel.saveUrl() }
    )
}
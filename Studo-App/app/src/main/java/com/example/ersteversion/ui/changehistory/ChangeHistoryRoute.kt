package com.example.ersteversion.ui.changehistory

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.example.ersteversion.data.AppContainer
import com.example.ersteversion.data.model.NutzerRolle
import com.example.ersteversion.data.repository.ChangeHistoryRepository

@Composable
fun ChangeHistoryRoute(role: NutzerRolle, appContainer: AppContainer, onChangesViewed: () -> Unit) {
    val factory = viewModelFactory {
        initializer {
            ChangeHistoryViewModel(ChangeHistoryRepository(appContainer.apiService, role))
        }
    }
    val viewModel: ChangeHistoryViewModel = viewModel(factory = factory)
    val uiState by viewModel.uiState.collectAsState()

    LaunchedEffect(uiState.changes) {
        if (uiState.changes.isNotEmpty()) {
            val ids = uiState.changes.map { it.id }.toSet()
            appContainer.seenChangesDataSource.markAsSeen(ids)
            onChangesViewed()
        }
    }

    ChangeHistoryContent(uiState = uiState)
}
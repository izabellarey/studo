package com.example.ersteversion.ui.timetable

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.example.ersteversion.data.AppContainer
import com.example.ersteversion.data.model.NutzerRolle
import com.example.ersteversion.data.repository.TimetableRepository

@Composable
fun TimetableRoute(rolle: NutzerRolle, appContainer: AppContainer) {
    val factory = viewModelFactory {
        initializer {
            TimetableViewModel(TimetableRepository(appContainer.apiService, rolle))
        }
    }
    val viewModel: TimetableViewModel = viewModel(factory = factory)
    val uiState by viewModel.uiState.collectAsState()

    TimetableContent(uiState = uiState, rolle = rolle)
}
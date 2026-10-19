package com.example.ersteversion.ui.roomplan

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.example.ersteversion.data.AppContainer
import com.example.ersteversion.data.repository.RoomPlanRepository

@Composable
fun RoomPlanRoute(
    appContainer: AppContainer,
    onRoomSelectedChange: (Boolean) -> Unit,
    registerBackAction: ((() -> Unit)) -> Unit
) {
    val factory = viewModelFactory {
        initializer {
            RoomPlanViewModel(RoomPlanRepository(appContainer.apiService))
        }
    }
    val viewModel: RoomPlanViewModel = viewModel(factory = factory)
    val uiState by viewModel.uiState.collectAsState()

    LaunchedEffect(uiState.selectedRoom) {
        onRoomSelectedChange(uiState.selectedRoom != null)
        registerBackAction { viewModel.backToOverview() }
    }

    RoomPlanContent(
        uiState = uiState,
        onRoomClick = { room -> viewModel.selectRoom(room) },
        onBackClick = { viewModel.backToOverview() }
    )
}
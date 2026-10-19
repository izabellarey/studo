package com.example.ersteversion.ui.roomplan

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.ersteversion.data.model.Room
import com.example.ersteversion.data.repository.RoomPlanRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class RoomPlanViewModel(
    private val repository: RoomPlanRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(RoomPlanUiState())
    val uiState: StateFlow<RoomPlanUiState> = _uiState.asStateFlow()

    init {
        loadCategories()
    }

    fun loadCategories() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)
            try {
                val categories = repository.getRoomCategories()
                val raster = repository.getStundenraster()
                _uiState.value = _uiState.value.copy(
                    categories = categories,
                    stundenraster = raster,
                    isLoading = false
                )
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(isLoading = false, errorMessage = e.message)
            }
        }
    }

    fun selectRoom(room: Room) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)
            try {
                val roomPlan = repository.getRoomPlan(room)
                _uiState.value = _uiState.value.copy(
                    selectedRoom = room,
                    selectedRoomPlan = roomPlan,
                    isLoading = false
                )
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(isLoading = false, errorMessage = e.message)
            }
        }
    }

    fun backToOverview() {
        _uiState.value = _uiState.value.copy(selectedRoom = null, selectedRoomPlan = null)
    }
}
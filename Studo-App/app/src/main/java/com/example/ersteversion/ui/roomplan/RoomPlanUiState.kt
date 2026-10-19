package com.example.ersteversion.ui.roomplan

import com.example.ersteversion.data.model.Room
import com.example.ersteversion.data.model.RoomCategory
import com.example.ersteversion.data.model.RoomPlan
import com.example.ersteversion.data.model.Stundenraster

data class RoomPlanUiState(
    val categories: List<RoomCategory> = emptyList(),
    val stundenraster: Stundenraster = Stundenraster(slots = emptyList()),
    val selectedRoom: Room? = null,
    val selectedRoomPlan: RoomPlan? = null,
    val isLoading: Boolean = false,
    val errorMessage: String? = null
)
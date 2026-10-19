package com.example.ersteversion.ui.timetable

import com.example.ersteversion.data.model.Stundenraster
import com.example.ersteversion.data.model.Timetable

data class TimetableUiState(
    val timetable: Timetable = Timetable(lessons = emptyList()),
    val stundenraster: Stundenraster = Stundenraster(slots = emptyList()),
    val isLoading: Boolean = false,
    val errorMessage: String? = null
)
package com.example.ersteversion.ui.changehistory

import com.example.ersteversion.data.model.Change

data class ChangeHistoryUiState(
    val changes: List<Change> = emptyList(),
    val isLoading: Boolean = false,
    val errorMessage: String? = null
)
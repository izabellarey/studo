package com.example.ersteversion.ui.settings

data class SettingsUiState(
    val serverUrl: String = "",
    val saved: Boolean = false,
    val isTesting: Boolean = false,
    val errorMessage: String? = null
)
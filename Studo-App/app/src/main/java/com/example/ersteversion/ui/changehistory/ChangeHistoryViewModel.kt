package com.example.ersteversion.ui.changehistory

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.ersteversion.data.repository.ChangeHistoryRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class ChangeHistoryViewModel(
    private val repository: ChangeHistoryRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(ChangeHistoryUiState())
    val uiState: StateFlow<ChangeHistoryUiState> = _uiState.asStateFlow()

    init {
        loadChanges()
    }

    fun loadChanges() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)
            try {
                val changes = repository.getChanges()
                _uiState.value = _uiState.value.copy(changes = changes, isLoading = false)
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(isLoading = false, errorMessage = e.message)
            }
        }
    }
}
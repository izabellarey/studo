package com.example.ersteversion.ui.login

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.ersteversion.data.model.NutzerRolle
import com.example.ersteversion.data.repository.AuthRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class LoginViewModel(
    private val repository: AuthRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(LoginUiState())
    val uiState: StateFlow<LoginUiState> = _uiState.asStateFlow()

    fun onUsernameChange(value: String) {
        _uiState.value = _uiState.value.copy(username = value, errorMessage = null)
    }

    fun onPasswordChange(value: String) {
        _uiState.value = _uiState.value.copy(password = value, errorMessage = null)
    }

    fun login(onSuccess: (NutzerRolle) -> Unit) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, errorMessage = null)
            try {
                val role = repository.login(_uiState.value.username, _uiState.value.password)
                _uiState.value = _uiState.value.copy(isLoading = false)
                onSuccess(role)
            } catch (e: retrofit2.HttpException) {
                val message = when (e.code()) {
                    401, 403 -> "Benutzername oder Passwort falsch."
                    else -> "Fehler bei der Anmeldung (Code ${e.code()})."
                }
                _uiState.value = _uiState.value.copy(isLoading = false, errorMessage = message)
            } catch (e: java.io.IOException) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    errorMessage = "Server nicht erreichbar. Bitte Verbindung prüfen."
                )
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    errorMessage = "Unbekannter Fehler: ${e.message}"
                )
            }
        }
    }
}
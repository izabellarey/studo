package com.example.ersteversion.ui.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.ersteversion.data.datastore.ServerUrlDataSource
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import java.io.IOException

class SettingsViewModel(
    private val serverUrlDataSource: ServerUrlDataSource
) : ViewModel() {

    private val _uiState = MutableStateFlow(SettingsUiState())
    val uiState: StateFlow<SettingsUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            val currentUrl = serverUrlDataSource.getUrl()
            _uiState.value = _uiState.value.copy(serverUrl = currentUrl)
        }
    }

    fun onUrlChange(value: String) {
        _uiState.value = _uiState.value.copy(serverUrl = value, saved = false, errorMessage = null)
    }

    fun saveUrl() {
        val url = _uiState.value.serverUrl.trim()

        if (!isValidUrlFormat(url)) {
            _uiState.value = _uiState.value.copy(errorMessage = "Ungültiges URL-Format. Beispiel: http://192.168.1.5:8080/")
            return
        }

        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isTesting = true, errorMessage = null)

            val reachable = testConnection(url)

            if (reachable) {
                serverUrlDataSource.saveUrl(url)
                _uiState.value = _uiState.value.copy(isTesting = false, saved = true)
            } else {
                _uiState.value = _uiState.value.copy(
                    isTesting = false,
                    errorMessage = "Server unter dieser URL nicht erreichbar."
                )
            }
        }
    }

    private fun isValidUrlFormat(url: String): Boolean {
        return url.startsWith("http://") || url.startsWith("https://")
    }

    private suspend fun testConnection(url: String): Boolean = withContext(Dispatchers.IO) {
        try {
            val client = OkHttpClient()
            val request = Request.Builder().url(url).build()
            val response = client.newCall(request).execute()
            response.isSuccessful || response.code in 400..499
        } catch (e: IOException) {
            false
        }
    }
}
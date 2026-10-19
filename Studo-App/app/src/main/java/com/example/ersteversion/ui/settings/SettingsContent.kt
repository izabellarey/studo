package com.example.ersteversion.ui.settings

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.example.ersteversion.R

// Konfigurierbare URL anzeigen

@Composable
fun SettingsContent(
    uiState: SettingsUiState,
    onUrlChange: (String) -> Unit,
    onSaveClick: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.Center
    ) {
        Text(stringResource(R.string.server_url))
        OutlinedTextField(
            value = uiState.serverUrl,
            onValueChange = onUrlChange,
            enabled = !uiState.isTesting,
            modifier = Modifier.padding(top = 8.dp)
        )

        if (uiState.errorMessage != null) {
            Text(uiState.errorMessage, color = Color.Red, modifier = Modifier.padding(top = 8.dp))
        }

        if (uiState.isTesting) {
            CircularProgressIndicator(modifier = Modifier.padding(top = 16.dp))
        } else {
            Button(onClick = onSaveClick, modifier = Modifier.padding(top = 16.dp)) {
                Text(stringResource(R.string.speichern))
            }
        }

        if (uiState.saved) {
            Text(stringResource(R.string.gespeichert_aenderungen_wirken_sofort), modifier = Modifier.padding(top = 8.dp))
        }
    }
}
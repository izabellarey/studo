package com.example.ersteversion.ui.login

import android.content.res.Configuration
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.ersteversion.R
import androidx.compose.material3.OutlinedTextFieldDefaults

@Composable
fun LoginContent(
    uiState: LoginUiState,
    onUsernameChange: (String) -> Unit,
    onPasswordChange: (String) -> Unit,
    onLoginClick: () -> Unit
) {
    var passwordVisible by remember { mutableStateOf(false) }

    val fieldColors = OutlinedTextFieldDefaults.colors(
        focusedTextColor = MaterialTheme.colorScheme.onSurface,
        unfocusedTextColor = MaterialTheme.colorScheme.onSurface,
        focusedBorderColor = MaterialTheme.colorScheme.onSurface,
        unfocusedBorderColor = MaterialTheme.colorScheme.onSurface,
        focusedLabelColor = MaterialTheme.colorScheme.onSurface,
        unfocusedLabelColor = MaterialTheme.colorScheme.onSurface,
        cursorColor = MaterialTheme.colorScheme.onSurface
    )

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        OutlinedTextField(
            value = uiState.username,
            onValueChange = onUsernameChange,
            label = { Text(stringResource(R.string.nutzername)) },
            enabled = !uiState.isLoading,
            colors = fieldColors
        )
        OutlinedTextField(
            value = uiState.password,
            onValueChange = onPasswordChange,
            label = { Text(stringResource(R.string.passwort)) },
            enabled = !uiState.isLoading,
            visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
            colors = fieldColors,
            trailingIcon = {
                IconButton(onClick = { passwordVisible = !passwordVisible }) {
                    Icon(
                        imageVector = if (passwordVisible) Icons.Default.Visibility else Icons.Default.VisibilityOff,
                        contentDescription = if (passwordVisible) stringResource(R.string.passwort_verbergen) else stringResource(
                            R.string.passwort_anzeigen
                        ),
                        tint = MaterialTheme.colorScheme.onSurface
                    )
                }
            }
        )

        if (uiState.errorMessage != null) {
            Text(
                text = uiState.errorMessage,
                color = Color.Red,
                modifier = Modifier.padding(top = 8.dp)
            )
        }

        if (uiState.isLoading) {
            CircularProgressIndicator(modifier = Modifier.padding(top = 16.dp))
        } else {
            Button(
                onClick = onLoginClick,
                modifier = Modifier.padding(top = 16.dp)
            ) {
                Text(stringResource(R.string.login))
            }
        }
    }
}

@Preview(showBackground = true, heightDp = 800, name = "leer Tagmodus", uiMode = Configuration.UI_MODE_NIGHT_NO)
@Preview(showBackground = true, heightDp = 800, name = "leer Nachtmodus", uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
private fun LoginContentPreview() {
    val fakeState = LoginUiState(
        username = "web",
        password = "start1234"
    )
    LoginContent(
        uiState = fakeState,
        onUsernameChange = {},
        onPasswordChange = {},
        onLoginClick = {}
    )
}

@Preview(showBackground = true, heightDp = 800, name = "Fehlermeldung Tagmodus", uiMode = Configuration.UI_MODE_NIGHT_NO)
@Preview(showBackground = true, heightDp = 800, name = "Fehlermeldung Nachtmodus", uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
private fun LoginContentErrorPreview() {
    val fakeState = LoginUiState(
        username = "web",
        password = "falsch",
        errorMessage = "Benutzername oder Passwort falsch."
    )
    LoginContent(
        uiState = fakeState,
        onUsernameChange = {},
        onPasswordChange = {},
        onLoginClick = {}
    )
}

@Preview(showBackground = true, heightDp = 800, name = "Ladezustand Tagmodus", uiMode = Configuration.UI_MODE_NIGHT_NO)
@Preview(showBackground = true, heightDp = 800, name = "Ladezustand Nachtmodus", uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
private fun LoginContentLoadingPreview() {
    val fakeState = LoginUiState(
        username = "web",
        password = "start1234",
        isLoading = true
    )
    LoginContent(
        uiState = fakeState,
        onUsernameChange = {},
        onPasswordChange = {},
        onLoginClick = {}
    )
}

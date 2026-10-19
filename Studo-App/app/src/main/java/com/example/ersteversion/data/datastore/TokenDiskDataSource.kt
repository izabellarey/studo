package com.example.ersteversion.data.datastore

import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.first

//
private val Context.dataStore by preferencesDataStore(name = "auth_prefs")
private val TOKEN_KEY = stringPreferencesKey("auth_token")

// Tokenverwaltung

class TokenDiskDataSource(private val context: Context) {

    suspend fun saveToken(token: String) {
        context.dataStore.edit { prefs -> prefs[TOKEN_KEY] = token }
    } // Speichert einen Token

    suspend fun getToken(): String? {
        return context.dataStore.data.first()[TOKEN_KEY]
    } // Wird beim Login genutzt, holt einen Token

    suspend fun clearToken() {
        context.dataStore.edit { prefs -> prefs.remove(TOKEN_KEY) }
    } // Wird beim Lougout genutzt, entfert einen Token
}
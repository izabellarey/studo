package com.example.ersteversion.data.datastore

import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.first
import com.example.ersteversion.data.network.NetworkConfig

// Datenzugriffsschicht für die Server-URL

private val Context.serverUrlDataStore by preferencesDataStore(name = "server_prefs")
private val SERVER_URL_KEY = stringPreferencesKey("server_url")

class ServerUrlDataSource(private val context: Context) {

    suspend fun saveUrl(url: String) {
        context.serverUrlDataStore.edit { prefs -> prefs[SERVER_URL_KEY] = url }
    }

    suspend fun getUrl(): String {
        return context.serverUrlDataStore.data.first()[SERVER_URL_KEY] ?: NetworkConfig.DEFAULT_BASE_URL
    }
}
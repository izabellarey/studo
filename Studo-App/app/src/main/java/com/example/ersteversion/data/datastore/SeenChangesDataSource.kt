package com.example.ersteversion.data.datastore

import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringSetPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.first

private val Context.seenChangesDataStore by preferencesDataStore(name = "seen_changes_prefs")
private val SEEN_IDS_KEY = stringSetPreferencesKey("seen_change_ids")

// Infos über die neuen (ungesehenen) Änderungen

class SeenChangesDataSource(private val context: Context) {

    suspend fun getSeenIds(): Set<String> {
        return context.seenChangesDataStore.data.first()[SEEN_IDS_KEY] ?: emptySet()
    }

    suspend fun markAsSeen(ids: Set<Int>) {
        val current = getSeenIds()
        context.seenChangesDataStore.edit { prefs ->
            prefs[SEEN_IDS_KEY] = current + ids.map { it.toString() }
        }
    }
    suspend fun clearSeenIds() {
        context.seenChangesDataStore.edit { prefs -> prefs.remove(SEEN_IDS_KEY ) }
    }
}
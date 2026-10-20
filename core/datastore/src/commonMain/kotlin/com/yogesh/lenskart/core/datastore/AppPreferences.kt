package com.yogesh.lenskart.core.datastore

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.PreferenceDataStoreFactory
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import okio.Path.Companion.toPath

/**
 * Jetpack DataStore is multiplatform: the same Preferences API on Android, iOS and desktop.
 * Only the FILE PATH differs per platform (see DataStoreModule.* actuals).
 * NOTE: in a real banking-grade app keep the token in Keystore/Keychain instead of DataStore.
 */
class AppPreferences(private val store: DataStore<Preferences>) {
    val themeMode: Flow<String?> = store.data.map { it[THEME] }
    val authToken: Flow<String?> = store.data.map { it[TOKEN] }
    val userJson: Flow<String?> = store.data.map { it[USER] }

    suspend fun setThemeMode(value: String) { store.edit { it[THEME] = value } }
    suspend fun saveSession(token: String, userJson: String) {
        store.edit { it[TOKEN] = token; it[USER] = userJson }
    }
    suspend fun clearSession() { store.edit { it.remove(TOKEN); it.remove(USER) } }

    private companion object {
        val THEME = stringPreferencesKey("theme_mode")
        val TOKEN = stringPreferencesKey("auth_token")
        val USER = stringPreferencesKey("user_json")
    }
}

internal const val DATASTORE_FILE = "lenskart.preferences_pb"

internal fun createDataStore(producePath: () -> String): DataStore<Preferences> =
    PreferenceDataStoreFactory.createWithPath(produceFile = { producePath().toPath() })

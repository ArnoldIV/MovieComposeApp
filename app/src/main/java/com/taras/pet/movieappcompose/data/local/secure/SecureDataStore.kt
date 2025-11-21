package com.taras.pet.movieappcompose.data.local.secure

import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import androidx.security.crypto.MasterKey
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.dataStore by preferencesDataStore(name = "secure_prefs")

class SecureDataStore(context: Context) {
    private val masterKey = MasterKey.Builder(context)
        .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
        .build()

    private val dataStore = context.dataStore

    companion object {
        val API_KEY = stringPreferencesKey("encrypted_api_key")
    }

    suspend fun saveApiKey(value: String) {
        dataStore.edit { prefs ->
            prefs[API_KEY] = value
        }
    }

    val apiKey: Flow<String?> = dataStore.data.map { prefs ->
        prefs[API_KEY]
    }
}
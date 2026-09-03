package com.ikogetech.ikogemind.data.repository

import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map

private val Context.dataStore by preferencesDataStore(name = "ikogemind_settings")

/**
 * ASSUMPTION (flagged, per model-routing.md TODO "API keys management approach"):
 * using Jetpack DataStore Preferences for now, in plaintext on-device storage.
 * This is NOT encrypted at rest. Fine for personal testing (v1), but before this
 * goes to any other user, swap the underlying storage for EncryptedSharedPreferences
 * or the Security-Crypto DataStore wrapper. Call sites below don't need to change,
 * only the implementation of get/set.
 */
class SettingsRepository(private val context: Context) {

    private object Keys {
        val GEMINI_API_KEY = stringPreferencesKey("gemini_api_key")
        val OPENROUTER_API_KEY = stringPreferencesKey("openrouter_api_key")
        val PREFERRED_PROVIDER = stringPreferencesKey("preferred_provider") // "auto" | "gemini" | "openrouter"
    }

    val geminiApiKey: Flow<String?> =
        context.dataStore.data.map { it[Keys.GEMINI_API_KEY] }

    val openRouterApiKey: Flow<String?> =
        context.dataStore.data.map { it[Keys.OPENROUTER_API_KEY] }

    val preferredProvider: Flow<String> =
        context.dataStore.data.map { it[Keys.PREFERRED_PROVIDER] ?: "auto" }

    suspend fun setGeminiApiKey(key: String) {
        context.dataStore.edit { it[Keys.GEMINI_API_KEY] = key }
    }

    suspend fun setOpenRouterApiKey(key: String) {
        context.dataStore.edit { it[Keys.OPENROUTER_API_KEY] = key }
    }

    suspend fun setPreferredProvider(provider: String) {
        context.dataStore.edit { it[Keys.PREFERRED_PROVIDER] = provider }
    }

    /** Synchronous-ish snapshot helper for the router, which needs a key at call time. */
    suspend fun currentGeminiKey(): String? = geminiApiKey.first()
    suspend fun currentOpenRouterKey(): String? = openRouterApiKey.first()

    suspend fun hasAnyKeyConfigured(): Boolean =
        !currentGeminiKey().isNullOrBlank() || !currentOpenRouterKey().isNullOrBlank()
}

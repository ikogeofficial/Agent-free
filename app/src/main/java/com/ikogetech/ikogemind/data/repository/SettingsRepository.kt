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
 * This is NOT encrypted at rest yet — the decided v1 approach is DataStore +
 * manual Android Keystore AES (not EncryptedSharedPreferences, which Google
 * deprecated in 2025) — that encryption layer is a separate follow-up. Call sites
 * below don't need to change when it lands, only the implementation of get/set.
 *
 * Four keys total: Gemini, plus one OpenRouter key PER hand-picked model (Llama
 * 3.1 405B / Qwen3 Coder / gpt-oss-120b) — per the "one key per model" fallback
 * decision, not one shared OpenRouter key. There's no "preferred provider" setting
 * here; v1 is a single fixed fallback chain (see ModelRouter), not user-selectable
 * per decisions-log.md "global model default only" — a prior preferredProvider
 * field existed but was never actually read by ModelRouter, so it's removed here
 * rather than left as dead state that implied a control that didn't exist.
 */
class SettingsRepository(private val context: Context) {

    private object Keys {
        val GEMINI_API_KEY = stringPreferencesKey("gemini_api_key")
        val OPENROUTER_KEY_LLAMA = stringPreferencesKey("openrouter_key_llama_3_1_405b")
        val OPENROUTER_KEY_QWEN_CODER = stringPreferencesKey("openrouter_key_qwen3_coder")
        val OPENROUTER_KEY_GPT_OSS = stringPreferencesKey("openrouter_key_gpt_oss_120b")
    }

    val geminiApiKey: Flow<String?> =
        context.dataStore.data.map { it[Keys.GEMINI_API_KEY] }

    val openRouterLlamaKey: Flow<String?> =
        context.dataStore.data.map { it[Keys.OPENROUTER_KEY_LLAMA] }

    val openRouterQwenCoderKey: Flow<String?> =
        context.dataStore.data.map { it[Keys.OPENROUTER_KEY_QWEN_CODER] }

    val openRouterGptOssKey: Flow<String?> =
        context.dataStore.data.map { it[Keys.OPENROUTER_KEY_GPT_OSS] }

    suspend fun setGeminiApiKey(key: String) {
        context.dataStore.edit { it[Keys.GEMINI_API_KEY] = key }
    }

    suspend fun setOpenRouterLlamaKey(key: String) {
        context.dataStore.edit { it[Keys.OPENROUTER_KEY_LLAMA] = key }
    }

    suspend fun setOpenRouterQwenCoderKey(key: String) {
        context.dataStore.edit { it[Keys.OPENROUTER_KEY_QWEN_CODER] = key }
    }

    suspend fun setOpenRouterGptOssKey(key: String) {
        context.dataStore.edit { it[Keys.OPENROUTER_KEY_GPT_OSS] = key }
    }

    /** Synchronous-ish snapshot helpers for the router, which needs keys at call time. */
    suspend fun currentGeminiKey(): String? = geminiApiKey.first()
    suspend fun currentOpenRouterLlamaKey(): String? = openRouterLlamaKey.first()
    suspend fun currentOpenRouterQwenCoderKey(): String? = openRouterQwenCoderKey.first()
    suspend fun currentOpenRouterGptOssKey(): String? = openRouterGptOssKey.first()

    suspend fun hasAnyKeyConfigured(): Boolean =
        !currentGeminiKey().isNullOrBlank() ||
            !currentOpenRouterLlamaKey().isNullOrBlank() ||
            !currentOpenRouterQwenCoderKey().isNullOrBlank() ||
            !currentOpenRouterGptOssKey().isNullOrBlank()
}

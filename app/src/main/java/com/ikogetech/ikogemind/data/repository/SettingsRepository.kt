package com.ikogetech.ikogemind.data.repository

import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.ikogetech.ikogemind.data.security.KeystoreCrypto
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map

private val Context.dataStore by preferencesDataStore(name = "ikogemind_settings")

/**
 * API keys are encrypted at rest via Android Keystore AES-GCM (KeystoreCrypto)
 * before being written to DataStore — DataStore never sees plaintext. Decided
 * over EncryptedSharedPreferences (deprecated by Google in 2025) and over Tink
 * (heavier than 4 string values need). See decisions-log.md.
 *
 * Reads are decrypt-or-fall-back-to-raw: any key saved before this encryption
 * layer existed is stored as plaintext, so decryptOrNull() returns null for it
 * and the raw value is used as-is. It gets encrypted automatically the next time
 * it's saved — no forced re-entry, no migration step for the user to run.
 *
 * Four keys total: Gemini, plus one OpenRouter key PER hand-picked model (Llama
 * 3.1 405B / Qwen3 Coder / gpt-oss-120b) — per the "one key per model" fallback
 * decision, not one shared OpenRouter key. There's no "preferred provider"
 * setting here; v1 is a single fixed fallback chain (see ModelRouter), not
 * user-selectable, per decisions-log.md "global model default only" — a prior
 * preferredProvider field existed but was never actually read by ModelRouter,
 * so it was removed rather than left as dead state implying a control that
 * didn't exist.
 */
class SettingsRepository(private val context: Context) {

    private object Keys {
        val GEMINI_API_KEY = stringPreferencesKey("gemini_api_key")
        val OPENROUTER_KEY_LLAMA = stringPreferencesKey("openrouter_key_llama_3_1_405b")
        val OPENROUTER_KEY_QWEN_CODER = stringPreferencesKey("openrouter_key_qwen3_coder")
        val OPENROUTER_KEY_GPT_OSS = stringPreferencesKey("openrouter_key_gpt_oss_120b")
    }

    /** Decrypt-or-fall-back-to-raw, see class doc. Blank stays blank either way. */
    private fun decodeStored(raw: String): String =
        if (raw.isBlank()) raw else KeystoreCrypto.decryptOrNull(raw) ?: raw

    val geminiApiKey: Flow<String?> =
        context.dataStore.data.map { it[Keys.GEMINI_API_KEY]?.let(::decodeStored) }

    val openRouterLlamaKey: Flow<String?> =
        context.dataStore.data.map { it[Keys.OPENROUTER_KEY_LLAMA]?.let(::decodeStored) }

    val openRouterQwenCoderKey: Flow<String?> =
        context.dataStore.data.map { it[Keys.OPENROUTER_KEY_QWEN_CODER]?.let(::decodeStored) }

    val openRouterGptOssKey: Flow<String?> =
        context.dataStore.data.map { it[Keys.OPENROUTER_KEY_GPT_OSS]?.let(::decodeStored) }

    suspend fun setGeminiApiKey(key: String) {
        context.dataStore.edit { it[Keys.GEMINI_API_KEY] = KeystoreCrypto.encrypt(key) }
    }

    suspend fun setOpenRouterLlamaKey(key: String) {
        context.dataStore.edit { it[Keys.OPENROUTER_KEY_LLAMA] = KeystoreCrypto.encrypt(key) }
    }

    suspend fun setOpenRouterQwenCoderKey(key: String) {
        context.dataStore.edit { it[Keys.OPENROUTER_KEY_QWEN_CODER] = KeystoreCrypto.encrypt(key) }
    }

    suspend fun setOpenRouterGptOssKey(key: String) {
        context.dataStore.edit { it[Keys.OPENROUTER_KEY_GPT_OSS] = KeystoreCrypto.encrypt(key) }
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

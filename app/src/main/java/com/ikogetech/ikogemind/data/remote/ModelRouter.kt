package com.ikogetech.ikogemind.data.remote

import com.ikogetech.ikogemind.data.repository.SettingsRepository
import kotlinx.coroutines.delay
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.HttpException
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

/** A single turn of chat history, provider-agnostic. */
data class ChatTurn(val role: String, val content: String)

data class ModelResult(
    val text: String,
    val providerUsed: String // e.g. "gemini" or "openrouter:meta-llama/llama-3.1-405b-instruct:free"
)

class ModelRouterException(message: String, val isRateLimit: Boolean) : Exception(message)

/**
 * Routing logic per the fallback-ordering decision (decisions-log.md):
 * 1. Gemini first, with ONE quick retry (short backoff) on a transient failure
 *    (429/5xx) before counting it as failed — most rate-limit hits are transient,
 *    so this alone absorbs a chunk of would-be errors. A non-transient failure
 *    (bad request, 404, etc.) falls through immediately without wasting a retry.
 * 2. On confirmed Gemini failure, fall through OpenRouter's three hand-picked free
 *    models in a fixed order, each behind its OWN API key (one key per model, per
 *    decision) — Llama 3.1 405B -> Qwen3 Coder -> gpt-oss-120b. One attempt each,
 *    no retry per model; a missing key or failed call just moves to the next.
 * 3. Free OpenRouter slugs rotate out with little notice — this is what caused the
 *    original hardcoded-list 404s during testing. As a last safety net before
 *    giving up, try OpenRouter's own "openrouter/free" auto-router once, using
 *    whichever of the three keys is available, so one retired slug doesn't
 *    dead-end the user.
 * 4. Caller (ModelStep) persists which provider actually served the response.
 */
class ModelRouter(private val settingsRepository: SettingsRepository) {

    private val loggingInterceptor = HttpLoggingInterceptor().apply {
        level = HttpLoggingInterceptor.Level.BASIC
    }
    private val okHttpClient = OkHttpClient.Builder()
        .addInterceptor(loggingInterceptor)
        .build()

    private val geminiApi: GeminiApi by lazy {
        Retrofit.Builder()
            .baseUrl(GeminiApi.BASE_URL)
            .client(okHttpClient)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(GeminiApi::class.java)
    }

    private val openRouterApi: OpenRouterApi by lazy {
        Retrofit.Builder()
            .baseUrl(OpenRouterApi.BASE_URL)
            .client(okHttpClient)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(OpenRouterApi::class.java)
    }

    suspend fun sendMessage(history: List<ChatTurn>): ModelResult {
        val geminiKey = settingsRepository.currentGeminiKey()
        val llamaKey = settingsRepository.currentOpenRouterLlamaKey()
        val qwenCoderKey = settingsRepository.currentOpenRouterQwenCoderKey()
        val gptOssKey = settingsRepository.currentOpenRouterGptOssKey()

        val anyOpenRouterKey =
            !llamaKey.isNullOrBlank() || !qwenCoderKey.isNullOrBlank() || !gptOssKey.isNullOrBlank()

        if (geminiKey.isNullOrBlank() && !anyOpenRouterKey) {
            throw ModelRouterException(
                "No API key configured. Add one in Settings.",
                isRateLimit = false
            )
        }

        if (!geminiKey.isNullOrBlank()) {
            callGeminiWithRetry(geminiKey, history)?.let { return it }
            // Gemini exhausted its one retry (or failed non-transiently) — fall
            // through to OpenRouter below.
        }

        // Fixed hand-picked order, one attempt each, own key per model.
        val openRouterAttempts = listOf(
            llamaKey to OpenRouterApi.LLAMA_3_1_405B,
            qwenCoderKey to OpenRouterApi.QWEN3_CODER,
            gptOssKey to OpenRouterApi.GPT_OSS_120B
        )

        for ((key, model) in openRouterAttempts) {
            if (key.isNullOrBlank()) continue
            try {
                return callOpenRouter(key, model, history)
            } catch (e: Exception) {
                // try next hand-picked model
            }
        }

        // Last safety net: a specific free slug may have been retired (see
        // OpenRouterApi companion doc). Try the auto-router once with whichever
        // key is available before giving up entirely.
        val fallbackKey = llamaKey ?: qwenCoderKey ?: gptOssKey
        if (!fallbackKey.isNullOrBlank()) {
            try {
                return callOpenRouter(fallbackKey, OpenRouterApi.AUTO_ROUTER, history)
            } catch (e: Exception) {
                // fall through to final failure below
            }
        }

        throw ModelRouterException("All configured providers failed.", isRateLimit = false)
    }

    /**
     * Returns a result on success, or null once Gemini has confirmed-failed (either
     * a non-transient error, or a transient one that also failed on the single retry).
     */
    private suspend fun callGeminiWithRetry(apiKey: String, history: List<ChatTurn>): ModelResult? {
        repeat(2) { attempt ->
            try {
                return callGemini(apiKey, history)
            } catch (e: HttpException) {
                val transient = e.code() == 429 || e.code() in 500..599
                if (attempt == 0 && transient) {
                    delay(1500)
                } else {
                    return null
                }
            } catch (e: Exception) {
                return null // non-HTTP failure (e.g. network) — don't retry, fall through
            }
        }
        return null
    }

    private suspend fun callGemini(apiKey: String, history: List<ChatTurn>): ModelResult {
        val request = GeminiRequest(
            contents = history.map {
                GeminiContent(
                    role = if (it.role == "assistant") "model" else "user",
                    parts = listOf(GeminiPart(text = it.content))
                )
            }
        )
        val response = geminiApi.generateContent(
            model = GeminiApi.DEFAULT_MODEL,
            apiKey = apiKey,
            request = request
        )
        val text = response.candidates
            ?.firstOrNull()
            ?.content
            ?.parts
            ?.joinToString("") { it.text }
            .orEmpty()

        return ModelResult(text = text, providerUsed = "gemini")
    }

    private suspend fun callOpenRouter(apiKey: String, model: String, history: List<ChatTurn>): ModelResult {
        val response = openRouterApi.chatCompletion(
            bearerToken = "Bearer $apiKey",
            request = OpenRouterRequest(
                model = model,
                messages = history.map { OpenRouterMessage(role = it.role, content = it.content) }
            )
        )
        val text = response.choices?.firstOrNull()?.message?.content.orEmpty()
        return ModelResult(text = text, providerUsed = "openrouter:$model")
    }
}

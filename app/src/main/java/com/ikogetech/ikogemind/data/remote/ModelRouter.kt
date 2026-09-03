package com.ikogetech.ikogemind.data.remote

import com.ikogetech.ikogemind.data.repository.SettingsRepository
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.HttpException
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

/** A single turn of chat history, provider-agnostic. */
data class ChatTurn(val role: String, val content: String)

data class ModelResult(
    val text: String,
    val providerUsed: String // e.g. "gemini" or "openrouter:meta-llama/llama-3.1-8b-instruct:free"
)

class ModelRouterException(message: String, val isRateLimit: Boolean) : Exception(message)

/**
 * Routing logic per model-routing.md v1:
 * 1. Default to Gemini free tier.
 * 2. If Gemini quota/rate limit is hit (HTTP 429, or 5xx as a broader "unavailable"
 *    signal), fall back to the first working OpenRouter free model.
 * 3. Caller (ModelStep) is responsible for persisting which provider actually served
 *    the response.
 *
 * Deliberately NOT task-based routing yet — see model-routing.md "Not in v1".
 * Adding a third provider later should only mean adding another branch here; nothing
 * upstream (pipeline, ViewModel, UI) needs to know provider details.
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
        val openRouterKey = settingsRepository.currentOpenRouterKey()

        if (geminiKey.isNullOrBlank() && openRouterKey.isNullOrBlank()) {
            throw ModelRouterException(
                "No API key configured. Add one in Settings.",
                isRateLimit = false
            )
        }

        if (!geminiKey.isNullOrBlank()) {
            try {
                return callGemini(geminiKey, history)
            } catch (e: HttpException) {
                val rateLimited = e.code() == 429 || e.code() in 500..599
                if (!rateLimited || openRouterKey.isNullOrBlank()) {
                    throw ModelRouterException(
                        "Gemini call failed (${e.code()}) and no fallback available.",
                        isRateLimit = rateLimited
                    )
                }
                // fall through to OpenRouter below
            }
        }

        if (!openRouterKey.isNullOrBlank()) {
            return callOpenRouter(openRouterKey, history)
        }

        throw ModelRouterException("All configured providers failed.", isRateLimit = false)
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

    private suspend fun callOpenRouter(apiKey: String, history: List<ChatTurn>): ModelResult {
        var lastError: Exception? = null

        for (model in OpenRouterApi.FREE_MODEL_FALLBACK_ORDER) {
            try {
                val response = openRouterApi.chatCompletion(
                    bearerToken = "Bearer $apiKey",
                    request = OpenRouterRequest(
                        model = model,
                        messages = history.map { OpenRouterMessage(role = it.role, content = it.content) }
                    )
                )
                val text = response.choices?.firstOrNull()?.message?.content.orEmpty()
                return ModelResult(text = text, providerUsed = "openrouter:$model")
            } catch (e: Exception) {
                lastError = e
                // try next free model in the fallback order
            }
        }

        throw ModelRouterException(
            "All OpenRouter free models failed: ${lastError?.message}",
            isRateLimit = false
        )
    }
}

package com.ikogetech.ikogemind.data.remote

import retrofit2.http.Body
import retrofit2.http.Header
import retrofit2.http.POST

/**
 * OpenRouter exposes an OpenAI-compatible /chat/completions endpoint, which is why
 * this DTO shape looks different from Gemini's. Non-streaming for v1, same reasoning
 * as GeminiApi.
 */
interface OpenRouterApi {
    @POST("api/v1/chat/completions")
    suspend fun chatCompletion(
        @Header("Authorization") bearerToken: String, // "Bearer <key>"
        @Body request: OpenRouterRequest
    ): OpenRouterResponse

    companion object {
        const val BASE_URL = "https://openrouter.ai/"
        // OpenRouter's own auto-router for free models — it picks from whatever
        // free models are currently live on their end, so we're not stuck
        // maintaining a hardcoded list of model slugs that silently go stale
        // (which is exactly what happened here: the original hardcoded list
        // returned 404s once those specific free slugs were retired).
        val FREE_MODEL_FALLBACK_ORDER = listOf(
            "openrouter/free"
        )
    }
}

data class OpenRouterRequest(
    val model: String,
    val messages: List<OpenRouterMessage>
)

data class OpenRouterMessage(
    val role: String, // "user" | "assistant" | "system"
    val content: String
)

data class OpenRouterResponse(
    val choices: List<OpenRouterChoice>?
)

data class OpenRouterChoice(
    val message: OpenRouterMessage?
)

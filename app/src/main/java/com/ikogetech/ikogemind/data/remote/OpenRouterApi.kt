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
        // ASSUMPTION: fill in the actual free-tier model slugs to enable at launch —
        // see model-routing.md TODO "Specific OpenRouter free models to enable".
        val FREE_MODEL_FALLBACK_ORDER = listOf(
            "meta-llama/llama-3.1-8b-instruct:free",
            "mistralai/mistral-7b-instruct:free",
            "qwen/qwen-2-7b-instruct:free"
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

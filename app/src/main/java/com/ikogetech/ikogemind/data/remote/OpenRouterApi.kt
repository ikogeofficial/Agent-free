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

        // Hand-picked fallback order (decisions-log.md), each behind its own
        // OpenRouter API key so a single account's rate limit doesn't gate all
        // three. Slugs verified directly against openrouter.ai model pages,
        // Sept 2026 — but free slugs on OpenRouter rotate out with little notice
        // (this has bitten this codebase before), so ModelRouter falls back to
        // AUTO_ROUTER below if all three fail.
        const val LLAMA_3_1_405B = "meta-llama/llama-3.1-405b-instruct:free"
        const val QWEN3_CODER = "qwen/qwen3-coder:free"
        const val GPT_OSS_120B = "openai/gpt-oss-120b:free"

        // Safety net only — OpenRouter's own auto-router, picks whatever free
        // model is currently live. Tried once, only after all three hand-picked
        // models above have failed.
        const val AUTO_ROUTER = "openrouter/free"
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

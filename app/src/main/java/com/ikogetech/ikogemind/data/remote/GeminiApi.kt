package com.ikogetech.ikogemind.data.remote

import retrofit2.http.Body
import retrofit2.http.POST
import retrofit2.http.Path
import retrofit2.http.Query

/**
 * Minimal client for the Gemini "generateContent" REST endpoint (AI Studio free-tier
 * API key, no billing account required at time of writing). Non-streaming for v1 —
 * ModelStep below fakes a stream by emitting the full text as one chunk. Swap for
 * the SSE "streamGenerateContent" endpoint later without touching callers, since
 * they only depend on ModelStep's Flow<String> contract.
 */
interface GeminiApi {
    @POST("v1beta/models/{model}:generateContent")
    suspend fun generateContent(
        @Path("model") model: String,
        @Query("key") apiKey: String,
        @Body request: GeminiRequest
    ): GeminiResponse

    companion object {
        const val BASE_URL = "https://generativelanguage.googleapis.com/"
        // gemini-1.5-flash was retired; current free-tier default as of Sept 2026
        // is gemini-3.8-flash. Google ships new model generations frequently —
        // if this starts 404ing again, check https://ai.google.dev/gemini-api/docs/models
        // for the current model id before assuming something else broke.
        const val DEFAULT_MODEL = "gemini-3.8-flash"
    }
}

data class GeminiRequest(
    val contents: List<GeminiContent>
)

data class GeminiContent(
    val role: String, // "user" | "model"
    val parts: List<GeminiPart>
)

data class GeminiPart(
    val text: String
)

data class GeminiResponse(
    val candidates: List<GeminiCandidate>?
)

data class GeminiCandidate(
    val content: GeminiContent?
)

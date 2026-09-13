package com.ikogetech.ikogemind.pipeline

import com.ikogetech.ikogemind.data.remote.ChatTurn
import com.ikogetech.ikogemind.data.remote.ModelRouter
import com.ikogetech.ikogemind.data.remote.ModelRouterException

/**
 * Tells the model what it's actually running inside. Without this, free-tier
 * models default to generic web-dev assumptions (React/JS clipboard snippets,
 * <button onClick>, browser APIs) for anything code-related, which is wrong here:
 * IkogeMind is a native Android/Kotlin/Jetpack Compose app, not a website. This
 * was confirmed live in-app (model answered a "how do I copy your response"
 * question with navigator.clipboard/React code instead of anything Android-native).
 */
private const val SYSTEM_PROMPT = """
You are the AI assistant embedded inside IkogeMind, a native Android app built
with Kotlin and Jetpack Compose — not a website or web app. The person talking
to you is using this app on their Android phone right now.

Rules:
- If asked for code or "how do I do X" involving this app's own behavior
  (copying text, sharing, notifications, buttons, navigation, etc.), answer in
  terms of native Android/Kotlin/Jetpack Compose APIs only. Never suggest
  JavaScript, HTML, React, or browser APIs (no navigator.clipboard, no
  <button onClick>, no useState) unless the person is explicitly asking about
  web development for something unrelated to this app.
- Message actions like copy, share, read-aloud, and regenerate are already
  built into this app's UI directly — you don't need to explain how to add
  them unless the person specifically asks about this app's own source code.
- For general questions unrelated to Android or this app, answer normally.
"""

class ModelStep(private val modelRouter: ModelRouter) : PipelineStep {

    override suspend fun run(context: PipelineContext): PipelineContext {
        val turns = listOf(ChatTurn(role = "system", content = SYSTEM_PROMPT.trim())) +
            context.history.map { ChatTurn(role = it.role, content = it.content) } +
            ChatTurn(role = "user", content = context.userMessage)

        return try {
            val result = modelRouter.sendMessage(turns)
            context.copy(
                rawModelOutput = result.text,
                providerUsed = result.providerUsed
            )
        } catch (e: ModelRouterException) {
            context.copy(error = PipelineError(message = e.message ?: "Model call failed", isRateLimit = e.isRateLimit))
        } catch (e: Exception) {
            context.copy(error = PipelineError(message = "Unexpected error: ${e.message}"))
        }
    }
}

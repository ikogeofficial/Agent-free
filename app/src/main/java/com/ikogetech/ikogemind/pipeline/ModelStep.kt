package com.ikogetech.ikogemind.pipeline

import com.ikogetech.ikogemind.data.remote.ChatTurn
import com.ikogetech.ikogemind.data.remote.ModelRouter
import com.ikogetech.ikogemind.data.remote.ModelRouterException

class ModelStep(private val modelRouter: ModelRouter) : PipelineStep {

    override suspend fun run(context: PipelineContext): PipelineContext {
        val turns = context.history.map { ChatTurn(role = it.role, content = it.content) } +
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

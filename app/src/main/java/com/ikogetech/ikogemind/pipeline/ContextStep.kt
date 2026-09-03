package com.ikogetech.ikogemind.pipeline

import com.ikogetech.ikogemind.data.local.MessageDao

/**
 * ASSUMPTION: last 20 messages is the context window sent to the model. No token
 * counting yet — free-tier models here have generous-enough context windows that
 * a flat message-count cap is fine for v1. Revisit if a provider's free tier turns
 * out to have a tight token limit (see model-routing.md TODO on rate limits).
 */
class ContextStep(
    private val messageDao: MessageDao,
    private val maxHistoryMessages: Int = 20
) : PipelineStep {

    override suspend fun run(context: PipelineContext): PipelineContext {
        val recent = messageDao.getRecent(context.conversationId, maxHistoryMessages)
            .reversed() // DAO returns newest-first; pipeline wants oldest-first
            .map { HistoryTurn(role = it.role, content = it.content) }

        return context.copy(history = recent)
    }
}

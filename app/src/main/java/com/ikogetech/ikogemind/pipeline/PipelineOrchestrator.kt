package com.ikogetech.ikogemind.pipeline

/**
 * Runs the configured steps in order. Deliberately dumb — no branching/parallelism
 * logic here. If a step sets context.error, remaining steps are skipped, so a failed
 * ModelStep doesn't waste time in PostProcess/FormatOutput.
 */
class PipelineOrchestrator(private val steps: List<PipelineStep>) {

    suspend fun run(conversationId: String, userMessage: String): PipelineContext {
        var context = PipelineContext(
            conversationId = conversationId,
            userMessage = userMessage
        )

        for (step in steps) {
            if (context.error != null) break
            context = step.run(context)
        }

        return context
    }
}

package com.ikogetech.ikogemind.pipeline

/**
 * v1: trims whitespace and guards against an empty/blank reply (some free models
 * occasionally return an empty candidate). No content filtering yet — nothing in
 * scope requires it for a personal-use v1, but this is the seam where it would go
 * (e.g. before opening the app up to the wider Ikogetech audience).
 */
class PostProcessStep : PipelineStep {

    override suspend fun run(context: PipelineContext): PipelineContext {
        val raw = context.rawModelOutput

        if (raw.isNullOrBlank()) {
            return context.copy(
                error = PipelineError(message = "Model returned an empty response. Try again.")
            )
        }

        return context.copy(cleanedOutput = raw.trim())
    }
}

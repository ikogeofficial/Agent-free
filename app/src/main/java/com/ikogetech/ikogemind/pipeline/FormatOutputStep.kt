package com.ikogetech.ikogemind.pipeline

/**
 * v1: pass-through (Compose Text renders plain strings fine; no markdown renderer
 * wired up yet). This step exists as a seam so adding markdown/code-block rendering
 * later is a change here only, not in ChatViewModel or ChatScreen.
 */
class FormatOutputStep : PipelineStep {

    override suspend fun run(context: PipelineContext): PipelineContext {
        return context.copy(formattedOutput = context.cleanedOutput)
    }
}

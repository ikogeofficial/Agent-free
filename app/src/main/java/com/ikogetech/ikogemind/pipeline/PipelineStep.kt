package com.ikogetech.ikogemind.pipeline

/**
 * Mutable-by-replacement context passed down the pipeline. Each step reads what it
 * needs and returns a new copy with its contribution added — steps never reach
 * outside this object (no direct DB/network calls from steps other than what their
 * job requires), which is what keeps them independently swappable per
 * architecture.md.
 */
data class PipelineContext(
    val conversationId: String,
    val userMessage: String,
    // Populated by ContextStep: prior turns, oldest first, NOT including userMessage.
    val history: List<HistoryTurn> = emptyList(),
    // Populated by ModelStep.
    val rawModelOutput: String? = null,
    val providerUsed: String? = null,
    // Populated by PostProcessStep.
    val cleanedOutput: String? = null,
    // Populated by FormatOutputStep.
    val formattedOutput: String? = null,
    // Any step can set this to short-circuit remaining steps.
    val error: PipelineError? = null
)

data class HistoryTurn(val role: String, val content: String)

data class PipelineError(val message: String, val isRateLimit: Boolean = false)

/**
 * One stage of the pipeline. Implementations should do exactly one job (retrieve
 * context, call model, clean output, format for UI) so any single stage can later
 * be swapped for a chain of agent-style steps without touching the others.
 */
interface PipelineStep {
    suspend fun run(context: PipelineContext): PipelineContext
}

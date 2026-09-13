package com.ikogetech.ikogemind.ui.chat

import android.content.Intent
import android.speech.tts.TextToSpeech
import android.widget.Toast
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Send
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.ThumbUp
import androidx.compose.material3.AssistChip
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.ikogetech.ikogemind.data.local.MessageEntity
import com.ikogetech.ikogemind.data.repository.ChatRepository
import com.ikogetech.ikogemind.pipeline.PipelineOrchestrator
import com.ikogetech.ikogemind.ui.ViewModelFactories

// Quick-action starter prompts for the empty ("New Chat") state — audience-specific
// per brand-notes.md (coding / AI / cybersecurity), not generic Copilot-style chips
// like "Write a first draft". Tapping fills the input rather than auto-sending, so
// the person can edit before committing.
private val QUICK_ACTIONS = listOf(
    "Explain a concept" to "Can you explain ",
    "Debug an error" to "Here's an error I'm getting: ",
    "Review my code" to "Can you review this code: "
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChatScreen(
    conversationId: String,
    chatRepository: ChatRepository,
    pipelineOrchestrator: PipelineOrchestrator,
    onBack: () -> Unit,
    onConversationIdAssigned: (String) -> Unit
) {
    val viewModel: ChatViewModel = viewModel(
        factory = ViewModelFactories.chat(chatRepository, pipelineOrchestrator, conversationId)
    )

    val messages by viewModel.messages.collectAsState()
    val uiState by viewModel.uiState.collectAsState()
    val activeConversationId by viewModel.activeConversationId.collectAsState()

    // Once a "new chat" gets a real id on first send, tell the nav host so back-stack
    // / conversation list stay in sync (screens-and-flows.md New Chat -> Chat Screen).
    LaunchedEffect(activeConversationId) {
        activeConversationId?.let { onConversationIdAssigned(it) }
    }

    var inputText by remember { mutableStateOf("") }
    val listState = rememberLazyListState()
    val context = LocalContext.current

    LaunchedEffect(messages.size) {
        if (messages.isNotEmpty()) listState.animateScrollToItem(messages.size - 1)
    }

    // Single shared TextToSpeech engine for the whole screen — one per message
    // bubble would spin up a new engine per row, which is wasteful and can talk
    // over itself. Read-aloud on any bubble routes through this same instance.
    val tts = remember {
        TextToSpeech(context.applicationContext, null)
    }
    DisposableEffect(Unit) {
        onDispose {
            tts.stop()
            tts.shutdown()
        }
    }

    // Drives the model-status chip: the provider that actually served the most
    // recent assistant reply, or the v1 default (Gemini, per model-routing.md)
    // before any reply has come back yet. Not a picker — v1 is global-default-only
    // (see decisions log), so this is status, not a control.
    val currentProviderLabel = remember(messages) {
        friendlyProviderLabel(messages.lastOrNull { it.role == "assistant" && it.providerUsed != null }?.providerUsed)
    }

    val isBusy = uiState is ChatUiState.Waiting

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Chat") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            if (messages.isEmpty()) {
                // Chat screen "Empty" state — doubles as the "New Chat" screen from
                // screens-and-flows.md; there's no separate NewChatScreen file.
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .weight(1f)
                        .padding(24.dp),
                    verticalArrangement = Arrangement.Center
                ) {
                    Text(
                        "What can I help you with?",
                        style = MaterialTheme.typography.titleLarge
                    )
                    Text(
                        "Ask about code, security, or anything else.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(top = 4.dp, bottom = 16.dp)
                    )
                    LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        items(QUICK_ACTIONS) { (label, starter) ->
                            AssistChip(
                                onClick = { inputText = starter },
                                label = { Text(label) }
                            )
                        }
                    }
                }
            } else {
                LazyColumn(
                    state = listState,
                    modifier = Modifier
                        .fillMaxSize()
                        .weight(1f)
                        .padding(horizontal = 12.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(messages, key = { it.id }) { message ->
                        MessageBubble(
                            message = message,
                            isBusy = isBusy,
                            onReadAloud = { text ->
                                tts.speak(text, TextToSpeech.QUEUE_FLUSH, null, message.id)
                            },
                            onShare = { text ->
                                val shareIntent = Intent(Intent.ACTION_SEND).apply {
                                    type = "text/plain"
                                    putExtra(Intent.EXTRA_TEXT, text)
                                }
                                context.startActivity(Intent.createChooser(shareIntent, null))
                            },
                            onFeedback = { feedback -> viewModel.setFeedback(message.id, feedback) },
                            onRegenerate = { viewModel.regenerate(message.id) }
                        )
                    }
                }
            }

            // "Streaming" state (ModelStep is non-streaming in v1, see ChatUiState note)
            if (uiState is ChatUiState.Waiting) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(8.dp),
                    horizontalArrangement = Arrangement.Center
                ) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(20.dp),
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            }

            // "Error" / "Rate-limited" states — note which provider failed, per
            // screens-and-flows.md ("show retry, note which provider failed")
            val state = uiState
            if (state is ChatUiState.Error) {
                Text(
                    text = if (state.isRateLimit)
                        "Rate limit hit on the current provider. ${state.message}"
                    else
                        "Something went wrong: ${state.message}",
                    color = MaterialTheme.colorScheme.error,
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp),
                    style = MaterialTheme.typography.bodySmall
                )
            }

            // Model status chip — one quiet indicator, not a dropdown/selector.
            Row(
                modifier = Modifier.padding(start = 16.dp, top = 4.dp),
                horizontalArrangement = Arrangement.Start
            ) {
                ModelStatusChip(currentProviderLabel)
            }

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                OutlinedTextField(
                    value = inputText,
                    onValueChange = { inputText = it },
                    modifier = Modifier.weight(1f),
                    placeholder = { Text("Message") }
                )
                IconButton(
                    onClick = {
                        val text = inputText
                        inputText = ""
                        viewModel.sendMessage(text)
                    }
                ) {
                    Icon(
                        Icons.Filled.Send,
                        contentDescription = "Send",
                        tint = MaterialTheme.colorScheme.primary
                    )
                }
            }
        }
    }
}

@Composable
private fun ModelStatusChip(label: String) {
    Surface(
        color = MaterialTheme.colorScheme.surfaceVariant,
        contentColor = MaterialTheme.colorScheme.onSurfaceVariant,
        shape = RoundedCornerShape(percent = 50)
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(6.dp)
                    .background(MaterialTheme.colorScheme.primary, CircleShape)
            )
            Text(label, style = MaterialTheme.typography.labelSmall)
        }
    }
}

/**
 * Maps a raw providerUsed string (set by ModelRouter, e.g. "gemini" or
 * "openrouter:<model-id>") to a short label for the status chip. Handles both the
 * current "openrouter/free" auto-router id and the specific hand-picked model slugs
 * (Llama 3.1 405B / Qwen3 Coder / gpt-oss-120b) from the fallback-ordering decision,
 * so this doesn't need another edit once ModelRouter is updated to match that
 * decision (see note left for that follow-up).
 */
private fun friendlyProviderLabel(raw: String?): String {
    if (raw == null || raw == "gemini") return "Gemini"
    val model = raw.removePrefix("openrouter:")
    return when {
        model.contains("llama-3.1-405b", ignoreCase = true) -> "Llama 3.1 405B"
        model.contains("qwen3-coder", ignoreCase = true) -> "Qwen3 Coder"
        model.contains("gpt-oss-120b", ignoreCase = true) -> "gpt-oss-120b"
        else -> "OpenRouter"
    }
}

// Long-press any message bubble to copy its raw text — added so users (and the
// model itself, when asked) don't need to hand-roll copy logic per platform; the
// model was suggesting navigator.clipboard/React snippets that don't apply to a
// native Android app (now also fixed at the source via ModelStep's system prompt).
// Uses Compose's LocalClipboardManager rather than the raw ClipboardManager system
// service — no Context boilerplate needed for plain text.
//
// Assistant replies additionally get a full action toolbar (copy, share,
// read-aloud, thumbs up/down, regenerate) below the bubble — user's own messages
// keep just the long-press copy since regenerate/feedback don't apply to them.
@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun MessageBubble(
    message: MessageEntity,
    isBusy: Boolean,
    onReadAloud: (String) -> Unit,
    onShare: (String) -> Unit,
    onFeedback: (String?) -> Unit,
    onRegenerate: () -> Unit
) {
    val isUser = message.role == "user"
    val clipboardManager = LocalClipboardManager.current
    val context = LocalContext.current

    fun copyToClipboard() {
        clipboardManager.setText(AnnotatedString(message.content))
        Toast.makeText(context, "Copied to clipboard", Toast.LENGTH_SHORT).show()
    }

    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = if (isUser) Alignment.End else Alignment.Start
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = if (isUser) Arrangement.End else Arrangement.Start
        ) {
            Card(
                modifier = Modifier
                    .width(260.dp)
                    .combinedClickable(
                        onClick = {},
                        onLongClick = { copyToClipboard() }
                    ),
                colors = when {
                    message.isError -> CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.errorContainer)
                    isUser -> CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
                    else -> CardDefaults.cardColors()
                }
            ) {
                Column(modifier = Modifier.padding(10.dp)) {
                    Text(message.content, style = MaterialTheme.typography.bodyMedium)
                    if (!isUser && message.providerUsed != null) {
                        Text(
                            friendlyProviderLabel(message.providerUsed),
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        }

        if (!isUser && !message.isError) {
            MessageActionsRow(
                feedback = message.feedback,
                regenerateEnabled = !isBusy,
                onCopy = { copyToClipboard() },
                onShare = { onShare(message.content) },
                onReadAloud = { onReadAloud(message.content) },
                onFeedback = onFeedback,
                onRegenerate = onRegenerate
            )
        }
    }
}

/**
 * The assistant-message action row from the reference design: copy, share,
 * read-aloud, thumbs up, thumbs down, regenerate. Every icon except thumbs-down
 * comes from material-icons-core (already a dependency, confirmed against the
 * material-icons-core bug that broke the build earlier) — ThumbDown isn't in core,
 * so it's rendered as a 180°-rotated ThumbUp instead of pulling in the much larger
 * material-icons-extended for one icon.
 */
@Composable
private fun MessageActionsRow(
    feedback: String?,
    regenerateEnabled: Boolean,
    onCopy: () -> Unit,
    onShare: () -> Unit,
    onReadAloud: () -> Unit,
    onFeedback: (String?) -> Unit,
    onRegenerate: () -> Unit
) {
    val iconSize = 16.dp
    val buttonSize = 32.dp

    Row(
        modifier = Modifier.padding(top = 2.dp),
        horizontalArrangement = Arrangement.spacedBy(0.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        IconButton(onClick = onCopy, modifier = Modifier.size(buttonSize)) {
            Icon(Icons.Filled.ContentCopy, contentDescription = "Copy", modifier = Modifier.size(iconSize))
        }
        IconButton(onClick = onShare, modifier = Modifier.size(buttonSize)) {
            Icon(Icons.Filled.Share, contentDescription = "Share", modifier = Modifier.size(iconSize))
        }
        IconButton(onClick = onReadAloud, modifier = Modifier.size(buttonSize)) {
            Icon(Icons.Filled.PlayArrow, contentDescription = "Read aloud", modifier = Modifier.size(iconSize))
        }
        IconButton(
            onClick = { onFeedback(if (feedback == "up") null else "up") },
            modifier = Modifier.size(buttonSize)
        ) {
            Icon(
                Icons.Filled.ThumbUp,
                contentDescription = "Good response",
                modifier = Modifier.size(iconSize),
                tint = if (feedback == "up") MaterialTheme.colorScheme.primary else LocalContentColor.current
            )
        }
        IconButton(
            onClick = { onFeedback(if (feedback == "down") null else "down") },
            modifier = Modifier.size(buttonSize)
        ) {
            Icon(
                Icons.Filled.ThumbUp,
                contentDescription = "Bad response",
                modifier = Modifier
                    .size(iconSize)
                    .graphicsLayer { rotationZ = 180f },
                tint = if (feedback == "down") MaterialTheme.colorScheme.error else LocalContentColor.current
            )
        }
        IconButton(onClick = onRegenerate, enabled = regenerateEnabled, modifier = Modifier.size(buttonSize)) {
            Icon(Icons.Filled.Refresh, contentDescription = "Regenerate", modifier = Modifier.size(iconSize))
        }
    }
}

package com.ikogetech.ikogemind.ui.chat

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
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Send
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.ikogetech.ikogemind.data.local.MessageEntity
import com.ikogetech.ikogemind.data.repository.ChatRepository
import com.ikogetech.ikogemind.pipeline.PipelineOrchestrator
import com.ikogetech.ikogemind.ui.ViewModelFactories

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

    LaunchedEffect(messages.size) {
        if (messages.isNotEmpty()) listState.animateScrollToItem(messages.size - 1)
    }

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
                // Chat screen "Empty" state
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .weight(1f),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        "Send a message to start chatting.",
                        style = MaterialTheme.typography.bodyMedium
                    )
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
                        MessageBubble(message)
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
                    CircularProgressIndicator(modifier = Modifier.size(20.dp))
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
                    Icon(Icons.Filled.Send, contentDescription = "Send")
                }
            }
        }
    }
}

@Composable
private fun MessageBubble(message: MessageEntity) {
    val isUser = message.role == "user"
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = if (isUser) Arrangement.End else Arrangement.Start
    ) {
        Card(
            modifier = Modifier.width(260.dp),
            colors = if (message.isError)
                CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.errorContainer)
            else
                CardDefaults.cardColors()
        ) {
            Column(modifier = Modifier.padding(10.dp)) {
                Text(message.content, style = MaterialTheme.typography.bodyMedium)
                if (!isUser && message.providerUsed != null) {
                    Text(
                        message.providerUsed,
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}

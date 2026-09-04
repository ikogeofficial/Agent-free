package com.ikogetech.ikogemind.ui.conversationlist

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.ikogetech.ikogemind.data.repository.ChatRepository
import com.ikogetech.ikogemind.ui.ViewModelFactories

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ConversationListScreen(
    chatRepository: ChatRepository,
    onOpenConversation: (String) -> Unit,
    onNewChat: () -> Unit,
    onOpenSettings: () -> Unit
) {
    val viewModel: ConversationListViewModel = viewModel(
        factory = ViewModelFactories.conversationList(chatRepository)
    )
    val conversations by viewModel.conversations.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("IkogeMind") },
                actions = {
                    IconButton(onClick = onOpenSettings) {
                        Icon(Icons.Filled.Settings, contentDescription = "Settings")
                    }
                }
            )
        },
        floatingActionButton = {
            FloatingActionButton(onClick = onNewChat) {
                Icon(Icons.Filled.Add, contentDescription = "New chat")
            }
        }
    ) { padding ->
        if (conversations.isEmpty()) {
            // Conversation list "Empty" state per screens-and-flows.md
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding),
                contentAlignment = Alignment.Center
            ) {
                Column {
                    Text(
                        "No chats yet",
                        style = MaterialTheme.typography.titleMedium
                    )
                    Text(
                        "Tap + to start one.",
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
            }
        } else {
            LazyColumn(modifier = Modifier.padding(padding)) {
                items(conversations, key = { it.id }) { conversation ->
                    ListItem(
                        headlineContent = { Text(conversation.title) },
                        supportingContent = {
                            if (conversation.lastMessagePreview.isNotBlank()) {
                                Text(conversation.lastMessagePreview)
                            }
                        },
                        modifier = Modifier.clickable { onOpenConversation(conversation.id) }
                    )
                }
            }
        }
    }
}

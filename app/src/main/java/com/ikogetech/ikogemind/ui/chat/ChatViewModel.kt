package com.ikogetech.ikogemind.ui.chat

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ikogetech.ikogemind.data.local.MessageEntity
import com.ikogetech.ikogemind.data.repository.ChatRepository
import com.ikogetech.ikogemind.pipeline.PipelineOrchestrator
import com.ikogetech.ikogemind.ui.navigation.Routes
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

/**
 * Matches screens-and-flows.md "Chat screen" states: Empty, Streaming, Error,
 * Rate-limited. v1's ModelStep is non-streaming (see GeminiApi/OpenRouterApi notes),
 * so "Streaming" here means "waiting on the pipeline", not token-by-token yet.
 */
sealed interface ChatUiState {
    data object Empty : ChatUiState
    data object Waiting : ChatUiState
    data object Idle : ChatUiState
    data class Error(val message: String, val isRateLimit: Boolean) : ChatUiState
}

class ChatViewModel(
    private val chatRepository: ChatRepository,
    private val pipelineOrchestrator: PipelineOrchestrator,
    initialConversationId: String
) : ViewModel() {

    private var conversationId: String? =
        initialConversationId.takeIf { it != Routes.Chat.NEW_CHAT_ID }

    private val _uiState = MutableStateFlow<ChatUiState>(
        if (conversationId == null) ChatUiState.Empty else ChatUiState.Idle
    )
    val uiState: StateFlow<ChatUiState> = _uiState.asStateFlow()

    /** Chat screen reads this to know whether it should re-point navigation at a real id. */
    private val _activeConversationId = MutableStateFlow(conversationId)
    val activeConversationId: StateFlow<String?> = _activeConversationId.asStateFlow()

    // Follows _activeConversationId rather than capturing conversationId once at init,
    // so messages actually appear once a "new chat" gets its real id on first send.
    val messages: StateFlow<List<MessageEntity>> =
        _activeConversationId
            .flatMapLatest { id -> id?.let { chatRepository.observeMessages(it) } ?: flowOf(emptyList()) }
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun sendMessage(text: String) {
        if (text.isBlank()) return

        viewModelScope.launch {
            _uiState.value = ChatUiState.Waiting

            val convId = conversationId ?: chatRepository.createConversation(text).id.also {
                conversationId = it
                _activeConversationId.value = it
            }

            chatRepository.addMessage(convId, role = "user", content = text)

            val result = pipelineOrchestrator.run(convId, text)

            if (result.error != null) {
                chatRepository.addMessage(
                    convId,
                    role = "assistant",
                    content = result.error.message,
                    isError = true
                )
                _uiState.value = ChatUiState.Error(
                    message = result.error.message,
                    isRateLimit = result.error.isRateLimit
                )
            } else {
                chatRepository.addMessage(
                    convId,
                    role = "assistant",
                    content = result.formattedOutput ?: result.rawModelOutput.orEmpty(),
                    providerUsed = result.providerUsed
                )
                _uiState.value = ChatUiState.Idle
            }
        }
    }
}

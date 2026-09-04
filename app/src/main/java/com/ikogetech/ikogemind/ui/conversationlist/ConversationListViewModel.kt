package com.ikogetech.ikogemind.ui.conversationlist

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ikogetech.ikogemind.data.local.ConversationEntity
import com.ikogetech.ikogemind.data.repository.ChatRepository
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class ConversationListViewModel(
    private val chatRepository: ChatRepository
) : ViewModel() {

    val conversations: StateFlow<List<ConversationEntity>> =
        chatRepository.observeConversations()
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun deleteConversation(id: String) {
        viewModelScope.launch { chatRepository.deleteConversation(id) }
    }
}

package com.ikogetech.ikogemind.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewmodel.CreationExtras
import com.ikogetech.ikogemind.data.repository.ChatRepository
import com.ikogetech.ikogemind.data.repository.SettingsRepository
import com.ikogetech.ikogemind.pipeline.PipelineOrchestrator
import com.ikogetech.ikogemind.ui.chat.ChatViewModel
import com.ikogetech.ikogemind.ui.conversationlist.ConversationListViewModel
import com.ikogetech.ikogemind.ui.settings.SettingsViewModel

/**
 * No Hilt for v1 (see IkogeMindApp comment) — these small factories are the price of
 * that choice. If the app grows past a handful of ViewModels, switching to Hilt is a
 * localized change: only these factories and the Application class need to move.
 */
object ViewModelFactories {

    fun chat(
        chatRepository: ChatRepository,
        pipelineOrchestrator: PipelineOrchestrator,
        conversationId: String
    ) = object : ViewModelProvider.Factory {
        override fun <T : ViewModel> create(modelClass: Class<T>, extras: CreationExtras): T {
            @Suppress("UNCHECKED_CAST")
            return ChatViewModel(chatRepository, pipelineOrchestrator, conversationId) as T
        }
    }

    fun conversationList(chatRepository: ChatRepository) = object : ViewModelProvider.Factory {
        override fun <T : ViewModel> create(modelClass: Class<T>, extras: CreationExtras): T {
            @Suppress("UNCHECKED_CAST")
            return ConversationListViewModel(chatRepository) as T
        }
    }

    fun settings(
        settingsRepository: SettingsRepository,
        chatRepository: ChatRepository
    ) = object : ViewModelProvider.Factory {
        override fun <T : ViewModel> create(modelClass: Class<T>, extras: CreationExtras): T {
            @Suppress("UNCHECKED_CAST")
            return SettingsViewModel(settingsRepository, chatRepository) as T
        }
    }
}

package com.ikogetech.ikogemind.ui.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ikogetech.ikogemind.data.repository.ChatRepository
import com.ikogetech.ikogemind.data.repository.SettingsRepository
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class SettingsViewModel(
    private val settingsRepository: SettingsRepository,
    private val chatRepository: ChatRepository
) : ViewModel() {

    val geminiApiKey: StateFlow<String?> =
        settingsRepository.geminiApiKey.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    val openRouterApiKey: StateFlow<String?> =
        settingsRepository.openRouterApiKey.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    val preferredProvider: StateFlow<String> =
        settingsRepository.preferredProvider.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), "auto")

    fun saveGeminiKey(key: String) {
        viewModelScope.launch { settingsRepository.setGeminiApiKey(key) }
    }

    fun saveOpenRouterKey(key: String) {
        viewModelScope.launch { settingsRepository.setOpenRouterApiKey(key) }
    }

    fun setPreferredProvider(provider: String) {
        viewModelScope.launch { settingsRepository.setPreferredProvider(provider) }
    }

    fun clearHistory() {
        viewModelScope.launch { chatRepository.clearAllHistory() }
    }
}

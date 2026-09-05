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

    val openRouterLlamaKey: StateFlow<String?> =
        settingsRepository.openRouterLlamaKey.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    val openRouterQwenCoderKey: StateFlow<String?> =
        settingsRepository.openRouterQwenCoderKey.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    val openRouterGptOssKey: StateFlow<String?> =
        settingsRepository.openRouterGptOssKey.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    fun saveGeminiKey(key: String) {
        viewModelScope.launch { settingsRepository.setGeminiApiKey(key) }
    }

    fun saveOpenRouterLlamaKey(key: String) {
        viewModelScope.launch { settingsRepository.setOpenRouterLlamaKey(key) }
    }

    fun saveOpenRouterQwenCoderKey(key: String) {
        viewModelScope.launch { settingsRepository.setOpenRouterQwenCoderKey(key) }
    }

    fun saveOpenRouterGptOssKey(key: String) {
        viewModelScope.launch { settingsRepository.setOpenRouterGptOssKey(key) }
    }

    fun clearHistory() {
        viewModelScope.launch { chatRepository.clearAllHistory() }
    }
}

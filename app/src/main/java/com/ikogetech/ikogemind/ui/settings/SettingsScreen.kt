package com.ikogetech.ikogemind.ui.settings

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Divider
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.ikogetech.ikogemind.data.repository.ChatRepository
import com.ikogetech.ikogemind.data.repository.SettingsRepository
import com.ikogetech.ikogemind.ui.ViewModelFactories

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    settingsRepository: SettingsRepository,
    chatRepository: ChatRepository,
    onBack: () -> Unit
) {
    val viewModel: SettingsViewModel = viewModel(
        factory = ViewModelFactories.settings(settingsRepository, chatRepository)
    )

    val savedGeminiKey by viewModel.geminiApiKey.collectAsState()
    val savedLlamaKey by viewModel.openRouterLlamaKey.collectAsState()
    val savedQwenCoderKey by viewModel.openRouterQwenCoderKey.collectAsState()
    val savedGptOssKey by viewModel.openRouterGptOssKey.collectAsState()

    var showClearConfirm by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Settings") },
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
                .fillMaxWidth()
                .padding(padding)
                .padding(16.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text("API keys", style = MaterialTheme.typography.titleMedium)
            Text(
                "Encrypted on-device with Android Keystore before storage.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            ApiKeyField(
                label = "Gemini API key",
                savedKey = savedGeminiKey,
                onSave = viewModel::saveGeminiKey
            )

            Divider()

            Text("OpenRouter keys", style = MaterialTheme.typography.titleMedium)
            Text(
                "One key per fallback model — Gemini falls back to these in order " +
                    "if it's rate-limited (model-routing.md).",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            ApiKeyField(
                label = "Llama 3.1 405B key",
                savedKey = savedLlamaKey,
                onSave = viewModel::saveOpenRouterLlamaKey
            )
            ApiKeyField(
                label = "Qwen3 Coder key",
                savedKey = savedQwenCoderKey,
                onSave = viewModel::saveOpenRouterQwenCoderKey
            )
            ApiKeyField(
                label = "gpt-oss-120b key",
                savedKey = savedGptOssKey,
                onSave = viewModel::saveOpenRouterGptOssKey
            )

            // "No API key set" empty state lives in Chat screen's Error path when
            // ModelRouter throws; this screen is where the user resolves it.

            Divider()

            Text("Data", style = MaterialTheme.typography.titleMedium)
            OutlinedButton(onClick = { showClearConfirm = true }) {
                Text("Clear all chat history")
            }
        }
    }

    if (showClearConfirm) {
        AlertDialog(
            onDismissRequest = { showClearConfirm = false },
            title = { Text("Clear all chat history?") },
            text = { Text("This deletes every conversation and message on this device. This can't be undone.") },
            confirmButton = {
                TextButton(onClick = {
                    viewModel.clearHistory()
                    showClearConfirm = false
                }) { Text("Clear") }
            },
            dismissButton = {
                TextButton(onClick = { showClearConfirm = false }) { Text("Cancel") }
            }
        )
    }
}

/**
 * One API-key input row: text field + Save button, "Saved" derived from whether the
 * field matches what's actually persisted (not a separate flag that resets on every
 * recomposition — see prior fix note this replaces).
 */
@Composable
private fun ApiKeyField(
    label: String,
    savedKey: String?,
    onSave: (String) -> Unit
) {
    var input by remember { mutableStateOf("") }
    LaunchedEffect(savedKey) { input = savedKey.orEmpty() }
    val isSaved = input.isNotBlank() && input == savedKey.orEmpty()

    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
        OutlinedTextField(
            value = input,
            onValueChange = { input = it },
            label = { Text(label) },
            singleLine = true,
            visualTransformation = PasswordVisualTransformation(),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
            modifier = Modifier.fillMaxWidth()
        )
        Button(onClick = { onSave(input) }) {
            Text(if (isSaved) "Saved" else "Save")
        }
    }
}

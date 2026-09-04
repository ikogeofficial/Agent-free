package com.ikogetech.ikogemind.ui.settings

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
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
import androidx.compose.material3.FilterChip
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
    val savedOpenRouterKey by viewModel.openRouterApiKey.collectAsState()
    val preferredProvider by viewModel.preferredProvider.collectAsState()

    var geminiInput by remember { mutableStateOf("") }
    var openRouterInput by remember { mutableStateOf("") }
    var showClearConfirm by remember { mutableStateOf(false) }
    var geminiJustSaved by remember { mutableStateOf(false) }
    var openRouterJustSaved by remember { mutableStateOf(false) }

    LaunchedEffect(savedGeminiKey) { geminiInput = savedGeminiKey.orEmpty() }
    LaunchedEffect(savedOpenRouterKey) { openRouterInput = savedOpenRouterKey.orEmpty() }

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
                "Stored on-device. Not encrypted yet in v1 — see SettingsRepository " +
                    "for the flagged TODO before this goes beyond personal testing.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            OutlinedTextField(
                value = geminiInput,
                onValueChange = { geminiInput = it; geminiJustSaved = false },
                label = { Text("Gemini API key") },
                singleLine = true,
                visualTransformation = PasswordVisualTransformation(),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                modifier = Modifier.fillMaxWidth()
            )
            Button(onClick = {
                viewModel.saveGeminiKey(geminiInput)
                geminiJustSaved = true
            }) {
                Text(if (geminiJustSaved) "Saved" else "Save Gemini key")
            }

            OutlinedTextField(
                value = openRouterInput,
                onValueChange = { openRouterInput = it; openRouterJustSaved = false },
                label = { Text("OpenRouter API key") },
                singleLine = true,
                visualTransformation = PasswordVisualTransformation(),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                modifier = Modifier.fillMaxWidth()
            )
            Button(onClick = {
                viewModel.saveOpenRouterKey(openRouterInput)
                openRouterJustSaved = true
            }) {
                Text(if (openRouterJustSaved) "Saved" else "Save OpenRouter key")
            }

            // "No API key set" empty state lives in Chat screen's Error path when
            // ModelRouter throws; this screen is where the user resolves it.

            Divider()

            Text("Preferred provider", style = MaterialTheme.typography.titleMedium)
            Row {
                listOf("auto", "gemini", "openrouter").forEach { option ->
                    FilterChip(
                        selected = preferredProvider == option,
                        onClick = { viewModel.setPreferredProvider(option) },
                        label = { Text(option) },
                        modifier = Modifier.padding(end = 8.dp)
                    )
                }
            }
            Text(
                "\"auto\" follows model-routing.md: Gemini first, OpenRouter free " +
                    "models on rate limit. Manual gemini/openrouter selection is stored " +
                    "but not yet read by ModelRouter — wire it in when task-based " +
                    "routing lands.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

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

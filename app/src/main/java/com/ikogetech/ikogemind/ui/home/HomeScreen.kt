package com.ikogetech.ikogemind.ui.home

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.Send
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.ikogetech.ikogemind.ui.theme.GlassSurface
import com.ikogetech.ikogemind.ui.theme.IkogeAccent
import com.ikogetech.ikogemind.ui.theme.IkogePillShape
import java.time.LocalTime

/**
 * App landing screen — the ChatGPT-app-style "Hello / How can I help you today? /
 * Ask anything" entry point from the reference images, matching the settled Home
 * screen decision in decisions-log.md (blue palette, glossy swirl-ring orb hero).
 *
 * Assumption (not yet settled elsewhere, flagging per project instructions): the
 * reference mockup greets a name ("Hello Robin"), but IkogeMind has no user-profile
 * or display-name storage yet (see data/repository — only chat + settings repos
 * exist). Using a time-of-day greeting instead of a hardcoded/fake name. If a
 * display name gets added later (e.g. to SettingsRepository), swap greetingText()
 * for a real "Hello {name}" without touching layout.
 *
 * Typing in the input bar and hitting send does NOT auto-send to the model — it
 * navigates to a new Chat screen with the draft prefilled, same "review before it's
 * sent" pattern already used by ChatScreen's quick-action chips. The mic icon is a
 * visual placeholder for now: no speech-to-text pipeline exists yet (not in
 * model-routing.md's v1 scope), so it just surfaces a toast rather than pretending
 * to work.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    onOpenHistory: () -> Unit,
    onOpenSettings: () -> Unit,
    onStartChat: (draft: String) -> Unit
) {
    var inputText by remember { mutableStateOf("") }
    val context = LocalContext.current

    fun submit() {
        val text = inputText
        if (text.isNotBlank()) {
            inputText = ""
            onStartChat(text)
        }
    }

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            TopAppBar(
                title = {},
                navigationIcon = {
                    IconButton(onClick = onOpenHistory) {
                        Icon(Icons.Filled.Menu, contentDescription = "Conversation history")
                    }
                },
                actions = {
                    IconButton(onClick = onOpenSettings) {
                        Icon(Icons.Filled.Settings, contentDescription = "Settings")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background,
                    titleContentColor = MaterialTheme.colorScheme.onBackground
                )
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background)
                .padding(padding)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .weight(1f)
                    .padding(horizontal = 32.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                OrbHero(modifier = Modifier.width(180.dp))

                Text(
                    text = greetingText(),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(top = 20.dp)
                )
                Text(
                    text = "How can I help you today?",
                    style = MaterialTheme.typography.titleLarge,
                    color = MaterialTheme.colorScheme.onBackground,
                    modifier = Modifier.padding(top = 4.dp)
                )
            }

            // Same pill-shaped glass input bar language as ChatScreen, so the app
            // doesn't introduce a second input-bar style for one screen.
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 16.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                GlassSurface(
                    modifier = Modifier.weight(1f),
                    shape = IkogePillShape
                ) {
                    TextField(
                        value = inputText,
                        onValueChange = { inputText = it },
                        modifier = Modifier.fillMaxWidth(),
                        placeholder = { Text("Ask anything...") },
                        shape = IkogePillShape,
                        colors = TextFieldDefaults.colors(
                            focusedContainerColor = Color.Transparent,
                            unfocusedContainerColor = Color.Transparent,
                            focusedIndicatorColor = Color.Transparent,
                            unfocusedIndicatorColor = Color.Transparent,
                            disabledIndicatorColor = Color.Transparent
                        )
                    )
                }
                GlassSurface(
                    shape = CircleShape,
                    fill = IkogeAccent,
                    glow = true
                ) {
                    IconButton(
                        onClick = {
                            if (inputText.isBlank()) {
                                Toast.makeText(context, "Voice input coming soon", Toast.LENGTH_SHORT).show()
                            } else {
                                submit()
                            }
                        }
                    ) {
                        Icon(
                            Icons.Filled.Send,
                            contentDescription = "Send",
                            tint = Color.White
                        )
                    }
                }
            }
        }
    }
}

private fun greetingText(): String {
    val hour = LocalTime.now().hour
    return when {
        hour < 12 -> "Good morning"
        hour < 18 -> "Good afternoon"
        else -> "Good evening"
    }
}

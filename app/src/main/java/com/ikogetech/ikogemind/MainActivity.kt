package com.ikogetech.ikogemind

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.ikogetech.ikogemind.ui.navigation.IkogeMindNavGraph
import com.ikogetech.ikogemind.ui.theme.IkogeMindTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        val app = application as IkogeMindApp

        setContent {
            IkogeMindTheme {
                IkogeMindNavGraph(
                    chatRepository = app.chatRepository,
                    settingsRepository = app.settingsRepository,
                    pipelineOrchestrator = app.pipelineOrchestrator
                )
            }
        }
    }
}

package com.ikogetech.ikogemind.ui.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.ikogetech.ikogemind.data.repository.ChatRepository
import com.ikogetech.ikogemind.data.repository.SettingsRepository
import com.ikogetech.ikogemind.pipeline.PipelineOrchestrator
import com.ikogetech.ikogemind.ui.chat.ChatScreen
import com.ikogetech.ikogemind.ui.conversationlist.ConversationListScreen
import com.ikogetech.ikogemind.ui.settings.SettingsScreen

@Composable
fun IkogeMindNavGraph(
    chatRepository: ChatRepository,
    settingsRepository: SettingsRepository,
    pipelineOrchestrator: PipelineOrchestrator,
    navController: NavHostController = rememberNavController()
) {
    NavHost(navController = navController, startDestination = Routes.ConversationList.route) {

        composable(Routes.ConversationList.route) {
            ConversationListScreen(
                chatRepository = chatRepository,
                onOpenConversation = { id -> navController.navigate(Routes.Chat.path(id)) },
                onNewChat = { navController.navigate(Routes.Chat.path(Routes.Chat.NEW_CHAT_ID)) },
                onOpenSettings = { navController.navigate(Routes.Settings.route) }
            )
        }

        composable(
            route = Routes.Chat.route,
            arguments = listOf(navArgument(Routes.Chat.ARG_CONVERSATION_ID) { defaultValue = Routes.Chat.NEW_CHAT_ID })
        ) { backStackEntry ->
            val conversationId = backStackEntry.arguments
                ?.getString(Routes.Chat.ARG_CONVERSATION_ID)
                ?: Routes.Chat.NEW_CHAT_ID

            ChatScreen(
                conversationId = conversationId,
                chatRepository = chatRepository,
                pipelineOrchestrator = pipelineOrchestrator,
                onBack = { navController.popBackStack() },
                onConversationIdAssigned = { realId ->
                    // Swap "new" for the real id in the back stack entry's saved state
                    // so back navigation / process death restore land on the right
                    // conversation, without pushing a duplicate nav entry.
                    backStackEntry.savedStateHandle[Routes.Chat.ARG_CONVERSATION_ID] = realId
                }
            )
        }

        composable(Routes.Settings.route) {
            SettingsScreen(
                settingsRepository = settingsRepository,
                chatRepository = chatRepository,
                onBack = { navController.popBackStack() }
            )
        }
    }
}

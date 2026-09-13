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
import com.ikogetech.ikogemind.ui.home.HomeScreen
import com.ikogetech.ikogemind.ui.settings.SettingsScreen

@Composable
fun IkogeMindNavGraph(
    chatRepository: ChatRepository,
    settingsRepository: SettingsRepository,
    pipelineOrchestrator: PipelineOrchestrator,
    navController: NavHostController = rememberNavController()
) {
    // Home is now the app's landing screen (settled decision, see decisions-log.md);
    // ConversationList moves to being reached via Home's history icon rather than
    // being the first thing shown on launch.
    NavHost(navController = navController, startDestination = Routes.Home.route) {

        composable(Routes.Home.route) {
            HomeScreen(
                onOpenHistory = { navController.navigate(Routes.ConversationList.route) },
                onOpenSettings = { navController.navigate(Routes.Settings.route) },
                onStartChat = { draft ->
                    navController.navigate(Routes.Chat.path(Routes.Chat.NEW_CHAT_ID))
                    // Set after navigate() (rather than passed as a nav arg) so the
                    // draft is out-of-band from the route pattern itself — same
                    // SavedStateHandle handoff NavGraph already relies on for
                    // onConversationIdAssigned below, just set on the entry we're
                    // navigating to instead of read from the one we're leaving.
                    navController.currentBackStackEntry
                        ?.savedStateHandle
                        ?.set(Routes.Chat.ARG_DRAFT, draft)
                }
            )
        }

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

            val initialDraft = backStackEntry.savedStateHandle
                .get<String>(Routes.Chat.ARG_DRAFT)

            ChatScreen(
                conversationId = conversationId,
                initialDraft = initialDraft,
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

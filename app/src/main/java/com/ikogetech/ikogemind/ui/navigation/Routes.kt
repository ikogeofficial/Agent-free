package com.ikogetech.ikogemind.ui.navigation

sealed class Routes(val route: String) {
    data object ConversationList : Routes("conversation_list")
    data object Settings : Routes("settings")

    data object Chat : Routes("chat/{conversationId}") {
        const val ARG_CONVERSATION_ID = "conversationId"
        const val NEW_CHAT_ID = "new"

        fun path(conversationId: String) = "chat/$conversationId"
    }
}

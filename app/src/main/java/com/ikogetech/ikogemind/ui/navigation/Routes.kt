package com.ikogetech.ikogemind.ui.navigation

sealed class Routes(val route: String) {
    data object Home : Routes("home")
    data object ConversationList : Routes("conversation_list")
    data object Settings : Routes("settings")

    data object Chat : Routes("chat/{conversationId}") {
        const val ARG_CONVERSATION_ID = "conversationId"
        const val NEW_CHAT_ID = "new"

        // Key used on the destination back stack entry's SavedStateHandle to carry
        // a draft message typed on the Home screen into Chat's input field (not
        // auto-sent — see HomeScreen.kt). Same handoff pattern NavGraph already uses
        // for onConversationIdAssigned, just set before navigate() instead of after.
        const val ARG_DRAFT = "draft"

        fun path(conversationId: String) = "chat/$conversationId"
    }
}

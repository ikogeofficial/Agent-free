package com.ikogetech.ikogemind.data.repository

import com.ikogetech.ikogemind.data.local.ConversationDao
import com.ikogetech.ikogemind.data.local.ConversationEntity
import com.ikogetech.ikogemind.data.local.MessageDao
import com.ikogetech.ikogemind.data.local.MessageEntity
import kotlinx.coroutines.flow.Flow
import java.util.UUID

class ChatRepository(
    private val conversationDao: ConversationDao,
    private val messageDao: MessageDao
) {
    fun observeConversations(): Flow<List<ConversationEntity>> =
        conversationDao.observeAll()

    fun observeMessages(conversationId: String): Flow<List<MessageEntity>> =
        messageDao.observeForConversation(conversationId)

    suspend fun createConversation(firstMessagePreview: String = "New chat"): ConversationEntity {
        val now = System.currentTimeMillis()
        val conversation = ConversationEntity(
            id = UUID.randomUUID().toString(),
            title = firstMessagePreview.take(40).ifBlank { "New chat" },
            createdAt = now,
            lastMessageAt = now,
            lastMessagePreview = ""
        )
        conversationDao.upsert(conversation)
        return conversation
    }

    suspend fun addMessage(
        conversationId: String,
        role: String,
        content: String,
        providerUsed: String? = null,
        isError: Boolean = false
    ) {
        val message = MessageEntity(
            id = UUID.randomUUID().toString(),
            conversationId = conversationId,
            role = role,
            content = content,
            createdAt = System.currentTimeMillis(),
            providerUsed = providerUsed,
            isError = isError
        )
        messageDao.insert(message)

        val conversation = conversationDao.getById(conversationId)
        if (conversation != null) {
            conversationDao.update(
                conversation.copy(
                    lastMessageAt = message.createdAt,
                    lastMessagePreview = content.take(80),
                    // Auto-title from first user message if still on the default title.
                    title = if (conversation.title == "New chat" && role == "user")
                        content.take(40) else conversation.title
                )
            )
        }
    }

    suspend fun deleteConversation(conversationId: String) {
        messageDao.deleteForConversation(conversationId)
        conversationDao.delete(conversationId)
    }

    suspend fun clearAllHistory() {
        messageDao.clearAll()
        conversationDao.clearAll()
    }
}

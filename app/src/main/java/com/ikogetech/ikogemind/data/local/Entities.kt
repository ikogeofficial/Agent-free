package com.ikogetech.ikogemind.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "conversations")
data class ConversationEntity(
    @PrimaryKey val id: String,
    val title: String,
    val createdAt: Long,
    val lastMessageAt: Long,
    val lastMessagePreview: String = ""
)

@Entity(tableName = "messages")
data class MessageEntity(
    @PrimaryKey val id: String,
    val conversationId: String,
    val role: String, // "user" | "assistant" | "system"
    val content: String,
    val createdAt: Long,
    val providerUsed: String? = null, // e.g. "gemini", "openrouter:llama-3-8b"
    val isError: Boolean = false
)

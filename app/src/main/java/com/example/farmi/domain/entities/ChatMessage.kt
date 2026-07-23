package com.example.farmi.domain.entities

import java.util.UUID

/**
 * Pure Domain Entity representing a Chat Message.
 */
data class ChatMessage(
    val id: UUID = UUID.randomUUID(),
    val content: String,
    val sender: MessageSender,
    val timestamp: Long = System.currentTimeMillis(),
    val qaId: String? = null,
    val likeStatus: Int = 0 // 0 = unrated, 1 = liked, -1 = disliked
)

enum class MessageSender {
    USER,
    ASSISTANT
}

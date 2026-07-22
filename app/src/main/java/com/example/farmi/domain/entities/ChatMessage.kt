package com.example.farmi.domain.entities

import java.util.UUID

/**
 * Pure Domain Entity representing a Chat Message.
 */
data class ChatMessage(
    val id: UUID = UUID.randomUUID(),
    val content: String,
    val sender: MessageSender,
    val timestamp: Long = System.currentTimeMillis()
)

enum class MessageSender {
    USER,
    ASSISTANT
}

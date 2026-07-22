package com.example.farmi.domain.entities

import java.util.UUID

/**
 * Pure Domain Entity representing a historical Chat Session thread.
 */
data class ChatSession(
    val id: UUID = UUID.randomUUID(),
    val title: String,
    val messages: List<ChatMessage>
)

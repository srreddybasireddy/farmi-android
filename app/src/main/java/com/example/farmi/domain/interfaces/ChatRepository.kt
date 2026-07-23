package com.example.farmi.domain.interfaces

data class ChatAdvisoryResponse(
    val answer: String,
    val qaId: String?
)

/**
 * Domain interface for chat advisory service.
 */
interface ChatRepository {
    suspend fun getAdvisoryResponse(deviceUuid: String, category: String, query: String): ChatAdvisoryResponse
    suspend fun submitRating(deviceUuid: String, qaId: String, rating: Int)
}

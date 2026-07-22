package com.example.farmi.domain.interfaces

/**
 * Domain interface for chat advisory service.
 */
interface ChatRepository {
    suspend fun getAdvisoryResponse(deviceUuid: String, category: String, query: String): String
}

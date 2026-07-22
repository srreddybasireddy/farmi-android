package com.example.farmi.domain.entities

import java.util.UUID

/**
 * Pure Domain Entity representing a Cattle / Livestock.
 */
data class Cattle(
    val id: UUID = UUID.randomUUID(),
    val tagNumber: String,
    val breed: String,
    val ageMonths: Int,
    val status: String,
    val healthAlerts: String? = null
)

package com.example.farmi.domain.entities

import java.util.Date
import java.util.UUID

/**
 * Pure Domain Entity representing a Crop.
 * This model is independent of database models or API response models (DTOs).
 */
data class Crop(
    val id: UUID = UUID.randomUUID(),
    val name: String,
    val variety: String,
    val plantedDate: Date,
    val estimatedHarvestDate: Date,
    val status: CropStatus
)

enum class CropStatus(val value: String) {
    PLANTED("Planted"),
    GROWING("Growing"),
    HARVESTING("Harvesting"),
    HARVESTED("Harvested")
}

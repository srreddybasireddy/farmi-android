package com.example.farmi.domain.entities

import java.util.UUID

/**
 * Pure Domain Entity representing a generic asset item (crops, livestock, equipment) in clean category listings.
 */
data class AssetItem(
    val id: UUID = UUID.randomUUID(),
    val title: String,
    val subtitle: String,
    val status: String,
    val statusColorName: String, // Domain representation of color (e.g., "green", "orange")
    val iconName: String
)

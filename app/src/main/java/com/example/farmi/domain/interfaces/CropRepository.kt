package com.example.farmi.domain.interfaces

import com.example.farmi.domain.entities.Crop
import java.util.UUID

/**
 * Repository Interface.
 * The Domain Layer uses this contract to interact with data services.
 */
interface CropRepository {
    suspend fun getCrops(): List<Crop>
    suspend fun addCrop(crop: Crop)
    suspend fun updateCrop(crop: Crop)
    suspend fun deleteCrop(id: UUID)
}

package com.example.farmi.domain.usecases

import com.example.farmi.domain.entities.Crop
import com.example.farmi.domain.interfaces.CropRepository

/**
 * Clean Architecture Use Case.
 * Encapsulates the business rule for fetching crops.
 */
class GetCropsUseCase(val repository: CropRepository) {
    suspend fun execute(): List<Crop> {
        val crops = repository.getCrops()
        return crops.sortedByDescending { it.plantedDate }
    }
}

package com.example.farmi.domain.usecases

import com.example.farmi.domain.entities.Crop
import com.example.farmi.domain.entities.CropStatus
import com.example.farmi.domain.interfaces.CropRepository
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Test
import java.util.Date
import java.util.UUID

class GetCropsUseCaseTest {

    private class FakeCropRepository(private val crops: List<Crop>) : CropRepository {
        override suspend fun getCrops(): List<Crop> = crops
        override suspend fun addCrop(crop: Crop) {}
        override suspend fun updateCrop(crop: Crop) {}
        override suspend fun deleteCrop(id: UUID) {}
    }

    @Test
    fun execute_ordersCropsByPlantedDateDescending() = runTest {
        val crop1 = Crop(name = "Crop 1", variety = "V1", plantedDate = Date(1000), estimatedHarvestDate = Date(5000), status = CropStatus.PLANTED)
        val crop2 = Crop(name = "Crop 2", variety = "V2", plantedDate = Date(3000), estimatedHarvestDate = Date(6000), status = CropStatus.GROWING)
        val crop3 = Crop(name = "Crop 3", variety = "V3", plantedDate = Date(2000), estimatedHarvestDate = Date(7000), status = CropStatus.HARVESTED)

        val repository = FakeCropRepository(listOf(crop1, crop2, crop3))
        val useCase = GetCropsUseCase(repository)

        val result = useCase.execute()

        assertEquals(3, result.size)
        assertEquals("Crop 2", result[0].name) // PlantedDate 3000
        assertEquals("Crop 3", result[1].name) // PlantedDate 2000
        assertEquals("Crop 1", result[2].name) // PlantedDate 1000
    }
}

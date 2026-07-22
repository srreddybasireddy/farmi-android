package com.example.farmi.ui.home

import com.example.farmi.domain.entities.Crop
import com.example.farmi.domain.entities.CropStatus
import com.example.farmi.domain.interfaces.CropRepository
import com.example.farmi.domain.interfaces.ChatRepository
import com.example.farmi.domain.usecases.GetCropsUseCase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import java.util.Date
import java.util.UUID

@OptIn(ExperimentalCoroutinesApi::class)
class HomeViewModelTest {

    private val testDispatcher = UnconfinedTestDispatcher()

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    private class FakeCropRepository(
        private val crops: List<Crop> = emptyList(),
        private val shouldFail: Boolean = false
    ) : CropRepository {
        override suspend fun getCrops(): List<Crop> {
            if (shouldFail) throw RuntimeException("Fetch failed")
            return crops
        }
        override suspend fun addCrop(crop: Crop) {}
        override suspend fun updateCrop(crop: Crop) {}
        override suspend fun deleteCrop(id: UUID) {}
    }

    private class FakeChatRepository : ChatRepository {
        override suspend fun getAdvisoryResponse(deviceUuid: String, category: String, query: String): String {
            return "Mock advisory answer"
        }
    }

    @Test
    fun loadCrops_success_updatesUiStateWithCropsList() = runTest {
        val crops = listOf(
            Crop(name = "Crop 1", variety = "V1", plantedDate = Date(), estimatedHarvestDate = Date(), status = CropStatus.PLANTED)
        )
        val repository = FakeCropRepository(crops)
        val useCase = GetCropsUseCase(repository)
        val viewModel = HomeViewModel(useCase, FakeChatRepository(), "test-device-uuid")

        // HomeViewModel triggers loadCrops() automatically in init
        val state = viewModel.uiState.value
        assertFalse(state.isLoading)
        assertNull(state.errorMessage)
        assertEquals(crops, state.crops)
    }

    @Test
    fun loadCrops_failure_updatesUiStateWithError() = runTest {
        val repository = FakeCropRepository(shouldFail = true)
        val useCase = GetCropsUseCase(repository)
        val viewModel = HomeViewModel(useCase, FakeChatRepository(), "test-device-uuid")

        val state = viewModel.uiState.value
        assertFalse(state.isLoading)
        assertEquals("Fetch failed", state.errorMessage)
        assertTrue(state.crops.isEmpty())
    }
}

package com.example.farmi

import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation3.runtime.entryProvider
import androidx.navigation3.runtime.rememberNavBackStack
import androidx.navigation3.ui.NavDisplay
import com.example.farmi.data.repositories.CropRepositoryImpl
import com.example.farmi.data.repositories.ChatRepositoryImpl
import com.example.farmi.domain.entities.AssetItem
import com.example.farmi.domain.usecases.GetCropsUseCase
import com.example.farmi.ui.home.CattleDetailScreen
import com.example.farmi.ui.home.CropDetailScreen
import com.example.farmi.ui.home.GenericAssetDetailScreen
import com.example.farmi.ui.home.HomeScreen
import com.example.farmi.ui.home.HomeViewModel
import com.example.farmi.ui.splash.SplashScreen
import android.content.Context
import androidx.compose.ui.platform.LocalContext

@Composable
fun MainNavigation() {
  val backStack = rememberNavBackStack(Splash)
  val context = LocalContext.current.applicationContext

  // Shared ViewModel scoped to MainNavigation so that all routes
  // share the active list data and chat states
  val homeViewModel: HomeViewModel = viewModel {
    val sharedPrefs = context.getSharedPreferences("farmi_prefs", Context.MODE_PRIVATE)
    val deviceUuid = sharedPrefs.getString("user_device_uuid", null) ?: run {
      val newUuid = java.util.UUID.randomUUID().toString()
      sharedPrefs.edit().putString("user_device_uuid", newUuid).apply()
      newUuid
    }
    val cropRepository = CropRepositoryImpl(deviceUuid)
    val chatRepository = ChatRepositoryImpl()
    HomeViewModel(GetCropsUseCase(cropRepository), chatRepository, deviceUuid)
  }

  NavDisplay(
    backStack = backStack,
    onBack = { backStack.removeLastOrNull() },
    entryProvider =
      entryProvider {
        entry<Splash> {
          SplashScreen(
            onTimeout = {
              backStack.removeLastOrNull()
              backStack.add(Home)
            },
            modifier = Modifier.safeDrawingPadding()
          )
        }
        
        entry<Home> {
          HomeScreen(
            viewModel = homeViewModel,
            onNavigateToCropDetail = { cropId ->
              backStack.add(CropDetail(cropId))
            },
            onNavigateToCattleDetail = { tagNumber ->
              backStack.add(CattleDetail(tagNumber))
            },
            onNavigateToGenericAssetDetail = { title, category ->
              backStack.add(GenericAssetDetail(title, category))
            },
            modifier = Modifier.safeDrawingPadding()
          )
        }

        entry<CropDetail> { key ->
          val cropId = key.cropId
          val crops = homeViewModel.uiState.collectAsState().value.crops
          val crop = crops.firstOrNull { it.name == cropId }
          if (crop != null) {
            CropDetailScreen(
              crop = crop,
              onBack = { backStack.removeLastOrNull() },
              modifier = Modifier.safeDrawingPadding()
            )
          }
        }

        entry<CattleDetail> { key ->
          val tagNumber = key.tagNumber
          val cattleList = homeViewModel.uiState.collectAsState().value.cattleList
          val cattle = cattleList.firstOrNull { it.tagNumber == tagNumber }
          if (cattle != null) {
            CattleDetailScreen(
              cattle = cattle,
              onBack = { backStack.removeLastOrNull() },
              modifier = Modifier.safeDrawingPadding()
            )
          }
        }

        entry<GenericAssetDetail> { key ->
          val title = key.title
          val category = key.category
          val assetItem = homeViewModel.activeCategoryAssets().firstOrNull { it.title == title } ?: AssetItem(
            title = title,
            subtitle = "Generic Asset",
            status = "Active",
            statusColorName = "green",
            iconName = "info"
          )
          GenericAssetDetailScreen(
            item = assetItem,
            onBack = { backStack.removeLastOrNull() },
            modifier = Modifier.safeDrawingPadding()
          )
        }
      },
  )
}

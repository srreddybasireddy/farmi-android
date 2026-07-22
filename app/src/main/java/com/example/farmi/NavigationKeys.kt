package com.example.farmi

import androidx.navigation3.runtime.NavKey
import kotlinx.serialization.Serializable

@Serializable data object Splash : NavKey
@Serializable data object Home : NavKey
@Serializable data class CropDetail(val cropId: String) : NavKey
@Serializable data class CattleDetail(val tagNumber: String) : NavKey
@Serializable data class GenericAssetDetail(val title: String, val category: String) : NavKey

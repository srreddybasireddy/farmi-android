package com.example.farmi.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.graphics.Color


private val DarkColorScheme = darkColorScheme(
  primary = IndigoPrimary,
  background = DarkBackground,
  surface = DarkBackground,
  onPrimary = Color.White,
  onBackground = Color.White,
  onSurface = Color.White
)

@Composable
fun FarmiTheme(
  darkTheme: Boolean = true, // Force dark mode to match iOS app premium dark design
  content: @Composable () -> Unit,
) {
  val colorScheme = DarkColorScheme

  MaterialTheme(colorScheme = colorScheme, typography = Typography, content = content)
}


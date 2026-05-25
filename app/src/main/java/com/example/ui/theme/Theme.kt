package com.example.ui.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext

private val DarkColorScheme =
  darkColorScheme(
    primary = GlowGreen,
    secondary = MutedSilver,
    tertiary = RefinedGreen,
    background = ObsidianBg,
    surface = ObsidianSurface,
    onPrimary = ObsidianBg,
    onSecondary = WarmOffWhite,
    onBackground = WarmOffWhite,
    onSurface = WarmOffWhite,
    surfaceVariant = ObsidianBorder,
    onSurfaceVariant = WarmSilverText
  )

@Composable
fun MyApplicationTheme(
  darkTheme: Boolean = true, // Force dark theme for obsidian brand
  dynamicColor: Boolean = false, // Disable dynamic colors to preserve premium brand look
  content: @Composable () -> Unit,
) {
  val colorScheme = DarkColorScheme

  MaterialTheme(colorScheme = colorScheme, typography = Typography, content = content)
}

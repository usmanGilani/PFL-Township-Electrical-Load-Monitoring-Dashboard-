package com.example.ui.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext

private val NordicSlateScheme =
  lightColorScheme(
    primary = GeoPrimary,
    onPrimary = GeoOnPrimary,
    primaryContainer = GeoPrimaryContainer,
    onPrimaryContainer = GeoOnPrimaryContainer,
    secondary = GeoSecondary,
    onSecondary = GeoOnSecondary,
    secondaryContainer = GeoSecondaryContainer,
    onSecondaryContainer = GeoOnSecondaryContainer,
    tertiary = GeoTertiary,
    onTertiary = GeoOnTertiary,
    tertiaryContainer = GeoTertiaryContainer,
    onTertiaryContainer = GeoOnTertiaryContainer,
    background = GeoBackground,
    onBackground = GeoOnBackground,
    surface = GeoSurface,
    onSurface = GeoOnSurface,
    surfaceVariant = GeoSurfaceVariant,
    onSurfaceVariant = GeoOnSurfaceVariant,
    outline = GeoOutline,
    outlineVariant = GeoOutlineVariant
  )

private val CyberpunkNeonScheme =
  darkColorScheme(
    primary = CyberPrimary,
    onPrimary = CyberOnPrimary,
    primaryContainer = CyberPrimaryContainer,
    onPrimaryContainer = CyberOnPrimaryContainer,
    secondary = CyberSecondary,
    onSecondary = CyberOnSecondary,
    secondaryContainer = CyberSecondaryContainer,
    onSecondaryContainer = CyberOnSecondaryContainer,
    tertiary = CyberTertiary,
    onTertiary = CyberOnTertiary,
    tertiaryContainer = CyberTertiaryContainer,
    onTertiaryContainer = CyberOnTertiaryContainer,
    background = CyberBackground,
    onBackground = CyberOnBackground,
    surface = CyberSurface,
    onSurface = CyberOnSurface,
    surfaceVariant = CyberSurfaceVariant,
    onSurfaceVariant = CyberOnSurfaceVariant,
    outline = CyberOutline,
    outlineVariant = CyberOutlineVariant
  )

private val SageGardenScheme =
  lightColorScheme(
    primary = SagePrimary,
    onPrimary = SageOnPrimary,
    primaryContainer = SagePrimaryContainer,
    onPrimaryContainer = SageOnPrimaryContainer,
    secondary = SageSecondary,
    onSecondary = SageOnSecondary,
    secondaryContainer = SageSecondaryContainer,
    onSecondaryContainer = SageOnSecondaryContainer,
    tertiary = SageTertiary,
    onTertiary = SageOnTertiary,
    tertiaryContainer = SageTertiaryContainer,
    onTertiaryContainer = SageOnTertiaryContainer,
    background = SageBackground,
    onBackground = SageOnBackground,
    surface = SageSurface,
    onSurface = SageOnSurface,
    surfaceVariant = SageSurfaceVariant,
    onSurfaceVariant = SageOnSurfaceVariant,
    outline = SageOutline,
    outlineVariant = SageOutlineVariant
  )

private val RoyalObsidianScheme =
  darkColorScheme(
    primary = GoldPrimary,
    onPrimary = GoldOnPrimary,
    primaryContainer = GoldPrimaryContainer,
    onPrimaryContainer = GoldOnPrimaryContainer,
    secondary = GoldSecondary,
    onSecondary = GoldOnSecondary,
    secondaryContainer = GoldSecondaryContainer,
    onSecondaryContainer = GoldOnSecondaryContainer,
    tertiary = GoldTertiary,
    onTertiary = GoldOnTertiary,
    tertiaryContainer = GoldTertiaryContainer,
    onTertiaryContainer = GoldOnTertiaryContainer,
    background = GoldBackground,
    onBackground = GoldOnBackground,
    surface = GoldSurface,
    onSurface = GoldOnSurface,
    surfaceVariant = GoldSurfaceVariant,
    onSurfaceVariant = GoldOnSurfaceVariant,
    outline = GoldOutline,
    outlineVariant = GoldOutlineVariant
  )

private val LightColorScheme = NordicSlateScheme
private val DarkColorScheme = CyberpunkNeonScheme

@Composable
fun MyApplicationTheme(
  selectedTheme: AppTheme = AppTheme.NORDIC_SLATE,
  content: @Composable () -> Unit,
) {
  val colorScheme = when (selectedTheme) {
    AppTheme.NORDIC_SLATE -> NordicSlateScheme
    AppTheme.CYBERPUNK_NEON -> CyberpunkNeonScheme
    AppTheme.SAGE_GARDEN -> SageGardenScheme
    AppTheme.ROYAL_OBSIDIAN -> RoyalObsidianScheme
  }

  MaterialTheme(
    colorScheme = colorScheme,
    typography = Typography,
    content = content
  )
}


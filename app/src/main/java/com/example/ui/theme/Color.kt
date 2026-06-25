package com.example.ui.theme

import androidx.compose.ui.graphics.Color

// --- Theme Options ---
enum class AppTheme(val displayName: String, val isDark: Boolean) {
    NORDIC_SLATE("Nordic Slate (Light)", false),
    CYBERPUNK_NEON("Cyberpunk Neon (Dark)", true),
    SAGE_GARDEN("Sage Garden (Light)", false),
    ROYAL_OBSIDIAN("Royal Obsidian (Dark)", true)
}

// --- 1. Nordic Slate (Modern Minimalist Light) ---
val GeoPrimary = Color(0xFF000000)
val GeoOnPrimary = Color(0xFFFFFFFF)
val GeoPrimaryContainer = Color(0xFFF1F5F9)
val GeoOnPrimaryContainer = Color(0xFF000000)
val GeoSecondary = Color(0xFF334155)
val GeoOnSecondary = Color(0xFFFFFFFF)
val GeoSecondaryContainer = Color(0xFFF8FAFC)
val GeoOnSecondaryContainer = Color(0xFF000000)
val GeoTertiary = Color(0xFF475569)
val GeoOnTertiary = Color(0xFFFFFFFF)
val GeoTertiaryContainer = Color(0xFFF1F5F9) 
val GeoOnTertiaryContainer = Color(0xFF000000)
val GeoBackground = Color(0xFFFFFFFF)
val GeoOnBackground = Color(0xFF000000)
val GeoSurface = Color(0xFFFFFFFF)
val GeoOnSurface = Color(0xFF000000)
val GeoSurfaceVariant = Color(0xFFF8FAFC)
val GeoOnSurfaceVariant = Color(0xFF334155)
val GeoOutline = Color(0xFF94A3B8)
val GeoOutlineVariant = Color(0xFFCBD5E1)

// --- 2. Cyberpunk Neon (Futuristic Tech Dark) ---
val CyberPrimary = Color(0xFF06B6D4)       // Neon Cyan
val CyberOnPrimary = Color(0xFF070A13)
val CyberPrimaryContainer = Color(0xFF0B1E2E)
val CyberOnPrimaryContainer = Color(0xFF06B6D4)
val CyberSecondary = Color(0xFFD946EF)     // Neon Purple/Pink
val CyberOnSecondary = Color(0xFF070A13)
val CyberSecondaryContainer = Color(0xFF1E0E2C)
val CyberOnSecondaryContainer = Color(0xFFD946EF)
val CyberTertiary = Color(0xFFF43F5E)      // Neon Rose
val CyberOnTertiary = Color(0xFFFFFFFF)
val CyberTertiaryContainer = Color(0xFF260D1E)
val CyberOnTertiaryContainer = Color(0xFFF43F5E)
val CyberBackground = Color(0xFF070A13)    // Deep Void Indigo
val CyberOnBackground = Color(0xFFF8FAFC)
val CyberSurface = Color(0xFF0D1222)       // Sleek Dark Surface
val CyberOnSurface = Color(0xFFF8FAFC)
val CyberSurfaceVariant = Color(0xFF1E2640)
val CyberOnSurfaceVariant = Color(0xFF94A3B8)
val CyberOutline = Color(0xFF38BDF8)       // Electric Cyan border
val CyberOutlineVariant = Color(0xFF475569)

// --- 3. Sage Garden (Earthy Natural Light) ---
val SagePrimary = Color(0xFF1E3F20)        // Deep Sage Forest
val SageOnPrimary = Color(0xFFF4F9F4)
val SagePrimaryContainer = Color(0xFFE2EFE2)
val SageOnPrimaryContainer = Color(0xFF1E3F20)
val SageSecondary = Color(0xFF4A6B53)      // Earthy Olive Leaf
val SageOnSecondary = Color(0xFFFFFFFF)
val SageSecondaryContainer = Color(0xFFF1F6F2)
val SageOnSecondaryContainer = Color(0xFF1E3F20)
val SageTertiary = Color(0xFF8B5A2B)       // Organic Wood / Amber
val SageOnTertiary = Color(0xFFFFFFFF)
val SageTertiaryContainer = Color(0xFFF9F1E6)
val SageOnTertiaryContainer = Color(0xFF8B5A2B)
val SageBackground = Color(0xFFFAFBF9)     // Soft Cream White
val SageOnBackground = Color(0xFF1A241B)
val SageSurface = Color(0xFFFFFFFF)
val SageOnSurface = Color(0xFF1A241B)
val SageSurfaceVariant = Color(0xFFF1F4F1)
val SageOnSurfaceVariant = Color(0xFF4A6B53)
val SageOutline = Color(0xFF6B8E72)        // Soft foliage green border
val SageOutlineVariant = Color(0xFFD4DDD5)

// --- 4. Royal Obsidian (Premium Black & Gold Dark) ---
val GoldPrimary = Color(0xFFD4AF37)        // Pure Metallic Gold
val GoldOnPrimary = Color(0xFF000000)
val GoldPrimaryContainer = Color(0xFF201D11)
val GoldOnPrimaryContainer = Color(0xFFD4AF37)
val GoldSecondary = Color(0xFFF3E5AB)      // Soft Champagne
val GoldOnSecondary = Color(0xFF000000)
val GoldSecondaryContainer = Color(0xFF1C1A14)
val GoldOnSecondaryContainer = Color(0xFFF3E5AB)
val GoldTertiary = Color(0xFFE5C158)       // Vivid Brass Gold
val GoldOnTertiary = Color(0xFF000000)
val GoldTertiaryContainer = Color(0xFF1A1915)
val GoldOnTertiaryContainer = Color(0xFFE5C158)
val GoldBackground = Color(0xFF0A0A0A)     // Onyx Black
val GoldOnBackground = Color(0xFFFFFFFF)
val GoldSurface = Color(0xFF121212)        // Deep Obsidian Gray
val GoldOnSurface = Color(0xFFFFFFFF)
val GoldSurfaceVariant = Color(0xFF1A1A1A)
val GoldOnSurfaceVariant = Color(0xFFE5E5E5)
val GoldOutline = Color(0xFF8A7322)        // Rich Bronze Gold border
val GoldOutlineVariant = Color(0xFF333333)

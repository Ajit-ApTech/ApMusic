package com.example.musicplayer.ui.theme

import android.app.Activity
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

// Neon Electric Violet & Cyber Cyan Theme for High-End Aesthetic Experience
val PastelMint = Color(0xFF8B5CF6)           // Electric Violet Accent
val PastelMintDark = Color(0xFF7C3AED)       // Deep Royal Violet
val PastelLavender = Color(0xFF06B6D4)       // Cyber Cyan Accent
val PastelPeach = Color(0xFFEC4899)          // Vibrant Magenta Highlight
val PastelRose = Color(0xFFF43F5E)           // Vivid Neon Crimson for Favorites
val PastelDarkBg = Color(0xFF090A0F)         // Deep Obsidian Midnight Background
val PastelCard = Color(0xFF131622)           // Dark Glass Slate Surface
val PastelCardElevated = Color(0xFF1E2235)   // Elevated Card & Capsule Surface
val PastelBorder = Color(0xFF2E354F)         // Hairline Dark Slate Border
val PastelTextPrimary = Color(0xFFF8FAFC)    // Pure Crisp Ice White
val PastelTextSecondary = Color(0xFF94A3B8)  // Clean Muted Slate Gray Subtitles

private val PastelColorScheme = darkColorScheme(
    primary = PastelMint,
    onPrimary = Color(0xFFFFFFFF),
    primaryContainer = PastelCard,
    onPrimaryContainer = PastelTextPrimary,
    secondary = PastelLavender,
    onSecondary = Color(0xFF000000),
    tertiary = PastelPeach,
    background = PastelDarkBg,
    onBackground = PastelTextPrimary,
    surface = PastelCard,
    onSurface = PastelTextPrimary,
    surfaceVariant = PastelCardElevated,
    onSurfaceVariant = PastelTextSecondary,
    outline = PastelBorder,
    error = PastelRose,
    onError = Color.White
)

@Composable
fun MusicplayerTheme(
    content: @Composable () -> Unit
) {
    val colorScheme = PastelColorScheme
    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            window.statusBarColor = PastelDarkBg.toArgb()
            window.navigationBarColor = PastelDarkBg.toArgb()
            WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = false
            WindowCompat.getInsetsController(window, view).isAppearanceLightNavigationBars = false
        }
    }

    MaterialTheme(
        colorScheme = colorScheme,
        content = content
    )
}

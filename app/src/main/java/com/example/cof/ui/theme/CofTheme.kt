package com.example.cof.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val OffWhite = Color(0xFFCCCCCC)

private val HighContrastDarkColorScheme = darkColorScheme(
    primary = OffWhite,
    onPrimary = Color.Black,
    primaryContainer = Color(0xFF303030),
    onPrimaryContainer = OffWhite,
    secondary = Color(0xFFCCCCCC),
    onSecondary = Color.Black,
    background = Color.Black,
    onBackground = OffWhite,
    surface = Color(0xFF121212),
    onSurface = OffWhite,
    surfaceVariant = Color(0xFF2A2A2A),
    onSurfaceVariant = Color(0xFFCCCCCC),
    outline = Color(0xFF888888),
    error = Color(0xFFFF5555),
    onError = Color.Black,
)

@Composable
fun CofTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = HighContrastDarkColorScheme,
        content = content,
    )
}

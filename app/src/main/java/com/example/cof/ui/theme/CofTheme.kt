package com.example.cof.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val HighContrastDarkColorScheme = darkColorScheme(
    primary = Color.White,
    onPrimary = Color.Black,
    primaryContainer = Color(0xFF303030),
    onPrimaryContainer = Color.White,
    secondary = Color(0xFFCCCCCC),
    onSecondary = Color.Black,
    background = Color.Black,
    onBackground = Color.White,
    surface = Color(0xFF121212),
    onSurface = Color.White,
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

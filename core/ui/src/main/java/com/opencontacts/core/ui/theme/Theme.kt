package com.opencontacts.core.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val LightColors = lightColorScheme(
    primary = Color(0xFF2563EB),
    onPrimary = Color.White,
    primaryContainer = Color(0xFFD9E2FF),
    onPrimaryContainer = Color(0xFF0A1F5A),
    secondary = Color(0xFF14B8A6),
    onSecondary = Color.White,
    tertiary = Color(0xFF8B5CF6),
    background = Color(0xFFF7F8FC),
    onBackground = Color(0xFF0F172A),
    surface = Color(0xFFFCFDFF),
    onSurface = Color(0xFF0F172A),
)

private val DarkColors = darkColorScheme(
    primary = Color(0xFF93C5FD),
    onPrimary = Color(0xFF0C1F63),
    primaryContainer = Color(0xFF2647C7),
    onPrimaryContainer = Color(0xFFE0E7FF),
    secondary = Color(0xFF5EEAD4),
    onSecondary = Color(0xFF003733),
    tertiary = Color(0xFFE9B6FF),
    background = Color(0xFF09101D),
    onBackground = Color(0xFFE5EAF4),
    surface = Color(0xFF111827),
    onSurface = Color(0xFFE5EAF4),
)

@Composable
fun OpenContactsTheme(
    themeMode: String = "SYSTEM",
    content: @Composable () -> Unit,
) {
    val useDarkTheme = when (themeMode.uppercase()) {
        "LIGHT" -> false
        "DARK" -> true
        else -> isSystemInDarkTheme()
    }

    MaterialTheme(
        colorScheme = if (useDarkTheme) DarkColors else LightColors,
        content = content,
    )
}

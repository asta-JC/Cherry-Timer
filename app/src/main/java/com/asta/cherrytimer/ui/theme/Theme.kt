package com.asta.cherrytimer.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.graphics.Color

private val LightColors = lightColorScheme(
    primary = Color(0xFFAF294A),
    onPrimary = Color(0xFFFFFFFF),
    primaryContainer = Color(0xFFF9DEE5),
    onPrimaryContainer = Color(0xFF652039),
    secondary = Color(0xFF78616B),
    onSecondary = Color(0xFFFFFFFF),
    secondaryContainer = Color(0xFFF1E7E7),
    onSecondaryContainer = Color(0xFF422731),
    tertiary = Color(0xFF698269),
    onTertiary = Color(0xFFFFFFFF),
    tertiaryContainer = Color(0xFFDEEADD),
    onTertiaryContainer = Color(0xFF19351D),
    background = Color(0xFFFCF8F5),
    onBackground = Color(0xFF422731),
    surface = Color(0xFFFFFCFA),
    onSurface = Color(0xFF422731),
    surfaceVariant = Color(0xFFF1E7E7),
    onSurfaceVariant = Color(0xFF78616B),
    outline = Color(0xFF927780),
    outlineVariant = Color(0xFFE9DDDF),
    surfaceContainerLowest = Color(0xFFFFFFFF),
    surfaceContainerLow = Color(0xFFFFFCFA),
    surfaceContainer = Color(0xFFF8F0EF),
    surfaceContainerHigh = Color(0xFFF1E7E7),
    surfaceContainerHighest = Color(0xFFE9DDDF),
    inverseSurface = Color(0xFF392A36),
    inverseOnSurface = Color(0xFFF9E8EE),
    inversePrimary = Color(0xFFFF9DB5),
    surfaceTint = Color(0xFFAF294A)
)

private val DarkColors = darkColorScheme(
    primary = Color(0xFFFF9DB5),
    onPrimary = Color(0xFF3B1528),
    primaryContainer = Color(0xFF502A3B),
    onPrimaryContainer = Color(0xFFFFD9E3),
    secondary = Color(0xFFCAB6C0),
    onSecondary = Color(0xFF3B1528),
    secondaryContainer = Color(0xFF392A36),
    onSecondaryContainer = Color(0xFFF9E8EE),
    tertiary = Color(0xFFB7D0AC),
    onTertiary = Color(0xFF19351D),
    tertiaryContainer = Color(0xFF304A32),
    onTertiaryContainer = Color(0xFFDEEADD),
    background = Color(0xFF1C151D),
    onBackground = Color(0xFFF9E8EE),
    surface = Color(0xFF261D27),
    onSurface = Color(0xFFF9E8EE),
    surfaceVariant = Color(0xFF392A36),
    onSurfaceVariant = Color(0xFFCAB6C0),
    outline = Color(0xFF9B828F),
    outlineVariant = Color(0xFF4B3845),
    surfaceContainerLowest = Color(0xFF181119),
    surfaceContainerLow = Color(0xFF221923),
    surfaceContainer = Color(0xFF2C212D),
    surfaceContainerHigh = Color(0xFF392A36),
    surfaceContainerHighest = Color(0xFF43323F),
    inverseSurface = Color(0xFFF1E7E7),
    inverseOnSurface = Color(0xFF422731),
    inversePrimary = Color(0xFFAF294A),
    surfaceTint = Color(0xFFFF9DB5)
)

val LocalAppearance = staticCompositionLocalOf { "system" }
val LocalSetAppearance = staticCompositionLocalOf<(String) -> Unit> { {} }

@Composable
fun CherryTimerTheme(darkTheme: Boolean = isSystemInDarkTheme(), content: @Composable () -> Unit) {
    MaterialTheme(colorScheme = if (darkTheme) DarkColors else LightColors, typography = Typography, content = content)
}

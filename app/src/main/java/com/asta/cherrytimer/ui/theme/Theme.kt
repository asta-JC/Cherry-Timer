package com.asta.cherrytimer.ui.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext

private val CherryColorScheme = darkColorScheme(
    primary = CherryPrimary,
    onPrimary = CherryMainText,
    primaryContainer = CherryPrimaryGradientStart,
    onPrimaryContainer = CherryMainText,
    secondary = CherryPrimary,
    onSecondary = CherryMainText,
    background = CherryBackground,
    onBackground = CherryMainText,
    surface = CherrySurface,
    onSurface = CherryMainText,
    surfaceVariant = CherrySurface,
    onSurfaceVariant = CherryMutedText,
    outline = CherryBorder,
    outlineVariant = CherryBorder
)

@Composable
fun CherryTimerTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = false,
    content: @Composable () -> Unit
) {
    val colorScheme = when {
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            val context = LocalContext.current
            if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        }
        else -> CherryColorScheme
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}

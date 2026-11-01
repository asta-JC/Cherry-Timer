package com.asta.cherrytimer

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.SystemBarStyle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.runtime.*
import com.asta.cherrytimer.navigation.AppNavGraph
import com.asta.cherrytimer.ui.theme.*

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val preferences = getSharedPreferences("appearance", MODE_PRIVATE)
        setContent {
            var appearance by remember { mutableStateOf(preferences.getString("mode", "system") ?: "system") }
            val dark = when (appearance) { "dark" -> true; "light" -> false; else -> isSystemInDarkTheme() }
            SideEffect {
                val style = if (dark) SystemBarStyle.dark(android.graphics.Color.TRANSPARENT)
                    else SystemBarStyle.light(android.graphics.Color.TRANSPARENT, android.graphics.Color.TRANSPARENT)
                enableEdgeToEdge(statusBarStyle = style, navigationBarStyle = style)
            }
            CompositionLocalProvider(LocalAppearance provides appearance, LocalSetAppearance provides { mode ->
                preferences.edit().putString("mode", mode).apply()
                appearance = mode
            }) {
                CherryTimerTheme(darkTheme = dark) { AppNavGraph() }
            }
        }
    }
}

package com.asta.cherrytimer.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.remember
import androidx.navigation3.runtime.NavEntry
import androidx.navigation3.ui.NavDisplay
import com.asta.cherrytimer.ui.screens.TimerScreen
import kotlinx.serialization.Serializable

@Serializable
data object TimerRoute

@Composable
fun AppNavGraph() {
    val backStack = remember { mutableStateListOf<Any>(TimerRoute) }

    NavDisplay(
        backStack = backStack,
        onBack = { backStack.removeLastOrNull() },
        entryProvider = { key ->
            when (key) {
                is TimerRoute -> NavEntry(key) {
                    TimerScreen()
                }
                else -> NavEntry(Unit) {
                    TimerScreen()
                }
            }
        }
    )
}

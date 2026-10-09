package com.asta.cherrytimer

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.asta.cherrytimer.navigation.AppNavGraph
import com.asta.cherrytimer.ui.theme.CherryTimerTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            CherryTimerTheme {
                AppNavGraph()
            }
        }
    }
}

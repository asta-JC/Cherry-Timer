package com.asta.cherrytimer.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowForward
import androidx.compose.material.icons.rounded.Pause
import androidx.compose.material.icons.rounded.PlayArrow
import androidx.compose.material.icons.rounded.Refresh
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.hapticfeedback.HapticFeedback
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.asta.cherrytimer.ui.theme.*
import com.asta.cherrytimer.util.CherryHapticsUtil
import com.asta.cherrytimer.viewmodel.PomodoroTimerViewModel
import com.asta.cherrytimer.viewmodel.PomodoroUiState
import com.asta.cherrytimer.viewmodel.TimerMode

/**
 * Clean, friendly Timer Controls Row.
 * Dynamically updates labels and icons according to timer state
 * (Start Focus / Start Break / Pause / Resume).
 */
@Composable
fun TimerControls(
    uiState: PomodoroUiState,
    viewModel: PomodoroTimerViewModel,
    haptic: HapticFeedback,
    modifier: Modifier = Modifier
) {
    val isPausedMidway = !uiState.isRunning && uiState.timeLeftSeconds < uiState.totalTimeSeconds

    val primaryActionText = when {
        uiState.isRunning -> "PAUSE"
        isPausedMidway -> "RESUME"
        uiState.currentMode == TimerMode.FOCUS -> "START FOCUS"
        else -> "START BREAK"
    }

    val primaryActionIcon = if (uiState.isRunning) {
        Icons.Rounded.Pause
    } else {
        Icons.Rounded.PlayArrow
    }

    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Reset Button
        IconButton(
            onClick = {
                CherryHapticsUtil.triggerClick(haptic)
                viewModel.resetTimer()
            },
            modifier = Modifier
                .size(56.dp)
                .background(CherrySurface, CircleShape)
                .border(1.dp, CherryBorder, CircleShape)
        ) {
            Icon(
                imageVector = Icons.Rounded.Refresh,
                contentDescription = "Reset timer",
                tint = CherryMainText,
                modifier = Modifier.size(22.dp)
            )
        }

        // Primary Action Button (Start / Pause / Resume)
        val gradientBrush = Brush.horizontalGradient(
            colors = listOf(CherryPrimaryGradientStart, CherryPrimaryGradientEnd)
        )

        Button(
            onClick = {
                CherryHapticsUtil.triggerClick(haptic)
                viewModel.toggleTimer()
            },
            modifier = Modifier
                .height(58.dp)
                .weight(1f)
                .shadow(elevation = 12.dp, shape = CircleShape, spotColor = CherryPrimary),
            colors = ButtonDefaults.buttonColors(containerColor = Color.Transparent),
            contentPadding = PaddingValues(0.dp),
            shape = CircleShape
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(gradientBrush),
                contentAlignment = Alignment.Center
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center
                ) {
                    Icon(
                        imageVector = primaryActionIcon,
                        contentDescription = primaryActionText,
                        tint = CherryMainText,
                        modifier = Modifier.size(24.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = primaryActionText,
                        color = CherryMainText,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.2.sp
                    )
                }
            }
        }

        // Skip Button
        IconButton(
            onClick = {
                CherryHapticsUtil.triggerClick(haptic)
                viewModel.skipTimer()
            },
            modifier = Modifier
                .size(56.dp)
                .background(CherrySurface, CircleShape)
                .border(1.dp, CherryBorder, CircleShape)
        ) {
            Icon(
                imageVector = Icons.AutoMirrored.Rounded.ArrowForward,
                contentDescription = "Skip to next session",
                tint = CherryMainText,
                modifier = Modifier.size(22.dp)
            )
        }
    }
}

package com.asta.cherrytimer.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.Remove
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
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
 * Quick Duration Presets and Stepper row.
 * Gives instant 1-tap access to popular durations (e.g., 15m, 25m, 45m, 60m)
 * and +/- quick minute nudges without having to dig through settings.
 */
@Composable
fun PresetChipsRow(
    uiState: PomodoroUiState,
    viewModel: PomodoroTimerViewModel,
    haptic: HapticFeedback,
    modifier: Modifier = Modifier
) {
    val presets = when (uiState.currentMode) {
        TimerMode.FOCUS -> listOf(15, 25, 45, 60)
        TimerMode.SHORT_BREAK -> listOf(3, 5, 10, 15)
        TimerMode.LONG_BREAK -> listOf(10, 15, 20, 30)
    }

    val currentDurationMinutes = when (uiState.currentMode) {
        TimerMode.FOCUS -> uiState.focusDurationMinutes
        TimerMode.SHORT_BREAK -> uiState.shortBreakDurationMinutes
        TimerMode.LONG_BREAK -> uiState.longBreakDurationMinutes
    }

    val stepMinutes = if (uiState.currentMode == TimerMode.SHORT_BREAK) 1 else 5

    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Quick Decrease Button (-5m or -1m)
        Box(
            modifier = Modifier
                .size(36.dp)
                .clip(CircleShape)
                .background(CherrySurface)
                .border(1.dp, CherryBorder, CircleShape)
                .clickable {
                    CherryHapticsUtil.triggerClick(haptic)
                    viewModel.adjustTime(-stepMinutes)
                },
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Rounded.Remove,
                contentDescription = "Decrease $stepMinutes minutes",
                tint = CherryMainText,
                modifier = Modifier.size(18.dp)
            )
        }

        Spacer(modifier = Modifier.width(8.dp))

        // Preset Chips
        Row(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            presets.forEach { minutes ->
                val isSelected = (!uiState.isRunning && uiState.timeLeftSeconds == minutes * 60) ||
                        (uiState.isRunning && currentDurationMinutes == minutes)

                val chipBg = if (isSelected) CherryPrimary.copy(alpha = 0.20f) else CherrySurface
                val chipBorder = if (isSelected) CherryPrimary else CherryBorder
                val chipText = if (isSelected) CherryPrimary else CherryMutedText

                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(12.dp))
                        .background(chipBg)
                        .border(1.dp, chipBorder, RoundedCornerShape(12.dp))
                        .clickable {
                            CherryHapticsUtil.triggerClick(haptic)
                            viewModel.setPresetDuration(minutes)
                        }
                        .padding(horizontal = 12.dp, vertical = 7.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "${minutes}m",
                        color = chipText,
                        fontSize = 12.sp,
                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium
                    )
                }
            }
        }

        Spacer(modifier = Modifier.width(8.dp))

        // Quick Increase Button (+5m or +1m)
        Box(
            modifier = Modifier
                .size(36.dp)
                .clip(CircleShape)
                .background(CherrySurface)
                .border(1.dp, CherryBorder, CircleShape)
                .clickable {
                    CherryHapticsUtil.triggerClick(haptic)
                    viewModel.adjustTime(stepMinutes)
                },
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Rounded.Add,
                contentDescription = "Increase $stepMinutes minutes",
                tint = CherryMainText,
                modifier = Modifier.size(18.dp)
            )
        }
    }
}

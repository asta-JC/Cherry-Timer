package com.asta.cherrytimer.ui.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.hapticfeedback.HapticFeedback
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.asta.cherrytimer.ui.theme.*
import com.asta.cherrytimer.util.CherryHapticsUtil
import com.asta.cherrytimer.viewmodel.PomodoroTimerViewModel
import com.asta.cherrytimer.viewmodel.TimerMode

/**
 * Modern, comfortable Mode Selector Pill Tab Bar.
 * Makes switching between Focus, Short Break, and Long Break effortless.
 */
@Composable
fun ModeSelector(
    currentMode: TimerMode,
    viewModel: PomodoroTimerViewModel,
    haptic: HapticFeedback,
    modifier: Modifier = Modifier
) {
    val modes = listOf(
        Triple(TimerMode.FOCUS, "Focus", "⏱️"),
        Triple(TimerMode.SHORT_BREAK, "Short Break", "☕"),
        Triple(TimerMode.LONG_BREAK, "Long Break", "🌴")
    )

    Row(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(20.dp))
            .background(CherrySurface)
            .border(1.dp, CherryBorder, RoundedCornerShape(20.dp))
            .padding(4.dp),
        horizontalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        modes.forEach { (mode, label, icon) ->
            val isSelected = currentMode == mode

            val animBgColor by animateColorAsState(
                targetValue = if (isSelected) CherryPrimary else Color.Transparent,
                animationSpec = tween(durationMillis = 200),
                label = "TabBgColor"
            )

            val animTextColor by animateColorAsState(
                targetValue = if (isSelected) CherryMainText else CherryMutedText,
                animationSpec = tween(durationMillis = 200),
                label = "TabTextColor"
            )

            Box(
                modifier = Modifier
                    .weight(1f)
                    .clip(RoundedCornerShape(16.dp))
                    .then(
                        if (isSelected) {
                            Modifier.background(
                                Brush.horizontalGradient(
                                    listOf(CherryPrimaryGradientStart, CherryPrimaryGradientEnd)
                                )
                            )
                        } else {
                            Modifier.background(animBgColor)
                        }
                    )
                    .clickable {
                        if (!isSelected) {
                            CherryHapticsUtil.triggerClick(haptic)
                            viewModel.switchMode(mode)
                        }
                    }
                    .padding(vertical = 10.dp),
                contentAlignment = Alignment.Center
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center
                ) {
                    Text(
                        text = icon,
                        fontSize = 13.sp
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = label,
                        color = animTextColor,
                        fontSize = 13.sp,
                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium
                    )
                }
            }
        }
    }
}

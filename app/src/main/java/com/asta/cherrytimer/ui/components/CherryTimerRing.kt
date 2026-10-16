package com.asta.cherrytimer.ui.components

import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.asta.cherrytimer.ui.theme.*
import com.asta.cherrytimer.viewmodel.PomodoroUiState
import com.asta.cherrytimer.viewmodel.TimerMode
import kotlin.math.cos
import kotlin.math.sin

/**
 * High-polish Cherry Timer Circular Progress Display.
 * Integrates the artistic Cherry artwork at the center with a crystal-clear
 * countdown timer, cycle indicator, and glowing circular progress ring.
 */
@Composable
fun CherryTimerRing(
    uiState: PomodoroUiState,
    timeFormatted: String,
    modifier: Modifier = Modifier,
    diameter: Dp = 270.dp,
    timeFontSize: TextUnit = 44.sp,
    onRingClick: () -> Unit
) {
    // Smooth progress animation so the arc moves with fluidity
    val targetProgress = if (uiState.totalTimeSeconds > 0) {
        (uiState.timeLeftSeconds.toFloat() / uiState.totalTimeSeconds.toFloat()).coerceIn(0f, 1f)
    } else 0f

    val animatedProgress by animateFloatAsState(
        targetValue = targetProgress,
        animationSpec = tween(durationMillis = 400, easing = LinearOutSlowInEasing),
        label = "RingProgressAnimation"
    )

    // Completion bounce animation when session finishes
    val bounceScale = remember { Animatable(1f) }
    LaunchedEffect(uiState.timeLeftSeconds) {
        if (uiState.timeLeftSeconds == 0) {
            bounceScale.animateTo(1.08f, spring(stiffness = Spring.StiffnessMedium))
            bounceScale.animateTo(1.0f, spring(stiffness = Spring.StiffnessLow))
        }
    }

    val modeTitle = when (uiState.currentMode) {
        TimerMode.FOCUS -> "FOCUS SESSION"
        TimerMode.SHORT_BREAK -> "SHORT BREAK"
        TimerMode.LONG_BREAK -> "LONG BREAK"
    }

    val ringColorStart = when (uiState.currentMode) {
        TimerMode.FOCUS -> CherryPrimaryGradientStart
        TimerMode.SHORT_BREAK -> Color(0xFFFF4081)
        TimerMode.LONG_BREAK -> Color(0xFFE040FB)
    }

    val ringColorEnd = when (uiState.currentMode) {
        TimerMode.FOCUS -> CherryPrimaryGradientEnd
        TimerMode.SHORT_BREAK -> CherryPrimary
        TimerMode.LONG_BREAK -> Color(0xFF9C27B0)
    }

    Box(
        modifier = modifier
            .size(diameter)
            .scale(bounceScale.value)
            .clip(CircleShape)
            .clickable(
                indication = null,
                interactionSource = remember { MutableInteractionSource() },
                onClick = onRingClick
            ),
        contentAlignment = Alignment.Center
    ) {
        // --- 1. Canvas Circular Progress Track & Head Cap ---
        Canvas(modifier = Modifier.fillMaxSize()) {
            val w = size.width
            val h = size.height
            val strokeWidthPx = 10.dp.toPx()
            val radius = (size.minDimension - strokeWidthPx) / 2f
            val center = Offset(w / 2f, h / 2f)

            // A) Background subtle glow behind entire circle
            if (uiState.isRunning) {
                drawCircle(
                    brush = Brush.radialGradient(
                        colors = listOf(
                            CherryGlow,
                            Color.Transparent
                        ),
                        center = center,
                        radius = radius * 1.05f
                    ),
                    radius = radius * 1.05f,
                    center = center
                )
            }

            // B) Inactive Background Track Ring
            drawCircle(
                color = CherryTrack,
                radius = radius,
                center = center,
                style = Stroke(width = strokeWidthPx, cap = StrokeCap.Round)
            )

            // C) 12 Subtle Watchmaker Graduation Dots around track
            for (i in 0 until 12) {
                val angleDeg = i * 30.0 - 90.0
                val angleRad = Math.toRadians(angleDeg)
                val dotX = (center.x + radius * cos(angleRad)).toFloat()
                val dotY = (center.y + radius * sin(angleRad)).toFloat()
                val isMajorTick = (i % 3 == 0)

                drawCircle(
                    color = if (isMajorTick) CherryBorder else CherryBorder.copy(alpha = 0.5f),
                    radius = if (isMajorTick) 2.2.dp.toPx() else 1.4.dp.toPx(),
                    center = Offset(dotX, dotY)
                )
            }

            // D) Active Progress Arc
            val sweepAngle = 360f * animatedProgress
            if (sweepAngle > 0.5f) {
                drawArc(
                    brush = Brush.sweepGradient(
                        colors = listOf(
                            ringColorStart,
                            ringColorEnd,
                            ringColorStart
                        ),
                        center = center
                    ),
                    startAngle = -90f,
                    sweepAngle = sweepAngle,
                    useCenter = false,
                    topLeft = Offset(center.x - radius, center.y - radius),
                    size = Size(radius * 2f, radius * 2f),
                    style = Stroke(width = strokeWidthPx, cap = StrokeCap.Round)
                )

                // E) Glowing Head Indicator Dot at the tip of progress arc
                val endAngleRad = Math.toRadians((sweepAngle - 90f).toDouble())
                val headX = (center.x + radius * cos(endAngleRad)).toFloat()
                val headY = (center.y + radius * sin(endAngleRad)).toFloat()

                // Outer soft glow for head
                drawCircle(
                    color = ringColorStart.copy(alpha = 0.6f),
                    radius = strokeWidthPx * 0.9f,
                    center = Offset(headX, headY)
                )
                // Crisp white-pink center dot
                drawCircle(
                    color = Color.White,
                    radius = strokeWidthPx * 0.45f,
                    center = Offset(headX, headY)
                )
            }
        }

        // --- 2. Centerpiece Content (Artwork + Clear Time + Status) ---
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
            modifier = Modifier.padding(horizontal = 24.dp)
        ) {
            // The centerpiece Cherry Artwork
            CherryArtwork(
                isRunning = uiState.isRunning,
                primaryColor = ringColorStart,
                glowEnabled = true,
                modifier = Modifier
                    .size(width = 96.dp, height = 76.dp)
            )

            Spacer(modifier = Modifier.height(2.dp))

            // Large, High-Contrast Countdown Time Display
            Text(
                text = timeFormatted,
                color = CherryMainText,
                fontSize = timeFontSize,
                fontWeight = FontWeight.Bold,
                fontFamily = FontFamily.Monospace,
                letterSpacing = 1.sp
            )

            Spacer(modifier = Modifier.height(2.dp))

            // Mode Badge
            Text(
                text = modeTitle,
                color = ringColorEnd,
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = 1.5.sp
            )

            Spacer(modifier = Modifier.height(6.dp))

            // Pomodoro Round Cycle Indicators (🍒 🍒 ⚪ ⚪)
            CycleCherryDots(
                currentRound = uiState.sessionCount,
                totalRounds = 4
            )

            Spacer(modifier = Modifier.height(3.dp))

            Text(
                text = "Round ${uiState.sessionCount} of 4",
                color = CherryMutedText,
                fontSize = 10.sp,
                fontWeight = FontWeight.Medium
            )
        }
    }
}

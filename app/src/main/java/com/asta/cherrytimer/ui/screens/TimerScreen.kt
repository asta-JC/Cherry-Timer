package com.asta.cherrytimer.ui.screens

import android.content.res.Configuration
import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowForward
import androidx.compose.material.icons.rounded.Clear
import androidx.compose.material.icons.rounded.History
import androidx.compose.material.icons.rounded.Notifications
import androidx.compose.material.icons.rounded.NotificationsOff
import androidx.compose.material.icons.rounded.Refresh
import androidx.compose.material.icons.rounded.Settings
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.asta.cherrytimer.ui.theme.*
import com.asta.cherrytimer.util.CherryHapticsUtil
import com.asta.cherrytimer.viewmodel.PomodoroSessionRecord
import com.asta.cherrytimer.viewmodel.PomodoroTimerViewModel
import com.asta.cherrytimer.viewmodel.PomodoroUiState
import com.asta.cherrytimer.viewmodel.TimerMode
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TimerScreen(
    viewModel: PomodoroTimerViewModel = viewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val haptic = LocalHapticFeedback.current
    val configuration = LocalConfiguration.current
    val isLandscape = configuration.orientation == Configuration.ORIENTATION_LANDSCAPE

    var showHistorySheet by remember { mutableStateOf(false) }
    var showSettingsSheet by remember { mutableStateOf(false) }

    val minutes = uiState.timeLeftSeconds / 60
    val seconds = uiState.timeLeftSeconds % 60
    val timeFormatted = String.format("%02d:%02d", minutes, seconds)
    val dateFormat = remember { SimpleDateFormat("MMM dd, HH:mm", Locale.getDefault()) }

    Scaffold(
        containerColor = CherryBackground,
        topBar = {
            TopAppBar(
                title = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        CherryAppIcon()
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text(
                                text = "CHERRY TIMER",
                                color = CherryMainText,
                                fontWeight = FontWeight.Bold,
                                fontSize = 16.sp,
                                fontFamily = FontFamily.Monospace,
                                letterSpacing = 2.sp
                            )
                            Text(
                                text = "Asta's project",
                                color = CherryMutedText,
                                fontSize = 11.sp
                            )
                        }
                    }
                },
                actions = {
                    IconButton(onClick = {
                        CherryHapticsUtil.triggerClick(haptic)
                        viewModel.toggleSound(!uiState.soundEnabled)
                    }) {
                        Icon(
                            imageVector = if (uiState.soundEnabled) Icons.Rounded.Notifications else Icons.Rounded.NotificationsOff,
                            contentDescription = "Toggle Sound",
                            tint = if (uiState.soundEnabled) CherryPrimary else CherryMutedText
                        )
                    }
                    IconButton(onClick = {
                        CherryHapticsUtil.triggerClick(haptic)
                        showSettingsSheet = true
                    }) {
                        Icon(
                            imageVector = Icons.Rounded.Settings,
                            contentDescription = "Settings",
                            tint = CherryMutedText
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = CherryBackground
                )
            )
        }
    ) { innerPadding ->
        BoxWithConstraints(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            val isWideScreen = maxWidth >= 600.dp || isLandscape

            if (isWideScreen && maxWidth >= 600.dp) {
                // Adaptive Two-Pane Side-by-Side Layout for Tablet / Foldable / Large Landscape
                Row(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(24.dp),
                    horizontalArrangement = Arrangement.spacedBy(24.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Left Pane: Timer & Controls
                    Column(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxHeight()
                            .verticalScroll(rememberScrollState()),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        ModeSelector(uiState.currentMode, viewModel, haptic)
                        TimerCircularDisplay(
                            uiState = uiState,
                            timeFormatted = timeFormatted,
                            circleSize = 220.dp,
                            canvasSize = 180.dp,
                            timeFontSize = 38.sp,
                            onClick = {
                                CherryHapticsUtil.triggerClick(haptic)
                                viewModel.toggleTimer()
                            }
                        )
                        TaskInputField(uiState.taskName, viewModel)
                        TimerControlsRow(viewModel, haptic)
                    }

                    // Right Pane: Stats & Session History List
                    Column(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxHeight()
                            .clip(RoundedCornerShape(24.dp))
                            .background(CherrySurface)
                            .border(1.5.dp, CherryBorder, RoundedCornerShape(24.dp))
                            .padding(20.dp),
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        Text(
                            text = "PRODUCTIVITY DASHBOARD",
                            color = CherryMutedText,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 1.5.sp,
                            fontFamily = FontFamily.Monospace
                        )
                        ProductivityStatsCard(
                            sessionsToday = uiState.sessionsCompletedToday,
                            totalMinutesToday = uiState.totalFocusMinutesToday,
                            streak = uiState.streak,
                            onHistoryClick = {
                                CherryHapticsUtil.triggerClick(haptic)
                                showHistorySheet = true
                            }
                        )
                        Text(
                            text = "Recent Sessions (${uiState.history.size})",
                            color = CherryMainText,
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold
                        )
                        if (uiState.history.isEmpty()) {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .weight(1f),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = "No focus sessions completed yet.",
                                    color = CherryMutedText,
                                    fontSize = 13.sp
                                )
                            }
                        } else {
                            LazyColumn(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .weight(1f),
                                verticalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                items(uiState.history) { session ->
                                    HistorySessionItem(session, dateFormat)
                                }
                            }
                        }
                    }
                }
            } else {
                // Standard Single-Column Layout for Mobile Portrait & Phone Landscape
                val circleSize = if (isLandscape) 180.dp else 250.dp
                val timeFontSize = if (isLandscape) 32.sp else 44.sp

                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .verticalScroll(rememberScrollState())
                        .imePadding()
                        .padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    ModeSelector(uiState.currentMode, viewModel, haptic)

                    ProductivityStatsCard(
                        sessionsToday = uiState.sessionsCompletedToday,
                        totalMinutesToday = uiState.totalFocusMinutesToday,
                        streak = uiState.streak,
                        onHistoryClick = {
                            CherryHapticsUtil.triggerClick(haptic)
                            showHistorySheet = true
                        }
                    )

                    TimerCircularDisplay(
                        uiState = uiState,
                        timeFormatted = timeFormatted,
                        circleSize = circleSize,
                        canvasSize = circleSize,
                        timeFontSize = timeFontSize,
                        onClick = {
                            CherryHapticsUtil.triggerClick(haptic)
                            viewModel.toggleTimer()
                        }
                    )

                    TaskInputField(uiState.taskName, viewModel)

                    TimerControlsRow(viewModel, haptic)
                }
            }
        }

        // History Bottom Sheet
        if (showHistorySheet) {
            HistoryBottomSheet(
                history = uiState.history,
                dateFormat = dateFormat,
                onDismiss = { showHistorySheet = false }
            )
        }

        // Settings Bottom Sheet
        if (showSettingsSheet) {
            SettingsBottomSheet(
                focusDuration = uiState.focusDurationMinutes,
                shortBreakDuration = uiState.shortBreakDurationMinutes,
                longBreakDuration = uiState.longBreakDurationMinutes,
                autoSwitch = uiState.autoSwitchModes,
                soundEnabled = uiState.soundEnabled,
                onDurationsChanged = { f, s, l -> viewModel.setDurations(f, s, l) },
                onAutoSwitchChanged = { viewModel.toggleAutoSwitch(it) },
                onSoundChanged = { viewModel.toggleSound(it) },
                onDismiss = { showSettingsSheet = false }
            )
        }
    }
}

@Composable
fun ModeSelector(
    currentMode: TimerMode,
    viewModel: PomodoroTimerViewModel,
    haptic: androidx.compose.ui.hapticfeedback.HapticFeedback
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(24.dp))
            .background(CherrySurface)
            .border(1.5.dp, CherryBorder, RoundedCornerShape(24.dp))
            .padding(4.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        val modes = listOf(
            TimerMode.FOCUS to "FOCUS",
            TimerMode.SHORT_BREAK to "SHORT BREAK",
            TimerMode.LONG_BREAK to "LONG BREAK"
        )
        modes.forEach { (mode, label) ->
            val isActive = currentMode == mode
            val backgroundColor = if (isActive) CherryPrimary else Color.Transparent
            val textColor = if (isActive) CherryMainText else CherryMutedText

            Box(
                modifier = Modifier
                    .weight(1f)
                    .clip(RoundedCornerShape(20.dp))
                    .background(backgroundColor)
                    .clickable {
                        CherryHapticsUtil.triggerClick(haptic)
                        viewModel.switchMode(mode)
                    }
                    .padding(vertical = 10.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = label,
                    color = textColor,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 0.5.sp
                )
            }
        }
    }
}

/**
 * TimerCircularDisplay acts as a wrapper around [CherryTimerView] so it drops
 * directly into the existing TimerScreen layout without shifting any screen elements.
 */
@Composable
fun TimerCircularDisplay(
    uiState: PomodoroUiState,
    timeFormatted: String,
    circleSize: Dp = 250.dp,
    @Suppress("UNUSED_PARAMETER") canvasSize: Dp = 200.dp,
    timeFontSize: TextUnit = 44.sp,
    onClick: () -> Unit
) {
    val progress = if (uiState.totalTimeSeconds > 0) {
        uiState.timeLeftSeconds.toFloat() / uiState.totalTimeSeconds.toFloat()
    } else 0f

    val modeLabel = when (uiState.currentMode) {
        TimerMode.FOCUS -> "FOCUS SESSION"
        TimerMode.SHORT_BREAK -> "SHORT BREAK"
        TimerMode.LONG_BREAK -> "LONG BREAK"
    }

    val statusText = if (uiState.currentMode == TimerMode.FOCUS) {
        if (uiState.isRunning) "Running" else "Paused"
    } else {
        if (uiState.isRunning) "Resting..." else "Paused"
    }

    CherryTimerView(
        progress = progress,
        timeText = timeFormatted,
        statusText = statusText,
        isRunning = uiState.isRunning,
        currentMode = uiState.currentMode,
        modeLabel = modeLabel,
        timeFontSize = timeFontSize,
        onClick = onClick,
        modifier = Modifier.size(circleSize)
    )
}

/**
 * Mode colors data holder for clean palette switching per timer mode.
 */
private data class CherryModePalette(
    val gradientStart: Color,
    val gradientEnd: Color,
    val darkShadow: Color,
    val trackOutline: Color
)

/**
 * Reusable Cherry-shaped Timer view rendered entirely in Jetpack Compose Canvas.
 *
 * @param progress Float value from 0f to 1f indicating timer progress remaining.
 * @param timeText Formatted text for remaining time (e.g., "05:00").
 * @param statusText Status description (e.g., "Paused" / "Running").
 * @param isRunning True if timer is running, triggering stem sway & glow pulse animations.
 * @param modifier Layout modifier for sizing and padding.
 * @param currentMode Current TimerMode (FOCUS, SHORT_BREAK, LONG_BREAK) for custom theme colors.
 * @param modeLabel Title string displayed above time (e.g., "FOCUS SESSION").
 * @param timeFontSize Text size for the main timer readout.
 * @param onClick Callback when the cherry timer is tapped.
 */
@Composable
fun CherryTimerView(
    progress: Float,
    timeText: String,
    statusText: String,
    isRunning: Boolean,
    modifier: Modifier = Modifier,
    currentMode: TimerMode = TimerMode.FOCUS,
    modeLabel: String = "FOCUS SESSION",
    timeFontSize: TextUnit = 44.sp,
    onClick: () -> Unit = {}
) {
    // -------------------------------------------------------------
    // ANIMATIONS
    // -------------------------------------------------------------

    // 1. Stem sway animation: Gently moves top joint left and right while running
    val infiniteTransition = rememberInfiniteTransition(label = "CherrySwayTransition")
    val swayOffsetPx by if (isRunning) {
        infiniteTransition.animateFloat(
            initialValue = -10f,
            targetValue = 10f,
            animationSpec = infiniteRepeatable(
                animation = tween(durationMillis = 2200, easing = FastOutSlowInEasing),
                repeatMode = RepeatMode.Reverse
            ),
            label = "StemSwayAnimation"
        )
    } else {
        remember { mutableStateOf(0f) }
    }

    // 2. Glow pulse animation: Soft glow breathing effect while running, calm glow when paused
    val glowAlpha by if (isRunning) {
        infiniteTransition.animateFloat(
            initialValue = 0.35f,
            targetValue = 0.80f,
            animationSpec = infiniteRepeatable(
                animation = tween(durationMillis = 1600, easing = LinearEasing),
                repeatMode = RepeatMode.Reverse
            ),
            label = "GlowPulseAnimation"
        )
    } else {
        remember { mutableStateOf(0.25f) }
    }

    // 3. Pop bounce animation when session finishes (progress reaching 0)
    val bounceScale = remember { Animatable(1f) }
    LaunchedEffect(progress) {
        if (progress <= 0f) {
            bounceScale.animateTo(1.14f, spring(stiffness = Spring.StiffnessMedium))
            bounceScale.animateTo(1f, spring(stiffness = Spring.StiffnessLow))
        }
    }

    // -------------------------------------------------------------
    // COLOR PALETTE PER MODE
    // -------------------------------------------------------------
    val palette = when (currentMode) {
        TimerMode.FOCUS -> CherryModePalette(
            gradientStart = Color(0xFFFF1744), // Glossy Cherry Red
            gradientEnd = Color(0xFFFF2D6B),   // Signature Cherry Pink-Red
            darkShadow = Color(0xFF700018),    // Deep Red Shadow
            trackOutline = Color(0xFF4A0E17)   // Dim Dark Red Track
        )
        TimerMode.SHORT_BREAK -> CherryModePalette(
            gradientStart = Color(0xFFFF4081), // Light Pink-Red
            gradientEnd = Color(0xFFFF2D6B),   // Vibrant Pink-Red
            darkShadow = Color(0xFF880E4F),    // Deep Pink Shadow
            trackOutline = Color(0xFF4A0E2E)   // Dim Dark Pink Track
        )
        TimerMode.LONG_BREAK -> CherryModePalette(
            gradientStart = Color(0xFFE040FB), // Magenta/Berry Highlight
            gradientEnd = Color(0xFF8E24AA),   // Deep Berry Purple
            darkShadow = Color(0xFF4A148C),    // Dark Berry Shadow
            trackOutline = Color(0xFF380B24)   // Dim Dark Berry Track
        )
    }

    // Container Box applying scale bounce, clipping, and tap click listener
    Box(
        modifier = modifier
            .graphicsLayer {
                scaleX = bounceScale.value
                scaleY = bounceScale.value
            }
            .clip(CircleShape)
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        // -------------------------------------------------------------
        // CANVAS DRAWING CHERRY GRAPHICS
        // -------------------------------------------------------------
        Canvas(modifier = Modifier.fillMaxSize()) {
            val canvasW = size.width
            val canvasH = size.height

            // --- GEOMETRY POSITIONS ---
            // Main Cherry (Front Right Cherry where timer text sits)
            val mainCx = canvasW * 0.52f
            val mainCy = canvasH * 0.58f
            val mainRadius = canvasW * 0.32f

            // Second Cherry (Smaller Back Left Cherry forming a pair)
            val secondCx = canvasW * 0.28f
            val secondCy = canvasH * 0.50f
            val secondRadius = canvasW * 0.21f

            // Stem Joint Point at top (Where curved stems join together)
            val stemJointX = canvasW * 0.50f + swayOffsetPx
            val stemJointY = canvasH * 0.11f

            // -------------------------------------------------------------
            // DRAWING STEP 1: BACKGROUND GLOW
            // Soft glowing aura behind the cherries on dark futuristic background
            // -------------------------------------------------------------
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(
                        palette.gradientEnd.copy(alpha = glowAlpha * 0.55f),
                        palette.gradientStart.copy(alpha = glowAlpha * 0.20f),
                        Color.Transparent
                    ),
                    center = Offset(mainCx, mainCy),
                    radius = mainRadius * 1.50f
                ),
                radius = mainRadius * 1.50f,
                center = Offset(mainCx, mainCy)
            )

            // -------------------------------------------------------------
            // DRAWING STEP 2: SECOND CHERRY (Back Cherry)
            // Drawn first so main cherry sits naturally in front of it
            // -------------------------------------------------------------
            // A) 3D Radial Shaded Body
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(
                        palette.gradientStart.copy(alpha = 0.95f),
                        palette.gradientEnd,
                        palette.darkShadow
                    ),
                    center = Offset(secondCx - secondRadius * 0.35f, secondCy - secondRadius * 0.35f),
                    radius = secondRadius * 1.25f
                ),
                radius = secondRadius,
                center = Offset(secondCx, secondCy)
            )
            // B) Glossy Shine Spot on Second Cherry
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(
                        Color.White.copy(alpha = 0.55f),
                        Color.White.copy(alpha = 0.0f)
                    ),
                    center = Offset(secondCx - secondRadius * 0.38f, secondCy - secondRadius * 0.38f),
                    radius = secondRadius * 0.42f
                ),
                radius = secondRadius * 0.42f,
                center = Offset(secondCx - secondRadius * 0.38f, secondCy - secondRadius * 0.38f)
            )

            // -------------------------------------------------------------
            // DRAWING STEP 3: CURVED GREEN STEMS
            // Two thin green curved stem paths joining at the top joint
            // -------------------------------------------------------------
            val stemColor = Color(0xFF4CAF50) // Fresh Stem Green
            val stemThicknessPx = 3.5.dp.toPx()

            // Stem 1: Connects top of Main Cherry to top joint
            val stem1Path = Path().apply {
                moveTo(mainCx - canvasW * 0.02f, mainCy - mainRadius * 0.88f)
                quadraticTo(
                    canvasW * 0.62f + swayOffsetPx * 0.5f,
                    canvasH * 0.32f,
                    stemJointX,
                    stemJointY
                )
            }
            drawPath(
                path = stem1Path,
                color = stemColor,
                style = Stroke(width = stemThicknessPx, cap = StrokeCap.Round)
            )

            // Stem 2: Connects top of Second Cherry to top joint
            val stem2Path = Path().apply {
                moveTo(secondCx, secondCy - secondRadius * 0.88f)
                quadraticTo(
                    canvasW * 0.32f + swayOffsetPx * 0.5f,
                    canvasH * 0.26f,
                    stemJointX,
                    stemJointY
                )
            }
            drawPath(
                path = stem2Path,
                color = stemColor,
                style = Stroke(width = stemThicknessPx, cap = StrokeCap.Round)
            )

            // -------------------------------------------------------------
            // DRAWING STEP 4: GREEN LEAF
            // Attached near stem top joint, stays green in all modes
            // -------------------------------------------------------------
            val leafPath = Path().apply {
                moveTo(stemJointX, stemJointY)
                cubicTo(
                    stemJointX + canvasW * 0.12f, stemJointY - canvasH * 0.06f,
                    stemJointX + canvasW * 0.22f, stemJointY - canvasH * 0.02f,
                    stemJointX + canvasW * 0.18f, stemJointY + canvasH * 0.05f
                )
                cubicTo(
                    stemJointX + canvasW * 0.10f, stemJointY + canvasH * 0.06f,
                    stemJointX + canvasW * 0.04f, stemJointY + canvasH * 0.03f,
                    stemJointX, stemJointY
                )
            }
            // Fill Leaf with Gradient Green
            drawPath(
                path = leafPath,
                brush = Brush.linearGradient(
                    colors = listOf(Color(0xFF81C784), Color(0xFF2E7D32)),
                    start = Offset(stemJointX, stemJointY),
                    end = Offset(stemJointX + canvasW * 0.18f, stemJointY + canvasH * 0.05f)
                )
            )
            // Leaf Center Vein
            val veinPath = Path().apply {
                moveTo(stemJointX, stemJointY)
                quadraticTo(
                    stemJointX + canvasW * 0.10f, stemJointY + canvasH * 0.01f,
                    stemJointX + canvasW * 0.16f, stemJointY + canvasH * 0.02f
                )
            }
            drawPath(
                path = veinPath,
                color = Color(0xFF1B5E20),
                style = Stroke(width = 1.5.dp.toPx(), cap = StrokeCap.Round)
            )

            // -------------------------------------------------------------
            // DRAWING STEP 5: MAIN CHERRY BODY (Front Cherry)
            // -------------------------------------------------------------
            // A) Rich 3D Radial Gradient Shading
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(
                        palette.gradientStart,
                        palette.gradientEnd,
                        palette.darkShadow
                    ),
                    center = Offset(mainCx - mainRadius * 0.35f, mainCy - mainRadius * 0.35f),
                    radius = mainRadius * 1.25f
                ),
                radius = mainRadius,
                center = Offset(mainCx, mainCy)
            )

            // B) Subtle Indentation Notch at Top of Main Cherry
            val notchPath = Path().apply {
                moveTo(mainCx - mainRadius * 0.25f, mainCy - mainRadius * 0.85f)
                quadraticTo(
                    mainCx, mainCy - mainRadius * 0.70f,
                    mainCx + mainRadius * 0.25f, mainCy - mainRadius * 0.85f
                )
            }
            drawPath(
                path = notchPath,
                color = palette.darkShadow.copy(alpha = 0.6f),
                style = Stroke(width = 2.5.dp.toPx(), cap = StrokeCap.Round)
            )

            // C) Glossy White Highlight Spot
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(
                        Color.White.copy(alpha = 0.65f),
                        Color.White.copy(alpha = 0.0f)
                    ),
                    center = Offset(mainCx - mainRadius * 0.42f, mainCy - mainRadius * 0.42f),
                    radius = mainRadius * 0.42f
                ),
                radius = mainRadius * 0.42f,
                center = Offset(mainCx - mainRadius * 0.42f, mainCy - mainRadius * 0.42f)
            )

            // -------------------------------------------------------------
            // DRAWING STEP 6: TIMING PROGRESS OUTLINE STROKE
            // Follows contour around main cherry body with rounded stroke caps
            // -------------------------------------------------------------
            val strokePx = 8.dp.toPx()
            val outlineRadius = mainRadius + strokePx * 0.6f

            // A) Dim Track Outline
            drawArc(
                color = palette.trackOutline,
                startAngle = -90f,
                sweepAngle = 360f,
                useCenter = false,
                topLeft = Offset(mainCx - outlineRadius, mainCy - outlineRadius),
                size = Size(outlineRadius * 2, outlineRadius * 2),
                style = Stroke(width = strokePx, cap = StrokeCap.Round)
            )

            // B) Glowing Active Progress Arc Filling Clockwise
            val sweepAngle = 360f * progress.coerceIn(0f, 1f)
            drawArc(
                brush = Brush.sweepGradient(
                    colors = listOf(
                        palette.gradientStart,
                        palette.gradientEnd,
                        Color.White
                    ),
                    center = Offset(mainCx, mainCy)
                ),
                startAngle = -90f,
                sweepAngle = sweepAngle,
                useCenter = false,
                topLeft = Offset(mainCx - outlineRadius, mainCy - outlineRadius),
                size = Size(outlineRadius * 2, outlineRadius * 2),
                style = Stroke(width = strokePx, cap = StrokeCap.Round)
            )
        }

        // -------------------------------------------------------------
        // DRAWING STEP 7: TIMER TEXT OVERLAY (Centered inside main cherry)
        // -------------------------------------------------------------
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
            modifier = Modifier
                .offset(x = 5.dp, y = 14.dp) // Aligns with main cherry center
        ) {
            if (modeLabel.isNotEmpty()) {
                Text(
                    text = modeLabel,
                    color = CherryMutedText,
                    fontSize = 10.sp,
                    fontWeight = FontWeight.SemiBold,
                    letterSpacing = 1.5.sp
                )
                Spacer(modifier = Modifier.height(2.dp))
            }

            Text(
                text = timeText,
                color = CherryMainText,
                fontSize = timeFontSize,
                fontWeight = FontWeight.Bold,
                fontFamily = FontFamily.Monospace
            )

            Spacer(modifier = Modifier.height(2.dp))

            Text(
                text = statusText,
                color = palette.gradientEnd,
                fontSize = 11.sp,
                fontWeight = FontWeight.Medium
            )
        }
    }
}

@Composable
fun TaskInputField(taskName: String, viewModel: PomodoroTimerViewModel) {
    val keyboardController = LocalSoftwareKeyboardController.current
    OutlinedTextField(
        value = taskName,
        onValueChange = { viewModel.setTaskName(it) },
        placeholder = {
            Text(
                text = "What are you focusing on?",
                color = CherryMutedText,
                fontSize = 14.sp
            )
        },
        trailingIcon = {
            if (taskName.isNotEmpty()) {
                IconButton(onClick = { viewModel.setTaskName("") }) {
                    Icon(
                        imageVector = Icons.Rounded.Clear,
                        contentDescription = "Clear task",
                        tint = CherryMutedText
                    )
                }
            }
        },
        keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
        keyboardActions = KeyboardActions(onDone = { keyboardController?.hide() }),
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(CherrySurface)
            .border(1.5.dp, CherryBorder, RoundedCornerShape(16.dp)),
        colors = OutlinedTextFieldDefaults.colors(
            focusedContainerColor = CherrySurface,
            unfocusedContainerColor = CherrySurface,
            disabledContainerColor = CherrySurface,
            focusedBorderColor = CherryPrimary,
            unfocusedBorderColor = Color.Transparent,
            focusedTextColor = CherryMainText,
            unfocusedTextColor = CherryMainText,
            cursorColor = CherryPrimary
        ),
        singleLine = true,
        shape = RoundedCornerShape(16.dp)
    )
}

@Composable
fun TimerControlsRow(
    viewModel: PomodoroTimerViewModel,
    haptic: androidx.compose.ui.hapticfeedback.HapticFeedback
) {
    val uiState by viewModel.uiState.collectAsState()
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        IconButton(
            onClick = {
                CherryHapticsUtil.triggerClick(haptic)
                viewModel.resetTimer()
            },
            modifier = Modifier
                .size(56.dp)
                .background(CherrySurface, CircleShape)
                .border(1.5.dp, CherryBorder, CircleShape)
        ) {
            Icon(
                imageVector = Icons.Rounded.Refresh,
                contentDescription = "Reset",
                tint = CherryMainText
            )
        }

        val gradientBrush = Brush.horizontalGradient(
            colors = listOf(CherryPrimaryGradientStart, CherryPrimaryGradientEnd)
        )

        Button(
            onClick = {
                CherryHapticsUtil.triggerClick(haptic)
                viewModel.toggleTimer()
            },
            modifier = Modifier
                .height(56.dp)
                .weight(1f)
                .shadow(12.dp, CircleShape, spotColor = CherryPrimary),
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
                Text(
                    text = if (uiState.isRunning) "PAUSE" else "START FOCUS",
                    color = CherryMainText,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.5.sp
                )
            }
        }

        IconButton(
            onClick = {
                CherryHapticsUtil.triggerClick(haptic)
                viewModel.skipTimer()
            },
            modifier = Modifier
                .size(56.dp)
                .background(CherrySurface, CircleShape)
                .border(1.5.dp, CherryBorder, CircleShape)
        ) {
            Icon(
                imageVector = Icons.AutoMirrored.Rounded.ArrowForward,
                contentDescription = "Skip",
                tint = CherryMainText
            )
        }
    }
}

@Composable
fun ProductivityStatsCard(
    sessionsToday: Int,
    totalMinutesToday: Int,
    streak: Int,
    onHistoryClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(20.dp))
            .background(CherrySurface)
            .border(1.5.dp, CherryBorder, RoundedCornerShape(20.dp))
            .padding(16.dp)
    ) {
        Column(modifier = Modifier.fillMaxWidth()) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "TODAY'S PRODUCTIVITY",
                    color = CherryMutedText,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.5.sp,
                    fontFamily = FontFamily.Monospace
                )
                IconButton(
                    onClick = onHistoryClick,
                    modifier = Modifier.size(32.dp)
                ) {
                    Icon(
                        imageVector = Icons.Rounded.History,
                        contentDescription = "Session History",
                        tint = CherryPrimary,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                StatItem(
                    label = "Sessions",
                    value = sessionsToday.toString(),
                    unit = ""
                )
                StatItem(
                    label = "Focus Time",
                    value = totalMinutesToday.toString(),
                    unit = "m"
                )
                StatItem(
                    label = "Streak",
                    value = streak.toString(),
                    unit = "d 🔥"
                )
            }
        }
    }
}

@Composable
fun StatItem(
    label: String,
    value: String,
    unit: String
) {
    Column(horizontalAlignment = Alignment.Start) {
        Row(verticalAlignment = Alignment.Bottom) {
            Text(
                text = value,
                color = CherryMainText,
                fontSize = 22.sp,
                fontWeight = FontWeight.Bold,
                fontFamily = FontFamily.Monospace
            )
            if (unit.isNotEmpty()) {
                Spacer(modifier = Modifier.width(2.dp))
                Text(
                    text = unit,
                    color = CherryPrimary,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }
        Spacer(modifier = Modifier.height(2.dp))
        Text(
            text = label,
            color = CherryMutedText,
            fontSize = 11.sp,
            fontWeight = FontWeight.Medium
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HistoryBottomSheet(
    history: List<PomodoroSessionRecord>,
    dateFormat: SimpleDateFormat,
    onDismiss: () -> Unit
) {
    val sheetState = rememberModalBottomSheetState()

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = CherrySurface,
        contentColor = CherryMainText,
        dragHandle = { BottomSheetDefaults.DragHandle(color = CherryBorder) }
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(24.dp)
                .navigationBarsPadding()
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Session History",
                    color = CherryMainText,
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "${history.size} total",
                    color = CherryMutedText,
                    fontSize = 14.sp
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            if (history.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(200.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(
                            imageVector = Icons.Rounded.History,
                            contentDescription = null,
                            tint = CherryMutedText,
                            modifier = Modifier.size(48.dp)
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "No focus sessions completed yet.",
                            color = CherryMutedText,
                            fontSize = 14.sp
                        )
                    }
                }
            } else {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(max = 400.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(history) { session ->
                        HistorySessionItem(session, dateFormat)
                    }
                }
            }
            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}

@Composable
fun HistorySessionItem(session: PomodoroSessionRecord, dateFormat: SimpleDateFormat) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(CherryBackground)
            .border(1.5.dp, CherryBorder, RoundedCornerShape(16.dp))
            .padding(16.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = session.taskName,
                    color = CherryMainText,
                    fontWeight = FontWeight.Bold,
                    fontSize = 15.sp
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = dateFormat.format(Date(session.timestamp)),
                    color = CherryMutedText,
                    fontSize = 12.sp
                )
            }
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(10.dp))
                    .background(CherryPrimary.copy(alpha = 0.15f))
                    .padding(horizontal = 10.dp, vertical = 6.dp)
            ) {
                Text(
                    text = "${session.durationMinutes} min",
                    color = CherryPrimary,
                    fontWeight = FontWeight.Bold,
                    fontSize = 13.sp
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsBottomSheet(
    focusDuration: Int,
    shortBreakDuration: Int,
    longBreakDuration: Int,
    autoSwitch: Boolean,
    soundEnabled: Boolean,
    onDurationsChanged: (Int, Int, Int) -> Unit,
    onAutoSwitchChanged: (Boolean) -> Unit,
    onSoundChanged: (Boolean) -> Unit,
    onDismiss: () -> Unit
) {
    val sheetState = rememberModalBottomSheetState()

    var focusDur by remember { mutableStateOf(focusDuration.toFloat()) }
    var shortDur by remember { mutableStateOf(shortBreakDuration.toFloat()) }
    var longDur by remember { mutableStateOf(longBreakDuration.toFloat()) }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = CherrySurface,
        contentColor = CherryMainText,
        dragHandle = { BottomSheetDefaults.DragHandle(color = CherryBorder) }
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(24.dp)
                .navigationBarsPadding(),
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {
            Text(
                text = "Timer Settings",
                color = CherryMainText,
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold
            )

            // Focus Duration
            Column {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = "Focus Duration",
                        color = CherryMainText,
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 15.sp
                    )
                    Text(
                        text = "${focusDur.toInt()} mins",
                        color = CherryPrimary,
                        fontWeight = FontWeight.Bold,
                        fontSize = 15.sp
                    )
                }
                Slider(
                    value = focusDur,
                    onValueChange = {
                        focusDur = it
                        onDurationsChanged(focusDur.toInt(), shortDur.toInt(), longDur.toInt())
                    },
                    valueRange = 5f..60f,
                    steps = 10,
                    colors = SliderDefaults.colors(
                        thumbColor = CherryPrimary,
                        activeTrackColor = CherryPrimary,
                        inactiveTrackColor = CherryBorder
                    )
                )
            }

            // Short Break Duration
            Column {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = "Short Break Duration",
                        color = CherryMainText,
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 15.sp
                    )
                    Text(
                        text = "${shortDur.toInt()} mins",
                        color = CherryPrimary,
                        fontWeight = FontWeight.Bold,
                        fontSize = 15.sp
                    )
                }
                Slider(
                    value = shortDur,
                    onValueChange = {
                        shortDur = it
                        onDurationsChanged(focusDur.toInt(), shortDur.toInt(), longDur.toInt())
                    },
                    valueRange = 1f..15f,
                    steps = 13,
                    colors = SliderDefaults.colors(
                        thumbColor = CherryPrimary,
                        activeTrackColor = CherryPrimary,
                        inactiveTrackColor = CherryBorder
                    )
                )
            }

            // Long Break Duration
            Column {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = "Long Break Duration",
                        color = CherryMainText,
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 15.sp
                    )
                    Text(
                        text = "${longDur.toInt()} mins",
                        color = CherryPrimary,
                        fontWeight = FontWeight.Bold,
                        fontSize = 15.sp
                    )
                }
                Slider(
                    value = longDur,
                    onValueChange = {
                        longDur = it
                        onDurationsChanged(focusDur.toInt(), shortDur.toInt(), longDur.toInt())
                    },
                    valueRange = 5f..30f,
                    steps = 24,
                    colors = SliderDefaults.colors(
                        thumbColor = CherryPrimary,
                        activeTrackColor = CherryPrimary,
                        inactiveTrackColor = CherryBorder
                    )
                )
            }

            // Auto-Switch Modes
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "Auto-Switch Modes",
                        color = CherryMainText,
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 15.sp
                    )
                    Text(
                        text = "Automatically start next session/break",
                        color = CherryMutedText,
                        fontSize = 12.sp
                    )
                }
                Switch(
                    checked = autoSwitch,
                    onCheckedChange = onAutoSwitchChanged,
                    colors = SwitchDefaults.colors(
                        checkedThumbColor = CherryMainText,
                        checkedTrackColor = CherryPrimary,
                        uncheckedThumbColor = CherryMutedText,
                        uncheckedTrackColor = CherryBorder
                    )
                )
            }

            // Sound Alerts
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "Sound Alerts",
                        color = CherryMainText,
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 15.sp
                    )
                    Text(
                        text = "Play chime when timer ends",
                        color = CherryMutedText,
                        fontSize = 12.sp
                    )
                }
                Switch(
                    checked = soundEnabled,
                    onCheckedChange = onSoundChanged,
                    colors = SwitchDefaults.colors(
                        checkedThumbColor = CherryMainText,
                        checkedTrackColor = CherryPrimary,
                        uncheckedThumbColor = CherryMutedText,
                        uncheckedTrackColor = CherryBorder
                    )
                )
            }

            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}

@Composable
fun CherryAppIcon(modifier: Modifier = Modifier) {
    Box(
        modifier = modifier
            .size(40.dp)
            .clip(RoundedCornerShape(12.dp))
            .background(CherrySurface)
            .border(1.5.dp, CherryBorder, RoundedCornerShape(12.dp)),
        contentAlignment = Alignment.Center
    ) {
        Canvas(modifier = Modifier.size(24.dp)) {
            val width = size.width
            val height = size.height
            // Draw cherry 1 (left)
            drawCircle(
                color = CherryPrimary,
                radius = width * 0.28f,
                center = Offset(width * 0.32f, height * 0.7f)
            )
            // Draw cherry 2 (right)
            drawCircle(
                color = CherryPrimaryGradientStart,
                radius = width * 0.28f,
                center = Offset(width * 0.72f, height * 0.75f)
            )
            // Draw stems
            val path = Path().apply {
                moveTo(width * 0.5f, height * 0.15f)
                quadraticTo(width * 0.35f, height * 0.35f, width * 0.32f, height * 0.7f)
                moveTo(width * 0.5f, height * 0.15f)
                quadraticTo(width * 0.65f, height * 0.4f, width * 0.72f, height * 0.75f)
            }
            drawPath(
                path = path,
                color = Color(0xFF4CAF50),
                style = Stroke(width = 3f, cap = StrokeCap.Round)
            )
            // Leaf
            drawCircle(
                color = Color(0xFF81C784),
                radius = width * 0.12f,
                center = Offset(width * 0.55f, height * 0.2f)
            )
        }
    }
}

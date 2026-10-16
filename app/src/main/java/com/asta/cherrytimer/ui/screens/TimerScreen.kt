package com.asta.cherrytimer.ui.screens

import android.content.res.Configuration
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.History
import androidx.compose.material.icons.rounded.Notifications
import androidx.compose.material.icons.rounded.NotificationsOff
import androidx.compose.material.icons.rounded.Settings
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.asta.cherrytimer.ui.components.*
import com.asta.cherrytimer.ui.theme.*
import com.asta.cherrytimer.util.CherryHapticsUtil
import com.asta.cherrytimer.viewmodel.PomodoroTimerViewModel
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
                        Box(
                            modifier = Modifier
                                .size(38.dp)
                                .clip(RoundedCornerShape(12.dp))
                                .background(CherrySurface)
                                .border(1.dp, CherryBorder, RoundedCornerShape(12.dp)),
                            contentAlignment = Alignment.Center
                        ) {
                            MiniCherryIcon(size = 24.dp)
                        }
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
                                text = "Stay Focused & Fresh",
                                color = CherryMutedText,
                                fontSize = 11.sp
                            )
                        }
                    }
                },
                actions = {
                    // Sound Toggle
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

                    // History Button
                    IconButton(onClick = {
                        CherryHapticsUtil.triggerClick(haptic)
                        showHistorySheet = true
                    }) {
                        Icon(
                            imageVector = Icons.Rounded.History,
                            contentDescription = "Session History",
                            tint = CherryMutedText
                        )
                    }

                    // Settings Button
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
            val isWideScreen = maxWidth >= 600.dp || (isLandscape && maxHeight < 500.dp)

            if (maxWidth >= 600.dp) {
                // Adaptive Two-Pane Layout for Foldable & Tablet
                Row(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(24.dp),
                    horizontalArrangement = Arrangement.spacedBy(24.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Left Pane: Hero Timer, Presets & Controls
                    Column(
                        modifier = Modifier
                            .weight(1.1f)
                            .fillMaxHeight()
                            .verticalScroll(rememberScrollState()),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        ModeSelector(uiState.currentMode, viewModel, haptic)

                        CherryTimerRing(
                            uiState = uiState,
                            timeFormatted = timeFormatted,
                            diameter = 260.dp,
                            timeFontSize = 42.sp,
                            onRingClick = {
                                CherryHapticsUtil.triggerClick(haptic)
                                viewModel.toggleTimer()
                            }
                        )

                        PresetChipsRow(uiState, viewModel, haptic)
                        TaskGoalCard(uiState.taskName, viewModel, haptic)
                        TimerControls(uiState, viewModel, haptic)
                    }

                    // Right Pane: Dashboard Stats & Recent Sessions
                    Column(
                        modifier = Modifier
                            .weight(0.9f)
                            .fillMaxHeight()
                            .clip(RoundedCornerShape(24.dp))
                            .background(CherrySurface)
                            .border(1.dp, CherryBorder, RoundedCornerShape(24.dp))
                            .padding(20.dp),
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        ProductivityCard(
                            sessionsToday = uiState.sessionsCompletedToday,
                            totalMinutesToday = uiState.totalFocusMinutesToday,
                            streak = uiState.streak,
                            onHistoryClick = { showHistorySheet = true },
                            haptic = haptic
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
                                    HistoryItemRow(session, dateFormat)
                                }
                            }
                        }
                    }
                }
            } else {
                // Single Column Layout for Standard Phones (Portrait & Phone Landscape)
                val ringDiameter = if (isLandscape) 210.dp else 265.dp
                val timeFontSize = if (isLandscape) 36.sp else 44.sp

                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .verticalScroll(rememberScrollState())
                        .imePadding()
                        .padding(horizontal = 20.dp, vertical = 12.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    // 1. Mode Pill Tabs
                    ModeSelector(uiState.currentMode, viewModel, haptic)

                    // 2. Central Hero Cherry Timer Ring with beautiful cherry artwork
                    CherryTimerRing(
                        uiState = uiState,
                        timeFormatted = timeFormatted,
                        diameter = ringDiameter,
                        timeFontSize = timeFontSize,
                        onRingClick = {
                            CherryHapticsUtil.triggerClick(haptic)
                            viewModel.toggleTimer()
                        }
                    )

                    // 3. One-Tap Quick Duration Presets & Stepper
                    PresetChipsRow(uiState, viewModel, haptic)

                    // 4. Focus Goal Card with quick suggestions dialog
                    TaskGoalCard(uiState.taskName, viewModel, haptic)

                    // 5. Unambiguous, clear Timer Action Controls (Start / Pause / Resume / Skip / Reset)
                    TimerControls(uiState, viewModel, haptic)

                    // 6. Productivity Dashboard Card (Sessions, Focus Time, Streak 🔥)
                    ProductivityCard(
                        sessionsToday = uiState.sessionsCompletedToday,
                        totalMinutesToday = uiState.totalFocusMinutesToday,
                        streak = uiState.streak,
                        onHistoryClick = { showHistorySheet = true },
                        haptic = haptic
                    )

                    Spacer(modifier = Modifier.height(16.dp))
                }
            }
        }

        // Session History Bottom Sheet
        if (showHistorySheet) {
            HistorySheet(
                history = uiState.history,
                dateFormat = dateFormat,
                onClearHistory = { viewModel.clearHistory() },
                onDismiss = { showHistorySheet = false }
            )
        }

        // Settings Bottom Sheet
        if (showSettingsSheet) {
            SettingsSheet(
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

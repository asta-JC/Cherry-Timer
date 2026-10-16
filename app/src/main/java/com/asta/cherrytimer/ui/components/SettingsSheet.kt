package com.asta.cherrytimer.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.Notifications
import androidx.compose.material.icons.rounded.Remove
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.asta.cherrytimer.ui.theme.*
import com.asta.cherrytimer.util.CherryAudioUtil

/**
 * Clean, user-friendly Timer Settings Bottom Sheet.
 * Replaces finicky sliders with intuitive steppers, preset pills,
 * and audio chime testing.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsSheet(
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
    val context = LocalContext.current
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    var focus by remember { mutableIntStateOf(focusDuration) }
    var shortBreak by remember { mutableIntStateOf(shortBreakDuration) }
    var longBreak by remember { mutableIntStateOf(longBreakDuration) }

    fun notifyChanged(f: Int = focus, s: Int = shortBreak, l: Int = longBreak) {
        onDurationsChanged(f, s, l)
    }

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
                .padding(horizontal = 24.dp)
                .padding(bottom = 32.dp)
                .navigationBarsPadding()
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Timer Settings",
                    color = CherryMainText,
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold
                )
            }

            // --- 1. Focus Duration Stepper ---
            DurationSettingCard(
                title = "Focus Duration",
                subtitle = "Length of each deep work session",
                currentMinutes = focus,
                step = 5,
                range = 5..120,
                presetOptions = listOf(15, 25, 30, 45, 60),
                onValueChange = {
                    focus = it
                    notifyChanged(f = it)
                }
            )

            // --- 2. Short Break Stepper ---
            DurationSettingCard(
                title = "Short Break Duration",
                subtitle = "Quick breather between focus sessions",
                currentMinutes = shortBreak,
                step = 1,
                range = 1..30,
                presetOptions = listOf(3, 5, 10, 15),
                onValueChange = {
                    shortBreak = it
                    notifyChanged(s = it)
                }
            )

            // --- 3. Long Break Stepper ---
            DurationSettingCard(
                title = "Long Break Duration",
                subtitle = "Extended rest after completing 4 rounds",
                currentMinutes = longBreak,
                step = 5,
                range = 5..60,
                presetOptions = listOf(10, 15, 20, 30),
                onValueChange = {
                    longBreak = it
                    notifyChanged(l = it)
                }
            )

            HorizontalDivider(color = CherryBorder.copy(alpha = 0.5f))

            // --- 4. Auto-Switch Modes ---
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "Auto-Start Next Round",
                        color = CherryMainText,
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 15.sp
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = "Automatically starts next session or break without waiting",
                        color = CherryMutedText,
                        fontSize = 12.sp
                    )
                }
                Spacer(modifier = Modifier.width(16.dp))
                Switch(
                    checked = autoSwitch,
                    onCheckedChange = onAutoSwitchChanged,
                    colors = SwitchDefaults.colors(
                        checkedThumbColor = CherryMainText,
                        checkedTrackColor = CherryPrimary,
                        uncheckedThumbColor = CherryMutedText,
                        uncheckedTrackColor = CherryBackground
                    )
                )
            }

            // --- 5. Sound Alerts & Test Sound ---
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "Sound Chimes",
                        color = CherryMainText,
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 15.sp
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = "Play melodic chime when timer completes",
                        color = CherryMutedText,
                        fontSize = 12.sp
                    )
                }
                Spacer(modifier = Modifier.width(16.dp))
                Switch(
                    checked = soundEnabled,
                    onCheckedChange = onSoundChanged,
                    colors = SwitchDefaults.colors(
                        checkedThumbColor = CherryMainText,
                        checkedTrackColor = CherryPrimary,
                        uncheckedThumbColor = CherryMutedText,
                        uncheckedTrackColor = CherryBackground
                    )
                )
            }

            if (soundEnabled) {
                OutlinedButton(
                    onClick = { CherryAudioUtil.playChime(context) },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(14.dp),
                    border = androidx.compose.foundation.BorderStroke(1.dp, CherryBorder),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = CherryPrimary)
                ) {
                    Icon(
                        imageVector = Icons.Rounded.Notifications,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Test Chime Sound",
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 13.sp
                    )
                }
            }
        }
    }
}

@Composable
private fun DurationSettingCard(
    title: String,
    subtitle: String,
    currentMinutes: Int,
    step: Int,
    range: IntRange,
    presetOptions: List<Int>,
    onValueChange: (Int) -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(CherryBackground)
            .border(1.dp, CherryBorder, RoundedCornerShape(16.dp))
            .padding(16.dp)
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = title,
                        color = CherryMainText,
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 14.sp
                    )
                    Text(
                        text = subtitle,
                        color = CherryMutedText,
                        fontSize = 11.sp
                    )
                }

                // Stepper Row
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(32.dp)
                            .clip(CircleShape)
                            .background(CherrySurface)
                            .border(1.dp, CherryBorder, CircleShape)
                            .clickable {
                                val next = (currentMinutes - step).coerceIn(range.first, range.last)
                                onValueChange(next)
                            },
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.Remove,
                            contentDescription = "Decrease",
                            tint = CherryMainText,
                            modifier = Modifier.size(16.dp)
                        )
                    }

                    Text(
                        text = "${currentMinutes}m",
                        color = CherryPrimary,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.widthIn(min = 36.dp)
                    )

                    Box(
                        modifier = Modifier
                            .size(32.dp)
                            .clip(CircleShape)
                            .background(CherrySurface)
                            .border(1.dp, CherryBorder, CircleShape)
                            .clickable {
                                val next = (currentMinutes + step).coerceIn(range.first, range.last)
                                onValueChange(next)
                            },
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.Add,
                            contentDescription = "Increase",
                            tint = CherryMainText,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }
            }

            // Quick preset pills
            Row(
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                presetOptions.forEach { preset ->
                    val isSelected = currentMinutes == preset
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .background(if (isSelected) CherryPrimary else CherrySurface)
                            .border(1.dp, if (isSelected) CherryPrimary else CherryBorder, RoundedCornerShape(8.dp))
                            .clickable { onValueChange(preset) }
                            .padding(horizontal = 10.dp, vertical = 5.dp)
                    ) {
                        Text(
                            text = "${preset}m",
                            color = if (isSelected) CherryMainText else CherryMutedText,
                            fontSize = 11.sp,
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium
                        )
                    }
                }
            }
        }
    }
}

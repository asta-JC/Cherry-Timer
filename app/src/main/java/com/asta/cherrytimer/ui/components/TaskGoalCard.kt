package com.asta.cherrytimer.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Check
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material.icons.rounded.Edit
import androidx.compose.material.icons.rounded.TrackChanges
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.hapticfeedback.HapticFeedback
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.asta.cherrytimer.ui.theme.*
import com.asta.cherrytimer.util.CherryHapticsUtil
import com.asta.cherrytimer.viewmodel.PomodoroTimerViewModel

/**
 * Comfortable, elegant Focus Task Goal Pill with interactive quick presets dialog.
 */
@Composable
fun TaskGoalCard(
    taskName: String,
    viewModel: PomodoroTimerViewModel,
    haptic: HapticFeedback,
    modifier: Modifier = Modifier
) {
    var showDialog by remember { mutableStateOf(false) }

    Box(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(CherrySurface)
            .border(1.dp, CherryBorder, RoundedCornerShape(16.dp))
            .clickable {
                CherryHapticsUtil.triggerClick(haptic)
                showDialog = true
            }
            .padding(horizontal = 16.dp, vertical = 12.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.weight(1f)
            ) {
                Icon(
                    imageVector = Icons.Rounded.TrackChanges,
                    contentDescription = null,
                    tint = CherryPrimary,
                    modifier = Modifier.size(18.dp)
                )

                Spacer(modifier = Modifier.width(10.dp))

                if (taskName.isNotBlank()) {
                    Text(
                        text = taskName,
                        color = CherryMainText,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.SemiBold,
                        maxLines = 1
                    )
                } else {
                    Text(
                        text = "Set a focus goal (e.g. Study, Code)...",
                        color = CherryMutedText,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Normal
                    )
                }
            }

            if (taskName.isNotBlank()) {
                IconButton(
                    onClick = {
                        CherryHapticsUtil.triggerClick(haptic)
                        viewModel.setTaskName("")
                    },
                    modifier = Modifier.size(24.dp)
                ) {
                    Icon(
                        imageVector = Icons.Rounded.Close,
                        contentDescription = "Clear task",
                        tint = CherryMutedText,
                        modifier = Modifier.size(16.dp)
                    )
                }
            } else {
                Icon(
                    imageVector = Icons.Rounded.Edit,
                    contentDescription = "Edit goal",
                    tint = CherryMutedText,
                    modifier = Modifier.size(16.dp)
                )
            }
        }
    }

    if (showDialog) {
        TaskGoalDialog(
            currentTask = taskName,
            onDismiss = { showDialog = false },
            onSave = {
                viewModel.setTaskName(it)
                showDialog = false
            }
        )
    }
}

@Composable
private fun TaskGoalDialog(
    currentTask: String,
    onDismiss: () -> Unit,
    onSave: (String) -> Unit
) {
    var text by remember { mutableStateOf(currentTask) }
    val keyboardController = LocalSoftwareKeyboardController.current

    val quickGoals = listOf(
        "📚 Study", "💻 Coding", "📖 Reading",
        "✍️ Writing", "🧠 Deep Work", "🧹 Review"
    )

    Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = RoundedCornerShape(24.dp),
            color = CherrySurface,
            border = androidx.compose.foundation.BorderStroke(1.5.dp, CherryBorder),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Focus Goal",
                        color = CherryMainText,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold
                    )
                    IconButton(
                        onClick = onDismiss,
                        modifier = Modifier.size(28.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.Close,
                            contentDescription = "Close",
                            tint = CherryMutedText
                        )
                    }
                }

                // Quick preset goal chips
                Text(
                    text = "QUICK SUGGESTIONS",
                    color = CherryMutedText,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.sp
                )

                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    quickGoals.forEach { goal ->
                        val cleanedGoal = goal.substringAfter(" ")
                        val isSelected = text.equals(cleanedGoal, ignoreCase = true)

                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(10.dp))
                                .background(if (isSelected) CherryPrimary else CherryBackground)
                                .border(1.dp, if (isSelected) CherryPrimary else CherryBorder, RoundedCornerShape(10.dp))
                                .clickable { text = cleanedGoal }
                                .padding(horizontal = 10.dp, vertical = 6.dp)
                        ) {
                            Text(
                                text = goal,
                                color = if (isSelected) CherryMainText else CherryMutedText,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Medium
                            )
                        }
                    }
                }

                // Custom input field
                OutlinedTextField(
                    value = text,
                    onValueChange = { text = it },
                    placeholder = {
                        Text(
                            text = "Or type custom goal...",
                            color = CherryMutedText,
                            fontSize = 14.sp
                        )
                    },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
                    keyboardActions = KeyboardActions(onDone = {
                        keyboardController?.hide()
                        onSave(text.trim())
                    }),
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(14.dp)),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedContainerColor = CherryBackground,
                        unfocusedContainerColor = CherryBackground,
                        focusedBorderColor = CherryPrimary,
                        unfocusedBorderColor = CherryBorder,
                        focusedTextColor = CherryMainText,
                        unfocusedTextColor = CherryMainText,
                        cursorColor = CherryPrimary
                    )
                )

                // Action buttons
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    if (text.isNotBlank()) {
                        OutlinedButton(
                            onClick = { onSave("") },
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(14.dp),
                            border = androidx.compose.foundation.BorderStroke(1.dp, CherryBorder),
                            colors = ButtonDefaults.outlinedButtonColors(contentColor = CherryMutedText)
                        ) {
                            Text("Clear")
                        }
                    }

                    Button(
                        onClick = { onSave(text.trim()) },
                        modifier = Modifier.weight(1.5f),
                        shape = RoundedCornerShape(14.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = CherryPrimary)
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.Check,
                            contentDescription = null,
                            tint = CherryMainText,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "Save Goal",
                            color = CherryMainText,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }
    }
}

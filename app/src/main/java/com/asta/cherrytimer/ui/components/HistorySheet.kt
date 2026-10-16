package com.asta.cherrytimer.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.DeleteOutline
import androidx.compose.material.icons.rounded.History
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.asta.cherrytimer.ui.theme.*
import com.asta.cherrytimer.viewmodel.PomodoroSessionRecord
import java.text.SimpleDateFormat
import java.util.*

/**
 * Session History Bottom Sheet.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HistorySheet(
    history: List<PomodoroSessionRecord>,
    dateFormat: SimpleDateFormat,
    onClearHistory: () -> Unit,
    onDismiss: () -> Unit
) {
    val sheetState = rememberModalBottomSheetState()
    var showClearConfirm by remember { mutableStateOf(false) }

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
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    MiniCherryIcon(size = 22.dp)
                    Spacer(modifier = Modifier.width(10.dp))
                    Text(
                        text = "Session History",
                        color = CherryMainText,
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold
                    )
                }

                if (history.isNotEmpty()) {
                    IconButton(
                        onClick = { showClearConfirm = true },
                        modifier = Modifier.size(32.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.DeleteOutline,
                            contentDescription = "Clear history",
                            tint = CherryMutedText
                        )
                    }
                }
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
                        MiniCherryIcon(size = 48.dp)
                        Spacer(modifier = Modifier.height(12.dp))
                        Text(
                            text = "No focus sessions completed yet.",
                            color = CherryMutedText,
                            fontSize = 14.sp
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "Complete your first session to see it here!",
                            color = CherryMutedText.copy(alpha = 0.7f),
                            fontSize = 12.sp
                        )
                    }
                }
            } else {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(max = 420.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    items(history) { session ->
                        HistoryItemRow(session, dateFormat)
                    }
                }
            }
        }
    }

    if (showClearConfirm) {
        AlertDialog(
            onDismissRequest = { showClearConfirm = false },
            containerColor = CherrySurface,
            title = {
                Text(text = "Clear History?", color = CherryMainText, fontWeight = FontWeight.Bold)
            },
            text = {
                Text(
                    text = "Are you sure you want to delete all completed session records?",
                    color = CherryMutedText
                )
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        onClearHistory()
                        showClearConfirm = false
                    }
                ) {
                    Text("Clear", color = CherryPrimary, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showClearConfirm = false }) {
                    Text("Cancel", color = CherryMutedText)
                }
            }
        )
    }
}

@Composable
fun HistoryItemRow(session: PomodoroSessionRecord, dateFormat: SimpleDateFormat) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(CherryBackground)
            .border(1.dp, CherryBorder, RoundedCornerShape(16.dp))
            .padding(14.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = session.taskName.ifBlank { "Focus Session" },
                    color = CherryMainText,
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = dateFormat.format(Date(session.timestamp)),
                    color = CherryMutedText,
                    fontSize = 11.sp
                )
            }

            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(10.dp))
                    .background(CherryPrimary.copy(alpha = 0.15f))
                    .border(1.dp, CherryPrimary.copy(alpha = 0.3f), RoundedCornerShape(10.dp))
                    .padding(horizontal = 10.dp, vertical = 5.dp)
            ) {
                Text(
                    text = "${session.durationMinutes}m",
                    color = CherryPrimary,
                    fontWeight = FontWeight.Bold,
                    fontSize = 12.sp
                )
            }
        }
    }
}

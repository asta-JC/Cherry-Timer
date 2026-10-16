package com.asta.cherrytimer.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.KeyboardArrowRight
import androidx.compose.material.icons.rounded.History
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.hapticfeedback.HapticFeedback
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.asta.cherrytimer.ui.theme.*
import com.asta.cherrytimer.util.CherryHapticsUtil

/**
 * Compact, comfortable Today's Productivity Dashboard Card.
 */
@Composable
fun ProductivityCard(
    sessionsToday: Int,
    totalMinutesToday: Int,
    streak: Int,
    onHistoryClick: () -> Unit,
    haptic: HapticFeedback,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(20.dp))
            .background(CherrySurface)
            .border(1.dp, CherryBorder, RoundedCornerShape(20.dp))
            .clickable {
                CherryHapticsUtil.triggerClick(haptic)
                onHistoryClick()
            }
            .padding(16.dp)
    ) {
        Column(modifier = Modifier.fillMaxWidth()) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    MiniCherryIcon(size = 18.dp)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "TODAY'S PRODUCTIVITY",
                        color = CherryMutedText,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.2.sp,
                        fontFamily = FontFamily.Monospace
                    )
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = "History",
                        color = CherryPrimary,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                    Icon(
                        imageVector = Icons.AutoMirrored.Rounded.KeyboardArrowRight,
                        contentDescription = "View History",
                        tint = CherryPrimary,
                        modifier = Modifier.size(16.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                // Sessions
                ProductivityStat(
                    label = "Sessions",
                    value = sessionsToday.toString(),
                    unit = "/ 4"
                )

                // Divider line
                Box(
                    modifier = Modifier
                        .height(36.dp)
                        .width(1.dp)
                        .background(CherryBorder.copy(alpha = 0.5f))
                )

                // Focus Time
                ProductivityStat(
                    label = "Focus Time",
                    value = totalMinutesToday.toString(),
                    unit = "min"
                )

                // Divider line
                Box(
                    modifier = Modifier
                        .height(36.dp)
                        .width(1.dp)
                        .background(CherryBorder.copy(alpha = 0.5f))
                )

                // Streak
                ProductivityStat(
                    label = "Day Streak",
                    value = streak.toString(),
                    unit = "🔥"
                )
            }
        }
    }
}

@Composable
private fun ProductivityStat(
    label: String,
    value: String,
    unit: String
) {
    Column(horizontalAlignment = Alignment.Start) {
        Row(verticalAlignment = Alignment.Bottom) {
            Text(
                text = value,
                color = CherryMainText,
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                fontFamily = FontFamily.Monospace
            )
            if (unit.isNotEmpty()) {
                Spacer(modifier = Modifier.width(3.dp))
                Text(
                    text = unit,
                    color = CherryPrimary,
                    fontSize = 12.sp,
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

package com.asta.cherrytimer.ui.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.asta.cherrytimer.R

@Composable
fun CherryBrand(modifier: Modifier = Modifier) {
    Image(painterResource(if (MaterialTheme.colorScheme.background.luminance() < 0.5f)
        R.drawable.cherryapps_logo_full_dark else R.drawable.cherryapps_logo_full),
        contentDescription = null, modifier = modifier)
}

@Composable
fun CycleCherryDots(currentRound: Int, totalRounds: Int = 4) {
    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        repeat(totalRounds) { index ->
            Box(Modifier.size(if (index + 1 == currentRound) 10.dp else 8.dp)
                .background(if (index + 1 <= currentRound) MaterialTheme.colorScheme.primary
                    else MaterialTheme.colorScheme.outlineVariant, CircleShape))
        }
    }
}

@Composable
fun MiniCherryIcon(size: Dp = 24.dp, modifier: Modifier = Modifier) {
    CherryBrand(modifier.size(size))
}

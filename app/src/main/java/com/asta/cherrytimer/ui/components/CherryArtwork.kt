package com.asta.cherrytimer.ui.components

import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.asta.cherrytimer.ui.theme.*

/**
 * Beautiful, artistic Cherry Centerpiece drawn entirely in Compose Canvas.
 * Features realistic 3D radial shading, glossy specular highlights,
 * curved organic green stems, two-tone leaf with center vein,
 * and gentle lifelike breathing & sway animations when running.
 */
@Composable
fun CherryArtwork(
    isRunning: Boolean,
    modifier: Modifier = Modifier,
    primaryColor: Color = CherryPrimary,
    glowEnabled: Boolean = true,
    onClick: (() -> Unit)? = null
) {
    val infiniteTransition = rememberInfiniteTransition(label = "CherryTransition")

    // 1. Stem and leaf sway animation
    val swayOffset by if (isRunning) {
        infiniteTransition.animateFloat(
            initialValue = -5f,
            targetValue = 5f,
            animationSpec = infiniteRepeatable(
                animation = tween(durationMillis = 2000, easing = FastOutSlowInEasing),
                repeatMode = RepeatMode.Reverse
            ),
            label = "StemSway"
        )
    } else {
        remember { Animatable(0f) }.asState()
    }

    // 2. Ambient glow pulsing
    val glowAlpha by if (isRunning && glowEnabled) {
        infiniteTransition.animateFloat(
            initialValue = 0.25f,
            targetValue = 0.70f,
            animationSpec = infiniteRepeatable(
                animation = tween(durationMillis = 1800, easing = LinearEasing),
                repeatMode = RepeatMode.Reverse
            ),
            label = "GlowPulse"
        )
    } else {
        remember { Animatable(if (glowEnabled) 0.30f else 0f) }.asState()
    }

    // 3. Subtle breathing scale pulse for the cherries
    val breathingScale by if (isRunning) {
        infiniteTransition.animateFloat(
            initialValue = 1.0f,
            targetValue = 1.035f,
            animationSpec = infiniteRepeatable(
                animation = tween(durationMillis = 1800, easing = FastOutSlowInEasing),
                repeatMode = RepeatMode.Reverse
            ),
            label = "BreathingScale"
        )
    } else {
        remember { Animatable(1.0f) }.asState()
    }

    val clickableModifier = if (onClick != null) {
        modifier.clickable(onClick = onClick)
    } else modifier

    Box(
        modifier = clickableModifier
            .scale(breathingScale),
        contentAlignment = Alignment.Center
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val w = size.width
            val h = size.height

            // Common dimensions
            val cx1 = w * 0.62f
            val cy1 = h * 0.68f
            val r1 = w * 0.23f // Front Right Cherry (hero)

            val cx2 = w * 0.38f
            val cy2 = h * 0.63f
            val r2 = w * 0.19f // Back Left Cherry

            val apexX = w * 0.50f + swayOffset
            val apexY = h * 0.14f // Stem apex joint

            // --- 1. Ambient Glow Aura behind cherries ---
            if (glowEnabled) {
                drawCircle(
                    brush = Brush.radialGradient(
                        colors = listOf(
                            primaryColor.copy(alpha = glowAlpha * 0.50f),
                            primaryColor.copy(alpha = glowAlpha * 0.15f),
                            Color.Transparent
                        ),
                        center = Offset(w * 0.50f, h * 0.65f),
                        radius = w * 0.48f
                    ),
                    radius = w * 0.48f,
                    center = Offset(w * 0.50f, h * 0.65f)
                )
            }

            // --- 2. Back / Left Cherry ---
            // A) 3D Radial Sphere Shading
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(
                        Color(0xFFFF5277), // Highlight red-pink
                        Color(0xFFFF1744), // Crimson
                        Color(0xFFAD1457), // Deep berry
                        Color(0xFF380010)  // Deep wine shadow
                    ),
                    center = Offset(cx2 - r2 * 0.30f, cy2 - r2 * 0.30f),
                    radius = r2 * 1.30f
                ),
                radius = r2,
                center = Offset(cx2, cy2)
            )

            // B) Top Pit Indentation Notch on Back Cherry
            val notchBack = Path().apply {
                moveTo(cx2 - r2 * 0.22f, cy2 - r2 * 0.82f)
                quadraticTo(cx2, cy2 - r2 * 0.68f, cx2 + r2 * 0.22f, cy2 - r2 * 0.82f)
            }
            drawPath(
                path = notchBack,
                color = Color(0xFF28000B).copy(alpha = 0.7f),
                style = Stroke(width = 1.8.dp.toPx(), cap = StrokeCap.Round)
            )

            // C) Glossy Specular Highlight Spot
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(
                        Color.White.copy(alpha = 0.70f),
                        Color.White.copy(alpha = 0.0f)
                    ),
                    center = Offset(cx2 - r2 * 0.38f, cy2 - r2 * 0.38f),
                    radius = r2 * 0.38f
                ),
                radius = r2 * 0.38f,
                center = Offset(cx2 - r2 * 0.38f, cy2 - r2 * 0.38f)
            )

            // --- 3. Curved Fresh Green Stems ---
            val stemThickness = 2.8.dp.toPx()

            // Stem to Back Cherry
            val stemBack = Path().apply {
                moveTo(cx2, cy2 - r2 * 0.75f)
                quadraticTo(
                    w * 0.36f + swayOffset * 0.4f,
                    h * 0.34f,
                    apexX,
                    apexY
                )
            }
            drawPath(
                path = stemBack,
                color = CherryLeafGreen,
                style = Stroke(width = stemThickness, cap = StrokeCap.Round)
            )

            // Stem to Front Cherry
            val stemFront = Path().apply {
                moveTo(cx1, cy1 - r1 * 0.75f)
                quadraticTo(
                    w * 0.63f + swayOffset * 0.5f,
                    h * 0.36f,
                    apexX,
                    apexY
                )
            }
            drawPath(
                path = stemFront,
                color = CherryLeafGreen,
                style = Stroke(width = stemThickness, cap = StrokeCap.Round)
            )

            // Apex joint node
            drawCircle(
                color = CherryLeafDark,
                radius = 2.2.dp.toPx(),
                center = Offset(apexX, apexY)
            )

            // --- 4. Lush Green Leaf ---
            val leafPath = Path().apply {
                moveTo(apexX, apexY)
                cubicTo(
                    apexX + w * 0.15f, apexY - h * 0.10f,
                    apexX + w * 0.26f, apexY - h * 0.07f,
                    apexX + w * 0.30f, apexY - h * 0.01f
                )
                cubicTo(
                    apexX + w * 0.24f, apexY + h * 0.07f,
                    apexX + w * 0.11f, apexY + h * 0.06f,
                    apexX, apexY
                )
            }

            // Two-tone leaf gradient fill
            drawPath(
                path = leafPath,
                brush = Brush.linearGradient(
                    colors = listOf(CherryLeafLight, CherryLeafDark),
                    start = Offset(apexX, apexY - h * 0.05f),
                    end = Offset(apexX + w * 0.30f, apexY)
                )
            )

            // Center leaf vein
            val veinPath = Path().apply {
                moveTo(apexX, apexY)
                quadraticTo(
                    apexX + w * 0.14f, apexY - h * 0.01f,
                    apexX + w * 0.24f, apexY - h * 0.01f
                )
            }
            drawPath(
                path = veinPath,
                color = Color(0xFF1B5E20),
                style = Stroke(width = 1.3.dp.toPx(), cap = StrokeCap.Round)
            )

            // --- 5. Front / Right Cherry (Main Hero) ---
            // A) Rich 3D Spherical Shading
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(
                        Color(0xFFFF5C8A), // Bright neon pink-red
                        Color(0xFFFF1744), // Glossy ruby red
                        Color(0xFFC2185B), // Raspberry
                        Color(0xFF4A0015)  // Deep shadow burgundy
                    ),
                    center = Offset(cx1 - r1 * 0.30f, cy1 - r1 * 0.32f),
                    radius = r1 * 1.32f
                ),
                radius = r1,
                center = Offset(cx1, cy1)
            )

            // B) Top Pit Indentation Notch
            val notchFront = Path().apply {
                moveTo(cx1 - r1 * 0.25f, cy1 - r1 * 0.80f)
                quadraticTo(cx1, cy1 - r1 * 0.66f, cx1 + r1 * 0.25f, cy1 - r1 * 0.80f)
            }
            drawPath(
                path = notchFront,
                color = Color(0xFF28000B).copy(alpha = 0.75f),
                style = Stroke(width = 2.dp.toPx(), cap = StrokeCap.Round)
            )

            // C) Primary Specular Glossy Shine
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(
                        Color.White.copy(alpha = 0.85f),
                        Color.White.copy(alpha = 0.25f),
                        Color.Transparent
                    ),
                    center = Offset(cx1 - r1 * 0.38f, cy1 - r1 * 0.40f),
                    radius = r1 * 0.38f
                ),
                radius = r1 * 0.38f,
                center = Offset(cx1 - r1 * 0.38f, cy1 - r1 * 0.40f)
            )

            // D) Soft ambient bounce rim light at bottom
            val bounceRimCenter = Offset(cx1 + r1 * 0.25f, cy1 + r1 * 0.25f)
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(
                        Color(0xFFFF8DA1).copy(alpha = 0.30f),
                        Color.Transparent
                    ),
                    center = bounceRimCenter,
                    radius = r1 * 0.45f
                ),
                radius = r1 * 0.45f,
                center = bounceRimCenter
            )
        }
    }
}

/**
 * Miniature Cherry icon used in the TopAppBar, History cards, and Badges.
 */
@Composable
fun MiniCherryIcon(
    modifier: Modifier = Modifier,
    size: Dp = 24.dp
) {
    Canvas(modifier = modifier.size(size)) {
        val w = this.size.width
        val h = this.size.height

        val apexX = w * 0.50f
        val apexY = h * 0.16f

        val cx1 = w * 0.65f
        val cy1 = h * 0.68f
        val r1 = w * 0.25f

        val cx2 = w * 0.35f
        val cy2 = h * 0.64f
        val r2 = w * 0.22f

        // Back cherry
        drawCircle(
            color = CherryPrimaryGradientStart,
            radius = r2,
            center = Offset(cx2, cy2)
        )
        // Gloss on back
        drawCircle(
            color = Color.White.copy(alpha = 0.6f),
            radius = r2 * 0.35f,
            center = Offset(cx2 - r2 * 0.3f, cy2 - r2 * 0.3f)
        )

        // Stems
        val stemPath = Path().apply {
            moveTo(cx2, cy2 - r2 * 0.7f)
            quadraticTo(w * 0.36f, h * 0.36f, apexX, apexY)
            moveTo(cx1, cy1 - r1 * 0.7f)
            quadraticTo(w * 0.64f, h * 0.36f, apexX, apexY)
        }
        drawPath(
            path = stemPath,
            color = CherryLeafGreen,
            style = Stroke(width = 1.8.dp.toPx(), cap = StrokeCap.Round)
        )

        // Leaf
        val leaf = Path().apply {
            moveTo(apexX, apexY)
            quadraticTo(apexX + w * 0.25f, apexY - h * 0.15f, apexX + w * 0.35f, apexY)
            quadraticTo(apexX + w * 0.18f, apexY + h * 0.10f, apexX, apexY)
        }
        drawPath(path = leaf, color = CherryLeafLight)

        // Front cherry
        drawCircle(
            color = CherryPrimary,
            radius = r1,
            center = Offset(cx1, cy1)
        )
        // Gloss on front
        drawCircle(
            color = Color.White.copy(alpha = 0.75f),
            radius = r1 * 0.35f,
            center = Offset(cx1 - r1 * 0.3f, cy1 - r1 * 0.3f)
        )
    }
}

/**
 * Unmistakable miniature twin cherries indicator (🍒) for Pomodoro round tracking.
 * Features connected twin cherries with slender curved stems, apex leaf,
 * and specular highlights. Inactive rounds show a sleek cherry silhouette.
 */
@Composable
fun TwinCherryIndicator(
    isCompleted: Boolean,
    isCurrent: Boolean,
    modifier: Modifier = Modifier,
    size: Dp = 22.dp
) {
    Canvas(modifier = modifier.size(size)) {
        val w = this.size.width
        val h = this.size.height

        val apexX = w * 0.50f
        val apexY = h * 0.14f

        // Front right cherry (slightly larger, sitting in front)
        val cx1 = w * 0.67f
        val cy1 = h * 0.70f
        val r1 = w * 0.24f

        // Back left cherry (tucked slightly behind)
        val cx2 = w * 0.33f
        val cy2 = h * 0.65f
        val r2 = w * 0.21f

        if (isCompleted || isCurrent) {
            // Soft radiant glow behind current round cherry pair
            if (isCurrent) {
                drawCircle(
                    color = CherryPrimary.copy(alpha = 0.35f),
                    radius = w * 0.48f,
                    center = Offset(w * 0.50f, h * 0.66f)
                )
            }

            // Curved green stems meeting at the top apex
            val stemThickness = 1.6.dp.toPx()
            val stemBack = Path().apply {
                moveTo(cx2, cy2 - r2 * 0.75f)
                quadraticTo(w * 0.35f, h * 0.35f, apexX, apexY)
            }
            drawPath(
                path = stemBack,
                color = CherryLeafGreen,
                style = Stroke(width = stemThickness, cap = StrokeCap.Round)
            )

            val stemFront = Path().apply {
                moveTo(cx1, cy1 - r1 * 0.75f)
                quadraticTo(w * 0.65f, h * 0.38f, apexX, apexY)
            }
            drawPath(
                path = stemFront,
                color = CherryLeafGreen,
                style = Stroke(width = stemThickness, cap = StrokeCap.Round)
            )

            // Perky green leaf at apex
            val leafPath = Path().apply {
                moveTo(apexX, apexY)
                quadraticTo(apexX + w * 0.26f, apexY - h * 0.16f, apexX + w * 0.38f, apexY - h * 0.04f)
                quadraticTo(apexX + w * 0.22f, apexY + h * 0.10f, apexX, apexY)
            }
            drawPath(
                path = leafPath,
                color = CherryLeafLight
            )

            // Back Cherry: 3D shaded sphere + highlight
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(
                        Color(0xFFFF5277),
                        if (isCurrent) CherryPrimary else CherryPrimaryGradientStart,
                        Color(0xFF5A0015)
                    ),
                    center = Offset(cx2 - r2 * 0.3f, cy2 - r2 * 0.3f),
                    radius = r2 * 1.3f
                ),
                radius = r2,
                center = Offset(cx2, cy2)
            )
            drawCircle(
                color = Color.White.copy(alpha = 0.75f),
                radius = r2 * 0.35f,
                center = Offset(cx2 - r2 * 0.35f, cy2 - r2 * 0.35f)
            )

            // Front Cherry: 3D shaded sphere + highlight
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(
                        Color(0xFFFF5C8A),
                        if (isCurrent) CherryPrimary else CherryPrimaryGradientStart,
                        Color(0xFF4A0015)
                    ),
                    center = Offset(cx1 - r1 * 0.3f, cy1 - r1 * 0.3f),
                    radius = r1 * 1.3f
                ),
                radius = r1,
                center = Offset(cx1, cy1)
            )
            drawCircle(
                color = Color.White.copy(alpha = 0.85f),
                radius = r1 * 0.35f,
                center = Offset(cx1 - r1 * 0.35f, cy1 - r1 * 0.35f)
            )
        } else {
            // Inactive upcoming round: Twin Cherry outline silhouette
            val strokePx = 1.2.dp.toPx()
            val dimColor = CherryBorder.copy(alpha = 0.75f)

            // Stems
            val stemBack = Path().apply {
                moveTo(cx2, cy2 - r2 * 0.75f)
                quadraticTo(w * 0.35f, h * 0.35f, apexX, apexY)
            }
            drawPath(
                path = stemBack,
                color = dimColor.copy(alpha = 0.5f),
                style = Stroke(width = strokePx, cap = StrokeCap.Round)
            )

            val stemFront = Path().apply {
                moveTo(cx1, cy1 - r1 * 0.75f)
                quadraticTo(w * 0.65f, h * 0.38f, apexX, apexY)
            }
            drawPath(
                path = stemFront,
                color = dimColor.copy(alpha = 0.5f),
                style = Stroke(width = strokePx, cap = StrokeCap.Round)
            )

            // Leaf outline
            val leafPath = Path().apply {
                moveTo(apexX, apexY)
                quadraticTo(apexX + w * 0.26f, apexY - h * 0.16f, apexX + w * 0.38f, apexY - h * 0.04f)
                quadraticTo(apexX + w * 0.22f, apexY + h * 0.10f, apexX, apexY)
            }
            drawPath(
                path = leafPath,
                color = dimColor.copy(alpha = 0.5f),
                style = Stroke(width = strokePx)
            )

            // Back Cherry (subtle dark silhouette + outline)
            drawCircle(
                color = CherrySurfaceCard,
                radius = r2,
                center = Offset(cx2, cy2)
            )
            drawCircle(
                color = dimColor,
                radius = r2,
                center = Offset(cx2, cy2),
                style = Stroke(width = strokePx)
            )

            // Front Cherry (subtle dark silhouette + outline)
            drawCircle(
                color = CherrySurfaceCard,
                radius = r1,
                center = Offset(cx1, cy1)
            )
            drawCircle(
                color = dimColor,
                radius = r1,
                center = Offset(cx1, cy1),
                style = Stroke(width = strokePx)
            )
        }
    }
}

/**
 * 4 Pomodoro cycle indicators represented by unmistakable miniature twin cherries!
 * Shows completed and active rounds in radiant glossy cherry, upcoming rounds in sleek cherry silhouette.
 */
@Composable
fun CycleCherryDots(
    currentRound: Int,
    totalRounds: Int = 4,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(10.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        for (i in 1..totalRounds) {
            val isCompleted = i < currentRound
            val isCurrent = i == currentRound

            TwinCherryIndicator(
                isCompleted = isCompleted,
                isCurrent = isCurrent,
                size = 22.dp
            )
        }
    }
}


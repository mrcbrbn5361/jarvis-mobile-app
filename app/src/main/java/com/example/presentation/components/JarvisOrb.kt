package com.example.presentation.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.*
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.unit.dp
import com.example.presentation.OrbState
import com.example.ui.theme.CyberCyan
import com.example.ui.theme.CyberTeal
import com.example.ui.theme.CyberBlue
import com.example.ui.theme.CyberOrange
import com.example.ui.theme.CyberPurple
import kotlin.math.cos
import kotlin.math.sin

@Composable
fun JarvisOrb(
    orbState: OrbState,
    modifier: Modifier = Modifier
) {
    val infiniteTransition = rememberInfiniteTransition(label = "orb_infinite")

    // Slow clockwise rotation for outer circle
    val slowRotation by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(12000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "slow_rotation"
    )

    // Fast counter-clockwise rotation for intermediate ring
    val fastRotation by infiniteTransition.animateFloat(
        initialValue = 360f,
        targetValue = 0f,
        animationSpec = infiniteRepeatable(
            animation = tween(4000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "fast_rotation"
    )

    // Pulsing/Breathing multiplier for inner solid core
    val pulseScale by infiniteTransition.animateFloat(
        initialValue = 0.85f,
        targetValue = 1.15f,
        animationSpec = infiniteRepeatable(
            animation = tween(1800, easing = EaseInOutSine),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulse"
    )

    // Wavy sine index for dynamic soundwave simulation
    val waveOffset by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 2f * Math.PI.toFloat(),
        animationSpec = infiniteRepeatable(
            animation = tween(1000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "wave_offset"
    )

    // Map OrbState to actual glowing colors
    val coreColor by animateColorAsState(
        targetValue = when (orbState) {
            OrbState.Idle -> CyberCyan
            OrbState.Listening -> CyberCyan
            OrbState.Thinking -> CyberPurple
            OrbState.Speaking -> CyberTeal
            OrbState.Error -> CyberOrange
        },
        animationSpec = tween(500),
        label = "core_color"
    )

    val auraColor by animateColorAsState(
        targetValue = when (orbState) {
            OrbState.Idle -> CyberBlue.copy(alpha = 0.2f)
            OrbState.Listening -> CyberCyan.copy(alpha = 0.4f)
            OrbState.Thinking -> CyberPurple.copy(alpha = 0.4f)
            OrbState.Speaking -> CyberCyan.copy(alpha = 0.35f)
            OrbState.Error -> CyberOrange.copy(alpha = 0.45f)
        },
        animationSpec = tween(500),
        label = "aura_color"
    )

    Canvas(
        modifier = modifier
            .size(240.dp)
            .aspectRatio(1f)
    ) {
        val width = size.width
        val height = size.height
        val center = Offset(width / 2f, height / 2f)
        val maxRadius = width.coerceAtMost(height) / 2f

        // 1. Draw outermost cyber radar scan ring
        drawCircle(
            color = coreColor.copy(alpha = 0.15f),
            radius = maxRadius * 0.95f,
            style = Stroke(
                width = 2f,
                pathEffect = PathEffect.dashPathEffect(floatArrayOf(10f, 20f), 0f)
            )
        )

        // 2. Draw rotating outer dotted-dash gear ring
        rotate(degrees = slowRotation, pivot = center) {
            drawCircle(
                color = coreColor.copy(alpha = 0.4f),
                radius = maxRadius * 0.82f,
                style = Stroke(
                    width = 4f,
                    pathEffect = PathEffect.dashPathEffect(floatArrayOf(25f, 15f, 5f, 15f), 0f)
                )
            )
        }

        // 3. Draw rotating inner segmented ring
        rotate(degrees = fastRotation, pivot = center) {
            drawCircle(
                color = coreColor.copy(alpha = 0.6f),
                radius = maxRadius * 0.65f,
                style = Stroke(
                    width = 6f,
                    pathEffect = PathEffect.dashPathEffect(floatArrayOf(50f, 25f), 0f)
                )
            )
            // Add technical crosshairs
            drawLine(
                color = coreColor.copy(alpha = 0.5f),
                start = Offset(center.x - maxRadius * 0.75f, center.y),
                end = Offset(center.x - maxRadius * 0.55f, center.y),
                strokeWidth = 3f
            )
            drawLine(
                color = coreColor.copy(alpha = 0.5f),
                start = Offset(center.x + maxRadius * 0.55f, center.y),
                end = Offset(center.x + maxRadius * 0.75f, center.y),
                strokeWidth = 3f
            )
        }

        // 4. Draw outer halo glow aura (Gradient background)
        drawCircle(
            brush = Brush.radialGradient(
                colors = listOf(auraColor, Color.Transparent),
                center = center,
                radius = maxRadius * 0.6f * if (orbState == OrbState.Thinking) 1.2f else pulseScale
            ),
            radius = maxRadius * 0.6f * if (orbState == OrbState.Thinking) 1.2f else pulseScale
        )

        // 5. Draw interactive dynamic core waves (Speech waves or processing visualizers)
        when (orbState) {
            OrbState.Speaking, OrbState.Listening -> {
                // Render live frequency waves projecting outwards
                val waveCount = 12
                val currentPulse = if (orbState == OrbState.Speaking) 1.25f else 1.05f
                for (i in 0 until waveCount) {
                    val angle = (i.toFloat() / waveCount) * 2f * Math.PI.toFloat()
                    val waveAmplitude = sin(angle * 3f + waveOffset) * 24f
                    val startRad = maxRadius * 0.35f
                    val endRad = maxRadius * 0.55f + waveAmplitude * currentPulse
                    
                    val startOffset = Offset(
                        x = center.x + cos(angle) * startRad,
                        y = center.y + sin(angle) * startRad
                    )
                    val endOffset = Offset(
                        x = center.x + cos(angle) * endRad,
                        y = center.y + sin(angle) * endRad
                    )

                    drawLine(
                        brush = Brush.linearGradient(listOf(coreColor, Color.Transparent)),
                        start = startOffset,
                        end = endOffset,
                        strokeWidth = 5f
                    )
                }
            }
            OrbState.Thinking -> {
                // Spinning multi-colored orbits
                val orbits = 4
                for (i in 0 until orbits) {
                    val orbitAngle = fastRotation + (i * 90)
                    rotate(degrees = orbitAngle, pivot = center) {
                        drawCircle(
                            color = CyberPurple.copy(alpha = 0.8f),
                            radius = maxRadius * 0.45f,
                            center = Offset(center.x + maxRadius * 0.2f, center.y),
                            style = Stroke(width = 3f)
                        )
                    }
                }
            }
            else -> {
                // Just quiet system orbits
                drawCircle(
                    color = coreColor.copy(alpha = 0.3f),
                    radius = maxRadius * 0.45f,
                    style = Stroke(width = 2f)
                )
            }
        }

        // 6. Solid Core glowing focal point
        drawCircle(
            brush = Brush.radialGradient(
                colors = listOf(Color.White, coreColor, coreColor.copy(alpha = 0.2f)),
                center = center,
                radius = maxRadius * 0.3f * pulseScale
            ),
            radius = maxRadius * 0.3f * pulseScale
        )
    }
}

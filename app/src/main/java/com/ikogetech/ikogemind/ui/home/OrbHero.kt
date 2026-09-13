package com.ikogetech.ikogemind.ui.home

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.rotate
import com.ikogetech.ikogemind.ui.theme.IkogeOrbDeep
import com.ikogetech.ikogemind.ui.theme.IkogeOrbGlow
import com.ikogetech.ikogemind.ui.theme.IkogeOrbHalo

/**
 * The Home screen's hero graphic — a glossy, swirl-ring orb built natively out of
 * Canvas primitives (radial gradients + rotating gradient arcs), per the settled
 * "blue palette, glossy swirl-ring orb" visual direction (see decisions-log.md).
 * Deliberately NOT a particle system or a bundled Lottie file: no extra animation
 * dependency, no asset to ship, and it scales to any size for free.
 *
 * Built from four layers, back to front:
 * 1. A soft halo radial-gradient bleeding out past the orb's edge into the
 *    black background (reads as ambient glow, matches the reference image).
 * 2. A dark, off-center-lit sphere base — the off-center radial gradient origin
 *    is what sells "glossy sphere" rather than "flat circle".
 * 3 & 4. Two rotating arcs at different radii/speeds/opacities, stroked with a
 *    sweep gradient that fades in and out — these are the "swirl" highlights
 *    catching the light as they turn. Two independent rotation speeds avoid the
 *    animation ever looking like a single spinning image.
 */
@Composable
fun OrbHero(modifier: Modifier = Modifier) {
    val infiniteTransition = rememberInfiniteTransition(label = "orbHero")

    val primaryAngle by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 7000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "orbPrimaryAngle"
    )

    val secondaryAngle by infiniteTransition.animateFloat(
        initialValue = 360f,
        targetValue = 0f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 11000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "orbSecondaryAngle"
    )

    Canvas(
        modifier = modifier.aspectRatio(1f)
    ) {
        val center = Offset(size.width / 2f, size.height / 2f)
        val radius = size.minDimension / 2f

        // Layer 1 — ambient halo, bigger than the orb itself, fades to transparent.
        drawCircle(
            brush = Brush.radialGradient(
                colors = listOf(
                    IkogeOrbHalo.copy(alpha = 0.35f),
                    IkogeOrbHalo.copy(alpha = 0.08f),
                    Color.Transparent
                ),
                center = center,
                radius = radius * 1.8f
            ),
            radius = radius * 1.8f,
            center = center
        )

        // Layer 2 — sphere base, light source offset up-left for a glossy feel.
        drawCircle(
            brush = Brush.radialGradient(
                colors = listOf(
                    IkogeOrbGlow.copy(alpha = 0.55f),
                    IkogeOrbDeep,
                    Color.Black
                ),
                center = Offset(center.x - radius * 0.35f, center.y - radius * 0.35f),
                radius = radius * 1.6f
            ),
            radius = radius * 0.94f,
            center = center
        )

        // Layer 3 — slow, wide, soft swirl arc.
        rotate(degrees = primaryAngle, pivot = center) {
            drawArc(
                brush = Brush.sweepGradient(
                    colors = listOf(
                        Color.Transparent,
                        IkogeOrbGlow.copy(alpha = 0.9f),
                        Color.Transparent,
                        IkogeOrbHalo.copy(alpha = 0.4f),
                        Color.Transparent
                    ),
                    center = center
                ),
                startAngle = 0f,
                sweepAngle = 300f,
                useCenter = false,
                style = Stroke(width = radius * 0.16f, cap = StrokeCap.Round),
                topLeft = Offset(center.x - radius * 0.86f, center.y - radius * 0.86f),
                size = Size(radius * 1.72f, radius * 1.72f)
            )
        }

        // Layer 4 — faster, thinner, brighter counter-rotating swirl arc; this is
        // what keeps the animation reading as a turning glossy ring rather than one
        // static shape spinning uniformly.
        rotate(degrees = secondaryAngle, pivot = center) {
            drawArc(
                brush = Brush.sweepGradient(
                    colors = listOf(
                        Color.Transparent,
                        Color.White.copy(alpha = 0.8f),
                        IkogeOrbGlow.copy(alpha = 0.6f),
                        Color.Transparent
                    ),
                    center = center
                ),
                startAngle = 40f,
                sweepAngle = 220f,
                useCenter = false,
                style = Stroke(width = radius * 0.06f, cap = StrokeCap.Round),
                topLeft = Offset(center.x - radius * 0.74f, center.y - radius * 0.74f),
                size = Size(radius * 1.48f, radius * 1.48f)
            )
        }

        // Layer 5 — inner shadow ring pulled toward the bottom-right edge, so the
        // sphere reads as lit from one side rather than glowing evenly all over.
        drawCircle(
            brush = Brush.radialGradient(
                colors = listOf(Color.Transparent, Color.Black.copy(alpha = 0.55f)),
                center = Offset(center.x + radius * 0.3f, center.y + radius * 0.3f),
                radius = radius * 1.1f
            ),
            radius = radius * 0.94f,
            center = center
        )
    }
}

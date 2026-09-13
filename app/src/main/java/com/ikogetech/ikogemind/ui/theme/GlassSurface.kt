package com.ikogetech.ikogemind.ui.theme

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.unit.dp

/**
 * The "polished black glass" card from the design direction: a near-black fill,
 * a thin brighter stroke that fades from the top-left corner (never an even
 * frosted-white overlay haze), and — when [glow] is on — a soft ambient shadow
 * tinted with the accent color instead of a hard black drop shadow.
 *
 * Used for message bubbles, the model-status chip, and anywhere else that used
 * to be a plain Material3 Card/Surface, so the whole screen reads as one
 * consistent material rather than mixed defaults plus one custom card.
 */
@Composable
fun GlassSurface(
    modifier: Modifier = Modifier,
    shape: Shape = RoundedCornerShape(18.dp),
    fill: Color = MaterialTheme.colorScheme.surface,
    glow: Boolean = false,
    content: @Composable () -> Unit
) {
    Box(
        modifier = modifier
            .then(
                if (glow) {
                    Modifier.shadow(
                        elevation = 12.dp,
                        shape = shape,
                        ambientColor = IkogeAccent.copy(alpha = 0.35f),
                        spotColor = IkogeAccent.copy(alpha = 0.35f)
                    )
                } else {
                    Modifier
                }
            )
            .background(fill, shape)
            .border(
                width = 1.dp,
                brush = Brush.linearGradient(
                    colors = listOf(
                        IkogeGlassHighlight.copy(alpha = 0.16f),
                        IkogeGlassHighlight.copy(alpha = 0.03f),
                        Color.Transparent
                    )
                ),
                shape = shape
            )
    ) {
        content()
    }
}

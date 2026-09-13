package com.ikogetech.ikogemind.ui.theme

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.path
import androidx.compose.ui.unit.dp

/**
 * Hand-built copy of Material's "content_copy" glyph.
 *
 * Icons.Filled.ContentCopy lives in material-icons-extended, which isn't a
 * dependency here (this project deliberately sticks to material-icons-core —
 * see the ThumbDown-via-180°-rotation workaround in ChatScreen.kt for the same
 * constraint). Pulling in the ~padding-heavy extended artifact for a single icon
 * isn't worth it, so the path data is transcribed by hand instead (matches the
 * standard 24x24 content_copy glyph exactly, three subpaths combined with the
 * default nonzero fill rule to punch the hollow "front sheet" hole).
 *
 * The path's own fill color is irrelevant — Icon() always overrides it via
 * ColorFilter.tint(tint), so any opaque color works as the placeholder here.
 */
val ContentCopyIcon: ImageVector by lazy {
    ImageVector.Builder(
        name = "ContentCopy",
        defaultWidth = 24.dp,
        defaultHeight = 24.dp,
        viewportWidth = 24f,
        viewportHeight = 24f
    ).apply {
        path(fill = SolidColor(Color.Black)) {
            // Back sheet — thin L-shaped frame peeking out top-left
            moveTo(16f, 1f)
            lineTo(4f, 1f)
            curveTo(2.9f, 1f, 2f, 1.9f, 2f, 3f)
            lineTo(2f, 17f)
            lineTo(4f, 17f)
            lineTo(4f, 3f)
            lineTo(16f, 3f)
            close()

            // Front sheet — outer rounded-rect boundary
            moveTo(19f, 5f)
            lineTo(8f, 5f)
            curveTo(6.9f, 5f, 6f, 5.9f, 6f, 7f)
            lineTo(6f, 21f)
            curveTo(6f, 22.1f, 6.9f, 23f, 8f, 23f)
            lineTo(19f, 23f)
            curveTo(20.1f, 23f, 21f, 22.1f, 21f, 21f)
            lineTo(21f, 7f)
            curveTo(21f, 5.9f, 20.1f, 5f, 19f, 5f)
            close()

            // Front sheet — inner cutout (creates the hollow center)
            moveTo(19f, 21f)
            lineTo(8f, 21f)
            lineTo(8f, 7f)
            lineTo(19f, 7f)
            lineTo(19f, 21f)
            close()
        }
    }.build()
}

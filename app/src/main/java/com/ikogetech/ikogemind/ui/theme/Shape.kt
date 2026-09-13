package com.ikogetech.ikogemind.ui.theme

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Shapes
import androidx.compose.ui.unit.dp

// Pill-shaped inputs/buttons, generously rounded everything else — the design
// direction calls for nothing sharp-cornered and nothing fully rectangular.
// extraLarge doubles as the reusable "pill" shape for the message input and
// round action buttons (percent = 50 always resolves to a full pill/circle
// regardless of the composable's actual width/height).
val IkogePillShape = RoundedCornerShape(percent = 50)

val IkogeShapes = Shapes(
    extraSmall = RoundedCornerShape(8.dp),
    small = RoundedCornerShape(12.dp),
    medium = RoundedCornerShape(18.dp),
    large = RoundedCornerShape(24.dp),
    extraLarge = IkogePillShape
)

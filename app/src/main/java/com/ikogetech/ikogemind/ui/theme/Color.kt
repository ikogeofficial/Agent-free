package com.ikogetech.ikogemind.ui.theme

import androidx.compose.ui.graphics.Color

// UI/UX overhaul — "polished black glass" direction (matches the reference images
// reviewed for this app: pure black base rather than near-black, glass cards with
// a thin brighter highlight along the top/corner edges instead of a frosted white
// overlay haze, pill shapes with a soft ambient glow). Replaces the earlier flat
// near-black/blue-violet palette below it in git history.
val IkogeBackground = Color(0xFF000000)
val IkogeGlassSurface = Color(0xFF121214) // assistant bubbles, chips, cards
val IkogeGlassSurfaceVariant = Color(0xFF1B1B1F) // user bubbles — one step lighter
val IkogeAccent = Color(0xFF6C8CFF) // send button, active states, glow source
val IkogeOnBackground = Color(0xFFF2F2F4)
val IkogeOnSurfaceMuted = Color(0xFFA0A0A8)
val IkogeError = Color(0xFFFF6B6B)
val IkogeErrorContainer = Color(0xFF2A1416)

// Used only by GlassSurface's border gradient — a thin bright-to-transparent
// stroke starting from the top-left corner, never a flat frosted overlay.
val IkogeGlassHighlight = Color(0xFFFFFFFF)

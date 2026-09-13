package com.ikogetech.ikogemind.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable

private val DarkColors = darkColorScheme(
    background = IkogeBackground,
    surface = IkogeGlassSurface,
    surfaceVariant = IkogeGlassSurfaceVariant,
    primary = IkogeAccent,
    onBackground = IkogeOnBackground,
    onSurface = IkogeOnBackground,
    onSurfaceVariant = IkogeOnSurfaceMuted,
    error = IkogeError,
    errorContainer = IkogeErrorContainer,
    onErrorContainer = IkogeOnBackground
)

// The glass-card look is designed for dark mode — light mode keeps Material3's
// defaults rather than trying to invert "black glass" into something that would
// no longer read as the same design language.
private val LightColors = lightColorScheme(
    primary = IkogeAccent,
    error = IkogeError
)

@Composable
fun IkogeMindTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = if (darkTheme) DarkColors else LightColors,
        typography = IkogeTypography,
        shapes = IkogeShapes,
        content = content
    )
}

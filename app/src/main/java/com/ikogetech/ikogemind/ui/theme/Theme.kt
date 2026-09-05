package com.ikogetech.ikogemind.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable

private val DarkColors = darkColorScheme(
    background = IkogeBackground,
    surface = IkogeSurface,
    surfaceVariant = IkogeSurfaceVariant,
    primary = IkogeAccent,
    onBackground = IkogeOnBackground,
    onSurface = IkogeOnBackground,
    onSurfaceVariant = IkogeOnSurfaceMuted,
    error = IkogeError
)

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
        content = content
    )
}

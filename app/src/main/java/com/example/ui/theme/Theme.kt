package com.example.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable

private val DarkColorScheme = darkColorScheme(
    primary = VibrantDarkPrimary,
    onPrimary = VibrantDarkOnPrimary,
    primaryContainer = VibrantDarkPrimaryContainer,
    onPrimaryContainer = VibrantDarkOnPrimaryContainer,
    secondary = VibrantSecondaryContainer,
    onSecondary = VibrantOnSecondaryContainer,
    tertiary = VibrantTertiary,
    onTertiary = VibrantOnTertiary,
    background = VibrantDarkBackground,
    onBackground = VibrantBackground,
    surface = VibrantDarkSurface,
    onSurface = VibrantBackground,
    surfaceVariant = VibrantDarkSurfaceContainer,
    onSurfaceVariant = VibrantOutline,
    outline = VibrantOutline,
    error = VibrantError,
    onError = VibrantOnError,
    errorContainer = VibrantOnErrorContainer,
    onErrorContainer = VibrantErrorContainer
)

private val LightColorScheme = lightColorScheme(
    primary = VibrantPrimary,
    onPrimary = VibrantOnPrimary,
    primaryContainer = VibrantPrimaryContainer,
    onPrimaryContainer = VibrantOnPrimaryContainer,
    secondary = VibrantSecondary,
    onSecondary = VibrantOnSecondary,
    secondaryContainer = VibrantSecondaryContainer,
    onSecondaryContainer = VibrantOnSecondaryContainer,
    tertiary = VibrantTertiary,
    onTertiary = VibrantOnTertiary,
    tertiaryContainer = VibrantTertiaryContainer,
    onTertiaryContainer = VibrantOnTertiaryContainer,
    background = VibrantBackground,
    onBackground = VibrantOnBackground,
    surface = VibrantSurface,
    onSurface = VibrantOnSurface,
    surfaceVariant = VibrantSurfaceVariant,
    onSurfaceVariant = VibrantOnSurfaceVariant,
    surfaceContainer = VibrantSurfaceContainer,
    surfaceContainerHigh = VibrantSurfaceContainerHigh,
    outline = VibrantOutline,
    outlineVariant = VibrantOutlineVariant,
    error = VibrantError,
    onError = VibrantOnError,
    errorContainer = VibrantErrorContainer,
    onErrorContainer = VibrantOnErrorContainer
)

@Composable
fun MyApplicationTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}

package com.example.fitgenius.ui.theme

import android.app.Activity
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

private val DarkColorScheme = darkColorScheme(
    primary = VibrantBlue,
    secondary = SoftBlue,
    background = DarkCharcoal,
    surface = SlateGray,
    surfaceVariant = LightSlate, // Se usará para las barras de navegación y tarjetas
    onPrimary = OffWhite,
    onSecondary = OffWhite,
    onBackground = OffWhite,
    onSurface = OffWhite,
    onSurfaceVariant = OffWhite,
    error = FieryRed,
    onError = OffWhite
)

private val LightColorScheme = lightColorScheme(
    primary = VibrantBlue,
    secondary = SoftBlue,
    background = OffWhite,
    surface = Color.White,
    surfaceVariant = LightGrayText, // Un gris claro para las tarjetas en modo claro
    onPrimary = OffWhite,
    onSecondary = OffWhite,
    onBackground = DarkCharcoal,
    onSurface = DarkCharcoal,
    onSurfaceVariant = DarkCharcoal,
    error = FieryRed,
    onError = OffWhite
)

@Composable
fun FitGeniusTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme
    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            window.statusBarColor = colorScheme.background.toArgb()
            WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = !darkTheme
        }
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}

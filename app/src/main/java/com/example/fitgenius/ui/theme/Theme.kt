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

// Paleta para el modo Oscuro, con Verde Gasolina y Azul Eléctrico
private val DarkColorScheme = darkColorScheme(
    primary = PetrolGreen,              // Verde gasolina como color principal
    secondary = BrightBlue,           // Azul para acentos secundarios
    tertiary = BrightBlue,            // Azul para botones y acciones importantes
    background = DarkBackground,        // Fondo oscuro principal
    surface = DarkSurface,              // Superficies como las tarjetas
    onPrimary = PureWhite,              // Texto sobre el color primario
    onSecondary = PureWhite,            // Texto sobre el color secundario
    onBackground = TextColorDark,       // Texto principal sobre el fondo
    onSurface = TextColorDark,          // Texto sobre las superficies
    error = Color(0xFFE57373),          // Un rojo más suave para errores
    onError = DarkBackground
)

// Paleta para el modo Claro, con Verde Gasolina y Azul Eléctrico
private val LightColorScheme = lightColorScheme(
    primary = PetrolGreen,              // Verde gasolina como color principal
    secondary = BrightBlue,           // Azul para acentos secundarios
    tertiary = BrightBlue,            // Azul para botones y acciones importantes
    background = OffWhite,              // Fondo claro y suave
    surface = PureWhite,                // Superficies como las tarjetas
    onPrimary = PureWhite,              // Texto sobre el color primario
    onSecondary = PureWhite,            // Texto sobre el color secundario
    onBackground = TextColorLight,      // Texto principal sobre el fondo
    onSurface = TextColorLight,         // Texto sobre las superficies
    error = Color(0xFFD32F2F),          // Un rojo más fuerte para errores
    onError = PureWhite
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
        typography = Typography, // Asumimos que tienes un archivo Type.kt
        content = content
    )
}

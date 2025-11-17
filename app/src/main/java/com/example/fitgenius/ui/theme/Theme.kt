package com.example.fitgenius.ui.theme

import android.app.Activity
import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

// Paleta para el modo Oscuro
private val DarkColorScheme = darkColorScheme(
    primary = PetrolGreen,          // El verde gasolina como color principal
    secondary = BrightBlue,         // El azul vibrante como color secundario/acento
    tertiary = BrightBlue,          // También se puede usar para otros acentos
    background = DarkGrey,          // Fondo oscuro
    surface = LightGrey,            // Superficies como las tarjetas
    onPrimary = PureWhite,          // Texto sobre el color primario
    onSecondary = PureWhite,        // Texto sobre el color secundario
    onBackground = TextColorDark,     // Texto sobre el fondo general
    onSurface = TextColorDark,        // Texto sobre las superficies
    error = Color(0xFFCF6679),              // Un color de error estándar para modo oscuro
    onError = DarkGrey
)

// Paleta para el modo Claro
private val LightColorScheme = lightColorScheme(
    primary = PetrolGreen,          // El verde gasolina como color principal
    secondary = BrightBlue,         // El azul vibrante como color secundario/acento
    tertiary = BrightBlue,          // También se puede usar para otros acentos
    background = OffWhite,          // Fondo claro (blanco roto)
    surface = PureWhite,            // Superficies como las tarjetas (blanco puro)
    onPrimary = PureWhite,          // Texto sobre el color primario
    onSecondary = PureWhite,        // Texto sobre el color secundario
    onBackground = TextColorLight,    // Texto sobre el fondo general
    onSurface = TextColorLight,       // Texto sobre las superficies
    error = Color(0xFFB00020),              // Un color de error estándar para modo claro
    onError = PureWhite
)

@Composable
fun FitGeniusTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = false, // Desactivamos el color dinámico para forzar nuestra paleta
    content: @Composable () -> Unit
) {
    val colorScheme = when {
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            val context = LocalContext.current
            if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        }
        darkTheme -> DarkColorScheme
        else -> LightColorScheme
    }
    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            window.statusBarColor = colorScheme.primary.toArgb()
            WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = !darkTheme
        }
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}

package com.rutaagua.camion.ui.theme

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

private val LightColorScheme = lightColorScheme(
    primary = AguaPrimary,
    onPrimary = SuperficieTarjeta,
    primaryContainer = AguaPrimaryLight,
    onPrimaryContainer = AguaPrimaryDark,
    secondary = AguaSecondary,
    onSecondary = SuperficieTarjeta,
    secondaryContainer = AguaSecondaryLight,
    background = SuperficieClara,
    surface = SuperficieTarjeta,
    onBackground = TextoOscuro,
    onSurface = TextoOscuro,
    outline = BordeSuave,
    error = RojoAlerta
)

private val DarkColorScheme = darkColorScheme(
    primary = AguaPrimaryLight,
    onPrimary = AguaPrimaryDark,
    secondary = AguaSecondary,
    background = Color(0xFF101922),
    surface = Color(0xFF1A2634),
    onBackground = Color(0xFFF1F5F9),
    onSurface = Color(0xFFF1F5F9),
    outline = Color(0xFF334155),
    error = Color(0xFFEF5350)
)

@Composable
fun RutaAguaTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme
    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as? Activity)?.window
            if (window != null) {
                window.statusBarColor = colorScheme.primary.toArgb()
                WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = false
            }
        }
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}

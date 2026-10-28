package com.rutaagua.camion.ui.theme

import android.app.Activity
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

/**
 * Esquema de color Marino Claro unificado.
 * Se mantiene consistente independientemente de si el celular tiene modo oscuro activo,
 * garantizando que las letras dentro de cajas de texto y tarjetas blancas jamás se camuflen.
 */
private val MarineLightColorScheme = lightColorScheme(
    primary = AguaPrimary,
    onPrimary = Color.White,
    primaryContainer = CamionClaySoft,
    onPrimaryContainer = AguaPrimaryDark,
    secondary = AguaSecondary,
    onSecondary = Color.White,
    secondaryContainer = CamionetaClaySoft,
    onSecondaryContainer = AguaSecondaryDark,
    background = SuperficieClara,
    surface = SuperficieTarjeta,
    surfaceVariant = ClayInsetBackground,
    onBackground = TextoOscuro,
    onSurface = TextoOscuro,
    onSurfaceVariant = TextoSecundario,
    outline = BordeSuave,
    error = RojoAlerta,
    onError = Color.White
)

@Composable
fun RutaAguaTheme(
    darkTheme: Boolean = false,
    content: @Composable () -> Unit
) {
    // Siempre usamos el esquema Marino Claro para evitar texto blanco sobre fondos claros
    val colorScheme = MarineLightColorScheme
    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as? Activity)?.window
            if (window != null) {
                window.statusBarColor = Color.Transparent.toArgb()
                WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = true
            }
        }
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}

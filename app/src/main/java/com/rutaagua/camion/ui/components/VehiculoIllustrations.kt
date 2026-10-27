package com.rutaagua.camion.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.rutaagua.camion.data.local.model.TipoVehiculo
import com.rutaagua.camion.ui.theme.AguaPrimary
import com.rutaagua.camion.ui.theme.AguaPrimaryDark
import com.rutaagua.camion.ui.theme.AguaPrimaryLight
import com.rutaagua.camion.ui.theme.AzulCorporativo
import com.rutaagua.camion.ui.theme.BidonAzulColor
import com.rutaagua.camion.ui.theme.BidonTransparenteColor
import com.rutaagua.camion.ui.theme.BidonTransparenteReflejo

/**
 * Ilustración vectorial detallada del Camión Repartidor de Agua (218 bidones).
 * Vista lateral limpia con cabina ejecutiva, tolva de carga con filas de bidones, triple eje y sombra.
 */
@Composable
fun CamionIllustration(
    modifier: Modifier = Modifier,
    size: Dp = 100.dp
) {
    Box(
        modifier = modifier.size(size),
        contentAlignment = Alignment.Center
    ) {
        Canvas(modifier = Modifier.size(size)) {
            val w = this.size.width
            val h = this.size.height

            // 0. Sombra suave en el suelo
            val shadowY = h * 0.86f
            drawOval(
                brush = Brush.radialGradient(
                    colors = listOf(Color(0x330F172A), Color.Transparent),
                    center = Offset(w * 0.5f, shadowY),
                    radius = w * 0.46f
                ),
                topLeft = Offset(w * 0.04f, shadowY - (h * 0.05f)),
                size = Size(w * 0.92f, h * 0.12f)
            )

            // 1. Chasis estructural inferior (Vigas de acero negro)
            val chassisY = h * 0.68f
            drawRoundRect(
                color = Color(0xFF1E293B),
                topLeft = Offset(w * 0.05f, chassisY),
                size = Size(w * 0.88f, h * 0.065f),
                cornerRadius = CornerRadius(w * 0.015f, w * 0.015f)
            )
            // Tanque de combustible metálico
            drawRoundRect(
                color = Color(0xFF94A3B8),
                topLeft = Offset(w * 0.38f, chassisY + (h * 0.015f)),
                size = Size(w * 0.14f, h * 0.045f),
                cornerRadius = CornerRadius(w * 0.01f, w * 0.01f)
            )

            // 2. Carrocería / Jaula de carga de botellones (Tolva Azul Corporativo)
            val cargoLeft = w * 0.04f
            val cargoTop = h * 0.22f
            val cargoWidth = w * 0.63f
            val cargoHeight = h * 0.46f

            // Fondo interior de la tolva
            drawRoundRect(
                brush = Brush.verticalGradient(
                    colors = listOf(Color(0xFF0F172A), Color(0xFF1E293B))
                ),
                topLeft = Offset(cargoLeft, cargoTop),
                size = Size(cargoWidth, cargoHeight),
                cornerRadius = CornerRadius(w * 0.03f, w * 0.03f)
            )

            // Botellones de agua visibles estibados en 2 filas
            val cols = 4
            val bottleW = (cargoWidth - (w * 0.06f)) / cols
            val bottleH = cargoHeight * 0.38f

            // Fila superior de bidones
            for (c in 0 until cols) {
                val bX = cargoLeft + (w * 0.03f) + (c * bottleW)
                val bY = cargoTop + (h * 0.04f)
                val colorBidon = if (c % 2 == 0) BidonTransparenteColor else BidonAzulColor

                // Cuerpo del bidón
                drawRoundRect(
                    brush = Brush.horizontalGradient(
                        colors = listOf(colorBidon, BidonTransparenteReflejo, colorBidon)
                    ),
                    topLeft = Offset(bX + (bottleW * 0.12f), bY + (bottleH * 0.15f)),
                    size = Size(bottleW * 0.76f, bottleH * 0.85f),
                    cornerRadius = CornerRadius(bottleW * 0.15f, bottleW * 0.15f)
                )
                // Pico / Cuello
                drawRect(
                    color = Color.White.copy(alpha = 0.9f),
                    topLeft = Offset(bX + (bottleW * 0.36f), bY),
                    size = Size(bottleW * 0.28f, bottleH * 0.18f)
                )
            }

            // Fila inferior de bidones
            for (c in 0 until cols) {
                val bX = cargoLeft + (w * 0.03f) + (c * bottleW)
                val bY = cargoTop + (cargoHeight * 0.52f)
                val colorBidon = if (c % 2 == 0) BidonAzulColor else BidonTransparenteColor

                drawRoundRect(
                    brush = Brush.horizontalGradient(
                        colors = listOf(colorBidon, BidonTransparenteReflejo, colorBidon)
                    ),
                    topLeft = Offset(bX + (bottleW * 0.12f), bY + (bottleH * 0.15f)),
                    size = Size(bottleW * 0.76f, bottleH * 0.85f),
                    cornerRadius = CornerRadius(bottleW * 0.15f, bottleW * 0.15f)
                )
                drawRect(
                    color = Color.White.copy(alpha = 0.9f),
                    topLeft = Offset(bX + (bottleW * 0.36f), bY),
                    size = Size(bottleW * 0.28f, bottleH * 0.18f)
                )
            }

            // Barandas protectoras y parantes metálicos de la tolva
            val strokeBaranda = Stroke(width = w * 0.015f)
            // Parantes verticales
            for (i in 0..cols) {
                val pX = cargoLeft + (w * 0.02f) + (i * ((cargoWidth - (w * 0.04f)) / cols))
                drawLine(
                    color = AzulCorporativo,
                    start = Offset(pX, cargoTop),
                    end = Offset(pX, cargoTop + cargoHeight),
                    strokeWidth = w * 0.018f
                )
            }
            // Largueros horizontales
            drawLine(
                color = AguaPrimaryLight,
                start = Offset(cargoLeft, cargoTop + (cargoHeight * 0.48f)),
                end = Offset(cargoLeft + cargoWidth, cargoTop + (cargoHeight * 0.48f)),
                strokeWidth = w * 0.018f
            )
            // Marco exterior de la tolva
            drawRoundRect(
                color = AguaPrimary,
                topLeft = Offset(cargoLeft, cargoTop),
                size = Size(cargoWidth, cargoHeight),
                cornerRadius = CornerRadius(w * 0.03f, w * 0.03f),
                style = strokeBaranda
            )

            // Techo de la tolva
            drawRoundRect(
                color = AguaPrimaryDark,
                topLeft = Offset(cargoLeft - (w * 0.01f), cargoTop - (h * 0.02f)),
                size = Size(cargoWidth + (w * 0.02f), h * 0.035f),
                cornerRadius = CornerRadius(w * 0.01f, w * 0.01f)
            )

            // 3. Cabina delantera aerodinámica moderna
            val cabLeft = cargoLeft + cargoWidth + (w * 0.015f)
            val cabTop = h * 0.28f
            val cabWidth = w * 0.27f
            val cabHeight = h * 0.42f

            // Silueta de la cabina
            val cabPath = Path().apply {
                moveTo(cabLeft, cabTop + cabHeight)
                lineTo(cabLeft, cabTop)
                lineTo(cabLeft + (cabWidth * 0.55f), cabTop)
                lineTo(cabLeft + cabWidth, cabTop + (cabHeight * 0.35f))
                lineTo(cabLeft + cabWidth, cabTop + (cabHeight * 0.88f))
                lineTo(cabLeft + (cabWidth * 0.90f), cabTop + cabHeight)
                close()
            }
            drawPath(
                path = cabPath,
                brush = Brush.verticalGradient(
                    colors = listOf(Color(0xFFF8FAFC), Color(0xFFE2E8F0), Color(0xFFCBD5E1))
                )
            )
            // Detalle lateral en Azul Corporativo en la puerta
            drawRect(
                color = AguaPrimary,
                topLeft = Offset(cabLeft + (w * 0.02f), cabTop + (cabHeight * 0.62f)),
                size = Size(cabWidth * 0.85f, h * 0.035f)
            )

            // Ventana lateral con gradiente de cristal
            val winPath = Path().apply {
                moveTo(cabLeft + (w * 0.03f), cabTop + (h * 0.035f))
                lineTo(cabLeft + (cabWidth * 0.50f), cabTop + (h * 0.035f))
                lineTo(cabLeft + (cabWidth * 0.88f), cabTop + (cabHeight * 0.34f))
                lineTo(cabLeft + (w * 0.03f), cabTop + (cabHeight * 0.44f))
                close()
            }
            drawPath(
                path = winPath,
                brush = Brush.linearGradient(
                    colors = listOf(Color(0xFFCFFAFE), Color(0xFF0284C7)),
                    start = Offset(cabLeft, cabTop),
                    end = Offset(cabLeft + cabWidth, cabTop + cabHeight)
                )
            )

            // Espejo retrovisor
            drawRoundRect(
                color = Color(0xFF1E293B),
                topLeft = Offset(cabLeft + (cabWidth * 0.84f), cabTop + (cabHeight * 0.30f)),
                size = Size(w * 0.03f, h * 0.08f),
                cornerRadius = CornerRadius(w * 0.008f, w * 0.008f)
            )

            // Parachoques y Faros LED
            drawRoundRect(
                color = Color(0xFF334155),
                topLeft = Offset(cabLeft + (cabWidth * 0.70f), cabTop + (cabHeight * 0.84f)),
                size = Size(cabWidth * 0.34f, h * 0.07f),
                cornerRadius = CornerRadius(w * 0.015f, w * 0.015f)
            )
            // Faro delantero
            drawRoundRect(
                brush = Brush.horizontalGradient(listOf(Color(0xFFFEF08A), Color(0xFFF59E0B))),
                topLeft = Offset(cabLeft + (cabWidth * 0.92f), cabTop + (cabHeight * 0.66f)),
                size = Size(w * 0.035f, h * 0.05f),
                cornerRadius = CornerRadius(w * 0.01f, w * 0.01f)
            )

            // 4. Ruedas robustas (3 ejes: 2 traseros de carga pesada, 1 delantero direccional)
            val wheelR = w * 0.085f
            val wheelY = h * 0.75f
            dibujarRuedaCompleta(centerX = cargoLeft + (w * 0.13f), centerY = wheelY, radius = wheelR)
            dibujarRuedaCompleta(centerX = cargoLeft + (w * 0.32f), centerY = wheelY, radius = wheelR)
            dibujarRuedaCompleta(centerX = cabLeft + (cabWidth * 0.58f), centerY = wheelY, radius = wheelR)
        }
    }
}

/**
 * Ilustración vectorial detallada de la Camioneta de Apoyo Pickup (80 bidones).
 * Vista lateral moderna estilo camioneta de reparto rápida con balde, barandas y bidones estibados.
 */
@Composable
fun CamionetaIllustration(
    modifier: Modifier = Modifier,
    size: Dp = 100.dp
) {
    Box(
        modifier = modifier.size(size),
        contentAlignment = Alignment.Center
    ) {
        Canvas(modifier = Modifier.size(size)) {
            val w = this.size.width
            val h = this.size.height

            // 0. Sombra en el suelo
            val shadowY = h * 0.86f
            drawOval(
                brush = Brush.radialGradient(
                    colors = listOf(Color(0x330F172A), Color.Transparent),
                    center = Offset(w * 0.5f, shadowY),
                    radius = w * 0.44f
                ),
                topLeft = Offset(w * 0.06f, shadowY - (h * 0.045f)),
                size = Size(w * 0.88f, h * 0.11f)
            )

            // 1. Chasis y estribo inferior
            val chassisY = h * 0.68f
            drawRoundRect(
                color = Color(0xFF1E293B),
                topLeft = Offset(w * 0.08f, chassisY),
                size = Size(w * 0.84f, h * 0.06f),
                cornerRadius = CornerRadius(w * 0.015f, w * 0.015f)
            )
            // Estribo lateral metálico
            drawRoundRect(
                color = Color(0xFF64748B),
                topLeft = Offset(w * 0.36f, chassisY + (h * 0.02f)),
                size = Size(w * 0.28f, h * 0.025f),
                cornerRadius = CornerRadius(w * 0.008f, w * 0.008f)
            )

            // 2. Balde / Paila trasera con barandas metálicas
            val bedLeft = w * 0.06f
            val bedTop = h * 0.44f
            val bedWidth = w * 0.42f
            val bedHeight = h * 0.25f

            // Bidones de agua apilados visibles sobre el balde
            val bottleW = bedWidth * 0.24f
            val bottleH = h * 0.22f
            for (i in 0..2) {
                val bX = bedLeft + (w * 0.04f) + (i * bottleW * 1.15f)
                val bY = bedTop - (bottleH * 0.70f)
                val colorBidon = if (i % 2 == 0) BidonAzulColor else BidonTransparenteColor

                drawRoundRect(
                    brush = Brush.horizontalGradient(
                        colors = listOf(colorBidon, BidonTransparenteReflejo, colorBidon)
                    ),
                    topLeft = Offset(bX, bY + (bottleH * 0.15f)),
                    size = Size(bottleW * 0.85f, bottleH * 0.85f),
                    cornerRadius = CornerRadius(bottleW * 0.18f, bottleW * 0.18f)
                )
                drawRect(
                    color = Color.White,
                    topLeft = Offset(bX + (bottleW * 0.30f), bY),
                    size = Size(bottleW * 0.25f, bottleH * 0.18f)
                )
            }

            // Balde metálico (Color Azul Metálico / Gris Corporativo)
            drawRoundRect(
                brush = Brush.verticalGradient(
                    colors = listOf(Color(0xFF0284C7), Color(0xFF0369A1))
                ),
                topLeft = Offset(bedLeft, bedTop),
                size = Size(bedWidth, bedHeight),
                cornerRadius = CornerRadius(w * 0.02f, w * 0.02f)
            )

            // Barandas tubulares metálicas protectoras
            drawLine(
                color = Color(0xFFCBD5E1),
                start = Offset(bedLeft + (w * 0.02f), bedTop - (h * 0.12f)),
                end = Offset(bedLeft + bedWidth - (w * 0.01f), bedTop - (h * 0.12f)),
                strokeWidth = w * 0.016f,
                cap = StrokeCap.Round
            )
            drawLine(
                color = Color(0xFF94A3B8),
                start = Offset(bedLeft + (w * 0.02f), bedTop - (h * 0.05f)),
                end = Offset(bedLeft + bedWidth - (w * 0.01f), bedTop - (h * 0.05f)),
                strokeWidth = w * 0.014f,
                cap = StrokeCap.Round
            )
            // Parantes verticales de la baranda
            drawLine(
                color = Color(0xFF94A3B8),
                start = Offset(bedLeft + (w * 0.02f), bedTop),
                end = Offset(bedLeft + (w * 0.02f), bedTop - (h * 0.13f)),
                strokeWidth = w * 0.016f
            )
            drawLine(
                color = Color(0xFF94A3B8),
                start = Offset(bedLeft + (bedWidth * 0.5f), bedTop),
                end = Offset(bedLeft + (bedWidth * 0.5f), bedTop - (h * 0.13f)),
                strokeWidth = w * 0.016f
            )

            // 3. Cabina deportiva de la camioneta (Cabina y Capó)
            val cabLeft = bedLeft + bedWidth - (w * 0.01f)
            val cabTop = h * 0.32f
            val cabWidth = w * 0.44f
            val cabHeight = h * 0.37f

            val cabinaPath = Path().apply {
                moveTo(cabLeft, cabTop + cabHeight)
                lineTo(cabLeft, cabTop)
                lineTo(cabLeft + (cabWidth * 0.42f), cabTop)
                lineTo(cabLeft + (cabWidth * 0.68f), cabTop + (cabHeight * 0.36f))
                lineTo(cabLeft + cabWidth, cabTop + (cabHeight * 0.40f))
                lineTo(cabLeft + cabWidth, cabTop + (cabHeight * 0.88f))
                lineTo(cabLeft + (cabWidth * 0.92f), cabTop + cabHeight)
                close()
            }
            drawPath(
                path = cabinaPath,
                brush = Brush.verticalGradient(
                    colors = listOf(Color(0xFF38BDF8), Color(0xFF0284C7), Color(0xFF0369A1))
                )
            )

            // Ventana lateral con división deportiva
            val winPath = Path().apply {
                moveTo(cabLeft + (w * 0.02f), cabTop + (h * 0.03f))
                lineTo(cabLeft + (cabWidth * 0.38f), cabTop + (h * 0.03f))
                lineTo(cabLeft + (cabWidth * 0.62f), cabTop + (cabHeight * 0.34f))
                lineTo(cabLeft + (w * 0.02f), cabTop + (cabHeight * 0.36f))
                close()
            }
            drawPath(
                path = winPath,
                brush = Brush.linearGradient(
                    colors = listOf(Color(0xFFE0F2FE), Color(0xFF0284C7)),
                    start = Offset(cabLeft, cabTop),
                    end = Offset(cabLeft + cabWidth, cabTop + cabHeight)
                )
            )

            // Pilar divisorio de la ventana
            drawLine(
                color = Color(0xFF0369A1),
                start = Offset(cabLeft + (cabWidth * 0.24f), cabTop + (h * 0.03f)),
                end = Offset(cabLeft + (cabWidth * 0.24f), cabTop + (cabHeight * 0.35f)),
                strokeWidth = w * 0.015f
            )

            // Manija de puerta
            drawRoundRect(
                color = Color(0xFF0F172A),
                topLeft = Offset(cabLeft + (cabWidth * 0.14f), cabTop + (cabHeight * 0.42f)),
                size = Size(w * 0.035f, h * 0.018f),
                cornerRadius = CornerRadius(w * 0.005f, w * 0.005f)
            )

            // Espejo retrovisor
            drawRoundRect(
                color = Color(0xFF0F172A),
                topLeft = Offset(cabLeft + (cabWidth * 0.58f), cabTop + (cabHeight * 0.30f)),
                size = Size(w * 0.028f, h * 0.065f),
                cornerRadius = CornerRadius(w * 0.006f, w * 0.006f)
            )

            // Faro delantero halógeno/LED
            drawRoundRect(
                brush = Brush.horizontalGradient(listOf(Color(0xFFFEF08A), Color(0xFFF59E0B))),
                topLeft = Offset(cabLeft + (cabWidth * 0.88f), cabTop + (cabHeight * 0.44f)),
                size = Size(w * 0.04f, h * 0.055f),
                cornerRadius = CornerRadius(w * 0.01f, w * 0.01f)
            )

            // 4. Llantas deportivas All-Terrain (2 ejes: 1 trasero y 1 delantero)
            val wheelR = w * 0.095f
            val wheelY = h * 0.74f
            dibujarRuedaCompleta(centerX = bedLeft + (w * 0.16f), centerY = wheelY, radius = wheelR)
            dibujarRuedaCompleta(centerX = cabLeft + (cabWidth * 0.62f), centerY = wheelY, radius = wheelR)
        }
    }
}

/**
 * Minicomponente reutilizable para insignias y badges de vehículos en listas y calendarios.
 */
@Composable
fun VehiculoThumbnail(
    tipo: TipoVehiculo,
    modifier: Modifier = Modifier,
    size: Dp = 28.dp
) {
    when (tipo) {
        TipoVehiculo.CAMION -> CamionIllustration(modifier = modifier, size = size)
        TipoVehiculo.CAMIONETA -> CamionetaIllustration(modifier = modifier, size = size)
    }
}

private fun DrawScope.dibujarRuedaCompleta(centerX: Float, centerY: Float, radius: Float) {
    // Neumático de goma negra con labrado exterior
    drawCircle(
        color = Color(0xFF0F172A),
        radius = radius,
        center = Offset(centerX, centerY)
    )
    // Rin de aleación plateado
    drawCircle(
        color = Color(0xFF94A3B8),
        radius = radius * 0.58f,
        center = Offset(centerX, centerY)
    )
    // Rayos / Detalles del aro
    drawCircle(
        color = Color(0xFFE2E8F0),
        radius = radius * 0.42f,
        center = Offset(centerX, centerY)
    )
    // Tapa central oscura del eje
    drawCircle(
        color = Color(0xFF1E293B),
        radius = radius * 0.20f,
        center = Offset(centerX, centerY)
    )
}

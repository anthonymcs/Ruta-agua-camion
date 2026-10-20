package com.rutaagua.camion.ui.components

import android.graphics.Paint
import androidx.annotation.DrawableRes
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Fill
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.rutaagua.camion.data.local.model.TipoBidon
import com.rutaagua.camion.ui.theme.AguaPrimary
import com.rutaagua.camion.ui.theme.BidonAzulColor
import com.rutaagua.camion.ui.theme.BidonAzulReflejo
import com.rutaagua.camion.ui.theme.BidonMarcadoColor
import com.rutaagua.camion.ui.theme.BidonMarcadoTapa
import com.rutaagua.camion.ui.theme.BidonTransparenteColor
import com.rutaagua.camion.ui.theme.BidonTransparenteReflejo
import com.rutaagua.camion.ui.theme.VerdeEfectivo

/**
 * Componente unificado para ilustrar los 3 tipos de bidones de 20L.
 * Si se proporciona [drawableResId], renderiza la imagen real de recursos;
 * de lo contrario dibuja la ilustración vectorial de alta definición mediante Canvas.
 */
@Composable
fun BidonIllustration(
    tipo: TipoBidon,
    modifier: Modifier = Modifier,
    size: Dp = 80.dp,
    @DrawableRes drawableResId: Int? = null
) {
    Box(
        modifier = modifier.size(size),
        contentAlignment = Alignment.Center
    ) {
        if (drawableResId != null) {
            Image(
                painter = painterResource(id = drawableResId),
                contentDescription = "Bidón ${tipo.name}",
                modifier = Modifier.fillMaxSize()
            )
        } else {
            when (tipo) {
                TipoBidon.TRANSPARENTE -> BidonTransparenteCanvas(modifier = Modifier.fillMaxSize())
                TipoBidon.AZUL -> BidonAzulCanvas(modifier = Modifier.fillMaxSize())
                TipoBidon.MARCADO -> BidonMarcadoCanvas(modifier = Modifier.fillMaxSize())
            }
        }
    }
}

/**
 * Bidón Transparente (PET): Color celeste cristalino con reflejos brillantes de agua clara y tapa blanca/celeste.
 */
@Composable
fun BidonTransparenteCanvas(modifier: Modifier = Modifier) {
    Canvas(modifier = modifier) {
        val gradienteCuerpo = Brush.verticalGradient(
            colors = listOf(
                BidonTransparenteReflejo,
                BidonTransparenteColor,
                Color(0xFF0288D1)
            )
        )

        dibujarEstructuraBidon(
            colorCuerpo = gradienteCuerpo,
            colorTapa = Color(0xFFE1F5FE),
            colorBorde = Color(0xFF0288D1),
            colorReflejo = Color.White.copy(alpha = 0.65f),
            esMarcado = false
        )
    }
}

/**
 * Bidón Azul: Cuerpo azul profundo/opaco característico de polietileno con tapa azul.
 */
@Composable
fun BidonAzulCanvas(modifier: Modifier = Modifier) {
    Canvas(modifier = modifier) {
        val gradienteCuerpo = Brush.verticalGradient(
            colors = listOf(
                BidonAzulReflejo,
                BidonAzulColor,
                Color(0xFF0D47A1)
            )
        )

        dibujarEstructuraBidon(
            colorCuerpo = gradienteCuerpo,
            colorTapa = Color(0xFF0D47A1),
            colorBorde = Color(0xFF0D47A1),
            colorReflejo = Color.White.copy(alpha = 0.35f),
            esMarcado = false
        )
    }
}

/**
 * Bidón Marcado: Bidón distintivo con sello/tapa roja llamativa y banda diagonal de advertencia.
 */
@Composable
fun BidonMarcadoCanvas(modifier: Modifier = Modifier) {
    Canvas(modifier = modifier) {
        val gradienteCuerpo = Brush.verticalGradient(
            colors = listOf(
                Color(0xFFFFB74D),
                BidonMarcadoColor,
                Color(0xFFBF360C)
            )
        )

        dibujarEstructuraBidon(
            colorCuerpo = gradienteCuerpo,
            colorTapa = BidonMarcadoTapa, // Tapa roja llamativa
            colorBorde = Color(0xFFBF360C),
            colorReflejo = Color.White.copy(alpha = 0.3f),
            esMarcado = true
        )
    }
}

/**
 * Dibuja la geometría realista de un botellón de 20 litros con sus ranuras ergonómicas.
 */
private fun DrawScope.dibujarEstructuraBidon(
    colorCuerpo: Brush,
    colorTapa: Color,
    colorBorde: Color,
    colorReflejo: Color,
    esMarcado: Boolean
) {
    val w = size.width
    val h = size.height

    val capWidth = w * 0.28f
    val capHeight = h * 0.12f
    val neckWidth = w * 0.34f
    val neckHeight = h * 0.10f
    val bodyWidth = w * 0.82f
    val bodyHeight = h * 0.72f
    val bodyLeft = (w - bodyWidth) / 2f
    val bodyTop = h * 0.22f

    // 1. Tapa del botellón
    val capLeft = (w - capWidth) / 2f
    val capTop = h * 0.04f
    drawRoundRect(
        color = colorTapa,
        topLeft = Offset(capLeft, capTop),
        size = Size(capWidth, capHeight),
        cornerRadius = CornerRadius(w * 0.04f, w * 0.04f)
    )
    drawRoundRect(
        color = colorBorde,
        topLeft = Offset(capLeft, capTop),
        size = Size(capWidth, capHeight),
        cornerRadius = CornerRadius(w * 0.04f, w * 0.04f),
        style = Stroke(width = w * 0.025f)
    )

    // 2. Cuello / asa superior
    val neckLeft = (w - neckWidth) / 2f
    drawRoundRect(
        color = Color.White.copy(alpha = 0.4f),
        topLeft = Offset(neckLeft, capTop + capHeight),
        size = Size(neckWidth, neckHeight),
        cornerRadius = CornerRadius(w * 0.03f, w * 0.03f)
    )

    // 3. Cuerpo principal del bidón de 20L
    drawRoundRect(
        brush = colorCuerpo,
        topLeft = Offset(bodyLeft, bodyTop),
        size = Size(bodyWidth, bodyHeight),
        cornerRadius = CornerRadius(w * 0.14f, w * 0.14f)
    )

    // 4. Ranuras ergonómicas horizontales
    val ringSpacing = bodyHeight * 0.20f
    for (i in 1..3) {
        val y = bodyTop + (i * ringSpacing)
        drawRoundRect(
            color = Color.Black.copy(alpha = 0.12f),
            topLeft = Offset(bodyLeft + (w * 0.04f), y),
            size = Size(bodyWidth - (w * 0.08f), h * 0.035f),
            cornerRadius = CornerRadius(w * 0.02f, w * 0.02f)
        )
    }

    // 5. Reflejo de brillo cristalino vertical
    drawRoundRect(
        color = colorReflejo,
        topLeft = Offset(bodyLeft + (bodyWidth * 0.12f), bodyTop + (h * 0.04f)),
        size = Size(bodyWidth * 0.12f, bodyHeight * 0.82f),
        cornerRadius = CornerRadius(w * 0.06f, w * 0.06f)
    )

    // 6. Si es MARCADO: Dibuja distintivo de advertencia (cinta diagonal llamativa)
    if (esMarcado) {
        val pathMarca = Path().apply {
            moveTo(bodyLeft + (bodyWidth * 0.15f), bodyTop + (bodyHeight * 0.65f))
            lineTo(bodyLeft + (bodyWidth * 0.85f), bodyTop + (bodyHeight * 0.35f))
            lineTo(bodyLeft + (bodyWidth * 0.85f), bodyTop + (bodyHeight * 0.50f))
            lineTo(bodyLeft + (bodyWidth * 0.15f), bodyTop + (bodyHeight * 0.80f))
            close()
        }
        drawPath(
            path = pathMarca,
            color = Color(0xFFD50000),
            style = Fill
        )

        drawCircle(
            color = Color.White,
            radius = w * 0.12f,
            center = Offset(w / 2f, bodyTop + (bodyHeight * 0.52f))
        )
        drawCircle(
            color = Color(0xFFD50000),
            radius = w * 0.12f,
            center = Offset(w / 2f, bodyTop + (bodyHeight * 0.52f)),
            style = Stroke(width = w * 0.03f)
        )
    }

    // Borde exterior del cuerpo
    drawRoundRect(
        color = colorBorde,
        topLeft = Offset(bodyLeft, bodyTop),
        size = Size(bodyWidth, bodyHeight),
        cornerRadius = CornerRadius(w * 0.14f, w * 0.14f),
        style = Stroke(width = w * 0.03f)
    )
}

// =========================================================================
// ILUSTRACIONES PARA LOS OTROS PRODUCTOS DEL CAMIÓN:
// 1) GALÓN (4L) CON AGARRADERA LATERAL
// 2) PACA (PACK 24 BOTELLAS DE 500ML)
// 3) MINI (PACK 12 BOTELLAS DE 500ML)
// =========================================================================

/**
 * 1) Galón (4L): Envase semitranslúcido de 4 litros con agarradera/asa lateral típica y tapa azul.
 */
@Composable
fun GalonIllustration(
    modifier: Modifier = Modifier,
    size: Dp = 60.dp,
    @DrawableRes drawableResId: Int? = null
) {
    Box(
        modifier = modifier.size(size),
        contentAlignment = Alignment.Center
    ) {
        if (drawableResId != null) {
            Image(
                painter = painterResource(id = drawableResId),
                contentDescription = "Galón 4L",
                modifier = Modifier.fillMaxSize()
            )
        } else {
            Canvas(modifier = Modifier.fillMaxSize()) {
                val w = this.size.width
                val h = this.size.height

                // Dimensiones del envase tipo galón
                val bodyW = w * 0.65f
                val bodyH = h * 0.72f
                val bodyL = w * 0.08f
                val bodyT = h * 0.22f

                // 1. Tapa en la parte superior del cuello
                val capW = w * 0.22f
                val capH = h * 0.10f
                val capL = bodyL + (w * 0.04f)
                val capT = h * 0.06f
                drawRoundRect(
                    color = AguaPrimary,
                    topLeft = Offset(capL, capT),
                    size = Size(capW, capH),
                    cornerRadius = CornerRadius(w * 0.03f, w * 0.03f)
                )

                // Cuello del galón
                drawRect(
                    color = Color(0xFFE0F7FA),
                    topLeft = Offset(capL + (w * 0.02f), capT + capH),
                    size = Size(capW - (w * 0.04f), bodyT - (capT + capH))
                )

                // 2. Agarradera / Asa lateral integrada en el costado derecho
                val handlePath = Path().apply {
                    val hL = bodyL + bodyW - (w * 0.05f)
                    val hT = bodyT + (bodyH * 0.12f)
                    val hW = w * 0.24f
                    val hH = bodyH * 0.60f

                    moveTo(hL, hT)
                    cubicTo(hL + hW, hT, hL + hW, hT + hH, hL, hT + hH)
                    lineTo(hL - (w * 0.06f), hT + hH - (h * 0.08f))
                    cubicTo(
                        hL + hW - (w * 0.09f), hT + hH - (h * 0.08f),
                        hL + hW - (w * 0.09f), hT + (h * 0.08f),
                        hL - (w * 0.06f), hT + (h * 0.08f)
                    )
                    close()
                }
                drawPath(
                    path = handlePath,
                    brush = Brush.horizontalGradient(
                        colors = listOf(Color(0xFFE0F7FA), Color(0xFFB2EBF2), Color(0xFF80DEEA))
                    )
                )
                drawPath(
                    path = handlePath,
                    color = AguaPrimary,
                    style = Stroke(width = w * 0.025f)
                )

                // 3. Cuerpo principal del Galón (Translúcido con agua)
                val gradienteGalon = Brush.verticalGradient(
                    colors = listOf(
                        Color(0xFFE0F7FA),
                        Color(0xFFB2EBF2),
                        Color(0xFF4DD0E1)
                    )
                )
                drawRoundRect(
                    brush = gradienteGalon,
                    topLeft = Offset(bodyL, bodyT),
                    size = Size(bodyW, bodyH),
                    cornerRadius = CornerRadius(w * 0.08f, w * 0.08f)
                )

                // Ranuras horizontales del galón
                val rY1 = bodyT + (bodyH * 0.35f)
                val rY2 = bodyT + (bodyH * 0.65f)
                drawRoundRect(
                    color = Color.White.copy(alpha = 0.7f),
                    topLeft = Offset(bodyL + (w * 0.04f), rY1),
                    size = Size(bodyW - (w * 0.08f), h * 0.035f),
                    cornerRadius = CornerRadius(w * 0.02f, w * 0.02f)
                )
                drawRoundRect(
                    color = Color.White.copy(alpha = 0.7f),
                    topLeft = Offset(bodyL + (w * 0.04f), rY2),
                    size = Size(bodyW - (w * 0.08f), h * 0.035f),
                    cornerRadius = CornerRadius(w * 0.02f, w * 0.02f)
                )

                // Nivel de líquido azul puro
                drawRoundRect(
                    color = Color(0xFF00ACC1).copy(alpha = 0.35f),
                    topLeft = Offset(bodyL + (w * 0.02f), bodyT + (bodyH * 0.25f)),
                    size = Size(bodyW - (w * 0.04f), bodyH * 0.70f),
                    cornerRadius = CornerRadius(w * 0.06f, w * 0.06f)
                )

                // Borde del cuerpo
                drawRoundRect(
                    color = AguaPrimary,
                    topLeft = Offset(bodyL, bodyT),
                    size = Size(bodyW, bodyH),
                    cornerRadius = CornerRadius(w * 0.08f, w * 0.08f),
                    style = Stroke(width = w * 0.025f)
                )

                // Badge "4L" en el frente del galón
                drawRoundRect(
                    color = AguaPrimary,
                    topLeft = Offset(bodyL + (w * 0.08f), bodyT + (bodyH * 0.40f)),
                    size = Size(bodyW - (w * 0.16f), h * 0.20f),
                    cornerRadius = CornerRadius(w * 0.04f, w * 0.04f)
                )

                // Texto "4L" centrado
                val paint = Paint().apply {
                    color = android.graphics.Color.WHITE
                    textSize = w * 0.18f
                    isFakeBoldText = true
                    textAlign = Paint.Align.CENTER
                }
                drawContext.canvas.nativeCanvas.drawText(
                    "4L",
                    bodyL + (bodyW / 2f),
                    bodyT + (bodyH * 0.55f),
                    paint
                )
            }
        }
    }
}

/**
 * 2) Paca (Pack 24 botellas de 500ml): Fardo ancho de botellas en plástico termoencogible con distintivo "24x".
 */
@Composable
fun PacaIllustration(
    modifier: Modifier = Modifier,
    size: Dp = 60.dp,
    @DrawableRes drawableResId: Int? = null
) {
    Box(
        modifier = modifier.size(size),
        contentAlignment = Alignment.Center
    ) {
        if (drawableResId != null) {
            Image(
                painter = painterResource(id = drawableResId),
                contentDescription = "Paca 24x",
                modifier = Modifier.fillMaxSize()
            )
        } else {
            Canvas(modifier = Modifier.fillMaxSize()) {
                val w = this.size.width
                val h = this.size.height

                val pacaW = w * 0.90f
                val pacaH = h * 0.76f
                val pacaL = (w - pacaW) / 2f
                val pacaT = (h - pacaH) / 2f

                // 1. Fondo del fardo termoencogible
                drawRoundRect(
                    brush = Brush.verticalGradient(
                        colors = listOf(Color(0xFFE1F5FE), Color(0xFFB3E5FC), Color(0xFF81D4FA))
                    ),
                    topLeft = Offset(pacaL, pacaT),
                    size = Size(pacaW, pacaH),
                    cornerRadius = CornerRadius(w * 0.08f, w * 0.08f)
                )

                // 2. Silueta de las botellas visibles en fila superior (4 botellitas)
                val bottleCount = 4
                val bW = pacaW * 0.18f
                val bH = pacaH * 0.46f
                val gap = (pacaW - (bottleCount * bW)) / (bottleCount + 1)

                for (i in 0 until bottleCount) {
                    val bL = pacaL + gap + (i * (bW + gap))
                    val bT = pacaT + (h * 0.06f)

                    // Tapitas azules en la parte superior
                    drawRoundRect(
                        color = Color(0xFF0277BD),
                        topLeft = Offset(bL + (bW * 0.25f), bT - (h * 0.04f)),
                        size = Size(bW * 0.5f, h * 0.05f),
                        cornerRadius = CornerRadius(w * 0.02f, w * 0.02f)
                    )

                    // Botellitas de agua transparentes
                    drawRoundRect(
                        color = Color.White.copy(alpha = 0.85f),
                        topLeft = Offset(bL, bT),
                        size = Size(bW, bH),
                        cornerRadius = CornerRadius(w * 0.04f, w * 0.04f)
                    )
                    // Agua interior
                    drawRoundRect(
                        color = Color(0xFF29B6F6).copy(alpha = 0.6f),
                        topLeft = Offset(bL + (w * 0.015f), bT + (bH * 0.20f)),
                        size = Size(bW - (w * 0.03f), bH * 0.75f),
                        cornerRadius = CornerRadius(w * 0.02f, w * 0.02f)
                    )
                }

                // 3. Cinta / Banda plástica envolvente del fardo con distintivo "24x"
                val bandT = pacaT + (pacaH * 0.52f)
                val bandH = pacaH * 0.40f
                drawRoundRect(
                    color = AguaPrimary,
                    topLeft = Offset(pacaL, bandT),
                    size = Size(pacaW, bandH),
                    cornerRadius = CornerRadius(w * 0.06f, w * 0.06f)
                )

                // Texto "24x"
                val paintBold = Paint().apply {
                    color = android.graphics.Color.WHITE
                    textSize = w * 0.20f
                    isFakeBoldText = true
                    textAlign = Paint.Align.CENTER
                }
                drawContext.canvas.nativeCanvas.drawText(
                    "24x",
                    w / 2f,
                    bandT + (bandH * 0.58f),
                    paintBold
                )

                // Texto subtítulo "500ml"
                val paintSub = Paint().apply {
                    color = android.graphics.Color.parseColor("#E0F7FA")
                    textSize = w * 0.10f
                    textAlign = Paint.Align.CENTER
                }
                drawContext.canvas.nativeCanvas.drawText(
                    "500ml",
                    w / 2f,
                    bandT + (bandH * 0.88f),
                    paintSub
                )

                // Borde exterior
                drawRoundRect(
                    color = Color(0xFF0277BD),
                    topLeft = Offset(pacaL, pacaT),
                    size = Size(pacaW, pacaH),
                    cornerRadius = CornerRadius(w * 0.08f, w * 0.08f),
                    style = Stroke(width = w * 0.025f)
                )
            }
        }
    }
}

/**
 * 3) Mini (Pack 12 botellas de 500ml): Medio fardo compacto con distintivo visual "12x".
 */
@Composable
fun MiniIllustration(
    modifier: Modifier = Modifier,
    size: Dp = 60.dp,
    @DrawableRes drawableResId: Int? = null
) {
    Box(
        modifier = modifier.size(size),
        contentAlignment = Alignment.Center
    ) {
        if (drawableResId != null) {
            Image(
                painter = painterResource(id = drawableResId),
                contentDescription = "Mini 12x",
                modifier = Modifier.fillMaxSize()
            )
        } else {
            Canvas(modifier = Modifier.fillMaxSize()) {
                val w = this.size.width
                val h = this.size.height

                // Formato más cuadrado/compacto (medio fardo)
                val miniW = w * 0.75f
                val miniH = h * 0.78f
                val miniL = (w - miniW) / 2f
                val miniT = (h - miniH) / 2f

                // 1. Fondo termoencogible
                drawRoundRect(
                    brush = Brush.verticalGradient(
                        colors = listOf(Color(0xFFE8F5E9), Color(0xFFC8E6C9), Color(0xFFA5D6A7))
                    ),
                    topLeft = Offset(miniL, miniT),
                    size = Size(miniW, miniH),
                    cornerRadius = CornerRadius(w * 0.08f, w * 0.08f)
                )

                // 2. Silueta de botellas visibles (2 botellas compactas frontales)
                val bottleCount = 2
                val bW = miniW * 0.32f
                val bH = miniH * 0.44f
                val gap = (miniW - (bottleCount * bW)) / (bottleCount + 1)

                for (i in 0 until bottleCount) {
                    val bL = miniL + gap + (i * (bW + gap))
                    val bT = miniT + (h * 0.06f)

                    // Tapitas verdes/azules
                    drawRoundRect(
                        color = VerdeEfectivo,
                        topLeft = Offset(bL + (bW * 0.25f), bT - (h * 0.04f)),
                        size = Size(bW * 0.5f, h * 0.05f),
                        cornerRadius = CornerRadius(w * 0.02f, w * 0.02f)
                    )

                    // Cuerpo botella
                    drawRoundRect(
                        color = Color.White.copy(alpha = 0.85f),
                        topLeft = Offset(bL, bT),
                        size = Size(bW, bH),
                        cornerRadius = CornerRadius(w * 0.04f, w * 0.04f)
                    )
                    // Agua interior
                    drawRoundRect(
                        color = Color(0xFF81C784).copy(alpha = 0.5f),
                        topLeft = Offset(bL + (w * 0.015f), bT + (bH * 0.20f)),
                        size = Size(bW - (w * 0.03f), bH * 0.75f),
                        cornerRadius = CornerRadius(w * 0.02f, w * 0.02f)
                    )
                }

                // 3. Cinta envolvente compacta con distintivo "12x"
                val bandT = miniT + (miniH * 0.50f)
                val bandH = miniH * 0.42f
                drawRoundRect(
                    color = VerdeEfectivo,
                    topLeft = Offset(miniL, bandT),
                    size = Size(miniW, bandH),
                    cornerRadius = CornerRadius(w * 0.06f, w * 0.06f)
                )

                // Texto "12x"
                val paintBold = Paint().apply {
                    color = android.graphics.Color.WHITE
                    textSize = w * 0.20f
                    isFakeBoldText = true
                    textAlign = Paint.Align.CENTER
                }
                drawContext.canvas.nativeCanvas.drawText(
                    "12x",
                    w / 2f,
                    bandT + (bandH * 0.58f),
                    paintBold
                )

                // Texto subtítulo "500ml"
                val paintSub = Paint().apply {
                    color = android.graphics.Color.parseColor("#E8F5E9")
                    textSize = w * 0.10f
                    textAlign = Paint.Align.CENTER
                }
                drawContext.canvas.nativeCanvas.drawText(
                    "500ml",
                    w / 2f,
                    bandT + (bandH * 0.88f),
                    paintSub
                )

                // Borde exterior
                drawRoundRect(
                    color = VerdeEfectivo,
                    topLeft = Offset(miniL, miniT),
                    size = Size(miniW, miniH),
                    cornerRadius = CornerRadius(w * 0.08f, w * 0.08f),
                    style = Stroke(width = w * 0.025f)
                )
            }
        }
    }
}

/**
 * Ilustración vectorial del Camión Repartidor de Agua Purificada para encabezados.
 */
@Composable
fun CamionRepartidorIllustration(
    modifier: Modifier = Modifier,
    size: Dp = 100.dp
) {
    Canvas(modifier = modifier.size(size)) {
        val w = this.size.width
        val h = this.size.height

        // 1. Tolva / Carrocería de carga (Azul Corporativo)
        val cargoLeft = w * 0.05f
        val cargoTop = h * 0.28f
        val cargoWidth = w * 0.62f
        val cargoHeight = h * 0.45f
        drawRoundRect(
            color = AguaPrimary,
            topLeft = Offset(cargoLeft, cargoTop),
            size = Size(cargoWidth, cargoHeight),
            cornerRadius = CornerRadius(w * 0.04f, w * 0.04f)
        )

        // Botellones visibles dentro de la tolva
        val bottleW = cargoWidth * 0.18f
        val bottleH = cargoHeight * 0.45f
        for (i in 0..3) {
            val bX = cargoLeft + (w * 0.04f) + (i * bottleW * 1.3f)
            val bY = cargoTop + (w * 0.04f)
            drawRoundRect(
                color = if (i % 2 == 0) BidonTransparenteColor else BidonAzulColor,
                topLeft = Offset(bX, bY),
                size = Size(bottleW, bottleH),
                cornerRadius = CornerRadius(w * 0.02f, w * 0.02f)
            )
        }

        // 2. Cabina del chofer
        val cabLeft = cargoLeft + cargoWidth + (w * 0.02f)
        val cabTop = h * 0.38f
        val cabWidth = w * 0.26f
        val cabHeight = h * 0.35f
        val cabPath = Path().apply {
            moveTo(cabLeft, cabTop + cabHeight)
            lineTo(cabLeft, cabTop)
            lineTo(cabLeft + (cabWidth * 0.65f), cabTop)
            lineTo(cabLeft + cabWidth, cabTop + (cabHeight * 0.45f))
            lineTo(cabLeft + cabWidth, cabTop + cabHeight)
            close()
        }
        drawPath(path = cabPath, color = Color(0xFF0288D1))

        // Ventana de la cabina
        val winPath = Path().apply {
            moveTo(cabLeft + (w * 0.03f), cabTop + (h * 0.03f))
            lineTo(cabLeft + (cabWidth * 0.55f), cabTop + (h * 0.03f))
            lineTo(cabLeft + (cabWidth * 0.85f), cabTop + (cabHeight * 0.42f))
            lineTo(cabLeft + (w * 0.03f), cabTop + (cabHeight * 0.42f))
            close()
        }
        drawPath(path = winPath, color = Color(0xFFE0F7FA))

        // Faro delantero
        drawCircle(
            color = Color(0xFFFFD54F),
            radius = w * 0.03f,
            center = Offset(cabLeft + cabWidth - (w * 0.02f), cabTop + (cabHeight * 0.70f))
        )

        // 3. Chasis inferior
        drawRect(
            color = Color(0xFF263238),
            topLeft = Offset(cargoLeft, cargoTop + cargoHeight),
            size = Size(w * 0.88f, h * 0.08f)
        )

        // 4. Ruedas del camión (Neumático + Aro)
        val wheelRadius = w * 0.09f
        val wheelY = cargoTop + cargoHeight + (h * 0.06f)

        dibujarRueda(cargoLeft + (w * 0.16f), wheelY, wheelRadius)
        dibujarRueda(cargoLeft + (w * 0.36f), wheelY, wheelRadius)
        dibujarRueda(cabLeft + (cabWidth * 0.55f), wheelY, wheelRadius)
    }
}

private fun DrawScope.dibujarRueda(centerX: Float, centerY: Float, radius: Float) {
    drawCircle(
        color = Color(0xFF212121),
        radius = radius,
        center = Offset(centerX, centerY)
    )
    drawCircle(
        color = Color(0xFFCFD8DC),
        radius = radius * 0.55f,
        center = Offset(centerX, centerY)
    )
    drawCircle(
        color = Color(0xFF455A64),
        radius = radius * 0.22f,
        center = Offset(centerX, centerY)
    )
}

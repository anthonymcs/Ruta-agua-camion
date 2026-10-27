package com.rutaagua.camion.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.rutaagua.camion.data.local.model.TipoVehiculo
import com.rutaagua.camion.ui.theme.AguaPrimary
import com.rutaagua.camion.ui.theme.CamionClayCanvas
import com.rutaagua.camion.ui.theme.CamionClayPrimary
import com.rutaagua.camion.ui.theme.CamionClayPrimaryDark
import com.rutaagua.camion.ui.theme.CamionClaySoft
import com.rutaagua.camion.ui.theme.CamionetaClayCanvas
import com.rutaagua.camion.ui.theme.CamionetaClayPrimary
import com.rutaagua.camion.ui.theme.CamionetaClayPrimaryDark
import com.rutaagua.camion.ui.theme.CamionetaClaySoft
import com.rutaagua.camion.ui.theme.ClayInsetBackground
import com.rutaagua.camion.ui.theme.ClayRimLight
import com.rutaagua.camion.ui.theme.ClayRimShadow
import com.rutaagua.camion.ui.theme.SuperficieTarjeta
import com.rutaagua.camion.ui.theme.TextoOscuro
import com.rutaagua.camion.ui.theme.TextoSecundario

/**
 * Tema operativo dinámico según el vehículo activo (Camión vs Camioneta).
 */
data class VehicleClayTheme(
    val tipo: TipoVehiculo,
    val nombreCorto: String,
    val nombreCompleto: String,
    val canvasColor: Color,
    val primary: Color,
    val primaryDark: Color,
    val softContainer: Color,
    val metasBidonesPreset: List<Int>,
    val topeBidonesDefault: Int,
    val topeGalonesDefault: Int,
    val topePacasDefault: Int,
    val topeMinisDefault: Int
) {
    val cardColor: Color get() = SuperficieTarjeta
    val primaryColor: Color get() = primary
    val primaryDarkColor: Color get() = primaryDark
    val accentBgColor: Color get() = softContainer
    val insetColor: Color get() = ClayInsetBackground
    val badgeLabel: String get() = if (tipo == TipoVehiculo.CAMIONETA) "CAMIONETA" else "CAMIÓN"
    val vehicleTitle: String get() = nombreCompleto
    val capacityLabel: String get() = "Tope ${topeBidonesDefault} bidones"
    val capacidadDefault: Int get() = topeBidonesDefault
}

typealias ClayVehicleTheme = VehicleClayTheme

fun obtenerClayThemePorVehiculo(vehiculo: TipoVehiculo): VehicleClayTheme {
    return if (vehiculo == TipoVehiculo.CAMIONETA) {
        VehicleClayTheme(
            tipo = TipoVehiculo.CAMIONETA,
            nombreCorto = "Camioneta",
            nombreCompleto = "Unidad Camioneta",
            canvasColor = CamionetaClayCanvas,
            primary = CamionetaClayPrimary,
            primaryDark = CamionetaClayPrimaryDark,
            softContainer = CamionetaClaySoft,
            metasBidonesPreset = listOf(80, 65, 50, 40),
            topeBidonesDefault = 80,
            topeGalonesDefault = 30,
            topePacasDefault = 4,
            topeMinisDefault = 2
        )
    } else {
        VehicleClayTheme(
            tipo = TipoVehiculo.CAMION,
            nombreCorto = "Camión",
            nombreCompleto = "Unidad Camión",
            canvasColor = CamionClayCanvas,
            primary = CamionClayPrimary,
            primaryDark = CamionClayPrimaryDark,
            softContainer = CamionClaySoft,
            metasBidonesPreset = listOf(218, 189, 164, 163),
            topeBidonesDefault = 218,
            topeGalonesDefault = 90,
            topePacasDefault = 6,
            topeMinisDefault = 2
        )
    }
}

/**
 * Superficie Claymorphic esculpida con bordes redondeados suaves, bisel luminoso superior
 * y sombra volumétrica suave.
 */
@Composable
fun ClayCard(
    modifier: Modifier = Modifier,
    containerColor: Color = SuperficieTarjeta,
    accentBorderColor: Color? = null,
    cornerRadius: Dp = 22.dp,
    elevation: Dp = 6.dp,
    contentPadding: PaddingValues = PaddingValues(16.dp),
    onClick: (() -> Unit)? = null,
    content: @Composable ColumnScope.() -> Unit
) {
    val shape = RoundedCornerShape(cornerRadius)
    val rimBrush = if (accentBorderColor != null) {
        Brush.linearGradient(
            colors = listOf(
                accentBorderColor.copy(alpha = 0.55f),
                accentBorderColor.copy(alpha = 0.25f)
            )
        )
    } else {
        Brush.linearGradient(
            colors = listOf(
                ClayRimLight,
                ClayRimShadow.copy(alpha = 0.75f)
            )
        )
    }

    val baseModifier = modifier
        .shadow(
            elevation = elevation,
            shape = shape,
            ambientColor = Color(0xFF0F172A).copy(alpha = 0.14f),
            spotColor = Color(0xFF0F172A).copy(alpha = 0.18f)
        )
        .clip(shape)
        .background(containerColor, shape)
        .border(width = 1.5.dp, brush = rimBrush, shape = shape)
        .then(if (onClick != null) Modifier.clickable(onClick = onClick) else Modifier)
        .padding(contentPadding)

    Column(
        modifier = baseModifier,
        content = content
    )
}

/**
 * Contenedor Clay hundido (Inset) para agrupar controles, contadores o métricas internas.
 */
@Composable
fun ClayInsetBox(
    modifier: Modifier = Modifier,
    backgroundColor: Color = ClayInsetBackground,
    containerColor: Color = backgroundColor,
    cornerRadius: Dp = 16.dp,
    contentPadding: PaddingValues = PaddingValues(horizontal = 12.dp, vertical = 10.dp),
    content: @Composable () -> Unit
) {
    val shape = RoundedCornerShape(cornerRadius)
    val insetBorder = Brush.linearGradient(
        colors = listOf(
            ClayRimShadow.copy(alpha = 0.7f),
            ClayRimLight
        )
    )
    Box(
        modifier = modifier
            .clip(shape)
            .background(containerColor, shape)
            .border(1.dp, insetBorder, shape)
            .padding(contentPadding)
    ) {
        content()
    }
}

/**
 * Botón táctil Claymorphic con volumen 3D suave.
 */
@Composable
fun ClayButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    containerColor: Color = AguaPrimary,
    contentColor: Color = Color.White,
    cornerRadius: Dp = 16.dp,
    height: Dp = 52.dp,
    contentPadding: PaddingValues = ButtonDefaults.ContentPadding,
    enabled: Boolean = true,
    content: @Composable RowScope.() -> Unit
) {
    val shape = RoundedCornerShape(cornerRadius)
    Button(
        onClick = onClick,
        enabled = enabled,
        shape = shape,
        contentPadding = contentPadding,
        colors = ButtonDefaults.buttonColors(
            containerColor = containerColor,
            contentColor = contentColor
        ),
        elevation = ButtonDefaults.buttonElevation(
            defaultElevation = 5.dp,
            pressedElevation = 1.dp
        ),
        border = BorderStroke(
            1.dp,
            Brush.verticalGradient(
                listOf(
                    Color.White.copy(alpha = 0.35f),
                    Color.Black.copy(alpha = 0.12f)
                )
            )
        ),
        modifier = modifier.height(height),
        content = content
    )
}

/**
 * Contador numérico Clay con ingreso manual directo por teclado además de botones - / +.
 * Permite escribir rápidamente cantidades grandes (ej. 20, 50, 120) sin tener que pulsar + repetidamente.
 */
@Composable
fun ClayEditableCounter(
    valor: Int = 0,
    onCambio: (Int) -> Unit = {},
    value: Int = valor,
    onValueChange: (Int) -> Unit = onCambio,
    modifier: Modifier = Modifier,
    accentColor: Color = AguaPrimary,
    compact: Boolean = false
) {
    val actualValue = value
    val actualOnChange = onValueChange
    var textoInterno by remember(actualValue) { mutableStateOf(if (actualValue == 0) "0" else actualValue.toString()) }
    var tieneFoco by remember { mutableStateOf(false) }

    LaunchedEffect(actualValue, tieneFoco) {
        if (!tieneFoco) {
            textoInterno = actualValue.toString()
        }
    }

    val btnSize = if (compact) 30.dp else 38.dp
    val fieldWidth = if (compact) 46.dp else 62.dp
    val fontSize = if (compact) 15.sp else 18.sp

    Row(
        modifier = modifier,
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(if (compact) 4.dp else 6.dp)
    ) {
        // Botón Menos (-)
        Surface(
            onClick = {
                val nuevo = maxOf(0, actualValue - 1)
                textoInterno = nuevo.toString()
                actualOnChange(nuevo)
            },
            shape = RoundedCornerShape(if (compact) 10.dp else 12.dp),
            color = Color(0xFFE2E8F0),
            border = BorderStroke(1.dp, Color.White),
            shadowElevation = 2.dp,
            modifier = Modifier.size(btnSize)
        ) {
            Box(contentAlignment = Alignment.Center) {
                Icon(
                    imageVector = Icons.Default.Remove,
                    contentDescription = "Restar",
                    tint = TextoOscuro,
                    modifier = Modifier.size(if (compact) 16.dp else 20.dp)
                )
            }
        }

        // Campo Editable Directo
        val boxShape = RoundedCornerShape(if (compact) 10.dp else 12.dp)
        Box(
            modifier = Modifier
                .width(fieldWidth)
                .height(btnSize)
                .clip(boxShape)
                .background(
                    if (tieneFoco) Color.White else ClayInsetBackground,
                    boxShape
                )
                .border(
                    width = if (tieneFoco) 2.dp else 1.dp,
                    color = if (tieneFoco) accentColor else ClayRimShadow,
                    shape = boxShape
                ),
            contentAlignment = Alignment.Center
        ) {
            BasicTextField(
                value = textoInterno,
                onValueChange = { raw ->
                    val limpio = raw.filter { it.isDigit() }.take(4)
                    if (limpio.isEmpty()) {
                        textoInterno = ""
                        actualOnChange(0)
                    } else {
                        val sinCeroInicial = if (limpio.length > 1 && limpio.startsWith("0")) {
                            limpio.trimStart('0').ifEmpty { "0" }
                        } else {
                            limpio
                        }
                        textoInterno = sinCeroInicial
                        actualOnChange(sinCeroInicial.toIntOrNull() ?: 0)
                    }
                },
                singleLine = true,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                cursorBrush = SolidColor(accentColor),
                textStyle = TextStyle(
                    color = if (actualValue > 0) accentColor else TextoOscuro,
                    fontSize = fontSize,
                    fontWeight = FontWeight.Black,
                    textAlign = TextAlign.Center
                ),
                modifier = Modifier
                    .fillMaxWidth()
                    .onFocusChanged { focusState ->
                        tieneFoco = focusState.isFocused
                        if (focusState.isFocused && textoInterno == "0") {
                            textoInterno = ""
                        } else if (!focusState.isFocused && textoInterno.isEmpty()) {
                            textoInterno = actualValue.toString()
                        }
                    }
            )
        }

        // Botón Más (+)
        Surface(
            onClick = {
                val nuevo = actualValue + 1
                textoInterno = nuevo.toString()
                actualOnChange(nuevo)
            },
            shape = RoundedCornerShape(if (compact) 10.dp else 12.dp),
            color = accentColor,
            border = BorderStroke(1.dp, Color.White.copy(alpha = 0.35f)),
            shadowElevation = 3.dp,
            modifier = Modifier.size(btnSize)
        ) {
            Box(contentAlignment = Alignment.Center) {
                Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = "Sumar",
                    tint = Color.White,
                    modifier = Modifier.size(if (compact) 16.dp else 20.dp)
                )
            }
        }
    }
}

/**
 * Etiqueta / Insignia Clay compacta.
 */
@Composable
fun ClayBadge(
    text: String,
    containerColor: Color = Color(0xFFE0F2FE),
    contentColor: Color = AguaPrimary,
    backgroundColor: Color = containerColor,
    textColor: Color = contentColor,
    modifier: Modifier = Modifier
) {
    Surface(
        color = backgroundColor,
        shape = RoundedCornerShape(10.dp),
        border = BorderStroke(1.dp, textColor.copy(alpha = 0.2f)),
        modifier = modifier
    ) {
        Text(
            text = text,
            color = textColor,
            fontWeight = FontWeight.Bold,
            fontSize = 11.sp,
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
        )
    }
}

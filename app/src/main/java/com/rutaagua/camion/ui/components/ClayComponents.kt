package com.rutaagua.camion.ui.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.gestures.waitForUpOrCancellation
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
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
import androidx.compose.ui.composed
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.rutaagua.camion.data.local.model.TipoVehiculo
import com.rutaagua.camion.ui.theme.AguaPrimary
import com.rutaagua.camion.ui.theme.BordeSuave
import com.rutaagua.camion.ui.theme.CamionClayCanvas
import com.rutaagua.camion.ui.theme.CamionClayPrimary
import com.rutaagua.camion.ui.theme.CamionClayPrimaryDark
import com.rutaagua.camion.ui.theme.CamionClaySoft
import com.rutaagua.camion.ui.theme.CamionetaClayCanvas
import com.rutaagua.camion.ui.theme.CamionetaClayPrimary
import com.rutaagua.camion.ui.theme.CamionetaClayPrimaryDark
import com.rutaagua.camion.ui.theme.CamionetaClaySoft
import com.rutaagua.camion.ui.theme.ClayInsetBackground
import com.rutaagua.camion.ui.theme.ClayRimShadow
import com.rutaagua.camion.ui.theme.PichinchaNavy
import com.rutaagua.camion.ui.theme.PichinchaYellow
import com.rutaagua.camion.ui.theme.SuperficieTarjeta
import com.rutaagua.camion.ui.theme.TextoOscuro
import com.rutaagua.camion.ui.theme.TextoSecundario

/**
 * Tema operativo dinámico estilo Banca Móvil según el vehículo activo (Camión vs Camioneta).
 */
data class VehicleClayTheme(
    val tipo: TipoVehiculo,
    val nombreCorto: String,
    val nombreCompleto: String,
    val canvasColor: Color,
    val primary: Color,
    val primaryDark: Color,
    val softContainer: Color,
    val ctaBackground: Color,
    val ctaContent: Color,
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
    val capacityLabel: String get() = "${topeBidonesDefault} bidones máx."
    val capacidadDefault: Int get() = topeBidonesDefault
}

typealias ClayVehicleTheme = VehicleClayTheme

fun obtenerClayThemePorVehiculo(vehiculo: TipoVehiculo): VehicleClayTheme {
    return if (vehiculo == TipoVehiculo.CAMIONETA) {
        VehicleClayTheme(
            tipo = TipoVehiculo.CAMIONETA,
            nombreCorto = "Camioneta",
            nombreCompleto = "Camioneta",
            canvasColor = CamionetaClayCanvas,
            primary = CamionetaClayPrimary,
            primaryDark = CamionetaClayPrimaryDark,
            softContainer = CamionetaClaySoft,
            ctaBackground = PichinchaYellow,
            ctaContent = PichinchaNavy,
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
            nombreCompleto = "Camión",
            canvasColor = CamionClayCanvas,
            primary = CamionClayPrimary,
            primaryDark = CamionClayPrimaryDark,
            softContainer = CamionClaySoft,
            ctaBackground = PichinchaYellow,
            ctaContent = PichinchaNavy,
            metasBidonesPreset = listOf(218, 189, 164, 163),
            topeBidonesDefault = 218,
            topeGalonesDefault = 90,
            topePacasDefault = 6,
            topeMinisDefault = 2
        )
    }
}

/**
 * Efecto reactivo de rebote táctil al presionar cualquier elemento interactivo.
 */
fun Modifier.bounceClick(scaleDown: Float = 0.96f): Modifier = composed {
    var isPressed by remember { mutableStateOf(false) }
    val scale by animateFloatAsState(
        targetValue = if (isPressed) scaleDown else 1f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessMedium
        ),
        label = "bounceScale"
    )
    this
        .scale(scale)
        .pointerInput(Unit) {
            while (true) {
                awaitPointerEventScope {
                    awaitFirstDown(requireUnconsumed = false)
                    isPressed = true
                    waitForUpOrCancellation()
                    isPressed = false
                }
            }
        }
}

/**
 * Tarjeta blanca estilo Banca Móvil con bordes limpios, sombra suave y respuesta táctil.
 */
@Composable
fun ClayCard(
    modifier: Modifier = Modifier,
    containerColor: Color = SuperficieTarjeta,
    accentBorderColor: Color? = null,
    cornerRadius: Dp = 20.dp,
    elevation: Dp = 4.dp,
    contentPadding: PaddingValues = PaddingValues(16.dp),
    onClick: (() -> Unit)? = null,
    content: @Composable ColumnScope.() -> Unit
) {
    val shape = RoundedCornerShape(cornerRadius)
    val borderColor = accentBorderColor ?: BordeSuave

    val baseModifier = modifier
        .then(if (onClick != null) Modifier.bounceClick(0.97f) else Modifier)
        .shadow(
            elevation = elevation,
            shape = shape,
            ambientColor = PichinchaNavy.copy(alpha = 0.08f),
            spotColor = PichinchaNavy.copy(alpha = 0.12f)
        )
        .clip(shape)
        .background(containerColor, shape)
        .border(width = if (accentBorderColor != null) 1.5.dp else 1.dp, color = borderColor, shape = shape)
        .then(if (onClick != null) Modifier.clickable(onClick = onClick) else Modifier)
        .padding(contentPadding)

    Column(
        modifier = baseModifier,
        content = content
    )
}

/**
 * Contenedor interno sutil para agrupar métricas o campos.
 */
@Composable
fun ClayInsetBox(
    modifier: Modifier = Modifier,
    backgroundColor: Color = ClayInsetBackground,
    containerColor: Color = backgroundColor,
    cornerRadius: Dp = 14.dp,
    contentPadding: PaddingValues = PaddingValues(horizontal = 12.dp, vertical = 10.dp),
    content: @Composable () -> Unit
) {
    val shape = RoundedCornerShape(cornerRadius)
    Box(
        modifier = modifier
            .clip(shape)
            .background(containerColor, shape)
            .border(1.dp, BordeSuave, shape)
            .padding(contentPadding)
    ) {
        content()
    }
}

/**
 * Botón estilo Banca Móvil con contraste automático garantizado (nunca se pierde el texto)
 * y animación reactiva al presionar.
 */
@Composable
fun ClayButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    containerColor: Color = PichinchaYellow,
    contentColor: Color = Color.Unspecified,
    cornerRadius: Dp = 14.dp,
    height: Dp = 50.dp,
    contentPadding: PaddingValues = PaddingValues(horizontal = 14.dp, vertical = 8.dp),
    enabled: Boolean = true,
    content: @Composable RowScope.() -> Unit
) {
    val resolvedContentColor = if (contentColor != Color.Unspecified) {
        // Si el fondo es claro (ej. Amarillo Pichincha o Gris claro) y pasaron Blanco por error, forzamos Azul Marino
        if (containerColor.luminance() > 0.45f && contentColor.luminance() > 0.7f) {
            PichinchaNavy
        } else if (containerColor.luminance() <= 0.45f && contentColor.luminance() < 0.3f) {
            Color.White
        } else {
            contentColor
        }
    } else {
        if (containerColor.luminance() > 0.45f) PichinchaNavy else Color.White
    }

    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    val scale by animateFloatAsState(
        targetValue = if (isPressed) 0.96f else 1f,
        animationSpec = spring(stiffness = Spring.StiffnessMedium),
        label = "btnScale"
    )

    val shape = RoundedCornerShape(cornerRadius)
    Button(
        onClick = onClick,
        enabled = enabled,
        interactionSource = interactionSource,
        shape = shape,
        contentPadding = contentPadding,
        colors = ButtonDefaults.buttonColors(
            containerColor = containerColor,
            contentColor = resolvedContentColor,
            disabledContainerColor = Color(0xFFE2E8F0),
            disabledContentColor = Color(0xFF94A3B8)
        ),
        elevation = ButtonDefaults.buttonElevation(
            defaultElevation = 2.dp,
            pressedElevation = 0.dp
        ),
        modifier = modifier
            .scale(scale)
            .height(height),
        content = content
    )
}

/**
 * Botón rápido estilo Banca Móvil Pichincha (Ícono en recuadro + etiqueta de 1 línea).
 */
@Composable
fun BankingQuickAction(
    icon: ImageVector,
    label: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    isPrimaryGold: Boolean = false,
    iconTint: Color = PichinchaNavy,
    containerColor: Color = Color.White
) {
    val bg = if (isPrimaryGold) PichinchaYellow else containerColor
    val fg = if (isPrimaryGold) PichinchaNavy else iconTint

    Surface(
        onClick = onClick,
        color = bg,
        shape = RoundedCornerShape(16.dp),
        border = BorderStroke(1.dp, if (isPrimaryGold) Color(0xFFE5C100) else BordeSuave),
        shadowElevation = 2.dp,
        modifier = modifier.bounceClick(0.95f)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 10.dp, horizontal = 6.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Box(
                modifier = Modifier
                    .size(36.dp)
                    .background(
                        if (isPrimaryGold) PichinchaNavy.copy(alpha = 0.1f) else fg.copy(alpha = 0.1f),
                        RoundedCornerShape(11.dp)
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = label,
                    tint = fg,
                    modifier = Modifier.size(20.dp)
                )
            }
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = label,
                color = PichinchaNavy,
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                textAlign = TextAlign.Center
            )
        }
    }
}

/**
 * Selector de pestañas segmentadas estilo Banca Móvil (ej. Día | Semanal | Mensual).
 */
@Composable
fun <T> BankingSegmentedTabs(
    options: List<T>,
    selectedOption: T,
    labelProvider: (T) -> String,
    onOptionSelected: (T) -> Unit,
    modifier: Modifier = Modifier,
    activeColor: Color = PichinchaNavy
) {
    Surface(
        color = Color(0xFFE6ECF5),
        shape = RoundedCornerShape(14.dp),
        modifier = modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(4.dp),
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            options.forEach { option ->
                val selected = option == selectedOption
                val bgColor by animateColorAsState(
                    targetValue = if (selected) activeColor else Color.Transparent,
                    label = "tabBg"
                )
                val txtColor by animateColorAsState(
                    targetValue = if (selected) Color.White else TextoSecundario,
                    label = "tabTxt"
                )

                Surface(
                    onClick = { onOptionSelected(option) },
                    color = bgColor,
                    shape = RoundedCornerShape(10.dp),
                    shadowElevation = if (selected) 2.dp else 0.dp,
                    modifier = Modifier.weight(1f)
                ) {
                    Box(
                        modifier = Modifier.padding(vertical = 9.dp, horizontal = 6.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = labelProvider(option),
                            color = txtColor,
                            fontSize = 12.sp,
                            fontWeight = if (selected) FontWeight.Black else FontWeight.SemiBold,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }
            }
        }
    }
}

/**
 * Contador numérico con ingreso manual directo por teclado además de botones - / +.
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

    val btnSize = if (compact) 32.dp else 38.dp
    val fieldWidth = if (compact) 48.dp else 60.dp
    val fontSize = if (compact) 15.sp else 17.sp

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
            shape = RoundedCornerShape(10.dp),
            color = Color(0xFFE6ECF5),
            modifier = Modifier.size(btnSize)
        ) {
            Box(contentAlignment = Alignment.Center) {
                Icon(
                    imageVector = Icons.Default.Remove,
                    contentDescription = "Restar",
                    tint = PichinchaNavy,
                    modifier = Modifier.size(if (compact) 16.dp else 18.dp)
                )
            }
        }

        // Campo Editable Directo
        val boxShape = RoundedCornerShape(10.dp)
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
                    color = if (actualValue > 0) accentColor else PichinchaNavy,
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
            shape = RoundedCornerShape(10.dp),
            color = accentColor,
            modifier = Modifier.size(btnSize)
        ) {
            Box(contentAlignment = Alignment.Center) {
                Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = "Sumar",
                    tint = Color.White,
                    modifier = Modifier.size(if (compact) 16.dp else 18.dp)
                )
            }
        }
    }
}

/**
 * Etiqueta / Insignia compacta de alto contraste.
 */
@Composable
fun ClayBadge(
    text: String,
    containerColor: Color = Color(0xFFE6ECF5),
    contentColor: Color = PichinchaNavy,
    backgroundColor: Color = containerColor,
    textColor: Color = contentColor,
    modifier: Modifier = Modifier
) {
    Surface(
        color = backgroundColor,
        shape = RoundedCornerShape(8.dp),
        modifier = modifier
    ) {
        Text(
            text = text,
            color = textColor,
            fontWeight = FontWeight.Bold,
            fontSize = 11.sp,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
        )
    }
}

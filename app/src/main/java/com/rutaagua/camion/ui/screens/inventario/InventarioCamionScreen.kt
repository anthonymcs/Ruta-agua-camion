package com.rutaagua.camion.ui.screens.inventario

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.DeleteOutline
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.LocalShipping
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.rutaagua.camion.data.local.entity.InventarioCamionEntity
import com.rutaagua.camion.data.local.entity.RecargaPlantaEntity
import com.rutaagua.camion.data.local.model.EstadisticasVentasTipo
import com.rutaagua.camion.data.local.model.RecargaCalculada
import com.rutaagua.camion.data.local.model.StockAlertas
import com.rutaagua.camion.data.local.model.TipoBidon
import com.rutaagua.camion.ui.components.BidonIllustration
import com.rutaagua.camion.ui.components.ClayBadge
import com.rutaagua.camion.ui.components.ClayButton
import com.rutaagua.camion.ui.components.ClayCard
import com.rutaagua.camion.ui.components.ClayEditableCounter
import com.rutaagua.camion.ui.components.ClayInsetBox
import com.rutaagua.camion.ui.components.ClayVehicleTheme
import com.rutaagua.camion.ui.components.GalonIllustration
import com.rutaagua.camion.ui.components.MiniIllustration
import com.rutaagua.camion.ui.components.PacaIllustration
import com.rutaagua.camion.ui.components.marineOutlinedTextFieldColors
import com.rutaagua.camion.ui.components.obtenerClayThemePorVehiculo
import com.rutaagua.camion.ui.theme.AmbarAdvertencia
import com.rutaagua.camion.ui.theme.BidonAzulColor
import com.rutaagua.camion.ui.theme.BidonAzulFondo
import com.rutaagua.camion.ui.theme.BidonMarcadoColor
import com.rutaagua.camion.ui.theme.BidonMarcadoFondo
import com.rutaagua.camion.ui.theme.BidonTransparenteColor
import com.rutaagua.camion.ui.theme.BidonTransparenteFondo
import com.rutaagua.camion.ui.theme.RojoAlerta
import com.rutaagua.camion.ui.theme.RojoAlertaFondo
import com.rutaagua.camion.ui.theme.TextoOscuro
import com.rutaagua.camion.ui.theme.TextoSecundario
import com.rutaagua.camion.ui.theme.VerdeEfectivo
import com.rutaagua.camion.ui.theme.VerdeEfectivoFondo
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun InventarioCamionScreen(
    viewModel: InventarioCamionViewModel,
    onCambiarVehiculoClick: (() -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    val inventario by viewModel.inventarioState.collectAsState()
    val vehiculoActivo by viewModel.vehiculoActivoState.collectAsState()
    val calculoRecarga by viewModel.calculoRecargaState.collectAsState()
    val alertasStock by viewModel.alertasStockState.collectAsState()
    val estadisticasVentas by viewModel.estadisticasVentasHoyState.collectAsState()
    val mostrarDialogoCapacidad by viewModel.mostrarDialogoAjusteCapacidad.collectAsState()
    val mostrarModalRecarga by viewModel.mostrarModalRecargaPlanta.collectAsState()
    val mensajeSnackbar by viewModel.mensajeSnackbar.collectAsState()
    val recargasHoy by viewModel.recargasHoyState.collectAsState()
    val recargaEnEdicion by viewModel.recargaEnEdicion.collectAsState()

    val clayTheme = obtenerClayThemePorVehiculo(vehiculoActivo)

    var recargaParaEliminar by remember { mutableStateOf<RecargaPlantaEntity?>(null) }
    var recargaParaEditar by remember { mutableStateOf<RecargaPlantaEntity?>(null) }
    var mostrarPinParaEditarRecarga by rememberSaveable { mutableStateOf(false) }
    var mostrarPinParaEliminarRecarga by rememberSaveable { mutableStateOf(false) }

    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(mensajeSnackbar) {
        mensajeSnackbar?.let {
            snackbarHostState.showSnackbar(it)
            viewModel.limpiarMensajeSnackbar()
        }
    }

    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    Scaffold(
        modifier = modifier,
        containerColor = clayTheme.canvasColor,
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
            contentPadding = PaddingValues(top = 6.dp, bottom = 92.dp)
        ) {
            // 1. Alerta de Stock Crítico
            if (alertasStock.mensajeAlerta != null) {
                item {
                    AnimatedVisibility(visible = true) {
                        BannerAlertaStockClay(alertasStock)
                    }
                }
            }

            // 2. Tarjeta de Capacidad y Carga Actual
            item {
                inventario?.let { inv ->
                    TarjetaCapacidadCamionClay(
                        inventario = inv,
                        clayTheme = clayTheme,
                        onAjustarTopeClick = { viewModel.abrirDialogoCapacidad() }
                    )
                }
            }

            // 3. Botón Principal de Recarga en Planta
            item {
                BotonRecargarEnPlantaClay(
                    calculoRecarga = calculoRecarga,
                    clayTheme = clayTheme,
                    onClick = { viewModel.abrirModalRecarga() }
                )
            }

            // 4. Tarjetas de los 3 Tipos de Bidones (Sin duplicar etiquetas arriba)
            item {
                Text(
                    text = "BIDONES DE 20L EN UNIDAD",
                    style = MaterialTheme.typography.labelLarge,
                    color = TextoSecundario,
                    fontWeight = FontWeight.Black,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }

            item {
                inventario?.let { inv ->
                    FilaTarjetasBidonesClay(
                        inventario = inv,
                        estadisticas = estadisticasVentas,
                        onCardClick = { viewModel.abrirModalRecarga() }
                    )
                }
            }

            // 5. Otros Productos (Galones, Pacas, Minis)
            item {
                Text(
                    text = "GALONES, PACAS Y MINIS",
                    style = MaterialTheme.typography.labelLarge,
                    color = TextoSecundario,
                    fontWeight = FontWeight.Black,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }

            item {
                inventario?.let { inv ->
                    FilaOtrosProductosClay(
                        inventario = inv,
                        estadisticas = estadisticasVentas,
                        clayTheme = clayTheme,
                        onCardClick = { viewModel.abrirModalRecarga() }
                    )
                }
            }

            // 6. Gráfico de Distribución y Salidas de Hoy
            item {
                Text(
                    text = "PROPORCIÓN Y SALIDAS DE HOY",
                    style = MaterialTheme.typography.labelLarge,
                    color = TextoSecundario,
                    fontWeight = FontWeight.Black,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }

            item {
                SeccionEstadisticasVentasHoyClay(
                    estadisticas = estadisticasVentas,
                    inventario = inventario,
                    clayTheme = clayTheme
                )
            }

            // 7. Historial de Recargas del Día (Editar / Eliminar con PIN 1234)
            if (recargasHoy.isNotEmpty()) {
                item {
                    Spacer(modifier = Modifier.height(2.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "CARGAS DE HOY (${recargasHoy.size})",
                            style = MaterialTheme.typography.labelLarge,
                            color = TextoSecundario,
                            fontWeight = FontWeight.Black,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        Text(
                            text = "PIN 1234",
                            style = MaterialTheme.typography.labelSmall,
                            color = TextoSecundario,
                            fontWeight = FontWeight.SemiBold,
                            maxLines = 1
                        )
                    }
                }

                items(recargasHoy.size) { idx ->
                    val recarga = recargasHoy[idx]
                    TarjetaRecargaItemClay(
                        recarga = recarga,
                        clayTheme = clayTheme,
                        onEditar = {
                            recargaParaEditar = recarga
                            mostrarPinParaEditarRecarga = true
                        },
                        onEliminar = {
                            recargaParaEliminar = recarga
                            mostrarPinParaEliminarRecarga = true
                        }
                    )
                }
            }
        }
    }

    // Modal para Registrar o Editar Recarga en Planta (Ingreso Manual Directo)
    if (mostrarModalRecarga) {
        ModalBottomSheet(
            onDismissRequest = { viewModel.cerrarModalRecarga() },
            sheetState = sheetState,
            containerColor = clayTheme.canvasColor
        ) {
            ModalRecargaPlantaContentClay(
                inventario = inventario,
                calculoRecarga = calculoRecarga,
                recargaEnEdicion = recargaEnEdicion,
                clayTheme = clayTheme,
                onConfirmarRecarga = { v, t, a, m, vac, gal, pac, min, obs ->
                    viewModel.registrarRecargaPlanta(v, t, a, m, vac, gal, pac, min, obs)
                },
                onCancelar = { viewModel.cerrarModalRecarga() }
            )
        }
    }

    // PIN de seguridad para EDITAR recarga
    if (mostrarPinParaEditarRecarga && recargaParaEditar != null) {
        com.rutaagua.camion.ui.security.SecurityPinDialog(
            titulo = "Editar Carga",
            subtitulo = "Ingresa el PIN 1234 para modificar la recarga",
            onDismissRequest = {
                mostrarPinParaEditarRecarga = false
                recargaParaEditar = null
            },
            onPinCorrecto = {
                mostrarPinParaEditarRecarga = false
                val r = recargaParaEditar
                recargaParaEditar = null
                viewModel.abrirModalRecarga(r)
            }
        )
    }

    // PIN de seguridad para ELIMINAR recarga
    if (mostrarPinParaEliminarRecarga && recargaParaEliminar != null) {
        com.rutaagua.camion.ui.security.SecurityPinDialog(
            titulo = "Eliminar Carga",
            subtitulo = "Ingresa el PIN 1234 para revertir esta carga",
            onDismissRequest = {
                mostrarPinParaEliminarRecarga = false
                recargaParaEliminar = null
            },
            onPinCorrecto = {
                mostrarPinParaEliminarRecarga = false
                recargaParaEliminar?.let { viewModel.eliminarRecargaPlanta(it) }
                recargaParaEliminar = null
            }
        )
    }

    // Diálogo de Ajuste Rápido de Capacidad Diaria
    if (mostrarDialogoCapacidad) {
        DialogoAjustarCapacidadClay(
            capacidadActual = inventario?.capacidadTopeBidones ?: clayTheme.capacidadDefault,
            presets = clayTheme.metasBidonesPreset,
            clayTheme = clayTheme,
            onConfirmar = { nuevoTope -> viewModel.cambiarCapacidadTope(nuevoTope) },
            onDismiss = { viewModel.cerrarDialogoCapacidad() }
        )
    }
}

@Composable
private fun BannerAlertaStockClay(alerta: StockAlertas) {
    ClayCard(
        modifier = Modifier.fillMaxWidth(),
        containerColor = RojoAlertaFondo,
        cornerRadius = 18.dp,
        elevation = 3.dp,
        contentPadding = PaddingValues(14.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(
                imageVector = Icons.Default.Warning,
                contentDescription = "Alerta",
                tint = RojoAlerta,
                modifier = Modifier.size(24.dp)
            )
            Spacer(modifier = Modifier.width(10.dp))
            Column {
                Text(
                    text = "STOCK BAJO EN RUTA",
                    style = MaterialTheme.typography.labelLarge,
                    color = RojoAlerta,
                    fontWeight = FontWeight.Black,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = alerta.mensajeAlerta ?: "",
                    style = MaterialTheme.typography.bodySmall,
                    color = TextoOscuro,
                    fontWeight = FontWeight.SemiBold,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
    }
}

@Composable
private fun TarjetaCapacidadCamionClay(
    inventario: InventarioCamionEntity,
    clayTheme: ClayVehicleTheme,
    onAjustarTopeClick: () -> Unit
) {
    val totalLlenos = inventario.totalBidonesLlenos()
    val vaciosRecogidos = inventario.bidonesVacios
    val tope = inventario.capacidadTopeBidones
    val faltantes = (tope - totalLlenos).coerceAtLeast(0)
    val progreso = if (tope > 0) (totalLlenos.toFloat() / tope).coerceIn(0f, 1f) else 0f

    ClayCard(
        modifier = Modifier.fillMaxWidth(),
        containerColor = clayTheme.cardColor,
        cornerRadius = 22.dp,
        elevation = 5.dp,
        contentPadding = PaddingValues(16.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "CAPACIDAD EN UNIDAD",
                    style = MaterialTheme.typography.labelSmall,
                    color = TextoSecundario,
                    fontWeight = FontWeight.Bold,
                    maxLines = 1,
                    softWrap = false,
                    overflow = TextOverflow.Ellipsis
                )
                Row(verticalAlignment = Alignment.Bottom) {
                    Text(
                        text = "$totalLlenos",
                        fontSize = 32.sp,
                        color = clayTheme.primaryColor,
                        fontWeight = FontWeight.Black,
                        maxLines = 1,
                        softWrap = false
                    )
                    Text(
                        text = " / $tope bidones",
                        fontSize = 14.sp,
                        color = TextoSecundario,
                        fontWeight = FontWeight.Bold,
                        maxLines = 1,
                        softWrap = false,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.padding(bottom = 4.dp, start = 4.dp)
                    )
                }
            }

            ClayButton(
                onClick = onAjustarTopeClick,
                containerColor = clayTheme.accentBgColor,
                contentColor = clayTheme.primaryDarkColor,
                cornerRadius = 12.dp,
                height = 40.dp,
                contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Tune,
                    contentDescription = null,
                    tint = clayTheme.primaryDarkColor,
                    modifier = Modifier.size(16.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = "Meta: $tope",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Black,
                    color = clayTheme.primaryDarkColor,
                    maxLines = 1,
                    softWrap = false
                )
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        LinearProgressIndicator(
            progress = { progreso },
            modifier = Modifier
                .fillMaxWidth()
                .height(10.dp)
                .clip(RoundedCornerShape(5.dp)),
            color = when {
                progreso > 0.4f -> clayTheme.primaryColor
                progreso > 0.15f -> AmbarAdvertencia
                else -> RojoAlerta
            },
            trackColor = clayTheme.insetColor,
            strokeCap = StrokeCap.Round
        )

        Spacer(modifier = Modifier.height(12.dp))

        ClayInsetBox(
            modifier = Modifier.fillMaxWidth(),
            containerColor = clayTheme.insetColor,
            cornerRadius = 14.dp,
            contentPadding = PaddingValues(horizontal = 12.dp, vertical = 10.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                IndicadorResumenStock(
                    color = clayTheme.primaryColor,
                    etiqueta = "Llenos",
                    valor = "$totalLlenos",
                    modifier = Modifier.weight(1f)
                )
                IndicadorResumenStock(
                    color = AmbarAdvertencia,
                    etiqueta = "Libres",
                    valor = "$faltantes",
                    modifier = Modifier.weight(1f)
                )
                IndicadorResumenStock(
                    color = Color(0xFF476278),
                    etiqueta = "Vacíos",
                    valor = "$vaciosRecogidos",
                    modifier = Modifier.weight(1f)
                )
            }
        }
    }
}

@Composable
private fun IndicadorResumenStock(
    color: Color,
    etiqueta: String,
    valor: String,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier,
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.Center
    ) {
        Box(
            modifier = Modifier
                .size(9.dp)
                .background(color, CircleShape)
        )
        Spacer(modifier = Modifier.width(6.dp))
        Text(
            text = "$etiqueta: ",
            fontSize = 12.sp,
            color = TextoSecundario,
            fontWeight = FontWeight.SemiBold,
            maxLines = 1,
            softWrap = false,
            overflow = TextOverflow.Ellipsis
        )
        Text(
            text = valor,
            fontSize = 13.sp,
            fontWeight = FontWeight.Black,
            color = TextoOscuro,
            maxLines = 1,
            softWrap = false
        )
    }
}

@Composable
private fun BotonRecargarEnPlantaClay(
    calculoRecarga: RecargaCalculada?,
    clayTheme: ClayVehicleTheme,
    onClick: () -> Unit
) {
    val subtexto = calculoRecarga?.let { " • Faltan ${it.bidonesFaltantesParaTope}" } ?: ""
    ClayButton(
        onClick = onClick,
        modifier = Modifier
            .fillMaxWidth()
            .height(54.dp),
        containerColor = clayTheme.primaryColor,
        contentColor = Color.White,
        cornerRadius = 16.dp,
        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp)
    ) {
        Icon(
            imageVector = Icons.Default.LocalShipping,
            contentDescription = null,
            tint = Color.White,
            modifier = Modifier.size(20.dp)
        )
        Spacer(modifier = Modifier.width(10.dp))
        Text(
            text = "Ingresar / Modificar Carga$subtexto",
            fontSize = 14.sp,
            fontWeight = FontWeight.Black,
            color = Color.White,
            maxLines = 1,
            softWrap = false,
            overflow = TextOverflow.Ellipsis
        )
    }
}

@Composable
private fun FilaTarjetasBidonesClay(
    inventario: InventarioCamionEntity,
    estadisticas: EstadisticasVentasTipo? = null,
    onCardClick: (() -> Unit)? = null
) {
    val totalTrans = inventario.bidonesLlenosTransparentes + (estadisticas?.transparentesVendidos ?: 0)
    val totalAzul = inventario.bidonesLlenosAzules + (estadisticas?.azulesVendidos ?: 0)
    val totalMarc = inventario.bidonesLlenosMarcados + (estadisticas?.marcadosVendidos ?: 0)

    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        TarjetaBidonIndividualClay(
            tipo = TipoBidon.TRANSPARENTE,
            nombre = "PET",
            cantidad = inventario.bidonesLlenosTransparentes,
            totalCargado = totalTrans,
            vendidosHoy = estadisticas?.transparentesVendidos ?: 0,
            colorFondo = BidonTransparenteFondo,
            colorAcento = BidonTransparenteColor,
            destacado = false,
            onClick = onCardClick,
            modifier = Modifier.weight(1f)
        )

        TarjetaBidonIndividualClay(
            tipo = TipoBidon.AZUL,
            nombre = "Azul",
            cantidad = inventario.bidonesLlenosAzules,
            totalCargado = totalAzul,
            vendidosHoy = estadisticas?.azulesVendidos ?: 0,
            colorFondo = BidonAzulFondo,
            colorAcento = BidonAzulColor,
            destacado = false,
            onClick = onCardClick,
            modifier = Modifier.weight(1f)
        )

        TarjetaBidonIndividualClay(
            tipo = TipoBidon.MARCADO,
            nombre = "Marcada",
            cantidad = inventario.bidonesLlenosMarcados,
            totalCargado = totalMarc,
            vendidosHoy = estadisticas?.marcadosVendidos ?: 0,
            colorFondo = BidonMarcadoFondo,
            colorAcento = BidonMarcadoColor,
            destacado = true,
            onClick = onCardClick,
            modifier = Modifier.weight(1f)
        )
    }
}

@Composable
private fun TarjetaBidonIndividualClay(
    tipo: TipoBidon,
    nombre: String,
    cantidad: Int,
    totalCargado: Int = 0,
    vendidosHoy: Int = 0,
    colorFondo: Color,
    colorAcento: Color,
    destacado: Boolean,
    onClick: (() -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    val stockBajo = cantidad <= 10
    val textoCantidad = if (totalCargado > 0) "$cantidad/$totalCargado" else "$cantidad"

    ClayCard(
        modifier = modifier,
        containerColor = colorFondo,
        accentBorderColor = if (destacado) colorAcento else null,
        cornerRadius = 18.dp,
        elevation = if (destacado) 5.dp else 3.dp,
        contentPadding = PaddingValues(vertical = 12.dp, horizontal = 6.dp),
        onClick = onClick
    ) {
        Column(
            modifier = Modifier.fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            BidonIllustration(tipo = tipo, size = 48.dp)

            Spacer(modifier = Modifier.height(6.dp))

            Text(
                text = textoCantidad,
                fontSize = 18.sp,
                fontWeight = FontWeight.Black,
                color = if (stockBajo) RojoAlerta else colorAcento,
                maxLines = 1,
                softWrap = false,
                overflow = TextOverflow.Ellipsis,
                textAlign = TextAlign.Center
            )

            Text(
                text = nombre,
                fontSize = 12.sp,
                fontWeight = FontWeight.Black,
                textAlign = TextAlign.Center,
                color = if (destacado) colorAcento else TextoOscuro,
                maxLines = 1,
                softWrap = false,
                overflow = TextOverflow.Ellipsis
            )

            Spacer(modifier = Modifier.height(4.dp))

            Surface(
                color = Color.White.copy(alpha = 0.85f),
                shape = RoundedCornerShape(8.dp)
            ) {
                Text(
                    text = "Salieron: $vendidosHoy",
                    color = TextoSecundario,
                    fontWeight = FontWeight.Bold,
                    fontSize = 10.sp,
                    maxLines = 1,
                    softWrap = false,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                )
            }
        }
    }
}

@Composable
private fun FilaOtrosProductosClay(
    inventario: InventarioCamionEntity,
    estadisticas: EstadisticasVentasTipo,
    clayTheme: ClayVehicleTheme,
    onCardClick: (() -> Unit)? = null
) {
    val totalGalones = inventario.galonesLlenos + estadisticas.galonesVendidos
    val totalPacas = inventario.pacasLlenas + estadisticas.pacasVendidas
    val totalMinis = inventario.minisLlenas + estadisticas.minisVendidas

    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        TarjetaProductoSecundarioClay(
            nombre = "Galones",
            cantidad = inventario.galonesLlenos,
            total = if (totalGalones > 0) totalGalones else inventario.capacidadTopeGalones,
            vendidos = estadisticas.galonesVendidos,
            ilustracion = { GalonIllustration(size = 42.dp) },
            clayTheme = clayTheme,
            onClick = onCardClick,
            modifier = Modifier.weight(1f)
        )

        TarjetaProductoSecundarioClay(
            nombre = "Pacas",
            cantidad = inventario.pacasLlenas,
            total = if (totalPacas > 0) totalPacas else inventario.capacidadTopePacas,
            vendidos = estadisticas.pacasVendidas,
            ilustracion = { PacaIllustration(size = 42.dp) },
            clayTheme = clayTheme,
            onClick = onCardClick,
            modifier = Modifier.weight(1f)
        )

        TarjetaProductoSecundarioClay(
            nombre = "Minis",
            cantidad = inventario.minisLlenas,
            total = if (totalMinis > 0) totalMinis else inventario.capacidadTopeMinis,
            vendidos = estadisticas.minisVendidas,
            ilustracion = { MiniIllustration(size = 42.dp) },
            clayTheme = clayTheme,
            onClick = onCardClick,
            modifier = Modifier.weight(1f)
        )
    }
}

@Composable
private fun TarjetaProductoSecundarioClay(
    nombre: String,
    cantidad: Int,
    total: Int,
    vendidos: Int,
    ilustracion: @Composable () -> Unit,
    clayTheme: ClayVehicleTheme,
    onClick: (() -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    ClayCard(
        modifier = modifier,
        containerColor = clayTheme.cardColor,
        cornerRadius = 18.dp,
        elevation = 3.dp,
        contentPadding = PaddingValues(vertical = 12.dp, horizontal = 6.dp),
        onClick = onClick
    ) {
        Column(
            modifier = Modifier.fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Box(
                modifier = Modifier.height(44.dp),
                contentAlignment = Alignment.Center
            ) {
                ilustracion()
            }

            Spacer(modifier = Modifier.height(6.dp))

            Text(
                text = "$cantidad/$total",
                fontSize = 17.sp,
                fontWeight = FontWeight.Black,
                color = clayTheme.primaryColor,
                maxLines = 1,
                softWrap = false,
                overflow = TextOverflow.Ellipsis,
                textAlign = TextAlign.Center
            )

            Text(
                text = nombre,
                fontSize = 12.sp,
                color = TextoOscuro,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center,
                maxLines = 1,
                softWrap = false,
                overflow = TextOverflow.Ellipsis
            )

            Spacer(modifier = Modifier.height(4.dp))

            Surface(
                color = clayTheme.insetColor,
                shape = RoundedCornerShape(8.dp)
            ) {
                Text(
                    text = "Salieron: $vendidos",
                    color = TextoSecundario,
                    fontWeight = FontWeight.Bold,
                    fontSize = 10.sp,
                    maxLines = 1,
                    softWrap = false,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                )
            }
        }
    }
}

/**
 * Modal de Recarga en Planta con ingreso manual directo por teclado (ClayEditableCounter).
 */
@Composable
private fun ModalRecargaPlantaContentClay(
    inventario: InventarioCamionEntity?,
    calculoRecarga: RecargaCalculada?,
    recargaEnEdicion: RecargaPlantaEntity? = null,
    clayTheme: ClayVehicleTheme,
    onConfirmarRecarga: (
        numeroVuelta: Int,
        trans: Int,
        azul: Int,
        marc: Int,
        vacios: Int,
        galones: Int,
        pacas: Int,
        minis: Int,
        observaciones: String
    ) -> Unit,
    onCancelar: () -> Unit
) {
    val editId = recargaEnEdicion?.id ?: -1L
    var numeroVuelta by rememberSaveable(editId) { mutableIntStateOf(recargaEnEdicion?.numeroVuelta ?: 1) }

    val faltantes = calculoRecarga?.bidonesFaltantesParaTope ?: 0
    var cantTrans by rememberSaveable(editId) {
        mutableIntStateOf(recargaEnEdicion?.bidonesCargadosTransparentes ?: (faltantes / 3))
    }
    var cantAzul by rememberSaveable(editId) {
        mutableIntStateOf(recargaEnEdicion?.bidonesCargadosAzules ?: (faltantes - (faltantes / 3) - (faltantes / 6)))
    }
    var cantMarc by rememberSaveable(editId) {
        mutableIntStateOf(recargaEnEdicion?.bidonesCargadosMarcados ?: (faltantes / 6))
    }
    var cantVaciosBajar by rememberSaveable(editId) {
        mutableIntStateOf(recargaEnEdicion?.bidonesVaciosDescargados ?: (inventario?.bidonesVacios ?: 0))
    }

    var cantGalones by rememberSaveable(editId) {
        mutableIntStateOf(recargaEnEdicion?.galonesCargados ?: 0)
    }
    var cantPacas by rememberSaveable(editId) {
        mutableIntStateOf(recargaEnEdicion?.pacasCargadas ?: 0)
    }
    var cantMinis by rememberSaveable(editId) {
        mutableIntStateOf(recargaEnEdicion?.minisCargadas ?: 0)
    }

    val observaciones by rememberSaveable(editId) { mutableStateOf(recargaEnEdicion?.observaciones ?: "") }

    val totalCargados = cantTrans + cantAzul + cantMarc

    LazyColumn(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 6.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = if (recargaEnEdicion != null) "MODIFICAR CARGA" else "REGISTRAR CARGA EN PLANTA",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Black,
                        color = clayTheme.primaryDarkColor,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Text(
                        text = "Toca cualquier número para escribir directo con el teclado",
                        style = MaterialTheme.typography.bodySmall,
                        color = TextoSecundario,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
                IconButton(onClick = onCancelar) {
                    Icon(imageVector = Icons.Default.Close, contentDescription = "Cerrar", tint = TextoOscuro)
                }
            }
        }

        // Selector de Vuelta + Botón Poner en 0
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                FilterChip(
                    selected = numeroVuelta == 1,
                    onClick = { numeroVuelta = 1 },
                    label = { Text("Vuelta 1", fontWeight = FontWeight.Bold, maxLines = 1) },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = clayTheme.accentBgColor,
                        selectedLabelColor = clayTheme.primaryDarkColor
                    ),
                    modifier = Modifier.weight(1f)
                )
                FilterChip(
                    selected = numeroVuelta == 2,
                    onClick = { numeroVuelta = 2 },
                    label = { Text("Vuelta 2", fontWeight = FontWeight.Bold, maxLines = 1) },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = clayTheme.accentBgColor,
                        selectedLabelColor = clayTheme.primaryDarkColor
                    ),
                    modifier = Modifier.weight(1f)
                )
                TextButton(
                    onClick = {
                        cantTrans = 0
                        cantAzul = 0
                        cantMarc = 0
                        cantGalones = 0
                        cantPacas = 0
                        cantMinis = 0
                    }
                ) {
                    Icon(
                        imageVector = Icons.Default.Refresh,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Cero", fontWeight = FontWeight.Bold, maxLines = 1)
                }
            }
        }

        // Resumen de espacios libres
        item {
            ClayInsetBox(
                modifier = Modifier.fillMaxWidth(),
                containerColor = VerdeEfectivoFondo,
                cornerRadius = 14.dp,
                contentPadding = PaddingValues(horizontal = 12.dp, vertical = 10.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "TOPE META: ${calculoRecarga?.capacidadTopeBidones ?: clayTheme.capacidadDefault} BIDONES",
                            style = MaterialTheme.typography.labelSmall,
                            color = VerdeEfectivo,
                            fontWeight = FontWeight.Black,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        Text(
                            text = "En unidad: ${calculoRecarga?.bidonesLlenosActuales ?: 0} • Libres: $faltantes",
                            style = MaterialTheme.typography.bodySmall,
                            color = TextoOscuro,
                            fontWeight = FontWeight.SemiBold,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                    ClayBadge(
                        text = "+$totalCargados",
                        backgroundColor = VerdeEfectivo,
                        textColor = Color.White
                    )
                }
            }
        }

        item {
            Text(
                text = "BIDONES DE 20L A SUBIR ($totalCargados)",
                style = MaterialTheme.typography.labelLarge,
                color = TextoSecundario,
                fontWeight = FontWeight.Black,
                maxLines = 1
            )
        }

        item {
            FilaRecargaManualClay(
                label = "Transparentes (PET)",
                subtitulo = "En unidad: ${inventario?.bidonesLlenosTransparentes ?: 0}",
                cantidad = cantTrans,
                accentColor = BidonTransparenteColor,
                ilustracion = { BidonIllustration(TipoBidon.TRANSPARENTE, size = 34.dp) },
                onCambio = { cantTrans = it }
            )
        }

        item {
            FilaRecargaManualClay(
                label = "Azules",
                subtitulo = "En unidad: ${inventario?.bidonesLlenosAzules ?: 0}",
                cantidad = cantAzul,
                accentColor = BidonAzulColor,
                ilustracion = { BidonIllustration(TipoBidon.AZUL, size = 34.dp) },
                onCambio = { cantAzul = it }
            )
        }

        item {
            FilaRecargaManualClay(
                label = "Marcadas",
                subtitulo = "En unidad: ${inventario?.bidonesLlenosMarcados ?: 0}",
                cantidad = cantMarc,
                accentColor = BidonMarcadoColor,
                ilustracion = { BidonIllustration(TipoBidon.MARCADO, size = 34.dp) },
                onCambio = { cantMarc = it }
            )
        }

        item {
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = "GALONES, PACAS Y MINIS A SUBIR",
                style = MaterialTheme.typography.labelLarge,
                color = TextoSecundario,
                fontWeight = FontWeight.Black,
                maxLines = 1
            )
        }

        item {
            FilaRecargaManualClay(
                label = "Galones (4L)",
                subtitulo = "En unidad: ${inventario?.galonesLlenos ?: 0}",
                cantidad = cantGalones,
                accentColor = clayTheme.primaryColor,
                ilustracion = { GalonIllustration(size = 34.dp) },
                onCambio = { cantGalones = it }
            )
        }

        item {
            FilaRecargaManualClay(
                label = "Pacas (24x500ml)",
                subtitulo = "En unidad: ${inventario?.pacasLlenas ?: 0}",
                cantidad = cantPacas,
                accentColor = clayTheme.primaryColor,
                ilustracion = { PacaIllustration(size = 34.dp) },
                onCambio = { cantPacas = it }
            )
        }

        item {
            FilaRecargaManualClay(
                label = "Minis (12x500ml)",
                subtitulo = "En unidad: ${inventario?.minisLlenas ?: 0}",
                cantidad = cantMinis,
                accentColor = clayTheme.primaryColor,
                ilustracion = { MiniIllustration(size = 34.dp) },
                onCambio = { cantMinis = it }
            )
        }

        item {
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = "VACÍOS BAJADOS EN PLANTA",
                style = MaterialTheme.typography.labelLarge,
                color = TextoSecundario,
                fontWeight = FontWeight.Black,
                maxLines = 1
            )
        }

        item {
            FilaRecargaManualClay(
                label = "Vacíos dejados en planta",
                subtitulo = "Vacíos arriba: ${inventario?.bidonesVacios ?: 0}",
                cantidad = cantVaciosBajar,
                accentColor = Color(0xFF476278),
                ilustracion = {
                    Box(
                        modifier = Modifier
                            .size(16.dp)
                            .background(Color(0xFF476278), CircleShape)
                    )
                },
                onCambio = { cantVaciosBajar = it }
            )
        }

        item {
            Spacer(modifier = Modifier.height(6.dp))
            ClayButton(
                onClick = {
                    onConfirmarRecarga(
                        numeroVuelta,
                        cantTrans,
                        cantAzul,
                        cantMarc,
                        cantVaciosBajar,
                        cantGalones,
                        cantPacas,
                        cantMinis,
                        observaciones
                    )
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(54.dp),
                containerColor = VerdeEfectivo,
                contentColor = Color.White,
                cornerRadius = 16.dp
            ) {
                Icon(imageVector = Icons.Default.Check, contentDescription = null)
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = if (recargaEnEdicion != null) {
                        "Guardar Cambios (+$totalCargados bidones)"
                    } else {
                        "Confirmar Carga (+$totalCargados bidones)"
                    },
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Black,
                    maxLines = 1,
                    softWrap = false,
                    overflow = TextOverflow.Ellipsis
                )
            }
            Spacer(modifier = Modifier.height(26.dp))
        }
    }
}

@Composable
private fun FilaRecargaManualClay(
    label: String,
    subtitulo: String,
    cantidad: Int,
    accentColor: Color,
    ilustracion: @Composable () -> Unit,
    onCambio: (Int) -> Unit
) {
    ClayCard(
        modifier = Modifier.fillMaxWidth(),
        containerColor = Color.White,
        cornerRadius = 16.dp,
        elevation = 2.dp,
        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 10.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.weight(1f)
            ) {
                Box(
                    modifier = Modifier.size(34.dp),
                    contentAlignment = Alignment.Center
                ) {
                    ilustracion()
                }
                Spacer(modifier = Modifier.width(10.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = label,
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Black,
                        color = TextoOscuro,
                        maxLines = 1,
                        softWrap = false,
                        overflow = TextOverflow.Ellipsis
                    )
                    Text(
                        text = subtitulo,
                        style = MaterialTheme.typography.labelSmall,
                        color = TextoSecundario,
                        maxLines = 1,
                        softWrap = false,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }

            Spacer(modifier = Modifier.width(8.dp))

            ClayEditableCounter(
                value = cantidad,
                onValueChange = onCambio,
                accentColor = accentColor
            )
        }
    }
}

@Composable
private fun TarjetaRecargaItemClay(
    recarga: RecargaPlantaEntity,
    clayTheme: ClayVehicleTheme,
    onEditar: () -> Unit,
    onEliminar: () -> Unit
) {
    val formatoHora = remember { SimpleDateFormat("HH:mm", Locale.getDefault()) }
    val horaTexto = formatoHora.format(Date(recarga.fecha))

    ClayCard(
        modifier = Modifier.fillMaxWidth(),
        containerColor = clayTheme.cardColor,
        cornerRadius = 18.dp,
        elevation = 3.dp,
        contentPadding = PaddingValues(14.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                ClayBadge(
                    text = "Vuelta ${recarga.numeroVuelta}",
                    backgroundColor = clayTheme.accentBgColor,
                    textColor = clayTheme.primaryDarkColor
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "$horaTexto • +${recarga.totalBidonesCargados()} bidones",
                    style = MaterialTheme.typography.labelMedium,
                    color = TextoOscuro,
                    fontWeight = FontWeight.Black,
                    maxLines = 1,
                    softWrap = false,
                    overflow = TextOverflow.Ellipsis
                )
            }

            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Surface(
                    color = clayTheme.accentBgColor,
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier.clickable(onClick = onEditar)
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 5.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.Edit,
                            contentDescription = "Editar carga",
                            tint = clayTheme.primaryDarkColor,
                            modifier = Modifier.size(14.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "Editar",
                            style = MaterialTheme.typography.labelSmall,
                            color = clayTheme.primaryDarkColor,
                            fontWeight = FontWeight.Bold,
                            maxLines = 1
                        )
                    }
                }

                Surface(
                    color = RojoAlertaFondo,
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier.clickable(onClick = onEliminar)
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 5.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.DeleteOutline,
                            contentDescription = "Eliminar carga",
                            tint = RojoAlerta,
                            modifier = Modifier.size(14.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "Borrar",
                            style = MaterialTheme.typography.labelSmall,
                            color = RojoAlerta,
                            fontWeight = FontWeight.Bold,
                            maxLines = 1
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            ClayBadge(
                text = "PET: ${recarga.bidonesCargadosTransparentes}",
                backgroundColor = BidonTransparenteFondo,
                textColor = BidonTransparenteColor
            )
            ClayBadge(
                text = "Azul: ${recarga.bidonesCargadosAzules}",
                backgroundColor = BidonAzulFondo,
                textColor = BidonAzulColor
            )
            ClayBadge(
                text = "Marcada: ${recarga.bidonesCargadosMarcados}",
                backgroundColor = BidonMarcadoFondo,
                textColor = BidonMarcadoColor
            )
        }

        if (recarga.galonesCargados > 0 || recarga.pacasCargadas > 0 || recarga.minisCargadas > 0) {
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "Otros: ${recarga.galonesCargados} galones • ${recarga.pacasCargadas} pacas • ${recarga.minisCargadas} minis",
                style = MaterialTheme.typography.labelSmall,
                color = TextoSecundario,
                fontWeight = FontWeight.SemiBold,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

@Composable
private fun DialogoAjustarCapacidadClay(
    capacidadActual: Int,
    presets: List<Int>,
    clayTheme: ClayVehicleTheme,
    onConfirmar: (Int) -> Unit,
    onDismiss: () -> Unit
) {
    var valorTexto by rememberSaveable { mutableStateOf("$capacidadActual") }

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = Color.White,
        title = {
            Text(
                text = "Capacidad Tope del Día",
                fontWeight = FontWeight.Black,
                color = TextoOscuro
            )
        },
        text = {
            Column {
                Text(
                    text = "Selecciona o escribe el tope de bidones para hoy:",
                    style = MaterialTheme.typography.bodyMedium,
                    color = TextoSecundario
                )
                Spacer(modifier = Modifier.height(12.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    presets.forEach { meta ->
                        OutlinedButton(
                            onClick = { valorTexto = "$meta" },
                            modifier = Modifier.weight(1f),
                            contentPadding = PaddingValues(horizontal = 4.dp, vertical = 6.dp)
                        ) {
                            Text(
                                text = "$meta",
                                fontWeight = FontWeight.Black,
                                color = clayTheme.primaryColor,
                                maxLines = 1
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                OutlinedTextField(
                    value = valorTexto,
                    onValueChange = { valorTexto = it.filter { char -> char.isDigit() } },
                    label = { Text("Tope de bidones") },
                    textStyle = TextStyle(
                        color = TextoOscuro,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold
                    ),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    singleLine = true,
                    colors = marineOutlinedTextFieldColors(accentColor = clayTheme.primaryColor),
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val nuevoValor = valorTexto.toIntOrNull() ?: capacidadActual
                    onConfirmar(nuevoValor)
                },
                colors = ButtonDefaults.buttonColors(
                    containerColor = clayTheme.primaryColor,
                    contentColor = Color.White
                )
            ) {
                Text("Guardar", fontWeight = FontWeight.Bold, maxLines = 1)
            }
        },
        dismissButton = {
            OutlinedButton(onClick = onDismiss) {
                Text("Cancelar", color = TextoOscuro, maxLines = 1)
            }
        }
    )
}

/**
 * Gráfico Donut limpio (sin textos duplicados de Marcadas) y adaptable
 * para que cada fila quepa en una sola línea tanto en vertical como en horizontal.
 */
@Composable
private fun SeccionEstadisticasVentasHoyClay(
    estadisticas: EstadisticasVentasTipo,
    inventario: InventarioCamionEntity? = null,
    clayTheme: ClayVehicleTheme
) {
    val stockTrans = inventario?.bidonesLlenosTransparentes ?: 0
    val stockAzul = inventario?.bidonesLlenosAzules ?: 0
    val stockMarc = inventario?.bidonesLlenosMarcados ?: 0
    val totalEnCamion = stockTrans + stockAzul + stockMarc

    val totalVendidos = estadisticas.totalBidonesVendidos
    val mostrandoStockEnGrafico = totalVendidos == 0 && totalEnCamion > 0
    val baseTotalGrafico = if (mostrandoStockEnGrafico) totalEnCamion else totalVendidos

    val valorTransGrafico = if (mostrandoStockEnGrafico) stockTrans else estadisticas.transparentesVendidos
    val valorAzulGrafico = if (mostrandoStockEnGrafico) stockAzul else estadisticas.azulesVendidos
    val valorMarcGrafico = if (mostrandoStockEnGrafico) stockMarc else estadisticas.marcadosVendidos

    val pctTrans = if (baseTotalGrafico > 0) valorTransGrafico.toFloat() / baseTotalGrafico else 0f
    val pctAzul = if (baseTotalGrafico > 0) valorAzulGrafico.toFloat() / baseTotalGrafico else 0f
    val pctMarc = if (baseTotalGrafico > 0) valorMarcGrafico.toFloat() / baseTotalGrafico else 0f

    val totalTransCargados = estadisticas.transparentesVendidos + stockTrans
    val totalAzulCargados = estadisticas.azulesVendidos + stockAzul
    val totalMarcCargados = estadisticas.marcadosVendidos + stockMarc

    ClayCard(
        modifier = Modifier.fillMaxWidth(),
        containerColor = clayTheme.cardColor,
        cornerRadius = 22.dp,
        elevation = 5.dp,
        contentPadding = PaddingValues(16.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            // Donut Chart compacto con arco resaltado para Marcadas
            Box(
                modifier = Modifier.size(110.dp),
                contentAlignment = Alignment.Center
            ) {
                Canvas(modifier = Modifier.size(96.dp)) {
                    val strokeNormal = 18.dp.toPx()
                    val strokeMarcada = 22.dp.toPx()

                    if (baseTotalGrafico == 0) {
                        drawArc(
                            color = Color(0xFFD5E4F0),
                            startAngle = 0f,
                            sweepAngle = 360f,
                            useCenter = false,
                            style = Stroke(width = strokeNormal)
                        )
                    } else {
                        var currentAngle = -90f

                        if (pctTrans > 0f) {
                            val sweep = pctTrans * 360f
                            drawArc(
                                color = BidonTransparenteColor,
                                startAngle = currentAngle,
                                sweepAngle = sweep,
                                useCenter = false,
                                style = Stroke(width = strokeNormal)
                            )
                            currentAngle += sweep
                        }

                        if (pctAzul > 0f) {
                            val sweep = pctAzul * 360f
                            drawArc(
                                color = BidonAzulColor,
                                startAngle = currentAngle,
                                sweepAngle = sweep,
                                useCenter = false,
                                style = Stroke(width = strokeNormal)
                            )
                            currentAngle += sweep
                        }

                        if (pctMarc > 0f) {
                            val sweep = pctMarc * 360f
                            drawArc(
                                color = BidonMarcadoColor,
                                startAngle = currentAngle,
                                sweepAngle = sweep,
                                useCenter = false,
                                style = Stroke(width = strokeMarcada)
                            )
                        }
                    }
                }

                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = "$baseTotalGrafico",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Black,
                        color = TextoOscuro,
                        maxLines = 1
                    )
                    Text(
                        text = if (mostrandoStockEnGrafico) "En unidad" else "Salieron",
                        color = TextoSecundario,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        maxLines = 1
                    )
                }
            }

            // Leyenda de 1 sola línea por ítem (Sin redundancia ni saltos de línea)
            Column(
                verticalArrangement = Arrangement.spacedBy(6.dp),
                modifier = Modifier.weight(1f)
            ) {
                ElementoLeyendaDonutClay(
                    color = BidonMarcadoColor,
                    fondoColor = BidonMarcadoFondo,
                    nombre = "Marcadas",
                    vendidos = estadisticas.marcadosVendidos,
                    quedan = stockMarc,
                    totalCargado = totalMarcCargados,
                    resaltado = true
                )
                ElementoLeyendaDonutClay(
                    color = BidonTransparenteColor,
                    fondoColor = BidonTransparenteFondo,
                    nombre = "PET",
                    vendidos = estadisticas.transparentesVendidos,
                    quedan = stockTrans,
                    totalCargado = totalTransCargados,
                    resaltado = false
                )
                ElementoLeyendaDonutClay(
                    color = BidonAzulColor,
                    fondoColor = BidonAzulFondo,
                    nombre = "Azules",
                    vendidos = estadisticas.azulesVendidos,
                    quedan = stockAzul,
                    totalCargado = totalAzulCargados,
                    resaltado = false
                )
            }
        }
    }
}

@Composable
private fun ElementoLeyendaDonutClay(
    color: Color,
    fondoColor: Color,
    nombre: String,
    vendidos: Int,
    quedan: Int,
    totalCargado: Int,
    resaltado: Boolean
) {
    Surface(
        color = if (resaltado) fondoColor else Color(0xFFF5F9FC),
        shape = RoundedCornerShape(10.dp),
        border = BorderStroke(
            width = if (resaltado) 1.5.dp else 1.dp,
            color = if (resaltado) color else Color(0xFFD5E4F0)
        ),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 10.dp, vertical = 6.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.weight(1f)
            ) {
                Box(
                    modifier = Modifier
                        .size(10.dp)
                        .background(color, CircleShape)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = nombre,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Black,
                    color = if (resaltado) color else TextoOscuro,
                    maxLines = 1,
                    softWrap = false,
                    overflow = TextOverflow.Ellipsis
                )
            }

            Text(
                text = "Quedan $quedan/$totalCargado (Sal: $vendidos)",
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                color = if (resaltado) color else TextoSecundario,
                maxLines = 1,
                softWrap = false,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

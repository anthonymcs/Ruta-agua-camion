package com.rutaagua.camion.ui.screens.inventario

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.rememberScrollState
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
    val mostrarModalEditarCargaActual by viewModel.mostrarModalEditarCargaActual.collectAsState()
    val mensajeSnackbar by viewModel.mensajeSnackbar.collectAsState()
    val recargasHoy by viewModel.recargasHoyState.collectAsState()
    val ultimaRecargaAnterior by viewModel.ultimaRecargaAnteriorState.collectAsState()
    val recargaEnEdicion by viewModel.recargaEnEdicion.collectAsState()

    val clayTheme = obtenerClayThemePorVehiculo(vehiculoActivo)

    var recargaParaEliminar by remember { mutableStateOf<RecargaPlantaEntity?>(null) }
    var recargaParaEditar by remember { mutableStateOf<RecargaPlantaEntity?>(null) }
    var mostrarPinParaEditarRecarga by rememberSaveable { mutableStateOf(false) }
    var mostrarPinParaEliminarRecarga by rememberSaveable { mutableStateOf(false) }
    var mostrarPinParaEditarCargaActual by rememberSaveable { mutableStateOf(false) }
    var mostrarPinParaBorrarCargaActual by rememberSaveable { mutableStateOf(false) }

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

            // 2. Tarjeta de Capacidad y Carga Actual (Click para Editar o Borrar Carga Actual con PIN 1234)
            item {
                inventario?.let { inv ->
                    TarjetaCapacidadCamionClay(
                        inventario = inv,
                        clayTheme = clayTheme,
                        onAjustarTopeClick = { viewModel.abrirDialogoCapacidad() },
                        onEditarCargaActualClick = { mostrarPinParaEditarCargaActual = true },
                        onBorrarCargaActualClick = { mostrarPinParaBorrarCargaActual = true }
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
                        onCardClick = { mostrarPinParaEditarCargaActual = true }
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
                        onCardClick = { mostrarPinParaEditarCargaActual = true }
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

            // Carga dejada lista el día anterior (iniciar la jornada sin reingresar desde cero)
            ultimaRecargaAnterior?.let { recAnt ->
                item {
                    Spacer(modifier = Modifier.height(4.dp))
                    ClayInsetBox(
                        modifier = Modifier.fillMaxWidth(),
                        containerColor = Color(0xFFF1F5F9),
                        cornerRadius = 16.dp,
                        contentPadding = PaddingValues(12.dp)
                    ) {
                        Column {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "CARGA INICIAL (DEJADA DÍA ANTERIOR)",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Black,
                                    color = TextoSecundario
                                )
                                Text(
                                    text = recAnt.fechaTexto,
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = TextoSecundario
                                )
                            }
                            Spacer(modifier = Modifier.height(4.dp))
                            val totAnt = recAnt.totalBidonesCargados()
                            val detAnt = buildList {
                                if (recAnt.bidonesCargadosAzules > 0) add("${recAnt.bidonesCargadosAzules} azules")
                                if (recAnt.bidonesCargadosTransparentes > 0) add("${recAnt.bidonesCargadosTransparentes} PET")
                                if (recAnt.bidonesCargadosMarcados > 0) add("${recAnt.bidonesCargadosMarcados} marcadas")
                                if (recAnt.galonesCargados > 0) add("${recAnt.galonesCargados} galones")
                                if (recAnt.pacasCargadas > 0) add("${recAnt.pacasCargadas} pacas")
                                if (recAnt.minisCargadas > 0) add("${recAnt.minisCargadas} minis")
                            }.joinToString(", ")
                            Text(
                                text = "El vehículo inició listo con +$totAnt bidones ($detAnt) cargados al cierre previo.",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = TextoOscuro
                            )
                        }
                    }
                }
            }

            // 7. Historial de Cargas del Día (Editar / Eliminar con PIN 1234)
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
                siguienteNumeroCarga = if (recargaEnEdicion != null) recargaEnEdicion!!.numeroVuelta else (recargasHoy.size + 1),
                clayTheme = clayTheme,
                onConfirmarRecarga = { v, t, a, m, vac, gal, pac, min, obs, nMeta, fijarFinal, fT, fA, fM, fG, fP, fMin ->
                    viewModel.registrarRecargaPlanta(
                        numeroVuelta = v,
                        cargadosTransparentes = t,
                        cargadosAzules = a,
                        cargadosMarcados = m,
                        vaciosDescargados = vac,
                        galonesCargados = gal,
                        pacasCargadas = pac,
                        minisCargadas = min,
                        observaciones = obs,
                        nuevaMetaBidones = nMeta,
                        fijarStockFinalDirecto = fijarFinal,
                        finalTransparentes = fT,
                        finalAzules = fA,
                        finalMarcados = fM,
                        finalGalones = fG,
                        finalPacas = fP,
                        finalMinis = fMin
                    )
                },
                onCancelar = { viewModel.cerrarModalRecarga() }
            )
        }
    }

    // PIN de seguridad para EDITAR recarga
    if (mostrarPinParaEditarRecarga && recargaParaEditar != null) {
        com.rutaagua.camion.ui.security.SecurityPinDialog(
            titulo = "Editar Carga",
            subtitulo = "Ingresa el PIN de seguridad para modificar la recarga",
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
            subtitulo = "Ingresa el PIN de seguridad para revertir esta carga",
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

    // PIN de seguridad para EDITAR CARGA ACTUAL en unidad
    if (mostrarPinParaEditarCargaActual) {
        com.rutaagua.camion.ui.security.SecurityPinDialog(
            titulo = "Editar Carga Actual",
            subtitulo = "Ingresa el PIN de seguridad para modificar o reemplazar la carga actual",
            onDismissRequest = { mostrarPinParaEditarCargaActual = false },
            onPinCorrecto = {
                mostrarPinParaEditarCargaActual = false
                viewModel.abrirModalEditarCargaActual()
            }
        )
    }

    // PIN de seguridad para BORRAR / VACIAR CARGA ACTUAL en unidad
    if (mostrarPinParaBorrarCargaActual) {
        com.rutaagua.camion.ui.security.SecurityPinDialog(
            titulo = "Borrar Carga Actual",
            subtitulo = "Ingresa el PIN de seguridad para vaciar la carga actual del vehículo",
            onDismissRequest = { mostrarPinParaBorrarCargaActual = false },
            onPinCorrecto = {
                mostrarPinParaBorrarCargaActual = false
                viewModel.borrarCargaActual()
            }
        )
    }

    // Modal para Editar o Borrar directamente la Carga Actual en Unidad
    if (mostrarModalEditarCargaActual) {
        ModalBottomSheet(
            onDismissRequest = { viewModel.cerrarModalEditarCargaActual() },
            sheetState = sheetState,
            containerColor = clayTheme.canvasColor
        ) {
            ModalEditarCargaActualContentClay(
                inventario = inventario,
                clayTheme = clayTheme,
                onGuardarCargaActual = { trans, azul, marc, vac, gal, pac, min ->
                    viewModel.establecerCargaActual(trans, azul, marc, vac, gal, pac, min)
                },
                onBorrarTodaLaCarga = {
                    viewModel.borrarCargaActual()
                },
                onCancelar = { viewModel.cerrarModalEditarCargaActual() }
            )
        }
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
    onAjustarTopeClick: () -> Unit,
    onEditarCargaActualClick: () -> Unit,
    onBorrarCargaActualClick: () -> Unit
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
        contentPadding = PaddingValues(16.dp),
        onClick = onEditarCargaActualClick
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "CAPACIDAD EN UNIDAD (TOCA PARA EDITAR)",
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
                height = 38.dp,
                contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Tune,
                    contentDescription = null,
                    tint = clayTheme.primaryDarkColor,
                    modifier = Modifier.size(15.dp)
                )
                Spacer(modifier = Modifier.width(5.dp))
                Text(
                    text = "Meta: $tope",
                    fontSize = 11.sp,
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

        Spacer(modifier = Modifier.height(10.dp))

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

        Spacer(modifier = Modifier.height(10.dp))

        // Acciones directas sobre la Carga Actual (Editar o Borrar Carga Actual con PIN 1234)
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Surface(
                onClick = onEditarCargaActualClick,
                color = clayTheme.accentBgColor,
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier
                    .weight(1f)
                    .height(38.dp)
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 10.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Edit,
                        contentDescription = null,
                        tint = clayTheme.primaryDarkColor,
                        modifier = Modifier.size(15.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "Editar Carga Actual",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Black,
                        color = clayTheme.primaryDarkColor,
                        maxLines = 1,
                        softWrap = false,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }

            Surface(
                onClick = onBorrarCargaActualClick,
                color = RojoAlertaFondo,
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier
                    .weight(1f)
                    .height(38.dp)
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 10.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.DeleteOutline,
                        contentDescription = null,
                        tint = RojoAlerta,
                        modifier = Modifier.size(15.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "Borrar Carga Actual",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Black,
                        color = RojoAlerta,
                        maxLines = 1,
                        softWrap = false,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }
        }
    }
}

@Composable
private fun ModalEditarCargaActualContentClay(
    inventario: InventarioCamionEntity?,
    clayTheme: ClayVehicleTheme,
    onGuardarCargaActual: (
        trans: Int,
        azul: Int,
        marc: Int,
        vacios: Int,
        galones: Int,
        pacas: Int,
        minis: Int
    ) -> Unit,
    onBorrarTodaLaCarga: () -> Unit,
    onCancelar: () -> Unit
) {
    var cantTrans by rememberSaveable { mutableIntStateOf(inventario?.bidonesLlenosTransparentes ?: 0) }
    var cantAzul by rememberSaveable { mutableIntStateOf(inventario?.bidonesLlenosAzules ?: 0) }
    var cantMarc by rememberSaveable { mutableIntStateOf(inventario?.bidonesLlenosMarcados ?: 0) }
    var cantVacios by rememberSaveable { mutableIntStateOf(inventario?.bidonesVacios ?: 0) }
    var cantGalones by rememberSaveable { mutableIntStateOf(inventario?.galonesLlenos ?: 0) }
    var cantPacas by rememberSaveable { mutableIntStateOf(inventario?.pacasLlenas ?: 0) }
    var cantMinis by rememberSaveable { mutableIntStateOf(inventario?.minisLlenas ?: 0) }

    val totalBidones = cantTrans + cantAzul + cantMarc

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
                        text = "EDITAR O BORRAR CARGA ACTUAL",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Black,
                        color = clayTheme.primaryDarkColor,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Text(
                        text = "Establece con cuántos bidones está saliendo hoy la unidad",
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

        // Botón rápido para Vaciar / Borrar toda la carga actual (poner en 0)
        item {
            Surface(
                onClick = onBorrarTodaLaCarga,
                color = RojoAlertaFondo,
                shape = RoundedCornerShape(14.dp),
                border = BorderStroke(1.dp, RojoAlerta.copy(alpha = 0.35f)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 14.dp, vertical = 10.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.DeleteOutline,
                        contentDescription = null,
                        tint = RojoAlerta,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Borrar Toda la Carga Actual (Dejar Unidad en 0)",
                        fontWeight = FontWeight.Black,
                        fontSize = 12.sp,
                        color = RojoAlerta,
                        maxLines = 1,
                        softWrap = false,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }
        }

        item {
            Text(
                text = "BIDONES DE 20L EN UNIDAD ($totalBidones)",
                style = MaterialTheme.typography.labelLarge,
                color = TextoSecundario,
                fontWeight = FontWeight.Black,
                maxLines = 1
            )
        }

        item {
            FilaRecargaManualClay(
                label = "Transparentes (PET)",
                subtitulo = "Cantidad exacta actual en unidad",
                cantidad = cantTrans,
                accentColor = BidonTransparenteColor,
                ilustracion = { BidonIllustration(TipoBidon.TRANSPARENTE, size = 34.dp) },
                onCambio = { cantTrans = it }
            )
        }

        item {
            FilaRecargaManualClay(
                label = "Azules",
                subtitulo = "Cantidad exacta actual en unidad",
                cantidad = cantAzul,
                accentColor = BidonAzulColor,
                ilustracion = { BidonIllustration(TipoBidon.AZUL, size = 34.dp) },
                onCambio = { cantAzul = it }
            )
        }

        item {
            FilaRecargaManualClay(
                label = "Marcadas",
                subtitulo = "Cantidad exacta actual en unidad",
                cantidad = cantMarc,
                accentColor = BidonMarcadoColor,
                ilustracion = { BidonIllustration(TipoBidon.MARCADO, size = 34.dp) },
                onCambio = { cantMarc = it }
            )
        }

        item {
            Text(
                text = "OTROS PRODUCTOS Y VACÍOS EN UNIDAD",
                style = MaterialTheme.typography.labelLarge,
                color = TextoSecundario,
                fontWeight = FontWeight.Black,
                maxLines = 1
            )
        }

        item {
            FilaRecargaManualClay(
                label = "Galones (4L)",
                subtitulo = "Stock actual en unidad",
                cantidad = cantGalones,
                accentColor = clayTheme.primaryColor,
                ilustracion = { GalonIllustration(size = 34.dp) },
                onCambio = { cantGalones = it }
            )
        }

        item {
            FilaRecargaManualClay(
                label = "Pacas (24x500ml)",
                subtitulo = "Stock actual en unidad",
                cantidad = cantPacas,
                accentColor = clayTheme.primaryColor,
                ilustracion = { PacaIllustration(size = 34.dp) },
                onCambio = { cantPacas = it }
            )
        }

        item {
            FilaRecargaManualClay(
                label = "Minis (12x500ml)",
                subtitulo = "Stock actual en unidad",
                cantidad = cantMinis,
                accentColor = clayTheme.primaryColor,
                ilustracion = { MiniIllustration(size = 34.dp) },
                onCambio = { cantMinis = it }
            )
        }

        item {
            FilaRecargaManualClay(
                label = "Envases Vacíos en Unidad",
                subtitulo = "Vacíos actuales arriba del vehículo",
                cantidad = cantVacios,
                accentColor = Color(0xFF476278),
                ilustracion = {
                    Box(
                        modifier = Modifier
                            .size(16.dp)
                            .background(Color(0xFF476278), CircleShape)
                    )
                },
                onCambio = { cantVacios = it }
            )
        }

        item {
            Spacer(modifier = Modifier.height(6.dp))
            ClayButton(
                onClick = {
                    onGuardarCargaActual(
                        cantTrans,
                        cantAzul,
                        cantMarc,
                        cantVacios,
                        cantGalones,
                        cantPacas,
                        cantMinis
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
                    text = "Guardar Carga Actual ($totalBidones bidones)",
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
            text = "Sumar Carga de Planta$subtexto",
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
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        TarjetaBidonIndividualClay(
            tipo = TipoBidon.TRANSPARENTE,
            nombre = "PET",
            cantidad = inventario.bidonesLlenosTransparentes,
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
    vendidosHoy: Int = 0,
    colorFondo: Color,
    colorAcento: Color,
    destacado: Boolean,
    onClick: (() -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    val stockBajo = cantidad <= 10

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
                text = "$cantidad",
                fontSize = 20.sp,
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
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        TarjetaProductoSecundarioClay(
            nombre = "Galones",
            cantidad = inventario.galonesLlenos,
            tope = inventario.capacidadTopeGalones,
            vendidos = estadisticas.galonesVendidos,
            ilustracion = { GalonIllustration(size = 42.dp) },
            clayTheme = clayTheme,
            onClick = onCardClick,
            modifier = Modifier.weight(1f)
        )

        TarjetaProductoSecundarioClay(
            nombre = "Pacas",
            cantidad = inventario.pacasLlenas,
            tope = inventario.capacidadTopePacas,
            vendidos = estadisticas.pacasVendidas,
            ilustracion = { PacaIllustration(size = 42.dp) },
            clayTheme = clayTheme,
            onClick = onCardClick,
            modifier = Modifier.weight(1f)
        )

        TarjetaProductoSecundarioClay(
            nombre = "Minis",
            cantidad = inventario.minisLlenas,
            tope = inventario.capacidadTopeMinis,
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
    tope: Int,
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
                text = "$cantidad / $tope",
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
    siguienteNumeroCarga: Int = 1,
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
        observaciones: String,
        nuevaMeta: Int?,
        fijarStockFinal: Boolean,
        finalTrans: Int?,
        finalAzul: Int?,
        finalMarc: Int?,
        finalGal: Int?,
        finalPac: Int?,
        finalMin: Int?
    ) -> Unit,
    onCancelar: () -> Unit
) {
    val editId = recargaEnEdicion?.id ?: -1L
    var numeroVuelta by rememberSaveable(editId) { mutableIntStateOf(recargaEnEdicion?.numeroVuelta ?: siguienteNumeroCarga) }

    val metaActual = calculoRecarga?.capacidadTopeBidones ?: (inventario?.capacidadTopeBidones ?: clayTheme.capacidadDefault)
    var metaSalida by rememberSaveable(editId) { mutableIntStateOf(metaActual) }
    var modoFijarTotal by rememberSaveable(editId) { mutableStateOf(false) }

    val transEnUnidad = inventario?.bidonesLlenosTransparentes ?: 0
    val azulEnUnidad = inventario?.bidonesLlenosAzules ?: 0
    val marcEnUnidad = inventario?.bidonesLlenosMarcados ?: 0
    val galEnUnidad = inventario?.galonesLlenos ?: 0
    val pacEnUnidad = inventario?.pacasLlenas ?: 0
    val minEnUnidad = inventario?.minisLlenas ?: 0
    val totalLlenosEnUnidad = transEnUnidad + azulEnUnidad + marcEnUnidad
    val faltantes = maxOf(0, metaSalida - totalLlenosEnUnidad)

    // Valores para Modo Aditivo (+X)
    var cantTrans by rememberSaveable(editId) {
        mutableIntStateOf(recargaEnEdicion?.bidonesCargadosTransparentes ?: 0)
    }
    var cantAzul by rememberSaveable(editId) {
        mutableIntStateOf(recargaEnEdicion?.bidonesCargadosAzules ?: 0)
    }
    var cantMarc by rememberSaveable(editId) {
        mutableIntStateOf(recargaEnEdicion?.bidonesCargadosMarcados ?: 0)
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

    // Valores para Modo Carga Total Cuadrada (con lo que sale)
    var finalTrans by rememberSaveable(editId) { mutableIntStateOf(transEnUnidad) }
    var finalAzul by rememberSaveable(editId) {
        mutableIntStateOf(if (metaSalida > (transEnUnidad + marcEnUnidad)) metaSalida - transEnUnidad - marcEnUnidad else azulEnUnidad)
    }
    var finalMarc by rememberSaveable(editId) { mutableIntStateOf(marcEnUnidad) }
    var finalGal by rememberSaveable(editId) { mutableIntStateOf(inventario?.galonesLlenos ?: 0) }
    var finalPac by rememberSaveable(editId) { mutableIntStateOf(inventario?.pacasLlenas ?: 0) }
    var finalMin by rememberSaveable(editId) { mutableIntStateOf(inventario?.minisLlenas ?: 0) }

    val observaciones by rememberSaveable(editId) { mutableStateOf(recargaEnEdicion?.observaciones ?: "") }

    val totalCargadosAditivo = cantTrans + cantAzul + cantMarc
    val totalFinalCuadrado = finalTrans + finalAzul + finalMarc

    val diffTrans = maxOf(0, finalTrans - transEnUnidad)
    val diffAzul = maxOf(0, finalAzul - azulEnUnidad)
    val diffMarc = maxOf(0, finalMarc - marcEnUnidad)
    val totalCargadosCalculados = diffTrans + diffAzul + diffMarc

    val galonesFaltan = maxOf(0, (inventario?.capacidadTopeGalones ?: 90) - (inventario?.galonesLlenos ?: 0))
    val pacasFaltan = maxOf(0, (inventario?.capacidadTopePacas ?: 6) - (inventario?.pacasLlenas ?: 0))
    val minisFaltan = maxOf(0, (inventario?.capacidadTopeMinis ?: 2) - (inventario?.minisLlenas ?: 0))

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
                        text = "Recarga y cuadre de stock para la jornada",
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

        // Selector dinámico de Carga (Carga 1, Carga 2, Carga 3...) + Botón Cero
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    modifier = Modifier
                        .weight(1f)
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    val totalOpciones = maxOf(4, siguienteNumeroCarga, numeroVuelta)
                    (1..totalOpciones).forEach { num ->
                        FilterChip(
                            selected = numeroVuelta == num,
                            onClick = { numeroVuelta = num },
                            label = { Text("Carga $num", fontWeight = FontWeight.Bold, maxLines = 1) },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = clayTheme.accentBgColor,
                                selectedLabelColor = clayTheme.primaryDarkColor
                            )
                        )
                    }
                }
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

        // Modo de Recarga: Cuadrar Carga Total de Salida vs Sumar Unidades
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                FilterChip(
                    selected = !modoFijarTotal,
                    onClick = { modoFijarTotal = false },
                    label = { Text("Sumar cargados (+X)", fontWeight = FontWeight.Bold, fontSize = 12.sp) },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = clayTheme.accentBgColor,
                        selectedLabelColor = clayTheme.primaryDarkColor
                    ),
                    modifier = Modifier.weight(1f)
                )
                FilterChip(
                    selected = modoFijarTotal,
                    onClick = { modoFijarTotal = true },
                    label = { Text("Cuadrar salida ($metaSalida)", fontWeight = FontWeight.Bold, fontSize = 12.sp) },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = VerdeEfectivoFondo,
                        selectedLabelColor = VerdeEfectivo
                    ),
                    modifier = Modifier.weight(1f)
                )
            }
        }

        // Ajuste directo de Meta/Tope para esta salida (ej. cambiar a 189 o 218 o 80)
        item {
            ClayCard(
                modifier = Modifier.fillMaxWidth(),
                containerColor = Color.White,
                cornerRadius = 14.dp,
                elevation = 2.dp,
                contentPadding = PaddingValues(horizontal = 12.dp, vertical = 8.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Meta / Límite de carga para esta salida",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = TextoOscuro
                        )
                        Text(
                            text = "Tope usual: $metaActual (puedes ajustarlo a 189, 218, etc.)",
                            fontSize = 10.sp,
                            color = TextoSecundario
                        )
                    }
                    ClayEditableCounter(
                        valor = metaSalida,
                        onCambio = { nuevo -> if (nuevo > 0) metaSalida = nuevo },
                        accentColor = clayTheme.primaryColor
                    )
                }
            }
        }

        // Resumen Informativo de la Carga
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
                        val textoEncabezado = if (modoFijarTotal) {
                            "SALDRÁ CON: $totalFinalCuadrado / $metaSalida BIDONES"
                        } else {
                            val saldra = totalLlenosEnUnidad + totalCargadosAditivo
                            "SALDRÁ CON: $saldra / $metaSalida BIDONES"
                        }

                        Text(
                            text = textoEncabezado,
                            style = MaterialTheme.typography.labelSmall,
                            color = VerdeEfectivo,
                            fontWeight = FontWeight.Black,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )

                        val textoDetalle = if (modoFijarTotal) {
                            "Llegó a fábrica con $totalLlenosEnUnidad • Se suben +$totalCargadosCalculados en planta"
                        } else {
                            "En unidad: $totalLlenosEnUnidad • Subiendo: +$totalCargadosAditivo (Faltan $faltantes)"
                        }

                        Text(
                            text = textoDetalle,
                            style = MaterialTheme.typography.bodySmall,
                            color = TextoOscuro,
                            fontWeight = FontWeight.SemiBold,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                    ClayBadge(
                        text = if (modoFijarTotal) "$totalFinalCuadrado / $metaSalida" else "+$totalCargadosAditivo",
                        backgroundColor = VerdeEfectivo,
                        textColor = Color.White
                    )
                }
            }
        }

        item {
            Text(
                text = if (modoFijarTotal) "BIDONES TOTALES CON LOS QUE SALE ($totalFinalCuadrado)" else "BIDONES A SUBIR AL VEHÍCULO ($totalCargadosAditivo)",
                style = MaterialTheme.typography.labelLarge,
                color = TextoSecundario,
                fontWeight = FontWeight.Black,
                maxLines = 1
            )
        }

        if (modoFijarTotal) {
            item {
                FilaRecargaManualClay(
                    label = "Transparentes (PET)",
                    subtitulo = "Llegó con: $transEnUnidad • Se suben: +$diffTrans",
                    cantidad = finalTrans,
                    accentColor = BidonTransparenteColor,
                    ilustracion = { BidonIllustration(TipoBidon.TRANSPARENTE, size = 34.dp) },
                    onCambio = { finalTrans = it }
                )
            }

            item {
                FilaRecargaManualClay(
                    label = "Azules Estándar",
                    subtitulo = "Llegó con: $azulEnUnidad • Se suben: +$diffAzul",
                    cantidad = finalAzul,
                    accentColor = BidonAzulColor,
                    ilustracion = { BidonIllustration(TipoBidon.AZUL, size = 34.dp) },
                    onCambio = { finalAzul = it }
                )
            }

            item {
                FilaRecargaManualClay(
                    label = "Marcadas",
                    subtitulo = "Llegó con: $marcEnUnidad • Se suben: +$diffMarc",
                    cantidad = finalMarc,
                    accentColor = BidonMarcadoColor,
                    ilustracion = { BidonIllustration(TipoBidon.MARCADO, size = 34.dp) },
                    onCambio = { finalMarc = it }
                )
            }
        } else {
            item {
                FilaRecargaManualClay(
                    label = "Transparentes (PET)",
                    subtitulo = "En unidad: $transEnUnidad",
                    cantidad = cantTrans,
                    accentColor = BidonTransparenteColor,
                    ilustracion = { BidonIllustration(TipoBidon.TRANSPARENTE, size = 34.dp) },
                    onCambio = { cantTrans = it }
                )
            }

            item {
                FilaRecargaManualClay(
                    label = "Azules Estándar",
                    subtitulo = "En unidad: $azulEnUnidad",
                    cantidad = cantAzul,
                    accentColor = BidonAzulColor,
                    ilustracion = { BidonIllustration(TipoBidon.AZUL, size = 34.dp) },
                    onCambio = { cantAzul = it }
                )
            }

            item {
                FilaRecargaManualClay(
                    label = "Marcadas",
                    subtitulo = "En unidad: $marcEnUnidad",
                    cantidad = cantMarc,
                    accentColor = BidonMarcadoColor,
                    ilustracion = { BidonIllustration(TipoBidon.MARCADO, size = 34.dp) },
                    onCambio = { cantMarc = it }
                )
            }
        }

        item {
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = "GALONES, PACAS Y MINIS",
                style = MaterialTheme.typography.labelLarge,
                color = TextoSecundario,
                fontWeight = FontWeight.Black,
                maxLines = 1
            )
        }

        if (modoFijarTotal) {
            item {
                FilaRecargaManualClay(
                    label = "Galones (4L)",
                    subtitulo = "Llegó con: $galEnUnidad / ${inventario?.capacidadTopeGalones ?: 90}",
                    cantidad = finalGal,
                    accentColor = clayTheme.primaryColor,
                    ilustracion = { GalonIllustration(size = 34.dp) },
                    onCambio = { finalGal = it }
                )
            }

            item {
                FilaRecargaManualClay(
                    label = "Pacas (24x500ml)",
                    subtitulo = "Llegó con: $pacEnUnidad / ${inventario?.capacidadTopePacas ?: 6}",
                    cantidad = finalPac,
                    accentColor = clayTheme.primaryColor,
                    ilustracion = { PacaIllustration(size = 34.dp) },
                    onCambio = { finalPac = it }
                )
            }

            item {
                FilaRecargaManualClay(
                    label = "Minis (12x500ml)",
                    subtitulo = "Llegó con: $minEnUnidad / ${inventario?.capacidadTopeMinis ?: 2}",
                    cantidad = finalMin,
                    accentColor = clayTheme.primaryColor,
                    ilustracion = { MiniIllustration(size = 34.dp) },
                    onCambio = { finalMin = it }
                )
            }
        } else {
            item {
                FilaRecargaManualClay(
                    label = "Galones (4L)",
                    subtitulo = "En unidad: $galEnUnidad / ${inventario?.capacidadTopeGalones ?: 90} • Faltan: $galonesFaltan para tope",
                    cantidad = cantGalones,
                    accentColor = clayTheme.primaryColor,
                    ilustracion = { GalonIllustration(size = 34.dp) },
                    onCambio = { cantGalones = it }
                )
            }

            item {
                FilaRecargaManualClay(
                    label = "Pacas (24x500ml)",
                    subtitulo = "En unidad: $pacEnUnidad / ${inventario?.capacidadTopePacas ?: 6} • Faltan: $pacasFaltan para tope",
                    cantidad = cantPacas,
                    accentColor = clayTheme.primaryColor,
                    ilustracion = { PacaIllustration(size = 34.dp) },
                    onCambio = { cantPacas = it }
                )
            }

            item {
                FilaRecargaManualClay(
                    label = "Minis (12x500ml)",
                    subtitulo = "En unidad: $minEnUnidad / ${inventario?.capacidadTopeMinis ?: 2} • Faltan: $minisFaltan para tope",
                    cantidad = cantMinis,
                    accentColor = clayTheme.primaryColor,
                    ilustracion = { MiniIllustration(size = 34.dp) },
                    onCambio = { cantMinis = it }
                )
            }
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
                subtitulo = if (modoFijarTotal) "Se descargan todos los vacíos recolectados (${inventario?.bidonesVacios ?: 0})" else "Vacíos arriba: ${inventario?.bidonesVacios ?: 0}",
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
                    if (modoFijarTotal) {
                        onConfirmarRecarga(
                            numeroVuelta,
                            diffTrans,
                            diffAzul,
                            diffMarc,
                            cantVaciosBajar,
                            maxOf(0, finalGal - galEnUnidad),
                            maxOf(0, finalPac - pacEnUnidad),
                            maxOf(0, finalMin - minEnUnidad),
                            observaciones,
                            metaSalida,
                            true,
                            finalTrans,
                            finalAzul,
                            finalMarc,
                            finalGal,
                            finalPac,
                            finalMin
                        )
                    } else {
                        onConfirmarRecarga(
                            numeroVuelta,
                            cantTrans,
                            cantAzul,
                            cantMarc,
                            cantVaciosBajar,
                            cantGalones,
                            cantPacas,
                            cantMinis,
                            observaciones,
                            metaSalida,
                            false,
                            null, null, null, null, null, null
                        )
                    }
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
                val etiquetaBoton = if (recargaEnEdicion != null) {
                    "Guardar Cambios"
                } else if (modoFijarTotal) {
                    "Confirmar Salida ($totalFinalCuadrado / $metaSalida bidones)"
                } else {
                    "Confirmar Carga (+$totalCargadosAditivo bidones)"
                }
                Text(
                    text = etiquetaBoton,
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
                    text = "Carga #${recarga.numeroVuelta}",
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

            // Leyenda de 1 sola línea por ítem mostrando stock actual real en unidad
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
                    resaltado = true
                )
                ElementoLeyendaDonutClay(
                    color = BidonTransparenteColor,
                    fondoColor = BidonTransparenteFondo,
                    nombre = "PET",
                    vendidos = estadisticas.transparentesVendidos,
                    quedan = stockTrans,
                    resaltado = false
                )
                ElementoLeyendaDonutClay(
                    color = BidonAzulColor,
                    fondoColor = BidonAzulFondo,
                    nombre = "Azules",
                    vendidos = estadisticas.azulesVendidos,
                    quedan = stockAzul,
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
                text = "$quedan en unidad (Sal: $vendidos)",
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

package com.rutaagua.camion.ui.screens.inventario

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Build
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.LocalShipping
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
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
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.rutaagua.camion.data.local.entity.InventarioCamionEntity
import com.rutaagua.camion.data.local.entity.RecargaPlantaEntity
import com.rutaagua.camion.data.local.model.EstadisticasVentasTipo
import com.rutaagua.camion.data.local.model.RecargaCalculada
import com.rutaagua.camion.data.local.model.StockAlertas
import com.rutaagua.camion.data.local.model.TipoBidon
import com.rutaagua.camion.data.local.model.TipoVehiculo
import com.rutaagua.camion.ui.components.BidonIllustration
import androidx.compose.material.icons.filled.Sync
import com.rutaagua.camion.ui.components.CamionIllustration
import com.rutaagua.camion.ui.components.CamionetaIllustration
import com.rutaagua.camion.ui.components.CamionRepartidorIllustration
import com.rutaagua.camion.ui.components.GalonIllustration
import com.rutaagua.camion.ui.components.MiniIllustration
import com.rutaagua.camion.ui.components.PacaIllustration
import com.rutaagua.camion.ui.theme.AguaPrimary
import com.rutaagua.camion.ui.theme.AguaPrimaryDark
import com.rutaagua.camion.ui.theme.AguaPrimaryLight
import com.rutaagua.camion.ui.theme.AmbarAdvertencia
import com.rutaagua.camion.ui.theme.AmbarFondo
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

    var recargaParaEliminar by remember { mutableStateOf<RecargaPlantaEntity?>(null) }
    var recargaParaEditar by remember { mutableStateOf<RecargaPlantaEntity?>(null) }
    var mostrarPinParaEditarRecarga by remember { mutableStateOf(false) }
    var mostrarPinParaEliminarRecarga by remember { mutableStateOf(false) }

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
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            HeaderCamion(
                vehiculoActivo = vehiculoActivo,
                onAjustarCapacidadClick = { viewModel.abrirDialogoCapacidad() },
                onCambiarVehiculoClick = onCambiarVehiculoClick
            )
        }
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
            contentPadding = PaddingValues(top = 12.dp, bottom = 96.dp)
        ) {
            // 1. Alerta de Stock Crítico (si aplica)
            item {
                AnimatedVisibility(visible = alertasStock.mensajeAlerta != null) {
                    BannerAlertaStock(alertasStock)
                }
            }

            // 2. Tarjeta Resumen y Barra de Capacidad del Camión
            item {
                inventario?.let { inv ->
                    TarjetaCapacidadCamion(
                        inventario = inv,
                        onAjustarTopeClick = { viewModel.abrirDialogoCapacidad() }
                    )
                }
            }

            // 3. Botón Gigante de Acción Principal: Recargar en Planta
            item {
                BotonRecargarEnPlanta(
                    calculoRecarga = calculoRecarga,
                    onClick = { viewModel.abrirModalRecarga() }
                )
            }

            // 4. Tarjetas de los 3 Tipos de Bidones (Transparentes, Azules, Marcados)
            item {
                Text(
                    text = "BIDONES DISPONIBLES EN CAMIÓN (20L)",
                    style = MaterialTheme.typography.labelLarge,
                    color = TextoSecundario
                )
            }

            item {
                inventario?.let { inv ->
                    FilaTarjetasBidones(inv, estadisticasVentas)
                }
            }

            // 5. Otros Productos con Ilustraciones Gráficas (Galones, Pacas, Minis)
            item {
                Text(
                    text = "OTROS PRODUCTOS EN RUTA",
                    style = MaterialTheme.typography.labelLarge,
                    color = TextoSecundario
                )
            }

            item {
                inventario?.let { inv ->
                    FilaOtrosProductos(inv)
                }
            }

            // 6. Estadísticas de Ventas y Proporción de Productos (Gráfico Donut)
            item {
                Text(
                    text = "PROPORCIÓN DE VENTAS HOY",
                    style = MaterialTheme.typography.labelLarge,
                    color = TextoSecundario
                )
            }

            item {
                SeccionEstadisticasVentasHoy(
                    estadisticas = estadisticasVentas,
                    inventario = inventario
                )
            }

            // 7. Historial de Recargas del Día con Edición y Eliminación protegidas por PIN
            if (recargasHoy.isNotEmpty()) {
                item {
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = "RECARGAS REGISTRADAS HOY (${recargasHoy.size})",
                        style = MaterialTheme.typography.labelLarge,
                        color = TextoSecundario
                    )
                }

                items(recargasHoy.size) { idx ->
                    val recarga = recargasHoy[idx]
                    TarjetaRecargaItem(
                        recarga = recarga,
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

    // Modal para Registrar o Editar Recarga en Planta
    if (mostrarModalRecarga) {
        ModalBottomSheet(
            onDismissRequest = { viewModel.cerrarModalRecarga() },
            sheetState = sheetState
        ) {
            ModalRecargaPlantaContent(
                inventario = inventario,
                calculoRecarga = calculoRecarga,
                recargaEnEdicion = recargaEnEdicion,
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
            titulo = "Editar Carga de Camión",
            subtitulo = "Ingresa la contraseña de seguridad (PIN)",
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
            titulo = "Eliminar Carga de Camión",
            subtitulo = "Ingresa la contraseña de seguridad (PIN)",
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
        DialogoAjustarCapacidad(
            capacidadActual = inventario?.capacidadTopeBidones ?: 218,
            onConfirmar = { nuevoTope -> viewModel.cambiarCapacidadTope(nuevoTope) },
            onDismiss = { viewModel.cerrarDialogoCapacidad() }
        )
    }
}

/**
 * Encabezado de la pantalla con datos del camión.
 */
@Composable
private fun HeaderCamion(
    vehiculoActivo: TipoVehiculo,
    onAjustarCapacidadClick: () -> Unit,
    onCambiarVehiculoClick: (() -> Unit)? = null
) {
    Surface(
        color = MaterialTheme.colorScheme.surface,
        shadowElevation = 2.dp
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                modifier = Modifier.weight(1f, fill = false),
                verticalAlignment = Alignment.CenterVertically
            ) {
                if (vehiculoActivo == TipoVehiculo.CAMIONETA) {
                    CamionetaIllustration(size = 50.dp)
                } else {
                    CamionIllustration(size = 50.dp)
                }
                Spacer(modifier = Modifier.width(12.dp))
                Column {
                    Text(
                        text = if (vehiculoActivo == TipoVehiculo.CAMIONETA) "ESTADO DE LA CAMIONETA" else "ESTADO DEL CAMIÓN",
                        style = MaterialTheme.typography.titleLarge,
                        color = AguaPrimaryDark
                    )
                    Text(
                        text = "Inventario en vivo • 100% Offline",
                        style = MaterialTheme.typography.bodyMedium,
                        color = TextoSecundario
                    )
                }
            }

            Row(verticalAlignment = Alignment.CenterVertically) {
                if (onCambiarVehiculoClick != null) {
                    IconButton(onClick = onCambiarVehiculoClick) {
                        Icon(
                            imageVector = Icons.Default.Sync,
                            contentDescription = "Cambiar Vehículo",
                            tint = AguaPrimary
                        )
                    }
                }
                IconButton(onClick = onAjustarCapacidadClick) {
                    Icon(
                        imageVector = Icons.Default.Settings,
                        contentDescription = "Ajustar Capacidad",
                        tint = AguaPrimary
                    )
                }
            }
        }
    }
}

/**
 * Banner superior para advertencias cuando el stock es bajo.
 */
@Composable
private fun BannerAlertaStock(alerta: StockAlertas) {
    Card(
        colors = CardDefaults.cardColors(containerColor = RojoAlertaFondo),
        shape = RoundedCornerShape(12.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier.padding(14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = Icons.Default.Warning,
                contentDescription = "Alerta",
                tint = RojoAlerta,
                modifier = Modifier.size(28.dp)
            )
            Spacer(modifier = Modifier.width(10.dp))
            Column {
                Text(
                    text = "ATENCIÓN: STOCK BAJO",
                    style = MaterialTheme.typography.titleMedium,
                    color = RojoAlerta,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = alerta.mensajeAlerta ?: "Faltan unidades de reserva.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = TextoOscuro
                )
            }
        }
    }
}

/**
 * Barra visual de capacidad máxima del camión (Llenos + Vacíos frente a la meta diaria).
 */
@Composable
private fun TarjetaCapacidadCamion(
    inventario: InventarioCamionEntity,
    onAjustarTopeClick: () -> Unit
) {
    val totalLlenos = inventario.totalBidonesLlenos()
    val vaciosRecogidos = inventario.bidonesVacios
    val tope = inventario.capacidadTopeBidones
    val progreso = if (tope > 0) (totalLlenos.toFloat() / tope).coerceIn(0f, 1f) else 0f

    Card(
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        shape = RoundedCornerShape(16.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(18.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "CAPACIDAD BIDONES HOY",
                        style = MaterialTheme.typography.labelLarge,
                        color = TextoSecundario
                    )
                    Row(verticalAlignment = Alignment.Bottom) {
                        Text(
                            text = "$totalLlenos",
                            style = MaterialTheme.typography.displayMedium,
                            color = AguaPrimary,
                            fontWeight = FontWeight.Black
                        )
                        Text(
                            text = " / $tope llenos",
                            style = MaterialTheme.typography.titleMedium,
                            color = TextoSecundario,
                            modifier = Modifier.padding(bottom = 6.dp, start = 4.dp)
                        )
                    }
                }

                OutlinedButton(
                    onClick = onAjustarTopeClick,
                    shape = RoundedCornerShape(8.dp),
                    contentPadding = PaddingValues(horizontal = 10.dp, vertical = 6.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Build,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(text = "Meta: $tope")
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            LinearProgressIndicator(
                progress = { progreso },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(10.dp)
                    .clip(RoundedCornerShape(5.dp)),
                color = when {
                    progreso > 0.4f -> AguaPrimary
                    progreso > 0.15f -> AmbarAdvertencia
                    else -> RojoAlerta
                },
                trackColor = Color(0xFFE2E8F0),
                strokeCap = StrokeCap.Round
            )

            Spacer(modifier = Modifier.height(14.dp))

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color(0xFFF1F5F9), RoundedCornerShape(10.dp))
                    .padding(vertical = 8.dp, horizontal = 12.dp),
                horizontalArrangement = Arrangement.SpaceAround
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(10.dp)
                            .background(AguaPrimary, CircleShape)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "Llenos: $totalLlenos",
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Bold
                    )
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(10.dp)
                            .background(Color(0xFF64748B), CircleShape)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "Vacíos subidos: $vaciosRecogidos",
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF334155)
                    )
                }
            }
        }
    }
}

/**
 * Botón prominente para abrir la calculadora y modal de recarga en planta.
 */
@Composable
private fun BotonRecargarEnPlanta(
    calculoRecarga: RecargaCalculada?,
    onClick: () -> Unit
) {
    Button(
        onClick = onClick,
        modifier = Modifier
            .fillMaxWidth()
            .height(64.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = VerdeEfectivo
        ),
        shape = RoundedCornerShape(14.dp),
        elevation = ButtonDefaults.buttonElevation(defaultElevation = 4.dp)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            Icon(
                imageVector = Icons.Default.LocalShipping,
                contentDescription = null,
                modifier = Modifier.size(28.dp)
            )
            Spacer(modifier = Modifier.width(12.dp))
            Column {
                Text(
                    text = "RECARGAR EN PLANTA",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Black,
                    color = Color.White
                )
                calculoRecarga?.let {
                    Text(
                        text = "Toca cargar: ${it.bidonesFaltantesParaTope} bidones para llegar al tope",
                        style = MaterialTheme.typography.bodyMedium,
                        color = Color.White.copy(alpha = 0.9f)
                    )
                }
            }
        }
    }
}

/**
 * Tarjetas de los 3 tipos de bidones con números gigantes y alertas visuales.
 */
@Composable
private fun FilaTarjetasBidones(
    inventario: InventarioCamionEntity,
    estadisticas: EstadisticasVentasTipo? = null
) {
    val totalTrans = inventario.bidonesLlenosTransparentes + (estadisticas?.transparentesVendidos ?: 0)
    val totalAzul = inventario.bidonesLlenosAzules + (estadisticas?.azulesVendidos ?: 0)
    val totalMarc = inventario.bidonesLlenosMarcados + (estadisticas?.marcadosVendidos ?: 0)

    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        TarjetaBidonIndividual(
            tipo = TipoBidon.TRANSPARENTE,
            nombre = "PET Cristal",
            cantidad = inventario.bidonesLlenosTransparentes,
            totalCargado = totalTrans,
            colorFondo = BidonTransparenteFondo,
            colorBorde = BidonTransparenteColor,
            modifier = Modifier.weight(1f)
        )

        TarjetaBidonIndividual(
            tipo = TipoBidon.AZUL,
            nombre = "Azul Fuerte",
            cantidad = inventario.bidonesLlenosAzules,
            totalCargado = totalAzul,
            colorFondo = BidonAzulFondo,
            colorBorde = BidonAzulColor,
            modifier = Modifier.weight(1f)
        )

        TarjetaBidonIndividual(
            tipo = TipoBidon.MARCADO,
            nombre = "Marcado",
            cantidad = inventario.bidonesLlenosMarcados,
            totalCargado = totalMarc,
            colorFondo = BidonMarcadoFondo,
            colorBorde = BidonMarcadoColor,
            modifier = Modifier.weight(1f)
        )
    }
}

@Composable
private fun TarjetaBidonIndividual(
    tipo: TipoBidon,
    nombre: String,
    cantidad: Int,
    totalCargado: Int = 0,
    colorFondo: Color,
    colorBorde: Color,
    modifier: Modifier = Modifier
) {
    val stockBajo = cantidad <= 10

    Card(
        modifier = modifier
            .border(
                width = if (stockBajo) 2.dp else 1.dp,
                color = if (stockBajo) RojoAlerta else colorBorde.copy(alpha = 0.5f),
                shape = RoundedCornerShape(16.dp)
            ),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = colorFondo),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 12.dp, horizontal = 8.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            BidonIllustration(tipo = tipo, size = 68.dp)

            Spacer(modifier = Modifier.height(8.dp))

            Row(verticalAlignment = Alignment.Bottom) {
                Text(
                    text = "$cantidad",
                    style = MaterialTheme.typography.displayLarge,
                    fontWeight = FontWeight.Black,
                    color = if (stockBajo) RojoAlerta else TextoOscuro
                )
                if (totalCargado > 0) {
                    Text(
                        text = "/$totalCargado",
                        style = MaterialTheme.typography.bodySmall,
                        color = TextoSecundario,
                        modifier = Modifier.padding(bottom = 6.dp, start = 2.dp),
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            Text(
                text = nombre,
                style = MaterialTheme.typography.labelLarge,
                textAlign = TextAlign.Center,
                color = TextoOscuro
            )

            if (stockBajo) {
                Spacer(modifier = Modifier.height(4.dp))
                Surface(
                    color = RojoAlerta,
                    shape = RoundedCornerShape(6.dp)
                ) {
                    Text(
                        text = "BAJO",
                        color = Color.White,
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Black,
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                    )
                }
            }
        }
    }
}

/**
 * Fila con Galones, Pacas y Minis integrando sus ilustraciones en Canvas.
 */
@Composable
private fun FilaOtrosProductos(inventario: InventarioCamionEntity) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        // Galones 4L
        TarjetaProductoConIlustracion(
            nombre = "Galones (4L)",
            cantidad = inventario.galonesLlenos,
            tope = inventario.capacidadTopeGalones,
            precio = "$0.75",
            ilustracion = { GalonIllustration(size = 54.dp) },
            esBajo = inventario.galonesLlenos <= 5,
            modifier = Modifier.weight(1f)
        )

        // Pacas 24x
        TarjetaProductoConIlustracion(
            nombre = "Pacas (24x)",
            cantidad = inventario.pacasLlenas,
            tope = inventario.capacidadTopePacas,
            precio = "$3.75",
            ilustracion = { PacaIllustration(size = 54.dp) },
            esBajo = inventario.pacasLlenas <= 1,
            modifier = Modifier.weight(1f)
        )

        // Minis 12x
        TarjetaProductoConIlustracion(
            nombre = "Minis (12x)",
            cantidad = inventario.minisLlenas,
            tope = inventario.capacidadTopeMinis,
            precio = "$1.75",
            ilustracion = { MiniIllustration(size = 54.dp) },
            esBajo = inventario.minisLlenas <= 1,
            modifier = Modifier.weight(1f)
        )
    }
}

@Composable
private fun TarjetaProductoConIlustracion(
    nombre: String,
    cantidad: Int,
    tope: Int,
    precio: String,
    ilustracion: @Composable () -> Unit,
    esBajo: Boolean,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.border(
            width = if (esBajo) 2.dp else 1.dp,
            color = if (esBajo) RojoAlerta else Color(0xFFE2E8F0),
            shape = RoundedCornerShape(14.dp)
        ),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 10.dp, horizontal = 6.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Ilustración gráfica
            Box(
                modifier = Modifier.height(56.dp),
                contentAlignment = Alignment.Center
            ) {
                ilustracion()
            }

            Spacer(modifier = Modifier.height(6.dp))

            // Número grande de existencias
            Row(verticalAlignment = Alignment.Bottom) {
                Text(
                    text = "$cantidad",
                    style = MaterialTheme.typography.displayMedium,
                    fontWeight = FontWeight.Black,
                    color = if (esBajo) RojoAlerta else AguaPrimary
                )
                Text(
                    text = "/$tope",
                    style = MaterialTheme.typography.bodySmall,
                    color = TextoSecundario,
                    modifier = Modifier.padding(bottom = 4.dp, start = 2.dp)
                )
            }

            Text(
                text = nombre,
                style = MaterialTheme.typography.labelMedium,
                color = TextoOscuro,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center
            )

            Text(
                text = precio,
                style = MaterialTheme.typography.labelSmall,
                color = VerdeEfectivo,
                fontWeight = FontWeight.Black
            )
        }
    }
}

/**
 * Modal BottomSheet interactivo para la Recarga en Planta con todas las ilustraciones.
 */
@Composable
private fun ModalRecargaPlantaContent(
    inventario: InventarioCamionEntity?,
    calculoRecarga: RecargaCalculada?,
    recargaEnEdicion: RecargaPlantaEntity? = null,
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
    var numeroVuelta by remember { mutableIntStateOf(recargaEnEdicion?.numeroVuelta ?: 1) }

    val faltantes = calculoRecarga?.bidonesFaltantesParaTope ?: 0
    var cantTrans by remember {
        mutableIntStateOf(recargaEnEdicion?.bidonesCargadosTransparentes ?: (faltantes / 3))
    }
    var cantAzul by remember {
        mutableIntStateOf(recargaEnEdicion?.bidonesCargadosAzules ?: (faltantes - (faltantes / 3) - (faltantes / 6)))
    }
    var cantMarc by remember {
        mutableIntStateOf(recargaEnEdicion?.bidonesCargadosMarcados ?: (faltantes / 6))
    }
    var cantVaciosBajar by remember {
        mutableIntStateOf(recargaEnEdicion?.bidonesVaciosDescargados ?: (inventario?.bidonesVacios ?: 0))
    }

    var cantGalones by remember {
        mutableIntStateOf(recargaEnEdicion?.galonesCargados ?: (calculoRecarga?.galonesFaltantes ?: 0))
    }
    var cantPacas by remember {
        mutableIntStateOf(recargaEnEdicion?.pacasCargadas ?: (calculoRecarga?.pacasFaltantes ?: 0))
    }
    var cantMinis by remember {
        mutableIntStateOf(recargaEnEdicion?.minisCargadas ?: (calculoRecarga?.minisFaltantes ?: 0))
    }

    var observaciones by remember { mutableStateOf(recargaEnEdicion?.observaciones ?: "") }

    val totalCargados = cantTrans + cantAzul + cantMarc

    LazyColumn(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp, vertical = 8.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = if (recargaEnEdicion != null) "EDITAR CARGA EN PLANTA" else "RECARGA EN PLANTA",
                        style = MaterialTheme.typography.titleLarge,
                        color = AguaPrimaryDark
                    )
                    Text(
                        text = if (recargaEnEdicion != null) "Modifica los valores cargados en este viaje" else "Abastecimiento de camión",
                        style = MaterialTheme.typography.bodyMedium,
                        color = TextoSecundario
                    )
                }
                IconButton(onClick = onCancelar) {
                    Icon(imageVector = Icons.Default.Close, contentDescription = "Cerrar")
                }
            }
        }

        // Selector de Vuelta (1 o 2)
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                FilterChip(
                    selected = numeroVuelta == 1,
                    onClick = { numeroVuelta = 1 },
                    label = { Text("Vuelta 1 (Mañana)", fontWeight = FontWeight.Bold) },
                    modifier = Modifier.weight(1f)
                )
                FilterChip(
                    selected = numeroVuelta == 2,
                    onClick = { numeroVuelta = 2 },
                    label = { Text("Vuelta 2 (Tarde)", fontWeight = FontWeight.Bold) },
                    modifier = Modifier.weight(1f)
                )
            }
        }

        // Tarjeta Calculadora de Recarga Automática
        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = VerdeEfectivoFondo),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Text(
                        text = "CALCULADORA DE PLANTA",
                        style = MaterialTheme.typography.labelLarge,
                        color = VerdeEfectivo,
                        fontWeight = FontWeight.Black
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "El camión tiene ${calculoRecarga?.bidonesLlenosActuales ?: 0} llenos. Para llegar a la meta de ${calculoRecarga?.capacidadTopeBidones ?: 218}:",
                        style = MaterialTheme.typography.bodyMedium,
                        color = TextoOscuro
                    )
                    Text(
                        text = "Te toca recargar exactamente: $faltantes bidones",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = VerdeEfectivo
                    )
                }
            }
        }

        // Sección 1: Bidones de 20L
        item {
            Text(
                text = "BIDONES DE 20L A CARGAR ($totalCargados / $faltantes)",
                style = MaterialTheme.typography.labelLarge,
                color = TextoSecundario
            )
        }

        item {
            FilaSelectorConIcono(
                label = "Transparentes (PET)",
                cantidad = cantTrans,
                ilustracion = { BidonIllustration(TipoBidon.TRANSPARENTE, size = 36.dp) },
                onCambio = { cantTrans = it }
            )
        }

        item {
            FilaSelectorConIcono(
                label = "Azules Estándar",
                cantidad = cantAzul,
                ilustracion = { BidonIllustration(TipoBidon.AZUL, size = 36.dp) },
                onCambio = { cantAzul = it }
            )
        }

        item {
            FilaSelectorConIcono(
                label = "Marcados",
                cantidad = cantMarc,
                ilustracion = { BidonIllustration(TipoBidon.MARCADO, size = 36.dp) },
                onCambio = { cantMarc = it }
            )
        }

        // Sección 2: Otros Productos a Cargar con sus Ilustraciones
        item {
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "OTROS PRODUCTOS A CARGAR EN PLANTA",
                style = MaterialTheme.typography.labelLarge,
                color = TextoSecundario
            )
        }

        item {
            FilaSelectorConIcono(
                label = "Galones (4L) - Tope 90",
                cantidad = cantGalones,
                ilustracion = { GalonIllustration(size = 36.dp) },
                onCambio = { cantGalones = it }
            )
        }

        item {
            FilaSelectorConIcono(
                label = "Pacas (24 botellas) - Tope 6",
                cantidad = cantPacas,
                ilustracion = { PacaIllustration(size = 36.dp) },
                onCambio = { cantPacas = it }
            )
        }

        item {
            FilaSelectorConIcono(
                label = "Minis (12 botellas) - Tope 2",
                cantidad = cantMinis,
                ilustracion = { MiniIllustration(size = 36.dp) },
                onCambio = { cantMinis = it }
            )
        }

        // Descarga de Vacíos en Planta
        item {
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "DESCARGA DE ENVASES VACÍOS EN PLANTA",
                style = MaterialTheme.typography.labelLarge,
                color = TextoSecundario
            )
        }

        item {
            FilaSelectorCantidad(
                label = "Vacíos a dejar en fábrica",
                cantidad = cantVaciosBajar,
                color = Color(0xFF64748B),
                onCambio = { cantVaciosBajar = it }
            )
        }

        // Botón de confirmación de la recarga
        item {
            Spacer(modifier = Modifier.height(8.dp))
            Button(
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
                    .height(58.dp),
                colors = ButtonDefaults.buttonColors(containerColor = VerdeEfectivo),
                shape = RoundedCornerShape(12.dp)
            ) {
                Icon(imageVector = Icons.Default.Check, contentDescription = null)
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = if (recargaEnEdicion != null) "GUARDAR CAMBIOS (+$totalCargados BIDONES)" else "CONFIRMAR RECARGA (+$totalCargados BIDONES)",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
            }
            Spacer(modifier = Modifier.height(28.dp))
        }
    }
}

/**
 * Control numérico enriquecido con la ilustración del producto a la izquierda.
 */
@Composable
private fun FilaSelectorConIcono(
    label: String,
    cantidad: Int,
    ilustracion: @Composable () -> Unit,
    onCambio: (Int) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color(0xFFF8FAFC), RoundedCornerShape(10.dp))
            .padding(horizontal = 12.dp, vertical = 6.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.weight(1f)
        ) {
            Box(
                modifier = Modifier.size(36.dp),
                contentAlignment = Alignment.Center
            ) {
                ilustracion()
            }
            Spacer(modifier = Modifier.width(10.dp))
            Text(
                text = label,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.SemiBold,
                color = TextoOscuro
            )
        }

        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            IconButton(
                onClick = { if (cantidad > 0) onCambio(cantidad - 1) },
                modifier = Modifier
                    .size(36.dp)
                    .background(Color(0xFFE2E8F0), CircleShape)
            ) {
                Icon(imageVector = Icons.Default.Remove, contentDescription = "Menos", tint = TextoOscuro)
            }

            Box(
                modifier = Modifier
                    .width(48.dp)
                    .background(Color(0xFFE2E8F0), RoundedCornerShape(8.dp))
                    .padding(vertical = 4.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "$cantidad",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Black,
                    color = TextoOscuro,
                    textAlign = TextAlign.Center
                )
            }

            IconButton(
                onClick = { onCambio(cantidad + 1) },
                modifier = Modifier
                    .size(36.dp)
                    .background(AguaPrimary, CircleShape)
            ) {
                Icon(imageVector = Icons.Default.Add, contentDescription = "Más", tint = Color.White)
            }
        }
    }
}

@Composable
private fun FilaSelectorCantidad(
    label: String,
    cantidad: Int,
    color: Color,
    onCambio: (Int) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color(0xFFF8FAFC), RoundedCornerShape(10.dp))
            .padding(horizontal = 12.dp, vertical = 6.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.weight(1f)
        ) {
            Box(
                modifier = Modifier
                    .size(12.dp)
                    .background(color, CircleShape)
            )
            Spacer(modifier = Modifier.width(10.dp))
            Text(
                text = label,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.SemiBold,
                color = TextoOscuro
            )
        }

        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            IconButton(
                onClick = { if (cantidad > 0) onCambio(cantidad - 1) },
                modifier = Modifier
                    .size(36.dp)
                    .background(Color(0xFFE2E8F0), CircleShape)
            ) {
                Icon(imageVector = Icons.Default.Remove, contentDescription = "Menos", tint = TextoOscuro)
            }

            Box(
                modifier = Modifier
                    .width(48.dp)
                    .background(Color(0xFFE2E8F0), RoundedCornerShape(8.dp))
                    .padding(vertical = 4.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "$cantidad",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Black,
                    color = TextoOscuro,
                    textAlign = TextAlign.Center
                )
            }

            IconButton(
                onClick = { onCambio(cantidad + 1) },
                modifier = Modifier
                    .size(36.dp)
                    .background(AguaPrimary, CircleShape)
            ) {
                Icon(imageVector = Icons.Default.Add, contentDescription = "Más", tint = Color.White)
            }
        }
    }
}

@Composable
private fun TarjetaRecargaItem(
    recarga: com.rutaagua.camion.data.local.entity.RecargaPlantaEntity,
    onEditar: () -> Unit,
    onEliminar: () -> Unit
) {
    val formatoHora = remember { java.text.SimpleDateFormat("HH:mm", java.util.Locale.getDefault()) }
    val horaTexto = formatoHora.format(java.util.Date(recarga.fecha))

    Card(
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFE2E8F0)),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Surface(
                        color = AguaPrimaryLight.copy(alpha = 0.2f),
                        shape = RoundedCornerShape(6.dp)
                    ) {
                        Text(
                            text = "Vuelta ${recarga.numeroVuelta}",
                            color = AguaPrimaryDark,
                            fontWeight = FontWeight.Black,
                            style = MaterialTheme.typography.labelSmall,
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "Hora: $horaTexto",
                        style = MaterialTheme.typography.labelSmall,
                        color = TextoSecundario
                    )
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    IconButton(onClick = onEditar, modifier = Modifier.size(28.dp)) {
                        Icon(
                            imageVector = Icons.Default.Build,
                            contentDescription = "Editar carga",
                            tint = AguaPrimary,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                    IconButton(onClick = onEliminar, modifier = Modifier.size(28.dp)) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Eliminar carga",
                            tint = RojoAlerta,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(6.dp))

            // Detalle de productos cargados
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Cargado: +${recarga.totalBidonesCargados()} bidones",
                    fontWeight = FontWeight.Bold,
                    style = MaterialTheme.typography.bodyMedium,
                    color = TextoOscuro
                )
                Text(
                    text = "(T: ${recarga.bidonesCargadosTransparentes} • A: ${recarga.bidonesCargadosAzules} • M: ${recarga.bidonesCargadosMarcados})",
                    style = MaterialTheme.typography.bodySmall,
                    color = TextoSecundario,
                    fontSize = 11.sp
                )
            }

            if (recarga.galonesCargados > 0 || recarga.pacasCargadas > 0 || recarga.minisCargadas > 0) {
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = "Otros: ${recarga.galonesCargados} gal. • ${recarga.pacasCargadas} pac. • ${recarga.minisCargadas} min.",
                    style = MaterialTheme.typography.labelSmall,
                    color = TextoSecundario,
                    fontSize = 11.sp
                )
            }

            if (recarga.bidonesVaciosDescargados > 0) {
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = "Vacíos descargados en planta: -${recarga.bidonesVaciosDescargados}",
                    style = MaterialTheme.typography.labelSmall,
                    color = VerdeEfectivo,
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 11.sp
                )
            }
        }
    }
}

/**
 * Diálogo para cambiar rápidamente la meta del día (218, 189, 164, 163 o libre).
 */
@Composable
private fun DialogoAjustarCapacidad(
    capacidadActual: Int,
    onConfirmar: (Int) -> Unit,
    onDismiss: () -> Unit
) {
    var valorTexto by remember { mutableStateOf("$capacidadActual") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Ajustar Capacidad del Día", fontWeight = FontWeight.Bold) },
        text = {
            Column {
                Text(
                    text = "Selecciona la meta de bidones con la que salió el camión hoy:",
                    style = MaterialTheme.typography.bodyMedium,
                    color = TextoSecundario
                )
                Spacer(modifier = Modifier.height(14.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    listOf(218, 189, 164, 163).forEach { meta ->
                        OutlinedButton(
                            onClick = { valorTexto = "$meta" },
                            modifier = Modifier.weight(1f),
                            contentPadding = PaddingValues(horizontal = 4.dp, vertical = 6.dp)
                        ) {
                            Text(text = "$meta", fontWeight = FontWeight.Bold)
                        }
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                OutlinedTextField(
                    value = valorTexto,
                    onValueChange = { valorTexto = it.filter { char -> char.isDigit() } },
                    label = { Text("Capacidad tope (Bidones)") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val nuevoValor = valorTexto.toIntOrNull() ?: capacidadActual
                    onConfirmar(nuevoValor)
                }
            ) {
                Text("Guardar Meta")
            }
        },
        dismissButton = {
            OutlinedButton(onClick = onDismiss) {
                Text("Cancelar")
            }
        }
    )
}

/**
 * Gráfico tipo Donut en Canvas que muestra la proporción de bidones vendidos hoy
 * y tarjetas de resumen para otros productos.
 */
@Composable
private fun SeccionEstadisticasVentasHoy(
    estadisticas: EstadisticasVentasTipo,
    inventario: InventarioCamionEntity? = null
) {
    val totalBidones = estadisticas.totalBidonesVendidos
    val pctTrans = if (totalBidones > 0) estadisticas.transparentesVendidos.toFloat() / totalBidones else 0f
    val pctAzul = if (totalBidones > 0) estadisticas.azulesVendidos.toFloat() / totalBidones else 0f
    val pctMarc = if (totalBidones > 0) estadisticas.marcadosVendidos.toFloat() / totalBidones else 0f

    Card(
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFE2E8F0)),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = "Desglose por Tipo de Bidón",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Black,
                color = TextoOscuro
            )
            Spacer(modifier = Modifier.height(14.dp))

            // Gráfico Donut y Leyenda
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceAround
            ) {
                // Donut Chart en Canvas
                Box(
                    modifier = Modifier.size(130.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Canvas(modifier = Modifier.size(120.dp)) {
                        val strokeWidth = 22.dp.toPx()
                        if (totalBidones == 0) {
                            drawArc(
                                color = Color(0xFFE2E8F0),
                                startAngle = 0f,
                                sweepAngle = 360f,
                                useCenter = false,
                                style = Stroke(width = strokeWidth)
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
                                    style = Stroke(width = strokeWidth)
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
                                    style = Stroke(width = strokeWidth)
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
                                    style = Stroke(width = strokeWidth)
                                )
                            }
                        }
                    }

                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = "$totalBidones",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Black,
                            color = TextoOscuro
                        )
                        Text(
                            text = "Bidones",
                            style = MaterialTheme.typography.labelSmall,
                            color = TextoSecundario,
                            fontSize = 10.sp
                        )
                    }
                }

                // Leyenda con porcentajes y stock actual restante
                Column(
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                    modifier = Modifier.padding(start = 8.dp)
                ) {
                    val stockTrans = inventario?.bidonesLlenosTransparentes
                    val totalTransRecargados = estadisticas.transparentesVendidos + (stockTrans ?: 0)
                    val labelTransQuedan = if (stockTrans != null) " • Quedan: $stockTrans/$totalTransRecargados" else ""

                    val stockAzul = inventario?.bidonesLlenosAzules
                    val totalAzulRecargados = estadisticas.azulesVendidos + (stockAzul ?: 0)
                    val labelAzulQuedan = if (stockAzul != null) " • Quedan: $stockAzul/$totalAzulRecargados" else ""

                    val stockMarc = inventario?.bidonesLlenosMarcados
                    val totalMarcRecargados = estadisticas.marcadosVendidos + (stockMarc ?: 0)
                    val labelMarcQuedan = if (stockMarc != null) " • Quedan: $stockMarc/$totalMarcRecargados" else ""

                    ElementoLeyendaDonut(
                        color = BidonTransparenteColor,
                        nombre = "PET Transparente",
                        cantidad = estadisticas.transparentesVendidos,
                        porcentaje = estadisticas.porcentajeTransparentes.toInt(),
                        textoExtra = labelTransQuedan
                    )
                    ElementoLeyendaDonut(
                        color = BidonAzulColor,
                        nombre = "Azul Normal",
                        cantidad = estadisticas.azulesVendidos,
                        porcentaje = estadisticas.porcentajeAzules.toInt(),
                        textoExtra = labelAzulQuedan
                    )
                    ElementoLeyendaDonut(
                        color = BidonMarcadoColor,
                        nombre = "Marcado / Sello",
                        cantidad = estadisticas.marcadosVendidos,
                        porcentaje = estadisticas.porcentajeMarcados.toInt(),
                        textoExtra = labelMarcQuedan
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Mini resumen de Galones, Pacas y Minis
            Text(
                text = "Otros Productos Vendidos Hoy",
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.Bold,
                color = TextoSecundario
            )
            Spacer(modifier = Modifier.height(8.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                val gQuedan = inventario?.galonesLlenos
                val gTotal = (gQuedan ?: 0) + estadisticas.galonesVendidos
                TarjetaMiniEstadistica(
                    titulo = "Galones (4L)",
                    cantidad = estadisticas.galonesVendidos,
                    ilustracion = { GalonIllustration(size = 24.dp) },
                    textoExtra = if (gQuedan != null) "Quedan: $gQuedan/$gTotal" else null,
                    modifier = Modifier.weight(1f)
                )

                val pQuedan = inventario?.pacasLlenas
                val pTotal = (pQuedan ?: 0) + estadisticas.pacasVendidas
                TarjetaMiniEstadistica(
                    titulo = "Pacas (24x)",
                    cantidad = estadisticas.pacasVendidas,
                    ilustracion = { PacaIllustration(size = 24.dp) },
                    textoExtra = if (pQuedan != null) "Quedan: $pQuedan/$pTotal" else null,
                    modifier = Modifier.weight(1f)
                )

                val mQuedan = inventario?.minisLlenas
                val mTotal = (mQuedan ?: 0) + estadisticas.minisVendidas
                TarjetaMiniEstadistica(
                    titulo = "Minis (12x)",
                    cantidad = estadisticas.minisVendidas,
                    ilustracion = { MiniIllustration(size = 24.dp) },
                    textoExtra = if (mQuedan != null) "Quedan: $mQuedan/$mTotal" else null,
                    modifier = Modifier.weight(1f)
                )
            }
        }
    }
}

@Composable
private fun ElementoLeyendaDonut(
    color: Color,
    nombre: String,
    cantidad: Int,
    porcentaje: Int,
    textoExtra: String = ""
) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Box(
            modifier = Modifier
                .size(12.dp)
                .background(color, CircleShape)
        )
        Spacer(modifier = Modifier.width(8.dp))
        Column {
            Text(
                text = nombre,
                style = MaterialTheme.typography.bodySmall,
                fontWeight = FontWeight.Bold,
                color = TextoOscuro
            )
            Text(
                text = "$cantidad un. ($porcentaje%)$textoExtra",
                style = MaterialTheme.typography.labelSmall,
                color = TextoSecundario,
                fontSize = 11.sp
            )
        }
    }
}

@Composable
private fun TarjetaMiniEstadistica(
    titulo: String,
    cantidad: Int,
    ilustracion: @Composable () -> Unit,
    textoExtra: String? = null,
    modifier: Modifier = Modifier
) {
    Surface(
        color = Color(0xFFF8FAFC),
        shape = RoundedCornerShape(10.dp),
        border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFE2E8F0)),
        modifier = modifier
    ) {
        Column(
            modifier = Modifier.padding(10.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            ilustracion()
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "$cantidad un.",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Black,
                color = TextoOscuro
            )
            Text(
                text = titulo,
                style = MaterialTheme.typography.labelSmall,
                color = TextoSecundario,
                fontSize = 10.sp,
                textAlign = TextAlign.Center
            )
            if (textoExtra != null) {
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = textoExtra,
                    style = MaterialTheme.typography.labelSmall,
                    color = AguaPrimaryDark,
                    fontWeight = FontWeight.Bold,
                    fontSize = 9.sp,
                    textAlign = TextAlign.Center
                )
            }
        }
    }
}

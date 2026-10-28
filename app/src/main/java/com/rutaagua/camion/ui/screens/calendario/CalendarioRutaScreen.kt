package com.rutaagua.camion.ui.screens.calendario

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Chat
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowLeft
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.HourglassTop
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.MeetingRoom
import androidx.compose.material.icons.filled.Navigation
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.WaterDrop
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.rutaagua.camion.data.local.entity.ClienteEntity
import com.rutaagua.camion.data.local.entity.TransaccionVentaEntity
import com.rutaagua.camion.data.local.entity.VisitaFallidaEntity
import com.rutaagua.camion.data.local.model.DiaSemana
import com.rutaagua.camion.data.local.model.TipoBidon
import com.rutaagua.camion.data.local.model.TipoCliente
import com.rutaagua.camion.data.local.model.TipoVehiculo
import com.rutaagua.camion.data.local.prefs.AppPreferences
import com.rutaagua.camion.ui.components.BankingSegmentedTabs
import com.rutaagua.camion.ui.components.BidonIllustration
import com.rutaagua.camion.ui.components.ClayBadge
import com.rutaagua.camion.ui.components.ClayCard
import com.rutaagua.camion.ui.components.ClayInsetBox
import com.rutaagua.camion.ui.components.DialogoVistaPreviaFoto
import com.rutaagua.camion.ui.components.FotoReferenciaThumbnail
import com.rutaagua.camion.ui.components.GuardarUbicacionDialog
import com.rutaagua.camion.ui.components.VehiculoThumbnail
import com.rutaagua.camion.ui.components.marineOutlinedTextFieldColors
import com.rutaagua.camion.ui.components.obtenerClayThemePorVehiculo
import com.rutaagua.camion.ui.screens.clientes.ClienteRutaItem
import com.rutaagua.camion.ui.screens.clientes.ClientesViewModel
import com.rutaagua.camion.ui.screens.clientes.EstadoEntregaRuta
import com.rutaagua.camion.ui.screens.clientes.HistorialClienteModalSheet
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
import com.rutaagua.camion.ui.theme.BordeSutil
import com.rutaagua.camion.ui.theme.FondoGrisClaro
import com.rutaagua.camion.ui.theme.RojoAlerta
import com.rutaagua.camion.ui.theme.RojoAlertaFondo
import com.rutaagua.camion.ui.theme.SuperficieBlanca
import com.rutaagua.camion.ui.theme.TextoOscuro
import com.rutaagua.camion.ui.theme.TextoSecundario
import com.rutaagua.camion.ui.theme.VerdeEfectivo
import com.rutaagua.camion.ui.theme.VerdeEfectivoFondo
import com.rutaagua.camion.ui.utils.EvidenciaVisitaHelper
import com.rutaagua.camion.ui.utils.GpsLocationHelper
import com.rutaagua.camion.ui.utils.WhatsAppHelper
import java.io.File
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

enum class ModoVistaCalendario(val titulo: String) {
    RUTA_SEMANAL("Ruta Semanal (L-S)"),
    ARCHIVO_HISTORICO("Archivo por Mes / Día")
}

enum class FiltroEstadoRuta(val etiqueta: String) {
    TODOS("Todos"),
    PENDIENTES("Pendientes"),
    ATENDIDOS("Atendidos"),
    NO_ESTABAN("No estaban")
}

/**
 * Pantalla del Calendario de Ruta Semanal (Lunes a Sábado) + Archivo Histórico por Mes, Día y Cliente.
 * Permite tanto despachar la ruta semanal como navegar a cualquier mes/día pasado (ej. hace 2 meses)
 * y verificar si se le dejó agua a un cliente de llamada o de ruta.
 */
@Composable
fun CalendarioRutaScreen(
    viewModel: ClientesViewModel,
    modifier: Modifier = Modifier,
    onRegistrarEntregaCliente: ((ClienteEntity) -> Unit)? = null
) {
    val context = LocalContext.current
    val appPreferences = remember { AppPreferences.getInstance(context) }
    val vehiculoActivo by appPreferences.vehiculoActivo.collectAsState()
    val clayTheme = obtenerClayThemePorVehiculo(vehiculoActivo)

    var modoVista by rememberSaveable { mutableStateOf(ModoVistaCalendario.RUTA_SEMANAL) }

    val diaSeleccionado by viewModel.diaSeleccionadoCalendario.collectAsState()
    val clientesRuta by viewModel.clientesRutaDiaState.collectAsState()
    val conteoPorDia by viewModel.conteoPorDiaSemanaState.collectAsState()
    val todosLosClientes by viewModel.todosLosClientesState.collectAsState()
    val diaActualHoy = ClientesViewModel.obtenerDiaSemanaActual()

    // Estados del Historial Individual de Cliente
    val clienteHistorial by viewModel.clienteHistorialSeleccionado.collectAsState()
    val historialTransacciones by viewModel.historialTransaccionesClienteState.collectAsState()
    val historialVisitas by viewModel.historialVisitasClienteState.collectAsState()

    // Estados del Explorador Histórico por Mes y Día
    val mesHistorico by viewModel.mesHistoricoSeleccionado.collectAsState()
    val fechaHistorica by viewModel.fechaHistoricaSeleccionada.collectAsState()
    val busquedaHistorica by viewModel.busquedaHistoricaCliente.collectAsState()
    val transaccionesMes by viewModel.transaccionesMesHistoricoState.collectAsState()
    val visitasMes by viewModel.visitasMesHistoricoState.collectAsState()

    var filtroEstadoSeleccionado by rememberSaveable { mutableStateOf(FiltroEstadoRuta.TODOS) }
    var fotoZoomPath by remember { mutableStateOf<String?>(null) }
    var fotoZoomNombre by remember { mutableStateOf("") }
    var clienteParaUbicacion by remember { mutableStateOf<ClienteEntity?>(null) }

    // Manejo de Cámara para evidencia de "Cliente No Estaba"
    var clienteParaEvidencia by remember { mutableStateOf<ClienteEntity?>(null) }
    var archivoFotoTemp by remember { mutableStateOf<File?>(null) }

    val cameraLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.TakePicture()
    ) { exitoso ->
        if (exitoso && clienteParaEvidencia != null && archivoFotoTemp != null) {
            val cliente = clienteParaEvidencia!!
            val archivo = archivoFotoTemp!!
            val horaActual = SimpleDateFormat("HH:mm", Locale.getDefault()).format(Date())
            val fechaActual = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).format(Date())

            viewModel.registrarVisitaFallida(cliente, archivo.absolutePath, vehiculoActivo.name)

            val mensaje = WhatsAppHelper.generarMensajeVisitaFallida(
                nombreCliente = cliente.nombre,
                fecha = fechaActual,
                hora = horaActual
            )
            WhatsAppHelper.enviarVisitaFallidaConFoto(
                context = context,
                telefonoRaw = cliente.telefono,
                mensaje = mensaje,
                fotoPath = archivo.absolutePath
            )
        }
        clienteParaEvidencia = null
        archivoFotoTemp = null
    }

    val totalClientes = clientesRuta.size
    val atendidos = clientesRuta.count { it.estado == EstadoEntregaRuta.ATENDIDO }
    val noEstaban = clientesRuta.count { it.estado == EstadoEntregaRuta.NO_ESTABA }
    val pendientes = clientesRuta.count { it.estado == EstadoEntregaRuta.PENDIENTE }

    val clientesFiltrados = remember(clientesRuta, filtroEstadoSeleccionado) {
        when (filtroEstadoSeleccionado) {
            FiltroEstadoRuta.TODOS -> clientesRuta
            FiltroEstadoRuta.PENDIENTES -> clientesRuta.filter { it.estado == EstadoEntregaRuta.PENDIENTE || it.estado == EstadoEntregaRuta.PROGRAMADO }
            FiltroEstadoRuta.ATENDIDOS -> clientesRuta.filter { it.estado == EstadoEntregaRuta.ATENDIDO }
            FiltroEstadoRuta.NO_ESTABAN -> clientesRuta.filter { it.estado == EstadoEntregaRuta.NO_ESTABA }
        }
    }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        containerColor = clayTheme.canvasColor
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            // Selector Superior: Ruta Semanal (L-S) vs Archivo por Mes / Día
            Box(modifier = Modifier.padding(horizontal = 16.dp, vertical = 6.dp)) {
                BankingSegmentedTabs(
                    options = ModoVistaCalendario.entries,
                    selectedOption = modoVista,
                    labelProvider = { it.titulo },
                    onOptionSelected = { modoVista = it },
                    activeColor = clayTheme.primary
                )
            }

            if (modoVista == ModoVistaCalendario.RUTA_SEMANAL) {
                // 1. Píldoras compactas para seleccionar día de la semana
                SelectorDiasPildoras(
                    diaSeleccionado = diaSeleccionado,
                    diaActualHoy = diaActualHoy,
                    conteoPorDia = conteoPorDia,
                    onSeleccionarDia = {
                        viewModel.seleccionarDiaCalendario(it)
                        filtroEstadoSeleccionado = FiltroEstadoRuta.TODOS
                    }
                )

                // 2. Barra de progreso visual segmentada (solo para el día actual)
                if (diaSeleccionado == diaActualHoy && totalClientes > 0) {
                    BarraProgresoSegmentada(
                        atendidos = atendidos,
                        noEstaban = noEstaban,
                        pendientes = pendientes,
                        total = totalClientes
                    )
                }

                // 3. Resumen de botellones requeridos por tipo
                if (clientesRuta.isNotEmpty()) {
                    ResumenBotellonesRequeridos(clientesRuta = clientesRuta)
                }

                // 4. Filtros rápidos en Chips
                FilaFiltrosEstado(
                    filtroSeleccionado = filtroEstadoSeleccionado,
                    total = totalClientes,
                    atendidos = atendidos,
                    noEstaban = noEstaban,
                    pendientes = pendientes,
                    onSeleccionarFiltro = { filtroEstadoSeleccionado = it }
                )

                // 5. Lista de Clientes Programados
                if (clientesFiltrados.isEmpty()) {
                    EstadoVacioDiaRuta(dia = diaSeleccionado)
                } else {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        items(clientesFiltrados, key = { it.cliente.id }) { item ->
                            TarjetaClienteRutaRedisenada(
                                item = item,
                                onEntregarAguaClick = {
                                    onRegistrarEntregaCliente?.invoke(item.cliente)
                                },
                                onNoEstabaClick = {
                                    val archivo = EvidenciaVisitaHelper.crearArchivoFotoEvidencia(context, item.cliente.id)
                                    archivoFotoTemp = archivo
                                    clienteParaEvidencia = item.cliente
                                    val uri = EvidenciaVisitaHelper.obtenerUriParaArchivo(context, archivo)
                                    cameraLauncher.launch(uri)
                                },
                                onAbrirMapa = {
                                    if (item.cliente.latitud != null || item.cliente.enlaceMapa.isNotBlank()) {
                                        GpsLocationHelper.abrirNavegacionDirecta(
                                            context = context,
                                            latitud = item.cliente.latitud,
                                            longitud = item.cliente.longitud,
                                            enlaceAlternativo = item.cliente.enlaceMapa
                                        )
                                    } else {
                                        clienteParaUbicacion = item.cliente
                                    }
                                },
                                onGuardarUbicacion = {
                                    clienteParaUbicacion = item.cliente
                                },
                                onHistorialClick = {
                                    viewModel.abrirHistorialCliente(item.cliente)
                                },
                                onVerFoto = { path, nombre ->
                                    fotoZoomPath = path
                                    fotoZoomNombre = nombre
                                },
                                onContactarWhatsApp = {
                                    val mensaje = WhatsAppHelper.generarMensajeEstadoCuenta(item.cliente)
                                    WhatsAppHelper.abrirChatWhatsApp(context, item.cliente.telefono, mensaje)
                                }
                            )
                        }
                        item {
                            Spacer(modifier = Modifier.height(80.dp))
                        }
                    }
                }
            } else {
                // MODO ARCHIVO HISTÓRICO POR MES, DÍA ESPECÍFICO Y CLIENTE
                SeccionArchivoHistoricoMesDia(
                    mesIso = mesHistorico,
                    mesActualIso = viewModel.mesActualIso,
                    fechaSeleccionadaIso = fechaHistorica,
                    busquedaCliente = busquedaHistorica,
                    transaccionesMes = transaccionesMes,
                    visitasMes = visitasMes,
                    clientes = todosLosClientes,
                    onCambiarMes = { delta -> viewModel.cambiarMesHistorico(delta) },
                    onIrMesActual = { viewModel.seleccionarMesActualHistorico() },
                    onSeleccionarFecha = { f -> viewModel.seleccionarFechaHistorica(f) },
                    onBusquedaChange = { q -> viewModel.onBusquedaHistoricaChange(q) },
                    onAbrirHistorialCliente = { c -> viewModel.abrirHistorialCliente(c) }
                )
            }
        }
    }

    if (clienteParaUbicacion != null) {
        GuardarUbicacionDialog(
            cliente = clienteParaUbicacion!!,
            onGuardarUbicacion = { lat, lng, enlace ->
                viewModel.actualizarUbicacionCliente(clienteParaUbicacion!!, lat, lng, enlace)
                clienteParaUbicacion = null
            },
            onDismissRequest = { clienteParaUbicacion = null }
        )
    }

    if (fotoZoomPath != null) {
        DialogoVistaPreviaFoto(
            fotoPath = fotoZoomPath!!,
            nombreCliente = fotoZoomNombre,
            onDismiss = { fotoZoomPath = null }
        )
    }

    clienteHistorial?.let { cliente ->
        HistorialClienteModalSheet(
            cliente = cliente,
            transacciones = historialTransacciones,
            visitasFallidas = historialVisitas,
            onDismiss = { viewModel.cerrarHistorialCliente() }
        )
    }
}

/**
 * Explorador Histórico Completo por Mes, Día Específico y Cliente (Ruta o Llamada).
 * Permite navegar hacia atrás a cualquier mes (ej. hace 2 meses o el año pasado),
 * tocar un día específico del calendario o ver todo el mes, y buscar por nombre de cliente.
 */
@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun SeccionArchivoHistoricoMesDia(
    mesIso: String,
    mesActualIso: String,
    fechaSeleccionadaIso: String?,
    busquedaCliente: String,
    transaccionesMes: List<TransaccionVentaEntity>,
    visitasMes: List<VisitaFallidaEntity>,
    clientes: List<ClienteEntity>,
    onCambiarMes: (Int) -> Unit,
    onIrMesActual: () -> Unit,
    onSeleccionarFecha: (String?) -> Unit,
    onBusquedaChange: (String) -> Unit,
    onAbrirHistorialCliente: (ClienteEntity) -> Unit
) {
    val mapaClientes = remember(clientes) { clientes.associateBy { it.id } }

    // Calcular nombre legible del mes (ej. "Noviembre 2026") y cantidad de días del mes
    val (nombreMesLegible, diasDelMes) = remember(mesIso) {
        try {
            val partes = mesIso.split("-")
            val anio = partes[0].toInt()
            val mesIdx = partes[1].toInt() - 1
            val cal = Calendar.getInstance().apply {
                set(Calendar.YEAR, anio)
                set(Calendar.MONTH, mesIdx)
                set(Calendar.DAY_OF_MONTH, 1)
            }
            val nombre = SimpleDateFormat("MMMM yyyy", Locale("es", "EC"))
                .format(cal.time)
                .replaceFirstChar { it.uppercase() }
            val maxDia = cal.getActualMaximum(Calendar.DAY_OF_MONTH)
            Pair(nombre, maxDia)
        } catch (_: Exception) {
            Pair(mesIso, 30)
        }
    }

    // Días del mes que tienen al menos 1 entrega o visita registrada
    val diasConActividad = remember(transaccionesMes, visitasMes) {
        val set = mutableSetOf<String>()
        transaccionesMes.forEach { set.add(it.fechaTexto) }
        visitasMes.forEach { set.add(it.fechaIso) }
        set
    }

    // Filtrar por día específico (si está seleccionado) y por nombre de cliente
    val transaccionesFiltradas = remember(transaccionesMes, fechaSeleccionadaIso, busquedaCliente, mapaClientes) {
        transaccionesMes.filter { tx ->
            val coincideFecha = fechaSeleccionadaIso == null || tx.fechaTexto == fechaSeleccionadaIso
            val nombreCliente = tx.clienteId?.let { mapaClientes[it]?.nombre }
                ?: tx.nombreClientePaso
                ?: ""
            val ubicacionCliente = tx.clienteId?.let { mapaClientes[it]?.ubicacion } ?: ""
            val coincideCliente = busquedaCliente.isBlank() ||
                    nombreCliente.contains(busquedaCliente, ignoreCase = true) ||
                    ubicacionCliente.contains(busquedaCliente, ignoreCase = true)
            coincideFecha && coincideCliente
        }
    }

    val visitasFiltradas = remember(visitasMes, fechaSeleccionadaIso, busquedaCliente) {
        visitasMes.filter { v ->
            val coincideFecha = fechaSeleccionadaIso == null || v.fechaIso == fechaSeleccionadaIso
            val coincideCliente = busquedaCliente.isBlank() ||
                    v.nombreCliente.contains(busquedaCliente, ignoreCase = true)
            coincideFecha && coincideCliente
        }
    }

    val totalBidonesPeriodo = remember(transaccionesFiltradas) {
        transaccionesFiltradas.sumOf {
            it.cantidadBidonesRecarga + it.cantidadBidonesPrestados + it.cantidadBidonesVendidosEnvase
        }
    }
    val totalMontoPeriodo = remember(transaccionesFiltradas) {
        transaccionesFiltradas.sumOf { it.totalImporte }
    }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        // 1. Selector de Mes y Año (< Noviembre 2026 >) + Cuadrícula de Días
        item {
            ClayCard(
                modifier = Modifier.fillMaxWidth(),
                cornerRadius = 18.dp,
                contentPadding = PaddingValues(14.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Surface(
                        onClick = { onCambiarMes(-1) },
                        color = FondoGrisClaro,
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier.size(38.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.KeyboardArrowLeft,
                                contentDescription = "Mes anterior",
                                tint = TextoOscuro
                            )
                        }
                    }

                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.weight(1f)
                    ) {
                        Text(
                            text = nombreMesLegible,
                            fontWeight = FontWeight.Black,
                            fontSize = 16.sp,
                            color = TextoOscuro,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        Text(
                            text = if (fechaSeleccionadaIso == null) "Mostrando todo el mes"
                            else "Día seleccionado: ${fechaSeleccionadaIso.split("-").reversed().joinToString("/")}",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = AguaPrimaryDark,
                            maxLines = 1
                        )
                    }

                    Surface(
                        onClick = { onCambiarMes(1) },
                        color = FondoGrisClaro,
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier.size(38.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                                contentDescription = "Mes siguiente",
                                tint = TextoOscuro
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Botones rápidos: [Ver Todo el Mes] y [Hoy / Mes Actual]
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    val viendoTodoElMes = fechaSeleccionadaIso == null
                    Surface(
                        onClick = { onSeleccionarFecha(null) },
                        color = if (viendoTodoElMes) AguaPrimary else FondoGrisClaro,
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier
                            .weight(1f)
                            .height(36.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Text(
                                text = "Ver Todo el Mes (${transaccionesMes.size})",
                                fontWeight = FontWeight.Bold,
                                fontSize = 11.sp,
                                color = if (viendoTodoElMes) Color.White else TextoOscuro,
                                maxLines = 1,
                                softWrap = false,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                    }

                    if (mesIso != mesActualIso) {
                        Surface(
                            onClick = onIrMesActual,
                            color = AguaPrimary.copy(alpha = 0.14f),
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier
                                .weight(1f)
                                .height(36.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Text(
                                    text = "Ir al Mes Actual",
                                    fontWeight = FontWeight.Black,
                                    fontSize = 11.sp,
                                    color = AguaPrimaryDark,
                                    maxLines = 1,
                                    softWrap = false
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Selector Horizontal de Días del Mes (1..31) con punto indicador en días con ventas
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    for (diaNum in 1..diasDelMes) {
                        val diaStr = String.format(Locale.US, "%02d", diaNum)
                        val fechaDiaIso = "$mesIso-$diaStr"
                        val seleccionado = fechaSeleccionadaIso == fechaDiaIso
                        val tieneDatos = diasConActividad.contains(fechaDiaIso)

                        Surface(
                            onClick = {
                                if (seleccionado) onSeleccionarFecha(null)
                                else onSeleccionarFecha(fechaDiaIso)
                            },
                            color = when {
                                seleccionado -> AguaPrimary
                                tieneDatos -> Color(0xFFE0F2FE)
                                else -> FondoGrisClaro
                            },
                            shape = RoundedCornerShape(10.dp),
                            border = if (tieneDatos && !seleccionado) BorderStroke(1.dp, AguaPrimary.copy(alpha = 0.4f)) else null,
                            modifier = Modifier.width(42.dp)
                        ) {
                            Column(
                                modifier = Modifier.padding(vertical = 6.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Text(
                                    text = "$diaNum",
                                    fontWeight = FontWeight.Black,
                                    fontSize = 13.sp,
                                    color = if (seleccionado) Color.White else TextoOscuro,
                                    maxLines = 1
                                )
                                Spacer(modifier = Modifier.height(2.dp))
                                Box(
                                    modifier = Modifier
                                        .size(5.dp)
                                        .clip(CircleShape)
                                        .background(
                                            when {
                                                seleccionado -> Color.White
                                                tieneDatos -> VerdeEfectivo
                                                else -> Color.Transparent
                                            }
                                        )
                                )
                            }
                        }
                    }
                }
            }
        }

        // 2. Buscador de Cliente Específico (de Llamada o de Ruta) en el mes o fecha
        item {
            OutlinedTextField(
                value = busquedaCliente,
                onValueChange = onBusquedaChange,
                placeholder = {
                    Text(
                        text = "Buscar cliente en este mes o fecha...",
                        fontSize = 13.sp,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                },
                leadingIcon = {
                    Icon(Icons.Default.Search, contentDescription = null, tint = AguaPrimary)
                },
                trailingIcon = {
                    if (busquedaCliente.isNotEmpty()) {
                        IconButton(onClick = { onBusquedaChange("") }) {
                            Icon(Icons.Default.Clear, contentDescription = "Limpiar")
                        }
                    }
                },
                singleLine = true,
                shape = RoundedCornerShape(14.dp),
                colors = marineOutlinedTextFieldColors(),
                modifier = Modifier.fillMaxWidth()
            )
        }

        // 3. Resumen Rápido del Filtro Seleccionado
        item {
            ClayInsetBox(
                modifier = Modifier.fillMaxWidth(),
                cornerRadius = 14.dp,
                contentPadding = PaddingValues(horizontal = 14.dp, vertical = 10.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "${transaccionesFiltradas.size} entregas • $totalBidonesPeriodo bidones",
                        fontWeight = FontWeight.Black,
                        fontSize = 12.sp,
                        color = TextoOscuro,
                        maxLines = 1
                    )
                    Text(
                        text = "Total: $${String.format(Locale.US, "%.2f", totalMontoPeriodo)}",
                        fontWeight = FontWeight.Black,
                        fontSize = 13.sp,
                        color = VerdeEfectivo,
                        maxLines = 1
                    )
                }
            }
        }

        // 4. Listado de Entregas y Visitas de la Fecha/Mes
        if (transaccionesFiltradas.isEmpty() && visitasFiltradas.isEmpty()) {
            item {
                ClayCard(
                    modifier = Modifier.fillMaxWidth(),
                    cornerRadius = 18.dp
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Icon(
                            imageVector = Icons.Default.CalendarMonth,
                            contentDescription = null,
                            tint = TextoSecundario.copy(alpha = 0.5f),
                            modifier = Modifier.size(40.dp)
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = if (fechaSeleccionadaIso != null) {
                                "Sin entregas el ${fechaSeleccionadaIso.split("-").reversed().joinToString("/")}"
                            } else {
                                "Sin registros en $nombreMesLegible"
                            },
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp,
                            color = TextoOscuro
                        )
                        if (fechaSeleccionadaIso != null && transaccionesMes.isNotEmpty()) {
                            Spacer(modifier = Modifier.height(6.dp))
                            Text(
                                text = "Toca \"Ver Todo el Mes\" para ver las ${transaccionesMes.size} entregas de $nombreMesLegible.",
                                fontSize = 12.sp,
                                color = TextoSecundario,
                                textAlign = TextAlign.Center
                            )
                        }
                    }
                }
            }
        } else {
            val horaFormat = SimpleDateFormat("dd/MM/yyyy • HH:mm", Locale.getDefault())

            items(transaccionesFiltradas, key = { "tx_${it.id}" }) { tx ->
                val clienteObj = tx.clienteId?.let { mapaClientes[it] }
                val nombreCliente = clienteObj?.nombre
                    ?: tx.nombreClientePaso?.takeIf { it.isNotBlank() }
                    ?: "Cliente #${tx.id}"
                val etiquetaVehiculo = if (tx.vehiculo == "CAMIONETA") "🛻 Camioneta" else "🚚 Camión"

                ClayCard(
                    modifier = Modifier.fillMaxWidth(),
                    cornerRadius = 16.dp,
                    elevation = 2.dp,
                    contentPadding = PaddingValues(14.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    text = nombreCliente,
                                    fontWeight = FontWeight.Black,
                                    fontSize = 15.sp,
                                    color = TextoOscuro,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis,
                                    modifier = Modifier.weight(1f, fill = false)
                                )
                                if (clienteObj != null) {
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Surface(
                                        color = if (clienteObj.tipoCliente == TipoCliente.LLAMADA) Color(0xFFF0FDF4) else Color(0xFFE0F2FE),
                                        shape = RoundedCornerShape(4.dp)
                                    ) {
                                        Text(
                                            text = if (clienteObj.tipoCliente == TipoCliente.LLAMADA) "LLAMADA" else "RUTA",
                                            fontSize = 9.sp,
                                            fontWeight = FontWeight.Black,
                                            color = if (clienteObj.tipoCliente == TipoCliente.LLAMADA) VerdeEfectivo else AguaPrimaryDark,
                                            maxLines = 1,
                                            modifier = Modifier.padding(horizontal = 4.dp, vertical = 1.dp)
                                        )
                                    }
                                }
                            }
                            Text(
                                text = "${horaFormat.format(Date(tx.fecha))} • $etiquetaVehiculo",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = TextoSecundario,
                                maxLines = 1
                            )
                        }

                        Spacer(modifier = Modifier.width(8.dp))

                        Text(
                            text = "$${String.format(Locale.US, "%.2f", tx.totalImporte)}",
                            fontWeight = FontWeight.Black,
                            fontSize = 17.sp,
                            color = VerdeEfectivo,
                            maxLines = 1
                        )
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    FlowRow(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        val tieneMulti = (tx.recargaTransparentes + tx.recargaAzules + tx.recargaMarcados) > 0
                        if (tieneMulti) {
                            if (tx.recargaTransparentes > 0) {
                                ClayBadge("${tx.recargaTransparentes} PET", BidonTransparenteFondo, BidonTransparenteColor)
                            }
                            if (tx.recargaAzules > 0) {
                                ClayBadge("${tx.recargaAzules} Azul", BidonAzulFondo, BidonAzulColor)
                            }
                            if (tx.recargaMarcados > 0) {
                                ClayBadge("${tx.recargaMarcados} Marc", BidonMarcadoFondo, BidonMarcadoColor)
                            }
                        } else if (tx.cantidadBidonesRecarga > 0) {
                            ClayBadge("${tx.cantidadBidonesRecarga} ${tx.tipoBidon.name}", BidonAzulFondo, BidonAzulColor)
                        }

                        if (tx.cantidadBidonesPrestados > 0) {
                            ClayBadge("+${tx.cantidadBidonesPrestados} Prest.", AmbarFondo, AmbarAdvertencia)
                        }
                        if (tx.cantidadBidonesGratis > 0) {
                            ClayBadge("+${tx.cantidadBidonesGratis} Gratis", VerdeEfectivoFondo, VerdeEfectivo)
                        }
                        if (tx.montoEfectivo > 0) {
                            ClayBadge("Efec $${String.format(Locale.US, "%.2f", tx.montoEfectivo)}", VerdeEfectivoFondo, VerdeEfectivo)
                        }
                        if (tx.montoTransferencia > 0) {
                            ClayBadge("Transf $${String.format(Locale.US, "%.2f", tx.montoTransferencia)}", Color(0xFFDBEAFE), AguaPrimaryDark)
                        }
                        if (tx.montoCredito > 0) {
                            ClayBadge("Fiado $${String.format(Locale.US, "%.2f", tx.montoCredito)}", RojoAlertaFondo, RojoAlerta)
                        }
                    }

                    if (clienteObj != null) {
                        Spacer(modifier = Modifier.height(8.dp))
                        Surface(
                            onClick = { onAbrirHistorialCliente(clienteObj) },
                            color = AguaPrimary.copy(alpha = 0.1f),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = Icons.Default.History,
                                    contentDescription = null,
                                    tint = AguaPrimaryDark,
                                    modifier = Modifier.size(14.dp)
                                )
                                Spacer(modifier = Modifier.width(5.dp))
                                Text(
                                    text = "Ver todo el historial de ${clienteObj.nombre}",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = AguaPrimaryDark,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }
                        }
                    }
                }
            }

            items(visitasFiltradas, key = { "vis_${it.id}" }) { vis ->
                Surface(
                    color = AmbarFondo,
                    shape = RoundedCornerShape(14.dp),
                    border = BorderStroke(1.dp, AmbarAdvertencia.copy(alpha = 0.35f)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(12.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = vis.nombreCliente,
                                fontWeight = FontWeight.Black,
                                fontSize = 14.sp,
                                color = TextoOscuro,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                            Text(
                                text = "No estaba en casa • ${vis.fechaIso.split("-").reversed().joinToString("/")} ${vis.horaDisplay}",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = AmbarAdvertencia,
                                maxLines = 1
                            )
                        }
                        ClayBadge("Ausente", Color.White, AmbarAdvertencia)
                    }
                }
            }
        }

        item {
            Spacer(modifier = Modifier.height(80.dp))
        }
    }
}

/**
 * Selector de días en píldoras compactas y estilizadas.
 */
@Composable
private fun SelectorDiasPildoras(
    diaSeleccionado: DiaSemana,
    diaActualHoy: DiaSemana,
    conteoPorDia: Map<DiaSemana, Int>,
    onSeleccionarDia: (DiaSemana) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .horizontalScroll(rememberScrollState())
            .padding(horizontal = 16.dp, vertical = 6.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        DiaSemana.entries.forEach { dia ->
            val esSeleccionado = dia == diaSeleccionado
            val esHoy = dia == diaActualHoy
            val count = conteoPorDia[dia] ?: 0

            val abrev = when (dia) {
                DiaSemana.LUNES -> "LUN"
                DiaSemana.MARTES -> "MAR"
                DiaSemana.MIERCOLES -> "MIÉ"
                DiaSemana.JUEVES -> "JUE"
                DiaSemana.VIERNES -> "VIE"
                DiaSemana.SABADO -> "SÁB"
            }

            Surface(
                modifier = Modifier
                    .clickable { onSeleccionarDia(dia) }
                    .width(72.dp),
                color = when {
                    esSeleccionado -> AguaPrimary
                    esHoy -> AguaPrimaryLight.copy(alpha = 0.15f)
                    else -> SuperficieBlanca
                },
                shape = RoundedCornerShape(12.dp),
                border = BorderStroke(
                    width = if (esSeleccionado) 2.dp else 1.dp,
                    color = if (esSeleccionado) AguaPrimary else if (esHoy) AguaPrimaryLight else BordeSutil
                ),
                shadowElevation = if (esSeleccionado) 3.dp else 1.dp
            ) {
                Column(
                    modifier = Modifier.padding(vertical = 8.dp, horizontal = 4.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    if (esHoy) {
                        Surface(
                            color = if (esSeleccionado) Color.White else AguaPrimary,
                            shape = RoundedCornerShape(4.dp)
                        ) {
                            Text(
                                text = "HOY",
                                color = if (esSeleccionado) AguaPrimary else Color.White,
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Black,
                                fontSize = 9.sp,
                                maxLines = 1,
                                modifier = Modifier.padding(horizontal = 4.dp, vertical = 1.dp)
                            )
                        }
                        Spacer(modifier = Modifier.height(2.dp))
                    }

                    Text(
                        text = abrev,
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Black,
                        color = if (esSeleccionado) Color.White else TextoOscuro,
                        maxLines = 1
                    )

                    Surface(
                        color = if (esSeleccionado) Color.White.copy(alpha = 0.25f) else FondoGrisClaro,
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.padding(top = 4.dp)
                    ) {
                        Text(
                            text = "$count",
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            color = if (esSeleccionado) Color.White else TextoSecundario,
                            maxLines = 1,
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 1.dp)
                        )
                    }
                }
            }
        }
    }
}

/**
 * Barra de progreso visual segmentada:
 * Verde (Atendidos) + Ámbar (No estaban) + Gris (Pendientes).
 */
@Composable
private fun BarraProgresoSegmentada(
    atendidos: Int,
    noEstaban: Int,
    pendientes: Int,
    total: Int
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 4.dp),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = SuperficieBlanca),
        border = BorderStroke(1.dp, BordeSutil),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "ESTADO DE LA RUTA DE HOY",
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.Black,
                    color = TextoOscuro,
                    maxLines = 1
                )
                Text(
                    text = "$atendidos de $total atendidos",
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.Black,
                    color = VerdeEfectivo,
                    maxLines = 1
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(8.dp)
                    .clip(RoundedCornerShape(4.dp))
                    .background(Color(0xFFE2E8F0))
            ) {
                val pesoAtendidos = if (total > 0) atendidos.toFloat() / total else 0f
                val pesoNoEstaban = if (total > 0) noEstaban.toFloat() / total else 0f
                val pesoPendientes = if (total > 0) pendientes.toFloat() / total else 0f

                if (pesoAtendidos > 0f) {
                    Box(
                        modifier = Modifier
                            .fillMaxHeight()
                            .weight(pesoAtendidos)
                            .background(VerdeEfectivo)
                    )
                }
                if (pesoNoEstaban > 0f) {
                    Box(
                        modifier = Modifier
                            .fillMaxHeight()
                            .weight(pesoNoEstaban)
                            .background(AmbarAdvertencia)
                    )
                }
                if (pesoPendientes > 0f) {
                    Box(
                        modifier = Modifier
                            .fillMaxHeight()
                            .weight(pesoPendientes)
                            .background(Color(0xFFCBD5E1))
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(modifier = Modifier.size(8.dp).clip(CircleShape).background(VerdeEfectivo))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(text = "Atendidos: $atendidos", fontSize = 11.sp, color = TextoSecundario, fontWeight = FontWeight.Bold, maxLines = 1)
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(modifier = Modifier.size(8.dp).clip(CircleShape).background(AmbarAdvertencia))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(text = "Ausentes: $noEstaban", fontSize = 11.sp, color = TextoSecundario, fontWeight = FontWeight.Bold, maxLines = 1)
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(modifier = Modifier.size(8.dp).clip(CircleShape).background(Color(0xFF94A3B8)))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(text = "Pendientes: $pendientes", fontSize = 11.sp, color = TextoSecundario, fontWeight = FontWeight.Bold, maxLines = 1)
                }
            }
        }
    }
}

@Composable
private fun FilaFiltrosEstado(
    filtroSeleccionado: FiltroEstadoRuta,
    total: Int,
    atendidos: Int,
    noEstaban: Int,
    pendientes: Int,
    onSeleccionarFiltro: (FiltroEstadoRuta) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .horizontalScroll(rememberScrollState())
            .padding(horizontal = 16.dp, vertical = 4.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        FiltroEstadoRuta.entries.forEach { filtro ->
            val seleccionado = filtro == filtroSeleccionado
            val contador = when (filtro) {
                FiltroEstadoRuta.TODOS -> total
                FiltroEstadoRuta.PENDIENTES -> pendientes
                FiltroEstadoRuta.ATENDIDOS -> atendidos
                FiltroEstadoRuta.NO_ESTABAN -> noEstaban
            }

            FilterChip(
                selected = seleccionado,
                onClick = { onSeleccionarFiltro(filtro) },
                label = {
                    Text(
                        text = "${filtro.etiqueta} ($contador)",
                        fontWeight = if (seleccionado) FontWeight.Bold else FontWeight.Normal,
                        fontSize = 12.sp,
                        maxLines = 1,
                        softWrap = false
                    )
                },
                colors = FilterChipDefaults.filterChipColors(
                    selectedContainerColor = AguaPrimaryLight.copy(alpha = 0.2f),
                    selectedLabelColor = AguaPrimaryDark
                )
            )
        }
    }
}

@Composable
private fun ResumenBotellonesRequeridos(clientesRuta: List<ClienteRutaItem>) {
    val trans = clientesRuta.count { it.cliente.tipoBidonPreferido == TipoBidon.TRANSPARENTE }
    val azul = clientesRuta.count { it.cliente.tipoBidonPreferido == TipoBidon.AZUL }
    val marc = clientesRuta.count { it.cliente.tipoBidonPreferido == TipoBidon.MARCADO }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 4.dp),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = SuperficieBlanca),
        border = BorderStroke(1.dp, BordeSutil),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 8.dp),
            horizontalArrangement = Arrangement.SpaceAround,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Clientes por tipo:",
                style = MaterialTheme.typography.labelSmall,
                color = TextoSecundario,
                fontWeight = FontWeight.Bold,
                maxLines = 1
            )

            Row(verticalAlignment = Alignment.CenterVertically) {
                BidonIllustration(tipo = TipoBidon.TRANSPARENTE, size = 18.dp)
                Spacer(modifier = Modifier.width(4.dp))
                Text(text = "$trans PET", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.bodySmall, maxLines = 1)
            }

            Row(verticalAlignment = Alignment.CenterVertically) {
                BidonIllustration(tipo = TipoBidon.AZUL, size = 18.dp)
                Spacer(modifier = Modifier.width(4.dp))
                Text(text = "$azul Azul", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.bodySmall, maxLines = 1)
            }

            Row(verticalAlignment = Alignment.CenterVertically) {
                BidonIllustration(tipo = TipoBidon.MARCADO, size = 18.dp)
                Spacer(modifier = Modifier.width(4.dp))
                Text(text = "$marc Marc", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.bodySmall, maxLines = 1)
            }
        }
    }
}

/**
 * Tarjeta individual de cliente de ruta sin doble padding y con botones en 1 sola línea garantizada.
 */
@Composable
private fun TarjetaClienteRutaRedisenada(
    item: ClienteRutaItem,
    onEntregarAguaClick: () -> Unit,
    onNoEstabaClick: () -> Unit,
    onAbrirMapa: () -> Unit,
    onGuardarUbicacion: () -> Unit,
    onHistorialClick: () -> Unit,
    onVerFoto: (path: String, nombre: String) -> Unit,
    onContactarWhatsApp: () -> Unit
) {
    val cliente = item.cliente
    val esAtendido = item.estado == EstadoEntregaRuta.ATENDIDO
    val noEstaba = item.estado == EstadoEntregaRuta.NO_ESTABA
    var expandido by rememberSaveable(cliente.id) { mutableStateOf(false) }

    ClayCard(
        cornerRadius = 20.dp,
        containerColor = when {
            esAtendido -> Color(0xFFF0FDF4)
            noEstaba -> Color(0xFFFFFBEB)
            else -> SuperficieBlanca
        },
        elevation = if (esAtendido) 2.dp else 4.dp,
        contentPadding = PaddingValues(14.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column {
            // Cabecera Minimalista (Nombre, Sector corto, Pastillas de deuda/estado)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { expandido = !expandido },
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = cliente.nombre,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Black,
                        color = TextoOscuro,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    if (cliente.ubicacion.isNotBlank()) {
                        Text(
                            text = cliente.ubicacion,
                            style = MaterialTheme.typography.bodySmall,
                            color = TextoSecundario,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }

                Spacer(modifier = Modifier.width(6.dp))

                Row(
                    horizontalArrangement = Arrangement.spacedBy(4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    if (cliente.deudaMonetaria > 0.0) {
                        Surface(
                            color = RojoAlertaFondo,
                            shape = RoundedCornerShape(6.dp)
                        ) {
                            Text(
                                text = "$${String.format(Locale.US, "%.2f", cliente.deudaMonetaria)}",
                                color = RojoAlerta,
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Black,
                                maxLines = 1,
                                softWrap = false,
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                            )
                        }
                    }

                    if (cliente.bidonesPrestados > 0) {
                        Surface(
                            color = AmbarFondo,
                            shape = RoundedCornerShape(6.dp)
                        ) {
                            Text(
                                text = "🍾 ${cliente.bidonesPrestados}",
                                color = AmbarAdvertencia,
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Black,
                                maxLines = 1,
                                softWrap = false,
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                            )
                        }
                    }

                    BadgeEstadoEntrega(item = item, onVerFoto = onVerFoto)

                    Icon(
                        imageVector = if (expandido) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                        contentDescription = if (expandido) "Colapsar" else "Expandir",
                        tint = TextoSecundario,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }

            AnimatedVisibility(visible = expandido) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 10.dp)
                ) {
                    HorizontalDivider(color = BordeSutil, modifier = Modifier.padding(bottom = 8.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            FotoReferenciaThumbnail(
                                fotoPath = cliente.fotoReferenciaPath,
                                onClick = {
                                    if (!cliente.fotoReferenciaPath.isNullOrBlank()) {
                                        onVerFoto(cliente.fotoReferenciaPath!!, cliente.nombre)
                                    }
                                },
                                size = 42.dp
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Column {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    BidonIllustration(tipo = cliente.tipoBidonPreferido, size = 18.dp)
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text(
                                        text = "${cliente.tipoBidonPreferido.name} • $${String.format(Locale.US, "%.2f", cliente.precioHabitual)}",
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 12.sp,
                                        color = AguaPrimaryDark,
                                        maxLines = 1
                                    )
                                }
                                if (cliente.telefono.isNotBlank()) {
                                    Text(
                                        text = "📞 ${cliente.telefono}",
                                        fontSize = 11.sp,
                                        color = TextoSecundario,
                                        maxLines = 1
                                    )
                                }
                            }
                        }

                        OutlinedButton(
                            onClick = onGuardarUbicacion,
                            shape = RoundedCornerShape(8.dp),
                            contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp),
                            modifier = Modifier.height(32.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.LocationOn,
                                contentDescription = null,
                                tint = if (cliente.latitud != null || cliente.enlaceMapa.isNotBlank()) VerdeEfectivo else AguaPrimary,
                                modifier = Modifier.size(14.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = if (cliente.latitud != null || cliente.enlaceMapa.isNotBlank()) "GPS ✓" else "Guardar GPS",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                maxLines = 1,
                                softWrap = false,
                                color = if (cliente.latitud != null || cliente.enlaceMapa.isNotBlank()) VerdeEfectivo else AguaPrimary
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Botones de Acción de 1 sola línea garantizada
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                // Fila 1: [💧 Vender] y [🚪 No estaba]
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Button(
                        onClick = onEntregarAguaClick,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (esAtendido) Color(0xFF15803D) else Color(0xFF22C55E)
                        ),
                        shape = RoundedCornerShape(12.dp),
                        contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                        elevation = ButtonDefaults.buttonElevation(defaultElevation = 2.dp, pressedElevation = 0.dp),
                        modifier = Modifier.weight(1f).height(42.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.WaterDrop,
                            contentDescription = null,
                            tint = Color.White,
                            modifier = Modifier.size(17.dp)
                        )
                        Spacer(modifier = Modifier.width(5.dp))
                        Text(
                            text = if (esAtendido) "Re-vender" else "Vender",
                            fontWeight = FontWeight.Black,
                            fontSize = 13.sp,
                            color = Color.White,
                            maxLines = 1,
                            softWrap = false,
                            overflow = TextOverflow.Ellipsis
                        )
                    }

                    Button(
                        onClick = onNoEstabaClick,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (noEstaba) Color(0xFFD97706) else Color(0xFFF59E0B)
                        ),
                        shape = RoundedCornerShape(12.dp),
                        contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                        elevation = ButtonDefaults.buttonElevation(defaultElevation = 2.dp, pressedElevation = 0.dp),
                        modifier = Modifier.weight(1f).height(42.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.CameraAlt,
                            contentDescription = null,
                            tint = Color.White,
                            modifier = Modifier.size(17.dp)
                        )
                        Spacer(modifier = Modifier.width(5.dp))
                        Text(
                            text = if (noEstaba) "Ausente ✓" else "No estaba",
                            fontWeight = FontWeight.Black,
                            fontSize = 13.sp,
                            color = Color.White,
                            maxLines = 1,
                            softWrap = false,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }

                // Fila 2: [🧭 Mapa GPS], [📅 Historial] y [💬 WhatsApp]
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Button(
                        onClick = onAbrirMapa,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color(0xFF0284C7)
                        ),
                        shape = RoundedCornerShape(12.dp),
                        contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp),
                        elevation = ButtonDefaults.buttonElevation(defaultElevation = 2.dp, pressedElevation = 0.dp),
                        modifier = Modifier.weight(1f).height(40.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Navigation,
                            contentDescription = "Mapa",
                            tint = Color.White,
                            modifier = Modifier.size(15.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = if (cliente.latitud != null || cliente.enlaceMapa.isNotBlank()) "Mapa GPS" else "Fijar GPS",
                            fontWeight = FontWeight.Bold,
                            fontSize = 11.sp,
                            color = Color.White,
                            maxLines = 1,
                            softWrap = false,
                            overflow = TextOverflow.Ellipsis
                        )
                    }

                    Button(
                        onClick = onHistorialClick,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = AguaPrimaryDark
                        ),
                        shape = RoundedCornerShape(12.dp),
                        contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp),
                        elevation = ButtonDefaults.buttonElevation(defaultElevation = 2.dp, pressedElevation = 0.dp),
                        modifier = Modifier.weight(1f).height(40.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.History,
                            contentDescription = "Historial",
                            tint = Color.White,
                            modifier = Modifier.size(15.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "Historial",
                            fontWeight = FontWeight.Bold,
                            fontSize = 11.sp,
                            color = Color.White,
                            maxLines = 1,
                            softWrap = false,
                            overflow = TextOverflow.Ellipsis
                        )
                    }

                    Button(
                        onClick = onContactarWhatsApp,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color(0xFF25D366)
                        ),
                        shape = RoundedCornerShape(12.dp),
                        contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp),
                        elevation = ButtonDefaults.buttonElevation(defaultElevation = 2.dp, pressedElevation = 0.dp),
                        modifier = Modifier.weight(1f).height(40.dp)
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.Chat,
                            contentDescription = "WhatsApp",
                            tint = Color.White,
                            modifier = Modifier.size(15.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "WhatsApp",
                            fontWeight = FontWeight.Bold,
                            fontSize = 11.sp,
                            color = Color.White,
                            maxLines = 1,
                            softWrap = false,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun BadgeEstadoEntrega(
    item: ClienteRutaItem,
    onVerFoto: (path: String, nombre: String) -> Unit
) {
    val tipoVehiculoEntrega = TipoVehiculo.desdeString(item.vehiculoEntrega)

    when (item.estado) {
        EstadoEntregaRuta.ATENDIDO -> {
            Surface(
                color = VerdeEfectivoFondo,
                shape = RoundedCornerShape(8.dp),
                border = BorderStroke(1.dp, Color(0xFF86EFAC))
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 3.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    VehiculoThumbnail(tipo = tipoVehiculoEntrega, size = 16.dp)
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "✓ ${item.horaEntrega ?: ""} (${item.bidonesEntregados})",
                        color = VerdeEfectivo,
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Black,
                        maxLines = 1,
                        softWrap = false
                    )
                }
            }
        }
        EstadoEntregaRuta.NO_ESTABA -> {
            Surface(
                color = Color(0xFFFFFBEB),
                shape = RoundedCornerShape(8.dp),
                border = BorderStroke(1.dp, Color(0xFFFDE68A)),
                modifier = Modifier.clickable {
                    if (!item.fotoEvidenciaPath.isNullOrBlank()) {
                        onVerFoto(item.fotoEvidenciaPath, "Evidencia: ${item.cliente.nombre}")
                    }
                }
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 3.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.MeetingRoom,
                        contentDescription = null,
                        tint = AmbarAdvertencia,
                        modifier = Modifier.size(14.dp)
                    )
                    Spacer(modifier = Modifier.width(3.dp))
                    Text(
                        text = "Ausente ${item.horaVisitaFallida ?: ""}",
                        color = AmbarAdvertencia,
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Black,
                        maxLines = 1,
                        softWrap = false
                    )
                }
            }
        }
        EstadoEntregaRuta.PENDIENTE -> {
            Surface(
                color = Color(0xFFF1F5F9),
                shape = RoundedCornerShape(8.dp),
                border = BorderStroke(1.dp, BordeSutil)
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 3.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.HourglassTop,
                        contentDescription = null,
                        tint = Color(0xFF64748B),
                        modifier = Modifier.size(13.dp)
                    )
                    Spacer(modifier = Modifier.width(3.dp))
                    Text(
                        text = "Pendiente",
                        color = Color(0xFF64748B),
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        maxLines = 1,
                        softWrap = false
                    )
                }
            }
        }
        EstadoEntregaRuta.PROGRAMADO -> {
            Surface(
                color = Color(0xFFF8FAFC),
                shape = RoundedCornerShape(8.dp),
                border = BorderStroke(1.dp, BordeSutil)
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 3.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.CalendarMonth,
                        contentDescription = null,
                        tint = Color(0xFF94A3B8),
                        modifier = Modifier.size(13.dp)
                    )
                    Spacer(modifier = Modifier.width(3.dp))
                    Text(
                        text = "Programado",
                        color = Color(0xFF94A3B8),
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        maxLines = 1,
                        softWrap = false
                    )
                }
            }
        }
    }
}

@Composable
private fun EstadoVacioDiaRuta(dia: DiaSemana) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Icon(
                imageVector = Icons.Default.CalendarMonth,
                contentDescription = null,
                tint = AguaPrimaryLight.copy(alpha = 0.5f),
                modifier = Modifier.size(64.dp)
            )
            Spacer(modifier = Modifier.height(14.dp))
            Text(
                text = "No hay clientes programados para el $dia",
                style = MaterialTheme.typography.titleMedium,
                color = TextoOscuro,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center
            )
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = "Edita un cliente en la pestaña de Clientes para asignarle este día de ruta semanal.",
                style = MaterialTheme.typography.bodyMedium,
                color = TextoSecundario,
                textAlign = TextAlign.Center
            )
        }
    }
}

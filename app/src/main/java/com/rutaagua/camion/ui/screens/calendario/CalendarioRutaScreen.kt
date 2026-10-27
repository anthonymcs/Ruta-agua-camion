package com.rutaagua.camion.ui.screens.calendario

import android.content.Context
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
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material.icons.filled.HourglassTop
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.MeetingRoom
import androidx.compose.material.icons.filled.Navigation
import androidx.compose.material.icons.filled.Phone
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
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.rutaagua.camion.data.local.entity.ClienteEntity
import com.rutaagua.camion.data.local.model.DiaSemana
import com.rutaagua.camion.data.local.model.TipoBidon
import com.rutaagua.camion.data.local.model.TipoVehiculo
import com.rutaagua.camion.ui.components.BidonIllustration
import com.rutaagua.camion.ui.components.DialogoVistaPreviaFoto
import com.rutaagua.camion.ui.components.FotoReferenciaThumbnail
import com.rutaagua.camion.ui.components.GuardarUbicacionDialog
import com.rutaagua.camion.ui.components.VehiculoThumbnail
import com.rutaagua.camion.ui.screens.clientes.ClienteRutaItem
import com.rutaagua.camion.ui.screens.clientes.ClientesViewModel
import com.rutaagua.camion.ui.screens.clientes.EstadoEntregaRuta
import com.rutaagua.camion.ui.utils.GpsLocationHelper
import com.rutaagua.camion.ui.theme.AguaPrimary
import com.rutaagua.camion.ui.theme.AguaPrimaryDark
import com.rutaagua.camion.ui.theme.AguaPrimaryLight
import com.rutaagua.camion.ui.theme.AmbarAdvertencia
import com.rutaagua.camion.ui.theme.AmbarFondo
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
import com.rutaagua.camion.ui.utils.WhatsAppHelper
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

enum class FiltroEstadoRuta(val etiqueta: String) {
    TODOS("Todos"),
    PENDIENTES("Pendientes"),
    ATENDIDOS("Atendidos"),
    NO_ESTABAN("No estaban")
}

/**
 * Pantalla rediseñada del Calendario de Ruta Semanal (Lunes a Sábado).
 * Proporciona píldoras compactas para selección rápida de días, barra de progreso
 * segmentada (Atendidos vs No Estaban vs Pendientes), filtros en chips y
 * tarjetas con acciones inmediatas para "Entregar Agua" y "No Estaba (Foto)".
 */
@Composable
fun CalendarioRutaScreen(
    viewModel: ClientesViewModel,
    modifier: Modifier = Modifier,
    onRegistrarEntregaCliente: ((ClienteEntity) -> Unit)? = null
) {
    val context = LocalContext.current
    val diaSeleccionado by viewModel.diaSeleccionadoCalendario.collectAsState()
    val clientesRuta by viewModel.clientesRutaDiaState.collectAsState()
    val conteoPorDia by viewModel.conteoPorDiaSemanaState.collectAsState()
    val diaActualHoy = ClientesViewModel.obtenerDiaSemanaActual()

    var filtroEstadoSeleccionado by remember { mutableStateOf(FiltroEstadoRuta.TODOS) }
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

            // 1. Guardar visita fallida local y en remoto
            viewModel.registrarVisitaFallida(cliente, archivo.absolutePath)

            // 2. Abrir WhatsApp con foto de constancia y mensaje prellenado
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

    // Contadores para métricas y filtros
    val totalClientes = clientesRuta.size
    val atendidos = clientesRuta.count { it.estado == EstadoEntregaRuta.ATENDIDO }
    val noEstaban = clientesRuta.count { it.estado == EstadoEntregaRuta.NO_ESTABA }
    val pendientes = clientesRuta.count { it.estado == EstadoEntregaRuta.PENDIENTE }

    // Filtrar la lista de clientes según el chip seleccionado
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
        containerColor = FondoGrisClaro,
        topBar = {
            HeaderCalendario()
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
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
        }
    }

    // Diálogo para capturar o editar ubicación GPS del cliente
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

    // Diálogo para ampliar foto de fachada o evidencia
    if (fotoZoomPath != null) {
        DialogoVistaPreviaFoto(
            fotoPath = fotoZoomPath!!,
            nombreCliente = fotoZoomNombre,
            onDismiss = { fotoZoomPath = null }
        )
    }
}

@Composable
private fun HeaderCalendario() {
    Surface(
        color = SuperficieBlanca,
        shadowElevation = 2.dp
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = Icons.Default.CalendarMonth,
                contentDescription = null,
                tint = AguaPrimary,
                modifier = Modifier.size(28.dp)
            )
            Spacer(modifier = Modifier.width(10.dp))
            Column {
                Text(
                    text = "CALENDARIO DE RUTA",
                    style = MaterialTheme.typography.titleMedium,
                    color = TextoOscuro,
                    fontWeight = FontWeight.Black
                )
                Text(
                    text = "Planificación semanal (Lunes a Sábado)",
                    style = MaterialTheme.typography.labelSmall,
                    color = TextoSecundario
                )
            }
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
            .padding(horizontal = 16.dp, vertical = 10.dp),
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
                                modifier = Modifier.padding(horizontal = 4.dp, vertical = 1.dp)
                            )
                        }
                        Spacer(modifier = Modifier.height(2.dp))
                    }

                    Text(
                        text = abrev,
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Black,
                        color = if (esSeleccionado) Color.White else TextoOscuro
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
                    color = TextoOscuro
                )
                Text(
                    text = "$atendidos de $total atendidos",
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.Black,
                    color = VerdeEfectivo
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Barra segmentada
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

            // Leyenda de contadores
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(modifier = Modifier.size(8.dp).clip(CircleShape).background(VerdeEfectivo))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(text = "Atendidos: $atendidos", fontSize = 11.sp, color = TextoSecundario, fontWeight = FontWeight.Bold)
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(modifier = Modifier.size(8.dp).clip(CircleShape).background(AmbarAdvertencia))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(text = "No estaban: $noEstaban", fontSize = 11.sp, color = TextoSecundario, fontWeight = FontWeight.Bold)
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(modifier = Modifier.size(8.dp).clip(CircleShape).background(Color(0xFF94A3B8)))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(text = "Pendientes: $pendientes", fontSize = 11.sp, color = TextoSecundario, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

/**
 * Fila de FilterChips para filtrar rápidamente la lista.
 */
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
                        fontSize = 12.sp
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

/**
 * Resumen de botellones a preparar en el camión por tipo.
 */
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
                text = "Envases esperados:",
                style = MaterialTheme.typography.labelSmall,
                color = TextoSecundario,
                fontWeight = FontWeight.Bold
            )

            Row(verticalAlignment = Alignment.CenterVertically) {
                BidonIllustration(tipo = TipoBidon.TRANSPARENTE, size = 20.dp)
                Spacer(modifier = Modifier.width(4.dp))
                Text(text = "$trans PET", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.bodySmall)
            }

            Row(verticalAlignment = Alignment.CenterVertically) {
                BidonIllustration(tipo = TipoBidon.AZUL, size = 20.dp)
                Spacer(modifier = Modifier.width(4.dp))
                Text(text = "$azul Azul", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.bodySmall)
            }

            Row(verticalAlignment = Alignment.CenterVertically) {
                BidonIllustration(tipo = TipoBidon.MARCADO, size = 20.dp)
                Spacer(modifier = Modifier.width(4.dp))
                Text(text = "$marc Marc", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.bodySmall)
            }
        }
    }
}

/**
 * Tarjeta individual estructurada, moderna y limpia para cada cliente de la ruta.
 * Diseño minimalista con dieta de texto y detalles secundarios colapsables.
 */
@Composable
private fun TarjetaClienteRutaRedisenada(
    item: ClienteRutaItem,
    onEntregarAguaClick: () -> Unit,
    onNoEstabaClick: () -> Unit,
    onAbrirMapa: () -> Unit,
    onGuardarUbicacion: () -> Unit,
    onVerFoto: (path: String, nombre: String) -> Unit,
    onContactarWhatsApp: () -> Unit
) {
    val cliente = item.cliente
    val esAtendido = item.estado == EstadoEntregaRuta.ATENDIDO
    val noEstaba = item.estado == EstadoEntregaRuta.NO_ESTABA
    var expandido by remember { mutableStateOf(false) }

    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = when {
                esAtendido -> Color(0xFFF0FDF4)
                noEstaba -> Color(0xFFFFFBEB)
                else -> SuperficieBlanca
            }
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = if (esAtendido) 1.dp else 2.dp),
        border = BorderStroke(
            width = 1.dp,
            color = when {
                esAtendido -> Color(0xFF86EFAC)
                noEstaba -> Color(0xFFFDE68A)
                else -> BordeSutil
            }
        ),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
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
                        color = TextoOscuro
                    )
                    if (cliente.ubicacion.isNotBlank()) {
                        Text(
                            text = cliente.ubicacion,
                            style = MaterialTheme.typography.bodySmall,
                            color = TextoSecundario,
                            maxLines = 1
                        )
                    }
                }

                // Pastillas de estado y deuda en fila compacta
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
                                text = "💲 $${String.format(Locale.US, "%.2f", cliente.deudaMonetaria)}",
                                color = RojoAlerta,
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Black,
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

            // Sección Colapsable con detalles secundarios
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
                                        color = AguaPrimaryDark
                                    )
                                }
                                if (cliente.telefono.isNotBlank()) {
                                    Text(
                                        text = "📞 ${cliente.telefono}",
                                        fontSize = 11.sp,
                                        color = TextoSecundario
                                    )
                                }
                            }
                        }

                        // Botón de gestionar / guardar ubicación GPS
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
                                text = if (cliente.latitud != null || cliente.enlaceMapa.isNotBlank()) "GPS ✓" else "📍 Guardar GPS",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (cliente.latitud != null || cliente.enlaceMapa.isNotBlank()) VerdeEfectivo else AguaPrimary
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Botones de Acción Grandes (Estilo Duolingo: Táctiles, Redondeados, Íconos Grandes)
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // Fila 1: [💧 Vender] y [🚪 No estaba]
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    // 💧 Vender (Verde brillante)
                    Button(
                        onClick = onEntregarAguaClick,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (esAtendido) Color(0xFF15803D) else Color(0xFF22C55E)
                        ),
                        shape = RoundedCornerShape(14.dp),
                        elevation = ButtonDefaults.buttonElevation(defaultElevation = 2.dp, pressedElevation = 0.dp),
                        modifier = Modifier.weight(1f).height(46.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.WaterDrop,
                            contentDescription = null,
                            tint = Color.White,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = if (esAtendido) "Re-vender" else "Vender",
                            fontWeight = FontWeight.Black,
                            fontSize = 14.sp,
                            color = Color.White
                        )
                    }

                    // 🚪 No estaba (Naranja/Ámbar con cámara)
                    Button(
                        onClick = onNoEstabaClick,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (noEstaba) Color(0xFFD97706) else Color(0xFFF59E0B)
                        ),
                        shape = RoundedCornerShape(14.dp),
                        elevation = ButtonDefaults.buttonElevation(defaultElevation = 2.dp, pressedElevation = 0.dp),
                        modifier = Modifier.weight(1f).height(46.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.CameraAlt,
                            contentDescription = null,
                            tint = Color.White,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = if (noEstaba) "No Estaba ✓" else "No estaba",
                            fontWeight = FontWeight.Black,
                            fontSize = 14.sp,
                            color = Color.White
                        )
                    }
                }

                // Fila 2: [🧭 Mapa GPS] y [💬 WhatsApp]
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    // 🧭 Mapa GPS (Azul técnico)
                    Button(
                        onClick = onAbrirMapa,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color(0xFF0284C7)
                        ),
                        shape = RoundedCornerShape(14.dp),
                        elevation = ButtonDefaults.buttonElevation(defaultElevation = 2.dp, pressedElevation = 0.dp),
                        modifier = Modifier.weight(1f).height(44.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Navigation,
                            contentDescription = "Mapa",
                            tint = Color.White,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = if (cliente.latitud != null || cliente.enlaceMapa.isNotBlank()) "Mapa GPS" else "Fijar GPS",
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.sp,
                            color = Color.White
                        )
                    }

                    // 💬 WhatsApp (Verde WhatsApp oficial)
                    Button(
                        onClick = onContactarWhatsApp,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color(0xFF25D366)
                        ),
                        shape = RoundedCornerShape(14.dp),
                        elevation = ButtonDefaults.buttonElevation(defaultElevation = 2.dp, pressedElevation = 0.dp),
                        modifier = Modifier.weight(1f).height(44.dp)
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.Chat,
                            contentDescription = "WhatsApp",
                            tint = Color.White,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "WhatsApp",
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.sp,
                            color = Color.White
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
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    VehiculoThumbnail(tipo = tipoVehiculoEntrega, size = 20.dp)
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "Atendido ${item.horaEntrega ?: ""} (${item.bidonesEntregados} un.)",
                        color = VerdeEfectivo,
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Black
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
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.MeetingRoom,
                        contentDescription = null,
                        tint = AmbarAdvertencia,
                        modifier = Modifier.size(15.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "No estaba (${item.horaVisitaFallida ?: ""})",
                        color = AmbarAdvertencia,
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Black
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
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.HourglassTop,
                        contentDescription = null,
                        tint = Color(0xFF64748B),
                        modifier = Modifier.size(14.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "Pendiente",
                        color = Color(0xFF64748B),
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold
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
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.CalendarMonth,
                        contentDescription = null,
                        tint = Color(0xFF94A3B8),
                        modifier = Modifier.size(14.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "Programado",
                        color = Color(0xFF94A3B8),
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold
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

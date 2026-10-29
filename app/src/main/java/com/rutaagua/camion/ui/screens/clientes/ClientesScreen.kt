package com.rutaagua.camion.ui.screens.clientes

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Chat
import androidx.compose.material.icons.filled.AccountBalance
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.MeetingRoom
import androidx.compose.material.icons.filled.Navigation
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
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
import androidx.compose.runtime.mutableDoubleStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import com.rutaagua.camion.data.local.prefs.AppPreferences
import com.rutaagua.camion.ui.components.ClayButton
import com.rutaagua.camion.ui.components.ClayCard
import com.rutaagua.camion.ui.components.ClayInsetBox
import com.rutaagua.camion.ui.components.CuentasBancariasDialog
import com.rutaagua.camion.ui.components.marineOutlinedTextFieldColors
import com.rutaagua.camion.ui.components.obtenerClayThemePorVehiculo
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.foundation.BorderStroke
import com.rutaagua.camion.ui.security.SecurityPinDialog
import java.text.SimpleDateFormat
import java.util.Date
import com.rutaagua.camion.data.local.entity.ClienteEntity
import com.rutaagua.camion.data.local.entity.VisitaFallidaEntity
import com.rutaagua.camion.data.local.model.DiaSemana
import com.rutaagua.camion.data.local.model.TipoBidon
import com.rutaagua.camion.data.local.model.TipoCliente
import com.rutaagua.camion.data.local.model.TipoVehiculo
import com.rutaagua.camion.ui.components.BidonIllustration
import com.rutaagua.camion.ui.components.DialogoVistaPreviaFoto
import com.rutaagua.camion.ui.components.FotoReferenciaThumbnail
import com.rutaagua.camion.ui.components.GuardarUbicacionDialog
import com.rutaagua.camion.ui.components.SelectorFotoReferencia
import com.rutaagua.camion.ui.components.VehiculoThumbnail
import com.rutaagua.camion.ui.utils.EvidenciaVisitaHelper
import com.rutaagua.camion.ui.utils.GpsLocationHelper
import com.rutaagua.camion.ui.utils.WhatsAppHelper
import java.io.File
import com.rutaagua.camion.ui.theme.AguaPrimary
import com.rutaagua.camion.ui.theme.AguaPrimaryDark
import com.rutaagua.camion.ui.theme.AguaPrimaryLight
import com.rutaagua.camion.ui.theme.AmbarAdvertencia
import com.rutaagua.camion.ui.theme.AmbarFondo
import com.rutaagua.camion.ui.theme.BordeSutil
import com.rutaagua.camion.ui.theme.FondoGrisClaro
import com.rutaagua.camion.ui.theme.RojoAlerta
import com.rutaagua.camion.ui.theme.RojoAlertaFondo
import com.rutaagua.camion.ui.theme.TextoOscuro
import com.rutaagua.camion.ui.theme.TextoSecundario
import com.rutaagua.camion.ui.theme.VerdeEfectivo
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
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ClientesScreen(
    viewModel: ClientesViewModel,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val appPreferences = remember { AppPreferences.getInstance(context) }
    val vehiculoActivo by appPreferences.vehiculoActivo.collectAsState()
    val clayTheme = obtenerClayThemePorVehiculo(vehiculoActivo)

    val clientes by viewModel.clientesFiltradosState.collectAsState()
    val transaccionesHoyClientes by viewModel.transaccionesHoyClientesState.collectAsState()
    val visitasFallidasHoy by viewModel.visitasFallidasHoyState.collectAsState()
    val query by viewModel.busquedaQuery.collectAsState()
    val filtroActual by viewModel.filtroSeleccionado.collectAsState()
    val mostrarModal by viewModel.mostrarModalCliente.collectAsState()
    val clienteEnEdicion by viewModel.clienteEnEdicion.collectAsState()
    val clienteAEliminar by viewModel.clienteAEliminar.collectAsState()
    val mensajeSnackbar by viewModel.mensajeSnackbar.collectAsState()
    val clienteHistorial by viewModel.clienteHistorialSeleccionado.collectAsState()
    val historialTransacciones by viewModel.historialTransaccionesClienteState.collectAsState()
    val historialVisitas by viewModel.historialVisitasClienteState.collectAsState()

    var clienteParaEliminar by remember { mutableStateOf<ClienteEntity?>(null) }
    var mostrarPinParaEliminarCliente by remember { mutableStateOf(false) }

    var guardadoPendiente by remember { mutableStateOf<(() -> Unit)?>(null) }
    var mostrarPinParaModificarSaldo by remember { mutableStateOf(false) }

    var fotoZoomPath by remember { mutableStateOf<String?>(null) }
    var fotoZoomNombre by remember { mutableStateOf("") }
    var clienteParaUbicacion by remember { mutableStateOf<ClienteEntity?>(null) }

    // Cámara para constancia de "Cliente No Estaba en Casa"
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

            viewModel.registrarVisitaFallida(cliente, archivo.absolutePath)

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

    val snackbarHostState = remember { SnackbarHostState() }
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    LaunchedEffect(mensajeSnackbar) {
        mensajeSnackbar?.let {
            snackbarHostState.showSnackbar(it)
            viewModel.limpiarMensajeSnackbar()
        }
    }

    Scaffold(
        modifier = modifier,
        containerColor = clayTheme.canvasColor,
        snackbarHost = { SnackbarHost(snackbarHostState) },
        floatingActionButton = {
            ClayButton(
                onClick = { viewModel.abrirCrearCliente() },
                containerColor = clayTheme.primaryColor,
                contentColor = Color.White,
                cornerRadius = 20.dp,
                modifier = Modifier.padding(bottom = 8.dp)
            ) {
                Icon(imageVector = Icons.Default.Add, contentDescription = "Nuevo Cliente")
                Spacer(modifier = Modifier.width(6.dp))
                Text(text = "NUEVO CLIENTE", fontWeight = FontWeight.Black)
            }
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            // 1. Barra de Búsqueda Superior
            BarraBusquedaClientes(
                query = query,
                onQueryChange = { viewModel.onQueryChange(it) }
            )

            // 2. Fila de Filtros Rápidos (FilterChips)
            FilaFiltrosClientes(
                filtroSeleccionado = filtroActual,
                onFiltroSelect = { viewModel.onFiltroSelect(it) }
            )

            // Contador de resultados
            Text(
                text = "${clientes.size} clientes encontrados",
                style = MaterialTheme.typography.bodySmall,
                color = TextoSecundario,
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp)
            )

            // 3. Lista de Clientes
            if (clientes.isEmpty()) {
                EstadoVacioClientes(query = query)
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(clientes, key = { it.id }) { cliente ->
                        TarjetaClienteItem(
                            cliente = cliente,
                            transaccionHoy = transaccionesHoyClientes[cliente.id],
                            visitaFallidaHoy = visitasFallidasHoy[cliente.id],
                            onHistorialClick = { viewModel.abrirHistorialCliente(cliente) },
                            onEditarClick = { viewModel.abrirEditarCliente(cliente) },
                            onEliminarClick = {
                                clienteParaEliminar = cliente
                                mostrarPinParaEliminarCliente = true
                            },
                            onAbrirMapa = {
                                if (cliente.latitud != null || cliente.enlaceMapa.isNotBlank()) {
                                    GpsLocationHelper.abrirNavegacionDirecta(
                                        context = context,
                                        latitud = cliente.latitud,
                                        longitud = cliente.longitud,
                                        enlaceAlternativo = cliente.enlaceMapa
                                    )
                                } else {
                                    clienteParaUbicacion = cliente
                                }
                            },
                            onGuardarUbicacion = {
                                clienteParaUbicacion = cliente
                            },
                            onVerFoto = { path, nombre ->
                                fotoZoomPath = path
                                fotoZoomNombre = nombre
                            },
                            onNoEstabaClick = {
                                val archivo = EvidenciaVisitaHelper.crearArchivoFotoEvidencia(context, cliente.id)
                                archivoFotoTemp = archivo
                                clienteParaEvidencia = cliente
                                val uri = EvidenciaVisitaHelper.obtenerUriParaArchivo(context, archivo)
                                cameraLauncher.launch(uri)
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

    // Modal BottomSheet de Agregar/Editar
    if (mostrarModal) {
        ModalBottomSheet(
            onDismissRequest = { viewModel.cerrarModalCliente() },
            sheetState = sheetState
        ) {
            ModalFormularioCliente(
                clienteExistente = clienteEnEdicion,
                onGuardar = { id, nom, tel, ubi, tipo, dias, tBidon, precio, deuda, prestados, foto ->
                    val requierePinSaldo = clienteEnEdicion != null && (
                        deuda != clienteEnEdicion!!.deudaMonetaria ||
                        prestados != clienteEnEdicion!!.bidonesPrestados
                    )

                    if (requierePinSaldo) {
                        guardadoPendiente = {
                            viewModel.guardarCliente(
                                id = id,
                                nombre = nom,
                                telefono = tel,
                                ubicacion = ubi,
                                tipoCliente = tipo,
                                diasVisita = dias,
                                tipoBidonPreferido = tBidon,
                                precioHabitual = precio,
                                deudaInicial = deuda,
                                bidonesPrestadosInicial = prestados,
                                fotoReferenciaPath = foto
                            )
                        }
                        mostrarPinParaModificarSaldo = true
                    } else {
                        viewModel.guardarCliente(
                            id = id,
                            nombre = nom,
                            telefono = tel,
                            ubicacion = ubi,
                            tipoCliente = tipo,
                            diasVisita = dias,
                            tipoBidonPreferido = tBidon,
                            precioHabitual = precio,
                            deudaInicial = deuda,
                            bidonesPrestadosInicial = prestados,
                            fotoReferenciaPath = foto
                        )
                    }
                },
                onCancelar = { viewModel.cerrarModalCliente() }
            )
        }
    }

    // Diálogo para ampliar foto de fachada
    if (fotoZoomPath != null) {
        DialogoVistaPreviaFoto(
            fotoPath = fotoZoomPath!!,
            nombreCliente = fotoZoomNombre,
            onDismiss = { fotoZoomPath = null }
        )
    }

    // Diálogo de Confirmación de Eliminación
    clienteAEliminar?.let { cliente ->
        AlertDialog(
            onDismissRequest = { viewModel.cancelarEliminarCliente() },
            title = { Text("Eliminar Cliente", fontWeight = FontWeight.Bold) },
            text = {
                Text("¿Estás seguro de que deseas eliminar a \"${cliente.nombre}\"? Esta acción no se puede deshacer.")
            },
            confirmButton = {
                Button(
                    onClick = { viewModel.confirmarEliminarCliente() },
                    colors = ButtonDefaults.buttonColors(containerColor = RojoAlerta)
                ) {
                    Text("Eliminar")
                }
            },
            dismissButton = {
                OutlinedButton(onClick = { viewModel.cancelarEliminarCliente() }) {
                    Text("Cancelar")
                }
            }
        )
    }

    // Diálogo de Seguridad PIN para Eliminar Cliente
    if (mostrarPinParaEliminarCliente && clienteParaEliminar != null) {
        SecurityPinDialog(
            titulo = "Autorizar Eliminación de Cliente",
            subtitulo = "Ingresa el PIN para autorizar la eliminación de \"${clienteParaEliminar!!.nombre}\"",
            onDismissRequest = {
                mostrarPinParaEliminarCliente = false
                clienteParaEliminar = null
            },
            onPinCorrecto = {
                val c = clienteParaEliminar!!
                mostrarPinParaEliminarCliente = false
                clienteParaEliminar = null
                viewModel.solicitarEliminarCliente(c)
            }
        )
    }

    // Diálogo de Seguridad PIN para Modificar Saldo / Envases Prestados
    if (mostrarPinParaModificarSaldo && guardadoPendiente != null) {
        SecurityPinDialog(
            titulo = "Autorizar Ajuste de Saldo / Envases",
            subtitulo = "Ingresa el PIN para autorizar la modificación manual de deuda monetaria o envases",
            onDismissRequest = {
                mostrarPinParaModificarSaldo = false
                guardadoPendiente = null
            },
            onPinCorrecto = {
                val accion = guardadoPendiente!!
                mostrarPinParaModificarSaldo = false
                guardadoPendiente = null
                accion()
            }
        )
    }

    // Modal de Historial Completo del Cliente (por meses, fechas y entregas pasadas)
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
 * Campo de búsqueda superior de clientes con botón de limpiar.
 */
@Composable
private fun BarraBusquedaClientes(
    query: String,
    onQueryChange: (String) -> Unit
) {
    Surface(
        color = MaterialTheme.colorScheme.surface,
        shadowElevation = 2.dp
    ) {
        OutlinedTextField(
            value = query,
            onValueChange = onQueryChange,
            placeholder = { Text("Buscar cliente por nombre o dirección...") },
            leadingIcon = {
                Icon(
                    imageVector = Icons.Default.Search,
                    contentDescription = "Buscar",
                    tint = AguaPrimary
                )
            },
            trailingIcon = {
                if (query.isNotEmpty()) {
                    IconButton(onClick = { onQueryChange("") }) {
                        Icon(imageVector = Icons.Default.Clear, contentDescription = "Limpiar")
                    }
                }
            },
            shape = RoundedCornerShape(14.dp),
            singleLine = true,
            colors = marineOutlinedTextFieldColors(),
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 10.dp)
        )
    }
}

/**
 * Fila desplazable horizontal de FilterChips.
 */
@Composable
private fun FilaFiltrosClientes(
    filtroSeleccionado: FiltroCliente,
    onFiltroSelect: (FiltroCliente) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .horizontalScroll(rememberScrollState())
            .padding(horizontal = 16.dp, vertical = 6.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        FiltroCliente.entries.forEach { filtro ->
            val seleccionado = filtro == filtroSeleccionado
            FilterChip(
                selected = seleccionado,
                onClick = { onFiltroSelect(filtro) },
                label = {
                    Text(
                        text = filtro.etiqueta,
                        fontWeight = if (seleccionado) FontWeight.Bold else FontWeight.Normal
                    )
                },
                colors = FilterChipDefaults.filterChipColors(
                    selectedContainerColor = AguaPrimaryLight.copy(alpha = 0.25f),
                    selectedLabelColor = AguaPrimaryDark
                )
            )
        }
    }
}

/**
 * Tarjeta individual estructurada, moderna y limpia para cada cliente del directorio.
 * Diseño minimalista con dieta de texto y detalles secundarios colapsables.
 */
@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun TarjetaClienteItem(
    cliente: ClienteEntity,
    transaccionHoy: com.rutaagua.camion.data.local.entity.TransaccionVentaEntity? = null,
    visitaFallidaHoy: VisitaFallidaEntity? = null,
    onHistorialClick: () -> Unit,
    onEditarClick: () -> Unit,
    onEliminarClick: () -> Unit,
    onAbrirMapa: () -> Unit,
    onGuardarUbicacion: () -> Unit,
    onVerFoto: (path: String, nombre: String) -> Unit,
    onNoEstabaClick: () -> Unit
) {
    val context = LocalContext.current
    var expandido by rememberSaveable(cliente.id) { mutableStateOf(false) }
    var mostrarCuentasBancarias by rememberSaveable(cliente.id) { mutableStateOf(false) }

    ClayCard(
        cornerRadius = 20.dp,
        containerColor = when {
            transaccionHoy != null -> Color(0xFFF0FDF4)
            visitaFallidaHoy != null -> Color(0xFFFFFBEB)
            else -> Color.White
        },
        elevation = 4.dp,
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
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = cliente.nombre,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Black,
                            color = TextoOscuro,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                            modifier = Modifier.weight(1f, fill = false)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Surface(
                            color = if (cliente.tipoCliente == TipoCliente.RUTA) Color(0xFFE0F2FE) else Color(0xFFF0FDF4),
                            shape = RoundedCornerShape(4.dp)
                        ) {
                            Text(
                                text = if (cliente.tipoCliente == TipoCliente.RUTA) "RUTA" else "LLAMADA",
                                color = if (cliente.tipoCliente == TipoCliente.RUTA) AguaPrimaryDark else VerdeEfectivo,
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold,
                                fontSize = 10.sp,
                                maxLines = 1,
                                softWrap = false,
                                modifier = Modifier.padding(horizontal = 4.dp, vertical = 1.dp)
                            )
                        }
                    }

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

                // Pastillas de estado y deuda en fila compacta de 1 línea
                Row(
                    horizontalArrangement = Arrangement.spacedBy(4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    if (cliente.deudaMonetaria > 0.0) {
                        Surface(
                            color = RojoAlertaFondo,
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Text(
                                text = "$${String.format(Locale.US, "%.2f", cliente.deudaMonetaria)}",
                                color = RojoAlerta,
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Black,
                                maxLines = 1,
                                softWrap = false,
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 3.dp)
                            )
                        }
                    } else {
                        Surface(
                            color = VerdeEfectivoFondo,
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Text(
                                text = "$0.00",
                                color = VerdeEfectivo,
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold,
                                maxLines = 1,
                                softWrap = false,
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 3.dp)
                            )
                        }
                    }

                    if (cliente.bidonesPrestados > 0) {
                        Surface(
                            color = AmbarFondo,
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Text(
                                text = "🍾 ${cliente.bidonesPrestados}",
                                color = AmbarAdvertencia,
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Black,
                                maxLines = 1,
                                softWrap = false,
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 3.dp)
                            )
                        }
                    }

                    if (transaccionHoy != null) {
                        Surface(
                            color = Color(0xFFF0FDF4),
                            shape = RoundedCornerShape(8.dp),
                            border = BorderStroke(1.dp, Color(0xFF86EFAC))
                        ) {
                            Text(
                                text = "✓ Hoy",
                                color = Color(0xFF15803D),
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold,
                                maxLines = 1,
                                softWrap = false,
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 3.dp)
                            )
                        }
                    } else if (visitaFallidaHoy != null) {
                        Surface(
                            color = Color(0xFFFFFBEB),
                            shape = RoundedCornerShape(8.dp),
                            border = BorderStroke(1.dp, Color(0xFFFDE68A))
                        ) {
                            Text(
                                text = "🚪 Ausente",
                                color = AmbarAdvertencia,
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold,
                                maxLines = 1,
                                softWrap = false,
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 3.dp)
                            )
                        }
                    }

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
                                size = 44.dp
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

                        // Botones de acción secundarios (Editar, Eliminar, Ubicación)
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            OutlinedButton(
                                onClick = onGuardarUbicacion,
                                shape = RoundedCornerShape(8.dp),
                                contentPadding = PaddingValues(horizontal = 6.dp, vertical = 2.dp),
                                modifier = Modifier.height(30.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.LocationOn,
                                    contentDescription = null,
                                    tint = if (cliente.latitud != null || cliente.enlaceMapa.isNotBlank()) VerdeEfectivo else AguaPrimary,
                                    modifier = Modifier.size(13.dp)
                                )
                                Spacer(modifier = Modifier.width(3.dp))
                                Text(
                                    text = if (cliente.latitud != null || cliente.enlaceMapa.isNotBlank()) "GPS ✓" else "📍 GPS",
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = if (cliente.latitud != null || cliente.enlaceMapa.isNotBlank()) VerdeEfectivo else AguaPrimary
                                )
                            }
                            IconButton(onClick = onEditarClick, modifier = Modifier.size(30.dp)) {
                                Icon(imageVector = Icons.Default.Edit, contentDescription = "Editar", tint = AguaPrimary, modifier = Modifier.size(17.dp))
                            }
                            IconButton(onClick = onEliminarClick, modifier = Modifier.size(30.dp)) {
                                Icon(imageVector = Icons.Default.Delete, contentDescription = "Eliminar", tint = Color(0xFF94A3B8), modifier = Modifier.size(17.dp))
                            }
                        }
                    }

                    // Días de visita si aplica
                    if (cliente.tipoCliente == TipoCliente.RUTA && cliente.diasVisita.isNotBlank()) {
                        Spacer(modifier = Modifier.height(6.dp))
                        FlowRow(
                            horizontalArrangement = Arrangement.spacedBy(4.dp),
                            verticalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            cliente.diasVisita.split(",").forEach { dia ->
                                Surface(
                                    color = AguaPrimary.copy(alpha = 0.15f),
                                    shape = RoundedCornerShape(4.dp)
                                ) {
                                    Text(
                                        text = dia.take(3),
                                        color = AguaPrimaryDark,
                                        style = MaterialTheme.typography.labelSmall,
                                        fontWeight = FontWeight.Bold,
                                        modifier = Modifier.padding(horizontal = 4.dp, vertical = 1.dp)
                                    )
                                }
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Botones de Acción Compactos de 1 sola línea garantizada
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // Fila 1: [🚪 No estaba], [🧭 Mapa GPS] y [📅 Historial]
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    // 1. [No estaba]
                    Button(
                        onClick = onNoEstabaClick,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (visitaFallidaHoy != null) Color(0xFFD97706) else Color(0xFFF59E0B)
                        ),
                        shape = RoundedCornerShape(12.dp),
                        contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp),
                        elevation = ButtonDefaults.buttonElevation(defaultElevation = 2.dp, pressedElevation = 0.dp),
                        modifier = Modifier
                            .weight(1f)
                            .height(42.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.CameraAlt,
                            contentDescription = null,
                            tint = Color.White,
                            modifier = Modifier.size(15.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = if (visitaFallidaHoy != null) "Ausente ✓" else "No estaba",
                            fontWeight = FontWeight.Bold,
                            fontSize = 11.sp,
                            color = Color.White,
                            maxLines = 1,
                            softWrap = false,
                            overflow = TextOverflow.Ellipsis
                        )
                    }

                    // 2. [Mapa GPS]
                    Button(
                        onClick = onAbrirMapa,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color(0xFF0284C7)
                        ),
                        shape = RoundedCornerShape(12.dp),
                        contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp),
                        elevation = ButtonDefaults.buttonElevation(defaultElevation = 2.dp, pressedElevation = 0.dp),
                        modifier = Modifier
                            .weight(1f)
                            .height(42.dp)
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

                    // 3. [Historial de Entregas por Mes/Día]
                    Button(
                        onClick = onHistorialClick,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = AguaPrimaryDark
                        ),
                        shape = RoundedCornerShape(12.dp),
                        contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp),
                        elevation = ButtonDefaults.buttonElevation(defaultElevation = 2.dp, pressedElevation = 0.dp),
                        modifier = Modifier
                            .weight(1f)
                            .height(42.dp)
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
                }

                // Fila 2: [💬 Estado WhatsApp] y [🏦 Enviar Cuenta]
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Button(
                        onClick = {
                            val mensaje = WhatsAppHelper.generarMensajeEstadoCuenta(cliente)
                            WhatsAppHelper.abrirChatWhatsApp(context, cliente.telefono, mensaje)
                        },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color(0xFF25D366)
                        ),
                        shape = RoundedCornerShape(12.dp),
                        contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                        elevation = ButtonDefaults.buttonElevation(defaultElevation = 2.dp, pressedElevation = 0.dp),
                        modifier = Modifier
                            .weight(1f)
                            .height(42.dp)
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.Chat,
                            contentDescription = "WhatsApp",
                            tint = Color.White,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(5.dp))
                        Text(
                            text = "Estado WhatsApp",
                            fontWeight = FontWeight.Bold,
                            fontSize = 11.sp,
                            color = Color.White,
                            maxLines = 1,
                            softWrap = false,
                            overflow = TextOverflow.Ellipsis
                        )
                    }

                    Button(
                        onClick = { mostrarCuentasBancarias = true },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color(0xFF1E3A8A)
                        ),
                        shape = RoundedCornerShape(12.dp),
                        contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                        elevation = ButtonDefaults.buttonElevation(defaultElevation = 2.dp, pressedElevation = 0.dp),
                        modifier = Modifier
                            .weight(1f)
                            .height(42.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.AccountBalance,
                            contentDescription = "Enviar Cuenta",
                            tint = Color.White,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(5.dp))
                        Text(
                            text = "Enviar Cuenta",
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

    if (mostrarCuentasBancarias) {
        CuentasBancariasDialog(
            telefonoCliente = cliente.telefono,
            nombreCliente = cliente.nombre,
            montoACobrar = cliente.deudaMonetaria.takeIf { it > 0.0 },
            onDismiss = { mostrarCuentasBancarias = false }
        )
    }
}

/**
 * Formulario modal de Crear o Editar Cliente.
 */
@Composable
private fun ModalFormularioCliente(
    clienteExistente: ClienteEntity?,
    onGuardar: (
        id: Long,
        nombre: String,
        telefono: String,
        ubicacion: String,
        tipoCliente: TipoCliente,
        diasVisita: List<DiaSemana>,
        tipoBidon: TipoBidon,
        precioHabitual: Double,
        deudaInicial: Double,
        bidonesPrestados: Int,
        fotoReferenciaPath: String?
    ) -> Unit,
    onCancelar: () -> Unit
) {
    val editId = clienteExistente?.id ?: -1L
    var nombre by rememberSaveable(editId) { mutableStateOf(clienteExistente?.nombre ?: "") }
    var telefono by rememberSaveable(editId) { mutableStateOf(clienteExistente?.telefono ?: "") }
    var ubicacion by rememberSaveable(editId) { mutableStateOf(clienteExistente?.ubicacion ?: "") }
    var tipoCliente by rememberSaveable(editId) { mutableStateOf(clienteExistente?.tipoCliente ?: TipoCliente.RUTA) }
    var fotoReferenciaPath by rememberSaveable(editId) { mutableStateOf(clienteExistente?.fotoReferenciaPath) }

    // Días de visita
    val diasSeleccionados = remember(editId) {
        mutableStateListOf<DiaSemana>().apply {
            if (clienteExistente != null && clienteExistente.diasVisita.isNotBlank()) {
                val existentes = clienteExistente.diasVisita.split(",")
                existentes.forEach { dStr ->
                    runCatching { DiaSemana.valueOf(dStr) }.getOrNull()?.let { add(it) }
                }
            } else {
                add(DiaSemana.LUNES)
                add(DiaSemana.JUEVES)
            }
        }
    }

    var tipoBidonPreferido by rememberSaveable(editId) {
        mutableStateOf(clienteExistente?.tipoBidonPreferido ?: TipoBidon.AZUL)
    }

    var precioHabitualTexto by rememberSaveable(editId) {
        mutableStateOf(clienteExistente?.precioHabitual?.let { String.format(Locale.US, "%.2f", it) } ?: "1.00")
    }

    var deudaInicialTexto by rememberSaveable(editId) {
        mutableStateOf(clienteExistente?.deudaMonetaria?.let { String.format(Locale.US, "%.2f", it) } ?: "0.00")
    }

    var bidonesPrestadosTexto by rememberSaveable(editId) {
        mutableStateOf(clienteExistente?.bidonesPrestados?.toString() ?: "0")
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp, vertical = 10.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        item {
            Text(
                text = if (clienteExistente == null) "NUEVO CLIENTE" else "EDITAR CLIENTE",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Black,
                color = AguaPrimaryDark
            )
        }

        // Nombre y Teléfono
        item {
            OutlinedTextField(
                value = nombre,
                onValueChange = { nombre = it },
                label = { Text("Nombre del cliente o negocio *") },
                singleLine = true,
                colors = marineOutlinedTextFieldColors(),
                modifier = Modifier.fillMaxWidth()
            )
        }

        item {
            OutlinedTextField(
                value = ubicacion,
                onValueChange = { ubicacion = it },
                label = { Text("Ubicación / Dirección / Referencia") },
                colors = marineOutlinedTextFieldColors(),
                modifier = Modifier.fillMaxWidth()
            )
        }

        item {
            OutlinedTextField(
                value = telefono,
                onValueChange = { telefono = it },
                label = { Text("📱 Teléfono / WhatsApp (Opcional)") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                singleLine = true,
                colors = marineOutlinedTextFieldColors(),
                modifier = Modifier.fillMaxWidth()
            )
        }

        // Tipo de Cliente: RUTA vs LLAMADA
        item {
            Text(text = "TIPO DE CLIENTE", style = MaterialTheme.typography.labelLarge, color = TextoSecundario)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                FilterChip(
                    selected = tipoCliente == TipoCliente.RUTA,
                    onClick = { tipoCliente = TipoCliente.RUTA },
                    label = { Text("Cliente de Ruta (Días fijos)", fontWeight = FontWeight.Bold) },
                    modifier = Modifier.weight(1f)
                )
                FilterChip(
                    selected = tipoCliente == TipoCliente.LLAMADA,
                    onClick = { tipoCliente = TipoCliente.LLAMADA },
                    label = { Text("Cliente de Llamada", fontWeight = FontWeight.Bold) },
                    modifier = Modifier.weight(1f)
                )
            }
        }

        // Selector múltiple de Días (si es cliente de ruta)
        if (tipoCliente == TipoCliente.RUTA) {
            item {
                Text(
                    text = "DÍAS DE VISITA EN LA SEMANA",
                    style = MaterialTheme.typography.labelLarge,
                    color = TextoSecundario
                )
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    DiaSemana.entries.forEach { dia ->
                        val sel = diasSeleccionados.contains(dia)
                        val abrev = when (dia) {
                            DiaSemana.LUNES -> "LUN"
                            DiaSemana.MARTES -> "MAR"
                            DiaSemana.MIERCOLES -> "MIE"
                            DiaSemana.JUEVES -> "JUE"
                            DiaSemana.VIERNES -> "VIE"
                            DiaSemana.SABADO -> "SAB"
                        }
                        Surface(
                            modifier = Modifier
                                .weight(1f)
                                .clickable {
                                    if (sel) diasSeleccionados.remove(dia) else diasSeleccionados.add(dia)
                                },
                            color = if (sel) AguaPrimary else Color(0xFFF1F5F9),
                            shape = RoundedCornerShape(8.dp),
                            border = if (!sel) androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFCBD5E1)) else null
                        ) {
                            Text(
                                text = abrev,
                                color = if (sel) Color.White else TextoOscuro,
                                style = MaterialTheme.typography.labelMedium,
                                fontWeight = FontWeight.Bold,
                                textAlign = TextAlign.Center,
                                modifier = Modifier.padding(vertical = 10.dp)
                            )
                        }
                    }
                }
            }
        }

        // Selector Visual de Tipo de Bidón Preferido
        item {
            Text(
                text = "TIPO DE BOTELLÓN QUE PREFIERE",
                style = MaterialTheme.typography.labelLarge,
                color = TextoSecundario
            )
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                OpcionVisualBidon(
                    tipo = TipoBidon.TRANSPARENTE,
                    nombre = "Transparente",
                    seleccionado = tipoBidonPreferido == TipoBidon.TRANSPARENTE,
                    onSelect = { tipoBidonPreferido = TipoBidon.TRANSPARENTE },
                    modifier = Modifier.weight(1f)
                )
                OpcionVisualBidon(
                    tipo = TipoBidon.AZUL,
                    nombre = "Azul",
                    seleccionado = tipoBidonPreferido == TipoBidon.AZUL,
                    onSelect = { tipoBidonPreferido = TipoBidon.AZUL },
                    modifier = Modifier.weight(1f)
                )
                OpcionVisualBidon(
                    tipo = TipoBidon.MARCADO,
                    nombre = "Marcado",
                    seleccionado = tipoBidonPreferido == TipoBidon.MARCADO,
                    onSelect = { tipoBidonPreferido = TipoBidon.MARCADO },
                    modifier = Modifier.weight(1f)
                )
            }
        }

        // Selector Rápido de Precio Habitual ($0.75, $0.85, $1.00, $1.25)
        item {
            Text(
                text = "PRECIO HABITUAL ACORDADO ($)",
                style = MaterialTheme.typography.labelLarge,
                color = TextoSecundario
            )
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                listOf("0.75", "0.85", "1.00", "1.25").forEach { precio ->
                    val sel = precioHabitualTexto == precio
                    FilterChip(
                        selected = sel,
                        onClick = { precioHabitualTexto = precio },
                        label = { Text("$$precio", fontWeight = FontWeight.Bold) },
                        modifier = Modifier.weight(1f)
                    )
                }
            }
            Spacer(modifier = Modifier.height(6.dp))
            OutlinedTextField(
                value = precioHabitualTexto,
                onValueChange = { precioHabitualTexto = it },
                label = { Text("Precio por bidón ($)") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                singleLine = true,
                colors = marineOutlinedTextFieldColors(),
                modifier = Modifier.fillMaxWidth()
            )
        }

        // Deuda inicial y bidones prestados iniciales
        item {
            Text(
                text = "SALDOS PENDIENTES INICIALES (OPCIONAL)",
                style = MaterialTheme.typography.labelLarge,
                color = TextoSecundario
            )
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                OutlinedTextField(
                    value = deudaInicialTexto,
                    onValueChange = { deudaInicialTexto = it },
                    label = { Text("Deuda dinero ($)") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    singleLine = true,
                    colors = marineOutlinedTextFieldColors(),
                    modifier = Modifier.weight(1f)
                )
                OutlinedTextField(
                    value = bidonesPrestadosTexto,
                    onValueChange = { bidonesPrestadosTexto = it.filter { c -> c.isDigit() } },
                    label = { Text("Envases prestados") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    singleLine = true,
                    colors = marineOutlinedTextFieldColors(),
                    modifier = Modifier.weight(1f)
                )
            }
        }

        // Foto de fachada o casa de referencia
        item {
            SelectorFotoReferencia(
                fotoPathActual = fotoReferenciaPath,
                onFotoCambiada = { fotoReferenciaPath = it }
            )
        }

        // Botones de acción
        item {
            Spacer(modifier = Modifier.height(10.dp))
            Button(
                onClick = {
                    val precio = precioHabitualTexto.toDoubleOrNull() ?: 1.00
                    val deuda = deudaInicialTexto.toDoubleOrNull() ?: 0.0
                    val prestados = bidonesPrestadosTexto.toIntOrNull() ?: 0
                    onGuardar(
                        clienteExistente?.id ?: 0L,
                        nombre,
                        telefono,
                        ubicacion,
                        tipoCliente,
                        diasSeleccionados.toList(),
                        tipoBidonPreferido,
                        precio,
                        deuda,
                        prestados,
                        fotoReferenciaPath
                    )
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                colors = ButtonDefaults.buttonColors(containerColor = AguaPrimary),
                shape = RoundedCornerShape(12.dp)
            ) {
                Text(
                    text = if (clienteExistente == null) "GUARDAR CLIENTE" else "ACTUALIZAR CLIENTE",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
            }
            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}

@Composable
private fun OpcionVisualBidon(
    tipo: TipoBidon,
    nombre: String,
    seleccionado: Boolean,
    onSelect: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .clickable { onSelect() }
            .border(
                width = if (seleccionado) 2.dp else 1.dp,
                color = if (seleccionado) AguaPrimary else Color(0xFFE2E8F0),
                shape = RoundedCornerShape(12.dp)
            ),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (seleccionado) AguaPrimaryLight.copy(alpha = 0.15f) else MaterialTheme.colorScheme.surface
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 10.dp, horizontal = 4.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            BidonIllustration(tipo = tipo, size = 46.dp)
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = nombre,
                style = MaterialTheme.typography.labelSmall,
                fontWeight = if (seleccionado) FontWeight.Black else FontWeight.Normal,
                textAlign = TextAlign.Center
            )
        }
    }
}

@Composable
private fun EstadoVacioClientes(query: String) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(32.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Icon(
                imageVector = Icons.Default.Person,
                contentDescription = null,
                tint = Color(0xFF94A3B8),
                modifier = Modifier.size(54.dp)
            )
            Spacer(modifier = Modifier.height(12.dp))
            Text(
                text = if (query.isBlank()) "No hay clientes en esta categoría" else "No se encontraron clientes para \"$query\"",
                style = MaterialTheme.typography.titleMedium,
                color = TextoSecundario,
                textAlign = TextAlign.Center
            )
        }
    }
}

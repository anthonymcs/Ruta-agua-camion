package com.rutaagua.camion.ui.screens.clientes

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
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.LocationOn
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
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.rutaagua.camion.data.local.entity.ClienteEntity
import com.rutaagua.camion.data.local.model.DiaSemana
import com.rutaagua.camion.data.local.model.TipoBidon
import com.rutaagua.camion.data.local.model.TipoCliente
import com.rutaagua.camion.ui.components.BidonIllustration
import com.rutaagua.camion.ui.components.DialogoVistaPreviaFoto
import com.rutaagua.camion.ui.components.FotoReferenciaThumbnail
import com.rutaagua.camion.ui.components.SelectorFotoReferencia
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
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ClientesScreen(
    viewModel: ClientesViewModel,
    modifier: Modifier = Modifier
) {
    val clientes by viewModel.clientesFiltradosState.collectAsState()
    val query by viewModel.busquedaQuery.collectAsState()
    val filtroActual by viewModel.filtroSeleccionado.collectAsState()
    val mostrarModal by viewModel.mostrarModalCliente.collectAsState()
    val clienteEnEdicion by viewModel.clienteEnEdicion.collectAsState()
    val clienteAEliminar by viewModel.clienteAEliminar.collectAsState()
    val mensajeSnackbar by viewModel.mensajeSnackbar.collectAsState()

    var fotoZoomPath by remember { mutableStateOf<String?>(null) }
    var fotoZoomNombre by remember { mutableStateOf("") }

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
        snackbarHost = { SnackbarHost(snackbarHostState) },
        floatingActionButton = {
            FloatingActionButton(
                onClick = { viewModel.abrirCrearCliente() },
                containerColor = AguaPrimary,
                contentColor = Color.White,
                shape = CircleShape,
                modifier = Modifier.padding(bottom = 16.dp)
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(imageVector = Icons.Default.Add, contentDescription = "Nuevo Cliente")
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(text = "NUEVO CLIENTE", fontWeight = FontWeight.Bold)
                }
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
                            onEditarClick = { viewModel.abrirEditarCliente(cliente) },
                            onEliminarClick = { viewModel.solicitarEliminarCliente(cliente) },
                            onVerFoto = { path, nombre ->
                                fotoZoomPath = path
                                fotoZoomNombre = nombre
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

    // Modal BottomSheet de Agregar/Editar
    if (mostrarModal) {
        ModalBottomSheet(
            onDismissRequest = { viewModel.cerrarModalCliente() },
            sheetState = sheetState
        ) {
            ModalFormularioCliente(
                clienteExistente = clienteEnEdicion,
                onGuardar = { id, nom, tel, ubi, tipo, dias, tBidon, precio, deuda, prestados, foto ->
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
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = AguaPrimary,
                unfocusedBorderColor = Color(0xFFCBD5E1)
            ),
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
 * Tarjeta individual para mostrar datos del cliente, días, tipo de bidón, precio y deudas.
 */
@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun TarjetaClienteItem(
    cliente: ClienteEntity,
    onEditarClick: () -> Unit,
    onEliminarClick: () -> Unit,
    onVerFoto: (path: String, nombre: String) -> Unit
) {
    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            // Fila Superior: Foto Fachada, Nombre, Tipo (RUTA/LLAMADA) y Botones
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Row(
                    modifier = Modifier.weight(1f),
                    verticalAlignment = Alignment.Top
                ) {
                    FotoReferenciaThumbnail(
                        fotoPath = cliente.fotoReferenciaPath,
                        onClick = {
                            if (!cliente.fotoReferenciaPath.isNullOrBlank()) {
                                onVerFoto(cliente.fotoReferenciaPath!!, cliente.nombre)
                            }
                        },
                        size = 52.dp
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = cliente.nombre,
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Black,
                                color = TextoOscuro
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            // Badge Tipo Cliente
                            Surface(
                                color = if (cliente.tipoCliente == TipoCliente.RUTA) Color(0xFFE0F2FE) else Color(0xFFF0FDF4),
                                shape = RoundedCornerShape(6.dp)
                            ) {
                                Text(
                                    text = cliente.tipoCliente.name,
                                    color = if (cliente.tipoCliente == TipoCliente.RUTA) AguaPrimaryDark else VerdeEfectivo,
                                    style = MaterialTheme.typography.labelSmall,
                                    fontWeight = FontWeight.Bold,
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                )
                            }
                        }

                        // Ubicación / Referencia
                        if (cliente.ubicacion.isNotBlank()) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.padding(top = 4.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.LocationOn,
                                    contentDescription = null,
                                    tint = TextoSecundario,
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = cliente.ubicacion,
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = TextoSecundario
                                )
                            }
                        }

                        // Teléfono
                        if (cliente.telefono.isNotBlank()) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.padding(top = 2.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Phone,
                                    contentDescription = null,
                                    tint = TextoSecundario,
                                    modifier = Modifier.size(15.dp)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = cliente.telefono,
                                    style = MaterialTheme.typography.bodySmall,
                                    color = TextoSecundario
                                )
                            }
                        }
                    }
                }

                // Botones de acción (Editar y Eliminar)
                Row {
                    IconButton(onClick = onEditarClick, modifier = Modifier.size(34.dp)) {
                        Icon(
                            imageVector = Icons.Default.Edit,
                            contentDescription = "Editar",
                            tint = AguaPrimary,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                    IconButton(onClick = onEliminarClick, modifier = Modifier.size(34.dp)) {
                        Icon(
                            imageVector = Icons.Default.Delete,
                            contentDescription = "Eliminar",
                            tint = Color(0xFF94A3B8),
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Fila Central: Bidón preferido + Precio habitual + Días de Ruta
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color(0xFFF8FAFC), RoundedCornerShape(10.dp))
                    .padding(horizontal = 12.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                // Minicomponente visual de bidón
                Row(verticalAlignment = Alignment.CenterVertically) {
                    BidonIllustration(tipo = cliente.tipoBidonPreferido, size = 36.dp)
                    Spacer(modifier = Modifier.width(8.dp))
                    Column {
                        Text(
                            text = when (cliente.tipoBidonPreferido) {
                                TipoBidon.TRANSPARENTE -> "Transparente (PET)"
                                TipoBidon.AZUL -> "Azul Estándar"
                                TipoBidon.MARCADO -> "Marcado"
                            },
                            style = MaterialTheme.typography.bodySmall,
                            fontWeight = FontWeight.Bold,
                            color = TextoOscuro
                        )
                        Text(
                            text = "Precio: $${String.format(Locale.US, "%.2f", cliente.precioHabitual)}",
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.Black,
                            color = AguaPrimary
                        )
                    }
                }

                // Días de visita si es cliente de ruta
                if (cliente.tipoCliente == TipoCliente.RUTA && cliente.diasVisita.isNotBlank()) {
                    FlowRow(
                        horizontalArrangement = Arrangement.spacedBy(4.dp),
                        verticalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        val dias = cliente.diasVisita.split(",")
                        dias.forEach { dia ->
                            val abreviado = when (dia) {
                                "LUNES" -> "LUN"
                                "MARTES" -> "MAR"
                                "MIERCOLES" -> "MIE"
                                "JUEVES" -> "JUE"
                                "VIERNES" -> "VIE"
                                "SABADO" -> "SAB"
                                else -> dia.take(3)
                            }
                            Surface(
                                color = AguaPrimary,
                                shape = RoundedCornerShape(4.dp)
                            ) {
                                Text(
                                    text = abreviado,
                                    color = Color.White,
                                    style = MaterialTheme.typography.labelSmall,
                                    fontWeight = FontWeight.Bold,
                                    modifier = Modifier.padding(horizontal = 5.dp, vertical = 2.dp)
                                )
                            }
                        }
                    }
                }
            }

            // Fila Inferior: Badges de Deuda en Dinero ($) o Bidones Prestados (si aplican)
            val tieneDeuda = cliente.deudaMonetaria > 0.0
            val tienePrestados = cliente.bidonesPrestados > 0

            if (tieneDeuda || tienePrestados) {
                Spacer(modifier = Modifier.height(10.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    if (tieneDeuda) {
                        Surface(
                            color = RojoAlertaFondo,
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier.weight(1f)
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "Debe dinero: $${String.format(Locale.US, "%.2f", cliente.deudaMonetaria)}",
                                    color = RojoAlerta,
                                    style = MaterialTheme.typography.bodySmall,
                                    fontWeight = FontWeight.Black
                                )
                            }
                        }
                    }

                    if (tienePrestados) {
                        Surface(
                            color = AmbarFondo,
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier.weight(1f)
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "Debe envases: ${cliente.bidonesPrestados} un.",
                                    color = AmbarAdvertencia,
                                    style = MaterialTheme.typography.bodySmall,
                                    fontWeight = FontWeight.Black
                                )
                            }
                        }
                    }
                }
            }
        }
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
    var nombre by remember { mutableStateOf(clienteExistente?.nombre ?: "") }
    var telefono by remember { mutableStateOf(clienteExistente?.telefono ?: "") }
    var ubicacion by remember { mutableStateOf(clienteExistente?.ubicacion ?: "") }
    var tipoCliente by remember { mutableStateOf(clienteExistente?.tipoCliente ?: TipoCliente.RUTA) }
    var fotoReferenciaPath by remember { mutableStateOf(clienteExistente?.fotoReferenciaPath) }

    // Días de visita
    val diasSeleccionados = remember {
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

    var tipoBidonPreferido by remember {
        mutableStateOf(clienteExistente?.tipoBidonPreferido ?: TipoBidon.AZUL)
    }

    var precioHabitualTexto by remember {
        mutableStateOf(clienteExistente?.precioHabitual?.let { String.format(Locale.US, "%.2f", it) } ?: "1.00")
    }

    var deudaInicialTexto by remember {
        mutableStateOf(clienteExistente?.deudaMonetaria?.let { String.format(Locale.US, "%.2f", it) } ?: "0.00")
    }

    var bidonesPrestadosTexto by remember {
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
                text = if (clienteExistente == null) "REGISTRAR NUEVO CLIENTE" else "EDITAR CLIENTE",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Black,
                color = AguaPrimaryDark
            )
            Text(
                text = "Datos de contacto, tipo de envase y frecuencia de entrega",
                style = MaterialTheme.typography.bodyMedium,
                color = TextoSecundario
            )
        }

        // Nombre y Teléfono
        item {
            OutlinedTextField(
                value = nombre,
                onValueChange = { nombre = it },
                label = { Text("Nombre del cliente o negocio *") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )
        }

        item {
            OutlinedTextField(
                value = ubicacion,
                onValueChange = { ubicacion = it },
                label = { Text("Ubicación / Dirección / Referencia") },
                modifier = Modifier.fillMaxWidth()
            )
        }

        item {
            OutlinedTextField(
                value = telefono,
                onValueChange = { telefono = it },
                label = { Text("Teléfono / WhatsApp") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                singleLine = true,
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
                    modifier = Modifier.weight(1f)
                )
                OutlinedTextField(
                    value = bidonesPrestadosTexto,
                    onValueChange = { bidonesPrestadosTexto = it.filter { c -> c.isDigit() } },
                    label = { Text("Envases prestados") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    singleLine = true,
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

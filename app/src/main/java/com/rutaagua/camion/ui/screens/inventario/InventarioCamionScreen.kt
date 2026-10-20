package com.rutaagua.camion.ui.screens.inventario

import androidx.compose.animation.AnimatedVisibility
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.rutaagua.camion.data.local.entity.InventarioCamionEntity
import com.rutaagua.camion.data.local.model.RecargaCalculada
import com.rutaagua.camion.data.local.model.StockAlertas
import com.rutaagua.camion.data.local.model.TipoBidon
import com.rutaagua.camion.ui.components.BidonIllustration
import com.rutaagua.camion.ui.components.CamionRepartidorIllustration
import com.rutaagua.camion.ui.components.GalonIllustration
import com.rutaagua.camion.ui.components.MiniIllustration
import com.rutaagua.camion.ui.components.PacaIllustration
import com.rutaagua.camion.ui.theme.AguaPrimary
import com.rutaagua.camion.ui.theme.AguaPrimaryDark
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
    modifier: Modifier = Modifier
) {
    val inventario by viewModel.inventarioState.collectAsState()
    val calculoRecarga by viewModel.calculoRecargaState.collectAsState()
    val alertasStock by viewModel.alertasStockState.collectAsState()
    val mostrarDialogoCapacidad by viewModel.mostrarDialogoAjusteCapacidad.collectAsState()
    val mostrarModalRecarga by viewModel.mostrarModalRecargaPlanta.collectAsState()
    val mensajeSnackbar by viewModel.mensajeSnackbar.collectAsState()

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
                onAjustarCapacidadClick = { viewModel.abrirDialogoCapacidad() }
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
                    FilaTarjetasBidones(inv)
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
        }
    }

    // Modal para Registrar Recarga en Planta
    if (mostrarModalRecarga) {
        ModalBottomSheet(
            onDismissRequest = { viewModel.cerrarModalRecarga() },
            sheetState = sheetState
        ) {
            ModalRecargaPlantaContent(
                inventario = inventario,
                calculoRecarga = calculoRecarga,
                onConfirmarRecarga = { v, t, a, m, vac, gal, pac, min, obs ->
                    viewModel.registrarRecargaPlanta(v, t, a, m, vac, gal, pac, min, obs)
                },
                onCancelar = { viewModel.cerrarModalRecarga() }
            )
        }
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
    onAjustarCapacidadClick: () -> Unit
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
            Row(verticalAlignment = Alignment.CenterVertically) {
                CamionRepartidorIllustration(size = 46.dp)
                Spacer(modifier = Modifier.width(12.dp))
                Column {
                    Text(
                        text = "ESTADO DEL CAMIÓN",
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
private fun FilaTarjetasBidones(inventario: InventarioCamionEntity) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        TarjetaBidonIndividual(
            tipo = TipoBidon.TRANSPARENTE,
            nombre = "PET Cristal",
            cantidad = inventario.bidonesLlenosTransparentes,
            colorFondo = BidonTransparenteFondo,
            colorBorde = BidonTransparenteColor,
            modifier = Modifier.weight(1f)
        )

        TarjetaBidonIndividual(
            tipo = TipoBidon.AZUL,
            nombre = "Azul Fuerte",
            cantidad = inventario.bidonesLlenosAzules,
            colorFondo = BidonAzulFondo,
            colorBorde = BidonAzulColor,
            modifier = Modifier.weight(1f)
        )

        TarjetaBidonIndividual(
            tipo = TipoBidon.MARCADO,
            nombre = "Marcado",
            cantidad = inventario.bidonesLlenosMarcados,
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

            Text(
                text = "$cantidad",
                style = MaterialTheme.typography.displayLarge,
                fontWeight = FontWeight.Black,
                color = if (stockBajo) RojoAlerta else TextoOscuro
            )

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
    var numeroVuelta by remember { mutableIntStateOf(1) }

    val faltantes = calculoRecarga?.bidonesFaltantesParaTope ?: 0
    var cantTrans by remember { mutableIntStateOf(faltantes / 3) }
    var cantAzul by remember { mutableIntStateOf(faltantes - (faltantes / 3) - (faltantes / 6)) }
    var cantMarc by remember { mutableIntStateOf(faltantes / 6) }
    var cantVaciosBajar by remember { mutableIntStateOf(inventario?.bidonesVacios ?: 0) }

    var cantGalones by remember { mutableIntStateOf(calculoRecarga?.galonesFaltantes ?: 0) }
    var cantPacas by remember { mutableIntStateOf(calculoRecarga?.pacasFaltantes ?: 0) }
    var cantMinis by remember { mutableIntStateOf(calculoRecarga?.minisFaltantes ?: 0) }

    var observaciones by remember { mutableStateOf("") }

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
                        text = "RECARGA EN PLANTA",
                        style = MaterialTheme.typography.titleLarge,
                        color = AguaPrimaryDark
                    )
                    Text(
                        text = "Abastecimiento de camión",
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
                    text = "CONFIRMAR RECARGA (+$totalCargados BIDONES)",
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

        Row(verticalAlignment = Alignment.CenterVertically) {
            IconButton(
                onClick = { if (cantidad > 0) onCambio(cantidad - 1) },
                modifier = Modifier
                    .size(36.dp)
                    .background(Color(0xFFE2E8F0), CircleShape)
            ) {
                Icon(imageVector = Icons.Default.Remove, contentDescription = "Menos", tint = TextoOscuro)
            }

            Text(
                text = "$cantidad",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Black,
                textAlign = TextAlign.Center,
                modifier = Modifier.width(46.dp)
            )

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

        Row(verticalAlignment = Alignment.CenterVertically) {
            IconButton(
                onClick = { if (cantidad > 0) onCambio(cantidad - 1) },
                modifier = Modifier
                    .size(36.dp)
                    .background(Color(0xFFE2E8F0), CircleShape)
            ) {
                Icon(imageVector = Icons.Default.Remove, contentDescription = "Menos", tint = TextoOscuro)
            }

            Text(
                text = "$cantidad",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Black,
                textAlign = TextAlign.Center,
                modifier = Modifier.width(46.dp)
            )

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

package com.rutaagua.camion.ui.screens.registro

import android.content.Context
import android.content.Intent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.horizontalScroll
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
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Fastfood
import androidx.compose.material.icons.filled.LocalShipping
import androidx.compose.material.icons.automirrored.filled.ReceiptLong
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
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
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.material.icons.filled.Lock
import com.rutaagua.camion.data.local.prefs.RolUsuario
import com.rutaagua.camion.data.local.entity.CierreCajaEntity
import com.rutaagua.camion.data.local.entity.GastoExtraEntity
import com.rutaagua.camion.data.local.entity.InventarioCamionEntity
import com.rutaagua.camion.data.local.entity.TransaccionVentaEntity
import com.rutaagua.camion.data.local.model.FormaPago
import com.rutaagua.camion.data.local.model.ResumenCierreDia
import com.rutaagua.camion.data.local.model.TipoBidon
import com.rutaagua.camion.ui.components.BidonIllustration
import com.rutaagua.camion.ui.components.CamionRepartidorIllustration
import com.rutaagua.camion.ui.components.GalonIllustration
import com.rutaagua.camion.ui.components.MiniIllustration
import com.rutaagua.camion.ui.components.PacaIllustration
import com.rutaagua.camion.ui.security.SecurityPinDialog
import com.rutaagua.camion.ui.theme.AguaPrimary
import com.rutaagua.camion.ui.theme.AguaPrimaryDark
import com.rutaagua.camion.ui.theme.AguaPrimaryLight
import com.rutaagua.camion.ui.theme.AmbarAdvertencia
import com.rutaagua.camion.ui.theme.AmbarFondo
import com.rutaagua.camion.ui.theme.AzulCorporativo
import com.rutaagua.camion.ui.theme.BidonAzulColor
import com.rutaagua.camion.ui.theme.BidonMarcadoColor
import com.rutaagua.camion.ui.theme.BidonTransparenteColor
import com.rutaagua.camion.ui.theme.RojoAlerta
import com.rutaagua.camion.ui.theme.RojoAlertaFondo
import com.rutaagua.camion.ui.theme.TextoOscuro
import com.rutaagua.camion.ui.theme.TextoSecundario
import com.rutaagua.camion.ui.theme.VerdeEfectivo
import com.rutaagua.camion.ui.theme.VerdeEfectivoFondo
import com.rutaagua.camion.ui.utils.WhatsAppHelper
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RegistroDiaScreen(
    viewModel: RegistroDiaViewModel,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val transacciones by viewModel.transaccionesHoyState.collectAsState()
    val gastos by viewModel.gastosHoyState.collectAsState()
    val inventario by viewModel.inventarioVehiculoActivoState.collectAsState()
    val filtroVehiculo by viewModel.filtroVehiculoState.collectAsState()
    val resumen by viewModel.resumenCierreDiaState.collectAsState()
    val todosLosClientes by viewModel.todosLosClientesState.collectAsState()
    val rolActivo by viewModel.rolActivo.collectAsState()
    val cierreHoy by viewModel.cierreHoyState.collectAsState()
    val mostrarDialogoCierre by viewModel.mostrarDialogoCierre.collectAsState()

    val mostrarModalVenta by viewModel.mostrarModalNuevaVenta.collectAsState()
    val clientePreseleccionado by viewModel.clientePreseleccionado.collectAsState()
    val transaccionEnEdicion by viewModel.transaccionEnEdicion.collectAsState()
    val transaccionAEliminar by viewModel.transaccionAEliminar.collectAsState()
    val mostrarDialogoGasto by viewModel.mostrarDialogoGasto.collectAsState()
    val mensajeSnackbar by viewModel.mensajeSnackbar.collectAsState()

    var ventaParaEditar by remember { mutableStateOf<Pair<TransaccionVentaEntity, com.rutaagua.camion.data.local.entity.ClienteEntity?>?>(null) }
    var ventaParaEliminar by remember { mutableStateOf<TransaccionVentaEntity?>(null) }
    var mostrarPinParaEditar by remember { mutableStateOf(false) }
    var mostrarPinParaEliminar by remember { mutableStateOf(false) }

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
        topBar = {
            HeaderRegistroDia(
                fechaDisplay = viewModel.fechaHoyDisplay,
                inventario = inventario,
                onCompartirWhatsApp = { compartirReporteWhatsApp(context, viewModel.generarTextoReporteWhatsApp()) }
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = { viewModel.abrirModalNuevaVenta() },
                containerColor = VerdeEfectivo,
                contentColor = Color.White,
                shape = CircleShape,
                modifier = Modifier.padding(bottom = 16.dp)
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 20.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(imageVector = Icons.Default.Add, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(text = "GUARDAR VENTA", fontWeight = FontWeight.Black, fontSize = 15.sp)
                }
            }
        }
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
            contentPadding = PaddingValues(bottom = 96.dp)
        ) {
            // 1. Tarjetas Horizontales de Cuadre Diario (Con Privacidad para Chofer)
            item {
                FilaTarjetasCuadre(
                    resumen = resumen,
                    esModoAdmin = (rolActivo == RolUsuario.ADMIN_ROLANDO)
                )
            }

            // 1.1 Si es el Perfil de Rolando (Administrador), mostrar Auditoría RP3 de Control Financiero
            if (rolActivo == RolUsuario.ADMIN_ROLANDO) {
                item {
                    AuditoriaCierreRP3Card(
                        cierre = cierreHoy,
                        resumen = resumen,
                        costoPlantaBidon = viewModel.costoPlantaBidon,
                        costoPlantaGalon = viewModel.costoPlantaGalon,
                        costoPlantaPaca = viewModel.costoPlantaPaca,
                        costoPlantaMini = viewModel.costoPlantaMini
                    )
                }
            }

            // 1.2 Botón Destacado: Realizar Cierre de Caja Ciego (Fin de Jornada)
            item {
                Button(
                    onClick = { viewModel.abrirDialogoCierre() },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (cierreHoy != null) Color(0xFF0F766E) else Color(0xFF0F172A)
                    ),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 4.dp)
                        .height(48.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Lock,
                        contentDescription = null,
                        tint = Color.White,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = if (cierreHoy != null) "🔒 Cierre Declarado ($${String.format(Locale.US, "%.2f", cierreHoy?.totalDeclarado)}) • Modificar" else "🔒 Realizar Cierre de Caja (Fin de Jornada)",
                        fontWeight = FontWeight.Bold,
                        color = Color.White,
                        fontSize = 14.sp
                    )
                }
            }

            // 2. Barra de Botones de Acción Rápida (Gasto Extra y Compartir)
            item {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 6.dp),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Button(
                        onClick = { viewModel.abrirDialogoGasto() },
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFE2E8F0)),
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier.weight(1f)
                    ) {
                        Icon(imageVector = Icons.Default.Fastfood, contentDescription = null, tint = TextoOscuro, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(text = "+ Gasto Extra", color = TextoOscuro, fontWeight = FontWeight.Bold)
                    }

                    Button(
                        onClick = { compartirReporteWhatsApp(context, viewModel.generarTextoReporteWhatsApp()) },
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF25D366)), // Color WhatsApp oficial
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier.weight(1f)
                    ) {
                        Icon(imageVector = Icons.Default.Share, contentDescription = null, tint = Color.White, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(text = "WhatsApp", color = Color.White, fontWeight = FontWeight.Bold)
                    }
                }
            }

            // 2.1 Selector de Vista por Vehículo (Visible solo para Administrador)
            if (rolActivo == RolUsuario.ADMIN_ROLANDO) {
                item {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 4.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        FiltroVehiculoRegistro.entries.forEach { filtro ->
                            val seleccionado = filtroVehiculo == filtro
                            FilterChip(
                                selected = seleccionado,
                                onClick = { viewModel.cambiarFiltroVehiculo(filtro) },
                                label = {
                                    Text(
                                        text = filtro.titulo,
                                        fontWeight = if (seleccionado) FontWeight.Bold else FontWeight.Normal,
                                        fontSize = 12.sp
                                    )
                                }
                            )
                        }
                    }
                }
            }

            // 3. Título del Registro de Ventas de Hoy
            item {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 8.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "ENTREGAS DE HOY (${transacciones.size})",
                        style = MaterialTheme.typography.labelLarge,
                        color = TextoSecundario
                    )
                    Text(
                        text = "Libreta digital física",
                        style = MaterialTheme.typography.bodySmall,
                        color = TextoSecundario
                    )
                }
            }

            // 4. Lista de Entregas Realizadas
            if (transacciones.isEmpty()) {
                item {
                    EstadoVacioRegistro()
                }
            } else {
                items(transacciones, key = { it.id }) { transaccion ->
                    val cliente = todosLosClientes.find { it.id == transaccion.clienteId }
                    TarjetaRegistroItem(
                        transaccion = transaccion,
                        nombreCliente = cliente?.nombre ?: transaccion.nombreClientePaso ?: "Cliente de Paso",
                        ubicacion = cliente?.ubicacion ?: "",
                        onEditar = {
                            ventaParaEditar = Pair(transaccion, cliente)
                            mostrarPinParaEditar = true
                        },
                        onEliminar = {
                            ventaParaEliminar = transaccion
                            mostrarPinParaEliminar = true
                        },
                        onEnviarRecibo = {
                            val mensajeRecibo = WhatsAppHelper.generarMensajeReciboEntrega(
                                transaccion = transaccion,
                                cliente = cliente,
                                fechaDisplay = viewModel.fechaHoyDisplay
                            )
                            WhatsAppHelper.abrirChatWhatsApp(context, cliente?.telefono, mensajeRecibo)
                        }
                    )
                }
            }

            // 5. Desglose de Gastos Extras si hay registrados hoy
            if (gastos.isNotEmpty()) {
                item {
                    Spacer(modifier = Modifier.height(14.dp))
                    Text(
                        text = "GASTOS EN RUTA (${gastos.size})",
                        style = MaterialTheme.typography.labelLarge,
                        color = TextoSecundario,
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp)
                    )
                }
                items(gastos, key = { it.id }) { gasto ->
                    TarjetaGastoItem(
                        gasto = gasto,
                        onEliminar = { viewModel.eliminarGasto(gasto) }
                    )
                }
            }
        }
    }

    // Modal BottomSheet de Guardar / Editar Venta
    if (mostrarModalVenta) {
        ModalBottomSheet(
            onDismissRequest = { viewModel.cerrarModalNuevaVenta() },
            sheetState = sheetState
        ) {
            NuevaVentaSheet(
                clientes = todosLosClientes,
                clientePreseleccionado = clientePreseleccionado,
                transaccionAEditar = transaccionEnEdicion,
                inventario = inventario,
                onConfirmarVenta = { cId, nomPaso, tipoB, cRec, pBidon, cPrest, cDev, cVendEnv, pEnv, cGal, cPac, cMin, fPago, mPag, nota, rTrans, rAzul, rMarc, mEf, mTr, mCr, mAb, mCanc ->
                    viewModel.registrarOEditarVenta(
                        clienteId = cId,
                        nombreClientePaso = nomPaso,
                        tipoBidon = tipoB,
                        cantidadRecarga = cRec,
                        precioUnitarioBidon = pBidon,
                        cantidadPrestados = cPrest,
                        cantidadDevueltos = cDev,
                        cantidadVendidosEnvase = cVendEnv,
                        precioUnitarioBidonVendido = pEnv,
                        cantidadGalones = cGal,
                        cantidadPacas = cPac,
                        cantidadMinis = cMin,
                        formaPago = fPago,
                        montoPagado = mPag,
                        nota = nota,
                        recargaTransparentes = rTrans,
                        recargaAzules = rAzul,
                        recargaMarcados = rMarc,
                        montoEfectivo = mEf,
                        montoTransferencia = mTr,
                        montoCredito = mCr,
                        montoAbono = mAb,
                        montoCanceladoDeuda = mCanc
                    )
                },
                onCancelar = { viewModel.cerrarModalNuevaVenta() }
            )
        }
    }

    // Diálogo de Confirmación para Eliminar Entrega
    if (transaccionAEliminar != null) {
        val cliente = todosLosClientes.find { it.id == transaccionAEliminar?.clienteId }
        val nombre = cliente?.nombre ?: transaccionAEliminar?.nombreClientePaso ?: "Cliente de Paso"
        AlertDialog(
            onDismissRequest = { viewModel.cancelarEliminarVenta() },
            title = { Text("¿Eliminar esta entrega?", fontWeight = FontWeight.Bold) },
            text = {
                Text(
                    text = "Se eliminará la entrega de \"$nombre\" ($${String.format(Locale.US, "%.2f", transaccionAEliminar?.totalImporte ?: 0.0)}).\n\n" +
                            "El stock de bidones volverá inmediatamente al camión y la deuda o envases del cliente serán restaurados a su estado previo.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = TextoOscuro
                )
            },
            confirmButton = {
                Button(
                    onClick = { viewModel.confirmarEliminarVenta() },
                    colors = ButtonDefaults.buttonColors(containerColor = RojoAlerta)
                ) {
                    Text("Eliminar Entrega", fontWeight = FontWeight.Bold, color = Color.White)
                }
            },
            dismissButton = {
                OutlinedButton(onClick = { viewModel.cancelarEliminarVenta() }) {
                    Text("Cancelar")
                }
            }
        )
    }

    // Diálogo de Cierre de Caja Ciego (Estilo RP3)
    if (mostrarDialogoCierre) {
        CierreCajaDialog(
            cierreExistente = cierreHoy,
            onConfirmarCierre = { ef, tr, obs ->
                viewModel.registrarCierreCaja(ef, tr, obs)
            },
            onDismissRequest = { viewModel.cerrarDialogoCierre() }
        )
    }

    // Diálogo de Agregar Gasto Extra
    if (mostrarDialogoGasto) {
        DialogoAgregarGasto(
            onConfirmar = { concepto, monto -> viewModel.registrarGastoExtra(concepto, monto) },
            onDismiss = { viewModel.cerrarDialogoGasto() }
        )
    }

    // Diálogo de Seguridad con PIN para Editar Venta
    if (mostrarPinParaEditar && ventaParaEditar != null) {
        SecurityPinDialog(
            titulo = "Autorizar Edición de Venta",
            subtitulo = "Ingresa el PIN para modificar los datos de la entrega",
            onDismissRequest = {
                mostrarPinParaEditar = false
                ventaParaEditar = null
            },
            onPinCorrecto = {
                val (t, c) = ventaParaEditar!!
                mostrarPinParaEditar = false
                ventaParaEditar = null
                viewModel.abrirEditarVenta(t, c)
            }
        )
    }

    // Diálogo de Seguridad con PIN para Eliminar Venta
    if (mostrarPinParaEliminar && ventaParaEliminar != null) {
        SecurityPinDialog(
            titulo = "Autorizar Eliminación de Venta",
            subtitulo = "Ingresa el PIN para autorizar la eliminación y reversión de inventario",
            onDismissRequest = {
                mostrarPinParaEliminar = false
                ventaParaEliminar = null
            },
            onPinCorrecto = {
                val t = ventaParaEliminar!!
                mostrarPinParaEliminar = false
                ventaParaEliminar = null
                viewModel.solicitarEliminarVenta(t)
            }
        )
    }
}

/**
 * Encabezado principal con datos del camión y mini-chips de stock en vivo.
 */
@Composable
private fun HeaderRegistroDia(
    fechaDisplay: String,
    inventario: InventarioCamionEntity?,
    onCompartirWhatsApp: () -> Unit
) {
    Surface(
        color = MaterialTheme.colorScheme.surface,
        shadowElevation = 2.dp
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 10.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    CamionRepartidorIllustration(size = 44.dp)
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        Text(
                            text = "RUTA AGUA CAMIÓN",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Black,
                            color = AguaPrimaryDark
                        )
                        Text(
                            text = "Fecha: $fechaDisplay",
                            style = MaterialTheme.typography.bodyMedium,
                            color = TextoSecundario
                        )
                    }
                }

                IconButton(onClick = onCompartirWhatsApp) {
                    Icon(
                        imageVector = Icons.Default.Share,
                        contentDescription = "Compartir Reporte",
                        tint = AguaPrimary
                    )
                }
            }

            // Mini-chips de existencias en el camión
            if (inventario != null) {
                Spacer(modifier = Modifier.height(8.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    MiniChipStock(
                        label = "Transp.",
                        cantidad = inventario.bidonesLlenosTransparentes,
                        color = BidonTransparenteColor,
                        modifier = Modifier.weight(1f)
                    )
                    MiniChipStock(
                        label = "Azules",
                        cantidad = inventario.bidonesLlenosAzules,
                        color = BidonAzulColor,
                        modifier = Modifier.weight(1f)
                    )
                    MiniChipStock(
                        label = "Marcados",
                        cantidad = inventario.bidonesLlenosMarcados,
                        color = BidonMarcadoColor,
                        modifier = Modifier.weight(1f)
                    )
                    MiniChipStock(
                        label = "Vacíos",
                        cantidad = inventario.bidonesVacios,
                        color = Color(0xFF64748B),
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        }
    }
}

@Composable
private fun MiniChipStock(
    label: String,
    cantidad: Int,
    color: Color,
    modifier: Modifier = Modifier
) {
    Surface(
        color = Color(0xFFF1F5F9),
        shape = RoundedCornerShape(8.dp),
        modifier = modifier
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 6.dp, vertical = 4.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            Box(
                modifier = Modifier
                    .size(8.dp)
                    .background(color, CircleShape)
            )
            Spacer(modifier = Modifier.width(4.dp))
            Text(
                text = "$label: $cantidad",
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                color = TextoOscuro
            )
        }
    }
}

/**
 * Fila desplazable de tarjetas del Cuadre de Caja Diario.
 * En Modo Operador (Teléfono de Ruta) se oculta estrictamente el efectivo neto esperado para el cierre ciego.
 */
@Composable
private fun FilaTarjetasCuadre(resumen: ResumenCierreDia, esModoAdmin: Boolean) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .horizontalScroll(rememberScrollState())
            .padding(horizontal = 16.dp, vertical = 10.dp),
        horizontalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        TarjetaMetricaCuadre(
            titulo = "Bidones Vendidos",
            valor = "${resumen.totalBidonesVendidos}",
            subtitulo = "${resumen.totalBidonesPrestados} prestados / ${resumen.totalBidonesDevueltos} devueltos",
            colorValor = AguaPrimary
        )

        // En Modo Operador de Ruta se OCULTA estrictamente el efectivo neto esperado (Cierre Ciego RP3)
        if (esModoAdmin) {
            TarjetaMetricaCuadre(
                titulo = "Efectivo Neto en Caja",
                valor = "$${String.format(Locale.US, "%.2f", resumen.efectivoNetoCaja)}",
                subtitulo = "Cobrado ventas - gastos",
                colorValor = VerdeEfectivo
            )
        }

        TarjetaMetricaCuadre(
            titulo = "Cobro Transferencias",
            valor = "$${String.format(Locale.US, "%.2f", resumen.totalIngresoTransferencia)}",
            subtitulo = "Banco / Móvil",
            colorValor = Color(0xFF0288D1)
        )

        TarjetaMetricaCuadre(
            titulo = "Créditos Otorgados",
            valor = "$${String.format(Locale.US, "%.2f", resumen.totalCreditosOtorgados)}",
            subtitulo = "Quedaron debiendo hoy",
            colorValor = RojoAlerta
        )

        TarjetaMetricaCuadre(
            titulo = "Abonos Cobrados",
            valor = "$${String.format(Locale.US, "%.2f", resumen.totalAbonosRecibidos + resumen.totalDeudasCanceladas)}",
            subtitulo = "Cobro de deudas previas",
            colorValor = Color(0xFF7B1FA2)
        )

        TarjetaMetricaCuadre(
            titulo = "Gastos en Ruta",
            valor = "$${String.format(Locale.US, "%.2f", resumen.totalGastosRuta)}",
            subtitulo = "Almuerzo, colas, etc.",
            colorValor = AmbarAdvertencia
        )
    }
}

@Composable
private fun TarjetaMetricaCuadre(
    titulo: String,
    valor: String,
    subtitulo: String,
    colorValor: Color
) {
    Card(
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        modifier = Modifier.width(170.dp)
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Text(
                text = titulo,
                style = MaterialTheme.typography.labelSmall,
                color = TextoSecundario
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = valor,
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Black,
                color = colorValor
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = subtitulo,
                style = MaterialTheme.typography.bodySmall,
                color = TextoSecundario,
                fontSize = 10.sp
            )
        }
    }
}

/**
 * Tarjeta individual para cada venta registrada en el día con opciones de editar y eliminar.
 */
@Composable
private fun TarjetaRegistroItem(
    transaccion: TransaccionVentaEntity,
    nombreCliente: String,
    ubicacion: String,
    onEditar: () -> Unit,
    onEliminar: () -> Unit,
    onEnviarRecibo: () -> Unit
) {
    val formatoHora = remember { SimpleDateFormat("HH:mm", Locale.getDefault()) }
    val horaTexto = formatoHora.format(Date(transaccion.fecha))

    Card(
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 4.dp)
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            // Fila 1: Hora, Nombre de Cliente y Botones
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    modifier = Modifier.weight(1f),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Surface(
                        color = Color(0xFFF1F5F9),
                        shape = RoundedCornerShape(6.dp)
                    ) {
                        Text(
                            text = horaTexto,
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            color = TextoSecundario,
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = nombreCliente,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Black,
                        color = TextoOscuro
                    )
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    val esPagoMixto = (transaccion.montoEfectivo > 0 && transaccion.montoCredito > 0) ||
                            (transaccion.montoEfectivo > 0 && transaccion.montoTransferencia > 0) ||
                            (transaccion.montoTransferencia > 0 && transaccion.montoCredito > 0) ||
                            (transaccion.montoAbono > 0 && transaccion.montoCredito > 0)

                    if (esPagoMixto) {
                        Surface(
                            color = Color(0xFFEFF6FF),
                            shape = RoundedCornerShape(6.dp),
                            border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFBFDBFE))
                        ) {
                            Text(
                                text = "Mixto",
                                color = Color(0xFF1D4ED8),
                                fontWeight = FontWeight.Black,
                                fontSize = 11.sp,
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                            )
                        }
                    } else {
                        BadgeFormaPago(formaPago = transaccion.formaPagoPrincipal)
                    }
                    Spacer(modifier = Modifier.width(4.dp))
                    IconButton(onClick = onEditar, modifier = Modifier.size(28.dp)) {
                        Icon(
                            imageVector = Icons.Default.Edit,
                            contentDescription = "Editar entrega",
                            tint = AguaPrimary,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                    IconButton(onClick = onEliminar, modifier = Modifier.size(28.dp)) {
                        Icon(
                            imageVector = Icons.Default.Delete,
                            contentDescription = "Eliminar entrega",
                            tint = RojoAlerta,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }
            }

            if (ubicacion.isNotBlank()) {
                Text(
                    text = ubicacion,
                    style = MaterialTheme.typography.bodySmall,
                    color = TextoSecundario,
                    modifier = Modifier.padding(top = 2.dp)
                )
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Fila 2: Artículos entregados con mini-ilustraciones y Total
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color(0xFFF8FAFC), RoundedCornerShape(8.dp))
                    .padding(horizontal = 10.dp, vertical = 6.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    // Bidones de 20L
                    val totalBidones = transaccion.cantidadBidonesRecarga + transaccion.cantidadBidonesVendidosEnvase
                    val tieneMulti = (transaccion.recargaTransparentes + transaccion.recargaAzules + transaccion.recargaMarcados) > 0

                    if (tieneMulti) {
                        if (transaccion.recargaTransparentes > 0) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                BidonIllustration(tipo = TipoBidon.TRANSPARENTE, size = 24.dp)
                                Spacer(modifier = Modifier.width(2.dp))
                                Text(text = "${transaccion.recargaTransparentes}", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.bodySmall)
                            }
                        }
                        if (transaccion.recargaAzules > 0) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                BidonIllustration(tipo = TipoBidon.AZUL, size = 24.dp)
                                Spacer(modifier = Modifier.width(2.dp))
                                Text(text = "${transaccion.recargaAzules}", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.bodySmall)
                            }
                        }
                        if (transaccion.recargaMarcados > 0) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                BidonIllustration(tipo = TipoBidon.MARCADO, size = 24.dp)
                                Spacer(modifier = Modifier.width(2.dp))
                                Text(text = "${transaccion.recargaMarcados}", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.bodySmall)
                            }
                        }
                    } else if (totalBidones > 0) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            BidonIllustration(tipo = transaccion.tipoBidon, size = 26.dp)
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = "$totalBidones un.",
                                fontWeight = FontWeight.Bold,
                                style = MaterialTheme.typography.bodySmall
                            )
                        }
                    }

                    // Galones
                    if (transaccion.cantidadGalones > 0) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            GalonIllustration(size = 24.dp)
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(text = "${transaccion.cantidadGalones} gal.", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.bodySmall)
                        }
                    }

                    // Pacas
                    if (transaccion.cantidadPacas > 0) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            PacaIllustration(size = 24.dp)
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(text = "${transaccion.cantidadPacas} pac.", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.bodySmall)
                        }
                    }

                    // Minis
                    if (transaccion.cantidadMinis > 0) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            MiniIllustration(size = 24.dp)
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(text = "${transaccion.cantidadMinis} min.", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.bodySmall)
                        }
                    }

                    // Envases Prestados o Devueltos
                    if (transaccion.cantidadBidonesPrestados > 0) {
                        Text(
                            text = "+${transaccion.cantidadBidonesPrestados} prestado",
                            color = AmbarAdvertencia,
                            fontWeight = FontWeight.Bold,
                            style = MaterialTheme.typography.labelSmall
                        )
                    }
                    if (transaccion.cantidadBidonesDevueltos > 0) {
                        Text(
                            text = "-${transaccion.cantidadBidonesDevueltos} devuelto",
                            color = VerdeEfectivo,
                            fontWeight = FontWeight.Bold,
                            style = MaterialTheme.typography.labelSmall
                        )
                    }
                }

                // Monto Total Cobrado
                Column(horizontalAlignment = Alignment.End) {
                    Text(
                        text = "$${String.format(Locale.US, "%.2f", transaccion.totalImporte)}",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Black,
                        color = AguaPrimary
                    )
                    val partesPago = mutableListOf<String>()
                    if (transaccion.montoEfectivo > 0) partesPago.add("Ef: $${String.format(Locale.US, "%.2f", transaccion.montoEfectivo)}")
                    if (transaccion.montoTransferencia > 0) partesPago.add("Tr: $${String.format(Locale.US, "%.2f", transaccion.montoTransferencia)}")
                    if (transaccion.montoCredito > 0) partesPago.add("Fiado: $${String.format(Locale.US, "%.2f", transaccion.montoCredito)}")
                    if (transaccion.montoAbono > 0) partesPago.add("Ab: $${String.format(Locale.US, "%.2f", transaccion.montoAbono)}")

                    if (partesPago.size > 1) {
                        Text(
                            text = partesPago.joinToString(" • "),
                            style = MaterialTheme.typography.labelSmall,
                            color = TextoSecundario,
                            fontSize = 10.sp
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Botón de WhatsApp para Enviar Recibo al Cliente
            Button(
                onClick = onEnviarRecibo,
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF25D366)),
                shape = RoundedCornerShape(10.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(38.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Share,
                    contentDescription = null,
                    tint = Color.White,
                    modifier = Modifier.size(16.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = "📲 Enviar recibo al cliente",
                    color = Color.White,
                    fontWeight = FontWeight.Bold,
                    fontSize = 12.sp
                )
            }
        }
    }
}

@Composable
private fun BadgeFormaPago(formaPago: FormaPago) {
    val (fondo, textoColor, label) = when (formaPago) {
        FormaPago.EFECTIVO -> Triple(VerdeEfectivoFondo, VerdeEfectivo, "Efectivo")
        FormaPago.TRANSFERENCIA -> Triple(Color(0xFFE0F2FE), AguaPrimaryDark, "Transferencia")
        FormaPago.CREDITO -> Triple(RojoAlertaFondo, RojoAlerta, "Crédito (Debe)")
        FormaPago.ABONO -> Triple(Color(0xFFF3E8FF), Color(0xFF7E22CE), "Abono")
        FormaPago.CANCELADO -> Triple(Color(0xFFDCFCE7), Color(0xFF15803D), "Cancelado")
    }

    Surface(
        color = fondo,
        shape = RoundedCornerShape(6.dp)
    ) {
        Text(
            text = label,
            color = textoColor,
            style = MaterialTheme.typography.labelSmall,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
        )
    }
}

@Composable
private fun TarjetaGastoItem(
    gasto: GastoExtraEntity,
    onEliminar: () -> Unit
) {
    Card(
        shape = RoundedCornerShape(10.dp),
        colors = CardDefaults.cardColors(containerColor = AmbarFondo),
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 3.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 8.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(imageVector = Icons.Default.Fastfood, contentDescription = null, tint = AmbarAdvertencia, modifier = Modifier.size(20.dp))
                Spacer(modifier = Modifier.width(8.dp))
                Text(text = gasto.concepto, fontWeight = FontWeight.Bold, color = TextoOscuro)
            }
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = "-$${String.format(Locale.US, "%.2f", gasto.monto)}",
                    fontWeight = FontWeight.Black,
                    color = RojoAlerta
                )
                IconButton(onClick = onEliminar, modifier = Modifier.size(30.dp)) {
                    Icon(imageVector = Icons.Default.Delete, contentDescription = "Eliminar", tint = Color(0xFF94A3B8), modifier = Modifier.size(18.dp))
                }
            }
        }
    }
}

/**
 * Diálogo para agregar un gasto extra en ruta con chips rápidos sugeridos.
 */
@Composable
private fun DialogoAgregarGasto(
    onConfirmar: (concepto: String, monto: Double) -> Unit,
    onDismiss: () -> Unit
) {
    var concepto by remember { mutableStateOf("Almuerzo") }
    var montoTexto by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Registrar Gasto Extra en Ruta", fontWeight = FontWeight.Bold) },
        text = {
            Column {
                Text(
                    text = "Este gasto se descontará del total de efectivo recaudado hoy:",
                    style = MaterialTheme.typography.bodyMedium,
                    color = TextoSecundario
                )
                Spacer(modifier = Modifier.height(10.dp))

                // Chips de conceptos sugeridos
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    listOf("Almuerzo", "Colas / Bebidas", "Comida", "Taller / Llantas", "Peaje / Gasolina").forEach { chip ->
                        FilterChip(
                            selected = concepto == chip,
                            onClick = { concepto = chip },
                            label = { Text(chip, fontSize = 12.sp) }
                        )
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                OutlinedTextField(
                    value = concepto,
                    onValueChange = { concepto = it },
                    label = { Text("Concepto del gasto") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(8.dp))

                OutlinedTextField(
                    value = montoTexto,
                    onValueChange = { montoTexto = it },
                    label = { Text("Monto ($)") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val monto = montoTexto.toDoubleOrNull() ?: 0.0
                    onConfirmar(concepto, monto)
                }
            ) {
                Text("Guardar Gasto")
            }
        },
        dismissButton = {
            OutlinedButton(onClick = onDismiss) {
                Text("Cancelar")
            }
        }
    )
}

@Composable
private fun EstadoVacioRegistro() {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(32.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Icon(
                imageVector = Icons.AutoMirrored.Filled.ReceiptLong,
                contentDescription = null,
                tint = Color(0xFF94A3B8),
                modifier = Modifier.size(54.dp)
            )
            Spacer(modifier = Modifier.height(10.dp))
            Text(
                text = "No hay entregas registradas hoy",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = TextoOscuro
            )
            Text(
                text = "Toca \"+ GUARDAR VENTA\" para registrar la primera entrega",
                style = MaterialTheme.typography.bodyMedium,
                color = TextoSecundario,
                textAlign = TextAlign.Center
            )
        }
    }
}

/**
 * Función utilitaria para compartir el texto del reporte por WhatsApp o selector del sistema.
 */
private fun compartirReporteWhatsApp(context: Context, textoReporte: String) {
    val intent = Intent(Intent.ACTION_SEND).apply {
        type = "text/plain"
        putExtra(Intent.EXTRA_TEXT, textoReporte)
        setPackage("com.whatsapp")
    }

    try {
        context.startActivity(intent)
    } catch (e: Exception) {
        // Si no tiene WhatsApp instalado, abre el selector universal de Android
        val chooser = Intent.createChooser(
            Intent(Intent.ACTION_SEND).apply {
                type = "text/plain"
                putExtra(Intent.EXTRA_TEXT, textoReporte)
            },
            "Compartir reporte de cierre de caja"
        )
        context.startActivity(chooser)
    }
}

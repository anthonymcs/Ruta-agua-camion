package com.rutaagua.camion.ui.screens.registro

import android.content.Context
import android.content.Intent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ReceiptLong
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Fastfood
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
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
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.rutaagua.camion.data.local.entity.ClienteEntity
import com.rutaagua.camion.data.local.entity.GastoExtraEntity
import com.rutaagua.camion.data.local.entity.InventarioCamionEntity
import com.rutaagua.camion.data.local.entity.TransaccionVentaEntity
import com.rutaagua.camion.data.local.model.ResumenCierreDia
import com.rutaagua.camion.data.local.model.TipoBidon
import com.rutaagua.camion.data.local.model.TipoVehiculo
import com.rutaagua.camion.data.local.prefs.RolUsuario
import com.rutaagua.camion.ui.components.BankingQuickAction
import com.rutaagua.camion.ui.components.BankingSegmentedTabs
import com.rutaagua.camion.ui.components.ClayBadge
import com.rutaagua.camion.ui.components.ClayButton
import com.rutaagua.camion.ui.components.ClayCard
import com.rutaagua.camion.ui.components.ClayInsetBox
import com.rutaagua.camion.ui.components.VehicleClayTheme
import com.rutaagua.camion.ui.components.bounceClick
import com.rutaagua.camion.ui.components.marineOutlinedTextFieldColors
import com.rutaagua.camion.ui.components.obtenerClayThemePorVehiculo
import com.rutaagua.camion.ui.security.SecurityPinDialog
import com.rutaagua.camion.ui.theme.AmbarAdvertencia
import com.rutaagua.camion.ui.theme.AmbarFondo
import com.rutaagua.camion.ui.theme.BidonAzulColor
import com.rutaagua.camion.ui.theme.BidonAzulFondo
import com.rutaagua.camion.ui.theme.BidonMarcadoColor
import com.rutaagua.camion.ui.theme.BidonMarcadoFondo
import com.rutaagua.camion.ui.theme.BidonTransparenteColor
import com.rutaagua.camion.ui.theme.BidonTransparenteFondo
import com.rutaagua.camion.ui.theme.BordeSuave
import com.rutaagua.camion.ui.theme.PichinchaNavy
import com.rutaagua.camion.ui.theme.PichinchaYellow
import com.rutaagua.camion.ui.theme.RojoAlerta
import com.rutaagua.camion.ui.theme.RojoAlertaFondo
import com.rutaagua.camion.ui.theme.TextoOscuro
import com.rutaagua.camion.ui.theme.TextoSecundario
import com.rutaagua.camion.ui.theme.VerdeEfectivo
import com.rutaagua.camion.ui.theme.VerdeEfectivoFondo
import com.rutaagua.camion.ui.utils.RutaNotificationHelper
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
    val alertasCreditos by viewModel.alertasCreditosSinCobroState.collectAsState()
    val rolActivo by viewModel.rolActivo.collectAsState()
    val cierreHoy by viewModel.cierreHoyState.collectAsState()
    val mostrarDialogoCierre by viewModel.mostrarDialogoCierre.collectAsState()

    val mostrarModalVenta by viewModel.mostrarModalNuevaVenta.collectAsState()
    val clientePreseleccionado by viewModel.clientePreseleccionado.collectAsState()
    val transaccionEnEdicion by viewModel.transaccionEnEdicion.collectAsState()
    val transaccionAEliminar by viewModel.transaccionAEliminar.collectAsState()
    val mostrarDialogoGasto by viewModel.mostrarDialogoGasto.collectAsState()
    val mensajeSnackbar by viewModel.mensajeSnackbar.collectAsState()

    var ventaParaEditar by remember { mutableStateOf<Pair<TransaccionVentaEntity, ClienteEntity?>?>(null) }
    var ventaParaEliminar by remember { mutableStateOf<TransaccionVentaEntity?>(null) }
    var mostrarPinParaEditar by rememberSaveable { mutableStateOf(false) }
    var mostrarPinParaEliminar by rememberSaveable { mutableStateOf(false) }

    val snackbarHostState = remember { SnackbarHostState() }
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    val tipoVehiculoActual = remember(inventario?.vehiculo, filtroVehiculo) {
        if (inventario?.vehiculo == "CAMIONETA" || filtroVehiculo == FiltroVehiculoRegistro.CAMIONETA) {
            TipoVehiculo.CAMIONETA
        } else {
            TipoVehiculo.CAMION
        }
    }
    val clayTheme = obtenerClayThemePorVehiculo(tipoVehiculoActual)

    LaunchedEffect(mensajeSnackbar) {
        mensajeSnackbar?.let {
            snackbarHostState.showSnackbar(it)
            viewModel.limpiarMensajeSnackbar()
        }
    }

    // Disparar notificación automática cuando haya clientes con crédito sin cobrar en un plazo de 2 días o más
    LaunchedEffect(alertasCreditos) {
        if (alertasCreditos.isNotEmpty()) {
            RutaNotificationHelper.notificarCreditosVencidosSiCorresponde(
                context = context,
                nombresClientes = alertasCreditos.map { it.cliente.nombre },
                totalDeudaVencida = alertasCreditos.sumOf { it.cliente.deudaMonetaria },
                fechaHoyIso = viewModel.fechaHoyTexto
            )
        }
    }

    Scaffold(
        modifier = modifier,
        containerColor = clayTheme.canvasColor,
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 10.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // 1. Selector de Vehículo solo en modo Administrador
            if (rolActivo == RolUsuario.ADMIN_ROLANDO) {
                item {
                    BankingSegmentedTabs(
                        options = FiltroVehiculoRegistro.entries,
                        selectedOption = filtroVehiculo,
                        labelProvider = { it.titulo },
                        onOptionSelected = { viewModel.cambiarFiltroVehiculo(it) },
                        activeColor = clayTheme.primary
                    )
                }
            }

            // 2. Tarjeta Hero estilo Banca Móvil (Saldo en Caja + Stock interactivo)
            item {
                TarjetaHeroBanca(
                    resumen = resumen,
                    inventario = inventario,
                    clayTheme = clayTheme
                )
            }

            // 3. Acciones Rápidas estilo Banca Móvil Pichincha
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    BankingQuickAction(
                        icon = Icons.Default.Add,
                        label = "Vender",
                        onClick = { viewModel.abrirModalNuevaVenta() },
                        isPrimaryGold = true,
                        modifier = Modifier.weight(1f)
                    )
                    BankingQuickAction(
                        icon = Icons.Default.Fastfood,
                        label = "Gasto",
                        onClick = { viewModel.abrirDialogoGasto() },
                        iconTint = AmbarAdvertencia,
                        modifier = Modifier.weight(1f)
                    )
                    BankingQuickAction(
                        icon = Icons.Default.Share,
                        label = "WhatsApp",
                        onClick = {
                            compartirPorWhatsApp(context, viewModel.generarTextoReporteWhatsApp())
                        },
                        iconTint = VerdeEfectivo,
                        modifier = Modifier.weight(1f)
                    )
                    BankingQuickAction(
                        icon = Icons.Default.Lock,
                        label = if (cierreHoy != null) "Cerrado ✓" else "Arqueo",
                        onClick = { viewModel.abrirDialogoCierre() },
                        iconTint = clayTheme.primary,
                        modifier = Modifier.weight(1f)
                    )
                }
            }

            // 3.5 Alerta de Clientes con Crédito sin cobrar en plazo >= 2 días
            if (alertasCreditos.isNotEmpty()) {
                item {
                    TarjetaAlertaCreditosSinCobro(
                        alertas = alertasCreditos,
                        onCobrarCliente = { cliente ->
                            viewModel.abrirModalNuevaVenta(cliente)
                        },
                        onRecordarWhatsApp = { alerta ->
                            val msg = "Hola *${alerta.cliente.nombre}*, le saludamos de la Ruta de Agua. " +
                                    "Le recordamos amablemente su saldo pendiente de *$${String.format(Locale.US, "%.2f", alerta.cliente.deudaMonetaria)}* " +
                                    "(último movimiento: ${alerta.fechaReferenciaTexto}). ¡Muchas gracias!"
                            WhatsAppHelper.abrirChatWhatsApp(context, alerta.cliente.telefono, msg)
                        }
                    )
                }
            }

            // 4. Lista de Gastos (si existen hoy)
            if (gastos.isNotEmpty()) {
                item {
                    SeccionGastosBanca(
                        gastos = gastos,
                        onEliminarGasto = { viewModel.eliminarGasto(it) }
                    )
                }
            }

            // 5. Cabecera de Movimientos de Hoy
            item {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 4.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Movimientos de hoy",
                        color = PichinchaNavy,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Black
                    )
                    ClayBadge(
                        text = "${transacciones.size} entregas",
                        containerColor = clayTheme.softContainer,
                        contentColor = clayTheme.primaryDark
                    )
                }
            }

            if (transacciones.isEmpty()) {
                item {
                    ClayCard(
                        modifier = Modifier.fillMaxWidth(),
                        cornerRadius = 18.dp
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 22.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.ReceiptLong,
                                contentDescription = null,
                                tint = TextoSecundario.copy(alpha = 0.5f),
                                modifier = Modifier.size(36.dp)
                            )
                            Spacer(modifier = Modifier.height(6.dp))
                            Text(
                                text = "Sin ventas hoy",
                                color = PichinchaNavy,
                                fontWeight = FontWeight.Bold,
                                fontSize = 14.sp
                            )
                        }
                    }
                }
            } else {
                val mapaClientes = todosLosClientes.associateBy { it.id }

                items(transacciones, key = { it.id }) { transaccion ->
                    val cliente = transaccion.clienteId?.let { mapaClientes[it] }
                    val nombreCliente = cliente?.nombre
                        ?: transaccion.nombreClientePaso?.takeIf { it.isNotBlank() }
                        ?: "Venta #${transaccion.id}"

                    TarjetaTransaccionBanca(
                        transaccion = transaccion,
                        nombreCliente = nombreCliente,
                        clayTheme = clayTheme,
                        onEditarClick = {
                            ventaParaEditar = Pair(transaccion, cliente)
                            mostrarPinParaEditar = true
                        },
                        onEliminarClick = {
                            ventaParaEliminar = transaccion
                            mostrarPinParaEliminar = true
                        },
                        onEnviarReciboWhatsApp = {
                            val recibo = WhatsAppHelper.generarMensajeReciboEntrega(
                                transaccion = transaccion,
                                cliente = cliente,
                                fechaDisplay = viewModel.fechaHoyDisplay
                            )
                            WhatsAppHelper.abrirChatWhatsApp(
                                context = context,
                                telefonoRaw = cliente?.telefono,
                                mensaje = recibo
                            )
                        }
                    )
                }
            }

            item {
                Spacer(modifier = Modifier.height(24.dp))
            }
        }
    }

    // Modal de Nueva / Editar Venta
    if (mostrarModalVenta) {
        ModalBottomSheet(
            onDismissRequest = { viewModel.cerrarModalNuevaVenta() },
            sheetState = sheetState,
            containerColor = clayTheme.canvasColor
        ) {
            NuevaVentaSheet(
                clientes = todosLosClientes,
                clientePreseleccionado = clientePreseleccionado,
                transaccionAEditar = transaccionEnEdicion,
                inventario = inventario,
                onConfirmarVenta = { cId, nomPaso, tBidon, cRec, pBidon, cPrest, cDev, cVend, pVend, cGal, cPac, cMin, fPago, mPagado, nota, rTrans, rAzul, rMarc, mEfec, mTransf, mCred, mAbono, mCanc, pTrans, pAzul, pMarc, cGratis, totalPers ->
                    viewModel.registrarOEditarVenta(
                        clienteId = cId,
                        nombreClientePaso = nomPaso,
                        tipoBidon = tBidon,
                        cantidadRecarga = cRec,
                        precioUnitarioBidon = pBidon,
                        cantidadPrestados = cPrest,
                        cantidadDevueltos = cDev,
                        cantidadVendidosEnvase = cVend,
                        precioUnitarioBidonVendido = pVend,
                        cantidadGalones = cGal,
                        cantidadPacas = cPac,
                        cantidadMinis = cMin,
                        formaPago = fPago,
                        montoPagado = mPagado,
                        nota = nota,
                        recargaTransparentes = rTrans,
                        recargaAzules = rAzul,
                        recargaMarcados = rMarc,
                        montoEfectivo = mEfec,
                        montoTransferencia = mTransf,
                        montoCredito = mCred,
                        montoAbono = mAbono,
                        montoCanceladoDeuda = mCanc,
                        prestadosTransparentes = pTrans,
                        prestadosAzules = pAzul,
                        prestadosMarcados = pMarc,
                        cantidadBidonesGratis = cGratis,
                        totalPersonalizado = totalPers
                    )
                },
                onCancelar = { viewModel.cerrarModalNuevaVenta() }
            )
        }
    }

    if (mostrarDialogoGasto) {
        DialogoRegistrarGastoBanca(
            clayTheme = clayTheme,
            onConfirmar = { desc, monto -> viewModel.registrarGastoExtra(desc, monto) },
            onDismiss = { viewModel.cerrarDialogoGasto() }
        )
    }

    if (mostrarDialogoCierre) {
        CierreCajaDialog(
            cierreExistente = cierreHoy,
            resumenDia = resumen,
            onConfirmarCierre = { efec, transf, obs ->
                viewModel.registrarCierreCaja(
                    efectivoDeclarado = efec,
                    transferenciasDeclaradas = transf,
                    observaciones = obs
                )
            },
            onDismissRequest = { viewModel.cerrarDialogoCierre() }
        )
    }

    transaccionAEliminar?.let { transaccion ->
        AlertDialog(
            onDismissRequest = { viewModel.cancelarEliminarVenta() },
            title = { Text("Eliminar Venta", fontWeight = FontWeight.Black) },
            text = {
                Text("¿Revertir esta venta de $${String.format(Locale.US, "%.2f", transaccion.totalImporte)}?")
            },
            confirmButton = {
                ClayButton(
                    onClick = { viewModel.confirmarEliminarVenta() },
                    containerColor = RojoAlerta,
                    contentColor = Color.White,
                    height = 42.dp
                ) {
                    Text("Eliminar", fontWeight = FontWeight.Black)
                }
            },
            dismissButton = {
                OutlinedButton(onClick = { viewModel.cancelarEliminarVenta() }) {
                    Text("Cancelar")
                }
            }
        )
    }

    if (mostrarPinParaEditar && ventaParaEditar != null) {
        SecurityPinDialog(
            titulo = "Editar Venta",
            subtitulo = "Ingresa el PIN 1234",
            onDismissRequest = {
                mostrarPinParaEditar = false
                ventaParaEditar = null
            },
            onPinCorrecto = {
                val (transaccion, cliente) = ventaParaEditar!!
                mostrarPinParaEditar = false
                ventaParaEditar = null
                viewModel.abrirEditarVenta(transaccion, cliente)
            }
        )
    }

    if (mostrarPinParaEliminar && ventaParaEliminar != null) {
        SecurityPinDialog(
            titulo = "Eliminar Venta",
            subtitulo = "Ingresa el PIN 1234",
            onDismissRequest = {
                mostrarPinParaEliminar = false
                ventaParaEliminar = null
            },
            onPinCorrecto = {
                val transaccion = ventaParaEliminar!!
                mostrarPinParaEliminar = false
                ventaParaEliminar = null
                viewModel.solicitarEliminarVenta(transaccion)
            }
        )
    }
}

/**
 * Tarjeta Principal estilo Banca Móvil Pichincha:
 * Muestra el Efectivo Neto con botón de ocultar/mostrar monto, stock interactivo de la unidad y desglose rápido.
 */
@Composable
private fun TarjetaHeroBanca(
    resumen: ResumenCierreDia,
    inventario: InventarioCamionEntity?,
    clayTheme: VehicleClayTheme
) {
    var ocultarMontos by rememberSaveable { mutableStateOf(false) }
    var verDetalleStock by rememberSaveable { mutableStateOf(false) }

    val llenos = inventario?.totalBidonesLlenos() ?: 0
    val tope = inventario?.capacidadTopeBidones ?: clayTheme.topeBidonesDefault

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(22.dp))
            .background(
                Brush.linearGradient(
                    colors = listOf(clayTheme.primaryDark, clayTheme.primary)
                )
            )
            .padding(18.dp)
    ) {
        Column {
            // Fila Superior: Etiqueta + Stock Interactivo de la Unidad
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = "Efectivo en caja",
                        color = Color.White.copy(alpha = 0.85f),
                        fontSize = 13.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    IconButton(
                        onClick = { ocultarMontos = !ocultarMontos },
                        modifier = Modifier.size(26.dp)
                    ) {
                        Icon(
                            imageVector = if (ocultarMontos) Icons.Default.VisibilityOff else Icons.Default.Visibility,
                            contentDescription = "Ocultar saldo",
                            tint = Color.White.copy(alpha = 0.85f),
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }

                // Pastilla interactiva de Stock del Vehículo (toca para expandir/colapsar desglose)
                Surface(
                    onClick = { verDetalleStock = !verDetalleStock },
                    color = Color.White.copy(alpha = 0.16f),
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier.bounceClick(0.95f)
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(7.dp)
                                .background(PichinchaYellow, CircleShape)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "${clayTheme.nombreCorto}: $llenos/$tope",
                            color = Color.White,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Black,
                            maxLines = 1
                        )
                    }
                }
            }

            // Saldo Principal Grande
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Bottom
            ) {
                Text(
                    text = if (ocultarMontos) "$ ••••••" else "$${String.format(Locale.US, "%.2f", resumen.efectivoNetoCaja)}",
                    color = Color.White,
                    fontSize = 32.sp,
                    fontWeight = FontWeight.Black
                )

                Column(horizontalAlignment = Alignment.End) {
                    Text(
                        text = "${resumen.totalBidonesVendidos} bidones hoy",
                        color = PichinchaYellow,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Black
                    )
                    if (resumen.totalBidonesPrestados > 0) {
                        Text(
                            text = "${resumen.totalBidonesPrestados} prestados",
                            color = Color.White.copy(alpha = 0.85f),
                            fontSize = 11.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }
            }

            // Desglose interactivo de stock si el usuario toca la pastilla del vehículo
            AnimatedVisibility(visible = verDetalleStock && inventario != null) {
                inventario?.let { inv ->
                    Surface(
                        color = Color.Black.copy(alpha = 0.22f),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 10.dp)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 12.dp, vertical = 8.dp),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = "PET: ${inv.bidonesLlenosTransparentes}",
                                color = Color.White,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = "Azul: ${inv.bidonesLlenosAzules}",
                                color = Color.White,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = "Marcadas: ${inv.bidonesLlenosMarcados}",
                                color = PichinchaYellow,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Black
                            )
                            Text(
                                text = "Vacíos: ${inv.bidonesVacios}",
                                color = Color.White.copy(alpha = 0.8f),
                                fontSize = 11.sp,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Fila de 4 Métricas Rápidas
            Surface(
                color = Color.White.copy(alpha = 0.12f),
                shape = RoundedCornerShape(14.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 10.dp, horizontal = 10.dp),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    MetricaHeroItem(
                        label = "Venta Bruta",
                        valor = if (ocultarMontos) "••••" else "$${String.format(Locale.US, "%.2f", resumen.granTotalVenta)}"
                    )
                    MetricaHeroItem(
                        label = "Transf.",
                        valor = if (ocultarMontos) "••••" else "$${String.format(Locale.US, "%.2f", resumen.totalIngresoTransferencia)}"
                    )
                    MetricaHeroItem(
                        label = "Créditos",
                        valor = if (ocultarMontos) "••••" else "$${String.format(Locale.US, "%.2f", resumen.totalCreditosOtorgados)}"
                    )
                    MetricaHeroItem(
                        label = "Abonos",
                        valor = if (ocultarMontos) "••••" else "$${String.format(Locale.US, "%.2f", resumen.totalAbonosRecibidos)}"
                    )
                }
            }
        }
    }
}

@Composable
private fun MetricaHeroItem(label: String, valor: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
            text = label,
            color = Color.White.copy(alpha = 0.72f),
            fontSize = 10.sp,
            fontWeight = FontWeight.Medium,
            maxLines = 1
        )
        Spacer(modifier = Modifier.height(2.dp))
        Text(
            text = valor,
            color = Color.White,
            fontSize = 13.sp,
            fontWeight = FontWeight.Black,
            maxLines = 1
        )
    }
}

@Composable
private fun SeccionGastosBanca(
    gastos: List<GastoExtraEntity>,
    onEliminarGasto: (GastoExtraEntity) -> Unit
) {
    ClayCard(
        modifier = Modifier.fillMaxWidth(),
        containerColor = AmbarFondo,
        cornerRadius = 16.dp,
        contentPadding = PaddingValues(12.dp)
    ) {
        Text(
            text = "Gastos de hoy (${gastos.size})",
            color = AmbarAdvertencia,
            fontSize = 12.sp,
            fontWeight = FontWeight.Black
        )
        Spacer(modifier = Modifier.height(4.dp))
        gastos.forEach { gasto ->
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 2.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = gasto.concepto,
                    color = TextoOscuro,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Medium,
                    modifier = Modifier.weight(1f),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = "-$${String.format(Locale.US, "%.2f", gasto.monto)}",
                        color = RojoAlerta,
                        fontWeight = FontWeight.Black,
                        fontSize = 13.sp
                    )
                    IconButton(
                        onClick = { onEliminarGasto(gasto) },
                        modifier = Modifier.size(26.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Delete,
                            contentDescription = "Eliminar",
                            tint = TextoSecundario,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }
            }
        }
    }
}

/**
 * Tarjeta de Transacción estilo Banca Móvil: limpia, compacta y con botones de 1 línea.
 */
@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun TarjetaTransaccionBanca(
    transaccion: TransaccionVentaEntity,
    nombreCliente: String,
    clayTheme: VehicleClayTheme,
    onEditarClick: () -> Unit,
    onEliminarClick: () -> Unit,
    onEnviarReciboWhatsApp: () -> Unit
) {
    val formatoHora = remember { SimpleDateFormat("HH:mm", Locale.getDefault()) }
    val horaTexto = formatoHora.format(Date(transaccion.fecha))

    ClayCard(
        modifier = Modifier.fillMaxWidth(),
        cornerRadius = 18.dp,
        elevation = 2.dp,
        contentPadding = PaddingValues(14.dp)
    ) {
        // Cabecera: Nombre del Cliente + Hora | Total Cobrado
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = nombreCliente,
                    color = PichinchaNavy,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Black,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = horaTexto,
                    color = TextoSecundario,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Medium
                )
            }

            Spacer(modifier = Modifier.width(8.dp))

            Text(
                text = "$${String.format(Locale.US, "%.2f", transaccion.totalImporte)}",
                color = VerdeEfectivo,
                fontSize = 18.sp,
                fontWeight = FontWeight.Black
            )
        }

        Spacer(modifier = Modifier.height(8.dp))

        // Pastillas de Productos y Pagos
        FlowRow(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(6.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            val tieneMulti = (transaccion.recargaTransparentes + transaccion.recargaAzules + transaccion.recargaMarcados) > 0
            if (tieneMulti) {
                if (transaccion.recargaTransparentes > 0) {
                    ClayBadge(
                        text = "${transaccion.recargaTransparentes} PET",
                        containerColor = BidonTransparenteFondo,
                        contentColor = BidonTransparenteColor
                    )
                }
                if (transaccion.recargaAzules > 0) {
                    ClayBadge(
                        text = "${transaccion.recargaAzules} Azul",
                        containerColor = BidonAzulFondo,
                        contentColor = BidonAzulColor
                    )
                }
                if (transaccion.recargaMarcados > 0) {
                    ClayBadge(
                        text = "${transaccion.recargaMarcados} Marc",
                        containerColor = BidonMarcadoFondo,
                        contentColor = BidonMarcadoColor
                    )
                }
            } else if (transaccion.cantidadBidonesRecarga > 0) {
                val (bg, fg, lbl) = when (transaccion.tipoBidon) {
                    TipoBidon.TRANSPARENTE -> Triple(BidonTransparenteFondo, BidonTransparenteColor, "PET")
                    TipoBidon.AZUL -> Triple(BidonAzulFondo, BidonAzulColor, "Azul")
                    TipoBidon.MARCADO -> Triple(BidonMarcadoFondo, BidonMarcadoColor, "Marc")
                }
                ClayBadge(
                    text = "${transaccion.cantidadBidonesRecarga} $lbl",
                    containerColor = bg,
                    contentColor = fg
                )
            }

            if (transaccion.cantidadBidonesPrestados > 0) {
                ClayBadge(
                    text = "+${transaccion.cantidadBidonesPrestados} Prest.",
                    containerColor = AmbarFondo,
                    contentColor = AmbarAdvertencia
                )
            }

            if (transaccion.cantidadBidonesGratis > 0) {
                val cobrados = maxOf(0, transaccion.cantidadBidonesRecarga + transaccion.cantidadBidonesPrestados - transaccion.cantidadBidonesGratis)
                ClayBadge(
                    text = "${cobrados}+${transaccion.cantidadBidonesGratis} Gratis",
                    containerColor = VerdeEfectivoFondo,
                    contentColor = VerdeEfectivo
                )
            }

            if (transaccion.cantidadBidonesVendidosEnvase > 0) {
                ClayBadge(
                    text = "${transaccion.cantidadBidonesVendidosEnvase} Env. Nuevo",
                    containerColor = clayTheme.softContainer,
                    contentColor = clayTheme.primaryDark
                )
            }

            if (transaccion.cantidadGalones > 0) {
                ClayBadge(text = "${transaccion.cantidadGalones} Gal")
            }
            if (transaccion.cantidadPacas > 0) {
                ClayBadge(text = "${transaccion.cantidadPacas} Pac")
            }
            if (transaccion.cantidadMinis > 0) {
                ClayBadge(text = "${transaccion.cantidadMinis} Min")
            }

            // Desglose de pago
            if (transaccion.montoEfectivo > 0) {
                ClayBadge(
                    text = "Efec $${String.format(Locale.US, "%.2f", transaccion.montoEfectivo)}",
                    containerColor = VerdeEfectivoFondo,
                    contentColor = VerdeEfectivo
                )
            }
            if (transaccion.montoTransferencia > 0) {
                ClayBadge(
                    text = "Transf $${String.format(Locale.US, "%.2f", transaccion.montoTransferencia)}",
                    containerColor = clayTheme.softContainer,
                    contentColor = clayTheme.primaryDark
                )
            }
            if (transaccion.montoCredito > 0) {
                ClayBadge(
                    text = "Fiado $${String.format(Locale.US, "%.2f", transaccion.montoCredito)}",
                    containerColor = RojoAlertaFondo,
                    contentColor = RojoAlerta
                )
            }
            if (transaccion.montoAbono > 0) {
                ClayBadge(
                    text = "Abono $${String.format(Locale.US, "%.2f", transaccion.montoAbono)}",
                    containerColor = VerdeEfectivoFondo,
                    contentColor = VerdeEfectivo
                )
            }
        }

        Spacer(modifier = Modifier.height(10.dp))
        HorizontalDivider(color = BordeSuave)
        Spacer(modifier = Modifier.height(8.dp))

        // Acciones inferiores: textos cortos que siempre caben en 1 línea
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Surface(
                onClick = onEnviarReciboWhatsApp,
                color = VerdeEfectivoFondo,
                shape = RoundedCornerShape(10.dp)
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.Share,
                        contentDescription = "Recibo",
                        tint = VerdeEfectivo,
                        modifier = Modifier.size(14.dp)
                    )
                    Spacer(modifier = Modifier.width(5.dp))
                    Text(
                        text = "Recibo",
                        color = VerdeEfectivo,
                        fontWeight = FontWeight.Black,
                        fontSize = 11.sp,
                        maxLines = 1
                    )
                }
            }

            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Surface(
                    onClick = onEditarClick,
                    color = Color(0xFFE6ECF5),
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.Edit,
                            contentDescription = "Editar",
                            tint = PichinchaNavy,
                            modifier = Modifier.size(14.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "Editar",
                            color = PichinchaNavy,
                            fontWeight = FontWeight.Bold,
                            fontSize = 11.sp,
                            maxLines = 1
                        )
                    }
                }

                Surface(
                    onClick = onEliminarClick,
                    color = RojoAlertaFondo,
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.Delete,
                            contentDescription = "Borrar",
                            tint = RojoAlerta,
                            modifier = Modifier.size(14.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "Borrar",
                            color = RojoAlerta,
                            fontWeight = FontWeight.Bold,
                            fontSize = 11.sp,
                            maxLines = 1
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun DialogoRegistrarGastoBanca(
    clayTheme: VehicleClayTheme,
    onConfirmar: (String, Double) -> Unit,
    onDismiss: () -> Unit
) {
    var descripcion by rememberSaveable { mutableStateOf("") }
    var montoTexto by rememberSaveable { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Registrar Gasto", fontWeight = FontWeight.Black, color = PichinchaNavy) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    listOf("Almuerzo", "Diésel", "Peaje").forEach { sug ->
                        Surface(
                            onClick = { descripcion = sug },
                            color = if (descripcion == sug) clayTheme.primary else Color(0xFFE6ECF5),
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier.weight(1f)
                        ) {
                            Text(
                                text = sug,
                                color = if (descripcion == sug) Color.White else PichinchaNavy,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                textAlign = TextAlign.Center,
                                maxLines = 1,
                                modifier = Modifier.padding(vertical = 8.dp)
                            )
                        }
                    }
                }

                OutlinedTextField(
                    value = descripcion,
                    onValueChange = { descripcion = it },
                    label = { Text("Concepto") },
                    singleLine = true,
                    colors = marineOutlinedTextFieldColors(accentColor = clayTheme.primary),
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = montoTexto,
                    onValueChange = { montoTexto = it },
                    label = { Text("Monto ($)") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    singleLine = true,
                    colors = marineOutlinedTextFieldColors(accentColor = clayTheme.primary),
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            ClayButton(
                onClick = {
                    val m = montoTexto.toDoubleOrNull() ?: 0.0
                    onConfirmar(descripcion, m)
                },
                containerColor = PichinchaYellow,
                contentColor = PichinchaNavy,
                height = 42.dp
            ) {
                Text("Guardar", fontWeight = FontWeight.Black)
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
private fun TarjetaAlertaCreditosSinCobro(
    alertas: List<AlertaCreditoVencido>,
    onCobrarCliente: (ClienteEntity) -> Unit,
    onRecordarWhatsApp: (AlertaCreditoVencido) -> Unit
) {
    var expandido by rememberSaveable { mutableStateOf(false) }
    val totalDeuda = alertas.sumOf { it.cliente.deudaMonetaria }

    ClayCard(
        modifier = Modifier.fillMaxWidth(),
        containerColor = Color(0xFFFFFBEB),
        accentBorderColor = AmbarAdvertencia,
        cornerRadius = 18.dp,
        contentPadding = PaddingValues(14.dp)
    ) {
        Column {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { expandido = !expandido },
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "⏰ CRÉDITOS SIN COBRAR (2+ DÍAS): ${alertas.size}",
                        color = Color(0xFFB45309),
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Black,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Text(
                        text = "Deuda acumulada: $${String.format(Locale.US, "%.2f", totalDeuda)} • Toca para ${if (expandido) "ocultar" else "ver y cobrar"}",
                        color = TextoOscuro,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.SemiBold,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }

                ClayBadge(
                    text = if (expandido) "Ocultar" else "Ver (${alertas.size})",
                    containerColor = AmbarAdvertencia,
                    contentColor = Color.White
                )
            }

            if (expandido) {
                Spacer(modifier = Modifier.height(10.dp))
                HorizontalDivider(color = AmbarAdvertencia.copy(alpha = 0.25f))
                Spacer(modifier = Modifier.height(6.dp))

                alertas.forEach { alerta ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 5.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = alerta.cliente.nombre,
                                color = TextoOscuro,
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Black,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                            Text(
                                text = "Debe $${String.format(Locale.US, "%.2f", alerta.cliente.deudaMonetaria)} • ${alerta.diasSinCobrar} días sin cobro (${alerta.fechaReferenciaTexto})",
                                color = RojoAlerta,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }

                        Spacer(modifier = Modifier.width(6.dp))

                        Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                            Surface(
                                onClick = { onCobrarCliente(alerta.cliente) },
                                color = VerdeEfectivo,
                                shape = RoundedCornerShape(8.dp)
                            ) {
                                Text(
                                    text = "Cobrar",
                                    color = Color.White,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Black,
                                    maxLines = 1,
                                    softWrap = false,
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 5.dp)
                                )
                            }

                            Surface(
                                onClick = { onRecordarWhatsApp(alerta) },
                                color = Color(0xFF25D366),
                                shape = RoundedCornerShape(8.dp)
                            ) {
                                Text(
                                    text = "WhatsApp",
                                    color = Color.White,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Black,
                                    maxLines = 1,
                                    softWrap = false,
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 5.dp)
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

private fun compartirPorWhatsApp(context: Context, textoReporte: String) {
    val sendIntent = Intent(Intent.ACTION_SEND).apply {
        type = "text/plain"
        putExtra(Intent.EXTRA_TEXT, textoReporte)
    }
    try {
        val whatsappIntent = Intent(sendIntent).apply {
            setPackage("com.whatsapp")
        }
        context.startActivity(whatsappIntent)
    } catch (_: Exception) {
        val chooser = Intent.createChooser(sendIntent, "Compartir reporte")
        context.startActivity(chooser)
    }
}

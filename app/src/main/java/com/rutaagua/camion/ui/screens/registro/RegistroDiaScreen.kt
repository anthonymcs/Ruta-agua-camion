package com.rutaagua.camion.ui.screens.registro

import android.content.Context
import android.content.Intent
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
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
import androidx.compose.material.icons.automirrored.filled.ReceiptLong
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Fastfood
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
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
import com.rutaagua.camion.data.local.entity.ClienteEntity
import com.rutaagua.camion.data.local.entity.GastoExtraEntity
import com.rutaagua.camion.data.local.entity.InventarioCamionEntity
import com.rutaagua.camion.data.local.entity.TransaccionVentaEntity
import com.rutaagua.camion.data.local.model.FormaPago
import com.rutaagua.camion.data.local.model.ResumenCierreDia
import com.rutaagua.camion.data.local.model.TipoBidon
import com.rutaagua.camion.data.local.model.TipoVehiculo
import com.rutaagua.camion.data.local.prefs.RolUsuario
import com.rutaagua.camion.ui.components.BidonIllustration
import com.rutaagua.camion.ui.components.ClayBadge
import com.rutaagua.camion.ui.components.ClayButton
import com.rutaagua.camion.ui.components.ClayCard
import com.rutaagua.camion.ui.components.ClayInsetBox
import com.rutaagua.camion.ui.components.GalonIllustration
import com.rutaagua.camion.ui.components.MiniIllustration
import com.rutaagua.camion.ui.components.PacaIllustration
import com.rutaagua.camion.ui.components.VehicleClayTheme
import com.rutaagua.camion.ui.components.obtenerClayThemePorVehiculo
import com.rutaagua.camion.ui.security.SecurityPinDialog
import com.rutaagua.camion.ui.theme.AguaPrimary
import com.rutaagua.camion.ui.theme.AguaPrimaryDark
import com.rutaagua.camion.ui.theme.AmbarAdvertencia
import com.rutaagua.camion.ui.theme.AmbarFondo
import com.rutaagua.camion.ui.theme.BidonAzulColor
import com.rutaagua.camion.ui.theme.BidonMarcadoColor
import com.rutaagua.camion.ui.theme.BidonTransparenteColor
import com.rutaagua.camion.ui.theme.ClayInsetBackground
import com.rutaagua.camion.ui.theme.RojoAlerta
import com.rutaagua.camion.ui.theme.RojoAlertaFondo
import com.rutaagua.camion.ui.theme.SuperficieTarjeta
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

    var ventaParaEditar by remember { mutableStateOf<Pair<TransaccionVentaEntity, ClienteEntity?>?>(null) }
    var ventaParaEliminar by remember { mutableStateOf<TransaccionVentaEntity?>(null) }
    var mostrarPinParaEditar by remember { mutableStateOf(false) }
    var mostrarPinParaEliminar by remember { mutableStateOf(false) }

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

    Scaffold(
        modifier = modifier,
        containerColor = clayTheme.canvasColor,
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            HeaderStockClay(
                inventario = inventario,
                clayTheme = clayTheme
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = { viewModel.abrirModalNuevaVenta() },
                containerColor = clayTheme.primary,
                contentColor = Color.White,
                shape = RoundedCornerShape(18.dp),
                modifier = Modifier.padding(bottom = 12.dp)
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 18.dp, vertical = 12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(imageVector = Icons.Default.Add, contentDescription = null)
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(text = "Nueva Entrega", fontWeight = FontWeight.Black, fontSize = 15.sp)
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
            // 1. Métricas del día en tarjetas Clay
            item {
                FilaTarjetasCuadreClay(
                    resumen = resumen,
                    esModoAdmin = (rolActivo == RolUsuario.ADMIN_ROLANDO),
                    clayTheme = clayTheme
                )
            }

            // 1.1 Si es el Perfil de Rolando, mostrar tarjeta de Arqueo
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

            // 2. Acciones operativas (Cierre de Caja, Gasto Extra, Reporte WhatsApp)
            item {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 4.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        ClayButton(
                            onClick = { viewModel.abrirDialogoCierre() },
                            containerColor = if (cierreHoy != null) VerdeEfectivo else Color(0xFF0F172A),
                            height = 46.dp,
                            modifier = Modifier.weight(1.2f)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Lock,
                                contentDescription = null,
                                tint = Color.White,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = if (cierreHoy != null) "Cierre: $${String.format(Locale.US, "%.2f", cierreHoy?.totalDeclarado)}"
                                else "Cierre de Caja",
                                fontWeight = FontWeight.Bold,
                                color = Color.White,
                                fontSize = 13.sp
                            )
                        }

                        ClayButton(
                            onClick = { viewModel.abrirDialogoGasto() },
                            containerColor = SuperficieTarjeta,
                            contentColor = TextoOscuro,
                            height = 46.dp,
                            modifier = Modifier.weight(0.9f)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Fastfood,
                                contentDescription = null,
                                tint = AmbarAdvertencia,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = "+ Gasto",
                                color = TextoOscuro,
                                fontWeight = FontWeight.Bold,
                                fontSize = 13.sp
                            )
                        }

                        ClayButton(
                            onClick = { compartirReporteWhatsApp(context, viewModel.generarTextoReporteWhatsApp()) },
                            containerColor = Color(0xFF16A34A),
                            height = 46.dp,
                            modifier = Modifier.weight(0.9f)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Share,
                                contentDescription = null,
                                tint = Color.White,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = "Reporte",
                                color = Color.White,
                                fontWeight = FontWeight.Bold,
                                fontSize = 13.sp
                            )
                        }
                    }
                }
            }

            // Selector de filtro de vehículo (Admin)
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
                                },
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = clayTheme.primary,
                                    selectedLabelColor = Color.White
                                )
                            )
                        }
                    }
                }
            }

            // 3. Cabecera de entregas del día
            item {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 18.dp, vertical = 10.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Entregas de hoy",
                        fontWeight = FontWeight.Black,
                        fontSize = 17.sp,
                        color = TextoOscuro
                    )
                    ClayBadge(
                        text = "${transacciones.size} registros",
                        containerColor = clayTheme.softContainer,
                        contentColor = clayTheme.primaryDark
                    )
                }
            }

            // 4. Lista de entregas
            if (transacciones.isEmpty()) {
                item {
                    EstadoVacioRegistroClay()
                }
            } else {
                items(transacciones, key = { it.id }) { transaccion ->
                    val cliente = todosLosClientes.find { it.id == transaccion.clienteId }
                    TarjetaRegistroItemClay(
                        transaccion = transaccion,
                        nombreCliente = cliente?.nombre?.takeIf { it.isNotBlank() }
                            ?: transaccion.nombreClientePaso?.takeIf { it.isNotBlank() }
                            ?: "Cliente",
                        ubicacion = cliente?.ubicacion ?: "",
                        clayTheme = clayTheme,
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

            // 5. Gastos en Ruta
            if (gastos.isNotEmpty()) {
                item {
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        text = "Gastos en Ruta (${gastos.size})",
                        fontWeight = FontWeight.Black,
                        fontSize = 15.sp,
                        color = TextoOscuro,
                        modifier = Modifier.padding(horizontal = 18.dp, vertical = 4.dp)
                    )
                }
                items(gastos, key = { it.id }) { gasto ->
                    TarjetaGastoItemClay(
                        gasto = gasto,
                        onEliminar = { viewModel.eliminarGasto(gasto) }
                    )
                }
            }
        }
    }

    // Modal de Nueva / Editar Entrega
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
                onConfirmarVenta = { cId, nomPaso, tipoB, cRec, pBidon, cPrest, cDev, cVendEnv, pEnv, cGal, cPac, cMin, fPago, mPag, nota, rTrans, rAzul, rMarc, mEf, mTr, mCr, mAb, mCanc, pTrans, pAzul, pMarc, cGratis, totalPers ->
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

    // Confirmación de eliminación
    if (transaccionAEliminar != null) {
        val cliente = todosLosClientes.find { it.id == transaccionAEliminar?.clienteId }
        val nombre = cliente?.nombre ?: transaccionAEliminar?.nombreClientePaso ?: "Cliente"
        AlertDialog(
            onDismissRequest = { viewModel.cancelarEliminarVenta() },
            title = { Text("¿Eliminar entrega?", fontWeight = FontWeight.Bold) },
            text = {
                Text(
                    text = "Se eliminará el registro de \"$nombre\" ($${String.format(Locale.US, "%.2f", transaccionAEliminar?.totalImporte ?: 0.0)}) y se restaurará el stock en el vehículo.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = TextoOscuro
                )
            },
            confirmButton = {
                Button(
                    onClick = { viewModel.confirmarEliminarVenta() },
                    colors = ButtonDefaults.buttonColors(containerColor = RojoAlerta)
                ) {
                    Text("Eliminar", fontWeight = FontWeight.Bold, color = Color.White)
                }
            },
            dismissButton = {
                OutlinedButton(onClick = { viewModel.cancelarEliminarVenta() }) {
                    Text("Cancelar")
                }
            }
        )
    }

    if (mostrarDialogoCierre) {
        CierreCajaDialog(
            cierreExistente = cierreHoy,
            onConfirmarCierre = { ef, tr, obs ->
                viewModel.registrarCierreCaja(ef, tr, obs)
            },
            onDismissRequest = { viewModel.cerrarDialogoCierre() }
        )
    }

    if (mostrarDialogoGasto) {
        DialogoAgregarGasto(
            onConfirmar = { concepto, monto -> viewModel.registrarGastoExtra(concepto, monto) },
            onDismiss = { viewModel.cerrarDialogoGasto() }
        )
    }

    if (mostrarPinParaEditar && ventaParaEditar != null) {
        SecurityPinDialog(
            titulo = "Editar Entrega",
            subtitulo = "Ingresa el PIN de autorización (1234)",
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

    if (mostrarPinParaEliminar && ventaParaEliminar != null) {
        SecurityPinDialog(
            titulo = "Eliminar Entrega",
            subtitulo = "Ingresa el PIN de autorización (1234)",
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

@Composable
private fun HeaderStockClay(
    inventario: InventarioCamionEntity?,
    clayTheme: VehicleClayTheme
) {
    if (inventario == null) return
    Surface(
        color = clayTheme.canvasColor,
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 8.dp),
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            MiniChipStockClay(
                label = "PET",
                cantidad = inventario.bidonesLlenosTransparentes,
                color = BidonTransparenteColor,
                modifier = Modifier.weight(1f)
            )
            MiniChipStockClay(
                label = "Azul",
                cantidad = inventario.bidonesLlenosAzules,
                color = BidonAzulColor,
                modifier = Modifier.weight(1f)
            )
            MiniChipStockClay(
                label = "Marcado",
                cantidad = inventario.bidonesLlenosMarcados,
                color = BidonMarcadoColor,
                modifier = Modifier.weight(1f)
            )
            MiniChipStockClay(
                label = "Vacíos",
                cantidad = inventario.bidonesVacios,
                color = Color(0xFF475569),
                modifier = Modifier.weight(1f)
            )
        }
    }
}

@Composable
private fun MiniChipStockClay(
    label: String,
    cantidad: Int,
    color: Color,
    modifier: Modifier = Modifier
) {
    Surface(
        color = SuperficieTarjeta,
        shape = RoundedCornerShape(12.dp),
        shadowElevation = 2.dp,
        border = BorderStroke(1.dp, color.copy(alpha = 0.25f)),
        modifier = modifier
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 6.dp, vertical = 6.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            Box(
                modifier = Modifier
                    .size(8.dp)
                    .background(color, CircleShape)
            )
            Spacer(modifier = Modifier.width(5.dp))
            Text(
                text = "$label: $cantidad",
                fontSize = 11.sp,
                fontWeight = FontWeight.Black,
                color = TextoOscuro
            )
        }
    }
}

@Composable
private fun FilaTarjetasCuadreClay(
    resumen: ResumenCierreDia,
    esModoAdmin: Boolean,
    clayTheme: VehicleClayTheme
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .horizontalScroll(rememberScrollState())
            .padding(horizontal = 16.dp, vertical = 8.dp),
        horizontalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        TarjetaMetricaClay(
            titulo = "Bidones Entregados",
            valor = "${resumen.totalBidonesVendidos}",
            subtitulo = "${resumen.totalBidonesPrestados} prest. • ${resumen.totalBidonesDevueltos} dev.",
            colorValor = clayTheme.primary
        )

        if (esModoAdmin) {
            TarjetaMetricaClay(
                titulo = "Efectivo Neto",
                valor = "$${String.format(Locale.US, "%.2f", resumen.efectivoNetoCaja)}",
                subtitulo = "En caja hoy",
                colorValor = VerdeEfectivo
            )
        }

        TarjetaMetricaClay(
            titulo = "Transferencias",
            valor = "$${String.format(Locale.US, "%.2f", resumen.totalIngresoTransferencia)}",
            subtitulo = "Banca móvil",
            colorValor = Color(0xFF0284C7)
        )

        TarjetaMetricaClay(
            titulo = "Créditos Hoy",
            valor = "$${String.format(Locale.US, "%.2f", resumen.totalCreditosOtorgados)}",
            subtitulo = "Fiado otorgado",
            colorValor = RojoAlerta
        )

        TarjetaMetricaClay(
            titulo = "Abonos Cobrados",
            valor = "$${String.format(Locale.US, "%.2f", resumen.totalAbonosRecibidos + resumen.totalDeudasCanceladas)}",
            subtitulo = "Deudas previas",
            colorValor = Color(0xFF7C3AED)
        )

        TarjetaMetricaClay(
            titulo = "Gastos Ruta",
            valor = "$${String.format(Locale.US, "%.2f", resumen.totalGastosRuta)}",
            subtitulo = "Salidas de caja",
            colorValor = AmbarAdvertencia
        )
    }
}

@Composable
private fun TarjetaMetricaClay(
    titulo: String,
    valor: String,
    subtitulo: String,
    colorValor: Color
) {
    ClayCard(
        modifier = Modifier.width(156.dp),
        cornerRadius = 20.dp,
        elevation = 4.dp,
        contentPadding = PaddingValues(14.dp)
    ) {
        Text(
            text = titulo,
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold,
            color = TextoSecundario
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = valor,
            fontSize = 22.sp,
            fontWeight = FontWeight.Black,
            color = colorValor
        )
        Spacer(modifier = Modifier.height(2.dp))
        Text(
            text = subtitulo,
            fontSize = 10.sp,
            fontWeight = FontWeight.Medium,
            color = TextoSecundario
        )
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun TarjetaRegistroItemClay(
    transaccion: TransaccionVentaEntity,
    nombreCliente: String,
    ubicacion: String,
    clayTheme: VehicleClayTheme,
    onEditar: () -> Unit,
    onEliminar: () -> Unit,
    onEnviarRecibo: () -> Unit
) {
    val formatoHora = remember { SimpleDateFormat("HH:mm", Locale.getDefault()) }
    val horaTexto = formatoHora.format(Date(transaccion.fecha))

    ClayCard(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 5.dp),
        cornerRadius = 20.dp,
        elevation = 4.dp,
        contentPadding = PaddingValues(14.dp)
    ) {
        // Cabecera: Nombre completo del cliente (sin truncar) + acciones
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.Top
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = nombreCliente,
                    fontSize = 17.sp,
                    fontWeight = FontWeight.Black,
                    color = TextoOscuro
                )
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    modifier = Modifier.padding(top = 2.dp)
                ) {
                    Text(
                        text = horaTexto,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = TextoSecundario
                    )
                    if (ubicacion.isNotBlank()) {
                        Text(
                            text = "• $ubicacion",
                            fontSize = 11.sp,
                            color = TextoSecundario
                        )
                    }
                }
            }

            Row(verticalAlignment = Alignment.CenterVertically) {
                val esPagoMixto = listOf(
                    transaccion.montoEfectivo > 0,
                    transaccion.montoTransferencia > 0,
                    transaccion.montoCredito > 0,
                    transaccion.montoAbono > 0
                ).count { it } > 1

                if (esPagoMixto) {
                    ClayBadge(
                        text = "Combinado",
                        containerColor = Color(0xFFDBEAFE),
                        contentColor = Color(0xFF1D4ED8)
                    )
                } else {
                    BadgeFormaPago(formaPago = transaccion.formaPagoPrincipal)
                }

                IconButton(onClick = onEditar, modifier = Modifier.size(30.dp)) {
                    Icon(
                        imageVector = Icons.Default.Edit,
                        contentDescription = "Editar",
                        tint = clayTheme.primary,
                        modifier = Modifier.size(16.dp)
                    )
                }
                IconButton(onClick = onEliminar, modifier = Modifier.size(30.dp)) {
                    Icon(
                        imageVector = Icons.Default.Delete,
                        contentDescription = "Eliminar",
                        tint = RojoAlerta,
                        modifier = Modifier.size(16.dp)
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        // Desglose de productos e importe en contenedor Clay Inset
        ClayInsetBox(
            modifier = Modifier.fillMaxWidth(),
            cornerRadius = 14.dp,
            contentPadding = PaddingValues(horizontal = 12.dp, vertical = 10.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                FlowRow(
                    modifier = Modifier.weight(1f),
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    val totalTrans = transaccion.recargaTransparentes + transaccion.prestadosTransparentes + transaccion.envasesVendidosTransparentes
                    val totalAzul = transaccion.recargaAzules + transaccion.prestadosAzules + transaccion.envasesVendidosAzules
                    val totalMarc = transaccion.recargaMarcados + transaccion.prestadosMarcados + transaccion.envasesVendidosMarcados
                    val tieneMulti = (totalTrans + totalAzul + totalMarc) > 0

                    if (tieneMulti) {
                        if (totalTrans > 0) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                BidonIllustration(tipo = TipoBidon.TRANSPARENTE, size = 22.dp)
                                Spacer(modifier = Modifier.width(3.dp))
                                Text(text = "$totalTrans PET", fontWeight = FontWeight.Black, fontSize = 12.sp, color = BidonTransparenteColor)
                            }
                        }
                        if (totalAzul > 0) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                BidonIllustration(tipo = TipoBidon.AZUL, size = 22.dp)
                                Spacer(modifier = Modifier.width(3.dp))
                                Text(text = "$totalAzul Azul", fontWeight = FontWeight.Black, fontSize = 12.sp, color = BidonAzulColor)
                            }
                        }
                        if (totalMarc > 0) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                BidonIllustration(tipo = TipoBidon.MARCADO, size = 22.dp)
                                Spacer(modifier = Modifier.width(3.dp))
                                Text(text = "$totalMarc Marc.", fontWeight = FontWeight.Black, fontSize = 12.sp, color = BidonMarcadoColor)
                            }
                        }
                    } else {
                        val totalBidones = transaccion.cantidadBidonesRecarga + transaccion.cantidadBidonesPrestados + transaccion.cantidadBidonesVendidosEnvase
                        if (totalBidones > 0) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                BidonIllustration(tipo = transaccion.tipoBidon, size = 22.dp)
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(text = "$totalBidones bid.", fontWeight = FontWeight.Black, fontSize = 12.sp)
                            }
                        }
                    }

                    // Notación "35+1" si hay bidón gratis
                    if (transaccion.cantidadBidonesGratis > 0) {
                        val totalEntregados = transaccion.cantidadBidonesRecarga + transaccion.cantidadBidonesPrestados
                        val cobrados = maxOf(0, totalEntregados - transaccion.cantidadBidonesGratis)
                        ClayBadge(
                            text = "${cobrados}+${transaccion.cantidadBidonesGratis}",
                            containerColor = Color(0xFFDBEAFE),
                            contentColor = AguaPrimaryDark
                        )
                    }

                    if (transaccion.cantidadBidonesPrestados > 0) {
                        ClayBadge(
                            text = "+${transaccion.cantidadBidonesPrestados} prestados",
                            containerColor = AmbarFondo,
                            contentColor = AmbarAdvertencia
                        )
                    }

                    if (transaccion.cantidadBidonesDevueltos > 0) {
                        ClayBadge(
                            text = "-${transaccion.cantidadBidonesDevueltos} devueltos",
                            containerColor = VerdeEfectivoFondo,
                            contentColor = VerdeEfectivo
                        )
                    }

                    if (transaccion.cantidadGalones > 0) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            GalonIllustration(size = 20.dp)
                            Spacer(modifier = Modifier.width(3.dp))
                            Text(text = "${transaccion.cantidadGalones} gal", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                        }
                    }

                    if (transaccion.cantidadPacas > 0) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            PacaIllustration(size = 20.dp)
                            Spacer(modifier = Modifier.width(3.dp))
                            Text(text = "${transaccion.cantidadPacas} pac", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                        }
                    }

                    if (transaccion.cantidadMinis > 0) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            MiniIllustration(size = 20.dp)
                            Spacer(modifier = Modifier.width(3.dp))
                            Text(text = "${transaccion.cantidadMinis} min", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                        }
                    }
                }

                Spacer(modifier = Modifier.width(8.dp))

                Column(horizontalAlignment = Alignment.End) {
                    Text(
                        text = "$${String.format(Locale.US, "%.2f", transaccion.totalImporte)}",
                        fontSize = 19.sp,
                        fontWeight = FontWeight.Black,
                        color = VerdeEfectivo
                    )
                    val partesPago = mutableListOf<String>()
                    if (transaccion.montoEfectivo > 0) partesPago.add("Ef: $${String.format(Locale.US, "%.2f", transaccion.montoEfectivo)}")
                    if (transaccion.montoTransferencia > 0) partesPago.add("Tr: $${String.format(Locale.US, "%.2f", transaccion.montoTransferencia)}")
                    if (transaccion.montoCredito > 0) partesPago.add("Fiado: $${String.format(Locale.US, "%.2f", transaccion.montoCredito)}")
                    if (transaccion.montoAbono > 0) partesPago.add("Ab: $${String.format(Locale.US, "%.2f", transaccion.montoAbono)}")

                    if (partesPago.size > 1) {
                        Text(
                            text = partesPago.joinToString(" • "),
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            color = TextoSecundario
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        // Botón compacto para enviar recibo por WhatsApp
        Surface(
            onClick = onEnviarRecibo,
            color = Color(0xFFECFDF3),
            shape = RoundedCornerShape(12.dp),
            border = BorderStroke(1.dp, Color(0xFF16A34A).copy(alpha = 0.3f)),
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(
                modifier = Modifier.padding(vertical = 7.dp),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.Share,
                    contentDescription = null,
                    tint = Color(0xFF15803D),
                    modifier = Modifier.size(14.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = "Enviar recibo por WhatsApp",
                    color = Color(0xFF15803D),
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
        FormaPago.TRANSFERENCIA -> Triple(Color(0xFFE0F2FE), AguaPrimaryDark, "Transf.")
        FormaPago.CREDITO -> Triple(RojoAlertaFondo, RojoAlerta, "Crédito")
        FormaPago.ABONO -> Triple(Color(0xFFF3E8FF), Color(0xFF7E22CE), "Abono")
        FormaPago.CANCELADO -> Triple(Color(0xFFDCFCE7), Color(0xFF15803D), "Cancelado")
    }
    ClayBadge(
        text = label,
        containerColor = fondo,
        contentColor = textoColor
    )
}

@Composable
private fun TarjetaGastoItemClay(
    gasto: GastoExtraEntity,
    onEliminar: () -> Unit
) {
    ClayCard(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 4.dp),
        containerColor = AmbarFondo,
        accentBorderColor = AmbarAdvertencia,
        cornerRadius = 16.dp,
        elevation = 2.dp,
        contentPadding = PaddingValues(horizontal = 14.dp, vertical = 10.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(imageVector = Icons.Default.Fastfood, contentDescription = null, tint = AmbarAdvertencia, modifier = Modifier.size(18.dp))
                Spacer(modifier = Modifier.width(8.dp))
                Text(text = gasto.concepto, fontWeight = FontWeight.Bold, color = TextoOscuro)
            }
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = "-$${String.format(Locale.US, "%.2f", gasto.monto)}",
                    fontWeight = FontWeight.Black,
                    color = RojoAlerta
                )
                IconButton(onClick = onEliminar, modifier = Modifier.size(28.dp)) {
                    Icon(imageVector = Icons.Default.Delete, contentDescription = "Eliminar", tint = TextoSecundario, modifier = Modifier.size(16.dp))
                }
            }
        }
    }
}

@Composable
private fun DialogoAgregarGasto(
    onConfirmar: (concepto: String, monto: Double) -> Unit,
    onDismiss: () -> Unit
) {
    var concepto by remember { mutableStateOf("Almuerzo") }
    var montoTexto by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Registrar Gasto", fontWeight = FontWeight.Black) },
        text = {
            Column {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    listOf("Almuerzo", "Bebidas", "Comida", "Taller", "Combustible").forEach { chip ->
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
                    label = { Text("Concepto") },
                    singleLine = true,
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(
                    value = montoTexto,
                    onValueChange = { montoTexto = it },
                    label = { Text("Monto ($)") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    singleLine = true,
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val monto = montoTexto.replace(',', '.').toDoubleOrNull() ?: 0.0
                    onConfirmar(concepto, monto)
                }
            ) {
                Text("Guardar")
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
private fun EstadoVacioRegistroClay() {
    ClayCard(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 12.dp),
        cornerRadius = 22.dp,
        contentPadding = PaddingValues(28.dp)
    ) {
        Column(
            modifier = Modifier.fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(
                imageVector = Icons.AutoMirrored.Filled.ReceiptLong,
                contentDescription = null,
                tint = TextoSecundario,
                modifier = Modifier.size(44.dp)
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "Sin entregas registradas hoy",
                fontWeight = FontWeight.Black,
                fontSize = 16.sp,
                color = TextoOscuro
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "Pulsa \"Nueva Entrega\" para anotar la primera venta.",
                fontSize = 13.sp,
                color = TextoSecundario,
                textAlign = TextAlign.Center
            )
        }
    }
}

private fun compartirReporteWhatsApp(context: Context, textoReporte: String) {
    val intent = Intent(Intent.ACTION_SEND).apply {
        type = "text/plain"
        putExtra(Intent.EXTRA_TEXT, textoReporte)
        setPackage("com.whatsapp")
    }
    try {
        context.startActivity(intent)
    } catch (_: Exception) {
        val chooser = Intent.createChooser(
            Intent(Intent.ACTION_SEND).apply {
                type = "text/plain"
                putExtra(Intent.EXTRA_TEXT, textoReporte)
            },
            "Compartir reporte"
        )
        context.startActivity(chooser)
    }
}

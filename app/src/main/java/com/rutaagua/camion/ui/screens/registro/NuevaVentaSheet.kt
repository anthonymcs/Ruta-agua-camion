package com.rutaagua.camion.ui.screens.registro

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.CreditCard
import androidx.compose.material.icons.filled.DoneAll
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Money
import androidx.compose.material.icons.filled.Payments
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableDoubleStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.rutaagua.camion.data.local.entity.ClienteEntity
import com.rutaagua.camion.data.local.entity.InventarioCamionEntity
import com.rutaagua.camion.data.local.entity.TransaccionVentaEntity
import com.rutaagua.camion.data.local.model.FormaPago
import com.rutaagua.camion.data.local.model.TipoBidon
import com.rutaagua.camion.ui.components.BidonIllustration
import com.rutaagua.camion.ui.components.ClayBadge
import com.rutaagua.camion.ui.components.ClayButton
import com.rutaagua.camion.ui.components.ClayCard
import com.rutaagua.camion.ui.components.ClayEditableCounter
import com.rutaagua.camion.ui.components.CuentasBancariasDialog
import com.rutaagua.camion.ui.components.ClayInsetBox
import com.rutaagua.camion.ui.components.GalonIllustration
import com.rutaagua.camion.ui.components.MiniIllustration
import com.rutaagua.camion.ui.components.PacaIllustration
import com.rutaagua.camion.ui.components.marineOutlinedTextFieldColors
import com.rutaagua.camion.ui.theme.AguaPrimary
import com.rutaagua.camion.ui.theme.AguaPrimaryDark
import com.rutaagua.camion.ui.theme.AmbarAdvertencia
import com.rutaagua.camion.ui.theme.AmbarFondo
import com.rutaagua.camion.ui.theme.BidonAzulColor
import com.rutaagua.camion.ui.theme.BidonMarcadoColor
import com.rutaagua.camion.ui.theme.BidonTransparenteColor
import com.rutaagua.camion.ui.theme.ClayInsetBackground
import com.rutaagua.camion.ui.theme.ClayRimShadow
import com.rutaagua.camion.ui.theme.RojoAlerta
import com.rutaagua.camion.ui.theme.TextoOscuro
import com.rutaagua.camion.ui.theme.TextoSecundario
import com.rutaagua.camion.ui.theme.VerdeEfectivo
import com.rutaagua.camion.ui.theme.VerdeEfectivoFondo
import java.util.Locale
import kotlin.math.abs

@Composable
fun NuevaVentaSheet(
    clientes: List<ClienteEntity>,
    clientePreseleccionado: ClienteEntity?,
    transaccionAEditar: TransaccionVentaEntity? = null,
    inventario: InventarioCamionEntity?,
    onConfirmarVenta: (
        clienteId: Long?,
        nombrePaso: String?,
        tipoBidon: TipoBidon,
        cantRecarga: Int,
        precioBidon: Double,
        cantPrestados: Int,
        cantDevueltos: Int,
        cantVendidosEnvase: Int,
        precioEnvaseVendido: Double,
        cantGalones: Int,
        cantPacas: Int,
        cantMinis: Int,
        formaPago: FormaPago,
        montoPagado: Double,
        nota: String,
        recargaTransparentes: Int,
        recargaAzules: Int,
        recargaMarcados: Int,
        montoEfectivo: Double,
        montoTransferencia: Double,
        montoCredito: Double,
        montoAbono: Double,
        montoCanceladoDeuda: Double,
        prestadosTransparentes: Int,
        prestadosAzules: Int,
        prestadosMarcados: Int,
        cantidadBidonesGratis: Int,
        totalPersonalizado: Double?,
        precioUnitarioGalon: Double,
        precioUnitarioPaca: Double,
        precioUnitarioMini: Double
    ) -> Unit,
    onCancelar: () -> Unit
) {
    // 1. CLIENTE
    val clienteInicial = clientePreseleccionado ?: transaccionAEditar?.clienteId?.let { id ->
        clientes.find { it.id == id }
    }

    var clienteSeleccionado by remember { mutableStateOf(clienteInicial) }
    var textoBusquedaCliente by remember {
        mutableStateOf(clienteInicial?.nombreVisual ?: transaccionAEditar?.nombreClientePaso ?: "")
    }
    var esClienteDePaso by remember { mutableStateOf(clienteInicial == null) }

    // 2. RECARGAS (CON ENVASE VACÍO DE RETORNO)
    var tipoBidonSeleccionado by remember {
        mutableStateOf(transaccionAEditar?.tipoBidon ?: clienteInicial?.tipoBidonPreferido ?: TipoBidon.AZUL)
    }

    var precioUnitarioBidon by remember {
        mutableDoubleStateOf(transaccionAEditar?.precioUnitarioBidon ?: clienteInicial?.precioHabitual ?: 1.00)
    }

    val inicioTrans = transaccionAEditar?.recargaTransparentes
        ?: if (transaccionAEditar?.tipoBidon == TipoBidon.TRANSPARENTE) transaccionAEditar.cantidadBidonesRecarga else 0
    val inicioAzul = transaccionAEditar?.recargaAzules
        ?: if (transaccionAEditar?.tipoBidon == TipoBidon.AZUL) transaccionAEditar.cantidadBidonesRecarga
        else if (transaccionAEditar == null && (clienteInicial?.tipoBidonPreferido == TipoBidon.AZUL || clienteInicial == null)) 1 else 0
    val inicioMarc = transaccionAEditar?.recargaMarcados
        ?: if (transaccionAEditar?.tipoBidon == TipoBidon.MARCADO) transaccionAEditar.cantidadBidonesRecarga else 0

    var cantRecargaTrans by remember { mutableIntStateOf(inicioTrans) }
    var cantRecargaAzul by remember { mutableIntStateOf(inicioAzul) }
    var cantRecargaMarc by remember { mutableIntStateOf(inicioMarc) }

    val cantRecarga = cantRecargaTrans + cantRecargaAzul + cantRecargaMarc

    // 3. BIDONES PRESTADOS (BAJAN LLENOS DEL CAMIÓN Y SE COBRAN HOY, PERO EL ENVASE SE RECOGE DESPUÉS)
    var cantPrestados by remember {
        mutableIntStateOf(transaccionAEditar?.cantidadBidonesPrestados ?: 0)
    }
    var tipoBidonPrestado by remember {
        mutableStateOf(
            when {
                (transaccionAEditar?.prestadosTransparentes ?: 0) > 0 -> TipoBidon.TRANSPARENTE
                (transaccionAEditar?.prestadosMarcados ?: 0) > 0 -> TipoBidon.MARCADO
                (transaccionAEditar?.prestadosAzules ?: 0) > 0 -> TipoBidon.AZUL
                else -> clienteInicial?.tipoBidonPreferido ?: TipoBidon.AZUL
            }
        )
    }

    // 4. CORTESÍA / GRATIS (EJ. "35+1": BAJA DEL CAMIÓN PERO NO SE COBRA)
    var cantGratis by remember {
        mutableIntStateOf(transaccionAEditar?.cantidadBidonesGratis ?: 0)
    }
    var mostrarCuentasBancarias by remember { mutableStateOf(false) }

    // 5. DEVUELTOS Y ENVASE NUEVO
    var cantDevueltos by remember {
        mutableIntStateOf(transaccionAEditar?.cantidadBidonesDevueltos ?: 0)
    }
    var cantVendidosEnvase by remember {
        mutableIntStateOf(transaccionAEditar?.cantidadBidonesVendidosEnvase ?: 0)
    }
    var precioEnvaseVendido by remember {
        mutableDoubleStateOf(
            if (transaccionAEditar != null && transaccionAEditar.precioUnitarioBidonVendido > 0)
                transaccionAEditar.precioUnitarioBidonVendido
            else 5.50
        )
    }

    // 6. OTROS PRODUCTOS (PRECIOS EDITABLES POR CLIENTE / NEGOCIACIÓN)
    var cantGalones by remember { mutableIntStateOf(transaccionAEditar?.cantidadGalones ?: 0) }
    var cantPacas by remember { mutableIntStateOf(transaccionAEditar?.cantidadPacas ?: 0) }
    var cantMinis by remember { mutableIntStateOf(transaccionAEditar?.cantidadMinis ?: 0) }

    var precioUnitarioGalon by remember {
        mutableDoubleStateOf(if (transaccionAEditar != null && transaccionAEditar.precioUnitarioGalon > 0) transaccionAEditar.precioUnitarioGalon else 0.75)
    }
    var precioUnitarioPaca by remember {
        mutableDoubleStateOf(if (transaccionAEditar != null && transaccionAEditar.precioUnitarioPaca > 0) transaccionAEditar.precioUnitarioPaca else 3.75)
    }
    var precioUnitarioMini by remember {
        mutableDoubleStateOf(if (transaccionAEditar != null && transaccionAEditar.precioUnitarioMini > 0) transaccionAEditar.precioUnitarioMini else 1.90)
    }

    var mostrarDialogoPrecioBidon by remember { mutableStateOf(false) }
    var textoNuevoPrecioBidon by remember { mutableStateOf("") }
    var productoAEditarPrecio by remember { mutableStateOf<Triple<String, Double, (Double) -> Unit>?>(null) }
    var textoNuevoPrecioProducto by remember { mutableStateOf("") }

    // 7. CÁLCULO DE TOTALES (RECARGAS + PRESTADOS SE COBRAN, MENOS GRATIS)
    val totalAguaEntregada = cantRecarga + cantPrestados
    val bidonesAguaACobrar = maxOf(0, totalAguaEntregada - cantGratis)
    val totalAgua = bidonesAguaACobrar * precioUnitarioBidon
    val totalEnvaseVendido = cantVendidosEnvase * precioEnvaseVendido
    val totalGalones = cantGalones * precioUnitarioGalon
    val totalPacas = cantPacas * precioUnitarioPaca
    val totalMinis = cantMinis * precioUnitarioMini
    val totalCalculado = totalAgua + totalEnvaseVendido + totalGalones + totalPacas + totalMinis

    var usarTotalManual by remember { mutableStateOf(false) }
    var totalManualTexto by remember { mutableStateOf("") }
    val totalGeneral = if (usarTotalManual) {
        totalManualTexto.replace(',', '.').toDoubleOrNull() ?: totalCalculado
    } else {
        totalCalculado
    }

    // 8. FORMA DE PAGO Y PAGO COMBINADO
    var modoPagoMixto by remember {
        mutableStateOf(
            if (transaccionAEditar != null) {
                listOf(
                    transaccionAEditar.montoEfectivo > 0,
                    transaccionAEditar.montoTransferencia > 0,
                    transaccionAEditar.montoCredito > 0,
                    transaccionAEditar.montoAbono > 0
                ).count { it } > 1
            } else false
        )
    }

    var formaPago by remember {
        mutableStateOf(transaccionAEditar?.formaPagoPrincipal ?: FormaPago.EFECTIVO)
    }
    var montoAbonoTexto by remember {
        mutableStateOf(
            when (transaccionAEditar?.formaPagoPrincipal) {
                FormaPago.ABONO -> transaccionAEditar.montoAbono.toString()
                FormaPago.CANCELADO -> transaccionAEditar.montoCanceladoDeuda.toString()
                else -> ""
            }
        )
    }

    var pagoMixtoEfectivo by remember {
        mutableStateOf(if (transaccionAEditar != null && transaccionAEditar.montoEfectivo > 0) transaccionAEditar.montoEfectivo.toString() else "")
    }
    var pagoMixtoTransferencia by remember {
        mutableStateOf(if (transaccionAEditar != null && transaccionAEditar.montoTransferencia > 0) transaccionAEditar.montoTransferencia.toString() else "")
    }
    var pagoMixtoCredito by remember {
        mutableStateOf(if (transaccionAEditar != null && transaccionAEditar.montoCredito > 0) transaccionAEditar.montoCredito.toString() else "")
    }
    var pagoMixtoAbonoDeuda by remember {
        mutableStateOf(if (transaccionAEditar != null && transaccionAEditar.montoAbono > 0) transaccionAEditar.montoAbono.toString() else "")
    }

    var nota by remember { mutableStateOf(transaccionAEditar?.nota ?: "") }
    val esEdicion = transaccionAEditar != null

    LazyColumn(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 18.dp, vertical = 6.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // Cabecera limpia
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = if (esEdicion) "Editar Entrega" else "Nueva Entrega",
                    fontSize = 22.sp,
                    fontWeight = FontWeight.Black,
                    color = TextoOscuro
                )
                IconButton(onClick = onCancelar) {
                    Icon(imageVector = Icons.Default.Close, contentDescription = "Cerrar")
                }
            }
        }

        // 1. CLIENTE
        item {
            ClayCard(
                modifier = Modifier.fillMaxWidth(),
                cornerRadius = 20.dp,
                contentPadding = PaddingValues(14.dp)
            ) {
                OutlinedTextField(
                    value = textoBusquedaCliente,
                    onValueChange = {
                        textoBusquedaCliente = it
                        if (clienteSeleccionado != null && it != clienteSeleccionado?.nombreVisual && it != clienteSeleccionado?.nombre) {
                            clienteSeleccionado = null
                            esClienteDePaso = true
                        }
                    },
                    placeholder = { Text("Nombre del cliente...") },
                    leadingIcon = {
                        Icon(
                            imageVector = if (clienteSeleccionado != null) Icons.Default.Person else Icons.Default.Search,
                            contentDescription = null,
                            tint = if (clienteSeleccionado != null) VerdeEfectivo else AguaPrimary
                        )
                    },
                    singleLine = true,
                    shape = RoundedCornerShape(14.dp),
                    colors = marineOutlinedTextFieldColors(accentColor = AguaPrimary),
                    modifier = Modifier.fillMaxWidth()
                )

                val sugerencias = remember(textoBusquedaCliente, clientes, clienteSeleccionado) {
                    if (clienteSeleccionado == null && textoBusquedaCliente.isNotBlank()) {
                        clientes.filter {
                            it.nombre.contains(textoBusquedaCliente, ignoreCase = true) ||
                                    it.alias.contains(textoBusquedaCliente, ignoreCase = true) ||
                                    it.ubicacion.contains(textoBusquedaCliente, ignoreCase = true)
                        }.take(4)
                    } else emptyList()
                }

                if (sugerencias.isNotEmpty()) {
                    Spacer(modifier = Modifier.height(8.dp))
                    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        sugerencias.forEach { cliente ->
                            ClayInsetBox(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable {
                                        clienteSeleccionado = cliente
                                        textoBusquedaCliente = cliente.nombreVisual
                                        esClienteDePaso = false
                                        tipoBidonSeleccionado = cliente.tipoBidonPreferido
                                        tipoBidonPrestado = cliente.tipoBidonPreferido
                                        precioUnitarioBidon = cliente.precioHabitual
                                    },
                                cornerRadius = 12.dp,
                                contentPadding = PaddingValues(10.dp)
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    BidonIllustration(tipo = cliente.tipoBidonPreferido, size = 26.dp)
                                    Spacer(modifier = Modifier.width(10.dp))
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(text = cliente.nombreVisual, fontWeight = FontWeight.Bold, color = TextoOscuro)
                                        val subInfo = if (cliente.alias.isNotBlank()) {
                                            "${cliente.nombre} • ${cliente.ubicacion} • $${String.format(Locale.US, "%.2f", cliente.precioHabitual)}"
                                        } else {
                                            "${cliente.ubicacion} • $${String.format(Locale.US, "%.2f", cliente.precioHabitual)}"
                                        }
                                        Text(
                                            text = subInfo,
                                            fontSize = 12.sp,
                                            color = TextoSecundario,
                                            maxLines = 1,
                                            overflow = TextOverflow.Ellipsis
                                        )
                                    }
                                }
                            }
                        }
                    }
                }

                clienteSeleccionado?.let { cliente ->
                    if (cliente.deudaMonetaria > 0.0 || cliente.bidonesPrestados > 0) {
                        Spacer(modifier = Modifier.height(8.dp))
                        Surface(
                            color = AmbarFondo,
                            shape = RoundedCornerShape(12.dp),
                            border = BorderStroke(1.dp, AmbarAdvertencia.copy(alpha = 0.35f)),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "Saldo actual:",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 12.sp,
                                    color = AmbarAdvertencia
                                )
                                Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                                    if (cliente.deudaMonetaria > 0.0) {
                                        Text(
                                            text = "Debe $${String.format(Locale.US, "%.2f", cliente.deudaMonetaria)}",
                                            fontWeight = FontWeight.Black,
                                            fontSize = 12.sp,
                                            color = RojoAlerta
                                        )
                                    }
                                    if (cliente.bidonesPrestados > 0) {
                                        Text(
                                            text = "${cliente.bidonesPrestados} prestados",
                                            fontWeight = FontWeight.Black,
                                            fontSize = 12.sp,
                                            color = TextoOscuro
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        // 2. RECARGAS DE 20L (CON RETORNO DE VACÍO)
        item {
            ClayCard(
                modifier = Modifier.fillMaxWidth(),
                cornerRadius = 20.dp,
                contentPadding = PaddingValues(14.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Recargas (Recibe vacío)",
                        fontWeight = FontWeight.Black,
                        fontSize = 15.sp,
                        color = TextoOscuro
                    )
                    ClayBadge(
                        text = "$cantRecarga recargas",
                        containerColor = Color(0xFFDBEAFE),
                        contentColor = AguaPrimaryDark
                    )
                }

                Spacer(modifier = Modifier.height(10.dp))

                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    FilaBidonClay(
                        tipo = TipoBidon.TRANSPARENTE,
                        nombre = "Transparente (PET)",
                        disponibles = inventario?.bidonesLlenosTransparentes ?: 0,
                        cantidad = cantRecargaTrans,
                        accentColor = BidonTransparenteColor,
                        onCambio = {
                            cantRecargaTrans = it
                            if (it > 0) {
                                tipoBidonSeleccionado = TipoBidon.TRANSPARENTE
                                if (cantPrestados == 0) tipoBidonPrestado = TipoBidon.TRANSPARENTE
                            }
                        }
                    )

                    FilaBidonClay(
                        tipo = TipoBidon.AZUL,
                        nombre = "Azul Estándar",
                        disponibles = inventario?.bidonesLlenosAzules ?: 0,
                        cantidad = cantRecargaAzul,
                        accentColor = BidonAzulColor,
                        onCambio = {
                            cantRecargaAzul = it
                            if (it > 0) {
                                tipoBidonSeleccionado = TipoBidon.AZUL
                                if (cantPrestados == 0) tipoBidonPrestado = TipoBidon.AZUL
                            }
                        }
                    )

                    FilaBidonClay(
                        tipo = TipoBidon.MARCADO,
                        nombre = "Marcado / Sello",
                        disponibles = inventario?.bidonesLlenosMarcados ?: 0,
                        cantidad = cantRecargaMarc,
                        accentColor = BidonMarcadoColor,
                        onCambio = {
                            cantRecargaMarc = it
                            if (it > 0) {
                                tipoBidonSeleccionado = TipoBidon.MARCADO
                                if (cantPrestados == 0) tipoBidonPrestado = TipoBidon.MARCADO
                            }
                        }
                    )
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Selector de precio unitario para bidones (con 0.80, 0.90 y precio personalizado)
                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Precio Bidón: $${String.format(Locale.US, "%.2f", precioUnitarioBidon)} c/u",
                            fontWeight = FontWeight.Black,
                            fontSize = 13.sp,
                            color = AguaPrimaryDark
                        )
                        Surface(
                            onClick = {
                                textoNuevoPrecioBidon = String.format(Locale.US, "%.2f", precioUnitarioBidon)
                                mostrarDialogoPrecioBidon = true
                            },
                            color = AguaPrimary.copy(alpha = 0.12f),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(Icons.Default.Edit, contentDescription = null, tint = AguaPrimary, modifier = Modifier.size(12.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("Personalizar $", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = AguaPrimary)
                            }
                        }
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        listOf(0.75, 0.80, 0.85, 0.90, 1.00, 1.25).forEach { precio ->
                            val sel = abs(precioUnitarioBidon - precio) < 0.001
                            FilterChip(
                                selected = sel,
                                onClick = { precioUnitarioBidon = precio },
                                label = { Text("$${String.format(Locale.US, "%.2f", precio)}", fontWeight = FontWeight.Bold, fontSize = 11.sp) },
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = AguaPrimary,
                                    selectedLabelColor = Color.White
                                ),
                                modifier = Modifier.weight(1f)
                            )
                        }
                    }
                }
            }
        }

        // 3. BIDONES PRESTADOS, CORTESÍAS ("35+1"), DEVUELTOS Y ENVASE NUEVO
        item {
            ClayCard(
                modifier = Modifier.fillMaxWidth(),
                cornerRadius = 20.dp,
                contentPadding = PaddingValues(14.dp)
            ) {
                Text(
                    text = "Préstamos, Cortesías (35+1) y Envases",
                    fontWeight = FontWeight.Black,
                    fontSize = 15.sp,
                    color = TextoOscuro
                )

                Spacer(modifier = Modifier.height(10.dp))

                // A. Bidones Prestados (Se cobran hoy + Descuentan carga del camión + Queda debiendo envase)
                ClayInsetBox(
                    modifier = Modifier.fillMaxWidth(),
                    cornerRadius = 14.dp,
                    contentPadding = PaddingValues(12.dp)
                ) {
                    Column {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = "Bidones Prestados Hoy",
                                    fontWeight = FontWeight.Black,
                                    fontSize = 14.sp,
                                    color = AmbarAdvertencia
                                )
                                Text(
                                    text = "Se cobran hoy y descuentan carga del camión",
                                    fontSize = 11.sp,
                                    color = TextoSecundario
                                )
                            }
                            ClayEditableCounter(
                                valor = cantPrestados,
                                onCambio = { cantPrestados = it },
                                accentColor = AmbarAdvertencia
                            )
                        }

                        AnimatedVisibility(visible = cantPrestados > 0) {
                            Column(modifier = Modifier.padding(top = 8.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = "Tipo prestado:",
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = TextoSecundario
                                    )
                                    TipoBidon.entries.forEach { t ->
                                        val sel = tipoBidonPrestado == t
                                        val label = when (t) {
                                            TipoBidon.TRANSPARENTE -> "PET"
                                            TipoBidon.AZUL -> "Azul"
                                            TipoBidon.MARCADO -> "Marcado"
                                        }
                                        FilterChip(
                                            selected = sel,
                                            onClick = { tipoBidonPrestado = t },
                                            label = { Text(label, fontSize = 11.sp, fontWeight = FontWeight.Bold) },
                                            modifier = Modifier.weight(1f)
                                        )
                                    }
                                }
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                // B. Bidón Gratis / Cortesía (Ej. 35+1)
                ClayInsetBox(
                    modifier = Modifier.fillMaxWidth(),
                    cornerRadius = 14.dp,
                    contentPadding = PaddingValues(12.dp)
                ) {
                    Column {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = "Bidón Gratis / Cortesía (Ej. 35+1)",
                                    fontWeight = FontWeight.Black,
                                    fontSize = 14.sp,
                                    color = AguaPrimaryDark
                                )
                                Text(
                                    text = "Incluido en los $totalAguaEntregada entregados, no se cobra",
                                    fontSize = 11.sp,
                                    color = TextoSecundario
                                )
                            }
                            ClayEditableCounter(
                                valor = cantGratis,
                                onCambio = { cantGratis = it.coerceAtMost(maxOf(1, totalAguaEntregada)) },
                                accentColor = AguaPrimary
                            )
                        }

                        if (cantGratis > 0 && totalAguaEntregada > 0) {
                            Spacer(modifier = Modifier.height(6.dp))
                            Surface(
                                color = Color(0xFFDBEAFE),
                                shape = RoundedCornerShape(10.dp),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Text(
                                    text = "Anote: ${bidonesAguaACobrar}+${cantGratis}  •  Bajan $totalAguaEntregada del camión, se cobran $bidonesAguaACobrar ($${String.format(Locale.US, "%.2f", totalAgua)})",
                                    fontWeight = FontWeight.Black,
                                    fontSize = 12.sp,
                                    color = AguaPrimaryDark,
                                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                // C. Envases Vacíos Devueltos de préstamos anteriores
                ClayInsetBox(
                    modifier = Modifier.fillMaxWidth(),
                    cornerRadius = 14.dp,
                    contentPadding = PaddingValues(12.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "Envases Prestados Devueltos",
                                fontWeight = FontWeight.Bold,
                                fontSize = 14.sp,
                                color = VerdeEfectivo
                            )
                            Text(
                                text = "Recoge vacíos pendientes sin llevar agua",
                                fontSize = 11.sp,
                                color = TextoSecundario
                            )
                        }
                        ClayEditableCounter(
                            valor = cantDevueltos,
                            onCambio = { cantDevueltos = it },
                            accentColor = VerdeEfectivo
                        )
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                // D. Venta de Bidón con Todo (Envase Nuevo)
                ClayInsetBox(
                    modifier = Modifier.fillMaxWidth(),
                    cornerRadius = 14.dp,
                    contentPadding = PaddingValues(12.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "Bidón con Todo (Envase Nuevo)",
                                fontWeight = FontWeight.Bold,
                                fontSize = 14.sp,
                                color = TextoOscuro
                            )
                            Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                FilterChip(
                                    selected = precioEnvaseVendido == 5.50,
                                    onClick = { precioEnvaseVendido = 5.50 },
                                    label = { Text("$5.50", fontSize = 11.sp, fontWeight = FontWeight.Bold) }
                                )
                                FilterChip(
                                    selected = precioEnvaseVendido == 6.00,
                                    onClick = { precioEnvaseVendido = 6.00 },
                                    label = { Text("$6.00", fontSize = 11.sp, fontWeight = FontWeight.Bold) }
                                )
                            }
                        }
                        ClayEditableCounter(
                            valor = cantVendidosEnvase,
                            onCambio = { cantVendidosEnvase = it },
                            accentColor = AguaPrimary
                        )
                    }
                }
            }
        }

        // 4. OTROS PRODUCTOS (GALONES, PACAS, MINIS)
        item {
            ClayCard(
                modifier = Modifier.fillMaxWidth(),
                cornerRadius = 20.dp,
                contentPadding = PaddingValues(14.dp)
            ) {
                Text(
                    text = "Galones, Pacas y Minis",
                    fontWeight = FontWeight.Black,
                    fontSize = 15.sp,
                    color = TextoOscuro
                )
                Spacer(modifier = Modifier.height(10.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    TarjetaSelectorSecundarioClay(
                        nombre = "Galón 4L",
                        precioUnitario = precioUnitarioGalon,
                        onEditarPrecio = {
                            textoNuevoPrecioProducto = String.format(Locale.US, "%.2f", precioUnitarioGalon)
                            productoAEditarPrecio = Triple("Galón 4L", precioUnitarioGalon) { precioUnitarioGalon = it }
                        },
                        disponibles = inventario?.galonesLlenos ?: 0,
                        valor = cantGalones,
                        ilustracion = { GalonIllustration(size = 32.dp) },
                        onCambio = { cantGalones = it },
                        modifier = Modifier.weight(1f)
                    )
                    TarjetaSelectorSecundarioClay(
                        nombre = "Paca 24x",
                        precioUnitario = precioUnitarioPaca,
                        onEditarPrecio = {
                            textoNuevoPrecioProducto = String.format(Locale.US, "%.2f", precioUnitarioPaca)
                            productoAEditarPrecio = Triple("Paca 24x", precioUnitarioPaca) { precioUnitarioPaca = it }
                        },
                        disponibles = inventario?.pacasLlenas ?: 0,
                        valor = cantPacas,
                        ilustracion = { PacaIllustration(size = 32.dp) },
                        onCambio = { cantPacas = it },
                        modifier = Modifier.weight(1f)
                    )
                    TarjetaSelectorSecundarioClay(
                        nombre = "Mini 12x",
                        precioUnitario = precioUnitarioMini,
                        onEditarPrecio = {
                            textoNuevoPrecioProducto = String.format(Locale.US, "%.2f", precioUnitarioMini)
                            productoAEditarPrecio = Triple("Mini 12x", precioUnitarioMini) { precioUnitarioMini = it }
                        },
                        disponibles = inventario?.minisLlenas ?: 0,
                        valor = cantMinis,
                        ilustracion = { MiniIllustration(size = 32.dp) },
                        onCambio = { cantMinis = it },
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        }

        // 5. TOTAL A COBRAR (CON OPCIÓN DE AJUSTE MANUAL) Y FORMA DE PAGO
        item {
            ClayCard(
                modifier = Modifier.fillMaxWidth(),
                containerColor = VerdeEfectivoFondo,
                accentBorderColor = VerdeEfectivo,
                cornerRadius = 22.dp,
                contentPadding = PaddingValues(16.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = if (cantGratis > 0) {
                                "TOTAL A COBRAR (${bidonesAguaACobrar}+${cantGratis} bidones)"
                            } else if (cantPrestados > 0) {
                                "TOTAL A COBRAR ($cantRecarga rec. + $cantPrestados prest.)"
                            } else {
                                "TOTAL A COBRAR"
                            },
                            fontWeight = FontWeight.Black,
                            fontSize = 12.sp,
                            color = VerdeEfectivo,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        Text(
                            text = "$${String.format(Locale.US, "%.2f", totalGeneral)}",
                            fontSize = 32.sp,
                            fontWeight = FontWeight.Black,
                            color = VerdeEfectivo,
                            maxLines = 1
                        )
                    }

                    Spacer(modifier = Modifier.width(8.dp))

                    Surface(
                        onClick = {
                            usarTotalManual = !usarTotalManual
                            if (usarTotalManual && totalManualTexto.isBlank()) {
                                totalManualTexto = String.format(Locale.US, "%.2f", totalCalculado)
                            }
                        },
                        color = Color.White,
                        shape = RoundedCornerShape(12.dp),
                        border = BorderStroke(1.dp, VerdeEfectivo.copy(alpha = 0.4f))
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.Edit,
                                contentDescription = null,
                                tint = VerdeEfectivo,
                                modifier = Modifier.size(14.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = if (usarTotalManual) "Automático" else "Editar Monto",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = VerdeEfectivo,
                                maxLines = 1,
                                softWrap = false
                            )
                        }
                    }
                }

                AnimatedVisibility(visible = usarTotalManual) {
                    Column(modifier = Modifier.padding(top = 10.dp)) {
                        OutlinedTextField(
                            value = totalManualTexto,
                            onValueChange = { totalManualTexto = it },
                            label = { Text("Monto total personalizado ($)") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                            singleLine = true,
                            shape = RoundedCornerShape(12.dp),
                            colors = marineOutlinedTextFieldColors(accentColor = VerdeEfectivo),
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                }
            }
        }

        // 6. FORMA DE PAGO
        item {
            ClayCard(
                modifier = Modifier.fillMaxWidth(),
                cornerRadius = 20.dp,
                contentPadding = PaddingValues(14.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Forma de Pago",
                        fontWeight = FontWeight.Black,
                        fontSize = 15.sp,
                        color = TextoOscuro
                    )

                    FilterChip(
                        selected = modoPagoMixto,
                        onClick = {
                            modoPagoMixto = !modoPagoMixto
                            if (modoPagoMixto && pagoMixtoEfectivo.isBlank() && pagoMixtoCredito.isBlank()) {
                                pagoMixtoEfectivo = String.format(Locale.US, "%.2f", totalGeneral)
                            }
                        },
                        label = {
                            Text(
                                text = if (modoPagoMixto) "Pago Combinado Activo" else "+ Combinar Pagos",
                                fontWeight = FontWeight.Bold,
                                fontSize = 11.sp
                            )
                        }
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))

                if (!modoPagoMixto) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        BotonFormaPagoClay(
                            titulo = "Efectivo",
                            icono = Icons.Default.Money,
                            seleccionado = formaPago == FormaPago.EFECTIVO,
                            onSelect = { formaPago = FormaPago.EFECTIVO },
                            modifier = Modifier.weight(1f)
                        )
                        BotonFormaPagoClay(
                            titulo = "Transf.",
                            icono = Icons.Default.CreditCard,
                            seleccionado = formaPago == FormaPago.TRANSFERENCIA,
                            onSelect = { formaPago = FormaPago.TRANSFERENCIA },
                            modifier = Modifier.weight(1f)
                        )
                        BotonFormaPagoClay(
                            titulo = "Crédito",
                            icono = Icons.Default.Payments,
                            seleccionado = formaPago == FormaPago.CREDITO,
                            onSelect = { formaPago = FormaPago.CREDITO },
                            modifier = Modifier.weight(1f)
                        )
                    }

                    Spacer(modifier = Modifier.height(6.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        BotonFormaPagoClay(
                            titulo = "Abono",
                            icono = Icons.Default.Payments,
                            seleccionado = formaPago == FormaPago.ABONO,
                            onSelect = {
                                formaPago = FormaPago.ABONO
                                if (montoAbonoTexto.isBlank()) montoAbonoTexto = String.format(Locale.US, "%.2f", totalGeneral)
                            },
                            modifier = Modifier.weight(1f)
                        )
                        BotonFormaPagoClay(
                            titulo = "Cancelado",
                            icono = Icons.Default.DoneAll,
                            seleccionado = formaPago == FormaPago.CANCELADO,
                            onSelect = {
                                formaPago = FormaPago.CANCELADO
                                val deudaPrevia = clienteSeleccionado?.deudaMonetaria ?: 0.0
                                montoAbonoTexto = String.format(Locale.US, "%.2f", totalGeneral + deudaPrevia)
                            },
                            modifier = Modifier.weight(1f)
                        )
                    }

                    if (formaPago == FormaPago.ABONO || formaPago == FormaPago.CANCELADO) {
                        Spacer(modifier = Modifier.height(8.dp))
                        OutlinedTextField(
                            value = montoAbonoTexto,
                            onValueChange = { montoAbonoTexto = it },
                            label = { Text("Monto pagado / abonado ($)") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                            singleLine = true,
                            shape = RoundedCornerShape(12.dp),
                            colors = marineOutlinedTextFieldColors(accentColor = AguaPrimary),
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                } else {
                    ClayInsetBox(
                        modifier = Modifier.fillMaxWidth(),
                        cornerRadius = 14.dp,
                        contentPadding = PaddingValues(12.dp)
                    ) {
                        Column {
                            Text(
                                text = "Distribuir Total ($${String.format(Locale.US, "%.2f", totalGeneral)}):",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Black,
                                color = AguaPrimaryDark
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                OutlinedTextField(
                                    value = pagoMixtoEfectivo,
                                    onValueChange = { pagoMixtoEfectivo = it },
                                    label = { Text("Efectivo ($)") },
                                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                                    singleLine = true,
                                    shape = RoundedCornerShape(12.dp),
                                    colors = marineOutlinedTextFieldColors(accentColor = AguaPrimary),
                                    modifier = Modifier.weight(1f)
                                )
                                OutlinedTextField(
                                    value = pagoMixtoCredito,
                                    onValueChange = { pagoMixtoCredito = it },
                                    label = { Text("Crédito / Fiado ($)") },
                                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                                    singleLine = true,
                                    shape = RoundedCornerShape(12.dp),
                                    colors = marineOutlinedTextFieldColors(accentColor = AguaPrimary),
                                    modifier = Modifier.weight(1f)
                                )
                            }
                            Spacer(modifier = Modifier.height(8.dp))
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                OutlinedTextField(
                                    value = pagoMixtoTransferencia,
                                    onValueChange = { pagoMixtoTransferencia = it },
                                    label = { Text("Transferencia ($)") },
                                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                                    singleLine = true,
                                    shape = RoundedCornerShape(12.dp),
                                    colors = marineOutlinedTextFieldColors(accentColor = AguaPrimary),
                                    modifier = Modifier.weight(1f)
                                )
                                OutlinedTextField(
                                    value = pagoMixtoAbonoDeuda,
                                    onValueChange = { pagoMixtoAbonoDeuda = it },
                                    label = { Text("Abono Deuda ($)") },
                                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                                    singleLine = true,
                                    shape = RoundedCornerShape(12.dp),
                                    colors = marineOutlinedTextFieldColors(accentColor = AguaPrimary),
                                    modifier = Modifier.weight(1f)
                                )
                            }

                            val sumaCubierta = (pagoMixtoEfectivo.replace(',', '.').toDoubleOrNull() ?: 0.0) +
                                    (pagoMixtoCredito.replace(',', '.').toDoubleOrNull() ?: 0.0) +
                                    (pagoMixtoTransferencia.replace(',', '.').toDoubleOrNull() ?: 0.0)
                            val diferencia = totalGeneral - sumaCubierta

                            Spacer(modifier = Modifier.height(6.dp))
                            if (abs(diferencia) > 0.01) {
                                Text(
                                    text = if (diferencia > 0) "Falta asignar: $${String.format(Locale.US, "%.2f", diferencia)}"
                                    else "Exceso asignado: $${String.format(Locale.US, "%.2f", -diferencia)}",
                                    fontSize = 11.sp,
                                    color = AmbarAdvertencia,
                                    fontWeight = FontWeight.Bold
                                )
                            } else {
                                Text(
                                    text = "Monto cuadrado ($${String.format(Locale.US, "%.2f", totalGeneral)})",
                                    fontSize = 11.sp,
                                    color = VerdeEfectivo,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                // Botón rápido para enviar tarjeta de cuenta bancaria (Pichincha, Pacífico, Bolivariano, Produbanco, Guayaquil) por WhatsApp
                Surface(
                    onClick = { mostrarCuentasBancarias = true },
                    color = if (formaPago == FormaPago.TRANSFERENCIA) Color(0xFFDBEAFE) else Color(0xFFF1F5F9),
                    shape = RoundedCornerShape(12.dp),
                    border = BorderStroke(
                        1.dp,
                        if (formaPago == FormaPago.TRANSFERENCIA) AguaPrimary else Color(0xFFCBD5E1)
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(40.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 12.dp),
                        horizontalArrangement = Arrangement.Center,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.CreditCard,
                            contentDescription = null,
                            tint = AguaPrimaryDark,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "Enviar Cuenta Bancaria por WhatsApp (Imagen)",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Black,
                            color = AguaPrimaryDark,
                            maxLines = 1,
                            softWrap = false,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }
            }
        }

        // Botón Confirmar Clay
        item {
            ClayButton(
                onClick = {
                    val pagado = when (formaPago) {
                        FormaPago.EFECTIVO, FormaPago.TRANSFERENCIA -> totalGeneral
                        FormaPago.CREDITO -> 0.0
                        FormaPago.ABONO, FormaPago.CANCELADO -> montoAbonoTexto.replace(',', '.').toDoubleOrNull() ?: totalGeneral
                    }

                    val nombreFinal = if (esClienteDePaso || clienteSeleccionado == null) {
                        if (textoBusquedaCliente.isBlank()) "Cliente de Paso" else textoBusquedaCliente.trim()
                    } else null

                    val mEf = if (modoPagoMixto) (pagoMixtoEfectivo.replace(',', '.').toDoubleOrNull() ?: 0.0) else 0.0
                    val mTr = if (modoPagoMixto) (pagoMixtoTransferencia.replace(',', '.').toDoubleOrNull() ?: 0.0) else 0.0
                    val mCr = if (modoPagoMixto) (pagoMixtoCredito.replace(',', '.').toDoubleOrNull() ?: 0.0) else 0.0
                    val mAb = if (modoPagoMixto) (pagoMixtoAbonoDeuda.replace(',', '.').toDoubleOrNull() ?: 0.0) else 0.0

                    val pTrans = if (tipoBidonPrestado == TipoBidon.TRANSPARENTE) cantPrestados else 0
                    val pAzul = if (tipoBidonPrestado == TipoBidon.AZUL) cantPrestados else 0
                    val pMarc = if (tipoBidonPrestado == TipoBidon.MARCADO) cantPrestados else 0

                    onConfirmarVenta(
                        clienteSeleccionado?.id,
                        nombreFinal,
                        tipoBidonSeleccionado,
                        cantRecarga,
                        precioUnitarioBidon,
                        cantPrestados,
                        cantDevueltos,
                        cantVendidosEnvase,
                        precioEnvaseVendido,
                        cantGalones,
                        cantPacas,
                        cantMinis,
                        formaPago,
                        pagado,
                        nota,
                        cantRecargaTrans,
                        cantRecargaAzul,
                        cantRecargaMarc,
                        mEf,
                        mTr,
                        mCr,
                        mAb,
                        0.0,
                        pTrans,
                        pAzul,
                        pMarc,
                        cantGratis,
                        if (usarTotalManual) totalGeneral else null,
                        precioUnitarioGalon,
                        precioUnitarioPaca,
                        precioUnitarioMini
                    )
                },
                containerColor = VerdeEfectivo,
                modifier = Modifier.fillMaxWidth(),
                height = 58.dp
            ) {
                Icon(imageVector = Icons.Default.Check, contentDescription = null)
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = if (esEdicion) "Guardar Cambios ($${String.format(Locale.US, "%.2f", totalGeneral)})"
                    else "Confirmar Entrega ($${String.format(Locale.US, "%.2f", totalGeneral)})",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Black
                )
            }
            Spacer(modifier = Modifier.height(28.dp))
        }
    }

    if (mostrarCuentasBancarias) {
        val nombreDest = clienteSeleccionado?.nombre
            ?: textoBusquedaCliente.takeIf { it.isNotBlank() }
        CuentasBancariasDialog(
            telefonoCliente = clienteSeleccionado?.telefono,
            nombreCliente = nombreDest,
            montoACobrar = totalGeneral.takeIf { it > 0.0 },
            onDismiss = { mostrarCuentasBancarias = false }
        )
    }

    if (mostrarDialogoPrecioBidon) {
        androidx.compose.material3.AlertDialog(
            onDismissRequest = { mostrarDialogoPrecioBidon = false },
            title = {
                Text(
                    text = "Precio por Bidón de 20L",
                    fontWeight = FontWeight.Black,
                    fontSize = 17.sp,
                    color = TextoOscuro
                )
            },
            text = {
                Column {
                    Text(
                        text = "Ingresa el precio unitario acordado para este despacho (ej. 0.80, 0.70):",
                        fontSize = 12.sp,
                        color = TextoSecundario
                    )
                    Spacer(modifier = Modifier.height(10.dp))
                    OutlinedTextField(
                        value = textoNuevoPrecioBidon,
                        onValueChange = { textoNuevoPrecioBidon = it },
                        label = { Text("Precio Unitario ($)") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                        singleLine = true,
                        shape = RoundedCornerShape(12.dp),
                        colors = marineOutlinedTextFieldColors(accentColor = AguaPrimary),
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                androidx.compose.material3.Button(
                    onClick = {
                        val p = textoNuevoPrecioBidon.replace(',', '.').toDoubleOrNull()
                        if (p != null && p > 0.0) {
                            precioUnitarioBidon = p
                        }
                        mostrarDialogoPrecioBidon = false
                    },
                    colors = androidx.compose.material3.ButtonDefaults.buttonColors(containerColor = AguaPrimary),
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Text("Aplicar Precio", fontWeight = FontWeight.Bold, color = Color.White)
                }
            },
            dismissButton = {
                androidx.compose.material3.TextButton(onClick = { mostrarDialogoPrecioBidon = false }) {
                    Text("Cancelar")
                }
            }
        )
    }

    productoAEditarPrecio?.let { (nombreProd, _, onActualizar) ->
        androidx.compose.material3.AlertDialog(
            onDismissRequest = { productoAEditarPrecio = null },
            title = {
                Text(
                    text = "Precio Unitario - $nombreProd",
                    fontWeight = FontWeight.Black,
                    fontSize = 17.sp,
                    color = TextoOscuro
                )
            },
            text = {
                Column {
                    Text(
                        text = "Modifica el precio unitario pactado para este cliente:",
                        fontSize = 12.sp,
                        color = TextoSecundario
                    )
                    Spacer(modifier = Modifier.height(10.dp))
                    OutlinedTextField(
                        value = textoNuevoPrecioProducto,
                        onValueChange = { textoNuevoPrecioProducto = it },
                        label = { Text("Precio Unitario ($)") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                        singleLine = true,
                        shape = RoundedCornerShape(12.dp),
                        colors = marineOutlinedTextFieldColors(accentColor = AguaPrimary),
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                androidx.compose.material3.Button(
                    onClick = {
                        val p = textoNuevoPrecioProducto.replace(',', '.').toDoubleOrNull()
                        if (p != null && p >= 0.0) {
                            onActualizar(p)
                        }
                        productoAEditarPrecio = null
                    },
                    colors = androidx.compose.material3.ButtonDefaults.buttonColors(containerColor = AguaPrimary),
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Text("Aplicar", fontWeight = FontWeight.Bold, color = Color.White)
                }
            },
            dismissButton = {
                androidx.compose.material3.TextButton(onClick = { productoAEditarPrecio = null }) {
                    Text("Cancelar")
                }
            }
        )
    }
}

@Composable
private fun FilaBidonClay(
    tipo: TipoBidon,
    nombre: String,
    disponibles: Int,
    cantidad: Int,
    accentColor: Color,
    onCambio: (Int) -> Unit
) {
    ClayInsetBox(
        modifier = Modifier.fillMaxWidth(),
        cornerRadius = 14.dp,
        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 8.dp)
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
                BidonIllustration(tipo = tipo, size = 34.dp)
                Spacer(modifier = Modifier.width(10.dp))
                Column {
                    Text(
                        text = nombre,
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp,
                        color = TextoOscuro
                    )
                    Text(
                        text = "En vehículo: $disponibles",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = if (disponibles <= 5) RojoAlerta else TextoSecundario
                    )
                }
            }

            ClayEditableCounter(
                valor = cantidad,
                onCambio = onCambio,
                accentColor = accentColor
            )
        }
    }
}

@Composable
private fun TarjetaSelectorSecundarioClay(
    nombre: String,
    precioUnitario: Double,
    onEditarPrecio: () -> Unit,
    disponibles: Int,
    valor: Int,
    ilustracion: @Composable () -> Unit,
    onCambio: (Int) -> Unit,
    modifier: Modifier = Modifier
) {
    ClayInsetBox(
        modifier = modifier,
        cornerRadius = 14.dp,
        contentPadding = PaddingValues(8.dp)
    ) {
        Column(
            modifier = Modifier.fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Box(modifier = Modifier.size(32.dp), contentAlignment = Alignment.Center) {
                ilustracion()
            }
            Text(text = nombre, fontSize = 12.sp, fontWeight = FontWeight.Bold, color = TextoOscuro)

            Surface(
                onClick = onEditarPrecio,
                shape = RoundedCornerShape(6.dp),
                color = Color.White.copy(alpha = 0.85f),
                border = BorderStroke(0.5.dp, AguaPrimary.copy(alpha = 0.35f)),
                modifier = Modifier.padding(vertical = 2.dp)
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 4.dp, vertical = 1.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "$${String.format(Locale.US, "%.2f", precioUnitario)}",
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Black,
                        color = AguaPrimaryDark
                    )
                    Spacer(modifier = Modifier.width(2.dp))
                    Icon(
                        imageVector = Icons.Default.Edit,
                        contentDescription = "Editar precio",
                        tint = AguaPrimary,
                        modifier = Modifier.size(9.dp)
                    )
                }
            }

            Text(text = "En unid: $disponibles", fontSize = 10.sp, color = TextoSecundario)
            Spacer(modifier = Modifier.height(4.dp))
            ClayEditableCounter(
                valor = valor,
                onCambio = onCambio,
                compact = true
            )
        }
    }
}

@Composable
private fun BotonFormaPagoClay(
    titulo: String,
    icono: androidx.compose.ui.graphics.vector.ImageVector,
    seleccionado: Boolean,
    onSelect: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        onClick = onSelect,
        modifier = modifier.height(46.dp),
        color = if (seleccionado) VerdeEfectivo else ClayInsetBackground,
        shape = RoundedCornerShape(14.dp),
        shadowElevation = if (seleccionado) 3.dp else 0.dp,
        border = BorderStroke(
            1.dp,
            if (seleccionado) Color.White.copy(alpha = 0.35f) else ClayRimShadow
        )
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 6.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            Icon(
                imageVector = icono,
                contentDescription = null,
                tint = if (seleccionado) Color.White else TextoOscuro,
                modifier = Modifier.size(16.dp)
            )
            Spacer(modifier = Modifier.width(4.dp))
            Text(
                text = titulo,
                color = if (seleccionado) Color.White else TextoOscuro,
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                maxLines = 1,
                softWrap = false,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

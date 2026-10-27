package com.rutaagua.camion.ui.screens.registro

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
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
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.CreditCard
import androidx.compose.material.icons.filled.DoneAll
import androidx.compose.material.icons.filled.Money
import androidx.compose.material.icons.filled.Payments
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.rutaagua.camion.data.local.entity.ClienteEntity
import com.rutaagua.camion.data.local.entity.InventarioCamionEntity
import com.rutaagua.camion.data.local.entity.TransaccionVentaEntity
import com.rutaagua.camion.data.local.model.FormaPago
import com.rutaagua.camion.data.local.model.TipoBidon
import com.rutaagua.camion.ui.components.BidonIllustration
import com.rutaagua.camion.ui.components.GalonIllustration
import com.rutaagua.camion.ui.components.MiniIllustration
import com.rutaagua.camion.ui.components.PacaIllustration
import com.rutaagua.camion.ui.theme.AguaPrimary
import com.rutaagua.camion.ui.theme.AguaPrimaryDark
import com.rutaagua.camion.ui.theme.AguaPrimaryLight
import com.rutaagua.camion.ui.theme.AmbarAdvertencia
import com.rutaagua.camion.ui.theme.AmbarFondo
import com.rutaagua.camion.ui.theme.RojoAlerta
import com.rutaagua.camion.ui.theme.TextoOscuro
import com.rutaagua.camion.ui.theme.TextoSecundario
import com.rutaagua.camion.ui.theme.VerdeEfectivo
import com.rutaagua.camion.ui.theme.VerdeEfectivoFondo
import java.util.Locale

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
        montoCanceladoDeuda: Double
    ) -> Unit,
    onCancelar: () -> Unit
) {
    // ------------------------------------------------------------------
    // SECCIÓN 1: CLIENTE
    // ------------------------------------------------------------------
    val clienteInicial = clientePreseleccionado ?: transaccionAEditar?.clienteId?.let { id ->
        clientes.find { it.id == id }
    }

    var clienteSeleccionado by remember { mutableStateOf(clienteInicial) }
    var textoBusquedaCliente by remember {
        mutableStateOf(clienteInicial?.nombre ?: transaccionAEditar?.nombreClientePaso ?: "")
    }
    var esClienteDePaso by remember { mutableStateOf(clienteInicial == null) }

    // ------------------------------------------------------------------
    // SECCIÓN 2: PRODUCTOS Y MOVIMIENTO
    // ------------------------------------------------------------------
    var tipoBidonSeleccionado by remember {
        mutableStateOf(transaccionAEditar?.tipoBidon ?: clienteInicial?.tipoBidonPreferido ?: TipoBidon.AZUL)
    }

    var precioUnitarioBidon by remember {
        mutableDoubleStateOf(transaccionAEditar?.precioUnitarioBidon ?: clienteInicial?.precioHabitual ?: 1.00)
    }

    val inicioTrans = transaccionAEditar?.recargaTransparentes ?: if (transaccionAEditar?.tipoBidon == TipoBidon.TRANSPARENTE) transaccionAEditar.cantidadBidonesRecarga else 0
    val inicioAzul = transaccionAEditar?.recargaAzules ?: if (transaccionAEditar?.tipoBidon == TipoBidon.AZUL) transaccionAEditar.cantidadBidonesRecarga else if (transaccionAEditar == null && clienteInicial?.tipoBidonPreferido == TipoBidon.AZUL) 1 else if (transaccionAEditar == null && clienteInicial == null) 1 else 0
    val inicioMarc = transaccionAEditar?.recargaMarcados ?: if (transaccionAEditar?.tipoBidon == TipoBidon.MARCADO) transaccionAEditar.cantidadBidonesRecarga else 0

    var cantRecargaTrans by remember { mutableIntStateOf(inicioTrans) }
    var cantRecargaAzul by remember { mutableIntStateOf(inicioAzul) }
    var cantRecargaMarc by remember { mutableIntStateOf(inicioMarc) }

    val cantRecarga = cantRecargaTrans + cantRecargaAzul + cantRecargaMarc
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

    var cantPrestados by remember {
        mutableIntStateOf(transaccionAEditar?.cantidadBidonesPrestados ?: 0)
    }
    var cantDevueltos by remember {
        mutableIntStateOf(transaccionAEditar?.cantidadBidonesDevueltos ?: 0)
    }

    var cantGalones by remember {
        mutableIntStateOf(transaccionAEditar?.cantidadGalones ?: 0)
    }
    var cantPacas by remember {
        mutableIntStateOf(transaccionAEditar?.cantidadPacas ?: 0)
    }
    var cantMinis by remember {
        mutableIntStateOf(transaccionAEditar?.cantidadMinis ?: 0)
    }

    // ------------------------------------------------------------------
    // SECCIÓN 3: PAGO MIXTO Y FORMAS DE PAGO
    // ------------------------------------------------------------------
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

    // Campos para pago mixto / combinado
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

    // Cálculos de Total
    val totalRecarga = cantRecarga * precioUnitarioBidon
    val totalEnvaseVendido = cantVendidosEnvase * precioEnvaseVendido
    val totalGalones = cantGalones * 0.75
    val totalPacas = cantPacas * 3.75
    val totalMinis = cantMinis * 1.75
    val totalGeneral = totalRecarga + totalEnvaseVendido + totalGalones + totalPacas + totalMinis

    val esEdicion = transaccionAEditar != null

    LazyColumn(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp, vertical = 8.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        // Encabezado
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = if (esEdicion) "EDITAR ENTREGA REGISTRADA" else "GUARDAR ENTREGA / VENTA",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Black,
                        color = AguaPrimaryDark
                    )
                    Text(
                        text = if (esEdicion) "Modifica los datos y se recalculará el inventario y saldos" else "Registro rápido en ruta física",
                        style = MaterialTheme.typography.bodyMedium,
                        color = TextoSecundario
                    )
                }
                IconButton(onClick = onCancelar) {
                    Icon(imageVector = Icons.Default.Close, contentDescription = "Cerrar")
                }
            }
        }

        // ==============================================================
        // 1. SELECCIÓN DE CLIENTE
        // ==============================================================
        item {
            Text(
                text = "1. CLIENTE",
                style = MaterialTheme.typography.labelLarge,
                color = TextoSecundario
            )
            Spacer(modifier = Modifier.height(4.dp))

            OutlinedTextField(
                value = textoBusquedaCliente,
                onValueChange = {
                    textoBusquedaCliente = it
                    if (clienteSeleccionado != null && it != clienteSeleccionado?.nombre) {
                        clienteSeleccionado = null
                        esClienteDePaso = true
                    }
                },
                placeholder = { Text("Buscar cliente o escribir nombre al paso...") },
                leadingIcon = {
                    Icon(
                        imageVector = if (clienteSeleccionado != null) Icons.Default.Person else Icons.Default.Search,
                        contentDescription = null,
                        tint = if (clienteSeleccionado != null) VerdeEfectivo else AguaPrimary
                    )
                },
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )

            val sugerencias = remember(textoBusquedaCliente, clientes, clienteSeleccionado) {
                if (clienteSeleccionado == null && textoBusquedaCliente.isNotBlank()) {
                    clientes.filter {
                        it.nombre.contains(textoBusquedaCliente, ignoreCase = true) ||
                                it.ubicacion.contains(textoBusquedaCliente, ignoreCase = true)
                    }.take(3)
                } else emptyList()
            }

            if (sugerencias.isNotEmpty()) {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    elevation = CardDefaults.cardElevation(defaultElevation = 3.dp)
                ) {
                    Column {
                        sugerencias.forEach { cliente ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable {
                                        clienteSeleccionado = cliente
                                        textoBusquedaCliente = cliente.nombre
                                        esClienteDePaso = false
                                        tipoBidonSeleccionado = cliente.tipoBidonPreferido
                                        precioUnitarioBidon = cliente.precioHabitual
                                    }
                                    .padding(12.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                BidonIllustration(tipo = cliente.tipoBidonPreferido, size = 28.dp)
                                Spacer(modifier = Modifier.width(10.dp))
                                Column {
                                    Text(text = cliente.nombre, fontWeight = FontWeight.Bold)
                                    Text(
                                        text = "${cliente.ubicacion} • $${String.format(Locale.US, "%.2f", cliente.precioHabitual)}",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = TextoSecundario
                                    )
                                }
                            }
                        }
                    }
                }
            }

            clienteSeleccionado?.let { cliente ->
                val tieneDeuda = cliente.deudaMonetaria > 0.0
                val tienePrestados = cliente.bidonesPrestados > 0
                if (tieneDeuda || tienePrestados) {
                    Spacer(modifier = Modifier.height(6.dp))
                    Card(
                        colors = CardDefaults.cardColors(containerColor = AmbarFondo),
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier.padding(10.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "⚠️ SALDOS PENDIENTES: ",
                                fontWeight = FontWeight.Black,
                                color = AmbarAdvertencia,
                                style = MaterialTheme.typography.labelSmall
                            )
                            if (tieneDeuda) {
                                Text(
                                    text = "Debe $${String.format(Locale.US, "%.2f", cliente.deudaMonetaria)} ",
                                    fontWeight = FontWeight.Bold,
                                    color = RojoAlerta,
                                    style = MaterialTheme.typography.bodySmall
                                )
                            }
                            if (tienePrestados) {
                                Text(
                                    text = "• Debe ${cliente.bidonesPrestados} envases",
                                    fontWeight = FontWeight.Bold,
                                    color = TextoOscuro,
                                    style = MaterialTheme.typography.bodySmall
                                )
                            }
                        }
                    }
                }
            }
        }

        // ==============================================================
        // 2. SELECCIÓN DE BIDONES DE 20L Y PRECIO (PERMITE COMBINAR)
        // ==============================================================
        item {
            Text(
                text = "2. RECARGAS DE BOTELLÓN DE 20L (COMBINA TIPOS)",
                style = MaterialTheme.typography.labelLarge,
                color = TextoSecundario
            )
            Spacer(modifier = Modifier.height(4.dp))

            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // PET Cristal
                FilaBidonCombinable(
                    tipo = TipoBidon.TRANSPARENTE,
                    nombre = "PET Cristal (Transparente)",
                    disponibles = inventario?.bidonesLlenosTransparentes ?: 0,
                    cantidad = cantRecargaTrans,
                    onCambio = {
                        cantRecargaTrans = it
                        if (it > 0) tipoBidonSeleccionado = TipoBidon.TRANSPARENTE
                    }
                )

                // Azul Fuerte
                FilaBidonCombinable(
                    tipo = TipoBidon.AZUL,
                    nombre = "Azul Fuerte (Estándar)",
                    disponibles = inventario?.bidonesLlenosAzules ?: 0,
                    cantidad = cantRecargaAzul,
                    onCambio = {
                        cantRecargaAzul = it
                        if (it > 0) tipoBidonSeleccionado = TipoBidon.AZUL
                    }
                )

                // Marcado
                FilaBidonCombinable(
                    tipo = TipoBidon.MARCADO,
                    nombre = "Marcado / Sello",
                    disponibles = inventario?.bidonesLlenosMarcados ?: 0,
                    cantidad = cantRecargaMarc,
                    onCambio = {
                        cantRecargaMarc = it
                        if (it > 0) tipoBidonSeleccionado = TipoBidon.MARCADO
                    }
                )
            }
        }

        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "Precio por recarga de bidón",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "Total recargas a entregar: $cantRecarga un.",
                        style = MaterialTheme.typography.bodySmall,
                        color = TextoSecundario
                    )
                }
            }

            Spacer(modifier = Modifier.height(6.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                listOf(0.75, 0.85, 1.00, 1.25).forEach { precio ->
                    val sel = precioUnitarioBidon == precio
                    FilterChip(
                        selected = sel,
                        onClick = { precioUnitarioBidon = precio },
                        label = { Text("$$precio", fontWeight = FontWeight.Bold) },
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        }

        // ==============================================================
        // CONTROL DE ENVASES (PRESTADOS, DEVUELTOS, ENVASE NUEVO)
        // ==============================================================
        item {
            Text(
                text = "MOVIMIENTO DE ENVASES Y OTROS CASOS",
                style = MaterialTheme.typography.labelLarge,
                color = TextoSecundario
            )
            Spacer(modifier = Modifier.height(6.dp))

            Card(
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                shape = RoundedCornerShape(12.dp),
                border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFE2E8F0)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(12.dp)) {
                    FilaContadorMovimiento(
                        titulo = "Bidón Prestado (No deja vacío)",
                        subtitulo = "Baja lleno, queda debiendo envase",
                        color = AmbarAdvertencia,
                        valor = cantPrestados,
                        onCambio = { cantPrestados = it }
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    FilaContadorMovimiento(
                        titulo = "Bidón Devuelto (Devuelve vacío)",
                        subtitulo = "Sube vacío, resta deuda de envase",
                        color = VerdeEfectivo,
                        valor = cantDevueltos,
                        onCambio = { cantDevueltos = it }
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "Venta Envase Nuevo (Lleno)",
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.Bold
                            )
                            Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                FilterChip(
                                    selected = precioEnvaseVendido == 5.50,
                                    onClick = { precioEnvaseVendido = 5.50 },
                                    label = { Text("$5.50", fontSize = 12.sp) }
                                )
                                FilterChip(
                                    selected = precioEnvaseVendido == 6.00,
                                    onClick = { precioEnvaseVendido = 6.00 },
                                    label = { Text("$6.00", fontSize = 12.sp) }
                                )
                            }
                        }
                        ControlContador(
                            valor = cantVendidosEnvase,
                            onCambio = { cantVendidosEnvase = it }
                        )
                    }
                }
            }
        }

        // ==============================================================
        // OTROS PRODUCTOS (GALONES, PACAS, MINIS)
        // ==============================================================
        item {
            Text(
                text = "OTROS PRODUCTOS",
                style = MaterialTheme.typography.labelLarge,
                color = TextoSecundario
            )
            Spacer(modifier = Modifier.height(6.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                TarjetaSelectorSecundario(
                    nombre = "Galón (4L)",
                    precio = "$0.75",
                    disponibles = inventario?.galonesLlenos ?: 0,
                    valor = cantGalones,
                    ilustracion = { GalonIllustration(size = 36.dp) },
                    onCambio = { cantGalones = it },
                    modifier = Modifier.weight(1f)
                )

                TarjetaSelectorSecundario(
                    nombre = "Paca 24x",
                    precio = "$3.75",
                    disponibles = inventario?.pacasLlenas ?: 0,
                    valor = cantPacas,
                    ilustracion = { PacaIllustration(size = 36.dp) },
                    onCambio = { cantPacas = it },
                    modifier = Modifier.weight(1f)
                )

                TarjetaSelectorSecundario(
                    nombre = "Mini 12x",
                    precio = "$1.75",
                    disponibles = inventario?.minisLlenas ?: 0,
                    valor = cantMinis,
                    ilustracion = { MiniIllustration(size = 36.dp) },
                    onCambio = { cantMinis = it },
                    modifier = Modifier.weight(1f)
                )
            }
        }

        // ==============================================================
        // 3. TOTAL Y FORMA DE PAGO
        // ==============================================================
        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = VerdeEfectivoFondo),
                shape = RoundedCornerShape(14.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "TOTAL A COBRAR",
                            style = MaterialTheme.typography.labelLarge,
                            fontWeight = FontWeight.Bold,
                            color = VerdeEfectivo
                        )
                        Text(
                            text = "$${String.format(Locale.US, "%.2f", totalGeneral)}",
                            style = MaterialTheme.typography.displayMedium,
                            fontWeight = FontWeight.Black,
                            color = VerdeEfectivo
                        )
                    }
                }
            }
        }

        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "3. FORMA DE PAGO",
                    style = MaterialTheme.typography.labelLarge,
                    color = TextoSecundario
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
                            text = if (modoPagoMixto) "✓ Pago Combinado (Activo)" else "+ Combinar Pagos",
                            fontWeight = FontWeight.Bold,
                            fontSize = 11.sp
                        )
                    }
                )
            }
            Spacer(modifier = Modifier.height(6.dp))

            if (!modoPagoMixto) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    BotonFormaPago(
                        titulo = "Efectivo",
                        icono = Icons.Default.Money,
                        seleccionado = formaPago == FormaPago.EFECTIVO,
                        onSelect = { formaPago = FormaPago.EFECTIVO },
                        modifier = Modifier.weight(1f)
                    )
                    BotonFormaPago(
                        titulo = "Transf.",
                        icono = Icons.Default.CreditCard,
                        seleccionado = formaPago == FormaPago.TRANSFERENCIA,
                        onSelect = { formaPago = FormaPago.TRANSFERENCIA },
                        modifier = Modifier.weight(1f)
                    )
                    BotonFormaPago(
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
                    BotonFormaPago(
                        titulo = "Abono",
                        icono = Icons.Default.Payments,
                        seleccionado = formaPago == FormaPago.ABONO,
                        onSelect = {
                            formaPago = FormaPago.ABONO
                            if (montoAbonoTexto.isBlank()) montoAbonoTexto = String.format(Locale.US, "%.2f", totalGeneral)
                        },
                        modifier = Modifier.weight(1f)
                    )
                    BotonFormaPago(
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
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            } else {
                // Interfaz para combinar múltiples formas de pago a la vez
                Card(
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    shape = RoundedCornerShape(12.dp),
                    border = androidx.compose.foundation.BorderStroke(1.dp, AguaPrimary),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Text(
                            text = "DIVIDIR TOTAL ($${String.format(Locale.US, "%.2f", totalGeneral)}) ENTRE FORMAS DE PAGO:",
                            style = MaterialTheme.typography.labelSmall,
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
                                modifier = Modifier.weight(1f)
                            )
                            OutlinedTextField(
                                value = pagoMixtoCredito,
                                onValueChange = { pagoMixtoCredito = it },
                                label = { Text("A Crédito ($)") },
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                                singleLine = true,
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
                                modifier = Modifier.weight(1f)
                            )
                            OutlinedTextField(
                                value = pagoMixtoAbonoDeuda,
                                onValueChange = { pagoMixtoAbonoDeuda = it },
                                label = { Text("Abono Deuda Anterior ($)") },
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                                singleLine = true,
                                modifier = Modifier.weight(1f)
                            )
                        }

                        val sumaCubierta = (pagoMixtoEfectivo.toDoubleOrNull() ?: 0.0) +
                                (pagoMixtoCredito.toDoubleOrNull() ?: 0.0) +
                                (pagoMixtoTransferencia.toDoubleOrNull() ?: 0.0)
                        val diferencia = totalGeneral - sumaCubierta

                        Spacer(modifier = Modifier.height(6.dp))
                        if (Math.abs(diferencia) > 0.01) {
                            Text(
                                text = if (diferencia > 0) "⚠️ Falta asignar: $${String.format(Locale.US, "%.2f", diferencia)}"
                                else "⚠️ Asignado en exceso: $${String.format(Locale.US, "%.2f", -diferencia)}",
                                style = MaterialTheme.typography.labelSmall,
                                color = AmbarAdvertencia,
                                fontWeight = FontWeight.Bold
                            )
                        } else {
                            Text(
                                text = "✓ El total cuadra exactamente ($${String.format(Locale.US, "%.2f", totalGeneral)})",
                                style = MaterialTheme.typography.labelSmall,
                                color = VerdeEfectivo,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }
        }

        // Botón Confirmar
        item {
            Spacer(modifier = Modifier.height(10.dp))
            Button(
                onClick = {
                    val pagado = when (formaPago) {
                        FormaPago.EFECTIVO, FormaPago.TRANSFERENCIA -> totalGeneral
                        FormaPago.CREDITO -> 0.0
                        FormaPago.ABONO, FormaPago.CANCELADO -> montoAbonoTexto.toDoubleOrNull() ?: totalGeneral
                    }

                    val nombreFinal = if (esClienteDePaso || clienteSeleccionado == null) {
                        if (textoBusquedaCliente.isBlank()) "Cliente de Paso" else textoBusquedaCliente.trim()
                    } else null

                    val mEf = if (modoPagoMixto) (pagoMixtoEfectivo.toDoubleOrNull() ?: 0.0) else 0.0
                    val mTr = if (modoPagoMixto) (pagoMixtoTransferencia.toDoubleOrNull() ?: 0.0) else 0.0
                    val mCr = if (modoPagoMixto) (pagoMixtoCredito.toDoubleOrNull() ?: 0.0) else 0.0
                    val mAb = if (modoPagoMixto) (pagoMixtoAbonoDeuda.toDoubleOrNull() ?: 0.0) else 0.0

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
                        0.0
                    )
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(60.dp),
                colors = ButtonDefaults.buttonColors(containerColor = VerdeEfectivo),
                shape = RoundedCornerShape(14.dp)
            ) {
                Icon(imageVector = Icons.Default.Check, contentDescription = null)
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = if (esEdicion) "GUARDAR CAMBIOS ($${String.format(Locale.US, "%.2f", totalGeneral)})" else "CONFIRMAR ENTREGA ($${String.format(Locale.US, "%.2f", totalGeneral)})",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Black
                )
            }
            Spacer(modifier = Modifier.height(30.dp))
        }
    }
}

@Composable
private fun TarjetaSelectorBidonVenta(
    tipo: TipoBidon,
    nombre: String,
    disponibles: Int,
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
            BidonIllustration(tipo = tipo, size = 42.dp)
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = nombre,
                style = MaterialTheme.typography.labelSmall,
                fontWeight = if (seleccionado) FontWeight.Black else FontWeight.Normal,
                textAlign = TextAlign.Center
            )
            Text(
                text = "Quedan: $disponibles",
                style = MaterialTheme.typography.bodySmall,
                color = if (disponibles <= 10) RojoAlerta else TextoSecundario,
                fontSize = 11.sp
            )
        }
    }
}

@Composable
private fun TarjetaSelectorSecundario(
    nombre: String,
    precio: String,
    disponibles: Int,
    valor: Int,
    ilustracion: @Composable () -> Unit,
    onCambio: (Int) -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(10.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFE2E8F0))
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(8.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Box(modifier = Modifier.size(36.dp), contentAlignment = Alignment.Center) {
                ilustracion()
            }
            Text(text = nombre, style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold)
            Text(text = "$precio (disp: $disponibles)", fontSize = 10.sp, color = TextoSecundario)
            Spacer(modifier = Modifier.height(4.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                IconButton(
                    onClick = { if (valor > 0) onCambio(valor - 1) },
                    modifier = Modifier
                        .size(26.dp)
                        .background(Color(0xFFE2E8F0), CircleShape)
                ) {
                    Icon(imageVector = Icons.Default.Remove, contentDescription = null, modifier = Modifier.size(16.dp))
                }
                Text(
                    text = "$valor",
                    fontWeight = FontWeight.Black,
                    modifier = Modifier.width(28.dp),
                    textAlign = TextAlign.Center
                )
                IconButton(
                    onClick = { onCambio(valor + 1) },
                    modifier = Modifier
                        .size(26.dp)
                        .background(AguaPrimary, CircleShape)
                ) {
                    Icon(imageVector = Icons.Default.Add, contentDescription = null, tint = Color.White, modifier = Modifier.size(16.dp))
                }
            }
        }
    }
}

@Composable
private fun FilaContadorMovimiento(
    titulo: String,
    subtitulo: String,
    color: Color,
    valor: Int,
    onCambio: (Int) -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(text = titulo, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Bold, color = color)
            Text(text = subtitulo, style = MaterialTheme.typography.bodySmall, color = TextoSecundario)
        }
        ControlContador(valor = valor, onCambio = onCambio)
    }
}

@Composable
private fun FilaBidonCombinable(
    tipo: TipoBidon,
    nombre: String,
    disponibles: Int,
    cantidad: Int,
    onCambio: (Int) -> Unit
) {
    Surface(
        color = Color(0xFFF8FAFC),
        shape = RoundedCornerShape(12.dp),
        border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFE2E8F0)),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 8.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.weight(1f)
            ) {
                BidonIllustration(tipo = tipo, size = 38.dp)
                Spacer(modifier = Modifier.width(10.dp))
                Column {
                    Text(
                        text = nombre,
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Bold,
                        color = TextoOscuro
                    )
                    Text(
                        text = "Disponibles en camión: $disponibles",
                        style = MaterialTheme.typography.bodySmall,
                        color = if (disponibles <= 10) RojoAlerta else TextoSecundario,
                        fontSize = 11.sp
                    )
                }
            }

            ControlContador(valor = cantidad, onCambio = onCambio)
        }
    }
}

@Composable
private fun ControlContador(
    valor: Int,
    onCambio: (Int) -> Unit
) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        IconButton(
            onClick = { if (valor > 0) onCambio(valor - 1) },
            modifier = Modifier
                .size(36.dp)
                .background(Color(0xFFE2E8F0), CircleShape)
        ) {
            Icon(imageVector = Icons.Default.Remove, contentDescription = "Menos", tint = TextoOscuro)
        }

        Text(
            text = "$valor",
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Black,
            textAlign = TextAlign.Center,
            modifier = Modifier.width(44.dp)
        )

        IconButton(
            onClick = { onCambio(valor + 1) },
            modifier = Modifier
                .size(36.dp)
                .background(AguaPrimary, CircleShape)
        ) {
            Icon(imageVector = Icons.Default.Add, contentDescription = "Más", tint = Color.White)
        }
    }
}

@Composable
private fun BotonFormaPago(
    titulo: String,
    icono: androidx.compose.ui.graphics.vector.ImageVector,
    seleccionado: Boolean,
    onSelect: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier
            .clickable { onSelect() }
            .height(48.dp),
        color = if (seleccionado) VerdeEfectivo else Color(0xFFF1F5F9),
        shape = RoundedCornerShape(10.dp),
        border = if (!seleccionado) androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFCBD5E1)) else null
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 4.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            Icon(
                imageVector = icono,
                contentDescription = null,
                tint = if (seleccionado) Color.White else TextoOscuro,
                modifier = Modifier.size(18.dp)
            )
            Spacer(modifier = Modifier.width(4.dp))
            Text(
                text = titulo,
                color = if (seleccionado) Color.White else TextoOscuro,
                style = MaterialTheme.typography.labelSmall,
                fontWeight = FontWeight.Bold
            )
        }
    }
}

package com.rutaagua.camion.ui.screens.clientes

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.HomeWork
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.rutaagua.camion.data.local.entity.ClienteEntity
import com.rutaagua.camion.data.local.entity.TransaccionVentaEntity
import com.rutaagua.camion.data.local.entity.VisitaFallidaEntity
import com.rutaagua.camion.data.local.model.TipoBidon
import com.rutaagua.camion.data.local.model.TipoCliente
import com.rutaagua.camion.ui.components.ClayBadge
import com.rutaagua.camion.ui.components.ClayCard
import com.rutaagua.camion.ui.components.ClayInsetBox
import com.rutaagua.camion.ui.components.marineOutlinedTextFieldColors
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
import com.rutaagua.camion.ui.theme.BordeSuave
import com.rutaagua.camion.ui.theme.FondoGrisClaro
import com.rutaagua.camion.ui.theme.RojoAlerta
import com.rutaagua.camion.ui.theme.RojoAlertaFondo
import com.rutaagua.camion.ui.theme.TextoOscuro
import com.rutaagua.camion.ui.theme.TextoSecundario
import com.rutaagua.camion.ui.theme.VerdeEfectivo
import com.rutaagua.camion.ui.theme.VerdeEfectivoFondo
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

private sealed class EventoHistorialCliente(
    val timestamp: Long,
    val fechaIso: String,
    val fechaLegible: String
) {
    class Entrega(
        val transaccion: TransaccionVentaEntity,
        fechaLegible: String
    ) : EventoHistorialCliente(transaccion.fecha, transaccion.fechaTexto, fechaLegible)

    class VisitaNoEstaba(
        val visita: VisitaFallidaEntity,
        fechaLegible: String
    ) : EventoHistorialCliente(visita.timestamp, visita.fechaIso, fechaLegible)
}

/**
 * Modal de Historial Completo de un Cliente específico (Entregas y Visitas de todos los meses/años).
 * Permite verificar al instante si se le dejó agua hace semanas o meses (ej. clientes de llamada o ruta).
 */
@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun HistorialClienteModalSheet(
    cliente: ClienteEntity,
    transacciones: List<TransaccionVentaEntity>,
    visitasFallidas: List<VisitaFallidaEntity>,
    onDismiss: () -> Unit
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    var filtroFechaTexto by rememberSaveable { mutableStateOf("") }

    val formatoFechaHora = remember { SimpleDateFormat("EEEE dd/MM/yyyy • HH:mm", Locale("es", "EC")) }
    val formatoSoloFecha = remember { SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()) }

    val eventosCombinados = remember(transacciones, visitasFallidas, filtroFechaTexto) {
        val lista = mutableListOf<EventoHistorialCliente>()
        transacciones.forEach { tx ->
            val legible = formatoFechaHora.format(Date(tx.fecha)).replaceFirstChar { it.uppercase() }
            lista.add(EventoHistorialCliente.Entrega(tx, legible))
        }
        visitasFallidas.forEach { vis ->
            val legible = "${vis.fechaIso.split("-").reversed().joinToString("/")} • ${vis.horaDisplay}"
            lista.add(EventoHistorialCliente.VisitaNoEstaba(vis, legible))
        }
        lista.sortByDescending { it.timestamp }

        if (filtroFechaTexto.isBlank()) {
            lista
        } else {
            val q = filtroFechaTexto.trim()
            lista.filter { ev ->
                ev.fechaIso.contains(q, ignoreCase = true) ||
                        ev.fechaLegible.contains(q, ignoreCase = true)
            }
        }
    }

    val totalBidonesHistoricos = remember(transacciones) {
        transacciones.sumOf {
            it.cantidadBidonesRecarga + it.cantidadBidonesPrestados + it.cantidadBidonesVendidosEnvase
        }
    }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = FondoGrisClaro
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(min = 380.dp, max = 680.dp)
                .padding(horizontal = 16.dp, vertical = 6.dp)
        ) {
            // Cabecera del Cliente
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.weight(1f)
                ) {
                    Box(
                        modifier = Modifier
                            .size(40.dp)
                            .background(AguaPrimary.copy(alpha = 0.14f), RoundedCornerShape(12.dp)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.History,
                            contentDescription = null,
                            tint = AguaPrimaryDark,
                            modifier = Modifier.size(22.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = cliente.nombre,
                            fontWeight = FontWeight.Black,
                            fontSize = 17.sp,
                            color = TextoOscuro,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        val tipoLabel = if (cliente.tipoCliente == TipoCliente.LLAMADA) "Cliente de Llamada" else "Cliente de Ruta (${cliente.diasVisita})"
                        Text(
                            text = "$tipoLabel • ${cliente.ubicacion.ifBlank { "Sin sector" }}",
                            fontSize = 12.sp,
                            color = TextoSecundario,
                            fontWeight = FontWeight.SemiBold,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }
                IconButton(onClick = onDismiss) {
                    Icon(Icons.Default.Close, contentDescription = "Cerrar", tint = TextoSecundario)
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Resumen Histórico Rápido
            ClayInsetBox(
                modifier = Modifier.fillMaxWidth(),
                cornerRadius = 14.dp,
                contentPadding = PaddingValues(horizontal = 12.dp, vertical = 10.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    MetricaHistorialItem("Entregas", "${transacciones.size}", AguaPrimaryDark)
                    MetricaHistorialItem("Bidones Total", "$totalBidonesHistoricos", VerdeEfectivo)
                    MetricaHistorialItem("No Estaba", "${visitasFallidas.size}", AmbarAdvertencia)
                    MetricaHistorialItem(
                        "Deuda Actual",
                        "$${String.format(Locale.US, "%.2f", cliente.deudaMonetaria)}",
                        if (cliente.deudaMonetaria > 0) RojoAlerta else TextoSecundario
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Buscador por Fecha o Mes (ej. "2026-11", "noviembre", "15/11/2026")
            OutlinedTextField(
                value = filtroFechaTexto,
                onValueChange = { filtroFechaTexto = it },
                placeholder = {
                    Text(
                        text = "Filtrar por fecha o mes (ej. 15/11/2026 o 2026-11)...",
                        fontSize = 12.sp,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                },
                leadingIcon = {
                    Icon(Icons.Default.Search, contentDescription = null, tint = AguaPrimary)
                },
                trailingIcon = {
                    if (filtroFechaTexto.isNotEmpty()) {
                        IconButton(onClick = { filtroFechaTexto = "" }) {
                            Icon(Icons.Default.Close, contentDescription = "Limpiar")
                        }
                    }
                },
                singleLine = true,
                shape = RoundedCornerShape(14.dp),
                colors = marineOutlinedTextFieldColors(),
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(10.dp))

            if (eventosCombinados.isEmpty()) {
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
                            modifier = Modifier.size(36.dp)
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = if (filtroFechaTexto.isBlank()) "Sin registros históricos para este cliente"
                            else "Sin registros en la fecha buscada",
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp,
                            color = TextoOscuro
                        )
                    }
                }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                    contentPadding = PaddingValues(bottom = 28.dp)
                ) {
                    items(eventosCombinados) { evento ->
                        when (evento) {
                            is EventoHistorialCliente.Entrega -> {
                                TarjetaEntregaHistorialItem(
                                    tx = evento.transaccion,
                                    fechaLegible = evento.fechaLegible,
                                    formatoSoloFecha = formatoSoloFecha
                                )
                            }
                            is EventoHistorialCliente.VisitaNoEstaba -> {
                                TarjetaVisitaHistorialItem(
                                    visita = evento.visita,
                                    fechaLegible = evento.fechaLegible
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun MetricaHistorialItem(label: String, valor: String, color: Color) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
            text = valor,
            fontWeight = FontWeight.Black,
            fontSize = 15.sp,
            color = color,
            maxLines = 1
        )
        Text(
            text = label,
            fontSize = 10.sp,
            fontWeight = FontWeight.SemiBold,
            color = TextoSecundario,
            maxLines = 1
        )
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun TarjetaEntregaHistorialItem(
    tx: TransaccionVentaEntity,
    fechaLegible: String,
    formatoSoloFecha: SimpleDateFormat
) {
    val etiquetaVehiculo = if (tx.vehiculo == "CAMIONETA") "🛻 Camioneta" else "🚚 Camión"

    ClayCard(
        modifier = Modifier.fillMaxWidth(),
        cornerRadius = 16.dp,
        elevation = 2.dp,
        contentPadding = PaddingValues(12.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = fechaLegible,
                    fontWeight = FontWeight.Black,
                    fontSize = 13.sp,
                    color = TextoOscuro,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = "Atendido por $etiquetaVehiculo",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = AguaPrimaryDark,
                    maxLines = 1
                )
            }

            Spacer(modifier = Modifier.width(8.dp))

            Text(
                text = "$${String.format(Locale.US, "%.2f", tx.totalImporte)}",
                fontWeight = FontWeight.Black,
                fontSize = 16.sp,
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
                    ClayBadge(
                        text = "${tx.recargaTransparentes} PET",
                        containerColor = BidonTransparenteFondo,
                        contentColor = BidonTransparenteColor
                    )
                }
                if (tx.recargaAzules > 0) {
                    ClayBadge(
                        text = "${tx.recargaAzules} Azul",
                        containerColor = BidonAzulFondo,
                        contentColor = BidonAzulColor
                    )
                }
                if (tx.recargaMarcados > 0) {
                    ClayBadge(
                        text = "${tx.recargaMarcados} Marc",
                        containerColor = BidonMarcadoFondo,
                        contentColor = BidonMarcadoColor
                    )
                }
            } else if (tx.cantidadBidonesRecarga > 0) {
                val (bg, fg, lbl) = when (tx.tipoBidon) {
                    TipoBidon.TRANSPARENTE -> Triple(BidonTransparenteFondo, BidonTransparenteColor, "PET")
                    TipoBidon.AZUL -> Triple(BidonAzulFondo, BidonAzulColor, "Azul")
                    TipoBidon.MARCADO -> Triple(BidonMarcadoFondo, BidonMarcadoColor, "Marc")
                }
                ClayBadge(
                    text = "${tx.cantidadBidonesRecarga} $lbl",
                    containerColor = bg,
                    contentColor = fg
                )
            }

            if (tx.cantidadBidonesPrestados > 0) {
                ClayBadge(
                    text = "+${tx.cantidadBidonesPrestados} Prest.",
                    containerColor = AmbarFondo,
                    contentColor = AmbarAdvertencia
                )
            }
            if (tx.cantidadBidonesDevueltos > 0) {
                ClayBadge(
                    text = "-${tx.cantidadBidonesDevueltos} Dev.",
                    containerColor = VerdeEfectivoFondo,
                    contentColor = VerdeEfectivo
                )
            }
            if (tx.cantidadBidonesGratis > 0) {
                ClayBadge(
                    text = "+${tx.cantidadBidonesGratis} Gratis",
                    containerColor = VerdeEfectivoFondo,
                    contentColor = VerdeEfectivo
                )
            }
            if (tx.cantidadGalones > 0) ClayBadge(text = "${tx.cantidadGalones} Gal")
            if (tx.cantidadPacas > 0) ClayBadge(text = "${tx.cantidadPacas} Pac")
            if (tx.cantidadMinis > 0) ClayBadge(text = "${tx.cantidadMinis} Min")

            if (tx.montoEfectivo > 0) {
                ClayBadge(
                    text = "Efec $${String.format(Locale.US, "%.2f", tx.montoEfectivo)}",
                    containerColor = VerdeEfectivoFondo,
                    contentColor = VerdeEfectivo
                )
            }
            if (tx.montoTransferencia > 0) {
                ClayBadge(
                    text = "Transf $${String.format(Locale.US, "%.2f", tx.montoTransferencia)}",
                    containerColor = Color(0xFFDBEAFE),
                    contentColor = AguaPrimaryDark
                )
            }
            if (tx.montoCredito > 0) {
                ClayBadge(
                    text = "Fiado $${String.format(Locale.US, "%.2f", tx.montoCredito)}",
                    containerColor = RojoAlertaFondo,
                    contentColor = RojoAlerta
                )
            }
            if (tx.montoAbono > 0) {
                ClayBadge(
                    text = "Abono $${String.format(Locale.US, "%.2f", tx.montoAbono)}",
                    containerColor = VerdeEfectivoFondo,
                    contentColor = VerdeEfectivo
                )
            }
        }
    }
}

@Composable
private fun TarjetaVisitaHistorialItem(
    visita: VisitaFallidaEntity,
    fechaLegible: String
) {
    val etiquetaVehiculo = if (visita.vehiculo == "CAMIONETA") "🛻 Camioneta" else "🚚 Camión"
    Surface(
        color = AmbarFondo,
        shape = RoundedCornerShape(14.dp),
        border = BorderStroke(1.dp, AmbarAdvertencia.copy(alpha = 0.35f)),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.weight(1f)
            ) {
                Icon(
                    imageVector = Icons.Default.HomeWork,
                    contentDescription = null,
                    tint = AmbarAdvertencia,
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "No estaba en casa",
                        fontWeight = FontWeight.Black,
                        fontSize = 13.sp,
                        color = AmbarAdvertencia,
                        maxLines = 1
                    )
                    Text(
                        text = "$fechaLegible • $etiquetaVehiculo",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = TextoOscuro,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }
            ClayBadge(
                text = "Visita sin entrega",
                containerColor = Color.White,
                contentColor = AmbarAdvertencia
            )
        }
    }
}

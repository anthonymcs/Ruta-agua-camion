package com.rutaagua.camion.ui.screens.calendario

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.HourglassTop
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.rutaagua.camion.data.local.model.DiaSemana
import com.rutaagua.camion.data.local.model.TipoBidon
import com.rutaagua.camion.ui.components.BidonIllustration
import com.rutaagua.camion.ui.screens.clientes.ClienteRutaItem
import com.rutaagua.camion.ui.screens.clientes.ClientesViewModel
import com.rutaagua.camion.ui.theme.AguaPrimary
import com.rutaagua.camion.ui.theme.AguaPrimaryDark
import com.rutaagua.camion.ui.theme.AguaPrimaryLight
import com.rutaagua.camion.ui.theme.AmbarAdvertencia
import com.rutaagua.camion.ui.theme.AmbarFondo
import com.rutaagua.camion.ui.theme.RojoAlerta
import com.rutaagua.camion.ui.theme.RojoAlertaFondo
import com.rutaagua.camion.ui.theme.TextoOscuro
import com.rutaagua.camion.ui.theme.TextoSecundario
import com.rutaagua.camion.ui.theme.VerdeEfectivo
import com.rutaagua.camion.ui.theme.VerdeEfectivoFondo
import java.util.Locale

@Composable
fun CalendarioRutaScreen(
    viewModel: ClientesViewModel,
    modifier: Modifier = Modifier
) {
    val diaSeleccionado by viewModel.diaSeleccionadoCalendario.collectAsState()
    val clientesRuta by viewModel.clientesRutaDiaState.collectAsState()
    val conteoPorDia by viewModel.conteoPorDiaSemanaState.collectAsState()
    val diaActualHoy = ClientesViewModel.obtenerDiaSemanaActual()

    val atendidos = clientesRuta.count { it.entregadoHoy }
    val totalClientes = clientesRuta.size
    val progreso = if (totalClientes > 0) atendidos.toFloat() / totalClientes else 0f

    Scaffold(
        modifier = modifier,
        topBar = {
            HeaderCalendario()
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            // 1. Selector Horizontal de Días de la Semana (Lunes a Sábado)
            SelectorDiasSemana(
                diaSeleccionado = diaSeleccionado,
                diaActualHoy = diaActualHoy,
                conteoPorDia = conteoPorDia,
                onSeleccionarDia = { viewModel.seleccionarDiaCalendario(it) }
            )

            // 2. Barra de Progreso de la Ruta de Hoy
            if (diaSeleccionado == diaActualHoy) {
                TarjetaProgresoRuta(
                    atendidos = atendidos,
                    total = totalClientes,
                    progreso = progreso
                )
            }

            // 3. Resumen de Botellones Previstos a Separar en el Camión
            if (clientesRuta.isNotEmpty()) {
                ResumenBotellonesRequeridos(clientesRuta = clientesRuta)
            }

            // 4. Lista de Clientes Programados para este Día
            if (clientesRuta.isEmpty()) {
                EstadoVacioDiaRuta(dia = diaSeleccionado)
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    items(clientesRuta, key = { it.cliente.id }) { item ->
                        TarjetaClienteRutaProgramado(item = item)
                    }
                    item {
                        Spacer(modifier = Modifier.height(80.dp))
                    }
                }
            }
        }
    }
}

@Composable
private fun HeaderCalendario() {
    Surface(
        color = MaterialTheme.colorScheme.surface,
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
                modifier = Modifier.size(32.dp)
            )
            Spacer(modifier = Modifier.width(12.dp))
            Column {
                Text(
                    text = "CALENDARIO DE RUTA",
                    style = MaterialTheme.typography.titleLarge,
                    color = AguaPrimaryDark,
                    fontWeight = FontWeight.Black
                )
                Text(
                    text = "Planificación semanal (Lunes a Sábado)",
                    style = MaterialTheme.typography.bodyMedium,
                    color = TextoSecundario
                )
            }
        }
    }
}

/**
 * Pestañas / Tarjetas horizontales de Lunes a Sábado con contador y etiqueta "HOY".
 */
@Composable
private fun SelectorDiasSemana(
    diaSeleccionado: DiaSemana,
    diaActualHoy: DiaSemana,
    conteoPorDia: Map<DiaSemana, Int>,
    onSeleccionarDia: (DiaSemana) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .horizontalScroll(rememberScrollState())
            .padding(horizontal = 16.dp, vertical = 12.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        DiaSemana.entries.forEach { dia ->
            val esSeleccionado = dia == diaSeleccionado
            val esHoy = dia == diaActualHoy
            val count = conteoPorDia[dia] ?: 0

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
                    .clickable { onSeleccionarDia(dia) }
                    .width(78.dp),
                color = when {
                    esSeleccionado -> AguaPrimary
                    esHoy -> AguaPrimaryLight.copy(alpha = 0.20f)
                    else -> MaterialTheme.colorScheme.surface
                },
                shape = RoundedCornerShape(12.dp),
                border = androidx.compose.foundation.BorderStroke(
                    width = if (esSeleccionado || esHoy) 2.dp else 1.dp,
                    color = if (esSeleccionado) AguaPrimary else if (esHoy) AguaPrimaryLight else Color(0xFFE2E8F0)
                ),
                shadowElevation = if (esSeleccionado) 3.dp else 1.dp
            ) {
                Column(
                    modifier = Modifier.padding(vertical = 10.dp, horizontal = 4.dp),
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
                                modifier = Modifier.padding(horizontal = 4.dp, vertical = 1.dp)
                            )
                        }
                        Spacer(modifier = Modifier.height(2.dp))
                    }

                    Text(
                        text = abrev,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Black,
                        color = if (esSeleccionado) Color.White else TextoOscuro
                    )

                    Text(
                        text = "$count clientes",
                        style = MaterialTheme.typography.bodySmall,
                        color = if (esSeleccionado) Color.White.copy(alpha = 0.9f) else TextoSecundario,
                        fontSize = 11.sp
                    )
                }
            }
        }
    }
}

/**
 * Tarjeta de progreso de entregas para el día de ruta actual.
 */
@Composable
private fun TarjetaProgresoRuta(
    atendidos: Int,
    total: Int,
    progreso: Float
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 4.dp),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = VerdeEfectivoFondo)
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "PROGRESO DE RUTA DE HOY",
                    style = MaterialTheme.typography.labelLarge,
                    color = VerdeEfectivo,
                    fontWeight = FontWeight.Black
                )
                Text(
                    text = "$atendidos de $total atendidos",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Black,
                    color = VerdeEfectivo
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            LinearProgressIndicator(
                progress = { progreso },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(8.dp)
                    .clip(RoundedCornerShape(4.dp)),
                color = VerdeEfectivo,
                trackColor = Color.White,
                strokeCap = StrokeCap.Round
            )
        }
    }
}

/**
 * Resumen de botellones a preparar en el camión por tipo para la ruta de este día.
 */
@Composable
private fun ResumenBotellonesRequeridos(clientesRuta: List<ClienteRutaItem>) {
    val trans = clientesRuta.count { it.cliente.tipoBidonPreferido == TipoBidon.TRANSPARENTE }
    val azul = clientesRuta.count { it.cliente.tipoBidonPreferido == TipoBidon.AZUL }
    val marc = clientesRuta.count { it.cliente.tipoBidonPreferido == TipoBidon.MARCADO }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 6.dp),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(10.dp),
            horizontalArrangement = Arrangement.SpaceAround,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Envases esperados:",
                style = MaterialTheme.typography.labelMedium,
                color = TextoSecundario
            )

            Row(verticalAlignment = Alignment.CenterVertically) {
                BidonIllustration(tipo = TipoBidon.TRANSPARENTE, size = 24.dp)
                Spacer(modifier = Modifier.width(4.dp))
                Text(text = "$trans PET", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.bodySmall)
            }

            Row(verticalAlignment = Alignment.CenterVertically) {
                BidonIllustration(tipo = TipoBidon.AZUL, size = 24.dp)
                Spacer(modifier = Modifier.width(4.dp))
                Text(text = "$azul Azul", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.bodySmall)
            }

            Row(verticalAlignment = Alignment.CenterVertically) {
                BidonIllustration(tipo = TipoBidon.MARCADO, size = 24.dp)
                Spacer(modifier = Modifier.width(4.dp))
                Text(text = "$marc Marc", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.bodySmall)
            }
        }
    }
}

/**
 * Tarjeta individual para cliente en la ruta del día.
 */
@Composable
private fun TarjetaClienteRutaProgramado(item: ClienteRutaItem) {
    val cliente = item.cliente
    val entregado = item.entregadoHoy

    Card(
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (entregado) Color(0xFFF8FAFC) else MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = if (entregado) 1.dp else 2.dp),
        border = androidx.compose.foundation.BorderStroke(
            width = 1.dp,
            color = if (entregado) VerdeEfectivo.copy(alpha = 0.5f) else Color(0xFFE2E8F0)
        ),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            // Datos del cliente
            Column(modifier = Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = cliente.nombre,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Black,
                        color = if (entregado) Color(0xFF64748B) else TextoOscuro
                    )
                }

                if (cliente.ubicacion.isNotBlank()) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.padding(top = 2.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.LocationOn,
                            contentDescription = null,
                            tint = TextoSecundario,
                            modifier = Modifier.size(15.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = cliente.ubicacion,
                            style = MaterialTheme.typography.bodySmall,
                            color = TextoSecundario
                        )
                    }
                }

                Spacer(modifier = Modifier.height(6.dp))

                // Tipo de bidón y precio
                Row(verticalAlignment = Alignment.CenterVertically) {
                    BidonIllustration(tipo = cliente.tipoBidonPreferido, size = 26.dp)
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "Precio habitual: $${String.format(Locale.US, "%.2f", cliente.precioHabitual)}",
                        style = MaterialTheme.typography.bodySmall,
                        fontWeight = FontWeight.Bold,
                        color = AguaPrimary
                    )
                }

                // Deuda o envases adeudados
                if (cliente.deudaMonetaria > 0.0 || cliente.bidonesPrestados > 0) {
                    Spacer(modifier = Modifier.height(4.dp))
                    Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        if (cliente.deudaMonetaria > 0.0) {
                            Text(
                                text = "Debe: $${String.format(Locale.US, "%.2f", cliente.deudaMonetaria)}",
                                color = RojoAlerta,
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold
                            )
                        }
                        if (cliente.bidonesPrestados > 0) {
                            Text(
                                text = "Debe: ${cliente.bidonesPrestados} envases",
                                color = AmbarAdvertencia,
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.width(8.dp))

            // Estado de Entrega (Atendido vs Pendiente)
            Surface(
                color = if (entregado) VerdeEfectivoFondo else Color(0xFFFFFBEB),
                shape = RoundedCornerShape(8.dp),
                border = androidx.compose.foundation.BorderStroke(
                    width = 1.dp,
                    color = if (entregado) VerdeEfectivo else AmbarAdvertencia
                )
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = if (entregado) Icons.Default.CheckCircle else Icons.Default.HourglassTop,
                        contentDescription = null,
                        tint = if (entregado) VerdeEfectivo else AmbarAdvertencia,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = if (entregado) "Atendido" else "Pendiente",
                        color = if (entregado) VerdeEfectivo else AmbarAdvertencia,
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Black
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
            .padding(32.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Icon(
                imageVector = Icons.Default.CalendarMonth,
                contentDescription = null,
                tint = Color(0xFF94A3B8),
                modifier = Modifier.size(54.dp)
            )
            Spacer(modifier = Modifier.height(12.dp))
            Text(
                text = "Sin clientes de ruta para el ${dia.name.lowercase().replaceFirstChar { it.uppercase() }}",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = TextoOscuro,
                textAlign = TextAlign.Center
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "Agrega clientes de ruta y asígnales este día para verlos programados aquí.",
                style = MaterialTheme.typography.bodyMedium,
                color = TextoSecundario,
                textAlign = TextAlign.Center
            )
        }
    }
}

package com.rutaagua.camion.ui.screens.reportes

import android.content.Intent
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
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
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.rutaagua.camion.data.local.model.TipoBidon
import com.rutaagua.camion.ui.components.BankingSegmentedTabs
import com.rutaagua.camion.ui.components.ClayBadge
import com.rutaagua.camion.ui.components.ClayButton
import com.rutaagua.camion.ui.components.ClayCard
import com.rutaagua.camion.ui.components.ClayInsetBox
import com.rutaagua.camion.ui.screens.registro.RegistroDiaViewModel
import com.rutaagua.camion.ui.theme.AmbarAdvertencia
import com.rutaagua.camion.ui.theme.AmbarFondo
import com.rutaagua.camion.ui.theme.BidonAzulColor
import com.rutaagua.camion.ui.theme.BidonAzulFondo
import com.rutaagua.camion.ui.theme.BidonMarcadoColor
import com.rutaagua.camion.ui.theme.BidonMarcadoFondo
import com.rutaagua.camion.ui.theme.BidonTransparenteColor
import com.rutaagua.camion.ui.theme.BidonTransparenteFondo
import com.rutaagua.camion.ui.theme.BordeSuave
import com.rutaagua.camion.ui.theme.CamionetaClayPrimary
import com.rutaagua.camion.ui.theme.CamionetaClayPrimaryDark
import com.rutaagua.camion.ui.theme.PichinchaCanvas
import com.rutaagua.camion.ui.theme.PichinchaNavy
import com.rutaagua.camion.ui.theme.PichinchaYellow
import com.rutaagua.camion.ui.theme.RojoAlerta
import com.rutaagua.camion.ui.theme.TextoOscuro
import com.rutaagua.camion.ui.theme.TextoSecundario
import com.rutaagua.camion.ui.theme.VerdeEfectivo
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

enum class PeriodoReporte(val titulo: String) {
    DIA("Día"),
    SEMANAL("Semanal"),
    MENSUAL("Mensual")
}

private data class ResumenPeriodoCalculado(
    val etiquetaRango: String,
    val totalTransacciones: Int,
    val ventaBruta: Double,
    val efectivo: Double,
    val transferencia: Double,
    val creditos: Double,
    val abonos: Double,
    val gastos: Double,
    val efectivoNeto: Double,
    val totalBidones: Int,
    val transparentes: Int,
    val azules: Int,
    val marcados: Int,
    val prestados: Int,
    val gratis: Int,
    val galones: Int,
    val pacas: Int,
    val minis: Int,
    val envasesNuevos: Int,
    val prestamosPorCliente: List<Pair<String, Int>>,
    val ventasPorFecha: List<Pair<String, Double>>
)

/**
 * Apartado de Reportes (Día, Semanal, Mensual) para la cuenta de la Camioneta.
 * Diseño estilo Banca Móvil: directo, reactivo y de alto contraste.
 */
@Composable
fun ReportesCamionetaScreen(
    viewModel: RegistroDiaViewModel,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val transacciones by viewModel.todasLasTransaccionesVehiculoState.collectAsState()
    val gastosTodos by viewModel.todosLosGastosState.collectAsState()
    val clientes by viewModel.todosLosClientesState.collectAsState()

    var periodoSeleccionado by rememberSaveable { mutableStateOf(PeriodoReporte.DIA) }

    val resumen = remember(transacciones, gastosTodos, clientes, periodoSeleccionado) {
        val ahora = Calendar.getInstance()
        val formatoIso = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        val formatoCorto = SimpleDateFormat("dd/MM", Locale.getDefault())
        val hoyIso = formatoIso.format(ahora.time)

        val inicioMillis: Long
        val etiquetaRango: String

        when (periodoSeleccionado) {
            PeriodoReporte.DIA -> {
                val calInicio = Calendar.getInstance().apply {
                    set(Calendar.HOUR_OF_DAY, 0)
                    set(Calendar.MINUTE, 0)
                    set(Calendar.SECOND, 0)
                    set(Calendar.MILLISECOND, 0)
                }
                inicioMillis = calInicio.timeInMillis
                etiquetaRango = "Hoy (${formatoCorto.format(Date())})"
            }
            PeriodoReporte.SEMANAL -> {
                val calInicio = Calendar.getInstance().apply {
                    add(Calendar.DAY_OF_YEAR, -6)
                    set(Calendar.HOUR_OF_DAY, 0)
                    set(Calendar.MINUTE, 0)
                    set(Calendar.SECOND, 0)
                    set(Calendar.MILLISECOND, 0)
                }
                inicioMillis = calInicio.timeInMillis
                etiquetaRango = "Últimos 7 días (${formatoCorto.format(calInicio.time)} - ${formatoCorto.format(Date())})"
            }
            PeriodoReporte.MENSUAL -> {
                val calInicio = Calendar.getInstance().apply {
                    set(Calendar.DAY_OF_MONTH, 1)
                    set(Calendar.HOUR_OF_DAY, 0)
                    set(Calendar.MINUTE, 0)
                    set(Calendar.SECOND, 0)
                    set(Calendar.MILLISECOND, 0)
                }
                inicioMillis = calInicio.timeInMillis
                val mesNombre = SimpleDateFormat("MMMM yyyy", Locale("es", "EC"))
                    .format(Date())
                    .replaceFirstChar { it.uppercase() }
                etiquetaRango = mesNombre
            }
        }

        val transFiltradas = transacciones.filter { t ->
            if (periodoSeleccionado == PeriodoReporte.DIA) {
                t.fechaTexto == hoyIso
            } else {
                t.fecha >= inicioMillis
            }
        }

        val gastosFiltrados = gastosTodos.filter { g ->
            val mismoVehiculo = g.vehiculo == "CAMIONETA"
            val enRango = if (periodoSeleccionado == PeriodoReporte.DIA) {
                g.fechaTexto == hoyIso
            } else {
                g.fecha >= inicioMillis
            }
            mismoVehiculo && enRango
        }

        var transCount = 0
        var azulCount = 0
        var marcCount = 0

        transFiltradas.forEach { t ->
            val sumaRecargaDet = t.recargaTransparentes + t.recargaAzules + t.recargaMarcados
            val rT = if (sumaRecargaDet > 0) t.recargaTransparentes else if (t.tipoBidon == TipoBidon.TRANSPARENTE) t.cantidadBidonesRecarga else 0
            val rA = if (sumaRecargaDet > 0) t.recargaAzules else if (t.tipoBidon == TipoBidon.AZUL) t.cantidadBidonesRecarga else 0
            val rM = if (sumaRecargaDet > 0) t.recargaMarcados else if (t.tipoBidon == TipoBidon.MARCADO) t.cantidadBidonesRecarga else 0

            val sumaPrestDet = t.prestadosTransparentes + t.prestadosAzules + t.prestadosMarcados
            val pT = if (sumaPrestDet > 0) t.prestadosTransparentes else if (t.tipoBidon == TipoBidon.TRANSPARENTE) t.cantidadBidonesPrestados else 0
            val pA = if (sumaPrestDet > 0) t.prestadosAzules else if (t.tipoBidon == TipoBidon.AZUL) t.cantidadBidonesPrestados else 0
            val pM = if (sumaPrestDet > 0) t.prestadosMarcados else if (t.tipoBidon == TipoBidon.MARCADO) t.cantidadBidonesPrestados else 0

            val vT = if (t.tipoBidon == TipoBidon.TRANSPARENTE) t.cantidadBidonesVendidosEnvase else 0
            val vA = if (t.tipoBidon == TipoBidon.AZUL) t.cantidadBidonesVendidosEnvase else 0
            val vM = if (t.tipoBidon == TipoBidon.MARCADO) t.cantidadBidonesVendidosEnvase else 0

            transCount += rT + pT + vT
            azulCount += rA + pA + vA
            marcCount += rM + pM + vM
        }

        val mapaClientes = clientes.associateBy({ it.id }, { it.nombre })
        val prestamosAgrupados = transFiltradas
            .filter { it.cantidadBidonesPrestados > 0 }
            .groupBy { t ->
                t.clienteId?.let { mapaClientes[it] }
                    ?: t.nombreClientePaso?.takeIf { it.isNotBlank() }
                    ?: "Cliente #${t.id}"
            }
            .map { (nombre, lista) -> nombre to lista.sumOf { it.cantidadBidonesPrestados } }

        val agrupadoPorDia = transFiltradas
            .groupBy { it.fechaTexto }
            .entries
            .sortedBy { it.key }
            .takeLast(7)
            .map { (fechaIso, lista) ->
                val etiqueta = fechaIso.takeLast(5).replace("-", "/")
                etiqueta to lista.sumOf { it.totalImporte }
            }

        val efec = transFiltradas.sumOf { it.montoEfectivo }
        val transf = transFiltradas.sumOf { it.montoTransferencia }
        val cred = transFiltradas.sumOf { it.montoCredito }
        val abon = transFiltradas.sumOf { it.montoAbono + it.montoCanceladoDeuda }
        val gast = gastosFiltrados.sumOf { it.monto }
        val bruta = transFiltradas.sumOf { it.totalImporte }
        val neto = (efec + abon) - gast

        ResumenPeriodoCalculado(
            etiquetaRango = etiquetaRango,
            totalTransacciones = transFiltradas.size,
            ventaBruta = bruta,
            efectivo = efec,
            transferencia = transf,
            creditos = cred,
            abonos = abon,
            gastos = gast,
            efectivoNeto = neto,
            totalBidones = transCount + azulCount + marcCount,
            transparentes = transCount,
            azules = azulCount,
            marcados = marcCount,
            prestados = transFiltradas.sumOf { it.cantidadBidonesPrestados },
            gratis = transFiltradas.sumOf { it.cantidadBidonesGratis },
            galones = transFiltradas.sumOf { it.cantidadGalones },
            pacas = transFiltradas.sumOf { it.cantidadPacas },
            minis = transFiltradas.sumOf { it.cantidadMinis },
            envasesNuevos = transFiltradas.sumOf { it.cantidadBidonesVendidosEnvase },
            prestamosPorCliente = prestamosAgrupados,
            ventasPorFecha = agrupadoPorDia
        )
    }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .background(PichinchaCanvas),
        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 12.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        // 1. Selector de Período: Día | Semanal | Mensual
        item {
            BankingSegmentedTabs(
                options = PeriodoReporte.entries,
                selectedOption = periodoSeleccionado,
                labelProvider = { it.titulo },
                onOptionSelected = { periodoSeleccionado = it },
                activeColor = CamionetaClayPrimary
            )
        }

        // 2. Tarjeta Principal estilo Banca Móvil
        item {
            AnimatedContent(targetState = resumen, label = "resumenAnim") { data ->
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(22.dp))
                        .background(
                            Brush.linearGradient(
                                colors = listOf(CamionetaClayPrimaryDark, CamionetaClayPrimary)
                            )
                        )
                        .padding(18.dp)
                ) {
                    Column {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text(
                                    text = "REPORTE ${periodoSeleccionado.titulo.uppercase()}",
                                    color = Color.White.copy(alpha = 0.75f),
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    text = data.etiquetaRango,
                                    color = Color.White,
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Black
                                )
                            }

                            ClayButton(
                                onClick = {
                                    val texto = construirTextoReportePeriodo(periodoSeleccionado, data)
                                    val intent = Intent(Intent.ACTION_SEND).apply {
                                        type = "text/plain"
                                        putExtra(Intent.EXTRA_TEXT, texto)
                                    }
                                    context.startActivity(Intent.createChooser(intent, "Compartir reporte"))
                                },
                                containerColor = PichinchaYellow,
                                contentColor = PichinchaNavy,
                                height = 38.dp,
                                cornerRadius = 12.dp,
                                contentPadding = PaddingValues(horizontal = 12.dp, vertical = 4.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Share,
                                    contentDescription = null,
                                    modifier = Modifier.size(15.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = "Compartir",
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Black,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(14.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.Bottom
                        ) {
                            Column {
                                Text(
                                    text = "Efectivo Neto",
                                    color = Color.White.copy(alpha = 0.8f),
                                    fontSize = 12.sp
                                )
                                Text(
                                    text = "$${String.format(Locale.US, "%.2f", data.efectivoNeto)}",
                                    color = Color.White,
                                    fontSize = 30.sp,
                                    fontWeight = FontWeight.Black
                                )
                            }

                            Column(horizontalAlignment = Alignment.End) {
                                Text(
                                    text = "Venta Bruta",
                                    color = Color.White.copy(alpha = 0.8f),
                                    fontSize = 11.sp
                                )
                                Text(
                                    text = "$${String.format(Locale.US, "%.2f", data.ventaBruta)}",
                                    color = PichinchaYellow,
                                    fontSize = 18.sp,
                                    fontWeight = FontWeight.Black
                                )
                                Text(
                                    text = "${data.totalTransacciones} entregas",
                                    color = Color.White.copy(alpha = 0.75f),
                                    fontSize = 11.sp
                                )
                            }
                        }
                    }
                }
            }
        }

        // 3. Bidones y Productos del Período
        item {
            ClayCard(
                modifier = Modifier.fillMaxWidth(),
                cornerRadius = 20.dp
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Bidones 20L Entregados",
                        color = PichinchaNavy,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Black
                    )
                    ClayBadge(
                        text = "${resumen.totalBidones} total",
                        containerColor = Color(0xFFE6ECF5),
                        contentColor = PichinchaNavy
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    CeldaMetricaProducto(
                        titulo = "PET",
                        valor = "${resumen.transparentes}",
                        fondo = BidonTransparenteFondo,
                        colorTexto = BidonTransparenteColor,
                        modifier = Modifier.weight(1f)
                    )
                    CeldaMetricaProducto(
                        titulo = "Azul",
                        valor = "${resumen.azules}",
                        fondo = BidonAzulFondo,
                        colorTexto = BidonAzulColor,
                        modifier = Modifier.weight(1f)
                    )
                    CeldaMetricaProducto(
                        titulo = "MARCADA",
                        valor = "${resumen.marcados}",
                        fondo = BidonMarcadoFondo,
                        colorTexto = BidonMarcadoColor,
                        modifier = Modifier.weight(1f)
                    )
                }

                Spacer(modifier = Modifier.height(10.dp))

                ClayInsetBox(
                    modifier = Modifier.fillMaxWidth(),
                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 8.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        DatoMiniInline("Prestados", "${resumen.prestados}", AmbarAdvertencia)
                        DatoMiniInline("Cortesía", "${resumen.gratis}", VerdeEfectivo)
                        DatoMiniInline("Galones", "${resumen.galones}", PichinchaNavy)
                        DatoMiniInline("Pacas", "${resumen.pacas}", PichinchaNavy)
                        DatoMiniInline("Minis", "${resumen.minis}", PichinchaNavy)
                    }
                }
            }
        }

        // 4. Desglose Financiero del Período
        item {
            ClayCard(
                modifier = Modifier.fillMaxWidth(),
                cornerRadius = 20.dp
            ) {
                Text(
                    text = "Resumen de Cobros",
                    color = PichinchaNavy,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Black
                )

                Spacer(modifier = Modifier.height(10.dp))

                FilaFinancieraBanca("Efectivo cobrado", resumen.efectivo, VerdeEfectivo)
                FilaFinancieraBanca("Transferencias", resumen.transferencia, PichinchaNavy)
                FilaFinancieraBanca("Abonos recibidos", resumen.abonos, CamionetaClayPrimary)
                FilaFinancieraBanca("Créditos otorgados", resumen.creditos, AmbarAdvertencia)
                FilaFinancieraBanca("Gastos de ruta", -resumen.gastos, RojoAlerta)

                HorizontalDivider(
                    modifier = Modifier.padding(vertical = 8.dp),
                    color = BordeSuave
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Efectivo Neto a Entregar",
                        color = PichinchaNavy,
                        fontWeight = FontWeight.Black,
                        fontSize = 14.sp
                    )
                    Text(
                        text = "$${String.format(Locale.US, "%.2f", resumen.efectivoNeto)}",
                        color = VerdeEfectivo,
                        fontWeight = FontWeight.Black,
                        fontSize = 16.sp
                    )
                }
            }
        }

        // 5. Gráfico de Barras por Día (cuando hay actividad)
        if (resumen.ventasPorFecha.isNotEmpty()) {
            item {
                ClayCard(
                    modifier = Modifier.fillMaxWidth(),
                    cornerRadius = 20.dp
                ) {
                    Text(
                        text = "Evolución Diaria",
                        color = PichinchaNavy,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Black
                    )
                    Spacer(modifier = Modifier.height(12.dp))

                    val maxMonto = (resumen.ventasPorFecha.maxOfOrNull { it.second } ?: 1.0).coerceAtLeast(1.0)
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(110.dp),
                        horizontalArrangement = Arrangement.SpaceEvenly,
                        verticalAlignment = Alignment.Bottom
                    ) {
                        resumen.ventasPorFecha.forEach { (fechaLabel, monto) ->
                            val frac by animateFloatAsState(
                                targetValue = (monto / maxMonto).toFloat().coerceIn(0.08f, 1f),
                                animationSpec = tween(400),
                                label = "barHeight"
                            )
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.Bottom,
                                modifier = Modifier.fillMaxHeight()
                            ) {
                                Text(
                                    text = "$${monto.toInt()}",
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = PichinchaNavy
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Box(
                                    modifier = Modifier
                                        .width(24.dp)
                                        .fillMaxHeight(frac * 0.7f)
                                        .clip(RoundedCornerShape(topStart = 6.dp, topEnd = 6.dp))
                                        .background(CamionetaClayPrimary)
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = fechaLabel,
                                    fontSize = 10.sp,
                                    color = TextoSecundario,
                                    fontWeight = FontWeight.SemiBold
                                )
                            }
                        }
                    }
                }
            }
        }

        // 6. Clientes con Bidones Prestados en el período
        if (resumen.prestamosPorCliente.isNotEmpty()) {
            item {
                ClayCard(
                    modifier = Modifier.fillMaxWidth(),
                    cornerRadius = 20.dp
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Bidones Prestados (${resumen.prestados})",
                            color = PichinchaNavy,
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Black
                        )
                        ClayBadge(
                            text = "${resumen.prestamosPorCliente.size} clientes",
                            containerColor = AmbarFondo,
                            contentColor = AmbarAdvertencia
                        )
                    }
                    Spacer(modifier = Modifier.height(8.dp))

                    resumen.prestamosPorCliente.forEach { (clienteNombre, cant) ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 5.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.weight(1f)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(8.dp)
                                        .background(AmbarAdvertencia, CircleShape)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = clienteNombre,
                                    color = TextoOscuro,
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }
                            Text(
                                text = "$cant bidones",
                                color = AmbarAdvertencia,
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Black
                            )
                        }
                    }
                }
            }
        }

        item {
            Spacer(modifier = Modifier.height(72.dp))
        }
    }
}

@Composable
private fun CeldaMetricaProducto(
    titulo: String,
    valor: String,
    fondo: Color,
    colorTexto: Color,
    modifier: Modifier = Modifier
) {
    Surface(
        color = fondo,
        shape = RoundedCornerShape(14.dp),
        modifier = modifier
    ) {
        Column(
            modifier = Modifier.padding(vertical = 10.dp, horizontal = 8.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = valor,
                color = colorTexto,
                fontSize = 22.sp,
                fontWeight = FontWeight.Black
            )
            Text(
                text = titulo,
                color = colorTexto,
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

@Composable
private fun DatoMiniInline(
    etiqueta: String,
    valor: String,
    color: Color
) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
            text = valor,
            color = color,
            fontSize = 14.sp,
            fontWeight = FontWeight.Black
        )
        Text(
            text = etiqueta,
            color = TextoSecundario,
            fontSize = 10.sp,
            fontWeight = FontWeight.SemiBold
        )
    }
}

@Composable
private fun FilaFinancieraBanca(
    etiqueta: String,
    monto: Double,
    colorMonto: Color
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = etiqueta,
            color = TextoSecundario,
            fontSize = 13.sp,
            fontWeight = FontWeight.Medium
        )
        Text(
            text = "$${String.format(Locale.US, "%.2f", monto)}",
            color = colorMonto,
            fontSize = 13.sp,
            fontWeight = FontWeight.Bold
        )
    }
}

private fun construirTextoReportePeriodo(
    periodo: PeriodoReporte,
    data: ResumenPeriodoCalculado
): String {
    return buildString {
        appendLine("*REPORTE ${periodo.titulo.uppercase()} - CAMIONETA*")
        appendLine("*Período:* ${data.etiquetaRango}")
        appendLine("─────────────────────────")
        appendLine("*BIDONES 20L ENTREGADOS:* ${data.totalBidones}")
        appendLine("  • Transparentes (PET): *${data.transparentes}*")
        appendLine("  • Azules: *${data.azules}*")
        appendLine("  • Marcadas: *${data.marcados}*")
        if (data.prestados > 0) {
            appendLine("*BIDONES PRESTADOS:* ${data.prestados}")
            data.prestamosPorCliente.forEach { (nombre, cant) ->
                appendLine("  ↳ $nombre: *$cant bidones*")
            }
        }
        if (data.gratis > 0) {
            appendLine("*Cortesías:* ${data.gratis}")
        }
        appendLine("*Galones:* ${data.galones} | *Pacas:* ${data.pacas} | *Minis:* ${data.minis}")
        appendLine("─────────────────────────")
        appendLine("*Venta Total Bruta:* $${String.format(Locale.US, "%.2f", data.ventaBruta)}")
        appendLine("*Efectivo:* $${String.format(Locale.US, "%.2f", data.efectivo)}")
        appendLine("*Transferencia:* $${String.format(Locale.US, "%.2f", data.transferencia)}")
        appendLine("*Abonos:* $${String.format(Locale.US, "%.2f", data.abonos)}")
        appendLine("*Créditos:* $${String.format(Locale.US, "%.2f", data.creditos)}")
        appendLine("*Gastos:* $${String.format(Locale.US, "%.2f", data.gastos)}")
        appendLine("*EFECTIVO NETO:* *$${String.format(Locale.US, "%.2f", data.efectivoNeto)}*")
    }
}

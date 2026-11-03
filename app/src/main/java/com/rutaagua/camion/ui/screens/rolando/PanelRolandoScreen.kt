package com.rutaagua.camion.ui.screens.rolando

import android.content.Intent
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
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
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowLeft
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.AccountBalance
import androidx.compose.material.icons.filled.AttachMoney
import androidx.compose.material.icons.filled.BarChart
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.ReceiptLong
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.TrendingUp
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.rutaagua.camion.data.local.entity.GastoExtraEntity
import com.rutaagua.camion.data.local.model.ResumenPrecioCliente
import com.rutaagua.camion.data.local.model.TipoBidon
import com.rutaagua.camion.data.local.model.calcularDesglosePorPrecio
import com.rutaagua.camion.ui.components.BankingSegmentedTabs
import com.rutaagua.camion.ui.components.ClayBadge
import com.rutaagua.camion.ui.components.ClayButton
import com.rutaagua.camion.ui.components.ClayCard
import com.rutaagua.camion.ui.screens.registro.AuditoriaCierreRP3Card
import com.rutaagua.camion.ui.screens.registro.FiltroVehiculoRegistro
import com.rutaagua.camion.ui.screens.registro.RegistroDiaViewModel
import com.rutaagua.camion.ui.screens.reportes.PeriodoReporte
import com.rutaagua.camion.ui.theme.AmbarAdvertencia
import com.rutaagua.camion.ui.theme.AmbarFondo
import com.rutaagua.camion.ui.theme.AzulCorporativo
import com.rutaagua.camion.ui.theme.BordeSutil
import com.rutaagua.camion.ui.theme.FondoGrisClaro
import com.rutaagua.camion.ui.theme.PichinchaNavy
import com.rutaagua.camion.ui.theme.PichinchaYellow
import com.rutaagua.camion.ui.theme.RojoAlerta
import com.rutaagua.camion.ui.theme.RojoAlertaFondo
import com.rutaagua.camion.ui.theme.SuperficieBlanca
import com.rutaagua.camion.ui.theme.TextoOscuro
import com.rutaagua.camion.ui.theme.TextoSecundario
import com.rutaagua.camion.ui.theme.VerdeEfectivo
import com.rutaagua.camion.ui.theme.VerdeEfectivoFondo
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

data class ResumenSemanaFinanciera(
    val numeroSemana: Int,
    val rangoDiasLabel: String,
    val totalEntregas: Int,
    val bidones: Int,
    val galones: Int,
    val pacas: Int,
    val minis: Int,
    val ventaBruta: Double,
    val efectivo: Double,
    val transferencia: Double,
    val creditos: Double,
    val abonos: Double,
    val gastos: Double,
    val ingresoNeto: Double,
    val efectivoNetoCaja: Double
)

private data class ResumenFinancieroPeriodo(
    val etiquetaRango: String,
    val vehiculoEtiqueta: String,
    val totalTransacciones: Int,
    val ventaBruta: Double,
    val efectivo: Double,
    val transferencia: Double,
    val creditos: Double,
    val abonos: Double,
    val gastos: Double,
    val costoPlantaTotal: Double,
    val utilidadBruta: Double,
    val utilidadNeta: Double,
    val ingresoNetoTotal: Double,
    val efectivoNetoCaja: Double,
    val totalBidones: Int,
    val transparentes: Int,
    val azules: Int,
    val marcados: Int,
    val galones: Int,
    val pacas: Int,
    val minis: Int,
    val ventasPorFecha: List<Pair<String, Double>>,
    val desgloseSemanasMes: List<ResumenSemanaFinanciera>,
    val desglosePorPrecio: List<ResumenPrecioCliente>,
    val gastosDetalle: List<GastoExtraEntity>
)

/**
 * Panel Exclusivo para Rolando (Administrador General).
 * 100% enfocado en Contabilidad, Finanzas y Auditoría:
 * - Ventas del día, semana, mes con navegación mensual histórica.
 * - Desglose semanal del mes (Semana 1..5) con Gran Total Mensual.
 * - Control exhaustivo de gastos operativos.
 * - Cálculo de Costos de Agua en Planta, Utilidad Bruta y Utilidad Neta real.
 * - Flujo de Cobros (Efectivo, Transferencias, Créditos Otorgados, Abonos).
 * - Clientes agrupados por precio de agua ($0.80, $0.75...).
 * - Gráfico visual de estadísticas financieras.
 * - Auditoría RP3 de cierre a ciegas vs sistema.
 */
@Composable
fun PanelRolandoScreen(
    viewModel: RegistroDiaViewModel,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val todasTransaccionesGlobal by viewModel.todasLasTransaccionesGlobalState.collectAsState()
    val gastosTodos by viewModel.todosLosGastosState.collectAsState()
    val cierreHoy by viewModel.cierreHoyState.collectAsState()

    var periodoSeleccionado by rememberSaveable { mutableStateOf(PeriodoReporte.DIA) }
    var offsetMeses by rememberSaveable { mutableIntStateOf(0) }
    var filtroVehiculo by rememberSaveable { mutableStateOf(FiltroVehiculoRegistro.CONSOLIDADO) }

    val costoBidon = viewModel.costoPlantaBidon
    val costoGalon = viewModel.costoPlantaGalon
    val costoPaca = viewModel.costoPlantaPaca
    val costoMini = viewModel.costoPlantaMini

    val resumen = remember(
        todasTransaccionesGlobal,
        gastosTodos,
        periodoSeleccionado,
        offsetMeses,
        filtroVehiculo
    ) {
        val ahora = Calendar.getInstance()
        val formatoIso = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        val formatoCorto = SimpleDateFormat("dd/MM", Locale.getDefault())
        val hoyIso = formatoIso.format(ahora.time)

        val vehiculoKey = filtroVehiculo.vehiculoKey
        val vehiculoEtiqueta = when (filtroVehiculo) {
            FiltroVehiculoRegistro.CONSOLIDADO -> "Consolidado (Camión + Camioneta)"
            FiltroVehiculoRegistro.CAMION -> "Camión Principal"
            FiltroVehiculoRegistro.CAMIONETA -> "Camioneta de Reparto"
        }

        val transaccionesVehiculo = if (vehiculoKey == null) {
            todasTransaccionesGlobal
        } else {
            todasTransaccionesGlobal.filter { it.vehiculo == vehiculoKey }
        }

        val gastosVehiculo = if (vehiculoKey == null) {
            gastosTodos
        } else {
            gastosTodos.filter { it.vehiculo == vehiculoKey }
        }

        val etiquetaRango: String
        val prefijoMesTarget: String
        val maxDiaMesTarget: Int
        val inicioMillis: Long

        when (periodoSeleccionado) {
            PeriodoReporte.DIA -> {
                val calInicio = Calendar.getInstance().apply {
                    set(Calendar.HOUR_OF_DAY, 0)
                    set(Calendar.MINUTE, 0)
                    set(Calendar.SECOND, 0)
                    set(Calendar.MILLISECOND, 0)
                }
                inicioMillis = calInicio.timeInMillis
                prefijoMesTarget = hoyIso.take(7)
                maxDiaMesTarget = calInicio.getActualMaximum(Calendar.DAY_OF_MONTH)
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
                prefijoMesTarget = hoyIso.take(7)
                maxDiaMesTarget = calInicio.getActualMaximum(Calendar.DAY_OF_MONTH)
                etiquetaRango = "Últimos 7 días (${formatoCorto.format(calInicio.time)} - ${formatoCorto.format(Date())})"
            }
            PeriodoReporte.MENSUAL -> {
                val calMes = Calendar.getInstance().apply {
                    set(Calendar.DAY_OF_MONTH, 1)
                    set(Calendar.HOUR_OF_DAY, 0)
                    set(Calendar.MINUTE, 0)
                    set(Calendar.SECOND, 0)
                    set(Calendar.MILLISECOND, 0)
                    add(Calendar.MONTH, offsetMeses)
                }
                inicioMillis = calMes.timeInMillis
                prefijoMesTarget = SimpleDateFormat("yyyy-MM", Locale.getDefault()).format(calMes.time)
                maxDiaMesTarget = calMes.getActualMaximum(Calendar.DAY_OF_MONTH)
                etiquetaRango = SimpleDateFormat("MMMM yyyy", Locale("es", "EC"))
                    .format(calMes.time)
                    .replaceFirstChar { it.uppercase() }
            }
        }

        val transFiltradas = transaccionesVehiculo.filter { t ->
            when (periodoSeleccionado) {
                PeriodoReporte.DIA -> t.fechaTexto == hoyIso
                PeriodoReporte.SEMANAL -> t.fecha >= inicioMillis
                PeriodoReporte.MENSUAL -> t.fechaTexto.startsWith(prefijoMesTarget)
            }
        }

        val gastosFiltrados = gastosVehiculo.filter { g ->
            when (periodoSeleccionado) {
                PeriodoReporte.DIA -> g.fechaTexto == hoyIso
                PeriodoReporte.SEMANAL -> g.fecha >= inicioMillis
                PeriodoReporte.MENSUAL -> g.fechaTexto.startsWith(prefijoMesTarget)
            }
        }

        var transCount = 0
        var azulCount = 0
        var marcCount = 0

        transFiltradas.forEach { t ->
            val sumaRecargas = t.recargaTransparentes + t.recargaAzules + t.recargaMarcados
            val rT = if (sumaRecargas > 0) t.recargaTransparentes else if (t.tipoBidon == TipoBidon.TRANSPARENTE) t.cantidadBidonesRecarga else 0
            val rA = if (sumaRecargas > 0) t.recargaAzules else if (t.tipoBidon == TipoBidon.AZUL) t.cantidadBidonesRecarga else 0
            val rM = if (sumaRecargas > 0) t.recargaMarcados else if (t.tipoBidon == TipoBidon.MARCADO) t.cantidadBidonesRecarga else 0

            val sumaPrestados = t.prestadosTransparentes + t.prestadosAzules + t.prestadosMarcados
            val pT = if (sumaPrestados > 0) t.prestadosTransparentes else if (t.tipoBidon == TipoBidon.TRANSPARENTE) t.cantidadBidonesPrestados else 0
            val pA = if (sumaPrestados > 0) t.prestadosAzules else if (t.tipoBidon == TipoBidon.AZUL) t.cantidadBidonesPrestados else 0
            val pM = if (sumaPrestados > 0) t.prestadosMarcados else if (t.tipoBidon == TipoBidon.MARCADO) t.cantidadBidonesPrestados else 0

            val sumaEnvases = t.envasesVendidosTransparentes + t.envasesVendidosAzules + t.envasesVendidosMarcados
            val vT = if (sumaEnvases > 0) t.envasesVendidosTransparentes else if (t.tipoBidon == TipoBidon.TRANSPARENTE) t.cantidadBidonesVendidosEnvase else 0
            val vA = if (sumaEnvases > 0) t.envasesVendidosAzules else if (t.tipoBidon == TipoBidon.AZUL) t.cantidadBidonesVendidosEnvase else 0
            val vM = if (sumaEnvases > 0) t.envasesVendidosMarcados else if (t.tipoBidon == TipoBidon.MARCADO) t.cantidadBidonesVendidosEnvase else 0

            transCount += rT + pT + vT
            azulCount += rA + pA + vA
            marcCount += rM + pM + vM
        }

        // Semanas del mes cuando está en modo MENSUAL
        val desgloseSemanas = if (periodoSeleccionado == PeriodoReporte.MENSUAL) {
            val rangos = listOf(
                Triple(1, 1, 7),
                Triple(2, 8, 14),
                Triple(3, 15, 21),
                Triple(4, 22, 28),
                Triple(5, 29, maxDiaMesTarget)
            ).filter { it.second <= it.third }

            rangos.map { (numSem, diaIni, diaFin) ->
                val txSemana = transFiltradas.filter { tx ->
                    val diaNum = tx.fechaTexto.takeLast(2).toIntOrNull() ?: 0
                    diaNum in diaIni..diaFin
                }
                val gastosSemana = gastosFiltrados.filter { g ->
                    val diaNum = g.fechaTexto.takeLast(2).toIntOrNull() ?: 0
                    diaNum in diaIni..diaFin
                }
                val bidSem = txSemana.sumOf {
                    it.cantidadBidonesRecarga + it.cantidadBidonesPrestados + it.cantidadBidonesVendidosEnvase
                }
                val galSem = txSemana.sumOf { it.cantidadGalones }
                val pacSem = txSemana.sumOf { it.cantidadPacas }
                val minSem = txSemana.sumOf { it.cantidadMinis }
                val brutaSem = txSemana.sumOf { it.totalImporte }
                val efecSem = txSemana.sumOf { it.montoEfectivo }
                val transfSem = txSemana.sumOf { it.montoTransferencia }
                val credSem = txSemana.sumOf { it.montoCredito }
                val abonSem = txSemana.sumOf { it.montoAbono + it.montoCanceladoDeuda }
                val gastSem = gastosSemana.sumOf { it.monto }
                val ingNetoSem = (efecSem + transfSem + abonSem) - gastSem
                val efecNetoSem = (efecSem + abonSem) - gastSem

                ResumenSemanaFinanciera(
                    numeroSemana = numSem,
                    rangoDiasLabel = "Días ${String.format(Locale.US, "%02d", diaIni)}-${String.format(Locale.US, "%02d", diaFin)}",
                    totalEntregas = txSemana.size,
                    bidones = bidSem,
                    galones = galSem,
                    pacas = pacSem,
                    minis = minSem,
                    ventaBruta = brutaSem,
                    efectivo = efecSem,
                    transferencia = transfSem,
                    creditos = credSem,
                    abonos = abonSem,
                    gastos = gastSem,
                    ingresoNeto = ingNetoSem,
                    efectivoNetoCaja = efecNetoSem
                )
            }
        } else {
            emptyList()
        }

        val totalBid = transCount + azulCount + marcCount
        val totalGal = transFiltradas.sumOf { it.cantidadGalones }
        val totalPac = transFiltradas.sumOf { it.cantidadPacas }
        val totalMin = transFiltradas.sumOf { it.cantidadMinis }

        val efec = transFiltradas.sumOf { it.montoEfectivo }
        val transf = transFiltradas.sumOf { it.montoTransferencia }
        val cred = transFiltradas.sumOf { it.montoCredito }
        val abon = transFiltradas.sumOf { it.montoAbono + it.montoCanceladoDeuda }
        val gast = gastosFiltrados.sumOf { it.monto }
        val bruta = transFiltradas.sumOf { it.totalImporte }
        val netoCaja = (efec + abon) - gast
        val ingresoNetoTotal = (efec + transf + abon) - gast

        val costoTotalAguaPlanta = 0.0
        val utilidadBruta = bruta
        val utilidadNeta = bruta - gast

        val agrupadoPorDia = transFiltradas
            .groupBy { it.fechaTexto }
            .entries
            .sortedBy { it.key }
            .takeLast(7)
            .map { (fechaIso, lista) ->
                val etiqueta = fechaIso.takeLast(5).replace("-", "/")
                etiqueta to lista.sumOf { it.totalImporte }
            }

        ResumenFinancieroPeriodo(
            etiquetaRango = etiquetaRango,
            vehiculoEtiqueta = vehiculoEtiqueta,
            totalTransacciones = transFiltradas.size,
            ventaBruta = bruta,
            efectivo = efec,
            transferencia = transf,
            creditos = cred,
            abonos = abon,
            gastos = gast,
            costoPlantaTotal = costoTotalAguaPlanta,
            utilidadBruta = utilidadBruta,
            utilidadNeta = utilidadNeta,
            ingresoNetoTotal = ingresoNetoTotal,
            efectivoNetoCaja = netoCaja,
            totalBidones = totalBid,
            transparentes = transCount,
            azules = azulCount,
            marcados = marcCount,
            galones = totalGal,
            pacas = totalPac,
            minis = totalMin,
            ventasPorFecha = agrupadoPorDia,
            desgloseSemanasMes = desgloseSemanas,
            desglosePorPrecio = calcularDesglosePorPrecio(transFiltradas),
            gastosDetalle = gastosFiltrados.sortedByDescending { it.fecha }
        )
    }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .background(FondoGrisClaro),
        contentPadding = PaddingValues(bottom = 32.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        // 1. Header Ejecutivo y Botón Compartir
        item {
            HeaderPanelRolando(
                fechaDisplay = resumen.etiquetaRango,
                onCompartir = {
                    val texto = construirTextoReporteFinanciero(periodoSeleccionado, resumen)
                    val intent = Intent(Intent.ACTION_SEND).apply {
                        type = "text/plain"
                        putExtra(Intent.EXTRA_TEXT, texto)
                    }
                    context.startActivity(Intent.createChooser(intent, "Compartir reporte contable"))
                }
            )
        }

        // 2. Selectores Contables: Período y Filtro de Vehículo
        item {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                BankingSegmentedTabs(
                    options = PeriodoReporte.entries,
                    selectedOption = periodoSeleccionado,
                    labelProvider = { it.titulo },
                    onOptionSelected = { periodoSeleccionado = it },
                    activeColor = AzulCorporativo
                )

                BankingSegmentedTabs(
                    options = FiltroVehiculoRegistro.entries,
                    selectedOption = filtroVehiculo,
                    labelProvider = { it.titulo },
                    onOptionSelected = { filtroVehiculo = it },
                    activeColor = PichinchaNavy
                )

                // Selector de Mes cuando está en modo MENSUAL
                if (periodoSeleccionado == PeriodoReporte.MENSUAL) {
                    Card(
                        shape = RoundedCornerShape(14.dp),
                        colors = CardDefaults.cardColors(containerColor = SuperficieBlanca),
                        border = BorderStroke(1.dp, BordeSutil),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 12.dp, vertical = 8.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Surface(
                                onClick = { offsetMeses -= 1 },
                                color = FondoGrisClaro,
                                shape = RoundedCornerShape(10.dp),
                                modifier = Modifier.size(36.dp)
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Icon(
                                        imageVector = Icons.AutoMirrored.Filled.KeyboardArrowLeft,
                                        contentDescription = "Mes anterior",
                                        tint = TextoOscuro
                                    )
                                }
                            }

                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text(
                                    text = resumen.etiquetaRango,
                                    fontWeight = FontWeight.Black,
                                    fontSize = 15.sp,
                                    color = PichinchaNavy,
                                    maxLines = 1
                                )
                                Text(
                                    text = if (offsetMeses == 0) "Mes en curso (Semanas 1 a 5)" else "Mes histórico • Toca ▶ para volver",
                                    fontSize = 11.sp,
                                    color = TextoSecundario,
                                    fontWeight = FontWeight.SemiBold,
                                    maxLines = 1
                                )
                            }

                            Surface(
                                onClick = { if (offsetMeses < 0) offsetMeses += 1 },
                                color = if (offsetMeses < 0) FondoGrisClaro else FondoGrisClaro.copy(alpha = 0.4f),
                                shape = RoundedCornerShape(10.dp),
                                modifier = Modifier.size(36.dp)
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Icon(
                                        imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                                        contentDescription = "Mes siguiente",
                                        tint = if (offsetMeses < 0) TextoOscuro else TextoSecundario.copy(alpha = 0.4f)
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }

        // 3. Tarjeta de Estado de Resultados Financieros (Utilidades y Costos de Planta)
        item {
            Column(modifier = Modifier.padding(horizontal = 16.dp)) {
                Text(
                    text = "ESTADO DE RESULTADOS CONTABLE (${periodoSeleccionado.titulo.uppercase()})",
                    style = MaterialTheme.typography.labelSmall,
                    color = TextoSecundario,
                    fontWeight = FontWeight.Black,
                    modifier = Modifier.padding(bottom = 6.dp)
                )

                Card(
                    shape = RoundedCornerShape(18.dp),
                    colors = CardDefaults.cardColors(containerColor = SuperficieBlanca),
                    border = BorderStroke(1.dp, BordeSutil),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Default.TrendingUp,
                                    contentDescription = null,
                                    tint = VerdeEfectivo,
                                    modifier = Modifier.size(22.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = "Rentabilidad Neta",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Black,
                                    color = TextoOscuro
                                )
                            }
                            Surface(
                                color = if (resumen.utilidadNeta >= 0.0) VerdeEfectivoFondo else RojoAlertaFondo,
                                shape = RoundedCornerShape(8.dp)
                            ) {
                                Text(
                                    text = if (resumen.utilidadNeta >= 0.0) "🟢 Rentable" else "🔴 Déficit",
                                    color = if (resumen.utilidadNeta >= 0.0) VerdeEfectivo else RojoAlerta,
                                    fontWeight = FontWeight.Black,
                                    fontSize = 11.sp,
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(14.dp))

                        // Métricas clave en cuadrícula
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            ItemMetricaFinanciera(
                                titulo = "Ventas Totales",
                                valor = "$${String.format(Locale.US, "%.2f", resumen.ventaBruta)}",
                                color = AzulCorporativo,
                                modifier = Modifier.weight(1f)
                            )
                            ItemMetricaFinanciera(
                                titulo = "Cobrado (Efec + Transf)",
                                valor = "$${String.format(Locale.US, "%.2f", resumen.efectivo + resumen.transferencia)}",
                                color = Color(0xFF0284C7),
                                modifier = Modifier.weight(1f)
                            )
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            ItemMetricaFinanciera(
                                titulo = "Gastos de Ruta",
                                valor = "-$${String.format(Locale.US, "%.2f", resumen.gastos)}",
                                color = AmbarAdvertencia,
                                modifier = Modifier.weight(1f)
                            )
                            ItemMetricaFinanciera(
                                titulo = "UTILIDAD NETA",
                                valor = "$${String.format(Locale.US, "%.2f", resumen.utilidadNeta)}",
                                color = if (resumen.utilidadNeta >= 0.0) VerdeEfectivo else RojoAlerta,
                                esDestacado = true,
                                modifier = Modifier.weight(1f)
                            )
                        }

                        HorizontalDivider(modifier = Modifier.padding(vertical = 12.dp), color = BordeSutil)

                        // Costos de Planta Unitarios
                        Text(
                            text = "Costos unitarios de planta configurados:",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = TextoSecundario
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text("Bidón: $${String.format(Locale.US, "%.2f", costoBidon)}", fontSize = 11.sp, color = TextoOscuro, fontWeight = FontWeight.Bold)
                            Text("Galón: $${String.format(Locale.US, "%.2f", costoGalon)}", fontSize = 11.sp, color = TextoOscuro, fontWeight = FontWeight.Bold)
                            Text("Paca: $${String.format(Locale.US, "%.2f", costoPaca)}", fontSize = 11.sp, color = TextoOscuro, fontWeight = FontWeight.Bold)
                            Text("Mini: $${String.format(Locale.US, "%.2f", costoMini)}", fontSize = 11.sp, color = TextoOscuro, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }

        // 4. Resumen de Flujo de Efectivo en Caja y Cartera
        item {
            Column(modifier = Modifier.padding(horizontal = 16.dp)) {
                Text(
                    text = "FLUJO DE COBROS Y ARQUEO (${periodoSeleccionado.titulo.uppercase()})",
                    style = MaterialTheme.typography.labelSmall,
                    color = TextoSecundario,
                    fontWeight = FontWeight.Black,
                    modifier = Modifier.padding(bottom = 6.dp)
                )

                Card(
                    shape = RoundedCornerShape(18.dp),
                    colors = CardDefaults.cardColors(containerColor = SuperficieBlanca),
                    border = BorderStroke(1.dp, BordeSutil),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        FilaFlujoCaja(etiqueta = "💵 Efectivo cobrado:", valor = "$${String.format(Locale.US, "%.2f", resumen.efectivo)}", color = VerdeEfectivo)
                        FilaFlujoCaja(etiqueta = "📲 Transferencias Bancarias:", valor = "$${String.format(Locale.US, "%.2f", resumen.transferencia)}", color = AzulCorporativo)
                        FilaFlujoCaja(etiqueta = "📝 Créditos Otorgados (Fiado):", valor = "$${String.format(Locale.US, "%.2f", resumen.creditos)}", color = RojoAlerta)
                        FilaFlujoCaja(etiqueta = "🤝 Abonos / Deudas Cobradas:", valor = "+$${String.format(Locale.US, "%.2f", resumen.abonos)}", color = Color(0xFF0F766E))
                        FilaFlujoCaja(etiqueta = "🧾 Gastos Operativos:", valor = "-$${String.format(Locale.US, "%.2f", resumen.gastos)}", color = AmbarAdvertencia)
                        HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp), color = BordeSutil)
                        FilaFlujoCaja(etiqueta = "💰 Efectivo Neto en Caja:", valor = "$${String.format(Locale.US, "%.2f", resumen.efectivoNetoCaja)}", color = VerdeEfectivo, negrita = true)
                        FilaFlujoCaja(etiqueta = "🏦 Ingreso Neto Total (+Transf):", valor = "$${String.format(Locale.US, "%.2f", resumen.ingresoNetoTotal)}", color = PichinchaNavy, negrita = true)
                        FilaFlujoCaja(etiqueta = "📦 Total Bidones 20L Vendidos:", valor = "${resumen.totalBidones} botellones", color = TextoOscuro, negrita = true)
                    }
                }
            }
        }

        // 5. DESGLOSE POR SEMANAS DEL MES (Semana 1..5) + GRAN TOTAL (Modo MENSUAL)
        if (periodoSeleccionado == PeriodoReporte.MENSUAL && resumen.desgloseSemanasMes.isNotEmpty()) {
            item {
                Column(modifier = Modifier.padding(horizontal = 16.dp)) {
                    Text(
                        text = "DIVISIÓN POR SEMANAS DEL MES",
                        style = MaterialTheme.typography.labelSmall,
                        color = TextoSecundario,
                        fontWeight = FontWeight.Black,
                        modifier = Modifier.padding(bottom = 6.dp)
                    )

                    Card(
                        shape = RoundedCornerShape(18.dp),
                        colors = CardDefaults.cardColors(containerColor = SuperficieBlanca),
                        border = BorderStroke(1.dp, BordeSutil),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(14.dp)) {
                            resumen.desgloseSemanasMes.forEach { sem ->
                                Surface(
                                    color = if (sem.totalEntregas > 0 || sem.gastos > 0.0) Color(0xFFF8FAFC) else FondoGrisClaro.copy(alpha = 0.45f),
                                    shape = RoundedCornerShape(12.dp),
                                    border = BorderStroke(1.dp, BordeSutil),
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(vertical = 4.dp)
                                ) {
                                    Column(modifier = Modifier.padding(10.dp)) {
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.SpaceBetween,
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Row(verticalAlignment = Alignment.CenterVertically) {
                                                Surface(
                                                    color = AzulCorporativo,
                                                    shape = RoundedCornerShape(6.dp)
                                                ) {
                                                    Text(
                                                        text = "SEMANA ${sem.numeroSemana}",
                                                        color = Color.White,
                                                        fontSize = 10.sp,
                                                        fontWeight = FontWeight.Black,
                                                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                                    )
                                                }
                                                Spacer(modifier = Modifier.width(6.dp))
                                                Text(
                                                    text = sem.rangoDiasLabel,
                                                    color = TextoSecundario,
                                                    fontSize = 11.sp,
                                                    fontWeight = FontWeight.Bold
                                                )
                                            }

                                            Text(
                                                text = "${sem.bidones} bid. • $${String.format(Locale.US, "%.2f", sem.ventaBruta)}",
                                                color = PichinchaNavy,
                                                fontSize = 12.sp,
                                                fontWeight = FontWeight.Black
                                            )
                                        }

                                        Spacer(modifier = Modifier.height(4.dp))

                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.SpaceBetween
                                        ) {
                                            Text("Efec: $${String.format(Locale.US, "%.2f", sem.efectivo)}", color = VerdeEfectivo, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                            Text("Transf: $${String.format(Locale.US, "%.2f", sem.transferencia)}", color = AzulCorporativo, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                            Text("Créd: $${String.format(Locale.US, "%.2f", sem.creditos)}", color = RojoAlerta, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                            Text("Neto: $${String.format(Locale.US, "%.2f", sem.ingresoNeto)}", color = VerdeEfectivo, fontSize = 11.sp, fontWeight = FontWeight.Black)
                                        }
                                    }
                                }
                            }

                            Spacer(modifier = Modifier.height(10.dp))

                            // Gran Total Mensual Acumulado
                            Surface(
                                color = VerdeEfectivoFondo,
                                shape = RoundedCornerShape(12.dp),
                                border = BorderStroke(1.5.dp, VerdeEfectivo.copy(alpha = 0.5f)),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Column(modifier = Modifier.padding(12.dp)) {
                                    Text(
                                        text = "GRAN TOTAL MENSUAL ACUMULADO (${resumen.etiquetaRango.uppercase()})",
                                        fontWeight = FontWeight.Black,
                                        fontSize = 12.sp,
                                        color = PichinchaNavy
                                    )
                                    Spacer(modifier = Modifier.height(6.dp))
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween
                                    ) {
                                        Text("Total Bidones: ${resumen.totalBidones}", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                                        Text("Venta Total: $${String.format(Locale.US, "%.2f", resumen.ventaBruta)}", fontWeight = FontWeight.Black, fontSize = 12.sp, color = PichinchaNavy)
                                    }
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween
                                    ) {
                                        Text("Créditos Mes: $${String.format(Locale.US, "%.2f", resumen.creditos)}", fontSize = 11.sp, color = RojoAlerta, fontWeight = FontWeight.Bold)
                                        Text("Gastos Mes: -$${String.format(Locale.US, "%.2f", resumen.gastos)}", fontSize = 11.sp, color = AmbarAdvertencia, fontWeight = FontWeight.Bold)
                                        Text("Ingreso Neto: $${String.format(Locale.US, "%.2f", resumen.ingresoNetoTotal)}", fontSize = 12.sp, color = VerdeEfectivo, fontWeight = FontWeight.Black)
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        // 6. Gráfico de Estadísticas Financieras
        if (resumen.ventasPorFecha.isNotEmpty()) {
            item {
                Column(modifier = Modifier.padding(horizontal = 16.dp)) {
                    Text(
                        text = "EVOLUCIÓN FINANCIERA RECIENTE",
                        style = MaterialTheme.typography.labelSmall,
                        color = TextoSecundario,
                        fontWeight = FontWeight.Black,
                        modifier = Modifier.padding(bottom = 6.dp)
                    )

                    Card(
                        shape = RoundedCornerShape(18.dp),
                        colors = CardDefaults.cardColors(containerColor = SuperficieBlanca),
                        border = BorderStroke(1.dp, BordeSutil),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(14.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text("Ventas Diarias Registradas", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = TextoOscuro)
                                ClayBadge(text = "${resumen.ventasPorFecha.size} días", containerColor = FondoGrisClaro, contentColor = AzulCorporativo)
                            }

                            Spacer(modifier = Modifier.height(14.dp))

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
                                        label = "barHeightRolando"
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
                                            color = AzulCorporativo
                                        )
                                        Spacer(modifier = Modifier.height(4.dp))
                                        Box(
                                            modifier = Modifier
                                                .width(22.dp)
                                                .fillMaxHeight(frac * 0.7f)
                                                .clip(RoundedCornerShape(topStart = 6.dp, topEnd = 6.dp))
                                                .background(AzulCorporativo)
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
            }
        }

        // 7. Desglose de Clientes por Precio Unitario de Agua
        if (resumen.desglosePorPrecio.isNotEmpty()) {
            item {
                Column(modifier = Modifier.padding(horizontal = 16.dp)) {
                    Text(
                        text = "CLIENTES POR PRECIO DE AGUA (${periodoSeleccionado.titulo.uppercase()})",
                        style = MaterialTheme.typography.labelSmall,
                        color = TextoSecundario,
                        fontWeight = FontWeight.Black,
                        modifier = Modifier.padding(bottom = 6.dp)
                    )

                    Card(
                        shape = RoundedCornerShape(18.dp),
                        colors = CardDefaults.cardColors(containerColor = SuperficieBlanca),
                        border = BorderStroke(1.dp, BordeSutil),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(14.dp)) {
                            resumen.desglosePorPrecio.forEachIndexed { idx, item ->
                                val cliLabel = if (item.cantidadClientes == 1) "1 cliente" else "${item.cantidadClientes} clientes"
                                FilaFlujoCaja(
                                    etiqueta = "Clientes $${String.format(Locale.US, "%.2f", item.precioUnitario)}: $cliLabel (${item.cantidadBidonesCobrados} bid.)",
                                    valor = "Total $${String.format(Locale.US, "%.2f", item.totalImporteAgua)}",
                                    color = AzulCorporativo,
                                    negrita = true
                                )
                                if (idx < resumen.desglosePorPrecio.lastIndex) {
                                    HorizontalDivider(modifier = Modifier.padding(vertical = 4.dp), color = BordeSutil)
                                }
                            }
                        }
                    }
                }
            }
        }

        // 8. Control de Gastos Operativos del Período
        item {
            Column(modifier = Modifier.padding(horizontal = 16.dp)) {
                Text(
                    text = "CONTROL DE GASTOS OPERATIVOS",
                    style = MaterialTheme.typography.labelSmall,
                    color = TextoSecundario,
                    fontWeight = FontWeight.Black,
                    modifier = Modifier.padding(bottom = 6.dp)
                )

                Card(
                    shape = RoundedCornerShape(18.dp),
                    colors = CardDefaults.cardColors(containerColor = SuperficieBlanca),
                    border = BorderStroke(1.dp, BordeSutil),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text("Total Gastos Ingresados", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                            ClayBadge(
                                text = "-$${String.format(Locale.US, "%.2f", resumen.gastos)}",
                                containerColor = RojoAlertaFondo,
                                contentColor = RojoAlerta
                            )
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        if (resumen.gastosDetalle.isEmpty()) {
                            Text("No hay gastos registrados en este período.", fontSize = 12.sp, color = TextoSecundario)
                        } else {
                            resumen.gastosDetalle.forEach { gasto ->
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(vertical = 4.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(text = gasto.concepto, color = TextoOscuro, fontSize = 13.sp, fontWeight = FontWeight.Bold, maxLines = 1, overflow = TextOverflow.Ellipsis)
                                        Text(text = "${gasto.fechaTexto} • ${gasto.vehiculo} • ${gasto.categoria}", color = TextoSecundario, fontSize = 11.sp)
                                    }
                                    Text(text = "-$${String.format(Locale.US, "%.2f", gasto.monto)}", color = RojoAlerta, fontSize = 13.sp, fontWeight = FontWeight.Black)
                                }
                            }
                        }
                    }
                }
            }
        }

        // 9. Auditoría de Cierre a Ciegas RP3 (visible cuando es DÍA)
        if (periodoSeleccionado == PeriodoReporte.DIA && cierreHoy != null) {
            item {
                Column(modifier = Modifier.padding(horizontal = 16.dp)) {
                    Text(
                        text = "AUDITORÍA DE CIERRE DE CAJA RP3 (HOY)",
                        style = MaterialTheme.typography.labelSmall,
                        color = TextoSecundario,
                        fontWeight = FontWeight.Black,
                        modifier = Modifier.padding(bottom = 6.dp)
                    )
                    AuditoriaCierreRP3Card(
                        cierre = cierreHoy,
                        resumen = com.rutaagua.camion.data.local.model.ResumenCierreDia(
                            fechaTexto = viewModel.fechaHoyTexto,
                            totalBidonesVendidos = resumen.totalBidones,
                            totalGalonesVendidos = resumen.galones,
                            totalPacasVendidas = resumen.pacas,
                            totalMinisVendidas = resumen.minis,
                            totalIngresoEfectivo = resumen.efectivo,
                            totalIngresoTransferencia = resumen.transferencia,
                            totalCreditosOtorgados = resumen.creditos,
                            totalAbonosRecibidos = resumen.abonos,
                            totalDeudasCanceladas = 0.0,
                            totalGastosRuta = resumen.gastos,
                            efectivoNetoCaja = resumen.efectivoNetoCaja
                        ),
                        costoPlantaBidon = costoBidon,
                        costoPlantaGalon = costoGalon,
                        costoPlantaPaca = costoPaca,
                        costoPlantaMini = costoMini
                    )
                }
            }
        }
    }
}

@Composable
private fun HeaderPanelRolando(
    fechaDisplay: String,
    onCompartir: () -> Unit
) {
    Surface(
        color = Color(0xFF0F172A), // Azul marino profundo ejecutivo
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 18.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "PANEL CONTABLE & FINANCIERO",
                        fontWeight = FontWeight.Black,
                        fontSize = 17.sp,
                        color = Color.White
                    )
                    Text(
                        text = "Rolando Administrador • $fechaDisplay",
                        fontSize = 12.sp,
                        color = Color(0xFF94A3B8)
                    )
                }

                ClayButton(
                    onClick = onCompartir,
                    containerColor = PichinchaYellow,
                    contentColor = PichinchaNavy,
                    height = 36.dp,
                    cornerRadius = 10.dp,
                    contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp)
                ) {
                    Icon(imageVector = Icons.Default.Share, contentDescription = null, modifier = Modifier.size(14.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Reporte", fontSize = 11.sp, fontWeight = FontWeight.Black)
                }
            }
        }
    }
}

@Composable
private fun ItemMetricaFinanciera(
    titulo: String,
    valor: String,
    color: Color,
    esDestacado: Boolean = false,
    modifier: Modifier = Modifier
) {
    Surface(
        color = if (esDestacado) color.copy(alpha = 0.1f) else FondoGrisClaro,
        shape = RoundedCornerShape(10.dp),
        border = BorderStroke(1.dp, if (esDestacado) color.copy(alpha = 0.3f) else BordeSutil),
        modifier = modifier
    ) {
        Column(modifier = Modifier.padding(10.dp)) {
            Text(text = titulo, fontSize = 11.sp, fontWeight = FontWeight.Bold, color = TextoSecundario)
            Spacer(modifier = Modifier.height(4.dp))
            Text(text = valor, fontSize = if (esDestacado) 17.sp else 15.sp, fontWeight = FontWeight.Black, color = color)
        }
    }
}

@Composable
private fun FilaFlujoCaja(
    etiqueta: String,
    valor: String,
    color: Color,
    negrita: Boolean = false
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
            fontSize = 13.sp,
            color = if (negrita) TextoOscuro else TextoSecundario,
            fontWeight = if (negrita) FontWeight.Bold else FontWeight.Normal,
            modifier = Modifier.weight(1f, fill = false),
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
        Spacer(modifier = Modifier.width(8.dp))
        Text(
            text = valor,
            fontSize = 13.sp,
            fontWeight = FontWeight.Black,
            color = color,
            maxLines = 1,
            softWrap = false
        )
    }
}

private fun construirTextoReporteFinanciero(
    periodo: PeriodoReporte,
    data: ResumenFinancieroPeriodo
): String {
    return buildString {
        appendLine("*📊 REPORTE FINANCIERO & CONTABLE - ATENEA*")
        appendLine("*Unidad:* ${data.vehiculoEtiqueta.uppercase()}")
        appendLine("*Período:* ${data.etiquetaRango} (${periodo.titulo})")
        appendLine("─────────────────────────")
        appendLine("*💵 ESTADO DE RESULTADOS:*")
        appendLine("• *Ventas Totales:* $${String.format(Locale.US, "%.2f", data.ventaBruta)}")
        appendLine("• *Gastos Operativos (Ruta):* -$${String.format(Locale.US, "%.2f", data.gastos)}")
        appendLine("• *GANANCIA NETA:* *$${String.format(Locale.US, "%.2f", data.utilidadNeta)}*")
        appendLine("─────────────────────────")
        appendLine("*🏦 ARQUEO Y FLUJO DE FONDOS:*")
        appendLine("• *Efectivo Cobrado:* $${String.format(Locale.US, "%.2f", data.efectivo)}")
        appendLine("• *Transferencias Bancarias:* $${String.format(Locale.US, "%.2f", data.transferencia)}")
        appendLine("• *Créditos Otorgados (Fiado):* $${String.format(Locale.US, "%.2f", data.creditos)}")
        appendLine("• *Abonos de Deuda:* +$${String.format(Locale.US, "%.2f", data.abonos)}")
        appendLine("• *Efectivo Neto en Caja:* *$${String.format(Locale.US, "%.2f", data.efectivoNetoCaja)}*")
        appendLine("• *INGRESO NETO TOTAL:* *$${String.format(Locale.US, "%.2f", data.ingresoNetoTotal)}*")
        appendLine("─────────────────────────")
        appendLine("*📦 VOLUMEN DE PRODUCTOS:*")
        appendLine("• *Bidones 20L:* ${data.totalBidones} (Azul: ${data.azules} | PET: ${data.transparentes} | Marc: ${data.marcados})")
        appendLine("• *Galones:* ${data.galones} | *Pacas:* ${data.pacas} | *Minis:* ${data.minis}")

        if (periodo == PeriodoReporte.MENSUAL && data.desgloseSemanasMes.isNotEmpty()) {
            appendLine("─────────────────────────")
            appendLine("*📅 DIVISIÓN POR SEMANAS DEL MES:*")
            data.desgloseSemanasMes.forEach { sem ->
                appendLine("• *Semana ${sem.numeroSemana} (${sem.rangoDiasLabel}):* ${sem.bidones} bid. | Venta: $${String.format(Locale.US, "%.2f", sem.ventaBruta)} | Créd: $${String.format(Locale.US, "%.2f", sem.creditos)} | Gastos: -$${String.format(Locale.US, "%.2f", sem.gastos)} | Neto: *$${String.format(Locale.US, "%.2f", sem.ingresoNeto)}*")
            }
        }

        if (data.desglosePorPrecio.isNotEmpty()) {
            appendLine("─────────────────────────")
            appendLine("*🏷️ CLIENTES POR PRECIO DE AGUA:*")
            data.desglosePorPrecio.forEach { item ->
                val etiq = "$${String.format(Locale.US, "%.2f", item.precioUnitario)}"
                appendLine("• *Clientes $etiq:* ${item.cantidadClientes} clientes (${item.cantidadBidonesCobrados} bid.) → Total: *$${String.format(Locale.US, "%.2f", item.totalImporteAgua)}*")
            }
        }

        if (data.gastosDetalle.isNotEmpty()) {
            appendLine("─────────────────────────")
            appendLine("*🧾 GASTOS REGISTRADOS:*")
            data.gastosDetalle.forEach { g ->
                appendLine("• ${g.fechaTexto} [${g.vehiculo}] ${g.concepto}: -$${String.format(Locale.US, "%.2f", g.monto)}")
            }
        }
    }
}

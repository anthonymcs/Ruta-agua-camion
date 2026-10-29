package com.rutaagua.camion.ui.screens.reportes

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
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.rutaagua.camion.data.local.entity.GastoExtraEntity
import com.rutaagua.camion.data.local.model.ResumenPrecioCliente
import com.rutaagua.camion.data.local.model.TipoBidon
import com.rutaagua.camion.data.local.model.TipoVehiculo
import com.rutaagua.camion.data.local.model.calcularDesglosePorPrecio
import com.rutaagua.camion.data.local.prefs.RolUsuario
import com.rutaagua.camion.ui.components.BankingSegmentedTabs
import com.rutaagua.camion.ui.components.ClayBadge
import com.rutaagua.camion.ui.components.ClayButton
import com.rutaagua.camion.ui.components.ClayCard
import com.rutaagua.camion.ui.components.ClayInsetBox
import com.rutaagua.camion.ui.components.obtenerClayThemePorVehiculo
import com.rutaagua.camion.ui.screens.registro.FiltroVehiculoRegistro
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
import com.rutaagua.camion.ui.theme.FondoGrisClaro
import com.rutaagua.camion.ui.theme.PichinchaCanvas
import com.rutaagua.camion.ui.theme.PichinchaNavy
import com.rutaagua.camion.ui.theme.PichinchaYellow
import com.rutaagua.camion.ui.theme.RojoAlerta
import com.rutaagua.camion.ui.theme.RojoAlertaFondo
import com.rutaagua.camion.ui.theme.TextoOscuro
import com.rutaagua.camion.ui.theme.TextoSecundario
import com.rutaagua.camion.ui.theme.VerdeEfectivo
import com.rutaagua.camion.ui.theme.VerdeEfectivoFondo
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

enum class PeriodoReporte(val titulo: String) {
    DIA("Día"),
    SEMANAL("Semanal"),
    MENSUAL("Mensual")
}

data class ResumenSemanaMes(
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

private data class ResumenPeriodoCalculado(
    val etiquetaRango: String,
    val vehiculoEtiqueta: String,
    val totalTransacciones: Int,
    val ventaBruta: Double,
    val efectivo: Double,
    val transferencia: Double,
    val creditos: Double,
    val abonos: Double,
    val gastos: Double,
    val ingresoNetoTotal: Double,
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
    val ventasPorFecha: List<Pair<String, Double>>,
    val desgloseSemanasMes: List<ResumenSemanaMes>,
    val desglosePorPrecio: List<ResumenPrecioCliente>,
    val gastosDetalle: List<GastoExtraEntity>
)

/**
 * Apartado de Reportes (Día, Semanal, Mensual) para Camión, Camioneta y Administración (Rolando).
 * Incluye desglose por semanas del mes (Semana 1 a 5), gastos reflejados, ingreso neto,
 * clientes por precio y gran total mensual.
 */
@Composable
fun ReportesCamionetaScreen(
    viewModel: RegistroDiaViewModel,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val todasTransaccionesGlobal by viewModel.todasLasTransaccionesGlobalState.collectAsState()
    val gastosTodos by viewModel.todosLosGastosState.collectAsState()
    val clientes by viewModel.todosLosClientesState.collectAsState()
    val vehiculoActivo by viewModel.vehiculoActivoState.collectAsState()
    val rolActivo by viewModel.rolActivo.collectAsState()
    val clayTheme = obtenerClayThemePorVehiculo(vehiculoActivo)

    var periodoSeleccionado by rememberSaveable { mutableStateOf(PeriodoReporte.MENSUAL) }
    var offsetMeses by rememberSaveable { mutableIntStateOf(0) }
    var filtroVehiculoReporte by rememberSaveable(vehiculoActivo, rolActivo) {
        mutableStateOf(
            if (rolActivo == RolUsuario.ADMIN_ROLANDO) {
                FiltroVehiculoRegistro.CONSOLIDADO
            } else if (vehiculoActivo == TipoVehiculo.CAMIONETA) {
                FiltroVehiculoRegistro.CAMIONETA
            } else {
                FiltroVehiculoRegistro.CAMION
            }
        )
    }

    val resumen = remember(
        todasTransaccionesGlobal,
        gastosTodos,
        clientes,
        periodoSeleccionado,
        offsetMeses,
        filtroVehiculoReporte
    ) {
        val ahora = Calendar.getInstance()
        val formatoIso = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        val formatoCorto = SimpleDateFormat("dd/MM", Locale.getDefault())
        val hoyIso = formatoIso.format(ahora.time)

        val vehiculoKey = filtroVehiculoReporte.vehiculoKey
        val vehiculoEtiqueta = when (filtroVehiculoReporte) {
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

        // Cálculo de Semanas del Mes (Semana 1..5) cuando el reporte es MENSUAL
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

                ResumenSemanaMes(
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
        val netoCaja = (efec + abon) - gast
        val ingresoNetoTotal = (efec + transf + abon) - gast

        ResumenPeriodoCalculado(
            etiquetaRango = etiquetaRango,
            vehiculoEtiqueta = vehiculoEtiqueta,
            totalTransacciones = transFiltradas.size,
            ventaBruta = bruta,
            efectivo = efec,
            transferencia = transf,
            creditos = cred,
            abonos = abon,
            gastos = gast,
            ingresoNetoTotal = ingresoNetoTotal,
            efectivoNeto = netoCaja,
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
            ventasPorFecha = agrupadoPorDia,
            desgloseSemanasMes = desgloseSemanas,
            desglosePorPrecio = calcularDesglosePorPrecio(transFiltradas),
            gastosDetalle = gastosFiltrados.sortedByDescending { it.fecha }
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
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                BankingSegmentedTabs(
                    options = PeriodoReporte.entries,
                    selectedOption = periodoSeleccionado,
                    labelProvider = { it.titulo },
                    onOptionSelected = { periodoSeleccionado = it },
                    activeColor = clayTheme.primaryColor
                )

                if (rolActivo == RolUsuario.ADMIN_ROLANDO) {
                    BankingSegmentedTabs(
                        options = FiltroVehiculoRegistro.entries,
                        selectedOption = filtroVehiculoReporte,
                        labelProvider = { it.titulo },
                        onOptionSelected = { filtroVehiculoReporte = it },
                        activeColor = PichinchaNavy
                    )
                }

                // Selector de Mes cuando está en modo MENSUAL
                if (periodoSeleccionado == PeriodoReporte.MENSUAL) {
                    ClayCard(
                        modifier = Modifier.fillMaxWidth(),
                        cornerRadius = 16.dp,
                        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 8.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
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

        // 2. Tarjeta Principal estilo Banca Móvil con Ingreso Neto y Venta Bruta
        item {
            AnimatedContent(targetState = resumen, label = "resumenAnim") { data ->
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(22.dp))
                        .background(
                            Brush.linearGradient(
                                colors = listOf(clayTheme.primaryDarkColor, clayTheme.primaryColor)
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
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = "REPORTE ${periodoSeleccionado.titulo.uppercase()} • ${data.vehiculoEtiqueta.uppercase()}",
                                    color = Color.White.copy(alpha = 0.8f),
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                                Text(
                                    text = data.etiquetaRango,
                                    color = Color.White,
                                    fontSize = 15.sp,
                                    fontWeight = FontWeight.Black,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }

                            Spacer(modifier = Modifier.width(8.dp))

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
                                    softWrap = false,
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
                                    text = "Ingreso Neto (Efec + Transf + Abonos - Gastos)",
                                    color = Color.White.copy(alpha = 0.85f),
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.SemiBold
                                )
                                Text(
                                    text = "$${String.format(Locale.US, "%.2f", data.ingresoNetoTotal)}",
                                    color = Color.White,
                                    fontSize = 30.sp,
                                    fontWeight = FontWeight.Black
                                )
                                Text(
                                    text = "Efectivo Neto Caja: $${String.format(Locale.US, "%.2f", data.efectivoNeto)}",
                                    color = PichinchaYellow,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold
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
                                    color = Color.White.copy(alpha = 0.8f),
                                    fontSize = 11.sp
                                )
                            }
                        }
                    }
                }
            }
        }

        // 3. DESGLOSE POR SEMANAS DEL MES (Semana 1 a 5) + GRAN TOTAL ABAJO (Cuando es MENSUAL)
        if (periodoSeleccionado == PeriodoReporte.MENSUAL && resumen.desgloseSemanasMes.isNotEmpty()) {
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
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "Desglose por Semanas del Mes",
                                color = PichinchaNavy,
                                fontSize = 15.sp,
                                fontWeight = FontWeight.Black,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                            Text(
                                text = "División semanal de bidones, ventas, créditos, gastos e ingreso neto",
                                color = TextoSecundario,
                                fontSize = 11.sp,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                        ClayBadge(
                            text = resumen.etiquetaRango,
                            containerColor = Color(0xFFE6ECF5),
                            contentColor = PichinchaNavy
                        )
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    resumen.desgloseSemanasMes.forEach { sem ->
                        Surface(
                            color = if (sem.totalEntregas > 0 || sem.gastos > 0.0) Color(0xFFF8FAFC) else FondoGrisClaro.copy(alpha = 0.45f),
                            shape = RoundedCornerShape(14.dp),
                            border = BorderStroke(1.dp, BordeSuave),
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp)
                        ) {
                            Column(modifier = Modifier.padding(12.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Surface(
                                            color = clayTheme.primaryColor,
                                            shape = RoundedCornerShape(8.dp)
                                        ) {
                                            Text(
                                                text = "SEMANA ${sem.numeroSemana}",
                                                color = Color.White,
                                                fontSize = 11.sp,
                                                fontWeight = FontWeight.Black,
                                                maxLines = 1,
                                                softWrap = false,
                                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                                            )
                                        }
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Text(
                                            text = sem.rangoDiasLabel,
                                            color = TextoSecundario,
                                            fontSize = 11.sp,
                                            fontWeight = FontWeight.Bold,
                                            maxLines = 1
                                        )
                                    }

                                    Text(
                                        text = "${sem.bidones} bidones • $${String.format(Locale.US, "%.2f", sem.ventaBruta)}",
                                        color = PichinchaNavy,
                                        fontSize = 13.sp,
                                        fontWeight = FontWeight.Black,
                                        maxLines = 1
                                    )
                                }

                                Spacer(modifier = Modifier.height(6.dp))

                                // Sub-métricas de la semana
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text(
                                        text = "Efec: $${String.format(Locale.US, "%.2f", sem.efectivo)}",
                                        color = VerdeEfectivo,
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                    Text(
                                        text = "Transf: $${String.format(Locale.US, "%.2f", sem.transferencia)}",
                                        color = PichinchaNavy,
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                    Text(
                                        text = "Créd: $${String.format(Locale.US, "%.2f", sem.creditos)}",
                                        color = AmbarAdvertencia,
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                }

                                Spacer(modifier = Modifier.height(2.dp))

                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text(
                                        text = "Abonos: $${String.format(Locale.US, "%.2f", sem.abonos)}",
                                        color = clayTheme.primaryDarkColor,
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                    Text(
                                        text = "Gastos: -$${String.format(Locale.US, "%.2f", sem.gastos)}",
                                        color = RojoAlerta,
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                    Text(
                                        text = "Neto: $${String.format(Locale.US, "%.2f", sem.ingresoNeto)}",
                                        color = VerdeEfectivo,
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Black
                                    )
                                }

                                if (sem.galones > 0 || sem.pacas > 0 || sem.minis > 0) {
                                    Spacer(modifier = Modifier.height(3.dp))
                                    Text(
                                        text = "Otros: ${sem.galones} galones • ${sem.pacas} pacas • ${sem.minis} minis",
                                        color = TextoSecundario,
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.SemiBold
                                    )
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    // GRAN TOTAL MENSUAL ABAJO DE LAS SEMANAS
                    Surface(
                        color = VerdeEfectivoFondo,
                        shape = RoundedCornerShape(14.dp),
                        border = BorderStroke(1.5.dp, VerdeEfectivo.copy(alpha = 0.5f)),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            Text(
                                text = "TOTAL MENSUAL ACUMULADO (${resumen.etiquetaRango.uppercase()})",
                                fontWeight = FontWeight.Black,
                                fontSize = 12.sp,
                                color = PichinchaNavy,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                            Spacer(modifier = Modifier.height(6.dp))
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(
                                    text = "Total Bidones: ${resumen.totalBidones}",
                                    fontWeight = FontWeight.Black,
                                    fontSize = 12.sp,
                                    color = TextoOscuro
                                )
                                Text(
                                    text = "Venta Total: $${String.format(Locale.US, "%.2f", resumen.ventaBruta)}",
                                    fontWeight = FontWeight.Black,
                                    fontSize = 12.sp,
                                    color = PichinchaNavy
                                )
                            }
                            Spacer(modifier = Modifier.height(2.dp))
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(
                                    text = "Gal: ${resumen.galones} • Pacas: ${resumen.pacas} • Minis: ${resumen.minis}",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 11.sp,
                                    color = TextoSecundario
                                )
                                Text(
                                    text = "Créditos Mes: $${String.format(Locale.US, "%.2f", resumen.creditos)}",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 11.sp,
                                    color = AmbarAdvertencia
                                )
                            }
                            Spacer(modifier = Modifier.height(2.dp))
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(
                                    text = "Gastos Mes: -$${String.format(Locale.US, "%.2f", resumen.gastos)}",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 11.sp,
                                    color = RojoAlerta
                                )
                                Text(
                                    text = "Ingreso Neto Mes: $${String.format(Locale.US, "%.2f", resumen.ingresoNetoTotal)}",
                                    fontWeight = FontWeight.Black,
                                    fontSize = 13.sp,
                                    color = VerdeEfectivo
                                )
                            }
                        }
                    }
                }
            }
        }

        // 4. Bidones en General, Galones, Pacas y Minis del Período
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
                        text = "Bidones, Galones, Pacas y Minis",
                        color = PichinchaNavy,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Black,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    ClayBadge(
                        text = "${resumen.totalBidones} bidones",
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

        // 5. Clientes por Precio de Agua (Desglose solicitado por Rolando)
        if (resumen.desglosePorPrecio.isNotEmpty()) {
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
                            text = "Clientes por Precio de Agua",
                            color = PichinchaNavy,
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Black
                        )
                        ClayBadge(
                            text = "${resumen.desglosePorPrecio.sumOf { it.cantidadClientes }} clientes",
                            containerColor = Color(0xFFE6ECF5),
                            contentColor = PichinchaNavy
                        )
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    resumen.desglosePorPrecio.forEach { itemPrecio ->
                        val etiquetaPrecio = "$${String.format(Locale.US, "%.2f", itemPrecio.precioUnitario)}"
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Clientes $etiquetaPrecio: ${itemPrecio.cantidadClientes} clientes (${itemPrecio.cantidadBidonesCobrados} bid.)",
                                color = TextoOscuro,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis,
                                modifier = Modifier.weight(1f)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "Total $${String.format(Locale.US, "%.2f", itemPrecio.totalImporteAgua)}",
                                color = VerdeEfectivo,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Black,
                                maxLines = 1
                            )
                        }
                    }
                }
            }
        }

        // 6. Desglose Financiero e Ingreso Neto del Período
        item {
            ClayCard(
                modifier = Modifier.fillMaxWidth(),
                cornerRadius = 20.dp
            ) {
                Text(
                    text = "Resumen Financiero e Ingreso Neto",
                    color = PichinchaNavy,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Black
                )

                Spacer(modifier = Modifier.height(10.dp))

                FilaFinancieraBanca("Venta Total Facturada", resumen.ventaBruta, PichinchaNavy)
                FilaFinancieraBanca("Efectivo cobrado en ventas", resumen.efectivo, VerdeEfectivo)
                FilaFinancieraBanca("Transferencias bancarias", resumen.transferencia, PichinchaNavy)
                FilaFinancieraBanca("Abonos recibidos de deuda", resumen.abonos, clayTheme.primaryColor)
                FilaFinancieraBanca("Créditos otorgados (fiado)", resumen.creditos, AmbarAdvertencia)
                FilaFinancieraBanca("Gastos ingresados en ruta", -resumen.gastos, RojoAlerta)

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
                        text = "Efectivo Neto en Caja",
                        color = TextoOscuro,
                        fontWeight = FontWeight.Bold,
                        fontSize = 13.sp
                    )
                    Text(
                        text = "$${String.format(Locale.US, "%.2f", resumen.efectivoNeto)}",
                        color = PichinchaNavy,
                        fontWeight = FontWeight.Black,
                        fontSize = 15.sp
                    )
                }

                Spacer(modifier = Modifier.height(4.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "INGRESO NETO TOTAL (C/ TRANSF.)",
                        color = PichinchaNavy,
                        fontWeight = FontWeight.Black,
                        fontSize = 14.sp
                    )
                    Text(
                        text = "$${String.format(Locale.US, "%.2f", resumen.ingresoNetoTotal)}",
                        color = VerdeEfectivo,
                        fontWeight = FontWeight.Black,
                        fontSize = 17.sp
                    )
                }
            }
        }

        // 7. Gastos Ingresados en el Período (Reflejados detalladamente)
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
                        text = "Gastos Ingresados en el Reporte",
                        color = PichinchaNavy,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Black
                    )
                    ClayBadge(
                        text = "-$${String.format(Locale.US, "%.2f", resumen.gastos)}",
                        containerColor = RojoAlertaFondo,
                        contentColor = RojoAlerta
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))

                if (resumen.gastosDetalle.isEmpty()) {
                    Text(
                        text = "No se registraron gastos en este período.",
                        color = TextoSecundario,
                        fontSize = 12.sp
                    )
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
                                Text(
                                    text = gasto.concepto,
                                    color = TextoOscuro,
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Bold,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                                Text(
                                    text = "${gasto.fechaTexto} • ${gasto.vehiculo} • ${gasto.categoria}",
                                    color = TextoSecundario,
                                    fontSize = 11.sp,
                                    maxLines = 1
                                )
                            }
                            Text(
                                text = "-$${String.format(Locale.US, "%.2f", gasto.monto)}",
                                color = RojoAlerta,
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Black
                            )
                        }
                    }
                }
            }
        }

        // 8. Gráfico de Barras por Día (cuando hay actividad)
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
                                        .background(clayTheme.primaryColor)
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

        // 9. Clientes con Bidones Prestados en el período
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
        appendLine("*📊 REPORTE ${periodo.titulo.uppercase()} - ${data.vehiculoEtiqueta.uppercase()}*")
        appendLine("*Período:* ${data.etiquetaRango}")
        appendLine("─────────────────────────")

        if (periodo == PeriodoReporte.MENSUAL && data.desgloseSemanasMes.isNotEmpty()) {
            appendLine("*📅 DIVISIÓN POR SEMANAS DEL MES:*")
            data.desgloseSemanasMes.forEach { sem ->
                appendLine(
                    "• *Semana ${sem.numeroSemana} (${sem.rangoDiasLabel}):* ${sem.bidones} bidones | " +
                            "Venta: *$${String.format(Locale.US, "%.2f", sem.ventaBruta)}* | " +
                            "Créditos: $${String.format(Locale.US, "%.2f", sem.creditos)} | " +
                            "Abonos: $${String.format(Locale.US, "%.2f", sem.abonos)} | " +
                            "Gastos: -$${String.format(Locale.US, "%.2f", sem.gastos)} | " +
                            "Neto: *$${String.format(Locale.US, "%.2f", sem.ingresoNeto)}*"
                )
            }
            appendLine("─────────────────────────")
        }

        appendLine("*💧 PRODUCTOS VENDIDOS EN GENERAL:*")
        appendLine("• *Bidones 20L Totales:* *${data.totalBidones}* (PET: ${data.transparentes} | Azul: ${data.azules} | Marcada: ${data.marcados})")
        appendLine("• *Galones:* *${data.galones}* | *Pacas:* *${data.pacas}* | *Minis:* *${data.minis}*")
        if (data.prestados > 0) {
            appendLine("• *Bidones Prestados:* ${data.prestados}")
            data.prestamosPorCliente.forEach { (nombre, cant) ->
                appendLine("  ↳ $nombre: *$cant bidones*")
            }
        }
        if (data.gratis > 0) {
            appendLine("• *Cortesías:* ${data.gratis}")
        }

        if (data.desglosePorPrecio.isNotEmpty()) {
            appendLine("─────────────────────────")
            appendLine("*🏷️ CLIENTES POR PRECIO DE AGUA:*")
            data.desglosePorPrecio.forEach { item ->
                val etiq = "$${String.format(Locale.US, "%.2f", item.precioUnitario)}"
                appendLine(
                    "• *Clientes $etiq:* ${item.cantidadClientes} clientes (${item.cantidadBidonesCobrados} bidones) → *Total: $${String.format(Locale.US, "%.2f", item.totalImporteAgua)}*"
                )
            }
        }

        if (data.gastosDetalle.isNotEmpty()) {
            appendLine("─────────────────────────")
            appendLine("*⛽ GASTOS INGRESADOS EN EL PERÍODO:*")
            data.gastosDetalle.forEach { g ->
                appendLine("• ${g.fechaTexto} - ${g.concepto}: *-$${String.format(Locale.US, "%.2f", g.monto)}*")
            }
        }

        appendLine("─────────────────────────")
        appendLine("*💰 GRAN TOTAL DEL PERÍODO (${data.etiquetaRango.uppercase()}):*")
        appendLine("• *Total Bidones:* ${data.totalBidones} | *Gal:* ${data.galones} | *Pacas:* ${data.pacas} | *Minis:* ${data.minis}")
        appendLine("• *Venta Total Bruta:* $${String.format(Locale.US, "%.2f", data.ventaBruta)}")
        appendLine("• *Suma Efectivo:* $${String.format(Locale.US, "%.2f", data.efectivo)}")
        appendLine("• *Suma Transferencias:* $${String.format(Locale.US, "%.2f", data.transferencia)}")
        appendLine("• *Suma Abonos:* $${String.format(Locale.US, "%.2f", data.abonos)}")
        appendLine("• *Suma Créditos:* $${String.format(Locale.US, "%.2f", data.creditos)}")
        appendLine("• *Suma Gastos:* -$${String.format(Locale.US, "%.2f", data.gastos)}")
        appendLine("• *Efectivo Neto en Caja:* *$${String.format(Locale.US, "%.2f", data.efectivoNeto)}*")
        appendLine("• *INGRESO NETO TOTAL:* *$${String.format(Locale.US, "%.2f", data.ingresoNetoTotal)}*")
    }
}

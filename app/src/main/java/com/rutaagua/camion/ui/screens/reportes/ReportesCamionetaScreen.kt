package com.rutaagua.camion.ui.screens.reportes

import android.content.Intent
import androidx.compose.animation.AnimatedContent
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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowLeft
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.LocalShipping
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Share
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
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.rutaagua.camion.data.local.model.TipoBidon
import com.rutaagua.camion.data.local.model.TipoVehiculo
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

data class EntregaBidonDetalle(
    val transaccionId: Long,
    val clienteNombre: String,
    val aliasCliente: String?,
    val esClientePaso: Boolean,
    val fechaTexto: String,
    val horaTexto: String,
    val fechaMillis: Long,
    val transparentes: Int,
    val azules: Int,
    val marcados: Int,
    val totalBidones: Int,
    val envasesNuevos: Int,
    val prestados: Int,
    val devueltos: Int,
    val cortesias: Int,
    val galones: Int,
    val pacas: Int,
    val minis: Int,
    val nota: String
)

data class ResumenBidonesPeriodo(
    val etiquetaRango: String,
    val vehiculoEtiqueta: String,
    val totalEntregas: Int,
    val totalBidones: Int,
    val transparentes: Int,
    val azules: Int,
    val marcados: Int,
    val envasesNuevos: Int,
    val prestados: Int,
    val devueltos: Int,
    val cortesias: Int,
    val galones: Int,
    val pacas: Int,
    val minis: Int,
    val entregas: List<EntregaBidonDetalle>,
    val prestamosPorCliente: List<Pair<String, Int>>
)

/**
 * Reportes Operativos de Entrega de Bidones (Día, Semanal, Mensual).
 * Exclusivo para Choferes y Operadores:
 * Muestra cuántos bidones se dejaron en el período, qué tipos exactos (Azul, PET, Marcada),
 * y la lista detallada de "¿A quién se le dejó los bidones?", con opción de compartir por WhatsApp.
 */
@OptIn(ExperimentalLayoutApi::class)
@Composable
fun ReportesCamionetaScreen(
    viewModel: RegistroDiaViewModel,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val todasTransaccionesGlobal by viewModel.todasLasTransaccionesGlobalState.collectAsState()
    val clientes by viewModel.todosLosClientesState.collectAsState()
    val vehiculoActivo by viewModel.vehiculoActivoState.collectAsState()
    val rolActivo by viewModel.rolActivo.collectAsState()
    val clayTheme = obtenerClayThemePorVehiculo(vehiculoActivo)

    var periodoSeleccionado by rememberSaveable { mutableStateOf(PeriodoReporte.DIA) }
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
        clientes,
        periodoSeleccionado,
        offsetMeses,
        filtroVehiculoReporte
    ) {
        val ahora = Calendar.getInstance()
        val formatoIso = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        val formatoCorto = SimpleDateFormat("dd/MM", Locale.getDefault())
        val formatoHora = SimpleDateFormat("hh:mm a", Locale.getDefault())
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

        val etiquetaRango: String
        val prefijoMesTarget: String
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

        val mapaClientes = clientes.associateBy { it.id }

        var transCount = 0
        var azulCount = 0
        var marcCount = 0
        var envasesCount = 0
        var prestCount = 0
        var devCount = 0
        var gratisCount = 0
        var galCount = 0
        var pacCount = 0
        var minCount = 0

        val listaEntregas = mutableListOf<EntregaBidonDetalle>()

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

            val tT = rT + pT + vT
            val tA = rA + pA + vA
            val tM = rM + pM + vM
            val totalBidonesEntrega = tT + tA + tM + t.cantidadBidonesGratis

            transCount += tT
            azulCount += tA
            marcCount += tM
            envasesCount += (vT + vA + vM)
            prestCount += t.cantidadBidonesPrestados
            devCount += t.cantidadBidonesDevueltos
            gratisCount += t.cantidadBidonesGratis
            galCount += t.cantidadGalones
            pacCount += t.cantidadPacas
            minCount += t.cantidadMinis

            val clienteObj = t.clienteId?.let { mapaClientes[it] }
            val nombreResolv = clienteObj?.nombre
                ?: t.nombreClientePaso?.takeIf { it.isNotBlank() }
                ?: "Cliente de paso"
            val aliasResolv = clienteObj?.alias?.takeIf { it.isNotBlank() }

            val horaFormateada = try {
                formatoHora.format(Date(t.fecha))
            } catch (_: Exception) {
                ""
            }

            listaEntregas.add(
                EntregaBidonDetalle(
                    transaccionId = t.id,
                    clienteNombre = nombreResolv,
                    aliasCliente = aliasResolv,
                    esClientePaso = t.clienteId == null,
                    fechaTexto = t.fechaTexto,
                    horaTexto = horaFormateada,
                    fechaMillis = t.fecha,
                    transparentes = tT,
                    azules = tA,
                    marcados = tM,
                    totalBidones = totalBidonesEntrega,
                    envasesNuevos = vT + vA + vM,
                    prestados = t.cantidadBidonesPrestados,
                    devueltos = t.cantidadBidonesDevueltos,
                    cortesias = t.cantidadBidonesGratis,
                    galones = t.cantidadGalones,
                    pacas = t.cantidadPacas,
                    minis = t.cantidadMinis,
                    nota = t.nota
                )
            )
        }

        // Préstamos pendientes agrupados por cliente en este período
        val prestamosAgrupados = transFiltradas
            .filter { it.cantidadBidonesPrestados > 0 }
            .groupBy { t ->
                val clienteObj = t.clienteId?.let { mapaClientes[it] }
                clienteObj?.alias?.takeIf { it.isNotBlank() }
                    ?: clienteObj?.nombre
                    ?: t.nombreClientePaso?.takeIf { it.isNotBlank() }
                    ?: "Cliente #${t.id}"
            }
            .map { (nombre, lista) -> nombre to lista.sumOf { it.cantidadBidonesPrestados } }

        ResumenBidonesPeriodo(
            etiquetaRango = etiquetaRango,
            vehiculoEtiqueta = vehiculoEtiqueta,
            totalEntregas = transFiltradas.size,
            totalBidones = transCount + azulCount + marcCount + gratisCount,
            transparentes = transCount,
            azules = azulCount,
            marcados = marcCount,
            envasesNuevos = envasesCount,
            prestados = prestCount,
            devueltos = devCount,
            cortesias = gratisCount,
            galones = galCount,
            pacas = pacCount,
            minis = minCount,
            entregas = listaEntregas.sortedByDescending { it.fechaMillis },
            prestamosPorCliente = prestamosAgrupados
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
                                    text = if (offsetMeses == 0) "Mes en curso" else "Mes histórico • Toca ▶ para volver",
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

        // 2. Tarjeta Resumen Principal de Bidones Entregados
        item {
            AnimatedContent(targetState = resumen, label = "resumenBidonesAnim") { data ->
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
                                    text = "ENTREGA DE BIDONES • ${data.vehiculoEtiqueta.uppercase()}",
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
                                    val texto = construirTextoReporteBidones(
                                        periodo = periodoSeleccionado,
                                        data = data
                                    )
                                    val intent = Intent(Intent.ACTION_SEND).apply {
                                        type = "text/plain"
                                        putExtra(Intent.EXTRA_TEXT, texto)
                                    }
                                    context.startActivity(Intent.createChooser(intent, "Compartir reporte de bidones"))
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
                                    text = "Total Botellones 20L Entregados",
                                    color = Color.White.copy(alpha = 0.85f),
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.SemiBold
                                )
                                Text(
                                    text = "${data.totalBidones}",
                                    color = Color.White,
                                    fontSize = 34.sp,
                                    fontWeight = FontWeight.Black
                                )
                                Text(
                                    text = "${data.totalEntregas} entregas registradas",
                                    color = PichinchaYellow,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }

                            Column(horizontalAlignment = Alignment.End) {
                                Text(
                                    text = "Tipos de Bidones:",
                                    color = Color.White.copy(alpha = 0.8f),
                                    fontSize = 11.sp
                                )
                                Text(
                                    text = "🔵 ${data.azules} Azules",
                                    color = Color.White,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    text = "⚪ ${data.transparentes} PET",
                                    color = Color.White,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    text = "🏷️ ${data.marcados} Marcadas",
                                    color = Color.White,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }
                }
            }
        }

        // 3. Tarjeta de Métricas por Tipo de Bidón y Productos Adicionales
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
                        text = "Desglose por Tipo de Botella",
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
                        titulo = "PET Transp.",
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
                        titulo = "Marcada",
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
                        DatoMiniInline("Envases Nuevos", "${resumen.envasesNuevos}", PichinchaNavy)
                        DatoMiniInline("Prestados", "${resumen.prestados}", AmbarAdvertencia)
                        DatoMiniInline("Devueltos", "${resumen.devueltos}", VerdeEfectivo)
                        DatoMiniInline("Cortesía", "${resumen.cortesias}", clayTheme.primaryColor)
                        DatoMiniInline("Galones", "${resumen.galones}", PichinchaNavy)
                        DatoMiniInline("Pacas", "${resumen.pacas}", PichinchaNavy)
                        DatoMiniInline("Minis", "${resumen.minis}", PichinchaNavy)
                    }
                }
            }
        }

        // 4. SECCIÓN DESTACADA: "¿A QUIÉN SE LE DEJÓ LOS BIDONES?"
        item {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 4.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "¿A QUIÉN SE LE DEJÓ LOS BIDONES?",
                    color = PichinchaNavy,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Black
                )
                ClayBadge(
                    text = "${resumen.entregas.size} despachos",
                    containerColor = Color(0xFFE6ECF5),
                    contentColor = PichinchaNavy
                )
            }
        }

        if (resumen.entregas.isEmpty()) {
            item {
                ClayCard(
                    modifier = Modifier.fillMaxWidth(),
                    cornerRadius = 16.dp
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 20.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Icon(
                            imageVector = Icons.Default.LocalShipping,
                            contentDescription = null,
                            tint = TextoSecundario,
                            modifier = Modifier.size(36.dp)
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "No se registraron entregas de bidones en este período.",
                            color = TextoSecundario,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }
            }
        } else {
            items(resumen.entregas, key = { it.transaccionId }) { entrega ->
                TarjetaEntregaClienteClay(
                    entrega = entrega,
                    esModoDia = periodoSeleccionado == PeriodoReporte.DIA
                )
            }
        }

        // 5. Clientes con Bidones Prestados en el período
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
                            text = "Bidones Prestados Pendientes",
                            color = PichinchaNavy,
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Black
                        )
                        ClayBadge(
                            text = "${resumen.prestados} botellones",
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

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun TarjetaEntregaClienteClay(
    entrega: EntregaBidonDetalle,
    esModoDia: Boolean,
    modifier: Modifier = Modifier
) {
    ClayCard(
        modifier = modifier.fillMaxWidth(),
        cornerRadius = 16.dp,
        contentPadding = PaddingValues(12.dp)
    ) {
        Column(modifier = Modifier.fillMaxWidth()) {
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
                            .size(30.dp)
                            .background(Color(0xFFE2E8F0), CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Person,
                            contentDescription = null,
                            tint = PichinchaNavy,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Column {
                        val tituloMostrar = entrega.aliasCliente?.let { "$it (${entrega.clienteNombre})" }
                            ?: entrega.clienteNombre
                        Text(
                            text = tituloMostrar,
                            fontWeight = FontWeight.Black,
                            fontSize = 14.sp,
                            color = TextoOscuro,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        val subtituloHora = if (esModoDia) {
                            if (entrega.horaTexto.isNotBlank()) entrega.horaTexto else entrega.fechaTexto
                        } else {
                            "${entrega.fechaTexto} • ${entrega.horaTexto}"
                        }
                        Text(
                            text = subtituloHora,
                            fontSize = 11.sp,
                            color = TextoSecundario,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }

                if (entrega.esClientePaso) {
                    ClayBadge(
                        text = "De paso",
                        containerColor = FondoGrisClaro,
                        contentColor = TextoSecundario
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Chips con los bidones dejados exactamente
            FlowRow(
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                if (entrega.azules > 0) {
                    ChipDetalleBidon(
                        texto = "${entrega.azules} Azul${if (entrega.azules > 1) "es" else ""}",
                        colorFondo = BidonAzulFondo,
                        colorTexto = BidonAzulColor
                    )
                }
                if (entrega.transparentes > 0) {
                    ChipDetalleBidon(
                        texto = "${entrega.transparentes} PET",
                        colorFondo = BidonTransparenteFondo,
                        colorTexto = BidonTransparenteColor
                    )
                }
                if (entrega.marcados > 0) {
                    ChipDetalleBidon(
                        texto = "${entrega.marcados} Marcada${if (entrega.marcados > 1) "s" else ""}",
                        colorFondo = BidonMarcadoFondo,
                        colorTexto = BidonMarcadoColor
                    )
                }
                if (entrega.envasesNuevos > 0) {
                    ChipDetalleBidon(
                        texto = "+${entrega.envasesNuevos} Envase Nuevo",
                        colorFondo = Color(0xFFE0E7FF),
                        colorTexto = Color(0xFF4338CA)
                    )
                }
                if (entrega.prestados > 0) {
                    ChipDetalleBidon(
                        texto = "⚠️ ${entrega.prestados} Prestado${if (entrega.prestados > 1) "s" else ""}",
                        colorFondo = AmbarFondo,
                        colorTexto = AmbarAdvertencia
                    )
                }
                if (entrega.devueltos > 0) {
                    ChipDetalleBidon(
                        texto = "🔄 ${entrega.devueltos} Devuelto${if (entrega.devueltos > 1) "s" else ""}",
                        colorFondo = VerdeEfectivoFondo,
                        colorTexto = VerdeEfectivo
                    )
                }
                if (entrega.cortesias > 0) {
                    ChipDetalleBidon(
                        texto = "🎁 ${entrega.cortesias} Cortesía",
                        colorFondo = VerdeEfectivoFondo,
                        colorTexto = VerdeEfectivo
                    )
                }
                if (entrega.galones > 0) {
                    ChipDetalleBidon(
                        texto = "${entrega.galones} Galón${if (entrega.galones > 1) "es" else ""}",
                        colorFondo = FondoGrisClaro,
                        colorTexto = PichinchaNavy
                    )
                }
                if (entrega.pacas > 0) {
                    ChipDetalleBidon(
                        texto = "${entrega.pacas} Paca${if (entrega.pacas > 1) "s" else ""}",
                        colorFondo = FondoGrisClaro,
                        colorTexto = PichinchaNavy
                    )
                }
                if (entrega.minis > 0) {
                    ChipDetalleBidon(
                        texto = "${entrega.minis} Mini${if (entrega.minis > 1) "s" else ""}",
                        colorFondo = FondoGrisClaro,
                        colorTexto = PichinchaNavy
                    )
                }
            }

            if (entrega.nota.isNotBlank()) {
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = "Nota: ${entrega.nota}",
                    fontStyle = FontStyle.Italic,
                    fontSize = 11.sp,
                    color = TextoSecundario
                )
            }
        }
    }
}

@Composable
private fun ChipDetalleBidon(
    texto: String,
    colorFondo: Color,
    colorTexto: Color
) {
    Surface(
        color = colorFondo,
        shape = RoundedCornerShape(8.dp),
        border = BorderStroke(1.dp, colorTexto.copy(alpha = 0.25f))
    ) {
        Text(
            text = texto,
            color = colorTexto,
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp),
            maxLines = 1
        )
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

private fun construirTextoReporteBidones(
    periodo: PeriodoReporte,
    data: ResumenBidonesPeriodo
): String {
    return buildString {
        appendLine("*💧 REPORTE DE ENTREGA DE BIDONES*")
        appendLine("*Unidad:* ${data.vehiculoEtiqueta.uppercase()}")
        appendLine("*Período:* ${data.etiquetaRango} (${periodo.titulo})")
        appendLine("─────────────────────────")
        appendLine("*📦 RESUMEN TOTAL DE BOTELLONES:*")
        appendLine("• *Total Bidones 20L:* *${data.totalBidones}*")
        appendLine("  ↳ 🔵 Azules: *${data.azules}*")
        appendLine("  ↳ ⚪ PET: *${data.transparentes}*")
        appendLine("  ↳ 🏷️ Marcadas: *${data.marcados}*")
        if (data.envasesNuevos > 0) {
            appendLine("• *Envases Nuevos Vendidos:* ${data.envasesNuevos}")
        }
        if (data.prestados > 0) {
            appendLine("• *Bidones Prestados:* ${data.prestados}")
        }
        if (data.devueltos > 0) {
            appendLine("• *Bidones Devueltos (Recuperados):* ${data.devueltos}")
        }
        if (data.cortesias > 0) {
            appendLine("• *Cortesías:* ${data.cortesias}")
        }
        if (data.galones > 0 || data.pacas > 0 || data.minis > 0) {
            appendLine("• *Otros Productos:* ${data.galones} galones | ${data.pacas} pacas | ${data.minis} minis")
        }
        appendLine("─────────────────────────")
        appendLine("*📋 DETALLE: ¿A QUIÉN SE LE DEJÓ LOS BIDONES?*")
        if (data.entregas.isEmpty()) {
            appendLine("• Sin entregas registradas en este período.")
        } else {
            data.entregas.forEachIndexed { index, entrega ->
                val clienteLabel = entrega.aliasCliente?.let { "$it (${entrega.clienteNombre})" } ?: entrega.clienteNombre
                val horaLabel = if (entrega.horaTexto.isNotBlank()) " [${entrega.horaTexto}]" else ""
                val detalleList = mutableListOf<String>()
                if (entrega.azules > 0) detalleList.add("${entrega.azules} Azul${if (entrega.azules > 1) "es" else ""}")
                if (entrega.transparentes > 0) detalleList.add("${entrega.transparentes} PET")
                if (entrega.marcados > 0) detalleList.add("${entrega.marcados} Marcada${if (entrega.marcados > 1) "s" else ""}")
                if (entrega.envasesNuevos > 0) detalleList.add("+${entrega.envasesNuevos} Envase Nuevo")
                if (entrega.prestados > 0) detalleList.add("⚠️ ${entrega.prestados} Prestado")
                if (entrega.devueltos > 0) detalleList.add("🔄 ${entrega.devueltos} Devuelto")
                if (entrega.cortesias > 0) detalleList.add("🎁 ${entrega.cortesias} Cortesía")
                if (entrega.galones > 0) detalleList.add("${entrega.galones} Gal.")
                if (entrega.pacas > 0) detalleList.add("${entrega.pacas} Paca")
                if (entrega.minis > 0) detalleList.add("${entrega.minis} Mini")

                appendLine("${index + 1}. *$clienteLabel*$horaLabel:")
                appendLine("   ↳ ${detalleList.joinToString(", ")}")
                if (entrega.nota.isNotBlank()) {
                    appendLine("     _Nota: ${entrega.nota}_")
                }
            }
        }

        if (data.prestamosPorCliente.isNotEmpty()) {
            appendLine("─────────────────────────")
            appendLine("*⚠️ PRÉSTAMOS PENDIENTES DE RECOGER:*")
            data.prestamosPorCliente.forEach { (nombre, cant) ->
                appendLine("• $nombre: *$cant bidones*")
            }
        }
    }
}

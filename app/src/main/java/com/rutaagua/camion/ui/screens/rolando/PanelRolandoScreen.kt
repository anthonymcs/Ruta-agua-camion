package com.rutaagua.camion.ui.screens.rolando

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Chat
import androidx.compose.material.icons.filled.AttachMoney
import androidx.compose.material.icons.filled.Business
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.ErrorOutline
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.LocalShipping
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.TrendingUp
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.rutaagua.camion.data.local.model.ResumenCierreDia
import com.rutaagua.camion.ui.components.BidonIllustration
import com.rutaagua.camion.ui.components.GalonIllustration
import com.rutaagua.camion.ui.components.MiniIllustration
import com.rutaagua.camion.ui.components.PacaIllustration
import com.rutaagua.camion.ui.screens.registro.AuditoriaCierreRP3Card
import com.rutaagua.camion.ui.screens.registro.FiltroVehiculoRegistro
import com.rutaagua.camion.ui.screens.registro.RegistroDiaViewModel
import com.rutaagua.camion.ui.theme.AguaPrimary
import com.rutaagua.camion.ui.theme.AguaPrimaryDark
import com.rutaagua.camion.ui.theme.AguaPrimaryLight
import com.rutaagua.camion.ui.theme.AmbarAdvertencia
import com.rutaagua.camion.ui.theme.AmbarFondo
import com.rutaagua.camion.ui.theme.AzulCorporativo
import com.rutaagua.camion.ui.theme.BordeSutil
import com.rutaagua.camion.ui.theme.FondoGrisClaro
import com.rutaagua.camion.ui.theme.RojoAlerta
import com.rutaagua.camion.ui.theme.RojoAlertaFondo
import com.rutaagua.camion.ui.theme.SuperficieBlanca
import com.rutaagua.camion.ui.theme.TextoOscuro
import com.rutaagua.camion.ui.theme.TextoSecundario
import com.rutaagua.camion.ui.theme.VerdeEfectivo
import com.rutaagua.camion.ui.theme.VerdeEfectivoFondo
import java.util.Locale

/**
 * Pantalla Ejecutiva EXCLUSIVA para Rolando (Administrador).
 * Centraliza costos de planta, cuadro comparativo de cierre a ciegas RP3 (Declarado vs Sistema),
 * cálculo de Utilidad Bruta y Utilidad Neta real, y vista consolidada de ambos vehículos.
 */
@Composable
fun PanelRolandoScreen(
    viewModel: RegistroDiaViewModel,
    modifier: Modifier = Modifier
) {
    val filtroVehiculo by viewModel.filtroVehiculoState.collectAsState()
    val resumen by viewModel.resumenCierreDiaState.collectAsState()
    val cierreHoy by viewModel.cierreHoyState.collectAsState()
    val transacciones by viewModel.transaccionesHoyState.collectAsState()
    val gastos by viewModel.gastosHoyState.collectAsState()

    val costoBidon = viewModel.costoPlantaBidon
    val costoGalon = viewModel.costoPlantaGalon
    val costoPaca = viewModel.costoPlantaPaca
    val costoMini = viewModel.costoPlantaMini

    val costoTotalAguaPlanta = (resumen.totalBidonesVendidos * costoBidon) +
            (resumen.totalGalonesVendidos * costoGalon) +
            (resumen.totalPacasVendidas * costoPaca) +
            (resumen.totalMinisVendidas * costoMini)

    val ingresoTotalVentas = resumen.totalIngresoEfectivo + resumen.totalIngresoTransferencia + resumen.totalCreditosOtorgados + resumen.totalAbonosRecibidos
    val utilidadBruta = (ingresoTotalVentas - costoTotalAguaPlanta).coerceAtLeast(0.0)
    val utilidadNeta = utilidadBruta - resumen.totalGastosRuta

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .background(FondoGrisClaro),
        contentPadding = PaddingValues(bottom = 32.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // 1. Header Ejecutivo
        item {
            HeaderPanelRolando(fechaDisplay = viewModel.fechaHoyDisplay)
        }

        // 2. Selector de Filtro de Unidad (Consolidado, Camión o Camioneta)
        item {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState())
                    .padding(horizontal = 16.dp, vertical = 2.dp),
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
                            selectedContainerColor = AzulCorporativo,
                            selectedLabelColor = Color.White
                        )
                    )
                }
            }
        }

        // 3. Tarjeta de Auditoría RP3 (Cierre a Ciegas del Camión vs Sistema)
        item {
            Column(modifier = Modifier.padding(horizontal = 16.dp)) {
                Text(
                    text = "AUDITORÍA DE CIERRE DE CAJA (ESTILO RP3)",
                    style = MaterialTheme.typography.labelSmall,
                    color = TextoSecundario,
                    fontWeight = FontWeight.Black,
                    modifier = Modifier.padding(bottom = 6.dp)
                )
                AuditoriaCierreRP3Card(
                    cierre = cierreHoy,
                    resumen = resumen,
                    costoPlantaBidon = costoBidon,
                    costoPlantaGalon = costoGalon,
                    costoPlantaPaca = costoPaca,
                    costoPlantaMini = costoMini
                )
            }
        }

        // 4. Panel Financiero: Utilidades y Costos de Planta
        item {
            Column(modifier = Modifier.padding(horizontal = 16.dp)) {
                Text(
                    text = "ESTADO DE RESULTADOS FINANCIEROS (CONFIDENCIAL)",
                    style = MaterialTheme.typography.labelSmall,
                    color = TextoSecundario,
                    fontWeight = FontWeight.Black,
                    modifier = Modifier.padding(bottom = 6.dp)
                )

                Card(
                    shape = RoundedCornerShape(16.dp),
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
                                    text = "Rentabilidad de la Jornada",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Black,
                                    color = TextoOscuro
                                )
                            }
                            Surface(
                                color = if (utilidadNeta >= 0.0) VerdeEfectivoFondo else RojoAlertaFondo,
                                shape = RoundedCornerShape(8.dp)
                            ) {
                                Text(
                                    text = if (utilidadNeta >= 0.0) "🟢 Rentable" else "🔴 Déficit",
                                    color = if (utilidadNeta >= 0.0) VerdeEfectivo else RojoAlerta,
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
                                titulo = "Ventas Brutas",
                                valor = "$${String.format(Locale.US, "%.2f", ingresoTotalVentas)}",
                                color = AzulCorporativo,
                                modifier = Modifier.weight(1f)
                            )
                            ItemMetricaFinanciera(
                                titulo = "Costo Agua Planta",
                                valor = "-$${String.format(Locale.US, "%.2f", costoTotalAguaPlanta)}",
                                color = RojoAlerta,
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
                                valor = "-$${String.format(Locale.US, "%.2f", resumen.totalGastosRuta)}",
                                color = AmbarAdvertencia,
                                modifier = Modifier.weight(1f)
                            )
                            ItemMetricaFinanciera(
                                titulo = "UTILIDAD NETA",
                                valor = "$${String.format(Locale.US, "%.2f", utilidadNeta)}",
                                color = if (utilidadNeta >= 0.0) VerdeEfectivo else RojoAlerta,
                                esDestacado = true,
                                modifier = Modifier.weight(1f)
                            )
                        }

                        HorizontalDivider(modifier = Modifier.padding(vertical = 14.dp), color = BordeSutil)

                        // Desglose de Costos de Planta Aplicados
                        Text(
                            text = "Costos Unitarios de Planta configurados:",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = TextoSecundario
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text("Bidón 20L: $${String.format(Locale.US, "%.2f", costoBidon)}", fontSize = 12.sp, color = TextoOscuro, fontWeight = FontWeight.Bold)
                            Text("Galón 4L: $${String.format(Locale.US, "%.2f", costoGalon)}", fontSize = 12.sp, color = TextoOscuro, fontWeight = FontWeight.Bold)
                            Text("Paca 24x: $${String.format(Locale.US, "%.2f", costoPaca)}", fontSize = 12.sp, color = TextoOscuro, fontWeight = FontWeight.Bold)
                            Text("Mini 12x: $${String.format(Locale.US, "%.2f", costoMini)}", fontSize = 12.sp, color = TextoOscuro, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }

        // 5. Resumen de Flujo de Efectivo en Caja
        item {
            Column(modifier = Modifier.padding(horizontal = 16.dp)) {
                Text(
                    text = "FLUJO DE COBROS Y CARTERA DE HOY",
                    style = MaterialTheme.typography.labelSmall,
                    color = TextoSecundario,
                    fontWeight = FontWeight.Black,
                    modifier = Modifier.padding(bottom = 6.dp)
                )

                Card(
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = SuperficieBlanca),
                    border = BorderStroke(1.dp, BordeSutil),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        FilaFlujoCaja(etiqueta = "💵 Efectivo del Día:", valor = "$${String.format(Locale.US, "%.2f", resumen.totalIngresoEfectivo)}", color = VerdeEfectivo)
                        FilaFlujoCaja(etiqueta = "📲 Transferencias Bancarias:", valor = "$${String.format(Locale.US, "%.2f", resumen.totalIngresoTransferencia)}", color = AzulCorporativo)
                        FilaFlujoCaja(etiqueta = "📝 Créditos Otorgados (Fiado hoy):", valor = "$${String.format(Locale.US, "%.2f", resumen.totalCreditosOtorgados)}", color = RojoAlerta)
                        FilaFlujoCaja(etiqueta = "🤝 Abonos / Deudas Cobradas:", valor = "+$${String.format(Locale.US, "%.2f", resumen.totalAbonosYCancelados)}", color = Color(0xFF0F766E))
                        FilaFlujoCaja(etiqueta = "🧾 Gastos en Ruta:", valor = "-$${String.format(Locale.US, "%.2f", resumen.totalGastosRuta)}", color = AmbarAdvertencia)
                        HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp), color = BordeSutil)
                        FilaFlujoCaja(etiqueta = "💰 Efectivo Neto en Caja:", valor = "$${String.format(Locale.US, "%.2f", resumen.efectivoNetoCaja)}", color = VerdeEfectivo, negrita = true)
                        FilaFlujoCaja(etiqueta = "📦 Total Bidones 20L Vendidos:", valor = "${resumen.totalBidonesVendidos} botellones", color = TextoOscuro, negrita = true)
                    }
                }
            }
        }

        // 6. Desglose de Clientes por Precio Unitario de Agua
        if (resumen.desglosePorPrecio.isNotEmpty()) {
            item {
                Column(modifier = Modifier.padding(horizontal = 16.dp)) {
                    Text(
                        text = "CLIENTES POR PRECIO DE AGUA (HOY)",
                        style = MaterialTheme.typography.labelSmall,
                        color = TextoSecundario,
                        fontWeight = FontWeight.Black,
                        modifier = Modifier.padding(bottom = 6.dp)
                    )

                    Card(
                        shape = RoundedCornerShape(16.dp),
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
    }
}

@Composable
private fun HeaderPanelRolando(fechaDisplay: String) {
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
                Column {
                    Text(
                        text = "PANEL FINANCIERO Y AUDITORÍA",
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

                Surface(
                    color = Color(0xFF1E293B),
                    shape = RoundedCornerShape(8.dp),
                    border = BorderStroke(1.dp, Color(0xFF334155))
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(imageVector = Icons.Default.Lock, contentDescription = null, tint = VerdeEfectivo, modifier = Modifier.size(13.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Acceso PIN OK", color = VerdeEfectivo, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    }
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
            fontWeight = if (negrita) FontWeight.Bold else FontWeight.Normal
        )
        Text(
            text = valor,
            fontSize = 13.sp,
            fontWeight = FontWeight.Black,
            color = color
        )
    }
}

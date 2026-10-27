package com.rutaagua.camion.ui.screens.registro

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Assessment
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Error
import androidx.compose.material.icons.filled.Info
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.rutaagua.camion.data.local.entity.CierreCajaEntity
import com.rutaagua.camion.data.local.model.ResumenCierreDia
import com.rutaagua.camion.ui.theme.AguaPrimary
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
import kotlin.math.abs

/**
 * Tarjeta de Auditoría de Cierre de Caja RP3 exclusiva para el perfil de Rolando (Administrador).
 * Compara en tiempo real lo declarado físicamente por el operador de ruta contra el cálculo
 * del sistema, y expone los costos de planta y utilidades brutas y netas.
 */
@Composable
fun AuditoriaCierreRP3Card(
    cierre: CierreCajaEntity?,
    resumen: ResumenCierreDia,
    costoPlantaBidon: Double,
    costoPlantaGalon: Double,
    costoPlantaPaca: Double,
    costoPlantaMini: Double,
    modifier: Modifier = Modifier
) {
    // Si aún no hay cierre declarado, usar resumen del sistema como referencia
    val efectivoDeclarado = cierre?.efectivoDeclarado ?: 0.0
    val transferenciasDeclaradas = cierre?.transferenciasDeclaradas ?: 0.0
    val totalDeclarado = cierre?.totalDeclarado ?: (efectivoDeclarado + transferenciasDeclaradas)

    val efectivoSistema = resumen.efectivoNetoCaja
    val transferenciasSistema = resumen.totalIngresoTransferencia
    val totalSistema = efectivoSistema + transferenciasSistema

    val diferencia = if (cierre != null) {
        cierre.diferenciaTotal
    } else {
        totalDeclarado - totalSistema
    }

    // Cálculos de costos y utilidades para Rolando
    val totalBidones = resumen.totalBidonesVendidos
    val totalGalones = resumen.totalGalonesVendidos
    val totalPacas = resumen.totalPacasVendidas
    val totalMinis = resumen.totalMinisVendidas

    val costoTotalAgua = (totalBidones * costoPlantaBidon) +
            (totalGalones * costoPlantaGalon) +
            (totalPacas * costoPlantaPaca) +
            (totalMinis * costoPlantaMini)

    val utilidadBruta = maxOf(0.0, resumen.granTotalVenta - costoTotalAgua)
    val utilidadNeta = utilidadBruta - resumen.totalGastosRuta

    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = SuperficieBlanca),
        border = BorderStroke(1.5.dp, Color(0xFF0284C7).copy(alpha = 0.4f)),
        elevation = CardDefaults.cardElevation(defaultElevation = 3.dp),
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 6.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            // Header del Panel
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .background(Color(0xFF0284C7).copy(alpha = 0.12f), CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Assessment,
                            contentDescription = null,
                            tint = Color(0xFF0284C7),
                            modifier = Modifier.size(20.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        Text(
                            text = "AUDITORÍA DE CAJA RP3",
                            fontWeight = FontWeight.Black,
                            fontSize = 15.sp,
                            color = TextoOscuro
                        )
                        Text(
                            text = "Exclusivo Rolando (Administrador)",
                            style = MaterialTheme.typography.labelSmall,
                            color = Color(0xFF0284C7),
                            fontWeight = FontWeight.Bold
                        )
                    }
                }

                Surface(
                    color = if (cierre != null) VerdeEfectivoFondo else FondoGrisClaro,
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text(
                        text = if (cierre != null) "Cierre Declarado" else "En Espera de Cierre",
                        color = if (cierre != null) VerdeEfectivo else TextoSecundario,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Comparativa RP3: Declarado vs Sistema
            Surface(
                color = FondoGrisClaro,
                shape = RoundedCornerShape(12.dp),
                border = BorderStroke(1.dp, BordeSutil),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(12.dp)) {
                    // Declarado por Ruta
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = "Declarado por Ruta:",
                            style = MaterialTheme.typography.bodySmall,
                            color = TextoSecundario
                        )
                        Text(
                            text = "$${String.format(Locale.US, "%.2f", totalDeclarado)} (Ef: $${String.format(Locale.US, "%.2f", efectivoDeclarado)} | Tr: $${String.format(Locale.US, "%.2f", transferenciasDeclaradas)})",
                            style = MaterialTheme.typography.bodySmall,
                            fontWeight = FontWeight.Bold,
                            color = TextoOscuro
                        )
                    }

                    Spacer(modifier = Modifier.height(4.dp))

                    // Sistema (Ventas reales - Gastos)
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = "Sistema (Ventas - Gastos):",
                            style = MaterialTheme.typography.bodySmall,
                            color = TextoSecundario
                        )
                        Text(
                            text = "$${String.format(Locale.US, "%.2f", totalSistema)} (Ef: $${String.format(Locale.US, "%.2f", efectivoSistema)} | Tr: $${String.format(Locale.US, "%.2f", transferenciasSistema)})",
                            style = MaterialTheme.typography.bodySmall,
                            fontWeight = FontWeight.Bold,
                            color = TextoOscuro
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Resultado del Arqueo (Cuadrado / Faltante / Sobrante)
            val estadoCuadre = when {
                cierre == null -> "Aún no han declarado el cierre en la ruta"
                diferencia < -0.05 -> "FALTANTE EN CAJA: -$${String.format(Locale.US, "%.2f", abs(diferencia))}"
                abs(diferencia) <= 0.05 -> "CAJA CUADRADA ($0.00)"
                else -> "SOBRANTE EN CAJA: +$${String.format(Locale.US, "%.2f", diferencia)}"
            }

            val (bgBadge, fgBadge, iconoCuadre) = when {
                cierre == null -> Triple(FondoGrisClaro, TextoSecundario, Icons.Default.Info)
                diferencia < -0.05 -> Triple(RojoAlertaFondo, RojoAlerta, Icons.Default.Error)
                abs(diferencia) <= 0.05 -> Triple(VerdeEfectivoFondo, VerdeEfectivo, Icons.Default.CheckCircle)
                else -> Triple(Color(0xFFE0F2FE), Color(0xFF0284C7), Icons.Default.Info)
            }

            Surface(
                color = bgBadge,
                shape = RoundedCornerShape(10.dp),
                border = BorderStroke(1.dp, fgBadge.copy(alpha = 0.5f)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = iconoCuadre,
                        contentDescription = null,
                        tint = fgBadge,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = estadoCuadre,
                        fontWeight = FontWeight.Black,
                        fontSize = 13.sp,
                        color = fgBadge
                    )
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Sección de Métricas Financieras (Costos y Utilidades)
            Text(
                text = "RENTABILIDAD ESTIMADA DE HOY",
                style = MaterialTheme.typography.labelSmall,
                fontWeight = FontWeight.Bold,
                color = TextoSecundario
            )

            Spacer(modifier = Modifier.height(8.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                MetricaFinancieraMini(
                    titulo = "Costo Planta",
                    valor = "$${String.format(Locale.US, "%.2f", costoTotalAgua)}",
                    color = Color(0xFF64748B),
                    modifier = Modifier.weight(1f)
                )
                MetricaFinancieraMini(
                    titulo = "Utilidad Bruta",
                    valor = "$${String.format(Locale.US, "%.2f", utilidadBruta)}",
                    color = AguaPrimary,
                    modifier = Modifier.weight(1f)
                )
                MetricaFinancieraMini(
                    titulo = "Utilidad Neta",
                    valor = "$${String.format(Locale.US, "%.2f", utilidadNeta)}",
                    color = VerdeEfectivo,
                    modifier = Modifier.weight(1f)
                )
            }
        }
    }
}

@Composable
private fun MetricaFinancieraMini(
    titulo: String,
    valor: String,
    color: Color,
    modifier: Modifier = Modifier
) {
    Surface(
        color = color.copy(alpha = 0.08f),
        shape = RoundedCornerShape(10.dp),
        border = BorderStroke(1.dp, color.copy(alpha = 0.25f)),
        modifier = modifier
    ) {
        Column(
            modifier = Modifier.padding(8.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = titulo,
                fontSize = 11.sp,
                fontWeight = FontWeight.SemiBold,
                color = TextoSecundario
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = valor,
                fontSize = 14.sp,
                fontWeight = FontWeight.Black,
                color = color
            )
        }
    }
}

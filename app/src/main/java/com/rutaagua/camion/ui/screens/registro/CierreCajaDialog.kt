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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.rutaagua.camion.data.local.entity.CierreCajaEntity
import com.rutaagua.camion.data.local.entity.RecargaPlantaEntity
import com.rutaagua.camion.data.local.model.EstadisticasVentasTipo
import com.rutaagua.camion.data.local.model.ResumenCierreDia
import com.rutaagua.camion.ui.components.marineOutlinedTextFieldColors
import com.rutaagua.camion.ui.theme.AguaPrimary
import com.rutaagua.camion.ui.theme.AguaPrimaryDark
import com.rutaagua.camion.ui.theme.AmbarAdvertencia
import com.rutaagua.camion.ui.theme.BordeSutil
import com.rutaagua.camion.ui.theme.FondoGrisClaro
import com.rutaagua.camion.ui.theme.RojoAlerta
import com.rutaagua.camion.ui.theme.SuperficieBlanca
import com.rutaagua.camion.ui.theme.TextoOscuro
import com.rutaagua.camion.ui.theme.TextoSecundario
import com.rutaagua.camion.ui.theme.VerdeEfectivo
import com.rutaagua.camion.ui.utils.WhatsAppHelper
import java.util.Locale

/**
 * Diálogo de Cierre de Caja y Reporte Diario para Rolando.
 * Al confirmar el cierre, muestra el reporte completo con la suma de Créditos, Abonos,
 * Efectivo, Transferencias y el desglose de cuántos clientes de cada precio compraron agua.
 */
@Composable
fun CierreCajaDialog(
    cierreExistente: CierreCajaEntity?,
    resumenDia: ResumenCierreDia? = null,
    estadisticas: EstadisticasVentasTipo? = null,
    recargasHoy: List<RecargaPlantaEntity> = emptyList(),
    onConfirmarCierre: (efectivo: Double, transferencias: Double, observaciones: String) -> Unit,
    onDismissRequest: () -> Unit
) {
    val context = LocalContext.current
    var efectivoInput by remember {
        mutableStateOf(if (cierreExistente != null) String.format(Locale.US, "%.2f", cierreExistente.efectivoDeclarado) else "")
    }
    var transferenciasInput by remember {
        mutableStateOf(if (cierreExistente != null) String.format(Locale.US, "%.2f", cierreExistente.transferenciasDeclaradas) else "")
    }
    var observacionesInput by remember {
        mutableStateOf(cierreExistente?.observaciones ?: "")
    }

    var cierreConfirmado by remember { mutableStateOf<CierreCajaEntity?>(null) }

    AlertDialog(
        onDismissRequest = onDismissRequest,
        shape = RoundedCornerShape(20.dp),
        containerColor = SuperficieBlanca,
        title = null,
        text = {
            if (cierreConfirmado == null) {
                // Formulario de Ingreso Ciego
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 8.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Box(
                            modifier = Modifier
                                .size(40.dp)
                                .background(AguaPrimary.copy(alpha = 0.12f), CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Lock,
                                contentDescription = null,
                                tint = AguaPrimary,
                                modifier = Modifier.size(22.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text(
                                text = "CIERRE DE CAJA CIEGO",
                                fontWeight = FontWeight.Black,
                                fontSize = 17.sp,
                                color = TextoOscuro
                            )
                            Text(
                                text = "Fin de Jornada • Estilo RP3",
                                style = MaterialTheme.typography.labelSmall,
                                color = TextoSecundario
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    Surface(
                        color = FondoGrisClaro,
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            text = "Cuenta físicamente el dinero en tu bolsillo y la suma de comprobantes de transferencia. Ingresa el total sin deducciones.",
                            style = MaterialTheme.typography.bodySmall,
                            color = TextoSecundario,
                            modifier = Modifier.padding(10.dp)
                        )
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    OutlinedTextField(
                        value = efectivoInput,
                        onValueChange = { efectivoInput = it },
                        label = { Text("💵 Efectivo Físico Contado ($)") },
                        placeholder = { Text("0.00") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                        singleLine = true,
                        colors = marineOutlinedTextFieldColors(accentColor = VerdeEfectivo),
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp)
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    OutlinedTextField(
                        value = transferenciasInput,
                        onValueChange = { transferenciasInput = it },
                        label = { Text("📱 Transferencias Verificadas ($)") },
                        placeholder = { Text("0.00") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                        singleLine = true,
                        colors = marineOutlinedTextFieldColors(accentColor = AguaPrimary),
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp)
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    OutlinedTextField(
                        value = observacionesInput,
                        onValueChange = { observacionesInput = it },
                        label = { Text("📝 Observaciones del día (Opcional)") },
                        placeholder = { Text("Ej. Faltó cambio en la última parada...") },
                        maxLines = 3,
                        colors = marineOutlinedTextFieldColors(accentColor = AguaPrimary),
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp)
                    )
                }
            } else {
                // Reporte Diario Completo al Cerrar Caja (Efectivo, Transferencias, Créditos, Abonos y Clientes por Precio)
                val c = cierreConfirmado!!
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .verticalScroll(rememberScrollState())
                        .padding(top = 8.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Box(
                        modifier = Modifier
                            .size(48.dp)
                            .background(VerdeEfectivo.copy(alpha = 0.15f), CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.CheckCircle,
                            contentDescription = null,
                            tint = VerdeEfectivo,
                            modifier = Modifier.size(32.dp)
                        )
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    Text(
                        text = "¡Cierre y Reporte del Día Listo!",
                        fontWeight = FontWeight.Black,
                        fontSize = 17.sp,
                        color = TextoOscuro,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    Surface(
                        color = FondoGrisClaro,
                        shape = RoundedCornerShape(14.dp),
                        border = BorderStroke(1.dp, BordeSutil),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(
                            modifier = Modifier.padding(14.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(
                                text = "TOTAL DECLARADO A ENTREGAR",
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold,
                                color = TextoSecundario,
                                textAlign = TextAlign.Center,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "$${String.format(Locale.US, "%.2f", c.totalDeclarado)}",
                                fontSize = 28.sp,
                                fontWeight = FontWeight.Black,
                                color = VerdeEfectivo
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "Efectivo: $${String.format(Locale.US, "%.2f", c.efectivoDeclarado)} • Transf: $${String.format(Locale.US, "%.2f", c.transferenciasDeclaradas)}",
                                style = MaterialTheme.typography.bodySmall,
                                fontWeight = FontWeight.SemiBold,
                                color = TextoOscuro,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                    }

                    if (resumenDia != null) {
                        Spacer(modifier = Modifier.height(10.dp))

                        // 1. Suma de Créditos, Abonos, Efectivo y Transferencias del Día
                        Surface(
                            color = FondoGrisClaro,
                            shape = RoundedCornerShape(14.dp),
                            border = BorderStroke(1.dp, BordeSutil),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(modifier = Modifier.padding(12.dp)) {
                                Text(
                                    text = "SUMA CONTABLE DEL DÍA",
                                    style = MaterialTheme.typography.labelSmall,
                                    fontWeight = FontWeight.Black,
                                    color = AguaPrimaryDark,
                                    maxLines = 1
                                )
                                Spacer(modifier = Modifier.height(6.dp))
                                FilaResumenReporteCierre("Efectivo Cobrado", resumenDia.totalIngresoEfectivo, VerdeEfectivo)
                                FilaResumenReporteCierre("Transferencias", resumenDia.totalIngresoTransferencia, AguaPrimaryDark)
                                FilaResumenReporteCierre("Créditos (Fiado)", resumenDia.totalCreditosOtorgados, RojoAlerta)
                                FilaResumenReporteCierre("Abonos a Deuda", resumenDia.totalAbonosYCancelados, AmbarAdvertencia)
                                FilaResumenReporteCierre("Gastos en Ruta", resumenDia.totalGastosRuta, TextoSecundario)
                                Spacer(modifier = Modifier.height(4.dp))
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text(
                                        text = "Efectivo Neto Caja:",
                                        fontWeight = FontWeight.Black,
                                        fontSize = 13.sp,
                                        color = TextoOscuro
                                    )
                                    Text(
                                        text = "$${String.format(Locale.US, "%.2f", resumenDia.efectivoNetoCaja)}",
                                        fontWeight = FontWeight.Black,
                                        fontSize = 13.sp,
                                        color = VerdeEfectivo
                                    )
                                }
                            }
                        }

                        // 2. Bidones Exactos Vendidos por Tipo en la Ruta
                        if (estadisticas != null) {
                            Spacer(modifier = Modifier.height(10.dp))
                            Surface(
                                color = FondoGrisClaro,
                                shape = RoundedCornerShape(14.dp),
                                border = BorderStroke(1.dp, BordeSutil),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Column(modifier = Modifier.padding(12.dp)) {
                                    Text(
                                        text = "BIDONES EXACTOS VENDIDOS EN RUTA",
                                        style = MaterialTheme.typography.labelSmall,
                                        fontWeight = FontWeight.Black,
                                        color = AguaPrimaryDark,
                                        maxLines = 1
                                    )
                                    Spacer(modifier = Modifier.height(6.dp))
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween
                                    ) {
                                        Text("Azules Estándar:", fontSize = 12.sp, color = TextoSecundario)
                                        Text("${estadisticas.azulesVendidos} un.", fontSize = 12.sp, fontWeight = FontWeight.Black, color = TextoOscuro)
                                    }
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween
                                    ) {
                                        Text("Transparentes (PET):", fontSize = 12.sp, color = TextoSecundario)
                                        Text("${estadisticas.transparentesVendidos} un.", fontSize = 12.sp, fontWeight = FontWeight.Black, color = TextoOscuro)
                                    }
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween
                                    ) {
                                        Text("Marcadas:", fontSize = 12.sp, color = TextoSecundario)
                                        Text("${estadisticas.marcadosVendidos} un.", fontSize = 12.sp, fontWeight = FontWeight.Black, color = TextoOscuro)
                                    }
                                    HorizontalDivider(modifier = Modifier.padding(vertical = 4.dp), color = BordeSutil)
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween
                                    ) {
                                        Text("TOTAL BIDONES 20L:", fontSize = 12.sp, fontWeight = FontWeight.Black, color = TextoOscuro)
                                        Text("${estadisticas.totalBidonesVendidos} botellones", fontSize = 12.sp, fontWeight = FontWeight.Black, color = AguaPrimaryDark)
                                    }
                                    if (estadisticas.galonesVendidos > 0 || estadisticas.pacasVendidas > 0 || estadisticas.minisVendidas > 0) {
                                        Spacer(modifier = Modifier.height(4.dp))
                                        val otros = buildList {
                                            if (estadisticas.galonesVendidos > 0) add("${estadisticas.galonesVendidos} gal.")
                                            if (estadisticas.pacasVendidas > 0) add("${estadisticas.pacasVendidas} pacas")
                                            if (estadisticas.minisVendidas > 0) add("${estadisticas.minisVendidas} minis")
                                        }.joinToString(" • ")
                                        Text("Otros productos: $otros", fontSize = 11.sp, color = TextoSecundario, fontWeight = FontWeight.Bold)
                                    }
                                }
                            }
                        }

                        // 3. Cargas Realizadas en el Día
                        if (recargasHoy.isNotEmpty()) {
                            Spacer(modifier = Modifier.height(10.dp))
                            Surface(
                                color = FondoGrisClaro,
                                shape = RoundedCornerShape(14.dp),
                                border = BorderStroke(1.dp, BordeSutil),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Column(modifier = Modifier.padding(12.dp)) {
                                    Text(
                                        text = "CARGAS REALIZADAS EN EL DÍA (${recargasHoy.size})",
                                        style = MaterialTheme.typography.labelSmall,
                                        fontWeight = FontWeight.Black,
                                        color = AguaPrimaryDark,
                                        maxLines = 1
                                    )
                                    Spacer(modifier = Modifier.height(6.dp))
                                    recargasHoy.sortedBy { it.numeroVuelta }.forEach { r ->
                                        Row(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .padding(vertical = 2.dp),
                                            horizontalArrangement = Arrangement.SpaceBetween
                                        ) {
                                            Text("Carga #${r.numeroVuelta}:", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = TextoOscuro)
                                            Text("+${r.totalBidonesCargados()} bidones", fontSize = 12.sp, fontWeight = FontWeight.Black, color = VerdeEfectivo)
                                        }
                                    }
                                }
                            }
                        }

                        // 4. Desglose de Clientes por Precio de Agua (Ej: "Clientes $0.80: 20 clientes • Total $16.00")
                        if (resumenDia.desglosePorPrecio.isNotEmpty()) {
                            Spacer(modifier = Modifier.height(10.dp))
                            Surface(
                                color = FondoGrisClaro,
                                shape = RoundedCornerShape(14.dp),
                                border = BorderStroke(1.dp, BordeSutil),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Column(modifier = Modifier.padding(12.dp)) {
                                    Text(
                                        text = "CLIENTES POR PRECIO DE AGUA",
                                        style = MaterialTheme.typography.labelSmall,
                                        fontWeight = FontWeight.Black,
                                        color = AguaPrimaryDark,
                                        maxLines = 1
                                    )
                                    Spacer(modifier = Modifier.height(6.dp))
                                    resumenDia.desglosePorPrecio.forEach { item ->
                                        val cliLabel = if (item.cantidadClientes == 1) "1 cliente" else "${item.cantidadClientes} clientes"
                                        Row(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .padding(vertical = 3.dp),
                                            horizontalArrangement = Arrangement.SpaceBetween,
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Text(
                                                text = "Clientes $${String.format(Locale.US, "%.2f", item.precioUnitario)}: $cliLabel (${item.cantidadBidonesCobrados} bid.)",
                                                fontSize = 12.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = TextoOscuro,
                                                maxLines = 1,
                                                overflow = TextOverflow.Ellipsis,
                                                modifier = Modifier.weight(1f)
                                            )
                                            Spacer(modifier = Modifier.width(6.dp))
                                            Text(
                                                text = "Total $${String.format(Locale.US, "%.2f", item.totalImporteAgua)}",
                                                fontSize = 12.sp,
                                                fontWeight = FontWeight.Black,
                                                color = AguaPrimaryDark,
                                                maxLines = 1,
                                                softWrap = false
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    Button(
                        onClick = {
                            val mensaje = WhatsAppHelper.generarMensajeCierreCajaOperador(c, resumenDia, estadisticas, recargasHoy)
                            WhatsAppHelper.abrirChatWhatsApp(context, null, mensaje)
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF25D366)),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(48.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Share,
                            contentDescription = null,
                            tint = Color.White,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Enviar Cierre a Rolando (WhatsApp)",
                            fontWeight = FontWeight.Bold,
                            color = Color.White,
                            maxLines = 1,
                            softWrap = false,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }
            }
        },
        confirmButton = {
            if (cierreConfirmado == null) {
                Button(
                    onClick = {
                        val ef = efectivoInput.replace(',', '.').toDoubleOrNull() ?: 0.0
                        val tr = transferenciasInput.replace(',', '.').toDoubleOrNull() ?: 0.0
                        onConfirmarCierre(ef, tr, observacionesInput.trim())
                        cierreConfirmado = CierreCajaEntity(
                            efectivoDeclarado = ef,
                            transferenciasDeclaradas = tr,
                            totalDeclarado = ef + tr,
                            observaciones = observacionesInput.trim(),
                            fechaIso = ""
                        )
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = VerdeEfectivo),
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Text("Confirmar Cierre", fontWeight = FontWeight.Bold, color = Color.White)
                }
            } else {
                Button(
                    onClick = onDismissRequest,
                    colors = ButtonDefaults.buttonColors(containerColor = AguaPrimary),
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Text("Listo / Salir", fontWeight = FontWeight.Bold, color = Color.White)
                }
            }
        },
        dismissButton = {
            if (cierreConfirmado == null) {
                OutlinedButton(
                    onClick = onDismissRequest,
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Text("Cancelar")
                }
            }
        }
    )
}

@Composable
private fun FilaResumenReporteCierre(
    etiqueta: String,
    valor: Double,
    colorValor: Color
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 2.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = etiqueta,
            fontSize = 12.sp,
            color = TextoSecundario,
            fontWeight = FontWeight.SemiBold,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier.weight(1f)
        )
        Text(
            text = "$${String.format(Locale.US, "%.2f", valor)}",
            fontSize = 12.sp,
            color = colorValor,
            fontWeight = FontWeight.Black,
            maxLines = 1,
            softWrap = false
        )
    }
}

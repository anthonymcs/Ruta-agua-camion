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
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.rutaagua.camion.data.local.entity.CierreCajaEntity
import com.rutaagua.camion.ui.theme.AguaPrimary
import com.rutaagua.camion.ui.theme.BordeSutil
import com.rutaagua.camion.ui.theme.FondoGrisClaro
import com.rutaagua.camion.ui.theme.SuperficieBlanca
import com.rutaagua.camion.ui.theme.TextoOscuro
import com.rutaagua.camion.ui.theme.TextoSecundario
import com.rutaagua.camion.ui.theme.VerdeEfectivo
import com.rutaagua.camion.ui.utils.WhatsAppHelper
import java.util.Locale

/**
 * Diálogo de Cierre de Caja Ciego (Estilo RP3 Gasolinera).
 * El operador ingresa únicamente los billetes, monedas y comprobantes contados.
 * El sistema NO le revela si sobra o falta dinero para garantizar honestidad y transparencia.
 */
@Composable
fun CierreCajaDialog(
    cierreExistente: CierreCajaEntity?,
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
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp)
                    )
                }
            } else {
                // Confirmación Final para el Operador (SIN FALTANTE NI SOBRANTE)
                val c = cierreConfirmado!!
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 12.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Box(
                        modifier = Modifier
                            .size(54.dp)
                            .background(VerdeEfectivo.copy(alpha = 0.15f), CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.CheckCircle,
                            contentDescription = null,
                            tint = VerdeEfectivo,
                            modifier = Modifier.size(36.dp)
                        )
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    Text(
                        text = "¡Cierre Declarado con Éxito!",
                        fontWeight = FontWeight.Black,
                        fontSize = 18.sp,
                        color = TextoOscuro
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    Surface(
                        color = FondoGrisClaro,
                        shape = RoundedCornerShape(14.dp),
                        border = BorderStroke(1.dp, BordeSutil),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(
                            modifier = Modifier.padding(16.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(
                                text = "TOTAL DECLARADO A ENTREGAR A ROLANDO",
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold,
                                color = TextoSecundario,
                                textAlign = TextAlign.Center
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "$${String.format(Locale.US, "%.2f", c.totalDeclarado)}",
                                fontSize = 30.sp,
                                fontWeight = FontWeight.Black,
                                color = VerdeEfectivo
                            )
                            Spacer(modifier = Modifier.height(6.dp))
                            Text(
                                text = "Efectivo: $${String.format(Locale.US, "%.2f", c.efectivoDeclarado)}  •  Transferencias: $${String.format(Locale.US, "%.2f", c.transferenciasDeclaradas)}",
                                style = MaterialTheme.typography.bodySmall,
                                fontWeight = FontWeight.SemiBold,
                                color = TextoOscuro
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    Button(
                        onClick = {
                            val mensaje = WhatsAppHelper.generarMensajeCierreCajaOperador(c)
                            WhatsAppHelper.abrirChatWhatsApp(context, null, mensaje)
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF25D366)),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.fillMaxWidth().height(48.dp)
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
                            color = Color.White
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

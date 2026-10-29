package com.rutaagua.camion.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
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
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Chat
import androidx.compose.material.icons.filled.AccountBalance
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
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
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.rutaagua.camion.ui.theme.AguaPrimary
import com.rutaagua.camion.ui.theme.AguaPrimaryDark
import com.rutaagua.camion.ui.theme.BordeSutil
import com.rutaagua.camion.ui.theme.FondoGrisClaro
import com.rutaagua.camion.ui.theme.PichinchaNavy
import com.rutaagua.camion.ui.theme.RojoAlerta
import com.rutaagua.camion.ui.theme.SuperficieBlanca
import com.rutaagua.camion.ui.theme.TextoOscuro
import com.rutaagua.camion.ui.theme.TextoSecundario
import com.rutaagua.camion.ui.theme.VerdeEfectivo
import com.rutaagua.camion.ui.utils.CuentaBancariaItem
import com.rutaagua.camion.ui.utils.CuentasBancariasHelper
import java.util.Locale

/**
 * Modal interactivo para visualizar, editar/agregar y enviar las Cuentas Bancarias
 * (Pichincha, Pacífico, Bolivariano, Produbanco, Banco Guayaquil, etc.) como tarjeta de imagen (.png)
 * y texto directamente hacia WhatsApp.
 */
@Composable
fun CuentasBancariasDialog(
    telefonoCliente: String? = null,
    nombreCliente: String? = null,
    montoACobrar: Double? = null,
    onDismiss: () -> Unit
) {
    val context = LocalContext.current
    var cuentas by remember { mutableStateOf(CuentasBancariasHelper.obtenerCuentasGuardadas(context)) }
    var mostrarFormulario by remember { mutableStateOf(false) }
    var cuentaEditando by remember { mutableStateOf<CuentaBancariaItem?>(null) }

    var formBanco by remember { mutableStateOf("Produbanco") }
    var formTipoCuenta by remember { mutableStateOf("Cuenta ahorro") }
    var formNumero by remember { mutableStateOf("") }
    var formTitular by remember { mutableStateOf("Holger Rolando Echeverría Palma") }
    var formCedula by remember { mutableStateOf("1305015974") }
    var formColorHex by remember { mutableLongStateOf(0xFF15803DL) }

    fun abrirFormulario(cuenta: CuentaBancariaItem?) {
        cuentaEditando = cuenta
        if (cuenta != null) {
            formBanco = cuenta.banco
            formTipoCuenta = cuenta.tipoCuenta
            formNumero = cuenta.numeroCuenta
            formTitular = cuenta.titular
            formCedula = cuenta.cedula
            formColorHex = cuenta.colorHex
        } else {
            formBanco = "Produbanco"
            formTipoCuenta = "Cuenta ahorro"
            formNumero = ""
            formTitular = "Holger Rolando Echeverría Palma"
            formCedula = "1305015974"
            formColorHex = 0xFF15803DL
        }
        mostrarFormulario = true
    }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            modifier = Modifier
                .fillMaxWidth(0.95f)
                .padding(vertical = 16.dp),
            shape = RoundedCornerShape(24.dp),
            color = SuperficieBlanca,
            tonalElevation = 8.dp
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState())
                    .padding(18.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Cabecera
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
                                imageVector = Icons.Default.AccountBalance,
                                contentDescription = null,
                                tint = AguaPrimaryDark,
                                modifier = Modifier.size(22.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(10.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "Cuentas para Transferencia",
                                fontWeight = FontWeight.Black,
                                fontSize = 17.sp,
                                color = PichinchaNavy,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                            Text(
                                text = if (!nombreCliente.isNullOrBlank()) {
                                    "Enviar tarjeta en imagen a $nombreCliente"
                                } else {
                                    "Toca una cuenta para enviarla como imagen a WhatsApp"
                                },
                                fontSize = 11.sp,
                                color = TextoSecundario,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                    }

                    IconButton(onClick = onDismiss) {
                        Icon(imageVector = Icons.Default.Close, contentDescription = "Cerrar", tint = TextoSecundario)
                    }
                }

                if (montoACobrar != null && montoACobrar > 0.0) {
                    Surface(
                        color = Color(0xFFDCFCE7),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            text = "Valor a transferir incluido en el envío: $${String.format(Locale.US, "%.2f", montoACobrar)}",
                            fontWeight = FontWeight.Black,
                            fontSize = 12.sp,
                            color = VerdeEfectivo,
                            textAlign = TextAlign.Center,
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp)
                        )
                    }
                }

                // Acciones Rápidas Superiores: [Enviar Todas] y [+ Agregar Banco]
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Button(
                        onClick = {
                            CuentasBancariasHelper.enviarTodasLasCuentasPorWhatsApp(
                                context = context,
                                cuentas = cuentas,
                                telefonoRaw = telefonoCliente,
                                nombreCliente = nombreCliente,
                                montoACobrar = montoACobrar
                            )
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF25D366)),
                        shape = RoundedCornerShape(12.dp),
                        contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                        modifier = Modifier
                            .weight(1f)
                            .height(40.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Share,
                            contentDescription = null,
                            tint = Color.White,
                            modifier = Modifier.size(15.dp)
                        )
                        Spacer(modifier = Modifier.width(5.dp))
                        Text(
                            text = "Enviar Todas (${cuentas.size})",
                            fontWeight = FontWeight.Black,
                            fontSize = 11.sp,
                            color = Color.White,
                            maxLines = 1,
                            softWrap = false,
                            overflow = TextOverflow.Ellipsis
                        )
                    }

                    OutlinedButton(
                        onClick = {
                            if (mostrarFormulario) mostrarFormulario = false
                            else abrirFormulario(null)
                        },
                        shape = RoundedCornerShape(12.dp),
                        border = BorderStroke(1.2.dp, AguaPrimary),
                        contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                        modifier = Modifier
                            .weight(1f)
                            .height(40.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Add,
                            contentDescription = null,
                            tint = AguaPrimaryDark,
                            modifier = Modifier.size(15.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = if (mostrarFormulario) "Ocultar Formulario" else "+ Otro Banco",
                            fontWeight = FontWeight.Black,
                            fontSize = 11.sp,
                            color = AguaPrimaryDark,
                            maxLines = 1,
                            softWrap = false,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }

                // Formulario colapsable para Agregar o Editar Cuenta (Produbanco, Banco Guayaquil, etc.)
                AnimatedVisibility(visible = mostrarFormulario) {
                    Surface(
                        color = FondoGrisClaro,
                        shape = RoundedCornerShape(16.dp),
                        border = BorderStroke(1.dp, BordeSutil),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(
                            modifier = Modifier.padding(12.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Text(
                                text = if (cuentaEditando == null) "AGREGAR CUENTA BANCARIA" else "EDITAR CUENTA BANCARIA",
                                fontWeight = FontWeight.Black,
                                fontSize = 12.sp,
                                color = PichinchaNavy
                            )

                            // Presets rápidos de Bancos del Ecuador
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .horizontalScroll(rememberScrollState()),
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                CuentasBancariasHelper.bancosPresets.forEach { (nombrePreset, colorPreset) ->
                                    FilterChip(
                                        selected = formBanco.equals(nombrePreset, ignoreCase = true),
                                        onClick = {
                                            formBanco = nombrePreset
                                            formColorHex = colorPreset
                                        },
                                        label = {
                                            Text(
                                                text = nombrePreset,
                                                fontSize = 11.sp,
                                                fontWeight = FontWeight.Bold,
                                                maxLines = 1,
                                                softWrap = false
                                            )
                                        }
                                    )
                                }
                            }

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                OutlinedTextField(
                                    value = formBanco,
                                    onValueChange = { formBanco = it },
                                    label = { Text("Banco") },
                                    singleLine = true,
                                    colors = marineOutlinedTextFieldColors(),
                                    modifier = Modifier.weight(1.2f)
                                )
                                OutlinedTextField(
                                    value = formTipoCuenta,
                                    onValueChange = { formTipoCuenta = it },
                                    label = { Text("Tipo (Ahorro/Cte)") },
                                    singleLine = true,
                                    colors = marineOutlinedTextFieldColors(),
                                    modifier = Modifier.weight(1f)
                                )
                            }

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                OutlinedTextField(
                                    value = formNumero,
                                    onValueChange = { formNumero = it },
                                    label = { Text("N° de Cuenta") },
                                    singleLine = true,
                                    colors = marineOutlinedTextFieldColors(),
                                    modifier = Modifier.weight(1f)
                                )
                                OutlinedTextField(
                                    value = formCedula,
                                    onValueChange = { formCedula = it },
                                    label = { Text("CI / RUC") },
                                    singleLine = true,
                                    colors = marineOutlinedTextFieldColors(),
                                    modifier = Modifier.weight(1f)
                                )
                            }

                            OutlinedTextField(
                                value = formTitular,
                                onValueChange = { formTitular = it },
                                label = { Text("Nombre del Titular") },
                                singleLine = true,
                                colors = marineOutlinedTextFieldColors(),
                                modifier = Modifier.fillMaxWidth()
                            )

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.End,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                OutlinedButton(
                                    onClick = {
                                        mostrarFormulario = false
                                        cuentaEditando = null
                                    },
                                    shape = RoundedCornerShape(10.dp),
                                    modifier = Modifier.height(36.dp)
                                ) {
                                    Text("Cancelar", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                }
                                Spacer(modifier = Modifier.width(8.dp))
                                Button(
                                    onClick = {
                                        if (formBanco.isNotBlank() && formNumero.isNotBlank()) {
                                            val nueva = CuentaBancariaItem(
                                                id = cuentaEditando?.id ?: "cta_${System.currentTimeMillis()}",
                                                banco = formBanco.trim(),
                                                tipoCuenta = formTipoCuenta.trim().ifBlank { "Cuenta ahorro" },
                                                numeroCuenta = formNumero.trim(),
                                                titular = formTitular.trim(),
                                                cedula = formCedula.trim(),
                                                colorHex = formColorHex
                                            )
                                            val actualizada = if (cuentaEditando != null) {
                                                cuentas.map { if (it.id == nueva.id) nueva else it }
                                            } else {
                                                cuentas + nueva
                                            }
                                            CuentasBancariasHelper.guardarListaCuentas(context, actualizada)
                                            cuentas = actualizada
                                            mostrarFormulario = false
                                            cuentaEditando = null
                                        }
                                    },
                                    colors = ButtonDefaults.buttonColors(containerColor = AguaPrimaryDark),
                                    shape = RoundedCornerShape(10.dp),
                                    modifier = Modifier.height(36.dp)
                                ) {
                                    Text("Guardar Cuenta", fontSize = 11.sp, fontWeight = FontWeight.Black, color = Color.White)
                                }
                            }
                        }
                    }
                }

                HorizontalDivider(color = BordeSutil)

                // Tarjetas Bancarias Visuales (Pichincha, Pacífico, Bolivariano y adicionales)
                cuentas.forEach { cuenta ->
                    TarjetaVisualCuentaBancaria(
                        cuenta = cuenta,
                        onEnviarWhatsApp = {
                            CuentasBancariasHelper.enviarCuentaPorWhatsApp(
                                context = context,
                                cuenta = cuenta,
                                telefonoRaw = telefonoCliente,
                                nombreCliente = nombreCliente,
                                montoACobrar = montoACobrar
                            )
                        },
                        onEditar = { abrirFormulario(cuenta) },
                        onEliminar = if (cuentas.size > 1) {
                            {
                                val filtrada = cuentas.filterNot { it.id == cuenta.id }
                                CuentasBancariasHelper.guardarListaCuentas(context, filtrada)
                                cuentas = filtrada
                            }
                        } else null
                    )
                }
            }
        }
    }
}

@Composable
private fun TarjetaVisualCuentaBancaria(
    cuenta: CuentaBancariaItem,
    onEnviarWhatsApp: () -> Unit,
    onEditar: () -> Unit,
    onEliminar: (() -> Unit)?
) {
    val colorBase = Color(cuenta.colorHex)
    val colorOscuro = Color(
        red = (colorBase.red * 0.84f).coerceIn(0f, 1f),
        green = (colorBase.green * 0.84f).coerceIn(0f, 1f),
        blue = (colorBase.blue * 0.84f).coerceIn(0f, 1f),
        alpha = 1f
    )

    val textoNumero = if (cuenta.banco.contains("Pacífico", ignoreCase = true)) {
        "Cta: ${cuenta.numeroCuenta}"
    } else if (cuenta.banco.contains("Pichincha", ignoreCase = true)) {
        "#${cuenta.numeroCuenta.removePrefix("#")}"
    } else {
        cuenta.numeroCuenta
    }

    val textoCedula = if (cuenta.banco.contains("Pacífico", ignoreCase = true)) {
        "Cédula: ${cuenta.cedula}"
    } else {
        "CI. ${cuenta.cedula}"
    }

    Surface(
        shape = RoundedCornerShape(22.dp),
        shadowElevation = 5.dp,
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onEnviarWhatsApp() }
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    Brush.linearGradient(
                        colors = listOf(colorBase, colorOscuro)
                    )
                )
                .padding(16.dp)
        ) {
            // Círculo decorativo sutil
            Box(
                modifier = Modifier
                    .size(90.dp)
                    .align(Alignment.TopEnd)
                    .clip(CircleShape)
                    .background(Color.White.copy(alpha = 0.12f))
            )

            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Spacer(modifier = Modifier.width(28.dp))
                    Text(
                        text = cuenta.banco,
                        color = Color.White,
                        fontWeight = FontWeight.Black,
                        fontSize = 20.sp,
                        textAlign = TextAlign.Center,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.weight(1f)
                    )
                    Row {
                        IconButton(
                            onClick = onEditar,
                            modifier = Modifier.size(28.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Edit,
                                contentDescription = "Editar",
                                tint = Color.White.copy(alpha = 0.9f),
                                modifier = Modifier.size(16.dp)
                            )
                        }
                        if (onEliminar != null) {
                            IconButton(
                                onClick = onEliminar,
                                modifier = Modifier.size(28.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Delete,
                                    contentDescription = "Eliminar",
                                    tint = Color.White.copy(alpha = 0.8f),
                                    modifier = Modifier.size(16.dp)
                                )
                            }
                        }
                    }
                }

                Text(
                    text = cuenta.tipoCuenta,
                    color = Color.White.copy(alpha = 0.95f),
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 14.sp,
                    maxLines = 1
                )

                Spacer(modifier = Modifier.height(4.dp))

                Text(
                    text = textoNumero,
                    color = Color.White,
                    fontWeight = FontWeight.Black,
                    fontSize = 22.sp,
                    maxLines = 1
                )

                Spacer(modifier = Modifier.height(4.dp))

                Text(
                    text = cuenta.titular,
                    color = Color.White,
                    fontWeight = FontWeight.Bold,
                    fontSize = 15.sp,
                    textAlign = TextAlign.Center,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )

                Text(
                    text = textoCedula,
                    color = Color.White.copy(alpha = 0.95f),
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 14.sp,
                    maxLines = 1
                )

                Spacer(modifier = Modifier.height(10.dp))

                // Botón directo de envío como comprobante/imagen a WhatsApp
                Surface(
                    onClick = onEnviarWhatsApp,
                    color = Color.Black.copy(alpha = 0.24f),
                    shape = RoundedCornerShape(12.dp),
                    border = BorderStroke(1.dp, Color.White.copy(alpha = 0.45f)),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(38.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 12.dp),
                        horizontalArrangement = Arrangement.Center,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.Chat,
                            contentDescription = null,
                            tint = Color.White,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "Enviar Tarjeta a WhatsApp",
                            color = Color.White,
                            fontWeight = FontWeight.Black,
                            fontSize = 12.sp,
                            maxLines = 1,
                            softWrap = false,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }
            }
        }
    }
}

package com.rutaagua.camion.ui.security

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Backspace
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.rutaagua.camion.data.local.prefs.AppPreferences
import com.rutaagua.camion.ui.theme.AguaPrimary
import com.rutaagua.camion.ui.theme.RojoDeuda
import com.rutaagua.camion.ui.theme.TextoOscuro
import com.rutaagua.camion.ui.theme.TextoSecundario

/**
 * Diálogo de seguridad con teclado numérico grande y PIN de 4 dígitos.
 * Protege acciones sensibles: editar/eliminar ventas, eliminar clientes y modificar saldos/envases.
 */
@Composable
fun SecurityPinDialog(
    titulo: String = "Autorización Requerida",
    subtitulo: String = "Ingresa el PIN de seguridad de 4 dígitos",
    onDismissRequest: () -> Unit,
    onPinCorrecto: () -> Unit
) {
    val context = LocalContext.current
    val appPreferences = remember { AppPreferences.getInstance(context) }
    var pinIngresado by remember { mutableStateOf("") }
    var errorPin by remember { mutableStateOf(false) }

    fun presionarDigito(digito: String) {
        if (pinIngresado.length < 4) {
            val nuevo = pinIngresado + digito
            pinIngresado = nuevo
            errorPin = false

            if (nuevo.length == 4) {
                if (appPreferences.validarPin(nuevo)) {
                    onPinCorrecto()
                } else {
                    errorPin = true
                    pinIngresado = ""
                }
            }
        }
    }

    fun borrarUltimo() {
        if (pinIngresado.isNotEmpty()) {
            pinIngresado = pinIngresado.dropLast(1)
            errorPin = false
        }
    }

    Dialog(onDismissRequest = onDismissRequest) {
        Card(
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            elevation = CardDefaults.cardElevation(defaultElevation = 8.dp),
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 8.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Header con icono de candado y botón cerrar
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(44.dp)
                            .clip(CircleShape)
                            .background(AguaPrimary.copy(alpha = 0.12f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Lock,
                            contentDescription = "Seguridad",
                            tint = AguaPrimary,
                            modifier = Modifier.size(24.dp)
                        )
                    }

                    IconButton(onClick = onDismissRequest) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Cerrar",
                            tint = TextoSecundario
                        )
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                Text(
                    text = titulo,
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = TextoOscuro
                )

                Spacer(modifier = Modifier.height(4.dp))

                Text(
                    text = subtitulo,
                    style = MaterialTheme.typography.bodyMedium,
                    color = TextoSecundario,
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(20.dp))

                // 4 Círculos de PIN
                Row(
                    horizontalArrangement = Arrangement.spacedBy(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    for (i in 0 until 4) {
                        val lleno = i < pinIngresado.length
                        val colorPunto = if (errorPin) {
                            RojoDeuda
                        } else if (lleno) {
                            AguaPrimary
                        } else {
                            Color(0xFFCBD5E1)
                        }

                        Box(
                            modifier = Modifier
                                .size(20.dp)
                                .clip(CircleShape)
                                .background(if (lleno || errorPin) colorPunto else Color.Transparent)
                                .border(2.dp, colorPunto, CircleShape)
                        )
                    }
                }

                if (errorPin) {
                    Spacer(modifier = Modifier.height(10.dp))
                    Text(
                        text = "PIN incorrecto. Inténtalo de nuevo.",
                        style = MaterialTheme.typography.labelMedium,
                        color = RojoDeuda,
                        fontWeight = FontWeight.SemiBold
                    )
                }

                Spacer(modifier = Modifier.height(24.dp))

                // Teclado Numérico Grande táctil para cabina
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    val filas = listOf(
                        listOf("1", "2", "3"),
                        listOf("4", "5", "6"),
                        listOf("7", "8", "9")
                    )

                    for (fila in filas) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            for (tecla in fila) {
                                TeclaBoton(
                                    texto = tecla,
                                    modifier = Modifier.weight(1f),
                                    onClick = { presionarDigito(tecla) }
                                )
                            }
                        }
                    }

                    // Última fila: Limpiar / 0 / Borrar
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .height(56.dp)
                                .clip(RoundedCornerShape(14.dp))
                                .clickable {
                                    pinIngresado = ""
                                    errorPin = false
                                },
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "C",
                                fontSize = 20.sp,
                                fontWeight = FontWeight.Bold,
                                color = TextoSecundario
                            )
                        }

                        TeclaBoton(
                            texto = "0",
                            modifier = Modifier.weight(1f),
                            onClick = { presionarDigito("0") }
                        )

                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .height(56.dp)
                                .clip(RoundedCornerShape(14.dp))
                                .background(Color(0xFFF1F5F9))
                                .clickable { borrarUltimo() },
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.Backspace,
                                contentDescription = "Borrar",
                                tint = TextoOscuro
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                TextButton(onClick = onDismissRequest) {
                    Text(
                        text = "Cancelar",
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Medium,
                        color = TextoSecundario
                    )
                }
            }
        }
    }
}

@Composable
private fun TeclaBoton(
    texto: String,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    Box(
        modifier = modifier
            .height(56.dp)
            .clip(RoundedCornerShape(14.dp))
            .background(Color(0xFFF1F5F9))
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = texto,
            fontSize = 22.sp,
            fontWeight = FontWeight.Bold,
            color = TextoOscuro
        )
    }
}

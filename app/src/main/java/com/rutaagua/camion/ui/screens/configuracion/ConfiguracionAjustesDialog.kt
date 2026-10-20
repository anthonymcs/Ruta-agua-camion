package com.rutaagua.camion.ui.screens.configuracion

import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.CloudDownload
import androidx.compose.material.icons.filled.CloudSync
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.LockReset
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.SystemUpdate
import androidx.compose.material.icons.filled.UploadFile
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.rutaagua.camion.data.backup.BackupManager
import com.rutaagua.camion.data.local.RutaAguaDatabase
import com.rutaagua.camion.data.local.model.TipoVehiculo
import com.rutaagua.camion.data.local.prefs.AppPreferences
import com.rutaagua.camion.data.remote.InfoActualizacionOta
import com.rutaagua.camion.data.remote.OTAUpdateChecker
import com.rutaagua.camion.ui.theme.AguaPrimary
import com.rutaagua.camion.ui.theme.AzulCorporativo
import com.rutaagua.camion.ui.theme.BordeSutil
import com.rutaagua.camion.ui.theme.FondoGrisClaro
import com.rutaagua.camion.ui.theme.GrisCard
import com.rutaagua.camion.ui.theme.RojoDeuda
import com.rutaagua.camion.ui.theme.SuperficieBlanca
import com.rutaagua.camion.ui.theme.TextoOscuro
import com.rutaagua.camion.ui.theme.TextoSecundario
import com.rutaagua.camion.ui.theme.VerdeEfectivo
import kotlinx.coroutines.launch

/**
 * Diálogo de Configuración General:
 * - Selección de Vehículo Activo (🚚 Camión vs 🛻 Camioneta)
 * - Cambio de PIN de Seguridad (4 dígitos)
 * - Respaldo Local JSON (Exportar / Importar / Compartir WhatsApp)
 * - Verificación de Actualizaciones OTA
 */
@Composable
fun ConfiguracionAjustesDialog(
    database: RutaAguaDatabase,
    onDismissRequest: () -> Unit
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    val appPreferences = remember { AppPreferences.getInstance(context) }
    val vehiculoActivo by appPreferences.vehiculoActivo.collectAsState()

    var seccionCambiarPinAbierta by remember { mutableStateOf(false) }
    var pinActualInput by remember { mutableStateOf("") }
    var nuevoPinInput by remember { mutableStateOf("") }
    var confirmarPinInput by remember { mutableStateOf("") }
    var errorPinMsg by remember { mutableStateOf<String?>(null) }
    var exitoPinMsg by remember { mutableStateOf<String?>(null) }

    var comprobandoOta by remember { mutableStateOf(false) }
    var infoOta by remember { mutableStateOf<InfoActualizacionOta?>(null) }
    var errorOtaMsg by remember { mutableStateOf<String?>(null) }

    var exportandoBackup by remember { mutableStateOf(false) }
    var importandoBackup by remember { mutableStateOf(false) }

    // Selector de archivo para Importar Respaldo JSON
    val filePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        if (uri != null) {
            importandoBackup = true
            coroutineScope.launch {
                val resultado = BackupManager.importarRespaldoJson(context, database, uri)
                importandoBackup = false
                resultado.onSuccess { total ->
                    Toast.makeText(context, "Respaldo restaurado: $total registros importados", Toast.LENGTH_LONG).show()
                }.onFailure { err ->
                    Toast.makeText(context, "Error al importar: ${err.message}", Toast.LENGTH_LONG).show()
                }
            }
        }
    }

    Dialog(onDismissRequest = onDismissRequest) {
        Card(
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = SuperficieBlanca),
            elevation = CardDefaults.cardElevation(defaultElevation = 8.dp),
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 16.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp)
                    .verticalScroll(rememberScrollState())
            ) {
                // Header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(40.dp)
                                .clip(CircleShape)
                                .background(AguaPrimary.copy(alpha = 0.12f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Settings,
                                contentDescription = null,
                                tint = AguaPrimary,
                                modifier = Modifier.size(24.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Text(
                            text = "Ajustes & Vehículo",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold,
                            color = TextoOscuro
                        )
                    }

                    IconButton(onClick = onDismissRequest) {
                        Icon(imageVector = Icons.Default.Close, contentDescription = "Cerrar", tint = TextoSecundario)
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))
                HorizontalDivider(color = BordeSutil)
                Spacer(modifier = Modifier.height(16.dp))

                // ==========================================
                // SECCIÓN 1: VEHÍCULO ACTIVO
                // ==========================================
                Text(
                    text = "VEHÍCULO OPERATIVO ACTUAL",
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.Bold,
                    color = AzulCorporativo
                )
                Spacer(modifier = Modifier.height(8.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    // Botón Camión
                    val esCamion = vehiculoActivo == TipoVehiculo.CAMION
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(14.dp))
                            .background(if (esCamion) AguaPrimary.copy(alpha = 0.12f) else FondoGrisClaro)
                            .border(
                                width = if (esCamion) 2.dp else 1.dp,
                                color = if (esCamion) AguaPrimary else BordeSutil,
                                shape = RoundedCornerShape(14.dp)
                            )
                            .clickable { appPreferences.guardarVehiculoActivo(TipoVehiculo.CAMION) }
                            .padding(12.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(text = "🚚", fontSize = 28.sp)
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "Camión",
                                fontWeight = FontWeight.Bold,
                                color = if (esCamion) AguaPrimary else TextoOscuro,
                                fontSize = 14.sp
                            )
                            Text(
                                text = "Cap. 218 un.",
                                style = MaterialTheme.typography.labelSmall,
                                color = TextoSecundario
                            )
                        }
                    }

                    // Botón Camioneta
                    val esCamioneta = vehiculoActivo == TipoVehiculo.CAMIONETA
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(14.dp))
                            .background(if (esCamioneta) AzulCorporativo.copy(alpha = 0.12f) else FondoGrisClaro)
                            .border(
                                width = if (esCamioneta) 2.dp else 1.dp,
                                color = if (esCamioneta) AzulCorporativo else BordeSutil,
                                shape = RoundedCornerShape(14.dp)
                            )
                            .clickable { appPreferences.guardarVehiculoActivo(TipoVehiculo.CAMIONETA) }
                            .padding(12.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(text = "🛻", fontSize = 28.sp)
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "Camioneta",
                                fontWeight = FontWeight.Bold,
                                color = if (esCamioneta) AzulCorporativo else TextoOscuro,
                                fontSize = 14.sp
                            )
                            Text(
                                text = "Cap. 80 un.",
                                style = MaterialTheme.typography.labelSmall,
                                color = TextoSecundario
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(20.dp))
                HorizontalDivider(color = BordeSutil)
                Spacer(modifier = Modifier.height(16.dp))

                // ==========================================
                // SECCIÓN 2: SEGURIDAD Y PIN
                // ==========================================
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "SEGURIDAD POR PIN",
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.Bold,
                            color = AzulCorporativo
                        )
                        Text(
                            text = "Protege edición/borrado de ventas y clientes",
                            style = MaterialTheme.typography.bodySmall,
                            color = TextoSecundario
                        )
                    }

                    TextButton(onClick = {
                        seccionCambiarPinAbierta = !seccionCambiarPinAbierta
                        errorPinMsg = null
                        exitoPinMsg = null
                    }) {
                        Text(
                            text = if (seccionCambiarPinAbierta) "Cerrar" else "Cambiar PIN",
                            fontWeight = FontWeight.Bold,
                            color = AguaPrimary
                        )
                    }
                }

                if (seccionCambiarPinAbierta) {
                    Spacer(modifier = Modifier.height(10.dp))
                    Card(
                        colors = CardDefaults.cardColors(containerColor = FondoGrisClaro),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(14.dp)) {
                            OutlinedTextField(
                                value = pinActualInput,
                                onValueChange = { if (it.length <= 4) pinActualInput = it },
                                label = { Text("PIN Actual") },
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.NumberPassword),
                                visualTransformation = PasswordVisualTransformation(),
                                singleLine = true,
                                modifier = Modifier.fillMaxWidth()
                            )

                            Spacer(modifier = Modifier.height(8.dp))

                            OutlinedTextField(
                                value = nuevoPinInput,
                                onValueChange = { if (it.length <= 4) nuevoPinInput = it },
                                label = { Text("Nuevo PIN (4 dígitos)") },
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.NumberPassword),
                                visualTransformation = PasswordVisualTransformation(),
                                singleLine = true,
                                modifier = Modifier.fillMaxWidth()
                            )

                            Spacer(modifier = Modifier.height(8.dp))

                            OutlinedTextField(
                                value = confirmarPinInput,
                                onValueChange = { if (it.length <= 4) confirmarPinInput = it },
                                label = { Text("Confirmar Nuevo PIN") },
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.NumberPassword),
                                visualTransformation = PasswordVisualTransformation(),
                                singleLine = true,
                                modifier = Modifier.fillMaxWidth()
                            )

                            if (errorPinMsg != null) {
                                Spacer(modifier = Modifier.height(6.dp))
                                Text(
                                    text = errorPinMsg!!,
                                    color = RojoDeuda,
                                    style = MaterialTheme.typography.bodySmall,
                                    fontWeight = FontWeight.SemiBold
                                )
                            }

                            if (exitoPinMsg != null) {
                                Spacer(modifier = Modifier.height(6.dp))
                                Text(
                                    text = exitoPinMsg!!,
                                    color = VerdeEfectivo,
                                    style = MaterialTheme.typography.bodySmall,
                                    fontWeight = FontWeight.SemiBold
                                )
                            }

                            Spacer(modifier = Modifier.height(10.dp))

                            Button(
                                onClick = {
                                    errorPinMsg = null
                                    exitoPinMsg = null
                                    if (!appPreferences.validarPin(pinActualInput)) {
                                        errorPinMsg = "El PIN actual es incorrecto"
                                        return@Button
                                    }
                                    if (nuevoPinInput.length != 4 || !nuevoPinInput.all { it.isDigit() }) {
                                        errorPinMsg = "El nuevo PIN debe tener exactamente 4 dígitos"
                                        return@Button
                                    }
                                    if (nuevoPinInput != confirmarPinInput) {
                                        errorPinMsg = "Los nuevos PIN no coinciden"
                                        return@Button
                                    }

                                    appPreferences.guardarPinSeguridad(nuevoPinInput)
                                    exitoPinMsg = "¡PIN actualizado exitosamente!"
                                    pinActualInput = ""
                                    nuevoPinInput = ""
                                    confirmarPinInput = ""
                                },
                                colors = ButtonDefaults.buttonColors(containerColor = AguaPrimary),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Text("Guardar Nuevo PIN", fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(20.dp))
                HorizontalDivider(color = BordeSutil)
                Spacer(modifier = Modifier.height(16.dp))

                // ==========================================
                // SECCIÓN 3: RESPALDO LOCAL JSON
                // ==========================================
                Text(
                    text = "COPIA DE SEGURIDAD LOCAL (JSON)",
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.Bold,
                    color = AzulCorporativo
                )
                Text(
                    text = "Exporta o restaura clientes, ventas y contabilidad sin internet",
                    style = MaterialTheme.typography.bodySmall,
                    color = TextoSecundario
                )
                Spacer(modifier = Modifier.height(10.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    OutlinedButton(
                        onClick = {
                            exportandoBackup = true
                            coroutineScope.launch {
                                try {
                                    val archivo = BackupManager.exportarRespaldoJson(context, database)
                                    BackupManager.compartirRespaldo(context, archivo)
                                } catch (e: Exception) {
                                    Toast.makeText(context, "Error exportando: ${e.message}", Toast.LENGTH_SHORT).show()
                                } finally {
                                    exportandoBackup = false
                                }
                            }
                        },
                        enabled = !exportandoBackup,
                        modifier = Modifier.weight(1f)
                    ) {
                        if (exportandoBackup) {
                            CircularProgressIndicator(modifier = Modifier.size(16.dp), strokeWidth = 2.dp)
                        } else {
                            Icon(imageVector = Icons.Default.Share, contentDescription = null, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Exportar", fontSize = 13.sp)
                        }
                    }

                    OutlinedButton(
                        onClick = { filePickerLauncher.launch("application/json") },
                        enabled = !importandoBackup,
                        modifier = Modifier.weight(1f)
                    ) {
                        if (importandoBackup) {
                            CircularProgressIndicator(modifier = Modifier.size(16.dp), strokeWidth = 2.dp)
                        } else {
                            Icon(imageVector = Icons.Default.UploadFile, contentDescription = null, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Importar", fontSize = 13.sp)
                        }
                    }
                }

                Spacer(modifier = Modifier.height(20.dp))
                HorizontalDivider(color = BordeSutil)
                Spacer(modifier = Modifier.height(16.dp))

                // ==========================================
                // SECCIÓN 4: ACTUALIZACIONES OTA
                // ==========================================
                Text(
                    text = "ACTUALIZACIONES DE LA APP (OTA)",
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.Bold,
                    color = AzulCorporativo
                )
                Spacer(modifier = Modifier.height(10.dp))

                Button(
                    onClick = {
                        comprobandoOta = true
                        errorOtaMsg = null
                        infoOta = null
                        coroutineScope.launch {
                            val res = OTAUpdateChecker.verificarNuevaVersion(context)
                            comprobandoOta = false
                            res.onSuccess { info ->
                                infoOta = info
                            }.onFailure { err ->
                                errorOtaMsg = err.message ?: "No se pudo conectar al servidor OTA"
                            }
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF0F172A)),
                    enabled = !comprobandoOta,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    if (comprobandoOta) {
                        CircularProgressIndicator(color = Color.White, modifier = Modifier.size(18.dp), strokeWidth = 2.dp)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Comprobando...", color = Color.White)
                    } else {
                        Icon(imageVector = Icons.Default.SystemUpdate, contentDescription = null)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Buscar Actualizaciones")
                    }
                }

                if (errorOtaMsg != null) {
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "Error al buscar: $errorOtaMsg",
                        style = MaterialTheme.typography.bodySmall,
                        color = RojoDeuda
                    )
                }

                if (infoOta != null) {
                    val info = infoOta!!
                    Spacer(modifier = Modifier.height(10.dp))
                    Card(
                        colors = CardDefaults.cardColors(
                            containerColor = if (info.hayNuevaVersion) VerdeEfectivo.copy(alpha = 0.12f) else FondoGrisClaro
                        ),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            if (info.hayNuevaVersion) {
                                Text(
                                    text = "¡Nueva versión disponible: v${info.versionNameRemoto}!",
                                    fontWeight = FontWeight.Bold,
                                    color = VerdeEfectivo
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = info.notasVersion,
                                    style = MaterialTheme.typography.bodySmall,
                                    color = TextoOscuro
                                )
                                Spacer(modifier = Modifier.height(10.dp))
                                Button(
                                    onClick = {
                                        OTAUpdateChecker.descargarEInstalarApk(
                                            context,
                                            info.urlDescargaApk,
                                            info.versionNameRemoto
                                        )
                                    },
                                    colors = ButtonDefaults.buttonColors(containerColor = VerdeEfectivo),
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Icon(imageVector = Icons.Default.CloudDownload, contentDescription = null)
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text("Descargar e Instalar APK", fontWeight = FontWeight.Bold)
                                }
                            } else {
                                Text(
                                    text = "✅ Tienes la última versión instalada",
                                    fontWeight = FontWeight.Bold,
                                    color = AzulCorporativo
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                Button(
                    onClick = onDismissRequest,
                    colors = ButtonDefaults.buttonColors(containerColor = FondoGrisClaro),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Cerrar", color = TextoOscuro, fontWeight = FontWeight.SemiBold)
                }
            }
        }
    }
}

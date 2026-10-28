package com.rutaagua.camion.ui.screens.configuracion

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
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
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AdminPanelSettings
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.CloudDownload
import androidx.compose.material.icons.filled.CloudUpload
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.PhoneAndroid
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.SystemUpdate
import androidx.compose.material.icons.filled.VerifiedUser
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.rutaagua.camion.data.backup.BackupManager
import com.rutaagua.camion.data.local.RutaAguaDatabase
import com.rutaagua.camion.data.local.model.TipoVehiculo
import com.rutaagua.camion.data.local.prefs.AppPreferences
import com.rutaagua.camion.data.local.prefs.RolUsuario
import com.rutaagua.camion.data.remote.InfoActualizacionOta
import com.rutaagua.camion.data.remote.OTAUpdateChecker
import com.rutaagua.camion.ui.components.ClayBadge
import com.rutaagua.camion.ui.components.marineOutlinedTextFieldColors
import com.rutaagua.camion.ui.security.SecurityPinDialog
import com.rutaagua.camion.ui.theme.AguaPrimary
import com.rutaagua.camion.ui.theme.AguaPrimaryDark
import com.rutaagua.camion.ui.theme.AmbarAdvertencia
import com.rutaagua.camion.ui.theme.AmbarFondo
import com.rutaagua.camion.ui.theme.AzulCorporativo
import com.rutaagua.camion.ui.theme.BordeSuave
import com.rutaagua.camion.ui.theme.FondoGrisClaro
import com.rutaagua.camion.ui.theme.RojoAlerta
import com.rutaagua.camion.ui.theme.TextoOscuro
import com.rutaagua.camion.ui.theme.TextoSecundario
import com.rutaagua.camion.ui.theme.VerdeEfectivo
import com.rutaagua.camion.ui.theme.VerdeEfectivoFondo
import kotlinx.coroutines.launch

/**
 * Diálogo de Configuración General:
 * - Rol de Dispositivo (Teléfono de Ruta vs Administrador Rolando con PIN)
 * - Seguridad por PIN (con diseño de 1 sola línea garantizado)
 * - Respaldo Mensual Automático + Autoprueba de Integridad (Local + Firebase Cloud)
 * - Actualizaciones Remotas OTA
 * (Nota: El cambio de vehículo operativo solo se realiza cerrando sesión desde el Login).
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
    val rolActivo by appPreferences.rolActivo.collectAsState()

    var mostrarPinParaModoRolando by rememberSaveable { mutableStateOf(false) }
    var mostrarCambiarPin by rememberSaveable { mutableStateOf(false) }

    var pinActual by rememberSaveable { mutableStateOf("") }
    var pinNuevo by rememberSaveable { mutableStateOf("") }
    var pinConfirmar by rememberSaveable { mutableStateOf("") }
    var mensajeEstado by rememberSaveable { mutableStateOf<String?>(null) }
    var esErrorEstado by rememberSaveable { mutableStateOf(false) }

    // Estado de Respaldo Mensual Automático
    var resumenRespaldoAuto by rememberSaveable {
        mutableStateOf(appPreferences.obtenerDetalleUltimoRespaldoAuto())
    }
    var probandoRespaldoAuto by rememberSaveable { mutableStateOf(false) }

    // Estado de OTA
    var urlOta by rememberSaveable { mutableStateOf(appPreferences.obtenerUrlOta()) }
    var buscandoOta by rememberSaveable { mutableStateOf(false) }
    var infoActualizacion by remember { mutableStateOf<InfoActualizacionOta?>(null) }
    val versionActual = remember {
        runCatching {
            context.packageManager.getPackageInfo(context.packageName, 0).versionName ?: "1.0"
        }.getOrDefault("1.0")
    }

    // Al abrir ajustes, verifica y prueba automáticamente el respaldo del mes en curso
    LaunchedEffect(Unit) {
        probandoRespaldoAuto = true
        val res = BackupManager.verificarYEjecutarRespaldoMensualAutomatico(
            context = context,
            database = database,
            forzarAhora = false
        )
        resumenRespaldoAuto = res.resumenVerificacion
        probandoRespaldoAuto = false
    }

    val launcherImportarJson = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        if (uri != null) {
            coroutineScope.launch {
                val resultado = BackupManager.importarRespaldoDesdeUri(context, uri, database)
                resultado.onSuccess { msg ->
                    esErrorEstado = false
                    mensajeEstado = msg
                }.onFailure { err ->
                    esErrorEstado = true
                    mensajeEstado = "Error al restaurar: ${err.localizedMessage}"
                }
            }
        }
    }

    Dialog(
        onDismissRequest = onDismissRequest,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            modifier = Modifier
                .fillMaxWidth(0.94f)
                .padding(vertical = 20.dp),
            shape = RoundedCornerShape(24.dp),
            color = Color.White,
            tonalElevation = 8.dp
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState())
                    .padding(18.dp)
            ) {
                // Cabecera
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Ajustes y Seguridad",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Black,
                            color = TextoOscuro,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        val etiquetaUnidad = if (vehiculoActivo == TipoVehiculo.CAMIONETA) "Camioneta" else "Camión Principal"
                        Text(
                            text = "Sesión activa: $etiquetaUnidad • v$versionActual",
                            style = MaterialTheme.typography.bodySmall,
                            color = AguaPrimaryDark,
                            fontWeight = FontWeight.Bold,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
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

                Spacer(modifier = Modifier.height(10.dp))

                // Mensaje de estado
                mensajeEstado?.let { msg ->
                    Surface(
                        color = if (esErrorEstado) Color(0xFFFFEBEE) else VerdeEfectivoFondo,
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 12.dp)
                    ) {
                        Text(
                            text = msg,
                            color = if (esErrorEstado) RojoAlerta else VerdeEfectivo,
                            style = MaterialTheme.typography.bodySmall,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(12.dp)
                        )
                    }
                }

                // ---------------------------------------------------------
                // 1. MODO DE PRIVACIDAD Y ROL (OPERADOR VS ROLANDO)
                // ---------------------------------------------------------
                Surface(
                    color = FondoGrisClaro,
                    shape = RoundedCornerShape(16.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Text(
                            text = "MODO DEL TELÉFONO",
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Black,
                            color = AguaPrimaryDark,
                            maxLines = 1
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "El modo Ruta oculta costos y ganancias privadas.",
                            style = MaterialTheme.typography.bodySmall,
                            color = TextoSecundario
                        )
                        Spacer(modifier = Modifier.height(10.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            val esOperador = rolActivo == RolUsuario.OPERADOR_RUTA
                            Button(
                                onClick = {
                                    appPreferences.guardarRolActivo(RolUsuario.OPERADOR_RUTA)
                                    esErrorEstado = false
                                    mensajeEstado = "Modo Teléfono de Ruta activado"
                                },
                                modifier = Modifier
                                    .weight(1f)
                                    .height(44.dp),
                                shape = RoundedCornerShape(12.dp),
                                contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = if (esOperador) AguaPrimary else Color.White,
                                    contentColor = if (esOperador) Color.White else TextoOscuro
                                )
                            ) {
                                Icon(Icons.Default.PhoneAndroid, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(5.dp))
                                Text(
                                    text = "Modo Ruta",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 12.sp,
                                    maxLines = 1,
                                    softWrap = false,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }

                            val esAdmin = rolActivo == RolUsuario.ADMIN_ROLANDO
                            Button(
                                onClick = {
                                    if (!esAdmin) {
                                        mostrarPinParaModoRolando = true
                                    }
                                },
                                modifier = Modifier
                                    .weight(1f)
                                    .height(44.dp),
                                shape = RoundedCornerShape(12.dp),
                                contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = if (esAdmin) AzulCorporativo else Color.White,
                                    contentColor = if (esAdmin) Color.White else TextoOscuro
                                )
                            ) {
                                Icon(Icons.Default.AdminPanelSettings, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(5.dp))
                                Text(
                                    text = "Rolando",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 12.sp,
                                    maxLines = 1,
                                    softWrap = false,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // ---------------------------------------------------------
                // 2. SEGURIDAD POR PIN (1 SOLA LÍNEA GARANTIZADA)
                // ---------------------------------------------------------
                Surface(
                    color = FondoGrisClaro,
                    shape = RoundedCornerShape(16.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = "SEGURIDAD POR PIN",
                                    style = MaterialTheme.typography.labelSmall,
                                    fontWeight = FontWeight.Black,
                                    color = AguaPrimaryDark,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                                Text(
                                    text = "Protege edición y borrado",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = TextoSecundario,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }

                            Spacer(modifier = Modifier.width(10.dp))

                            Surface(
                                onClick = { mostrarCambiarPin = !mostrarCambiarPin },
                                color = AguaPrimary.copy(alpha = 0.12f),
                                shape = RoundedCornerShape(10.dp),
                                border = BorderStroke(1.dp, AguaPrimary.copy(alpha = 0.3f))
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Lock,
                                        contentDescription = null,
                                        tint = AguaPrimaryDark,
                                        modifier = Modifier.size(15.dp)
                                    )
                                    Spacer(modifier = Modifier.width(5.dp))
                                    Text(
                                        text = if (mostrarCambiarPin) "Ocultar" else "Cambiar PIN",
                                        fontWeight = FontWeight.Black,
                                        fontSize = 12.sp,
                                        color = AguaPrimaryDark,
                                        maxLines = 1,
                                        softWrap = false
                                    )
                                }
                            }
                        }

                        if (mostrarCambiarPin) {
                            Spacer(modifier = Modifier.height(10.dp))
                            OutlinedTextField(
                                value = pinActual,
                                onValueChange = { if (it.length <= 4 && it.all { c -> c.isDigit() }) pinActual = it },
                                label = { Text("PIN actual") },
                                visualTransformation = PasswordVisualTransformation(),
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.NumberPassword),
                                singleLine = true,
                                colors = marineOutlinedTextFieldColors(),
                                modifier = Modifier.fillMaxWidth()
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                OutlinedTextField(
                                    value = pinNuevo,
                                    onValueChange = { if (it.length <= 4 && it.all { c -> c.isDigit() }) pinNuevo = it },
                                    label = { Text("Nuevo PIN") },
                                    visualTransformation = PasswordVisualTransformation(),
                                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.NumberPassword),
                                    singleLine = true,
                                    colors = marineOutlinedTextFieldColors(),
                                    modifier = Modifier.weight(1f)
                                )
                                OutlinedTextField(
                                    value = pinConfirmar,
                                    onValueChange = { if (it.length <= 4 && it.all { c -> c.isDigit() }) pinConfirmar = it },
                                    label = { Text("Confirmar") },
                                    visualTransformation = PasswordVisualTransformation(),
                                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.NumberPassword),
                                    singleLine = true,
                                    colors = marineOutlinedTextFieldColors(),
                                    modifier = Modifier.weight(1f)
                                )
                            }
                            Spacer(modifier = Modifier.height(10.dp))
                            Button(
                                onClick = {
                                    if (!appPreferences.validarPin(pinActual)) {
                                        esErrorEstado = true
                                        mensajeEstado = "El PIN actual no es correcto"
                                    } else if (pinNuevo.length != 4 || pinNuevo != pinConfirmar) {
                                        esErrorEstado = true
                                        mensajeEstado = "El nuevo PIN debe tener 4 dígitos y coincidir"
                                    } else {
                                        appPreferences.guardarPinSeguridad(pinNuevo)
                                        pinActual = ""
                                        pinNuevo = ""
                                        pinConfirmar = ""
                                        mostrarCambiarPin = false
                                        esErrorEstado = false
                                        mensajeEstado = "PIN actualizado con éxito"
                                    }
                                },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(44.dp),
                                shape = RoundedCornerShape(12.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = AguaPrimary)
                            ) {
                                Text(
                                    text = "Guardar Nuevo PIN",
                                    fontWeight = FontWeight.Bold,
                                    maxLines = 1,
                                    softWrap = false
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // ---------------------------------------------------------
                // 3. RESPALDO MENSUAL AUTOMÁTICO Y AUTOPRUEBA (LOCAL + FIREBASE)
                // ---------------------------------------------------------
                Surface(
                    color = FondoGrisClaro,
                    shape = RoundedCornerShape(16.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.weight(1f)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.VerifiedUser,
                                    contentDescription = null,
                                    tint = VerdeEfectivo,
                                    modifier = Modifier.size(18.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = "RESPALDO MENSUAL AUTOMÁTICO",
                                    style = MaterialTheme.typography.labelSmall,
                                    fontWeight = FontWeight.Black,
                                    color = AguaPrimaryDark,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }

                            ClayBadge(
                                text = "Auto + Nube",
                                containerColor = VerdeEfectivoFondo,
                                contentColor = VerdeEfectivo
                            )
                        }

                        Spacer(modifier = Modifier.height(6.dp))

                        Surface(
                            color = Color.White,
                            shape = RoundedCornerShape(12.dp),
                            border = BorderStroke(1.dp, BordeSuave),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 8.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                if (probandoRespaldoAuto) {
                                    CircularProgressIndicator(
                                        modifier = Modifier.size(16.dp),
                                        strokeWidth = 2.dp,
                                        color = AguaPrimary
                                    )
                                } else {
                                    Icon(
                                        imageVector = Icons.Default.CheckCircle,
                                        contentDescription = null,
                                        tint = VerdeEfectivo,
                                        modifier = Modifier.size(16.dp)
                                    )
                                }
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = resumenRespaldoAuto,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = TextoOscuro,
                                    modifier = Modifier.weight(1f)
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        // Fila de 3 acciones de 1 sola línea: Autoprobar | Exportar | Restaurar
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Button(
                                onClick = {
                                    coroutineScope.launch {
                                        probandoRespaldoAuto = true
                                        val res = BackupManager.verificarYEjecutarRespaldoMensualAutomatico(
                                            context = context,
                                            database = database,
                                            forzarAhora = true
                                        )
                                        resumenRespaldoAuto = res.resumenVerificacion
                                        probandoRespaldoAuto = false
                                        esErrorEstado = !res.exito
                                        mensajeEstado = if (res.exito) {
                                            "Respaldo mensual autogenerado y verificado al 100%"
                                        } else {
                                            res.resumenVerificacion
                                        }
                                    }
                                },
                                modifier = Modifier
                                    .weight(1f)
                                    .height(42.dp),
                                shape = RoundedCornerShape(12.dp),
                                contentPadding = PaddingValues(horizontal = 6.dp, vertical = 4.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = AguaPrimary)
                            ) {
                                Icon(Icons.Default.Refresh, contentDescription = null, modifier = Modifier.size(15.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = "Autoprobar",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 11.sp,
                                    maxLines = 1,
                                    softWrap = false,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }

                            Button(
                                onClick = {
                                    coroutineScope.launch {
                                        try {
                                            val archivo = BackupManager.exportarRespaldoJson(context, database)
                                            BackupManager.compartirArchivoRespaldo(context, archivo)
                                            esErrorEstado = false
                                            mensajeEstado = "Respaldo listo para compartir"
                                        } catch (e: Exception) {
                                            esErrorEstado = true
                                            mensajeEstado = "Error al exportar: ${e.localizedMessage}"
                                        }
                                    }
                                },
                                modifier = Modifier
                                    .weight(1f)
                                    .height(42.dp),
                                shape = RoundedCornerShape(12.dp),
                                contentPadding = PaddingValues(horizontal = 6.dp, vertical = 4.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = VerdeEfectivo)
                            ) {
                                Icon(Icons.Default.CloudUpload, contentDescription = null, modifier = Modifier.size(15.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = "Exportar",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 11.sp,
                                    maxLines = 1,
                                    softWrap = false,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }

                            OutlinedButton(
                                onClick = { launcherImportarJson.launch("application/json") },
                                modifier = Modifier
                                    .weight(1f)
                                    .height(42.dp),
                                shape = RoundedCornerShape(12.dp),
                                contentPadding = PaddingValues(horizontal = 6.dp, vertical = 4.dp)
                            ) {
                                Icon(Icons.Default.CloudDownload, contentDescription = null, modifier = Modifier.size(15.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = "Restaurar",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 11.sp,
                                    maxLines = 1,
                                    softWrap = false,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // ---------------------------------------------------------
                // 4. ACTUALIZACIONES REMOTAS (OTA)
                // ---------------------------------------------------------
                Surface(
                    color = FondoGrisClaro,
                    shape = RoundedCornerShape(16.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Text(
                            text = "ACTUALIZACIÓN REMOTA (OTA)",
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Black,
                            color = AguaPrimaryDark,
                            maxLines = 1
                        )
                        Spacer(modifier = Modifier.height(6.dp))

                        OutlinedTextField(
                            value = urlOta,
                            onValueChange = {
                                urlOta = it
                                appPreferences.guardarUrlOta(it)
                            },
                            label = { Text("URL de version.json") },
                            singleLine = true,
                            colors = marineOutlinedTextFieldColors(),
                            modifier = Modifier.fillMaxWidth()
                        )

                        Spacer(modifier = Modifier.height(8.dp))

                        Button(
                            onClick = {
                                buscandoOta = true
                                appPreferences.guardarUrlOta(urlOta.trim())
                                coroutineScope.launch {
                                    val result = OTAUpdateChecker.verificarNuevaVersion(context)
                                    buscandoOta = false
                                    result.onSuccess { info ->
                                        infoActualizacion = info
                                        if (info.hayNuevaVersion) {
                                            esErrorEstado = false
                                            mensajeEstado = "¡Nueva versión ${info.versionNameRemoto} disponible!"
                                        } else {
                                            esErrorEstado = false
                                            mensajeEstado = "Ya tienes la última versión instalada (v$versionActual)"
                                        }
                                    }.onFailure { err ->
                                        esErrorEstado = true
                                        mensajeEstado = "No se pudo verificar OTA: ${err.localizedMessage}"
                                    }
                                }
                            },
                            enabled = !buscandoOta,
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(44.dp),
                            shape = RoundedCornerShape(12.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = AzulCorporativo)
                        ) {
                            if (buscandoOta) {
                                CircularProgressIndicator(
                                    modifier = Modifier.size(18.dp),
                                    color = Color.White,
                                    strokeWidth = 2.dp
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("Verificando...", maxLines = 1, softWrap = false)
                            } else {
                                Icon(Icons.Default.SystemUpdate, contentDescription = null, modifier = Modifier.size(18.dp))
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = "Buscar Actualización",
                                    fontWeight = FontWeight.Bold,
                                    maxLines = 1,
                                    softWrap = false,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }
                        }

                        infoActualizacion?.let { info ->
                            if (info.hayNuevaVersion && info.urlDescargaApk.isNotBlank()) {
                                Spacer(modifier = Modifier.height(8.dp))
                                Surface(
                                    color = AmbarFondo,
                                    shape = RoundedCornerShape(12.dp),
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Column(modifier = Modifier.padding(12.dp)) {
                                        Text(
                                            text = "Versión ${info.versionNameRemoto} lista para instalar",
                                            fontWeight = FontWeight.Black,
                                            color = AmbarAdvertencia
                                        )
                                        if (info.notasVersion.isNotBlank()) {
                                            Text(
                                                text = info.notasVersion,
                                                style = MaterialTheme.typography.bodySmall,
                                                color = TextoOscuro
                                            )
                                        }
                                        Spacer(modifier = Modifier.height(8.dp))
                                        Button(
                                            onClick = {
                                                OTAUpdateChecker.descargarEInstalarApk(
                                                    context = context,
                                                    urlApk = info.urlDescargaApk,
                                                    versionName = info.versionNameRemoto
                                                )
                                                esErrorEstado = false
                                                mensajeEstado = "Descargando actualización en la barra de notificaciones..."
                                            },
                                            modifier = Modifier.fillMaxWidth(),
                                            colors = ButtonDefaults.buttonColors(containerColor = VerdeEfectivo)
                                        ) {
                                            Text(
                                                text = "Descargar e Instalar",
                                                fontWeight = FontWeight.Black,
                                                maxLines = 1,
                                                softWrap = false
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    if (mostrarPinParaModoRolando) {
        SecurityPinDialog(
            titulo = "Activar Modo Rolando",
            subtitulo = "Ingresa el PIN de seguridad (Por defecto: 1234)",
            onDismissRequest = { mostrarPinParaModoRolando = false },
            onPinCorrecto = {
                mostrarPinParaModoRolando = false
                appPreferences.guardarRolActivo(RolUsuario.ADMIN_ROLANDO)
                esErrorEstado = false
                mensajeEstado = "Modo Administrador Rolando activado"
            }
        )
    }
}

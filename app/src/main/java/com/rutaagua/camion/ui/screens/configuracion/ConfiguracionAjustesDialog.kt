package com.rutaagua.camion.ui.screens.configuracion

import android.content.Context
import android.widget.Toast
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
import androidx.compose.material.icons.filled.Cached
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.CloudDone
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.NotificationsActive
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Sync
import androidx.compose.material.icons.filled.SystemUpdate
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
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
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.google.firebase.auth.FirebaseAuth
import com.rutaagua.camion.data.local.RutaAguaDatabase
import com.rutaagua.camion.data.local.model.TipoVehiculo
import com.rutaagua.camion.data.local.prefs.AppPreferences
import com.rutaagua.camion.data.local.prefs.RolUsuario
import com.rutaagua.camion.data.remote.InfoActualizacionOta
import com.rutaagua.camion.data.remote.OTAUpdateChecker
import com.rutaagua.camion.ui.components.ClayBadge
import com.rutaagua.camion.ui.components.VehiculoThumbnail
import com.rutaagua.camion.ui.components.marineOutlinedTextFieldColors
import com.rutaagua.camion.ui.theme.AguaPrimary
import com.rutaagua.camion.ui.theme.AguaPrimaryDark
import com.rutaagua.camion.ui.theme.AmbarAdvertencia
import com.rutaagua.camion.ui.theme.AmbarFondo
import com.rutaagua.camion.ui.theme.AzulCorporativo
import com.rutaagua.camion.ui.theme.BordeSuave
import com.rutaagua.camion.ui.theme.BordeSutil
import com.rutaagua.camion.ui.theme.FondoGrisClaro
import com.rutaagua.camion.ui.theme.RojoAlerta
import com.rutaagua.camion.ui.theme.TextoOscuro
import com.rutaagua.camion.ui.theme.TextoSecundario
import com.rutaagua.camion.ui.theme.VerdeEfectivo
import com.rutaagua.camion.ui.theme.VerdeEfectivoFondo
import kotlinx.coroutines.launch

/**
 * Diálogo de Configuración General:
 * - Sesión activa y estado de sincronización en tiempo real.
 * - Preferencias operativas (vibración háptica, firma WhatsApp).
 * - Seguridad (Cambio de PIN de 4 dígitos).
 * - Almacenamiento (Estado del respaldo automático y limpieza de caché).
 * - Comprobador de actualizaciones OTA conectadas a GitHub Releases.
 */
@Composable
fun ConfiguracionAjustesDialog(
    database: RutaAguaDatabase,
    onDismissRequest: () -> Unit
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    val appPreferences = remember { AppPreferences.getInstance(context) }
    val auth = remember { FirebaseAuth.getInstance() }

    val vehiculoActivo by appPreferences.vehiculoActivo.collectAsState()
    val rolActivo by appPreferences.rolActivo.collectAsState()

    var mostrarCambiarPin by rememberSaveable { mutableStateOf(false) }
    var pinActual by rememberSaveable { mutableStateOf("") }
    var pinNuevo by rememberSaveable { mutableStateOf("") }
    var pinConfirmar by rememberSaveable { mutableStateOf("") }

    var mensajeEstado by rememberSaveable { mutableStateOf<String?>(null) }
    var esErrorEstado by rememberSaveable { mutableStateOf(false) }

    var buscandoOta by rememberSaveable { mutableStateOf(false) }
    var infoActualizacion by remember { mutableStateOf<InfoActualizacionOta?>(null) }

    val versionActual = remember {
        runCatching {
            context.packageManager.getPackageInfo(context.packageName, 0).versionName ?: "1.1.0"
        }.getOrDefault("1.1.0")
    }

    val usuarioEmail = auth.currentUser?.email ?: if (vehiculoActivo == TipoVehiculo.CAMIONETA) "camioneta@rutaatenea.app" else "camion@rutaatenea.app"

    Dialog(
        onDismissRequest = onDismissRequest,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            modifier = Modifier
                .fillMaxWidth(0.94f)
                .padding(vertical = 18.dp),
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
                // 1. Cabecera con botón de cerrar
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Ajustes del Sistema",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Black,
                            color = TextoOscuro,
                            maxLines = 1,
                            softWrap = false,
                            overflow = TextOverflow.Ellipsis
                        )
                        Text(
                            text = "Atenea • v$versionActual",
                            style = MaterialTheme.typography.bodySmall,
                            color = TextoSecundario,
                            fontWeight = FontWeight.SemiBold,
                            maxLines = 1,
                            softWrap = false
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

                // Banner de mensajes dinámicos de éxito o error
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
                // 2. PERFIL Y SESIÓN ACTIVA
                // ---------------------------------------------------------
                Surface(
                    color = FondoGrisClaro,
                    shape = RoundedCornerShape(16.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(14.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        VehiculoThumbnail(
                            tipo = vehiculoActivo,
                            size = 46.dp
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = usuarioEmail,
                                fontWeight = FontWeight.Black,
                                fontSize = 14.sp,
                                color = TextoOscuro,
                                maxLines = 1,
                                softWrap = false,
                                overflow = TextOverflow.Ellipsis
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                ClayBadge(
                                    text = if (rolActivo == RolUsuario.ADMIN_ROLANDO) "ADMINISTRACIÓN" else "OPERADOR RUTA",
                                    containerColor = if (rolActivo == RolUsuario.ADMIN_ROLANDO) AzulCorporativo.copy(alpha = 0.15f) else VerdeEfectivoFondo,
                                    contentColor = if (rolActivo == RolUsuario.ADMIN_ROLANDO) AzulCorporativo else VerdeEfectivo
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Box(
                                        modifier = Modifier
                                            .size(6.dp)
                                            .background(VerdeEfectivo, CircleShape)
                                    )
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text(
                                        text = "En línea",
                                        fontSize = 11.sp,
                                        color = VerdeEfectivo,
                                        fontWeight = FontWeight.Bold,
                                        maxLines = 1,
                                        softWrap = false
                                    )
                                }
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // ---------------------------------------------------------
                // 3. PREFERENCIAS DE RUTA (Vibración, Comprobantes)
                // ---------------------------------------------------------
                Surface(
                    color = FondoGrisClaro,
                    shape = RoundedCornerShape(16.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Text(
                            text = "PREFERENCIAS DE RUTA",
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Black,
                            color = AguaPrimaryDark,
                            maxLines = 1
                        )
                        Spacer(modifier = Modifier.height(10.dp))

                        // Flota compartida
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.Sync,
                                contentDescription = null,
                                tint = AguaPrimaryDark,
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(10.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = "Base de Clientes Compartida",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 13.sp,
                                    color = TextoOscuro,
                                    maxLines = 1,
                                    softWrap = false
                                )
                                Text(
                                    text = "Camión y Camioneta sincronizados en tiempo real",
                                    fontSize = 11.sp,
                                    color = TextoSecundario,
                                    maxLines = 1,
                                    softWrap = false
                                )
                            }
                            ClayBadge(
                                text = "Activo",
                                containerColor = Color(0xFFF0FDF4),
                                contentColor = VerdeEfectivo
                            )
                        }

                        Spacer(modifier = Modifier.height(8.dp))
                        HorizontalDivider(color = BordeSuave)
                        Spacer(modifier = Modifier.height(8.dp))

                        // Comprobante WhatsApp
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.Share,
                                contentDescription = null,
                                tint = VerdeEfectivo,
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(10.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = "Firma de WhatsApp",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 13.sp,
                                    color = TextoOscuro,
                                    maxLines = 1,
                                    softWrap = false
                                )
                                Text(
                                    text = "Aqua Apolo 💧🚚 (Automática en comprobantes)",
                                    fontSize = 11.sp,
                                    color = TextoSecundario,
                                    maxLines = 1,
                                    softWrap = false
                                )
                            }
                            ClayBadge(
                                text = "Activo",
                                containerColor = VerdeEfectivoFondo,
                                contentColor = VerdeEfectivo
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // ---------------------------------------------------------
                // 4. SEGURIDAD Y PIN
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
                                    imageVector = Icons.Default.Security,
                                    contentDescription = null,
                                    tint = AguaPrimaryDark,
                                    modifier = Modifier.size(20.dp)
                                )
                                Spacer(modifier = Modifier.width(10.dp))
                                Column {
                                    Text(
                                        text = "PIN de Seguridad",
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 13.sp,
                                        color = TextoOscuro,
                                        maxLines = 1,
                                        softWrap = false
                                    )
                                    Text(
                                        text = "Protege edición y eliminación de ventas",
                                        fontSize = 11.sp,
                                        color = TextoSecundario,
                                        maxLines = 1,
                                        softWrap = false
                                    )
                                }
                            }

                            Button(
                                onClick = { mostrarCambiarPin = !mostrarCambiarPin },
                                shape = RoundedCornerShape(10.dp),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = if (mostrarCambiarPin) Color(0xFFCBD5E1) else AguaPrimary
                                ),
                                contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                                modifier = Modifier.height(34.dp)
                            ) {
                                Text(
                                    text = if (mostrarCambiarPin) "Cancelar" else "Cambiar",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = if (mostrarCambiarPin) TextoOscuro else Color.White,
                                    maxLines = 1,
                                    softWrap = false
                                )
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
                                    .height(42.dp),
                                shape = RoundedCornerShape(12.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = AguaPrimary)
                            ) {
                                Text("Guardar Nuevo PIN", fontWeight = FontWeight.Bold, maxLines = 1, softWrap = false)
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // ---------------------------------------------------------
                // 5. ALMACENAMIENTO Y COPIAS DE SEGURIDAD
                // ---------------------------------------------------------
                Surface(
                    color = FondoGrisClaro,
                    shape = RoundedCornerShape(16.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Text(
                            text = "ALMACENAMIENTO Y COPIAS",
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Black,
                            color = AguaPrimaryDark,
                            maxLines = 1
                        )
                        Spacer(modifier = Modifier.height(10.dp))

                        // Estado de respaldo
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.CloudDone,
                                contentDescription = null,
                                tint = VerdeEfectivo,
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(10.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = "Copia de Seguridad Automática",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 13.sp,
                                    color = TextoOscuro,
                                    maxLines = 1,
                                    softWrap = false
                                )
                                Text(
                                    text = "Base de datos Room local + Sincronización en la nube",
                                    fontSize = 11.sp,
                                    color = TextoSecundario,
                                    maxLines = 1,
                                    softWrap = false
                                )
                            }
                            ClayBadge(
                                text = "Al día ✓",
                                containerColor = VerdeEfectivoFondo,
                                contentColor = VerdeEfectivo
                            )
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        // Limpiar caché
                        Button(
                            onClick = {
                                var liberado = 0L
                                runCatching {
                                    context.cacheDir.listFiles()?.forEach { file ->
                                        liberado += file.length()
                                        file.deleteRecursively()
                                    }
                                    context.externalCacheDir?.listFiles()?.forEach { file ->
                                        liberado += file.length()
                                        file.deleteRecursively()
                                    }
                                }
                                val kb = (liberado / 1024).coerceAtLeast(0)
                                esErrorEstado = false
                                mensajeEstado = "Memoria caché optimizada ($kb KB liberados)"
                                Toast.makeText(context, "Caché de la app liberada", Toast.LENGTH_SHORT).show()
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(38.dp),
                            shape = RoundedCornerShape(10.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = Color.White),
                            border = BorderStroke(1.dp, Color(0xFFCBD5E1))
                        ) {
                            Icon(
                                imageVector = Icons.Default.Cached,
                                contentDescription = null,
                                tint = TextoSecundario,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "Liberar espacio de caché temporal",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = TextoOscuro,
                                maxLines = 1,
                                softWrap = false
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // ---------------------------------------------------------
                // 6. ACTUALIZACIONES DEL SISTEMA (GITHUB RELEASES)
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
                            Text(
                                text = "ACTUALIZACIONES DEL SISTEMA",
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Black,
                                color = AguaPrimaryDark,
                                maxLines = 1
                            )
                            ClayBadge(
                                text = "v$versionActual",
                                containerColor = AzulCorporativo.copy(alpha = 0.12f),
                                contentColor = AzulCorporativo
                            )
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        Button(
                            onClick = {
                                buscandoOta = true
                                coroutineScope.launch {
                                    val result = OTAUpdateChecker.verificarNuevaVersion(context)
                                    buscandoOta = false
                                    result.onSuccess { info ->
                                        infoActualizacion = info
                                        if (info.hayNuevaVersion) {
                                            esErrorEstado = false
                                            mensajeEstado = "¡Nueva versión v${info.versionNameRemoto} lista!"
                                        } else {
                                            esErrorEstado = false
                                            mensajeEstado = "Atenea está actualizada a la última versión (v$versionActual)"
                                        }
                                    }.onFailure { err ->
                                        esErrorEstado = true
                                        mensajeEstado = "No se pudo verificar: ${err.localizedMessage}"
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
                                Text("Comprobando en GitHub...", maxLines = 1, softWrap = false, color = Color.White)
                            } else {
                                Icon(Icons.Default.SystemUpdate, contentDescription = null, tint = Color.White, modifier = Modifier.size(18.dp))
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = "Comprobar Actualizaciones",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 13.sp,
                                    maxLines = 1,
                                    softWrap = false,
                                    color = Color.White
                                )
                            }
                        }

                        infoActualizacion?.let { info ->
                            if (info.hayNuevaVersion && info.urlDescargaApk.isNotBlank()) {
                                Spacer(modifier = Modifier.height(10.dp))
                                Surface(
                                    color = AmbarFondo,
                                    shape = RoundedCornerShape(12.dp),
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Column(modifier = Modifier.padding(12.dp)) {
                                        Text(
                                            text = "Versión ${info.versionNameRemoto} lista para instalar",
                                            fontWeight = FontWeight.Black,
                                            color = AmbarAdvertencia,
                                            fontSize = 13.sp,
                                            maxLines = 1,
                                            softWrap = false
                                        )
                                        if (info.notasVersion.isNotBlank()) {
                                            Spacer(modifier = Modifier.height(4.dp))
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
                                                mensajeEstado = "Descargando actualización en notificaciones..."
                                            },
                                            modifier = Modifier.fillMaxWidth(),
                                            colors = ButtonDefaults.buttonColors(containerColor = VerdeEfectivo),
                                            shape = RoundedCornerShape(10.dp)
                                        ) {
                                            Text(
                                                text = "Descargar e Instalar Ahora",
                                                fontWeight = FontWeight.Black,
                                                fontSize = 13.sp,
                                                maxLines = 1,
                                                softWrap = false,
                                                color = Color.White
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Pie de autoría corporativo
                Text(
                    text = "Aqua Apolo • Distribución y Control de Rutas",
                    style = MaterialTheme.typography.bodySmall,
                    color = TextoSecundario,
                    textAlign = TextAlign.Center,
                    fontWeight = FontWeight.Medium,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }
    }
}

package com.rutaagua.camion.ui.auth

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Login
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.ErrorOutline
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material.icons.filled.WaterDrop
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusDirection
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.google.firebase.FirebaseNetworkException
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException
import com.google.firebase.auth.FirebaseAuthInvalidUserException
import com.rutaagua.camion.data.local.model.TipoVehiculo
import com.rutaagua.camion.data.local.prefs.RolUsuario
import com.rutaagua.camion.data.local.prefs.resolverPerfilPorEmail
import com.rutaagua.camion.ui.components.ClayBadge
import com.rutaagua.camion.ui.components.ClayButton
import com.rutaagua.camion.ui.components.ClayCard
import com.rutaagua.camion.ui.components.bounceClick
import com.rutaagua.camion.ui.components.marineOutlinedTextFieldColors
import com.rutaagua.camion.ui.theme.AzulCorporativo
import com.rutaagua.camion.ui.theme.BordeSuave
import com.rutaagua.camion.ui.theme.BordeSutil
import com.rutaagua.camion.ui.theme.FondoGrisClaro
import com.rutaagua.camion.ui.theme.PichinchaCanvas
import com.rutaagua.camion.ui.theme.PichinchaNavy
import com.rutaagua.camion.ui.theme.PichinchaYellow
import com.rutaagua.camion.ui.theme.RojoAlerta
import com.rutaagua.camion.ui.theme.RojoAlertaFondo
import com.rutaagua.camion.ui.theme.TextoOscuro
import com.rutaagua.camion.ui.theme.TextoSecundario

/**
 * Pantalla formal de Inicio de Sesión con Firebase Authentication (Email/Password).
 * Diseñada siguiendo la estética Duolingo y Banking UI:
 * - Botones redondeados táctiles, microinteracciones de click y contraste visual de alta legibilidad.
 * - Chips de acceso directo para [🚚 Camión], [🛻 Camioneta] y [👑 Rolando].
 * - Asignación dinámica de roles y vehículo según la cuenta autenticada.
 */
@Composable
fun LoginScreen(
    onLoginExitoso: (TipoVehiculo, RolUsuario) -> Unit,
    modifier: Modifier = Modifier
) {
    val focusManager = LocalFocusManager.current
    val auth = remember { FirebaseAuth.getInstance() }

    var email by rememberSaveable { mutableStateOf("") }
    var password by rememberSaveable { mutableStateOf("") }
    var passwordVisible by rememberSaveable { mutableStateOf(false) }
    var cargando by rememberSaveable { mutableStateOf(false) }
    var errorMensaje by rememberSaveable { mutableStateOf<String?>(null) }

    fun ejecutarLogin() {
        val emailLimpio = email.trim()
        val passwordLimpio = password.trim()

        if (emailLimpio.isBlank()) {
            errorMensaje = "Por favor, ingresa tu correo electrónico."
            return
        }
        if (passwordLimpio.isBlank()) {
            errorMensaje = "Por favor, ingresa tu contraseña."
            return
        }

        cargando = true
        errorMensaje = null
        focusManager.clearFocus()

        auth.signInWithEmailAndPassword(emailLimpio, passwordLimpio)
            .addOnSuccessListener { result ->
                cargando = false
                val correoAutenticado = result.user?.email ?: emailLimpio
                val (vehiculo, rol) = resolverPerfilPorEmail(correoAutenticado)
                onLoginExitoso(vehiculo, rol)
            }
            .addOnFailureListener { exception ->
                cargando = false
                errorMensaje = when (exception) {
                    is FirebaseAuthInvalidCredentialsException -> {
                        "Contraseña o correo incorrectos. Verifica tus credenciales."
                    }
                    is FirebaseAuthInvalidUserException -> {
                        "No se encontró ninguna cuenta registrada con este correo."
                    }
                    is FirebaseNetworkException -> {
                        "Sin conexión a internet. Revisa tu conexión de datos móviles o WiFi."
                    }
                    else -> {
                        val msg = exception.localizedMessage.orEmpty()
                        if (msg.contains("network", ignoreCase = true)) {
                            "Fallo de conexión. Intenta nuevamente en unos segundos."
                        } else if (msg.contains("password", ignoreCase = true) || msg.contains("credential", ignoreCase = true)) {
                            "Contraseña incorrecta. Inténtalo de nuevo."
                        } else {
                            "Error al iniciar sesión: ${exception.message ?: "Intenta nuevamente"}"
                        }
                    }
                }
            }
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(PichinchaCanvas)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
                .navigationBarsPadding()
                .imePadding()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp, vertical = 24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            // =========================================================================
            // 1. ENCABEZADO ESTILO DUOLINGO / BANKING UI
            // =========================================================================
            Box(
                modifier = Modifier
                    .size(92.dp)
                    .clip(RoundedCornerShape(26.dp))
                    .background(
                        Brush.linearGradient(
                            colors = listOf(Color(0xFF0284C7), PichinchaNavy)
                        )
                    ),
                contentAlignment = Alignment.Center
            ) {
                Box(
                    modifier = Modifier
                        .size(54.dp)
                        .background(Color.White.copy(alpha = 0.2f), CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.WaterDrop,
                        contentDescription = "Logo Atenea",
                        tint = PichinchaYellow,
                        modifier = Modifier.size(34.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            Text(
                text = "Aqua Apolo • Atenea",
                fontWeight = FontWeight.Black,
                fontSize = 24.sp,
                color = PichinchaNavy,
                textAlign = TextAlign.Center
            )

            Text(
                text = "Distribución de Agua Purificada",
                fontSize = 13.sp,
                color = TextoSecundario,
                fontWeight = FontWeight.SemiBold,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(6.dp))

            ClayBadge(
                text = "INICIO DE SESIÓN EN RUTA",
                backgroundColor = Color(0xFFE0F2FE),
                textColor = AzulCorporativo
            )

            Spacer(modifier = Modifier.height(24.dp))

            // =========================================================================
            // 2. ACCESOS DIRECTOS POR PERFIL (CHIPS RÁPIDOS)
            // =========================================================================
            ClayCard(
                modifier = Modifier.fillMaxWidth(),
                cornerRadius = 18.dp,
                elevation = 2.dp,
                contentPadding = PaddingValues(14.dp)
            ) {
                Column(modifier = Modifier.fillMaxWidth()) {
                    Text(
                        text = "Seleccionar cuenta de acceso rápido:",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = TextoSecundario,
                        modifier = Modifier.padding(bottom = 8.dp)
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        ChipAccesoRapido(
                            etiqueta = "🚚 Camión",
                            emailDestino = "camion@rutaatenea.app",
                            emailActual = email,
                            colorAcento = AzulCorporativo,
                            onClick = {
                                email = "camion@rutaatenea.app"
                                errorMensaje = null
                                focusManager.moveFocus(FocusDirection.Down)
                            },
                            modifier = Modifier.weight(1f)
                        )

                        ChipAccesoRapido(
                            etiqueta = "🛻 Camioneta",
                            emailDestino = "camioneta@rutaatenea.app",
                            emailActual = email,
                            colorAcento = Color(0xFF0D9488),
                            onClick = {
                                email = "camioneta@rutaatenea.app"
                                errorMensaje = null
                                focusManager.moveFocus(FocusDirection.Down)
                            },
                            modifier = Modifier.weight(1f)
                        )

                        ChipAccesoRapido(
                            etiqueta = "👑 Rolando",
                            emailDestino = "rolando@rutaatenea.app",
                            emailActual = email,
                            colorAcento = Color(0xFFB45309),
                            onClick = {
                                email = "rolando@rutaatenea.app"
                                errorMensaje = null
                                focusManager.moveFocus(FocusDirection.Down)
                            },
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(18.dp))

            // =========================================================================
            // 3. FORMULARIO DE ENTRADA (EMAIL Y PASSWORD)
            // =========================================================================
            ClayCard(
                modifier = Modifier.fillMaxWidth(),
                cornerRadius = 20.dp,
                elevation = 3.dp,
                contentPadding = PaddingValues(18.dp)
            ) {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    // Campo de Correo Electrónico
                    Column {
                        Text(
                            text = "Correo Electrónico",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = TextoOscuro,
                            modifier = Modifier.padding(bottom = 6.dp)
                        )

                        OutlinedTextField(
                            value = email,
                            onValueChange = {
                                email = it
                                if (errorMensaje != null) errorMensaje = null
                            },
                            modifier = Modifier.fillMaxWidth(),
                            placeholder = { Text("ej. camion@rutaatenea.app", fontSize = 13.sp) },
                            leadingIcon = {
                                Icon(
                                    imageVector = Icons.Default.Email,
                                    contentDescription = "Correo",
                                    tint = if (email.isNotBlank()) PichinchaNavy else TextoSecundario
                                )
                            },
                            trailingIcon = {
                                if (email.isNotBlank()) {
                                    IconButton(onClick = { email = "" }) {
                                        Icon(
                                            imageVector = Icons.Default.Clear,
                                            contentDescription = "Limpiar correo",
                                            tint = TextoSecundario,
                                            modifier = Modifier.size(18.dp)
                                        )
                                    }
                                }
                            },
                            shape = RoundedCornerShape(14.dp),
                            singleLine = true,
                            keyboardOptions = KeyboardOptions(
                                keyboardType = KeyboardType.Email,
                                imeAction = ImeAction.Next
                            ),
                            keyboardActions = KeyboardActions(
                                onNext = { focusManager.moveFocus(FocusDirection.Down) }
                            ),
                            colors = marineOutlinedTextFieldColors(accentColor = PichinchaNavy)
                        )
                    }

                    // Campo de Contraseña
                    Column {
                        Text(
                            text = "Contraseña",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = TextoOscuro,
                            modifier = Modifier.padding(bottom = 6.dp)
                        )

                        OutlinedTextField(
                            value = password,
                            onValueChange = {
                                password = it
                                if (errorMensaje != null) errorMensaje = null
                            },
                            modifier = Modifier.fillMaxWidth(),
                            placeholder = { Text("Ingresa tu contraseña", fontSize = 13.sp) },
                            leadingIcon = {
                                Icon(
                                    imageVector = Icons.Default.Lock,
                                    contentDescription = "Contraseña",
                                    tint = if (password.isNotBlank()) PichinchaNavy else TextoSecundario
                                )
                            },
                            trailingIcon = {
                                IconButton(onClick = { passwordVisible = !passwordVisible }) {
                                    Icon(
                                        imageVector = if (passwordVisible) Icons.Default.Visibility else Icons.Default.VisibilityOff,
                                        contentDescription = if (passwordVisible) "Ocultar contraseña" else "Ver contraseña",
                                        tint = if (passwordVisible) PichinchaNavy else TextoSecundario,
                                        modifier = Modifier.size(20.dp)
                                    )
                                }
                            },
                            visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                            shape = RoundedCornerShape(14.dp),
                            singleLine = true,
                            keyboardOptions = KeyboardOptions(
                                keyboardType = KeyboardType.Password,
                                imeAction = ImeAction.Done
                            ),
                            keyboardActions = KeyboardActions(
                                onDone = {
                                    focusManager.clearFocus()
                                    ejecutarLogin()
                                }
                            ),
                            colors = marineOutlinedTextFieldColors(accentColor = PichinchaNavy)
                        )
                    }

                    // Mensaje de Error en Español
                    AnimatedVisibility(
                        visible = errorMensaje != null,
                        enter = fadeIn(),
                        exit = fadeOut()
                    ) {
                        Surface(
                            color = RojoAlertaFondo,
                            shape = RoundedCornerShape(12.dp),
                            border = BorderStroke(1.dp, RojoAlerta.copy(alpha = 0.35f)),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                modifier = Modifier.padding(12.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = Icons.Default.ErrorOutline,
                                    contentDescription = "Error",
                                    tint = RojoAlerta,
                                    modifier = Modifier.size(20.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = errorMensaje.orEmpty(),
                                    color = RojoAlerta,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    lineHeight = 16.sp
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(4.dp))

                    // Botón Principal de Acción (Estilo Táctil Duolingo)
                    ClayButton(
                        onClick = { ejecutarLogin() },
                        modifier = Modifier.fillMaxWidth(),
                        enabled = !cargando,
                        containerColor = if (cargando) PichinchaNavy.copy(alpha = 0.6f) else PichinchaNavy,
                        contentColor = Color.White,
                        height = 54.dp,
                        cornerRadius = 16.dp
                    ) {
                        if (cargando) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(22.dp),
                                color = Color.White,
                                strokeWidth = 2.5.dp
                            )
                            Spacer(modifier = Modifier.width(10.dp))
                            Text(
                                text = "Autenticando...",
                                fontSize = 15.sp,
                                fontWeight = FontWeight.Black
                            )
                        } else {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.Login,
                                contentDescription = null,
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(10.dp))
                            Text(
                                text = "Iniciar Sesión",
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Black
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // =========================================================================
            // 4. PIE DE PÁGINA
            // =========================================================================
            Text(
                text = "Sesión persistente offline • Cloud Firestore Sincronizado",
                fontSize = 11.sp,
                color = TextoSecundario,
                fontWeight = FontWeight.Medium,
                textAlign = TextAlign.Center
            )
        }
    }
}

/**
 * Chip táctil redondeado para autocompletar rápidamente el correo de cada chofer o administrador.
 */
@Composable
private fun ChipAccesoRapido(
    etiqueta: String,
    emailDestino: String,
    emailActual: String,
    colorAcento: Color,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val seleccionado = emailActual.trim().equals(emailDestino, ignoreCase = true)

    Surface(
        color = if (seleccionado) colorAcento.copy(alpha = 0.15f) else FondoGrisClaro,
        shape = RoundedCornerShape(12.dp),
        border = BorderStroke(
            width = if (seleccionado) 1.5.dp else 1.dp,
            color = if (seleccionado) colorAcento else BordeSuave
        ),
        modifier = modifier
            .bounceClick()
            .clickable { onClick() }
    ) {
        Box(
            modifier = Modifier.padding(horizontal = 6.dp, vertical = 10.dp),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = etiqueta,
                color = if (seleccionado) colorAcento else TextoOscuro,
                fontWeight = if (seleccionado) FontWeight.Black else FontWeight.Bold,
                fontSize = 11.sp,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

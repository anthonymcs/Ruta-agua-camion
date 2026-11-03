package com.rutaagua.camion.ui.auth

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Login
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.google.firebase.FirebaseNetworkException
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException
import com.google.firebase.auth.FirebaseAuthInvalidUserException
import com.rutaagua.camion.R
import com.rutaagua.camion.data.local.model.TipoVehiculo
import com.rutaagua.camion.data.local.prefs.AppPreferences
import com.rutaagua.camion.data.local.prefs.RolUsuario
import com.rutaagua.camion.data.local.prefs.resolverPerfilPorEmail
import com.rutaagua.camion.ui.components.ClayButton
import com.rutaagua.camion.ui.components.ClayCard
import com.rutaagua.camion.ui.components.VehiculoThumbnail
import com.rutaagua.camion.ui.components.bounceClick
import com.rutaagua.camion.ui.components.marineOutlinedTextFieldColors
import com.rutaagua.camion.ui.theme.MarineOcean
import com.rutaagua.camion.ui.theme.MarineOceanDark
import com.rutaagua.camion.ui.theme.MarineOceanLight
import com.rutaagua.camion.ui.theme.PichinchaCanvas
import com.rutaagua.camion.ui.theme.RojoAlerta
import com.rutaagua.camion.ui.theme.RojoAlertaFondo
import com.rutaagua.camion.ui.theme.TextoOscuro
import com.rutaagua.camion.ui.theme.TextoSecundario

/**
 * Pantalla de Inicio de Sesión con el diseño original marino y equilibrado:
 * - Cabecera azul marina curvada con degradado y logo oficial.
 * - Tarjeta de "Ingreso Rápido" visible exclusivamente cuando el usuario ya ha ingresado con credenciales válidas en este celular.
 * - Validación mediante Firebase Authentication (Email/Password) con asignación dinámica de rol y vehículo.
 */
@Composable
fun LoginScreen(
    onLoginExitoso: (TipoVehiculo, RolUsuario) -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val focusManager = LocalFocusManager.current
    val appPreferences = remember { AppPreferences.getInstance(context) }
    val auth = remember { FirebaseAuth.getInstance() }

    var cuentaGuardada by remember { mutableStateOf(appPreferences.tieneCuentaGuardadaEnDispositivo()) }
    val vehiculoGuardado = remember(cuentaGuardada) { appPreferences.obtenerVehiculoActivo() }
    val rolGuardado = remember(cuentaGuardada) { appPreferences.obtenerRolActivo() }

    var usuario by rememberSaveable { mutableStateOf(appPreferences.obtenerUltimoUsuarioGuardado()) }
    var clave by rememberSaveable { mutableStateOf("") }
    var recordarSesion by rememberSaveable { mutableStateOf(appPreferences.obtenerRecordarSesion()) }
    var mostrarClave by rememberSaveable { mutableStateOf(false) }
    var cargando by rememberSaveable { mutableStateOf(false) }
    var mensajeError by rememberSaveable { mutableStateOf<String?>(null) }

    fun intentarLogin() {
        val userLimpio = usuario.trim()
        val claveLimpia = clave.trim()

        if (userLimpio.isBlank()) {
            mensajeError = "Por favor, ingresa tu usuario o correo."
            return
        }
        if (claveLimpia.isBlank()) {
            mensajeError = "Por favor, ingresa tu contraseña."
            return
        }

        focusManager.clearFocus()
        cargando = true
        mensajeError = null

        // Si se ingresa solo el identificador rápido, autocompletar el dominio oficial
        val emailFinal = if (!userLimpio.contains("@")) {
            when (userLimpio.lowercase()) {
                "camion" -> "camion@rutaatenea.app"
                "camioneta" -> "camioneta@rutaatenea.app"
                "rolando", "admin" -> "rolando@rutaatenea.app"
                else -> "$userLimpio@rutaatenea.app"
            }
        } else {
            userLimpio.lowercase()
        }

        auth.signInWithEmailAndPassword(emailFinal, claveLimpia)
            .addOnSuccessListener { result ->
                cargando = false
                val correoAutenticado = result.user?.email ?: emailFinal
                val (vehiculo, rol) = resolverPerfilPorEmail(correoAutenticado)

                if (recordarSesion) {
                    appPreferences.registrarAccesoExitoso(
                        usuario = userLimpio,
                        vehiculo = vehiculo,
                        rol = rol,
                        recordar = true
                    )
                } else {
                    appPreferences.guardarVehiculoActivo(vehiculo)
                    appPreferences.guardarRolActivo(rol)
                    appPreferences.guardarSesionIniciada(true)
                }

                onLoginExitoso(vehiculo, rol)
            }
            .addOnFailureListener { exception ->
                cargando = false
                mensajeError = when (exception) {
                    is FirebaseAuthInvalidCredentialsException -> {
                        "Usuario o contraseña incorrectos."
                    }
                    is FirebaseAuthInvalidUserException -> {
                        "No se encontró una cuenta registrada con este usuario."
                    }
                    is FirebaseNetworkException -> {
                        "Sin conexión a internet. Revisa tus datos móviles o WiFi."
                    }
                    else -> {
                        val msg = exception.localizedMessage.orEmpty()
                        if (msg.contains("network", ignoreCase = true)) {
                            "Fallo de conexión. Intenta nuevamente."
                        } else if (msg.contains("password", ignoreCase = true) || msg.contains("credential", ignoreCase = true)) {
                            "Contraseña incorrecta."
                        } else {
                            "Error de autenticación: ${exception.message ?: "Intenta nuevamente"}"
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
        // Cabecera Marina Suave con degradado original
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(270.dp)
                .clip(RoundedCornerShape(bottomStart = 34.dp, bottomEnd = 34.dp))
                .background(
                    Brush.verticalGradient(
                        colors = listOf(MarineOceanDark, MarineOcean, MarineOceanLight)
                    )
                )
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .statusBarsPadding()
                    .padding(top = 24.dp, start = 24.dp, end = 24.dp),
                horizontalAlignment = Alignment.Start
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Surface(
                        color = Color.White.copy(alpha = 0.15f),
                        shape = RoundedCornerShape(16.dp),
                        border = BorderStroke(1.dp, Color.White.copy(alpha = 0.3f)),
                        modifier = Modifier.size(56.dp)
                    ) {
                        Image(
                            painter = painterResource(id = R.drawable.ic_logo_aqua_apolo),
                            contentDescription = "Logo Atenea",
                            contentScale = ContentScale.Crop,
                            modifier = Modifier
                                .fillMaxSize()
                                .clip(RoundedCornerShape(16.dp))
                        )
                    }
                    Spacer(modifier = Modifier.width(14.dp))
                    Column {
                        Text(
                            text = "Atenea",
                            color = Color.White,
                            fontSize = 23.sp,
                            fontWeight = FontWeight.Black,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        Text(
                            text = "Gestión de Ruta y Unidades",
                            color = Color.White.copy(alpha = 0.85f),
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Medium,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }

                Spacer(modifier = Modifier.height(22.dp))

                Text(
                    text = "Bienvenido de nuevo",
                    color = Color.White,
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Black,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = "Accede a tu unidad asignada",
                    color = Color.White.copy(alpha = 0.85f),
                    fontSize = 14.sp,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }

        // Contenido desplazable (adaptable en vertical y horizontal)
        Column(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
                .navigationBarsPadding()
                .imePadding()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(175.dp))

            // 1. Tarjeta de INGRESO RÁPIDO (Únicamente si ya inició sesión exitosamente antes en este celular)
            AnimatedVisibility(visible = cuentaGuardada) {
                val nombrePerfilGuardado = when {
                    rolGuardado == RolUsuario.ADMIN_ROLANDO -> "Rolando (Administrador)"
                    vehiculoGuardado == TipoVehiculo.CAMIONETA -> "Camioneta de Ruta"
                    else -> "Camión Principal"
                }

                ClayCard(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 14.dp),
                    containerColor = Color.White,
                    accentBorderColor = MarineOceanLight,
                    cornerRadius = 22.dp,
                    elevation = 8.dp,
                    contentPadding = PaddingValues(16.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.Bolt,
                                contentDescription = null,
                                tint = MarineOceanLight,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "Ingreso rápido guardado",
                                color = MarineOcean,
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Black,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                        TextButton(
                            onClick = {
                                appPreferences.olvidarCuentaGuardada()
                                cuentaGuardada = false
                            },
                            contentPadding = PaddingValues(horizontal = 8.dp, vertical = 0.dp),
                            modifier = Modifier.height(28.dp)
                        ) {
                            Text(
                                text = "Olvidar",
                                color = TextoSecundario,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    Surface(
                        onClick = {
                            if (auth.currentUser != null) {
                                val (vehiculo, rol) = resolverPerfilPorEmail(auth.currentUser?.email)
                                appPreferences.guardarSesionIniciada(true)
                                onLoginExitoso(vehiculo, rol)
                            } else {
                                // Pre-llenar usuario guardado para solicitar clave
                                usuario = appPreferences.obtenerUltimoUsuarioGuardado()
                                mensajeError = "Ingresa tu contraseña para acceder a esta cuenta."
                            }
                        },
                        color = Color(0xFFEBF6FC),
                        shape = RoundedCornerShape(16.dp),
                        border = BorderStroke(1.dp, Color(0xFFB8DFF2)),
                        modifier = Modifier
                            .fillMaxWidth()
                            .bounceClick(0.97f)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 14.dp, vertical = 12.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.weight(1f)
                            ) {
                                VehiculoThumbnail(
                                    tipo = vehiculoGuardado,
                                    size = 42.dp
                                )
                                Spacer(modifier = Modifier.width(12.dp))
                                Column {
                                    Text(
                                        text = nombrePerfilGuardado,
                                        color = TextoOscuro,
                                        fontWeight = FontWeight.Black,
                                        fontSize = 15.sp,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                    Text(
                                        text = "Toca para entrar a tu unidad",
                                        color = MarineOceanLight,
                                        fontWeight = FontWeight.SemiBold,
                                        fontSize = 12.sp,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                }
                            }

                            Surface(
                                color = MarineOcean,
                                shape = RoundedCornerShape(12.dp)
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = "Entrar",
                                        color = Color.White,
                                        fontWeight = FontWeight.Black,
                                        fontSize = 12.sp,
                                        maxLines = 1
                                    )
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Icon(
                                        imageVector = Icons.AutoMirrored.Filled.Login,
                                        contentDescription = null,
                                        tint = Color.White,
                                        modifier = Modifier.size(15.dp)
                                    )
                                }
                            }
                        }
                    }
                }
            }

            // 2. Tarjeta Principal de Inicio de Sesión con Credenciales
            ClayCard(
                modifier = Modifier.fillMaxWidth(),
                containerColor = Color.White,
                cornerRadius = 24.dp,
                elevation = 8.dp,
                contentPadding = PaddingValues(20.dp)
            ) {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    Text(
                        text = if (cuentaGuardada) "O ingresa con otra cuenta" else "Iniciar sesión",
                        color = TextoOscuro,
                        fontSize = 17.sp,
                        fontWeight = FontWeight.Black,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )

                    OutlinedTextField(
                        value = usuario,
                        onValueChange = {
                            usuario = it
                            mensajeError = null
                        },
                        label = {
                            Text(
                                text = "Usuario o Correo",
                                fontWeight = FontWeight.SemiBold,
                                maxLines = 1
                            )
                        },
                        placeholder = {
                            Text(
                                text = "Ej: camion o camion@rutaatenea.app",
                                color = TextoSecundario.copy(alpha = 0.6f),
                                maxLines = 1
                            )
                        },
                        leadingIcon = {
                            Icon(
                                imageVector = Icons.Default.Person,
                                contentDescription = null,
                                tint = MarineOcean
                            )
                        },
                        textStyle = TextStyle(
                            color = TextoOscuro,
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold
                        ),
                        singleLine = true,
                        shape = RoundedCornerShape(14.dp),
                        keyboardOptions = KeyboardOptions(
                            keyboardType = KeyboardType.Email,
                            imeAction = ImeAction.Next
                        ),
                        colors = marineOutlinedTextFieldColors(accentColor = MarineOcean),
                        modifier = Modifier.fillMaxWidth()
                    )

                    OutlinedTextField(
                        value = clave,
                        onValueChange = {
                            clave = it
                            mensajeError = null
                        },
                        label = {
                            Text(
                                text = "Contraseña",
                                fontWeight = FontWeight.SemiBold,
                                maxLines = 1
                            )
                        },
                        placeholder = {
                            Text(
                                text = "Ingresa tu clave",
                                color = TextoSecundario.copy(alpha = 0.6f),
                                maxLines = 1
                            )
                        },
                        leadingIcon = {
                            Icon(
                                imageVector = Icons.Default.Lock,
                                contentDescription = null,
                                tint = MarineOcean
                            )
                        },
                        trailingIcon = {
                            IconButton(onClick = { mostrarClave = !mostrarClave }) {
                                Icon(
                                    imageVector = if (mostrarClave) Icons.Default.VisibilityOff else Icons.Default.Visibility,
                                    contentDescription = "Mostrar contraseña",
                                    tint = TextoSecundario
                                )
                            }
                        },
                        textStyle = TextStyle(
                            color = TextoOscuro,
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold
                        ),
                        visualTransformation = if (mostrarClave) VisualTransformation.None else PasswordVisualTransformation(),
                        singleLine = true,
                        shape = RoundedCornerShape(14.dp),
                        keyboardOptions = KeyboardOptions(
                            keyboardType = KeyboardType.Password,
                            imeAction = ImeAction.Done
                        ),
                        keyboardActions = KeyboardActions(
                            onDone = { intentarLogin() }
                        ),
                        colors = marineOutlinedTextFieldColors(accentColor = MarineOcean),
                        modifier = Modifier.fillMaxWidth()
                    )

                    // Opción de Recordar sesión en este dispositivo
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(10.dp))
                            .clickable { recordarSesion = !recordarSesion }
                            .padding(vertical = 2.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Checkbox(
                            checked = recordarSesion,
                            onCheckedChange = { recordarSesion = it },
                            colors = CheckboxDefaults.colors(
                                checkedColor = MarineOcean,
                                uncheckedColor = TextoSecundario,
                                checkmarkColor = Color.White
                            )
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "Recordar mi sesión en este celular",
                            color = TextoOscuro,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.SemiBold,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }

                    AnimatedVisibility(visible = mensajeError != null) {
                        Surface(
                            color = RojoAlertaFondo,
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(
                                text = mensajeError ?: "",
                                color = RojoAlerta,
                                fontWeight = FontWeight.Bold,
                                fontSize = 13.sp,
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 10.dp)
                            )
                        }
                    }

                    // Botón Principal Azul Marino Oceánico con Texto Blanco Nítido
                    ClayButton(
                        onClick = { intentarLogin() },
                        enabled = !cargando,
                        modifier = Modifier.fillMaxWidth(),
                        containerColor = MarineOcean,
                        contentColor = Color.White,
                        cornerRadius = 14.dp,
                        height = 52.dp
                    ) {
                        if (cargando) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(20.dp),
                                color = Color.White,
                                strokeWidth = 2.5.dp
                            )
                            Spacer(modifier = Modifier.width(10.dp))
                            Text(
                                text = "Verificando...",
                                fontSize = 15.sp,
                                fontWeight = FontWeight.Black,
                                color = Color.White
                            )
                        } else {
                            Text(
                                text = "Ingresar",
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Black,
                                color = Color.White,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(32.dp))
        }
    }
}

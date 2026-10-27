package com.rutaagua.camion.ui.screens

import androidx.compose.animation.AnimatedVisibility
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
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.Key
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
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
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.rutaagua.camion.data.local.model.TipoVehiculo
import com.rutaagua.camion.data.local.prefs.AppPreferences
import com.rutaagua.camion.data.local.prefs.RolUsuario
import com.rutaagua.camion.data.repository.RutaAguaRepository
import com.rutaagua.camion.ui.components.CamionIllustration
import com.rutaagua.camion.ui.components.CamionetaIllustration
import com.rutaagua.camion.ui.components.ClayBadge
import com.rutaagua.camion.ui.components.ClayButton
import com.rutaagua.camion.ui.components.ClayCard
import com.rutaagua.camion.ui.components.ClayInsetBox
import com.rutaagua.camion.ui.components.obtenerClayThemePorVehiculo
import com.rutaagua.camion.ui.theme.ClayInsetBackground
import com.rutaagua.camion.ui.theme.ClayRimShadow
import com.rutaagua.camion.ui.theme.RojoAlerta
import com.rutaagua.camion.ui.theme.RojoAlertaFondo
import com.rutaagua.camion.ui.theme.SuperficieTarjeta
import com.rutaagua.camion.ui.theme.TextoOscuro
import com.rutaagua.camion.ui.theme.TextoSecundario

/**
 * Pantalla de Inicio de Sesión con estética Claymorphism y credenciales únicas por unidad:
 * - Camión (usuario: camion, clave: 1111)
 * - Camioneta (usuario: camioneta, clave: 2222)
 * - Administración (usuario: rolando, clave: 1234)
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SeleccionVehiculoScreen(
    repository: RutaAguaRepository,
    onSeleccionarPerfil: (TipoVehiculo, RolUsuario) -> Unit,
    onAbrirAjustes: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val appPreferences = remember { AppPreferences.getInstance(context) }

    val inventarioCamion by repository.obtenerInventarioCamion("CAMION").collectAsState(initial = null)
    val inventarioCamioneta by repository.obtenerInventarioCamion("CAMIONETA").collectAsState(initial = null)

    var usuarioInput by remember { mutableStateOf("camion") }
    var claveInput by remember { mutableStateOf("") }
    var mostrarClave by remember { mutableStateOf(false) }
    var mensajeError by remember { mutableStateOf<String?>(null) }

    val usuarioNormalizado = usuarioInput.trim().lowercase().replace("ó", "o")
    val esVistaCamioneta = usuarioNormalizado == "camioneta" || usuarioNormalizado == "rolando" || usuarioNormalizado == "admin"
    val vehiculoPreview = if (esVistaCamioneta) TipoVehiculo.CAMIONETA else TipoVehiculo.CAMION
    val clayTheme = obtenerClayThemePorVehiculo(vehiculoPreview)

    val invActivo = if (esVistaCamioneta) inventarioCamioneta else inventarioCamion
    val llenosActuales = invActivo?.totalBidonesLlenos() ?: 0
    val topeActual = invActivo?.capacidadTopeBidones ?: clayTheme.topeBidonesDefault
    val vaciosActuales = invActivo?.bidonesVacios ?: 0

    Scaffold(
        modifier = modifier.fillMaxSize(),
        containerColor = clayTheme.canvasColor
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 22.dp, vertical = 16.dp)
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Barra Superior Minimalista
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Surface(
                        color = clayTheme.primary,
                        shape = RoundedCornerShape(12.dp),
                        shadowElevation = 4.dp,
                        modifier = Modifier.size(38.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Text(
                                text = "AA",
                                color = Color.White,
                                fontWeight = FontWeight.Black,
                                fontSize = 15.sp
                            )
                        }
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        Text(
                            text = "Aqua Apolo",
                            fontWeight = FontWeight.Black,
                            fontSize = 20.sp,
                            color = TextoOscuro
                        )
                        Text(
                            text = "Control de Ruta",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = TextoSecundario
                        )
                    }
                }

                Surface(
                    onClick = onAbrirAjustes,
                    shape = RoundedCornerShape(14.dp),
                    color = SuperficieTarjeta,
                    shadowElevation = 3.dp,
                    border = BorderStroke(1.dp, Color.White),
                    modifier = Modifier.size(42.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            imageVector = Icons.Default.Settings,
                            contentDescription = "Ajustes",
                            tint = TextoOscuro,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Tarjeta Hero Clay del Vehículo Seleccionado
            ClayCard(
                modifier = Modifier.fillMaxWidth(),
                cornerRadius = 28.dp,
                elevation = 8.dp,
                contentPadding = PaddingValues(20.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    ClayBadge(
                        text = if (usuarioNormalizado == "rolando" || usuarioNormalizado == "admin") "Administración" else clayTheme.nombreCompleto,
                        containerColor = clayTheme.softContainer,
                        contentColor = clayTheme.primaryDark
                    )

                    Text(
                        text = "Capacidad: $topeActual bid.",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = TextoSecundario
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    ClayInsetBox(
                        modifier = Modifier.size(96.dp),
                        cornerRadius = 22.dp,
                        backgroundColor = clayTheme.softContainer.copy(alpha = 0.45f),
                        contentPadding = PaddingValues(8.dp)
                    ) {
                        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                            if (esVistaCamioneta) {
                                CamionetaIllustration(modifier = Modifier.size(80.dp))
                            } else {
                                CamionIllustration(modifier = Modifier.size(80.dp))
                            }
                        }
                    }

                    Spacer(modifier = Modifier.width(16.dp))

                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = when (usuarioNormalizado) {
                                "rolando", "admin" -> "Rolando • Supervisor"
                                "camioneta" -> "Camioneta de Reparto"
                                else -> "Camión Principal"
                            },
                            fontWeight = FontWeight.Black,
                            fontSize = 20.sp,
                            color = TextoOscuro
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        ClayInsetBox(
                            modifier = Modifier.fillMaxWidth(),
                            cornerRadius = 12.dp,
                            contentPadding = PaddingValues(horizontal = 12.dp, vertical = 8.dp)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(
                                    text = "Llenos: $llenosActuales/$topeActual",
                                    fontWeight = FontWeight.Black,
                                    fontSize = 13.sp,
                                    color = clayTheme.primaryDark
                                )
                                Text(
                                    text = "Vacíos: $vaciosActuales",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 13.sp,
                                    color = TextoSecundario
                                )
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(18.dp))

            // Formulario Clay de Inicio de Sesión
            ClayCard(
                modifier = Modifier.fillMaxWidth(),
                cornerRadius = 26.dp,
                elevation = 8.dp,
                contentPadding = PaddingValues(20.dp)
            ) {
                Text(
                    text = "Iniciar Sesión",
                    fontWeight = FontWeight.Black,
                    fontSize = 19.sp,
                    color = TextoOscuro
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "Selecciona la unidad o ingresa tus credenciales",
                    fontSize = 13.sp,
                    color = TextoSecundario
                )

                Spacer(modifier = Modifier.height(14.dp))

                // Selector Rápido de Unidad (Clay Pills)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    SelectorPerfilClayPill(
                        titulo = "Camión",
                        subtitulo = "218 bid.",
                        seleccionado = usuarioNormalizado == "camion",
                        colorActivo = obtenerClayThemePorVehiculo(TipoVehiculo.CAMION).primary,
                        onClick = {
                            usuarioInput = "camion"
                            mensajeError = null
                        },
                        modifier = Modifier.weight(1f)
                    )
                    SelectorPerfilClayPill(
                        titulo = "Camioneta",
                        subtitulo = "80 bid.",
                        seleccionado = usuarioNormalizado == "camioneta",
                        colorActivo = obtenerClayThemePorVehiculo(TipoVehiculo.CAMIONETA).primary,
                        onClick = {
                            usuarioInput = "camioneta"
                            mensajeError = null
                        },
                        modifier = Modifier.weight(1f)
                    )
                    SelectorPerfilClayPill(
                        titulo = "Rolando",
                        subtitulo = "Admin",
                        seleccionado = usuarioNormalizado == "rolando" || usuarioNormalizado == "admin",
                        colorActivo = Color(0xFF0F172A),
                        onClick = {
                            usuarioInput = "rolando"
                            mensajeError = null
                        },
                        modifier = Modifier.weight(1f)
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Campo Usuario
                OutlinedTextField(
                    value = usuarioInput,
                    onValueChange = {
                        usuarioInput = it
                        mensajeError = null
                    },
                    label = { Text("Usuario de Unidad") },
                    placeholder = { Text("camion / camioneta / rolando") },
                    leadingIcon = {
                        Icon(
                            imageVector = Icons.Default.Person,
                            contentDescription = null,
                            tint = clayTheme.primary
                        )
                    },
                    singleLine = true,
                    shape = RoundedCornerShape(16.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = clayTheme.primary,
                        unfocusedBorderColor = ClayRimShadow,
                        focusedContainerColor = Color.White,
                        unfocusedContainerColor = ClayInsetBackground
                    ),
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(12.dp))

                // Campo Contraseña / Clave
                OutlinedTextField(
                    value = claveInput,
                    onValueChange = {
                        claveInput = it
                        mensajeError = null
                    },
                    label = { Text("Clave de Acceso") },
                    placeholder = {
                        Text(
                            when (usuarioNormalizado) {
                                "camioneta" -> "Clave camioneta (2222)"
                                "rolando", "admin" -> "Clave admin (1234)"
                                else -> "Clave camión (1111)"
                            }
                        )
                    },
                    leadingIcon = {
                        Icon(
                            imageVector = Icons.Default.Lock,
                            contentDescription = null,
                            tint = clayTheme.primary
                        )
                    },
                    trailingIcon = {
                        IconButton(onClick = { mostrarClave = !mostrarClave }) {
                            Icon(
                                imageVector = if (mostrarClave) Icons.Default.VisibilityOff else Icons.Default.Visibility,
                                contentDescription = "Mostrar clave"
                            )
                        }
                    },
                    visualTransformation = if (mostrarClave) VisualTransformation.None else PasswordVisualTransformation(),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                    singleLine = true,
                    shape = RoundedCornerShape(16.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = clayTheme.primary,
                        unfocusedBorderColor = ClayRimShadow,
                        focusedContainerColor = Color.White,
                        unfocusedContainerColor = ClayInsetBackground
                    ),
                    modifier = Modifier.fillMaxWidth()
                )

                AnimatedVisibility(visible = mensajeError != null) {
                    Surface(
                        color = RojoAlertaFondo,
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 10.dp)
                    ) {
                        Text(
                            text = mensajeError ?: "",
                            color = RojoAlerta,
                            fontWeight = FontWeight.Bold,
                            fontSize = 12.sp,
                            modifier = Modifier.padding(10.dp),
                            textAlign = TextAlign.Center
                        )
                    }
                }

                Spacer(modifier = Modifier.height(18.dp))

                ClayButton(
                    onClick = {
                        val resultado = appPreferences.autenticarCredenciales(usuarioInput, claveInput)
                        if (resultado != null) {
                            onSeleccionarPerfil(resultado.first, resultado.second)
                        } else {
                            mensajeError = "Credenciales incorrectas. Verifica el usuario y la clave."
                        }
                    },
                    containerColor = clayTheme.primary,
                    modifier = Modifier.fillMaxWidth(),
                    height = 56.dp
                ) {
                    Text(
                        text = "Ingresar a ${clayTheme.nombreCorto}",
                        fontWeight = FontWeight.Black,
                        fontSize = 16.sp,
                        color = Color.White
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                        contentDescription = null,
                        tint = Color.White,
                        modifier = Modifier.size(20.dp)
                    )
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Guía rápida de credenciales únicas asignadas
                ClayInsetBox(
                    modifier = Modifier.fillMaxWidth(),
                    cornerRadius = 14.dp,
                    contentPadding = PaddingValues(12.dp)
                ) {
                    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.Key,
                                contentDescription = null,
                                tint = clayTheme.primaryDark,
                                modifier = Modifier.size(15.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "Credenciales por vehículo (toca para rellenar):",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = TextoOscuro
                            )
                        }
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = "Camión: camion / ${appPreferences.obtenerClaveCamion()}",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = TextoSecundario,
                                modifier = Modifier.clickable {
                                    usuarioInput = "camion"
                                    claveInput = appPreferences.obtenerClaveCamion()
                                }
                            )
                            Text(
                                text = "Camioneta: camioneta / ${appPreferences.obtenerClaveCamioneta()}",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = TextoSecundario,
                                modifier = Modifier.clickable {
                                    usuarioInput = "camioneta"
                                    claveInput = appPreferences.obtenerClaveCamioneta()
                                }
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun SelectorPerfilClayPill(
    titulo: String,
    subtitulo: String,
    seleccionado: Boolean,
    colorActivo: Color,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        onClick = onClick,
        shape = RoundedCornerShape(16.dp),
        color = if (seleccionado) colorActivo else ClayInsetBackground,
        shadowElevation = if (seleccionado) 4.dp else 0.dp,
        border = BorderStroke(
            1.dp,
            if (seleccionado) Color.White.copy(alpha = 0.35f) else ClayRimShadow
        ),
        modifier = modifier
    ) {
        Column(
            modifier = Modifier.padding(vertical = 10.dp, horizontal = 6.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = titulo,
                fontWeight = FontWeight.Black,
                fontSize = 13.sp,
                color = if (seleccionado) Color.White else TextoOscuro
            )
            Text(
                text = subtitulo,
                fontWeight = FontWeight.SemiBold,
                fontSize = 10.sp,
                color = if (seleccionado) Color.White.copy(alpha = 0.85f) else TextoSecundario
            )
        }
    }
}

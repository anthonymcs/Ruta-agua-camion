package com.rutaagua.camion.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material.icons.filled.WaterDrop
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.rutaagua.camion.data.local.model.TipoVehiculo
import com.rutaagua.camion.data.local.prefs.AppPreferences
import com.rutaagua.camion.data.local.prefs.RolUsuario
import com.rutaagua.camion.data.repository.RutaAguaRepository
import com.rutaagua.camion.ui.components.ClayButton
import com.rutaagua.camion.ui.components.ClayCard
import com.rutaagua.camion.ui.theme.PichinchaCanvas
import com.rutaagua.camion.ui.theme.PichinchaNavy
import com.rutaagua.camion.ui.theme.PichinchaNavyDark
import com.rutaagua.camion.ui.theme.PichinchaYellow
import com.rutaagua.camion.ui.theme.RojoAlerta
import com.rutaagua.camion.ui.theme.RojoAlertaFondo
import com.rutaagua.camion.ui.theme.TextoSecundario

/**
 * Pantalla de Inicio de Sesión estilo Banca Móvil (limpia, minimalista y directa).
 */
@Composable
fun SeleccionVehiculoScreen(
    repository: RutaAguaRepository,
    onSeleccionarPerfil: (TipoVehiculo, RolUsuario) -> Unit,
    onAbrirAjustes: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val focusManager = LocalFocusManager.current
    val appPreferences = remember { AppPreferences.getInstance(context) }

    var usuario by rememberSaveable { mutableStateOf("") }
    var clave by rememberSaveable { mutableStateOf("") }
    var mostrarClave by rememberSaveable { mutableStateOf(false) }
    var mensajeError by rememberSaveable { mutableStateOf<String?>(null) }

    val intentarLogin = {
        focusManager.clearFocus()
        val resultado = appPreferences.autenticarCredenciales(usuario, clave)
        if (resultado != null) {
            mensajeError = null
            onSeleccionarPerfil(resultado.first, resultado.second)
        } else {
            mensajeError = "Usuario o contraseña incorrectos"
        }
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(PichinchaCanvas)
    ) {
        // Cabecera Azul Marino estilo Banca Móvil
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(290.dp)
                .clip(RoundedCornerShape(bottomStart = 36.dp, bottomEnd = 36.dp))
                .background(
                    Brush.verticalGradient(
                        colors = listOf(PichinchaNavyDark, PichinchaNavy)
                    )
                )
        ) {
            // Botón discreto de ajustes en la esquina superior derecha
            IconButton(
                onClick = onAbrirAjustes,
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(top = 38.dp, end = 16.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Settings,
                    contentDescription = "Ajustes",
                    tint = Color.White.copy(alpha = 0.8f)
                )
            }

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 64.dp, start = 28.dp, end = 28.dp),
                horizontalAlignment = Alignment.Start
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Surface(
                        color = PichinchaYellow,
                        shape = RoundedCornerShape(14.dp),
                        modifier = Modifier.size(46.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(
                                imageVector = Icons.Default.WaterDrop,
                                contentDescription = null,
                                tint = PichinchaNavy,
                                modifier = Modifier.size(26.dp)
                            )
                        }
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Text(
                            text = "Aqua Apolo",
                            color = Color.White,
                            fontSize = 22.sp,
                            fontWeight = FontWeight.Black
                        )
                        Text(
                            text = "Control de Ruta",
                            color = Color.White.copy(alpha = 0.75f),
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }

                Spacer(modifier = Modifier.height(28.dp))

                Text(
                    text = "Hola, bienvenido",
                    color = Color.White,
                    fontSize = 26.sp,
                    fontWeight = FontWeight.Black
                )
                Text(
                    text = "Ingresa con tu usuario asignado",
                    color = Color.White.copy(alpha = 0.8f),
                    fontSize = 14.sp
                )
            }
        }

        // Tarjeta Flotante de Login
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 22.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(215.dp))

            ClayCard(
                modifier = Modifier.fillMaxWidth(),
                containerColor = Color.White,
                cornerRadius = 24.dp,
                elevation = 10.dp
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 6.dp, vertical = 8.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Text(
                        text = "Iniciar sesión",
                        color = PichinchaNavy,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Black
                    )

                    OutlinedTextField(
                        value = usuario,
                        onValueChange = {
                            usuario = it
                            mensajeError = null
                        },
                        label = { Text("Usuario") },
                        leadingIcon = {
                            Icon(
                                imageVector = Icons.Default.Person,
                                contentDescription = null,
                                tint = PichinchaNavy
                            )
                        },
                        singleLine = true,
                        shape = RoundedCornerShape(14.dp),
                        keyboardOptions = KeyboardOptions(
                            keyboardType = KeyboardType.Text,
                            imeAction = ImeAction.Next
                        ),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = PichinchaNavy,
                            unfocusedBorderColor = Color(0xFFCBD5E1),
                            focusedLabelColor = PichinchaNavy,
                            focusedContainerColor = Color.White,
                            unfocusedContainerColor = Color(0xFFF8FAFC)
                        ),
                        modifier = Modifier.fillMaxWidth()
                    )

                    OutlinedTextField(
                        value = clave,
                        onValueChange = {
                            clave = it
                            mensajeError = null
                        },
                        label = { Text("Contraseña") },
                        leadingIcon = {
                            Icon(
                                imageVector = Icons.Default.Lock,
                                contentDescription = null,
                                tint = PichinchaNavy
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
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = PichinchaNavy,
                            unfocusedBorderColor = Color(0xFFCBD5E1),
                            focusedLabelColor = PichinchaNavy,
                            focusedContainerColor = Color.White,
                            unfocusedContainerColor = Color(0xFFF8FAFC)
                        ),
                        modifier = Modifier.fillMaxWidth()
                    )

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

                    Spacer(modifier = Modifier.height(4.dp))

                    // Botón Principal Amarillo Banca Móvil con Texto Azul Marino
                    ClayButton(
                        onClick = intentarLogin,
                        modifier = Modifier.fillMaxWidth(),
                        containerColor = PichinchaYellow,
                        contentColor = PichinchaNavy,
                        cornerRadius = 14.dp,
                        height = 54.dp
                    ) {
                        Text(
                            text = "Ingresar",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Black,
                            color = PichinchaNavy,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(32.dp))
        }
    }
}

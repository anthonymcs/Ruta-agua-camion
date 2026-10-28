package com.rutaagua.camion.ui.components

import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.DeleteOutline
import androidx.compose.material.icons.filled.GpsFixed
import androidx.compose.material.icons.filled.Link
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Navigation
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.rutaagua.camion.data.local.entity.ClienteEntity
import com.rutaagua.camion.ui.theme.AguaPrimary
import com.rutaagua.camion.ui.theme.AguaPrimaryDark
import com.rutaagua.camion.ui.theme.BordeSutil
import com.rutaagua.camion.ui.theme.FondoGrisClaro
import com.rutaagua.camion.ui.theme.RojoAlerta
import com.rutaagua.camion.ui.theme.TextoOscuro
import com.rutaagua.camion.ui.theme.TextoSecundario
import com.rutaagua.camion.ui.theme.VerdeEfectivo
import com.rutaagua.camion.ui.utils.GpsLocationHelper
import java.util.Locale

/**
 * Diálogo rápido para capturar y guardar la ubicación GPS o enlace de mapa del cliente a costo $0.00.
 */
@Composable
fun GuardarUbicacionDialog(
    cliente: ClienteEntity,
    onGuardarUbicacion: (lat: Double?, lng: Double?, enlace: String) -> Unit,
    onDismissRequest: () -> Unit
) {
    val context = LocalContext.current
    var latitud by remember { mutableStateOf(cliente.latitud) }
    var longitud by remember { mutableStateOf(cliente.longitud) }
    var enlaceMapa by remember { mutableStateOf(cliente.enlaceMapa) }
    var buscandoGps by remember { mutableStateOf(false) }

    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        val granted = permissions.values.any { it }
        if (granted) {
            buscandoGps = true
            GpsLocationHelper.capturarUbicacionActual(
                context = context,
                onUbicacionExitosa = { lat, lng ->
                    buscandoGps = false
                    latitud = lat
                    longitud = lng
                    Toast.makeText(context, "Coordenadas capturadas con éxito", Toast.LENGTH_SHORT).show()
                },
                onError = { err ->
                    buscandoGps = false
                    Toast.makeText(context, err, Toast.LENGTH_LONG).show()
                }
            )
        } else {
            Toast.makeText(context, "Permiso de ubicación denegado", Toast.LENGTH_SHORT).show()
        }
    }

    AlertDialog(
        onDismissRequest = onDismissRequest,
        shape = RoundedCornerShape(20.dp),
        containerColor = Color.White,
        title = {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.LocationOn,
                        contentDescription = null,
                        tint = AguaPrimary,
                        modifier = Modifier.size(24.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Ubicación del Cliente",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Black,
                        color = TextoOscuro
                    )
                }
                IconButton(onClick = onDismissRequest, modifier = Modifier.size(28.dp)) {
                    Icon(imageVector = Icons.Default.Close, contentDescription = "Cerrar", tint = TextoSecundario)
                }
            }
        },
        text = {
            Column(modifier = Modifier.fillMaxWidth()) {
                Text(
                    text = cliente.nombre,
                    fontWeight = FontWeight.Bold,
                    fontSize = 15.sp,
                    color = AguaPrimaryDark
                )
                if (cliente.ubicacion.isNotBlank()) {
                    Text(
                        text = cliente.ubicacion,
                        fontSize = 12.sp,
                        color = TextoSecundario,
                        modifier = Modifier.padding(bottom = 12.dp)
                    )
                } else {
                    Spacer(modifier = Modifier.height(10.dp))
                }

                // Opción 1: Captura directa por satélite GPS
                Button(
                    onClick = {
                        if (GpsLocationHelper.tienePermisosUbicacion(context)) {
                            buscandoGps = true
                            GpsLocationHelper.capturarUbicacionActual(
                                context = context,
                                onUbicacionExitosa = { lat, lng ->
                                    buscandoGps = false
                                    latitud = lat
                                    longitud = lng
                                    Toast.makeText(context, "Coordenadas fijadas", Toast.LENGTH_SHORT).show()
                                },
                                onError = { err ->
                                    buscandoGps = false
                                    Toast.makeText(context, err, Toast.LENGTH_LONG).show()
                                }
                            )
                        } else {
                            permissionLauncher.launch(
                                arrayOf(
                                    android.Manifest.permission.ACCESS_FINE_LOCATION,
                                    android.Manifest.permission.ACCESS_COARSE_LOCATION
                                )
                            )
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = if (latitud != null) VerdeEfectivo else AguaPrimary),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth().height(48.dp),
                    enabled = !buscandoGps
                ) {
                    if (buscandoGps) {
                        CircularProgressIndicator(
                            color = Color.White,
                            modifier = Modifier.size(20.dp),
                            strokeWidth = 2.dp
                        )
                        Spacer(modifier = Modifier.width(10.dp))
                        Text("Buscando satélites GPS...", fontWeight = FontWeight.Bold, color = Color.White, fontSize = 13.sp)
                    } else {
                        Icon(imageVector = Icons.Default.GpsFixed, contentDescription = null, tint = Color.White, modifier = Modifier.size(20.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = if (latitud != null) "Actualizar Coordenadas GPS" else "Capturar GPS (En la Casa)",
                            fontWeight = FontWeight.Bold,
                            color = Color.White,
                            fontSize = 12.sp,
                            maxLines = 1,
                            softWrap = false
                        )
                    }
                }

                // Indicador de coordenadas capturadas
                if (latitud != null && longitud != null) {
                    Spacer(modifier = Modifier.height(10.dp))
                    Surface(
                        color = FondoGrisClaro,
                        shape = RoundedCornerShape(10.dp),
                        border = BorderStroke(1.dp, BordeSutil),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier.padding(10.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text(
                                    text = "📍 Coordenadas listas:",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = TextoSecundario
                                )
                                Text(
                                    text = "${String.format(Locale.US, "%.5f", latitud)}, ${String.format(Locale.US, "%.5f", longitud)}",
                                    fontWeight = FontWeight.Black,
                                    fontSize = 13.sp,
                                    color = TextoOscuro
                                )
                            }
                            Row {
                                IconButton(
                                    onClick = {
                                        GpsLocationHelper.abrirNavegacionDirecta(context, latitud, longitud)
                                    },
                                    modifier = Modifier.size(34.dp)
                                ) {
                                    Icon(imageVector = Icons.Default.Navigation, contentDescription = "Probar ruta", tint = AguaPrimary)
                                }
                                IconButton(
                                    onClick = {
                                        latitud = null
                                        longitud = null
                                    },
                                    modifier = Modifier.size(34.dp)
                                ) {
                                    Icon(imageVector = Icons.Default.DeleteOutline, contentDescription = "Borrar", tint = RojoAlerta)
                                }
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Opción 2: Pegar enlace de WhatsApp o Google Maps
                Text(
                    text = "O pegar enlace de Google Maps / WhatsApp:",
                    style = MaterialTheme.typography.labelSmall,
                    color = TextoSecundario,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(4.dp))
                OutlinedTextField(
                    value = enlaceMapa,
                    onValueChange = {
                        enlaceMapa = it
                        // Si contiene coordenadas implícitas, detectarlas automáticamente
                        val coords = GpsLocationHelper.extraerCoordenadasDeEnlace(it)
                        if (coords != null) {
                            latitud = coords.first
                            longitud = coords.second
                            Toast.makeText(context, "Coordenadas detectadas del link", Toast.LENGTH_SHORT).show()
                        }
                    },
                    placeholder = { Text("https://maps.app.goo.gl/... o https://wa.me/...") },
                    leadingIcon = {
                        Icon(imageVector = Icons.Default.Link, contentDescription = null, tint = AguaPrimary)
                    },
                    singleLine = true,
                    shape = RoundedCornerShape(12.dp),
                    colors = marineOutlinedTextFieldColors(),
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    onGuardarUbicacion(latitud, longitud, enlaceMapa.trim())
                    onDismissRequest()
                },
                colors = ButtonDefaults.buttonColors(containerColor = AguaPrimary),
                shape = RoundedCornerShape(10.dp)
            ) {
                Text("Guardar Ubicación", fontWeight = FontWeight.Bold, color = Color.White)
            }
        },
        dismissButton = {
            OutlinedButton(
                onClick = onDismissRequest,
                shape = RoundedCornerShape(10.dp)
            ) {
                Text("Cancelar", color = TextoOscuro)
            }
        }
    )
}

package com.rutaagua.camion.ui.screens.inventario

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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.LocalShipping
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.BasicAlertDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.DialogProperties
import com.rutaagua.camion.data.local.entity.InventarioCamionEntity
import com.rutaagua.camion.data.local.model.TipoBidon
import com.rutaagua.camion.ui.components.BidonIllustration
import com.rutaagua.camion.ui.components.ClayBadge
import com.rutaagua.camion.ui.components.ClayButton
import com.rutaagua.camion.ui.components.ClayCard
import com.rutaagua.camion.ui.components.ClayEditableCounter
import com.rutaagua.camion.ui.components.ClayInsetBox
import com.rutaagua.camion.ui.components.ClayVehicleTheme
import com.rutaagua.camion.ui.components.GalonIllustration
import com.rutaagua.camion.ui.components.MiniIllustration
import com.rutaagua.camion.ui.components.PacaIllustration
import com.rutaagua.camion.ui.theme.BidonAzulColor
import com.rutaagua.camion.ui.theme.BidonMarcadoColor
import com.rutaagua.camion.ui.theme.BidonTransparenteColor
import com.rutaagua.camion.ui.theme.TextoOscuro
import com.rutaagua.camion.ui.theme.TextoSecundario
import com.rutaagua.camion.ui.theme.VerdeEfectivo
import com.rutaagua.camion.ui.theme.VerdeEfectivoFondo

/**
 * Diálogo interactivo y obligatorio de inicio de jornada:
 * Se muestra si la unidad arranca en 0 bidones para obligar el registro
 * exacto de la carga física con la que sale el vehículo (evitando discrepancias).
 * Si el vehículo ya tenía carga del día anterior, no bloquea y permite continuar.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DialogoCargaInicialObligatoria(
    inventario: InventarioCamionEntity?,
    clayTheme: ClayVehicleTheme,
    onConfirmarCargaInicial: (
        metaBidones: Int,
        trans: Int,
        azul: Int,
        marc: Int,
        galones: Int,
        pacas: Int,
        minis: Int
    ) -> Unit
) {
    val metaInicial = inventario?.capacidadTopeBidones ?: clayTheme.capacidadDefault
    var metaBidones by rememberSaveable { mutableIntStateOf(metaInicial) }
    var cantTrans by rememberSaveable { mutableIntStateOf(0) }
    var cantAzul by rememberSaveable { mutableIntStateOf(0) }
    var cantMarc by rememberSaveable { mutableIntStateOf(0) }

    var cantGalones by rememberSaveable { mutableIntStateOf(0) }
    var cantPacas by rememberSaveable { mutableIntStateOf(0) }
    var cantMinis by rememberSaveable { mutableIntStateOf(0) }

    val totalBidones = cantTrans + cantAzul + cantMarc

    BasicAlertDialog(
        onDismissRequest = { /* Diálogo obligatorio: no se cancela tocando afuera */ },
        properties = DialogProperties(
            dismissOnBackPress = false,
            dismissOnClickOutside = false,
            usePlatformDefaultWidth = false
        ),
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp)
    ) {
        Surface(
            shape = RoundedCornerShape(26.dp),
            color = clayTheme.canvasColor,
            tonalElevation = 8.dp
        ) {
            LazyColumn(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(18.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                item {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(46.dp)
                                .background(clayTheme.accentBgColor, RoundedCornerShape(14.dp)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.LocalShipping,
                                contentDescription = null,
                                tint = clayTheme.primaryDarkColor,
                                modifier = Modifier.size(26.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "CARGA INICIAL DE RUTA",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Black,
                                color = clayTheme.primaryDarkColor
                            )
                            Text(
                                text = "Ingresa con cuántos bidones sale ${clayTheme.vehicleTitle} hoy",
                                style = MaterialTheme.typography.bodySmall,
                                color = TextoSecundario
                            )
                        }
                    }
                }

                // Ajuste de Meta Diaria para la jornada (ej. 218 o 189)
                item {
                    ClayCard(
                        modifier = Modifier.fillMaxWidth(),
                        containerColor = Color.White,
                        cornerRadius = 16.dp,
                        elevation = 2.dp,
                        contentPadding = PaddingValues(horizontal = 14.dp, vertical = 10.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = "Meta / Límite de carga hoy",
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = TextoOscuro
                                )
                                Text(
                                    text = "Tope usual: $metaInicial (ej. 218 o 189)",
                                    fontSize = 11.sp,
                                    color = TextoSecundario
                                )
                            }
                            ClayEditableCounter(
                                valor = metaBidones,
                                onCambio = { nuevo -> if (nuevo > 0) metaBidones = nuevo },
                                accentColor = clayTheme.primaryColor
                            )
                        }
                    }
                }

                // Resumen de estado de carga
                item {
                    ClayInsetBox(
                        modifier = Modifier.fillMaxWidth(),
                        containerColor = if (totalBidones > 0) VerdeEfectivoFondo else Color(0xFFFFF7EB),
                        cornerRadius = 14.dp,
                        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 10.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = "TOTAL A SALIR: $totalBidones / $metaBidones BIDONES",
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Black,
                                    color = if (totalBidones > 0) VerdeEfectivo else Color(0xFFD97706)
                                )
                                Text(
                                    text = if (totalBidones == 0) "Indica la cantidad cargada para comenzar" else "Carga lista para salir a clientes",
                                    fontSize = 11.sp,
                                    color = TextoOscuro
                                )
                            }
                            ClayBadge(
                                text = "$totalBidones",
                                backgroundColor = if (totalBidones > 0) VerdeEfectivo else Color(0xFFD97706),
                                textColor = Color.White
                            )
                        }
                    }
                }

                item {
                    Text(
                        text = "BIDONES DE 20L",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Black,
                        color = TextoSecundario
                    )
                }

                item {
                    FilaCargaSimple(
                        nombre = "Transparentes (PET)",
                        cantidad = cantTrans,
                        color = BidonTransparenteColor,
                        ilustracion = { BidonIllustration(TipoBidon.TRANSPARENTE, size = 32.dp) },
                        onCambio = { cantTrans = it }
                    )
                }

                item {
                    FilaCargaSimple(
                        nombre = "Azules Estándar",
                        cantidad = cantAzul,
                        color = BidonAzulColor,
                        ilustracion = { BidonIllustration(TipoBidon.AZUL, size = 32.dp) },
                        onCambio = { cantAzul = it }
                    )
                }

                item {
                    FilaCargaSimple(
                        nombre = "Marcadas",
                        cantidad = cantMarc,
                        color = BidonMarcadoColor,
                        ilustracion = { BidonIllustration(TipoBidon.MARCADO, size = 32.dp) },
                        onCambio = { cantMarc = it }
                    )
                }

                item {
                    Text(
                        text = "OTROS PRODUCTOS",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Black,
                        color = TextoSecundario
                    )
                }

                item {
                    FilaCargaSimple(
                        nombre = "Galones (4L)",
                        cantidad = cantGalones,
                        color = clayTheme.primaryColor,
                        ilustracion = { GalonIllustration(size = 32.dp) },
                        onCambio = { cantGalones = it }
                    )
                }

                item {
                    FilaCargaSimple(
                        nombre = "Pacas (24x500ml)",
                        cantidad = cantPacas,
                        color = clayTheme.primaryColor,
                        ilustracion = { PacaIllustration(size = 32.dp) },
                        onCambio = { cantPacas = it }
                    )
                }

                item {
                    FilaCargaSimple(
                        nombre = "Minis (12x500ml)",
                        cantidad = cantMinis,
                        color = clayTheme.primaryColor,
                        ilustracion = { MiniIllustration(size = 32.dp) },
                        onCambio = { cantMinis = it }
                    )
                }

                item {
                    Spacer(modifier = Modifier.height(6.dp))
                    ClayButton(
                        onClick = {
                            onConfirmarCargaInicial(
                                metaBidones,
                                cantTrans,
                                cantAzul,
                                cantMarc,
                                cantGalones,
                                cantPacas,
                                cantMinis
                            )
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(52.dp),
                        containerColor = if (totalBidones > 0) VerdeEfectivo else clayTheme.primaryDarkColor,
                        contentColor = Color.White,
                        cornerRadius = 16.dp
                    ) {
                        Text(
                            text = if (totalBidones > 0) "Confirmar Carga ($totalBidones bidones)" else "Ingresar Carga y Salir a Ruta",
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Black,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun FilaCargaSimple(
    nombre: String,
    cantidad: Int,
    color: Color,
    ilustracion: @Composable () -> Unit,
    onCambio: (Int) -> Unit
) {
    ClayCard(
        modifier = Modifier.fillMaxWidth(),
        containerColor = Color.White,
        cornerRadius = 14.dp,
        elevation = 2.dp,
        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 8.dp)
    ) {
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
                    modifier = Modifier.size(32.dp),
                    contentAlignment = Alignment.Center
                ) {
                    ilustracion()
                }
                Spacer(modifier = Modifier.width(10.dp))
                Text(
                    text = nombre,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold,
                    color = TextoOscuro,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
            ClayEditableCounter(
                valor = cantidad,
                onCambio = onCambio,
                accentColor = color
            )
        }
    }
}


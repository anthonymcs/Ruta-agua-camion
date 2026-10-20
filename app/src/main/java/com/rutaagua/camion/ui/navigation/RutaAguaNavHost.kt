package com.rutaagua.camion.ui.navigation

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.rutaagua.camion.data.local.RutaAguaDatabase
import com.rutaagua.camion.data.local.model.TipoVehiculo
import com.rutaagua.camion.data.local.prefs.AppPreferences
import com.rutaagua.camion.data.repository.RutaAguaRepository
import com.rutaagua.camion.ui.screens.calendario.CalendarioRutaScreen
import com.rutaagua.camion.ui.screens.clientes.ClientesScreen
import com.rutaagua.camion.ui.screens.clientes.ClientesViewModel
import com.rutaagua.camion.ui.screens.configuracion.ConfiguracionAjustesDialog
import com.rutaagua.camion.ui.screens.inventario.InventarioCamionScreen
import com.rutaagua.camion.ui.screens.inventario.InventarioCamionViewModel
import com.rutaagua.camion.ui.screens.registro.RegistroDiaScreen
import com.rutaagua.camion.ui.screens.registro.RegistroDiaViewModel
import com.rutaagua.camion.ui.theme.AguaPrimary
import com.rutaagua.camion.ui.theme.AguaPrimaryLight
import com.rutaagua.camion.ui.theme.AzulCorporativo
import com.rutaagua.camion.ui.theme.BordeSutil
import com.rutaagua.camion.ui.theme.TextoOscuro
import com.rutaagua.camion.ui.theme.TextoSecundario

class RutaAguaViewModelFactory(
    private val repository: RutaAguaRepository,
    private val appPreferences: AppPreferences
) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return when {
            modelClass.isAssignableFrom(RegistroDiaViewModel::class.java) -> {
                RegistroDiaViewModel(repository, appPreferences) as T
            }
            modelClass.isAssignableFrom(InventarioCamionViewModel::class.java) -> {
                InventarioCamionViewModel(repository, appPreferences) as T
            }
            modelClass.isAssignableFrom(ClientesViewModel::class.java) -> {
                ClientesViewModel(repository) as T
            }
            else -> throw IllegalArgumentException("Unknown ViewModel class: ${modelClass.name}")
        }
    }
}

/**
 * Scaffold principal con Header superior de vehículo activo, barra inferior de 4 pestañas y NavHost.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RutaAguaNavHost(
    repository: RutaAguaRepository,
    database: RutaAguaDatabase,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val appPreferences = remember { AppPreferences.getInstance(context) }
    val vehiculoActivo by appPreferences.vehiculoActivo.collectAsState()
    var mostrarModalAjustes by remember { mutableStateOf(false) }

    val navController = rememberNavController()
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route ?: AppDestinations.REGISTRO.ruta

    val factory = RutaAguaViewModelFactory(repository, appPreferences)

    val registroDiaViewModel: RegistroDiaViewModel = viewModel(factory = factory)
    val inventarioViewModel: InventarioCamionViewModel = viewModel(factory = factory)
    val clientesViewModel: ClientesViewModel = viewModel(factory = factory)

    Scaffold(
        modifier = modifier.fillMaxSize(),
        topBar = {
            TopAppBar(
                title = {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = "💧 Ruta Agua",
                                fontWeight = FontWeight.Black,
                                fontSize = 18.sp,
                                color = TextoOscuro
                            )
                            Spacer(modifier = Modifier.width(10.dp))

                            // Pastilla / Selector Rápido del Vehículo Activo (Toque para alternar)
                            val esCamion = vehiculoActivo == TipoVehiculo.CAMION
                            Surface(
                                color = if (esCamion) AguaPrimary.copy(alpha = 0.12f) else AzulCorporativo.copy(alpha = 0.12f),
                                shape = RoundedCornerShape(20.dp),
                                border = BorderStroke(1.dp, if (esCamion) AguaPrimary else AzulCorporativo),
                                modifier = Modifier.clickable {
                                    val nuevo = if (esCamion) TipoVehiculo.CAMIONETA else TipoVehiculo.CAMION
                                    appPreferences.guardarVehiculoActivo(nuevo)
                                }
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = if (esCamion) "🚚 Camión" else "🛻 Camioneta",
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 12.sp,
                                        color = if (esCamion) AguaPrimary else AzulCorporativo
                                    )
                                }
                            }
                        }

                        IconButton(onClick = { mostrarModalAjustes = true }) {
                            Icon(
                                imageVector = Icons.Default.Settings,
                                contentDescription = "Ajustes",
                                tint = TextoOscuro
                            )
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            )
        },
        bottomBar = {
            NavigationBar(
                containerColor = MaterialTheme.colorScheme.surface,
                tonalElevation = 6.dp
            ) {
                AppDestinations.entries.forEach { destination ->
                    val selected = currentRoute == destination.ruta
                    NavigationBarItem(
                        selected = selected,
                        onClick = {
                            if (currentRoute != destination.ruta) {
                                navController.navigate(destination.ruta) {
                                    popUpTo(navController.graph.findStartDestination().id) {
                                        saveState = true
                                    }
                                    launchSingleTop = true
                                    restoreState = true
                                }
                            }
                        },
                        icon = {
                            Icon(
                                imageVector = destination.icono,
                                contentDescription = destination.titulo,
                                modifier = Modifier.size(26.dp)
                            )
                        },
                        label = {
                            Text(
                                text = destination.titulo,
                                style = MaterialTheme.typography.labelMedium,
                                fontWeight = if (selected) FontWeight.Bold else FontWeight.Normal
                            )
                        },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = AguaPrimary,
                            selectedTextColor = AguaPrimary,
                            unselectedIconColor = TextoSecundario,
                            unselectedTextColor = TextoSecundario,
                            indicatorColor = AguaPrimaryLight.copy(alpha = 0.25f)
                        )
                    )
                }
            }
        }
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = AppDestinations.REGISTRO.ruta,
            modifier = Modifier.padding(innerPadding)
        ) {
            // Pestaña 1: Registro Diario de Ventas, Arqueo de Caja y WhatsApp
            composable(AppDestinations.REGISTRO.ruta) {
                RegistroDiaScreen(viewModel = registroDiaViewModel)
            }

            // Pestaña 2: Camión (Inventario en vivo y Recarga en Planta)
            composable(AppDestinations.CAMION.ruta) {
                InventarioCamionScreen(viewModel = inventarioViewModel)
            }

            // Pestaña 3: Directorio de Clientes y Saldos
            composable(AppDestinations.CLIENTES.ruta) {
                ClientesScreen(viewModel = clientesViewModel)
            }

            // Pestaña 4: Calendario de Ruta Semanal (Lunes a Sábado)
            composable(AppDestinations.CALENDARIO.ruta) {
                CalendarioRutaScreen(
                    viewModel = clientesViewModel,
                    onRegistrarEntregaCliente = { cliente ->
                        registroDiaViewModel.abrirModalNuevaVenta(cliente)
                        navController.navigate(AppDestinations.REGISTRO.ruta) {
                            popUpTo(navController.graph.findStartDestination().id) {
                                saveState = true
                            }
                            launchSingleTop = true
                            restoreState = true
                        }
                    }
                )
            }
        }

        if (mostrarModalAjustes) {
            ConfiguracionAjustesDialog(
                database = database,
                onDismissRequest = { mostrarModalAjustes = false }
            )
        }
    }
}

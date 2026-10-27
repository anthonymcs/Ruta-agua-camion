package com.rutaagua.camion.ui.navigation

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Analytics
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.LocalShipping
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.People
import androidx.compose.material.icons.filled.PointOfSale
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Sync
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalDrawerSheet
import androidx.compose.material3.ModalNavigationDrawer
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.NavigationDrawerItem
import androidx.compose.material3.NavigationDrawerItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberDrawerState
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
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
import com.rutaagua.camion.data.local.prefs.RolUsuario
import com.rutaagua.camion.data.repository.RutaAguaRepository
import com.rutaagua.camion.ui.components.CamionIllustration
import com.rutaagua.camion.ui.components.CamionetaIllustration
import com.rutaagua.camion.ui.components.VehiculoThumbnail
import com.rutaagua.camion.ui.screens.SeleccionVehiculoScreen
import com.rutaagua.camion.ui.screens.calendario.CalendarioRutaScreen
import com.rutaagua.camion.ui.screens.clientes.ClientesScreen
import com.rutaagua.camion.ui.screens.clientes.ClientesViewModel
import com.rutaagua.camion.ui.screens.configuracion.ConfiguracionAjustesDialog
import com.rutaagua.camion.ui.screens.inventario.InventarioCamionScreen
import com.rutaagua.camion.ui.screens.inventario.InventarioCamionViewModel
import com.rutaagua.camion.ui.screens.registro.RegistroDiaScreen
import com.rutaagua.camion.ui.screens.registro.RegistroDiaViewModel
import com.rutaagua.camion.ui.screens.rolando.PanelRolandoScreen
import com.rutaagua.camion.ui.theme.AguaPrimary
import com.rutaagua.camion.ui.theme.AguaPrimaryLight
import com.rutaagua.camion.ui.theme.AzulCorporativo
import com.rutaagua.camion.ui.theme.BordeSutil
import com.rutaagua.camion.ui.theme.FondoGrisClaro
import com.rutaagua.camion.ui.theme.SuperficieBlanca
import com.rutaagua.camion.ui.theme.TextoOscuro
import com.rutaagua.camion.ui.theme.TextoSecundario
import com.rutaagua.camion.ui.theme.VerdeEfectivo
import kotlinx.coroutines.launch

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
 * Scaffold principal con ModalNavigationDrawer (Menú Lateral), TopAppBar limpio,
 * barra inferior de navegación y NavHost con separación estricta de perfiles.
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
    val rolActivo by appPreferences.rolActivo.collectAsState()
    var vehiculoSeleccionado by rememberSaveable {
        mutableStateOf<String?>(if (appPreferences.sesionIniciada()) appPreferences.obtenerVehiculoActivo().name else null)
    }
    var mostrarModalAjustes by rememberSaveable { mutableStateOf(false) }

    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
    val coroutineScope = rememberCoroutineScope()

    val navController = rememberNavController()
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route ?: AppDestinations.REGISTRO.ruta

    val factory = RutaAguaViewModelFactory(repository, appPreferences)

    val registroDiaViewModel: RegistroDiaViewModel = viewModel(factory = factory)
    val inventarioViewModel: InventarioCamionViewModel = viewModel(factory = factory)
    val clientesViewModel: ClientesViewModel = viewModel(factory = factory)

    // Si aún no ha seleccionado el vehículo en esta sesión, mostramos la pantalla de bienvenida y elección
    if (vehiculoSeleccionado == null) {
        SeleccionVehiculoScreen(
            repository = repository,
            onSeleccionarPerfil = { tipo, rol ->
                appPreferences.guardarVehiculoActivo(tipo)
                appPreferences.guardarRolActivo(rol)
                appPreferences.guardarSesionIniciada(true)
                registroDiaViewModel.cambiarFiltroSegunVehiculo(tipo)
                vehiculoSeleccionado = tipo.name
            },
            onAbrirAjustes = { mostrarModalAjustes = true }
        )

        if (mostrarModalAjustes) {
            ConfiguracionAjustesDialog(
                database = database,
                onDismissRequest = { mostrarModalAjustes = false }
            )
        }
        return
    }

    ModalNavigationDrawer(
        drawerState = drawerState,
        gesturesEnabled = true,
        drawerContent = {
            ModalDrawerSheet(
                modifier = Modifier.width(310.dp),
                drawerContainerColor = Color.White
            ) {
                // Header del Menú Lateral
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(Color(0xFF0F172A))
                        .padding(horizontal = 20.dp, vertical = 24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    // Ilustración vectorial grande del vehículo en operación
                    Box(
                        modifier = Modifier
                            .size(110.dp)
                            .background(Color(0xFF1E293B), RoundedCornerShape(16.dp))
                            .padding(8.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        if (vehiculoActivo == TipoVehiculo.CAMIONETA) {
                            CamionetaIllustration(modifier = Modifier.fillMaxSize())
                        } else {
                            CamionIllustration(modifier = Modifier.fillMaxSize())
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    val nombreVehiculo = if (vehiculoActivo == TipoVehiculo.CAMIONETA) "CAMIONETA" else "CAMIÓN"
                    Text(
                        text = if (rolActivo == RolUsuario.ADMIN_ROLANDO) "$nombreVehiculo (ROLANDO)" else "$nombreVehiculo (OPERADOR)",
                        color = Color.White,
                        fontWeight = FontWeight.Black,
                        fontSize = 17.sp
                    )

                    Text(
                        text = if (rolActivo == RolUsuario.ADMIN_ROLANDO) "Panel Administrador" else "Teléfono de Ruta",
                        color = Color(0xFF94A3B8),
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Medium
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    // Indicador de conexión en tiempo real
                    Surface(
                        color = Color(0xFF1E293B),
                        shape = RoundedCornerShape(12.dp),
                        border = BorderStroke(1.dp, Color(0xFF334155))
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(8.dp)
                                    .clip(CircleShape)
                                    .background(VerdeEfectivo)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "🟢 En línea (Firebase)",
                                color = VerdeEfectivo,
                                fontWeight = FontWeight.Bold,
                                fontSize = 11.sp
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    // Botón para alternar vehículo
                    Button(
                        onClick = {
                            coroutineScope.launch { drawerState.close() }
                            appPreferences.guardarSesionIniciada(false)
                            vehiculoSeleccionado = null
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = AzulCorporativo),
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(40.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Sync,
                            contentDescription = null,
                            tint = Color.White,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "🔄 Cambiar Vehículo",
                            color = Color.White,
                            fontWeight = FontWeight.Bold,
                            fontSize = 12.sp
                        )
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Ítem 1: Ruta del Día (Calendario)
                NavigationDrawerItem(
                    icon = { Icon(Icons.Default.CalendarMonth, contentDescription = null, modifier = Modifier.size(24.dp)) },
                    label = { Text("📅 Ruta del Día", fontWeight = FontWeight.Bold, fontSize = 15.sp) },
                    selected = currentRoute == AppDestinations.CALENDARIO.ruta,
                    onClick = {
                        coroutineScope.launch { drawerState.close() }
                        navController.navigate(AppDestinations.CALENDARIO.ruta) {
                            popUpTo(navController.graph.findStartDestination().id) { saveState = true }
                            launchSingleTop = true
                            restoreState = true
                        }
                    },
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 3.dp),
                    colors = NavigationDrawerItemDefaults.colors(
                        selectedContainerColor = AguaPrimaryLight.copy(alpha = 0.2f),
                        selectedIconColor = AguaPrimary,
                        selectedTextColor = AguaPrimary
                    )
                )

                // Ítem 2: Clientes
                NavigationDrawerItem(
                    icon = { Icon(Icons.Default.People, contentDescription = null, modifier = Modifier.size(24.dp)) },
                    label = { Text("👥 Clientes", fontWeight = FontWeight.Bold, fontSize = 15.sp) },
                    selected = currentRoute == AppDestinations.CLIENTES.ruta,
                    onClick = {
                        coroutineScope.launch { drawerState.close() }
                        navController.navigate(AppDestinations.CLIENTES.ruta) {
                            popUpTo(navController.graph.findStartDestination().id) { saveState = true }
                            launchSingleTop = true
                            restoreState = true
                        }
                    },
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 3.dp),
                    colors = NavigationDrawerItemDefaults.colors(
                        selectedContainerColor = AguaPrimaryLight.copy(alpha = 0.2f),
                        selectedIconColor = AguaPrimary,
                        selectedTextColor = AguaPrimary
                    )
                )

                // Ítem 3: Inventario
                NavigationDrawerItem(
                    icon = { Icon(Icons.Default.LocalShipping, contentDescription = null, modifier = Modifier.size(24.dp)) },
                    label = { Text("🚚 Inventario", fontWeight = FontWeight.Bold, fontSize = 15.sp) },
                    selected = currentRoute == AppDestinations.CAMION.ruta,
                    onClick = {
                        coroutineScope.launch { drawerState.close() }
                        navController.navigate(AppDestinations.CAMION.ruta) {
                            popUpTo(navController.graph.findStartDestination().id) { saveState = true }
                            launchSingleTop = true
                            restoreState = true
                        }
                    },
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 3.dp),
                    colors = NavigationDrawerItemDefaults.colors(
                        selectedContainerColor = AguaPrimaryLight.copy(alpha = 0.2f),
                        selectedIconColor = AguaPrimary,
                        selectedTextColor = AguaPrimary
                    )
                )

                // Ítem 4: Caja y Cierre
                NavigationDrawerItem(
                    icon = { Icon(Icons.Default.PointOfSale, contentDescription = null, modifier = Modifier.size(24.dp)) },
                    label = { Text("💵 Caja y Cierre", fontWeight = FontWeight.Bold, fontSize = 15.sp) },
                    selected = currentRoute == AppDestinations.REGISTRO.ruta,
                    onClick = {
                        coroutineScope.launch { drawerState.close() }
                        navController.navigate(AppDestinations.REGISTRO.ruta) {
                            popUpTo(navController.graph.findStartDestination().id) { saveState = true }
                            launchSingleTop = true
                            restoreState = true
                        }
                    },
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 3.dp),
                    colors = NavigationDrawerItemDefaults.colors(
                        selectedContainerColor = AguaPrimaryLight.copy(alpha = 0.2f),
                        selectedIconColor = AguaPrimary,
                        selectedTextColor = AguaPrimary
                    )
                )

                // Ítem 5: Panel de Rolando (Administrador) - SOLO visible para Rolando
                if (rolActivo == RolUsuario.ADMIN_ROLANDO) {
                    NavigationDrawerItem(
                        icon = { Icon(Icons.Default.Analytics, contentDescription = null, tint = AzulCorporativo, modifier = Modifier.size(24.dp)) },
                        label = { Text("📊 Panel de Rolando (Administrador)", fontWeight = FontWeight.Black, color = AzulCorporativo, fontSize = 15.sp) },
                        selected = currentRoute == AppDestinations.PANEL_ROLANDO.ruta,
                        onClick = {
                            coroutineScope.launch { drawerState.close() }
                            navController.navigate(AppDestinations.PANEL_ROLANDO.ruta) {
                                popUpTo(navController.graph.findStartDestination().id) { saveState = true }
                                launchSingleTop = true
                                restoreState = true
                            }
                        },
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 3.dp),
                        colors = NavigationDrawerItemDefaults.colors(
                            selectedContainerColor = AzulCorporativo.copy(alpha = 0.15f),
                            selectedIconColor = AzulCorporativo,
                            selectedTextColor = AzulCorporativo
                        )
                    )
                }

                HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp), color = BordeSutil)

                // Ítem 6: Ajustes
                NavigationDrawerItem(
                    icon = { Icon(Icons.Default.Settings, contentDescription = null, modifier = Modifier.size(24.dp)) },
                    label = { Text("⚙️ Ajustes", fontWeight = FontWeight.Bold, fontSize = 15.sp) },
                    selected = false,
                    onClick = {
                        coroutineScope.launch { drawerState.close() }
                        mostrarModalAjustes = true
                    },
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 3.dp)
                )
            }
        }
    ) {
        Scaffold(
            modifier = modifier.fillMaxSize(),
            topBar = {
                TopAppBar(
                    navigationIcon = {
                        IconButton(onClick = { coroutineScope.launch { drawerState.open() } }) {
                            Icon(
                                imageVector = Icons.Default.Menu,
                                contentDescription = "Menú Principal",
                                tint = TextoOscuro,
                                modifier = Modifier.size(28.dp)
                            )
                        }
                    },
                    title = {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            // Ilustración + Nombre del Vehículo en Operación y Rol
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                VehiculoThumbnail(
                                    tipo = vehiculoActivo,
                                    size = 38.dp
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Column {
                                    val nombreVehiculo = if (vehiculoActivo == TipoVehiculo.CAMIONETA) "CAMIONETA" else "CAMIÓN"
                                    Text(
                                        text = if (rolActivo == RolUsuario.ADMIN_ROLANDO) "$nombreVehiculo (Rolando)" else "$nombreVehiculo (Ruta)",
                                        fontWeight = FontWeight.Black,
                                        fontSize = 15.sp,
                                        color = TextoOscuro
                                    )
                                    Text(
                                        text = if (rolActivo == RolUsuario.ADMIN_ROLANDO) "Panel Administrador" else "Modo Operador • 100% Offline",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = if (rolActivo == RolUsuario.ADMIN_ROLANDO) Color(0xFF0284C7) else VerdeEfectivo,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }

                            Row(verticalAlignment = Alignment.CenterVertically) {
                                // Botón "Cambiar" rápido
                                Surface(
                                    color = FondoGrisClaro,
                                    shape = RoundedCornerShape(10.dp),
                                    border = BorderStroke(1.dp, BordeSutil),
                                    modifier = Modifier.clickable {
                                        appPreferences.guardarSesionIniciada(false)
                                        vehiculoSeleccionado = null
                                    }
                                ) {
                                    Row(
                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 5.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Sync,
                                            contentDescription = "Cambiar Vehículo",
                                            tint = AguaPrimary,
                                            modifier = Modifier.size(15.dp)
                                        )
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text(
                                            text = "Cambiar",
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 11.sp,
                                            color = AguaPrimary
                                        )
                                    }
                                }

                                Spacer(modifier = Modifier.width(4.dp))

                                IconButton(onClick = { mostrarModalAjustes = true }) {
                                    Icon(
                                        imageVector = Icons.Default.Settings,
                                        contentDescription = "Ajustes",
                                        tint = TextoOscuro,
                                        modifier = Modifier.size(22.dp)
                                    )
                                }
                            }
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = SuperficieBlanca
                    )
                )
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

                // Pestaña 2: Inventario en vivo y Recarga en Planta (Camión / Camioneta)
                composable(AppDestinations.CAMION.ruta) {
                    InventarioCamionScreen(
                        viewModel = inventarioViewModel,
                        onCambiarVehiculoClick = {
                            appPreferences.guardarSesionIniciada(false)
                            vehiculoSeleccionado = null
                        }
                    )
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

                // Destino 5: Panel de Rolando (Auditoría y Utilidades)
                composable(AppDestinations.PANEL_ROLANDO.ruta) {
                    PanelRolandoScreen(viewModel = registroDiaViewModel)
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
}

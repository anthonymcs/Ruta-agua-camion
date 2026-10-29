package com.rutaagua.camion.ui.navigation

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Logout
import androidx.compose.material.icons.filled.Analytics
import androidx.compose.material.icons.filled.BarChart
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.LocalShipping
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.People
import androidx.compose.material.icons.filled.PointOfSale
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
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
import androidx.compose.material3.rememberDrawerState
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
import androidx.compose.ui.text.style.TextOverflow
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
import com.rutaagua.camion.ui.components.ClayBadge
import com.rutaagua.camion.ui.components.ClayButton
import com.rutaagua.camion.ui.components.ClayCard
import com.rutaagua.camion.ui.components.VehiculoThumbnail
import com.rutaagua.camion.ui.components.obtenerClayThemePorVehiculo
import com.rutaagua.camion.ui.screens.SeleccionVehiculoScreen
import com.rutaagua.camion.ui.screens.calendario.CalendarioRutaScreen
import com.rutaagua.camion.ui.screens.clientes.ClientesScreen
import com.rutaagua.camion.ui.screens.clientes.ClientesViewModel
import com.rutaagua.camion.ui.screens.configuracion.ConfiguracionAjustesDialog
import com.rutaagua.camion.ui.screens.inventario.InventarioCamionScreen
import com.rutaagua.camion.ui.screens.inventario.InventarioCamionViewModel
import com.rutaagua.camion.ui.screens.registro.RegistroDiaScreen
import com.rutaagua.camion.ui.screens.registro.RegistroDiaViewModel
import com.rutaagua.camion.ui.screens.reportes.ReportesCamionetaScreen
import com.rutaagua.camion.ui.screens.rolando.PanelRolandoScreen
import com.rutaagua.camion.ui.theme.AzulCorporativo
import com.rutaagua.camion.ui.theme.BordeSutil
import com.rutaagua.camion.ui.theme.RojoAlerta
import com.rutaagua.camion.ui.theme.RojoAlertaFondo
import com.rutaagua.camion.ui.theme.TextoOscuro
import com.rutaagua.camion.ui.theme.TextoSecundario
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

    val clayTheme = obtenerClayThemePorVehiculo(vehiculoActivo)

    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
    val coroutineScope = rememberCoroutineScope()

    val navController = rememberNavController()
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route ?: AppDestinations.REGISTRO.ruta

    val factory = RutaAguaViewModelFactory(repository, appPreferences)

    val registroDiaViewModel: RegistroDiaViewModel = viewModel(factory = factory)
    val inventarioViewModel: InventarioCamionViewModel = viewModel(factory = factory)
    val clientesViewModel: ClientesViewModel = viewModel(factory = factory)

    // Autogeneración y autoprueba automática del respaldo mensual al iniciar la aplicación
    LaunchedEffect(Unit) {
        com.rutaagua.camion.data.backup.BackupManager.verificarYEjecutarRespaldoMensualAutomatico(
            context = context,
            database = database,
            forzarAhora = false
        )
    }

    // Si aún no ha iniciado sesión, mostramos la pantalla de Login (sin tuerca de ajustes)
    if (vehiculoSeleccionado == null) {
        SeleccionVehiculoScreen(
            repository = repository,
            onSeleccionarPerfil = { tipo, rol ->
                appPreferences.guardarVehiculoActivo(tipo)
                appPreferences.guardarRolActivo(rol)
                registroDiaViewModel.cambiarFiltroSegunVehiculo(tipo)
                vehiculoSeleccionado = tipo.name
            }
        )
        return
    }

    ModalNavigationDrawer(
        drawerState = drawerState,
        gesturesEnabled = true,
        drawerContent = {
            ModalDrawerSheet(
                modifier = Modifier.width(300.dp),
                drawerContainerColor = clayTheme.canvasColor
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .statusBarsPadding()
                        .verticalScroll(rememberScrollState())
                ) {
                    // Header del Menú Lateral
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(14.dp)
                    ) {
                        ClayCard(
                            modifier = Modifier.fillMaxWidth(),
                            containerColor = clayTheme.cardColor,
                            cornerRadius = 22.dp,
                            elevation = 5.dp,
                            contentPadding = PaddingValues(16.dp)
                        ) {
                            Column(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(84.dp)
                                        .background(clayTheme.accentBgColor, RoundedCornerShape(20.dp))
                                        .padding(10.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    if (vehiculoActivo == TipoVehiculo.CAMIONETA) {
                                        CamionetaIllustration(modifier = Modifier.fillMaxSize())
                                    } else {
                                        CamionIllustration(modifier = Modifier.fillMaxSize())
                                    }
                                }

                                Spacer(modifier = Modifier.height(10.dp))

                                ClayBadge(
                                    text = if (rolActivo == RolUsuario.ADMIN_ROLANDO) "ADMINISTRACIÓN" else clayTheme.badgeLabel,
                                    backgroundColor = clayTheme.accentBgColor,
                                    textColor = clayTheme.primaryDarkColor
                                )

                                Spacer(modifier = Modifier.height(4.dp))

                                Text(
                                    text = clayTheme.vehicleTitle,
                                    color = TextoOscuro,
                                    fontWeight = FontWeight.Black,
                                    fontSize = 18.sp,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )

                                Text(
                                    text = clayTheme.capacityLabel,
                                    color = TextoSecundario,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }
                        }
                    }

                    // Opciones de navegación
                    NavigationDrawerItem(
                        icon = { Icon(Icons.Default.PointOfSale, contentDescription = null) },
                        label = { Text("Ventas y Caja", fontWeight = FontWeight.Bold, maxLines = 1, overflow = TextOverflow.Ellipsis) },
                        selected = currentRoute == AppDestinations.REGISTRO.ruta,
                        onClick = {
                            coroutineScope.launch { drawerState.close() }
                            navController.navigate(AppDestinations.REGISTRO.ruta) {
                                popUpTo(navController.graph.findStartDestination().id) { saveState = true }
                                launchSingleTop = true
                                restoreState = true
                            }
                        },
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 2.dp),
                        colors = NavigationDrawerItemDefaults.colors(
                            selectedContainerColor = clayTheme.accentBgColor,
                            selectedIconColor = clayTheme.primaryDarkColor,
                            selectedTextColor = clayTheme.primaryDarkColor
                        )
                    )

                    NavigationDrawerItem(
                        icon = { Icon(Icons.Default.LocalShipping, contentDescription = null) },
                        label = { Text("Carga e Inventario", fontWeight = FontWeight.Bold, maxLines = 1, overflow = TextOverflow.Ellipsis) },
                        selected = currentRoute == AppDestinations.CAMION.ruta,
                        onClick = {
                            coroutineScope.launch { drawerState.close() }
                            navController.navigate(AppDestinations.CAMION.ruta) {
                                popUpTo(navController.graph.findStartDestination().id) { saveState = true }
                                launchSingleTop = true
                                restoreState = true
                            }
                        },
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 2.dp),
                        colors = NavigationDrawerItemDefaults.colors(
                            selectedContainerColor = clayTheme.accentBgColor,
                            selectedIconColor = clayTheme.primaryDarkColor,
                            selectedTextColor = clayTheme.primaryDarkColor
                        )
                    )

                    NavigationDrawerItem(
                        icon = { Icon(Icons.Default.People, contentDescription = null) },
                        label = { Text("Clientes", fontWeight = FontWeight.Bold, maxLines = 1, overflow = TextOverflow.Ellipsis) },
                        selected = currentRoute == AppDestinations.CLIENTES.ruta,
                        onClick = {
                            coroutineScope.launch { drawerState.close() }
                            navController.navigate(AppDestinations.CLIENTES.ruta) {
                                popUpTo(navController.graph.findStartDestination().id) { saveState = true }
                                launchSingleTop = true
                                restoreState = true
                            }
                        },
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 2.dp),
                        colors = NavigationDrawerItemDefaults.colors(
                            selectedContainerColor = clayTheme.accentBgColor,
                            selectedIconColor = clayTheme.primaryDarkColor,
                            selectedTextColor = clayTheme.primaryDarkColor
                        )
                    )

                    NavigationDrawerItem(
                        icon = { Icon(Icons.Default.CalendarMonth, contentDescription = null) },
                        label = { Text("Ruta y Calendario", fontWeight = FontWeight.Bold, maxLines = 1, overflow = TextOverflow.Ellipsis) },
                        selected = currentRoute == AppDestinations.CALENDARIO.ruta,
                        onClick = {
                            coroutineScope.launch { drawerState.close() }
                            navController.navigate(AppDestinations.CALENDARIO.ruta) {
                                popUpTo(navController.graph.findStartDestination().id) { saveState = true }
                                launchSingleTop = true
                                restoreState = true
                            }
                        },
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 2.dp),
                        colors = NavigationDrawerItemDefaults.colors(
                            selectedContainerColor = clayTheme.accentBgColor,
                            selectedIconColor = clayTheme.primaryDarkColor,
                            selectedTextColor = clayTheme.primaryDarkColor
                        )
                    )

                    NavigationDrawerItem(
                        icon = { Icon(Icons.Default.BarChart, contentDescription = null) },
                        label = { Text("Reportes (Día / Semanal / Mensual)", fontWeight = FontWeight.Black, maxLines = 1, overflow = TextOverflow.Ellipsis) },
                        selected = currentRoute == "reportes_camioneta",
                        onClick = {
                            coroutineScope.launch { drawerState.close() }
                            navController.navigate("reportes_camioneta") {
                                popUpTo(navController.graph.findStartDestination().id) { saveState = true }
                                launchSingleTop = true
                                restoreState = true
                            }
                        },
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 2.dp),
                        colors = NavigationDrawerItemDefaults.colors(
                            selectedContainerColor = clayTheme.accentBgColor,
                            selectedIconColor = clayTheme.primaryDarkColor,
                            selectedTextColor = clayTheme.primaryDarkColor
                        )
                    )

                    if (rolActivo == RolUsuario.ADMIN_ROLANDO) {
                        NavigationDrawerItem(
                            icon = { Icon(Icons.Default.Analytics, contentDescription = null, tint = AzulCorporativo) },
                            label = { Text("Panel Rolando", fontWeight = FontWeight.Black, color = AzulCorporativo, maxLines = 1, overflow = TextOverflow.Ellipsis) },
                            selected = currentRoute == AppDestinations.PANEL_ROLANDO.ruta,
                            onClick = {
                                coroutineScope.launch { drawerState.close() }
                                navController.navigate(AppDestinations.PANEL_ROLANDO.ruta) {
                                    popUpTo(navController.graph.findStartDestination().id) { saveState = true }
                                    launchSingleTop = true
                                    restoreState = true
                                }
                            },
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 2.dp),
                            colors = NavigationDrawerItemDefaults.colors(
                                selectedContainerColor = AzulCorporativo.copy(alpha = 0.15f),
                                selectedIconColor = AzulCorporativo,
                                selectedTextColor = AzulCorporativo
                            )
                        )
                    }

                    HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp, vertical = 10.dp), color = BordeSutil)

                    // Opción de Configuraciones en Barra Lateral
                    NavigationDrawerItem(
                        icon = { Icon(Icons.Default.Settings, contentDescription = "Configuraciones", tint = clayTheme.primaryDarkColor) },
                        label = {
                            Text(
                                text = "Configuraciones",
                                fontWeight = FontWeight.Black,
                                color = TextoOscuro,
                                maxLines = 1,
                                softWrap = false,
                                overflow = TextOverflow.Ellipsis
                            )
                        },
                        selected = false,
                        onClick = {
                            coroutineScope.launch { drawerState.close() }
                            mostrarModalAjustes = true
                        },
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 2.dp)
                    )

                    // Opción de Salir / Cerrar Sesión en Barra Lateral
                    NavigationDrawerItem(
                        icon = {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.Logout,
                                contentDescription = "Salir",
                                tint = RojoAlerta
                            )
                        },
                        label = {
                            Text(
                                text = "Cerrar Sesión / Salir",
                                fontWeight = FontWeight.Black,
                                color = RojoAlerta,
                                maxLines = 1,
                                softWrap = false,
                                overflow = TextOverflow.Ellipsis
                            )
                        },
                        selected = false,
                        onClick = {
                            coroutineScope.launch { drawerState.close() }
                            appPreferences.guardarSesionIniciada(false)
                            vehiculoSeleccionado = null
                        },
                        modifier = Modifier
                            .padding(horizontal = 12.dp, vertical = 4.dp)
                            .navigationBarsPadding(),
                        colors = NavigationDrawerItemDefaults.colors(
                            unselectedContainerColor = RojoAlertaFondo.copy(alpha = 0.55f)
                        )
                    )
                }
            }
        }
    ) {
        val tituloPantallaActual = when (currentRoute) {
            AppDestinations.REGISTRO.ruta -> "Ventas y Caja"
            AppDestinations.CAMION.ruta -> "Carga e Inventario"
            AppDestinations.CLIENTES.ruta -> "Clientes"
            AppDestinations.CALENDARIO.ruta -> "Ruta y Calendario"
            "reportes_camioneta" -> "Reportes"
            AppDestinations.PANEL_ROLANDO.ruta -> "Panel Rolando"
            else -> "Ventas y Caja"
        }

        Scaffold(
            modifier = modifier.fillMaxSize(),
            containerColor = clayTheme.canvasColor,
            topBar = {
                // Top Bar limpia: solo el botón de menú lateral e identificación de unidad y pantalla actual
                Surface(
                    color = clayTheme.canvasColor,
                    shadowElevation = 0.dp,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .statusBarsPadding()
                            .padding(horizontal = 16.dp, vertical = 10.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.weight(1f)
                        ) {
                            // Botón para abrir la barra lateral izquierda
                            ClayCard(
                                modifier = Modifier.size(44.dp),
                                containerColor = clayTheme.cardColor,
                                cornerRadius = 14.dp,
                                elevation = 3.dp,
                                contentPadding = PaddingValues(0.dp),
                                onClick = { coroutineScope.launch { drawerState.open() } }
                            ) {
                                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                                    Icon(
                                        imageVector = Icons.Default.Menu,
                                        contentDescription = "Menú",
                                        tint = TextoOscuro,
                                        modifier = Modifier.size(23.dp)
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.width(10.dp))

                            VehiculoThumbnail(
                                tipo = vehiculoActivo,
                                size = 38.dp
                            )

                            Spacer(modifier = Modifier.width(10.dp))

                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = clayTheme.vehicleTitle,
                                    fontWeight = FontWeight.Black,
                                    fontSize = 16.sp,
                                    color = TextoOscuro,
                                    maxLines = 1,
                                    softWrap = false,
                                    overflow = TextOverflow.Ellipsis
                                )
                                Text(
                                    text = if (rolActivo == RolUsuario.ADMIN_ROLANDO) "Modo Rolando" else clayTheme.capacityLabel,
                                    style = MaterialTheme.typography.labelSmall,
                                    color = clayTheme.primaryColor,
                                    fontWeight = FontWeight.Bold,
                                    maxLines = 1,
                                    softWrap = false,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }
                        }

                        Spacer(modifier = Modifier.width(8.dp))

                        ClayBadge(
                            text = tituloPantallaActual,
                            backgroundColor = clayTheme.accentBgColor,
                            textColor = clayTheme.primaryDarkColor
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
                composable(AppDestinations.REGISTRO.ruta) {
                    RegistroDiaScreen(viewModel = registroDiaViewModel)
                }

                composable(AppDestinations.CAMION.ruta) {
                    InventarioCamionScreen(
                        viewModel = inventarioViewModel,
                        onCambiarVehiculoClick = {
                            appPreferences.guardarSesionIniciada(false)
                            vehiculoSeleccionado = null
                        }
                    )
                }

                composable(AppDestinations.CLIENTES.ruta) {
                    ClientesScreen(viewModel = clientesViewModel)
                }

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

                composable("reportes_camioneta") {
                    ReportesCamionetaScreen(viewModel = registroDiaViewModel)
                }

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

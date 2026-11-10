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
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.LocalShipping
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.People
import androidx.compose.material.icons.filled.PointOfSale
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.SystemUpdate
import androidx.compose.material3.AlertDialog
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
import com.google.firebase.auth.FirebaseAuth
import com.rutaagua.camion.data.local.RutaAguaDatabase
import com.rutaagua.camion.data.local.model.TipoVehiculo
import com.rutaagua.camion.data.local.prefs.AppPreferences
import com.rutaagua.camion.data.local.prefs.RolUsuario
import com.rutaagua.camion.data.local.prefs.resolverPerfilPorEmail
import com.rutaagua.camion.data.remote.InfoActualizacionOta
import com.rutaagua.camion.data.remote.OTAUpdateChecker
import com.rutaagua.camion.data.repository.RutaAguaRepository
import com.rutaagua.camion.ui.auth.LoginScreen
import com.rutaagua.camion.ui.components.CamionIllustration
import com.rutaagua.camion.ui.components.CamionetaIllustration
import com.rutaagua.camion.ui.components.ClayBadge
import com.rutaagua.camion.ui.components.ClayButton
import com.rutaagua.camion.ui.components.ClayCard
import com.rutaagua.camion.ui.components.VehiculoThumbnail
import com.rutaagua.camion.ui.components.obtenerClayThemePorVehiculo
import com.rutaagua.camion.ui.screens.calendario.CalendarioRutaScreen
import com.rutaagua.camion.ui.screens.clientes.ClientesScreen
import com.rutaagua.camion.ui.screens.clientes.ClientesViewModel
import com.rutaagua.camion.ui.screens.configuracion.ConfiguracionAjustesDialog
import com.rutaagua.camion.ui.screens.inventario.DialogoCargaInicialObligatoria
import com.rutaagua.camion.ui.screens.inventario.InventarioCamionScreen
import com.rutaagua.camion.ui.screens.inventario.InventarioCamionViewModel
import com.rutaagua.camion.ui.screens.registro.RegistroDiaScreen
import com.rutaagua.camion.ui.screens.registro.RegistroDiaViewModel
import com.rutaagua.camion.ui.screens.reportes.ReportesCamionetaScreen
import com.rutaagua.camion.ui.screens.rolando.PanelRolandoScreen
import com.rutaagua.camion.ui.theme.AzulCorporativo
import com.rutaagua.camion.ui.theme.BordeSutil
import com.rutaagua.camion.ui.theme.FondoGrisClaro
import com.rutaagua.camion.ui.theme.PichinchaCanvas
import com.rutaagua.camion.ui.theme.RojoAlerta
import com.rutaagua.camion.ui.theme.RojoAlertaFondo
import com.rutaagua.camion.ui.theme.TextoOscuro
import com.rutaagua.camion.ui.theme.TextoSecundario
import com.rutaagua.camion.ui.theme.VerdeEfectivo
import com.rutaagua.camion.ui.theme.VerdeEfectivoFondo
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

    val auth = remember { FirebaseAuth.getInstance() }
    val usuarioInicial = auth.currentUser
    val startDestination = remember {
        if (usuarioInicial != null) AppDestinations.REGISTRO.ruta else AppDestinations.LOGIN.ruta
    }

    var mostrarModalAjustes by rememberSaveable { mutableStateOf(false) }
    var mostrarDialogoConfirmarLogout by rememberSaveable { mutableStateOf(false) }
    var infoActualizacionOta by remember { mutableStateOf<InfoActualizacionOta?>(null) }
    var mostrarDialogoActualizacion by rememberSaveable { mutableStateOf(false) }
    var cargaInicialYaConfirmada by rememberSaveable { mutableStateOf(false) }

    val clayTheme = obtenerClayThemePorVehiculo(vehiculoActivo)

    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
    val coroutineScope = rememberCoroutineScope()

    val navController = rememberNavController()
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route ?: startDestination

    val factory = RutaAguaViewModelFactory(repository, appPreferences)

    val registroDiaViewModel: RegistroDiaViewModel = viewModel(factory = factory)
    val inventarioViewModel: InventarioCamionViewModel = viewModel(factory = factory)
    val clientesViewModel: ClientesViewModel = viewModel(factory = factory)

    val inventarioActual by inventarioViewModel.inventarioState.collectAsState()

    // Si el vehículo ya tiene carga registrada (de días anteriores o de una recarga previa), marcar como confirmada
    LaunchedEffect(inventarioActual) {
        val inv = inventarioActual
        if (inv != null && (inv.totalBidonesLlenos() > 0 || inv.galonesLlenos > 0 || inv.pacasLlenas > 0 || inv.minisLlenas > 0)) {
            cargaInicialYaConfirmada = true
        }
    }

    // Sincronización automática de rol y vehículo si hay sesión iniciada al arrancar la app
    LaunchedEffect(Unit) {
        val user = auth.currentUser
        if (user != null) {
            val (tipo, rol) = resolverPerfilPorEmail(user.email)
            appPreferences.guardarVehiculoActivo(tipo)
            appPreferences.guardarRolActivo(rol)
            appPreferences.guardarSesionIniciada(true)
            registroDiaViewModel.cambiarFiltroSegunVehiculo(tipo)
        } else {
            appPreferences.guardarSesionIniciada(false)
        }
    }

    // Autogeneración y autoprueba automática del respaldo mensual al iniciar la aplicación
    LaunchedEffect(Unit) {
        com.rutaagua.camion.data.backup.BackupManager.verificarYEjecutarRespaldoMensualAutomatico(
            context = context,
            database = database,
            forzarAhora = false
        )
    }

    // Comprobación automática de actualizaciones remotas OTA desde GitHub Releases
    LaunchedEffect(Unit) {
        try {
            val result = OTAUpdateChecker.verificarNuevaVersion(context)
            result.onSuccess { info ->
                if (info.hayNuevaVersion && info.urlDescargaApk.isNotBlank()) {
                    infoActualizacionOta = info
                    mostrarDialogoActualizacion = true
                }
            }
        } catch (_: Exception) {}
    }

    ModalNavigationDrawer(
        drawerState = drawerState,
        gesturesEnabled = currentRoute != AppDestinations.LOGIN.ruta,
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
                        label = { Text("Reportes de Bidones", fontWeight = FontWeight.Bold, maxLines = 1, overflow = TextOverflow.Ellipsis) },
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
                            label = { Text("Contabilidad (Rolando)", fontWeight = FontWeight.Black, color = AzulCorporativo, maxLines = 1, overflow = TextOverflow.Ellipsis) },
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
                                contentDescription = "Cerrar Sesión",
                                tint = RojoAlerta
                            )
                        },
                        label = {
                            Text(
                                text = "Cerrar Sesión",
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
                            mostrarDialogoConfirmarLogout = true
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
            AppDestinations.LOGIN.ruta -> "Iniciar Sesión"
            AppDestinations.REGISTRO.ruta -> "Ventas y Caja"
            AppDestinations.CAMION.ruta -> "Carga e Inventario"
            AppDestinations.CLIENTES.ruta -> "Clientes"
            AppDestinations.CALENDARIO.ruta -> "Ruta y Calendario"
            "reportes_camioneta" -> "Reportes de Bidones"
            AppDestinations.PANEL_ROLANDO.ruta -> "Contabilidad (Rolando)"
            else -> "Ventas y Caja"
        }

        Scaffold(
            modifier = modifier.fillMaxSize(),
            containerColor = if (currentRoute == AppDestinations.LOGIN.ruta) PichinchaCanvas else clayTheme.canvasColor,
            topBar = {
                if (currentRoute != AppDestinations.LOGIN.ruta) {
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
            }
        ) { innerPadding ->
            val paddingContent = if (currentRoute == AppDestinations.LOGIN.ruta) PaddingValues(0.dp) else innerPadding
            NavHost(
                navController = navController,
                startDestination = startDestination,
                modifier = Modifier.padding(paddingContent)
            ) {
                composable(AppDestinations.LOGIN.ruta) {
                    LoginScreen(
                        onLoginExitoso = { tipo, rol ->
                            cargaInicialYaConfirmada = false
                            appPreferences.guardarVehiculoActivo(tipo)
                            appPreferences.guardarRolActivo(rol)
                            appPreferences.guardarSesionIniciada(true)
                            registroDiaViewModel.cambiarFiltroSegunVehiculo(tipo)
                            navController.navigate(AppDestinations.REGISTRO.ruta) {
                                popUpTo(AppDestinations.LOGIN.ruta) { inclusive = true }
                                launchSingleTop = true
                            }
                        }
                    )
                }

                composable(AppDestinations.REGISTRO.ruta) {
                    RegistroDiaScreen(viewModel = registroDiaViewModel)
                }

                composable(AppDestinations.CAMION.ruta) {
                    InventarioCamionScreen(
                        viewModel = inventarioViewModel,
                        onCambiarVehiculoClick = {
                            mostrarDialogoConfirmarLogout = true
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

            // Diálogo obligatorio de registro de carga inicial si la unidad arranca en 0
            val debePedirCargaInicial = currentRoute != AppDestinations.LOGIN.ruta &&
                rolActivo != RolUsuario.ADMIN_ROLANDO &&
                !cargaInicialYaConfirmada &&
                inventarioActual != null &&
                (inventarioActual?.let { it.totalBidonesLlenos() == 0 && it.galonesLlenos == 0 && it.pacasLlenas == 0 && it.minisLlenas == 0 } == true)

            if (debePedirCargaInicial) {
                DialogoCargaInicialObligatoria(
                    inventario = inventarioActual,
                    clayTheme = clayTheme,
                    onConfirmarCargaInicial = { meta, trans, azul, marc, galones, pacas, minis ->
                        cargaInicialYaConfirmada = true
                        inventarioViewModel.cambiarCapacidadTope(meta)
                        inventarioViewModel.establecerCargaActual(
                            transparentes = trans,
                            azules = azul,
                            marcados = marc,
                            vacios = 0,
                            galones = galones,
                            pacas = pacas,
                            minis = minis
                        )
                    }
                )
            }

            if (mostrarDialogoConfirmarLogout) {
                AlertDialog(
                    onDismissRequest = { mostrarDialogoConfirmarLogout = false },
                    shape = RoundedCornerShape(20.dp),
                    containerColor = Color.White,
                    icon = {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.Logout,
                            contentDescription = null,
                            tint = RojoAlerta,
                            modifier = Modifier.size(28.dp)
                        )
                    },
                    title = {
                        Text(
                            text = "Cerrar Sesión",
                            fontWeight = FontWeight.Black,
                            color = TextoOscuro,
                            fontSize = 18.sp
                        )
                    },
                    text = {
                        Text(
                            text = "¿Estás seguro de que deseas cerrar la sesión actual de ${auth.currentUser?.email ?: "este dispositivo"}?",
                            color = TextoSecundario,
                            fontSize = 14.sp
                        )
                    },
                    confirmButton = {
                        ClayButton(
                            onClick = {
                                mostrarDialogoConfirmarLogout = false
                                coroutineScope.launch { drawerState.close() }
                                cargaInicialYaConfirmada = false
                                auth.signOut()
                                appPreferences.guardarSesionIniciada(false)
                                navController.navigate(AppDestinations.LOGIN.ruta) {
                                    popUpTo(0) { inclusive = true }
                                    launchSingleTop = true
                                }
                            },
                            containerColor = RojoAlerta,
                            contentColor = Color.White,
                            height = 42.dp,
                            cornerRadius = 12.dp
                        ) {
                            Text("Sí, Cerrar Sesión", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                        }
                    },
                    dismissButton = {
                        ClayButton(
                            onClick = { mostrarDialogoConfirmarLogout = false },
                            containerColor = FondoGrisClaro,
                            contentColor = TextoOscuro,
                            height = 42.dp,
                            cornerRadius = 12.dp
                        ) {
                            Text("Cancelar", fontWeight = FontWeight.SemiBold, fontSize = 13.sp)
                        }
                    }
                )
            }

            // Diálogo Interactivo Estilo Duolingo de Actualización OTA Automática
            if (mostrarDialogoActualizacion && infoActualizacionOta != null) {
                val info = infoActualizacionOta!!
                AlertDialog(
                    onDismissRequest = { mostrarDialogoActualizacion = false },
                    icon = {
                        Box(
                            modifier = Modifier
                                .size(54.dp)
                                .background(VerdeEfectivoFondo, RoundedCornerShape(16.dp)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.SystemUpdate,
                                contentDescription = null,
                                tint = VerdeEfectivo,
                                modifier = Modifier.size(28.dp)
                            )
                        }
                    },
                    title = {
                        Text(
                            text = "¡Nueva Versión Disponible!",
                            fontWeight = FontWeight.Black,
                            color = TextoOscuro,
                            fontSize = 18.sp
                        )
                    },
                    text = {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .verticalScroll(rememberScrollState()),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Text(
                                text = "Atenea v${info.versionNameRemoto} lista para instalar.",
                                color = VerdeEfectivo,
                                fontWeight = FontWeight.Bold,
                                fontSize = 14.sp
                            )
                            if (info.notasVersion.isNotBlank()) {
                                Surface(
                                    color = FondoGrisClaro,
                                    shape = RoundedCornerShape(12.dp),
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Text(
                                        text = info.notasVersion,
                                        color = TextoSecundario,
                                        fontSize = 12.sp,
                                        modifier = Modifier.padding(10.dp)
                                    )
                                }
                            }
                            Text(
                                text = "Se descargará mediante DownloadManager y se abrirá el instalador oficial automáticamente.",
                                color = TextoSecundario,
                                fontSize = 11.sp
                            )
                        }
                    },
                    confirmButton = {
                        ClayButton(
                            onClick = {
                                mostrarDialogoActualizacion = false
                                OTAUpdateChecker.descargarEInstalarApk(
                                    context = context,
                                    urlApk = info.urlDescargaApk,
                                    versionName = info.versionNameRemoto
                                )
                            },
                            containerColor = VerdeEfectivo,
                            contentColor = Color.White,
                            height = 46.dp,
                            cornerRadius = 14.dp
                        ) {
                            Icon(
                                imageVector = Icons.Default.Download,
                                contentDescription = null,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Actualizar Ahora", fontWeight = FontWeight.Black, fontSize = 13.sp)
                        }
                    },
                    dismissButton = {
                        ClayButton(
                            onClick = { mostrarDialogoActualizacion = false },
                            containerColor = FondoGrisClaro,
                            contentColor = TextoOscuro,
                            height = 46.dp,
                            cornerRadius = 14.dp
                        ) {
                            Text("Más tarde", fontWeight = FontWeight.SemiBold, fontSize = 13.sp)
                        }
                    }
                )
            }
        }
    }
}

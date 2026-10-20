package com.rutaagua.camion.ui.navigation

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.rutaagua.camion.data.repository.RutaAguaRepository
import com.rutaagua.camion.ui.screens.calendario.CalendarioRutaPlaceholderScreen
import com.rutaagua.camion.ui.screens.clientes.ClientesPlaceholderScreen
import com.rutaagua.camion.ui.screens.inventario.InventarioCamionScreen
import com.rutaagua.camion.ui.screens.inventario.InventarioCamionViewModel
import com.rutaagua.camion.ui.screens.registro.RegistroVentasPlaceholderScreen
import com.rutaagua.camion.ui.theme.AguaPrimary
import com.rutaagua.camion.ui.theme.AguaPrimaryLight
import com.rutaagua.camion.ui.theme.TextoOscuro
import com.rutaagua.camion.ui.theme.TextoSecundario

class InventarioViewModelFactory(
    private val repository: RutaAguaRepository
) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(InventarioCamionViewModel::class.java)) {
            return InventarioCamionViewModel(repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class: ${modelClass.name}")
    }
}

/**
 * Scaffold principal con barra de navegación inferior de 4 pestañas y NavHost.
 */
@Composable
fun RutaAguaNavHost(
    repository: RutaAguaRepository,
    modifier: Modifier = Modifier
) {
    val navController = rememberNavController()
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route ?: AppDestinations.CAMION.ruta

    // Inicialización del ViewModel de Inventario
    val inventarioViewModel: InventarioCamionViewModel = viewModel(
        factory = InventarioViewModelFactory(repository)
    )

    Scaffold(
        modifier = modifier.fillMaxSize(),
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
            startDestination = AppDestinations.CAMION.ruta,
            modifier = Modifier.padding(innerPadding)
        ) {
            // Pestaña 1: Registro de Ventas y Arqueo
            composable(AppDestinations.REGISTRO.ruta) {
                RegistroVentasPlaceholderScreen()
            }

            // Pestaña 2: Camión (Inventario en vivo y Recarga en Planta)
            composable(AppDestinations.CAMION.ruta) {
                InventarioCamionScreen(viewModel = inventarioViewModel)
            }

            // Pestaña 3: Directorio de Clientes
            composable(AppDestinations.CLIENTES.ruta) {
                ClientesPlaceholderScreen()
            }

            // Pestaña 4: Calendario de Ruta Semanal
            composable(AppDestinations.CALENDARIO.ruta) {
                CalendarioRutaPlaceholderScreen()
            }
        }
    }
}

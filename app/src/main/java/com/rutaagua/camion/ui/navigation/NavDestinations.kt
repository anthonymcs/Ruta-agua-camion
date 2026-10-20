package com.rutaagua.camion.ui.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.LocalShipping
import androidx.compose.material.icons.filled.People
import androidx.compose.material.icons.filled.PointOfSale
import androidx.compose.ui.graphics.vector.ImageVector

/**
 * Destinos principales de navegación de la barra inferior.
 */
enum class AppDestinations(
    val ruta: String,
    val titulo: String,
    val icono: ImageVector
) {
    REGISTRO(
        ruta = "registro",
        titulo = "Registro / Hoy",
        icono = Icons.Default.PointOfSale
    ),
    CAMION(
        ruta = "camion",
        titulo = "Camión",
        icono = Icons.Default.LocalShipping
    ),
    CLIENTES(
        ruta = "clientes",
        titulo = "Clientes",
        icono = Icons.Default.People
    ),
    CALENDARIO(
        ruta = "calendario",
        titulo = "Calendario",
        icono = Icons.Default.CalendarMonth
    )
}

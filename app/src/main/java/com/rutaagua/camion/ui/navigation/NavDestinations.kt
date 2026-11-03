package com.rutaagua.camion.ui.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Login
import androidx.compose.material.icons.filled.Analytics
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.LocalShipping
import androidx.compose.material.icons.filled.People
import androidx.compose.material.icons.filled.PointOfSale
import androidx.compose.ui.graphics.vector.ImageVector

/**
 * Destinos principales de navegación de la aplicación.
 */
enum class AppDestinations(
    val ruta: String,
    val titulo: String,
    val icono: ImageVector
) {
    LOGIN(
        ruta = "login",
        titulo = "Iniciar Sesión",
        icono = Icons.AutoMirrored.Filled.Login
    ),
    REGISTRO(
        ruta = "registro",
        titulo = "Caja / Hoy",
        icono = Icons.Default.PointOfSale
    ),
    CAMION(
        ruta = "camion",
        titulo = "Inventario",
        icono = Icons.Default.LocalShipping
    ),
    CLIENTES(
        ruta = "clientes",
        titulo = "Clientes",
        icono = Icons.Default.People
    ),
    CALENDARIO(
        ruta = "calendario",
        titulo = "Ruta",
        icono = Icons.Default.CalendarMonth
    ),
    PANEL_ROLANDO(
        ruta = "panel_rolando",
        titulo = "Panel Rolando",
        icono = Icons.Default.Analytics
    )
}

package com.rutaagua.camion.data.local.model

/**
 * Estadísticas de proporción y desglose de productos vendidos en una fecha.
 */
data class EstadisticasVentasTipo(
    val transparentesVendidos: Int = 0,
    val azulesVendidos: Int = 0,
    val marcadosVendidos: Int = 0,
    val totalBidonesVendidos: Int = 0,
    val galonesVendidos: Int = 0,
    val pacasVendidas: Int = 0,
    val minisVendidas: Int = 0,
    val envasesNuevosVendidos: Int = 0
) {
    val porcentajeTransparentes: Float
        get() = if (totalBidonesVendidos > 0) (transparentesVendidos.toFloat() / totalBidonesVendidos) * 100f else 0f

    val porcentajeAzules: Float
        get() = if (totalBidonesVendidos > 0) (azulesVendidos.toFloat() / totalBidonesVendidos) * 100f else 0f

    val porcentajeMarcados: Float
        get() = if (totalBidonesVendidos > 0) (marcadosVendidos.toFloat() / totalBidonesVendidos) * 100f else 0f
}

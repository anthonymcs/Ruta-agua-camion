package com.rutaagua.camion.data.local.model

/**
 * Cálculo en tiempo real de faltantes para llegar al tope de carga en planta.
 */
data class RecargaCalculada(
    val capacidadTopeBidones: Int,
    val bidonesLlenosActuales: Int,
    val bidonesFaltantesParaTope: Int,
    val bidonesVaciosParaDescargar: Int,
    val galonesFaltantes: Int,
    val pacasFaltantes: Int,
    val minisFaltantes: Int
)

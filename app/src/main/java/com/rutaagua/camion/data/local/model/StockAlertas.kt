package com.rutaagua.camion.data.local.model

/**
 * Alertas de stock bajo en camión según umbrales de seguridad.
 */
data class StockAlertas(
    val transparentesBajos: Boolean = false,
    val azulesBajos: Boolean = false,
    val marcadosBajos: Boolean = false,
    val galonesBajos: Boolean = false,
    val pacasBajas: Boolean = false,
    val minisBajas: Boolean = false,
    val mensajeAlerta: String? = null
)

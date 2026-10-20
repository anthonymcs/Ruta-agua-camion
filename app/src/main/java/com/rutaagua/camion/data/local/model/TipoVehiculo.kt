package com.rutaagua.camion.data.local.model

/**
 * Representa los vehículos operativos del sistema.
 */
enum class TipoVehiculo(
    val idNombre: String,
    val etiqueta: String,
    val icono: String,
    val topeBidonesPredeterminado: Int,
    val topeGalonesPredeterminado: Int,
    val topePacasPredeterminado: Int,
    val topeMinisPredeterminado: Int
) {
    CAMION(
        idNombre = "CAMION",
        etiqueta = "Camión",
        icono = "🚚",
        topeBidonesPredeterminado = 218,
        topeGalonesPredeterminado = 90,
        topePacasPredeterminado = 6,
        topeMinisPredeterminado = 2
    ),
    CAMIONETA(
        idNombre = "CAMIONETA",
        etiqueta = "Camioneta",
        icono = "🛻",
        topeBidonesPredeterminado = 80,
        topeGalonesPredeterminado = 30,
        topePacasPredeterminado = 2,
        topeMinisPredeterminado = 1
    );

    companion object {
        fun desdeString(valor: String?): TipoVehiculo {
            return entries.find { it.name.equals(valor, ignoreCase = true) || it.idNombre.equals(valor, ignoreCase = true) } ?: CAMION
        }
    }
}

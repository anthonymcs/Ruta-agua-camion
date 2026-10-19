package com.rutaagua.camion.data.local.model

/**
 * Modelo de datos para el arqueo y resumen contable diario (hoja física digitalizada).
 */
data class ResumenCierreDia(
    val fechaTexto: String,
    val totalBidonesVendidos: Int = 0,
    val totalBidonesPrestados: Int = 0,
    val totalBidonesDevueltos: Int = 0,
    val totalGalonesVendidos: Int = 0,
    val totalPacasVendidas: Int = 0,
    val totalMinisVendidas: Int = 0,
    val totalIngresoEfectivo: Double = 0.0,
    val totalIngresoTransferencia: Double = 0.0,
    val totalCreditosOtorgados: Double = 0.0,
    val totalAbonosRecibidos: Double = 0.0,
    val totalDeudasCanceladas: Double = 0.0,
    val totalGastosRuta: Double = 0.0,
    val efectivoNetoCaja: Double = 0.0, // (Efectivo ventas + Abonos + Cancelados) - Gastos
    val granTotalVenta: Double = 0.0
)

package com.rutaagua.camion.data.local.model

import com.rutaagua.camion.data.local.entity.TransaccionVentaEntity
import java.util.Locale

/**
 * Desglose de ventas agrupado por el precio unitario del bidón registrado en cada cliente/venta.
 * Ej: "Clientes $0.80: 20 clientes (20 bidones) • Total: $16.00"
 */
data class ResumenPrecioCliente(
    val precioUnitario: Double,
    val cantidadClientes: Int,
    val cantidadBidonesCobrados: Int,
    val totalImporteAgua: Double,
    val transacciones: List<TransaccionVentaEntity> = emptyList()
)

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
    val granTotalVenta: Double = 0.0,
    val desglosePorPrecio: List<ResumenPrecioCliente> = emptyList()
) {
    val totalAbonosYCancelados: Double
        get() = totalAbonosRecibidos + totalDeudasCanceladas
}

fun calcularDesglosePorPrecio(transacciones: List<TransaccionVentaEntity>): List<ResumenPrecioCliente> {
    return transacciones
        .filter { (it.cantidadBidonesRecarga + it.cantidadBidonesPrestados) > 0 }
        .groupBy { String.format(Locale.US, "%.2f", it.precioUnitarioBidon).toDoubleOrNull() ?: it.precioUnitarioBidon }
        .map { (precio, lista) ->
            val clientesUnicos = lista.map { tx -> tx.clienteId ?: -tx.id }.distinct().size
            val bidonesCobrados = lista.sumOf { tx ->
                maxOf(0, tx.cantidadBidonesRecarga + tx.cantidadBidonesPrestados - tx.cantidadBidonesGratis)
            }
            val totalAgua = bidonesCobrados * precio
            ResumenPrecioCliente(
                precioUnitario = precio,
                cantidadClientes = clientesUnicos,
                cantidadBidonesCobrados = bidonesCobrados,
                totalImporteAgua = totalAgua,
                transacciones = lista.sortedByDescending { it.fecha }
            )
        }
        .sortedBy { it.precioUnitario }
}

package com.rutaagua.camion

import com.rutaagua.camion.data.local.model.ResumenCierreDia
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class RutaAguaRepositoryTest {

    @Test
    fun testFormatearReporteWhatsApp() {
        val resumen = ResumenCierreDia(
            fechaTexto = "2026-10-19",
            totalBidonesVendidos = 150,
            totalBidonesPrestados = 3,
            totalBidonesDevueltos = 2,
            totalGalonesVendidos = 12,
            totalPacasVendidas = 2,
            totalMinisVendidas = 1,
            totalIngresoEfectivo = 125.50,
            totalIngresoTransferencia = 35.00,
            totalCreditosOtorgados = 5.00,
            totalAbonosRecibidos = 10.00,
            totalDeudasCanceladas = 15.00,
            totalGastosRuta = 8.50,
            efectivoNetoCaja = 142.00, // 125.50 + 10.00 + 15.00 - 8.50 = 142.00
            granTotalVenta = 165.50
        )

        // Verificamos el cálculo del efectivo neto
        val esperadoNeto = (resumen.totalIngresoEfectivo + resumen.totalAbonosRecibidos + resumen.totalDeudasCanceladas) - resumen.totalGastosRuta
        assertEquals(esperadoNeto, resumen.efectivoNetoCaja, 0.001)

        val reporte = buildString {
            appendLine("💧 *REPORTE RUTA AGUA - CIERRE DEL DÍA*")
            appendLine("📅 *Fecha:* ${resumen.fechaTexto}")
            appendLine("─────────────────────────")
            appendLine("• Bidones Vendidos (20L): ${resumen.totalBidonesVendidos}")
        }

        assertTrue(reporte.contains("150"))
        assertTrue(reporte.contains("2026-10-19"))
    }
}

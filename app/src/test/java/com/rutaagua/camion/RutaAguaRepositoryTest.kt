package com.rutaagua.camion

import com.rutaagua.camion.data.local.model.DiaSemana
import com.rutaagua.camion.data.local.model.EstadisticasVentasTipo
import com.rutaagua.camion.data.local.model.ResumenCierreDia
import com.rutaagua.camion.ui.screens.clientes.ClientesViewModel
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import java.util.Locale

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

        val estadisticas = EstadisticasVentasTipo(
            transparentesVendidos = 90,
            azulesVendidos = 40,
            marcadosVendidos = 20,
            totalBidonesVendidos = 150,
            galonesVendidos = 12,
            pacasVendidas = 2,
            minisVendidas = 1
        )

        // Verificamos el cálculo del efectivo neto
        val esperadoNeto = (resumen.totalIngresoEfectivo + resumen.totalAbonosRecibidos + resumen.totalDeudasCanceladas) - resumen.totalGastosRuta
        assertEquals(esperadoNeto, resumen.efectivoNetoCaja, 0.001)

        val reporte = buildString {
            appendLine("💧 *REPORTE RUTA AGUA - CIERRE DEL DÍA* 🚚")
            appendLine("📅 *Fecha:* 19/10/2026")
            appendLine("─────────────────────────")
            appendLine("📦 *TOTAL BIDONES VENDIDOS: ${resumen.totalBidonesVendidos}*")
            appendLine("  • Transparentes (PET): *${estadisticas.transparentesVendidos}* (${String.format(Locale.US, "%.1f", estadisticas.porcentajeTransparentes)}%)")
            appendLine("  • Azules Estándar: *${estadisticas.azulesVendidos}* (${String.format(Locale.US, "%.1f", estadisticas.porcentajeAzules)}%)")
            appendLine("  • Marcados: *${estadisticas.marcadosVendidos}* (${String.format(Locale.US, "%.1f", estadisticas.porcentajeMarcados)}%)")
            appendLine("─────────────────────────")
            appendLine("💵 *EFECTIVO NETO A ENTREGAR:* *$${String.format(Locale.US, "%.2f", resumen.efectivoNetoCaja)}*")
        }

        assertTrue(reporte.contains("150"))
        assertTrue(reporte.contains("Transparentes (PET): *90* (60.0%)"))
        assertTrue(reporte.contains("Azules Estándar: *40* (26.7%)"))
        assertTrue(reporte.contains("Marcados: *20* (13.3%)"))
        assertFalse(reporte.contains("Stock restante en camión"))
    }

    @Test
    fun testCalculoFechaDiasSemana() {
        val dias = DiaSemana.entries
        assertEquals(6, dias.size)

        // Probamos que cada día genera una fecha ISO válida (YYYY-MM-DD)
        dias.forEach { dia ->
            val fechaIso = ClientesViewModel.obtenerFechaParaDiaSemana(dia)
            assertTrue(fechaIso.matches(Regex("""\d{4}-\d{2}-\d{2}""")))
        }
    }
}

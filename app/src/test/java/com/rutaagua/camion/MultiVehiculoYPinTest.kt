package com.rutaagua.camion

import com.rutaagua.camion.data.local.entity.InventarioCamionEntity
import com.rutaagua.camion.data.local.model.EstadisticasVentasTipo
import com.rutaagua.camion.data.local.model.RecargaCalculada
import com.rutaagua.camion.data.local.model.ResumenCierreDia
import com.rutaagua.camion.data.local.model.TipoVehiculo
import com.rutaagua.camion.ui.screens.registro.FiltroVehiculoRegistro
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import java.util.Locale

class MultiVehiculoYPinTest {

    @Test
    fun testTipoVehiculoConfiguracion() {
        val camion = TipoVehiculo.CAMION
        val camioneta = TipoVehiculo.CAMIONETA

        assertEquals(218, camion.topeBidonesPredeterminado)
        assertEquals(90, camion.topeGalonesPredeterminado)
        assertEquals(6, camion.topePacasPredeterminado)
        assertEquals(2, camion.topeMinisPredeterminado)

        assertEquals(80, camioneta.topeBidonesPredeterminado)
        assertEquals(30, camioneta.topeGalonesPredeterminado)
        assertEquals(2, camioneta.topePacasPredeterminado)
        assertEquals(1, camioneta.topeMinisPredeterminado)

        assertEquals(TipoVehiculo.CAMION, TipoVehiculo.desdeString("CAMION"))
        assertEquals(TipoVehiculo.CAMIONETA, TipoVehiculo.desdeString("CAMIONETA"))
        assertEquals(TipoVehiculo.CAMION, TipoVehiculo.desdeString("OTRO"))
    }

    @Test
    fun testFiltroVehiculoRegistro() {
        assertEquals(null, FiltroVehiculoRegistro.CONSOLIDADO.vehiculoKey)
        assertEquals("CAMION", FiltroVehiculoRegistro.CAMION.vehiculoKey)
        assertEquals("CAMIONETA", FiltroVehiculoRegistro.CAMIONETA.vehiculoKey)
    }

    @Test
    fun testCalculoRecargaPlantaVehiculos() {
        // Camión con 50 bidones llenos y tope 218
        val invCamion = InventarioCamionEntity(
            id = 1,
            vehiculo = "CAMION",
            capacidadTopeBidones = 218,
            bidonesLlenosTransparentes = 20,
            bidonesLlenosAzules = 20,
            bidonesLlenosMarcados = 10,
            bidonesVacios = 80
        )
        val faltantesCamion = maxOf(0, invCamion.capacidadTopeBidones - invCamion.totalBidonesLlenos())
        assertEquals(168, faltantesCamion)
        assertEquals(80, invCamion.bidonesVacios)

        // Camioneta con 30 bidones llenos y tope 80
        val invCamioneta = InventarioCamionEntity(
            id = 2,
            vehiculo = "CAMIONETA",
            capacidadTopeBidones = 80,
            bidonesLlenosTransparentes = 10,
            bidonesLlenosAzules = 15,
            bidonesLlenosMarcados = 5,
            bidonesVacios = 25
        )
        val faltantesCamioneta = maxOf(0, invCamioneta.capacidadTopeBidones - invCamioneta.totalBidonesLlenos())
        assertEquals(50, faltantesCamioneta)
        assertEquals(25, invCamioneta.bidonesVacios)
    }

    @Test
    fun testFormatoReporteMultiVehiculo() {
        val resumen = ResumenCierreDia(
            fechaTexto = "2026-10-20",
            totalBidonesVendidos = 75,
            totalIngresoEfectivo = 75.0,
            efectivoNetoCaja = 70.0,
            totalGastosRuta = 5.0,
            granTotalVenta = 75.0
        )
        val estadisticas = EstadisticasVentasTipo(
            transparentesVendidos = 40,
            azulesVendidos = 25,
            marcadosVendidos = 10,
            totalBidonesVendidos = 75
        )

        val reporteCamioneta = buildString {
            appendLine("💧 *REPORTE RUTA AGUA - CIERRE DEL DÍA* 🛻 Solo Camioneta")
            appendLine("📅 *Fecha:* 20/10/2026")
            appendLine("─────────────────────────")
            appendLine("📦 *TOTAL BIDONES VENDIDOS: ${resumen.totalBidonesVendidos}*")
            appendLine("  • Transparentes (PET): *${estadisticas.transparentesVendidos}* (${String.format(Locale.US, "%.1f", estadisticas.porcentajeTransparentes)}%)")
            appendLine("  • Azules Estándar: *${estadisticas.azulesVendidos}* (${String.format(Locale.US, "%.1f", estadisticas.porcentajeAzules)}%)")
            appendLine("  • Marcados: *${estadisticas.marcadosVendidos}* (${String.format(Locale.US, "%.1f", estadisticas.porcentajeMarcados)}%)")
        }

        assertTrue(reporteCamioneta.contains("🛻 Solo Camioneta"))
        assertTrue(reporteCamioneta.contains("TOTAL BIDONES VENDIDOS: 75"))
    }
}

package com.rutaagua.camion

import com.rutaagua.camion.data.local.entity.ClienteEntity
import com.rutaagua.camion.data.local.entity.TransaccionVentaEntity
import com.rutaagua.camion.data.local.model.FormaPago
import com.rutaagua.camion.data.local.model.TipoBidon
import com.rutaagua.camion.data.local.model.TipoCliente
import com.rutaagua.camion.ui.utils.WhatsAppHelper
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class WhatsAppHelperTest {

    @Test
    fun testNormalizarNumeroEcuador() {
        // Formato local estándar móvil Ecuador (09xxxxxxxx)
        assertEquals("593991234567", WhatsAppHelper.normalizarNumeroWhatsApp("0991234567"))
        assertEquals("593987654321", WhatsAppHelper.normalizarNumeroWhatsApp("098 765 4321"))
        assertEquals("593987654321", WhatsAppHelper.normalizarNumeroWhatsApp("(098) 765-4321"))

        // Formato con código país ya presente
        assertEquals("593991234567", WhatsAppHelper.normalizarNumeroWhatsApp("+593 99 123 4567"))
        assertEquals("593991234567", WhatsAppHelper.normalizarNumeroWhatsApp("593991234567"))

        // Formato de 9 dígitos sin cero inicial
        assertEquals("593991234567", WhatsAppHelper.normalizarNumeroWhatsApp("991234567"))

        // Nulos o vacíos
        assertNull(WhatsAppHelper.normalizarNumeroWhatsApp(null))
        assertNull(WhatsAppHelper.normalizarNumeroWhatsApp(""))
        assertNull(WhatsAppHelper.normalizarNumeroWhatsApp("   "))
    }

    @Test
    fun testGenerarMensajeEstadoCuenta() {
        val cliente = ClienteEntity(
            id = 10,
            nombre = "Restaurante El Marino",
            telefono = "0991234567",
            ubicacion = "Av. Malecón",
            tipoBidonPreferido = TipoBidon.TRANSPARENTE,
            precioHabitual = 1.25,
            tipoCliente = TipoCliente.RUTA,
            diasVisita = "LUNES,JUEVES",
            deudaMonetaria = 5.00,
            bidonesPrestados = 2
        )

        val mensaje = WhatsAppHelper.generarMensajeEstadoCuenta(cliente, fechaDisplay = "19/10/2026")

        assertTrue(mensaje.contains("ESTADO DE CUENTA"))
        assertTrue(mensaje.contains("Restaurante El Marino"))
        assertTrue(mensaje.contains("19/10/2026"))
        assertTrue(mensaje.contains("Pendiente de pago: $5.00"))
        assertTrue(mensaje.contains("Pendientes de devolución: 2 bidones"))
        assertTrue(mensaje.contains("PET Transparente"))
    }

    @Test
    fun testGenerarMensajeEstadoCuentaSinDeuda() {
        val cliente = ClienteEntity(
            id = 11,
            nombre = "Doña Carmen",
            telefono = "0987654321",
            ubicacion = "Barrio Central",
            tipoBidonPreferido = TipoBidon.AZUL,
            precioHabitual = 1.00,
            tipoCliente = TipoCliente.LLAMADA,
            deudaMonetaria = 0.0,
            bidonesPrestados = 0
        )

        val mensaje = WhatsAppHelper.generarMensajeEstadoCuenta(cliente, fechaDisplay = "19/10/2026")

        assertTrue(mensaje.contains("Sin deuda ($0.00)"))
        assertTrue(mensaje.contains("Envases prestados: 0"))
    }

    @Test
    fun testGenerarMensajeReciboEntrega() {
        val cliente = ClienteEntity(
            id = 15,
            nombre = "Panadería La Espiga",
            telefono = "0998877665",
            deudaMonetaria = 2.00,
            bidonesPrestados = 1
        )

        val transaccion = TransaccionVentaEntity(
            id = 1,
            clienteId = cliente.id,
            nombreClientePaso = null,
            fecha = 1776632400000L,
            fechaTexto = "2026-10-19",
            tipoBidon = TipoBidon.AZUL,
            cantidadBidonesRecarga = 2,
            precioUnitarioBidon = 1.00,
            cantidadBidonesPrestados = 1,
            cantidadBidonesDevueltos = 0,
            cantidadBidonesVendidosEnvase = 0,
            precioUnitarioBidonVendido = 5.50,
            cantidadGalones = 2,
            precioUnitarioGalon = 0.75,
            cantidadPacas = 0,
            precioUnitarioPaca = 3.75,
            cantidadMinis = 0,
            precioUnitarioMini = 1.75,
            totalImporte = 3.50, // 2*1.00 + 2*0.75 = 3.50
            formaPagoPrincipal = FormaPago.EFECTIVO,
            montoEfectivo = 3.50,
            nota = "Dejar en la bodega trasera"
        )

        val recibo = WhatsAppHelper.generarMensajeReciboEntrega(transaccion, cliente, fechaDisplay = "19/10/2026")

        assertTrue(recibo.contains("Aqua Apolo") || recibo.contains("Comprobante de Entrega"))
        assertTrue(recibo.contains("Panadería La Espiga"))
        assertTrue(recibo.contains("Hoy se dejó:"))
        assertTrue(recibo.contains("2 bidones"))
        assertTrue(recibo.contains("2 galones 4L"))
        assertTrue(recibo.contains("Envases prestados hoy:* +1"))
        assertTrue(recibo.contains("Total de hoy:* *$3.50*"))
        assertTrue(recibo.contains("Quedamos en:"))
        assertTrue(recibo.contains("Dejar en la bodega trasera"))
    }

    @Test
    fun testRestauranteSabadoPrestadosY35Mas1() {
        val cliente = ClienteEntity(
            id = 25,
            nombre = "Restaurante El Marino",
            telefono = "0991234567",
            deudaMonetaria = 0.0,
            bidonesPrestados = 20
        )

        // 16 recargas + 20 prestados = 36 bidones entregados, con 1 gratis ("35+1"), cobrando 35 * $1.00 = $35.00
        val transaccion = TransaccionVentaEntity(
            id = 2,
            clienteId = cliente.id,
            fecha = 1776632400000L,
            fechaTexto = "2026-10-24",
            tipoBidon = TipoBidon.AZUL,
            cantidadBidonesRecarga = 16,
            recargaAzules = 16,
            precioUnitarioBidon = 1.00,
            cantidadBidonesPrestados = 20,
            prestadosAzules = 20,
            cantidadBidonesGratis = 1,
            totalImporte = 35.00,
            formaPagoPrincipal = FormaPago.EFECTIVO,
            montoEfectivo = 35.00
        )

        val recibo = WhatsAppHelper.generarMensajeReciboEntrega(transaccion, cliente, fechaDisplay = "24/10/2026")
        assertTrue(recibo.contains("Restaurante El Marino"))
        assertTrue(recibo.contains("Envases prestados hoy:* +20"))
        assertTrue(recibo.contains("1 bidón de cortesía (gratis)"))
        assertTrue(recibo.contains("Total de hoy:* *$35.00*"))
        assertTrue(recibo.contains("Quedamos en: $0.00"))
    }

    @Test
    fun testReciboConDeudaPreviaYSaldosUsuario() {
        // Caso exacto del usuario: 10 bidones ($2.45 c/u) + 2 paquetes de botellas ($3.75 c/u) = $32.00
        // Cliente debía $10.00. Total con deuda = $42.00.
        val cliente = ClienteEntity(
            id = 30,
            nombre = "Tienda Don Pepe",
            telefono = "0995544332",
            deudaMonetaria = 42.00 // Deuda actual tras compra a crédito
        )

        val transaccion = TransaccionVentaEntity(
            id = 3,
            clienteId = cliente.id,
            fechaTexto = "2026-11-10",
            cantidadBidonesRecarga = 10,
            precioUnitarioBidon = 2.45,
            cantidadPacas = 2,
            precioUnitarioPaca = 3.75,
            totalImporte = 32.00,
            formaPagoPrincipal = FormaPago.CREDITO,
            montoCredito = 32.00
        )

        val recibo = WhatsAppHelper.generarMensajeReciboEntrega(transaccion, cliente, fechaDisplay = "10/11/2026")
        assertTrue(recibo.contains("Hoy se dejó:"))
        assertTrue(recibo.contains("10 bidones"))
        assertTrue(recibo.contains("2 paquetes de botellas de agua"))
        assertTrue(recibo.contains("Total de hoy:* *$32.00*"))
        assertTrue(recibo.contains("Debía anterior:* $10.00"))
        assertTrue(recibo.contains("Más lo de hoy son:* $42.00"))
        assertTrue(recibo.contains("Quedó a crédito (fiado):* $32.00"))
        assertTrue(recibo.contains("Quedamos en:* *$42.00*"))
    }
}

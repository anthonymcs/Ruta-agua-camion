package com.rutaagua.camion

import com.rutaagua.camion.data.local.entity.ClienteEntity
import com.rutaagua.camion.data.local.model.DiaSemana
import com.rutaagua.camion.data.local.model.TipoBidon
import com.rutaagua.camion.data.local.model.TipoCliente
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class ClientesTest {

    @Test
    fun testDiasVisitaClienteRuta() {
        val dias = listOf(DiaSemana.LUNES, DiaSemana.JUEVES)
        val diasTexto = dias.joinToString(",") { it.name }

        val cliente = ClienteEntity(
            id = 1,
            nombre = "Tienda Don Pepe",
            telefono = "0987654321",
            ubicacion = "Av. Principal #123",
            tipoCliente = TipoCliente.RUTA,
            diasVisita = diasTexto,
            tipoBidonPreferido = TipoBidon.AZUL,
            precioHabitual = 1.00,
            deudaMonetaria = 5.00,
            bidonesPrestados = 2
        )

        assertTrue(cliente.diasVisita.contains(DiaSemana.LUNES.name))
        assertTrue(cliente.diasVisita.contains(DiaSemana.JUEVES.name))
        assertFalse(cliente.diasVisita.contains(DiaSemana.MARTES.name))
        assertEquals(5.00, cliente.deudaMonetaria, 0.001)
        assertEquals(2, cliente.bidonesPrestados)
    }

    @Test
    fun testDiaSemanaConversion() {
        val diasSemana = DiaSemana.entries
        assertEquals(6, diasSemana.size)
        assertTrue(diasSemana.contains(DiaSemana.LUNES))
        assertTrue(diasSemana.contains(DiaSemana.SABADO))
    }

    @Test
    fun testAliasYNombreVisual() {
        val clienteConAlias = ClienteEntity(
            id = 10,
            nombre = "Luis Alberto Pérez Gómez",
            alias = "Don Lucho",
            telefono = "0991234567"
        )
        // En la interfaz visual se debe mostrar el alias/apodo
        assertEquals("Don Lucho", clienteConAlias.nombreVisual)
        // Pero el nombre oficial se mantiene para WhatsApp y comprobantes
        assertEquals("Luis Alberto Pérez Gómez", clienteConAlias.nombre)

        val clienteSinAlias = ClienteEntity(
            id = 11,
            nombre = "María Fernanda Castro",
            alias = "",
            telefono = "0987654321"
        )
        // Sin alias, nombreVisual debe retornar el nombre oficial
        assertEquals("María Fernanda Castro", clienteSinAlias.nombreVisual)
        assertEquals("María Fernanda Castro", clienteSinAlias.nombre)
    }
}

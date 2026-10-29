package com.rutaagua.camion.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.rutaagua.camion.data.local.model.TipoBidon
import com.rutaagua.camion.data.local.model.TipoCliente

/**
 * Entidad que representa a un cliente guardado en el sistema.
 * Soporta clientes de ruta periódica y clientes de llamada a demanda.
 */
@Entity(tableName = "clientes")
data class ClienteEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val nombre: String,
    val alias: String = "", // Apodo o alias para visualizar en la app
    val telefono: String = "",
    val ubicacion: String = "", // Dirección o referencia geográfica
    val tipoBidonPreferido: TipoBidon = TipoBidon.AZUL,
    val precioHabitual: Double = 1.00, // Precio negociado ($0.75 - $1.25)
    val tipoCliente: TipoCliente = TipoCliente.RUTA,
    val diasVisita: String = "", // Cadena separada por comas ej. "LUNES,JUEVES"
    val deudaMonetaria: Double = 0.0, // Saldo adeudado en $
    val bidonesPrestados: Int = 0, // Envases de 20L que el cliente adeuda físicamente
    val fotoReferenciaPath: String? = null, // Ruta interna de la foto de la casa/edificio
    val latitud: Double? = null, // Coordenada GPS latitud
    val longitud: Double? = null, // Coordenada GPS longitud
    val enlaceMapa: String = "", // Enlace de Google Maps / WhatsApp
    val activo: Boolean = true,
    val fechaCreacion: Long = System.currentTimeMillis()
) {
    /**
     * Nombre a mostrar en la interfaz de usuario: si tiene apodo/alias se muestra ese,
     * de lo contrario se muestra su nombre registrado.
     * El campo [nombre] se mantiene intacto para comprobantes, recibos y notas oficiales.
     */
    val nombreVisual: String
        get() = if (alias.isNotBlank()) alias else nombre
}

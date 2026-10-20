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
    val telefono: String = "",
    val ubicacion: String = "", // Dirección o referencia geográfica
    val tipoBidonPreferido: TipoBidon = TipoBidon.AZUL,
    val precioHabitual: Double = 1.00, // Precio negociado ($0.75 - $1.25)
    val tipoCliente: TipoCliente = TipoCliente.RUTA,
    val diasVisita: String = "", // Cadena separada por comas ej. "LUNES,JUEVES"
    val deudaMonetaria: Double = 0.0, // Saldo adeudado en $
    val bidonesPrestados: Int = 0, // Envases de 20L que el cliente adeuda físicamente
    val fotoReferenciaPath: String? = null, // Ruta interna de la foto de la casa/edificio
    val activo: Boolean = true,
    val fechaCreacion: Long = System.currentTimeMillis()
)

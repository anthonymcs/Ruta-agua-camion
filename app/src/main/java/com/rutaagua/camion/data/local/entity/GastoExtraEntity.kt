package com.rutaagua.camion.data.local.entity

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

/**
 * Entidad que registra los gastos operativos cotidianos en ruta
 * (almuerzo, colas/bebidas, compras menores, peajes/taller).
 * Se descuentan automáticamente del efectivo recaudado en el cierre del día.
 */
@Entity(
    tableName = "gastos_extras",
    indices = [Index("fechaTexto")]
)
data class GastoExtraEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val vehiculo: String = "CAMION", // "CAMION" o "CAMIONETA"
    val fecha: Long = System.currentTimeMillis(),
    val fechaTexto: String, // "YYYY-MM-DD"
    val concepto: String, // Ej: "Almuerzo chofer y ayudante", "Bebidas / Colas"
    val monto: Double,
    val categoria: String = "RUTA" // RUTA, VEHICULO, ALIMENTACION, OTROS
)

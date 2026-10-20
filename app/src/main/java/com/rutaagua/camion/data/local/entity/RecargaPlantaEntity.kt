package com.rutaagua.camion.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Entidad que registra los abastecimientos en la planta purificadora (1 o 2 vueltas diarias).
 */
@Entity(tableName = "recargas_planta")
data class RecargaPlantaEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val vehiculo: String = "CAMION", // "CAMION" o "CAMIONETA"
    val fecha: Long = System.currentTimeMillis(),
    val fechaTexto: String, // Formato "YYYY-MM-DD" para filtros diarios
    val numeroVuelta: Int = 1, // Vuelta 1 o 2 del día
    // Bidones cargados en planta
    val bidonesCargadosTransparentes: Int = 0,
    val bidonesCargadosAzules: Int = 0,
    val bidonesCargadosMarcados: Int = 0,
    // Envases vacíos descargados en planta
    val bidonesVaciosDescargados: Int = 0,
    // Otros productos abastecidos
    val galonesCargados: Int = 0,
    val pacasCargadas: Int = 0,
    val minisCargadas: Int = 0,
    val observaciones: String = ""
) {
    /** Total de bidones cargados en este viaje */
    fun totalBidonesCargados(): Int =
        bidonesCargadosTransparentes + bidonesCargadosAzules + bidonesCargadosMarcados
}

package com.rutaagua.camion.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.rutaagua.camion.data.local.model.TipoBidon

/**
 * Entidad que mantiene el estado actual en tiempo real de la carga del camión.
 * Se maneja como un registro único persistente (id = 1).
 */
@Entity(tableName = "inventario_camion")
data class InventarioCamionEntity(
    @PrimaryKey
    val id: Int = 1,
    val vehiculo: String = "CAMION", // "CAMION" o "CAMIONETA"
    // Metas / Topes de capacidad habitual del camión (ajustables según la jornada)
    val capacidadTopeBidones: Int = 218,
    val capacidadTopeGalones: Int = 90,
    val capacidadTopePacas: Int = 6,
    val capacidadTopeMinis: Int = 2,

    // Existencias actuales de bidones llenos clasificados por tipo
    val bidonesLlenosTransparentes: Int = 0,
    val bidonesLlenosAzules: Int = 0,
    val bidonesLlenosMarcados: Int = 0,

    // Envases vacíos acumulados (subidos al camión tras entrega o devolución)
    val bidonesVacios: Int = 0,

    // Existencias de otros productos
    val galonesLlenos: Int = 0,
    val pacasLlenas: Int = 0,
    val minisLlenas: Int = 0,

    val fechaUltimaActualizacion: Long = System.currentTimeMillis()
) {
    /** Total de bidones llenos disponibles a bordo */
    fun totalBidonesLlenos(): Int =
        bidonesLlenosTransparentes + bidonesLlenosAzules + bidonesLlenosMarcados

    /** Cantidad faltante de bidones para alcanzar la capacidad tope al recargar en planta */
    fun bidonesFaltantesParaTope(): Int =
        maxOf(0, capacidadTopeBidones - totalBidonesLlenos())

    /** Obtiene el stock disponible para un tipo de bidón específico */
    fun stockPorTipo(tipo: TipoBidon): Int = when (tipo) {
        TipoBidon.TRANSPARENTE -> bidonesLlenosTransparentes
        TipoBidon.AZUL -> bidonesLlenosAzules
        TipoBidon.MARCADO -> bidonesLlenosMarcados
    }
}

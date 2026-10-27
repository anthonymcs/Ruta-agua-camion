package com.rutaagua.camion.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Entidad que registra una visita donde el cliente no se encontraba en casa.
 * Incluye la evidencia fotográfica de la fachada y sincronización en tiempo real.
 */
@Entity(tableName = "visitas_fallidas")
data class VisitaFallidaEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val clienteId: Long,
    val nombreCliente: String,
    val telefonoCliente: String = "",
    val fechaIso: String, // "yyyy-MM-dd"
    val horaDisplay: String, // "HH:mm"
    val timestamp: Long = System.currentTimeMillis(),
    val vehiculo: String = "CAMION", // "CAMION" o "CAMIONETA"
    val fotoEvidenciaPath: String? = null,
    val fotoBase64Thumbnail: String? = null,
    val sincronizado: Boolean = false
)

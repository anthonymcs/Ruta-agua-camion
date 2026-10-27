package com.rutaagua.camion.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Entidad que registra el cierre de caja de la jornada (estilo RP3 gasolinera).
 * - En el Teléfono de Ruta, el operador realiza un "cierre ciego", declarando únicamente
 *   el dinero físico contado y los comprobantes de transferencias sin saber si sobra o falta.
 * - En el perfil de Rolando (Administrador), audita la diferencia exacta (cuadrado, faltante, sobrante),
 *   costos de planta, utilidad bruta y utilidad neta.
 */
@Entity(tableName = "cierres_caja")
data class CierreCajaEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val fechaIso: String, // "yyyy-MM-dd"
    val timestamp: Long = System.currentTimeMillis(),
    val vehiculo: String = "CAMION", // "CAMION" o "CAMIONETA"
    val efectivoDeclarado: Double = 0.0,
    val transferenciasDeclaradas: Double = 0.0,
    val totalDeclarado: Double = efectivoDeclarado + transferenciasDeclaradas,
    val observaciones: String = "",

    // Auditoría de Sistema (oculto al operador de ruta)
    val efectivoSistema: Double = 0.0,
    val transferenciasSistema: Double = 0.0,
    val totalSistema: Double = 0.0,
    val diferenciaEfectivo: Double = 0.0,
    val diferenciaTransferencia: Double = 0.0,
    val diferenciaTotal: Double = 0.0,

    // Métricas operativas y financieras (para Rolando Administrador)
    val bidonesVendidos: Int = 0,
    val galonesVendidos: Int = 0,
    val pacasVendidas: Int = 0,
    val minisVendidas: Int = 0,
    val gastosRuta: Double = 0.0,
    val costoTotalPlanta: Double = 0.0,
    val utilidadBruta: Double = 0.0,
    val utilidadNeta: Double = 0.0,

    val sincronizado: Boolean = false
)

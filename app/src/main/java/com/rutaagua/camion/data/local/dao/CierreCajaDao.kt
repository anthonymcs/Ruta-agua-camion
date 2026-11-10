package com.rutaagua.camion.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.rutaagua.camion.data.local.entity.CierreCajaEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface CierreCajaDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertar(cierre: CierreCajaEntity): Long

    @Update
    suspend fun actualizar(cierre: CierreCajaEntity)

    @Query("SELECT * FROM cierres_caja WHERE fechaIso = :fechaIso AND vehiculo = :vehiculo ORDER BY id DESC LIMIT 1")
    fun obtenerCierrePorFechaYVehiculoFlow(fechaIso: String, vehiculo: String): Flow<CierreCajaEntity?>

    @Query("SELECT * FROM cierres_caja WHERE fechaIso = :fechaIso AND vehiculo = :vehiculo ORDER BY id DESC LIMIT 1")
    suspend fun obtenerCierrePorFechaYVehiculoDirecto(fechaIso: String, vehiculo: String): CierreCajaEntity?

    @Query("SELECT * FROM cierres_caja WHERE fechaIso = :fechaIso ORDER BY id DESC LIMIT 1")
    fun obtenerUltimoCierrePorFechaFlow(fechaIso: String): Flow<CierreCajaEntity?>

    @Query("SELECT * FROM cierres_caja WHERE id = :id LIMIT 1")
    suspend fun obtenerPorId(id: Long): CierreCajaEntity?

    @Query("SELECT * FROM cierres_caja ORDER BY timestamp DESC")
    fun obtenerTodosFlow(): Flow<List<CierreCajaEntity>>

    @Query("SELECT * FROM cierres_caja ORDER BY timestamp DESC")
    suspend fun obtenerTodosDirecto(): List<CierreCajaEntity>

    @Query("SELECT * FROM cierres_caja WHERE sincronizado = 0")
    suspend fun obtenerPendientesDeSincronizar(): List<CierreCajaEntity>

    @Query("UPDATE cierres_caja SET sincronizado = 1 WHERE id = :id")
    suspend fun marcarComoSincronizado(id: Long)
}

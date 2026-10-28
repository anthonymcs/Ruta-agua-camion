package com.rutaagua.camion.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.rutaagua.camion.data.local.entity.VisitaFallidaEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface VisitaFallidaDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertar(visita: VisitaFallidaEntity): Long

    @Query("SELECT * FROM visitas_fallidas WHERE fechaIso = :fechaIso ORDER BY timestamp DESC")
    fun obtenerVisitasPorFechaFlow(fechaIso: String): Flow<List<VisitaFallidaEntity>>

    @Query("SELECT * FROM visitas_fallidas ORDER BY timestamp DESC")
    fun obtenerTodasVisitasFlow(): Flow<List<VisitaFallidaEntity>>

    @Query("SELECT * FROM visitas_fallidas WHERE clienteId = :clienteId ORDER BY timestamp DESC")
    fun obtenerVisitasPorClienteFlow(clienteId: Long): Flow<List<VisitaFallidaEntity>>

    @Query("SELECT * FROM visitas_fallidas WHERE fechaIso LIKE :prefijoMes || '%' ORDER BY timestamp DESC")
    fun obtenerVisitasPorMesFlow(prefijoMes: String): Flow<List<VisitaFallidaEntity>>

    @Query("SELECT * FROM visitas_fallidas WHERE fechaIso = :fechaIso AND clienteId = :clienteId ORDER BY timestamp DESC LIMIT 1")
    fun obtenerVisitaPorClienteYFechaFlow(clienteId: Long, fechaIso: String): Flow<VisitaFallidaEntity?>

    @Query("SELECT * FROM visitas_fallidas WHERE fechaIso = :fechaIso AND clienteId = :clienteId ORDER BY timestamp DESC LIMIT 1")
    suspend fun obtenerVisitaPorClienteYFechaDirecto(clienteId: Long, fechaIso: String): VisitaFallidaEntity?

    @Query("SELECT * FROM visitas_fallidas WHERE id = :id LIMIT 1")
    suspend fun obtenerPorId(id: Long): VisitaFallidaEntity?

    @Query("SELECT * FROM visitas_fallidas WHERE sincronizado = 0")
    suspend fun obtenerPendientesDeSincronizar(): List<VisitaFallidaEntity>

    @Query("UPDATE visitas_fallidas SET sincronizado = 1 WHERE id = :id")
    suspend fun marcarComoSincronizado(id: Long)

    @Query("DELETE FROM visitas_fallidas WHERE id = :id")
    suspend fun eliminarPorId(id: Long)
}

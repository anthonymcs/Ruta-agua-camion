package com.rutaagua.camion.data.local.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.rutaagua.camion.data.local.entity.GastoExtraEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface GastoDao {

    @Query("SELECT * FROM gastos_extras WHERE fechaTexto = :fechaTexto ORDER BY fecha DESC")
    fun obtenerPorFechaFlow(fechaTexto: String): Flow<List<GastoExtraEntity>>

    @Query("SELECT * FROM gastos_extras ORDER BY fecha DESC")
    fun obtenerTodosFlow(): Flow<List<GastoExtraEntity>>

    @Query("SELECT COALESCE(SUM(monto), 0.0) FROM gastos_extras WHERE fechaTexto = :fechaTexto")
    fun sumarGastosPorFechaFlow(fechaTexto: String): Flow<Double>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertar(gasto: GastoExtraEntity): Long

    @Delete
    suspend fun eliminar(gasto: GastoExtraEntity): Int
}

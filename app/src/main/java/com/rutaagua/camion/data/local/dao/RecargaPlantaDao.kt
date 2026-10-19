package com.rutaagua.camion.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.rutaagua.camion.data.local.entity.RecargaPlantaEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface RecargaPlantaDao {

    @Query("SELECT * FROM recargas_planta WHERE fechaTexto = :fechaTexto ORDER BY fecha DESC")
    fun obtenerRecargasPorFechaFlow(fechaTexto: String): Flow<List<RecargaPlantaEntity>>

    @Query("SELECT * FROM recargas_planta ORDER BY fecha DESC")
    fun obtenerTodasFlow(): Flow<List<RecargaPlantaEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertar(recarga: RecargaPlantaEntity): Long
}

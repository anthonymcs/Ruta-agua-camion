package com.rutaagua.camion.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.rutaagua.camion.data.local.entity.InventarioCamionEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface InventarioDao {

    @Query("SELECT * FROM inventario_camion WHERE id = 1 LIMIT 1")
    fun obtenerInventarioFlow(): Flow<InventarioCamionEntity?>

    @Query("SELECT * FROM inventario_camion WHERE id = 1 LIMIT 1")
    suspend fun obtenerInventarioDirecto(): InventarioCamionEntity?

    @Query("SELECT * FROM inventario_camion WHERE vehiculo = :vehiculo LIMIT 1")
    fun obtenerInventarioPorVehiculoFlow(vehiculo: String): Flow<InventarioCamionEntity?>

    @Query("SELECT * FROM inventario_camion WHERE vehiculo = :vehiculo LIMIT 1")
    suspend fun obtenerInventarioPorVehiculoDirecto(vehiculo: String): InventarioCamionEntity?

    @Query("SELECT * FROM inventario_camion")
    fun obtenerTodosLosInventariosFlow(): Flow<List<InventarioCamionEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertarOActualizar(inventario: InventarioCamionEntity): Long

    @Update
    suspend fun actualizar(inventario: InventarioCamionEntity): Int

    @Query("""
        UPDATE inventario_camion 
        SET capacidadTopeBidones = :topeBidones,
            capacidadTopeGalones = :topeGalones,
            capacidadTopePacas = :topePacas,
            capacidadTopeMinis = :topeMinis,
            fechaUltimaActualizacion = :fecha
        WHERE id = 1
    """)
    suspend fun actualizarMetasCapacidad(
        topeBidones: Int,
        topeGalones: Int,
        topePacas: Int,
        topeMinis: Int,
        fecha: Long = System.currentTimeMillis()
    ): Int

    @Query("""
        UPDATE inventario_camion 
        SET capacidadTopeBidones = :topeBidones,
            capacidadTopeGalones = :topeGalones,
            capacidadTopePacas = :topePacas,
            capacidadTopeMinis = :topeMinis,
            fechaUltimaActualizacion = :fecha
        WHERE vehiculo = :vehiculo
    """)
    suspend fun actualizarMetasCapacidadVehiculo(
        vehiculo: String,
        topeBidones: Int,
        topeGalones: Int,
        topePacas: Int,
        topeMinis: Int,
        fecha: Long = System.currentTimeMillis()
    ): Int
}

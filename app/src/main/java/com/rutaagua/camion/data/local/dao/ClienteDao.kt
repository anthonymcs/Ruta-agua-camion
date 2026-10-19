package com.rutaagua.camion.data.local.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.rutaagua.camion.data.local.entity.ClienteEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface ClienteDao {

    @Query("SELECT * FROM clientes WHERE activo = 1 ORDER BY nombre ASC")
    fun obtenerTodosFlow(): Flow<List<ClienteEntity>>

    @Query("SELECT * FROM clientes WHERE activo = 1 AND nombre LIKE '%' || :query || '%' ORDER BY nombre ASC")
    fun buscarPorNombreFlow(query: String): Flow<List<ClienteEntity>>

    @Query("SELECT * FROM clientes WHERE activo = 1 AND diasVisita LIKE '%' || :diaSemana || '%' ORDER BY nombre ASC")
    fun obtenerPorDiaSemanaFlow(diaSemana: String): Flow<List<ClienteEntity>>

    @Query("SELECT * FROM clientes WHERE id = :id LIMIT 1")
    fun obtenerPorIdFlow(id: Long): Flow<ClienteEntity?>

    @Query("SELECT * FROM clientes WHERE id = :id LIMIT 1")
    suspend fun obtenerPorIdDirecto(id: Long): ClienteEntity?

    @Query("SELECT * FROM clientes WHERE activo = 1 AND deudaMonetaria > 0 ORDER BY deudaMonetaria DESC")
    fun obtenerClientesConDeudaFlow(): Flow<List<ClienteEntity>>

    @Query("SELECT * FROM clientes WHERE activo = 1 AND bidonesPrestados > 0 ORDER BY bidonesPrestados DESC")
    fun obtenerClientesConBidonesPrestadosFlow(): Flow<List<ClienteEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertar(cliente: ClienteEntity): Long

    @Update
    suspend fun actualizar(cliente: ClienteEntity): Int

    @Query("UPDATE clientes SET deudaMonetaria = :nuevaDeuda, bidonesPrestados = :nuevosBidones WHERE id = :id")
    suspend fun actualizarDeudaYBidones(id: Long, nuevaDeuda: Double, nuevosBidones: Int): Int

    @Delete
    suspend fun eliminar(cliente: ClienteEntity): Int

    @Query("UPDATE clientes SET activo = 0 WHERE id = :id")
    suspend fun desactivar(id: Long): Int
}

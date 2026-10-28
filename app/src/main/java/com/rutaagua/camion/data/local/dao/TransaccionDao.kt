package com.rutaagua.camion.data.local.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.rutaagua.camion.data.local.entity.TransaccionVentaEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface TransaccionDao {

    @Query("SELECT * FROM transacciones_ventas WHERE fechaTexto = :fechaTexto ORDER BY fecha DESC")
    fun obtenerPorFechaFlow(fechaTexto: String): Flow<List<TransaccionVentaEntity>>

    @Query("""
        SELECT * FROM transacciones_ventas 
        WHERE fechaTexto = :fechaTexto AND (:vehiculo IS NULL OR vehiculo = :vehiculo) 
        ORDER BY fecha DESC
    """)
    fun obtenerPorFechaYVehiculoFlow(fechaTexto: String, vehiculo: String?): Flow<List<TransaccionVentaEntity>>

    @Query("SELECT * FROM transacciones_ventas ORDER BY fecha DESC")
    fun obtenerTodasFlow(): Flow<List<TransaccionVentaEntity>>

    @Query("SELECT * FROM transacciones_ventas WHERE (:vehiculo IS NULL OR vehiculo = :vehiculo) ORDER BY fecha DESC")
    fun obtenerTodasPorVehiculoFlow(vehiculo: String?): Flow<List<TransaccionVentaEntity>>

    @Query("SELECT * FROM transacciones_ventas WHERE clienteId = :clienteId ORDER BY fecha DESC")
    fun obtenerPorClienteFlow(clienteId: Long): Flow<List<TransaccionVentaEntity>>

    @Query("SELECT * FROM transacciones_ventas WHERE fechaTexto LIKE :prefijoMes || '%' ORDER BY fecha DESC")
    fun obtenerPorMesFlow(prefijoMes: String): Flow<List<TransaccionVentaEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertar(transaccion: TransaccionVentaEntity): Long

    @Update
    suspend fun actualizar(transaccion: TransaccionVentaEntity): Int

    @Delete
    suspend fun eliminar(transaccion: TransaccionVentaEntity): Int

    // Consultas sumatorias reactivas para el Cierre de Caja del Día (Hoja de Cuadre)
    @Query("SELECT COALESCE(SUM(montoEfectivo), 0.0) FROM transacciones_ventas WHERE fechaTexto = :fechaTexto")
    fun sumarEfectivoPorFechaFlow(fechaTexto: String): Flow<Double>

    @Query("SELECT COALESCE(SUM(montoTransferencia), 0.0) FROM transacciones_ventas WHERE fechaTexto = :fechaTexto")
    fun sumarTransferenciaPorFechaFlow(fechaTexto: String): Flow<Double>

    @Query("SELECT COALESCE(SUM(montoCredito), 0.0) FROM transacciones_ventas WHERE fechaTexto = :fechaTexto")
    fun sumarCreditoPorFechaFlow(fechaTexto: String): Flow<Double>

    @Query("SELECT COALESCE(SUM(montoAbono), 0.0) FROM transacciones_ventas WHERE fechaTexto = :fechaTexto")
    fun sumarAbonosPorFechaFlow(fechaTexto: String): Flow<Double>

    @Query("SELECT COALESCE(SUM(montoCanceladoDeuda), 0.0) FROM transacciones_ventas WHERE fechaTexto = :fechaTexto")
    fun sumarCanceladosPorFechaFlow(fechaTexto: String): Flow<Double>

    @Query("SELECT COALESCE(SUM(totalImporte), 0.0) FROM transacciones_ventas WHERE fechaTexto = :fechaTexto")
    fun sumarTotalVentasPorFechaFlow(fechaTexto: String): Flow<Double>

    // Sumatorias de volumen de producto del día
    @Query("SELECT COALESCE(SUM(cantidadBidonesRecarga), 0) FROM transacciones_ventas WHERE fechaTexto = :fechaTexto")
    fun sumarBidonesRecargaPorFechaFlow(fechaTexto: String): Flow<Int>

    @Query("SELECT COALESCE(SUM(cantidadBidonesPrestados), 0) FROM transacciones_ventas WHERE fechaTexto = :fechaTexto")
    fun sumarBidonesPrestadosPorFechaFlow(fechaTexto: String): Flow<Int>

    @Query("SELECT COALESCE(SUM(cantidadBidonesDevueltos), 0) FROM transacciones_ventas WHERE fechaTexto = :fechaTexto")
    fun sumarBidonesDevueltosPorFechaFlow(fechaTexto: String): Flow<Int>

    @Query("SELECT COALESCE(SUM(cantidadBidonesVendidosEnvase), 0) FROM transacciones_ventas WHERE fechaTexto = :fechaTexto")
    fun sumarBidonesVendidosEnvasePorFechaFlow(fechaTexto: String): Flow<Int>

    @Query("SELECT COALESCE(SUM(cantidadGalones), 0) FROM transacciones_ventas WHERE fechaTexto = :fechaTexto")
    fun sumarGalonesPorFechaFlow(fechaTexto: String): Flow<Int>

    @Query("SELECT COALESCE(SUM(cantidadPacas), 0) FROM transacciones_ventas WHERE fechaTexto = :fechaTexto")
    fun sumarPacasPorFechaFlow(fechaTexto: String): Flow<Int>

    @Query("SELECT COALESCE(SUM(cantidadMinis), 0) FROM transacciones_ventas WHERE fechaTexto = :fechaTexto")
    fun sumarMinisPorFechaFlow(fechaTexto: String): Flow<Int>

    // Desglose de ventas de bidones por tipo específico en una fecha
    @Query("""
        SELECT COALESCE(SUM(cantidadBidonesRecarga + cantidadBidonesVendidosEnvase), 0) 
        FROM transacciones_ventas 
        WHERE fechaTexto = :fechaTexto AND tipoBidon = :tipoBidon
    """)
    fun sumarBidonesPorTipoYFechaFlow(tipoBidon: String, fechaTexto: String): Flow<Int>
}

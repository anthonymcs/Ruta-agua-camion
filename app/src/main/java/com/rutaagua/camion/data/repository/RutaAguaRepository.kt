package com.rutaagua.camion.data.repository

import androidx.room.withTransaction
import com.rutaagua.camion.data.local.RutaAguaDatabase
import com.rutaagua.camion.data.local.entity.ClienteEntity
import com.rutaagua.camion.data.local.entity.GastoExtraEntity
import com.rutaagua.camion.data.local.entity.InventarioCamionEntity
import com.rutaagua.camion.data.local.entity.RecargaPlantaEntity
import com.rutaagua.camion.data.local.entity.TransaccionVentaEntity
import com.rutaagua.camion.data.local.model.EstadisticasVentasTipo
import com.rutaagua.camion.data.local.model.RecargaCalculada
import com.rutaagua.camion.data.local.model.ResumenCierreDia
import com.rutaagua.camion.data.local.model.StockAlertas
import com.rutaagua.camion.data.local.model.TipoBidon
import com.rutaagua.camion.data.remote.FirestoreSyncManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext
import java.util.Locale

/**
 * Repositorio central que coordina y garantiza la integridad de los datos
 * entre el inventario del vehículo (Camión / Camioneta), clientes, ventas y contabilidad diaria.
 */
class RutaAguaRepository(private val database: RutaAguaDatabase) {

    private val clienteDao = database.clienteDao()
    private val inventarioDao = database.inventarioDao()
    private val transaccionDao = database.transaccionDao()
    private val recargaPlantaDao = database.recargaPlantaDao()
    private val gastoDao = database.gastoDao()

    var syncManager: FirestoreSyncManager? = null

    // ==========================================
    // CLIENTES
    // ==========================================

    fun obtenerTodosLosClientes(): Flow<List<ClienteEntity>> =
        clienteDao.obtenerTodosFlow()

    fun buscarClientesPorNombre(query: String): Flow<List<ClienteEntity>> =
        clienteDao.buscarPorNombreFlow(query)

    fun obtenerClientesPorDiaRuta(diaSemana: String): Flow<List<ClienteEntity>> =
        clienteDao.obtenerPorDiaSemanaFlow(diaSemana)

    fun obtenerClientePorId(id: Long): Flow<ClienteEntity?> =
        clienteDao.obtenerPorIdFlow(id)

    suspend fun obtenerClientePorIdDirecto(id: Long): ClienteEntity? = withContext(Dispatchers.IO) {
        clienteDao.obtenerPorIdDirecto(id)
    }

    fun obtenerClientesConDeuda(): Flow<List<ClienteEntity>> =
        clienteDao.obtenerClientesConDeudaFlow()

    fun obtenerClientesConBidonesPrestados(): Flow<List<ClienteEntity>> =
        clienteDao.obtenerClientesConBidonesPrestadosFlow()

    suspend fun guardarCliente(cliente: ClienteEntity): Long = withContext(Dispatchers.IO) {
        val id = if (cliente.id == 0L) {
            clienteDao.insertar(cliente)
        } else {
            clienteDao.actualizar(cliente)
            cliente.id
        }
        clienteDao.obtenerPorIdDirecto(id)?.let {
            syncManager?.subirCliente(it)
        }
        id
    }

    suspend fun eliminarCliente(cliente: ClienteEntity) = withContext(Dispatchers.IO) {
        clienteDao.eliminar(cliente)
        syncManager?.eliminarCliente(cliente.id)
    }

    suspend fun desactivarCliente(id: Long) = withContext(Dispatchers.IO) {
        clienteDao.desactivar(id)
        clienteDao.obtenerPorIdDirecto(id)?.let {
            syncManager?.subirCliente(it)
        }
    }

    // ==========================================
    // INVENTARIO DEL VEHÍCULO Y ALERTAS
    // ==========================================

    fun obtenerInventarioCamion(vehiculo: String = "CAMION"): Flow<InventarioCamionEntity?> =
        inventarioDao.obtenerInventarioPorVehiculoFlow(vehiculo)

    suspend fun actualizarCapacidadTope(
        vehiculo: String,
        topeBidones: Int,
        topeGalones: Int,
        topePacas: Int,
        topeMinis: Int
    ) = withContext(Dispatchers.IO) {
        inventarioDao.actualizarMetasCapacidadVehiculo(
            vehiculo = vehiculo,
            topeBidones = topeBidones,
            topeGalones = topeGalones,
            topePacas = topePacas,
            topeMinis = topeMinis
        )
        inventarioDao.obtenerInventarioPorVehiculoDirecto(vehiculo)?.let { inv ->
            syncManager?.subirInventario(inv)
        }
    }

    /**
     * Cálculo reactivo de cuántos bidones y productos faltan para recargar a tope en la planta.
     */
    fun obtenerCalculoRecargaPlanta(vehiculo: String = "CAMION"): Flow<RecargaCalculada?> {
        return inventarioDao.obtenerInventarioPorVehiculoFlow(vehiculo).map { inventario ->
            inventario?.let { inv ->
                val llenosActuales = inv.totalBidonesLlenos()
                RecargaCalculada(
                    capacidadTopeBidones = inv.capacidadTopeBidones,
                    bidonesLlenosActuales = llenosActuales,
                    bidonesFaltantesParaTope = maxOf(0, inv.capacidadTopeBidones - llenosActuales),
                    bidonesVaciosParaDescargar = inv.bidonesVacios,
                    galonesFaltantes = maxOf(0, inv.capacidadTopeGalones - inv.galonesLlenos),
                    pacasFaltantes = maxOf(0, inv.capacidadTopePacas - inv.pacasLlenas),
                    minisFaltantes = maxOf(0, inv.capacidadTopeMinis - inv.minisLlenas)
                )
            }
        }
    }

    /**
     * Alertas de stock bajo en tiempo real (umbrales de seguridad personalizables).
     */
    fun obtenerAlertasStock(vehiculo: String = "CAMION"): Flow<StockAlertas> {
        return inventarioDao.obtenerInventarioPorVehiculoFlow(vehiculo).map { inv ->
            if (inv == null) {
                StockAlertas()
            } else {
                val transBajos = inv.bidonesLlenosTransparentes <= 5
                val azulBajos = inv.bidonesLlenosAzules <= 10
                val marcBajos = inv.bidonesLlenosMarcados <= 5
                val galBajos = inv.galonesLlenos <= 5
                val pacBajas = inv.pacasLlenas <= 1
                val minBajas = inv.minisLlenas <= 1

                val mensajes = mutableListOf<String>()
                if (transBajos) mensajes.add("Pocos Transparentes (${inv.bidonesLlenosTransparentes})")
                if (azulBajos) mensajes.add("Pocos Azules (${inv.bidonesLlenosAzules})")
                if (marcBajos) mensajes.add("Pocos Marcados (${inv.bidonesLlenosMarcados})")
                if (galBajos) mensajes.add("Pocos Galones (${inv.galonesLlenos})")

                StockAlertas(
                    transparentesBajos = transBajos,
                    azulesBajos = azulBajos,
                    marcadosBajos = marcBajos,
                    galonesBajos = galBajos,
                    pacasBajas = pacBajas,
                    minisBajas = minBajas,
                    mensajeAlerta = if (mensajes.isNotEmpty()) mensajes.joinToString(", ") else null
                )
            }
        }
    }

    // ==========================================
    // RECARGAS EN PLANTA (TRANSACCIÓN SEGURA)
    // ==========================================

    suspend fun registrarRecargaPlanta(recarga: RecargaPlantaEntity): Long = withContext(Dispatchers.IO) {
        database.withTransaction {
            val actual = inventarioDao.obtenerInventarioPorVehiculoDirecto(recarga.vehiculo)
                ?: InventarioCamionEntity(
                    id = if (recarga.vehiculo == "CAMIONETA") 2 else 1,
                    vehiculo = recarga.vehiculo
                )

            val nuevoTrans = actual.bidonesLlenosTransparentes + recarga.bidonesCargadosTransparentes
            val nuevoAzul = actual.bidonesLlenosAzules + recarga.bidonesCargadosAzules
            val nuevoMarc = actual.bidonesLlenosMarcados + recarga.bidonesCargadosMarcados
            val nuevosVacios = maxOf(0, actual.bidonesVacios - recarga.bidonesVaciosDescargados)
            val nuevosGalones = actual.galonesLlenos + recarga.galonesCargados
            val nuevasPacas = actual.pacasLlenas + recarga.pacasCargadas
            val nuevasMinis = actual.minisLlenas + recarga.minisCargadas

            val inventarioActualizado = actual.copy(
                bidonesLlenosTransparentes = nuevoTrans,
                bidonesLlenosAzules = nuevoAzul,
                bidonesLlenosMarcados = nuevoMarc,
                bidonesVacios = nuevosVacios,
                galonesLlenos = nuevosGalones,
                pacasLlenas = nuevasPacas,
                minisLlenas = nuevasMinis,
                fechaUltimaActualizacion = System.currentTimeMillis()
            )

            inventarioDao.insertarOActualizar(inventarioActualizado)
            val idGenerado = recargaPlantaDao.insertar(recarga)

            val recargaConId = recarga.copy(id = idGenerado)
            syncManager?.subirRecarga(recargaConId)
            syncManager?.subirInventario(inventarioActualizado)

            idGenerado
        }
    }

    fun obtenerRecargasPorFecha(fechaTexto: String, vehiculo: String? = null): Flow<List<RecargaPlantaEntity>> =
        recargaPlantaDao.obtenerRecargasPorFechaYVehiculoFlow(fechaTexto, vehiculo)

    // ==========================================
    // TRANSACCIÓN DE VENTA, EDICIÓN Y REVERSIÓN
    // ==========================================

    suspend fun registrarVenta(transaccion: TransaccionVentaEntity): Long = withContext(Dispatchers.IO) {
        database.withTransaction {
            aplicarImpactoVenta(transaccion)
            val idGenerado = transaccionDao.insertar(transaccion)
            val txConId = transaccion.copy(id = idGenerado)
            syncManager?.subirTransaccion(txConId)
            idGenerado
        }
    }

    /**
     * Elimina una venta revirtiendo de forma segura e íntegra el stock del vehículo y los saldos del cliente.
     */
    suspend fun eliminarVenta(transaccion: TransaccionVentaEntity): Int = withContext(Dispatchers.IO) {
        database.withTransaction {
            revertirImpactoVenta(transaccion)
            val rows = transaccionDao.eliminar(transaccion)
            syncManager?.eliminarTransaccion(transaccion.id)
            rows
        }
    }

    /**
     * Edita una venta de manera atómica: revierte la venta anterior y aplica la nueva en una sola transacción.
     */
    suspend fun editarVenta(
        transaccionAnterior: TransaccionVentaEntity,
        transaccionNueva: TransaccionVentaEntity
    ): Int = withContext(Dispatchers.IO) {
        database.withTransaction {
            revertirImpactoVenta(transaccionAnterior)
            aplicarImpactoVenta(transaccionNueva)
            val rows = transaccionDao.actualizar(transaccionNueva.copy(id = transaccionAnterior.id))
            syncManager?.subirTransaccion(transaccionNueva.copy(id = transaccionAnterior.id))
            rows
        }
    }

    private suspend fun aplicarImpactoVenta(transaccion: TransaccionVentaEntity) {
        val inventarioActual = inventarioDao.obtenerInventarioPorVehiculoDirecto(transaccion.vehiculo)
            ?: InventarioCamionEntity(
                id = if (transaccion.vehiculo == "CAMIONETA") 2 else 1,
                vehiculo = transaccion.vehiculo
            )

        val bidonesQueBajan = transaccion.cantidadBidonesRecarga +
                transaccion.cantidadBidonesPrestados +
                transaccion.cantidadBidonesVendidosEnvase

        var transLlenos = inventarioActual.bidonesLlenosTransparentes
        var azulLlenos = inventarioActual.bidonesLlenosAzules
        var marcLlenos = inventarioActual.bidonesLlenosMarcados

        when (transaccion.tipoBidon) {
            TipoBidon.TRANSPARENTE -> transLlenos = maxOf(0, transLlenos - bidonesQueBajan)
            TipoBidon.AZUL -> azulLlenos = maxOf(0, azulLlenos - bidonesQueBajan)
            TipoBidon.MARCADO -> marcLlenos = maxOf(0, marcLlenos - bidonesQueBajan)
        }

        val galonesLlenos = maxOf(0, inventarioActual.galonesLlenos - transaccion.cantidadGalones)
        val pacasLlenas = maxOf(0, inventarioActual.pacasLlenas - transaccion.cantidadPacas)
        val minisLlenas = maxOf(0, inventarioActual.minisLlenas - transaccion.cantidadMinis)

        val vaciosQueSuben = transaccion.cantidadBidonesRecarga + transaccion.cantidadBidonesDevueltos
        val nuevosVacios = inventarioActual.bidonesVacios + vaciosQueSuben

        val inventarioActualizado = inventarioActual.copy(
            bidonesLlenosTransparentes = transLlenos,
            bidonesLlenosAzules = azulLlenos,
            bidonesLlenosMarcados = marcLlenos,
            bidonesVacios = nuevosVacios,
            galonesLlenos = galonesLlenos,
            pacasLlenas = pacasLlenas,
            minisLlenas = minisLlenas,
            fechaUltimaActualizacion = System.currentTimeMillis()
        )
        inventarioDao.insertarOActualizar(inventarioActualizado)
        syncManager?.subirInventario(inventarioActualizado)

        transaccion.clienteId?.let { cId ->
            val clienteActual = clienteDao.obtenerPorIdDirecto(cId)
            if (clienteActual != null) {
                val nuevosPrestados = maxOf(
                    0,
                    clienteActual.bidonesPrestados +
                            transaccion.cantidadBidonesPrestados -
                            transaccion.cantidadBidonesDevueltos
                )
                val nuevaDeuda = maxOf(
                    0.0,
                    clienteActual.deudaMonetaria +
                            transaccion.montoCredito -
                            transaccion.montoAbono -
                            transaccion.montoCanceladoDeuda
                )
                clienteDao.actualizarDeudaYBidones(
                    id = cId,
                    nuevaDeuda = nuevaDeuda,
                    nuevosBidones = nuevosPrestados
                )
                clienteDao.obtenerPorIdDirecto(cId)?.let { c ->
                    syncManager?.subirCliente(c)
                }
            }
        }
    }

    private suspend fun revertirImpactoVenta(t: TransaccionVentaEntity) {
        val inventario = inventarioDao.obtenerInventarioPorVehiculoDirecto(t.vehiculo)
            ?: InventarioCamionEntity(
                id = if (t.vehiculo == "CAMIONETA") 2 else 1,
                vehiculo = t.vehiculo
            )
        val bidonesQueHabianBajado = t.cantidadBidonesRecarga + t.cantidadBidonesPrestados + t.cantidadBidonesVendidosEnvase

        var trans = inventario.bidonesLlenosTransparentes
        var azul = inventario.bidonesLlenosAzules
        var marc = inventario.bidonesLlenosMarcados

        when (t.tipoBidon) {
            TipoBidon.TRANSPARENTE -> trans += bidonesQueHabianBajado
            TipoBidon.AZUL -> azul += bidonesQueHabianBajado
            TipoBidon.MARCADO -> marc += bidonesQueHabianBajado
        }

        val galones = inventario.galonesLlenos + t.cantidadGalones
        val pacas = inventario.pacasLlenas + t.cantidadPacas
        val minis = inventario.minisLlenas + t.cantidadMinis

        val vaciosQueHabianSubido = t.cantidadBidonesRecarga + t.cantidadBidonesDevueltos
        val nuevosVacios = maxOf(0, inventario.bidonesVacios - vaciosQueHabianSubido)

        val inventarioActualizado = inventario.copy(
            bidonesLlenosTransparentes = trans,
            bidonesLlenosAzules = azul,
            bidonesLlenosMarcados = marc,
            bidonesVacios = nuevosVacios,
            galonesLlenos = galones,
            pacasLlenas = pacas,
            minisLlenas = minis,
            fechaUltimaActualizacion = System.currentTimeMillis()
        )
        inventarioDao.insertarOActualizar(inventarioActualizado)
        syncManager?.subirInventario(inventarioActualizado)

        t.clienteId?.let { cId ->
            val cliente = clienteDao.obtenerPorIdDirecto(cId)
            if (cliente != null) {
                val prestados = maxOf(
                    0,
                    cliente.bidonesPrestados - t.cantidadBidonesPrestados + t.cantidadBidonesDevueltos
                )
                val deuda = maxOf(
                    0.0,
                    cliente.deudaMonetaria - t.montoCredito + t.montoAbono + t.montoCanceladoDeuda
                )
                clienteDao.actualizarDeudaYBidones(cId, deuda, prestados)
                clienteDao.obtenerPorIdDirecto(cId)?.let { c ->
                    syncManager?.subirCliente(c)
                }
            }
        }
    }

    fun obtenerTransaccionesPorFecha(fechaTexto: String, vehiculo: String? = null): Flow<List<TransaccionVentaEntity>> =
        transaccionDao.obtenerPorFechaYVehiculoFlow(fechaTexto, vehiculo)

    fun obtenerTodasLasTransacciones(vehiculo: String? = null): Flow<List<TransaccionVentaEntity>> =
        transaccionDao.obtenerTodasPorVehiculoFlow(vehiculo)

    fun obtenerTransaccionesDeCliente(clienteId: Long): Flow<List<TransaccionVentaEntity>> =
        transaccionDao.obtenerPorClienteFlow(clienteId)

    // ==========================================
    // ESTADÍSTICAS Y PORCENTAJES DE VENTAS HOY
    // ==========================================

    fun obtenerEstadisticasVentasPorTipo(fechaTexto: String, vehiculo: String? = null): Flow<EstadisticasVentasTipo> {
        return transaccionDao.obtenerPorFechaYVehiculoFlow(fechaTexto, vehiculo).map { transacciones ->
            var trans = 0
            var azul = 0
            var marc = 0
            var gal = 0
            var pac = 0
            var min = 0
            var env = 0
            for (t in transacciones) {
                val b = t.cantidadBidonesRecarga + t.cantidadBidonesVendidosEnvase
                when (t.tipoBidon) {
                    TipoBidon.TRANSPARENTE -> trans += b
                    TipoBidon.AZUL -> azul += b
                    TipoBidon.MARCADO -> marc += b
                }
                gal += t.cantidadGalones
                pac += t.cantidadPacas
                min += t.cantidadMinis
                env += t.cantidadBidonesVendidosEnvase
            }
            val totalBidones = trans + azul + marc
            EstadisticasVentasTipo(
                transparentesVendidos = trans,
                azulesVendidos = azul,
                marcadosVendidos = marc,
                totalBidonesVendidos = totalBidones,
                galonesVendidos = gal,
                pacasVendidas = pac,
                minisVendidas = min,
                envasesNuevosVendidos = env
            )
        }
    }

    // ==========================================
    // GASTOS EXTRAS DE RUTA
    // ==========================================

    suspend fun registrarGastoExtra(gasto: GastoExtraEntity): Long = withContext(Dispatchers.IO) {
        val id = gastoDao.insertar(gasto)
        val gastoConId = gasto.copy(id = id)
        syncManager?.subirGasto(gastoConId)
        id
    }

    suspend fun eliminarGastoExtra(gasto: GastoExtraEntity) = withContext(Dispatchers.IO) {
        gastoDao.eliminar(gasto)
        syncManager?.eliminarGasto(gasto.id)
    }

    fun obtenerGastosPorFecha(fechaTexto: String, vehiculo: String? = null): Flow<List<GastoExtraEntity>> =
        gastoDao.obtenerPorFechaYVehiculoFlow(fechaTexto, vehiculo)

    // ==========================================
    // CIERRE DIARIO Y REPORTE WHATSAPP
    // ==========================================

    fun obtenerResumenCierreDia(fechaTexto: String, vehiculo: String? = null): Flow<ResumenCierreDia> {
        val fTrans = transaccionDao.obtenerPorFechaYVehiculoFlow(fechaTexto, vehiculo)
        val fGastos = gastoDao.obtenerPorFechaYVehiculoFlow(fechaTexto, vehiculo)

        return combine(fTrans, fGastos) { transacciones, gastos ->
            val totalEfectivo = transacciones.sumOf { it.montoEfectivo }
            val totalTransferencia = transacciones.sumOf { it.montoTransferencia }
            val totalCredito = transacciones.sumOf { it.montoCredito }
            val totalAbono = transacciones.sumOf { it.montoAbono }
            val totalCancelado = transacciones.sumOf { it.montoCanceladoDeuda }
            val granTotalVenta = transacciones.sumOf { it.totalImporte }

            val totalBidonesRecarga = transacciones.sumOf { it.cantidadBidonesRecarga }
            val totalBidonesPrestados = transacciones.sumOf { it.cantidadBidonesPrestados }
            val totalBidonesDevueltos = transacciones.sumOf { it.cantidadBidonesDevueltos }
            val totalBidonesVendidosEnvase = transacciones.sumOf { it.cantidadBidonesVendidosEnvase }
            val totalGalones = transacciones.sumOf { it.cantidadGalones }
            val totalPacas = transacciones.sumOf { it.cantidadPacas }
            val totalMinis = transacciones.sumOf { it.cantidadMinis }

            val totalGastos = gastos.sumOf { it.monto }
            val totalEfectivoEntrante = totalEfectivo + totalAbono + totalCancelado
            val efectivoNeto = totalEfectivoEntrante - totalGastos

            ResumenCierreDia(
                fechaTexto = fechaTexto,
                totalBidonesVendidos = totalBidonesRecarga + totalBidonesVendidosEnvase,
                totalBidonesPrestados = totalBidonesPrestados,
                totalBidonesDevueltos = totalBidonesDevueltos,
                totalGalonesVendidos = totalGalones,
                totalPacasVendidas = totalPacas,
                totalMinisVendidas = totalMinis,
                totalIngresoEfectivo = totalEfectivo,
                totalIngresoTransferencia = totalTransferencia,
                totalCreditosOtorgados = totalCredito,
                totalAbonosRecibidos = totalAbono,
                totalDeudasCanceladas = totalCancelado,
                totalGastosRuta = totalGastos,
                efectivoNetoCaja = efectivoNeto,
                granTotalVenta = granTotalVenta
            )
        }
    }

    /**
     * Formatea el reporte para WhatsApp con desglose exacto por tipo de bidón y sin stock restante.
     */
    fun formatearReporteWhatsApp(
        resumen: ResumenCierreDia,
        estadisticas: EstadisticasVentasTipo,
        fechaDisplay: String,
        vehiculoLabel: String = "🚚 Camión"
    ): String {
        return buildString {
            appendLine("💧 *REPORTE RUTA AGUA - CIERRE DEL DÍA* $vehiculoLabel")
            appendLine("📅 *Fecha:* $fechaDisplay")
            appendLine("─────────────────────────")
            appendLine("📦 *TOTAL BIDONES VENDIDOS: ${resumen.totalBidonesVendidos}*")
            appendLine("  • Transparentes (PET): *${estadisticas.transparentesVendidos}* (${String.format(Locale.US, "%.1f", estadisticas.porcentajeTransparentes)}%)")
            appendLine("  • Azules Estándar: *${estadisticas.azulesVendidos}* (${String.format(Locale.US, "%.1f", estadisticas.porcentajeAzules)}%)")
            appendLine("  • Marcados: *${estadisticas.marcadosVendidos}* (${String.format(Locale.US, "%.1f", estadisticas.porcentajeMarcados)}%)")

            if (resumen.totalBidonesPrestados > 0) {
                appendLine("• Bidones Prestados (deben envase): *${resumen.totalBidonesPrestados}*")
            }
            if (resumen.totalBidonesDevueltos > 0) {
                appendLine("• Envases Recuperados/Devueltos: *${resumen.totalBidonesDevueltos}*")
            }
            if (estadisticas.envasesNuevosVendidos > 0) {
                appendLine("• Envases Nuevos Vendidos: *${estadisticas.envasesNuevosVendidos}*")
            }
            if (resumen.totalGalonesVendidos > 0) {
                appendLine("• Galones (4L): *${resumen.totalGalonesVendidos}*")
            }
            if (resumen.totalPacasVendidas > 0) {
                appendLine("• Pacas (24x500ml): *${resumen.totalPacasVendidas}*")
            }
            if (resumen.totalMinisVendidas > 0) {
                appendLine("• Minis (12x500ml): *${resumen.totalMinisVendidas}*")
            }

            appendLine("─────────────────────────")
            appendLine("💰 *CONTABILIDAD:*")
            appendLine("• Venta Total Bruta: *$${String.format(Locale.US, "%.2f", resumen.granTotalVenta)}*")
            appendLine("• Cobro en Efectivo: *$${String.format(Locale.US, "%.2f", resumen.totalIngresoEfectivo)}*")
            if (resumen.totalIngresoTransferencia > 0) {
                appendLine("• Transferencias: *$${String.format(Locale.US, "%.2f", resumen.totalIngresoTransferencia)}*")
            }
            if (resumen.totalCreditosOtorgados > 0) {
                appendLine("• Créditos Otorgados (Fiado): *$${String.format(Locale.US, "%.2f", resumen.totalCreditosOtorgados)}*")
            }
            if (resumen.totalAbonosRecibidos > 0) {
                appendLine("• Abonos Recibidos: *$${String.format(Locale.US, "%.2f", resumen.totalAbonosRecibidos)}*")
            }
            if (resumen.totalDeudasCanceladas > 0) {
                appendLine("• Deudas Canceladas: *$${String.format(Locale.US, "%.2f", resumen.totalDeudasCanceladas)}*")
            }

            appendLine("─────────────────────────")
            appendLine("🧾 *GASTOS EN RUTA:* *$${String.format(Locale.US, "%.2f", resumen.totalGastosRuta)}*")
            appendLine("💵 *EFECTIVO NETO A ENTREGAR:* *$${String.format(Locale.US, "%.2f", resumen.efectivoNetoCaja)}*")
            appendLine("─────────────────────────")
            appendLine("💧 *Ruta Agua - Sistema Offline & Sincronizado*")
        }
    }
}

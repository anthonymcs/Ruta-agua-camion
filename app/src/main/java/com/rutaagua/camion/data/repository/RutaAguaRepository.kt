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
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext
import java.util.Locale

/**
 * Repositorio central que coordina y garantiza la integridad de los datos
 * entre el inventario del camión, clientes, ventas y contabilidad diaria.
 */
class RutaAguaRepository(private val database: RutaAguaDatabase) {

    private val clienteDao = database.clienteDao()
    private val inventarioDao = database.inventarioDao()
    private val transaccionDao = database.transaccionDao()
    private val recargaPlantaDao = database.recargaPlantaDao()
    private val gastoDao = database.gastoDao()

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

    fun obtenerClientesConDeuda(): Flow<List<ClienteEntity>> =
        clienteDao.obtenerClientesConDeudaFlow()

    fun obtenerClientesConBidonesPrestados(): Flow<List<ClienteEntity>> =
        clienteDao.obtenerClientesConBidonesPrestadosFlow()

    suspend fun guardarCliente(cliente: ClienteEntity): Long = withContext(Dispatchers.IO) {
        if (cliente.id == 0L) {
            clienteDao.insertar(cliente)
        } else {
            clienteDao.actualizar(cliente)
            cliente.id
        }
    }

    suspend fun eliminarCliente(cliente: ClienteEntity) = withContext(Dispatchers.IO) {
        clienteDao.eliminar(cliente)
    }

    suspend fun desactivarCliente(id: Long) = withContext(Dispatchers.IO) {
        clienteDao.desactivar(id)
    }

    // ==========================================
    // INVENTARIO DEL CAMIÓN Y ALERTAS
    // ==========================================

    fun obtenerInventarioCamion(): Flow<InventarioCamionEntity?> =
        inventarioDao.obtenerInventarioFlow()

    suspend fun actualizarCapacidadTope(
        topeBidones: Int,
        topeGalones: Int,
        topePacas: Int,
        topeMinis: Int
    ) = withContext(Dispatchers.IO) {
        inventarioDao.actualizarMetasCapacidad(
            topeBidones = topeBidones,
            topeGalones = topeGalones,
            topePacas = topePacas,
            topeMinis = topeMinis
        )
    }

    /**
     * Cálculo reactivo de cuántos bidones y productos faltan para recargar a tope en la planta.
     */
    fun obtenerCalculoRecargaPlanta(): Flow<RecargaCalculada?> {
        return inventarioDao.obtenerInventarioFlow().map { inventario ->
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
    fun obtenerAlertasStock(): Flow<StockAlertas> {
        return inventarioDao.obtenerInventarioFlow().map { inv ->
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
            val actual = inventarioDao.obtenerInventarioDirecto() ?: InventarioCamionEntity(id = 1)

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
            recargaPlantaDao.insertar(recarga)
        }
    }

    fun obtenerRecargasPorFecha(fechaTexto: String): Flow<List<RecargaPlantaEntity>> =
        recargaPlantaDao.obtenerRecargasPorFechaFlow(fechaTexto)

    // ==========================================
    // TRANSACCIÓN DE VENTA, EDICIÓN Y REVERSIÓN
    // ==========================================

    suspend fun registrarVenta(transaccion: TransaccionVentaEntity): Long = withContext(Dispatchers.IO) {
        database.withTransaction {
            aplicarImpactoVenta(transaccion)
            transaccionDao.insertar(transaccion)
        }
    }

    /**
     * Elimina una venta revirtiendo de forma segura e íntegra el stock del camión y los saldos del cliente.
     */
    suspend fun eliminarVenta(transaccion: TransaccionVentaEntity): Int = withContext(Dispatchers.IO) {
        database.withTransaction {
            revertirImpactoVenta(transaccion)
            transaccionDao.eliminar(transaccion)
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
            transaccionDao.actualizar(transaccionNueva.copy(id = transaccionAnterior.id))
        }
    }

    private suspend fun aplicarImpactoVenta(transaccion: TransaccionVentaEntity) {
        val inventarioActual = inventarioDao.obtenerInventarioDirecto() ?: InventarioCamionEntity(id = 1)

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
            }
        }
    }

    private suspend fun revertirImpactoVenta(t: TransaccionVentaEntity) {
        val inventario = inventarioDao.obtenerInventarioDirecto() ?: InventarioCamionEntity(id = 1)
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
            }
        }
    }

    fun obtenerTransaccionesPorFecha(fechaTexto: String): Flow<List<TransaccionVentaEntity>> =
        transaccionDao.obtenerPorFechaFlow(fechaTexto)

    fun obtenerTransaccionesDeCliente(clienteId: Long): Flow<List<TransaccionVentaEntity>> =
        transaccionDao.obtenerPorClienteFlow(clienteId)

    // ==========================================
    // ESTADÍSTICAS Y PORCENTAJES DE VENTAS HOY
    // ==========================================

    fun obtenerEstadisticasVentasPorTipo(fechaTexto: String): Flow<EstadisticasVentasTipo> {
        val fTrans = transaccionDao.sumarBidonesPorTipoYFechaFlow(TipoBidon.TRANSPARENTE.name, fechaTexto)
        val fAzul = transaccionDao.sumarBidonesPorTipoYFechaFlow(TipoBidon.AZUL.name, fechaTexto)
        val fMarc = transaccionDao.sumarBidonesPorTipoYFechaFlow(TipoBidon.MARCADO.name, fechaTexto)
        val fGal = transaccionDao.sumarGalonesPorFechaFlow(fechaTexto)
        val fPac = transaccionDao.sumarPacasPorFechaFlow(fechaTexto)
        val fMin = transaccionDao.sumarMinisPorFechaFlow(fechaTexto)
        val fEnvNuevos = transaccionDao.sumarBidonesVendidosEnvasePorFechaFlow(fechaTexto)

        return combine(fTrans, fAzul, fMarc) { trans, azul, marc ->
            Triple(trans, azul, marc)
        }.combine(combine(fGal, fPac, fMin, fEnvNuevos) { gal, pac, min, env ->
            SecundariosParts(gal, pac, min, env)
        }) { (trans, azul, marc), sec ->
            val totalBidones = trans + azul + marc
            EstadisticasVentasTipo(
                transparentesVendidos = trans,
                azulesVendidos = azul,
                marcadosVendidos = marc,
                totalBidonesVendidos = totalBidones,
                galonesVendidos = sec.gal,
                pacasVendidas = sec.pac,
                minisVendidas = sec.min,
                envasesNuevosVendidos = sec.env
            )
        }
    }

    private data class SecundariosParts(val gal: Int, val pac: Int, val min: Int, val env: Int)

    // ==========================================
    // GASTOS EXTRAS DE RUTA
    // ==========================================

    suspend fun registrarGastoExtra(gasto: GastoExtraEntity): Long = withContext(Dispatchers.IO) {
        gastoDao.insertar(gasto)
    }

    suspend fun eliminarGastoExtra(gasto: GastoExtraEntity) = withContext(Dispatchers.IO) {
        gastoDao.eliminar(gasto)
    }

    fun obtenerGastosPorFecha(fechaTexto: String): Flow<List<GastoExtraEntity>> =
        gastoDao.obtenerPorFechaFlow(fechaTexto)

    // ==========================================
    // CIERRE DIARIO Y REPORTE WHATSAPP
    // ==========================================

    fun obtenerResumenCierreDia(fechaTexto: String): Flow<ResumenCierreDia> {
        val fEfectivo = transaccionDao.sumarEfectivoPorFechaFlow(fechaTexto)
        val fTransfer = transaccionDao.sumarTransferenciaPorFechaFlow(fechaTexto)
        val fCredito = transaccionDao.sumarCreditoPorFechaFlow(fechaTexto)
        val fAbono = transaccionDao.sumarAbonosPorFechaFlow(fechaTexto)
        val fCancelado = transaccionDao.sumarCanceladosPorFechaFlow(fechaTexto)
        val fTotalVenta = transaccionDao.sumarTotalVentasPorFechaFlow(fechaTexto)

        val fBidonesRecarga = transaccionDao.sumarBidonesRecargaPorFechaFlow(fechaTexto)
        val fBidonesPrestados = transaccionDao.sumarBidonesPrestadosPorFechaFlow(fechaTexto)
        val fBidonesDevueltos = transaccionDao.sumarBidonesDevueltosPorFechaFlow(fechaTexto)
        val fBidonesVendidos = transaccionDao.sumarBidonesVendidosEnvasePorFechaFlow(fechaTexto)

        val fGalones = transaccionDao.sumarGalonesPorFechaFlow(fechaTexto)
        val fPacas = transaccionDao.sumarPacasPorFechaFlow(fechaTexto)
        val fMinis = transaccionDao.sumarMinisPorFechaFlow(fechaTexto)

        val fGastos = gastoDao.sumarGastosPorFechaFlow(fechaTexto)

        val fFinanciero = combine(
            fEfectivo,
            fTransfer,
            fCredito,
            fAbono,
            fCancelado
        ) { ef, tr, cr, ab, ca ->
            FinancialBase(ef, tr, cr, ab, ca)
        }.combine(fTotalVenta) { base, tv ->
            FinancialParts(base.ef, base.tr, base.cr, base.ab, base.ca, tv)
        }

        val fBidones = combine(
            fBidonesRecarga,
            fBidonesPrestados,
            fBidonesDevueltos,
            fBidonesVendidos
        ) { br, bp, bd, bv ->
            BidonesParts(br, bp, bd, bv)
        }

        val fOtros = combine(
            fGalones,
            fPacas,
            fMinis
        ) { gal, pac, min ->
            OtrosParts(gal, pac, min)
        }

        val fProductos = combine(fBidones, fOtros) { b, o ->
            ProductParts(
                bidonesRecarga = b.recarga,
                bidonesPrestados = b.prestados,
                bidonesDevueltos = b.devueltos,
                bidonesVendidosEnvase = b.vendidos,
                galones = o.galones,
                pacas = o.pacas,
                minis = o.minis
            )
        }

        return combine(fFinanciero, fProductos, fGastos) { fin, prod, gastos ->
            val totalBidonesVendidos = prod.bidonesRecarga + prod.bidonesVendidosEnvase
            val totalEfectivoEntrante = fin.efectivo + fin.abono + fin.cancelado
            val efectivoNeto = totalEfectivoEntrante - gastos

            ResumenCierreDia(
                fechaTexto = fechaTexto,
                totalBidonesVendidos = totalBidonesVendidos,
                totalBidonesPrestados = prod.bidonesPrestados,
                totalBidonesDevueltos = prod.bidonesDevueltos,
                totalGalonesVendidos = prod.galones,
                totalPacasVendidas = prod.pacas,
                totalMinisVendidas = prod.minis,
                totalIngresoEfectivo = fin.efectivo,
                totalIngresoTransferencia = fin.transferencia,
                totalCreditosOtorgados = fin.credito,
                totalAbonosRecibidos = fin.abono,
                totalDeudasCanceladas = fin.cancelado,
                totalGastosRuta = gastos,
                efectivoNetoCaja = efectivoNeto,
                granTotalVenta = fin.totalVenta
            )
        }
    }

    /**
     * Formatea el reporte para WhatsApp con desglose exacto por tipo de bidón y sin stock restante.
     */
    fun formatearReporteWhatsApp(
        resumen: ResumenCierreDia,
        estadisticas: EstadisticasVentasTipo,
        fechaDisplay: String
    ): String {
        return buildString {
            appendLine("💧 *REPORTE RUTA AGUA - CIERRE DEL DÍA* 🚚")
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
            appendLine("🚚 *Ruta Agua Camión - Sistema 100% Offline*")
        }
    }

    private data class FinancialBase(
        val ef: Double,
        val tr: Double,
        val cr: Double,
        val ab: Double,
        val ca: Double
    )

    private data class FinancialParts(
        val efectivo: Double,
        val transferencia: Double,
        val credito: Double,
        val abono: Double,
        val cancelado: Double,
        val totalVenta: Double
    )

    private data class BidonesParts(
        val recarga: Int,
        val prestados: Int,
        val devueltos: Int,
        val vendidos: Int
    )

    private data class OtrosParts(
        val galones: Int,
        val pacas: Int,
        val minis: Int
    )

    private data class ProductParts(
        val bidonesRecarga: Int,
        val bidonesPrestados: Int,
        val bidonesDevueltos: Int,
        val bidonesVendidosEnvase: Int,
        val galones: Int,
        val pacas: Int,
        val minis: Int
    )
}

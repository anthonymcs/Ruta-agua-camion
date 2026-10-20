package com.rutaagua.camion.ui.screens.registro

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.rutaagua.camion.data.local.entity.ClienteEntity
import com.rutaagua.camion.data.local.entity.GastoExtraEntity
import com.rutaagua.camion.data.local.entity.InventarioCamionEntity
import com.rutaagua.camion.data.local.entity.TransaccionVentaEntity
import com.rutaagua.camion.data.local.model.FormaPago
import com.rutaagua.camion.data.local.model.ResumenCierreDia
import com.rutaagua.camion.data.local.model.TipoBidon
import com.rutaagua.camion.data.repository.RutaAguaRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class RegistroDiaViewModel(
    private val repository: RutaAguaRepository
) : ViewModel() {

    private val formatoFechaIso = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
    private val formatoFechaDisplay = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())

    val fechaHoyTexto: String = formatoFechaIso.format(Date())
    val fechaHoyDisplay: String = formatoFechaDisplay.format(Date())

    // -------------------------------------------------------------
    // ESTADOS REACTIVOS
    // -------------------------------------------------------------

    // Transacciones del día en vivo
    val transaccionesHoyState: StateFlow<List<TransaccionVentaEntity>> = repository
        .obtenerTransaccionesPorFecha(fechaHoyTexto)
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    // Gastos extras del día
    val gastosHoyState: StateFlow<List<GastoExtraEntity>> = repository
        .obtenerGastosPorFecha(fechaHoyTexto)
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    // Inventario del camión para validar stock restante
    val inventarioCamionState: StateFlow<InventarioCamionEntity?> = repository
        .obtenerInventarioCamion()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = null
        )

    // Resumen diario de arqueo de caja y totales
    val resumenCierreDiaState: StateFlow<ResumenCierreDia> = repository
        .obtenerResumenCierreDia(fechaHoyTexto)
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = ResumenCierreDia(fechaTexto = fechaHoyTexto)
        )

    // Lista de clientes para autocompletado en el modal de venta
    val todosLosClientesState: StateFlow<List<ClienteEntity>> = repository
        .obtenerTodosLosClientes()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    // -------------------------------------------------------------
    // DIÁLOGOS Y MODALES
    // -------------------------------------------------------------
    private val _mostrarModalNuevaVenta = MutableStateFlow(false)
    val mostrarModalNuevaVenta: StateFlow<Boolean> = _mostrarModalNuevaVenta.asStateFlow()

    private val _clientePreseleccionado = MutableStateFlow<ClienteEntity?>(null)
    val clientePreseleccionado: StateFlow<ClienteEntity?> = _clientePreseleccionado.asStateFlow()

    private val _mostrarDialogoGasto = MutableStateFlow(false)
    val mostrarDialogoGasto: StateFlow<Boolean> = _mostrarDialogoGasto.asStateFlow()

    private val _mensajeSnackbar = MutableStateFlow<String?>(null)
    val mensajeSnackbar: StateFlow<String?> = _mensajeSnackbar.asStateFlow()

    fun abrirModalNuevaVenta(cliente: ClienteEntity? = null) {
        _clientePreseleccionado.value = cliente
        _mostrarModalNuevaVenta.value = true
    }

    fun cerrarModalNuevaVenta() {
        _clientePreseleccionado.value = null
        _mostrarModalNuevaVenta.value = false
    }

    fun abrirDialogoGasto() {
        _mostrarDialogoGasto.value = true
    }

    fun cerrarDialogoGasto() {
        _mostrarDialogoGasto.value = false
    }

    fun limpiarMensajeSnackbar() {
        _mensajeSnackbar.value = null
    }

    // -------------------------------------------------------------
    // OPERACIONES DE VENTA Y GASTOS
    // -------------------------------------------------------------

    /**
     * Registra una venta, descuenta del inventario atómicamente y actualiza saldo del cliente.
     */
    fun registrarNuevaVenta(
        clienteId: Long?,
        nombreClientePaso: String?,
        tipoBidon: TipoBidon,
        cantidadRecarga: Int,
        precioUnitarioBidon: Double,
        cantidadPrestados: Int,
        cantidadDevueltos: Int,
        cantidadVendidosEnvase: Int,
        precioUnitarioBidonVendido: Double,
        cantidadGalones: Int,
        cantidadPacas: Int,
        cantidadMinis: Int,
        formaPago: FormaPago,
        montoPagado: Double,
        nota: String = ""
    ) {
        viewModelScope.launch {
            val totalBidonesSalen = cantidadRecarga + cantidadPrestados + cantidadVendidosEnvase
            val inv = inventarioCamionState.value

            // Validación de existencia de botellones en camión
            if (inv != null && totalBidonesSalen > 0) {
                val disponibleTipo = inv.stockPorTipo(tipoBidon)
                if (disponibleTipo < totalBidonesSalen) {
                    _mensajeSnackbar.value = "Stock insuficiente: solo quedan $disponibleTipo bidones ${tipoBidon.name} en el camión"
                    return@launch
                }
            }

            // Cálculo del valor de los productos vendidos
            val subtotalRecarga = cantidadRecarga * precioUnitarioBidon
            val subtotalVendidosEnvase = cantidadVendidosEnvase * precioUnitarioBidonVendido
            val subtotalGalones = cantidadGalones * 0.75
            val subtotalPacas = cantidadPacas * 3.75
            val subtotalMinis = cantidadMinis * 1.75
            val totalVentaHoy = subtotalRecarga + subtotalVendidosEnvase + subtotalGalones + subtotalPacas + subtotalMinis

            var efectivo = 0.0
            var transferencia = 0.0
            var credito = 0.0
            var abono = 0.0
            var cancelado = 0.0

            when (formaPago) {
                FormaPago.EFECTIVO -> efectivo = totalVentaHoy
                FormaPago.TRANSFERENCIA -> transferencia = totalVentaHoy
                FormaPago.CREDITO -> credito = totalVentaHoy
                FormaPago.ABONO -> {
                    // El cliente entrega agua y paga parte de deuda previa o paga una parte
                    efectivo = montoPagado
                    abono = montoPagado
                }
                FormaPago.CANCELADO -> {
                    // El cliente liquida la deuda completa
                    efectivo = montoPagado
                    cancelado = montoPagado
                }
            }

            val transaccion = TransaccionVentaEntity(
                clienteId = clienteId,
                nombreClientePaso = nombreClientePaso,
                fecha = System.currentTimeMillis(),
                fechaTexto = fechaHoyTexto,
                tipoBidon = tipoBidon,
                cantidadBidonesRecarga = cantidadRecarga,
                precioUnitarioBidon = precioUnitarioBidon,
                cantidadBidonesPrestados = cantidadPrestados,
                cantidadBidonesDevueltos = cantidadDevueltos,
                cantidadBidonesVendidosEnvase = cantidadVendidosEnvase,
                precioUnitarioBidonVendido = precioUnitarioBidonVendido,
                cantidadGalones = cantidadGalones,
                precioUnitarioGalon = 0.75,
                cantidadPacas = cantidadPacas,
                precioUnitarioPaca = 3.75,
                cantidadMinis = cantidadMinis,
                precioUnitarioMini = 1.75,
                totalImporte = totalVentaHoy,
                formaPagoPrincipal = formaPago,
                montoEfectivo = efectivo,
                montoTransferencia = transferencia,
                montoCredito = credito,
                montoAbono = abono,
                montoCanceladoDeuda = cancelado,
                nota = nota
            )

            repository.registrarVenta(transaccion)
            _mostrarModalNuevaVenta.value = false
            _clientePreseleccionado.value = null
            _mensajeSnackbar.value = "Venta registrada con éxito (Total: $${String.format(Locale.US, "%.2f", totalVentaHoy)})"
        }
    }

    /**
     * Registra un gasto extra en ruta (almuerzo, colas, comida, taller).
     */
    fun registrarGastoExtra(
        concepto: String,
        monto: Double,
        categoria: String = "RUTA"
    ) {
        if (concepto.isBlank() || monto <= 0) {
            _mensajeSnackbar.value = "Ingresa un concepto y un monto válido para el gasto"
            return
        }

        viewModelScope.launch {
            val gasto = GastoExtraEntity(
                fecha = System.currentTimeMillis(),
                fechaTexto = fechaHoyTexto,
                concepto = concepto.trim(),
                monto = monto,
                categoria = categoria
            )
            repository.registrarGastoExtra(gasto)
            _mostrarDialogoGasto.value = false
            _mensajeSnackbar.value = "Gasto registrado: $${String.format(Locale.US, "%.2f", monto)} ($concepto)"
        }
    }

    fun eliminarGasto(gasto: GastoExtraEntity) {
        viewModelScope.launch {
            repository.eliminarGastoExtra(gasto)
            _mensajeSnackbar.value = "Gasto eliminado"
        }
    }

    /**
     * Genera el reporte oficial de cierre del día con formato listo para WhatsApp.
     */
    fun generarTextoReporteWhatsApp(): String {
        val resumen = resumenCierreDiaState.value
        val inv = inventarioCamionState.value

        return buildString {
            appendLine("💧 *REPORTE RUTA AGUA - CAMIÓN* 🚚")
            appendLine("📅 *Fecha:* $fechaHoyDisplay")
            appendLine("─────────────────────────")
            appendLine("📦 *PRODUCTOS VENDIDOS:*")
            appendLine("• Bidones Vendidos (20L): *${resumen.totalBidonesVendidos}*")
            if (resumen.totalBidonesPrestados > 0) {
                appendLine("• Bidones Prestados (deben envase): *${resumen.totalBidonesPrestados}*")
            }
            if (resumen.totalBidonesDevueltos > 0) {
                appendLine("• Envases Recuperados/Devueltos: *${resumen.totalBidonesDevueltos}*")
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
            appendLine("💰 *CONTABILIDAD DEL DÍA:*")
            appendLine("• Venta Total: *$${String.format(Locale.US, "%.2f", resumen.granTotalVenta)}*")
            appendLine("• Cobro en Efectivo: *$${String.format(Locale.US, "%.2f", resumen.totalIngresoEfectivo)}*")
            if (resumen.totalIngresoTransferencia > 0) {
                appendLine("• Transferencias: *$${String.format(Locale.US, "%.2f", resumen.totalIngresoTransferencia)}*")
            }
            if (resumen.totalCreditosOtorgados > 0) {
                appendLine("• Créditos Otorgados (Fiado): *$${String.format(Locale.US, "%.2f", resumen.totalCreditosOtorgados)}*")
            }
            if (resumen.totalAbonosRecibidos > 0) {
                appendLine("• Abonos a Deudas Previas: *$${String.format(Locale.US, "%.2f", resumen.totalAbonosRecibidos)}*")
            }
            if (resumen.totalDeudasCanceladas > 0) {
                appendLine("• Deudas Liquidadas: *$${String.format(Locale.US, "%.2f", resumen.totalDeudasCanceladas)}*")
            }
            appendLine("─────────────────────────")
            appendLine("🧾 *GASTOS EN RUTA:* *$${String.format(Locale.US, "%.2f", resumen.totalGastosRuta)}*")
            appendLine("💵 *EFECTIVO NETO A ENTREGAR:* *$${String.format(Locale.US, "%.2f", resumen.efectivoNetoCaja)}*")
            if (inv != null) {
                appendLine("─────────────────────────")
                appendLine("🚚 *STOCK RESTANTE EN CAMIÓN:*")
                appendLine("• Transparentes: ${inv.bidonesLlenosTransparentes} | Azules: ${inv.bidonesLlenosAzules} | Marcados: ${inv.bidonesLlenosMarcados}")
                appendLine("• Vacíos en camión: ${inv.bidonesVacios}")
            }
            appendLine("─────────────────────────")
            appendLine("Sistema Móvil Ruta Agua 100% Offline")
        }
    }
}

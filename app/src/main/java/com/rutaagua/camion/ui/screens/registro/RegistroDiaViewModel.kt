package com.rutaagua.camion.ui.screens.registro

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.rutaagua.camion.data.local.entity.ClienteEntity
import com.rutaagua.camion.data.local.entity.GastoExtraEntity
import com.rutaagua.camion.data.local.entity.InventarioCamionEntity
import com.rutaagua.camion.data.local.entity.TransaccionVentaEntity
import com.rutaagua.camion.data.local.model.EstadisticasVentasTipo
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

    // Estadísticas detalladas de ventas por tipo de bidón y producto
    val estadisticasVentasHoyState: StateFlow<EstadisticasVentasTipo> = repository
        .obtenerEstadisticasVentasPorTipo(fechaHoyTexto)
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = EstadisticasVentasTipo()
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

    private val _transaccionEnEdicion = MutableStateFlow<TransaccionVentaEntity?>(null)
    val transaccionEnEdicion: StateFlow<TransaccionVentaEntity?> = _transaccionEnEdicion.asStateFlow()

    private val _transaccionAEliminar = MutableStateFlow<TransaccionVentaEntity?>(null)
    val transaccionAEliminar: StateFlow<TransaccionVentaEntity?> = _transaccionAEliminar.asStateFlow()

    private val _mostrarDialogoGasto = MutableStateFlow(false)
    val mostrarDialogoGasto: StateFlow<Boolean> = _mostrarDialogoGasto.asStateFlow()

    private val _mensajeSnackbar = MutableStateFlow<String?>(null)
    val mensajeSnackbar: StateFlow<String?> = _mensajeSnackbar.asStateFlow()

    fun abrirModalNuevaVenta(cliente: ClienteEntity? = null) {
        _clientePreseleccionado.value = cliente
        _transaccionEnEdicion.value = null
        _mostrarModalNuevaVenta.value = true
    }

    fun abrirEditarVenta(transaccion: TransaccionVentaEntity, cliente: ClienteEntity?) {
        _clientePreseleccionado.value = cliente
        _transaccionEnEdicion.value = transaccion
        _mostrarModalNuevaVenta.value = true
    }

    fun cerrarModalNuevaVenta() {
        _clientePreseleccionado.value = null
        _transaccionEnEdicion.value = null
        _mostrarModalNuevaVenta.value = false
    }

    fun solicitarEliminarVenta(transaccion: TransaccionVentaEntity) {
        _transaccionAEliminar.value = transaccion
    }

    fun cancelarEliminarVenta() {
        _transaccionAEliminar.value = null
    }

    fun confirmarEliminarVenta() {
        val transaccion = _transaccionAEliminar.value ?: return
        viewModelScope.launch {
            repository.eliminarVenta(transaccion)
            _transaccionAEliminar.value = null
            _mensajeSnackbar.value = "Entrega eliminada y stock/saldo revertido correctamente"
        }
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
     * Registra o edita una venta, descontando o ajustando el inventario atómicamente.
     */
    fun registrarOEditarVenta(
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
            val transaccionPrevia = _transaccionEnEdicion.value

            // Validación de existencia de botellones en camión (considerando si es edición)
            if (inv != null && totalBidonesSalen > 0) {
                var disponibleTipo = inv.stockPorTipo(tipoBidon)
                if (transaccionPrevia != null && transaccionPrevia.tipoBidon == tipoBidon) {
                    val antesBajaron = transaccionPrevia.cantidadBidonesRecarga +
                            transaccionPrevia.cantidadBidonesPrestados +
                            transaccionPrevia.cantidadBidonesVendidosEnvase
                    disponibleTipo += antesBajaron
                }
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
                    efectivo = montoPagado
                    abono = montoPagado
                }
                FormaPago.CANCELADO -> {
                    efectivo = montoPagado
                    cancelado = montoPagado
                }
            }

            val nuevaTransaccion = TransaccionVentaEntity(
                id = transaccionPrevia?.id ?: 0L,
                clienteId = clienteId,
                nombreClientePaso = nombreClientePaso,
                fecha = transaccionPrevia?.fecha ?: System.currentTimeMillis(),
                fechaTexto = transaccionPrevia?.fechaTexto ?: fechaHoyTexto,
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

            if (transaccionPrevia != null) {
                repository.editarVenta(transaccionPrevia, nuevaTransaccion)
                _mensajeSnackbar.value = "Entrega actualizada correctamente"
            } else {
                repository.registrarVenta(nuevaTransaccion)
                _mensajeSnackbar.value = "Venta guardada (Total: $${String.format(Locale.US, "%.2f", totalVentaHoy)})"
            }

            _mostrarModalNuevaVenta.value = false
            _clientePreseleccionado.value = null
            _transaccionEnEdicion.value = null
        }
    }

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
     * Genera el reporte oficial de cierre del día con formato listo para WhatsApp (sin stock y con desglose por tipo).
     */
    fun generarTextoReporteWhatsApp(): String {
        val resumen = resumenCierreDiaState.value
        val estadisticas = estadisticasVentasHoyState.value
        return repository.formatearReporteWhatsApp(resumen, estadisticas, fechaHoyDisplay)
    }
}

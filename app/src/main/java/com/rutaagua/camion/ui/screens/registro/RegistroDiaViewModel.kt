package com.rutaagua.camion.ui.screens.registro

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.rutaagua.camion.data.local.entity.CierreCajaEntity
import com.rutaagua.camion.data.local.entity.ClienteEntity
import com.rutaagua.camion.data.local.entity.GastoExtraEntity
import com.rutaagua.camion.data.local.entity.InventarioCamionEntity
import com.rutaagua.camion.data.local.entity.TransaccionVentaEntity
import com.rutaagua.camion.data.local.model.EstadisticasVentasTipo
import com.rutaagua.camion.data.local.model.FormaPago
import com.rutaagua.camion.data.local.model.ResumenCierreDia
import com.rutaagua.camion.data.local.model.TipoBidon
import com.rutaagua.camion.data.local.prefs.AppPreferences
import com.rutaagua.camion.data.local.prefs.RolUsuario
import com.rutaagua.camion.data.repository.RutaAguaRepository
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

enum class FiltroVehiculoRegistro(val titulo: String, val vehiculoKey: String?) {
    CONSOLIDADO("Consolidado", null),
    CAMION("Camión", "CAMION"),
    CAMIONETA("Camioneta", "CAMIONETA")
}

@OptIn(ExperimentalCoroutinesApi::class)
class RegistroDiaViewModel(
    private val repository: RutaAguaRepository,
    private val appPreferences: AppPreferences
) : ViewModel() {

    private val formatoFechaIso = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
    private val formatoFechaDisplay = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())

    val fechaHoyTexto: String = formatoFechaIso.format(Date())
    val fechaHoyDisplay: String = formatoFechaDisplay.format(Date())

    // Filtro rápido de vista: Inicializado directamente con el vehículo activo para aislar operaciones
    val filtroVehiculoState = MutableStateFlow(
        if (appPreferences.obtenerVehiculoActivo() == com.rutaagua.camion.data.local.model.TipoVehiculo.CAMIONETA) {
            FiltroVehiculoRegistro.CAMIONETA
        } else {
            FiltroVehiculoRegistro.CAMION
        }
    )

    // -------------------------------------------------------------
    // ESTADOS REACTIVOS
    // -------------------------------------------------------------

    // Transacciones del día en vivo según filtro seleccionado
    val transaccionesHoyState: StateFlow<List<TransaccionVentaEntity>> = filtroVehiculoState
        .flatMapLatest { filtro ->
            repository.obtenerTransaccionesPorFecha(fechaHoyTexto, filtro.vehiculoKey)
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    // Gastos extras del día según filtro seleccionado
    val gastosHoyState: StateFlow<List<GastoExtraEntity>> = filtroVehiculoState
        .flatMapLatest { filtro ->
            repository.obtenerGastosPorFecha(fechaHoyTexto, filtro.vehiculoKey)
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    // Histórico completo de transacciones del vehículo activo para Reportes (Día / Semanal / Mensual)
    val todasLasTransaccionesVehiculoState: StateFlow<List<TransaccionVentaEntity>> = appPreferences.vehiculoActivo
        .flatMapLatest { vehiculo ->
            repository.obtenerTodasLasTransacciones(vehiculo.name)
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    // Histórico completo de gastos para Reportes (Día / Semanal / Mensual)
    val todosLosGastosState: StateFlow<List<GastoExtraEntity>> = repository
        .obtenerTodosLosGastos()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    // Inventario del vehículo activo para validar stock restante al registrar venta
    val inventarioVehiculoActivoState: StateFlow<InventarioCamionEntity?> = appPreferences.vehiculoActivo
        .flatMapLatest { vehiculo ->
            repository.obtenerInventarioCamion(vehiculo.name)
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = null
        )

    // Resumen diario de arqueo de caja y totales (Consolidado o por Vehículo)
    val resumenCierreDiaState: StateFlow<ResumenCierreDia> = filtroVehiculoState
        .flatMapLatest { filtro ->
            repository.obtenerResumenCierreDia(fechaHoyTexto, filtro.vehiculoKey)
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = ResumenCierreDia(fechaTexto = fechaHoyTexto)
        )

    // Estadísticas detalladas de ventas por tipo de producto
    val estadisticasVentasHoyState: StateFlow<EstadisticasVentasTipo> = filtroVehiculoState
        .flatMapLatest { filtro ->
            repository.obtenerEstadisticasVentasPorTipo(fechaHoyTexto, filtro.vehiculoKey)
        }
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

    // Rol activo y costos de planta
    val rolActivo: StateFlow<RolUsuario> = appPreferences.rolActivo
    val costoPlantaBidon: Double get() = appPreferences.obtenerCostoPlantaBidon()
    val costoPlantaGalon: Double get() = appPreferences.obtenerCostoPlantaGalon()
    val costoPlantaPaca: Double get() = appPreferences.obtenerCostoPlantaPaca()
    val costoPlantaMini: Double get() = appPreferences.obtenerCostoPlantaMini()

    // Cierre de caja de hoy en vivo
    val cierreHoyState: StateFlow<CierreCajaEntity?> = filtroVehiculoState
        .flatMapLatest { filtro ->
            if (filtro.vehiculoKey != null) {
                repository.obtenerCierrePorFechaYVehiculo(fechaHoyTexto, filtro.vehiculoKey)
            } else {
                repository.obtenerUltimoCierrePorFecha(fechaHoyTexto)
            }
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = null
        )

    // -------------------------------------------------------------
    // DIÁLOGOS Y MODALES
    // -------------------------------------------------------------
    private val _mostrarModalNuevaVenta = MutableStateFlow(false)
    val mostrarModalNuevaVenta: StateFlow<Boolean> = _mostrarModalNuevaVenta.asStateFlow()

    private val _mostrarDialogoCierre = MutableStateFlow(false)
    val mostrarDialogoCierre: StateFlow<Boolean> = _mostrarDialogoCierre.asStateFlow()

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

    fun cambiarFiltroVehiculo(filtro: FiltroVehiculoRegistro) {
        filtroVehiculoState.value = filtro
    }

    fun cambiarFiltroSegunVehiculo(tipo: com.rutaagua.camion.data.local.model.TipoVehiculo) {
        filtroVehiculoState.value = if (tipo == com.rutaagua.camion.data.local.model.TipoVehiculo.CAMIONETA) {
            FiltroVehiculoRegistro.CAMIONETA
        } else {
            FiltroVehiculoRegistro.CAMION
        }
    }

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
     * Registra o edita una venta, vinculada al vehículo activo y descontando el inventario atómicamente.
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
        nota: String = "",
        recargaTransparentes: Int = 0,
        recargaAzules: Int = 0,
        recargaMarcados: Int = 0,
        montoEfectivo: Double = 0.0,
        montoTransferencia: Double = 0.0,
        montoCredito: Double = 0.0,
        montoAbono: Double = 0.0,
        montoCanceladoDeuda: Double = 0.0,
        prestadosTransparentes: Int = 0,
        prestadosAzules: Int = 0,
        prestadosMarcados: Int = 0,
        cantidadBidonesGratis: Int = 0,
        totalPersonalizado: Double? = null
    ) {
        viewModelScope.launch {
            val vehiculoOperativo = appPreferences.obtenerVehiculoActivo().name
            val transaccionPrevia = _transaccionEnEdicion.value
            val vehiculoVenta = transaccionPrevia?.vehiculo ?: vehiculoOperativo

            val inv = inventarioVehiculoActivoState.value
            val tieneMultiBidon = (recargaTransparentes + recargaAzules + recargaMarcados) > 0
            val tieneMultiPrestados = (prestadosTransparentes + prestadosAzules + prestadosMarcados) > 0

            val finalRecTrans = if (tieneMultiBidon) recargaTransparentes else if (tipoBidon == TipoBidon.TRANSPARENTE) cantidadRecarga else 0
            val finalRecAzul = if (tieneMultiBidon) recargaAzules else if (tipoBidon == TipoBidon.AZUL) cantidadRecarga else 0
            val finalRecMarc = if (tieneMultiBidon) recargaMarcados else if (tipoBidon == TipoBidon.MARCADO) cantidadRecarga else 0

            val finalPrestTrans = if (tieneMultiPrestados) prestadosTransparentes else if (tipoBidon == TipoBidon.TRANSPARENTE) cantidadPrestados else 0
            val finalPrestAzul = if (tieneMultiPrestados) prestadosAzules else if (tipoBidon == TipoBidon.AZUL) cantidadPrestados else 0
            val finalPrestMarc = if (tieneMultiPrestados) prestadosMarcados else if (tipoBidon == TipoBidon.MARCADO) cantidadPrestados else 0

            val totalBidonesSalen = cantidadRecarga + cantidadPrestados + cantidadVendidosEnvase

            // Validación de existencia total en el vehículo
            if (inv != null && totalBidonesSalen > 0) {
                val antesBajaron = if (transaccionPrevia != null) {
                    transaccionPrevia.cantidadBidonesRecarga +
                            transaccionPrevia.cantidadBidonesPrestados +
                            transaccionPrevia.cantidadBidonesVendidosEnvase
                } else 0
                val totalDisponibleVehiculo = inv.totalBidonesLlenos() + antesBajaron
                if (totalBidonesSalen > totalDisponibleVehiculo && totalDisponibleVehiculo > 0) {
                    _mensajeSnackbar.value = "Stock insuficiente en $vehiculoVenta: piden $totalBidonesSalen bidones y quedan $totalDisponibleVehiculo"
                    return@launch
                }
            }

            // Cálculo del valor de los productos vendidos (Recargas + Prestados se cobran, restando cortesías ej. "35+1")
            val bidonesAguaACobrar = maxOf(0, cantidadRecarga + cantidadPrestados - cantidadBidonesGratis)
            val subtotalAgua = bidonesAguaACobrar * precioUnitarioBidon
            val subtotalVendidosEnvase = cantidadVendidosEnvase * precioUnitarioBidonVendido
            val subtotalGalones = cantidadGalones * 0.75
            val subtotalPacas = cantidadPacas * 3.75
            val subtotalMinis = cantidadMinis * 1.75
            val totalCalculado = subtotalAgua + subtotalVendidosEnvase + subtotalGalones + subtotalPacas + subtotalMinis
            val totalVentaHoy = totalPersonalizado ?: totalCalculado

            val usoDesgloseFinanciero = (montoEfectivo + montoTransferencia + montoCredito + montoAbono + montoCanceladoDeuda) > 0.0

            var finalEfectivo = 0.0
            var finalTransferencia = 0.0
            var finalCredito = 0.0
            var finalAbono = 0.0
            var finalCancelado = 0.0

            if (usoDesgloseFinanciero) {
                finalEfectivo = montoEfectivo
                finalTransferencia = montoTransferencia
                finalCredito = montoCredito
                finalAbono = montoAbono
                finalCancelado = montoCanceladoDeuda
            } else {
                when (formaPago) {
                    FormaPago.EFECTIVO -> finalEfectivo = totalVentaHoy
                    FormaPago.TRANSFERENCIA -> finalTransferencia = totalVentaHoy
                    FormaPago.CREDITO -> finalCredito = totalVentaHoy
                    FormaPago.ABONO -> {
                        finalEfectivo = montoPagado
                        finalAbono = montoPagado
                    }
                    FormaPago.CANCELADO -> {
                        finalEfectivo = montoPagado
                        finalCancelado = montoPagado
                    }
                }
            }

            val nuevaTransaccion = TransaccionVentaEntity(
                id = transaccionPrevia?.id ?: 0L,
                vehiculo = vehiculoVenta,
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
                recargaTransparentes = finalRecTrans,
                recargaAzules = finalRecAzul,
                recargaMarcados = finalRecMarc,
                prestadosTransparentes = finalPrestTrans,
                prestadosAzules = finalPrestAzul,
                prestadosMarcados = finalPrestMarc,
                cantidadBidonesGratis = cantidadBidonesGratis,
                envasesVendidosTransparentes = if (tipoBidon == TipoBidon.TRANSPARENTE) cantidadVendidosEnvase else 0,
                envasesVendidosAzules = if (tipoBidon == TipoBidon.AZUL) cantidadVendidosEnvase else 0,
                envasesVendidosMarcados = if (tipoBidon == TipoBidon.MARCADO) cantidadVendidosEnvase else 0,
                cantidadGalones = cantidadGalones,
                precioUnitarioGalon = 0.75,
                cantidadPacas = cantidadPacas,
                precioUnitarioPaca = 3.75,
                cantidadMinis = cantidadMinis,
                precioUnitarioMini = 1.75,
                totalImporte = totalVentaHoy,
                formaPagoPrincipal = formaPago,
                montoEfectivo = finalEfectivo,
                montoTransferencia = finalTransferencia,
                montoCredito = finalCredito,
                montoAbono = finalAbono,
                montoCanceladoDeuda = finalCancelado,
                nota = nota
            )

            if (transaccionPrevia != null) {
                repository.editarVenta(transaccionPrevia, nuevaTransaccion)
                _mensajeSnackbar.value = "Entrega actualizada"
            } else {
                repository.registrarVenta(nuevaTransaccion)
                _mensajeSnackbar.value = "Entrega guardada ($${String.format(Locale.US, "%.2f", totalVentaHoy)})"
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
            val vehiculoOperativo = appPreferences.obtenerVehiculoActivo().name
            val gasto = GastoExtraEntity(
                vehiculo = vehiculoOperativo,
                fecha = System.currentTimeMillis(),
                fechaTexto = fechaHoyTexto,
                concepto = concepto.trim(),
                monto = monto,
                categoria = categoria
            )
            repository.registrarGastoExtra(gasto)
            _mostrarDialogoGasto.value = false
            _mensajeSnackbar.value = "Gasto en $vehiculoOperativo: $${String.format(Locale.US, "%.2f", monto)} ($concepto)"
        }
    }

    fun eliminarGasto(gasto: GastoExtraEntity) {
        viewModelScope.launch {
            repository.eliminarGastoExtra(gasto)
            _mensajeSnackbar.value = "Gasto eliminado"
        }
    }

    fun abrirDialogoCierre() {
        _mostrarDialogoCierre.value = true
    }

    fun cerrarDialogoCierre() {
        _mostrarDialogoCierre.value = false
    }

    fun registrarCierreCaja(
        efectivoDeclarado: Double,
        transferenciasDeclaradas: Double,
        observaciones: String
    ) {
        viewModelScope.launch {
            val resumen = resumenCierreDiaState.value
            val vehiculoActual = appPreferences.obtenerVehiculoActivo().name

            val efectivoSistema = resumen.efectivoNetoCaja
            val transferenciasSistema = resumen.totalIngresoTransferencia
            val totalSistema = efectivoSistema + transferenciasSistema
            val totalDeclarado = efectivoDeclarado + transferenciasDeclaradas

            val diferenciaEfectivo = efectivoDeclarado - efectivoSistema
            val diferenciaTransferencia = transferenciasDeclaradas - transferenciasSistema
            val diferenciaTotal = totalDeclarado - totalSistema

            val costoTotalPlanta = (resumen.totalBidonesVendidos * costoPlantaBidon) +
                    (resumen.totalGalonesVendidos * costoPlantaGalon) +
                    (resumen.totalPacasVendidas * costoPlantaPaca) +
                    (resumen.totalMinisVendidas * costoPlantaMini)

            val utilidadBruta = maxOf(0.0, resumen.granTotalVenta - costoTotalPlanta)
            val utilidadNeta = utilidadBruta - resumen.totalGastosRuta

            val cierreExistente = cierreHoyState.value
            val cierreEntity = CierreCajaEntity(
                id = cierreExistente?.id ?: 0L,
                fechaIso = fechaHoyTexto,
                timestamp = System.currentTimeMillis(),
                vehiculo = vehiculoActual,
                efectivoDeclarado = efectivoDeclarado,
                transferenciasDeclaradas = transferenciasDeclaradas,
                totalDeclarado = totalDeclarado,
                observaciones = observaciones,
                efectivoSistema = efectivoSistema,
                transferenciasSistema = transferenciasSistema,
                totalSistema = totalSistema,
                diferenciaEfectivo = diferenciaEfectivo,
                diferenciaTransferencia = diferenciaTransferencia,
                diferenciaTotal = diferenciaTotal,
                bidonesVendidos = resumen.totalBidonesVendidos,
                galonesVendidos = resumen.totalGalonesVendidos,
                pacasVendidas = resumen.totalPacasVendidas,
                minisVendidas = resumen.totalMinisVendidas,
                gastosRuta = resumen.totalGastosRuta,
                costoTotalPlanta = costoTotalPlanta,
                utilidadBruta = utilidadBruta,
                utilidadNeta = utilidadNeta,
                sincronizado = false
            )

            repository.guardarCierreCaja(cierreEntity)
            _mensajeSnackbar.value = "Cierre de caja guardado exitosamente"
        }
    }

    /**
     * Genera el reporte oficial de cierre del día con formato listo para WhatsApp (sin stock y con desglose por tipo).
     */
    fun generarTextoReporteWhatsApp(): String {
        val resumen = resumenCierreDiaState.value
        val estadisticas = estadisticasVentasHoyState.value
        val filtroActual = filtroVehiculoState.value
        val transacciones = transaccionesHoyState.value
        val clientes = todosLosClientesState.value

        val vehiculoLabel = when (filtroActual) {
            FiltroVehiculoRegistro.CONSOLIDADO -> "Consolidado General"
            FiltroVehiculoRegistro.CAMION -> "Camión"
            FiltroVehiculoRegistro.CAMIONETA -> "Camioneta"
        }

        val prestamosDetalle = transacciones
            .filter { it.cantidadBidonesPrestados > 0 }
            .map { tx ->
                val nombre = clientes.find { it.id == tx.clienteId }?.nombre
                    ?: tx.nombreClientePaso?.takeIf { it.isNotBlank() }
                    ?: "Cliente"
                Pair(nombre, tx.cantidadBidonesPrestados)
            }

        val totalGratisHoy = transacciones.sumOf { it.cantidadBidonesGratis }

        return repository.formatearReporteWhatsApp(
            resumen = resumen,
            estadisticas = estadisticas,
            fechaDisplay = fechaHoyDisplay,
            vehiculoLabel = vehiculoLabel,
            prestamosDetalle = prestamosDetalle,
            totalGratisHoy = totalGratisHoy
        )
    }
}

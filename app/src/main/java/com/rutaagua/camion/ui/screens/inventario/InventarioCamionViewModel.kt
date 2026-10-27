package com.rutaagua.camion.ui.screens.inventario

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.rutaagua.camion.data.local.entity.InventarioCamionEntity
import com.rutaagua.camion.data.local.entity.RecargaPlantaEntity
import com.rutaagua.camion.data.local.model.EstadisticasVentasTipo
import com.rutaagua.camion.data.local.model.RecargaCalculada
import com.rutaagua.camion.data.local.model.StockAlertas
import com.rutaagua.camion.data.local.model.TipoVehiculo
import com.rutaagua.camion.data.local.prefs.AppPreferences
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

@OptIn(ExperimentalCoroutinesApi::class)
class InventarioCamionViewModel(
    private val repository: RutaAguaRepository,
    private val appPreferences: AppPreferences
) : ViewModel() {

    private val formatoFecha = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
    val fechaHoyTexto: String = formatoFecha.format(Date())

    val vehiculoActivoState: StateFlow<TipoVehiculo> = appPreferences.vehiculoActivo

    // Estado del inventario en tiempo real según vehículo activo (Camión / Camioneta)
    val inventarioState: StateFlow<InventarioCamionEntity?> = vehiculoActivoState
        .flatMapLatest { vehiculo ->
            repository.obtenerInventarioCamion(vehiculo.name)
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = null
        )

    // Cálculo automático de recargas faltantes en planta
    val calculoRecargaState: StateFlow<RecargaCalculada?> = vehiculoActivoState
        .flatMapLatest { vehiculo ->
            repository.obtenerCalculoRecargaPlanta(vehiculo.name)
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = null
        )

    // Alertas de stock crítico en el vehículo activo
    val alertasStockState: StateFlow<StockAlertas> = vehiculoActivoState
        .flatMapLatest { vehiculo ->
            repository.obtenerAlertasStock(vehiculo.name)
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = StockAlertas()
        )

    // Historial de recargas de hoy en planta para el vehículo activo
    val recargasHoyState: StateFlow<List<RecargaPlantaEntity>> = vehiculoActivoState
        .flatMapLatest { vehiculo ->
            repository.obtenerRecargasPorFecha(fechaHoyTexto, vehiculo.name)
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    // Estadísticas de ventas de hoy por tipo de bidón y producto para el vehículo activo
    val estadisticasVentasHoyState: StateFlow<EstadisticasVentasTipo> = vehiculoActivoState
        .flatMapLatest { vehiculo ->
            repository.obtenerEstadisticasVentasPorTipo(fechaHoyTexto, vehiculo.name)
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = EstadisticasVentasTipo()
        )

    // Control de diálogos UI
    private val _mostrarDialogoAjusteCapacidad = MutableStateFlow(false)
    val mostrarDialogoAjusteCapacidad: StateFlow<Boolean> = _mostrarDialogoAjusteCapacidad.asStateFlow()

    private val _mostrarModalRecargaPlanta = MutableStateFlow(false)
    val mostrarModalRecargaPlanta: StateFlow<Boolean> = _mostrarModalRecargaPlanta.asStateFlow()

    private val _mensajeSnackbar = MutableStateFlow<String?>(null)
    val mensajeSnackbar: StateFlow<String?> = _mensajeSnackbar.asStateFlow()

    fun abrirDialogoCapacidad() {
        _mostrarDialogoAjusteCapacidad.value = true
    }

    fun cerrarDialogoCapacidad() {
        _mostrarDialogoAjusteCapacidad.value = false
    }

    // Control de edición y eliminación de recargas
    private val _recargaEnEdicion = MutableStateFlow<RecargaPlantaEntity?>(null)
    val recargaEnEdicion: StateFlow<RecargaPlantaEntity?> = _recargaEnEdicion.asStateFlow()

    fun abrirModalRecarga(recargaAEditar: RecargaPlantaEntity? = null) {
        _recargaEnEdicion.value = recargaAEditar
        _mostrarModalRecargaPlanta.value = true
    }

    fun cerrarModalRecarga() {
        _recargaEnEdicion.value = null
        _mostrarModalRecargaPlanta.value = false
    }

    fun eliminarRecargaPlanta(recarga: RecargaPlantaEntity) {
        viewModelScope.launch {
            repository.eliminarRecargaPlanta(recarga)
            _mensajeSnackbar.value = "Recarga de vuelta ${recarga.numeroVuelta} eliminada y stock revertido"
        }
    }

    fun limpiarMensajeSnackbar() {
        _mensajeSnackbar.value = null
    }

    /**
     * Actualiza la capacidad máxima del vehículo actual (ej. 218 en Camión, 80 en Camioneta).
     */
    fun cambiarCapacidadTope(
        topeBidones: Int,
        topeGalones: Int = 90,
        topePacas: Int = 6,
        topeMinis: Int = 2
    ) {
        viewModelScope.launch {
            val vehiculo = appPreferences.obtenerVehiculoActivo().name
            repository.actualizarCapacidadTope(
                vehiculo = vehiculo,
                topeBidones = topeBidones,
                topeGalones = topeGalones,
                topePacas = topePacas,
                topeMinis = topeMinis
            )
            _mostrarDialogoAjusteCapacidad.value = false
            _mensajeSnackbar.value = "Meta de $vehiculo ajustada a $topeBidones bidones"
        }
    }

    /**
     * Ejecuta o edita la recarga en planta purificadora (Vuelta 1 o 2).
     */
    fun registrarRecargaPlanta(
        numeroVuelta: Int,
        cargadosTransparentes: Int,
        cargadosAzules: Int,
        cargadosMarcados: Int,
        vaciosDescargados: Int,
        galonesCargados: Int = 0,
        pacasCargadas: Int = 0,
        minisCargadas: Int = 0,
        observaciones: String = ""
    ) {
        viewModelScope.launch {
            val vehiculo = appPreferences.obtenerVehiculoActivo().name
            val recargaPrevia = _recargaEnEdicion.value

            val recarga = RecargaPlantaEntity(
                id = recargaPrevia?.id ?: 0L,
                vehiculo = recargaPrevia?.vehiculo ?: vehiculo,
                fecha = recargaPrevia?.fecha ?: System.currentTimeMillis(),
                fechaTexto = recargaPrevia?.fechaTexto ?: fechaHoyTexto,
                numeroVuelta = numeroVuelta,
                bidonesCargadosTransparentes = cargadosTransparentes,
                bidonesCargadosAzules = cargadosAzules,
                bidonesCargadosMarcados = cargadosMarcados,
                bidonesVaciosDescargados = vaciosDescargados,
                galonesCargados = galonesCargados,
                pacasCargadas = pacasCargadas,
                minisCargadas = minisCargadas,
                observaciones = observaciones
            )

            if (recargaPrevia != null) {
                repository.editarRecargaPlanta(recargaPrevia, recarga)
                _mensajeSnackbar.value = "Recarga actualizada en $vehiculo (Vuelta $numeroVuelta)"
            } else {
                repository.registrarRecargaPlanta(recarga)
                val total = cargadosTransparentes + cargadosAzules + cargadosMarcados
                _mensajeSnackbar.value = "Recarga Planta $vehiculo (Vuelta $numeroVuelta): +$total bidones"
            }

            _recargaEnEdicion.value = null
            _mostrarModalRecargaPlanta.value = false
        }
    }
}

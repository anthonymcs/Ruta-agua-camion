package com.rutaagua.camion.ui.screens.inventario

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.rutaagua.camion.data.local.entity.InventarioCamionEntity
import com.rutaagua.camion.data.local.entity.RecargaPlantaEntity
import com.rutaagua.camion.data.local.model.EstadisticasVentasTipo
import com.rutaagua.camion.data.local.model.RecargaCalculada
import com.rutaagua.camion.data.local.model.StockAlertas
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

class InventarioCamionViewModel(
    private val repository: RutaAguaRepository
) : ViewModel() {

    private val formatoFecha = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
    val fechaHoyTexto: String = formatoFecha.format(Date())

    // Estado del inventario en tiempo real
    val inventarioState: StateFlow<InventarioCamionEntity?> = repository
        .obtenerInventarioCamion()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = null
        )

    // Cálculo automático de recargas faltantes en planta
    val calculoRecargaState: StateFlow<RecargaCalculada?> = repository
        .obtenerCalculoRecargaPlanta()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = null
        )

    // Alertas de stock crítico en camión
    val alertasStockState: StateFlow<StockAlertas> = repository
        .obtenerAlertasStock()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = StockAlertas()
        )

    // Historial de recargas de hoy en planta
    val recargasHoyState: StateFlow<List<RecargaPlantaEntity>> = repository
        .obtenerRecargasPorFecha(fechaHoyTexto)
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    // Estadísticas de ventas de hoy por tipo de bidón y producto
    val estadisticasVentasHoyState: StateFlow<EstadisticasVentasTipo> = repository
        .obtenerEstadisticasVentasPorTipo(fechaHoyTexto)
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

    fun abrirModalRecarga() {
        _mostrarModalRecargaPlanta.value = true
    }

    fun cerrarModalRecarga() {
        _mostrarModalRecargaPlanta.value = false
    }

    fun limpiarMensajeSnackbar() {
        _mensajeSnackbar.value = null
    }

    /**
     * Actualiza la capacidad máxima del camión según cómo haya salido el día (ej. 218, 189, 164, 163).
     */
    fun cambiarCapacidadTope(
        topeBidones: Int,
        topeGalones: Int = 90,
        topePacas: Int = 6,
        topeMinis: Int = 2
    ) {
        viewModelScope.launch {
            repository.actualizarCapacidadTope(
                topeBidones = topeBidones,
                topeGalones = topeGalones,
                topePacas = topePacas,
                topeMinis = topeMinis
            )
            _mostrarDialogoAjusteCapacidad.value = false
            _mensajeSnackbar.value = "Meta del camión ajustada a $topeBidones bidones"
        }
    }

    /**
     * Ejecuta la recarga en planta purificadora (Vuelta 1 o 2).
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
            val recarga = RecargaPlantaEntity(
                fecha = System.currentTimeMillis(),
                fechaTexto = fechaHoyTexto,
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
            repository.registrarRecargaPlanta(recarga)
            _mostrarModalRecargaPlanta.value = false
            val total = cargadosTransparentes + cargadosAzules + cargadosMarcados
            _mensajeSnackbar.value = "Recarga Planta (Vuelta $numeroVuelta) guardada: +$total bidones"
        }
    }
}

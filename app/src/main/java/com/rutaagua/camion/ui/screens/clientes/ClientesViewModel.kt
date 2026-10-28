package com.rutaagua.camion.ui.screens.clientes

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.rutaagua.camion.data.local.entity.ClienteEntity
import com.rutaagua.camion.data.local.entity.TransaccionVentaEntity
import com.rutaagua.camion.data.local.entity.VisitaFallidaEntity
import com.rutaagua.camion.data.local.model.DiaSemana
import com.rutaagua.camion.data.local.model.TipoBidon
import com.rutaagua.camion.data.local.model.TipoCliente
import com.rutaagua.camion.data.repository.RutaAguaRepository
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

enum class FiltroCliente(val etiqueta: String) {
    TODOS("Todos"),
    RUTA("Ruta"),
    LLAMADA("Llamada"),
    CON_DEUDA("Con Deuda $"),
    CON_BIDONES_PRESTADOS("Deben Envases")
}

enum class EstadoEntregaRuta {
    ATENDIDO,
    NO_ESTABA,
    PENDIENTE,
    PROGRAMADO
}

data class ClienteRutaItem(
    val cliente: ClienteEntity,
    val estado: EstadoEntregaRuta,
    val bidonesEntregados: Int = 0,
    val vehiculoEntrega: String? = null,
    val horaEntrega: String? = null,
    val horaVisitaFallida: String? = null,
    val fotoEvidenciaPath: String? = null
)

@OptIn(ExperimentalCoroutinesApi::class)
class ClientesViewModel(
    private val repository: RutaAguaRepository
) : ViewModel() {

    private val formatoFechaIso = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
    private val formatoMesIso = SimpleDateFormat("yyyy-MM", Locale.getDefault())
    val fechaHoyIso: String = formatoFechaIso.format(Date())
    val mesActualIso: String = formatoMesIso.format(Date())

    // Lista completa de clientes para mapear nombres e información en el historial
    val todosLosClientesState: StateFlow<List<ClienteEntity>> = repository
        .obtenerTodosLosClientes()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    // -------------------------------------------------------------
    // DIRECTORIO DE CLIENTES
    // -------------------------------------------------------------
    val busquedaQuery = MutableStateFlow("")
    val filtroSeleccionado = MutableStateFlow(FiltroCliente.TODOS)

    val clientesFiltradosState: StateFlow<List<ClienteEntity>> = combine(
        todosLosClientesState,
        busquedaQuery,
        filtroSeleccionado
    ) { lista, query, filtro ->
        lista.filter { cliente ->
            val coincideTexto = query.isBlank() ||
                    cliente.nombre.contains(query, ignoreCase = true) ||
                    cliente.ubicacion.contains(query, ignoreCase = true) ||
                    cliente.telefono.contains(query, ignoreCase = true)

            val coincideFiltro = when (filtro) {
                FiltroCliente.TODOS -> true
                FiltroCliente.RUTA -> cliente.tipoCliente == TipoCliente.RUTA
                FiltroCliente.LLAMADA -> cliente.tipoCliente == TipoCliente.LLAMADA
                FiltroCliente.CON_DEUDA -> cliente.deudaMonetaria > 0.0
                FiltroCliente.CON_BIDONES_PRESTADOS -> cliente.bidonesPrestados > 0
            }

            coincideTexto && coincideFiltro
        }
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    // Mapa de entregas de hoy para mostrar badges cruzados de qué vehículo atendió al cliente
    val transaccionesHoyClientesState: StateFlow<Map<Long, TransaccionVentaEntity>> = repository
        .obtenerTransaccionesPorFecha(fechaHoyIso)
        .map { transacciones ->
            transacciones.filter { it.clienteId != null }.associateBy { it.clienteId!! }
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyMap()
        )

    // Mapa de visitas fallidas de hoy ("No estaba en casa")
    val visitasFallidasHoyState: StateFlow<Map<Long, VisitaFallidaEntity>> = repository
        .obtenerVisitasFallidasPorFecha(fechaHoyIso)
        .map { visitas ->
            visitas.associateBy { it.clienteId }
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyMap()
        )

    // Estados para BottomSheet de Crear/Editar
    val clienteEnEdicion = MutableStateFlow<ClienteEntity?>(null)
    val mostrarModalCliente = MutableStateFlow(false)

    // Estado para Diálogo de Confirmación de Eliminación
    val clienteAEliminar = MutableStateFlow<ClienteEntity?>(null)

    val mensajeSnackbar = MutableStateFlow<String?>(null)

    // -------------------------------------------------------------
    // HISTORIAL COMPLETO DE UN CLIENTE ESPECÍFICO (TODOS LOS MESES/AÑOS)
    // -------------------------------------------------------------
    private val _clienteHistorialSeleccionado = MutableStateFlow<ClienteEntity?>(null)
    val clienteHistorialSeleccionado: StateFlow<ClienteEntity?> = _clienteHistorialSeleccionado.asStateFlow()

    val historialTransaccionesClienteState: StateFlow<List<TransaccionVentaEntity>> = _clienteHistorialSeleccionado
        .flatMapLatest { cliente ->
            if (cliente == null) {
                flowOf(emptyList())
            } else {
                repository.obtenerTransaccionesDeCliente(cliente.id)
            }
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    val historialVisitasClienteState: StateFlow<List<VisitaFallidaEntity>> = _clienteHistorialSeleccionado
        .flatMapLatest { cliente ->
            if (cliente == null) {
                flowOf(emptyList())
            } else {
                repository.obtenerVisitasFallidasPorCliente(cliente.id)
            }
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    fun abrirHistorialCliente(cliente: ClienteEntity) {
        _clienteHistorialSeleccionado.value = cliente
    }

    fun cerrarHistorialCliente() {
        _clienteHistorialSeleccionado.value = null
    }

    // -------------------------------------------------------------
    // EXPLORADOR HISTÓRICO POR MES, DÍA ESPECÍFICO Y CLIENTE
    // -------------------------------------------------------------
    val mesHistoricoSeleccionado = MutableStateFlow(mesActualIso)
    // Si fechaHistoricaSeleccionada es null, muestra todos los registros del mes; si tiene valor ("yyyy-MM-dd"), filtra ese día exacto
    val fechaHistoricaSeleccionada = MutableStateFlow<String?>(fechaHoyIso)
    val busquedaHistoricaCliente = MutableStateFlow("")

    val transaccionesMesHistoricoState: StateFlow<List<TransaccionVentaEntity>> = mesHistoricoSeleccionado
        .flatMapLatest { prefijoMes ->
            repository.obtenerTransaccionesPorMes(prefijoMes)
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    val visitasMesHistoricoState: StateFlow<List<VisitaFallidaEntity>> = mesHistoricoSeleccionado
        .flatMapLatest { prefijoMes ->
            repository.obtenerVisitasFallidasPorMes(prefijoMes)
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    fun cambiarMesHistorico(deltaMeses: Int) {
        try {
            val partes = mesHistoricoSeleccionado.value.split("-")
            val anio = partes[0].toInt()
            val mes = partes[1].toInt() - 1
            val cal = Calendar.getInstance().apply {
                set(Calendar.YEAR, anio)
                set(Calendar.MONTH, mes)
                set(Calendar.DAY_OF_MONTH, 1)
                add(Calendar.MONTH, deltaMeses)
            }
            val nuevoMesIso = formatoMesIso.format(cal.time)
            mesHistoricoSeleccionado.value = nuevoMesIso
            // Si cambiamos al mes actual seleccionamos hoy; si es un mes pasado mostramos todo el mes por defecto
            fechaHistoricaSeleccionada.value = if (nuevoMesIso == mesActualIso) fechaHoyIso else null
        } catch (_: Exception) {
        }
    }

    fun seleccionarMesActualHistorico() {
        mesHistoricoSeleccionado.value = mesActualIso
        fechaHistoricaSeleccionada.value = fechaHoyIso
    }

    fun seleccionarFechaHistorica(fechaIso: String?) {
        fechaHistoricaSeleccionada.value = fechaIso
    }

    fun onBusquedaHistoricaChange(query: String) {
        busquedaHistoricaCliente.value = query
    }

    // -------------------------------------------------------------
    // CALENDARIO DE RUTA SEMANAL (LUNES A SÁBADO)
    // -------------------------------------------------------------
    val diaSeleccionadoCalendario = MutableStateFlow(obtenerDiaSemanaActual())

    // Clientes asignados al día seleccionado con su fecha exacta correspondiente
    val clientesRutaDiaState: StateFlow<List<ClienteRutaItem>> = diaSeleccionadoCalendario
        .flatMapLatest { dia ->
            val fechaDia = obtenerFechaParaDiaSemana(dia)
            val esDiaFuturo = fechaDia > fechaHoyIso

            combine(
                repository.obtenerClientesPorDiaRuta(dia.name),
                repository.obtenerTransaccionesPorFecha(fechaDia),
                repository.obtenerVisitasFallidasPorFecha(fechaDia)
            ) { clientesRuta, transaccionesDelDia, visitasDelDia ->
                val horaFormat = SimpleDateFormat("HH:mm", Locale.getDefault())
                clientesRuta.map { cliente ->
                    if (esDiaFuturo) {
                        ClienteRutaItem(
                            cliente = cliente,
                            estado = EstadoEntregaRuta.PROGRAMADO,
                            bidonesEntregados = 0
                        )
                    } else {
                        val transaccionesCliente = transaccionesDelDia.filter { it.clienteId == cliente.id }
                        val fueAtendido = transaccionesCliente.isNotEmpty()
                        val visitaFallida = visitasDelDia.firstOrNull { it.clienteId == cliente.id }

                        if (fueAtendido) {
                            val bidones = transaccionesCliente.sumOf {
                                it.cantidadBidonesRecarga + it.cantidadBidonesPrestados + it.cantidadBidonesVendidosEnvase
                            }
                            val ultimaTx = transaccionesCliente.maxByOrNull { it.fecha }
                            val vehiculo = ultimaTx?.vehiculo
                            val hora = ultimaTx?.let { horaFormat.format(Date(it.fecha)) }

                            ClienteRutaItem(
                                cliente = cliente,
                                estado = EstadoEntregaRuta.ATENDIDO,
                                bidonesEntregados = bidones,
                                vehiculoEntrega = vehiculo,
                                horaEntrega = hora
                            )
                        } else if (visitaFallida != null) {
                            ClienteRutaItem(
                                cliente = cliente,
                                estado = EstadoEntregaRuta.NO_ESTABA,
                                vehiculoEntrega = visitaFallida.vehiculo,
                                horaVisitaFallida = visitaFallida.horaDisplay,
                                fotoEvidenciaPath = visitaFallida.fotoEvidenciaPath
                            )
                        } else {
                            ClienteRutaItem(
                                cliente = cliente,
                                estado = EstadoEntregaRuta.PENDIENTE
                            )
                        }
                    }
                }
            }
        }.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    // Conteo de clientes asignados a cada día de la semana
    val conteoPorDiaSemanaState: StateFlow<Map<DiaSemana, Int>> = todosLosClientesState
        .map { clientes ->
            DiaSemana.entries.associateWith { dia ->
                clientes.count { c ->
                    c.tipoCliente == TipoCliente.RUTA && c.diasVisita.contains(dia.name)
                }
            }
        }.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyMap()
        )

    // -------------------------------------------------------------
    // MÉTODOS DE CLIENTES (CRUD)
    // -------------------------------------------------------------
    fun onQueryChange(nuevaQuery: String) {
        busquedaQuery.value = nuevaQuery
    }

    fun onFiltroSelect(filtro: FiltroCliente) {
        filtroSeleccionado.value = filtro
    }

    fun abrirCrearCliente() {
        clienteEnEdicion.value = null
        mostrarModalCliente.value = true
    }

    fun abrirEditarCliente(cliente: ClienteEntity) {
        clienteEnEdicion.value = cliente
        mostrarModalCliente.value = true
    }

    fun cerrarModalCliente() {
        clienteEnEdicion.value = null
        mostrarModalCliente.value = false
    }

    fun solicitarEliminarCliente(cliente: ClienteEntity) {
        clienteAEliminar.value = cliente
    }

    fun cancelarEliminarCliente() {
        clienteAEliminar.value = null
    }

    fun confirmarEliminarCliente() {
        val cliente = clienteAEliminar.value ?: return
        viewModelScope.launch {
            repository.eliminarCliente(cliente)
            clienteAEliminar.value = null
            mensajeSnackbar.value = "Cliente \"${cliente.nombre}\" eliminado"
        }
    }

    fun guardarCliente(
        id: Long,
        nombre: String,
        telefono: String,
        ubicacion: String,
        tipoCliente: TipoCliente,
        diasVisita: List<DiaSemana>,
        tipoBidonPreferido: TipoBidon,
        precioHabitual: Double,
        deudaInicial: Double = 0.0,
        bidonesPrestadosInicial: Int = 0,
        fotoReferenciaPath: String? = null
    ) {
        if (nombre.isBlank()) {
            mensajeSnackbar.value = "El nombre del cliente no puede estar vacío"
            return
        }

        viewModelScope.launch {
            val diasTexto = if (tipoCliente == TipoCliente.RUTA) {
                diasVisita.joinToString(",") { it.name }
            } else ""

            val cliente = ClienteEntity(
                id = id,
                nombre = nombre.trim(),
                telefono = telefono.trim(),
                ubicacion = ubicacion.trim(),
                tipoCliente = tipoCliente,
                diasVisita = diasTexto,
                tipoBidonPreferido = tipoBidonPreferido,
                precioHabitual = precioHabitual,
                deudaMonetaria = deudaInicial,
                bidonesPrestados = bidonesPrestadosInicial,
                fotoReferenciaPath = fotoReferenciaPath,
                activo = true
            )

            repository.guardarCliente(cliente)
            mostrarModalCliente.value = false
            clienteEnEdicion.value = null
            mensajeSnackbar.value = if (id == 0L) "Cliente guardado con éxito" else "Cliente actualizado"
        }
    }

    fun seleccionarDiaCalendario(dia: DiaSemana) {
        diaSeleccionadoCalendario.value = dia
    }

    fun registrarVisitaFallida(cliente: ClienteEntity, fotoPath: String?, vehiculo: String = "CAMION") {
        viewModelScope.launch {
            val horaActual = SimpleDateFormat("HH:mm", Locale.getDefault()).format(Date())
            val visita = VisitaFallidaEntity(
                clienteId = cliente.id,
                nombreCliente = cliente.nombre,
                telefonoCliente = cliente.telefono,
                fechaIso = fechaHoyIso,
                horaDisplay = horaActual,
                timestamp = System.currentTimeMillis(),
                vehiculo = vehiculo,
                fotoEvidenciaPath = fotoPath,
                sincronizado = false
            )
            repository.registrarVisitaFallida(visita)
            mensajeSnackbar.value = "Visita registrada: ${cliente.nombre} no estaba en casa"
        }
    }

    fun actualizarUbicacionCliente(cliente: ClienteEntity, latitud: Double?, longitud: Double?, enlaceMapa: String) {
        viewModelScope.launch {
            val actualizado = cliente.copy(
                latitud = latitud,
                longitud = longitud,
                enlaceMapa = enlaceMapa
            )
            repository.guardarCliente(actualizado)
            mensajeSnackbar.value = "Ubicación de ${cliente.nombre} actualizada"
        }
    }

    fun limpiarMensajeSnackbar() {
        mensajeSnackbar.value = null
    }

    companion object {
        fun obtenerDiaSemanaActual(): DiaSemana {
            val cal = Calendar.getInstance()
            return when (cal.get(Calendar.DAY_OF_WEEK)) {
                Calendar.MONDAY -> DiaSemana.LUNES
                Calendar.TUESDAY -> DiaSemana.MARTES
                Calendar.WEDNESDAY -> DiaSemana.MIERCOLES
                Calendar.THURSDAY -> DiaSemana.JUEVES
                Calendar.FRIDAY -> DiaSemana.VIERNES
                Calendar.SATURDAY -> DiaSemana.SABADO
                else -> DiaSemana.LUNES
            }
        }

        /**
         * Calcula la fecha exacta (YYYY-MM-DD) del día de la semana actual.
         */
        fun obtenerFechaParaDiaSemana(dia: DiaSemana): String {
            val cal = Calendar.getInstance()
            val dayOfWeekTarget = when (dia) {
                DiaSemana.LUNES -> Calendar.MONDAY
                DiaSemana.MARTES -> Calendar.TUESDAY
                DiaSemana.MIERCOLES -> Calendar.WEDNESDAY
                DiaSemana.JUEVES -> Calendar.THURSDAY
                DiaSemana.VIERNES -> Calendar.FRIDAY
                DiaSemana.SABADO -> Calendar.SATURDAY
            }
            cal.set(Calendar.DAY_OF_WEEK, dayOfWeekTarget)
            val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
            return sdf.format(cal.time)
        }
    }
}

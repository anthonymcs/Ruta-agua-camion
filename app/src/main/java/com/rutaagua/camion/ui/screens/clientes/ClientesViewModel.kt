package com.rutaagua.camion.ui.screens.clientes

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.rutaagua.camion.data.local.entity.ClienteEntity
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

data class ClienteRutaItem(
    val cliente: ClienteEntity,
    val entregadoHoy: Boolean,
    val bidonesEntregadosHoy: Int = 0
)

@OptIn(ExperimentalCoroutinesApi::class)
class ClientesViewModel(
    private val repository: RutaAguaRepository
) : ViewModel() {

    private val formatoFecha = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
    val fechaHoyTexto: String = formatoFecha.format(Date())

    // -------------------------------------------------------------
    // DIRECTORIO DE CLIENTES
    // -------------------------------------------------------------
    val busquedaQuery = MutableStateFlow("")
    val filtroSeleccionado = MutableStateFlow(FiltroCliente.TODOS)

    val clientesFiltradosState: StateFlow<List<ClienteEntity>> = combine(
        repository.obtenerTodosLosClientes(),
        busquedaQuery,
        filtroSeleccionado
    ) { lista, query, filtro ->
        lista.filter { cliente ->
            // Filtro por búsqueda (nombre o ubicación)
            val coincideTexto = query.isBlank() ||
                    cliente.nombre.contains(query, ignoreCase = true) ||
                    cliente.ubicacion.contains(query, ignoreCase = true) ||
                    cliente.telefono.contains(query, ignoreCase = true)

            // Filtro por categoría o saldo
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

    // Estados para BottomSheet de Crear/Editar
    val clienteEnEdicion = MutableStateFlow<ClienteEntity?>(null)
    val mostrarModalCliente = MutableStateFlow(false)

    // Estado para Diálogo de Confirmación de Eliminación
    val clienteAEliminar = MutableStateFlow<ClienteEntity?>(null)

    val mensajeSnackbar = MutableStateFlow<String?>(null)

    // -------------------------------------------------------------
    // CALENDARIO DE RUTA SEMANAL (LUNES A SÁBADO)
    // -------------------------------------------------------------
    val diaSeleccionadoCalendario = MutableStateFlow(obtenerDiaSemanaActual())

    // Clientes asignados al día seleccionado cruzados con las ventas de hoy
    val clientesRutaDiaState: StateFlow<List<ClienteRutaItem>> = diaSeleccionadoCalendario
        .flatMapLatest { dia ->
            combine(
                repository.obtenerClientesPorDiaRuta(dia.name),
                repository.obtenerTransaccionesPorFecha(fechaHoyTexto)
            ) { clientesRuta, transaccionesHoy ->
                clientesRuta.map { cliente ->
                    val transaccionesCliente = transaccionesHoy.filter { it.clienteId == cliente.id }
                    val entregado = transaccionesCliente.isNotEmpty()
                    val bidonesHoy = transaccionesCliente.sumOf {
                        it.cantidadBidonesRecarga + it.cantidadBidonesPrestados + it.cantidadBidonesVendidosEnvase
                    }
                    ClienteRutaItem(
                        cliente = cliente,
                        entregadoHoy = entregado,
                        bidonesEntregadosHoy = bidonesHoy
                    )
                }
            }
        }.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    // Conteo de clientes asignados a cada día de la semana (para badges en el encabezado)
    val conteoPorDiaSemanaState: StateFlow<Map<DiaSemana, Int>> = repository
        .obtenerTodosLosClientes()
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
        bidonesPrestadosInicial: Int = 0
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
                activo = true
            )

            repository.guardarCliente(cliente)
            mostrarModalCliente.value = false
            clienteEnEdicion.value = null
            mensajeSnackbar.value = if (id == 0L) "Cliente guardado con éxito" else "Cliente actualizado"
        }
    }

    // -------------------------------------------------------------
    // MÉTODOS DE CALENDARIO
    // -------------------------------------------------------------
    fun seleccionarDiaCalendario(dia: DiaSemana) {
        diaSeleccionadoCalendario.value = dia
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
                else -> DiaSemana.LUNES // Domingo apunta a inicio de semana
            }
        }
    }
}

package com.rutaagua.camion.data.remote

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.util.Base64
import android.util.Log
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import com.google.firebase.firestore.SetOptions
import com.google.firebase.firestore.firestoreSettings
import com.google.firebase.firestore.persistentCacheSettings
import com.rutaagua.camion.data.local.RutaAguaDatabase
import com.rutaagua.camion.data.local.entity.CierreCajaEntity
import com.rutaagua.camion.data.local.entity.ClienteEntity
import com.rutaagua.camion.data.local.entity.GastoExtraEntity
import com.rutaagua.camion.data.local.entity.InventarioCamionEntity
import com.rutaagua.camion.data.local.entity.RecargaPlantaEntity
import com.rutaagua.camion.data.local.entity.TransaccionVentaEntity
import com.rutaagua.camion.data.local.entity.VisitaFallidaEntity
import com.rutaagua.camion.data.local.model.FormaPago
import com.rutaagua.camion.data.local.model.TipoBidon
import com.rutaagua.camion.data.local.model.TipoCliente
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.FileOutputStream

/**
 * Gestor de sincronización en tiempo real con Cloud Firestore (Offline-First).
 * Mantiene la persistencia 100% offline y sincroniza datos de manera bidireccional
 * entre todos los vehículos (Camión y Camioneta) a costo $0.00.
 */
class FirestoreSyncManager(
    private val context: Context,
    private val database: RutaAguaDatabase,
    private val scope: CoroutineScope = CoroutineScope(Dispatchers.IO)
) {

    private val auth: FirebaseAuth = FirebaseAuth.getInstance()
    private val firestore: FirebaseFirestore = FirebaseFirestore.getInstance()

    private var listenerClientes: ListenerRegistration? = null
    private var listenerTransacciones: ListenerRegistration? = null
    private var listenerInventarios: ListenerRegistration? = null
    private var listenerRecargas: ListenerRegistration? = null
    private var listenerGastos: ListenerRegistration? = null
    private var listenerCierres: ListenerRegistration? = null
    private var listenerVisitasFallidas: ListenerRegistration? = null

    // Timestamp de inicio para filtrar transacciones nuevas y lanzar notificaciones
    private val appStartTimestamp = System.currentTimeMillis()

    // Flags para evitar bucles de eco local -> remoto -> local
    private var sincronizandoDesdeRemoto = false

    fun iniciar() {
        configurarPersistenciaYAutenticacion()
        iniciarObservadoresRemotos()
    }

    private fun configurarPersistenciaYAutenticacion() {
        try {
            // 1. Activar caché offline persistente de Firestore
            val settings = firestoreSettings {
                setLocalCacheSettings(persistentCacheSettings { })
            }
            firestore.firestoreSettings = settings

            // 2. Autenticación Anónima transparente en segundo plano
            if (auth.currentUser == null) {
                auth.signInAnonymously()
                    .addOnSuccessListener {
                        Log.d(TAG, "Autenticación anónima exitosa: ${it.user?.uid}")
                    }
                    .addOnFailureListener { e ->
                        Log.w(TAG, "Fallo auth anónima (modo offline continúa): ${e.message}")
                    }
            }
        } catch (e: Exception) {
            Log.w(TAG, "Error configurando Firestore: ${e.message}")
        }
    }

    private fun iniciarObservadoresRemotos() {
        // --- 1. COLECCIÓN CLIENTES (Compartida 100% entre Camión y Camioneta) ---
        listenerClientes = firestore.collection("clientes")
            .addSnapshotListener { snapshot, error ->
                if (error != null || snapshot == null) return@addSnapshotListener
                scope.launch {
                    sincronizandoDesdeRemoto = true
                    try {
                        for (doc in snapshot.documents) {
                            val id = doc.getLong("id") ?: doc.id.toLongOrNull() ?: continue
                            val nombre = doc.getString("nombre") ?: continue
                            val telefono = doc.getString("telefono") ?: ""
                            val ubicacion = doc.getString("ubicacion") ?: ""
                            val tipoBidon = runCatching { TipoBidon.valueOf(doc.getString("tipoBidonPreferido") ?: "AZUL") }.getOrDefault(TipoBidon.AZUL)
                            val precioHabitual = doc.getDouble("precioHabitual") ?: 1.0
                            val tipoCliente = runCatching { TipoCliente.valueOf(doc.getString("tipoCliente") ?: "RUTA") }.getOrDefault(TipoCliente.RUTA)
                            val diasVisita = doc.getString("diasVisita") ?: ""
                            val deudaMonetaria = doc.getDouble("deudaMonetaria") ?: 0.0
                            val bidonesPrestados = doc.getLong("bidonesPrestados")?.toInt() ?: 0
                            val latitud = doc.getDouble("latitud")
                            val longitud = doc.getDouble("longitud")
                            val enlaceMapa = doc.getString("enlaceMapa") ?: ""
                            val activo = doc.getBoolean("activo") ?: true
                            val fechaCreacion = doc.getLong("fechaCreacion") ?: System.currentTimeMillis()

                            // Si viene foto en miniatura Base64, restaurarla en almacenamiento local
                            val fotoBase64 = doc.getString("fotoBase64")
                            val localPath = if (!fotoBase64.isNullOrBlank()) {
                                guardarBase64AArchivoLocal(id, fotoBase64)
                            } else null

                            val clienteActual = database.clienteDao().obtenerPorIdDirecto(id)
                            val clienteEntity = ClienteEntity(
                                id = id,
                                nombre = nombre,
                                telefono = telefono,
                                ubicacion = ubicacion,
                                tipoBidonPreferido = tipoBidon,
                                precioHabitual = precioHabitual,
                                tipoCliente = tipoCliente,
                                diasVisita = diasVisita,
                                deudaMonetaria = deudaMonetaria,
                                bidonesPrestados = bidonesPrestados,
                                fotoReferenciaPath = localPath ?: clienteActual?.fotoReferenciaPath,
                                latitud = latitud,
                                longitud = longitud,
                                enlaceMapa = enlaceMapa,
                                activo = activo,
                                fechaCreacion = fechaCreacion
                            )
                            database.clienteDao().insertar(clienteEntity)
                        }
                    } finally {
                        sincronizandoDesdeRemoto = false
                    }
                }
            }

        // --- 2. COLECCIÓN TRANSACCIONES (Con visibilidad cruzada de qué vehículo entregó) ---
        listenerTransacciones = firestore.collection("transacciones")
            .addSnapshotListener { snapshot, error ->
                if (error != null || snapshot == null) return@addSnapshotListener
                scope.launch {
                    sincronizandoDesdeRemoto = true
                    try {
                        // Detectar notificaciones en tiempo real cuando el otro vehículo realiza una venta
                        for (change in snapshot.documentChanges) {
                            if (change.type == com.google.firebase.firestore.DocumentChange.Type.ADDED) {
                                val doc = change.document
                                val fecha = doc.getLong("fecha") ?: 0L
                                val vehiculoDoc = doc.getString("vehiculo") ?: "CAMION"
                                val miVehiculo = com.rutaagua.camion.data.local.prefs.AppPreferences.getInstance(context).obtenerVehiculoActivo().name
                                if (fecha >= appStartTimestamp && vehiculoDoc != miVehiculo) {
                                    val cId = doc.getLong("clienteId")
                                    val nombrePaso = doc.getString("nombreClientePaso")
                                    val nombreCliente = if (!nombrePaso.isNullOrBlank()) {
                                        nombrePaso
                                    } else if (cId != null) {
                                        database.clienteDao().obtenerPorIdDirecto(cId)?.nombre ?: "Cliente de Ruta"
                                    } else {
                                        "Cliente de Ruta"
                                    }
                                    val formatoHora = java.text.SimpleDateFormat("HH:mm", java.util.Locale.getDefault())
                                    val horaStr = formatoHora.format(java.util.Date(fecha))
                                    val nombreVehiculo = if (vehiculoDoc == "CAMIONETA") "La camioneta" else "El camión"
                                    val emojiVehiculo = if (vehiculoDoc == "CAMIONETA") "🛻" else "🚚"
                                    com.rutaagua.camion.ui.utils.RutaNotificationHelper.mostrarNotificacionRuta(
                                        context = context,
                                        titulo = "$emojiVehiculo Despacho de Cliente en Ruta",
                                        mensaje = "$nombreVehiculo realizó el despacho de cliente: $nombreCliente ($horaStr)."
                                    )
                                }
                            }
                        }

                        for (doc in snapshot.documents) {
                            val id = doc.getLong("id") ?: doc.id.toLongOrNull() ?: continue
                            val vehiculo = doc.getString("vehiculo") ?: "CAMION"
                            val clienteId = doc.getLong("clienteId")
                            val nombrePaso = doc.getString("nombreClientePaso")
                            val fecha = doc.getLong("fecha") ?: System.currentTimeMillis()
                            val fechaTexto = doc.getString("fechaTexto") ?: ""
                            val tipoBidon = runCatching { TipoBidon.valueOf(doc.getString("tipoBidon") ?: "AZUL") }.getOrDefault(TipoBidon.AZUL)
                            val cantRec = doc.getLong("cantidadBidonesRecarga")?.toInt() ?: 0
                            val pBidon = doc.getDouble("precioUnitarioBidon") ?: 1.0
                            val cantPrest = doc.getLong("cantidadBidonesPrestados")?.toInt() ?: 0
                            val cantDev = doc.getLong("cantidadBidonesDevueltos")?.toInt() ?: 0
                            val cantVendEnv = doc.getLong("cantidadBidonesVendidosEnvase")?.toInt() ?: 0
                            val pEnv = doc.getDouble("precioUnitarioBidonVendido") ?: 0.0
                            val recTrans = doc.getLong("recargaTransparentes")?.toInt() ?: 0
                            val recAzul = doc.getLong("recargaAzules")?.toInt() ?: 0
                            val recMarc = doc.getLong("recargaMarcados")?.toInt() ?: 0
                            val prestTrans = doc.getLong("prestadosTransparentes")?.toInt() ?: 0
                            val prestAzul = doc.getLong("prestadosAzules")?.toInt() ?: 0
                            val prestMarc = doc.getLong("prestadosMarcados")?.toInt() ?: 0
                            val cantGratis = doc.getLong("cantidadBidonesGratis")?.toInt() ?: 0
                            val envVendTrans = doc.getLong("envasesVendidosTransparentes")?.toInt() ?: 0
                            val envVendAzul = doc.getLong("envasesVendidosAzules")?.toInt() ?: 0
                            val envVendMarc = doc.getLong("envasesVendidosMarcados")?.toInt() ?: 0
                            val cantGal = doc.getLong("cantidadGalones")?.toInt() ?: 0
                            val pGal = doc.getDouble("precioUnitarioGalon") ?: 0.75
                            val cantPac = doc.getLong("cantidadPacas")?.toInt() ?: 0
                            val pPac = doc.getDouble("precioUnitarioPaca") ?: 3.75
                            val cantMin = doc.getLong("cantidadMinis")?.toInt() ?: 0
                            val pMin = doc.getDouble("precioUnitarioMini") ?: 1.75
                            val totalImp = doc.getDouble("totalImporte") ?: 0.0
                            val formaPago = runCatching { FormaPago.valueOf(doc.getString("formaPagoPrincipal") ?: "EFECTIVO") }.getOrDefault(FormaPago.EFECTIVO)
                            val mEf = doc.getDouble("montoEfectivo") ?: 0.0
                            val mTr = doc.getDouble("montoTransferencia") ?: 0.0
                            val mCr = doc.getDouble("montoCredito") ?: 0.0
                            val mAb = doc.getDouble("montoAbono") ?: 0.0
                            val mCanc = doc.getDouble("montoCanceladoDeuda") ?: 0.0
                            val nota = doc.getString("nota") ?: ""

                            val transaccion = TransaccionVentaEntity(
                                id = id,
                                vehiculo = vehiculo,
                                clienteId = clienteId,
                                nombreClientePaso = nombrePaso,
                                fecha = fecha,
                                fechaTexto = fechaTexto,
                                tipoBidon = tipoBidon,
                                cantidadBidonesRecarga = cantRec,
                                precioUnitarioBidon = pBidon,
                                cantidadBidonesPrestados = cantPrest,
                                cantidadBidonesDevueltos = cantDev,
                                cantidadBidonesVendidosEnvase = cantVendEnv,
                                precioUnitarioBidonVendido = pEnv,
                                recargaTransparentes = recTrans,
                                recargaAzules = recAzul,
                                recargaMarcados = recMarc,
                                prestadosTransparentes = prestTrans,
                                prestadosAzules = prestAzul,
                                prestadosMarcados = prestMarc,
                                envasesVendidosTransparentes = envVendTrans,
                                envasesVendidosAzules = envVendAzul,
                                envasesVendidosMarcados = envVendMarc,
                                cantidadBidonesGratis = cantGratis,
                                cantidadGalones = cantGal,
                                precioUnitarioGalon = pGal,
                                cantidadPacas = cantPac,
                                precioUnitarioPaca = pPac,
                                cantidadMinis = cantMin,
                                precioUnitarioMini = pMin,
                                totalImporte = totalImp,
                                formaPagoPrincipal = formaPago,
                                montoEfectivo = mEf,
                                montoTransferencia = mTr,
                                montoCredito = mCr,
                                montoAbono = mAb,
                                montoCanceladoDeuda = mCanc,
                                nota = nota
                            )
                            database.transaccionDao().insertar(transaccion)
                        }
                    } finally {
                        sincronizandoDesdeRemoto = false
                    }
                }
            }

        // --- 3. COLECCIÓN INVENTARIOS (Uno por vehículo: CAMION y CAMIONETA) ---
        listenerInventarios = firestore.collection("inventarios")
            .addSnapshotListener { snapshot, error ->
                if (error != null || snapshot == null) return@addSnapshotListener
                scope.launch {
                    sincronizandoDesdeRemoto = true
                    try {
                        for (doc in snapshot.documents) {
                            val vehiculo = doc.getString("vehiculo") ?: doc.id
                            val id = if (vehiculo == "CAMIONETA") 2 else 1
                            val inv = InventarioCamionEntity(
                                id = id,
                                vehiculo = vehiculo,
                                capacidadTopeBidones = doc.getLong("capacidadTopeBidones")?.toInt() ?: if (id == 2) 80 else 218,
                                capacidadTopeGalones = doc.getLong("capacidadTopeGalones")?.toInt() ?: if (id == 2) 30 else 90,
                                capacidadTopePacas = doc.getLong("capacidadTopePacas")?.toInt() ?: if (id == 2) 2 else 6,
                                capacidadTopeMinis = doc.getLong("capacidadTopeMinis")?.toInt() ?: if (id == 2) 1 else 2,
                                bidonesLlenosTransparentes = doc.getLong("bidonesLlenosTransparentes")?.toInt() ?: 0,
                                bidonesLlenosAzules = doc.getLong("bidonesLlenosAzules")?.toInt() ?: 0,
                                bidonesLlenosMarcados = doc.getLong("bidonesLlenosMarcados")?.toInt() ?: 0,
                                bidonesVacios = doc.getLong("bidonesVacios")?.toInt() ?: 0,
                                galonesLlenos = doc.getLong("galonesLlenos")?.toInt() ?: 0,
                                pacasLlenas = doc.getLong("pacasLlenas")?.toInt() ?: 0,
                                minisLlenas = doc.getLong("minisLlenas")?.toInt() ?: 0,
                                fechaUltimaActualizacion = doc.getLong("fechaUltimaActualizacion") ?: System.currentTimeMillis()
                            )
                            database.inventarioDao().insertarOActualizar(inv)
                        }
                    } finally {
                        sincronizandoDesdeRemoto = false
                    }
                }
            }

        // --- 4. COLECCIÓN RECARGAS PLANTA ---
        listenerRecargas = firestore.collection("recargas_planta")
            .addSnapshotListener { snapshot, error ->
                if (error != null || snapshot == null) return@addSnapshotListener
                scope.launch {
                    sincronizandoDesdeRemoto = true
                    try {
                        for (doc in snapshot.documents) {
                            val id = doc.getLong("id") ?: doc.id.toLongOrNull() ?: continue
                            val vehiculo = doc.getString("vehiculo") ?: "CAMION"
                            val fecha = doc.getLong("fecha") ?: System.currentTimeMillis()
                            val fechaTexto = doc.getString("fechaTexto") ?: ""
                            val vuelta = doc.getLong("numeroVuelta")?.toInt() ?: 1
                            val recarga = RecargaPlantaEntity(
                                id = id,
                                vehiculo = vehiculo,
                                fecha = fecha,
                                fechaTexto = fechaTexto,
                                numeroVuelta = vuelta,
                                bidonesCargadosTransparentes = doc.getLong("bidonesCargadosTransparentes")?.toInt() ?: 0,
                                bidonesCargadosAzules = doc.getLong("bidonesCargadosAzules")?.toInt() ?: 0,
                                bidonesCargadosMarcados = doc.getLong("bidonesCargadosMarcados")?.toInt() ?: 0,
                                bidonesVaciosDescargados = doc.getLong("bidonesVaciosDescargados")?.toInt() ?: 0,
                                galonesCargados = doc.getLong("galonesCargados")?.toInt() ?: 0,
                                pacasCargadas = doc.getLong("pacasCargadas")?.toInt() ?: 0,
                                minisCargadas = doc.getLong("minisCargadas")?.toInt() ?: 0,
                                observaciones = doc.getString("observaciones") ?: ""
                            )
                            database.recargaPlantaDao().insertar(recarga)
                        }
                    } finally {
                        sincronizandoDesdeRemoto = false
                    }
                }
            }

        // --- 5. COLECCIÓN GASTOS EXTRAS ---
        listenerGastos = firestore.collection("gastos")
            .addSnapshotListener { snapshot, error ->
                if (error != null || snapshot == null) return@addSnapshotListener
                scope.launch {
                    sincronizandoDesdeRemoto = true
                    try {
                        for (doc in snapshot.documents) {
                            val id = doc.getLong("id") ?: doc.id.toLongOrNull() ?: continue
                            val vehiculo = doc.getString("vehiculo") ?: "CAMION"
                            val fecha = doc.getLong("fecha") ?: System.currentTimeMillis()
                            val fechaTexto = doc.getString("fechaTexto") ?: ""
                            val concepto = doc.getString("concepto") ?: ""
                            val monto = doc.getDouble("monto") ?: 0.0
                            val categoria = doc.getString("categoria") ?: "RUTA"

                            val gasto = GastoExtraEntity(
                                id = id,
                                vehiculo = vehiculo,
                                fecha = fecha,
                                fechaTexto = fechaTexto,
                                concepto = concepto,
                                monto = monto,
                                categoria = categoria
                            )
                            database.gastoDao().insertar(gasto)
                        }
                    } finally {
                        sincronizandoDesdeRemoto = false
                    }
                }
            }

        // --- 6. COLECCIÓN CIERRES DE CAJA ---
        listenerCierres = firestore.collection("cierres_caja")
            .addSnapshotListener { snapshot, error ->
                if (error != null || snapshot == null) return@addSnapshotListener
                scope.launch {
                    sincronizandoDesdeRemoto = true
                    try {
                        for (doc in snapshot.documents) {
                            val id = doc.getLong("id") ?: doc.id.toLongOrNull() ?: continue
                            val fechaIso = doc.getString("fechaIso") ?: ""
                            val vehiculo = doc.getString("vehiculo") ?: "CAMION"
                            val cierre = CierreCajaEntity(
                                id = id,
                                fechaIso = fechaIso,
                                timestamp = doc.getLong("timestamp") ?: System.currentTimeMillis(),
                                vehiculo = vehiculo,
                                efectivoDeclarado = doc.getDouble("efectivoDeclarado") ?: 0.0,
                                transferenciasDeclaradas = doc.getDouble("transferenciasDeclaradas") ?: 0.0,
                                totalDeclarado = doc.getDouble("totalDeclarado") ?: 0.0,
                                observaciones = doc.getString("observaciones") ?: "",
                                efectivoSistema = doc.getDouble("efectivoSistema") ?: 0.0,
                                transferenciasSistema = doc.getDouble("transferenciasSistema") ?: 0.0,
                                totalSistema = doc.getDouble("totalSistema") ?: 0.0,
                                diferenciaEfectivo = doc.getDouble("diferenciaEfectivo") ?: 0.0,
                                diferenciaTransferencia = doc.getDouble("diferenciaTransferencia") ?: 0.0,
                                diferenciaTotal = doc.getDouble("diferenciaTotal") ?: 0.0,
                                bidonesVendidos = doc.getLong("bidonesVendidos")?.toInt() ?: 0,
                                galonesVendidos = doc.getLong("galonesVendidos")?.toInt() ?: 0,
                                pacasVendidas = doc.getLong("pacasVendidas")?.toInt() ?: 0,
                                minisVendidas = doc.getLong("minisVendidas")?.toInt() ?: 0,
                                gastosRuta = doc.getDouble("gastosRuta") ?: 0.0,
                                costoTotalPlanta = doc.getDouble("costoTotalPlanta") ?: 0.0,
                                utilidadBruta = doc.getDouble("utilidadBruta") ?: 0.0,
                                utilidadNeta = doc.getDouble("utilidadNeta") ?: 0.0,
                                sincronizado = true
                            )
                            database.cierreCajaDao().insertar(cierre)
                        }
                    } finally {
                        sincronizandoDesdeRemoto = false
                    }
                }
            }

        // --- 7. COLECCIÓN VISITAS FALLIDAS (NO ESTABA EN CASA) ---
        listenerVisitasFallidas = firestore.collection("visitas_fallidas")
            .addSnapshotListener { snapshot, error ->
                if (error != null || snapshot == null) return@addSnapshotListener
                scope.launch {
                    sincronizandoDesdeRemoto = true
                    try {
                        for (doc in snapshot.documents) {
                            val id = doc.getLong("id") ?: doc.id.toLongOrNull() ?: continue
                            val clienteId = doc.getLong("clienteId") ?: 0L
                            val nombreCliente = doc.getString("nombreCliente") ?: ""
                            val telefonoCliente = doc.getString("telefonoCliente") ?: ""
                            val fechaIso = doc.getString("fechaIso") ?: ""
                            val horaDisplay = doc.getString("horaDisplay") ?: ""
                            val vehiculo = doc.getString("vehiculo") ?: "CAMION"

                            val visita = VisitaFallidaEntity(
                                id = id,
                                clienteId = clienteId,
                                nombreCliente = nombreCliente,
                                telefonoCliente = telefonoCliente,
                                fechaIso = fechaIso,
                                horaDisplay = horaDisplay,
                                timestamp = doc.getLong("timestamp") ?: System.currentTimeMillis(),
                                vehiculo = vehiculo,
                                fotoEvidenciaPath = null,
                                fotoBase64Thumbnail = doc.getString("fotoBase64Thumbnail"),
                                sincronizado = true
                            )
                            database.visitaFallidaDao().insertar(visita)
                        }
                    } finally {
                        sincronizandoDesdeRemoto = false
                    }
                }
            }
    }

    // =========================================================================
    // MÉTODOS DE SUBIDA (LOCAL -> FIRESTORE)
    // =========================================================================

    fun subirCliente(cliente: ClienteEntity) {
        if (sincronizandoDesdeRemoto) return
        scope.launch {
            val fotoMiniaturaBase64 = cliente.fotoReferenciaPath?.let { comprimirFotoABase64(it) }

            val data = hashMapOf(
                "id" to cliente.id,
                "nombre" to cliente.nombre,
                "telefono" to cliente.telefono,
                "ubicacion" to cliente.ubicacion,
                "tipoBidonPreferido" to cliente.tipoBidonPreferido.name,
                "precioHabitual" to cliente.precioHabitual,
                "tipoCliente" to cliente.tipoCliente.name,
                "diasVisita" to cliente.diasVisita,
                "deudaMonetaria" to cliente.deudaMonetaria,
                "bidonesPrestados" to cliente.bidonesPrestados,
                "latitud" to cliente.latitud,
                "longitud" to cliente.longitud,
                "enlaceMapa" to cliente.enlaceMapa,
                "activo" to cliente.activo,
                "fechaCreacion" to cliente.fechaCreacion,
                "fotoBase64" to (fotoMiniaturaBase64 ?: "")
            )

            firestore.collection("clientes")
                .document(cliente.id.toString())
                .set(data, SetOptions.merge())
        }
    }

    fun eliminarCliente(clienteId: Long) {
        if (sincronizandoDesdeRemoto) return
        firestore.collection("clientes").document(clienteId.toString()).delete()
    }

    fun subirTransaccion(transaccion: TransaccionVentaEntity) {
        if (sincronizandoDesdeRemoto) return
        val data = hashMapOf(
            "id" to transaccion.id,
            "vehiculo" to transaccion.vehiculo,
            "clienteId" to transaccion.clienteId,
            "nombreClientePaso" to transaccion.nombreClientePaso,
            "fecha" to transaccion.fecha,
            "fechaTexto" to transaccion.fechaTexto,
            "tipoBidon" to transaccion.tipoBidon.name,
            "cantidadBidonesRecarga" to transaccion.cantidadBidonesRecarga,
            "precioUnitarioBidon" to transaccion.precioUnitarioBidon,
            "cantidadBidonesPrestados" to transaccion.cantidadBidonesPrestados,
            "cantidadBidonesDevueltos" to transaccion.cantidadBidonesDevueltos,
            "cantidadBidonesVendidosEnvase" to transaccion.cantidadBidonesVendidosEnvase,
            "precioUnitarioBidonVendido" to transaccion.precioUnitarioBidonVendido,
            "recargaTransparentes" to transaccion.recargaTransparentes,
            "recargaAzules" to transaccion.recargaAzules,
            "recargaMarcados" to transaccion.recargaMarcados,
            "prestadosTransparentes" to transaccion.prestadosTransparentes,
            "prestadosAzules" to transaccion.prestadosAzules,
            "prestadosMarcados" to transaccion.prestadosMarcados,
            "cantidadBidonesGratis" to transaccion.cantidadBidonesGratis,
            "envasesVendidosTransparentes" to transaccion.envasesVendidosTransparentes,
            "envasesVendidosAzules" to transaccion.envasesVendidosAzules,
            "envasesVendidosMarcados" to transaccion.envasesVendidosMarcados,
            "cantidadGalones" to transaccion.cantidadGalones,
            "precioUnitarioGalon" to transaccion.precioUnitarioGalon,
            "cantidadPacas" to transaccion.cantidadPacas,
            "precioUnitarioPaca" to transaccion.precioUnitarioPaca,
            "cantidadMinis" to transaccion.cantidadMinis,
            "precioUnitarioMini" to transaccion.precioUnitarioMini,
            "totalImporte" to transaccion.totalImporte,
            "formaPagoPrincipal" to transaccion.formaPagoPrincipal.name,
            "montoEfectivo" to transaccion.montoEfectivo,
            "montoTransferencia" to transaccion.montoTransferencia,
            "montoCredito" to transaccion.montoCredito,
            "montoAbono" to transaccion.montoAbono,
            "montoCanceladoDeuda" to transaccion.montoCanceladoDeuda,
            "nota" to transaccion.nota
        )
        firestore.collection("transacciones")
            .document(transaccion.id.toString())
            .set(data, SetOptions.merge())
    }

    fun eliminarTransaccion(transaccionId: Long) {
        if (sincronizandoDesdeRemoto) return
        firestore.collection("transacciones").document(transaccionId.toString()).delete()
    }

    fun subirInventario(inventario: InventarioCamionEntity) {
        if (sincronizandoDesdeRemoto) return
        val data = hashMapOf(
            "id" to inventario.id,
            "vehiculo" to inventario.vehiculo,
            "capacidadTopeBidones" to inventario.capacidadTopeBidones,
            "capacidadTopeGalones" to inventario.capacidadTopeGalones,
            "capacidadTopePacas" to inventario.capacidadTopePacas,
            "capacidadTopeMinis" to inventario.capacidadTopeMinis,
            "bidonesLlenosTransparentes" to inventario.bidonesLlenosTransparentes,
            "bidonesLlenosAzules" to inventario.bidonesLlenosAzules,
            "bidonesLlenosMarcados" to inventario.bidonesLlenosMarcados,
            "bidonesVacios" to inventario.bidonesVacios,
            "galonesLlenos" to inventario.galonesLlenos,
            "pacasLlenas" to inventario.pacasLlenas,
            "minisLlenas" to inventario.minisLlenas,
            "fechaUltimaActualizacion" to inventario.fechaUltimaActualizacion
        )
        firestore.collection("inventarios")
            .document(inventario.vehiculo)
            .set(data, SetOptions.merge())
    }

    fun subirRecarga(recarga: RecargaPlantaEntity) {
        if (sincronizandoDesdeRemoto) return
        val data = hashMapOf(
            "id" to recarga.id,
            "vehiculo" to recarga.vehiculo,
            "fecha" to recarga.fecha,
            "fechaTexto" to recarga.fechaTexto,
            "numeroVuelta" to recarga.numeroVuelta,
            "bidonesCargadosTransparentes" to recarga.bidonesCargadosTransparentes,
            "bidonesCargadosAzules" to recarga.bidonesCargadosAzules,
            "bidonesCargadosMarcados" to recarga.bidonesCargadosMarcados,
            "bidonesVaciosDescargados" to recarga.bidonesVaciosDescargados,
            "galonesCargados" to recarga.galonesCargados,
            "pacasCargadas" to recarga.pacasCargadas,
            "minisCargadas" to recarga.minisCargadas,
            "observaciones" to recarga.observaciones
        )
        firestore.collection("recargas_planta")
            .document(recarga.id.toString())
            .set(data, SetOptions.merge())
    }

    fun eliminarRecarga(recargaId: Long) {
        if (sincronizandoDesdeRemoto) return
        firestore.collection("recargas_planta").document(recargaId.toString()).delete()
    }

    fun subirGasto(gasto: GastoExtraEntity) {
        if (sincronizandoDesdeRemoto) return
        val data = hashMapOf(
            "id" to gasto.id,
            "vehiculo" to gasto.vehiculo,
            "fecha" to gasto.fecha,
            "fechaTexto" to gasto.fechaTexto,
            "concepto" to gasto.concepto,
            "monto" to gasto.monto,
            "categoria" to gasto.categoria
        )
        firestore.collection("gastos")
            .document(gasto.id.toString())
            .set(data, SetOptions.merge())
    }

    fun eliminarGasto(gastoId: Long) {
        if (sincronizandoDesdeRemoto) return
        firestore.collection("gastos").document(gastoId.toString()).delete()
    }

    fun subirCierreCaja(cierre: CierreCajaEntity) {
        if (sincronizandoDesdeRemoto) return
        val data = hashMapOf(
            "id" to cierre.id,
            "fechaIso" to cierre.fechaIso,
            "timestamp" to cierre.timestamp,
            "vehiculo" to cierre.vehiculo,
            "efectivoDeclarado" to cierre.efectivoDeclarado,
            "transferenciasDeclaradas" to cierre.transferenciasDeclaradas,
            "totalDeclarado" to cierre.totalDeclarado,
            "observaciones" to cierre.observaciones,
            "efectivoSistema" to cierre.efectivoSistema,
            "transferenciasSistema" to cierre.transferenciasSistema,
            "totalSistema" to cierre.totalSistema,
            "diferenciaEfectivo" to cierre.diferenciaEfectivo,
            "diferenciaTransferencia" to cierre.diferenciaTransferencia,
            "diferenciaTotal" to cierre.diferenciaTotal,
            "bidonesVendidos" to cierre.bidonesVendidos,
            "galonesVendidos" to cierre.galonesVendidos,
            "pacasVendidas" to cierre.pacasVendidas,
            "minisVendidas" to cierre.minisVendidas,
            "gastosRuta" to cierre.gastosRuta,
            "costoTotalPlanta" to cierre.costoTotalPlanta,
            "utilidadBruta" to cierre.utilidadBruta,
            "utilidadNeta" to cierre.utilidadNeta
        )
        firestore.collection("cierres_caja")
            .document(cierre.id.toString())
            .set(data, SetOptions.merge())
    }

    fun subirVisitaFallida(visita: VisitaFallidaEntity) {
        if (sincronizandoDesdeRemoto) return
        val miniaturaBase64 = visita.fotoEvidenciaPath?.let { comprimirFotoABase64(it) }
        val data = hashMapOf(
            "id" to visita.id,
            "clienteId" to visita.clienteId,
            "nombreCliente" to visita.nombreCliente,
            "telefonoCliente" to visita.telefonoCliente,
            "fechaIso" to visita.fechaIso,
            "horaDisplay" to visita.horaDisplay,
            "timestamp" to visita.timestamp,
            "vehiculo" to visita.vehiculo,
            "fotoBase64Thumbnail" to (miniaturaBase64 ?: "")
        )
        firestore.collection("visitas_fallidas")
            .document(visita.id.toString())
            .set(data, SetOptions.merge())
    }

    fun detener() {
        listenerClientes?.remove()
        listenerTransacciones?.remove()
        listenerInventarios?.remove()
        listenerRecargas?.remove()
        listenerGastos?.remove()
        listenerCierres?.remove()
        listenerVisitasFallidas?.remove()
    }

    // =========================================================================
    // UTILIDADES DE FOTO MINIATURA EN BASE64 (<20KB)
    // =========================================================================

    private fun comprimirFotoABase64(path: String): String? {
        val file = File(path)
        if (!file.exists()) return null
        return try {
            val original = BitmapFactory.decodeFile(file.absolutePath) ?: return null
            val maxDim = maxOf(original.width, original.height)
            val escala = if (maxDim > 160) 160f / maxDim else 1f
            val ancho = (original.width * escala).toInt()
            val alto = (original.height * escala).toInt()
            val resized = Bitmap.createScaledBitmap(original, ancho, alto, true)

            val out = ByteArrayOutputStream()
            resized.compress(Bitmap.CompressFormat.JPEG, 65, out)
            val bytes = out.toByteArray()
            Base64.encodeToString(bytes, Base64.NO_WRAP)
        } catch (_: Exception) {
            null
        }
    }

    private fun guardarBase64AArchivoLocal(clienteId: Long, base64: String): String? {
        return try {
            val bytes = Base64.decode(base64, Base64.NO_WRAP)
            val dir = File(context.filesDir, "fotos_clientes").apply { if (!exists()) mkdirs() }
            val destino = File(dir, "cliente_${clienteId}_sync.jpg")
            FileOutputStream(destino).use { it.write(bytes) }
            destino.absolutePath
        } catch (_: Exception) {
            null
        }
    }

    companion object {
        private const val TAG = "FirestoreSyncManager"
    }
}

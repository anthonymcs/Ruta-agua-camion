package com.rutaagua.camion.data.backup

import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.core.content.FileProvider
import androidx.room.withTransaction
import com.google.firebase.FirebaseApp
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
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
import com.rutaagua.camion.data.local.prefs.AppPreferences
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.withContext
import org.json.JSONArray
import org.json.JSONObject
import java.io.ByteArrayOutputStream
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.zip.GZIPOutputStream
import android.util.Base64

data class ResultadoRespaldoAuto(
    val exito: Boolean,
    val mesIso: String,
    val totalRegistros: Int,
    val tamanoKb: Double,
    val resumenVerificacion: String,
    val archivoLocal: File? = null
)

/**
 * Gestor de copias de seguridad (Exportación, Restauración y Respaldo Mensual Automático con Autoprueba)
 * en formato JSON + Sincronización de Snapshot Mensual en Firebase Firestore (Plan Gratis Spark $0.00).
 */
object BackupManager {

    private const val CARPETA_RESPALDOS_MENSUALES = "respaldos_mensuales"
    private const val COLLECTION_RESPALDOS_MENSUALES = "respaldos_mensuales"

    /**
     * Se ejecuta automáticamente al iniciar la app (y bajo demanda desde Ajustes).
     * Si cambió el mes o si la cantidad de registros creció desde el último respaldo del mes,
     * genera automáticamente el respaldo mensual, ejecuta una autoprueba de lectura/integridad
     * y sube el snapshot mensual a Firebase Firestore.
     */
    suspend fun verificarYEjecutarRespaldoMensualAutomatico(
        context: Context,
        database: RutaAguaDatabase,
        forzarAhora: Boolean = false
    ): ResultadoRespaldoAuto = withContext(Dispatchers.IO) {
        try {
            val prefs = AppPreferences.getInstance(context)
            val mesActualIso = SimpleDateFormat("yyyy-MM", Locale.getDefault()).format(Date())

            val clientes = database.clienteDao().obtenerTodosFlow().first()
            val inventarios = database.inventarioDao().obtenerTodosLosInventariosFlow().first()
            val transacciones = database.transaccionDao().obtenerTodasFlow().first()
            val recargas = database.recargaPlantaDao().obtenerTodasFlow().first()
            val gastos = database.gastoDao().obtenerTodosFlow().first()
            val cierres = database.cierreCajaDao().obtenerTodosFlow().first()
            val visitas = database.visitaFallidaDao().obtenerTodasVisitasFlow().first()

            val conteoTotalActual = clientes.size + inventarios.size + transacciones.size +
                    recargas.size + gastos.size + cierres.size + visitas.size

            val ultimoMesGuardado = prefs.obtenerMesUltimoRespaldoAuto()
            val ultimoConteoGuardado = prefs.obtenerConteoUltimoRespaldoAuto()
            val dirMensual = File(context.filesDir, CARPETA_RESPALDOS_MENSUALES).apply { mkdirs() }
            val archivoMes = File(dirMensual, "respaldo_auto_${mesActualIso}.json")

            val necesitaActualizar = forzarAhora ||
                    ultimoMesGuardado != mesActualIso ||
                    !archivoMes.exists() ||
                    conteoTotalActual != ultimoConteoGuardado

            if (!necesitaActualizar && archivoMes.exists()) {
                val kb = archivoMes.length() / 1024.0
                return@withContext ResultadoRespaldoAuto(
                    exito = true,
                    mesIso = mesActualIso,
                    totalRegistros = conteoTotalActual,
                    tamanoKb = kb,
                    resumenVerificacion = prefs.obtenerDetalleUltimoRespaldoAuto(),
                    archivoLocal = archivoMes
                )
            }

            // 1. Construir JSON completo de la base de datos
            val root = construirJsonRespaldoCompleto(
                mesIso = mesActualIso,
                clientes = clientes,
                inventarios = inventarios,
                transacciones = transacciones,
                recargas = recargas,
                gastos = gastos,
                cierres = cierres,
                visitas = visitas
            )

            val jsonString = root.toString()
            archivoMes.writeText(jsonString, Charsets.UTF_8)

            // Conservar máximo 24 meses locales para no ocupar almacenamiento innecesario
            limpiarRespaldosAntiguos(dirMensual, maxArchivos = 24)

            // 2. AUTOPRUEBA DE INTEGRIDAD: Leer el archivo recién escrito y verificar que todos los datos sean legibles
            val pruebaIntegridad = probarIntegridadArchivoJson(
                archivo = archivoMes,
                clientesEsperados = clientes.size,
                transaccionesEsperadas = transacciones.size,
                inventariosEsperados = inventarios.size
            )

            val tamanoKb = archivoMes.length() / 1024.0
            val ahora = System.currentTimeMillis()
            val resumenTexto = if (pruebaIntegridad) {
                "Verificado OK ($mesActualIso) • ${clientes.size} clientes, ${transacciones.size} entregas (${String.format(Locale.US, "%.1f", tamanoKb)} KB)"
            } else {
                "Advertencia en verificación de respaldo ($mesActualIso)"
            }

            prefs.guardarResultadoRespaldoAuto(
                mesIso = mesActualIso,
                timestamp = ahora,
                detalle = resumenTexto,
                conteoRegistros = conteoTotalActual
            )

            // 3. Subir Snapshot Mensual a Firebase Firestore (Plan Gratis Spark)
            subirSnapshotMensualFirestore(
                context = context,
                mesIso = mesActualIso,
                jsonRaw = jsonString,
                clientesCount = clientes.size,
                transaccionesCount = transacciones.size,
                ventasMesCount = transacciones.count { it.fechaTexto.startsWith(mesActualIso) },
                totalVendidoMes = transacciones.filter { it.fechaTexto.startsWith(mesActualIso) }.sumOf { it.totalImporte }
            )

            ResultadoRespaldoAuto(
                exito = pruebaIntegridad,
                mesIso = mesActualIso,
                totalRegistros = conteoTotalActual,
                tamanoKb = tamanoKb,
                resumenVerificacion = resumenTexto,
                archivoLocal = archivoMes
            )
        } catch (e: Exception) {
            e.printStackTrace()
            ResultadoRespaldoAuto(
                exito = false,
                mesIso = SimpleDateFormat("yyyy-MM", Locale.getDefault()).format(Date()),
                totalRegistros = 0,
                tamanoKb = 0.0,
                resumenVerificacion = "Error al autogenerar: ${e.localizedMessage ?: "Desconocido"}"
            )
        }
    }

    /**
     * Prueba automáticamente que el archivo JSON generado se pueda leer y parsear sin errores
     * y que coincidan los conteos con la base de datos activa.
     */
    fun probarIntegridadArchivoJson(
        archivo: File,
        clientesEsperados: Int,
        transaccionesEsperadas: Int,
        inventariosEsperados: Int
    ): Boolean {
        return try {
            if (!archivo.exists() || archivo.length() == 0L) return false
            val contenido = archivo.readText(Charsets.UTF_8)
            val json = JSONObject(contenido)

            val arrClientes = json.getJSONArray("clientes")
            val arrTransacciones = json.getJSONArray("transacciones")
            val arrInventarios = json.getJSONArray("inventarios")

            // Validar que cada registro tenga las llaves esenciales
            if (arrClientes.length() != clientesEsperados) return false
            if (arrTransacciones.length() != transaccionesEsperadas) return false
            if (arrInventarios.length() != inventariosEsperados) return false

            for (i in 0 until arrClientes.length()) {
                val c = arrClientes.getJSONObject(i)
                if (!c.has("id") || !c.has("nombre")) return false
            }
            for (i in 0 until arrTransacciones.length()) {
                val t = arrTransacciones.getJSONObject(i)
                if (!t.has("id") || !t.has("fechaTexto") || !t.has("totalImporte")) return false
            }
            true
        } catch (e: Exception) {
            false
        }
    }

    /**
     * Guarda una copia mensual comprimida (GZIP + Base64) en la colección `respaldos_mensuales/{YYYY-MM}`
     * de Firebase Firestore para tener respaldo en la nube por mes sin gastar espacio.
     */
    private fun subirSnapshotMensualFirestore(
        context: Context,
        mesIso: String,
        jsonRaw: String,
        clientesCount: Int,
        transaccionesCount: Int,
        ventasMesCount: Int,
        totalVendidoMes: Double
    ) {
        try {
            val apps = FirebaseApp.getApps(context)
            if (apps.isEmpty()) return
            val firestore = FirebaseFirestore.getInstance()

            val gzipBytes = comprimirGzip(jsonRaw)
            val base64Gzip = Base64.encodeToString(gzipBytes, Base64.NO_WRAP)

            val data = hashMapOf<String, Any?>(
                "mesIso" to mesIso,
                "fechaActualizacion" to System.currentTimeMillis(),
                "totalClientes" to clientesCount,
                "totalTransaccionesHistoricas" to transaccionesCount,
                "entregasDelMes" to ventasMesCount,
                "ventaTotalDelMes" to totalVendidoMes,
                "tamanoComprimidoBytes" to gzipBytes.size,
                "verificadoAutomaticamente" to true
            )

            // Firestore permite hasta 1 MB por documento; un año entero comprimido en GZIP pesa ~40-90 KB
            if (base64Gzip.length < 750_000) {
                data["respaldoGzipBase64"] = base64Gzip
            }

            firestore.collection(COLLECTION_RESPALDOS_MENSUALES)
                .document(mesIso)
                .set(data, SetOptions.merge())
        } catch (_: Exception) {
            // Silencioso en modo offline
        }
    }

    private fun comprimirGzip(texto: String): ByteArray {
        val bos = ByteArrayOutputStream()
        GZIPOutputStream(bos).bufferedWriter(Charsets.UTF_8).use { writer ->
            writer.write(texto)
        }
        return bos.toByteArray()
    }

    private fun limpiarRespaldosAntiguos(directorio: File, maxArchivos: Int) {
        val archivos = directorio.listFiles { f -> f.name.endsWith(".json") }
            ?.sortedByDescending { it.name }
            ?: return
        if (archivos.size > maxArchivos) {
            archivos.drop(maxArchivos).forEach { it.delete() }
        }
    }

    fun listarRespaldosMensualesLocales(context: Context): List<File> {
        val dir = File(context.filesDir, CARPETA_RESPALDOS_MENSUALES)
        if (!dir.exists()) return emptyList()
        return dir.listFiles { f -> f.name.endsWith(".json") }
            ?.sortedByDescending { it.name }
            ?: emptyList()
    }

    suspend fun exportarRespaldoJson(
        context: Context,
        database: RutaAguaDatabase
    ): File = withContext(Dispatchers.IO) {
        val resultadoAuto = verificarYEjecutarRespaldoMensualAutomatico(
            context = context,
            database = database,
            forzarAhora = true
        )

        val clientes = database.clienteDao().obtenerTodosFlow().first()
        val inventarios = database.inventarioDao().obtenerTodosLosInventariosFlow().first()
        val transacciones = database.transaccionDao().obtenerTodasFlow().first()
        val recargas = database.recargaPlantaDao().obtenerTodasFlow().first()
        val gastos = database.gastoDao().obtenerTodosFlow().first()
        val cierres = database.cierreCajaDao().obtenerTodosFlow().first()
        val visitas = database.visitaFallidaDao().obtenerTodasVisitasFlow().first()

        val root = construirJsonRespaldoCompleto(
            mesIso = resultadoAuto.mesIso,
            clientes = clientes,
            inventarios = inventarios,
            transacciones = transacciones,
            recargas = recargas,
            gastos = gastos,
            cierres = cierres,
            visitas = visitas
        )

        val backupDir = File(context.cacheDir, "backups").apply { mkdirs() }
        val fechaArchivo = SimpleDateFormat("yyyyMMdd_HHmm", Locale.getDefault()).format(Date())
        val backupFile = File(backupDir, "respaldo_ruta_agua_$fechaArchivo.json")
        backupFile.writeText(root.toString(2), Charsets.UTF_8)
        backupFile
    }

    private fun construirJsonRespaldoCompleto(
        mesIso: String,
        clientes: List<ClienteEntity>,
        inventarios: List<InventarioCamionEntity>,
        transacciones: List<TransaccionVentaEntity>,
        recargas: List<RecargaPlantaEntity>,
        gastos: List<GastoExtraEntity>,
        cierres: List<CierreCajaEntity>,
        visitas: List<VisitaFallidaEntity>
    ): JSONObject {
        val root = JSONObject()
        root.put("versionRespaldo", 4)
        root.put("mesRespaldo", mesIso)
        root.put("fechaExportacion", System.currentTimeMillis())
        root.put("fechaLegible", SimpleDateFormat("dd/MM/yyyy HH:mm:ss", Locale.getDefault()).format(Date()))

        val clientesArr = JSONArray()
        clientes.forEach { c ->
            clientesArr.put(JSONObject().apply {
                put("id", c.id)
                put("nombre", c.nombre)
                put("alias", c.alias)
                put("telefono", c.telefono)
                put("ubicacion", c.ubicacion)
                put("latitud", c.latitud ?: JSONObject.NULL)
                put("longitud", c.longitud ?: JSONObject.NULL)
                put("enlaceMapa", c.enlaceMapa)
                put("fotoReferenciaPath", c.fotoReferenciaPath ?: JSONObject.NULL)
                put("tipoCliente", c.tipoCliente.name)
                put("diasVisita", c.diasVisita)
                put("tipoBidonPreferido", c.tipoBidonPreferido.name)
                put("precioHabitual", c.precioHabitual)
                put("deudaMonetaria", c.deudaMonetaria)
                put("bidonesPrestados", c.bidonesPrestados)
                put("activo", c.activo)
            })
        }
        root.put("clientes", clientesArr)

        val invArr = JSONArray()
        inventarios.forEach { inv ->
            invArr.put(JSONObject().apply {
                put("id", inv.id)
                put("vehiculo", inv.vehiculo)
                put("capacidadTopeBidones", inv.capacidadTopeBidones)
                put("bidonesLlenosTransparentes", inv.bidonesLlenosTransparentes)
                put("bidonesLlenosAzules", inv.bidonesLlenosAzules)
                put("bidonesLlenosMarcados", inv.bidonesLlenosMarcados)
                put("bidonesVacios", inv.bidonesVacios)
                put("galonesLlenos", inv.galonesLlenos)
                put("capacidadTopeGalones", inv.capacidadTopeGalones)
                put("pacasLlenas", inv.pacasLlenas)
                put("capacidadTopePacas", inv.capacidadTopePacas)
                put("minisLlenas", inv.minisLlenas)
                put("capacidadTopeMinis", inv.capacidadTopeMinis)
                put("fechaUltimaActualizacion", inv.fechaUltimaActualizacion)
            })
        }
        root.put("inventarios", invArr)

        val transArr = JSONArray()
        transacciones.forEach { t ->
            transArr.put(JSONObject().apply {
                put("id", t.id)
                put("vehiculo", t.vehiculo)
                put("clienteId", t.clienteId ?: JSONObject.NULL)
                put("nombreClientePaso", t.nombreClientePaso ?: JSONObject.NULL)
                put("fecha", t.fecha)
                put("fechaTexto", t.fechaTexto)
                put("tipoBidon", t.tipoBidon.name)
                put("cantidadBidonesRecarga", t.cantidadBidonesRecarga)
                put("precioUnitarioBidon", t.precioUnitarioBidon)
                put("cantidadBidonesPrestados", t.cantidadBidonesPrestados)
                put("cantidadBidonesDevueltos", t.cantidadBidonesDevueltos)
                put("cantidadBidonesVendidosEnvase", t.cantidadBidonesVendidosEnvase)
                put("precioUnitarioBidonVendido", t.precioUnitarioBidonVendido)
                put("recargaTransparentes", t.recargaTransparentes)
                put("recargaAzules", t.recargaAzules)
                put("recargaMarcados", t.recargaMarcados)
                put("prestadosTransparentes", t.prestadosTransparentes)
                put("prestadosAzules", t.prestadosAzules)
                put("prestadosMarcados", t.prestadosMarcados)
                put("cantidadBidonesGratis", t.cantidadBidonesGratis)
                put("envasesVendidosTransparentes", t.envasesVendidosTransparentes)
                put("envasesVendidosAzules", t.envasesVendidosAzules)
                put("envasesVendidosMarcados", t.envasesVendidosMarcados)
                put("cantidadGalones", t.cantidadGalones)
                put("precioUnitarioGalon", t.precioUnitarioGalon)
                put("cantidadPacas", t.cantidadPacas)
                put("precioUnitarioPaca", t.precioUnitarioPaca)
                put("cantidadMinis", t.cantidadMinis)
                put("precioUnitarioMini", t.precioUnitarioMini)
                put("totalImporte", t.totalImporte)
                put("formaPagoPrincipal", t.formaPagoPrincipal.name)
                put("montoEfectivo", t.montoEfectivo)
                put("montoTransferencia", t.montoTransferencia)
                put("montoCredito", t.montoCredito)
                put("montoAbono", t.montoAbono)
                put("montoCanceladoDeuda", t.montoCanceladoDeuda)
                put("nota", t.nota)
            })
        }
        root.put("transacciones", transArr)

        val recArr = JSONArray()
        recargas.forEach { r ->
            recArr.put(JSONObject().apply {
                put("id", r.id)
                put("vehiculo", r.vehiculo)
                put("fecha", r.fecha)
                put("fechaTexto", r.fechaTexto)
                put("bidonesCargadosTransparentes", r.bidonesCargadosTransparentes)
                put("bidonesCargadosAzules", r.bidonesCargadosAzules)
                put("bidonesCargadosMarcados", r.bidonesCargadosMarcados)
                put("bidonesVaciosDescargados", r.bidonesVaciosDescargados)
                put("galonesCargados", r.galonesCargados)
                put("pacasCargadas", r.pacasCargadas)
                put("minisCargadas", r.minisCargadas)
                put("observaciones", r.observaciones)
            })
        }
        root.put("recargasPlanta", recArr)

        val gasArr = JSONArray()
        gastos.forEach { g ->
            gasArr.put(JSONObject().apply {
                put("id", g.id)
                put("vehiculo", g.vehiculo)
                put("fecha", g.fecha)
                put("fechaTexto", g.fechaTexto)
                put("concepto", g.concepto)
                put("monto", g.monto)
                put("categoria", g.categoria)
            })
        }
        root.put("gastos", gasArr)

        val cierresArr = JSONArray()
        cierres.forEach { ci ->
            cierresArr.put(JSONObject().apply {
                put("id", ci.id)
                put("fechaIso", ci.fechaIso)
                put("timestamp", ci.timestamp)
                put("vehiculo", ci.vehiculo)
                put("efectivoDeclarado", ci.efectivoDeclarado)
                put("transferenciasDeclaradas", ci.transferenciasDeclaradas)
                put("totalDeclarado", ci.totalDeclarado)
                put("observaciones", ci.observaciones)
                put("efectivoSistema", ci.efectivoSistema)
                put("transferenciasSistema", ci.transferenciasSistema)
                put("totalSistema", ci.totalSistema)
                put("diferenciaEfectivo", ci.diferenciaEfectivo)
                put("diferenciaTransferencia", ci.diferenciaTransferencia)
                put("diferenciaTotal", ci.diferenciaTotal)
                put("bidonesVendidos", ci.bidonesVendidos)
                put("galonesVendidos", ci.galonesVendidos)
                put("pacasVendidas", ci.pacasVendidas)
                put("minisVendidas", ci.minisVendidas)
                put("gastosRuta", ci.gastosRuta)
                put("costoTotalPlanta", ci.costoTotalPlanta)
                put("utilidadBruta", ci.utilidadBruta)
                put("utilidadNeta", ci.utilidadNeta)
            })
        }
        root.put("cierresCaja", cierresArr)

        val visitasArr = JSONArray()
        visitas.forEach { v ->
            visitasArr.put(JSONObject().apply {
                put("id", v.id)
                put("clienteId", v.clienteId)
                put("nombreCliente", v.nombreCliente)
                put("telefonoCliente", v.telefonoCliente)
                put("fechaIso", v.fechaIso)
                put("horaDisplay", v.horaDisplay)
                put("timestamp", v.timestamp)
                put("vehiculo", v.vehiculo)
                put("fotoEvidenciaPath", v.fotoEvidenciaPath ?: JSONObject.NULL)
            })
        }
        root.put("visitasFallidas", visitasArr)

        return root
    }

    fun compartirArchivoRespaldo(context: Context, archivo: File) {
        val uri = FileProvider.getUriForFile(
            context,
            "${context.packageName}.fileprovider",
            archivo
        )
        val sendIntent = Intent(Intent.ACTION_SEND).apply {
            type = "application/json"
            putExtra(Intent.EXTRA_STREAM, uri)
            putExtra(Intent.EXTRA_SUBJECT, "Respaldo Ruta Agua (${archivo.name})")
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }
        val chooser = Intent.createChooser(sendIntent, "Guardar o enviar respaldo")
        context.startActivity(chooser)
    }

    suspend fun importarRespaldoDesdeUri(
        context: Context,
        uri: Uri,
        database: RutaAguaDatabase
    ): Result<String> = withContext(Dispatchers.IO) {
        try {
            val content = context.contentResolver.openInputStream(uri)?.bufferedReader()?.use { it.readText() }
                ?: return@withContext Result.failure(Exception("No se pudo leer el archivo seleccionado"))
            restaurarDesdeTextoJson(content, database)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun importarRespaldoDesdeArchivoLocal(
        archivo: File,
        database: RutaAguaDatabase
    ): Result<String> = withContext(Dispatchers.IO) {
        try {
            if (!archivo.exists()) return@withContext Result.failure(Exception("El archivo no existe"))
            val content = archivo.readText(Charsets.UTF_8)
            restaurarDesdeTextoJson(content, database)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    private suspend fun restaurarDesdeTextoJson(
        content: String,
        database: RutaAguaDatabase
    ): Result<String> {
        return try {
            val root = JSONObject(content)
            var clientesRestaurados = 0
            var ventasRestauradas = 0

            database.withTransaction {
                root.optJSONArray("clientes")?.let { arr ->
                    for (i in 0 until arr.length()) {
                        val obj = arr.getJSONObject(i)
                        val cliente = ClienteEntity(
                            id = obj.getLong("id"),
                            nombre = obj.getString("nombre"),
                            alias = obj.optString("alias", ""),
                            telefono = obj.optString("telefono", ""),
                            ubicacion = obj.optString("ubicacion", ""),
                            latitud = if (obj.isNull("latitud")) null else obj.optDouble("latitud"),
                            longitud = if (obj.isNull("longitud")) null else obj.optDouble("longitud"),
                            enlaceMapa = obj.optString("enlaceMapa", ""),
                            fotoReferenciaPath = if (obj.isNull("fotoReferenciaPath")) null else obj.optString("fotoReferenciaPath"),
                            tipoCliente = runCatching { TipoCliente.valueOf(obj.getString("tipoCliente")) }.getOrDefault(TipoCliente.RUTA),
                            diasVisita = obj.optString("diasVisita", ""),
                            tipoBidonPreferido = runCatching { TipoBidon.valueOf(obj.getString("tipoBidonPreferido")) }.getOrDefault(TipoBidon.AZUL),
                            precioHabitual = obj.optDouble("precioHabitual", 1.50),
                            deudaMonetaria = obj.optDouble("deudaMonetaria", 0.0),
                            bidonesPrestados = obj.optInt("bidonesPrestados", 0),
                            activo = obj.optBoolean("activo", true)
                        )
                        database.clienteDao().insertar(cliente)
                        clientesRestaurados++
                    }
                }

                root.optJSONArray("inventarios")?.let { arr ->
                    for (i in 0 until arr.length()) {
                        val obj = arr.getJSONObject(i)
                        val inv = InventarioCamionEntity(
                            id = obj.getInt("id"),
                            vehiculo = obj.optString("vehiculo", "CAMION"),
                            capacidadTopeBidones = obj.optInt("capacidadTopeBidones", 120),
                            bidonesLlenosTransparentes = obj.optInt("bidonesLlenosTransparentes", 0),
                            bidonesLlenosAzules = obj.optInt("bidonesLlenosAzules", 0),
                            bidonesLlenosMarcados = obj.optInt("bidonesLlenosMarcados", 0),
                            bidonesVacios = obj.optInt("bidonesVacios", 0),
                            galonesLlenos = obj.optInt("galonesLlenos", 0),
                            capacidadTopeGalones = obj.optInt("capacidadTopeGalones", 20),
                            pacasLlenas = obj.optInt("pacasLlenas", 0),
                            capacidadTopePacas = obj.optInt("capacidadTopePacas", 15),
                            minisLlenas = obj.optInt("minisLlenas", 0),
                            capacidadTopeMinis = obj.optInt("capacidadTopeMinis", 10),
                            fechaUltimaActualizacion = obj.optLong("fechaUltimaActualizacion", System.currentTimeMillis())
                        )
                        database.inventarioDao().insertarOActualizar(inv)
                    }
                }

                root.optJSONArray("transacciones")?.let { arr ->
                    for (i in 0 until arr.length()) {
                        val obj = arr.getJSONObject(i)
                        val tx = TransaccionVentaEntity(
                            id = obj.getLong("id"),
                            vehiculo = obj.optString("vehiculo", "CAMION"),
                            clienteId = if (obj.isNull("clienteId")) null else obj.getLong("clienteId"),
                            nombreClientePaso = if (obj.isNull("nombreClientePaso")) null else obj.optString("nombreClientePaso"),
                            fecha = obj.getLong("fecha"),
                            fechaTexto = obj.getString("fechaTexto"),
                            tipoBidon = runCatching { TipoBidon.valueOf(obj.getString("tipoBidon")) }.getOrDefault(TipoBidon.AZUL),
                            cantidadBidonesRecarga = obj.optInt("cantidadBidonesRecarga", 0),
                            precioUnitarioBidon = obj.optDouble("precioUnitarioBidon", 1.50),
                            cantidadBidonesPrestados = obj.optInt("cantidadBidonesPrestados", 0),
                            cantidadBidonesDevueltos = obj.optInt("cantidadBidonesDevueltos", 0),
                            cantidadBidonesVendidosEnvase = obj.optInt("cantidadBidonesVendidosEnvase", 0),
                            precioUnitarioBidonVendido = obj.optDouble("precioUnitarioBidonVendido", 5.0),
                            recargaTransparentes = obj.optInt("recargaTransparentes", 0),
                            recargaAzules = obj.optInt("recargaAzules", 0),
                            recargaMarcados = obj.optInt("recargaMarcados", 0),
                            prestadosTransparentes = obj.optInt("prestadosTransparentes", 0),
                            prestadosAzules = obj.optInt("prestadosAzules", 0),
                            prestadosMarcados = obj.optInt("prestadosMarcados", 0),
                            cantidadBidonesGratis = obj.optInt("cantidadBidonesGratis", 0),
                            envasesVendidosTransparentes = obj.optInt("envasesVendidosTransparentes", 0),
                            envasesVendidosAzules = obj.optInt("envasesVendidosAzules", 0),
                            envasesVendidosMarcados = obj.optInt("envasesVendidosMarcados", 0),
                            cantidadGalones = obj.optInt("cantidadGalones", 0),
                            precioUnitarioGalon = obj.optDouble("precioUnitarioGalon", 0.75),
                            cantidadPacas = obj.optInt("cantidadPacas", 0),
                            precioUnitarioPaca = obj.optDouble("precioUnitarioPaca", 3.75),
                            cantidadMinis = obj.optInt("cantidadMinis", 0),
                            precioUnitarioMini = obj.optDouble("precioUnitarioMini", 1.75),
                            totalImporte = obj.optDouble("totalImporte", 0.0),
                            formaPagoPrincipal = runCatching { FormaPago.valueOf(obj.getString("formaPagoPrincipal")) }.getOrDefault(FormaPago.EFECTIVO),
                            montoEfectivo = obj.optDouble("montoEfectivo", 0.0),
                            montoTransferencia = obj.optDouble("montoTransferencia", 0.0),
                            montoCredito = obj.optDouble("montoCredito", 0.0),
                            montoAbono = obj.optDouble("montoAbono", 0.0),
                            montoCanceladoDeuda = obj.optDouble("montoCanceladoDeuda", 0.0),
                            nota = obj.optString("nota", "")
                        )
                        database.transaccionDao().insertar(tx)
                        ventasRestauradas++
                    }
                }

                root.optJSONArray("recargasPlanta")?.let { arr ->
                    for (i in 0 until arr.length()) {
                        val obj = arr.getJSONObject(i)
                        val rec = RecargaPlantaEntity(
                            id = obj.getLong("id"),
                            vehiculo = obj.optString("vehiculo", "CAMION"),
                            fecha = obj.getLong("fecha"),
                            fechaTexto = obj.getString("fechaTexto"),
                            bidonesCargadosTransparentes = obj.optInt("bidonesCargadosTransparentes", 0),
                            bidonesCargadosAzules = obj.optInt("bidonesCargadosAzules", 0),
                            bidonesCargadosMarcados = obj.optInt("bidonesCargadosMarcados", 0),
                            bidonesVaciosDescargados = obj.optInt("bidonesVaciosDescargados", 0),
                            galonesCargados = obj.optInt("galonesCargados", 0),
                            pacasCargadas = obj.optInt("pacasCargadas", 0),
                            minisCargadas = obj.optInt("minisCargadas", 0),
                            observaciones = obj.optString("observaciones", obj.optString("observacion", ""))
                        )
                        database.recargaPlantaDao().insertar(rec)
                    }
                }

                root.optJSONArray("gastos")?.let { arr ->
                    for (i in 0 until arr.length()) {
                        val obj = arr.getJSONObject(i)
                        val gasto = GastoExtraEntity(
                            id = obj.getLong("id"),
                            vehiculo = obj.optString("vehiculo", "CAMION"),
                            fecha = obj.getLong("fecha"),
                            fechaTexto = obj.getString("fechaTexto"),
                            concepto = obj.getString("concepto"),
                            monto = obj.getDouble("monto"),
                            categoria = obj.optString("categoria", "RUTA")
                        )
                        database.gastoDao().insertar(gasto)
                    }
                }

                root.optJSONArray("cierresCaja")?.let { arr ->
                    for (i in 0 until arr.length()) {
                        val obj = arr.getJSONObject(i)
                        val cierre = CierreCajaEntity(
                            id = obj.getLong("id"),
                            fechaIso = obj.getString("fechaIso"),
                            timestamp = obj.optLong("timestamp", System.currentTimeMillis()),
                            vehiculo = obj.optString("vehiculo", "CAMION"),
                            efectivoDeclarado = obj.optDouble("efectivoDeclarado", 0.0),
                            transferenciasDeclaradas = obj.optDouble("transferenciasDeclaradas", 0.0),
                            totalDeclarado = obj.optDouble("totalDeclarado", 0.0),
                            observaciones = obj.optString("observaciones", ""),
                            efectivoSistema = obj.optDouble("efectivoSistema", 0.0),
                            transferenciasSistema = obj.optDouble("transferenciasSistema", 0.0),
                            totalSistema = obj.optDouble("totalSistema", 0.0),
                            diferenciaEfectivo = obj.optDouble("diferenciaEfectivo", 0.0),
                            diferenciaTransferencia = obj.optDouble("diferenciaTransferencia", 0.0),
                            diferenciaTotal = obj.optDouble("diferenciaTotal", 0.0),
                            bidonesVendidos = obj.optInt("bidonesVendidos", 0),
                            galonesVendidos = obj.optInt("galonesVendidos", 0),
                            pacasVendidas = obj.optInt("pacasVendidas", 0),
                            minisVendidas = obj.optInt("minisVendidas", 0),
                            gastosRuta = obj.optDouble("gastosRuta", 0.0),
                            costoTotalPlanta = obj.optDouble("costoTotalPlanta", 0.0),
                            utilidadBruta = obj.optDouble("utilidadBruta", 0.0),
                            utilidadNeta = obj.optDouble("utilidadNeta", 0.0),
                            sincronizado = true
                        )
                        database.cierreCajaDao().insertar(cierre)
                    }
                }

                root.optJSONArray("visitasFallidas")?.let { arr ->
                    for (i in 0 until arr.length()) {
                        val obj = arr.getJSONObject(i)
                        val visita = VisitaFallidaEntity(
                            id = obj.getLong("id"),
                            clienteId = obj.getLong("clienteId"),
                            nombreCliente = obj.optString("nombreCliente", ""),
                            telefonoCliente = obj.optString("telefonoCliente", ""),
                            fechaIso = obj.getString("fechaIso"),
                            horaDisplay = obj.optString("horaDisplay", ""),
                            timestamp = obj.optLong("timestamp", System.currentTimeMillis()),
                            vehiculo = obj.optString("vehiculo", "CAMION"),
                            fotoEvidenciaPath = if (obj.isNull("fotoEvidenciaPath")) null else obj.optString("fotoEvidenciaPath"),
                            sincronizado = true
                        )
                        database.visitaFallidaDao().insertar(visita)
                    }
                }
            }

            Result.success("Restaurados $clientesRestaurados clientes y $ventasRestauradas entregas con éxito")
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}

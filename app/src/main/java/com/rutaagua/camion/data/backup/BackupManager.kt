package com.rutaagua.camion.data.backup

import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.core.content.FileProvider
import androidx.room.withTransaction
import com.rutaagua.camion.data.local.RutaAguaDatabase
import com.rutaagua.camion.data.local.entity.ClienteEntity
import com.rutaagua.camion.data.local.entity.GastoExtraEntity
import com.rutaagua.camion.data.local.entity.InventarioCamionEntity
import com.rutaagua.camion.data.local.entity.RecargaPlantaEntity
import com.rutaagua.camion.data.local.entity.TransaccionVentaEntity
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.withContext
import org.json.JSONArray
import org.json.JSONObject
import java.io.File
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * Gestor de exportación e importación de respaldos locales en formato JSON.
 */
object BackupManager {

    suspend fun exportarRespaldoJson(context: Context, database: RutaAguaDatabase): File = withContext(Dispatchers.IO) {
        val root = JSONObject()
        val formatoFecha = SimpleDateFormat("yyyy-MM-dd_HH-mm-ss", Locale.getDefault())
        val timestampTexto = formatoFecha.format(Date())

        root.put("versionApp", 1)
        root.put("fechaExportacion", timestampTexto)
        root.put("timestamp", System.currentTimeMillis())

        // 1. Clientes
        val clientes = database.clienteDao().obtenerTodosFlow().first()
        val jsonClientes = JSONArray()
        for (c in clientes) {
            val obj = JSONObject().apply {
                put("id", c.id)
                put("nombre", c.nombre)
                put("telefono", c.telefono)
                put("ubicacion", c.ubicacion)
                put("tipoBidonPreferido", c.tipoBidonPreferido.name)
                put("precioHabitual", c.precioHabitual)
                put("tipoCliente", c.tipoCliente.name)
                put("diasVisita", c.diasVisita)
                put("deudaMonetaria", c.deudaMonetaria)
                put("bidonesPrestados", c.bidonesPrestados)
                put("fotoReferenciaPath", c.fotoReferenciaPath ?: "")
                put("activo", c.activo)
                put("fechaCreacion", c.fechaCreacion)
            }
            jsonClientes.put(obj)
        }
        root.put("clientes", jsonClientes)

        // 2. Transacciones
        val transacciones = database.transaccionDao().obtenerTodasFlow().first()
        val jsonTrans = JSONArray()
        for (t in transacciones) {
            val obj = JSONObject().apply {
                put("id", t.id)
                put("vehiculo", t.vehiculo)
                put("clienteId", t.clienteId ?: JSONObject.NULL)
                put("nombreClientePaso", t.nombreClientePaso ?: "")
                put("fecha", t.fecha)
                put("fechaTexto", t.fechaTexto)
                put("tipoBidon", t.tipoBidon.name)
                put("cantidadBidonesRecarga", t.cantidadBidonesRecarga)
                put("precioUnitarioBidon", t.precioUnitarioBidon)
                put("cantidadBidonesPrestados", t.cantidadBidonesPrestados)
                put("cantidadBidonesDevueltos", t.cantidadBidonesDevueltos)
                put("cantidadBidonesVendidosEnvase", t.cantidadBidonesVendidosEnvase)
                put("precioUnitarioBidonVendido", t.precioUnitarioBidonVendido)
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
            }
            jsonTrans.put(obj)
        }
        root.put("transacciones", jsonTrans)

        // 3. Inventarios
        val inventarios = database.inventarioDao().obtenerTodosLosInventariosFlow().first()
        val jsonInv = JSONArray()
        for (i in inventarios) {
            val obj = JSONObject().apply {
                put("id", i.id)
                put("vehiculo", i.vehiculo)
                put("capacidadTopeBidones", i.capacidadTopeBidones)
                put("capacidadTopeGalones", i.capacidadTopeGalones)
                put("capacidadTopePacas", i.capacidadTopePacas)
                put("capacidadTopeMinis", i.capacidadTopeMinis)
                put("bidonesLlenosTransparentes", i.bidonesLlenosTransparentes)
                put("bidonesLlenosAzules", i.bidonesLlenosAzules)
                put("bidonesLlenosMarcados", i.bidonesLlenosMarcados)
                put("bidonesVacios", i.bidonesVacios)
                put("galonesLlenos", i.galonesLlenos)
                put("pacasLlenas", i.pacasLlenas)
                put("minisLlenas", i.minisLlenas)
                put("fechaUltimaActualizacion", i.fechaUltimaActualizacion)
            }
            jsonInv.put(obj)
        }
        root.put("inventarios", jsonInv)

        // 4. Gastos
        val gastos = database.gastoDao().obtenerTodosFlow().first()
        val jsonGastos = JSONArray()
        for (g in gastos) {
            val obj = JSONObject().apply {
                put("id", g.id)
                put("vehiculo", g.vehiculo)
                put("fecha", g.fecha)
                put("fechaTexto", g.fechaTexto)
                put("concepto", g.concepto)
                put("monto", g.monto)
                put("categoria", g.categoria)
            }
            jsonGastos.put(obj)
        }
        root.put("gastos", jsonGastos)

        // Guardar en archivo
        val dir = File(context.cacheDir, "respaldos").apply { if (!exists()) mkdirs() }
        val archivo = File(dir, "respaldo_ruta_agua_$timestampTexto.json")
        FileOutputStream(archivo).use { out ->
            out.write(root.toString(2).toByteArray(Charsets.UTF_8))
        }
        archivo
    }

    fun compartirRespaldo(context: Context, archivo: File) {
        val uri = FileProvider.getUriForFile(context, "${context.packageName}.fileprovider", archivo)
        val intent = Intent(Intent.ACTION_SEND).apply {
            type = "application/json"
            putExtra(Intent.EXTRA_STREAM, uri)
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }
        context.startActivity(Intent.createChooser(intent, "Compartir respaldo de Ruta Agua"))
    }

    suspend fun importarRespaldoJson(context: Context, database: RutaAguaDatabase, uri: Uri): Result<Int> = withContext(Dispatchers.IO) {
        try {
            val jsonString = context.contentResolver.openInputStream(uri)?.use { stream ->
                stream.bufferedReader(Charsets.UTF_8).readText()
            } ?: return@withContext Result.failure(Exception("No se pudo leer el archivo seleccionado"))

            val root = JSONObject(jsonString)
            var registrosImportados = 0

            database.withTransaction {
                // Clientes
                if (root.has("clientes")) {
                    val arr = root.getJSONArray("clientes")
                    for (idx in 0 until arr.length()) {
                        val obj = arr.getJSONObject(idx)
                        val cliente = ClienteEntity(
                            id = obj.optLong("id", 0L),
                            nombre = obj.getString("nombre"),
                            telefono = obj.optString("telefono", ""),
                            ubicacion = obj.optString("ubicacion", ""),
                            tipoBidonPreferido = runCatching { com.rutaagua.camion.data.local.model.TipoBidon.valueOf(obj.getString("tipoBidonPreferido")) }.getOrDefault(com.rutaagua.camion.data.local.model.TipoBidon.AZUL),
                            precioHabitual = obj.optDouble("precioHabitual", 1.0),
                            tipoCliente = runCatching { com.rutaagua.camion.data.local.model.TipoCliente.valueOf(obj.getString("tipoCliente")) }.getOrDefault(com.rutaagua.camion.data.local.model.TipoCliente.RUTA),
                            diasVisita = obj.optString("diasVisita", ""),
                            deudaMonetaria = obj.optDouble("deudaMonetaria", 0.0),
                            bidonesPrestados = obj.optInt("bidonesPrestados", 0),
                            fotoReferenciaPath = obj.optString("fotoReferenciaPath").takeIf { it.isNotBlank() },
                            activo = obj.optBoolean("activo", true),
                            fechaCreacion = obj.optLong("fechaCreacion", System.currentTimeMillis())
                        )
                        database.clienteDao().insertar(cliente)
                        registrosImportados++
                    }
                }

                // Transacciones
                if (root.has("transacciones")) {
                    val arr = root.getJSONArray("transacciones")
                    for (idx in 0 until arr.length()) {
                        val obj = arr.getJSONObject(idx)
                        val trans = TransaccionVentaEntity(
                            id = obj.optLong("id", 0L),
                            vehiculo = obj.optString("vehiculo", "CAMION"),
                            clienteId = if (obj.isNull("clienteId")) null else obj.getLong("clienteId"),
                            nombreClientePaso = obj.optString("nombreClientePaso").takeIf { it.isNotBlank() },
                            fecha = obj.optLong("fecha", System.currentTimeMillis()),
                            fechaTexto = obj.getString("fechaTexto"),
                            tipoBidon = runCatching { com.rutaagua.camion.data.local.model.TipoBidon.valueOf(obj.getString("tipoBidon")) }.getOrDefault(com.rutaagua.camion.data.local.model.TipoBidon.AZUL),
                            cantidadBidonesRecarga = obj.optInt("cantidadBidonesRecarga", 0),
                            precioUnitarioBidon = obj.optDouble("precioUnitarioBidon", 1.0),
                            cantidadBidonesPrestados = obj.optInt("cantidadBidonesPrestados", 0),
                            cantidadBidonesDevueltos = obj.optInt("cantidadBidonesDevueltos", 0),
                            cantidadBidonesVendidosEnvase = obj.optInt("cantidadBidonesVendidosEnvase", 0),
                            precioUnitarioBidonVendido = obj.optDouble("precioUnitarioBidonVendido", 0.0),
                            cantidadGalones = obj.optInt("cantidadGalones", 0),
                            precioUnitarioGalon = obj.optDouble("precioUnitarioGalon", 0.75),
                            cantidadPacas = obj.optInt("cantidadPacas", 0),
                            precioUnitarioPaca = obj.optDouble("precioUnitarioPaca", 3.75),
                            cantidadMinis = obj.optInt("cantidadMinis", 0),
                            precioUnitarioMini = obj.optDouble("precioUnitarioMini", 1.75),
                            totalImporte = obj.optDouble("totalImporte", 0.0),
                            formaPagoPrincipal = runCatching { com.rutaagua.camion.data.local.model.FormaPago.valueOf(obj.getString("formaPagoPrincipal")) }.getOrDefault(com.rutaagua.camion.data.local.model.FormaPago.EFECTIVO),
                            montoEfectivo = obj.optDouble("montoEfectivo", 0.0),
                            montoTransferencia = obj.optDouble("montoTransferencia", 0.0),
                            montoCredito = obj.optDouble("montoCredito", 0.0),
                            montoAbono = obj.optDouble("montoAbono", 0.0),
                            montoCanceladoDeuda = obj.optDouble("montoCanceladoDeuda", 0.0),
                            nota = obj.optString("nota", "")
                        )
                        database.transaccionDao().insertar(trans)
                        registrosImportados++
                    }
                }
            }
            Result.success(registrosImportados)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}

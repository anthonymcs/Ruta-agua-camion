package com.rutaagua.camion.data.remote

import android.app.DownloadManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.widget.Toast
import androidx.core.content.FileProvider
import com.rutaagua.camion.BuildConfig
import com.rutaagua.camion.data.local.prefs.AppPreferences
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.io.File
import java.net.HttpURLConnection
import java.net.URL

data class InfoActualizacionOta(
    val hayNuevaVersion: Boolean,
    val versionCodeRemoto: Long,
    val versionNameRemoto: String,
    val urlDescargaApk: String,
    val notasVersion: String
)

/**
 * Gestor de verificación y descarga de actualizaciones OTA (Over-The-Air) para Atenea / Ruta Agua.
 * Conectado directamente a la API de GitHub Releases (/releases/latest).
 */
object OTAUpdateChecker {

    /**
     * Compara semánticamente dos versiones (ej. "1.1.0" > "1.0").
     */
    fun esVersionSuperior(remota: String, actual: String): Boolean {
        val cleanRemota = remota.trim().removePrefix("v").removePrefix("V")
        val cleanActual = actual.trim().removePrefix("v").removePrefix("V")
        val partesR = cleanRemota.split(".").mapNotNull { it.toIntOrNull() }
        val partesA = cleanActual.split(".").mapNotNull { it.toIntOrNull() }
        val maxLen = maxOf(partesR.size, partesA.size)
        for (i in 0 until maxLen) {
            val r = partesR.getOrElse(i) { 0 }
            val a = partesA.getOrElse(i) { 0 }
            if (r > a) return true
            if (r < a) return false
        }
        return false
    }

    /**
     * Consulta el endpoint de GitHub Releases o URL configurada para evaluar nuevas versiones.
     */
    suspend fun verificarNuevaVersion(context: Context): Result<InfoActualizacionOta> = withContext(Dispatchers.IO) {
        try {
            val urlRemota = AppPreferences.getInstance(context).obtenerUrlOta()
            val url = URL(urlRemota)
            val conexion = (url.openConnection() as HttpURLConnection).apply {
                connectTimeout = 8000
                readTimeout = 8000
                requestMethod = "GET"
                setRequestProperty("Accept", "application/vnd.github+json, application/json")
                setRequestProperty("User-Agent", "AteneaApp-Android")
            }

            if (conexion.responseCode == 404) {
                // No hay releases publicadas todavía
                return@withContext Result.success(
                    InfoActualizacionOta(
                        hayNuevaVersion = false,
                        versionCodeRemoto = BuildConfig.VERSION_CODE.toLong(),
                        versionNameRemoto = BuildConfig.VERSION_NAME,
                        urlDescargaApk = "",
                        notasVersion = "No hay versiones remotas publicadas aún en GitHub Releases."
                    )
                )
            }

            if (conexion.responseCode !in 200..299) {
                return@withContext Result.failure(Exception("Servidor remoto respondió con código ${conexion.responseCode}"))
            }

            val contenido = conexion.inputStream.bufferedReader().readText()
            val json = JSONObject(contenido)

            val tagRemoto = json.optString("tag_name", "").trim()
            val nombreRelease = json.optString("name", "")
            val cuerpoRelease = json.optString("body", "").ifBlank {
                "Actualización con mejoras de rendimiento y estabilidad para la ruta."
            }

            // Buscar asset APK en caso de ser GitHub Releases
            var urlDescargaApk = ""
            val assetsArray = json.optJSONArray("assets")
            if (assetsArray != null && assetsArray.length() > 0) {
                for (i in 0 until assetsArray.length()) {
                    val asset = assetsArray.getJSONObject(i)
                    val nombreAsset = asset.optString("name", "")
                    val downloadUrl = asset.optString("browser_download_url", "")
                    if (nombreAsset.endsWith(".apk", ignoreCase = true)) {
                        urlDescargaApk = downloadUrl
                        break
                    }
                }
                // Si ningún asset termina en .apk explícitamente, tomar el primer asset disponible
                if (urlDescargaApk.isBlank() && assetsArray.length() > 0) {
                    urlDescargaApk = assetsArray.getJSONObject(0).optString("browser_download_url", "")
                }
            }

            // Si es un json directo (legacy/espejo)
            if (urlDescargaApk.isBlank()) {
                urlDescargaApk = json.optString("downloadUrl", "")
            }

            val versionNameRemoto = if (tagRemoto.isNotBlank()) {
                tagRemoto.removePrefix("v").removePrefix("V")
            } else {
                json.optString("versionName", "1.0")
            }

            // Intentar extraer versionCode de regex en el cuerpo o de atributo directo
            val versionCodeRegex = Regex("(?i)versionCode\\s*[:=]\\s*(\\d+)")
            val matchCode = versionCodeRegex.find(cuerpoRelease)
            val parsedCodeFromBody = matchCode?.groupValues?.getOrNull(1)?.toLongOrNull()

            val versionCodeRemoto = parsedCodeFromBody
                ?: json.optLong("versionCode", 0L).takeIf { it > 0L }
                ?: tagRemoto.removePrefix("v").removePrefix("V").toLongOrNull()
                ?: 0L

            val versionCodeActual = BuildConfig.VERSION_CODE.toLong()
            val versionNameActual = BuildConfig.VERSION_NAME

            val hayNueva = (versionCodeRemoto > versionCodeActual) || esVersionSuperior(versionNameRemoto, versionNameActual)

            val notasFinales = if (nombreRelease.isNotBlank() && nombreRelease != tagRemoto) {
                "$nombreRelease\n\n$cuerpoRelease"
            } else {
                cuerpoRelease
            }

            Result.success(
                InfoActualizacionOta(
                    hayNuevaVersion = hayNueva,
                    versionCodeRemoto = if (versionCodeRemoto > 0) versionCodeRemoto else versionCodeActual,
                    versionNameRemoto = versionNameRemoto,
                    urlDescargaApk = urlDescargaApk,
                    notasVersion = notasFinales
                )
            )
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Inicia la descarga del APK actualizado mediante DownloadManager y registra
     * la instalación automática con FileProvider una vez completada.
     */
    fun descargarEInstalarApk(context: Context, urlApk: String, versionName: String) {
        if (urlApk.isBlank()) {
            Toast.makeText(context, "No hay URL válida de descarga para esta versión.", Toast.LENGTH_LONG).show()
            return
        }

        try {
            val nombreArchivo = "Atenea_v$versionName.apk"
            val uri = Uri.parse(urlApk)
            val request = DownloadManager.Request(uri).apply {
                setTitle("Actualización Atenea v$versionName")
                setDescription("Descargando nueva versión del sistema...")
                setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED)
                setDestinationInExternalFilesDir(context, Environment.DIRECTORY_DOWNLOADS, nombreArchivo)
            }

            val manager = context.getSystemService(Context.DOWNLOAD_SERVICE) as DownloadManager
            val downloadId = manager.enqueue(request)

            Toast.makeText(context, "Descargando Atenea v$versionName. Se notificará al finalizar.", Toast.LENGTH_SHORT).show()

            // Registrar receptor para autoinstalar cuando termine la descarga
            val receiver = object : BroadcastReceiver() {
                override fun onReceive(c: Context?, intent: Intent?) {
                    val id = intent?.getLongExtra(DownloadManager.EXTRA_DOWNLOAD_ID, -1L) ?: -1L
                    if (id == downloadId) {
                        try {
                            c?.unregisterReceiver(this)
                        } catch (_: Exception) {}

                        val archivoDestino = File(context.getExternalFilesDir(Environment.DIRECTORY_DOWNLOADS), nombreArchivo)
                        if (archivoDestino.exists()) {
                            lanzarInstaladorApk(context, archivoDestino)
                        }
                    }
                }
            }

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                context.registerReceiver(
                    receiver,
                    IntentFilter(DownloadManager.ACTION_DOWNLOAD_COMPLETE),
                    Context.RECEIVER_EXPORTED
                )
            } else {
                context.registerReceiver(
                    receiver,
                    IntentFilter(DownloadManager.ACTION_DOWNLOAD_COMPLETE)
                )
            }

        } catch (e: Exception) {
            // Si DownloadManager falla por permisos o restricciones, abrir el enlace en el navegador
            try {
                val intent = Intent(Intent.ACTION_VIEW, Uri.parse(urlApk)).apply {
                    addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                }
                context.startActivity(intent)
            } catch (ex: Exception) {
                Toast.makeText(context, "Error al iniciar descarga: ${e.message}", Toast.LENGTH_LONG).show()
            }
        }
    }

    /**
     * Lanza el instalador del paquete APK de Android usando FileProvider.
     */
    fun lanzarInstaladorApk(context: Context, archivoApk: File) {
        try {
            val uri = FileProvider.getUriForFile(context, "${context.packageName}.fileprovider", archivoApk)
            val intent = Intent(Intent.ACTION_VIEW).apply {
                setDataAndType(uri, "application/vnd.android.package-archive")
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            context.startActivity(intent)
        } catch (e: Exception) {
            Toast.makeText(context, "Error al abrir instalador: ${e.message}", Toast.LENGTH_LONG).show()
        }
    }
}

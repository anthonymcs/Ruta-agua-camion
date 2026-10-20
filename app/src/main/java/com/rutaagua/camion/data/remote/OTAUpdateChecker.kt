package com.rutaagua.camion.data.remote

import android.app.DownloadManager
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Environment
import androidx.core.content.FileProvider
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
 * Gestor de verificación y descarga de actualizaciones OTA (Over-The-Air) para Ruta Agua.
 */
object OTAUpdateChecker {

    suspend fun verificarNuevaVersion(context: Context): Result<InfoActualizacionOta> = withContext(Dispatchers.IO) {
        try {
            val urlRemota = AppPreferences.getInstance(context).obtenerUrlOta()
            val url = URL(urlRemota)
            val conexion = (url.openConnection() as HttpURLConnection).apply {
                connectTimeout = 6000
                readTimeout = 6000
                requestMethod = "GET"
                setRequestProperty("Accept", "application/json")
            }

            if (conexion.responseCode !in 200..299) {
                return@withContext Result.failure(Exception("Servidor remoto respondió con código ${conexion.responseCode}"))
            }

            val contenido = conexion.inputStream.bufferedReader().readText()
            val json = JSONObject(contenido)

            val versionCodeRemoto = json.optLong("versionCode", 1L)
            val versionNameRemoto = json.optString("versionName", "1.0")
            val urlDescarga = json.optString("downloadUrl", "")
            val changelog = json.optString("changelog", "Mejoras de rendimiento y correcciones del camión.")

            val pInfo = context.packageManager.getPackageInfo(context.packageName, 0)
            val versionCodeActual = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                pInfo.longVersionCode
            } else {
                @Suppress("DEPRECATION")
                pInfo.versionCode.toLong()
            }

            val hayNueva = versionCodeRemoto > versionCodeActual

            Result.success(
                InfoActualizacionOta(
                    hayNuevaVersion = hayNueva,
                    versionCodeRemoto = versionCodeRemoto,
                    versionNameRemoto = versionNameRemoto,
                    urlDescargaApk = urlDescarga,
                    notasVersion = changelog
                )
            )
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Inicia la descarga del APK actualizado mediante DownloadManager.
     */
    fun descargarEInstalarApk(context: Context, urlApk: String, versionName: String) {
        try {
            val uri = Uri.parse(urlApk)
            val request = DownloadManager.Request(uri).apply {
                setTitle("Actualización Ruta Agua v$versionName")
                setDescription("Descargando nueva versión del sistema...")
                setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED)
                setDestinationInExternalFilesDir(context, Environment.DIRECTORY_DOWNLOADS, "RutaAgua_v$versionName.apk")
            }

            val manager = context.getSystemService(Context.DOWNLOAD_SERVICE) as DownloadManager
            manager.enqueue(request)
        } catch (e: Exception) {
            // Si DownloadManager falla, abrir URL directa en el navegador
            val intent = Intent(Intent.ACTION_VIEW, Uri.parse(urlApk))
            context.startActivity(intent)
        }
    }

    /**
     * Lanza el instalador del paquete APK de Android.
     */
    fun lanzarInstaladorApk(context: Context, archivoApk: File) {
        val uri = FileProvider.getUriForFile(context, "${context.packageName}.fileprovider", archivoApk)
        val intent = Intent(Intent.ACTION_VIEW).apply {
            setDataAndType(uri, "application/vnd.android.package-archive")
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }
        context.startActivity(intent)
    }
}

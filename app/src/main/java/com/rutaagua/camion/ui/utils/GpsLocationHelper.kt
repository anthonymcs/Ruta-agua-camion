package com.rutaagua.camion.ui.utils

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.location.Location
import android.location.LocationListener
import android.location.LocationManager
import android.net.Uri
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.widget.Toast
import androidx.core.content.ContextCompat

/**
 * Utilidad offline-first para captura de coordenadas GPS y navegación directa con Google Maps
 * con costo $0.00 (sin requerir APIs de pago de Google Maps).
 */
object GpsLocationHelper {

    /**
     * Verifica si la app cuenta con permisos de ubicación.
     */
    fun tienePermisosUbicacion(context: Context): Boolean {
        val fine = ContextCompat.checkSelfPermission(context, android.Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED
        val coarse = ContextCompat.checkSelfPermission(context, android.Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED
        return fine || coarse
    }

    /**
     * Obtiene las coordenadas GPS actuales del teléfono usando el LocationManager nativo del sistema.
     */
    @SuppressLint("MissingPermission")
    fun capturarUbicacionActual(
        context: Context,
        onUbicacionExitosa: (latitud: Double, longitud: Double) -> Unit,
        onError: (mensaje: String) -> Unit
    ) {
        if (!tienePermisosUbicacion(context)) {
            onError("Permiso de ubicación no otorgado")
            return
        }

        val locationManager = context.getSystemService(Context.LOCATION_SERVICE) as? LocationManager
        if (locationManager == null) {
            onError("Servicio de ubicación no disponible en el dispositivo")
            return
        }

        val gpsActivado = locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)
        val networkActivado = locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER)

        if (!gpsActivado && !networkActivado) {
            onError("El GPS está apagado. Activa la ubicación en los ajustes del teléfono.")
            return
        }

        // 1. Probar última ubicación conocida rápida
        val lastGps = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER)
        val lastNetwork = locationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER)

        val mejorUltima: Location? = when {
            lastGps != null && lastNetwork != null -> if (lastGps.time > lastNetwork.time) lastGps else lastNetwork
            lastGps != null -> lastGps
            else -> lastNetwork
        }

        // Si la última ubicación tiene menos de 2 minutos, podemos usarla o pedir un refresco rápido
        if (mejorUltima != null && (System.currentTimeMillis() - mejorUltima.time) < 120_000) {
            onUbicacionExitosa(mejorUltima.latitude, mejorUltima.longitude)
            return
        }

        // 2. Solicitar actualización puntual
        var recibida = false
        val provider = if (gpsActivado) LocationManager.GPS_PROVIDER else LocationManager.NETWORK_PROVIDER

        val listener = object : LocationListener {
            override fun onLocationChanged(location: Location) {
                if (!recibida) {
                    recibida = true
                    locationManager.removeUpdates(this)
                    onUbicacionExitosa(location.latitude, location.longitude)
                }
            }

            @Deprecated("Deprecated in Java")
            override fun onStatusChanged(provider: String?, status: Int, extras: Bundle?) {}
            override fun onProviderEnabled(provider: String) {}
            override fun onProviderDisabled(provider: String) {}
        }

        try {
            locationManager.requestSingleUpdate(provider, listener, Looper.getMainLooper())

            // Timeout de seguridad de 8 segundos si el GPS tarda en fijar satélites
            Handler(Looper.getMainLooper()).postDelayed({
                if (!recibida) {
                    locationManager.removeUpdates(listener)
                    if (mejorUltima != null) {
                        onUbicacionExitosa(mejorUltima.latitude, mejorUltima.longitude)
                    } else {
                        onError("Tiempo de espera agotado buscando satélites GPS. Intenta en un área despejada.")
                    }
                }
            }, 8000)
        } catch (e: Exception) {
            if (mejorUltima != null) {
                onUbicacionExitosa(mejorUltima.latitude, mejorUltima.longitude)
            } else {
                onError("Error al conectar con el sensor GPS: ${e.message}")
            }
        }
    }

    /**
     * Abre Google Maps trazando la ruta de navegación directa punto a punto.
     */
    fun abrirNavegacionDirecta(
        context: Context,
        latitud: Double?,
        longitud: Double?,
        enlaceAlternativo: String = ""
    ) {
        if (latitud != null && longitud != null) {
            // Intent con modo navegación guiada directa
            val uriNavegacion = Uri.parse("google.navigation:q=$latitud,$longitud&mode=d")
            val intentMaps = Intent(Intent.ACTION_VIEW, uriNavegacion).apply {
                setPackage("com.google.android.apps.maps")
            }

            try {
                context.startActivity(intentMaps)
            } catch (e: Exception) {
                // Fallback a navegador web o visor genérico de mapas
                try {
                    val webUri = Uri.parse("https://www.google.com/maps/dir/?api=1&destination=$latitud,$longitud")
                    val fallbackIntent = Intent(Intent.ACTION_VIEW, webUri)
                    context.startActivity(fallbackIntent)
                } catch (ex: Exception) {
                    Toast.makeText(context, "No se encontró aplicación de mapas instalada", Toast.LENGTH_SHORT).show()
                }
            }
        } else if (enlaceAlternativo.isNotBlank()) {
            val coords = extraerCoordenadasDeEnlace(enlaceAlternativo)
            if (coords != null) {
                abrirNavegacionDirecta(context, coords.first, coords.second)
            } else {
                try {
                    val enlaceIntent = Intent(Intent.ACTION_VIEW, Uri.parse(enlaceAlternativo))
                    context.startActivity(enlaceIntent)
                } catch (e: Exception) {
                    Toast.makeText(context, "Enlace de mapa no válido", Toast.LENGTH_SHORT).show()
                }
            }
        } else {
            Toast.makeText(context, "El cliente no tiene ubicación registrada", Toast.LENGTH_SHORT).show()
        }
    }

    /**
     * Intenta extraer coordenadas (lat, lng) de URLs de Google Maps o WhatsApp.
     * Ejemplos soportados:
     * - https://maps.google.com/?q=-2.1894,-79.8890
     * - https://www.google.com/maps/@-2.1894,-79.8890,17z
     * - geo:-2.1894,-79.8890
     */
    fun extraerCoordenadasDeEnlace(enlace: String): Pair<Double, Double>? {
        if (enlace.isBlank()) return null
        return try {
            val regexQ = Regex("[?&]q=([+-]?\\d+\\.?\\d+),([+-]?\\d+\\.?\\d+)")
            val matchQ = regexQ.find(enlace)
            if (matchQ != null) {
                val lat = matchQ.groupValues[1].toDouble()
                val lng = matchQ.groupValues[2].toDouble()
                return Pair(lat, lng)
            }

            val regexArroba = Regex("@([+-]?\\d+\\.?\\d+),([+-]?\\d+\\.?\\d+)")
            val matchArroba = regexArroba.find(enlace)
            if (matchArroba != null) {
                val lat = matchArroba.groupValues[1].toDouble()
                val lng = matchArroba.groupValues[2].toDouble()
                return Pair(lat, lng)
            }

            val regexRaw = Regex("([+-]?\\d+\\.\\d+)\\s*,\\s*([+-]?\\d+\\.\\d+)")
            val matchRaw = regexRaw.find(enlace)
            if (matchRaw != null) {
                val lat = matchRaw.groupValues[1].toDouble()
                val lng = matchRaw.groupValues[2].toDouble()
                return Pair(lat, lng)
            }
            null
        } catch (e: Exception) {
            null
        }
    }
}

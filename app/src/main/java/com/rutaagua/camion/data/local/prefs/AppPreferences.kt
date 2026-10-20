package com.rutaagua.camion.data.local.prefs

import android.content.Context
import android.content.SharedPreferences
import com.rutaagua.camion.data.local.model.TipoVehiculo
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

/**
 * Gestor centralizado de preferencias locales para vehículo activo y seguridad por PIN.
 */
class AppPreferences(context: Context) {

    private val prefs: SharedPreferences = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    private val _vehiculoActivo = MutableStateFlow(obtenerVehiculoActivo())
    val vehiculoActivo: StateFlow<TipoVehiculo> = _vehiculoActivo.asStateFlow()

    fun obtenerVehiculoActivo(): TipoVehiculo {
        val raw = prefs.getString(KEY_VEHICULO_ACTIVO, TipoVehiculo.CAMION.name)
        return TipoVehiculo.desdeString(raw)
    }

    fun guardarVehiculoActivo(tipo: TipoVehiculo) {
        prefs.edit().putString(KEY_VEHICULO_ACTIVO, tipo.name).apply()
        _vehiculoActivo.value = tipo
    }

    fun obtenerPinSeguridad(): String {
        return prefs.getString(KEY_PIN_SEGURIDAD, DEFAULT_PIN) ?: DEFAULT_PIN
    }

    fun validarPin(pinIngresado: String): Boolean {
        return obtenerPinSeguridad() == pinIngresado.trim()
    }

    fun guardarPinSeguridad(nuevoPin: String): Boolean {
        if (nuevoPin.length != 4 || !nuevoPin.all { it.isDigit() }) return false
        prefs.edit().putString(KEY_PIN_SEGURIDAD, nuevoPin).apply()
        return true
    }

    fun obtenerUrlOta(): String {
        return prefs.getString(KEY_URL_OTA, DEFAULT_OTA_URL) ?: DEFAULT_OTA_URL
    }

    fun guardarUrlOta(url: String) {
        prefs.edit().putString(KEY_URL_OTA, url.trim()).apply()
    }

    companion object {
        private const val PREFS_NAME = "ruta_agua_prefs"
        private const val KEY_VEHICULO_ACTIVO = "key_vehiculo_activo"
        private const val KEY_PIN_SEGURIDAD = "key_pin_seguridad"
        private const val KEY_URL_OTA = "key_url_ota"

        const val DEFAULT_PIN = "1234"
        const val DEFAULT_OTA_URL = "https://raw.githubusercontent.com/anthony-dev/ruta-agua/main/ota/version.json"

        @Volatile
        private var INSTANCE: AppPreferences? = null

        fun getInstance(context: Context): AppPreferences {
            return INSTANCE ?: synchronized(this) {
                INSTANCE ?: AppPreferences(context.applicationContext).also { INSTANCE = it }
            }
        }
    }
}

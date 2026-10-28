package com.rutaagua.camion.data.local.prefs

import android.content.Context
import android.content.SharedPreferences
import com.rutaagua.camion.data.local.model.TipoVehiculo
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

/**
 * Rol del usuario en la aplicación:
 * - OPERADOR_RUTA: Chofer/Ayudante en el camión. Privacidad estricta: NO ve costos ni utilidades ni arqueos con faltante/sobrante.
 * - ADMIN_ROLANDO: Dueño del negocio (opera la camioneta o supervisa). Acceso total con PIN a costos, utilidades y auditoría RP3.
 */
enum class RolUsuario(val titulo: String) {
    OPERADOR_RUTA("Teléfono de Ruta (Operador)"),
    ADMIN_ROLANDO("Rolando (Administrador)")
}

/**
 * Gestor centralizado de preferencias locales para vehículo activo, roles, costos de planta y seguridad por PIN.
 */
class AppPreferences(context: Context) {

    private val prefs: SharedPreferences = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    private val _vehiculoActivo = MutableStateFlow(obtenerVehiculoActivo())
    val vehiculoActivo: StateFlow<TipoVehiculo> = _vehiculoActivo.asStateFlow()

    private val _rolActivo = MutableStateFlow(obtenerRolActivo())
    val rolActivo: StateFlow<RolUsuario> = _rolActivo.asStateFlow()

    fun obtenerVehiculoActivo(): TipoVehiculo {
        val raw = prefs.getString(KEY_VEHICULO_ACTIVO, TipoVehiculo.CAMION.name)
        return TipoVehiculo.desdeString(raw)
    }

    fun guardarVehiculoActivo(tipo: TipoVehiculo) {
        prefs.edit().putString(KEY_VEHICULO_ACTIVO, tipo.name).apply()
        _vehiculoActivo.value = tipo
    }

    fun obtenerRolActivo(): RolUsuario {
        val raw = prefs.getString(KEY_ROL_ACTIVO, RolUsuario.OPERADOR_RUTA.name)
        return runCatching { RolUsuario.valueOf(raw ?: RolUsuario.OPERADOR_RUTA.name) }.getOrDefault(RolUsuario.OPERADOR_RUTA)
    }

    fun guardarRolActivo(rol: RolUsuario) {
        prefs.edit().putString(KEY_ROL_ACTIVO, rol.name).apply()
        _rolActivo.value = rol
    }

    fun esModoAdmin(): Boolean {
        return obtenerRolActivo() == RolUsuario.ADMIN_ROLANDO
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

    // Costos de planta configurables por Rolando
    fun obtenerCostoPlantaBidon(): Double = prefs.getFloat(KEY_COSTO_BIDON, 0.35f).toDouble()
    fun guardarCostoPlantaBidon(costo: Double) = prefs.edit().putFloat(KEY_COSTO_BIDON, costo.toFloat()).apply()

    fun obtenerCostoPlantaGalon(): Double = prefs.getFloat(KEY_COSTO_GALON, 0.25f).toDouble()
    fun guardarCostoPlantaGalon(costo: Double) = prefs.edit().putFloat(KEY_COSTO_GALON, costo.toFloat()).apply()

    fun obtenerCostoPlantaPaca(): Double = prefs.getFloat(KEY_COSTO_PACA, 2.20f).toDouble()
    fun guardarCostoPlantaPaca(costo: Double) = prefs.edit().putFloat(KEY_COSTO_PACA, costo.toFloat()).apply()

    fun obtenerCostoPlantaMini(): Double = prefs.getFloat(KEY_COSTO_MINI, 1.10f).toDouble()
    fun guardarCostoPlantaMini(costo: Double) = prefs.edit().putFloat(KEY_COSTO_MINI, costo.toFloat()).apply()

    fun obtenerCostoPlantaEnvaseNuevo(): Double = prefs.getFloat(KEY_COSTO_ENVASE_NUEVO, 3.00f).toDouble()
    fun guardarCostoPlantaEnvaseNuevo(costo: Double) = prefs.edit().putFloat(KEY_COSTO_ENVASE_NUEVO, costo.toFloat()).apply()

    fun obtenerUrlOta(): String {
        return prefs.getString(KEY_URL_OTA, DEFAULT_OTA_URL) ?: DEFAULT_OTA_URL
    }

    fun guardarUrlOta(url: String) {
        prefs.edit().putString(KEY_URL_OTA, url.trim()).apply()
    }

    fun sesionIniciada(): Boolean {
        return prefs.getBoolean(KEY_SESION_INICIADA, false)
    }

    fun guardarSesionIniciada(iniciada: Boolean) {
        prefs.edit().putBoolean(KEY_SESION_INICIADA, iniciada).apply()
    }

    fun obtenerClaveCamion(): String = prefs.getString(KEY_CLAVE_CAMION, "1111") ?: "1111"
    fun guardarClaveCamion(clave: String) = prefs.edit().putString(KEY_CLAVE_CAMION, clave.trim()).apply()

    fun obtenerClaveCamioneta(): String = prefs.getString(KEY_CLAVE_CAMIONETA, "2222") ?: "2222"
    fun guardarClaveCamioneta(clave: String) = prefs.edit().putString(KEY_CLAVE_CAMIONETA, clave.trim()).apply()

    /**
     * Valida credenciales únicas de inicio de sesión y devuelve el par (TipoVehiculo, RolUsuario)
     * o null si las credenciales no coinciden.
     */
    fun autenticarCredenciales(usuarioRaw: String, claveRaw: String): Pair<TipoVehiculo, RolUsuario>? {
        val usuario = usuarioRaw.trim().lowercase()
            .replace("ó", "o")
            .replace("á", "a")
        val clave = claveRaw.trim()
        val pinAdmin = obtenerPinSeguridad()
        val claveCamion = obtenerClaveCamion()
        val claveCamioneta = obtenerClaveCamioneta()

        return when (usuario) {
            "camion", "ruta1", "chofer" -> {
                if (clave == claveCamion || clave == "camion123" || clave == pinAdmin) {
                    Pair(TipoVehiculo.CAMION, RolUsuario.OPERADOR_RUTA)
                } else null
            }
            "camioneta", "ruta2" -> {
                if (clave == claveCamioneta || clave == "camioneta123" || clave == pinAdmin) {
                    Pair(TipoVehiculo.CAMIONETA, RolUsuario.OPERADOR_RUTA)
                } else null
            }
            "rolando", "admin", "administrador" -> {
                if (clave == pinAdmin || clave == "rolando123") {
                    Pair(TipoVehiculo.CAMIONETA, RolUsuario.ADMIN_ROLANDO)
                } else null
            }
            else -> null
        }
    }

    companion object {
        private const val PREFS_NAME = "ruta_agua_prefs"
        private const val KEY_SESION_INICIADA = "key_sesion_iniciada_v3_banca"
        private const val KEY_VEHICULO_ACTIVO = "key_vehiculo_activo"
        private const val KEY_ROL_ACTIVO = "key_rol_activo"
        private const val KEY_PIN_SEGURIDAD = "key_pin_seguridad"
        private const val KEY_CLAVE_CAMION = "key_clave_camion"
        private const val KEY_CLAVE_CAMIONETA = "key_clave_camioneta"
        private const val KEY_URL_OTA = "key_url_ota"

        private const val KEY_COSTO_BIDON = "key_costo_bidon"
        private const val KEY_COSTO_GALON = "key_costo_galon"
        private const val KEY_COSTO_PACA = "key_costo_paca"
        private const val KEY_COSTO_MINI = "key_costo_mini"
        private const val KEY_COSTO_ENVASE_NUEVO = "key_costo_envase_nuevo"

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

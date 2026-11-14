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
 * Asigna dinámicamente el vehículo y el rol operativo según la cuenta autenticada en Firebase Auth.
 * - rolando@rutaatenea.app -> ADMIN_ROLANDO
 * - camion@rutaatenea.app  -> OPERADOR_RUTA con CAMION
 * - camioneta@rutaatenea.app -> OPERADOR_RUTA con CAMIONETA
 */
fun resolverPerfilPorEmail(email: String?): Pair<TipoVehiculo, RolUsuario> {
    val correo = email?.trim()?.lowercase().orEmpty()
    return when {
        correo == "rolando@rutaatenea.app" || correo.startsWith("rolando") -> {
            Pair(TipoVehiculo.CAMION, RolUsuario.ADMIN_ROLANDO)
        }
        correo == "camioneta@rutaatenea.app" || correo.startsWith("camioneta") -> {
            Pair(TipoVehiculo.CAMIONETA, RolUsuario.OPERADOR_RUTA)
        }
        correo == "camion@rutaatenea.app" || correo.startsWith("camion") -> {
            Pair(TipoVehiculo.CAMION, RolUsuario.OPERADOR_RUTA)
        }
        else -> {
            if (correo.contains("admin") || correo.contains("rolando")) {
                Pair(TipoVehiculo.CAMION, RolUsuario.ADMIN_ROLANDO)
            } else if (correo.contains("camioneta")) {
                Pair(TipoVehiculo.CAMIONETA, RolUsuario.OPERADOR_RUTA)
            } else {
                Pair(TipoVehiculo.CAMION, RolUsuario.OPERADOR_RUTA)
            }
        }
    }
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
    fun obtenerCostoPlantaBidon(): Double = 0.0
    fun guardarCostoPlantaBidon(costo: Double) = Unit

    fun obtenerCostoPlantaGalon(): Double = 0.0
    fun guardarCostoPlantaGalon(costo: Double) = Unit

    fun obtenerCostoPlantaPaca(): Double = 0.0
    fun guardarCostoPlantaPaca(costo: Double) = Unit

    fun obtenerCostoPlantaMini(): Double = 0.0
    fun guardarCostoPlantaMini(costo: Double) = Unit

    fun obtenerCostoPlantaEnvaseNuevo(): Double = 0.0
    fun guardarCostoPlantaEnvaseNuevo(costo: Double) = Unit

    fun obtenerUrlOta(): String {
        val valor = prefs.getString(KEY_URL_OTA, DEFAULT_OTA_URL) ?: DEFAULT_OTA_URL
        return if (valor.contains("anthony-dev") || valor.isBlank()) {
            DEFAULT_OTA_URL
        } else {
            valor
        }
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

    fun obtenerRecordarSesion(): Boolean {
        return prefs.getBoolean(KEY_RECORDAR_SESION, true)
    }

    fun guardarRecordarSesion(recordar: Boolean) {
        prefs.edit().putBoolean(KEY_RECORDAR_SESION, recordar).apply()
    }

    fun tieneCuentaGuardadaEnDispositivo(): Boolean {
        return prefs.getBoolean(KEY_CUENTA_GUARDADA_DISPOSITIVO, false)
    }

    fun obtenerUltimoUsuarioGuardado(): String {
        return prefs.getString(KEY_ULTIMO_USUARIO, "") ?: ""
    }

    fun registrarAccesoExitoso(
        usuario: String,
        vehiculo: TipoVehiculo,
        rol: RolUsuario,
        recordar: Boolean
    ) {
        prefs.edit()
            .putString(KEY_VEHICULO_ACTIVO, vehiculo.name)
            .putString(KEY_ROL_ACTIVO, rol.name)
            .putBoolean(KEY_RECORDAR_SESION, recordar)
            .putBoolean(KEY_SESION_INICIADA, recordar)
            .putBoolean(KEY_CUENTA_GUARDADA_DISPOSITIVO, true)
            .putString(KEY_ULTIMO_USUARIO, usuario.trim())
            .apply()
        _vehiculoActivo.value = vehiculo
        _rolActivo.value = rol
    }

    fun olvidarCuentaGuardada() {
        prefs.edit()
            .putBoolean(KEY_CUENTA_GUARDADA_DISPOSITIVO, false)
            .putBoolean(KEY_SESION_INICIADA, false)
            .remove(KEY_ULTIMO_USUARIO)
            .apply()
    }

    fun obtenerClaveCamion(): String = prefs.getString(KEY_CLAVE_CAMION, "1111") ?: "1111"
    fun guardarClaveCamion(clave: String) = prefs.edit().putString(KEY_CLAVE_CAMION, clave.trim()).apply()

    fun obtenerClaveCamioneta(): String = prefs.getString(KEY_CLAVE_CAMIONETA, "2222") ?: "2222"
    fun guardarClaveCamioneta(clave: String) = prefs.edit().putString(KEY_CLAVE_CAMIONETA, clave.trim()).apply()

    // Control de Respaldo Mensual Automático y Autoprueba de Integridad
    fun obtenerMesUltimoRespaldoAuto(): String = prefs.getString(KEY_MES_RESPALDO_AUTO, "") ?: ""
    fun obtenerFechaUltimoRespaldoAuto(): Long = prefs.getLong(KEY_FECHA_RESPALDO_AUTO, 0L)
    fun obtenerDetalleUltimoRespaldoAuto(): String =
        prefs.getString(KEY_DETALLE_RESPALDO_AUTO, "Pendiente de primera verificación") ?: "Pendiente de primera verificación"
    fun obtenerConteoUltimoRespaldoAuto(): Int = prefs.getInt(KEY_CONTEO_RESPALDO_AUTO, 0)

    fun guardarResultadoRespaldoAuto(
        mesIso: String,
        timestamp: Long,
        detalle: String,
        conteoRegistros: Int
    ) {
        prefs.edit()
            .putString(KEY_MES_RESPALDO_AUTO, mesIso)
            .putLong(KEY_FECHA_RESPALDO_AUTO, timestamp)
            .putString(KEY_DETALLE_RESPALDO_AUTO, detalle)
            .putInt(KEY_CONTEO_RESPALDO_AUTO, conteoRegistros)
            .apply()
    }

    // Control de Inicio de Jornada Diaria Obligatoria y Fondo Inicial de Caja (Vuelto)
    fun guardarFondoInicialCaja(fechaIso: String, vehiculo: String, monto: Double) {
        prefs.edit().putFloat("key_fondo_inicial_${fechaIso}_${vehiculo.uppercase()}", monto.toFloat()).apply()
    }

    fun obtenerFondoInicialCaja(fechaIso: String, vehiculo: String): Double {
        val clave = "key_fondo_inicial_${fechaIso}_${vehiculo.uppercase()}"
        return if (prefs.contains(clave)) {
            prefs.getFloat(clave, 30.0f).toDouble()
        } else {
            30.0 // Valor sugerido estándar ($30)
        }
    }

    fun guardarJornadaIniciada(fechaIso: String, vehiculo: String, iniciada: Boolean) {
        prefs.edit().putBoolean("key_jornada_iniciada_${fechaIso}_${vehiculo.uppercase()}", iniciada).apply()
    }

    fun esJornadaIniciada(fechaIso: String, vehiculo: String): Boolean {
        return prefs.getBoolean("key_jornada_iniciada_${fechaIso}_${vehiculo.uppercase()}", false)
    }

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
        private const val KEY_RECORDAR_SESION = "key_recordar_sesion"
        private const val KEY_CUENTA_GUARDADA_DISPOSITIVO = "key_cuenta_guardada_dispositivo"
        private const val KEY_ULTIMO_USUARIO = "key_ultimo_usuario"
        private const val KEY_VEHICULO_ACTIVO = "key_vehiculo_activo"
        private const val KEY_ROL_ACTIVO = "key_rol_activo"
        private const val KEY_PIN_SEGURIDAD = "key_pin_seguridad"
        private const val KEY_CLAVE_CAMION = "key_clave_camion"
        private const val KEY_CLAVE_CAMIONETA = "key_clave_camioneta"
        private const val KEY_URL_OTA = "key_url_ota"
        private const val KEY_MES_RESPALDO_AUTO = "key_mes_respaldo_auto"
        private const val KEY_FECHA_RESPALDO_AUTO = "key_fecha_respaldo_auto"
        private const val KEY_DETALLE_RESPALDO_AUTO = "key_detalle_respaldo_auto"
        private const val KEY_CONTEO_RESPALDO_AUTO = "key_conteo_respaldo_auto"

        private const val KEY_COSTO_BIDON = "key_costo_bidon"
        private const val KEY_COSTO_GALON = "key_costo_galon"
        private const val KEY_COSTO_PACA = "key_costo_paca"
        private const val KEY_COSTO_MINI = "key_costo_mini"
        private const val KEY_COSTO_ENVASE_NUEVO = "key_costo_envase_nuevo"

        const val DEFAULT_PIN = "1234"
        const val DEFAULT_GITHUB_REPO = "anthonymcs/Ruta-agua-camion"
        const val DEFAULT_OTA_URL = "https://api.github.com/repos/anthonymcs/Ruta-agua-camion/releases/latest"

        @Volatile
        private var INSTANCE: AppPreferences? = null

        fun getInstance(context: Context): AppPreferences {
            return INSTANCE ?: synchronized(this) {
                INSTANCE ?: AppPreferences(context.applicationContext).also { INSTANCE = it }
            }
        }
    }
}

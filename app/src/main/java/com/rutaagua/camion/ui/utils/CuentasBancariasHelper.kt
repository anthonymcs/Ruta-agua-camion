package com.rutaagua.camion.ui.utils

import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.LinearGradient
import android.graphics.Paint
import android.graphics.RectF
import android.graphics.Shader
import android.graphics.Typeface
import androidx.core.content.FileProvider
import org.json.JSONArray
import org.json.JSONObject
import java.io.File
import java.io.FileOutputStream
import java.util.Locale

data class CuentaBancariaItem(
    val id: String,
    val banco: String,
    val tipoCuenta: String,
    val numeroCuenta: String,
    val titular: String,
    val cedula: String,
    val colorHex: Long
)

object CuentasBancariasHelper {

    private const val PREFS_NAME = "ruta_agua_cuentas_bancarias"
    private const val KEY_CUENTAS_JSON = "cuentas_json_v1"

    val bancosPresets: List<Pair<String, Long>> = listOf(
        "Banco Pichincha" to 0xFFF5B82EL,
        "Produbanco" to 0xFF15803DL,
        "Banco Guayaquil" to 0xFFD90479L,
        "Banco Pacífico" to 0xFF7C2230L,
        "Banco Bolivariano" to 0xFF4668D9L,
        "Cooperativa JEP" to 0xFF0284C7L
    )

    fun obtenerCuentasPorDefecto(): List<CuentaBancariaItem> = listOf(
        CuentaBancariaItem(
            id = "pichincha_rolando",
            banco = "Banco Pichincha",
            tipoCuenta = "Cuenta ahorro",
            numeroCuenta = "2203687594",
            titular = "Holger Rolando Echeverría Palma",
            cedula = "1305015974",
            colorHex = 0xFFF5B82EL
        ),
        CuentaBancariaItem(
            id = "pacifico_rolando",
            banco = "Banco Pacífico",
            tipoCuenta = "Ahorro",
            numeroCuenta = "1068047274",
            titular = "Holger Echeverría",
            cedula = "1305015974",
            colorHex = 0xFF7C2230L
        ),
        CuentaBancariaItem(
            id = "bolivariano_rolando",
            banco = "Banco Bolivariano",
            tipoCuenta = "Ahorro",
            numeroCuenta = "1101487120",
            titular = "Holger Echeverría",
            cedula = "1305015974",
            colorHex = 0xFF4668D9L
        )
    )

    fun obtenerCuentasGuardadas(context: Context): List<CuentaBancariaItem> {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val raw = prefs.getString(KEY_CUENTAS_JSON, null)
        if (raw.isNullOrBlank()) {
            val defecto = obtenerCuentasPorDefecto()
            guardarListaCuentas(context, defecto)
            return defecto
        }
        return try {
            val arr = JSONArray(raw)
            val lista = mutableListOf<CuentaBancariaItem>()
            for (i in 0 until arr.length()) {
                val obj = arr.getJSONObject(i)
                lista.add(
                    CuentaBancariaItem(
                        id = obj.optString("id", "cta_$i"),
                        banco = obj.optString("banco", "Banco"),
                        tipoCuenta = obj.optString("tipoCuenta", "Cuenta ahorro"),
                        numeroCuenta = obj.optString("numeroCuenta", ""),
                        titular = obj.optString("titular", ""),
                        cedula = obj.optString("cedula", ""),
                        colorHex = obj.optLong("colorHex", 0xFF1E3A8AL)
                    )
                )
            }
            if (lista.isEmpty()) obtenerCuentasPorDefecto() else lista
        } catch (_: Exception) {
            obtenerCuentasPorDefecto()
        }
    }

    fun guardarListaCuentas(context: Context, cuentas: List<CuentaBancariaItem>) {
        val arr = JSONArray()
        cuentas.forEach { c ->
            val obj = JSONObject().apply {
                put("id", c.id)
                put("banco", c.banco)
                put("tipoCuenta", c.tipoCuenta)
                put("numeroCuenta", c.numeroCuenta)
                put("titular", c.titular)
                put("cedula", c.cedula)
                put("colorHex", c.colorHex)
            }
            arr.put(obj)
        }
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            .edit()
            .putString(KEY_CUENTAS_JSON, arr.toString())
            .apply()
    }

    fun generarTextoCuenta(
        cuenta: CuentaBancariaItem,
        nombreCliente: String? = null,
        montoACobrar: Double? = null
    ): String {
        return buildString {
            if (!nombreCliente.isNullOrBlank()) {
                appendLine("Hola *$nombreCliente*, te compartimos los datos para transferencia bancaria:")
                appendLine()
            } else {
                appendLine("*💳 DATOS PARA TRANSFERENCIA BANCARIA*")
            }
            appendLine("🏦 *${cuenta.banco}*")
            appendLine("📌 *Tipo:* ${cuenta.tipoCuenta}")
            appendLine("🔢 *N° Cuenta:* ${cuenta.numeroCuenta}")
            appendLine("👤 *Titular:* ${cuenta.titular}")
            appendLine("🪪 *CI / RUC:* ${cuenta.cedula}")
            if (montoACobrar != null && montoACobrar > 0.0) {
                appendLine("💵 *Valor a transferir:* *$${String.format(Locale.US, "%.2f", montoACobrar)}*")
            }
            appendLine()
            append("Por favor envíanos el comprobante por este medio. ¡Gracias!")
        }
    }

    fun generarTextoTodasLasCuentas(
        cuentas: List<CuentaBancariaItem>,
        nombreCliente: String? = null,
        montoACobrar: Double? = null
    ): String {
        return buildString {
            if (!nombreCliente.isNullOrBlank()) {
                appendLine("Hola *$nombreCliente*, te compartimos nuestras cuentas bancarias disponibles para transferencia:")
            } else {
                appendLine("*💳 CUENTAS BANCARIAS PARA TRANSFERENCIA*")
            }
            if (montoACobrar != null && montoACobrar > 0.0) {
                appendLine("💵 *Valor a transferir:* *$${String.format(Locale.US, "%.2f", montoACobrar)}*")
            }
            appendLine("─────────────────────────")
            cuentas.forEach { cuenta ->
                appendLine("🏦 *${cuenta.banco}* (${cuenta.tipoCuenta})")
                appendLine("• *Cuenta:* ${cuenta.numeroCuenta}")
                appendLine("• *Titular:* ${cuenta.titular}")
                appendLine("• *CI:* ${cuenta.cedula}")
                appendLine("─────────────────────────")
            }
            append("Por favor envíanos tu comprobante por este medio. ¡Gracias!")
        }
    }

    /**
     * Genera un archivo PNG con el diseño de tarjeta bancaria profesional (igual a las imágenes de Rolando).
     */
    fun generarArchivoImagenTarjeta(
        context: Context,
        cuenta: CuentaBancariaItem,
        montoACobrar: Double? = null
    ): File {
        val width = 960
        val height = if (montoACobrar != null && montoACobrar > 0.0) 680 else 600
        val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)

        val baseColor = cuenta.colorHex.toInt()
        val darkColor = oscurecerColor(baseColor, 0.82f)

        // Fondo con bordes redondeados y gradiente suave
        val bgPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            shader = LinearGradient(
                0f, 0f, width.toFloat(), height.toFloat(),
                baseColor, darkColor, Shader.TileMode.CLAMP
            )
        }
        val cardRect = RectF(0f, 0f, width.toFloat(), height.toFloat())
        canvas.drawRoundRect(cardRect, 48f, 48f, bgPaint)

        // Círculos decorativos translúcidos estilo banca moderna
        val decoPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.argb(28, 255, 255, 255)
            style = Paint.Style.FILL
        }
        canvas.drawCircle(width * 0.88f, height * 0.18f, 210f, decoPaint)
        canvas.drawCircle(width * 0.12f, height * 0.88f, 160f, decoPaint)

        val textPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.WHITE
            textAlign = Paint.Align.CENTER
            setShadowLayer(6f, 0f, 3f, Color.argb(75, 0, 0, 0))
        }

        val centerX = width / 2f

        // 1. Nombre del Banco
        textPaint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        textPaint.textSize = 64f
        canvas.drawText(cuenta.banco, centerX, 130f, textPaint)

        // Línea divisoria sutil
        val linePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.argb(95, 255, 255, 255)
            strokeWidth = 3f
        }
        canvas.drawLine(width * 0.2f, 165f, width * 0.8f, 165f, linePaint)

        // 2. Tipo de Cuenta
        textPaint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.NORMAL)
        textPaint.textSize = 48f
        canvas.drawText(cuenta.tipoCuenta, centerX, 240f, textPaint)

        // 3. Número de Cuenta (Destacado)
        textPaint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        textPaint.textSize = 68f
        val prefijoCuenta = if (cuenta.banco.contains("Pacífico", ignoreCase = true)) {
            "Cta: ${cuenta.numeroCuenta}"
        } else if (cuenta.banco.contains("Pichincha", ignoreCase = true)) {
            "#${cuenta.numeroCuenta.removePrefix("#")}"
        } else {
            cuenta.numeroCuenta
        }
        canvas.drawText(prefijoCuenta, centerX, 335f, textPaint)

        // 4. Nombre del Titular
        textPaint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        textPaint.textSize = if (cuenta.titular.length > 24) 44f else 52f
        canvas.drawText(cuenta.titular, centerX, 430f, textPaint)

        // 5. Cédula
        textPaint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.NORMAL)
        textPaint.textSize = 48f
        val prefijoCedula = if (cuenta.banco.contains("Pacífico", ignoreCase = true)) {
            "Cédula: ${cuenta.cedula}"
        } else {
            "CI. ${cuenta.cedula}"
        }
        canvas.drawText(prefijoCedula, centerX, 515f, textPaint)

        // 6. Valor a transferir opcional
        if (montoACobrar != null && montoACobrar > 0.0) {
            val pillPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                color = Color.argb(55, 0, 0, 0)
            }
            val pillRect = RectF(width * 0.18f, 555f, width * 0.82f, 635f)
            canvas.drawRoundRect(pillRect, 28f, 28f, pillPaint)

            textPaint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            textPaint.textSize = 40f
            canvas.drawText(
                "Total a transferir: $${String.format(Locale.US, "%.2f", montoACobrar)}",
                centerX,
                608f,
                textPaint
            )
        }

        val dir = File(context.cacheDir, "cuentas_bancarias").apply {
            if (!exists()) mkdirs()
        }
        val file = File(dir, "tarjeta_${cuenta.id}.png")
        FileOutputStream(file).use { out ->
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, out)
        }
        return file
    }

    /**
     * Genera una sola imagen apilando todas las tarjetas bancarias activas.
     */
    fun generarArchivoImagenTodasLasCuentas(
        context: Context,
        cuentas: List<CuentaBancariaItem>,
        montoACobrar: Double? = null
    ): File {
        val cardWidth = 960
        val singleHeight = 420
        val spacing = 24
        val headerHeight = if (montoACobrar != null && montoACobrar > 0.0) 150 else 100
        val totalHeight = headerHeight + cuentas.size * (singleHeight + spacing) + 24

        val bitmap = Bitmap.createBitmap(cardWidth, totalHeight, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)
        canvas.drawColor(Color.parseColor("#F1F5F9"))

        val headerPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.parseColor("#0F265C")
            textAlign = Paint.Align.CENTER
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            textSize = 42f
        }
        canvas.drawText("CUENTAS PARA TRANSFERENCIA", cardWidth / 2f, 62f, headerPaint)

        if (montoACobrar != null && montoACobrar > 0.0) {
            headerPaint.color = Color.parseColor("#15803D")
            headerPaint.textSize = 36f
            canvas.drawText(
                "Valor a transferir: $${String.format(Locale.US, "%.2f", montoACobrar)}",
                cardWidth / 2f,
                115f,
                headerPaint
            )
        }

        var currentY = headerHeight.toFloat()
        val textPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.WHITE
            textAlign = Paint.Align.CENTER
            setShadowLayer(5f, 0f, 2f, Color.argb(75, 0, 0, 0))
        }

        cuentas.forEach { cuenta ->
            val baseColor = cuenta.colorHex.toInt()
            val darkColor = oscurecerColor(baseColor, 0.82f)
            val rect = RectF(28f, currentY, (cardWidth - 28).toFloat(), currentY + singleHeight)
            val bgPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                shader = LinearGradient(
                    rect.left, rect.top, rect.right, rect.bottom,
                    baseColor, darkColor, Shader.TileMode.CLAMP
                )
            }
            canvas.drawRoundRect(rect, 38f, 38f, bgPaint)

            val cx = cardWidth / 2f
            textPaint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            textPaint.textSize = 48f
            canvas.drawText("${cuenta.banco} (${cuenta.tipoCuenta})", cx, currentY + 85f, textPaint)

            textPaint.textSize = 58f
            canvas.drawText("Cta: ${cuenta.numeroCuenta}", cx, currentY + 175f, textPaint)

            textPaint.textSize = 40f
            canvas.drawText(cuenta.titular, cx, currentY + 260f, textPaint)

            textPaint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.NORMAL)
            textPaint.textSize = 38f
            canvas.drawText("CI. ${cuenta.cedula}", cx, currentY + 335f, textPaint)

            currentY += singleHeight + spacing
        }

        val dir = File(context.cacheDir, "cuentas_bancarias").apply {
            if (!exists()) mkdirs()
        }
        val file = File(dir, "tarjetas_todas_cuentas.png")
        FileOutputStream(file).use { out ->
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, out)
        }
        return file
    }

    /**
     * Envía la tarjeta bancaria en imagen (.png) junto con el texto copiable hacia WhatsApp.
     */
    fun enviarCuentaPorWhatsApp(
        context: Context,
        cuenta: CuentaBancariaItem,
        telefonoRaw: String? = null,
        nombreCliente: String? = null,
        montoACobrar: Double? = null
    ) {
        val archivoImagen = generarArchivoImagenTarjeta(context, cuenta, montoACobrar)
        val texto = generarTextoCuenta(cuenta, nombreCliente, montoACobrar)
        compartirImagenYTextoWhatsApp(context, archivoImagen, texto, telefonoRaw)
    }

    /**
     * Envía todas las cuentas bancarias en una sola imagen + texto hacia WhatsApp.
     */
    fun enviarTodasLasCuentasPorWhatsApp(
        context: Context,
        cuentas: List<CuentaBancariaItem>,
        telefonoRaw: String? = null,
        nombreCliente: String? = null,
        montoACobrar: Double? = null
    ) {
        if (cuentas.isEmpty()) return
        val archivoImagen = generarArchivoImagenTodasLasCuentas(context, cuentas, montoACobrar)
        val texto = generarTextoTodasLasCuentas(cuentas, nombreCliente, montoACobrar)
        compartirImagenYTextoWhatsApp(context, archivoImagen, texto, telefonoRaw)
    }

    private fun compartirImagenYTextoWhatsApp(
        context: Context,
        archivoImagen: File,
        texto: String,
        telefonoRaw: String?
    ) {
        try {
            val uri = FileProvider.getUriForFile(
                context,
                "${context.packageName}.fileprovider",
                archivoImagen
            )
            val numeroLimpio = telefonoRaw
                ?.takeIf { it.isNotBlank() }
                ?.let { formatearTelefonoEcuador(it) }

            val intent = Intent(Intent.ACTION_SEND).apply {
                type = "image/png"
                putExtra(Intent.EXTRA_STREAM, uri)
                putExtra(Intent.EXTRA_TEXT, texto)
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                if (!numeroLimpio.isNullOrBlank()) {
                    putExtra("jid", "$numeroLimpio@s.whatsapp.net")
                }
            }

            try {
                intent.setPackage("com.whatsapp")
                context.startActivity(intent)
            } catch (_: Exception) {
                try {
                    intent.setPackage("com.whatsapp.w4b")
                    context.startActivity(intent)
                } catch (_: Exception) {
                    intent.setPackage(null)
                    val chooser = Intent.createChooser(intent, "Enviar cuenta bancaria por WhatsApp").apply {
                        addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                    }
                    context.startActivity(chooser)
                }
            }
        } catch (_: Exception) {
            WhatsAppHelper.abrirChatWhatsApp(context, telefonoRaw ?: "", texto)
        }
    }

    private fun formatearTelefonoEcuador(raw: String): String {
        val digitos = raw.filter { it.isDigit() }
        return when {
            digitos.startsWith("593") -> digitos
            digitos.startsWith("0") && digitos.length >= 10 -> "593" + digitos.drop(1)
            digitos.length == 9 -> "593$digitos"
            else -> digitos
        }
    }

    private fun oscurecerColor(color: Int, factor: Float): Int {
        val a = Color.alpha(color)
        val r = (Color.red(color) * factor).toInt().coerceIn(0, 255)
        val g = (Color.green(color) * factor).toInt().coerceIn(0, 255)
        val b = (Color.blue(color) * factor).toInt().coerceIn(0, 255)
        return Color.argb(a, r, g, b)
    }
}

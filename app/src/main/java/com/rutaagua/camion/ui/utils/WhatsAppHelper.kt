package com.rutaagua.camion.ui.utils

import android.content.Context
import android.content.Intent
import android.net.Uri
import com.rutaagua.camion.data.local.entity.ClienteEntity
import com.rutaagua.camion.data.local.entity.TransaccionVentaEntity
import com.rutaagua.camion.data.local.model.FormaPago
import com.rutaagua.camion.data.local.model.TipoBidon
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * Utilidades para conexión directa y generación de recibos/estados de cuenta hacia WhatsApp.
 */
object WhatsAppHelper {

    /**
     * Normaliza el número de teléfono para enlaces de WhatsApp:
     * - Remueve caracteres no numéricos.
     * - Si empieza con '09' (Ecuador, 10 dígitos), reemplaza el '0' por '593'.
     * - Si tiene 9 dígitos empezando en '9', antepone '593'.
     */
    fun normalizarNumeroWhatsApp(telefonoRaw: String?): String? {
        if (telefonoRaw.isNullOrBlank()) return null
        val digitos = telefonoRaw.filter { it.isDigit() }
        if (digitos.isBlank()) return null

        return when {
            digitos.length == 10 && digitos.startsWith("09") -> "593" + digitos.substring(1)
            digitos.length == 9 && digitos.startsWith("9") -> "593$digitos"
            digitos.startsWith("593") && digitos.length >= 11 -> digitos
            digitos.length >= 7 -> digitos
            else -> digitos
        }
    }

    /**
     * Abre directamente el chat de WhatsApp con el número especificado y el mensaje precargado.
     * Si no tiene número registrado, abre el selector de contactos de WhatsApp o el menú compartir.
     */
    fun abrirChatWhatsApp(context: Context, telefonoRaw: String?, mensaje: String) {
        val numeroFormateado = normalizarNumeroWhatsApp(telefonoRaw)

        if (!numeroFormateado.isNullOrBlank()) {
            val url = "https://wa.me/$numeroFormateado?text=${Uri.encode(mensaje)}"
            val intentDirecto = Intent(Intent.ACTION_VIEW, Uri.parse(url)).apply {
                setPackage("com.whatsapp")
            }
            try {
                context.startActivity(intentDirecto)
                return
            } catch (_: Exception) {
                // Si WhatsApp no maneja el intent con package directo, intentar con el navegador/app default
                try {
                    val intentWeb = Intent(Intent.ACTION_VIEW, Uri.parse(url))
                    context.startActivity(intentWeb)
                    return
                } catch (_: Exception) {
                    compartirTextoUniversal(context, mensaje)
                }
            }
        } else {
            // Sin teléfono asignado: abrir WhatsApp para elegir contacto
            val intentCompartirWhatsApp = Intent(Intent.ACTION_SEND).apply {
                type = "text/plain"
                putExtra(Intent.EXTRA_TEXT, mensaje)
                setPackage("com.whatsapp")
            }
            try {
                context.startActivity(intentCompartirWhatsApp)
            } catch (_: Exception) {
                compartirTextoUniversal(context, mensaje)
            }
        }
    }

    private fun compartirTextoUniversal(context: Context, mensaje: String) {
        val intentUniversal = Intent(Intent.ACTION_SEND).apply {
            type = "text/plain"
            putExtra(Intent.EXTRA_TEXT, mensaje)
        }
        val chooser = Intent.createChooser(intentUniversal, "Enviar mensaje al cliente")
        context.startActivity(chooser)
    }

    /**
     * Genera el texto formal y amigable del estado de cuenta de un cliente.
     */
    fun generarMensajeEstadoCuenta(
        cliente: ClienteEntity,
        fechaDisplay: String = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).format(Date())
    ): String {
        return buildString {
            appendLine("💧 *ESTADO DE CUENTA - RUTA AGUA* 🚚")
            appendLine("Hola *${cliente.nombre}*, un cordial saludo de parte del equipo de *Ruta Agua*.")
            appendLine("📅 *Fecha:* $fechaDisplay")
            appendLine("─────────────────────────")
            appendLine("📊 *RESUMEN DE SU CUENTA:*")

            if (cliente.deudaMonetaria <= 0.0) {
                appendLine("• Saldo en dinero: *✅ Sin deuda ($0.00)*")
            } else {
                appendLine("• Saldo en dinero: *🔸 Pendiente de pago: $${String.format(Locale.US, "%.2f", cliente.deudaMonetaria)}*")
            }

            if (cliente.bidonesPrestados <= 0) {
                appendLine("• Envases de 20L: *✅ Envases prestados: 0*")
            } else {
                appendLine("• Envases de 20L: *🍾 Pendientes de devolución: ${cliente.bidonesPrestados} bidones*")
            }

            val tipoNombre = when (cliente.tipoBidonPreferido) {
                TipoBidon.TRANSPARENTE -> "PET Transparente"
                TipoBidon.AZUL -> "Azul Estándar"
                TipoBidon.MARCADO -> "Marcado"
            }
            appendLine("• Envase habitual: *$tipoNombre* ($${String.format(Locale.US, "%.2f", cliente.precioHabitual)} c/u)")

            appendLine("─────────────────────────")
            appendLine("¡Agradecemos mucho su preferencia! Si necesita agua purificada o tiene alguna consulta, con gusto le atendemos.")
            appendLine("🚚 *Ruta Agua - Reparto a Domicilio*")
        }
    }

    /**
     * Genera el recibo digital de una entrega del día lista para enviar por WhatsApp.
     */
    fun generarMensajeReciboEntrega(
        transaccion: TransaccionVentaEntity,
        cliente: ClienteEntity?,
        fechaDisplay: String = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).format(Date(transaccion.fecha))
    ): String {
        val horaTexto = SimpleDateFormat("HH:mm", Locale.getDefault()).format(Date(transaccion.fecha))
        val nombre = cliente?.nombre ?: transaccion.nombreClientePaso ?: "Estimado(a) Cliente"

        return buildString {
            appendLine("💧 *RECIBO DE ENTREGA - RUTA AGUA* 🚚")
            appendLine("Hola *$nombre*, confirmamos su entrega de hoy.")
            appendLine("📅 *Fecha:* $fechaDisplay • ⏰ *Hora:* $horaTexto")
            appendLine("─────────────────────────")
            appendLine("📦 *PRODUCTOS ENTREGADOS:*")

            val totalBidones = transaccion.cantidadBidonesRecarga + transaccion.cantidadBidonesVendidosEnvase
            if (totalBidones > 0) {
                val tipoNombre = when (transaccion.tipoBidon) {
                    TipoBidon.TRANSPARENTE -> "PET Transparente"
                    TipoBidon.AZUL -> "Azul Estándar"
                    TipoBidon.MARCADO -> "Marcado"
                }
                if (transaccion.cantidadBidonesRecarga > 0) {
                    appendLine("  • Recarga Bidón 20L ($tipoNombre): *${transaccion.cantidadBidonesRecarga} un.* x $${String.format(Locale.US, "%.2f", transaccion.precioUnitarioBidon)}")
                }
                if (transaccion.cantidadBidonesVendidosEnvase > 0) {
                    appendLine("  • Bidón Nuevo + Agua ($tipoNombre): *${transaccion.cantidadBidonesVendidosEnvase} un.* x $${String.format(Locale.US, "%.2f", transaccion.precioUnitarioBidonVendido)}")
                }
            }

            if (transaccion.cantidadGalones > 0) {
                appendLine("  • Galones (4L): *${transaccion.cantidadGalones} un.* x $${String.format(Locale.US, "%.2f", transaccion.precioUnitarioGalon)}")
            }
            if (transaccion.cantidadPacas > 0) {
                appendLine("  • Pacas (24x500ml): *${transaccion.cantidadPacas} un.* x $${String.format(Locale.US, "%.2f", transaccion.precioUnitarioPaca)}")
            }
            if (transaccion.cantidadMinis > 0) {
                appendLine("  • Minis (12x500ml): *${transaccion.cantidadMinis} un.* x $${String.format(Locale.US, "%.2f", transaccion.precioUnitarioMini)}")
            }

            // Movimiento de envases prestados/devueltos
            if (transaccion.cantidadBidonesPrestados > 0) {
                appendLine("  • Envases prestados hoy: *+${transaccion.cantidadBidonesPrestados} un.* (queda debiendo)")
            }
            if (transaccion.cantidadBidonesDevueltos > 0) {
                appendLine("  • Envases devueltos hoy: *-${transaccion.cantidadBidonesDevueltos} un.* (recolectados)")
            }

            appendLine("─────────────────────────")
            appendLine("💰 *TOTAL DE LA ENTREGA:* *$${String.format(Locale.US, "%.2f", transaccion.totalImporte)}*")

            val detallePago = when (transaccion.formaPagoPrincipal) {
                FormaPago.EFECTIVO -> "Efectivo ($${String.format(Locale.US, "%.2f", transaccion.totalImporte)})"
                FormaPago.TRANSFERENCIA -> "Transferencia bancaria ($${String.format(Locale.US, "%.2f", transaccion.totalImporte)})"
                FormaPago.CREDITO -> "Crédito pendiente ($${String.format(Locale.US, "%.2f", transaccion.totalImporte)})"
                FormaPago.ABONO -> "Abono a cuenta ($${String.format(Locale.US, "%.2f", transaccion.montoAbono)})"
                FormaPago.CANCELADO -> "Deuda cancelada ($${String.format(Locale.US, "%.2f", transaccion.montoCanceladoDeuda)})"
            }
            appendLine("💳 *Forma de Pago:* $detallePago")

            if (cliente != null) {
                appendLine("─────────────────────────")
                appendLine("📊 *ESTADO ACTUAL DE SU CUENTA:*")
                if (cliente.deudaMonetaria <= 0.0) {
                    appendLine("• Saldo en dinero: *✅ Al día ($0.00)*")
                } else {
                    appendLine("• Saldo en dinero: *🔸 Debe: $${String.format(Locale.US, "%.2f", cliente.deudaMonetaria)}*")
                }

                if (cliente.bidonesPrestados <= 0) {
                    appendLine("• Envases prestados: *✅ 0 envases*")
                } else {
                    appendLine("• Envases prestados: *🍾 Debe: ${cliente.bidonesPrestados} bidones*")
                }
            }

            if (transaccion.nota.isNotBlank()) {
                appendLine("📝 *Nota:* ${transaccion.nota}")
            }

            appendLine("─────────────────────────")
            appendLine("¡Muchas gracias por su compra! Que tenga un excelente día.")
            appendLine("🚚 *Ruta Agua - Reparto a Domicilio*")
        }
    }
}

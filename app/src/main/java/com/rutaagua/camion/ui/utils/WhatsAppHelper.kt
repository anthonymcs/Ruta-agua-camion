package com.rutaagua.camion.ui.utils

import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.core.content.FileProvider
import com.rutaagua.camion.data.local.entity.CierreCajaEntity
import com.rutaagua.camion.data.local.entity.ClienteEntity
import com.rutaagua.camion.data.local.entity.TransaccionVentaEntity
import com.rutaagua.camion.data.local.model.FormaPago
import com.rutaagua.camion.data.local.model.ResumenCierreDia
import com.rutaagua.camion.data.local.model.TipoBidon
import java.io.File
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
            appendLine("💧 *ESTADO DE CUENTA - ATENEA* 🚚")
            appendLine("Hola *${cliente.nombre}*, un cordial saludo de parte del equipo de *Atenea*.")
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
            appendLine("Gracias por confiar en nosotros. Atentamente, Atenea 💧🚚")
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
            appendLine("💧 *RECIBO DE ENTREGA - ATENEA* 🚚")
            appendLine("Hola *$nombre*, confirmamos su entrega de hoy.")
            appendLine("📅 *Fecha:* $fechaDisplay • ⏰ *Hora:* $horaTexto")
            appendLine("─────────────────────────")
            appendLine("📦 *PRODUCTOS ENTREGADOS:*")

            val tieneMulti = (transaccion.recargaTransparentes + transaccion.recargaAzules + transaccion.recargaMarcados) > 0
            if (tieneMulti) {
                if (transaccion.recargaTransparentes > 0) {
                    appendLine("  • Recarga Bidón 20L (PET Transparente): *${transaccion.recargaTransparentes} un.* x $${String.format(Locale.US, "%.2f", transaccion.precioUnitarioBidon)}")
                }
                if (transaccion.recargaAzules > 0) {
                    appendLine("  • Recarga Bidón 20L (Azul Estándar): *${transaccion.recargaAzules} un.* x $${String.format(Locale.US, "%.2f", transaccion.precioUnitarioBidon)}")
                }
                if (transaccion.recargaMarcados > 0) {
                    appendLine("  • Recarga Bidón 20L (Marcado): *${transaccion.recargaMarcados} un.* x $${String.format(Locale.US, "%.2f", transaccion.precioUnitarioBidon)}")
                }
                if (transaccion.cantidadBidonesVendidosEnvase > 0) {
                    appendLine("  • Bidón con Todo / Nuevo: *${transaccion.cantidadBidonesVendidosEnvase} un.* x $${String.format(Locale.US, "%.2f", transaccion.precioUnitarioBidonVendido)}")
                }
            } else {
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

            // Movimiento de envases prestados/devueltos y cortesías
            if (transaccion.cantidadBidonesPrestados > 0) {
                appendLine("  • Envases prestados hoy: *+${transaccion.cantidadBidonesPrestados} un.* x $${String.format(Locale.US, "%.2f", transaccion.precioUnitarioBidon)} (queda debiendo envase)")
            }
            if (transaccion.cantidadBidonesGratis > 0) {
                val cobrados = maxOf(0, transaccion.cantidadBidonesRecarga + transaccion.cantidadBidonesPrestados - transaccion.cantidadBidonesGratis)
                appendLine("  • Cortesía (${cobrados}+${transaccion.cantidadBidonesGratis}): *${transaccion.cantidadBidonesGratis} bidón gratis*")
            }
            if (transaccion.cantidadBidonesDevueltos > 0) {
                appendLine("  • Envases devueltos hoy: *-${transaccion.cantidadBidonesDevueltos} un.* (recolectados)")
            }

            appendLine("─────────────────────────")
            appendLine("💰 *TOTAL DE LA ENTREGA:* *$${String.format(Locale.US, "%.2f", transaccion.totalImporte)}*")

            val partesPago = mutableListOf<String>()
            if (transaccion.montoEfectivo > 0) partesPago.add("Efectivo: $${String.format(Locale.US, "%.2f", transaccion.montoEfectivo)}")
            if (transaccion.montoTransferencia > 0) partesPago.add("Transferencia: $${String.format(Locale.US, "%.2f", transaccion.montoTransferencia)}")
            if (transaccion.montoCredito > 0) partesPago.add("Crédito: $${String.format(Locale.US, "%.2f", transaccion.montoCredito)}")
            if (transaccion.montoAbono > 0) partesPago.add("Abono Deuda: $${String.format(Locale.US, "%.2f", transaccion.montoAbono)}")

            val detallePago = if (partesPago.size > 1) {
                partesPago.joinToString(" + ")
            } else {
                when (transaccion.formaPagoPrincipal) {
                    FormaPago.EFECTIVO -> "Efectivo ($${String.format(Locale.US, "%.2f", transaccion.totalImporte)})"
                    FormaPago.TRANSFERENCIA -> "Transferencia bancaria ($${String.format(Locale.US, "%.2f", transaccion.totalImporte)})"
                    FormaPago.CREDITO -> "Crédito pendiente ($${String.format(Locale.US, "%.2f", transaccion.totalImporte)})"
                    FormaPago.ABONO -> "Abono a cuenta ($${String.format(Locale.US, "%.2f", transaccion.montoAbono)})"
                    FormaPago.CANCELADO -> "Deuda cancelada ($${String.format(Locale.US, "%.2f", transaccion.montoCanceladoDeuda)})"
                }
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
            appendLine("Gracias por confiar en nosotros. Atentamente, Atenea 💧🚚")
        }
    }

    /**
     * Genera el mensaje de cortesía con foto cuando el cliente no estaba en casa.
     */
    fun generarMensajeVisitaFallida(nombreCliente: String, fecha: String, hora: String): String {
        return "¡Hola $nombreCliente! 🚚💧 Pasamos hoy $fecha a las $hora para realizar su entrega de agua, pero no encontramos a nadie en casa. Le adjuntamos la foto de constancia de nuestra visita. Escríbanos a este número de ruta si desea que pasemos más tarde.\n\nGracias por confiar en nosotros. Atentamente, Atenea 💧🚚"
    }

    /**
     * Envía la constancia de visita fallida con foto adjunta mediante WhatsApp.
     */
    fun enviarVisitaFallidaConFoto(
        context: Context,
        telefonoRaw: String?,
        mensaje: String,
        fotoPath: String?
    ) {
        val numeroNormalizado = normalizarNumeroWhatsApp(telefonoRaw)
        val uriFoto = if (!fotoPath.isNullOrBlank()) {
            val file = File(fotoPath)
            if (file.exists()) {
                try {
                    FileProvider.getUriForFile(
                        context,
                        "${context.packageName}.fileprovider",
                        file
                    )
                } catch (_: Exception) {
                    null
                }
            } else null
        } else null

        if (uriFoto != null) {
            val intentFoto = Intent(Intent.ACTION_SEND).apply {
                type = "image/jpeg"
                putExtra(Intent.EXTRA_STREAM, uriFoto)
                putExtra(Intent.EXTRA_TEXT, mensaje)
                if (!numeroNormalizado.isNullOrBlank()) {
                    putExtra("jid", "$numeroNormalizado@s.whatsapp.net")
                }
                setPackage("com.whatsapp")
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            }
            try {
                context.startActivity(intentFoto)
                return
            } catch (_: Exception) {
                // Fallback sin fijar package com.whatsapp
                try {
                    val chooser = Intent.createChooser(
                        Intent(Intent.ACTION_SEND).apply {
                            type = "image/jpeg"
                            putExtra(Intent.EXTRA_STREAM, uriFoto)
                            putExtra(Intent.EXTRA_TEXT, mensaje)
                            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                        },
                        "Enviar constancia de visita"
                    )
                    context.startActivity(chooser)
                    return
                } catch (_: Exception) {}
            }
        }

        // Si no hay foto válida, enviar solo texto
        abrirChatWhatsApp(context, telefonoRaw, mensaje)
    }

    /**
     * Genera el texto del resumen de Cierre de Caja para enviar por WhatsApp a Rolando,
     * incluyendo la suma de Créditos, Abonos, Efectivo, Transferencias y el desglose por precio de cliente.
     */
    fun generarMensajeCierreCajaOperador(
        cierre: CierreCajaEntity,
        resumenDia: ResumenCierreDia? = null
    ): String {
        val total = cierre.totalDeclarado
        val efectivo = cierre.efectivoDeclarado
        val transferencias = cierre.transferenciasDeclaradas
        val vehiculo = if (cierre.vehiculo == "CAMIONETA") "CAMIONETA" else "CAMIÓN"
        val fechaMostrar = cierre.fechaIso.ifBlank {
            SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).format(Date())
        }

        return buildString {
            appendLine("🔒 *CIERRE DE CAJA Y REPORTE DIARIO - $vehiculo* 🚚💧")
            appendLine("📅 *Fecha:* $fechaMostrar")
            appendLine("─────────────────────────")
            appendLine("💵 *Efectivo contado a entregar:* $${String.format(Locale.US, "%.2f", efectivo)}")
            appendLine("📱 *Transferencias verificadas:* $${String.format(Locale.US, "%.2f", transferencias)}")
            appendLine("💰 *TOTAL DECLARADO A ENTREGAR:* *$${String.format(Locale.US, "%.2f", total)}*")

            if (resumenDia != null) {
                appendLine("─────────────────────────")
                appendLine("📊 *SUMA DE OPERACIONES DEL DÍA:*")
                appendLine("• Efectivo del día: *$${String.format(Locale.US, "%.2f", resumenDia.totalIngresoEfectivo)}*")
                appendLine("• Transferencias del día: *$${String.format(Locale.US, "%.2f", resumenDia.totalIngresoTransferencia)}*")
                appendLine("• Créditos otorgados (Fiado): *$${String.format(Locale.US, "%.2f", resumenDia.totalCreditosOtorgados)}*")
                appendLine("• Abonos / Deudas cobradas: *$${String.format(Locale.US, "%.2f", resumenDia.totalAbonosYCancelados)}*")
                appendLine("• Gastos en ruta: *$${String.format(Locale.US, "%.2f", resumenDia.totalGastosRuta)}*")
                appendLine("• Efectivo Neto Sistema: *$${String.format(Locale.US, "%.2f", resumenDia.efectivoNetoCaja)}*")

                if (resumenDia.desglosePorPrecio.isNotEmpty()) {
                    appendLine("─────────────────────────")
                    appendLine("🏷️ *CLIENTES POR PRECIO DE AGUA:*")
                    resumenDia.desglosePorPrecio.forEach { item ->
                        val etiquetaClientes = if (item.cantidadClientes == 1) "1 cliente" else "${item.cantidadClientes} clientes"
                        appendLine(
                            "  • Clientes $${String.format(Locale.US, "%.2f", item.precioUnitario)}: *$etiquetaClientes* (${item.cantidadBidonesCobrados} bidones) → *Total: $${String.format(Locale.US, "%.2f", item.totalImporteAgua)}*"
                        )
                    }
                }
            }

            if (cierre.observaciones.isNotBlank()) {
                appendLine("─────────────────────────")
                appendLine("📝 *Observaciones:* ${cierre.observaciones}")
            }
            appendLine("─────────────────────────")
            appendLine("Enviado desde Atenea • Cierre de Jornada 💧🚚")
        }
    }
}

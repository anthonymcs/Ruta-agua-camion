package com.rutaagua.camion.ui.utils

import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.core.content.FileProvider
import com.rutaagua.camion.data.local.entity.CierreCajaEntity
import com.rutaagua.camion.data.local.entity.ClienteEntity
import com.rutaagua.camion.data.local.entity.RecargaPlantaEntity
import com.rutaagua.camion.data.local.entity.TransaccionVentaEntity
import com.rutaagua.camion.data.local.model.EstadisticasVentasTipo
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
            appendLine("💧 *ESTADO DE CUENTA* 🚚")
            appendLine("Hola *${cliente.nombre}*, un cordial saludo de parte del equipo de Repartidor de Agua.")
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
            appendLine("Gracias por confiar en nosotros. 💧🚚")
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
        val nombre = cliente?.nombreVisual ?: cliente?.nombre ?: transaccion.nombreClientePaso ?: "Estimado(a) Cliente"

        return buildString {
            appendLine("💧 *Aqua Apolo - Comprobante de Entrega* 🚚")
            appendLine("Hola *$nombre*, detalle de su entrega hoy ($fechaDisplay $horaTexto):")
            appendLine()

            // 1. Productos dejados hoy
            val itemsDejados = mutableListOf<String>()
            val tieneMulti = (transaccion.recargaTransparentes + transaccion.recargaAzules + transaccion.recargaMarcados) > 0
            if (tieneMulti) {
                if (transaccion.recargaTransparentes > 0) {
                    val sub = transaccion.recargaTransparentes * transaccion.precioUnitarioBidon
                    itemsDejados.add("${transaccion.recargaTransparentes} bidones transparentes ($${String.format(Locale.US, "%.2f", transaccion.precioUnitarioBidon)} c/u): $${String.format(Locale.US, "%.2f", sub)}")
                }
                if (transaccion.recargaAzules > 0) {
                    val sub = transaccion.recargaAzules * transaccion.precioUnitarioBidon
                    itemsDejados.add("${transaccion.recargaAzules} bidones azules ($${String.format(Locale.US, "%.2f", transaccion.precioUnitarioBidon)} c/u): $${String.format(Locale.US, "%.2f", sub)}")
                }
                if (transaccion.recargaMarcados > 0) {
                    val sub = transaccion.recargaMarcados * transaccion.precioUnitarioBidon
                    itemsDejados.add("${transaccion.recargaMarcados} bidones marcados ($${String.format(Locale.US, "%.2f", transaccion.precioUnitarioBidon)} c/u): $${String.format(Locale.US, "%.2f", sub)}")
                }
            } else if (transaccion.cantidadBidonesRecarga > 0) {
                val sub = transaccion.cantidadBidonesRecarga * transaccion.precioUnitarioBidon
                itemsDejados.add("${transaccion.cantidadBidonesRecarga} bidones ($${String.format(Locale.US, "%.2f", transaccion.precioUnitarioBidon)} c/u): $${String.format(Locale.US, "%.2f", sub)}")
            }

            if (transaccion.cantidadBidonesVendidosEnvase > 0) {
                val sub = transaccion.cantidadBidonesVendidosEnvase * transaccion.precioUnitarioBidonVendido
                itemsDejados.add("${transaccion.cantidadBidonesVendidosEnvase} envases con agua ($${String.format(Locale.US, "%.2f", transaccion.precioUnitarioBidonVendido)} c/u): $${String.format(Locale.US, "%.2f", sub)}")
            }

            if (transaccion.cantidadGalones > 0) {
                val sub = transaccion.cantidadGalones * transaccion.precioUnitarioGalon
                itemsDejados.add("${transaccion.cantidadGalones} galones 4L ($${String.format(Locale.US, "%.2f", transaccion.precioUnitarioGalon)} c/u): $${String.format(Locale.US, "%.2f", sub)}")
            }

            if (transaccion.cantidadPacas > 0) {
                val sub = transaccion.cantidadPacas * transaccion.precioUnitarioPaca
                itemsDejados.add("${transaccion.cantidadPacas} paquetes de botellas de agua ($${String.format(Locale.US, "%.2f", transaccion.precioUnitarioPaca)} c/u): $${String.format(Locale.US, "%.2f", sub)}")
            }

            if (transaccion.cantidadMinis > 0) {
                val sub = transaccion.cantidadMinis * transaccion.precioUnitarioMini
                itemsDejados.add("${transaccion.cantidadMinis} paquetes minis ($${String.format(Locale.US, "%.2f", transaccion.precioUnitarioMini)} c/u): $${String.format(Locale.US, "%.2f", sub)}")
            }

            if (transaccion.cantidadBidonesGratis > 0) {
                itemsDejados.add("${transaccion.cantidadBidonesGratis} bidón de cortesía (gratis)")
            }

            if (itemsDejados.isNotEmpty()) {
                appendLine("📦 *Hoy se dejó:*")
                itemsDejados.forEach { appendLine("• $it") }
                appendLine("💵 *Total de hoy:* *$${String.format(Locale.US, "%.2f", transaccion.totalImporte)}*")
            } else if (transaccion.montoAbono > 0 || transaccion.montoCanceladoDeuda > 0) {
                appendLine("📦 *Despacho de hoy:* Gestión de cobro / abono a cuenta")
            }

            // 2. Movimiento de envases prestados/devueltos si aplica
            if (transaccion.cantidadBidonesPrestados > 0 || transaccion.cantidadBidonesDevueltos > 0) {
                appendLine()
                if (transaccion.cantidadBidonesPrestados > 0) {
                    appendLine("🍾 *Envases prestados hoy:* +${transaccion.cantidadBidonesPrestados} (pendiente devolución)")
                }
                if (transaccion.cantidadBidonesDevueltos > 0) {
                    appendLine("🔄 *Envases devueltos hoy:* -${transaccion.cantidadBidonesDevueltos}")
                }
            }

            // 3. Cuenta y Liquidación de Deudas
            appendLine()
            val deudaActual = cliente?.deudaMonetaria ?: transaccion.montoCredito
            val deudaAnterior = maxOf(0.0, deudaActual - transaccion.montoCredito + transaccion.montoAbono + transaccion.montoCanceladoDeuda)
            val totalPagadoHoy = transaccion.montoEfectivo + transaccion.montoTransferencia

            if (deudaAnterior > 0.05) {
                appendLine("🔸 *Debía anterior:* $${String.format(Locale.US, "%.2f", deudaAnterior)}")
                val totalConDeuda = deudaAnterior + transaccion.totalImporte
                appendLine("📊 *Más lo de hoy son:* $${String.format(Locale.US, "%.2f", totalConDeuda)}")
            }

            if (totalPagadoHoy > 0.05) {
                val metodo = when {
                    transaccion.montoEfectivo > 0 && transaccion.montoTransferencia > 0 -> "Efectivo $${String.format(Locale.US, "%.2f", transaccion.montoEfectivo)} + Transf $${String.format(Locale.US, "%.2f", transaccion.montoTransferencia)}"
                    transaccion.montoTransferencia > 0 -> "Transferencia"
                    else -> "Efectivo"
                }
                appendLine("💳 *Pagó / Abonó hoy:* $${String.format(Locale.US, "%.2f", totalPagadoHoy)} ($metodo)")
            } else if (transaccion.montoCredito > 0.05) {
                appendLine("💳 *Quedó a crédito (fiado):* $${String.format(Locale.US, "%.2f", transaccion.montoCredito)}")
            }

            if (deudaActual <= 0.05) {
                appendLine("✅ *Quedamos en: $0.00* (¡Cuenta al día!)")
            } else {
                appendLine("🔸 *Quedamos en:* *$${String.format(Locale.US, "%.2f", deudaActual)}*")
            }

            if (cliente != null && cliente.bidonesPrestados > 0) {
                appendLine("🍾 *Envases prestados pendientes:* ${cliente.bidonesPrestados} bidones")
            }

            if (transaccion.nota.isNotBlank()) {
                appendLine("📝 *Nota:* ${transaccion.nota}")
            }

            appendLine()
            append("¡Muchas gracias por su preferencia! 💧🚚")
        }
    }

    /**
     * Genera el mensaje de cortesía con foto cuando el cliente no estaba en casa.
     */
    fun generarMensajeVisitaFallida(nombreCliente: String, fecha: String, hora: String): String {
        return "¡Hola $nombreCliente! 🚚💧 Pasamos hoy $fecha a las $hora para realizar su entrega de agua, pero no encontramos a nadie en casa. Le adjuntamos la foto de constancia de nuestra visita.\n\nGracias por confiar en nosotros. Atentamente, Atenea 💧🚚"
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
     * incluyendo la suma de Créditos, Abonos, Efectivo, Transferencias, bidones exactos vendidos por tipo
     * y el desglose de cargas del día.
     */
    fun generarMensajeCierreCajaOperador(
        cierre: CierreCajaEntity,
        resumenDia: ResumenCierreDia? = null,
        estadisticas: EstadisticasVentasTipo? = null,
        recargasHoy: List<RecargaPlantaEntity> = emptyList()
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
                appendLine("📊 *SUMA CONTABLE DEL DÍA:*")
                appendLine("• Efectivo del día: *$${String.format(Locale.US, "%.2f", resumenDia.totalIngresoEfectivo)}*")
                appendLine("• Transferencias del día: *$${String.format(Locale.US, "%.2f", resumenDia.totalIngresoTransferencia)}*")
                appendLine("• Créditos otorgados (Fiado): *$${String.format(Locale.US, "%.2f", resumenDia.totalCreditosOtorgados)}*")
                appendLine("• Abonos / Deudas cobradas: *$${String.format(Locale.US, "%.2f", resumenDia.totalAbonosYCancelados)}*")
                appendLine("• Gastos en ruta: *$${String.format(Locale.US, "%.2f", resumenDia.totalGastosRuta)}*")
                appendLine("• Efectivo Neto en Caja: *$${String.format(Locale.US, "%.2f", resumenDia.efectivoNetoCaja)}*")
            }

            if (estadisticas != null) {
                appendLine("─────────────────────────")
                appendLine("📦 *BIDONES EXACTOS VENDIDOS EN RUTA:*")
                appendLine("  • Azules: *${estadisticas.azulesVendidos} un.*")
                appendLine("  • Transparentes (PET): *${estadisticas.transparentesVendidos} un.*")
                appendLine("  • Marcadas: *${estadisticas.marcadosVendidos} un.*")
                appendLine("  • TOTAL BIDONES 20L: *${estadisticas.totalBidonesVendidos} un.*")
                if (estadisticas.envasesNuevosVendidos > 0) {
                    appendLine("  • Envases nuevos (con todo): *${estadisticas.envasesNuevosVendidos} un.*")
                }
                if (estadisticas.galonesVendidos > 0) appendLine("  • Galones (4L): *${estadisticas.galonesVendidos} un.*")
                if (estadisticas.pacasVendidas > 0) appendLine("  • Pacas (24x): *${estadisticas.pacasVendidas} un.*")
                if (estadisticas.minisVendidas > 0) appendLine("  • Minis (12x): *${estadisticas.minisVendidas} un.*")
            }

            if (resumenDia != null && resumenDia.desglosePorPrecio.isNotEmpty()) {
                appendLine("─────────────────────────")
                appendLine("🏷️ *CLIENTES POR PRECIO DE AGUA:*")
                resumenDia.desglosePorPrecio.forEach { item ->
                    val etiquetaClientes = if (item.cantidadClientes == 1) "1 cliente" else "${item.cantidadClientes} clientes"
                    appendLine(
                        "  • Clientes $${String.format(Locale.US, "%.2f", item.precioUnitario)}: *$etiquetaClientes* (${item.cantidadBidonesCobrados} bidones) → *Total: $${String.format(Locale.US, "%.2f", item.totalImporteAgua)}*"
                    )
                }
            }

            if (recargasHoy.isNotEmpty()) {
                appendLine("─────────────────────────")
                appendLine("🚚 *CARGAS REALIZADAS EN EL DÍA (${recargasHoy.size}):*")
                recargasHoy.sortedBy { it.numeroVuelta }.forEach { r ->
                    val detalle = buildList {
                        if (r.bidonesCargadosAzules > 0) add("${r.bidonesCargadosAzules} azules")
                        if (r.bidonesCargadosTransparentes > 0) add("${r.bidonesCargadosTransparentes} PET")
                        if (r.bidonesCargadosMarcados > 0) add("${r.bidonesCargadosMarcados} marcadas")
                        if (r.galonesCargados > 0) add("${r.galonesCargados} galones")
                        if (r.pacasCargadas > 0) add("${r.pacasCargadas} pacas")
                        if (r.minisCargadas > 0) add("${r.minisCargadas} minis")
                    }.joinToString(", ")
                    appendLine("  • Carga ${r.numeroVuelta}: *+${r.totalBidonesCargados()} bidones* ($detalle)")
                }
                val totalBidonesCargadosHoy = recargasHoy.sumOf { it.totalBidonesCargados() }
                appendLine("  • Total abastecido hoy: *+$totalBidonesCargadosHoy bidones*")
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

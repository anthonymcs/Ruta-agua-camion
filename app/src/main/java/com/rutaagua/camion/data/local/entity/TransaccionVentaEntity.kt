package com.rutaagua.camion.data.local.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import com.rutaagua.camion.data.local.model.FormaPago
import com.rutaagua.camion.data.local.model.TipoBidon

/**
 * Entidad que registra cada venta, entrega o abono en la ruta.
 * Vinculada opcionalmente a un cliente registrado (o de paso).
 */
@Entity(
    tableName = "transacciones_ventas",
    foreignKeys = [
        ForeignKey(
            entity = ClienteEntity::class,
            parentColumns = ["id"],
            childColumns = ["clienteId"],
            onDelete = ForeignKey.SET_NULL
        )
    ],
    indices = [
        Index("clienteId"),
        Index("fechaTexto")
    ]
)
data class TransaccionVentaEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val vehiculo: String = "CAMION", // "CAMION" o "CAMIONETA"
    val clienteId: Long? = null, // Null si es cliente de paso ocasional
    val nombreClientePaso: String? = null, // Nombre o referencia para clientes de paso
    val fecha: Long = System.currentTimeMillis(),
    val fechaTexto: String, // "YYYY-MM-DD" para auditoría y arqueo diario

    // Desglose de Bidones de 20L
    val tipoBidon: TipoBidon = TipoBidon.AZUL,
    val cantidadBidonesRecarga: Int = 0, // Recarga estándar (total o azul por compatibilidad)
    val precioUnitarioBidon: Double = 1.00,
    val cantidadBidonesPrestados: Int = 0, // Baja lleno, NO sube vacío (debe envase)
    val cantidadBidonesDevueltos: Int = 0, // Sube vacío, reduce bidones prestados del cliente
    val cantidadBidonesVendidosEnvase: Int = 0, // Venta de envase nuevo ($5.50 - $6.00)
    val precioUnitarioBidonVendido: Double = 0.0,

    // Desglose multi-tipo por bidón específico en una misma entrega
    val recargaTransparentes: Int = 0,
    val recargaAzules: Int = 0,
    val recargaMarcados: Int = 0,
    val prestadosTransparentes: Int = 0,
    val prestadosAzules: Int = 0,
    val prestadosMarcados: Int = 0,
    val envasesVendidosTransparentes: Int = 0,
    val envasesVendidosAzules: Int = 0,
    val envasesVendidosMarcados: Int = 0,
    val cantidadBidonesGratis: Int = 0, // Bidón de cortesía (ej. "35+1", baja del camión pero no se cobra)

    // Otros productos de venta regular
    val cantidadGalones: Int = 0, // 4L ($0.75 c/u)
    val precioUnitarioGalon: Double = 0.75,
    val cantidadPacas: Int = 0, // Pack 24 x 500ml ($3.75 c/u)
    val precioUnitarioPaca: Double = 3.75,
    val cantidadMinis: Int = 0, // Pack 12 x 500ml ($1.75 c/u)
    val precioUnitarioMini: Double = 1.75,

    // Liquidación financiera
    val totalImporte: Double = 0.0, // Monto total de los artículos vendidos hoy
    val formaPagoPrincipal: FormaPago = FormaPago.EFECTIVO,
    val montoEfectivo: Double = 0.0,
    val montoTransferencia: Double = 0.0,
    val montoCredito: Double = 0.0, // Quedó debiendo en esta transacción
    val montoAbono: Double = 0.0, // Pagó parte de una deuda previa
    val montoCanceladoDeuda: Double = 0.0, // Liquidó el total de deuda previa
    val nota: String = ""
)

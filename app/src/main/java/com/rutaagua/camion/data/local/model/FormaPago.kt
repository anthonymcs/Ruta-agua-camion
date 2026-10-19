package com.rutaagua.camion.data.local.model

/**
 * Métodos y modalidades de pago registradas en ruta física:
 * - EFECTIVO: Pago en dinero en efectivo.
 * - TRANSFERENCIA: Pago mediante transferencia bancaria.
 * - CREDITO: El cliente queda debiendo el importe.
 * - ABONO: Pago parcial para reducir deuda pendiente anterior.
 * - CANCELADO: Liquidación total de la deuda acumulada.
 */
enum class FormaPago {
    EFECTIVO,
    TRANSFERENCIA,
    CREDITO,
    ABONO,
    CANCELADO
}

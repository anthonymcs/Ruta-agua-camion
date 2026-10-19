package com.rutaagua.camion.data.local.model

/**
 * Clasificación de clientes guardados en el directorio:
 * - RUTA: Clientes con días fijos de visita (se reflejan en el calendario).
 * - LLAMADA: Clientes que realizan pedidos a demanda (WhatsApp o llamada).
 */
enum class TipoCliente {
    RUTA,
    LLAMADA
}

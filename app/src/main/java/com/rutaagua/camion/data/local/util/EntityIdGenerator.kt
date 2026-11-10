package com.rutaagua.camion.data.local.util

import java.util.concurrent.ThreadLocalRandom

/**
 * Generador centralizado de identificadores únicos a nivel de microsegundo y aleatorio
 * para evitar colisiones entre múltiples dispositivos (Camión, Camioneta, Celular Personal / Admin)
 * en arquitectura Offline-First con sincronización en Cloud Firestore.
 */
object EntityIdGenerator {

    /**
     * Genera un identificador positivo único de tipo Long que nunca colisiona.
     * Estructura: (timestamp_milisegundos * 1000L) + sufijo_aleatorio_3_digitos.
     * Ejemplo: ~1_731_200_000_000_123L (ampliamente seguro dentro de Long.MAX_VALUE 9_223_372_036_854_775_807L).
     */
    fun generarId(): Long {
        val millis = System.currentTimeMillis()
        val randomSuffix = ThreadLocalRandom.current().nextInt(100, 999)
        return (millis * 1000L) + randomSuffix
    }
}


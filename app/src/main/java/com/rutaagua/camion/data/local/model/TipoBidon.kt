package com.rutaagua.camion.data.local.model

/**
 * Clasificación de los bidones de 20L según material y estado:
 * - TRANSPARENTE: Bidón PET, exigido por clientes específicos.
 * - AZUL: Envase estándar de otro material/color.
 * - MARCADO: Bidones en mal estado o con sellos de otro color/marca.
 */
enum class TipoBidon {
    TRANSPARENTE,
    AZUL,
    MARCADO
}

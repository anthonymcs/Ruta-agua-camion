package com.rutaagua.camion.data.local.converter

import androidx.room.TypeConverter
import com.rutaagua.camion.data.local.model.FormaPago
import com.rutaagua.camion.data.local.model.TipoBidon
import com.rutaagua.camion.data.local.model.TipoCliente

/**
 * Conversores de tipos para persistir Enums en Room de forma robusta.
 */
class Converters {

    @TypeConverter
    fun fromTipoBidon(tipo: TipoBidon?): String? = tipo?.name

    @TypeConverter
    fun toTipoBidon(value: String?): TipoBidon? =
        value?.let { runCatching { TipoBidon.valueOf(it) }.getOrDefault(TipoBidon.AZUL) }

    @TypeConverter
    fun fromTipoCliente(tipo: TipoCliente?): String? = tipo?.name

    @TypeConverter
    fun toTipoCliente(value: String?): TipoCliente? =
        value?.let { runCatching { TipoCliente.valueOf(it) }.getOrDefault(TipoCliente.RUTA) }

    @TypeConverter
    fun fromFormaPago(forma: FormaPago?): String? = forma?.name

    @TypeConverter
    fun toFormaPago(value: String?): FormaPago? =
        value?.let { runCatching { FormaPago.valueOf(it) }.getOrDefault(FormaPago.EFECTIVO) }
}

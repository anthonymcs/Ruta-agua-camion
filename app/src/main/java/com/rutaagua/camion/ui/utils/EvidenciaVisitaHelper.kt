package com.rutaagua.camion.ui.utils

import android.content.Context
import android.net.Uri
import androidx.core.content.FileProvider
import java.io.File

object EvidenciaVisitaHelper {

    fun crearArchivoFotoEvidencia(context: Context, clienteId: Long): File {
        val dir = File(context.filesDir, "fotos_evidencia").apply {
            if (!exists()) mkdirs()
        }
        return File(dir, "evidencia_${clienteId}_${System.currentTimeMillis()}.jpg")
    }

    fun obtenerUriParaArchivo(context: Context, file: File): Uri {
        return FileProvider.getUriForFile(
            context,
            "${context.packageName}.fileprovider",
            file
        )
    }
}

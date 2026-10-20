package com.rutaagua.camion

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.rutaagua.camion.data.local.RutaAguaDatabase
import com.rutaagua.camion.data.repository.RutaAguaRepository
import com.rutaagua.camion.ui.navigation.RutaAguaNavHost
import com.rutaagua.camion.ui.theme.RutaAguaTheme

/**
 * Actividad Principal de "Ruta Agua Camión" - 100% Offline.
 */
class MainActivity : ComponentActivity() {

    private lateinit var database: RutaAguaDatabase
    private lateinit var repository: RutaAguaRepository

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        // Inicialización de persistencia local Room y repositorio central
        database = RutaAguaDatabase.getDatabase(applicationContext)
        repository = RutaAguaRepository(database)

        setContent {
            RutaAguaTheme {
                RutaAguaNavHost(repository = repository)
            }
        }
    }
}

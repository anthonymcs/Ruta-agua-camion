package com.rutaagua.camion

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.rutaagua.camion.data.local.RutaAguaDatabase
import com.rutaagua.camion.data.remote.FirestoreSyncManager
import com.rutaagua.camion.data.repository.RutaAguaRepository
import com.rutaagua.camion.ui.navigation.RutaAguaNavHost
import com.rutaagua.camion.ui.theme.RutaAguaTheme

/**
 * Actividad Principal de "Ruta Agua" - Multi-Vehículo, Sincronización en Tiempo Real y 100% Offline.
 */
class MainActivity : ComponentActivity() {

    private lateinit var database: RutaAguaDatabase
    private lateinit var repository: RutaAguaRepository
    private lateinit var syncManager: FirestoreSyncManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        // 1. Inicialización de persistencia local Room
        database = RutaAguaDatabase.getDatabase(applicationContext)

        // 2. Repositorio central
        repository = RutaAguaRepository(database)

        // 3. Sincronización remota Cloud Firestore en segundo plano (Offline-first, $0.00 costo)
        syncManager = FirestoreSyncManager(applicationContext, database)
        repository.syncManager = syncManager
        syncManager.iniciar()

        setContent {
            RutaAguaTheme {
                RutaAguaNavHost(
                    repository = repository,
                    database = database
                )
            }
        }
    }
}

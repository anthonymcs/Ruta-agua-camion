package com.rutaagua.camion.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import androidx.sqlite.db.SupportSQLiteDatabase
import com.rutaagua.camion.data.local.converter.Converters
import com.rutaagua.camion.data.local.dao.ClienteDao
import com.rutaagua.camion.data.local.dao.GastoDao
import com.rutaagua.camion.data.local.dao.InventarioDao
import com.rutaagua.camion.data.local.dao.RecargaPlantaDao
import com.rutaagua.camion.data.local.dao.TransaccionDao
import com.rutaagua.camion.data.local.entity.ClienteEntity
import com.rutaagua.camion.data.local.entity.GastoExtraEntity
import com.rutaagua.camion.data.local.entity.InventarioCamionEntity
import com.rutaagua.camion.data.local.entity.RecargaPlantaEntity
import com.rutaagua.camion.data.local.entity.TransaccionVentaEntity
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@Database(
    entities = [
        ClienteEntity::class,
        InventarioCamionEntity::class,
        RecargaPlantaEntity::class,
        TransaccionVentaEntity::class,
        GastoExtraEntity::class
    ],
    version = 2,
    exportSchema = false
)
@TypeConverters(Converters::class)
abstract class RutaAguaDatabase : RoomDatabase() {

    abstract fun clienteDao(): ClienteDao
    abstract fun inventarioDao(): InventarioDao
    abstract fun recargaPlantaDao(): RecargaPlantaDao
    abstract fun transaccionDao(): TransaccionDao
    abstract fun gastoDao(): GastoDao

    companion object {
        @Volatile
        private var INSTANCE: RutaAguaDatabase? = null

        fun getDatabase(context: Context, scope: CoroutineScope = CoroutineScope(Dispatchers.IO)): RutaAguaDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    RutaAguaDatabase::class.java,
                    "ruta_agua_camion.db"
                )
                    .addCallback(RutaAguaDatabaseCallback(scope))
                    .fallbackToDestructiveMigration(dropAllTables = true)
                    .build()
                INSTANCE = instance
                instance
            }
        }

        private class RutaAguaDatabaseCallback(
            private val scope: CoroutineScope
        ) : RoomDatabase.Callback() {
            override fun onCreate(db: SupportSQLiteDatabase) {
                super.onCreate(db)
                // Inicializa el inventario del camión con los valores tope predeterminados
                INSTANCE?.let { database ->
                    scope.launch {
                        val inventarioInicial = InventarioCamionEntity(
                            id = 1,
                            capacidadTopeBidones = 218,
                            capacidadTopeGalones = 90,
                            capacidadTopePacas = 6,
                            capacidadTopeMinis = 2,
                            bidonesLlenosTransparentes = 0,
                            bidonesLlenosAzules = 0,
                            bidonesLlenosMarcados = 0,
                            bidonesVacios = 0,
                            galonesLlenos = 0,
                            pacasLlenas = 0,
                            minisLlenas = 0
                        )
                        database.inventarioDao().insertarOActualizar(inventarioInicial)
                    }
                }
            }
        }
    }
}

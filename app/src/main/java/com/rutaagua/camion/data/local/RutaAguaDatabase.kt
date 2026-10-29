package com.rutaagua.camion.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import com.rutaagua.camion.data.local.converter.Converters
import com.rutaagua.camion.data.local.dao.ClienteDao
import com.rutaagua.camion.data.local.dao.CierreCajaDao
import com.rutaagua.camion.data.local.dao.GastoDao
import com.rutaagua.camion.data.local.dao.InventarioDao
import com.rutaagua.camion.data.local.dao.RecargaPlantaDao
import com.rutaagua.camion.data.local.dao.TransaccionDao
import com.rutaagua.camion.data.local.dao.VisitaFallidaDao
import com.rutaagua.camion.data.local.entity.ClienteEntity
import com.rutaagua.camion.data.local.entity.CierreCajaEntity
import com.rutaagua.camion.data.local.entity.GastoExtraEntity
import com.rutaagua.camion.data.local.entity.InventarioCamionEntity
import com.rutaagua.camion.data.local.entity.RecargaPlantaEntity
import com.rutaagua.camion.data.local.entity.TransaccionVentaEntity
import com.rutaagua.camion.data.local.entity.VisitaFallidaEntity
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@Database(
    entities = [
        ClienteEntity::class,
        InventarioCamionEntity::class,
        RecargaPlantaEntity::class,
        TransaccionVentaEntity::class,
        GastoExtraEntity::class,
        CierreCajaEntity::class,
        VisitaFallidaEntity::class
    ],
    version = 8,
    exportSchema = false
)
@TypeConverters(Converters::class)
abstract class RutaAguaDatabase : RoomDatabase() {

    abstract fun clienteDao(): ClienteDao
    abstract fun inventarioDao(): InventarioDao
    abstract fun recargaPlantaDao(): RecargaPlantaDao
    abstract fun transaccionDao(): TransaccionDao
    abstract fun gastoDao(): GastoDao
    abstract fun cierreCajaDao(): CierreCajaDao
    abstract fun visitaFallidaDao(): VisitaFallidaDao

    companion object {
        @Volatile
        private var INSTANCE: RutaAguaDatabase? = null

        private val MIGRATION_6_7 = object : Migration(6, 7) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("ALTER TABLE transacciones_ventas ADD COLUMN prestadosTransparentes INTEGER NOT NULL DEFAULT 0")
                db.execSQL("ALTER TABLE transacciones_ventas ADD COLUMN prestadosAzules INTEGER NOT NULL DEFAULT 0")
                db.execSQL("ALTER TABLE transacciones_ventas ADD COLUMN prestadosMarcados INTEGER NOT NULL DEFAULT 0")
                db.execSQL("ALTER TABLE transacciones_ventas ADD COLUMN cantidadBidonesGratis INTEGER NOT NULL DEFAULT 0")
            }
        }

        private val MIGRATION_7_8 = object : Migration(7, 8) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("ALTER TABLE clientes ADD COLUMN alias TEXT NOT NULL DEFAULT ''")
            }
        }

        fun getDatabase(context: Context, scope: CoroutineScope = CoroutineScope(Dispatchers.IO)): RutaAguaDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    RutaAguaDatabase::class.java,
                    "ruta_agua_camion.db"
                )
                    .addMigrations(MIGRATION_6_7, MIGRATION_7_8)
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
                // Inicializa los inventarios independientes para Camión y Camioneta
                INSTANCE?.let { database ->
                    scope.launch {
                        val inventarioCamion = InventarioCamionEntity(
                            id = 1,
                            vehiculo = "CAMION",
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
                        val inventarioCamioneta = InventarioCamionEntity(
                            id = 2,
                            vehiculo = "CAMIONETA",
                            capacidadTopeBidones = 80,
                            capacidadTopeGalones = 30,
                            capacidadTopePacas = 2,
                            capacidadTopeMinis = 1,
                            bidonesLlenosTransparentes = 0,
                            bidonesLlenosAzules = 0,
                            bidonesLlenosMarcados = 0,
                            bidonesVacios = 0,
                            galonesLlenos = 0,
                            pacasLlenas = 0,
                            minisLlenas = 0
                        )
                        database.inventarioDao().insertarOActualizar(inventarioCamion)
                        database.inventarioDao().insertarOActualizar(inventarioCamioneta)
                    }
                }
            }
        }
    }
}

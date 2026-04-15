package id.rancak.app.di

import androidx.room.Room
import androidx.sqlite.driver.bundled.BundledSQLiteDriver
import id.rancak.app.data.local.db.AppDatabase
import id.rancak.app.data.local.db.AppDatabaseConstructor
import id.rancak.app.data.printing.PrinterManager
import id.rancak.app.data.sync.SyncManager
import kotlinx.coroutines.Dispatchers
import org.koin.core.module.Module
import org.koin.dsl.module
import platform.Foundation.NSDocumentDirectory
import platform.Foundation.NSFileManager
import platform.Foundation.NSUserDomainMask

actual val platformModule: Module = module {
    single { SyncManager() }
    single { PrinterManager() }  // TCP/IP + BLE via CoreBluetooth
    single<AppDatabase> {
        val docDir = NSFileManager.defaultManager
            .URLsForDirectory(NSDocumentDirectory, NSUserDomainMask)
            .first() as platform.Foundation.NSURL
        val dbPath = "${docDir.path}/rancak.db"
        Room.databaseBuilder<AppDatabase>(
            name = dbPath,
            factory = AppDatabaseConstructor::initialize
        ).setDriver(BundledSQLiteDriver())
         .setQueryCoroutineContext(Dispatchers.IO)
         .fallbackToDestructiveMigration(true)
         .build()
    }
}

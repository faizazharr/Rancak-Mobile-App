package id.rancak.app.di

import androidx.room.Room
import id.rancak.app.data.local.db.AppDatabase
import id.rancak.app.data.printing.PrinterManager
import id.rancak.app.data.sync.SyncManager
import org.koin.android.ext.koin.androidContext
import org.koin.core.module.Module
import org.koin.dsl.module

actual val platformModule: Module = module {
    single { SyncManager(androidContext()) }
    single { PrinterManager().apply { init(androidContext()) } }
    single<AppDatabase> {
        Room.databaseBuilder<AppDatabase>(
            context = androidContext(),
            name = androidContext().getDatabasePath("rancak.db").absolutePath
        ).fallbackToDestructiveMigration(true)
         .build()
    }
}

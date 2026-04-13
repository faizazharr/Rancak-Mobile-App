package id.rancak.app.di

import id.rancak.app.data.printing.PrinterManager
import id.rancak.app.data.sync.SyncManager
import org.koin.android.ext.koin.androidContext
import org.koin.core.module.Module
import org.koin.dsl.module

actual val platformModule: Module = module {
    single { SyncManager(androidContext()) }
    single { PrinterManager() }
}

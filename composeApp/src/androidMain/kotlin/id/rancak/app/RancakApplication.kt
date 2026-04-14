package id.rancak.app

import android.app.Application
import androidx.work.WorkManager
import id.rancak.app.di.DEMO_MODE
import id.rancak.app.di.appModules
import org.koin.android.ext.koin.androidContext
import org.koin.android.ext.koin.androidLogger
import org.koin.core.context.startKoin
import org.koin.core.logger.Level

/**
 * Android Application class.
 *
 * Initialises Koin at the application level so that WorkManager workers
 * (e.g. SyncWorker) can access the DI container even when the app is
 * running in the background without the Compose UI being active.
 *
 * The Compose [App] composable uses [org.koin.compose.KoinContext] to
 * consume this already-running Koin instance.
 */
class RancakApplication : Application() {

    override fun onCreate() {
        super.onCreate()

        startKoin {
            androidLogger(Level.ERROR)
            androidContext(this@RancakApplication)
            modules(appModules)
        }

        // Batalkan semua background sync yang tersisa saat mode demo aktif.
        // Ini penting agar SyncWorker dari sesi sebelumnya tidak coba
        // inject RancakApiService yang tidak ada di DI graph demo.
        if (DEMO_MODE) {
            WorkManager.getInstance(this).cancelAllWork()
        }
    }
}

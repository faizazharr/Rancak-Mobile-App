package id.rancak.app

import android.app.Application
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
            androidLogger(Level.ERROR)  // Reduce noise in production
            androidContext(this@RancakApplication)
            modules(appModules)
        }
    }
}

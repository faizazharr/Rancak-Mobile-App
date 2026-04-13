package id.rancak.app

import androidx.compose.ui.window.ComposeUIViewController
import id.rancak.app.data.sync.SyncManager
import id.rancak.app.data.sync.runIosSync
import id.rancak.app.di.appModules
import kotlinx.coroutines.*
import org.koin.core.context.GlobalContext
import org.koin.core.context.startKoin
import platform.BackgroundTasks.BGProcessingTask
import platform.BackgroundTasks.BGProcessingTaskRequest
import platform.BackgroundTasks.BGTaskScheduler
import platform.UIKit.UIViewController

/**
 * Entry point iOS yang dipanggil dari Swift ContentView.
 *
 * Melakukan dua inisialisasi penting:
 * 1. Koin global — supaya SyncWorker dan BGTask handler bisa akses DI
 *    tanpa harus ada Compose tree aktif.
 * 2. BGTask handler — harus didaftarkan sebelum app selesai launching
 *    (sebelum application(_:didFinishLaunchingWithOptions:) return).
 *
 * Kedua langkah ini dijaga flag [initialized] supaya tidak dobel.
 */

private var initialized = false

fun MainViewController(): UIViewController {
    if (!initialized) {
        initialized = true

        // Inisialisasi Koin global (bukan di Compose tree)
        // KoinAppWrapper.ios.kt menggunakan KoinContext untuk mengkonsumsi instance ini
        startKoin { modules(appModules) }

        // Daftarkan BGTask handler
        // WAJIB dipanggil sebelum app selesai launching — di sinilah tempatnya
        registerBgTaskHandler()
    }

    return ComposeUIViewController { App() }
}

/**
 * Mendaftarkan handler untuk BGProcessingTask sync.
 *
 * Dipanggil sekali saat app pertama kali launch. iOS menjalankan handler ini
 * ketika BGTaskScheduler memutuskan saatnya menjalankan task (biasanya saat
 * device charging + connected + app ada di background).
 *
 * Task identifier "id.rancak.app.sync" WAJIB ada di Info.plist:
 *   <key>BGTaskSchedulerPermittedIdentifiers</key>
 *   <array>
 *     <string>id.rancak.app.sync</string>
 *   </array>
 */
internal fun registerBgTaskHandler() {
    BGTaskScheduler.shared.registerForTaskWithIdentifier(
        identifier  = SyncManager.TASK_ID,
        usingQueue  = null  // nil = main queue
    ) { task ->
        val bgTask = task as? BGProcessingTask ?: run {
            task?.setTaskCompletedWithSuccess(false)
            return@registerForTaskWithIdentifier
        }

        val scope = CoroutineScope(Dispatchers.Default + SupervisorJob())

        // Ekspirasi — iOS minta kita berhenti secepatnya
        bgTask.expirationHandler = {
            scope.cancel()
            bgTask.setTaskCompletedWithSuccess(false)
        }

        scope.launch {
            // Re-schedule DULU sebelum menjalankan (iOS best practice)
            val nextRequest = BGProcessingTaskRequest(identifier = SyncManager.TASK_ID)
            nextRequest.requiresNetworkConnectivity = true
            nextRequest.requiresExternalPower = false
            BGTaskScheduler.shared.submitTaskRequest(nextRequest, error = null)

            val success = runIosSync()
            bgTask.setTaskCompletedWithSuccess(success)
        }
    }
}

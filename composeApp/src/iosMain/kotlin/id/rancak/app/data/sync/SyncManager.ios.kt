package id.rancak.app.data.sync

import kotlinx.coroutines.*
import platform.BackgroundTasks.BGProcessingTaskRequest
import platform.BackgroundTasks.BGTaskScheduler
import platform.Foundation.NSNotificationCenter
import platform.Foundation.NSObjectProtocol
import platform.Foundation.NSOperationQueue
import platform.UIKit.UIApplicationDidBecomeActiveNotification

/**
 * iOS implementation of SyncManager.
 *
 * Strategi sync berlapis:
 *
 * 1. **Immediate** — saat scheduleSync() dipanggil (setelah sale di-queue),
 *    langsung jalankan runIosSync() via coroutine.
 *
 * 2. **Foreground** — daftarkan observer UIApplicationDidBecomeActiveNotification
 *    supaya setiap kali app dibuka kembali, sync otomatis dicoba.
 *
 * 3. **Background** — submit BGProcessingTask (identifier: "id.rancak.app.sync")
 *    sebagai fallback. iOS akan menjalankannya saat device charging + connected.
 *    Handler-nya didaftarkan di MainViewController.registerBgTaskHandler().
 *
 * Catatan: BGProcessingTask WAJIB terdaftar di Info.plist:
 *   <key>BGTaskSchedulerPermittedIdentifiers</key>
 *   <array>
 *     <string>id.rancak.app.sync</string>
 *   </array>
 */
actual class SyncManager {

    private val scope = CoroutineScope(Dispatchers.Default + SupervisorJob())
    private var activeSyncJob: Job? = null
    private var foregroundObserver: NSObjectProtocol? = null

    actual fun scheduleSync() {
        // 1. Langsung coba sync sekarang
        triggerSync()

        // 2. Sync otomatis setiap kali app menjadi aktif (dari background)
        registerForegroundObserver()

        // 3. Schedule background task sebagai fallback
        submitBgTask()
    }

    actual fun cancelSync() {
        activeSyncJob?.cancel()
        activeSyncJob = null
        unregisterForegroundObserver()
        BGTaskScheduler.shared.cancelTaskRequestWithIdentifier(TASK_ID)
    }

    // ── Private helpers ───────────────────────────────────────────────────────

    private fun triggerSync() {
        // Batalkan sync sebelumnya jika masih berjalan, lalu mulai yang baru
        activeSyncJob?.cancel()
        activeSyncJob = scope.launch {
            runIosSync()
        }
    }

    private fun registerForegroundObserver() {
        if (foregroundObserver != null) return  // Sudah terdaftar

        foregroundObserver = NSNotificationCenter.defaultCenter.addObserverForName(
            name   = UIApplicationDidBecomeActiveNotification,
            `object` = null,
            queue  = NSOperationQueue.mainQueue
        ) { _ ->
            triggerSync()
        }
    }

    private fun unregisterForegroundObserver() {
        foregroundObserver?.let { observer ->
            NSNotificationCenter.defaultCenter.removeObserver(observer)
            foregroundObserver = null
        }
    }

    private fun submitBgTask() {
        val request = BGProcessingTaskRequest(identifier = TASK_ID)
        request.requiresNetworkConnectivity = true
        request.requiresExternalPower = false
        // Abaikan error — gagal submit = tidak ada background task, tapi
        // foreground sync (langkah 1 & 2) sudah meng-cover kasus ini.
        BGTaskScheduler.shared.submitTaskRequest(request, error = null)
    }

    companion object {
        const val TASK_ID = "id.rancak.app.sync"
    }
}

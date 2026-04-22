package id.rancak.app.data.sync

/**
 * Platform-specific sync scheduler.
 *
 * Android: uses WorkManager to run [SyncWorker] in the background
 *          with a CONNECTED network constraint.
 * iOS:     triggers a coroutine-based sync on the next app foreground.
 */
expect class SyncManager : SyncScheduler {
    /** Enqueue a one-time background sync task (deduplicated by work name). */
    override fun scheduleSync()

    /** Cancel any pending sync task. */
    fun cancelSync()
}

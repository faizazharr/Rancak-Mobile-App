package id.rancak.app.data.sync

/**
 * Minimal interface extracted from [SyncManager] so that classes depending on
 * sync scheduling (e.g. [id.rancak.app.data.repository.SaleRepositoryImpl]) can
 * be unit-tested without needing a platform context (Android WorkManager / iOS BGTaskScheduler).
 */
interface SyncScheduler {
    fun scheduleSync()
}

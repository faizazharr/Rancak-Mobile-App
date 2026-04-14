package id.rancak.app.data.sync

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import id.rancak.app.data.local.OfflineSaleQueue
import id.rancak.app.data.local.TokenManager
import id.rancak.app.data.remote.api.RancakApiService
import id.rancak.app.data.local.toBatchItem
import id.rancak.app.data.remote.dto.sale.BatchSalesRequest
import id.rancak.app.di.DEMO_MODE
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject

/**
 * WorkManager worker that syncs all pending offline sales to the backend.
 *
 * Runs in the background when:
 * - Network becomes available (CONNECTED constraint)
 * - Triggered by [SyncManager.scheduleSync] after an offline sale is queued
 *
 * Uses the `POST /sales/batch` endpoint. Each sale has a unique idempotency key
 * so the server safely rejects duplicate submissions.
 *
 * The worker retries with exponential backoff on failure (configured in SyncManager).
 * It uses [KoinComponent] to access DI bindings from the global Koin context
 * started in [id.rancak.app.RancakApplication].
 */
class SyncWorker(
    context: Context,
    params: WorkerParameters
) : CoroutineWorker(context, params), KoinComponent {

    private val queue: OfflineSaleQueue by inject()
    private val api: RancakApiService by inject()
    private val tokenManager: TokenManager by inject()

    override suspend fun doWork(): Result {
        // Jangan sync saat mode demo — tidak ada API service di DI graph
        if (DEMO_MODE) return Result.success()

        // Safety check: ensure we have a tenant before attempting sync
        val tenantUuid = tokenManager.tenantUuid
            ?: return Result.failure()  // Can't sync without tenant context

        val pending = queue.getAll()
        if (pending.isEmpty()) return Result.success()

        return try {
            val batchItems = pending.map { it.toBatchItem() }
            val response = api.batchSales(tenantUuid, BatchSalesRequest(sales = batchItems))

            if (response.status == "ok" && response.data != null) {
                val data = response.data
                // Remove successfully processed items (created OR duplicate = safe to remove)
                data.results.forEach { result ->
                    if (result.status == "created" || result.status == "duplicate") {
                        queue.remove(result.idempotencyKey)
                    }
                }
                // If any items errored, retry worker
                if (data.errors > 0) Result.retry() else Result.success()
            } else {
                Result.retry()
            }
        } catch (e: Exception) {
            // Network/server error — WorkManager will retry with exponential backoff
            Result.retry()
        }
    }

}

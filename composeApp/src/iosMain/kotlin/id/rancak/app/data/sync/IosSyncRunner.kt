package id.rancak.app.data.sync

import id.rancak.app.data.local.OfflineSaleQueue
import id.rancak.app.data.local.TokenManager
import id.rancak.app.data.local.toBatchItem
import id.rancak.app.data.remote.api.RancakApiService
import id.rancak.app.data.remote.dto.sale.BatchSalesRequest
import org.koin.core.context.GlobalContext

/**
 * Menjalankan satu siklus sync offline sales ke backend.
 *
 * Dipakai oleh:
 * - [SyncManager] saat app di foreground (langsung setelah sale di-queue)
 * - BGTask handler di [id.rancak.app.MainViewController] saat background
 *
 * Mengambil dependensi dari Koin global (diinit di MainViewController).
 * Mengembalikan `true` jika semua pending sales berhasil di-sync, `false` jika ada
 * yang gagal atau terjadi error (WorkManager akan retry jika false).
 */
internal suspend fun runIosSync(): Boolean {
    val koin = GlobalContext.getOrNull() ?: return false  // Koin belum init

    val queue        = koin.get<OfflineSaleQueue>()
    val api          = koin.get<RancakApiService>()
    val tokenManager = koin.get<TokenManager>()

    val tenantUuid = tokenManager.tenantUuid ?: return false  // Belum login

    val pending = queue.getAll()
    if (pending.isEmpty()) return true  // Tidak ada yang perlu di-sync

    return try {
        val response = api.batchSales(
            tenantUuid = tenantUuid,
            request    = BatchSalesRequest(sales = pending.map { it.toBatchItem() })
        )

        if (response.status == "ok" && response.data != null) {
            val data = response.data
            // Hapus item yang berhasil atau sudah ada (idempoten)
            data.results.forEach { result ->
                if (result.status == "created" || result.status == "duplicate") {
                    queue.remove(result.idempotencyKey)
                }
            }
            data.errors == 0  // true jika semua item sukses
        } else {
            false
        }
    } catch (_: Exception) {
        false  // Network error — akan dicoba lagi saat foreground berikutnya
    }
}

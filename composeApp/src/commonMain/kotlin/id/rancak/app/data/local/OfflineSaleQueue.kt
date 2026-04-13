package id.rancak.app.data.local

import com.russhwolf.settings.Settings
import com.russhwolf.settings.set
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

/**
 * Persistent FIFO queue for sales created while the device is offline.
 *
 * Backed by [Settings] (SharedPreferences on Android / NSUserDefaults on iOS).
 * All mutations are serialised to JSON and written atomically to a single key.
 *
 * Thread-safety note: operations are not concurrent-safe by themselves;
 * callers must run them on a single coroutine dispatcher (e.g. IO).
 */
class OfflineSaleQueue(private val settings: Settings) {

    private val json = Json {
        ignoreUnknownKeys = true
        encodeDefaults = true
    }

    /** Add a sale to the back of the queue. */
    fun enqueue(sale: PendingSale) {
        val updated = getAll().toMutableList().also { it.add(sale) }
        persist(updated)
    }

    /** Return all queued sales, oldest first. */
    fun getAll(): List<PendingSale> {
        val raw = settings.getStringOrNull(KEY_QUEUE) ?: return emptyList()
        return try {
            json.decodeFromString(raw)
        } catch (_: Exception) {
            emptyList()
        }
    }

    /** Remove the sale with [idempotencyKey] from the queue (after successful sync). */
    fun remove(idempotencyKey: String) {
        persist(getAll().filter { it.idempotencyKey != idempotencyKey })
    }

    /** Remove all entries (e.g. after a full batch sync). */
    fun clear() {
        settings.remove(KEY_QUEUE)
    }

    val isEmpty: Boolean get() = getAll().isEmpty()
    val size: Int        get() = getAll().size

    // ── private ──

    private fun persist(sales: List<PendingSale>) {
        settings[KEY_QUEUE] = json.encodeToString(sales)
    }

    companion object {
        private const val KEY_QUEUE = "rancak_offline_sale_queue"
    }
}

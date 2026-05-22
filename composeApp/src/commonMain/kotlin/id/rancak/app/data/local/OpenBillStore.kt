package id.rancak.app.data.local

import com.russhwolf.settings.Settings
import com.russhwolf.settings.set
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

/**
 * Penyimpanan lokal untuk daftar open bill kasir.
 *
 * Menggunakan [Settings] (SharedPreferences / NSUserDefaults) — tidak ada koneksi
 * internet yang dibutuhkan. Setiap perubahan langsung diserialisasi ke JSON.
 */
class OpenBillStore(private val json: Json) {

    private val settings = Settings()

    private var cachedBills: List<LocalOpenBill>? = null

    /** Simpan atau perbarui open bill (berdasarkan [LocalOpenBill.id]). */
    fun save(bill: LocalOpenBill) {
        val all = getAll().toMutableList()
        val idx = all.indexOfFirst { it.id == bill.id }
        if (idx >= 0) all[idx] = bill else all.add(bill)
        persist(all)
    }

    /** Kembalikan semua open bill yang tersimpan, dari yang terlama ke terbaru. */
    fun getAll(): List<LocalOpenBill> {
        cachedBills?.let { return it }
        val raw = settings.getStringOrNull(KEY) ?: return emptyList()
        return try {
            val decoded = json.decodeFromString<List<LocalOpenBill>>(raw)
            cachedBills = decoded
            decoded
        } catch (_: Exception) {
            emptyList()
        }
    }

    /** Cari satu open bill berdasarkan ID. */
    fun get(id: String): LocalOpenBill? = getAll().find { it.id == id }

    /** Hapus open bill dengan [id] yang diberikan. */
    fun remove(id: String) {
        persist(getAll().filter { it.id != id })
    }

    /** Hapus semua open bill. */
    fun clear() {
        settings.remove(KEY)
        cachedBills = emptyList()
    }

    val count: Int get() = getAll().size

    // ── private ──────────────────────────────────────────────────────────────

    private fun persist(bills: List<LocalOpenBill>) {
        cachedBills = bills
        settings[KEY] = json.encodeToString(bills)
    }

    companion object {
        private const val KEY = "rancak_local_open_bills"
    }
}

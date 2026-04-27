package id.rancak.app.domain.repository

import id.rancak.app.domain.model.AppConfig
import id.rancak.app.domain.model.Printer
import id.rancak.app.domain.model.Resource

/**
 * Manajemen konfigurasi perangkat — printer (per device JWT-scoped, atau
 * global) dan key-value app config tenant.
 */
interface DeviceConfigRepository {

    // ── Printers ─────────────────────────────────────────────────────────────
    /**
     * Daftar printer untuk device aktif (JWT-scoped) — server filter otomatis
     * berdasarkan device_id di JWT.
     */
    suspend fun getPrinters(): Resource<List<Printer>>
    /** Daftar semua printer di tenant — biasanya untuk admin. */
    suspend fun getAllPrinters(): Resource<List<Printer>>
    suspend fun getPrinter(printerId: String): Resource<Printer>
    suspend fun createPrinter(
        printerName: String,
        printerType: String,
        connectionType: String,
        address: String,
        paperWidthMm: Int = 58,
        isDefault: Boolean = false
    ): Resource<Printer>
    suspend fun updatePrinter(
        printerId: String,
        printerName: String? = null,
        connectionType: String? = null,
        address: String? = null,
        paperWidthMm: Int? = null,
        isDefault: Boolean? = null
    ): Resource<Printer>
    suspend fun deletePrinter(printerId: String): Resource<Unit>

    // ── App config (key-value) ───────────────────────────────────────────────
    /** Daftar semua key-value config tenant. */
    suspend fun getAppConfig(): Resource<List<AppConfig>>
    suspend fun upsertAppConfig(key: String, value: String): Resource<AppConfig>
    suspend fun deleteAppConfig(key: String): Resource<Unit>
}

package id.rancak.app.data.repository

import id.rancak.app.data.local.TokenManager
import id.rancak.app.data.mapper.toDomain
import id.rancak.app.data.remote.api.RancakApiService
import id.rancak.app.data.remote.api.createPrinter
import id.rancak.app.data.remote.api.deleteAppConfig
import id.rancak.app.data.remote.api.deletePrinter
import id.rancak.app.data.remote.api.getAllPrinters
import id.rancak.app.data.remote.api.getAppConfig
import id.rancak.app.data.remote.api.getPrinter
import id.rancak.app.data.remote.api.getPrinters
import id.rancak.app.data.remote.api.updatePrinter
import id.rancak.app.data.remote.api.upsertAppConfig
import id.rancak.app.data.remote.dto.deviceconfig.CreatePrinterConfigRequest
import id.rancak.app.data.remote.dto.deviceconfig.UpdatePrinterConfigRequest
import id.rancak.app.domain.model.AppConfig
import id.rancak.app.domain.model.Printer
import id.rancak.app.domain.model.Resource
import id.rancak.app.domain.repository.DeviceConfigRepository

class DeviceConfigRepositoryImpl(
    private val api: RancakApiService,
    private val tokenManager: TokenManager
) : DeviceConfigRepository {

    private val tenantUuid: String
        get() = tokenManager.tenantUuid ?: throw IllegalStateException("Tenant belum dipilih")

    override suspend fun getPrinters(): Resource<List<Printer>> = safe(
        block = { api.getPrinters(tenantUuid) },
        map = { list -> list.map { it.toDomain() } },
        errorMsg = "Gagal memuat printer"
    )

    override suspend fun getAllPrinters(): Resource<List<Printer>> = safe(
        block = { api.getAllPrinters(tenantUuid) },
        map = { list -> list.map { it.toDomain() } },
        errorMsg = "Gagal memuat printer"
    )

    override suspend fun getPrinter(printerId: String): Resource<Printer> = safe(
        block = { api.getPrinter(tenantUuid, printerId) },
        map = { it.toDomain() },
        errorMsg = "Printer tidak ditemukan"
    )

    override suspend fun createPrinter(
        printerName: String,
        printerType: String,
        connectionType: String,
        address: String,
        paperWidthMm: Int,
        isDefault: Boolean
    ): Resource<Printer> = safe(
        block = {
            api.createPrinter(
                tenantUuid,
                CreatePrinterConfigRequest(
                    printerName = printerName,
                    printerType = printerType,
                    connectionType = connectionType,
                    address = address,
                    paperWidthMm = paperWidthMm,
                    isDefault = isDefault
                )
            )
        },
        map = { it.toDomain() },
        errorMsg = "Gagal menambah printer"
    )

    override suspend fun updatePrinter(
        printerId: String,
        printerName: String?,
        connectionType: String?,
        address: String?,
        paperWidthMm: Int?,
        isDefault: Boolean?
    ): Resource<Printer> = safe(
        block = {
            api.updatePrinter(
                tenantUuid,
                printerId,
                UpdatePrinterConfigRequest(
                    printerName = printerName,
                    connectionType = connectionType,
                    address = address,
                    paperWidthMm = paperWidthMm,
                    isDefault = isDefault
                )
            )
        },
        map = { it.toDomain() },
        errorMsg = "Gagal memperbarui printer"
    )

    override suspend fun deletePrinter(printerId: String): Resource<Unit> = safeUnit(
        block = { api.deletePrinter(tenantUuid, printerId) },
        errorMsg = "Gagal menghapus printer"
    )

    override suspend fun getAppConfig(): Resource<List<AppConfig>> = safe(
        block = { api.getAppConfig(tenantUuid) },
        map = { list -> list.map { it.toDomain() } },
        errorMsg = "Gagal memuat config"
    )

    override suspend fun upsertAppConfig(key: String, value: String): Resource<AppConfig> = safe(
        block = { api.upsertAppConfig(tenantUuid, key, value) },
        map = { it.toDomain() },
        errorMsg = "Gagal menyimpan config"
    )

    override suspend fun deleteAppConfig(key: String): Resource<Unit> = safeUnit(
        block = { api.deleteAppConfig(tenantUuid, key) },
        errorMsg = "Gagal menghapus config"
    )
}

// ── Helpers ─────────────────────────────────────────────────────────────────

private suspend fun <T, R> safe(
    block: suspend () -> id.rancak.app.data.remote.dto.ApiResponse<T>,
    map: (T) -> R,
    errorMsg: String
): Resource<R> = try {
    val response = block()
    if (response.isSuccess && response.data != null) {
        Resource.Success(map(response.data))
    } else {
        Resource.Error(response.message ?: errorMsg)
    }
} catch (e: Exception) {
    Resource.Error(e.message ?: "Kesalahan jaringan")
}

private suspend fun safeUnit(
    block: suspend () -> id.rancak.app.data.remote.dto.ApiResponse<Unit>,
    errorMsg: String
): Resource<Unit> = try {
    val response = block()
    if (response.isSuccess) Resource.Success(Unit)
    else Resource.Error(response.message ?: errorMsg)
} catch (e: Exception) {
    Resource.Error(e.message ?: "Kesalahan jaringan")
}

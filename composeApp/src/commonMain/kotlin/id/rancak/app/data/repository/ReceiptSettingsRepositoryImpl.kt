package id.rancak.app.data.repository

import id.rancak.app.data.local.TokenManager
import id.rancak.app.data.mapper.toDomain
import id.rancak.app.data.mapper.toUpdateDto
import id.rancak.app.data.remote.api.RancakApiService
import id.rancak.app.data.remote.api.getReceiptSettings
import id.rancak.app.data.remote.api.patchReceiptSettings
import id.rancak.app.data.util.safe
import id.rancak.app.domain.model.ReceiptSettingsConfig
import id.rancak.app.domain.model.Resource
import id.rancak.app.domain.repository.ReceiptSettingsRepository

class ReceiptSettingsRepositoryImpl(
    private val api: RancakApiService,
    private val tokenManager: TokenManager
) : ReceiptSettingsRepository {

    private val tenantUuid: String
        get() = tokenManager.tenantUuid ?: throw IllegalStateException("Tenant belum dipilih")

    override suspend fun getReceiptSettings(): Resource<ReceiptSettingsConfig> =
        safe(
            block    = { api.getReceiptSettings(tenantUuid) },
            map      = { it.toDomain() },
            errorMsg = "Gagal memuat pengaturan struk"
        )

    override suspend fun updateReceiptSettings(settings: ReceiptSettingsConfig): Resource<ReceiptSettingsConfig> =
        safe(
            block    = { api.patchReceiptSettings(tenantUuid, settings.toUpdateDto()) },
            map      = { it.toDomain() },
            errorMsg = "Gagal menyimpan pengaturan struk"
        )
}

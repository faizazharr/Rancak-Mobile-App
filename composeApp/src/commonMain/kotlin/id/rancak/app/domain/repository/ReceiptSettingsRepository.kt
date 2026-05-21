package id.rancak.app.domain.repository

import id.rancak.app.domain.model.ReceiptSettingsConfig
import id.rancak.app.domain.model.Resource

interface ReceiptSettingsRepository {
    /** Ambil konfigurasi struk dari server. */
    suspend fun getReceiptSettings(): Resource<ReceiptSettingsConfig>

    /** Update sebagian atau seluruh konfigurasi struk (PATCH semantics). */
    suspend fun updateReceiptSettings(settings: ReceiptSettingsConfig): Resource<ReceiptSettingsConfig>
}

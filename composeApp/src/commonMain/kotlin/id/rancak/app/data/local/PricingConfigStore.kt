package id.rancak.app.data.local

import id.rancak.app.domain.model.DiscountRule
import id.rancak.app.domain.model.Resource
import id.rancak.app.domain.model.Surcharge
import id.rancak.app.domain.model.TaxConfig
import id.rancak.app.domain.repository.AdminRepository
import id.rancak.app.domain.repository.DiscountRuleUpdate
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.persistentListOf
import kotlinx.collections.immutable.toImmutableList
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock

/**
 * Cache singleton untuk konfigurasi pricing (Pajak, Surcharge, Aturan Diskon).
 *
 * Bertanggung jawab untuk men-sinkronkan state antara halaman **Manajemen Pricing**
 * (Admin) dan **Kasir** (POS) secara realtime. Setiap perubahan (create / update /
 * delete / toggle isActive) yang dilakukan via store ini akan langsung memunculkan
 * perubahan pada semua observer (mis. [CartViewModel]) tanpa perlu reload manual.
 */
class PricingConfigStore(
    private val adminRepository: AdminRepository
) {
    private val _taxConfigs    = MutableStateFlow<ImmutableList<TaxConfig>>(persistentListOf())
    private val _surcharges    = MutableStateFlow<ImmutableList<Surcharge>>(persistentListOf())
    private val _discountRules = MutableStateFlow<ImmutableList<DiscountRule>>(persistentListOf())

    private val refreshMutex = Mutex()
    @Volatile private var loaded = false

    val taxConfigs:    StateFlow<ImmutableList<TaxConfig>>    = _taxConfigs.asStateFlow()
    val surcharges:    StateFlow<ImmutableList<Surcharge>>    = _surcharges.asStateFlow()
    val discountRules: StateFlow<ImmutableList<DiscountRule>> = _discountRules.asStateFlow()

    /** Pastikan store sudah di-load minimal sekali. Aman dipanggil berulang. */
    suspend fun ensureLoaded() {
        if (!loaded) refresh()
    }

    /** Muat ulang seluruh konfigurasi dari server (idempotent, ter-mutex). */
    suspend fun refresh() {
        refreshMutex.withLock {
            (adminRepository.getTaxConfigs()    as? Resource.Success)?.let { _taxConfigs.value    = it.data.toImmutableList() }
            (adminRepository.getSurcharges()    as? Resource.Success)?.let { _surcharges.value    = it.data.toImmutableList() }
            (adminRepository.getDiscountRules() as? Resource.Success)?.let { _discountRules.value = it.data.toImmutableList() }
            loaded = true
        }
    }

    /** Reset seluruh cache — dipanggil saat tenant berubah / logout. */
    fun clear() {
        _taxConfigs.value    = persistentListOf()
        _surcharges.value    = persistentListOf()
        _discountRules.value = persistentListOf()
        loaded = false
    }

    // ── Tax Config ───────────────────────────────────────────────────────────

    fun upsertTax(saved: TaxConfig) {
        _taxConfigs.value = if (_taxConfigs.value.any { it.uuid == saved.uuid })
            _taxConfigs.value.map { if (it.uuid == saved.uuid) saved else it }.toImmutableList()
        else (_taxConfigs.value + saved).toImmutableList()
    }

    fun removeTax(uuid: String) {
        _taxConfigs.value = _taxConfigs.value.filter { it.uuid != uuid }.toImmutableList()
    }

    suspend fun toggleTaxActive(tax: TaxConfig, isActive: Boolean): Resource<TaxConfig> {
        val result = adminRepository.updateTaxConfig(configId = tax.uuid, isActive = isActive)
        if (result is Resource.Success) upsertTax(result.data)
        return result
    }

    // ── Surcharge ────────────────────────────────────────────────────────────

    fun upsertSurcharge(saved: Surcharge) {
        _surcharges.value = if (_surcharges.value.any { it.uuid == saved.uuid })
            _surcharges.value.map { if (it.uuid == saved.uuid) saved else it }.toImmutableList()
        else (_surcharges.value + saved).toImmutableList()
    }

    fun removeSurcharge(uuid: String) {
        _surcharges.value = _surcharges.value.filter { it.uuid != uuid }.toImmutableList()
    }

    suspend fun toggleSurchargeActive(surcharge: Surcharge, isActive: Boolean): Resource<Surcharge> {
        val result = adminRepository.updateSurcharge(surchargeId = surcharge.uuid, isActive = isActive)
        if (result is Resource.Success) upsertSurcharge(result.data)
        return result
    }

    // ── Discount Rule ────────────────────────────────────────────────────────

    fun upsertDiscountRule(saved: DiscountRule) {
        _discountRules.value = if (_discountRules.value.any { it.uuid == saved.uuid })
            _discountRules.value.map { if (it.uuid == saved.uuid) saved else it }.toImmutableList()
        else (_discountRules.value + saved).toImmutableList()
    }

    fun removeDiscountRule(uuid: String) {
        _discountRules.value = _discountRules.value.filter { it.uuid != uuid }.toImmutableList()
    }

    suspend fun toggleDiscountRuleActive(rule: DiscountRule, isActive: Boolean): Resource<DiscountRule> {
        val result = adminRepository.updateDiscountRule(
            ruleId = rule.uuid,
            update = DiscountRuleUpdate(isActive = isActive)
        )
        if (result is Resource.Success) upsertDiscountRule(result.data)
        return result
    }
}

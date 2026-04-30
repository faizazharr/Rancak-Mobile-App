package id.rancak.app.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import id.rancak.app.data.local.PricingConfigStore
import id.rancak.app.domain.model.DiscountRule
import id.rancak.app.domain.model.Resource
import id.rancak.app.domain.model.Surcharge
import id.rancak.app.domain.model.TaxConfig
import id.rancak.app.domain.repository.AdminRepository
import id.rancak.app.domain.repository.DiscountRuleUpdate
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class PricingManagementUiState(
    val surcharges: List<Surcharge> = emptyList(),
    val taxConfigs: List<TaxConfig> = emptyList(),
    val discountRules: List<DiscountRule> = emptyList(),
    val isLoading: Boolean = false,
    val error: String? = null,
    val successMessage: String? = null,
    val isSubmitting: Boolean = false,
    // Surcharge dialog state
    val showSurchargeForm: Boolean = false,
    val editingSurcharge: Surcharge? = null,
    val showSurchargeDeleteConfirm: Boolean = false,
    // Tax dialog state
    val showTaxForm: Boolean = false,
    val editingTax: TaxConfig? = null,
    val showTaxDeleteConfirm: Boolean = false,
    // Discount rule dialog state
    val showDiscountForm: Boolean = false,
    val editingDiscount: DiscountRule? = null,
    val showDiscountDeleteConfirm: Boolean = false
)

class PricingManagementViewModel(
    private val adminRepository: AdminRepository,
    private val pricingStore: PricingConfigStore
) : ViewModel() {

    private val _uiState = MutableStateFlow(PricingManagementUiState())
    val uiState: StateFlow<PricingManagementUiState> = _uiState.asStateFlow()

    init {
        // Sinkronkan dari store agar perubahan dari kasir / panel lain langsung terlihat.
        viewModelScope.launch {
            pricingStore.taxConfigs.collect { list ->
                _uiState.update { it.copy(taxConfigs = list) }
            }
        }
        viewModelScope.launch {
            pricingStore.surcharges.collect { list ->
                _uiState.update { it.copy(surcharges = list) }
            }
        }
        viewModelScope.launch {
            pricingStore.discountRules.collect { list ->
                _uiState.update { it.copy(discountRules = list) }
            }
        }
        loadAll()
    }

    fun loadAll() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }
            pricingStore.refresh()
            _uiState.update { it.copy(isLoading = false) }
        }
    }

    // ── Surcharge ─────────────────────────────────────────────────────────────

    fun openSurchargeForm(surcharge: Surcharge? = null) =
        _uiState.update { it.copy(editingSurcharge = surcharge, showSurchargeForm = true) }

    fun closeSurchargeForm() = _uiState.update { it.copy(showSurchargeForm = false, editingSurcharge = null) }

    fun openSurchargeDeleteConfirm(surcharge: Surcharge) =
        _uiState.update { it.copy(editingSurcharge = surcharge, showSurchargeDeleteConfirm = true) }

    fun closeSurchargeDeleteConfirm() =
        _uiState.update { it.copy(showSurchargeDeleteConfirm = false, editingSurcharge = null) }

    fun saveSurcharge(orderType: String, name: String, amount: String, isPercentage: Boolean, maxAmount: String?) {
        val existing = _uiState.value.editingSurcharge
        viewModelScope.launch {
            _uiState.update { it.copy(isSubmitting = true) }
            val result = if (existing == null) {
                adminRepository.createSurcharge(orderType, name, amount, isPercentage, maxAmount)
            } else {
                adminRepository.updateSurcharge(existing.uuid, name, amount, isPercentage, maxAmount)
            }
            when (result) {
                is Resource.Success -> {
                    val saved = result.data
                    pricingStore.upsertSurcharge(saved)
                    _uiState.update { state ->
                        state.copy(isSubmitting = false, showSurchargeForm = false, editingSurcharge = null,
                            successMessage = if (existing == null) "Surcharge \"${saved.name}\" berhasil ditambahkan"
                                             else "Surcharge \"${saved.name}\" berhasil diperbarui")
                    }
                }
                is Resource.Error -> _uiState.update { it.copy(isSubmitting = false, error = result.message) }
                is Resource.Loading -> {}
            }
        }
    }

    fun deleteSurcharge() {
        val surcharge = _uiState.value.editingSurcharge ?: return
        viewModelScope.launch {
            _uiState.update { it.copy(isSubmitting = true) }
            when (val r = adminRepository.deleteSurcharge(surcharge.uuid)) {
                is Resource.Success -> {
                    pricingStore.removeSurcharge(surcharge.uuid)
                    _uiState.update { state ->
                        state.copy(isSubmitting = false, showSurchargeDeleteConfirm = false, editingSurcharge = null,
                            successMessage = "Surcharge \"${surcharge.name}\" berhasil dihapus")
                    }
                }
                is Resource.Error -> _uiState.update { it.copy(isSubmitting = false, error = r.message) }
                is Resource.Loading -> {}
            }
        }
    }

    /** Toggle aktif/nonaktif Surcharge — langsung sinkron ke kasir. */
    fun toggleSurchargeActive(surcharge: Surcharge, isActive: Boolean) {
        viewModelScope.launch {
            when (val r = pricingStore.toggleSurchargeActive(surcharge, isActive)) {
                is Resource.Success -> _uiState.update {
                    it.copy(successMessage = if (isActive) "Surcharge \"${r.data.name}\" diaktifkan"
                                             else "Surcharge \"${r.data.name}\" dinonaktifkan")
                }
                is Resource.Error -> _uiState.update { it.copy(error = r.message) }
                is Resource.Loading -> {}
            }
        }
    }

    // ── Tax Config ────────────────────────────────────────────────────────────

    fun openTaxForm(tax: TaxConfig? = null) = _uiState.update { it.copy(editingTax = tax, showTaxForm = true) }
    fun closeTaxForm() = _uiState.update { it.copy(showTaxForm = false, editingTax = null) }

    fun openTaxDeleteConfirm(tax: TaxConfig) =
        _uiState.update { it.copy(editingTax = tax, showTaxDeleteConfirm = true) }

    fun closeTaxDeleteConfirm() = _uiState.update { it.copy(showTaxDeleteConfirm = false, editingTax = null) }

    fun saveTax(name: String, rate: String, applyTo: String, sortOrder: Int) {
        val existing = _uiState.value.editingTax
        viewModelScope.launch {
            _uiState.update { it.copy(isSubmitting = true) }
            val result = if (existing == null) {
                adminRepository.createTaxConfig(name, rate, applyTo, sortOrder)
            } else {
                adminRepository.updateTaxConfig(existing.uuid, name, rate, applyTo, sortOrder)
            }
            when (result) {
                is Resource.Success -> {
                    val saved = result.data
                    pricingStore.upsertTax(saved)
                    _uiState.update { state ->
                        state.copy(isSubmitting = false, showTaxForm = false, editingTax = null,
                            successMessage = if (existing == null) "Pajak \"${saved.name}\" berhasil ditambahkan"
                                             else "Pajak \"${saved.name}\" berhasil diperbarui")
                    }
                }
                is Resource.Error -> _uiState.update { it.copy(isSubmitting = false, error = result.message) }
                is Resource.Loading -> {}
            }
        }
    }

    fun deleteTax() {
        val tax = _uiState.value.editingTax ?: return
        viewModelScope.launch {
            _uiState.update { it.copy(isSubmitting = true) }
            when (val r = adminRepository.deleteTaxConfig(tax.uuid)) {
                is Resource.Success -> {
                    pricingStore.removeTax(tax.uuid)
                    _uiState.update { state ->
                        state.copy(isSubmitting = false, showTaxDeleteConfirm = false, editingTax = null,
                            successMessage = "Pajak \"${tax.name}\" berhasil dihapus")
                    }
                }
                is Resource.Error -> _uiState.update { it.copy(isSubmitting = false, error = r.message) }
                is Resource.Loading -> {}
            }
        }
    }

    /** Toggle aktif/nonaktif Pajak — langsung sinkron ke kasir. */
    fun toggleTaxActive(tax: TaxConfig, isActive: Boolean) {
        viewModelScope.launch {
            when (val r = pricingStore.toggleTaxActive(tax, isActive)) {
                is Resource.Success -> _uiState.update {
                    it.copy(successMessage = if (isActive) "Pajak \"${r.data.name}\" diaktifkan"
                                             else "Pajak \"${r.data.name}\" dinonaktifkan")
                }
                is Resource.Error -> _uiState.update { it.copy(error = r.message) }
                is Resource.Loading -> {}
            }
        }
    }

    // ── Discount Rules ────────────────────────────────────────────────────────

    fun openDiscountForm(rule: DiscountRule? = null) =
        _uiState.update { it.copy(editingDiscount = rule, showDiscountForm = true) }

    fun closeDiscountForm() = _uiState.update { it.copy(showDiscountForm = false, editingDiscount = null) }

    fun openDiscountDeleteConfirm(rule: DiscountRule) =
        _uiState.update { it.copy(editingDiscount = rule, showDiscountDeleteConfirm = true) }

    fun closeDiscountDeleteConfirm() =
        _uiState.update { it.copy(showDiscountDeleteConfirm = false, editingDiscount = null) }

    fun saveDiscount(
        name: String, discountValue: Double, discountType: String,
        ruleType: String, isActive: Boolean, description: String?,
        maxDiscount: Double?, minPurchaseAmount: Double?
    ) {
        val existing = _uiState.value.editingDiscount
        viewModelScope.launch {
            _uiState.update { it.copy(isSubmitting = true) }
            val result = if (existing == null) {
                adminRepository.createDiscountRule(name, discountValue, description, ruleType, discountType,
                    minPurchaseAmount = minPurchaseAmount, maxDiscount = maxDiscount, isActive = isActive)
            } else {
                adminRepository.updateDiscountRule(existing.uuid, DiscountRuleUpdate(name, description, ruleType,
                    discountType, discountValue, maxDiscount = maxDiscount,
                    minPurchaseAmount = minPurchaseAmount, isActive = isActive))
            }
            when (result) {
                is Resource.Success -> {
                    val saved = result.data
                    pricingStore.upsertDiscountRule(saved)
                    _uiState.update { state ->
                        state.copy(isSubmitting = false, showDiscountForm = false, editingDiscount = null,
                            successMessage = if (existing == null) "Aturan diskon \"${saved.name}\" berhasil ditambahkan"
                                             else "Aturan diskon \"${saved.name}\" berhasil diperbarui")
                    }
                }
                is Resource.Error -> _uiState.update { it.copy(isSubmitting = false, error = result.message) }
                is Resource.Loading -> {}
            }
        }
    }

    fun deleteDiscount() {
        val rule = _uiState.value.editingDiscount ?: return
        viewModelScope.launch {
            _uiState.update { it.copy(isSubmitting = true) }
            when (val r = adminRepository.deleteDiscountRule(rule.uuid)) {
                is Resource.Success -> {
                    pricingStore.removeDiscountRule(rule.uuid)
                    _uiState.update { state ->
                        state.copy(isSubmitting = false, showDiscountDeleteConfirm = false, editingDiscount = null,
                            successMessage = "Aturan diskon \"${rule.name}\" berhasil dihapus")
                    }
                }
                is Resource.Error -> _uiState.update { it.copy(isSubmitting = false, error = r.message) }
                is Resource.Loading -> {}
            }
        }
    }

    /** Toggle aktif/nonaktif Aturan Diskon — langsung sinkron ke kasir. */
    fun toggleDiscountActive(rule: DiscountRule, isActive: Boolean) {
        viewModelScope.launch {
            when (val r = pricingStore.toggleDiscountRuleActive(rule, isActive)) {
                is Resource.Success -> _uiState.update {
                    it.copy(successMessage = if (isActive) "Aturan diskon \"${r.data.name}\" diaktifkan"
                                             else "Aturan diskon \"${r.data.name}\" dinonaktifkan")
                }
                is Resource.Error -> _uiState.update { it.copy(error = r.message) }
                is Resource.Loading -> {}
            }
        }
    }

    fun clearSuccessMessage() = _uiState.update { it.copy(successMessage = null) }
    fun clearError() = _uiState.update { it.copy(error = null) }
}

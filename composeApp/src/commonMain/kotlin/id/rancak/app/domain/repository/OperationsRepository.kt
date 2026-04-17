package id.rancak.app.domain.repository

import id.rancak.app.domain.model.*

interface OperationsRepository {
    suspend fun getTables(): Resource<List<Table>>
    suspend fun getCurrentShift(): Resource<Shift?>
    suspend fun openShift(openingCash: String): Resource<Shift>
    suspend fun closeShift(closingCash: String, note: String?): Resource<Shift>
    suspend fun getKdsOrders(): Resource<List<KdsOrder>>
    suspend fun updateKdsStatus(kdsUuid: String, status: KdsStatus): Resource<Unit>
    suspend fun getSurcharges(): Resource<List<Surcharge>>
    suspend fun getTaxConfigs(): Resource<List<TaxConfig>>
    suspend fun getDiscountRules(): Resource<List<DiscountRule>>
    suspend fun validateVoucher(code: String, subtotal: Long): Resource<VoucherValidation>
    suspend fun previewDiscount(total: Long): Resource<DiscountPreview>
    suspend fun syncCatalog(updatedAfter: String? = null): Resource<Unit>
    suspend fun syncStatus(): Resource<Boolean>
    suspend fun getShiftSummaryById(shiftUuid: String): Resource<ShiftSummary>
}

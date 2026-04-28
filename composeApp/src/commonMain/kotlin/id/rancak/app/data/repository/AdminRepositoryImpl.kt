package id.rancak.app.data.repository

import id.rancak.app.data.local.TokenManager
import id.rancak.app.data.mapper.toDomain
import id.rancak.app.data.remote.api.RancakApiService
import id.rancak.app.data.remote.api.adjustStock
import id.rancak.app.data.remote.api.createBundle
import id.rancak.app.data.remote.api.createCategory
import id.rancak.app.data.remote.api.createDiscountRule
import id.rancak.app.data.remote.api.createModifier
import id.rancak.app.data.remote.api.createProduct
import id.rancak.app.data.remote.api.createProductModifier
import id.rancak.app.data.remote.api.createProductBatch
import id.rancak.app.data.remote.api.createSurcharge
import id.rancak.app.data.remote.api.createTaxConfig
import id.rancak.app.data.remote.api.createTable
import id.rancak.app.data.remote.api.createVariant
import id.rancak.app.data.remote.api.createVariantGroup
import id.rancak.app.data.remote.api.createVoucher
import id.rancak.app.data.remote.api.deleteBundle
import id.rancak.app.data.remote.api.deleteCategory
import id.rancak.app.data.remote.api.deleteDiscountRule
import id.rancak.app.data.remote.api.deleteModifier
import id.rancak.app.data.remote.api.deleteProduct
import id.rancak.app.data.remote.api.deleteSurcharge
import id.rancak.app.data.remote.api.deleteTaxConfig
import id.rancak.app.data.remote.api.deleteTable
import id.rancak.app.data.remote.api.deleteVariant
import id.rancak.app.data.remote.api.deleteVariantGroup
import id.rancak.app.data.remote.api.deleteVoucher
import id.rancak.app.data.remote.api.getBundle
import id.rancak.app.data.remote.api.getDiscountRule
import id.rancak.app.data.remote.api.getModifier
import id.rancak.app.data.remote.api.getProductBatches
import id.rancak.app.data.remote.api.getSurcharge
import id.rancak.app.data.remote.api.getTaxConfig
import id.rancak.app.data.remote.api.getTable
import id.rancak.app.data.remote.api.getVariantGroups
import id.rancak.app.data.remote.api.getVariants
import id.rancak.app.data.remote.api.getVoucher
import id.rancak.app.data.remote.api.getVouchers
import id.rancak.app.data.remote.api.updateBundle
import id.rancak.app.data.remote.api.updateCategory
import id.rancak.app.data.remote.api.updateDiscountRule
import id.rancak.app.data.remote.api.updateModifier
import id.rancak.app.data.remote.api.updateProduct
import id.rancak.app.data.remote.api.updateReceiptSettings
import id.rancak.app.data.remote.api.updateSurcharge
import id.rancak.app.data.remote.api.updateTaxConfig
import id.rancak.app.data.remote.api.updateTable
import id.rancak.app.data.remote.api.updateVariant
import id.rancak.app.data.remote.api.updateVariantGroup
import id.rancak.app.data.remote.api.updateVoucher
import id.rancak.app.data.remote.dto.admin.BundleItemRequest
import id.rancak.app.data.remote.dto.admin.CreateCategoryRequest
import id.rancak.app.data.remote.dto.admin.CreateProductBatchRequest
import id.rancak.app.data.remote.dto.admin.CreateProductRequest
import id.rancak.app.data.remote.dto.admin.CreateBundleRequest
import id.rancak.app.data.remote.dto.admin.CreateDiscountRuleRequest
import id.rancak.app.data.remote.dto.admin.CreateModifierRequest
import id.rancak.app.data.remote.dto.admin.CreateSurchargeRequest
import id.rancak.app.data.remote.dto.admin.CreateTaxConfigRequest
import id.rancak.app.data.remote.dto.admin.CreateTableRequest
import id.rancak.app.data.remote.dto.admin.CreateVariantGroupRequest
import id.rancak.app.data.remote.dto.admin.CreateVariantRequest
import id.rancak.app.data.remote.dto.admin.CreateVoucherRequest
import id.rancak.app.data.remote.dto.admin.StockAdjustmentRequest
import id.rancak.app.data.remote.dto.admin.UpdateBundleRequest
import id.rancak.app.data.remote.dto.admin.UpdateCategoryRequest
import id.rancak.app.data.remote.dto.admin.UpdateDiscountRuleRequest
import id.rancak.app.data.remote.dto.admin.UpdateModifierRequest
import id.rancak.app.data.remote.dto.admin.UpdateProductRequest
import id.rancak.app.data.remote.dto.admin.UpdateReceiptSettingsRequest
import id.rancak.app.data.remote.dto.admin.UpdateSurchargeRequest
import id.rancak.app.data.remote.dto.admin.UpdateTaxConfigRequest
import id.rancak.app.data.remote.dto.admin.UpdateTableRequest
import id.rancak.app.data.remote.dto.admin.UpdateVariantGroupRequest
import id.rancak.app.data.remote.dto.admin.UpdateVariantRequest
import id.rancak.app.data.remote.dto.admin.UpdateVoucherRequest
import id.rancak.app.domain.model.*
import id.rancak.app.domain.repository.AdminRepository
import id.rancak.app.domain.repository.BundleItemEntry
import id.rancak.app.domain.repository.BundleUpdate
import id.rancak.app.domain.repository.DiscountRuleUpdate
import id.rancak.app.domain.repository.ReceiptSettingsUpdate
import id.rancak.app.domain.repository.StockAdjustmentResult
import id.rancak.app.domain.repository.VoucherUpdate

class AdminRepositoryImpl(
    private val api: RancakApiService,
    private val tokenManager: TokenManager
) : AdminRepository {

    private val tenantUuid: String
        get() = tokenManager.tenantUuid ?: throw IllegalStateException("Tenant belum dipilih")

    // ── Surcharges ─────────────────────────────────────────────────────────────

    override suspend fun getSurcharge(surchargeId: String): Resource<Surcharge> = safe(
        block = { api.getSurcharge(tenantUuid, surchargeId) },
        map = { it.toDomain() },
        errorMsg = "Gagal memuat surcharge"
    )

    override suspend fun createSurcharge(
        orderType: String, name: String, amount: String, isPercentage: Boolean,
        maxAmount: String?, sortOrder: Int
    ): Resource<Surcharge> = safe(
        block = { api.createSurcharge(tenantUuid, CreateSurchargeRequest(orderType, name, amount, isPercentage, maxAmount, sortOrder)) },
        map = { it.toDomain() },
        errorMsg = "Gagal membuat surcharge"
    )

    override suspend fun updateSurcharge(
        surchargeId: String, name: String?, amount: String?, isPercentage: Boolean?,
        maxAmount: String?, isActive: Boolean?, sortOrder: Int?
    ): Resource<Surcharge> = safe(
        block = { api.updateSurcharge(tenantUuid, surchargeId, UpdateSurchargeRequest(name, amount, isPercentage, maxAmount, isActive, sortOrder)) },
        map = { it.toDomain() },
        errorMsg = "Gagal mengupdate surcharge"
    )

    override suspend fun deleteSurcharge(surchargeId: String): Resource<Unit> = safeUnit(
        block = { api.deleteSurcharge(tenantUuid, surchargeId) },
        errorMsg = "Gagal menghapus surcharge"
    )

    // ── Vouchers ───────────────────────────────────────────────────────────────

    override suspend fun getVouchers(isActive: Boolean?, page: Int, limit: Int): Resource<List<Voucher>> = safe(
        block = { api.getVouchers(tenantUuid, isActive, page, limit) },
        map = { list -> list.map { it.toDomain() } },
        errorMsg = "Gagal memuat voucher"
    )

    override suspend fun getVoucher(voucherId: String): Resource<Voucher> = safe(
        block = { api.getVoucher(tenantUuid, voucherId) },
        map = { it.toDomain() },
        errorMsg = "Gagal memuat voucher"
    )

    override suspend fun createVoucher(
        code: String, name: String, discountType: String, discountValue: String, validFrom: String,
        description: String?, maxDiscount: String?, minPurchase: String, usageLimit: Int?,
        validUntil: String?, isActive: Boolean
    ): Resource<Voucher> = safe(
        block = { api.createVoucher(tenantUuid, CreateVoucherRequest(code, name, description, discountType, discountValue, maxDiscount, minPurchase, usageLimit, validFrom, validUntil, isActive)) },
        map = { it.toDomain() },
        errorMsg = "Gagal membuat voucher"
    )

    override suspend fun updateVoucher(voucherId: String, update: VoucherUpdate): Resource<Voucher> = safe(
        block = { api.updateVoucher(tenantUuid, voucherId, UpdateVoucherRequest(update.code, update.name, update.description, update.discountType, update.discountValue, update.maxDiscount, update.minPurchase, update.usageLimit, update.validFrom, update.validUntil, update.isActive)) },
        map = { it.toDomain() },
        errorMsg = "Gagal mengupdate voucher"
    )

    override suspend fun deleteVoucher(voucherId: String): Resource<Unit> = safeUnit(
        block = { api.deleteVoucher(tenantUuid, voucherId) },
        errorMsg = "Gagal menghapus voucher"
    )

    // ── Tax Configs ────────────────────────────────────────────────────────────

    override suspend fun getTaxConfig(configId: String): Resource<TaxConfig> = safe(
        block = { api.getTaxConfig(tenantUuid, configId) },
        map = { it.toDomain() },
        errorMsg = "Gagal memuat konfigurasi pajak"
    )

    override suspend fun createTaxConfig(name: String, rate: String, applyTo: String, sortOrder: Int): Resource<TaxConfig> = safe(
        block = { api.createTaxConfig(tenantUuid, CreateTaxConfigRequest(name, rate, applyTo, sortOrder)) },
        map = { it.toDomain() },
        errorMsg = "Gagal membuat konfigurasi pajak"
    )

    override suspend fun updateTaxConfig(configId: String, name: String?, rate: String?, applyTo: String?, sortOrder: Int?, isActive: Boolean?): Resource<TaxConfig> = safe(
        block = { api.updateTaxConfig(tenantUuid, configId, UpdateTaxConfigRequest(name, rate, applyTo, sortOrder, isActive)) },
        map = { it.toDomain() },
        errorMsg = "Gagal mengupdate konfigurasi pajak"
    )

    override suspend fun deleteTaxConfig(configId: String): Resource<Unit> = safeUnit(
        block = { api.deleteTaxConfig(tenantUuid, configId) },
        errorMsg = "Gagal menghapus konfigurasi pajak"
    )

    // ── Tables ─────────────────────────────────────────────────────────────────

    override suspend fun getTable(tableId: String): Resource<Table> = safe(
        block = { api.getTable(tenantUuid, tableId) },
        map = { it.toDomain() },
        errorMsg = "Gagal memuat meja"
    )

    override suspend fun createTable(name: String, area: String?, capacity: Int, isActive: Boolean, sortOrder: Int): Resource<Table> = safe(
        block = { api.createTable(tenantUuid, CreateTableRequest(name, area, capacity, isActive, sortOrder)) },
        map = { it.toDomain() },
        errorMsg = "Gagal membuat meja"
    )

    override suspend fun updateTable(tableId: String, name: String?, area: String?, capacity: Int?, status: String?, isActive: Boolean?, sortOrder: Int?): Resource<Table> = safe(
        block = { api.updateTable(tenantUuid, tableId, UpdateTableRequest(name, area, capacity, status, isActive, sortOrder)) },
        map = { it.toDomain() },
        errorMsg = "Gagal mengupdate meja"
    )

    override suspend fun deleteTable(tableId: String): Resource<Unit> = safeUnit(
        block = { api.deleteTable(tenantUuid, tableId) },
        errorMsg = "Gagal menghapus meja"
    )

    // ── Bundles ────────────────────────────────────────────────────────────────

    override suspend fun getBundle(bundleId: String): Resource<Bundle> = safe(
        block = { api.getBundle(tenantUuid, bundleId) },
        map = { it.toDomain() },
        errorMsg = "Gagal memuat bundle"
    )

    override suspend fun createBundle(name: String, price: String, items: List<BundleItemEntry>, description: String?, sku: String?, isActive: Boolean, sortOrder: Int): Resource<Bundle> = safe(
        block = { api.createBundle(tenantUuid, CreateBundleRequest(name, price, items.map { BundleItemRequest(it.productUuid, it.qty) }, description, sku, isActive, sortOrder)) },
        map = { it.toDomain() },
        errorMsg = "Gagal membuat bundle"
    )

    override suspend fun updateBundle(bundleId: String, update: BundleUpdate): Resource<Bundle> = safe(
        block = { api.updateBundle(tenantUuid, bundleId, UpdateBundleRequest(update.name, update.description, update.price, update.sku, update.isActive, update.sortOrder, update.items?.map { BundleItemRequest(it.productUuid, it.qty) })) },
        map = { it.toDomain() },
        errorMsg = "Gagal mengupdate bundle"
    )

    override suspend fun deleteBundle(bundleId: String): Resource<Unit> = safeUnit(
        block = { api.deleteBundle(tenantUuid, bundleId) },
        errorMsg = "Gagal menghapus bundle"
    )

    // ── Modifiers ──────────────────────────────────────────────────────────────

    override suspend fun getModifier(modifierId: String): Resource<Modifier> = safe(
        block = { api.getModifier(tenantUuid, modifierId) },
        map = { it.toDomain() },
        errorMsg = "Gagal memuat modifier"
    )

    override suspend fun createModifier(name: String, sortOrder: Int, isActive: Boolean): Resource<Modifier> = safe(
        block = { api.createModifier(tenantUuid, CreateModifierRequest(name, sortOrder, isActive)) },
        map = { it.toDomain() },
        errorMsg = "Gagal membuat modifier"
    )

    override suspend fun createProductModifier(productUuid: String, name: String, sortOrder: Int, isActive: Boolean): Resource<Modifier> = safe(
        block = { api.createProductModifier(tenantUuid, productUuid, CreateModifierRequest(name, sortOrder, isActive)) },
        map = { it.toDomain() },
        errorMsg = "Gagal membuat modifier produk"
    )

    override suspend fun updateModifier(modifierId: String, name: String?, sortOrder: Int?, isActive: Boolean?): Resource<Modifier> = safe(
        block = { api.updateModifier(tenantUuid, modifierId, UpdateModifierRequest(name, sortOrder, isActive)) },
        map = { it.toDomain() },
        errorMsg = "Gagal mengupdate modifier"
    )

    override suspend fun deleteModifier(modifierId: String): Resource<Unit> = safeUnit(
        block = { api.deleteModifier(tenantUuid, modifierId) },
        errorMsg = "Gagal menghapus modifier"
    )

    // ── Discount Rules ─────────────────────────────────────────────────────────

    override suspend fun getDiscountRule(ruleId: String): Resource<DiscountRule> = safe(
        block = { api.getDiscountRule(tenantUuid, ruleId) },
        map = { it.toDomain() },
        errorMsg = "Gagal memuat aturan diskon"
    )

    override suspend fun createDiscountRule(
        name: String, discountValue: Double, description: String?, ruleType: String, discountType: String,
        startTime: String?, endTime: String?, applicableDays: List<Int>?, minPurchaseAmount: Double?,
        priority: Int, stackable: Boolean, maxDiscount: Double?, isActive: Boolean
    ): Resource<DiscountRule> = safe(
        block = { api.createDiscountRule(tenantUuid, CreateDiscountRuleRequest(name, description, ruleType, discountType, discountValue, startTime, endTime, applicableDays, minPurchaseAmount, priority, stackable, maxDiscount, isActive)) },
        map = { it.toDomain() },
        errorMsg = "Gagal membuat aturan diskon"
    )

    override suspend fun updateDiscountRule(ruleId: String, update: DiscountRuleUpdate): Resource<DiscountRule> = safe(
        block = { api.updateDiscountRule(tenantUuid, ruleId, UpdateDiscountRuleRequest(update.name, update.description, update.ruleType, update.discountType, update.discountValue, update.startTime, update.endTime, update.applicableDays, update.minPurchaseAmount, update.priority, update.stackable, update.maxDiscount, update.isActive)) },
        map = { it.toDomain() },
        errorMsg = "Gagal mengupdate aturan diskon"
    )

    override suspend fun deleteDiscountRule(ruleId: String): Resource<Unit> = safeUnit(
        block = { api.deleteDiscountRule(tenantUuid, ruleId) },
        errorMsg = "Gagal menghapus aturan diskon"
    )

    // ── Variant Groups ─────────────────────────────────────────────────────────

    override suspend fun getVariantGroups(productId: String): Resource<List<VariantGroup>> = safe(
        block = { api.getVariantGroups(tenantUuid, productId) },
        map = { list -> list.map { it.toDomain() } },
        errorMsg = "Gagal memuat variant group"
    )

    override suspend fun createVariantGroup(productId: String, name: String, isRequired: Boolean, sortOrder: Int): Resource<VariantGroup> = safe(
        block = { api.createVariantGroup(tenantUuid, productId, CreateVariantGroupRequest(name, isRequired, sortOrder)) },
        map = { it.toDomain() },
        errorMsg = "Gagal membuat variant group"
    )

    override suspend fun updateVariantGroup(productId: String, groupId: String, name: String?, isRequired: Boolean?, sortOrder: Int?): Resource<VariantGroup> = safe(
        block = { api.updateVariantGroup(tenantUuid, productId, groupId, UpdateVariantGroupRequest(name, isRequired, sortOrder)) },
        map = { it.toDomain() },
        errorMsg = "Gagal mengupdate variant group"
    )

    override suspend fun deleteVariantGroup(productId: String, groupId: String): Resource<Unit> = safeUnit(
        block = { api.deleteVariantGroup(tenantUuid, productId, groupId) },
        errorMsg = "Gagal menghapus variant group"
    )

    // ── Variants ───────────────────────────────────────────────────────────────

    override suspend fun getVariants(productId: String, groupId: String): Resource<List<Variant>> = safe(
        block = { api.getVariants(tenantUuid, productId, groupId) },
        map = { list -> list.map { it.toDomain() } },
        errorMsg = "Gagal memuat varian"
    )

    override suspend fun createVariant(productId: String, groupId: String, name: String, priceAdjustment: String, isDefault: Boolean, isActive: Boolean, sortOrder: Int): Resource<Variant> = safe(
        block = { api.createVariant(tenantUuid, productId, groupId, CreateVariantRequest(name, priceAdjustment, isDefault, isActive, sortOrder)) },
        map = { it.toDomain() },
        errorMsg = "Gagal membuat varian"
    )

    override suspend fun updateVariant(productId: String, groupId: String, variantId: String, name: String?, priceAdjustment: String?, isDefault: Boolean?, isActive: Boolean?, sortOrder: Int?): Resource<Variant> = safe(
        block = { api.updateVariant(tenantUuid, productId, groupId, variantId, UpdateVariantRequest(name, priceAdjustment, isDefault, isActive, sortOrder)) },
        map = { it.toDomain() },
        errorMsg = "Gagal mengupdate varian"
    )

    override suspend fun deleteVariant(productId: String, groupId: String, variantId: String): Resource<Unit> = safeUnit(
        block = { api.deleteVariant(tenantUuid, productId, groupId, variantId) },
        errorMsg = "Gagal menghapus varian"
    )

    // ── Stock adjustment ───────────────────────────────────────────────────────

    override suspend fun adjustStock(productId: String, adjustmentType: String, quantity: Double, note: String?): Resource<StockAdjustmentResult> = safe(
        block = { api.adjustStock(tenantUuid, productId, StockAdjustmentRequest(adjustmentType, quantity, note)) },
        map = { dto -> StockAdjustmentResult(dto.productUuid, dto.productName, dto.stockBefore, dto.stockAfter, dto.adjustmentType, dto.quantity) },
        errorMsg = "Gagal menyesuaikan stok"
    )

    override suspend fun getProductBatches(productId: String): Resource<List<ProductBatch>> = safe(
        block = { api.getProductBatches(tenantUuid, productId) },
        map = { list -> list.map { it.toDomain() } },
        errorMsg = "Gagal memuat batch produk"
    )

    override suspend fun createProductBatch(
        productId: String, quantity: Double, expiryDate: String?,
        costPrice: Long?, batchNumber: String?, note: String?, receivedAt: String?
    ): Resource<ProductBatch> = safe(
        block = { api.createProductBatch(tenantUuid, productId, CreateProductBatchRequest(quantity, expiryDate, costPrice, batchNumber, note, receivedAt)) },
        map = { it.toDomain() },
        errorMsg = "Gagal menambah batch produk"
    )

    // ── Product CRUD ───────────────────────────────────────────────────────────

    override suspend fun createProduct(
        name: String, price: Long, description: String?, sku: String?, barcode: String?,
        categoryUuid: String?, unit: String?, stock: Double, hasExpiry: Boolean, isActive: Boolean
    ): Resource<Product> = safe(
        block = { api.createProduct(tenantUuid, CreateProductRequest(name, price, description, sku, barcode, categoryUuid, unit, stock, hasExpiry, isActive)) },
        map = { it.toDomain() },
        errorMsg = "Gagal membuat produk"
    )

    override suspend fun updateProduct(
        productId: String, name: String?, price: Long?, description: String?, sku: String?,
        barcode: String?, categoryUuid: String?, unit: String?, isActive: Boolean?
    ): Resource<Product> = safe(
        block = { api.updateProduct(tenantUuid, productId, UpdateProductRequest(name, price, description, sku, barcode, categoryUuid, unit, isActive)) },
        map = { it.toDomain() },
        errorMsg = "Gagal mengupdate produk"
    )

    override suspend fun deleteProduct(productId: String): Resource<Unit> = safeUnit(
        block = { api.deleteProduct(tenantUuid, productId) },
        errorMsg = "Gagal menghapus produk"
    )

    // ── Category CRUD ──────────────────────────────────────────────────────────

    override suspend fun createCategory(name: String, description: String?): Resource<Category> = safe(
        block = { api.createCategory(tenantUuid, CreateCategoryRequest(name, description)) },
        map = { it.toDomain() },
        errorMsg = "Gagal membuat kategori"
    )

    override suspend fun updateCategory(categoryId: String, name: String?, description: String?): Resource<Category> = safe(
        block = { api.updateCategory(tenantUuid, categoryId, UpdateCategoryRequest(name, description)) },
        map = { it.toDomain() },
        errorMsg = "Gagal mengupdate kategori"
    )

    override suspend fun deleteCategory(categoryId: String): Resource<Unit> = safeUnit(
        block = { api.deleteCategory(tenantUuid, categoryId) },
        errorMsg = "Gagal menghapus kategori"
    )

    // ── Receipt settings ───────────────────────────────────────────────────────

    override suspend fun updateReceiptSettings(update: ReceiptSettingsUpdate): Resource<ReceiptSettings> = safe(
        block = { api.updateReceiptSettings(tenantUuid, UpdateReceiptSettingsRequest(update.logoUrl, update.email, update.website, update.npwp, update.receiptHeader, update.receiptFooter, update.receiptFooter2, update.logoPosition, update.logoSizePct, update.receiptNameSize, update.separatorStyle, update.separatorCount, update.footerPosition, update.receiptInstagram, update.receiptFacebook)) },
        map = { it.toDomain() },
        errorMsg = "Gagal mengupdate pengaturan struk"
    )
}

// ── Helpers ────────────────────────────────────────────────────────────────────

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

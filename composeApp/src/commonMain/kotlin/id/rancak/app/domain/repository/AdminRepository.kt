package id.rancak.app.domain.repository

import id.rancak.app.domain.model.*

/**
 * Admin/Owner write operations: pricing rules, tables, bundles, modifiers,
 * product variants, stock adjustment, and receipt settings.
 */
interface AdminRepository {

    // ── Surcharges ─────────────────────────────────────────────────────────────
    suspend fun getSurcharge(surchargeId: String): Resource<Surcharge>
    suspend fun createSurcharge(
        orderType: String,
        name: String,
        amount: String,
        isPercentage: Boolean = false,
        maxAmount: String? = null,
        sortOrder: Int = 0
    ): Resource<Surcharge>
    suspend fun updateSurcharge(
        surchargeId: String,
        name: String? = null,
        amount: String? = null,
        isPercentage: Boolean? = null,
        maxAmount: String? = null,
        isActive: Boolean? = null,
        sortOrder: Int? = null
    ): Resource<Surcharge>
    suspend fun deleteSurcharge(surchargeId: String): Resource<Unit>

    // ── Vouchers ───────────────────────────────────────────────────────────────
    suspend fun getVouchers(isActive: Boolean? = null, page: Int = 1, limit: Int = 50): Resource<List<Voucher>>
    suspend fun getVoucher(voucherId: String): Resource<Voucher>
    suspend fun createVoucher(
        code: String,
        name: String,
        discountType: String,
        discountValue: String,
        validFrom: String,
        description: String? = null,
        maxDiscount: String? = null,
        minPurchase: String = "0",
        usageLimit: Int? = null,
        validUntil: String? = null,
        isActive: Boolean = true
    ): Resource<Voucher>
    suspend fun updateVoucher(voucherId: String, update: VoucherUpdate): Resource<Voucher>
    suspend fun deleteVoucher(voucherId: String): Resource<Unit>

    // ── Tax Configs ────────────────────────────────────────────────────────────
    suspend fun getTaxConfig(configId: String): Resource<TaxConfig>
    suspend fun createTaxConfig(
        name: String,
        rate: String,
        applyTo: String = "after_discount",
        sortOrder: Int = 0
    ): Resource<TaxConfig>
    suspend fun updateTaxConfig(
        configId: String,
        name: String? = null,
        rate: String? = null,
        applyTo: String? = null,
        sortOrder: Int? = null,
        isActive: Boolean? = null
    ): Resource<TaxConfig>
    suspend fun deleteTaxConfig(configId: String): Resource<Unit>

    // ── Tables ─────────────────────────────────────────────────────────────────
    suspend fun getTable(tableId: String): Resource<Table>
    suspend fun createTable(
        name: String,
        area: String? = null,
        capacity: Int = 2,
        isActive: Boolean = true,
        sortOrder: Int = 0
    ): Resource<Table>
    suspend fun updateTable(
        tableId: String,
        name: String? = null,
        area: String? = null,
        capacity: Int? = null,
        status: String? = null,
        isActive: Boolean? = null,
        sortOrder: Int? = null
    ): Resource<Table>
    suspend fun deleteTable(tableId: String): Resource<Unit>

    // ── Bundles ────────────────────────────────────────────────────────────────
    suspend fun getBundle(bundleId: String): Resource<Bundle>
    suspend fun createBundle(
        name: String,
        price: String,
        items: List<BundleItemEntry>,
        description: String? = null,
        sku: String? = null,
        isActive: Boolean = true,
        sortOrder: Int = 0
    ): Resource<Bundle>
    suspend fun updateBundle(bundleId: String, update: BundleUpdate): Resource<Bundle>
    suspend fun deleteBundle(bundleId: String): Resource<Unit>

    // ── Modifiers ──────────────────────────────────────────────────────────────
    suspend fun getModifier(modifierId: String): Resource<Modifier>
    suspend fun createModifier(name: String, sortOrder: Int = 0, isActive: Boolean = true): Resource<Modifier>
    suspend fun createProductModifier(
        productUuid: String,
        name: String,
        sortOrder: Int = 0,
        isActive: Boolean = true
    ): Resource<Modifier>
    suspend fun updateModifier(
        modifierId: String,
        name: String? = null,
        sortOrder: Int? = null,
        isActive: Boolean? = null
    ): Resource<Modifier>
    suspend fun deleteModifier(modifierId: String): Resource<Unit>

    // ── Discount Rules ─────────────────────────────────────────────────────────
    suspend fun getDiscountRule(ruleId: String): Resource<DiscountRule>
    suspend fun createDiscountRule(
        name: String,
        discountValue: Double,
        description: String? = null,
        ruleType: String = "always",
        discountType: String = "pct",
        startTime: String? = null,
        endTime: String? = null,
        applicableDays: List<Int>? = null,
        minPurchaseAmount: Double? = null,
        priority: Int = 0,
        stackable: Boolean = false,
        maxDiscount: Double? = null,
        isActive: Boolean = true
    ): Resource<DiscountRule>
    suspend fun updateDiscountRule(ruleId: String, update: DiscountRuleUpdate): Resource<DiscountRule>
    suspend fun deleteDiscountRule(ruleId: String): Resource<Unit>

    // ── Variant Groups ─────────────────────────────────────────────────────────
    suspend fun getVariantGroups(productId: String): Resource<List<VariantGroup>>
    suspend fun createVariantGroup(
        productId: String,
        name: String,
        isRequired: Boolean = false,
        sortOrder: Int = 0
    ): Resource<VariantGroup>
    suspend fun updateVariantGroup(
        productId: String,
        groupId: String,
        name: String? = null,
        isRequired: Boolean? = null,
        sortOrder: Int? = null
    ): Resource<VariantGroup>
    suspend fun deleteVariantGroup(productId: String, groupId: String): Resource<Unit>

    // ── Variants ───────────────────────────────────────────────────────────────
    suspend fun getVariants(productId: String, groupId: String): Resource<List<Variant>>
    suspend fun createVariant(
        productId: String,
        groupId: String,
        name: String,
        priceAdjustment: String = "0",
        isDefault: Boolean = false,
        isActive: Boolean = true,
        sortOrder: Int = 0
    ): Resource<Variant>
    suspend fun updateVariant(
        productId: String,
        groupId: String,
        variantId: String,
        name: String? = null,
        priceAdjustment: String? = null,
        isDefault: Boolean? = null,
        isActive: Boolean? = null,
        sortOrder: Int? = null
    ): Resource<Variant>
    suspend fun deleteVariant(productId: String, groupId: String, variantId: String): Resource<Unit>

    // ── Stock adjustment ───────────────────────────────────────────────────────
    suspend fun adjustStock(
        productId: String,
        adjustmentType: String,
        quantity: Double,
        note: String? = null
    ): Resource<StockAdjustmentResult>

    suspend fun getProductBatches(productId: String): Resource<List<ProductBatch>>
    suspend fun createProductBatch(
        productId: String, quantity: Double, expiryDate: String? = null,
        costPrice: Long? = null, batchNumber: String? = null,
        note: String? = null, receivedAt: String? = null
    ): Resource<ProductBatch>

    // ── Receipt settings ───────────────────────────────────────────────────────
    suspend fun updateReceiptSettings(update: ReceiptSettingsUpdate): Resource<ReceiptSettings>
}

// ── Support types ──────────────────────────────────────────────────────────────

data class VoucherUpdate(
    val code: String? = null,
    val name: String? = null,
    val description: String? = null,
    val discountType: String? = null,
    val discountValue: String? = null,
    val maxDiscount: String? = null,
    val minPurchase: String? = null,
    val usageLimit: Int? = null,
    val validFrom: String? = null,
    val validUntil: String? = null,
    val isActive: Boolean? = null
)

data class BundleItemEntry(val productUuid: String, val qty: String = "1")

data class BundleUpdate(
    val name: String? = null,
    val description: String? = null,
    val price: String? = null,
    val sku: String? = null,
    val isActive: Boolean? = null,
    val sortOrder: Int? = null,
    val items: List<BundleItemEntry>? = null
)

data class DiscountRuleUpdate(
    val name: String? = null,
    val description: String? = null,
    val ruleType: String? = null,
    val discountType: String? = null,
    val discountValue: Double? = null,
    val startTime: String? = null,
    val endTime: String? = null,
    val applicableDays: List<Int>? = null,
    val minPurchaseAmount: Double? = null,
    val priority: Int? = null,
    val stackable: Boolean? = null,
    val maxDiscount: Double? = null,
    val isActive: Boolean? = null
)

data class StockAdjustmentResult(
    val productUuid: String,
    val productName: String,
    val stockBefore: Double,
    val stockAfter: Double,
    val adjustmentType: String,
    val quantity: Double
)

data class ReceiptSettingsUpdate(
    val logoUrl: String? = null,
    val email: String? = null,
    val website: String? = null,
    val npwp: String? = null,
    val receiptHeader: String? = null,
    val receiptFooter: String? = null,
    val receiptFooter2: String? = null,
    val logoPosition: String? = null,
    val logoSizePct: Int? = null,
    val receiptNameSize: String? = null,
    val separatorStyle: String? = null,
    val separatorCount: Int? = null,
    val footerPosition: String? = null,
    val receiptInstagram: String? = null,
    val receiptFacebook: String? = null
)

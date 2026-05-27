package id.rancak.app

import com.russhwolf.settings.MapSettings
import id.rancak.app.data.local.TokenManager
import id.rancak.app.data.local.db.dao.CategoryDao
import id.rancak.app.data.local.db.dao.ProductDao
import id.rancak.app.data.local.db.dao.SaleDao
import id.rancak.app.data.local.db.entity.CategoryEntity
import id.rancak.app.data.local.db.entity.ProductEntity
import id.rancak.app.data.local.db.entity.SaleEntity
import id.rancak.app.data.local.db.entity.SaleItemEntity
import id.rancak.app.data.remote.api.RancakApiService
import id.rancak.app.data.sync.SyncScheduler
import io.ktor.client.*
import io.ktor.client.engine.mock.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.http.*
import io.ktor.serialization.kotlinx.json.*
import kotlinx.serialization.json.Json

// ─────────────────────────────────────────────────────────────────────────────
// TokenManager factory (in-memory settings — no SharedPreferences/NSUserDefaults)
// ─────────────────────────────────────────────────────────────────────────────

fun testTokenManager(tenantUuid: String? = "tenant-001"): TokenManager {
    val settings = MapSettings()
    val tm = TokenManager(settings)
    if (tenantUuid != null) tm.setTenant(tenantUuid, "Kafe Rancak")
    return tm
}

// ─────────────────────────────────────────────────────────────────────────────
// Ktor MockEngine helpers — build a RancakApiService backed by controlled responses
// ─────────────────────────────────────────────────────────────────────────────

private val testJson = Json {
    ignoreUnknownKeys = true
    isLenient = true
    coerceInputValues = true
}

/**
 * Creates a [RancakApiService] whose HTTP engine responds using [handler].
 * The handler receives the full [HttpRequestData] so tests can assert on URL/method/body.
 */
fun mockApiService(
    handler: suspend MockRequestHandleScope.(request: io.ktor.client.request.HttpRequestData) -> io.ktor.client.request.HttpResponseData
): RancakApiService {
    val engine = MockEngine(handler)
    val client = HttpClient(engine) {
        install(ContentNegotiation) {
            json(testJson)
        }
    }
    return RancakApiService(client)
}

/** Convenience: always returns the same JSON body with status 200 OK. */
fun mockApiService(responseBody: String, status: HttpStatusCode = HttpStatusCode.OK) =
    mockApiService { _ ->
        respond(
            content    = responseBody,
            status     = status,
            headers    = headersOf(HttpHeaders.ContentType, "application/json")
        )
    }

// ─────────────────────────────────────────────────────────────────────────────
// In-memory DAO fakes
// ─────────────────────────────────────────────────────────────────────────────

class FakeProductDao : ProductDao {
    val products = mutableListOf<ProductEntity>()

    override suspend fun getAll(): List<ProductEntity> = products.toList()

    override suspend fun search(query: String, categoryUuid: String): List<ProductEntity> =
        products.filter { p ->
            (query.isEmpty()        || p.name.contains(query, ignoreCase = true)
                                    || p.sku?.contains(query, ignoreCase = true) == true) &&
            (categoryUuid.isEmpty() || p.categoryUuid == categoryUuid)
        }

    override suspend fun findByBarcode(barcode: String): ProductEntity? =
        products.firstOrNull { it.barcode == barcode }

    override suspend fun upsertAll(products: List<ProductEntity>) {
        val incoming = products.associateBy { it.uuid }
        val existing = this.products.filter { it.uuid !in incoming }.toMutableList()
        existing.addAll(incoming.values)
        this.products.clear()
        this.products.addAll(existing)
    }

    override suspend fun deleteAll() { products.clear() }
}

class FakeCategoryDao : CategoryDao {
    val categories = mutableListOf<CategoryEntity>()

    override suspend fun getAll(): List<CategoryEntity> = categories.toList()

    override suspend fun upsertAll(categories: List<CategoryEntity>) {
        val incoming = categories.associateBy { it.uuid }
        val existing = this.categories.filter { it.uuid !in incoming }.toMutableList()
        existing.addAll(incoming.values)
        this.categories.clear()
        this.categories.addAll(existing)
    }

    override suspend fun deleteAll() { categories.clear() }
}

class FakeSaleDao : SaleDao {
    val sales = mutableListOf<SaleEntity>()
    val items = mutableListOf<SaleItemEntity>()

    override suspend fun getAll(): List<SaleEntity> = sales.toList()
    override suspend fun getActive(): List<SaleEntity> = sales.filter { it.status in listOf("held", "served") }
    override suspend fun findByUuid(uuid: String): SaleEntity? = sales.firstOrNull { it.uuid == uuid }
    override suspend fun getItemsForSale(saleUuid: String): List<SaleItemEntity> = items.filter { it.saleUuid == saleUuid }

    override suspend fun upsertSales(sales: List<SaleEntity>) {
        val incoming = sales.associateBy { it.uuid }
        val existing = this.sales.filter { it.uuid !in incoming }.toMutableList()
        existing.addAll(incoming.values)
        this.sales.clear()
        this.sales.addAll(existing)
    }

    override suspend fun upsertItems(items: List<SaleItemEntity>) {
        this.items.addAll(items)
    }

    override suspend fun upsertSalesWithItems(sales: List<SaleEntity>, items: List<SaleItemEntity>) {
        upsertSales(sales)
        upsertItems(items)
    }

    override suspend fun deleteAll() { sales.clear() }
    override suspend fun deleteAllItems() { items.clear() }
}

// ─────────────────────────────────────────────────────────────────────────────
// No-op SyncScheduler
// ─────────────────────────────────────────────────────────────────────────────

class FakeSyncScheduler : SyncScheduler {
    var scheduleCallCount = 0
    override fun scheduleSync() { scheduleCallCount++ }
}

// ─────────────────────────────────────────────────────────────────────────────
// Fake CartRepository
// ─────────────────────────────────────────────────────────────────────────────

class FakeCartRepository : id.rancak.app.domain.repository.CartRepository {
    override fun observeItems(): kotlinx.coroutines.flow.Flow<List<id.rancak.app.domain.model.CartItem>> =
        kotlinx.coroutines.flow.flowOf(emptyList())

    override suspend fun addOrIncrement(
        product: id.rancak.app.domain.model.Product,
        variantUuid: String?,
        variantName: String?
    ) {}

    override suspend fun updateQuantity(productUuid: String, variantUuid: String?, qty: Int) {}
    override suspend fun updateNote(productUuid: String, variantUuid: String?, note: String) {}
    override suspend fun removeItem(productUuid: String, variantUuid: String?) {}
    override suspend fun clearAll() {}
    override suspend fun replaceAll(items: List<id.rancak.app.domain.model.CartItem>) {}
}

// ─────────────────────────────────────────────────────────────────────────────
// Fake AdminRepository
// ─────────────────────────────────────────────────────────────────────────────

class FakeAdminRepository : id.rancak.app.domain.repository.AdminRepository {
    override suspend fun getSurcharges(): id.rancak.app.domain.model.Resource<List<id.rancak.app.domain.model.Surcharge>> =
        id.rancak.app.domain.model.Resource.Error("unused")

    override suspend fun getSurcharge(surchargeId: String): id.rancak.app.domain.model.Resource<id.rancak.app.domain.model.Surcharge> =
        id.rancak.app.domain.model.Resource.Error("unused")

    override suspend fun createSurcharge(
        orderType: String, name: String, amount: String, isPercentage: Boolean, maxAmount: String?, sortOrder: Int
    ): id.rancak.app.domain.model.Resource<id.rancak.app.domain.model.Surcharge> =
        id.rancak.app.domain.model.Resource.Error("unused")

    override suspend fun updateSurcharge(
        surchargeId: String, name: String?, amount: String?, isPercentage: Boolean?, maxAmount: String?, isActive: Boolean?, sortOrder: Int?
    ): id.rancak.app.domain.model.Resource<id.rancak.app.domain.model.Surcharge> =
        id.rancak.app.domain.model.Resource.Error("unused")

    override suspend fun deleteSurcharge(surchargeId: String): id.rancak.app.domain.model.Resource<Unit> =
        id.rancak.app.domain.model.Resource.Error("unused")

    override suspend fun getVouchers(isActive: Boolean?, page: Int, limit: Int): id.rancak.app.domain.model.Resource<List<id.rancak.app.domain.model.Voucher>> =
        id.rancak.app.domain.model.Resource.Error("unused")

    override suspend fun getVoucher(voucherId: String): id.rancak.app.domain.model.Resource<id.rancak.app.domain.model.Voucher> =
        id.rancak.app.domain.model.Resource.Error("unused")

    override suspend fun createVoucher(
        code: String, name: String, discountType: String, discountValue: String, validFrom: String,
        description: String?, maxDiscount: String?, minPurchase: String, usageLimit: Int?, validUntil: String?, isActive: Boolean
    ): id.rancak.app.domain.model.Resource<id.rancak.app.domain.model.Voucher> =
        id.rancak.app.domain.model.Resource.Error("unused")

    override suspend fun updateVoucher(
        voucherId: String, update: id.rancak.app.domain.repository.VoucherUpdate
    ): id.rancak.app.domain.model.Resource<id.rancak.app.domain.model.Voucher> =
        id.rancak.app.domain.model.Resource.Error("unused")

    override suspend fun deleteVoucher(voucherId: String): id.rancak.app.domain.model.Resource<Unit> =
        id.rancak.app.domain.model.Resource.Error("unused")

    override suspend fun getTaxConfigs(): id.rancak.app.domain.model.Resource<List<id.rancak.app.domain.model.TaxConfig>> =
        id.rancak.app.domain.model.Resource.Error("unused")

    override suspend fun getTaxConfig(configId: String): id.rancak.app.domain.model.Resource<id.rancak.app.domain.model.TaxConfig> =
        id.rancak.app.domain.model.Resource.Error("unused")

    override suspend fun createTaxConfig(
        name: String, rate: String, applyTo: String, sortOrder: Int
    ): id.rancak.app.domain.model.Resource<id.rancak.app.domain.model.TaxConfig> =
        id.rancak.app.domain.model.Resource.Error("unused")

    override suspend fun updateTaxConfig(
        configId: String, name: String?, rate: String?, applyTo: String?, sortOrder: Int?, isActive: Boolean?
    ): id.rancak.app.domain.model.Resource<id.rancak.app.domain.model.TaxConfig> =
        id.rancak.app.domain.model.Resource.Error("unused")

    override suspend fun deleteTaxConfig(configId: String): id.rancak.app.domain.model.Resource<Unit> =
        id.rancak.app.domain.model.Resource.Error("unused")

    override suspend fun getTable(tableId: String): id.rancak.app.domain.model.Resource<id.rancak.app.domain.model.Table> =
        id.rancak.app.domain.model.Resource.Error("unused")

    override suspend fun createTable(
        name: String, area: String?, capacity: Int, isActive: Boolean, sortOrder: Int
    ): id.rancak.app.domain.model.Resource<id.rancak.app.domain.model.Table> =
        id.rancak.app.domain.model.Resource.Error("unused")

    override suspend fun updateTable(
        tableId: String, name: String?, area: String?, capacity: Int?, status: String?, isActive: Boolean?, sortOrder: Int?
    ): id.rancak.app.domain.model.Resource<id.rancak.app.domain.model.Table> =
        id.rancak.app.domain.model.Resource.Error("unused")

    override suspend fun deleteTable(tableId: String): id.rancak.app.domain.model.Resource<Unit> =
        id.rancak.app.domain.model.Resource.Error("unused")

    override suspend fun getBundle(bundleId: String): id.rancak.app.domain.model.Resource<id.rancak.app.domain.model.Bundle> =
        id.rancak.app.domain.model.Resource.Error("unused")

    override suspend fun createBundle(
        name: String, price: String, items: List<id.rancak.app.domain.repository.BundleItemEntry>,
        description: String?, sku: String?, isActive: Boolean, sortOrder: Int
    ): id.rancak.app.domain.model.Resource<id.rancak.app.domain.model.Bundle> =
        id.rancak.app.domain.model.Resource.Error("unused")

    override suspend fun updateBundle(
        bundleId: String, update: id.rancak.app.domain.repository.BundleUpdate
    ): id.rancak.app.domain.model.Resource<id.rancak.app.domain.model.Bundle> =
        id.rancak.app.domain.model.Resource.Error("unused")

    override suspend fun deleteBundle(bundleId: String): id.rancak.app.domain.model.Resource<Unit> =
        id.rancak.app.domain.model.Resource.Error("unused")

    override suspend fun getModifiers(): id.rancak.app.domain.model.Resource<List<id.rancak.app.domain.model.Modifier>> =
        id.rancak.app.domain.model.Resource.Error("unused")

    override suspend fun getModifier(modifierId: String): id.rancak.app.domain.model.Resource<id.rancak.app.domain.model.Modifier> =
        id.rancak.app.domain.model.Resource.Error("unused")

    override suspend fun createModifier(name: String, sortOrder: Int, isActive: Boolean): id.rancak.app.domain.model.Resource<id.rancak.app.domain.model.Modifier> =
        id.rancak.app.domain.model.Resource.Error("unused")

    override suspend fun createProductModifier(
        productUuid: String, name: String, sortOrder: Int, isActive: Boolean
    ): id.rancak.app.domain.model.Resource<id.rancak.app.domain.model.Modifier> =
        id.rancak.app.domain.model.Resource.Error("unused")

    override suspend fun updateModifier(
        modifierId: String, name: String?, sortOrder: Int?, isActive: Boolean?
    ): id.rancak.app.domain.model.Resource<id.rancak.app.domain.model.Modifier> =
        id.rancak.app.domain.model.Resource.Error("unused")

    override suspend fun deleteModifier(modifierId: String): id.rancak.app.domain.model.Resource<Unit> =
        id.rancak.app.domain.model.Resource.Error("unused")

    override suspend fun getDiscountRules(): id.rancak.app.domain.model.Resource<List<id.rancak.app.domain.model.DiscountRule>> =
        id.rancak.app.domain.model.Resource.Error("unused")

    override suspend fun getDiscountRule(ruleId: String): id.rancak.app.domain.model.Resource<id.rancak.app.domain.model.DiscountRule> =
        id.rancak.app.domain.model.Resource.Error("unused")

    override suspend fun createDiscountRule(
        name: String, discountValue: Double, description: String?, ruleType: String, discountType: String,
        startTime: String?, endTime: String?, applicableDays: List<Int>?, minPurchaseAmount: Double?,
        priority: Int, stackable: Boolean, maxDiscount: Double?, isActive: Boolean
    ): id.rancak.app.domain.model.Resource<id.rancak.app.domain.model.DiscountRule> =
        id.rancak.app.domain.model.Resource.Error("unused")

    override suspend fun updateDiscountRule(
        ruleId: String, update: id.rancak.app.domain.repository.DiscountRuleUpdate
    ): id.rancak.app.domain.model.Resource<id.rancak.app.domain.model.DiscountRule> =
        id.rancak.app.domain.model.Resource.Error("unused")

    override suspend fun deleteDiscountRule(ruleId: String): id.rancak.app.domain.model.Resource<Unit> =
        id.rancak.app.domain.model.Resource.Error("unused")

    override suspend fun getVariantGroups(productId: String): id.rancak.app.domain.model.Resource<List<id.rancak.app.domain.model.VariantGroup>> =
        id.rancak.app.domain.model.Resource.Error("unused")

    override suspend fun createVariantGroup(
        productId: String, name: String, isRequired: Boolean, sortOrder: Int
    ): id.rancak.app.domain.model.Resource<id.rancak.app.domain.model.VariantGroup> =
        id.rancak.app.domain.model.Resource.Error("unused")

    override suspend fun updateVariantGroup(
        productId: String, groupId: String, name: String?, isRequired: Boolean?, sortOrder: Int?
    ): id.rancak.app.domain.model.Resource<id.rancak.app.domain.model.VariantGroup> =
        id.rancak.app.domain.model.Resource.Error("unused")

    override suspend fun deleteVariantGroup(productId: String, groupId: String): id.rancak.app.domain.model.Resource<Unit> =
        id.rancak.app.domain.model.Resource.Error("unused")

    override suspend fun getVariants(productId: String, groupId: String): id.rancak.app.domain.model.Resource<List<id.rancak.app.domain.model.Variant>> =
        id.rancak.app.domain.model.Resource.Error("unused")

    override suspend fun createVariant(
        productId: String, groupId: String, name: String, priceAdjustment: String, isDefault: Boolean, isActive: Boolean, sortOrder: Int
    ): id.rancak.app.domain.model.Resource<id.rancak.app.domain.model.Variant> =
        id.rancak.app.domain.model.Resource.Error("unused")

    override suspend fun updateVariant(
        productId: String, groupId: String, variantId: String, name: String?, priceAdjustment: String?, isDefault: Boolean?, isActive: Boolean?, sortOrder: Int?
    ): id.rancak.app.domain.model.Resource<id.rancak.app.domain.model.Variant> =
        id.rancak.app.domain.model.Resource.Error("unused")

    override suspend fun deleteVariant(productId: String, groupId: String, variantId: String): id.rancak.app.domain.model.Resource<Unit> =
        id.rancak.app.domain.model.Resource.Error("unused")

    override suspend fun adjustStock(
        productId: String, adjustmentType: String, quantity: Double, note: String?
    ): id.rancak.app.domain.model.Resource<id.rancak.app.domain.repository.StockAdjustmentResult> =
        id.rancak.app.domain.model.Resource.Error("unused")

    override suspend fun getProductBatches(productId: String): id.rancak.app.domain.model.Resource<List<id.rancak.app.domain.model.ProductBatch>> =
        id.rancak.app.domain.model.Resource.Error("unused")

    override suspend fun createProductBatch(
        productId: String, quantity: Double, expiryDate: String?, costPrice: Long?, batchNumber: String?, note: String?, receivedAt: String?
    ): id.rancak.app.domain.model.Resource<id.rancak.app.domain.model.ProductBatch> =
        id.rancak.app.domain.model.Resource.Error("unused")

    override suspend fun createProduct(
        name: String, price: Long, description: String?, sku: String?, barcode: String?, categoryUuid: String?, unit: String?, stock: Double, hasExpiry: Boolean, isActive: Boolean
    ): id.rancak.app.domain.model.Resource<id.rancak.app.domain.model.Product> =
        id.rancak.app.domain.model.Resource.Error("unused")

    override suspend fun updateProduct(
        productId: String, name: String?, price: Long?, description: String?, sku: String?, barcode: String?, categoryUuid: String?, unit: String?, isActive: Boolean?
    ): id.rancak.app.domain.model.Resource<id.rancak.app.domain.model.Product> =
        id.rancak.app.domain.model.Resource.Error("unused")

    override suspend fun deleteProduct(productId: String): id.rancak.app.domain.model.Resource<Unit> =
        id.rancak.app.domain.model.Resource.Error("unused")

    override suspend fun createCategory(name: String, description: String?): id.rancak.app.domain.model.Resource<id.rancak.app.domain.model.Category> =
        id.rancak.app.domain.model.Resource.Error("unused")

    override suspend fun updateCategory(categoryId: String, name: String?, description: String?): id.rancak.app.domain.model.Resource<id.rancak.app.domain.model.Category> =
        id.rancak.app.domain.model.Resource.Error("unused")

    override suspend fun deleteCategory(categoryId: String): id.rancak.app.domain.model.Resource<Unit> =
        id.rancak.app.domain.model.Resource.Error("unused")

    override suspend fun updateReceiptSettings(update: id.rancak.app.domain.repository.ReceiptSettingsUpdate): id.rancak.app.domain.model.Resource<id.rancak.app.domain.model.ReceiptSettingsConfig> =
        id.rancak.app.domain.model.Resource.Error("unused")
}

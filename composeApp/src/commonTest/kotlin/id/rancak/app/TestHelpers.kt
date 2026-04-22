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

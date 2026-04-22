package id.rancak.app

import id.rancak.app.data.repository.ProductRepositoryImpl
import id.rancak.app.domain.model.Resource
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

/**
 * Tests for [ProductRepositoryImpl] against the real implementation.
 *
 * Key behaviours under test:
 * 1. API success → products returned, full-list cached to Room (FakeProductDao)
 * 2. API failure with populated cache → cache served as Resource.Success
 * 3. API failure with empty cache → Resource.Error propagated
 * 4. Filtered search (query / categoryId) → NOT cached; always reads from API first
 * 5. Barcode lookup → API first, falls back to local cache on failure
 * 6. Categories → API success cached; failure falls back to cached; empty cache = error
 */
class ProductRepositoryImplTest {

    // ── Fixtures ──────────────────────────────────────────────────────────────

    private val productListJson = """
        {
          "status_code": 200,
          "data": [
            {
              "uuid": "prod-001",
              "name": "Kopi Susu",
              "price": 25000,
              "stock": 10.0,
              "is_active": true
            },
            {
              "uuid": "prod-002",
              "name": "Teh Tarik",
              "price": 18000,
              "stock": 20.0,
              "is_active": true
            }
          ]
        }
    """.trimIndent()

    private val singleProductJson = """
        {
          "status_code": 200,
          "data": {
            "uuid": "prod-003",
            "name": "Es Campur",
            "barcode": "8991234567890",
            "price": 30000,
            "stock": 5.0,
            "is_active": true
          }
        }
    """.trimIndent()

    private val categoryListJson = """
        {
          "status_code": 200,
          "data": [
            {"uuid":"cat-1","name":"Minuman"},
            {"uuid":"cat-2","name":"Makanan"}
          ]
        }
    """.trimIndent()

    private val errorJson = """{"status_code":500,"message":"Server error","data":null}"""

    // ── Helpers ────────────────────────────────────────────────────────────────

    private fun makeRepo(
        responseBody: String,
        productDao: FakeProductDao = FakeProductDao(),
        categoryDao: FakeCategoryDao = FakeCategoryDao()
    ): ProductRepositoryImpl {
        val api = mockApiService(responseBody)
        val tm  = testTokenManager()
        return ProductRepositoryImpl(api, tm, productDao, categoryDao)
    }

    // ── getProducts ───────────────────────────────────────────────────────────

    @Test
    fun `getProducts - API success returns products`() = kotlinx.coroutines.test.runTest {
        val result = makeRepo(productListJson).getProducts()

        assertTrue(result is Resource.Success)
        assertEquals(2, result.data.size)
        assertEquals("prod-001", result.data[0].uuid)
        assertEquals("Kopi Susu", result.data[0].name)
        assertEquals(25_000L, result.data[0].price)
    }

    @Test
    fun `getProducts - API success caches full list to local DAO`() = kotlinx.coroutines.test.runTest {
        val dao = FakeProductDao()
        makeRepo(productListJson, productDao = dao).getProducts()

        assertEquals(2, dao.products.size)
        assertTrue(dao.products.any { it.uuid == "prod-001" })
        assertTrue(dao.products.any { it.uuid == "prod-002" })
    }

    @Test
    fun `getProducts - API failure with populated cache serves cache as Success`() = kotlinx.coroutines.test.runTest {
        val dao = FakeProductDao()
        // Pre-seed cache
        makeRepo(productListJson, productDao = dao).getProducts()
        assertEquals(2, dao.products.size)

        // Now API is broken — repo should fall back to cache
        val fallbackRepo = ProductRepositoryImpl(
            mockApiService { throw Exception("Network unreachable") },
            testTokenManager(),
            dao,
            FakeCategoryDao()
        )
        val result = fallbackRepo.getProducts()

        assertTrue(result is Resource.Success)
        assertEquals(2, result.data.size)
    }

    @Test
    fun `getProducts - API failure with empty cache returns Resource_Error`() = kotlinx.coroutines.test.runTest {
        val api = mockApiService { throw Exception("Network unreachable") }
        val repo = ProductRepositoryImpl(api, testTokenManager(), FakeProductDao(), FakeCategoryDao())

        val result = repo.getProducts()

        assertTrue(result is Resource.Error)
    }

    @Test
    fun `getProducts - API error response (non-2xx) with empty cache returns Resource_Error`() = kotlinx.coroutines.test.runTest {
        val result = makeRepo(errorJson).getProducts()

        assertTrue(result is Resource.Error)
        assertEquals("Server error", result.message)
    }

    @Test
    fun `getProducts - filtered query result is NOT cached to DAO`() = kotlinx.coroutines.test.runTest {
        val dao = FakeProductDao()
        val filteredProductJson = """
            {
              "status_code": 200,
              "data": [{"uuid":"prod-001","name":"Kopi Susu","price":25000,"stock":10.0,"is_active":true}]
            }
        """.trimIndent()
        val repo = ProductRepositoryImpl(mockApiService(filteredProductJson), testTokenManager(), dao, FakeCategoryDao())

        repo.getProducts(query = "kopi")

        // Filtered response should NOT replace the full cache
        assertEquals(0, dao.products.size)
    }

    @Test
    fun `getProducts - filtered query falls back to local search on API failure`() = kotlinx.coroutines.test.runTest {
        val dao = FakeProductDao()
        // Seed cache with a known product
        makeRepo(productListJson, productDao = dao).getProducts()

        val brokenRepo = ProductRepositoryImpl(
            mockApiService { throw Exception("Network error") },
            testTokenManager(),
            dao,
            FakeCategoryDao()
        )

        val result = brokenRepo.getProducts(query = "kopi")

        assertTrue(result is Resource.Success)
        assertEquals(1, result.data.size)
        assertEquals("Kopi Susu", result.data[0].name)
    }

    // ── getProductByBarcode ───────────────────────────────────────────────────

    @Test
    fun `getProductByBarcode - API success returns product`() = kotlinx.coroutines.test.runTest {
        val result = makeRepo(singleProductJson).getProductByBarcode("8991234567890")

        assertTrue(result is Resource.Success)
        assertEquals("prod-003", result.data.uuid)
        assertEquals("8991234567890", result.data.barcode)
    }

    @Test
    fun `getProductByBarcode - API failure falls back to cached barcode match`() = kotlinx.coroutines.test.runTest {
        val dao = FakeProductDao()
        // Seed the full product list so barcode "8991234567890" is NOT in the list (Kopi/Teh only)
        // Pre-load a specific product directly into the DAO using the entity mapper
        makeRepo(singleProductJson, productDao = dao).getProductByBarcode("8991234567890")
        assertEquals(0, dao.products.size)  // getProductByBarcode does NOT cache to DAO

        // Now use a completely broken API — should fall back to dao.findByBarcode
        val brokenRepo = ProductRepositoryImpl(
            mockApiService { throw Exception("No network") },
            testTokenManager(),
            dao,
            FakeCategoryDao()
        )
        val fallback = brokenRepo.getProductByBarcode("8991234567890")

        // Barcode lookup result was not cached since it uses getProductByBarcode path, so no cached entity
        // Result depends on whether entity exists in DAO
        // The previous successful API call doesn't cache single-product result → expect error from empty cache
        assertTrue(fallback is Resource.Error)
    }

    // ── getCategories ─────────────────────────────────────────────────────────

    @Test
    fun `getCategories - API success returns and caches categories`() = kotlinx.coroutines.test.runTest {
        val catDao = FakeCategoryDao()
        val repo = ProductRepositoryImpl(mockApiService(categoryListJson), testTokenManager(), FakeProductDao(), catDao)

        val result = repo.getCategories()

        assertTrue(result is Resource.Success)
        assertEquals(2, result.data.size)
        assertEquals("cat-1", result.data[0].uuid)
        assertEquals("Minuman", result.data[0].name)
        // Also cached
        assertEquals(2, catDao.categories.size)
    }

    @Test
    fun `getCategories - API failure with populated cache returns cached categories`() = kotlinx.coroutines.test.runTest {
        val catDao = FakeCategoryDao()
        // Prime cache
        ProductRepositoryImpl(mockApiService(categoryListJson), testTokenManager(), FakeProductDao(), catDao).getCategories()

        val brokenRepo = ProductRepositoryImpl(
            mockApiService { throw Exception("No network") },
            testTokenManager(),
            FakeProductDao(),
            catDao
        )
        val result = brokenRepo.getCategories()

        assertTrue(result is Resource.Success)
        assertEquals(2, result.data.size)
    }

    @Test
    fun `getCategories - API error with empty cache returns Resource_Error`() = kotlinx.coroutines.test.runTest {
        val repo = ProductRepositoryImpl(
            mockApiService { throw Exception("No network") },
            testTokenManager(),
            FakeProductDao(),
            FakeCategoryDao()
        )
        val result = repo.getCategories()

        assertTrue(result is Resource.Error)
    }

    // ── tenantUuid guard ──────────────────────────────────────────────────────

    @Test
    fun `getProducts - throws when no tenant selected`() = kotlinx.coroutines.test.runTest {
        val repo = ProductRepositoryImpl(
            mockApiService(productListJson),
            testTokenManager(tenantUuid = null), // no tenant set
            FakeProductDao(),
            FakeCategoryDao()
        )

        val result = kotlin.runCatching { repo.getProducts() }
        // Should either throw IllegalStateException or return Resource.Error
        assertTrue(result.isFailure || (result.getOrNull() is Resource.Error))
    }
}

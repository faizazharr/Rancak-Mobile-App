package id.rancak.app

import com.russhwolf.settings.MapSettings
import id.rancak.app.data.local.OfflineSaleQueue
import id.rancak.app.data.local.TokenManager
import id.rancak.app.data.repository.SaleRepositoryImpl
import id.rancak.app.domain.model.OrderType
import id.rancak.app.domain.model.PaymentMethod
import id.rancak.app.domain.model.Resource
import id.rancak.app.domain.repository.CartItem
import io.ktor.client.engine.mock.*
import io.ktor.http.*
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

/**
 * Tests for [SaleRepositoryImpl] against the real implementation.
 *
 * Key behaviours under test:
 * 1. createSale success → Resource.Success with mapped Sale domain model
 * 2. createSale 409 (idempotency duplicate) WITH data → treated as success
 * 3. createSale 409 WITHOUT data → Resource.Error with friendly message
 * 4. createSale network exception (non-QRIS) → enqueued in OfflineSaleQueue
 * 5. createSale QRIS + network exception → NOT enqueued, returns error
 * 6. createSale schedules sync after offline enqueue
 * 7. getSales success → Resource.Success
 * 8. getSales failure → Resource.Error
 */
class SaleRepositoryImplTest {

    // ── Fixtures ──────────────────────────────────────────────────────────────

    private val saleSuccessJson = """
        {
          "status_code": 200,
          "message": "Penjualan berhasil dibuat",
          "data": {
            "uuid": "sale-abc",
            "invoice_no": "INV-001",
            "status": "paid",
            "payment_method": "cash",
            "subtotal": 50000,
            "total": 50000,
            "paid_amount": 50000,
            "change_amount": 0,
            "order_type": "dine_in",
            "items": []
          }
        }
    """.trimIndent()

    private val sale409WithDataJson = """
        {
          "status_code": 409,
          "message": "Penjualan sudah ada",
          "data": {
            "uuid": "sale-dup",
            "invoice_no": "INV-002",
            "status": "paid",
            "payment_method": "cash",
            "subtotal": 30000,
            "total": 30000,
            "paid_amount": 30000,
            "change_amount": 0,
            "order_type": "dine_in",
            "items": []
          }
        }
    """.trimIndent()

    private val sale409NoDataJson = """
        {"status_code":409,"message":"Penjualan sudah ada","data":null}
    """.trimIndent()

    private val salesListJson = """
        {
          "status_code": 200,
          "data": [
            {
              "uuid": "sale-1",
              "invoice_no": "INV-001",
              "status": "paid",
              "subtotal": 25000,
              "total": 25000,
              "paid_amount": 25000,
              "change_amount": 0,
              "order_type": "dine_in",
              "items": []
            }
          ]
        }
    """.trimIndent()

    // ── Helpers ────────────────────────────────────────────────────────────────

    private fun makeRepo(
        responseBody: String,
        tokenManager: TokenManager = testTokenManager(),
        queue: OfflineSaleQueue = OfflineSaleQueue(MapSettings()),
        syncScheduler: FakeSyncScheduler = FakeSyncScheduler(),
        saleDao: FakeSaleDao = FakeSaleDao()
    ) = SaleRepositoryImpl(
        api            = mockApiService(responseBody),
        tokenManager   = tokenManager,
        offlineQueue   = queue,
        syncManager    = syncScheduler,
        saleDao        = saleDao
    )

    private fun defaultCartItems() = listOf(
        CartItem(productUuid = "prod-001", productName = "Kopi Susu", qty = 2, price = 25_000L)
    )

    private suspend fun SaleRepositoryImpl.createTestSale(
        paymentMethod: PaymentMethod = PaymentMethod.CASH,
        paidAmount: Long = 50_000L
    ) = createSale(
        items         = defaultCartItems(),
        paymentMethod = paymentMethod,
        paidAmount    = paidAmount,
        orderType     = OrderType.DINE_IN,
        tableUuid     = null,
        customerName  = null,
        note          = null,
        hold          = false
    )

    // ── createSale ────────────────────────────────────────────────────────────

    @Test
    fun `createSale - 200 success returns Resource_Success`() = kotlinx.coroutines.test.runTest {
        val result = makeRepo(saleSuccessJson).createTestSale()
        assertTrue(result is Resource.Success<*>)
        val sale = (result as Resource.Success<id.rancak.app.domain.model.Sale>).data
        assertEquals<String>("sale-abc", sale.uuid)
        assertEquals<String>("paid", sale.status.name.lowercase())
    }

    @Test
    fun `createSale - 409 with data is treated as success (idempotency)`() = kotlinx.coroutines.test.runTest {
        val result = makeRepo(sale409WithDataJson).createTestSale(paidAmount = 30_000L)

        assertTrue(result is Resource.Success, "Expected Success for 409 with data, got $result")
        assertEquals("sale-dup", (result as Resource.Success).data.uuid)
    }

    @Test
    fun `createSale - 409 without data returns Resource_Error`() = kotlinx.coroutines.test.runTest {
        val result = makeRepo(sale409NoDataJson).createTestSale(paidAmount = 30_000L)

        assertTrue(result is Resource.Error)
        assertTrue(result.message.isNotBlank())
    }

    @Test
    fun `createSale - network error enqueues offline and returns error message`() = kotlinx.coroutines.test.runTest {
        val queue = OfflineSaleQueue(MapSettings())
        val sync  = FakeSyncScheduler()
        val api   = mockApiService { throw Exception("UnknownHostException: Unable to resolve host") }
        val repo  = SaleRepositoryImpl(api, testTokenManager(), queue, sync, FakeSaleDao())

        val result = repo.createTestSale()
        assertTrue(result is Resource.Error)
        assertTrue(result.message.startsWith("Offline:"), "Expected Offline: prefix, got: ${result.message}")
        // Sale was queued
        assertEquals(1, queue.size)
        // Sync was scheduled
        assertEquals(1, sync.scheduleCallCount)
    }

    @Test
    fun `createSale - network error with QRIS is NOT queued offline`() = kotlinx.coroutines.test.runTest {
        val queue = OfflineSaleQueue(MapSettings())
        val api   = mockApiService { throw Exception("UnknownHostException: No network") }
        val repo  = SaleRepositoryImpl(api, testTokenManager(), queue, FakeSyncScheduler(), FakeSaleDao())

        val result = repo.createTestSale(paymentMethod = PaymentMethod.QRIS)

        assertTrue(result is Resource.Error)
        assertTrue(result.message.contains("QRIS") || result.message.contains("internet"),
            "Expected QRIS-related error message, got: ${result.message}")
        // QRIS sales must NOT be queued
        assertTrue(queue.isEmpty)
    }

    @Test
    fun `createSale - non-network exception is not queued`() = kotlinx.coroutines.test.runTest {
        val queue = OfflineSaleQueue(MapSettings())
        val api   = mockApiService { throw Exception("Serialization error or unexpected server response") }
        val repo  = SaleRepositoryImpl(api, testTokenManager(), queue, FakeSyncScheduler(), FakeSaleDao())

        val result = repo.createTestSale()
        assertTrue(result is Resource.Error)
        assertTrue(queue.isEmpty)
    }

    @Test
    fun `createSale - sends X-Idempotency-Key header`() = kotlinx.coroutines.test.runTest {
        var capturedIdempotencyKey: String? = null

        val api = mockApiService { request ->
            capturedIdempotencyKey = request.headers["X-Idempotency-Key"]
            respond(
                content = saleSuccessJson,
                status  = io.ktor.http.HttpStatusCode.OK,
                headers = io.ktor.http.headersOf(io.ktor.http.HttpHeaders.ContentType, "application/json")
            )
        }
        val repo = SaleRepositoryImpl(api, testTokenManager(), OfflineSaleQueue(MapSettings()), FakeSyncScheduler(), FakeSaleDao())

        repo.createTestSale()

        assertTrue(capturedIdempotencyKey != null, "X-Idempotency-Key header must be set")
        assertTrue(capturedIdempotencyKey!!.isNotBlank())
        // Should be a valid UUID (36 chars with hyphens)
        assertEquals(36, capturedIdempotencyKey!!.length)
    }

    // ── getSales ──────────────────────────────────────────────────────────────

    @Test
    fun `getSales - success returns list of sales`() = kotlinx.coroutines.test.runTest {
        val result = makeRepo(salesListJson).getSales()

        assertTrue(result is Resource.Success)
        assertEquals(1, result.data.size)
        assertEquals("sale-1", result.data[0].uuid)
        assertEquals("INV-001", result.data[0].invoiceNo)
    }

    @Test
    fun `getSales - API error returns Resource_Error`() = kotlinx.coroutines.test.runTest {
        val result = makeRepo("""{"status_code":500,"message":"Kesalahan server","data":null}""").getSales()

        assertTrue(result is Resource.Error)
        assertEquals("Kesalahan server", result.message)
    }

    @Test
    fun `getSales - network exception returns Resource_Error`() = kotlinx.coroutines.test.runTest {
        val api  = mockApiService { throw Exception("Network error") }
        val repo = SaleRepositoryImpl(api, testTokenManager(), OfflineSaleQueue(MapSettings()), FakeSyncScheduler(), FakeSaleDao())

        val result = repo.getSales()

        assertTrue(result is Resource.Error)
    }

    @Test
    fun `getSales - caches results to local SaleDao`() = kotlinx.coroutines.test.runTest {
        val saleDao = FakeSaleDao()
        val repo = SaleRepositoryImpl(
            mockApiService(salesListJson),
            testTokenManager(),
            OfflineSaleQueue(MapSettings()),
            FakeSyncScheduler(),
            saleDao
        )

        repo.getSales()

        assertFalse(saleDao.sales.isEmpty(), "Sales should be cached in local DAO after API success")
        assertEquals("sale-1", saleDao.sales.first().uuid)
    }
}

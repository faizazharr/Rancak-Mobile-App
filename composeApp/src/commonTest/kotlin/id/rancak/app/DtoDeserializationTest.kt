package id.rancak.app

import id.rancak.app.data.remote.dto.ApiResponse
import id.rancak.app.data.remote.dto.operations.PaymentMethodReportDto
import id.rancak.app.data.remote.dto.operations.ShiftSummaryDto
import id.rancak.app.data.remote.dto.sale.SaleDto
import id.rancak.app.data.remote.dto.auth.MyTenantDto
import kotlinx.serialization.json.Json
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue

/**
 * Verifies that every DTO correctly deserialises real backend JSON payloads.
 * These tests catch @SerialName mismatches before they reach the device.
 */
class DtoDeserializationTest {

    private val json = Json {
        ignoreUnknownKeys = true
        isLenient = true
        coerceInputValues = true
    }

    // ── ApiResponse envelope ──────────────────────────────────────────────────

    @Test
    fun `ApiResponse envelope - success case`() {
        val raw = """{"status_code":200,"message":"Success","data":{"name":"Test"}}"""

        @kotlinx.serialization.Serializable
        data class Simple(val name: String)

        val response = json.decodeFromString<ApiResponse<Simple>>(raw)
        assertEquals(200, response.statusCode)
        assertEquals("Success", response.message)
        assertEquals("Test", response.data?.name)
        assertTrue(response.isSuccess)
    }

    @Test
    fun `ApiResponse envelope - error case returns isSuccess=false`() {
        val raw = """{"status_code":422,"message":"Stok tidak cukup","data":null}"""

        @kotlinx.serialization.Serializable
        data class Empty(val dummy: String? = null)

        val response = json.decodeFromString<ApiResponse<Empty>>(raw)
        assertEquals(422, response.statusCode)
        assertEquals("Stok tidak cukup", response.message)
        assertNull(response.data)
        assertFalse(response.isSuccess)
    }

    @Test
    fun `ApiResponse isSuccess - 200 through 299 are success`() {
        for (code in 200..299) {
            val r = ApiResponse<Unit>(statusCode = code)
            assertTrue(r.isSuccess, "Expected isSuccess=true for status_code=$code")
        }
    }

    @Test
    fun `ApiResponse isSuccess - 400 and above are not success`() {
        for (code in listOf(400, 401, 402, 404, 409, 422, 500)) {
            val r = ApiResponse<Unit>(statusCode = code)
            assertFalse(r.isSuccess, "Expected isSuccess=false for status_code=$code")
        }
    }

    // ── PaymentMethodReportDto ─────────────────────────────────────────────────

    @Test
    fun `PaymentMethodReportDto deserialises payment_method field correctly`() {
        val raw = """{"payment_method":"cash","transaction_count":5,"total":250000}"""
        val dto = json.decodeFromString<PaymentMethodReportDto>(raw)
        assertEquals("cash", dto.method)
        assertEquals(5, dto.count)
        assertEquals(250000L, dto.total)
    }

    @Test
    fun `PaymentMethodReportDto handles missing transaction_count with default`() {
        val raw = """{"payment_method":"qris","total":150000}"""
        val dto = json.decodeFromString<PaymentMethodReportDto>(raw)
        assertEquals("qris", dto.method)
        assertEquals(0, dto.count)
    }

    // ── ShiftSummaryDto ────────────────────────────────────────────────────────

    @Test
    fun `ShiftSummaryDto deserialises payment_breakdown correctly`() {
        val raw = """
            {
              "uuid": "shift-123",
              "status": "closed",
              "opening_cash": "500000",
              "total_transactions": 12,
              "void_transactions": 1,
              "gross_total": 3000000,
              "total_discount": 50000,
              "total_tax": 100000,
              "net_total": 3050000,
              "total_expenses": 75000,
              "total_cash_in": 0,
              "payment_breakdown": [
                {"payment_method":"cash","transaction_count":8,"total":2000000},
                {"payment_method":"qris","transaction_count":4,"total":1000000}
              ]
            }
        """.trimIndent()
        val dto = json.decodeFromString<ShiftSummaryDto>(raw)
        assertEquals("shift-123", dto.uuid)
        assertEquals("500000", dto.openingCash)
        assertEquals(12, dto.totalTransactions)
        assertEquals(1, dto.voidTransactions)
        assertEquals(3000000L, dto.grossTotal)
        assertEquals(50000L, dto.totalDiscount)
        assertEquals(100000L, dto.totalTax)
        assertEquals(3050000L, dto.netTotal)
        assertEquals(2, dto.payments.size)
        assertEquals("cash", dto.payments[0].method)
        assertEquals("qris", dto.payments[1].method)
        assertEquals(2000000L, dto.payments[0].total)
    }

    @Test
    fun `ShiftSummaryDto falls back to payment_summary when payment_breakdown absent`() {
        val raw = """
            {
              "uuid": "shift-old",
              "status": "open",
              "opening_cash": "100000",
              "payment_summary": [
                {"payment_method":"cash","total":500000}
              ]
            }
        """.trimIndent()
        val dto = json.decodeFromString<ShiftSummaryDto>(raw)
        assertEquals(1, dto.payments.size)
        assertEquals("cash", dto.payments[0].method)
    }

    // ── SaleDto ────────────────────────────────────────────────────────────────

    @Test
    fun `SaleDto deserialises basic fields`() {
        val raw = """
            {
              "uuid": "sale-abc",
              "invoice_no": "INV-20260422-000001",
              "status": "paid",
              "payment_method": "cash",
              "subtotal": 75000,
              "total": 75000,
              "paid_amount": 100000,
              "change_amount": 25000,
              "order_type": "dine_in",
              "items": []
            }
        """.trimIndent()
        val dto = json.decodeFromString<SaleDto>(raw)
        assertEquals("sale-abc", dto.uuid)
        assertEquals("INV-20260422-000001", dto.invoiceNo)
        assertEquals("paid", dto.status)
        assertEquals(75000L, dto.subtotal)
        assertEquals(25000L, dto.changeAmount)
    }

    // ── MyTenantDto ────────────────────────────────────────────────────────────

    @Test
    fun `MyTenantDto deserialises tenant fields`() {
        val raw = """
            {
              "uuid": "tenant-001",
              "name": "Warung Rancak",
              "subscription_status": "active"
            }
        """.trimIndent()
        val dto = json.decodeFromString<MyTenantDto>(raw)
        assertEquals("tenant-001", dto.uuid)
        assertEquals("Warung Rancak", dto.name)
        assertEquals("active", dto.subscriptionStatus)
    }
}

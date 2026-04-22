package id.rancak.app

import com.russhwolf.settings.MapSettings
import id.rancak.app.data.local.OfflineSaleQueue
import id.rancak.app.data.local.PendingSale
import id.rancak.app.data.local.PendingSaleItem
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

/**
 * Tests for [OfflineSaleQueue] — the persistent FIFO queue that stores
 * sales created while the device is offline.
 *
 * Uses [MapSettings] (in-memory, no Android context required) so these
 * tests run in commonTest on all targets.
 */
class OfflineSaleQueueTest {

    private lateinit var queue: OfflineSaleQueue

    @BeforeTest
    fun setUp() {
        queue = OfflineSaleQueue(MapSettings())
    }

    private var saleCounter = 0

    private fun fakeSale(
        key: String = "idem-key-${++saleCounter}",
        paymentMethod: String = "cash",
        paidAmount: Long = 50_000L
    ) = PendingSale(
        idempotencyKey  = key,
        tenantUuid      = "tenant-001",
        items           = listOf(PendingSaleItem("prod-001", qty = 2)),
        paymentMethod   = paymentMethod,
        paidAmount      = paidAmount,
        orderType       = "dine_in",
        deviceCreatedAt = "2026-04-22T10:00:00Z",
        deviceId        = "device-abc",
        enqueuedAt      = 1_000L
    )

    // ── isEmpty / size ─────────────────────────────────────────────────────────

    @Test
    fun `queue starts empty`() {
        assertTrue(queue.isEmpty)
        assertEquals(0, queue.size)
    }

    // ── enqueue ───────────────────────────────────────────────────────────────

    @Test
    fun `enqueue adds item to queue`() {
        queue.enqueue(fakeSale("key-1"))
        assertFalse(queue.isEmpty)
        assertEquals(1, queue.size)
    }

    @Test
    fun `enqueue preserves insertion order (FIFO)`() {
        queue.enqueue(fakeSale("first"))
        queue.enqueue(fakeSale("second"))
        queue.enqueue(fakeSale("third"))

        val all = queue.getAll()
        assertEquals(3, all.size)
        assertEquals("first", all[0].idempotencyKey)
        assertEquals("second", all[1].idempotencyKey)
        assertEquals("third", all[2].idempotencyKey)
    }

    @Test
    fun `enqueue persists data across queue instances (same settings)`() {
        val settings = MapSettings()
        val q1 = OfflineSaleQueue(settings)
        q1.enqueue(fakeSale("persisted-key"))

        val q2 = OfflineSaleQueue(settings)
        assertEquals(1, q2.size)
        assertEquals("persisted-key", q2.getAll()[0].idempotencyKey)
    }

    // ── remove ────────────────────────────────────────────────────────────────

    @Test
    fun `remove deletes the sale with matching idempotency key`() {
        queue.enqueue(fakeSale("key-a"))
        queue.enqueue(fakeSale("key-b"))

        queue.remove("key-a")

        assertEquals(1, queue.size)
        assertEquals("key-b", queue.getAll()[0].idempotencyKey)
    }

    @Test
    fun `remove is a no-op when key not found`() {
        queue.enqueue(fakeSale("only-key"))
        queue.remove("non-existent")
        assertEquals(1, queue.size)
    }

    // ── clear ─────────────────────────────────────────────────────────────────

    @Test
    fun `clear empties the queue`() {
        queue.enqueue(fakeSale("x"))
        queue.enqueue(fakeSale("y"))
        queue.clear()
        assertTrue(queue.isEmpty)
        assertEquals(0, queue.size)
    }

    // ── round-trip serialisation ───────────────────────────────────────────────

    @Test
    fun `sale fields survive serialisation round-trip`() {
        val original = fakeSale(
            key           = "rt-key",
            paymentMethod = "qris",
            paidAmount    = 99_000L
        ).copy(
            customerName = "Budi Santoso",
            note         = "Tanpa cabai",
            discount     = 5_000L,
            tax          = 2_000L
        )
        queue.enqueue(original)
        val restored = queue.getAll().first()

        assertEquals("rt-key",         restored.idempotencyKey)
        assertEquals("tenant-001",     restored.tenantUuid)
        assertEquals("qris",           restored.paymentMethod)
        assertEquals(99_000L,          restored.paidAmount)
        assertEquals("Budi Santoso",   restored.customerName)
        assertEquals("Tanpa cabai",    restored.note)
        assertEquals(5_000L,           restored.discount)
        assertEquals(2_000L,           restored.tax)
        assertEquals(1,                restored.items.size)
        assertEquals("prod-001",       restored.items[0].productUuid)
        assertEquals(2,                restored.items[0].qty)
    }
}

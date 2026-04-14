package id.rancak.app.data.local

import id.rancak.app.data.remote.dto.sale.BatchSaleItem
import id.rancak.app.data.remote.dto.sale.SaleItemRequest
import kotlinx.serialization.Serializable

/**
 * Represents a sale that was created offline and is waiting to be synced
 * to the backend once network is available.
 *
 * The [idempotencyKey] ensures the server won't create duplicate entries
 * even if the sync is triggered multiple times.
 */
@Serializable
data class PendingSale(
    /** UUID generated at creation time — used as idempotency key on the server. */
    val idempotencyKey: String,
    val tenantUuid: String,
    val items: List<PendingSaleItem>,
    val paymentMethod: String,
    val paidAmount: Long,
    val orderType: String,
    val tableUuid: String? = null,
    val customerName: String? = null,
    /** Jumlah tamu. Default 1. */
    val pax: Int = 1,
    val note: String? = null,
    val hold: Boolean = false,
    // ── Komponen biaya ────────────────────────────────────────────────────
    val discount: Long = 0,
    val tax: Long = 0,
    val adminFee: Long = 0,
    val deliveryFee: Long = 0,
    val tip: Long = 0,
    val voucherCode: String? = null,
    /** ISO-8601 timestamp of when the sale was created on the device. */
    val deviceCreatedAt: String,
    /** Unique identifier of the device (e.g. Android ID or UUID stored in settings). */
    val deviceId: String,
    /** Epoch millis — used to order pending sales chronologically during batch sync. */
    val enqueuedAt: Long = 0L
)

@Serializable
data class PendingSaleItem(
    val productUuid: String,
    val qty: Int,
    val variantUuid: String? = null,
    val note: String? = null
)

/**
 * Converts a [PendingSale] to the DTO format required by the batch sales API.
 * Shared between Android (SyncWorker) and iOS (IosSyncRunner).
 */
fun PendingSale.toBatchItem() = BatchSaleItem(
    idempotencyKey  = idempotencyKey,
    deviceCreatedAt = deviceCreatedAt,
    deviceId        = deviceId,
    items = items.map { item ->
        SaleItemRequest(
            productUuid = item.productUuid,
            qty         = item.qty,
            variantUuid = item.variantUuid,
            note        = item.note
        )
    },
    paymentMethod = paymentMethod,
    paidAmount    = paidAmount,
    orderType     = orderType,
    customerName  = customerName,
    pax           = pax.takeIf { it > 1 },
    note          = note,
    hold          = hold.takeIf { it },
    discount      = discount.takeIf { it > 0 },
    tax           = tax.takeIf { it > 0 },
    adminFee      = adminFee.takeIf { it > 0 },
    deliveryFee   = deliveryFee.takeIf { it > 0 },
    tip           = tip.takeIf { it > 0 },
    voucherCode   = voucherCode,
    tableUuid     = tableUuid
)

package id.rancak.app.data.printing

import id.rancak.app.domain.model.Sale
import id.rancak.app.domain.model.SaleStatus

/**
 * Maps a [Sale] domain model to [ReceiptData] for ESC/POS printing.
 *
 * [storeName], [storeAddress], [storePhone], and [cashierName] should come
 * from the tenant/settings; sensible defaults are provided so the receipt
 * is never blank.
 *
 * Note: [Sale.SaleItem.qty] is a String on the domain layer; we parse it
 * safely with [String.toIntOrNull], defaulting to 1 if parsing fails.
 */
fun Sale.toReceiptData(
    storeName: String = "Rancak",
    storeAddress: String? = null,
    storePhone: String? = null,
    cashierName: String? = null,
    tableName: String? = null,
    footerText: String? = null
): ReceiptData = ReceiptData(
    storeName     = storeName,
    storeAddress  = storeAddress,
    storePhone    = storePhone,
    invoiceNo     = invoiceNo ?: uuid.take(8).uppercase(),
    orderType     = orderType.value,
    tableName     = tableName,
    cashierName   = cashierName,
    createdAt     = createdAt.orEmpty(),
    items         = items.map { item ->
        ReceiptItem(
            name        = item.productName,
            variantName = item.variantName,
            qty         = item.qty.toIntOrNull() ?: 1,
            price       = item.price,
            subtotal    = item.subtotal,
            note        = item.note
        )
    },
    subtotal      = subtotal,
    discount      = discount,
    surcharge     = surcharge,
    tax           = tax,
    total         = total,
    paymentMethod = paymentMethod?.value,
    paidAmount    = paidAmount,
    changeAmount  = changeAmount,
    footerText    = footerText,
    isVoided      = status == SaleStatus.VOID || status == SaleStatus.CANCELLED
)

/**
 * Maps a [Sale] domain model to [KitchenTicketData] for KOT printing.
 *
 * KOT contains no prices — only item names, quantities, notes,
 * table/queue info, and order metadata for kitchen staff.
 */
fun Sale.toKitchenTicketData(
    storeName: String = "Rancak",
    cashierName: String? = null,
    tableName: String? = null,
    customerName: String? = null
): KitchenTicketData = KitchenTicketData(
    storeName     = storeName,
    invoiceNo     = invoiceNo ?: uuid.take(8).uppercase(),
    orderType     = orderType.value,
    tableName     = tableName,
    queueNumber   = queueNumber,
    customerName  = customerName,
    cashierName   = cashierName,
    createdAt     = createdAt.orEmpty(),
    items         = items.map { item ->
        KitchenTicketItem(
            name = if (!item.variantName.isNullOrBlank())
                "${item.productName} (${item.variantName})"
            else item.productName,
            qty  = item.qty.toIntOrNull() ?: 1,
            note = item.note
        )
    }
)

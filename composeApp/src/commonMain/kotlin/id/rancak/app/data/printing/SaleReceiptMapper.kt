package id.rancak.app.data.printing

import id.rancak.app.domain.model.Sale

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
    storeName: String = "Rancak POS",
    storeAddress: String? = null,
    storePhone: String? = null,
    cashierName: String? = null,
    tableName: String? = null
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
    changeAmount  = changeAmount
)

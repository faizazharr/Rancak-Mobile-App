package id.rancak.app.domain.model

// ── Stock opname ────────────────────────────────────────────────────────────

data class StockOpname(
    val uuid: String,
    val opnameNo: String,
    /** "draft" | "finalized" | "cancelled" */
    val status: String,
    val note: String? = null,
    val itemCount: Int = 0,
    val createdBy: String? = null,
    val finalizedBy: String? = null,
    val finalizedAt: String? = null,
    val createdAt: String
)

data class OpnameItem(
    val productUuid: String,
    val productName: String,
    val sku: String? = null,
    val systemStock: Double,
    val actualStock: Double,
    /** actual - system (negatif = shortage, positif = surplus). */
    val difference: Double,
    val note: String? = null
)

data class StockOpnameDetail(
    val opname: StockOpname,
    val items: List<OpnameItem>,
    val shortageCount: Int,
    val surplusCount: Int
)

/** Input untuk submit hasil hitung fisik per produk. */
data class OpnameItemEntry(
    val productUuid: String,
    val actualStock: Double,
    val note: String? = null
)

// ── Suppliers ───────────────────────────────────────────────────────────────

data class Supplier(
    val uuid: String,
    val name: String,
    val contactName: String? = null,
    val phone: String? = null,
    val email: String? = null,
    val address: String? = null,
    val npwp: String? = null,
    val notes: String? = null,
    val isActive: Boolean = true,
    val createdAt: String? = null,
    val updatedAt: String? = null
)

data class SupplierInput(
    val name: String,
    val contactName: String? = null,
    val phone: String? = null,
    val email: String? = null,
    val address: String? = null,
    val npwp: String? = null,
    val notes: String? = null
)

// ── Purchase orders ─────────────────────────────────────────────────────────

data class PurchaseOrderItem(
    val uuid: String,
    val productUuid: String,
    val productName: String,
    val qtyOrdered: Double,
    val qtyReceived: Double,
    val unitCost: Double,
    val subtotal: Double,
    val notes: String? = null
)

data class PurchaseOrder(
    val uuid: String,
    val poNumber: String,
    val supplierUuid: String? = null,
    val supplierName: String? = null,
    /** "draft" | "ordered" | "partial" | "received" | "cancelled" */
    val status: String,
    val orderDate: String,
    val expectedDate: String? = null,
    val receivedDate: String? = null,
    val subtotal: Double,
    val taxAmount: Double,
    val shippingCost: Double,
    val total: Double,
    val notes: String? = null,
    val createdBy: String? = null,
    val createdAt: String,
    val updatedAt: String,
    val items: List<PurchaseOrderItem> = emptyList()
)

data class POItemEntry(
    val productUuid: String,
    val qtyOrdered: Double,
    val unitCost: Double,
    val notes: String? = null
)

data class ReceiveItemEntry(
    val itemUuid: String,
    val qtyReceived: Double
)

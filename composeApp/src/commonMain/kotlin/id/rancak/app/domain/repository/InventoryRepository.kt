package id.rancak.app.domain.repository

import id.rancak.app.domain.model.OpnameItemEntry
import id.rancak.app.domain.model.POItemEntry
import id.rancak.app.domain.model.PurchaseOrder
import id.rancak.app.domain.model.PurchaseOrderItem
import id.rancak.app.domain.model.ReceiveItemEntry
import id.rancak.app.domain.model.Resource
import id.rancak.app.domain.model.StockOpname
import id.rancak.app.domain.model.StockOpnameDetail
import id.rancak.app.domain.model.Supplier
import id.rancak.app.domain.model.SupplierInput

/**
 * Manajemen inventori — stock opname, supplier, dan purchase order.
 */
interface InventoryRepository {

    // ── Stock opname ─────────────────────────────────────────────────────────
    suspend fun getStockOpnames(status: String? = null, page: Int = 1, limit: Int = 20): Resource<List<StockOpname>>
    suspend fun createStockOpname(note: String? = null): Resource<StockOpname>
    suspend fun getStockOpnameDetail(opnameId: String): Resource<StockOpnameDetail>
    suspend fun cancelStockOpname(opnameId: String): Resource<StockOpname>
    suspend fun upsertOpnameItems(opnameId: String, items: List<OpnameItemEntry>): Resource<StockOpnameDetail>
    suspend fun deleteOpnameItem(opnameId: String, productUuid: String): Resource<Unit>
    /** Kunci hasil opname dan terapkan adjustment stok. Tidak bisa dibatalkan. */
    suspend fun finalizeStockOpname(opnameId: String): Resource<StockOpnameDetail>

    // ── Suppliers ────────────────────────────────────────────────────────────
    suspend fun getSuppliers(isActive: Boolean? = null): Resource<List<Supplier>>
    suspend fun getSupplier(supplierId: String): Resource<Supplier>
    suspend fun createSupplier(input: SupplierInput): Resource<Supplier>
    suspend fun updateSupplier(
        supplierId: String,
        name: String? = null,
        contactName: String? = null,
        phone: String? = null,
        email: String? = null,
        address: String? = null,
        npwp: String? = null,
        notes: String? = null,
        isActive: Boolean? = null
    ): Resource<Supplier>
    suspend fun deleteSupplier(supplierId: String): Resource<Unit>

    // ── Purchase orders ──────────────────────────────────────────────────────
    suspend fun getPurchaseOrders(
        status: String? = null,
        supplierUuid: String? = null,
        page: Int = 1,
        limit: Int = 20
    ): Resource<List<PurchaseOrder>>
    suspend fun getPurchaseOrderDetail(poId: String): Resource<PurchaseOrder>
    suspend fun createPurchaseOrder(
        supplierUuid: String? = null,
        orderDate: String? = null,
        expectedDate: String? = null,
        taxAmount: Double = 0.0,
        shippingCost: Double = 0.0,
        notes: String? = null,
        items: List<POItemEntry> = emptyList()
    ): Resource<PurchaseOrder>
    suspend fun updatePurchaseOrder(
        poId: String,
        supplierUuid: String? = null,
        orderDate: String? = null,
        expectedDate: String? = null,
        taxAmount: Double? = null,
        shippingCost: Double? = null,
        notes: String? = null
    ): Resource<PurchaseOrder>
    suspend fun addPurchaseOrderItem(poId: String, item: POItemEntry): Resource<PurchaseOrderItem>
    suspend fun updatePurchaseOrderItem(
        poId: String,
        itemId: String,
        qtyOrdered: Double? = null,
        unitCost: Double? = null,
        notes: String? = null
    ): Resource<PurchaseOrderItem>
    suspend fun deletePurchaseOrderItem(poId: String, itemId: String): Resource<Unit>
    /** Kirim PO ke supplier — status menjadi `ordered`. */
    suspend fun sendPurchaseOrder(poId: String): Resource<PurchaseOrder>
    /** Terima barang sebagian/penuh — server update qty_received & stok. */
    suspend fun receivePurchaseOrder(
        poId: String,
        items: List<ReceiveItemEntry>,
        receivedDate: String? = null,
        notes: String? = null
    ): Resource<PurchaseOrder>
    suspend fun cancelPurchaseOrder(poId: String): Resource<PurchaseOrder>
}

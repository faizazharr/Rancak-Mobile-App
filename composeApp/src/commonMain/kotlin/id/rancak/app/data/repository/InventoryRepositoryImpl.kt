package id.rancak.app.data.repository

import id.rancak.app.data.local.TokenManager
import id.rancak.app.data.mapper.toDomain
import id.rancak.app.data.remote.api.RancakApiService
import id.rancak.app.data.remote.api.addPurchaseOrderItem
import id.rancak.app.data.remote.api.cancelPurchaseOrder
import id.rancak.app.data.remote.api.cancelStockOpname
import id.rancak.app.data.remote.api.createPurchaseOrder
import id.rancak.app.data.remote.api.createStockOpname
import id.rancak.app.data.remote.api.createSupplier
import id.rancak.app.data.remote.api.deleteOpnameItem
import id.rancak.app.data.remote.api.deletePurchaseOrderItem
import id.rancak.app.data.remote.api.deleteSupplier
import id.rancak.app.data.remote.api.finalizeStockOpname
import id.rancak.app.data.remote.api.getPurchaseOrderDetail
import id.rancak.app.data.remote.api.getPurchaseOrders
import id.rancak.app.data.remote.api.getStockOpnameDetail
import id.rancak.app.data.remote.api.getStockOpnames
import id.rancak.app.data.remote.api.getSupplier
import id.rancak.app.data.remote.api.getSuppliers
import id.rancak.app.data.remote.api.receivePurchaseOrder
import id.rancak.app.data.remote.api.sendPurchaseOrder
import id.rancak.app.data.remote.api.updatePurchaseOrder
import id.rancak.app.data.remote.api.updatePurchaseOrderItem
import id.rancak.app.data.remote.api.updateSupplier
import id.rancak.app.data.remote.api.upsertOpnameItems
import id.rancak.app.data.remote.dto.inventory.CreatePORequest
import id.rancak.app.data.remote.dto.inventory.CreateSupplierRequest
import id.rancak.app.data.remote.dto.inventory.POItemInput
import id.rancak.app.data.remote.dto.inventory.ReceivePORequest
import id.rancak.app.data.remote.dto.inventory.UpdatePOHeaderRequest
import id.rancak.app.data.remote.dto.inventory.UpdatePOItemRequest
import id.rancak.app.data.remote.dto.inventory.UpdateSupplierRequest
import id.rancak.app.data.remote.dto.inventory.UpsertOpnameItemEntry
import id.rancak.app.data.remote.dto.inventory.UpsertOpnameItemsRequest
import id.rancak.app.data.remote.dto.inventory.ReceiveItemEntry as ReceiveItemEntryDto
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
import id.rancak.app.domain.repository.InventoryRepository

class InventoryRepositoryImpl(
    private val api: RancakApiService,
    private val tokenManager: TokenManager
) : InventoryRepository {

    private val tenantUuid: String
        get() = tokenManager.tenantUuid ?: throw IllegalStateException("Tenant belum dipilih")

    // ── Stock opname ─────────────────────────────────────────────────────────

    override suspend fun getStockOpnames(status: String?, page: Int, limit: Int): Resource<List<StockOpname>> =
        safeList(
            block = { api.getStockOpnames(tenantUuid, status, page, limit) },
            errorMsg = "Gagal memuat opname"
        ) { it.toDomain() }

    override suspend fun createStockOpname(note: String?): Resource<StockOpname> = safe(
        block = { api.createStockOpname(tenantUuid, note) },
        map = { it.toDomain() },
        errorMsg = "Gagal membuat opname"
    )

    override suspend fun getStockOpnameDetail(opnameId: String): Resource<StockOpnameDetail> = safe(
        block = { api.getStockOpnameDetail(tenantUuid, opnameId) },
        map = { it.toDomain() },
        errorMsg = "Opname tidak ditemukan"
    )

    override suspend fun cancelStockOpname(opnameId: String): Resource<StockOpname> = safe(
        block = { api.cancelStockOpname(tenantUuid, opnameId) },
        map = { it.toDomain() },
        errorMsg = "Gagal membatalkan opname"
    )

    override suspend fun upsertOpnameItems(
        opnameId: String,
        items: List<OpnameItemEntry>
    ): Resource<StockOpnameDetail> = safe(
        block = {
            api.upsertOpnameItems(
                tenantUuid,
                opnameId,
                UpsertOpnameItemsRequest(
                    items.map { UpsertOpnameItemEntry(it.productUuid, it.actualStock, it.note) }
                )
            )
        },
        map = { it.toDomain() },
        errorMsg = "Gagal menyimpan item opname"
    )

    override suspend fun deleteOpnameItem(opnameId: String, productUuid: String): Resource<Unit> = safeUnit(
        block = { api.deleteOpnameItem(tenantUuid, opnameId, productUuid) },
        errorMsg = "Gagal menghapus item opname"
    )

    override suspend fun finalizeStockOpname(opnameId: String): Resource<StockOpnameDetail> = safe(
        block = { api.finalizeStockOpname(tenantUuid, opnameId) },
        map = { it.toDomain() },
        errorMsg = "Gagal mem-finalize opname"
    )

    // ── Suppliers ────────────────────────────────────────────────────────────

    override suspend fun getSuppliers(isActive: Boolean?): Resource<List<Supplier>> = safeList(
        block = { api.getSuppliers(tenantUuid, isActive) },
        errorMsg = "Gagal memuat supplier"
    ) { it.toDomain() }

    override suspend fun getSupplier(supplierId: String): Resource<Supplier> = safe(
        block = { api.getSupplier(tenantUuid, supplierId) },
        map = { it.toDomain() },
        errorMsg = "Supplier tidak ditemukan"
    )

    override suspend fun createSupplier(input: SupplierInput): Resource<Supplier> = safe(
        block = {
            api.createSupplier(
                tenantUuid,
                CreateSupplierRequest(
                    name = input.name,
                    contactName = input.contactName,
                    phone = input.phone,
                    email = input.email,
                    address = input.address,
                    npwp = input.npwp,
                    notes = input.notes
                )
            )
        },
        map = { it.toDomain() },
        errorMsg = "Gagal membuat supplier"
    )

    override suspend fun updateSupplier(
        supplierId: String,
        name: String?,
        contactName: String?,
        phone: String?,
        email: String?,
        address: String?,
        npwp: String?,
        notes: String?,
        isActive: Boolean?
    ): Resource<Supplier> = safe(
        block = {
            api.updateSupplier(
                tenantUuid,
                supplierId,
                UpdateSupplierRequest(name, contactName, phone, email, address, npwp, notes, isActive)
            )
        },
        map = { it.toDomain() },
        errorMsg = "Gagal memperbarui supplier"
    )

    override suspend fun deleteSupplier(supplierId: String): Resource<Unit> = safeUnit(
        block = { api.deleteSupplier(tenantUuid, supplierId) },
        errorMsg = "Gagal menghapus supplier"
    )

    // ── Purchase orders ──────────────────────────────────────────────────────

    override suspend fun getPurchaseOrders(
        status: String?,
        supplierUuid: String?,
        page: Int,
        limit: Int
    ): Resource<List<PurchaseOrder>> = safeList(
        block = { api.getPurchaseOrders(tenantUuid, status, supplierUuid, page, limit) },
        errorMsg = "Gagal memuat PO"
    ) { it.toDomain() }

    override suspend fun getPurchaseOrderDetail(poId: String): Resource<PurchaseOrder> = safe(
        block = { api.getPurchaseOrderDetail(tenantUuid, poId) },
        map = { it.toDomain() },
        errorMsg = "PO tidak ditemukan"
    )

    override suspend fun createPurchaseOrder(
        supplierUuid: String?,
        orderDate: String?,
        expectedDate: String?,
        taxAmount: Double,
        shippingCost: Double,
        notes: String?,
        items: List<POItemEntry>
    ): Resource<PurchaseOrder> = safe(
        block = {
            api.createPurchaseOrder(
                tenantUuid,
                CreatePORequest(
                    supplierUuid = supplierUuid,
                    orderDate = orderDate,
                    expectedDate = expectedDate,
                    taxAmount = taxAmount,
                    shippingCost = shippingCost,
                    notes = notes,
                    items = items.map { POItemInput(it.productUuid, it.qtyOrdered, it.unitCost, it.notes) }
                )
            )
        },
        map = { it.toDomain() },
        errorMsg = "Gagal membuat PO"
    )

    override suspend fun updatePurchaseOrder(
        poId: String,
        supplierUuid: String?,
        orderDate: String?,
        expectedDate: String?,
        taxAmount: Double?,
        shippingCost: Double?,
        notes: String?
    ): Resource<PurchaseOrder> = safe(
        block = {
            api.updatePurchaseOrder(
                tenantUuid,
                poId,
                UpdatePOHeaderRequest(supplierUuid, orderDate, expectedDate, taxAmount, shippingCost, notes)
            )
        },
        map = { it.toDomain() },
        errorMsg = "Gagal memperbarui PO"
    )

    override suspend fun addPurchaseOrderItem(poId: String, item: POItemEntry): Resource<PurchaseOrderItem> = safe(
        block = {
            api.addPurchaseOrderItem(
                tenantUuid,
                poId,
                POItemInput(item.productUuid, item.qtyOrdered, item.unitCost, item.notes)
            )
        },
        map = { it.toDomain() },
        errorMsg = "Gagal menambah item PO"
    )

    override suspend fun updatePurchaseOrderItem(
        poId: String,
        itemId: String,
        qtyOrdered: Double?,
        unitCost: Double?,
        notes: String?
    ): Resource<PurchaseOrderItem> = safe(
        block = {
            api.updatePurchaseOrderItem(
                tenantUuid,
                poId,
                itemId,
                UpdatePOItemRequest(qtyOrdered, unitCost, notes)
            )
        },
        map = { it.toDomain() },
        errorMsg = "Gagal memperbarui item PO"
    )

    override suspend fun deletePurchaseOrderItem(poId: String, itemId: String): Resource<Unit> = safeUnit(
        block = { api.deletePurchaseOrderItem(tenantUuid, poId, itemId) },
        errorMsg = "Gagal menghapus item PO"
    )

    override suspend fun sendPurchaseOrder(poId: String): Resource<PurchaseOrder> = safe(
        block = { api.sendPurchaseOrder(tenantUuid, poId) },
        map = { it.toDomain() },
        errorMsg = "Gagal mengirim PO"
    )

    override suspend fun receivePurchaseOrder(
        poId: String,
        items: List<ReceiveItemEntry>,
        receivedDate: String?,
        notes: String?
    ): Resource<PurchaseOrder> = safe(
        block = {
            api.receivePurchaseOrder(
                tenantUuid,
                poId,
                ReceivePORequest(
                    items = items.map { ReceiveItemEntryDto(it.itemUuid, it.qtyReceived) },
                    receivedDate = receivedDate,
                    notes = notes
                )
            )
        },
        map = { it.toDomain() },
        errorMsg = "Gagal menerima PO"
    )

    override suspend fun cancelPurchaseOrder(poId: String): Resource<PurchaseOrder> = safe(
        block = { api.cancelPurchaseOrder(tenantUuid, poId) },
        map = { it.toDomain() },
        errorMsg = "Gagal membatalkan PO"
    )
}

// ── Helpers (file-private) ──────────────────────────────────────────────────

private suspend fun <T, R> safe(
    block: suspend () -> id.rancak.app.data.remote.dto.ApiResponse<T>,
    map: (T) -> R,
    errorMsg: String
): Resource<R> = try {
    val response = block()
    if (response.isSuccess && response.data != null) {
        Resource.Success(map(response.data))
    } else {
        Resource.Error(response.message ?: errorMsg)
    }
} catch (e: Exception) {
    Resource.Error(e.message ?: "Kesalahan jaringan")
}

private suspend fun <T, R> safeList(
    block: suspend () -> id.rancak.app.data.remote.dto.ApiResponse<List<T>>,
    errorMsg: String,
    map: (T) -> R
): Resource<List<R>> = try {
    val response = block()
    if (response.isSuccess && response.data != null) {
        Resource.Success(response.data.map(map))
    } else {
        Resource.Error(response.message ?: errorMsg)
    }
} catch (e: Exception) {
    Resource.Error(e.message ?: "Kesalahan jaringan")
}

private suspend fun safeUnit(
    block: suspend () -> id.rancak.app.data.remote.dto.ApiResponse<Unit>,
    errorMsg: String
): Resource<Unit> = try {
    val response = block()
    if (response.isSuccess) Resource.Success(Unit)
    else Resource.Error(response.message ?: errorMsg)
} catch (e: Exception) {
    Resource.Error(e.message ?: "Kesalahan jaringan")
}

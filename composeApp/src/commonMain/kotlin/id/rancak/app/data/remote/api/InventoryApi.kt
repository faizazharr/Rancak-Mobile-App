package id.rancak.app.data.remote.api

import id.rancak.app.data.remote.dto.ApiResponse
import id.rancak.app.data.remote.dto.inventory.CreateOpnameRequest
import id.rancak.app.data.remote.dto.inventory.CreatePORequest
import id.rancak.app.data.remote.dto.inventory.CreateSupplierRequest
import id.rancak.app.data.remote.dto.inventory.OpnameDetailDto
import id.rancak.app.data.remote.dto.inventory.OpnameDto
import id.rancak.app.data.remote.dto.inventory.PurchaseOrderDto
import id.rancak.app.data.remote.dto.inventory.PurchaseOrderItemDto
import id.rancak.app.data.remote.dto.inventory.ReceivePORequest
import id.rancak.app.data.remote.dto.inventory.SupplierDto
import id.rancak.app.data.remote.dto.inventory.UpdatePOHeaderRequest
import id.rancak.app.data.remote.dto.inventory.UpdatePOItemRequest
import id.rancak.app.data.remote.dto.inventory.UpdateSupplierRequest
import id.rancak.app.data.remote.dto.inventory.UpsertOpnameItemsRequest
import io.ktor.client.call.body
import io.ktor.client.request.delete
import io.ktor.client.request.get
import io.ktor.client.request.parameter
import io.ktor.client.request.patch
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.http.ContentType
import io.ktor.http.contentType

/**
 * Inventory operations — stock opname, suppliers, dan purchase orders.
 */

private fun tenantUrl(tenantUuid: String) =
    ApiConstants.BASE_URL + ApiConstants.tenantPath(tenantUuid)

// ── Stock Opname ────────────────────────────────────────────────────────────

suspend fun RancakApiService.getStockOpnames(
    tenantUuid: String,
    status: String? = null,
    page: Int = 1,
    limit: Int = 20
): ApiResponse<List<OpnameDto>> =
    client.get(tenantUrl(tenantUuid) + "/stock-opname") {
        status?.let { parameter("status", it) }
        parameter("page", page)
        parameter("limit", limit)
    }.body()

suspend fun RancakApiService.createStockOpname(
    tenantUuid: String,
    note: String? = null
): ApiResponse<OpnameDto> =
    client.post(tenantUrl(tenantUuid) + "/stock-opname") {
        contentType(ContentType.Application.Json)
        setBody(CreateOpnameRequest(note))
    }.body()

suspend fun RancakApiService.getStockOpnameDetail(
    tenantUuid: String,
    opnameId: String
): ApiResponse<OpnameDetailDto> =
    client.get(tenantUrl(tenantUuid) + "/stock-opname/$opnameId").body()

suspend fun RancakApiService.cancelStockOpname(
    tenantUuid: String,
    opnameId: String
): ApiResponse<Unit> =
    client.delete(tenantUrl(tenantUuid) + "/stock-opname/$opnameId").body()

suspend fun RancakApiService.upsertOpnameItems(
    tenantUuid: String,
    opnameId: String,
    request: UpsertOpnameItemsRequest
): ApiResponse<Unit> =
    client.post(tenantUrl(tenantUuid) + "/stock-opname/$opnameId/items") {
        contentType(ContentType.Application.Json)
        setBody(request)
    }.body()

suspend fun RancakApiService.deleteOpnameItem(
    tenantUuid: String,
    opnameId: String,
    productUuid: String
): ApiResponse<Unit> =
    client.delete(tenantUrl(tenantUuid) + "/stock-opname/$opnameId/items/$productUuid").body()

suspend fun RancakApiService.finalizeStockOpname(
    tenantUuid: String,
    opnameId: String
): ApiResponse<Unit> =
    client.post(tenantUrl(tenantUuid) + "/stock-opname/$opnameId/finalize").body()

// ── Suppliers ───────────────────────────────────────────────────────────────

suspend fun RancakApiService.getSuppliers(
    tenantUuid: String,
    isActive: Boolean? = null
): ApiResponse<List<SupplierDto>> =
    client.get(tenantUrl(tenantUuid) + "/suppliers") {
        isActive?.let { parameter("is_active", it) }
    }.body()

suspend fun RancakApiService.getSupplier(
    tenantUuid: String,
    supplierId: String
): ApiResponse<SupplierDto> =
    client.get(tenantUrl(tenantUuid) + "/suppliers/$supplierId").body()

suspend fun RancakApiService.createSupplier(
    tenantUuid: String,
    request: CreateSupplierRequest
): ApiResponse<SupplierDto> =
    client.post(tenantUrl(tenantUuid) + "/suppliers") {
        contentType(ContentType.Application.Json)
        setBody(request)
    }.body()

suspend fun RancakApiService.updateSupplier(
    tenantUuid: String,
    supplierId: String,
    request: UpdateSupplierRequest
): ApiResponse<SupplierDto> =
    client.patch(tenantUrl(tenantUuid) + "/suppliers/$supplierId") {
        contentType(ContentType.Application.Json)
        setBody(request)
    }.body()

suspend fun RancakApiService.deleteSupplier(
    tenantUuid: String,
    supplierId: String
): ApiResponse<Unit> =
    client.delete(tenantUrl(tenantUuid) + "/suppliers/$supplierId").body()

// ── Purchase Orders ─────────────────────────────────────────────────────────

suspend fun RancakApiService.getPurchaseOrders(
    tenantUuid: String,
    status: String? = null,
    supplierUuid: String? = null,
    page: Int = 1,
    limit: Int = 20
): ApiResponse<List<PurchaseOrderDto>> =
    client.get(tenantUrl(tenantUuid) + "/purchases") {
        status?.let { parameter("status", it) }
        supplierUuid?.let { parameter("supplier_uuid", it) }
        parameter("page", page)
        parameter("limit", limit)
    }.body()

suspend fun RancakApiService.getPurchaseOrderDetail(
    tenantUuid: String,
    poId: String
): ApiResponse<PurchaseOrderDto> =
    client.get(tenantUrl(tenantUuid) + "/purchases/$poId").body()

suspend fun RancakApiService.createPurchaseOrder(
    tenantUuid: String,
    request: CreatePORequest
): ApiResponse<PurchaseOrderDto> =
    client.post(tenantUrl(tenantUuid) + "/purchases") {
        contentType(ContentType.Application.Json)
        setBody(request)
    }.body()

suspend fun RancakApiService.updatePurchaseOrder(
    tenantUuid: String,
    poId: String,
    request: UpdatePOHeaderRequest
): ApiResponse<PurchaseOrderDto> =
    client.patch(tenantUrl(tenantUuid) + "/purchases/$poId") {
        contentType(ContentType.Application.Json)
        setBody(request)
    }.body()

suspend fun RancakApiService.addPurchaseOrderItem(
    tenantUuid: String,
    poId: String,
    request: id.rancak.app.data.remote.dto.inventory.POItemInput
): ApiResponse<PurchaseOrderItemDto> =
    client.post(tenantUrl(tenantUuid) + "/purchases/$poId/items") {
        contentType(ContentType.Application.Json)
        setBody(request)
    }.body()

suspend fun RancakApiService.updatePurchaseOrderItem(
    tenantUuid: String,
    poId: String,
    itemId: String,
    request: UpdatePOItemRequest
): ApiResponse<PurchaseOrderItemDto> =
    client.patch(tenantUrl(tenantUuid) + "/purchases/$poId/items/$itemId") {
        contentType(ContentType.Application.Json)
        setBody(request)
    }.body()

suspend fun RancakApiService.deletePurchaseOrderItem(
    tenantUuid: String,
    poId: String,
    itemId: String
): ApiResponse<Unit> =
    client.delete(tenantUrl(tenantUuid) + "/purchases/$poId/items/$itemId").body()

/** Tandai PO sebagai sent ke supplier — status menjadi `ordered`. */
suspend fun RancakApiService.sendPurchaseOrder(
    tenantUuid: String,
    poId: String
): ApiResponse<PurchaseOrderDto> =
    client.post(tenantUrl(tenantUuid) + "/purchases/$poId/send").body()

/** Terima barang sebagian/penuh — server update qty_received tiap item dan stok produk. */
suspend fun RancakApiService.receivePurchaseOrder(
    tenantUuid: String,
    poId: String,
    request: ReceivePORequest
): ApiResponse<PurchaseOrderDto> =
    client.post(tenantUrl(tenantUuid) + "/purchases/$poId/receive") {
        contentType(ContentType.Application.Json)
        setBody(request)
    }.body()

suspend fun RancakApiService.cancelPurchaseOrder(
    tenantUuid: String,
    poId: String
): ApiResponse<PurchaseOrderDto> =
    client.post(tenantUrl(tenantUuid) + "/purchases/$poId/cancel").body()

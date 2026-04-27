package id.rancak.app.data.remote.dto.inventory

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

// ── Stock Opname ────────────────────────────────────────────────────────────

@Serializable
data class OpnameDto(
    val uuid: String,
    @SerialName("opname_no")    val opnameNo: String,
    val status: String,                        // draft|finalized|cancelled
    val note: String? = null,
    @SerialName("item_count")   val itemCount: Int = 0,
    @SerialName("created_by")   val createdBy: String? = null,
    @SerialName("finalized_by") val finalizedBy: String? = null,
    @SerialName("finalized_at") val finalizedAt: String? = null,
    @SerialName("created_at")   val createdAt: String
)

@Serializable
data class OpnameItemDto(
    @SerialName("product_uuid") val productUuid: String,
    @SerialName("product_name") val productName: String,
    val sku: String? = null,
    @SerialName("system_stock") val systemStock: Double,
    @SerialName("actual_stock") val actualStock: Double,
    val difference: Double,
    val note: String? = null
)

@Serializable
data class OpnameDetailDto(
    val uuid: String,
    @SerialName("opname_no")      val opnameNo: String,
    val status: String,
    val note: String? = null,
    @SerialName("item_count")     val itemCount: Int = 0,
    @SerialName("created_by")     val createdBy: String? = null,
    @SerialName("finalized_by")   val finalizedBy: String? = null,
    @SerialName("finalized_at")   val finalizedAt: String? = null,
    @SerialName("created_at")     val createdAt: String,
    val items: List<OpnameItemDto> = emptyList(),
    @SerialName("shortage_count") val shortageCount: Int = 0,
    @SerialName("surplus_count")  val surplusCount: Int = 0
)

@Serializable
data class CreateOpnameRequest(val note: String? = null)

@Serializable
data class UpsertOpnameItemEntry(
    @SerialName("product_uuid") val productUuid: String,
    @SerialName("actual_stock") val actualStock: Double,
    val note: String? = null
)

@Serializable
data class UpsertOpnameItemsRequest(val items: List<UpsertOpnameItemEntry>)

// ── Suppliers ───────────────────────────────────────────────────────────────

@Serializable
data class SupplierDto(
    val uuid: String,
    val name: String,
    @SerialName("contact_name") val contactName: String? = null,
    val phone: String? = null,
    val email: String? = null,
    val address: String? = null,
    val npwp: String? = null,
    val notes: String? = null,
    @SerialName("is_active") val isActive: Boolean = true,
    @SerialName("created_at") val createdAt: String? = null,
    @SerialName("updated_at") val updatedAt: String? = null
)

@Serializable
data class CreateSupplierRequest(
    val name: String,
    @SerialName("contact_name") val contactName: String? = null,
    val phone: String? = null,
    val email: String? = null,
    val address: String? = null,
    val npwp: String? = null,
    val notes: String? = null
)

@Serializable
data class UpdateSupplierRequest(
    val name: String? = null,
    @SerialName("contact_name") val contactName: String? = null,
    val phone: String? = null,
    val email: String? = null,
    val address: String? = null,
    val npwp: String? = null,
    val notes: String? = null,
    @SerialName("is_active") val isActive: Boolean? = null
)

// ── Purchase Orders ─────────────────────────────────────────────────────────

@Serializable
data class PurchaseOrderItemDto(
    val uuid: String,
    @SerialName("product_uuid") val productUuid: String,
    @SerialName("product_name") val productName: String,
    @SerialName("qty_ordered")  val qtyOrdered: String,
    @SerialName("qty_received") val qtyReceived: String,
    @SerialName("unit_cost")    val unitCost: String,
    val subtotal: String,
    val notes: String? = null
)

@Serializable
data class PurchaseOrderDto(
    val uuid: String,
    @SerialName("po_number")     val poNumber: String,
    @SerialName("supplier_uuid") val supplierUuid: String? = null,
    @SerialName("supplier_name") val supplierName: String? = null,
    val status: String,                                    // draft|ordered|partial|received|cancelled
    @SerialName("order_date")    val orderDate: String,
    @SerialName("expected_date") val expectedDate: String? = null,
    @SerialName("received_date") val receivedDate: String? = null,
    val subtotal: String,
    @SerialName("tax_amount")    val taxAmount: String,
    @SerialName("shipping_cost") val shippingCost: String,
    val total: String,
    val notes: String? = null,
    @SerialName("created_by")    val createdBy: String? = null,
    @SerialName("created_at")    val createdAt: String,
    @SerialName("updated_at")    val updatedAt: String,
    val items: List<PurchaseOrderItemDto> = emptyList()
)

@Serializable
data class POItemInput(
    @SerialName("product_uuid") val productUuid: String,
    @SerialName("qty_ordered")  val qtyOrdered: Double,
    @SerialName("unit_cost")    val unitCost: Double,
    val notes: String? = null
)

@Serializable
data class CreatePORequest(
    @SerialName("supplier_uuid") val supplierUuid: String? = null,
    @SerialName("order_date")    val orderDate: String? = null,
    @SerialName("expected_date") val expectedDate: String? = null,
    @SerialName("tax_amount")    val taxAmount: Double = 0.0,
    @SerialName("shipping_cost") val shippingCost: Double = 0.0,
    val notes: String? = null,
    val items: List<POItemInput> = emptyList()
)

@Serializable
data class UpdatePOHeaderRequest(
    @SerialName("supplier_uuid") val supplierUuid: String? = null,
    @SerialName("order_date")    val orderDate: String? = null,
    @SerialName("expected_date") val expectedDate: String? = null,
    @SerialName("tax_amount")    val taxAmount: Double? = null,
    @SerialName("shipping_cost") val shippingCost: Double? = null,
    val notes: String? = null
)

@Serializable
data class UpdatePOItemRequest(
    @SerialName("qty_ordered") val qtyOrdered: Double? = null,
    @SerialName("unit_cost")   val unitCost: Double? = null,
    val notes: String? = null
)

@Serializable
data class ReceiveItemEntry(
    @SerialName("item_uuid")    val itemUuid: String,
    @SerialName("qty_received") val qtyReceived: Double
)

@Serializable
data class ReceivePORequest(
    val items: List<ReceiveItemEntry>,
    @SerialName("received_date") val receivedDate: String? = null,
    val notes: String? = null
)

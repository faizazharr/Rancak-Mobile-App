package id.rancak.app.data.remote.api

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * KDS-related DTOs. Colocated in [id.rancak.app.data.remote.api] to preserve
 * existing imports while keeping the api-service file small.
 */

@Serializable
data class KdsOrderDto(
    val uuid: String,
    @SerialName("invoice_no") val invoiceNo: String? = null,
    @SerialName("order_type") val orderType: String? = null,
    @SerialName("table_name") val tableName: String? = null,
    @SerialName("queue_number") val queueNumber: Int? = null,
    @SerialName("customer_name") val customerName: String? = null,
    val note: String? = null,
    val status: String,
    val items: List<KdsItemDto> = emptyList(),
    @SerialName("created_at") val createdAt: String? = null
)

@Serializable
data class KdsItemDto(
    val uuid: String,
    @SerialName("product_name") val productName: String,
    val qty: String,
    @SerialName("variant_name") val variantName: String? = null,
    val note: String? = null,
    val status: String = "pending"
)

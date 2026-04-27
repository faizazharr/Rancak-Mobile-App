package id.rancak.app.data.remote.dto.billing

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class PlanDto(
    val uuid: String,
    val code: String,
    val name: String,
    val description: String? = null,
    @SerialName("base_price") val basePrice: Double = 0.0,
    @SerialName("tax_rate") val taxRate: Double = 0.0,
    @SerialName("duration_days") val durationDays: Int = 0,
    @SerialName("max_users") val maxUsers: Int? = null,
    @SerialName("is_trial") val isTrial: Boolean = false,
    @SerialName("total_price") val totalPrice: Double = 0.0
)

@Serializable
data class SubscriptionStateDto(
    val status: String,
    val plan: String? = null,
    @SerialName("started_at") val startedAt: String? = null,
    @SerialName("expires_at") val expiresAt: String? = null,
    @SerialName("max_users") val maxUsers: Int? = null,
    @SerialName("had_trial") val hadTrial: Boolean = false
)

@Serializable
data class InvoiceDto(
    val uuid: String,
    @SerialName("invoice_no") val invoiceNo: String,
    @SerialName("plan_code") val planCode: String,
    @SerialName("plan_name") val planName: String,
    @SerialName("duration_days") val durationDays: Int = 0,
    @SerialName("base_amount") val baseAmount: Double = 0.0,
    @SerialName("tax_rate") val taxRate: Double = 0.0,
    @SerialName("tax_amount") val taxAmount: Double = 0.0,
    @SerialName("total_amount") val totalAmount: Double = 0.0,
    val status: String,
    @SerialName("issued_at") val issuedAt: String? = null,
    @SerialName("due_at") val dueAt: String? = null,
    @SerialName("paid_at") val paidAt: String? = null,
    @SerialName("cancelled_at") val cancelledAt: String? = null,
    @SerialName("applied_at") val appliedAt: String? = null,
    @SerialName("created_at") val createdAt: String? = null,
    @SerialName("xendit_qr_id") val xenditQrId: String? = null,
    @SerialName("qr_string") val qrString: String? = null,
    @SerialName("xendit_ref_id") val xenditRefId: String? = null,
    @SerialName("using_webhook") val usingWebhook: Boolean = false
)

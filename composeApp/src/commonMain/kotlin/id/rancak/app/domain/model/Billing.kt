package id.rancak.app.domain.model

data class Plan(
    val uuid: String,
    val code: String,
    val name: String,
    val description: String?,
    val basePrice: Double,
    val taxRate: Double,
    val durationDays: Int,
    val maxUsers: Int?,
    val isTrial: Boolean,
    val totalPrice: Double
)

data class SubscriptionState(
    val status: String,
    val plan: String?,
    val startedAt: String?,
    val expiresAt: String?,
    val maxUsers: Int?,
    val hadTrial: Boolean
)

data class Invoice(
    val uuid: String,
    val invoiceNo: String,
    val planCode: String,
    val planName: String,
    val durationDays: Int,
    val baseAmount: Double,
    val taxRate: Double,
    val taxAmount: Double,
    val totalAmount: Double,
    val status: String,
    val issuedAt: String?,
    val dueAt: String?,
    val paidAt: String?,
    val cancelledAt: String?,
    val appliedAt: String?,
    val createdAt: String?,
    val xenditQrId: String?,
    val qrString: String?,
    val xenditRefId: String?,
    val usingWebhook: Boolean
)

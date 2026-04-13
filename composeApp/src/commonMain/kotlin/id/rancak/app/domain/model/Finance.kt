package id.rancak.app.domain.model

data class CashIn(
    val uuid: String,
    val amount: Long,
    val source: String?,
    val description: String?,
    val note: String?,
    val createdAt: String?
)

data class Expense(
    val uuid: String,
    val amount: Long,
    val description: String?,
    val note: String?,
    val categoryUuid: String?,
    val expenseDate: String?,
    val createdAt: String?
)

data class Bundle(
    val uuid: String,
    val name: String,
    val price: Long,
    val isActive: Boolean,
    val items: List<BundleItem>
)

data class BundleItem(
    val productUuid: String,
    val productName: String?,
    val qty: Int
)

data class Modifier(
    val uuid: String,
    val name: String,
    val sortOrder: Int,
    val productUuid: String?
)

data class ReportSummary(
    val totalSales: Long,
    val totalTransactions: Int,
    val totalDiscount: Long,
    val totalTax: Long,
    val totalNet: Long,
    val paymentMethods: List<PaymentMethodReport>
)

data class PaymentMethodReport(
    val method: String,
    val total: Long,
    val count: Int
)

data class ProductReport(
    val productUuid: String,
    val productName: String,
    val qtySold: Int,
    val totalRevenue: Long
)

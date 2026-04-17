package id.rancak.app.domain.model

data class CashIn(
    val uuid: String,
    val amount: Long,
    val source: String?,
    val description: String?,
    val note: String?,
    val cashierUuid: String?,
    val cashierName: String?,
    val shiftUuid: String?,
    val cashInDate: String?,
    val createdAt: String?
)

data class Expense(
    val uuid: String,
    val amount: Long,
    val description: String?,
    val note: String?,
    val categoryUuid: String?,
    val categoryName: String?,
    val cashierUuid: String?,
    val cashierName: String?,
    val expenseDate: String?,
    val createdAt: String?,
    val updatedAt: String?
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

data class PaymentMethodReport(
    val method: String,
    val total: Long,
    val count: Int
)

data class ShiftSummary(
    val uuid: String,
    val openedAt: String?,
    val closedAt: String?,
    val status: String,
    val openingCash: String,
    val closingCash: String?,
    val expectedCash: String?,
    val cashDifference: String?,
    val cashierName: String?,
    val totalSales: Long,
    val totalTransactions: Int,
    val totalExpenses: Long,
    val totalCashIn: Long,
    val paymentSummary: List<PaymentMethodReport>
)

data class MySalesReport(
    val totalSales: Long,
    val totalTransactions: Int,
    val cashTotal: Long
)

data class DailyCategoryReport(
    val categoryName: String,
    val totalSales: Long,
    val totalQty: Double
)

data class Receipt(
    val invoiceNo: String?,
    val tenantName: String?,
    val tenantAddress: String?,
    val tenantPhone: String?,
    val customerName: String?,
    val queueNumber: Int?,
    val orderType: String?,
    val cashierName: String?,
    val createdAt: String?,
    val items: List<ReceiptItemDomain>,
    val subtotal: Long,
    val discount: Long,
    val surcharge: Long,
    val tax: Long,
    val deliveryFee: Long,
    val tip: Long,
    val adminFee: Long,
    val total: Long,
    val paidAmount: Long,
    val changeAmount: Long,
    val paymentMethod: String?
)

data class ReceiptItemDomain(
    val productName: String,
    val variantName: String?,
    val qty: Int,
    val price: Long,
    val subtotal: Long,
    val note: String?
)

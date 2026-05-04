package id.rancak.app.domain.model

import androidx.compose.runtime.Immutable

@Immutable
data class Table(
    val uuid: String,
    val name: String,
    val area: String?,
    val capacity: Int?,
    val status: TableStatus,
    val isActive: Boolean,
    val sortOrder: Int,
    val activeSaleUuid: String?
)

enum class TableStatus(val value: String) {
    AVAILABLE("available"),
    OCCUPIED("occupied"),
    INACTIVE("inactive");

    companion object {
        fun from(value: String?): TableStatus =
            entries.firstOrNull { it.value == value } ?: AVAILABLE
    }
}

@Immutable
data class Shift(
    val uuid: String,
    val openedAt: String?,
    val closedAt: String?,
    val status: ShiftStatus,
    val openingCash: String,
    val closingCash: String?,
    val expectedCash: String?,
    val cashDifference: String?,
    val cashierName: String?,
    val totalSales: Long?,
    val totalTransactions: Int?,
    val totalExpenses: Long?,
    val totalCashIn: Long?
)

enum class ShiftStatus(val value: String) {
    OPEN("open"),
    CLOSED("closed");

    companion object {
        fun from(value: String?): ShiftStatus =
            entries.firstOrNull { it.value == value } ?: CLOSED
    }
}

@Immutable
data class KdsOrder(
    val uuid: String,
    val invoiceNo: String?,
    val orderType: OrderType,
    val tableName: String?,
    val queueNumber: Int?,
    val customerName: String?,
    val note: String?,
    val status: KdsStatus,
    val items: List<KdsItem>,
    val createdAt: String?
)

@Immutable
data class KdsItem(
    val uuid: String,
    val productName: String,
    val qty: String,
    val variantName: String?,
    val note: String?,
    val status: KdsItemStatus
)

enum class KdsStatus(val value: String) {
    NEW("new"),
    COOKING("cooking"),
    READY("ready"),
    DONE("done");

    companion object {
        fun from(value: String?): KdsStatus =
            entries.firstOrNull { it.value == value } ?: NEW
    }
}

enum class KdsItemStatus(val value: String) {
    PENDING("pending"),
    COOKING("cooking"),
    READY("ready"),
    DONE("done");

    companion object {
        fun from(value: String?): KdsItemStatus =
            entries.firstOrNull { it.value == value } ?: PENDING
    }
}

@Immutable
data class Surcharge(
    val uuid: String,
    val orderType: String?,
    val name: String,
    val amount: Long,
    val isPercentage: Boolean,
    val maxAmount: Long?,
    val isActive: Boolean,
    val sortOrder: Int
)

@Immutable
data class TaxConfig(
    val uuid: String,
    val name: String,
    val rate: Double,
    val applyTo: String,
    val sortOrder: Int,
    val isActive: Boolean
)

@Immutable
data class DiscountRule(
    val uuid: String,
    val name: String,
    val description: String?,
    val ruleType: String,
    val discountType: String,
    val discountValue: Double,
    val startTime: String?,
    val endTime: String?,
    val applicableDays: List<Int>?,
    val minPurchaseAmount: Long?,
    val priority: Int,
    val stackable: Boolean,
    val maxDiscount: Long?,
    val isActive: Boolean
)

@Immutable
data class VoucherValidation(
    val voucher: Voucher,
    val discountApplied: Long
)

@Immutable
data class Voucher(
    val uuid: String,
    val code: String,
    val name: String,
    val description: String?,
    val discountType: String,
    val discountValue: Long,
    val maxDiscount: Long?,
    val minPurchase: Long,
    val usageLimit: Int?,
    val usageCount: Int,
    val validFrom: String?,
    val validUntil: String?,
    val isActive: Boolean
)

@Immutable
data class DiscountPreview(
    val appliedRules: List<AppliedRule>,
    val totalDiscount: Long,
    val finalTotal: Long
)

@Immutable
data class AppliedRule(
    val uuid: String,
    val name: String,
    val ruleType: String,
    val discount: Long
)

@Immutable
data class OrderBoardOrder(
    val uuid: String,
    val invoiceNo: String?,
    val queueNumber: Int?,
    val orderType: OrderType,
    val customerName: String?,
    val status: SaleStatus,
    val createdAt: String?,
    val servedAt: String?,
    val items: List<OrderBoardItem>
)

@Immutable
data class OrderBoardItem(
    val productName: String,
    val qty: Int,
    val note: String?
)

/** Cash count: rekonsiliasi kas fisik vs sistem. */
@Immutable
data class CashCount(
    val uuid: String,
    val shiftUuid: String,
    val expectedCash: Double,
    val actualCash: Double,
    /** actual - expected. Positif = lebih, negatif = kurang. */
    val difference: Double,
    val denominations: Map<String, Int>?,
    val note: String?,
    val countedAt: String
)

/** Rekap shift per kasir untuk laporan harian. */
@Immutable
data class CashierShiftSummary(
    val cashierUuid: String?,
    val cashierName: String,
    val shiftUuid: String?,
    val openedAt: String,
    val closedAt: String?,
    val shiftStatus: String,
    val totalTransactions: Int,
    val voidCount: Int,
    val refundCount: Int,
    val grossTotal: Double,
    val cashTotal: Double,
    val nonCashTotal: Double,
    /** Selisih kas — null jika belum ada cash count. */
    val cashDifference: Double?
)

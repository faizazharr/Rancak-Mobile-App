package id.rancak.app.domain.model

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

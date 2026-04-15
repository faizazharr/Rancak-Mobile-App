package id.rancak.app.data.printing

/**
 * Shared data models for cross-platform ESC/POS printing.
 * Android: Bluetooth Classic (SPP) + TCP/IP
 * iOS:     Bluetooth LE (BLE via CoreBluetooth) + TCP/IP
 */

/** Result type returned from all print operations. */
sealed class PrintResult {
    object Success : PrintResult()
    data class Error(val message: String) : PrintResult()
}

/** A discovered printer that can be connected to. */
data class PrinterDevice(
    /** Human-readable name (e.g. "XP-58" or "192.168.1.100"). */
    val name: String,
    /**
     * Address used to connect:
     * - Bluetooth Android: MAC address "AA:BB:CC:DD:EE:FF"
     * - Bluetooth iOS BLE: UUID string from CoreBluetooth
     * - Network: IP address "192.168.1.100"
     */
    val address: String,
    val type: PrinterConnectionType
)

enum class PrinterConnectionType {
    BLUETOOTH,
    NETWORK
}

/**
 * Print mode — determines how cashier receipt and KOT are sent to printer(s).
 *
 * - [DUAL_PRINTER]: Two separate printers (cashier + kitchen). App sends
 *   two parallel requests and writes to each printer simultaneously.
 * - [SINGLE_KOT_FIRST]: One printer. KOT prints first, then paper cut,
 *   then cashier receipt. Default for restaurants where kitchen speed matters.
 * - [SINGLE_RECEIPT_FIRST]: One printer. Cashier receipt prints first,
 *   then paper cut, then KOT. For food courts / "pay first, cook later".
 * - [RECEIPT_ONLY]: Only cashier receipt, no KOT. For retail / non-food.
 */
enum class PrintMode(val value: String) {
    DUAL_PRINTER("dual_printer"),
    SINGLE_KOT_FIRST("single_kot_first"),
    SINGLE_RECEIPT_FIRST("single_receipt_first"),
    RECEIPT_ONLY("receipt_only");

    companion object {
        fun from(value: String?): PrintMode =
            entries.firstOrNull { it.value == value } ?: RECEIPT_ONLY
    }
}

/** Full receipt data to be rendered as ESC/POS bytes. */
data class ReceiptData(
    val storeName: String,
    val storeAddress: String? = null,
    val storePhone: String? = null,
    val invoiceNo: String,
    val orderType: String,
    val tableName: String? = null,
    val cashierName: String? = null,
    val createdAt: String,
    val items: List<ReceiptItem>,
    val subtotal: Long,
    val discount: Long = 0,
    val surcharge: Long = 0,
    val tax: Long = 0,
    val deliveryFee: Long = 0,
    val tip: Long = 0,
    val total: Long,
    val paymentMethod: String? = null,
    val paidAmount: Long = 0,
    val changeAmount: Long = 0,
    val footerText: String? = null,
    /** Jika true, struk akan diberi stempel VOID di bagian atas. */
    val isVoided: Boolean = false
)

data class ReceiptItem(
    val name: String,
    val variantName: String? = null,
    val qty: Int,
    val price: Long,
    val subtotal: Long,
    val note: String? = null
)

/**
 * Kitchen Order Ticket data — sent to kitchen printer.
 * No prices, no totals. Focused on what to cook and where to deliver.
 */
data class KitchenTicketData(
    val storeName: String,
    val invoiceNo: String,
    val orderType: String,
    val tableName: String? = null,
    val queueNumber: Int? = null,
    val customerName: String? = null,
    val cashierName: String? = null,
    val createdAt: String,
    val items: List<KitchenTicketItem>
)

data class KitchenTicketItem(
    val name: String,
    val qty: Int,
    val note: String? = null
)

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
    val changeAmount: Long = 0
)

data class ReceiptItem(
    val name: String,
    val variantName: String? = null,
    val qty: Int,
    val price: Long,
    val subtotal: Long,
    val note: String? = null
)

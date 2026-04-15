package id.rancak.app.data.local

import com.russhwolf.settings.Settings
import com.russhwolf.settings.set

/**
 * Persistent app settings stored via multiplatform-settings.
 * Covers printer connection, receipt header, and general preferences.
 */
class SettingsStore {

    private val settings = Settings()

    // ── Printer ──────────────────────────────────────────────────────────────

    var printerType: String
        get() = settings.getString(KEY_PRINTER_TYPE, TYPE_BLUETOOTH)
        set(value) { settings[KEY_PRINTER_TYPE] = value }

    var printerName: String
        get() = settings.getString(KEY_PRINTER_NAME, "")
        set(value) { settings[KEY_PRINTER_NAME] = value }

    var printerAddress: String
        get() = settings.getString(KEY_PRINTER_ADDRESS, "")
        set(value) { settings[KEY_PRINTER_ADDRESS] = value }

    var networkPrinterIp: String
        get() = settings.getString(KEY_NETWORK_IP, "")
        set(value) { settings[KEY_NETWORK_IP] = value }

    var networkPrinterPort: Int
        get() = settings.getInt(KEY_NETWORK_PORT, 9100)
        set(value) { settings[KEY_NETWORK_PORT] = value }

    val hasPrinter: Boolean
        get() = printerAddress.isNotBlank() || networkPrinterIp.isNotBlank()

    // ── Receipt ──────────────────────────────────────────────────────────────

    var receiptStoreName: String
        get() = settings.getString(KEY_RECEIPT_STORE_NAME, "")
        set(value) { settings[KEY_RECEIPT_STORE_NAME] = value }

    var receiptStoreAddress: String
        get() = settings.getString(KEY_RECEIPT_STORE_ADDRESS, "")
        set(value) { settings[KEY_RECEIPT_STORE_ADDRESS] = value }

    var receiptStorePhone: String
        get() = settings.getString(KEY_RECEIPT_STORE_PHONE, "")
        set(value) { settings[KEY_RECEIPT_STORE_PHONE] = value }

    var receiptFooter: String
        get() = settings.getString(KEY_RECEIPT_FOOTER, "Terima kasih!")
        set(value) { settings[KEY_RECEIPT_FOOTER] = value }

    // ── General ──────────────────────────────────────────────────────────────

    var autoPrintReceipt: Boolean
        get() = settings.getBoolean(KEY_AUTO_PRINT, false)
        set(value) { settings[KEY_AUTO_PRINT] = value }

    var paperWidth: Int
        get() = settings.getInt(KEY_PAPER_WIDTH, 58)
        set(value) { settings[KEY_PAPER_WIDTH] = value }

    // ── Helpers ──────────────────────────────────────────────────────────────

    fun clearPrinter() {
        settings.remove(KEY_PRINTER_TYPE)
        settings.remove(KEY_PRINTER_NAME)
        settings.remove(KEY_PRINTER_ADDRESS)
        settings.remove(KEY_NETWORK_IP)
        settings.remove(KEY_NETWORK_PORT)
    }

    companion object {
        const val TYPE_BLUETOOTH = "bluetooth"
        const val TYPE_NETWORK = "network"

        private const val KEY_PRINTER_TYPE = "rancak_printer_type"
        private const val KEY_PRINTER_NAME = "rancak_printer_name"
        private const val KEY_PRINTER_ADDRESS = "rancak_printer_address"
        private const val KEY_NETWORK_IP = "rancak_network_ip"
        private const val KEY_NETWORK_PORT = "rancak_network_port"
        private const val KEY_RECEIPT_STORE_NAME = "rancak_receipt_store_name"
        private const val KEY_RECEIPT_STORE_ADDRESS = "rancak_receipt_store_address"
        private const val KEY_RECEIPT_STORE_PHONE = "rancak_receipt_store_phone"
        private const val KEY_RECEIPT_FOOTER = "rancak_receipt_footer"
        private const val KEY_AUTO_PRINT = "rancak_auto_print"
        private const val KEY_PAPER_WIDTH = "rancak_paper_width"
    }
}

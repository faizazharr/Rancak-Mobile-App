package id.rancak.app.data.local

import androidx.compose.runtime.Stable
import com.russhwolf.settings.Settings
import com.russhwolf.settings.set

/**
 * Persistent app settings stored via multiplatform-settings.
 * Covers printer connection, receipt header, and general preferences.
 */
@Stable
class SettingsStore {

    private val settings = Settings()

    // ── Cashier Printer (primary) ────────────────────────────────────────────

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

    // ── Kitchen Printer (secondary, for dual-printer mode) ───────────────────

    var kitchenPrinterType: String
        get() = settings.getString(KEY_KITCHEN_PRINTER_TYPE, TYPE_BLUETOOTH)
        set(value) { settings[KEY_KITCHEN_PRINTER_TYPE] = value }

    var kitchenPrinterName: String
        get() = settings.getString(KEY_KITCHEN_PRINTER_NAME, "")
        set(value) { settings[KEY_KITCHEN_PRINTER_NAME] = value }

    var kitchenPrinterAddress: String
        get() = settings.getString(KEY_KITCHEN_PRINTER_ADDRESS, "")
        set(value) { settings[KEY_KITCHEN_PRINTER_ADDRESS] = value }

    var kitchenNetworkPrinterIp: String
        get() = settings.getString(KEY_KITCHEN_NETWORK_IP, "")
        set(value) { settings[KEY_KITCHEN_NETWORK_IP] = value }

    var kitchenNetworkPrinterPort: Int
        get() = settings.getInt(KEY_KITCHEN_NETWORK_PORT, 9100)
        set(value) { settings[KEY_KITCHEN_NETWORK_PORT] = value }

    val hasKitchenPrinter: Boolean
        get() = kitchenPrinterAddress.isNotBlank() || kitchenNetworkPrinterIp.isNotBlank()

    // ── Print Mode ───────────────────────────────────────────────────────────

    /** One of PrintMode.value: "receipt_only", "dual_printer", "single_kot_first", "single_receipt_first" */
    var printMode: String
        get() = settings.getString(KEY_PRINT_MODE, "receipt_only")
        set(value) { settings[KEY_PRINT_MODE] = value }

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

    // ── Receipt API settings (synced from /receipt-settings) ─────────────────

    var receiptHeader: String
        get() = settings.getString(KEY_RECEIPT_HEADER, "")
        set(value) { settings[KEY_RECEIPT_HEADER] = value }

    var receiptFooter2: String
        get() = settings.getString(KEY_RECEIPT_FOOTER2, "")
        set(value) { settings[KEY_RECEIPT_FOOTER2] = value }

    var receiptSeparatorStyle: String
        get() = settings.getString(KEY_RECEIPT_SEPARATOR_STYLE, "dashed")
        set(value) { settings[KEY_RECEIPT_SEPARATOR_STYLE] = value }

    var receiptSeparatorCount: Int
        get() = settings.getInt(KEY_RECEIPT_SEPARATOR_COUNT, 1)
        set(value) { settings[KEY_RECEIPT_SEPARATOR_COUNT] = value }

    var receiptNameSize: String
        get() = settings.getString(KEY_RECEIPT_NAME_SIZE, "large")
        set(value) { settings[KEY_RECEIPT_NAME_SIZE] = value }

    var receiptLogoPosition: String
        get() = settings.getString(KEY_RECEIPT_LOGO_POSITION, "center")
        set(value) { settings[KEY_RECEIPT_LOGO_POSITION] = value }

    var receiptLogoSizePct: Int
        get() = settings.getInt(KEY_RECEIPT_LOGO_SIZE_PCT, 80)
        set(value) { settings[KEY_RECEIPT_LOGO_SIZE_PCT] = value }

    var receiptFooterPosition: String
        get() = settings.getString(KEY_RECEIPT_FOOTER_POSITION, "center")
        set(value) { settings[KEY_RECEIPT_FOOTER_POSITION] = value }

    var receiptInstagram: String
        get() = settings.getString(KEY_RECEIPT_INSTAGRAM, "")
        set(value) { settings[KEY_RECEIPT_INSTAGRAM] = value }

    var receiptFacebook: String
        get() = settings.getString(KEY_RECEIPT_FACEBOOK, "")
        set(value) { settings[KEY_RECEIPT_FACEBOOK] = value }

    var receiptWifiSsid: String
        get() = settings.getString(KEY_RECEIPT_WIFI_SSID, "")
        set(value) { settings[KEY_RECEIPT_WIFI_SSID] = value }

    var receiptWifiPassword: String
        get() = settings.getString(KEY_RECEIPT_WIFI_PASSWORD, "")
        set(value) { settings[KEY_RECEIPT_WIFI_PASSWORD] = value }

    var receiptEmail: String
        get() = settings.getString(KEY_RECEIPT_EMAIL, "")
        set(value) { settings[KEY_RECEIPT_EMAIL] = value }

    var receiptWebsite: String
        get() = settings.getString(KEY_RECEIPT_WEBSITE, "")
        set(value) { settings[KEY_RECEIPT_WEBSITE] = value }

    var receiptNpwp: String
        get() = settings.getString(KEY_RECEIPT_NPWP, "")
        set(value) { settings[KEY_RECEIPT_NPWP] = value }

    // ── Merchant Static QRIS ────────────────────────────────────────────

    /**
     * QRIS string statis milik merchant (EMVCo payload). Dipakai untuk
     * pembayaran split-bill per pelanggan, di mana setiap pelanggan scan
     * QR yang sama dan memasukkan nominal manual sesuai bagiannya.
     */
    var merchantQrisString: String
        get() = settings.getString(KEY_MERCHANT_QRIS, "")
        set(value) { settings[KEY_MERCHANT_QRIS] = value }

    // ── General ──────────────────────────────────────────────────────────────

    var autoPrintReceipt: Boolean
        get() = settings.getBoolean(KEY_AUTO_PRINT, false)
        set(value) { settings[KEY_AUTO_PRINT] = value }

    var paperWidth: Int
        get() = settings.getInt(KEY_PAPER_WIDTH, 58)
        set(value) { settings[KEY_PAPER_WIDTH] = value }

    /** Jumlah salinan struk yang dicetak per transaksi. Default 1, max 3. */
    var receiptCopies: Int
        get() = settings.getInt(KEY_RECEIPT_COPIES, 1)
        set(value) { settings[KEY_RECEIPT_COPIES] = value.coerceIn(1, 3) }

    /** Cetak otomatis tiket nomor antrian setelah pesanan dibuat. */
    var autoPrintQueue: Boolean
        get() = settings.getBoolean(KEY_AUTO_PRINT_QUEUE, false)
        set(value) { settings[KEY_AUTO_PRINT_QUEUE] = value }

    /** Tampilkan logo toko di bagian atas struk. */
    var receiptShowLogo: Boolean
        get() = settings.getBoolean(KEY_RECEIPT_SHOW_LOGO, false)
        set(value) { settings[KEY_RECEIPT_SHOW_LOGO] = value }

    // ── Helpers ──────────────────────────────────────────────────────────────

    fun clearPrinter() {
        settings.remove(KEY_PRINTER_TYPE)
        settings.remove(KEY_PRINTER_NAME)
        settings.remove(KEY_PRINTER_ADDRESS)
        settings.remove(KEY_NETWORK_IP)
        settings.remove(KEY_NETWORK_PORT)
    }

    fun clearKitchenPrinter() {
        settings.remove(KEY_KITCHEN_PRINTER_TYPE)
        settings.remove(KEY_KITCHEN_PRINTER_NAME)
        settings.remove(KEY_KITCHEN_PRINTER_ADDRESS)
        settings.remove(KEY_KITCHEN_NETWORK_IP)
        settings.remove(KEY_KITCHEN_NETWORK_PORT)
    }

    companion object {
        const val TYPE_BLUETOOTH = "bluetooth"
        const val TYPE_NETWORK = "network"

        private const val KEY_PRINTER_TYPE = "rancak_printer_type"
        private const val KEY_PRINTER_NAME = "rancak_printer_name"
        private const val KEY_PRINTER_ADDRESS = "rancak_printer_address"
        private const val KEY_NETWORK_IP = "rancak_network_ip"
        private const val KEY_NETWORK_PORT = "rancak_network_port"

        private const val KEY_KITCHEN_PRINTER_TYPE = "rancak_kitchen_printer_type"
        private const val KEY_KITCHEN_PRINTER_NAME = "rancak_kitchen_printer_name"
        private const val KEY_KITCHEN_PRINTER_ADDRESS = "rancak_kitchen_printer_address"
        private const val KEY_KITCHEN_NETWORK_IP = "rancak_kitchen_network_ip"
        private const val KEY_KITCHEN_NETWORK_PORT = "rancak_kitchen_network_port"

        private const val KEY_PRINT_MODE = "rancak_print_mode"

        private const val KEY_RECEIPT_STORE_NAME = "rancak_receipt_store_name"
        private const val KEY_RECEIPT_STORE_ADDRESS = "rancak_receipt_store_address"
        private const val KEY_RECEIPT_STORE_PHONE = "rancak_receipt_store_phone"
        private const val KEY_RECEIPT_FOOTER = "rancak_receipt_footer"
        private const val KEY_RECEIPT_HEADER = "rancak_receipt_header"
        private const val KEY_RECEIPT_FOOTER2 = "rancak_receipt_footer2"
        private const val KEY_RECEIPT_SEPARATOR_STYLE = "rancak_receipt_separator_style"
        private const val KEY_RECEIPT_SEPARATOR_COUNT = "rancak_receipt_separator_count"
        private const val KEY_RECEIPT_NAME_SIZE = "rancak_receipt_name_size"
        private const val KEY_RECEIPT_LOGO_POSITION = "rancak_receipt_logo_position"
        private const val KEY_RECEIPT_LOGO_SIZE_PCT = "rancak_receipt_logo_size_pct"
        private const val KEY_RECEIPT_FOOTER_POSITION = "rancak_receipt_footer_position"
        private const val KEY_RECEIPT_INSTAGRAM = "rancak_receipt_instagram"
        private const val KEY_RECEIPT_FACEBOOK = "rancak_receipt_facebook"
        private const val KEY_RECEIPT_WIFI_SSID = "rancak_receipt_wifi_ssid"
        private const val KEY_RECEIPT_WIFI_PASSWORD = "rancak_receipt_wifi_password"
        private const val KEY_RECEIPT_EMAIL = "rancak_receipt_email"
        private const val KEY_RECEIPT_WEBSITE = "rancak_receipt_website"
        private const val KEY_RECEIPT_NPWP = "rancak_receipt_npwp"
        private const val KEY_MERCHANT_QRIS = "rancak_merchant_qris"
        private const val KEY_AUTO_PRINT = "rancak_auto_print"
        private const val KEY_PAPER_WIDTH = "rancak_paper_width"
        private const val KEY_RECEIPT_COPIES = "rancak_receipt_copies"
        private const val KEY_AUTO_PRINT_QUEUE = "rancak_auto_print_queue"
        private const val KEY_RECEIPT_SHOW_LOGO = "rancak_receipt_show_logo"
    }
}

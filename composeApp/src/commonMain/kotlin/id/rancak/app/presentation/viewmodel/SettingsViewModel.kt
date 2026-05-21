package id.rancak.app.presentation.viewmodel

import androidx.compose.runtime.Immutable

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import id.rancak.app.data.local.SettingsStore
import id.rancak.app.data.printing.EscPosBuilder
import id.rancak.app.data.printing.PrintMode
import id.rancak.app.data.printing.PrintResult
import id.rancak.app.data.printing.PrinterConnectionType
import id.rancak.app.data.printing.PrinterDevice
import id.rancak.app.data.printing.PrinterManager
import id.rancak.app.data.printing.ReceiptData
import id.rancak.app.data.printing.KitchenTicketData
import id.rancak.app.data.printing.KitchenTicketItem
import id.rancak.app.domain.model.Resource
import id.rancak.app.domain.model.ReceiptSettingsConfig
import id.rancak.app.domain.repository.DeviceConfigRepository
import id.rancak.app.domain.repository.ReceiptSettingsRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

@Immutable
data class SettingsUiState(
    // Cashier Printer
    val printerType: String = SettingsStore.TYPE_BLUETOOTH,
    val savedPrinterName: String = "",
    val savedPrinterAddress: String = "",
    val networkIp: String = "",
    val networkPort: String = "9100",
    val discoveredPrinters: List<PrinterDevice> = emptyList(),
    val isScanning: Boolean = false,
    val isConnecting: Boolean = false,
    val isPrinting: Boolean = false,
    val printerMessage: String? = null,
    val hasScannedOnce: Boolean = false,
    val isBluetoothOn: Boolean = true,
    val isConnected: Boolean = false,

    // Kitchen Printer
    val kitchenPrinterType: String = SettingsStore.TYPE_BLUETOOTH,
    val kitchenPrinterName: String = "",
    val kitchenPrinterAddress: String = "",
    val kitchenNetworkIp: String = "",
    val kitchenNetworkPort: String = "9100",

    // Print Mode
    val printMode: PrintMode = PrintMode.RECEIPT_ONLY,

    // Receipt
    val storeName: String = "",
    val storeAddress: String = "",
    val storePhone: String = "",
    val footerText: String = "Terima kasih!",

    // Merchant Static QRIS
    val merchantQrisString: String = "",

    // General
    val autoPrint: Boolean = false,
    val paperWidth: Int = 58,
    /** Jumlah salinan struk per transaksi (1–3). */
    val receiptCopies: Int = 1,
    /** Auto-cetak tiket nomor antrian saat pesanan dibuat. */
    val autoPrintQueue: Boolean = false,
    /** Tampilkan logo toko di bagian atas struk. */
    val showLogo: Boolean = false,

    // ── Receipt API settings (synced from /receipt-settings) ─────────────────
    /** Teks bebas di bawah identitas toko (tagline, dll.). */
    val receiptHeader: String = "",
    /** Baris kedua footer struk (kebijakan retur, promo, dll.). */
    val receiptFooter2: String = "",
    /** Gaya pemisah antar seksi: "dashed" | "double" | "none" */
    val separatorStyle: String = "dashed",
    /** Jumlah baris pemisah: 1 | 2 */
    val separatorCount: Int = 1,
    /** Ukuran font nama toko: "normal" | "large" | "xlarge" */
    val receiptNameSize: String = "large",
    /** Posisi logo: "left" | "center" | "right" */
    val logoPosition: String = "center",
    /** Lebar logo sebagai % lebar kertas: 60 | 70 | 80 | 100 */
    val logoSizePct: Int = 80,
    /** Alignment footer: "left" | "center" | "right" */
    val footerPosition: String = "center",
    /** Username Instagram (tanpa @). */
    val receiptInstagram: String = "",
    /** URL/username Facebook. */
    val receiptFacebook: String = "",
    /** Nama SSID WiFi toko. */
    val receiptWifiSsid: String = "",
    /** Password WiFi (hanya ditampilkan jika wifiSsid juga diisi). */
    val receiptWifiPassword: String = "",
    /** Email toko — dicetak jika diisi. */
    val receiptEmail: String = "",
    /** Website toko — dicetak jika diisi. */
    val receiptWebsite: String = "",
    /** NPWP toko — dicetak jika diisi. */
    val receiptNpwp: String = ""
) {
    val hasPrinter: Boolean
        get() = savedPrinterName.isNotBlank() && savedPrinterAddress.isNotBlank()

    val hasKitchenPrinter: Boolean
        get() = kitchenPrinterName.isNotBlank() && kitchenPrinterAddress.isNotBlank()

    fun toReceiptSettingsConfig() = ReceiptSettingsConfig(
        receiptHeader     = receiptHeader.takeIf { it.isNotBlank() },
        receiptFooter     = footerText.takeIf { it.isNotBlank() },
        receiptFooter2    = receiptFooter2.takeIf { it.isNotBlank() },
        separatorStyle    = separatorStyle,
        separatorCount    = separatorCount,
        receiptNameSize   = receiptNameSize,
        logoPosition      = logoPosition,
        logoSizePct       = logoSizePct,
        footerPosition    = footerPosition,
        receiptInstagram  = receiptInstagram.takeIf { it.isNotBlank() },
        receiptFacebook   = receiptFacebook.takeIf { it.isNotBlank() },
        receiptWifiSsid   = receiptWifiSsid.takeIf { it.isNotBlank() },
        receiptWifiPassword = receiptWifiPassword.takeIf { it.isNotBlank() },
        email             = receiptEmail.takeIf { it.isNotBlank() },
        website           = receiptWebsite.takeIf { it.isNotBlank() },
        npwp              = receiptNpwp.takeIf { it.isNotBlank() }
    )
}

class SettingsViewModel(
    private val settingsStore: SettingsStore,
    private val printerManager: PrinterManager,
    private val deviceConfigRepository: DeviceConfigRepository,
    private val receiptSettingsRepository: ReceiptSettingsRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(SettingsUiState())
    val uiState: StateFlow<SettingsUiState> = _uiState.asStateFlow()

    init {
        // Load persisted values into UI state
        _uiState.value = SettingsUiState(
            printerType = settingsStore.printerType,
            savedPrinterName = settingsStore.printerName,
            savedPrinterAddress = settingsStore.printerAddress,
            networkIp = settingsStore.networkPrinterIp,
            networkPort = settingsStore.networkPrinterPort.toString(),
            kitchenPrinterType = settingsStore.kitchenPrinterType,
            kitchenPrinterName = settingsStore.kitchenPrinterName,
            kitchenPrinterAddress = settingsStore.kitchenPrinterAddress,
            kitchenNetworkIp = settingsStore.kitchenNetworkPrinterIp,
            kitchenNetworkPort = settingsStore.kitchenNetworkPrinterPort.toString(),
            printMode = PrintMode.from(settingsStore.printMode),
            storeName = settingsStore.receiptStoreName,
            storeAddress = settingsStore.receiptStoreAddress,
            storePhone = settingsStore.receiptStorePhone,
            footerText = settingsStore.receiptFooter,
            merchantQrisString = settingsStore.merchantQrisString,
            autoPrint = settingsStore.autoPrintReceipt,
            paperWidth = settingsStore.paperWidth,
            receiptCopies = settingsStore.receiptCopies,
            autoPrintQueue = settingsStore.autoPrintQueue,
            showLogo = settingsStore.receiptShowLogo,
            receiptHeader       = settingsStore.receiptHeader,
            receiptFooter2      = settingsStore.receiptFooter2,
            separatorStyle      = settingsStore.receiptSeparatorStyle,
            separatorCount      = settingsStore.receiptSeparatorCount,
            receiptNameSize     = settingsStore.receiptNameSize,
            logoPosition        = settingsStore.receiptLogoPosition,
            logoSizePct         = settingsStore.receiptLogoSizePct,
            footerPosition      = settingsStore.receiptFooterPosition,
            receiptInstagram    = settingsStore.receiptInstagram,
            receiptFacebook     = settingsStore.receiptFacebook,
            receiptWifiSsid     = settingsStore.receiptWifiSsid,
            receiptWifiPassword = settingsStore.receiptWifiPassword,
            receiptEmail        = settingsStore.receiptEmail,
            receiptWebsite      = settingsStore.receiptWebsite,
            receiptNpwp         = settingsStore.receiptNpwp,
            isBluetoothOn = try { printerManager.isBluetoothEnabled() } catch (_: Exception) { false }
        )
        // Tarik konfigurasi dari server (fire-and-forget) — override nilai lokal
        // kalau server punya nilai berbeda. Aman karena cuma key kecil & deterministik.
        loadAppConfigFromServer()
        loadReceiptSettings()
    }

    /**
     * Tarik konfigurasi `/device-config/app` dari server. Untuk key yang relevan
     * dengan thermal print (`paper_width_mm`, `auto_print_receipt`, `receipt_copies`)
     * sync ke state lokal supaya seragam antar perangkat.
     */
    private fun loadAppConfigFromServer() {
        viewModelScope.launch {
            when (val result = deviceConfigRepository.getAppConfig()) {
                is Resource.Success -> {
                    val map = result.data.associate { it.key to it.value }
                    map["paper_width_mm"]?.toIntOrNull()?.let { width ->
                        if (width == 58 || width == 70 || width == 80) {
                            settingsStore.paperWidth = width
                            _uiState.update { it.copy(paperWidth = width) }
                        }
                    }
                    map["auto_print_receipt"]?.let { value ->
                        val enabled = value.equals("true", ignoreCase = true)
                        settingsStore.autoPrintReceipt = enabled
                        _uiState.update { it.copy(autoPrint = enabled) }
                    }
                    map["receipt_copies"]?.toIntOrNull()?.let { copies ->
                        val safe = copies.coerceIn(1, 3)
                        settingsStore.receiptCopies = safe
                        _uiState.update { it.copy(receiptCopies = safe) }
                    }
                    map["auto_print_queue"]?.let { value ->
                        val enabled = value.equals("true", ignoreCase = true)
                        settingsStore.autoPrintQueue = enabled
                        _uiState.update { it.copy(autoPrintQueue = enabled) }
                    }
                }
                else -> Unit // diam — fallback ke nilai lokal
            }
        }
    }

    /** Push satu key ke `/device-config/app/{key}` (fire-and-forget). */
    private fun pushAppConfig(key: String, value: String) {
        viewModelScope.launch {
            // Hasil sengaja diabaikan: bila gagal (offline / 4xx) tetap simpan
            // lokal supaya UX tidak ketahan.
            deviceConfigRepository.upsertAppConfig(key, value)
        }
    }

    /**
     * Tarik konfigurasi `/receipt-settings` dari server. Field relevan
     * di-sync ke state lokal supaya seragam antar perangkat.
     */
    private fun loadReceiptSettings() {
        viewModelScope.launch {
            when (val result = receiptSettingsRepository.getReceiptSettings()) {
                is Resource.Success -> {
                    val s = result.data
                    // Simpan ke local cache
                    s.receiptFooter?.let { settingsStore.receiptFooter = it }
                    s.receiptHeader?.let { settingsStore.receiptHeader = it } ?: run { settingsStore.receiptHeader = "" }
                    s.receiptFooter2?.let { settingsStore.receiptFooter2 = it } ?: run { settingsStore.receiptFooter2 = "" }
                    settingsStore.receiptSeparatorStyle = s.separatorStyle
                    settingsStore.receiptSeparatorCount = s.separatorCount
                    settingsStore.receiptNameSize = s.receiptNameSize
                    settingsStore.receiptLogoPosition = s.logoPosition
                    settingsStore.receiptLogoSizePct = s.logoSizePct
                    settingsStore.receiptFooterPosition = s.footerPosition
                    s.receiptInstagram?.let { settingsStore.receiptInstagram = it } ?: run { settingsStore.receiptInstagram = "" }
                    s.receiptFacebook?.let { settingsStore.receiptFacebook = it } ?: run { settingsStore.receiptFacebook = "" }
                    s.receiptWifiSsid?.let { settingsStore.receiptWifiSsid = it } ?: run { settingsStore.receiptWifiSsid = "" }
                    s.receiptWifiPassword?.let { settingsStore.receiptWifiPassword = it } ?: run { settingsStore.receiptWifiPassword = "" }
                    s.email?.let { settingsStore.receiptEmail = it } ?: run { settingsStore.receiptEmail = "" }
                    s.website?.let { settingsStore.receiptWebsite = it } ?: run { settingsStore.receiptWebsite = "" }
                    s.npwp?.let { settingsStore.receiptNpwp = it } ?: run { settingsStore.receiptNpwp = "" }
                    // Update UI state
                    _uiState.update { st ->
                        st.copy(
                            footerText          = s.receiptFooter ?: st.footerText,
                            receiptHeader       = s.receiptHeader ?: "",
                            receiptFooter2      = s.receiptFooter2 ?: "",
                            separatorStyle      = s.separatorStyle,
                            separatorCount      = s.separatorCount,
                            receiptNameSize     = s.receiptNameSize,
                            logoPosition        = s.logoPosition,
                            logoSizePct         = s.logoSizePct,
                            footerPosition      = s.footerPosition,
                            receiptInstagram    = s.receiptInstagram ?: "",
                            receiptFacebook     = s.receiptFacebook ?: "",
                            receiptWifiSsid     = s.receiptWifiSsid ?: "",
                            receiptWifiPassword = s.receiptWifiPassword ?: "",
                            receiptEmail        = s.email ?: "",
                            receiptWebsite      = s.website ?: "",
                            receiptNpwp         = s.npwp ?: ""
                        )
                    }
                }
                else -> Unit // diam — fallback ke nilai lokal
            }
        }
    }

    /**
     * Buat [ReceiptSettingsConfig] dari state saat ini dan push ke server
     * (fire-and-forget — UX tidak diblokir).
     */
    private fun pushReceiptSettings() {
        viewModelScope.launch {
            val st = _uiState.value
            receiptSettingsRepository.updateReceiptSettings(
                id.rancak.app.domain.model.ReceiptSettingsConfig(
                    receiptFooter     = st.footerText.ifBlank { null },
                    receiptHeader     = st.receiptHeader.ifBlank { null },
                    receiptFooter2    = st.receiptFooter2.ifBlank { null },
                    separatorStyle    = st.separatorStyle,
                    separatorCount    = st.separatorCount,
                    receiptNameSize   = st.receiptNameSize,
                    logoPosition      = st.logoPosition,
                    logoSizePct       = st.logoSizePct,
                    footerPosition    = st.footerPosition,
                    receiptInstagram  = st.receiptInstagram.ifBlank { null },
                    receiptFacebook   = st.receiptFacebook.ifBlank { null },
                    receiptWifiSsid   = st.receiptWifiSsid.ifBlank { null },
                    receiptWifiPassword = st.receiptWifiPassword.ifBlank { null },
                    email             = st.receiptEmail.ifBlank { null },
                    website           = st.receiptWebsite.ifBlank { null },
                    npwp              = st.receiptNpwp.ifBlank { null }
                )
            )
        }
    }

    fun checkBluetoothState() {
        val btOn = try { printerManager.isBluetoothEnabled() } catch (_: Exception) { false }
        _uiState.update { it.copy(isBluetoothOn = btOn) }
    }

    // ── Printer type toggle ──────────────────────────────────────────────────

    fun setPrinterType(type: String) {
        settingsStore.printerType = type
        _uiState.update { it.copy(printerType = type, printerMessage = null) }
    }

    // ── Bluetooth discovery ──────────────────────────────────────────────────

    fun scanBluetoothPrinters() {
        viewModelScope.launch {
            _uiState.update { it.copy(isScanning = true, printerMessage = null, discoveredPrinters = emptyList()) }
            try {
                val devices = printerManager.getBluetoothPrinters()
                _uiState.update {
                    it.copy(
                        discoveredPrinters = devices,
                        isScanning = false,
                        hasScannedOnce = true,
                        printerMessage = when {
                            devices.isEmpty() -> "Tidak ditemukan printer Bluetooth.\nPastikan Bluetooth aktif dan printer sudah di-pair di Pengaturan perangkat."
                            else -> null
                        }
                    )
                }
            } catch (e: Exception) {
                val msg = when {
                    e.message?.contains("permission", ignoreCase = true) == true ->
                        "Izin Bluetooth belum diberikan. Buka Pengaturan → Izin Aplikasi untuk mengaktifkan."
                    e.message?.contains("disabled", ignoreCase = true) == true ||
                    e.message?.contains("not enabled", ignoreCase = true) == true ->
                        "Bluetooth tidak aktif. Silakan aktifkan Bluetooth di Pengaturan perangkat."
                    else -> e.message ?: "Gagal mencari printer Bluetooth"
                }
                _uiState.update {
                    it.copy(
                        isScanning = false,
                        hasScannedOnce = true,
                        printerMessage = msg
                    )
                }
            }
        }
    }

    fun selectBluetoothPrinter(device: PrinterDevice) {
        viewModelScope.launch {
            _uiState.update { it.copy(isConnecting = true, printerMessage = null) }

            // Test actual connection by sending an empty ESC/POS init command
            val initCmd = byteArrayOf(0x1B, 0x40) // ESC @ = initialize printer
            val result = printerManager.printViaBluetooth(device.address, initCmd)

            when (result) {
                is PrintResult.Success -> {
                    settingsStore.printerType = SettingsStore.TYPE_BLUETOOTH
                    settingsStore.printerName = device.name
                    settingsStore.printerAddress = device.address
                    _uiState.update {
                        it.copy(
                            isConnecting = false,
                            isConnected = true,
                            printerType = SettingsStore.TYPE_BLUETOOTH,
                            savedPrinterName = device.name,
                            savedPrinterAddress = device.address,
                            printerMessage = "Printer \"${device.name}\" berhasil terhubung dan tersimpan"
                        )
                    }
                }
                is PrintResult.Error -> {
                    _uiState.update {
                        it.copy(
                            isConnecting = false,
                            isConnected = false,
                            printerMessage = "Gagal terhubung ke \"${device.name}\": ${result.message}"
                        )
                    }
                }
            }
        }
    }

    fun disconnectPrinter() {
        settingsStore.clearPrinter()
        _uiState.update {
            it.copy(
                savedPrinterName = "",
                savedPrinterAddress = "",
                networkIp = "",
                isConnected = false,
                printerMessage = "Printer telah diputus dan dihapus"
            )
        }
    }

    // ── Print mode ───────────────────────────────────────────────────────────

    fun setPrintMode(mode: PrintMode) {
        settingsStore.printMode = mode.value
        _uiState.update { it.copy(printMode = mode) }
    }

    // ── Kitchen printer ──────────────────────────────────────────────────────

    fun setKitchenPrinterType(type: String) {
        _uiState.update { it.copy(kitchenPrinterType = type) }
    }

    fun selectKitchenBluetoothPrinter(device: PrinterDevice) {
        viewModelScope.launch {
            _uiState.update { it.copy(isConnecting = true, printerMessage = null) }
            val initCmd = byteArrayOf(0x1B, 0x40)
            val result = printerManager.printViaBluetooth(device.address, initCmd)
            when (result) {
                is PrintResult.Success -> {
                    settingsStore.kitchenPrinterType = SettingsStore.TYPE_BLUETOOTH
                    settingsStore.kitchenPrinterName = device.name
                    settingsStore.kitchenPrinterAddress = device.address
                    _uiState.update {
                        it.copy(
                            isConnecting = false,
                            kitchenPrinterType = SettingsStore.TYPE_BLUETOOTH,
                            kitchenPrinterName = device.name,
                            kitchenPrinterAddress = device.address,
                            printerMessage = "Printer dapur \"${device.name}\" berhasil terhubung"
                        )
                    }
                }
                is PrintResult.Error -> {
                    _uiState.update {
                        it.copy(
                            isConnecting = false,
                            printerMessage = "Gagal terhubung ke printer dapur \"${device.name}\": ${result.message}"
                        )
                    }
                }
            }
        }
    }

    fun saveKitchenNetworkPrinter() {
        val ip = _uiState.value.kitchenNetworkIp.trim()
        val port = _uiState.value.kitchenNetworkPort.trim().toIntOrNull() ?: 9100
        if (ip.isBlank()) {
            _uiState.update { it.copy(printerMessage = "Masukkan alamat IP printer dapur") }
            return
        }
        settingsStore.kitchenPrinterType = SettingsStore.TYPE_NETWORK
        settingsStore.kitchenNetworkPrinterIp = ip
        settingsStore.kitchenNetworkPrinterPort = port
        settingsStore.kitchenPrinterName = "Kitchen ($ip:$port)"
        settingsStore.kitchenPrinterAddress = ip
        _uiState.update {
            it.copy(
                kitchenPrinterType = SettingsStore.TYPE_NETWORK,
                kitchenPrinterName = "Kitchen ($ip:$port)",
                kitchenPrinterAddress = ip,
                printerMessage = "Printer dapur jaringan tersimpan"
            )
        }
    }

    fun setKitchenNetworkIp(ip: String) {
        _uiState.update { it.copy(kitchenNetworkIp = ip) }
    }

    fun setKitchenNetworkPort(port: String) {
        _uiState.update { it.copy(kitchenNetworkPort = port) }
    }

    fun disconnectKitchenPrinter() {
        settingsStore.clearKitchenPrinter()
        _uiState.update {
            it.copy(
                kitchenPrinterName = "",
                kitchenPrinterAddress = "",
                kitchenNetworkIp = "",
                printerMessage = "Printer dapur telah diputus dan dihapus"
            )
        }
    }

    // ── Network printer ──────────────────────────────────────────────────────

    fun setNetworkIp(ip: String) {
        _uiState.update { it.copy(networkIp = ip) }
    }

    fun setNetworkPort(port: String) {
        _uiState.update { it.copy(networkPort = port) }
    }

    fun saveNetworkPrinter() {
        val ip = _uiState.value.networkIp.trim()
        val port = _uiState.value.networkPort.trim().toIntOrNull() ?: 9100
        if (ip.isBlank()) {
            _uiState.update { it.copy(printerMessage = "Masukkan alamat IP printer") }
            return
        }
        settingsStore.printerType = SettingsStore.TYPE_NETWORK
        settingsStore.networkPrinterIp = ip
        settingsStore.networkPrinterPort = port
        settingsStore.printerName = "Network ($ip:$port)"
        settingsStore.printerAddress = ip
        _uiState.update {
            it.copy(
                printerType = SettingsStore.TYPE_NETWORK,
                savedPrinterName = "Network ($ip:$port)",
                savedPrinterAddress = ip,
                printerMessage = "Printer jaringan tersimpan"
            )
        }
    }

    // ── Test print ───────────────────────────────────────────────────────────

    fun testPrint() {
        viewModelScope.launch {
            _uiState.update { it.copy(isPrinting = true, printerMessage = null) }
            val state = _uiState.value

            val testReceipt = ReceiptData(
                storeName = state.storeName.ifBlank { "Rancak POS" },
                storeAddress = state.storeAddress.ifBlank { null },
                storePhone = state.storePhone.ifBlank { null },
                invoiceNo = "TEST-001",
                orderType = "Dine In",
                tableName = "Meja 1",
                cashierName = "Test",
                createdAt = "15/04/2026 12:00",
                items = listOf(
                    id.rancak.app.data.printing.ReceiptItem(
                        name = "Nasi Goreng",
                        qty = 2,
                        price = 35000,
                        subtotal = 70000
                    )
                ),
                subtotal = 70000,
                total = 70000,
                paymentMethod = "Tunai",
                paidAmount = 100000,
                changeAmount = 30000,
                footerText = state.footerText.ifBlank { null }
            )

            val testKot = KitchenTicketData(
                storeName = state.storeName.ifBlank { "Rancak POS" },
                invoiceNo = "TEST-001",
                orderType = "Dine In",
                tableName = "Meja 1",
                cashierName = "Test",
                createdAt = "15/04/2026 12:00",
                items = listOf(
                    KitchenTicketItem(name = "Nasi Goreng", qty = 2, note = "pedas level 3")
                )
            )

            val data = when (state.printMode) {
                PrintMode.RECEIPT_ONLY -> EscPosBuilder.buildReceipt(testReceipt)
                PrintMode.SINGLE_KOT_FIRST -> EscPosBuilder.buildCombinedReceipt(testReceipt, testKot, kotFirst = true)
                PrintMode.SINGLE_RECEIPT_FIRST -> EscPosBuilder.buildCombinedReceipt(testReceipt, testKot, kotFirst = false)
                PrintMode.DUAL_PRINTER -> EscPosBuilder.buildReceipt(testReceipt) // test cashier printer only
            }

            val result = if (state.printerType == SettingsStore.TYPE_BLUETOOTH) {
                if (state.savedPrinterAddress.isBlank()) {
                    PrintResult.Error("Belum ada printer Bluetooth tersimpan")
                } else {
                    printerManager.printViaBluetooth(state.savedPrinterAddress, data)
                }
            } else {
                val ip = state.networkIp.ifBlank { state.savedPrinterAddress }
                val port = state.networkPort.toIntOrNull() ?: 9100
                if (ip.isBlank()) {
                    PrintResult.Error("Belum ada IP printer tersimpan")
                } else {
                    printerManager.printViaNetwork(ip, port, data)
                }
            }

            val message = when (result) {
                is PrintResult.Success -> "Test print berhasil!"
                is PrintResult.Error -> "Gagal print: ${result.message}"
            }
            _uiState.update { it.copy(isPrinting = false, printerMessage = message) }
        }
    }

    // ── Receipt settings ─────────────────────────────────────────────────────

    fun setStoreName(value: String) {
        settingsStore.receiptStoreName = value
        _uiState.update { it.copy(storeName = value) }
    }

    fun setStoreAddress(value: String) {
        settingsStore.receiptStoreAddress = value
        _uiState.update { it.copy(storeAddress = value) }
    }

    fun setStorePhone(value: String) {
        settingsStore.receiptStorePhone = value
        _uiState.update { it.copy(storePhone = value) }
    }

    fun setFooterText(value: String) {
        settingsStore.receiptFooter = value
        _uiState.update { it.copy(footerText = value) }
        pushReceiptSettings()
    }

    fun setMerchantQrisString(value: String) {
        settingsStore.merchantQrisString = value
        _uiState.update { it.copy(merchantQrisString = value) }
    }

    // ── General settings ─────────────────────────────────────────────────────

    fun setAutoPrint(enabled: Boolean) {
        settingsStore.autoPrintReceipt = enabled
        _uiState.update { it.copy(autoPrint = enabled) }
        pushAppConfig("auto_print_receipt", if (enabled) "true" else "false")
    }

    fun setPaperWidth(width: Int) {
        settingsStore.paperWidth = width
        _uiState.update { it.copy(paperWidth = width) }
        pushAppConfig("paper_width_mm", width.toString())
    }

    fun setReceiptCopies(copies: Int) {
        val safe = copies.coerceIn(1, 3)
        settingsStore.receiptCopies = safe
        _uiState.update { it.copy(receiptCopies = safe) }
        pushAppConfig("receipt_copies", safe.toString())
    }

    fun setAutoPrintQueue(enabled: Boolean) {
        settingsStore.autoPrintQueue = enabled
        _uiState.update { it.copy(autoPrintQueue = enabled) }
        pushAppConfig("auto_print_queue", if (enabled) "true" else "false")
    }

    fun setShowLogo(enabled: Boolean) {
        settingsStore.receiptShowLogo = enabled
        _uiState.update { it.copy(showLogo = enabled) }
    }

    // ── Receipt API settings ─────────────────────────────────────────────────

    fun setReceiptHeader(value: String) {
        settingsStore.receiptHeader = value
        _uiState.update { it.copy(receiptHeader = value) }
        pushReceiptSettings()
    }

    fun setReceiptFooter2(value: String) {
        settingsStore.receiptFooter2 = value
        _uiState.update { it.copy(receiptFooter2 = value) }
        pushReceiptSettings()
    }

    fun setSeparatorStyle(value: String) {
        settingsStore.receiptSeparatorStyle = value
        _uiState.update { it.copy(separatorStyle = value) }
        pushReceiptSettings()
    }

    fun setSeparatorCount(value: Int) {
        settingsStore.receiptSeparatorCount = value
        _uiState.update { it.copy(separatorCount = value) }
        pushReceiptSettings()
    }

    fun setReceiptNameSize(value: String) {
        settingsStore.receiptNameSize = value
        _uiState.update { it.copy(receiptNameSize = value) }
        pushReceiptSettings()
    }

    fun setLogoPosition(value: String) {
        settingsStore.receiptLogoPosition = value
        _uiState.update { it.copy(logoPosition = value) }
        pushReceiptSettings()
    }

    fun setLogoSizePct(value: Int) {
        settingsStore.receiptLogoSizePct = value
        _uiState.update { it.copy(logoSizePct = value) }
        pushReceiptSettings()
    }

    fun setFooterPosition(value: String) {
        settingsStore.receiptFooterPosition = value
        _uiState.update { it.copy(footerPosition = value) }
        pushReceiptSettings()
    }

    fun setReceiptInstagram(value: String) {
        settingsStore.receiptInstagram = value
        _uiState.update { it.copy(receiptInstagram = value) }
        pushReceiptSettings()
    }

    fun setReceiptFacebook(value: String) {
        settingsStore.receiptFacebook = value
        _uiState.update { it.copy(receiptFacebook = value) }
        pushReceiptSettings()
    }

    fun setReceiptWifiSsid(value: String) {
        settingsStore.receiptWifiSsid = value
        _uiState.update { it.copy(receiptWifiSsid = value) }
        pushReceiptSettings()
    }

    fun setReceiptWifiPassword(value: String) {
        settingsStore.receiptWifiPassword = value
        _uiState.update { it.copy(receiptWifiPassword = value) }
        pushReceiptSettings()
    }

    fun setReceiptEmail(value: String) {
        settingsStore.receiptEmail = value
        _uiState.update { it.copy(receiptEmail = value) }
        pushReceiptSettings()
    }

    fun setReceiptWebsite(value: String) {
        settingsStore.receiptWebsite = value
        _uiState.update { it.copy(receiptWebsite = value) }
        pushReceiptSettings()
    }

    fun setReceiptNpwp(value: String) {
        settingsStore.receiptNpwp = value
        _uiState.update { it.copy(receiptNpwp = value) }
        pushReceiptSettings()
    }

    fun clearMessage() {
        _uiState.update { it.copy(printerMessage = null) }
    }
}

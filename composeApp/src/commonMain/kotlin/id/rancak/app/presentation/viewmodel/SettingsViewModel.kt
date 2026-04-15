package id.rancak.app.presentation.viewmodel

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
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

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

    // General
    val autoPrint: Boolean = false,
    val paperWidth: Int = 58
) {
    val hasPrinter: Boolean
        get() = savedPrinterName.isNotBlank() && savedPrinterAddress.isNotBlank()

    val hasKitchenPrinter: Boolean
        get() = kitchenPrinterName.isNotBlank() && kitchenPrinterAddress.isNotBlank()
}

class SettingsViewModel(
    private val settingsStore: SettingsStore,
    private val printerManager: PrinterManager
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
            autoPrint = settingsStore.autoPrintReceipt,
            paperWidth = settingsStore.paperWidth,
            isBluetoothOn = try { printerManager.isBluetoothEnabled() } catch (_: Exception) { false }
        )
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
    }

    // ── General settings ─────────────────────────────────────────────────────

    fun setAutoPrint(enabled: Boolean) {
        settingsStore.autoPrintReceipt = enabled
        _uiState.update { it.copy(autoPrint = enabled) }
    }

    fun setPaperWidth(width: Int) {
        settingsStore.paperWidth = width
        _uiState.update { it.copy(paperWidth = width) }
    }

    fun clearMessage() {
        _uiState.update { it.copy(printerMessage = null) }
    }
}

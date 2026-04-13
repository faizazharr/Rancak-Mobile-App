package id.rancak.app.data.printing

/**
 * Cross-platform ESC/POS printer manager.
 *
 * Android implementation ([PrinterManager.android.kt]):
 *   - Bluetooth Classic (SPP/RFCOMM) — all paired Bluetooth printers
 *   - TCP/IP via Java Socket to port 9100
 *
 * iOS implementation ([PrinterManager.ios.kt]):
 *   - Bluetooth LE (BLE via CoreBluetooth) — modern Bluetooth thermal printers
 *   - TCP/IP via CFStreamCreatePairWithSocketToHost (Foundation) to port 9100
 *
 * Receipt bytes are built by [EscPosBuilder] which is pure Kotlin (commonMain).
 * Both platforms receive identical byte arrays and send them to the printer.
 *
 * Usage:
 * ```kotlin
 * val receipt = EscPosBuilder.buildReceipt(receiptData)
 *
 * // Network printer (both platforms)
 * printerManager.printViaNetwork("192.168.1.100", 9100, receipt)
 *
 * // Bluetooth (platform-specific, same API)
 * printerManager.printViaBluetooth(device.address, receipt)
 * ```
 */
expect class PrinterManager() {

    // ── Network (TCP/IP) ── identical on both platforms ──────────────────────

    /**
     * Print via Wi-Fi/LAN.
     * @param ipAddress Printer IP (e.g. "192.168.1.100")
     * @param port      TCP port — almost always 9100 for ESC/POS printers
     * @param data      Raw ESC/POS bytes from [EscPosBuilder.buildReceipt]
     */
    suspend fun printViaNetwork(
        ipAddress: String,
        port: Int = 9100,
        data: ByteArray
    ): PrintResult

    // ── Bluetooth ── platform-specific implementation ─────────────────────────

    /**
     * Scan / return available Bluetooth printers.
     * Android: returns already-paired SPP devices.
     * iOS: scans for BLE peripherals advertising ESC/POS service UUIDs.
     */
    suspend fun getBluetoothPrinters(): List<PrinterDevice>

    /**
     * Print via Bluetooth.
     * Android: uses RFCOMM (Bluetooth Classic).
     * iOS: uses CoreBluetooth BLE GATT write.
     * @param address On Android: MAC "AA:BB:CC:DD:EE:FF".
     *                On iOS: CBPeripheral UUID string.
     */
    suspend fun printViaBluetooth(
        address: String,
        data: ByteArray
    ): PrintResult
}

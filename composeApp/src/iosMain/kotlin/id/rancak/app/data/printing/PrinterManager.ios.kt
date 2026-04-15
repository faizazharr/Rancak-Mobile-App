package id.rancak.app.data.printing

import kotlinx.cinterop.*
import kotlinx.coroutines.*
import platform.CoreBluetooth.*
import platform.Foundation.*
import platform.posix.*

/**
 * iOS ESC/POS printer manager.
 *
 * ══════════════════════════════════════════════════════════════════
 * SETUP WAJIB DI XCODE (sebelum build):
 *
 * 1. Tambahkan ke Info.plist:
 *      <key>NSBluetoothAlwaysUsageDescription</key>
 *      <string>Dibutuhkan untuk print struk via Bluetooth printer</string>
 *      <key>NSBluetoothPeripheralUsageDescription</key>
 *      <string>Dibutuhkan untuk print struk via Bluetooth printer</string>
 *      <key>NSLocalNetworkUsageDescription</key>
 *      <string>Dibutuhkan untuk print struk via printer jaringan (Wi-Fi/LAN)</string>
 *
 * 2. Signing & Capabilities → add "Background Modes":
 *      ✅ Uses Bluetooth LE accessories
 * ══════════════════════════════════════════════════════════════════
 *
 * TCP/IP (Wi-Fi) — printer di meja kasir atau jaringan LAN:
 *   • Implementasi dengan POSIX socket (blocking, timeout via SO_SNDTIMEO)
 *   • Kirim raw ESC/POS bytes ke IP:9100 — sama persis dengan Android
 *   • Tidak perlu entitlement khusus selain NSLocalNetworkUsageDescription
 *
 * Bluetooth LE (BLE) — printer portable:
 *   • CoreBluetooth CBCentralManager
 *   • Kompatibel: Epson TM-P20/P80, Star SM-L200/mPOP, XPrinter XP-P300, Rongta RPP02N
 *   • Data dikirim dalam chunks 180 byte (kompatibel universal)
 *
 * ⚠️  Bluetooth Classic (SPP) TIDAK bisa di iOS tanpa sertifikasi MFi Apple.
 *     Kalau printer hanya punya SPP, gunakan mode Wi-Fi printer tersebut.
 */
actual class PrinterManager actual constructor() {

    // ── TCP/IP via POSIX socket ───────────────────────────────────────────────
    //
    // Menggunakan POSIX socket API (bukan CFStream) karena:
    // - Blocking I/O dengan timeout SO_SNDTIMEO tidak perlu delay() di dalam memScoped
    // - Lebih prediktable untuk native memory lifecycle
    // - Tersedia di semua versi iOS tanpa perlu import tambahan

    @OptIn(ExperimentalForeignApi::class)
    actual suspend fun printViaNetwork(
        ipAddress: String,
        port: Int,
        data: ByteArray
    ): PrintResult = withContext(Dispatchers.Default) {

        // withTimeoutOrNull handles connect timeout di level coroutine,
        // SO_SNDTIMEO handles write timeout di level socket.
        withTimeoutOrNull(10_000L) {
            posixPrint(ipAddress, port, data)
        } ?: PrintResult.Error("Timeout koneksi ke printer $ipAddress:$port")
    }

    @OptIn(ExperimentalForeignApi::class)
    private fun posixPrint(ipAddress: String, port: Int, data: ByteArray): PrintResult {
        // 1. Buat socket
        val sockfd = socket(AF_INET, SOCK_STREAM, 0)
        if (sockfd < 0) return PrintResult.Error("Gagal membuat socket (errno $errno)")

        return memScoped {
            // 2. Set timeout kirim 8 detik
            val tv = alloc<timeval>()
            tv.tv_sec = 8
            tv.tv_usec = 0
            setsockopt(sockfd, SOL_SOCKET, SO_SNDTIMEO,
                tv.ptr, sizeOf<timeval>().convert())

            // 3. Siapkan alamat tujuan
            val serverAddr = alloc<sockaddr_in>()
            serverAddr.sin_family = AF_INET.convert()
            // htons: konversi port ke network byte order
            serverAddr.sin_port = ((port and 0xFF) shl 8 or ((port ushr 8) and 0xFF))
                .toUShort()
            // inet_pton: konversi string IP ke binary
            val inetResult = inet_pton(
                AF_INET,
                ipAddress,
                serverAddr.sin_addr.ptr
            )
            if (inetResult <= 0) {
                close(sockfd)
                return@memScoped PrintResult.Error("Alamat IP tidak valid: $ipAddress")
            }

            // 4. Connect (blocking — timeout dari withTimeoutOrNull di atas)
            val connectResult = connect(
                sockfd,
                serverAddr.ptr.reinterpret(),
                sizeOf<sockaddr_in>().convert()
            )
            if (connectResult < 0) {
                close(sockfd)
                return@memScoped PrintResult.Error(
                    "Tidak bisa konek ke $ipAddress:$port (errno $errno). " +
                    "Pastikan printer menyala dan terhubung ke jaringan yang sama."
                )
            }

            // 5. Kirim semua data ESC/POS (send bisa partial, loop sampai selesai)
            var offset = 0
            while (offset < data.size) {
                val sent = data.usePinned { pinned ->
                    send(
                        sockfd,
                        pinned.addressOf(offset),
                        (data.size - offset).convert(),
                        0
                    )
                }
                when {
                    sent.toInt() < 0 -> {
                        close(sockfd)
                        return@memScoped PrintResult.Error(
                            "Gagal mengirim data ke printer (errno $errno)"
                        )
                    }
                    else -> offset += sent.toInt()
                }
            }

            // 6. Tutup koneksi
            close(sockfd)
            PrintResult.Success
        }
    }

    // ── Bluetooth LE via CoreBluetooth ────────────────────────────────────────

    private val bleDelegate = BleCentralDelegate()

    actual fun isBluetoothEnabled(): Boolean {
        // CBCentralManager state check — requires an initialized manager
        return bleDelegate.isBtPoweredOn()
    }

    actual suspend fun getBluetoothPrinters(): List<PrinterDevice> =
        bleDelegate.scanForPrinters()

    actual suspend fun printViaBluetooth(
        address: String,
        data: ByteArray
    ): PrintResult = bleDelegate.printToPeripheral(address, data)
}

// ─────────────────────────────────────────────────────────────────────────────
// BLE delegate — scan, connect, dan chunked write ke printer
// ─────────────────────────────────────────────────────────────────────────────
//
// Flow BLE printing:
//   1. Scan → kumpulkan CBPeripheral yang terdeteksi (filter yang punya nama)
//   2. Connect → CBCentralManager.connect(peripheral)
//   3. Discover services → peripheral.discoverServices(nil)
//   4. Discover characteristics → peripheral.discoverCharacteristics(nil, service)
//   5. Find writable characteristic (Write or WriteWithoutResponse property)
//   6. Chunked write → kirim 180-byte chunks satu per satu sampai selesai
//   7. Disconnect → centralManager.cancelPeripheralConnection(peripheral)

@OptIn(ExperimentalForeignApi::class)
private class BleCentralDelegate :
    NSObject(),
    CBCentralManagerDelegateProtocol,
    CBPeripheralDelegateProtocol {

    private var central: CBCentralManager? = null
    private val scanned = mutableListOf<CBPeripheral>()

    private var scanJob:  CompletableDeferred<List<PrinterDevice>>? = null
    private var printJob: CompletableDeferred<PrintResult>? = null

    private var writeChar:    CBCharacteristic? = null
    private var pendingBytes: ByteArray? = null
    private var writeOffset = 0
    private val CHUNK_SIZE  = 180   // 180B universal; some printers support 512B

    fun isBtPoweredOn(): Boolean {
        return central?.state == CBManagerStatePoweredOn
    }

    // ── Scan ──────────────────────────────────────────────────────────────────

    suspend fun scanForPrinters(): List<PrinterDevice> {
        val job = CompletableDeferred<List<PrinterDevice>>()
        scanJob = job
        scanned.clear()

        // CBCentralManager harus dibuat di main thread pada iOS
        withContext(Dispatchers.Main) {
            central = CBCentralManager(delegate = this@BleCentralDelegate, queue = null)
        }

        // Tunggu 5 detik lalu stop scan
        delay(5_000)
        if (!job.isCompleted) {
            central?.stopScan()
            job.complete(
                scanned.map {
                    PrinterDevice(
                        name    = it.name ?: "BLE Printer",
                        address = it.identifier.UUIDString,
                        type    = PrinterConnectionType.BLUETOOTH
                    )
                }
            )
        }
        return job.await()
    }

    // ── Print ──────────────────────────────────────────────────────────────────

    suspend fun printToPeripheral(uuid: String, data: ByteArray): PrintResult {
        val peripheral = scanned.firstOrNull { it.identifier.UUIDString == uuid }
            ?: return PrintResult.Error(
                "Printer $uuid tidak ditemukan — panggil getBluetoothPrinters() dulu"
            )

        val job = CompletableDeferred<PrintResult>()
        printJob  = job
        writeChar = null
        pendingBytes = data
        writeOffset  = 0

        peripheral.delegate = this
        central?.connectPeripheral(peripheral, options = null)

        // Timeout keseluruhan proses connect + print = 20 detik
        return withTimeoutOrNull(20_000L) { job.await() }
            ?: run {
                central?.cancelPeripheralConnection(peripheral)
                PrintResult.Error("BLE print timeout setelah 20 detik")
            }
    }

    // ── CBCentralManagerDelegate ──────────────────────────────────────────────

    override fun centralManagerDidUpdateState(central: CBCentralManager) {
        when (central.state) {
            CBManagerStatePoweredOn  -> {
                // Sudah siap scan — scanForPrinters() akan trigger ini
                if (scanJob?.isCompleted == false) {
                    central.scanForPeripheralsWithServices(null, null)
                }
            }
            CBManagerStatePoweredOff -> {
                val msg = "Bluetooth tidak aktif — aktifkan di Settings > Bluetooth"
                scanJob?.complete(emptyList())
                printJob?.complete(PrintResult.Error(msg))
            }
            CBManagerStateUnauthorized -> {
                val msg = "Izin Bluetooth belum diberikan — buka Settings > Privacy > Bluetooth"
                scanJob?.complete(emptyList())
                printJob?.complete(PrintResult.Error(msg))
            }
            CBManagerStateUnsupported -> {
                val msg = "Perangkat ini tidak mendukung Bluetooth LE"
                scanJob?.complete(emptyList())
                printJob?.complete(PrintResult.Error(msg))
            }
            else -> { /* CBManagerStateUnknown / Resetting — tunggu */ }
        }
    }

    override fun centralManager(
        central: CBCentralManager,
        didDiscoverPeripheral: CBPeripheral,
        advertisementData: Map<Any?, *>,
        RSSI: NSNumber
    ) {
        // Hanya tambahkan printer yang punya nama (filter noise dari perangkat lain)
        if (didDiscoverPeripheral.name != null &&
            !scanned.any { it.identifier.UUIDString == didDiscoverPeripheral.identifier.UUIDString }
        ) {
            scanned.add(didDiscoverPeripheral)
        }
    }

    override fun centralManager(
        central: CBCentralManager,
        didConnectPeripheral: CBPeripheral
    ) {
        // Koneksi berhasil → cari semua service
        didConnectPeripheral.discoverServices(null)
    }

    override fun centralManager(
        central: CBCentralManager,
        didFailToConnectPeripheral: CBPeripheral,
        error: NSError?
    ) {
        printJob?.complete(
            PrintResult.Error("Gagal konek ke printer BLE: ${error?.localizedDescription ?: "unknown"}")
        )
    }

    override fun centralManager(
        central: CBCentralManager,
        didDisconnectPeripheral: CBPeripheral,
        error: NSError?
    ) {
        // Disconnect setelah print selesai = normal
        // Jika printJob belum selesai dan ada error = koneksi putus di tengah print
        if (printJob?.isCompleted == false) {
            printJob?.complete(PrintResult.Error("Koneksi Bluetooth terputus saat print"))
        }
    }

    // ── CBPeripheralDelegate ──────────────────────────────────────────────────

    override fun peripheral(
        peripheral: CBPeripheral,
        didDiscoverServices: NSError?
    ) {
        if (didDiscoverServices != null) {
            printJob?.complete(
                PrintResult.Error("Gagal temukan service printer: ${didDiscoverServices.localizedDescription}")
            )
            return
        }
        // Cari characteristic di semua service
        peripheral.services?.forEach { service ->
            peripheral.discoverCharacteristics(null, service as CBService)
        }
    }

    override fun peripheral(
        peripheral: CBPeripheral,
        didDiscoverCharacteristicsForService: CBService,
        error: NSError?
    ) {
        if (error != null) {
            printJob?.complete(
                PrintResult.Error("Gagal temukan characteristic: ${error.localizedDescription}")
            )
            return
        }
        // Sudah ada dari service sebelumnya
        if (writeChar != null) return

        // Cari characteristic dengan property Write atau WriteWithoutResponse
        val writable = didDiscoverCharacteristicsForService.characteristics
            ?.filterIsInstance<CBCharacteristic>()
            ?.firstOrNull { char ->
                (char.properties and CBCharacteristicPropertyWriteWithoutResponse != 0uL) ||
                (char.properties and CBCharacteristicPropertyWrite != 0uL)
            }

        if (writable != null) {
            writeChar = writable
            sendNextChunk(peripheral)
        } else if (didDiscoverCharacteristicsForService == peripheral.services?.lastOrNull()) {
            // Sudah cek semua service, tidak ada writable characteristic
            printJob?.complete(
                PrintResult.Error(
                    "Printer tidak punya writable BLE characteristic. " +
                    "Coba gunakan mode Wi-Fi printer."
                )
            )
        }
    }

    override fun peripheral(
        peripheral: CBPeripheral,
        didWriteValueForCharacteristic: CBCharacteristic,
        error: NSError?
    ) {
        if (error != null) {
            central?.cancelPeripheralConnection(peripheral)
            printJob?.complete(PrintResult.Error("Error saat mengirim data: ${error.localizedDescription}"))
            return
        }
        // Chunk sebelumnya berhasil → kirim chunk berikutnya
        sendNextChunk(peripheral)
    }

    // ── Chunked write ──────────────────────────────────────────────────────────
    //
    // BLE MTU biasanya 20–512 byte. Kita pakai 180 byte per chunk agar
    // kompatibel dengan semua printer tanpa perlu negotiate MTU.

    private fun sendNextChunk(peripheral: CBPeripheral) {
        val data = pendingBytes ?: return
        val char = writeChar   ?: return

        if (writeOffset >= data.size) {
            // Semua data terkirim → disconnect dan selesaikan
            central?.cancelPeripheralConnection(peripheral)
            printJob?.complete(PrintResult.Success)
            return
        }

        val chunkEnd  = minOf(writeOffset + CHUNK_SIZE, data.size)
        val chunk     = data.copyOfRange(writeOffset, chunkEnd)
        writeOffset   = chunkEnd

        val nsData: NSData = chunk.usePinned { pinned ->
            NSData.dataWithBytes(pinned.addressOf(0), chunk.size.toULong())
        }

        // WriteWithoutResponse → tidak ada ACK, langsung kirim chunk berikutnya
        // WriteWithResponse    → tunggu callback didWriteValueForCharacteristic
        val writeType =
            if (char.properties and CBCharacteristicPropertyWriteWithoutResponse != 0uL)
                CBCharacteristicWriteWithoutResponse
            else
                CBCharacteristicWriteWithResponse

        peripheral.writeValue(nsData, char, writeType)

        if (writeType == CBCharacteristicWriteWithoutResponse) {
            sendNextChunk(peripheral)
        }
    }
}

package id.rancak.app.data.printing

import android.annotation.SuppressLint
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothManager
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.content.ContextCompat
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.OutputStream
import java.net.InetSocketAddress
import java.net.Socket

/**
 * Android ESC/POS printer manager.
 *
 * Bluetooth Classic (SPP/RFCOMM):
 *   - Works with virtually all Bluetooth thermal printers (XP-58, Epson TM-T20, etc.)
 *   - Uses the standard SPP UUID: 00001101-0000-1000-8000-00805F9B34FB
 *   - Only paired devices are returned (pair in Android Bluetooth Settings first)
 *
 * TCP/IP (Wi-Fi):
 *   - Sends raw bytes to IP:port (default 9100) via Java Socket
 *   - Printer must be on the same LAN/Wi-Fi network
 */
actual class PrinterManager actual constructor() {

    private var appContext: Context? = null

    fun init(context: Context) {
        appContext = context.applicationContext
    }

    private fun getAdapter(): BluetoothAdapter? {
        val ctx = appContext ?: return null
        val manager = ctx.getSystemService(Context.BLUETOOTH_SERVICE) as? BluetoothManager
        return manager?.adapter
    }

    private fun hasBluetoothPermission(): Boolean {
        val ctx = appContext ?: return false
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            ContextCompat.checkSelfPermission(
                ctx, android.Manifest.permission.BLUETOOTH_CONNECT
            ) == PackageManager.PERMISSION_GRANTED
        } else {
            true // BLUETOOTH_CONNECT not required below API 31
        }
    }

    // ── TCP/IP ──────────────────────────────────────────────────────────────

    actual suspend fun printViaNetwork(
        ipAddress: String,
        port: Int,
        data: ByteArray
    ): PrintResult = withContext(Dispatchers.IO) {
        try {
            val socket = Socket()
            socket.connect(InetSocketAddress(ipAddress, port), CONNECT_TIMEOUT_MS)
            socket.soTimeout = WRITE_TIMEOUT_MS

            val out: OutputStream = socket.getOutputStream()
            out.write(data)
            out.flush()
            out.close()
            socket.close()

            PrintResult.Success
        } catch (e: Exception) {
            PrintResult.Error("TCP print failed: ${e.message}")
        }
    }

    // ── Bluetooth Classic (SPP) ──────────────────────────────────────────────

    @SuppressLint("MissingPermission")
    actual suspend fun getBluetoothPrinters(): List<PrinterDevice> =
        withContext(Dispatchers.IO) {
            if (!hasBluetoothPermission()) return@withContext emptyList()

            val adapter = getAdapter() ?: return@withContext emptyList()
            if (!adapter.isEnabled) return@withContext emptyList()

            adapter.bondedDevices.map { device ->
                PrinterDevice(
                    name    = device.name ?: device.address,
                    address = device.address,
                    type    = PrinterConnectionType.BLUETOOTH
                )
            }
        }

    @SuppressLint("MissingPermission")
    actual suspend fun printViaBluetooth(
        address: String,
        data: ByteArray
    ): PrintResult = withContext(Dispatchers.IO) {
        if (!hasBluetoothPermission()) {
            return@withContext PrintResult.Error("Izin Bluetooth (BLUETOOTH_CONNECT) belum diberikan")
        }
        val adapter = getAdapter()
            ?: return@withContext PrintResult.Error("Bluetooth not available on this device")

        if (!adapter.isEnabled) {
            return@withContext PrintResult.Error("Bluetooth is disabled — please enable it in Settings")
        }

        val device: BluetoothDevice? = adapter.bondedDevices
            .firstOrNull { it.address.equals(address, ignoreCase = true) }

        if (device == null) {
            return@withContext PrintResult.Error(
                "Printer $address not found — pair it in Android Bluetooth Settings first"
            )
        }

        return@withContext try {
            val socket = device.createRfcommSocketToServiceRecord(SPP_UUID)
            adapter.cancelDiscovery()  // Speeds up connection
            socket.connect()

            val out: OutputStream = socket.outputStream
            out.write(data)
            out.flush()
            out.close()
            socket.close()

            PrintResult.Success
        } catch (e: Exception) {
            PrintResult.Error("Bluetooth print failed: ${e.message}")
        }
    }

    companion object {
        private val SPP_UUID = java.util.UUID.fromString("00001101-0000-1000-8000-00805F9B34FB")
        private const val CONNECT_TIMEOUT_MS = 5_000
        private const val WRITE_TIMEOUT_MS   = 10_000
    }
}

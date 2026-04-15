package id.rancak.app.presentation.components

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import id.rancak.app.data.local.SettingsStore
import id.rancak.app.data.printing.EscPosBuilder
import id.rancak.app.data.printing.PrintMode
import id.rancak.app.data.printing.PrintResult
import id.rancak.app.data.printing.PrinterManager
import id.rancak.app.data.printing.toKitchenTicketData
import id.rancak.app.data.printing.toReceiptData
import id.rancak.app.domain.model.Sale
import kotlinx.coroutines.async
import kotlinx.coroutines.launch

internal enum class PrintDialogTab { BLUETOOTH, NETWORK }

internal suspend fun sendToPrinter(
    printerManager: PrinterManager,
    type: String,
    btAddress: String,
    networkIp: String,
    networkPort: Int,
    data: ByteArray
): PrintResult = if (type == SettingsStore.TYPE_BLUETOOTH) {
    printerManager.printViaBluetooth(btAddress, data)
} else {
    printerManager.printViaNetwork(networkIp, networkPort, data)
}

/**
 * Dialog cetak struk — dapat digunakan dari PaymentScreen maupun SalesHistoryScreen.
 *
 * @param sale           Data transaksi yang akan dicetak.
 * @param printerManager Injeksi printer manager.
 * @param settingsStore  Injeksi settings store.
 * @param onDismiss      Dipanggil saat dialog ditutup.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PrintDialog(
    sale: Sale,
    printerManager: PrinterManager,
    settingsStore: SettingsStore,
    onDismiss: () -> Unit
) {
    val scope = rememberCoroutineScope()

    val printMode = PrintMode.from(settingsStore.printMode)

    val hasSavedPrinter  = settingsStore.printerAddress.isNotBlank()
    val savedType        = settingsStore.printerType
    val savedName        = settingsStore.printerName
    val savedAddress     = settingsStore.printerAddress
    val savedNetworkIp   = settingsStore.networkPrinterIp
    val savedNetworkPort = settingsStore.networkPrinterPort

    val hasKitchenPrinter    = settingsStore.hasKitchenPrinter
    val kitchenType          = settingsStore.kitchenPrinterType
    val kitchenAddress       = settingsStore.kitchenPrinterAddress
    val kitchenNetworkIp     = settingsStore.kitchenNetworkPrinterIp
    val kitchenNetworkPort   = settingsStore.kitchenNetworkPrinterPort

    val storeName   = settingsStore.receiptStoreName.ifBlank { "Rancak" }
    val storeAddress = settingsStore.receiptStoreAddress.ifBlank { null }
    val storePhone  = settingsStore.receiptStorePhone.ifBlank { null }
    val footerText  = settingsStore.receiptFooter.ifBlank { null }

    var selectedTab by remember {
        mutableStateOf(
            if (savedType == SettingsStore.TYPE_NETWORK) PrintDialogTab.NETWORK
            else PrintDialogTab.BLUETOOTH
        )
    }
    var networkIp   by remember { mutableStateOf(savedNetworkIp) }
    var isPrinting  by remember { mutableStateOf(false) }
    var printResult by remember { mutableStateOf<PrintResult?>(null) }

    suspend fun executePrint(
        cashierType: String,
        cashierBtAddr: String,
        cashierNetIp: String,
        cashierNetPort: Int
    ): PrintResult {
        val receiptData = sale.toReceiptData(
            storeName    = storeName,
            storeAddress = storeAddress,
            storePhone   = storePhone,
            footerText   = footerText
        )

        return when (printMode) {
            PrintMode.RECEIPT_ONLY -> {
                val bytes = EscPosBuilder.buildReceipt(receiptData)
                sendToPrinter(printerManager, cashierType, cashierBtAddr, cashierNetIp, cashierNetPort, bytes)
            }

            PrintMode.SINGLE_KOT_FIRST, PrintMode.SINGLE_RECEIPT_FIRST -> {
                val kitchenData = sale.toKitchenTicketData(storeName = storeName)
                val kotFirst    = printMode == PrintMode.SINGLE_KOT_FIRST
                val bytes       = EscPosBuilder.buildCombinedReceipt(receiptData, kitchenData, kotFirst)
                sendToPrinter(printerManager, cashierType, cashierBtAddr, cashierNetIp, cashierNetPort, bytes)
            }

            PrintMode.DUAL_PRINTER -> {
                val receiptBytes = EscPosBuilder.buildReceipt(receiptData)
                val kitchenData  = sale.toKitchenTicketData(storeName = storeName)
                val kotBytes     = EscPosBuilder.buildKitchenTicket(kitchenData)

                val cashierDeferred = scope.async {
                    sendToPrinter(printerManager, cashierType, cashierBtAddr, cashierNetIp, cashierNetPort, receiptBytes)
                }
                val kitchenDeferred = if (hasKitchenPrinter) {
                    scope.async {
                        sendToPrinter(printerManager, kitchenType, kitchenAddress,
                            kitchenNetworkIp, kitchenNetworkPort, kotBytes)
                    }
                } else null

                val cashierResult = cashierDeferred.await()
                val kitchenResult = kitchenDeferred?.await()

                when {
                    cashierResult is PrintResult.Error -> cashierResult
                    kitchenResult is PrintResult.Error ->
                        PrintResult.Error("Struk kasir berhasil, tapi gagal cetak KOT dapur: ${kitchenResult.message}")
                    else -> PrintResult.Success
                }
            }
        }
    }

    // Auto-print saat dialog pertama kali dibuka (jika printer tersimpan)
    var autoPrintAttempted by remember { mutableStateOf(false) }
    LaunchedEffect(hasSavedPrinter) {
        if (hasSavedPrinter && !autoPrintAttempted) {
            autoPrintAttempted = true
            isPrinting  = true
            printResult = null
            printResult = executePrint(savedType, savedAddress, savedNetworkIp, savedNetworkPort)
            isPrinting  = false
        }
    }

    AlertDialog(
        onDismissRequest = { if (!isPrinting) onDismiss() },
        icon  = { Icon(Icons.Default.Print, contentDescription = null) },
        title = { Text("Cetak Struk") },
        text  = {
            Column {
                // ── Mode label ────────────────────────────────────────────────
                val modeLabel = when (printMode) {
                    PrintMode.RECEIPT_ONLY         -> null
                    PrintMode.DUAL_PRINTER         -> "Mode: Dua Printer (Kasir + Dapur)"
                    PrintMode.SINGLE_KOT_FIRST     -> "Mode: Satu Printer (KOT dulu)"
                    PrintMode.SINGLE_RECEIPT_FIRST -> "Mode: Satu Printer (Struk dulu)"
                }
                if (modeLabel != null) {
                    Text(modeLabel,
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.padding(bottom = 8.dp))
                }

                // ── Printer tersimpan ─────────────────────────────────────────
                if (hasSavedPrinter) {
                    Surface(
                        color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f),
                        shape = MaterialTheme.shapes.small,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier.padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                if (savedType == SettingsStore.TYPE_BLUETOOTH) Icons.Default.Bluetooth
                                else Icons.Default.Wifi,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(Modifier.width(8.dp))
                            Column(Modifier.weight(1f)) {
                                Text(savedName.ifBlank { savedAddress },
                                    style = MaterialTheme.typography.bodyMedium,
                                    fontWeight = FontWeight.SemiBold)
                                Text(
                                    if (printMode == PrintMode.DUAL_PRINTER) "Printer kasir"
                                    else "Printer tersimpan dari Pengaturan",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                        }
                    }
                    Spacer(Modifier.height(4.dp))
                }

                // ── Kitchen printer info (dual mode) ─────────────────────────
                if (printMode == PrintMode.DUAL_PRINTER && hasKitchenPrinter) {
                    Surface(
                        color = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.3f),
                        shape = MaterialTheme.shapes.small,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier.padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                if (kitchenType == SettingsStore.TYPE_BLUETOOTH) Icons.Default.Bluetooth
                                else Icons.Default.Wifi,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.secondary,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(Modifier.width(8.dp))
                            Column(Modifier.weight(1f)) {
                                Text(settingsStore.kitchenPrinterName.ifBlank { kitchenAddress },
                                    style = MaterialTheme.typography.bodyMedium,
                                    fontWeight = FontWeight.SemiBold)
                                Text("Printer dapur (KOT)",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                        }
                    }
                    Spacer(Modifier.height(4.dp))
                } else if (printMode == PrintMode.DUAL_PRINTER && !hasKitchenPrinter) {
                    Surface(
                        color = MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.3f),
                        shape = MaterialTheme.shapes.small,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier.padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(Icons.Default.Warning, contentDescription = null,
                                tint = MaterialTheme.colorScheme.error, modifier = Modifier.size(18.dp))
                            Spacer(Modifier.width(8.dp))
                            Text("Printer dapur belum diatur. KOT tidak akan dicetak.",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onErrorContainer)
                        }
                    }
                    Spacer(Modifier.height(4.dp))
                }

                Spacer(Modifier.height(8.dp))

                // ── Manual fallback (no saved printer / error) ────────────────
                if (!hasSavedPrinter || printResult is PrintResult.Error) {
                    if (printResult is PrintResult.Error && hasSavedPrinter) {
                        Text("Gagal mencetak ke printer tersimpan. Pilih printer lain atau coba lagi:",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.error,
                            modifier = Modifier.padding(bottom = 8.dp))
                    }

                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        FilterChip(
                            selected = selectedTab == PrintDialogTab.BLUETOOTH,
                            onClick  = { selectedTab = PrintDialogTab.BLUETOOTH; printResult = null },
                            label    = { Text("Bluetooth") },
                            leadingIcon = if (selectedTab == PrintDialogTab.BLUETOOTH) {
                                { Icon(Icons.Default.Bluetooth, contentDescription = null, modifier = Modifier.size(16.dp)) }
                            } else null,
                            modifier = Modifier.weight(1f)
                        )
                        FilterChip(
                            selected = selectedTab == PrintDialogTab.NETWORK,
                            onClick  = { selectedTab = PrintDialogTab.NETWORK; printResult = null },
                            label    = { Text("Wi-Fi / LAN") },
                            leadingIcon = if (selectedTab == PrintDialogTab.NETWORK) {
                                { Icon(Icons.Default.Wifi, contentDescription = null, modifier = Modifier.size(16.dp)) }
                            } else null,
                            modifier = Modifier.weight(1f)
                        )
                    }

                    Spacer(Modifier.height(12.dp))

                    when (selectedTab) {
                        PrintDialogTab.NETWORK -> {
                            OutlinedTextField(
                                value = networkIp,
                                onValueChange = { networkIp = it },
                                modifier = Modifier.fillMaxWidth(),
                                label = { Text("IP Printer") },
                                placeholder = { Text("192.168.1.100") },
                                leadingIcon = { Icon(Icons.Default.Wifi, contentDescription = null) },
                                singleLine = true,
                                shape = MaterialTheme.shapes.medium,
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal)
                            )
                        }
                        PrintDialogTab.BLUETOOTH -> {
                            Text("Buka Pengaturan untuk menghubungkan printer Bluetooth.",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                textAlign = TextAlign.Center,
                                modifier = Modifier.fillMaxWidth())
                        }
                    }
                }

                // ── Print result feedback ─────────────────────────────────────
                printResult?.let { result ->
                    Spacer(Modifier.height(12.dp))
                    when (result) {
                        is PrintResult.Success -> Surface(
                            color = MaterialTheme.colorScheme.primaryContainer,
                            shape = MaterialTheme.shapes.small,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(modifier = Modifier.padding(10.dp), verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.CheckCircle, contentDescription = null,
                                    tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(18.dp))
                                Spacer(Modifier.width(8.dp))
                                Text("Struk berhasil dicetak!", style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onPrimaryContainer)
                            }
                        }
                        is PrintResult.Error -> Surface(
                            color = MaterialTheme.colorScheme.errorContainer,
                            shape = MaterialTheme.shapes.small,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(modifier = Modifier.padding(10.dp), verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.Warning, contentDescription = null,
                                    tint = MaterialTheme.colorScheme.error, modifier = Modifier.size(18.dp))
                                Spacer(Modifier.width(8.dp))
                                Text(result.message, style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onErrorContainer)
                            }
                        }
                    }
                }

                // ── Loading ───────────────────────────────────────────────────
                if (isPrinting) {
                    Spacer(Modifier.height(12.dp))
                    Row(Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.Center,
                        verticalAlignment = Alignment.CenterVertically) {
                        CircularProgressIndicator(modifier = Modifier.size(20.dp), strokeWidth = 2.dp)
                        Spacer(Modifier.width(8.dp))
                        Text("Mencetak...", style = MaterialTheme.typography.bodySmall)
                    }
                }
            }
        },
        confirmButton = {
            val canPrint = !isPrinting && when {
                hasSavedPrinter && printResult !is PrintResult.Error -> true
                selectedTab == PrintDialogTab.NETWORK -> networkIp.isNotBlank()
                else -> false
            }
            val buttonText = when {
                printResult is PrintResult.Success -> "Cetak Ulang"
                printResult is PrintResult.Error   -> "Coba Lagi"
                else                               -> "Cetak"
            }
            Button(
                onClick = {
                    scope.launch {
                        isPrinting  = true
                        printResult = null
                        printResult = when {
                            hasSavedPrinter ->
                                executePrint(savedType, savedAddress, savedNetworkIp, savedNetworkPort)
                            selectedTab == PrintDialogTab.NETWORK ->
                                executePrint(SettingsStore.TYPE_NETWORK, "", networkIp.trim(), 9100)
                            else ->
                                PrintResult.Error("Pilih printer terlebih dahulu di Pengaturan")
                        }
                        isPrinting = false
                    }
                },
                enabled = canPrint
            ) { Text(buttonText) }
        },
        dismissButton = {
            TextButton(onClick = onDismiss, enabled = !isPrinting) { Text("Tutup") }
        }
    )
}

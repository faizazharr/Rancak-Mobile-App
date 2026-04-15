package id.rancak.app.presentation.ui.payment

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
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
import id.rancak.app.data.printing.toReceiptData
import id.rancak.app.data.printing.toKitchenTicketData
import id.rancak.app.domain.model.PaymentMethod
import id.rancak.app.domain.model.Sale
import id.rancak.app.presentation.components.*
import androidx.compose.ui.zIndex
import id.rancak.app.presentation.designsystem.RancakTheme
import id.rancak.app.presentation.util.formatRupiah
import id.rancak.app.presentation.viewmodel.CartViewModel
import id.rancak.app.presentation.viewmodel.PaymentViewModel
import androidx.compose.ui.tooling.preview.Preview
import kotlinx.coroutines.async
import kotlinx.coroutines.launch
import org.koin.compose.koinInject
import org.koin.compose.viewmodel.koinViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PaymentScreen(
    onBack: () -> Unit,
    onPaymentComplete: () -> Unit,
    cartViewModel: CartViewModel,
    paymentViewModel: PaymentViewModel = koinViewModel()
) {
    val cartState by cartViewModel.uiState.collectAsState()
    val paymentState by paymentViewModel.uiState.collectAsState()
    val printerManager: PrinterManager = koinInject()
    val settingsStore: SettingsStore = koinInject()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Pembayaran") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Kembali")
                    }
                }
            )
        }
    ) { padding ->
        Box(modifier = Modifier.fillMaxSize()) {
        // ── Error banner ───────────────────────────────────────────────────
        ErrorBanner(
            error = paymentState.error,
            onDismiss = paymentViewModel::clearError,
            modifier = Modifier
                .fillMaxWidth()
                .align(Alignment.TopCenter)
                .zIndex(10f)
        )

        if (paymentState.completedSale != null) {
            // ── Success State ──────────────────────────────────────────────────
            PaymentSuccessContent(
                sale = paymentState.completedSale!!,
                printerManager = printerManager,
                settingsStore = settingsStore,
                onNewTransaction = {
                    paymentViewModel.reset()
                    cartViewModel.clearCart()
                    onPaymentComplete()
                },
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
            )
        } else {
            // ── Payment Form ───────────────────────────────────────────────────
            PaymentFormContent(
                itemCount = cartState.itemCount,
                subtotal = cartState.subtotal,
                selectedMethod = paymentState.selectedMethod,
                onSelectMethod = paymentViewModel::selectMethod,
                paidAmount = paymentState.paidAmount,
                onPaidAmountChange = paymentViewModel::setPaidAmount,
                isCashSelected = paymentState.selectedMethod == PaymentMethod.CASH,
                isProcessing = paymentState.isProcessing,
                onProcessPayment = {
                    paymentViewModel.processPayment(
                        items        = cartState.items,
                        orderType    = cartState.orderType,
                        tableUuid    = cartState.tableUuid,
                        customerName = cartState.customerName,
                        note         = cartState.note,
                        pax          = cartState.pax,
                        discount     = cartState.discount,
                        tax          = cartState.tax,
                        adminFee     = cartState.adminFee,
                        deliveryFee  = cartState.deliveryFee,
                        tip          = cartState.tip,
                        voucherCode  = cartState.voucherCode.takeIf { it.isNotBlank() }
                    )
                },
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
            )
        } // end else
        } // end Box
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// Payment Form Content
// ─────────────────────────────────────────────────────────────────────────────

@Composable
internal fun PaymentFormContent(
    itemCount: Int,
    subtotal: Long,
    selectedMethod: PaymentMethod,
    onSelectMethod: (PaymentMethod) -> Unit,
    paidAmount: String,
    onPaidAmountChange: (String) -> Unit,
    isCashSelected: Boolean,
    isProcessing: Boolean,
    onProcessPayment: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .verticalScroll(rememberScrollState())
            .padding(12.dp)
    ) {
        // Order Summary Card
        Card(
            shape = MaterialTheme.shapes.medium,
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
            )
        ) {
            Column(modifier = Modifier.padding(12.dp)) {
                Text(
                    "Ringkasan Pesanan",
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(Modifier.height(4.dp))
                SummaryRow(
                    label = "$itemCount item",
                    value = formatRupiah(subtotal)
                )
                HorizontalDivider(modifier = Modifier.padding(vertical = 4.dp))
                SummaryRow(
                    label = "Total",
                    value = formatRupiah(subtotal),
                    isBold = true,
                    valueColor = MaterialTheme.colorScheme.primary
                )
            }
        }

        Spacer(Modifier.height(16.dp))

        // Payment Method
        Text(
            "Metode Pembayaran",
            style = MaterialTheme.typography.labelLarge,
            fontWeight = FontWeight.SemiBold,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Spacer(Modifier.height(6.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            PaymentMethod.entries.forEach { method ->
                PaymentMethodChip(
                    method = method.value,
                    isSelected = selectedMethod == method,
                    onClick = { onSelectMethod(method) },
                    modifier = Modifier.weight(1f)
                )
            }
        }

        Spacer(Modifier.height(16.dp))

        // Paid Amount (cash only)
        if (isCashSelected) {
            Text(
                "Jumlah Bayar",
                style = MaterialTheme.typography.labelLarge,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(Modifier.height(6.dp))

            OutlinedTextField(
                value = paidAmount,
                onValueChange = onPaidAmountChange,
                modifier = Modifier.fillMaxWidth(),
                prefix = { Text("Rp ") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                singleLine = true,
                shape = MaterialTheme.shapes.medium
            )

            // Quick amount buttons
            Spacer(Modifier.height(6.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                val quickAmounts = listOf(
                    subtotal,
                    ((subtotal / 10000) + 1) * 10000,
                    ((subtotal / 50000) + 1) * 50000,
                    ((subtotal / 100000) + 1) * 100000
                ).distinct().sorted()

                quickAmounts.take(4).forEach { amount ->
                    OutlinedButton(
                        onClick = { onPaidAmountChange(amount.toString()) },
                        modifier = Modifier.weight(1f),
                        shape = MaterialTheme.shapes.small,
                        contentPadding = PaddingValues(4.dp)
                    ) {
                        Text(
                            formatRupiah(amount),
                            style = MaterialTheme.typography.labelSmall,
                            textAlign = TextAlign.Center
                        )
                    }
                }
            }
        }

        // Error
        Spacer(Modifier.height(20.dp))

        RancakButton(
            text = "Proses Pembayaran",
            onClick = onProcessPayment,
            isLoading = isProcessing,
            modifier = Modifier.fillMaxWidth()
        )
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// Success content with print option
// ─────────────────────────────────────────────────────────────────────────────

@Composable
private fun PaymentSuccessContent(
    sale: Sale,
    printerManager: PrinterManager,
    settingsStore: SettingsStore,
    onNewTransaction: () -> Unit,
    modifier: Modifier = Modifier
) {
    var showPrintDialog by remember { mutableStateOf(false) }

    Column(
        modifier = modifier.padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            Icons.Default.CheckCircle,
            contentDescription = null,
            modifier = Modifier.size(56.dp),
            tint = MaterialTheme.colorScheme.primary
        )
        Spacer(Modifier.height(16.dp))
        Text(
            "Transaksi Berhasil!",
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold
        )
        Spacer(Modifier.height(4.dp))
        Text(
            sale.invoiceNo ?: "",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Spacer(Modifier.height(4.dp))
        Text(
            formatRupiah(sale.total),
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary
        )
        if (sale.changeAmount > 0) {
            Text(
                "Kembalian: ${formatRupiah(sale.changeAmount)}",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.secondary
            )
        }
        Spacer(Modifier.height(24.dp))

        // Print receipt button
        OutlinedButton(
            onClick = { showPrintDialog = true },
            modifier = Modifier.fillMaxWidth(),
            shape = MaterialTheme.shapes.medium
        ) {
            Icon(Icons.Default.Print, contentDescription = null, modifier = Modifier.size(16.dp))
            Spacer(Modifier.width(6.dp))
            Text("Cetak Struk")
        }

        Spacer(Modifier.height(8.dp))

        // New transaction button
        RancakButton(
            text = "Transaksi Baru",
            onClick = onNewTransaction,
            modifier = Modifier.fillMaxWidth()
        )
    }

    if (showPrintDialog) {
        PrintDialog(
            sale = sale,
            printerManager = printerManager,
            settingsStore = settingsStore,
            onDismiss = { showPrintDialog = false }
        )
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// Print Dialog
// ─────────────────────────────────────────────────────────────────────────────

private enum class PrintDialogTab { BLUETOOTH, NETWORK }

/**
 * Sends [data] to a printer based on saved settings.
 * Returns [PrintResult].
 */
private suspend fun sendToPrinter(
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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun PrintDialog(
    sale: Sale,
    printerManager: PrinterManager,
    settingsStore: SettingsStore,
    onDismiss: () -> Unit
) {
    val scope = rememberCoroutineScope()

    // Read print mode
    val printMode = PrintMode.from(settingsStore.printMode)

    // Pre-load saved cashier printer settings
    val hasSavedPrinter = settingsStore.printerAddress.isNotBlank()
    val savedType = settingsStore.printerType
    val savedName = settingsStore.printerName
    val savedAddress = settingsStore.printerAddress
    val savedNetworkIp = settingsStore.networkPrinterIp
    val savedNetworkPort = settingsStore.networkPrinterPort

    // Kitchen printer settings (for dual printer mode)
    val hasKitchenPrinter = settingsStore.hasKitchenPrinter
    val kitchenType = settingsStore.kitchenPrinterType
    val kitchenAddress = settingsStore.kitchenPrinterAddress
    val kitchenNetworkIp = settingsStore.kitchenNetworkPrinterIp
    val kitchenNetworkPort = settingsStore.kitchenNetworkPrinterPort

    // Receipt settings
    val storeName = settingsStore.receiptStoreName.ifBlank { "Rancak" }
    val storeAddress = settingsStore.receiptStoreAddress.ifBlank { null }
    val storePhone = settingsStore.receiptStorePhone.ifBlank { null }
    val footerText = settingsStore.receiptFooter.ifBlank { null }

    var selectedTab by remember {
        mutableStateOf(
            if (savedType == SettingsStore.TYPE_NETWORK) PrintDialogTab.NETWORK
            else PrintDialogTab.BLUETOOTH
        )
    }
    var networkIp by remember { mutableStateOf(savedNetworkIp) }
    var isPrinting by remember { mutableStateOf(false) }
    var printResult by remember { mutableStateOf<PrintResult?>(null) }

    /**
     * Executes print based on the current [PrintMode].
     * - RECEIPT_ONLY: cashier receipt only
     * - SINGLE_KOT_FIRST / SINGLE_RECEIPT_FIRST: combined bytes to one printer
     * - DUAL_PRINTER: cashier receipt to cashier printer + KOT to kitchen printer in parallel
     */
    suspend fun executePrint(
        cashierType: String,
        cashierBtAddr: String,
        cashierNetIp: String,
        cashierNetPort: Int
    ): PrintResult {
        val receiptData = sale.toReceiptData(
            storeName = storeName,
            storeAddress = storeAddress,
            storePhone = storePhone,
            footerText = footerText
        )

        return when (printMode) {
            PrintMode.RECEIPT_ONLY -> {
                val bytes = EscPosBuilder.buildReceipt(receiptData)
                sendToPrinter(printerManager, cashierType, cashierBtAddr, cashierNetIp, cashierNetPort, bytes)
            }

            PrintMode.SINGLE_KOT_FIRST, PrintMode.SINGLE_RECEIPT_FIRST -> {
                val kitchenData = sale.toKitchenTicketData(storeName = storeName)
                val kotFirst = printMode == PrintMode.SINGLE_KOT_FIRST
                val bytes = EscPosBuilder.buildCombinedReceipt(receiptData, kitchenData, kotFirst)
                sendToPrinter(printerManager, cashierType, cashierBtAddr, cashierNetIp, cashierNetPort, bytes)
            }

            PrintMode.DUAL_PRINTER -> {
                val receiptBytes = EscPosBuilder.buildReceipt(receiptData)
                val kitchenData = sale.toKitchenTicketData(storeName = storeName)
                val kotBytes = EscPosBuilder.buildKitchenTicket(kitchenData)

                // Print cashier receipt to cashier printer
                val cashierDeferred = scope.async {
                    sendToPrinter(printerManager, cashierType, cashierBtAddr, cashierNetIp, cashierNetPort, receiptBytes)
                }

                // Print KOT to kitchen printer (if configured)
                val kitchenDeferred = if (hasKitchenPrinter) {
                    scope.async {
                        sendToPrinter(
                            printerManager, kitchenType, kitchenAddress,
                            kitchenNetworkIp, kitchenNetworkPort, kotBytes
                        )
                    }
                } else null

                val cashierResult = cashierDeferred.await()
                val kitchenResult = kitchenDeferred?.await()

                // Report result — prioritize errors
                when {
                    cashierResult is PrintResult.Error -> cashierResult
                    kitchenResult is PrintResult.Error ->
                        PrintResult.Error("Struk kasir berhasil, tapi gagal cetak KOT dapur: ${kitchenResult.message}")
                    else -> PrintResult.Success
                }
            }
        }
    }

    // If saved printer is set, auto-print immediately when dialog opens
    var autoPrintAttempted by remember { mutableStateOf(false) }
    LaunchedEffect(hasSavedPrinter) {
        if (hasSavedPrinter && !autoPrintAttempted) {
            autoPrintAttempted = true
            isPrinting = true
            printResult = null
            printResult = executePrint(savedType, savedAddress, savedNetworkIp, savedNetworkPort)
            isPrinting = false
        }
    }

    AlertDialog(
        onDismissRequest = { if (!isPrinting) onDismiss() },
        icon = { Icon(Icons.Default.Print, contentDescription = null) },
        title = { Text("Cetak Struk") },
        text = {
            Column {
                // Print mode indicator
                val modeLabel = when (printMode) {
                    PrintMode.RECEIPT_ONLY -> null
                    PrintMode.DUAL_PRINTER -> "Mode: Dua Printer (Kasir + Dapur)"
                    PrintMode.SINGLE_KOT_FIRST -> "Mode: Satu Printer (KOT dulu)"
                    PrintMode.SINGLE_RECEIPT_FIRST -> "Mode: Satu Printer (Struk dulu)"
                }
                if (modeLabel != null) {
                    Text(
                        modeLabel,
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.padding(bottom = 8.dp)
                    )
                }

                // Show saved printer info
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
                                Text(
                                    savedName.ifBlank { savedAddress },
                                    style = MaterialTheme.typography.bodyMedium,
                                    fontWeight = FontWeight.SemiBold
                                )
                                Text(
                                    if (printMode == PrintMode.DUAL_PRINTER) "Printer kasir"
                                    else "Printer tersimpan dari Pengaturan",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }
                    Spacer(Modifier.height(4.dp))
                }

                // Show kitchen printer info (dual mode)
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
                                Text(
                                    settingsStore.kitchenPrinterName.ifBlank { kitchenAddress },
                                    style = MaterialTheme.typography.bodyMedium,
                                    fontWeight = FontWeight.SemiBold
                                )
                                Text(
                                    "Printer dapur (KOT)",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
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
                            Icon(
                                Icons.Default.Warning,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.error,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(Modifier.width(8.dp))
                            Text(
                                "Printer dapur belum diatur. KOT tidak akan dicetak.",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onErrorContainer
                            )
                        }
                    }
                    Spacer(Modifier.height(4.dp))
                }

                Spacer(Modifier.height(8.dp))

                // Manual fallback: tab selector (only when no saved printer OR user wants to retry)
                if (!hasSavedPrinter || (printResult is PrintResult.Error)) {
                    if (printResult is PrintResult.Error && hasSavedPrinter) {
                        Text(
                            "Gagal mencetak ke printer tersimpan. Pilih printer lain atau coba lagi:",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.error,
                            modifier = Modifier.padding(bottom = 8.dp)
                        )
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        FilterChip(
                            selected = selectedTab == PrintDialogTab.BLUETOOTH,
                            onClick = { selectedTab = PrintDialogTab.BLUETOOTH; printResult = null },
                            label = { Text("Bluetooth") },
                            leadingIcon = if (selectedTab == PrintDialogTab.BLUETOOTH) {
                                { Icon(Icons.Default.Bluetooth, contentDescription = null, modifier = Modifier.size(16.dp)) }
                            } else null,
                            modifier = Modifier.weight(1f)
                        )
                        FilterChip(
                            selected = selectedTab == PrintDialogTab.NETWORK,
                            onClick = { selectedTab = PrintDialogTab.NETWORK; printResult = null },
                            label = { Text("Wi-Fi / LAN") },
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
                            Text(
                                "Buka Pengaturan untuk menghubungkan printer Bluetooth.",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                textAlign = TextAlign.Center,
                                modifier = Modifier.fillMaxWidth()
                            )
                        }
                    }
                }

                // Print result feedback
                printResult?.let { result ->
                    Spacer(Modifier.height(12.dp))
                    when (result) {
                        is PrintResult.Success -> Surface(
                            color = MaterialTheme.colorScheme.primaryContainer,
                            shape = MaterialTheme.shapes.small,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                modifier = Modifier.padding(10.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    Icons.Default.CheckCircle,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.size(18.dp)
                                )
                                Spacer(Modifier.width(8.dp))
                                Text(
                                    "Struk berhasil dicetak!",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onPrimaryContainer
                                )
                            }
                        }
                        is PrintResult.Error -> Surface(
                            color = MaterialTheme.colorScheme.errorContainer,
                            shape = MaterialTheme.shapes.small,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                modifier = Modifier.padding(10.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    Icons.Default.Warning,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.error,
                                    modifier = Modifier.size(18.dp)
                                )
                                Spacer(Modifier.width(8.dp))
                                Text(
                                    result.message,
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onErrorContainer
                                )
                            }
                        }
                    }
                }

                // Loading indicator
                if (isPrinting) {
                    Spacer(Modifier.height(12.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.Center,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        CircularProgressIndicator(modifier = Modifier.size(20.dp), strokeWidth = 2.dp)
                        Spacer(Modifier.width(8.dp))
                        Text("Mencetak...", style = MaterialTheme.typography.bodySmall)
                    }
                }
            }
        },
        confirmButton = {
            // Retry / manual print button
            val canPrint = !isPrinting && when {
                hasSavedPrinter && printResult !is PrintResult.Error -> true
                selectedTab == PrintDialogTab.NETWORK -> networkIp.isNotBlank()
                else -> false
            }
            val buttonText = when {
                printResult is PrintResult.Success -> "Cetak Ulang"
                printResult is PrintResult.Error -> "Coba Lagi"
                else -> "Cetak"
            }
            Button(
                onClick = {
                    scope.launch {
                        isPrinting = true
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
            ) {
                Text(buttonText)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss, enabled = !isPrinting) {
                Text("Tutup")
            }
        }
    )
}

// ─────────────────────────────────────────────────────────────────────────────
// Preview Content Composables
// ─────────────────────────────────────────────────────────────────────────────

@Composable
internal fun PaymentSuccessPreviewContent(
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier.padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            Icons.Default.CheckCircle,
            contentDescription = null,
            modifier = Modifier.size(56.dp),
            tint = MaterialTheme.colorScheme.primary
        )
        Spacer(Modifier.height(16.dp))
        Text(
            "Transaksi Berhasil!",
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold
        )
        Spacer(Modifier.height(4.dp))
        Text(
            "INV-2024-0001",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Spacer(Modifier.height(4.dp))
        Text(
            formatRupiah(75000),
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary
        )
        Text(
            "Kembalian: ${formatRupiah(25000)}",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.secondary
        )
        Spacer(Modifier.height(24.dp))

        // Print receipt button
        OutlinedButton(
            onClick = { },
            modifier = Modifier.fillMaxWidth(),
            shape = MaterialTheme.shapes.medium
        ) {
            Icon(Icons.Default.Print, contentDescription = null, modifier = Modifier.size(16.dp))
            Spacer(Modifier.width(6.dp))
            Text("Cetak Struk")
        }

        Spacer(Modifier.height(8.dp))

        // New transaction button
        RancakButton(
            text = "Transaksi Baru",
            onClick = { },
            modifier = Modifier.fillMaxWidth()
        )
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// Previews
// ─────────────────────────────────────────────────────────────────────────────

@Preview
@Composable
private fun PaymentFormPreview() {
    RancakTheme {
        PaymentFormContent(
            itemCount = 3,
            subtotal = 75000,
            selectedMethod = PaymentMethod.CASH,
            onSelectMethod = { },
            paidAmount = "",
            onPaidAmountChange = { },
            isCashSelected = true,
            isProcessing = false,
            onProcessPayment = { },
            modifier = Modifier
                .fillMaxSize()
                .padding(12.dp)
        )
    }
}

@Preview
@Composable
private fun PaymentSuccessPreview() {
    RancakTheme {
        PaymentSuccessPreviewContent(
            modifier = Modifier.fillMaxSize()
        )
    }
}

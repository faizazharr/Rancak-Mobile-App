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
import id.rancak.app.data.printing.PrintResult
import id.rancak.app.data.printing.PrinterManager
import id.rancak.app.data.printing.toReceiptData
import id.rancak.app.domain.model.PaymentMethod
import id.rancak.app.domain.model.Sale
import id.rancak.app.presentation.components.*
import androidx.compose.ui.zIndex
import id.rancak.app.presentation.designsystem.RancakTheme
import id.rancak.app.presentation.util.formatRupiah
import id.rancak.app.presentation.viewmodel.CartViewModel
import id.rancak.app.presentation.viewmodel.PaymentViewModel
import androidx.compose.ui.tooling.preview.Preview
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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun PrintDialog(
    sale: Sale,
    printerManager: PrinterManager,
    settingsStore: SettingsStore,
    onDismiss: () -> Unit
) {
    val scope = rememberCoroutineScope()

    // Pre-load saved printer settings
    val hasSavedPrinter = settingsStore.printerAddress.isNotBlank()
    val savedType = settingsStore.printerType
    val savedName = settingsStore.printerName
    val savedAddress = settingsStore.printerAddress
    val savedNetworkIp = settingsStore.networkPrinterIp
    val savedNetworkPort = settingsStore.networkPrinterPort

    var selectedTab by remember {
        mutableStateOf(
            if (savedType == SettingsStore.TYPE_NETWORK) PrintDialogTab.NETWORK
            else PrintDialogTab.BLUETOOTH
        )
    }
    var networkIp by remember { mutableStateOf(savedNetworkIp) }
    var isPrinting by remember { mutableStateOf(false) }
    var printResult by remember { mutableStateOf<PrintResult?>(null) }

    // If saved printer is set, auto-print immediately when dialog opens
    var autoPrintAttempted by remember { mutableStateOf(false) }
    LaunchedEffect(hasSavedPrinter) {
        if (hasSavedPrinter && !autoPrintAttempted) {
            autoPrintAttempted = true
            isPrinting = true
            printResult = null
            val bytes = EscPosBuilder.buildReceipt(sale.toReceiptData())
            printResult = if (savedType == SettingsStore.TYPE_BLUETOOTH) {
                printerManager.printViaBluetooth(savedAddress, bytes)
            } else {
                printerManager.printViaNetwork(savedNetworkIp, savedNetworkPort, bytes)
            }
            isPrinting = false
        }
    }

    AlertDialog(
        onDismissRequest = { if (!isPrinting) onDismiss() },
        icon = { Icon(Icons.Default.Print, contentDescription = null) },
        title = { Text("Cetak Struk") },
        text = {
            Column {
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
                                    "Printer tersimpan dari Pengaturan",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }
                    Spacer(Modifier.height(12.dp))
                }

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
                hasSavedPrinter && printResult !is PrintResult.Error -> true // retry with saved
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
                        val bytes = EscPosBuilder.buildReceipt(sale.toReceiptData())
                        printResult = when {
                            hasSavedPrinter && savedType == SettingsStore.TYPE_BLUETOOTH ->
                                printerManager.printViaBluetooth(savedAddress, bytes)
                            hasSavedPrinter && savedType == SettingsStore.TYPE_NETWORK ->
                                printerManager.printViaNetwork(savedNetworkIp, savedNetworkPort, bytes)
                            selectedTab == PrintDialogTab.NETWORK ->
                                printerManager.printViaNetwork(networkIp.trim(), data = bytes)
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

package id.rancak.app.presentation.ui.payment

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
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
import id.rancak.app.data.printing.EscPosBuilder
import id.rancak.app.data.printing.PrintResult
import id.rancak.app.data.printing.PrinterConnectionType
import id.rancak.app.data.printing.PrinterDevice
import id.rancak.app.data.printing.PrinterManager
import id.rancak.app.data.printing.toReceiptData
import id.rancak.app.domain.model.PaymentMethod
import id.rancak.app.domain.model.Sale
import id.rancak.app.presentation.components.*
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
    cartViewModel: CartViewModel = koinViewModel(),
    paymentViewModel: PaymentViewModel = koinViewModel()
) {
    val cartState by cartViewModel.uiState.collectAsState()
    val paymentState by paymentViewModel.uiState.collectAsState()
    val printerManager: PrinterManager = koinInject()

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
        if (paymentState.completedSale != null) {
            // ── Success State ──────────────────────────────────────────────────
            PaymentSuccessContent(
                sale = paymentState.completedSale!!,
                printerManager = printerManager,
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
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
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
                            label = "${cartState.itemCount} item",
                            value = formatRupiah(cartState.subtotal)
                        )
                        HorizontalDivider(modifier = Modifier.padding(vertical = 4.dp))
                        SummaryRow(
                            label = "Total",
                            value = formatRupiah(cartState.subtotal),
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
                            isSelected = paymentState.selectedMethod == method,
                            onClick = { paymentViewModel.selectMethod(method) },
                            modifier = Modifier.weight(1f)
                        )
                    }
                }

                Spacer(Modifier.height(16.dp))

                // Paid Amount (cash only)
                if (paymentState.selectedMethod == PaymentMethod.CASH) {
                    Text(
                        "Jumlah Bayar",
                        style = MaterialTheme.typography.labelLarge,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(Modifier.height(6.dp))

                    OutlinedTextField(
                        value = paymentState.paidAmount,
                        onValueChange = paymentViewModel::setPaidAmount,
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
                            cartState.subtotal,
                            ((cartState.subtotal / 10000) + 1) * 10000,
                            ((cartState.subtotal / 50000) + 1) * 50000,
                            ((cartState.subtotal / 100000) + 1) * 100000
                        ).distinct().sorted()

                        quickAmounts.take(4).forEach { amount ->
                            OutlinedButton(
                                onClick = { paymentViewModel.setPaidAmount(amount.toString()) },
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
                if (paymentState.error != null) {
                    Spacer(Modifier.height(8.dp))
                    Surface(
                        color = MaterialTheme.colorScheme.errorContainer,
                        shape = MaterialTheme.shapes.small,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            text = paymentState.error!!,
                            modifier = Modifier.padding(8.dp),
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onErrorContainer
                        )
                    }
                }

                Spacer(Modifier.height(20.dp))

                RancakButton(
                    text = "Proses Pembayaran",
                    onClick = {
                        paymentViewModel.processPayment(
                            items = cartState.items,
                            orderType = cartState.orderType,
                            tableUuid = cartState.tableUuid,
                            customerName = cartState.customerName,
                            note = cartState.note
                        )
                    },
                    isLoading = paymentState.isProcessing,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// Success content with print option
// ─────────────────────────────────────────────────────────────────────────────

@Composable
private fun PaymentSuccessContent(
    sale: Sale,
    printerManager: PrinterManager,
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
    onDismiss: () -> Unit
) {
    val scope = rememberCoroutineScope()

    var selectedTab by remember { mutableStateOf(PrintDialogTab.BLUETOOTH) }
    var bluetoothPrinters by remember { mutableStateOf<List<PrinterDevice>>(emptyList()) }
    var isLoadingBt by remember { mutableStateOf(false) }
    var selectedDevice by remember { mutableStateOf<PrinterDevice?>(null) }
    var networkIp by remember { mutableStateOf("") }
    var isPrinting by remember { mutableStateOf(false) }
    var printResult by remember { mutableStateOf<PrintResult?>(null) }

    // Load Bluetooth printers when dialog opens or tab switches to BT
    LaunchedEffect(selectedTab) {
        if (selectedTab == PrintDialogTab.BLUETOOTH) {
            isLoadingBt = true
            bluetoothPrinters = printerManager.getBluetoothPrinters()
            isLoadingBt = false
        }
    }

    AlertDialog(
        onDismissRequest = { if (!isPrinting) onDismiss() },
        icon = { Icon(Icons.Default.Print, contentDescription = null) },
        title = { Text("Cetak Struk") },
        text = {
            Column {
                // Tab selector
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

                Spacer(Modifier.height(16.dp))

                when (selectedTab) {
                    PrintDialogTab.BLUETOOTH -> {
                        if (isLoadingBt) {
                            Box(Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
                                CircularProgressIndicator(modifier = Modifier.size(24.dp))
                            }
                        } else if (bluetoothPrinters.isEmpty()) {
                            Text(
                                "Tidak ada printer Bluetooth ditemukan.\nPastikan printer sudah dipasangkan.",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                textAlign = TextAlign.Center,
                                modifier = Modifier.fillMaxWidth()
                            )
                        } else {
                            LazyColumn(
                                modifier = Modifier.heightIn(max = 200.dp),
                                verticalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                items(bluetoothPrinters) { device ->
                                    val isSelected = selectedDevice == device
                                    Surface(
                                        onClick = { selectedDevice = if (isSelected) null else device },
                                        shape = MaterialTheme.shapes.small,
                                        color = if (isSelected)
                                            MaterialTheme.colorScheme.primaryContainer
                                        else
                                            MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                                        modifier = Modifier.fillMaxWidth()
                                    ) {
                                        Row(
                                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 10.dp),
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Icon(
                                                Icons.Default.Print,
                                                contentDescription = null,
                                                modifier = Modifier.size(20.dp),
                                                tint = if (isSelected)
                                                    MaterialTheme.colorScheme.primary
                                                else
                                                    MaterialTheme.colorScheme.onSurfaceVariant
                                            )
                                            Spacer(Modifier.width(8.dp))
                                            Column(Modifier.weight(1f)) {
                                                Text(
                                                    device.name,
                                                    style = MaterialTheme.typography.bodyMedium,
                                                    fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Normal
                                                )
                                                Text(
                                                    device.address,
                                                    style = MaterialTheme.typography.labelSmall,
                                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                                )
                                            }
                                            if (isSelected) {
                                                Icon(
                                                    Icons.Default.CheckCircle,
                                                    contentDescription = null,
                                                    tint = MaterialTheme.colorScheme.primary,
                                                    modifier = Modifier.size(20.dp)
                                                )
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }

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
            }
        },
        confirmButton = {
            val canPrint = !isPrinting && when (selectedTab) {
                PrintDialogTab.BLUETOOTH -> selectedDevice != null
                PrintDialogTab.NETWORK   -> networkIp.isNotBlank()
            }
            Button(
                onClick = {
                    scope.launch {
                        isPrinting = true
                        printResult = null
                        val bytes = EscPosBuilder.buildReceipt(sale.toReceiptData())
                        printResult = when (selectedTab) {
                            PrintDialogTab.BLUETOOTH ->
                                printerManager.printViaBluetooth(selectedDevice!!.address, bytes)
                            PrintDialogTab.NETWORK   ->
                                printerManager.printViaNetwork(networkIp.trim(), data = bytes)
                        }
                        isPrinting = false
                    }
                },
                enabled = canPrint
            ) {
                if (isPrinting) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(18.dp),
                        strokeWidth = 2.dp,
                        color = MaterialTheme.colorScheme.onPrimary
                    )
                    Spacer(Modifier.width(8.dp))
                }
                Text("Cetak")
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
// Previews
// ─────────────────────────────────────────────────────────────────────────────

@Preview
@Composable
private fun PaymentFormPreview() {
    RancakTheme {
        Column(Modifier.padding(16.dp)) {
            Card(
                shape = MaterialTheme.shapes.medium,
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
            ) {
                Column(Modifier.padding(16.dp)) {
                    Text("Ringkasan Pesanan", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.SemiBold)
                    Spacer(Modifier.height(8.dp))
                    SummaryRow(label = "3 item", value = "Rp 75.000")
                    HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))
                    SummaryRow(label = "Total", value = "Rp 75.000", isBold = true, valueColor = MaterialTheme.colorScheme.primary)
                }
            }
            Spacer(Modifier.height(24.dp))
            Text("Metode Pembayaran", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.SemiBold)
            Spacer(Modifier.height(8.dp))
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                PaymentMethodChip(method = "Cash", isSelected = true, onClick = {}, modifier = Modifier.weight(1f))
                PaymentMethodChip(method = "QRIS", isSelected = false, onClick = {}, modifier = Modifier.weight(1f))
                PaymentMethodChip(method = "Card", isSelected = false, onClick = {}, modifier = Modifier.weight(1f))
            }
            Spacer(Modifier.height(24.dp))
            RancakButton(text = "Proses Pembayaran", onClick = {}, modifier = Modifier.fillMaxWidth())
        }
    }
}

@Preview
@Composable
private fun PaymentSuccessPreview() {
    RancakTheme {
        Column(
            modifier = Modifier.fillMaxSize().padding(32.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Icon(Icons.Default.CheckCircle, null, Modifier.size(80.dp), MaterialTheme.colorScheme.primary)
            Spacer(Modifier.height(24.dp))
            Text("Transaksi Berhasil!", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
            Spacer(Modifier.height(8.dp))
            Text("INV-2024-0001", style = MaterialTheme.typography.bodyLarge, color = MaterialTheme.colorScheme.onSurfaceVariant)
            Spacer(Modifier.height(8.dp))
            Text("Rp 75.000", style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
            Text("Kembalian: Rp 25.000", style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.secondary)
            Spacer(Modifier.height(32.dp))
            OutlinedButton(onClick = {}, modifier = Modifier.fillMaxWidth(), shape = MaterialTheme.shapes.medium) {
                Icon(Icons.Default.Print, contentDescription = null)
                Spacer(Modifier.width(8.dp))
                Text("Cetak Struk")
            }
            Spacer(Modifier.height(12.dp))
            RancakButton(text = "Transaksi Baru", onClick = {}, modifier = Modifier.fillMaxWidth())
        }
    }
}

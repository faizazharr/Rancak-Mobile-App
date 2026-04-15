package id.rancak.app.presentation.ui.payment

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Backspace
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
import id.rancak.app.presentation.components.RancakTopBar
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
            RancakTopBar(
                title = "Pembayaran",
                icon = Icons.Default.PointOfSale,
                subtitle = "Pilih metode & masukkan jumlah bayar",
                onBack = onBack
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

        when {
            // ── Success State ──────────────────────────────────────────────────
            paymentState.completedSale != null -> {
                PaymentSuccessContent(
                    sale = paymentState.completedSale!!,
                    printerManager = printerManager,
                    settingsStore = settingsStore,
                    onNewTransaction = {
                        onPaymentComplete()
                        cartViewModel.clearCart()
                    },
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(padding)
                )
            }

            // ── QRIS Waiting State ────────────────────────────────────────────
            paymentState.isQrisWaiting -> {
                QrisWaitingContent(
                    qrString     = paymentState.qrisQrString!!,
                    amount       = paymentState.qrisAmount,
                    isPolling    = paymentState.isQrisPolling,
                    onCancel     = paymentViewModel::cancelQrisPayment,
                    modifier     = Modifier
                        .fillMaxSize()
                        .padding(padding)
                )
            }

            // ── Payment Form ──────────────────────────────────────────────────
            else -> {
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
            } // end else (PaymentFormContent)
        } // end when
        } // end Box
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// Payment Form Content
// ─────────────────────────────────────────────────────────────────────────────

// Metode yang ditampilkan — hanya Cash dan QRIS
private val visiblePaymentMethods = listOf(PaymentMethod.CASH, PaymentMethod.QRIS)

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
    val changeAmount = remember(paidAmount, subtotal) {
        val paid = paidAmount.toLongOrNull() ?: 0L
        if (paid > subtotal) paid - subtotal else 0L
    }

    val quickAmounts = remember(subtotal) {
        listOf(
            subtotal,
            ((subtotal / 10000) + 1) * 10000,
            ((subtotal / 50000) + 1) * 50000,
            ((subtotal / 100000) + 1) * 100000
        ).distinct().sorted()
    }

    // ── 2-Kolom: Kiri = ringkasan, Kanan = input ─────────────────────────────
    Row(
        modifier = modifier.padding(16.dp),
        horizontalArrangement = Arrangement.spacedBy(16.dp)
    ) {

        // ════════════════════════════════════════════════════════════════════
        // KIRI — Ringkasan Pesanan & Total
        // ════════════════════════════════════════════════════════════════════
        Column(
            modifier = Modifier
                .weight(0.42f)
                .fillMaxHeight(),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {

            // Hero total
            Card(
                shape = MaterialTheme.shapes.large,
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.primary
                ),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp, vertical = 24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(5.dp)
                    ) {
                        Icon(
                            Icons.Default.ShoppingCart,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.65f),
                            modifier = Modifier.size(13.dp)
                        )
                        Text(
                            "$itemCount item",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.65f)
                        )
                    }
                    Text(
                        "Total Pembayaran",
                        style = MaterialTheme.typography.labelLarge,
                        color = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.85f)
                    )
                    Text(
                        formatRupiah(subtotal),
                        style = MaterialTheme.typography.headlineMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onPrimary
                    )
                }
            }

            // Detail ringkasan
            Card(
                shape = MaterialTheme.shapes.medium,
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.45f)
                ),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        "Ringkasan",
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(Modifier.height(10.dp))
                    SummaryRow(
                        label = "$itemCount item",
                        value = formatRupiah(subtotal)
                    )
                    HorizontalDivider(
                        modifier = Modifier.padding(vertical = 8.dp),
                        color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)
                    )
                    SummaryRow(
                        label = "Total",
                        value = formatRupiah(subtotal),
                        isBold = true,
                        valueColor = MaterialTheme.colorScheme.primary
                    )
                }
            }

            // Kembalian (muncul di kiri saat cash & ada kembalian)
            if (isCashSelected && changeAmount > 0) {
                Card(
                    shape = MaterialTheme.shapes.medium,
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f)
                    ),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Icon(
                                Icons.Default.SwapHoriz,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(16.dp)
                            )
                            Text(
                                "Kembalian",
                                style = MaterialTheme.typography.labelMedium,
                                color = MaterialTheme.colorScheme.onPrimaryContainer
                            )
                        }
                        Text(
                            formatRupiah(changeAmount),
                            style = MaterialTheme.typography.headlineSmall,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                }
            }
        }

        // ════════════════════════════════════════════════════════════════════
        // KANAN — Metode, Input, Numpad, Tombol
        // ════════════════════════════════════════════════════════════════════
        Column(
            modifier = Modifier
                .weight(0.58f)
                .fillMaxHeight()
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {

            // Metode pembayaran
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(
                    "Metode Pembayaran",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.SemiBold
                )
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    visiblePaymentMethods.forEach { method ->
                        PaymentMethodChip(
                            method = method.value,
                            isSelected = selectedMethod == method,
                            onClick = { onSelectMethod(method) },
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
            }

            // Input cash
            if (isCashSelected) {

                // Display jumlah bayar
                Surface(
                    shape = MaterialTheme.shapes.medium,
                    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 14.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column {
                            Text(
                                "Jumlah Bayar",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Spacer(Modifier.height(2.dp))
                            Text(
                                text = if (paidAmount.isEmpty()) "Rp 0"
                                       else formatRupiah(paidAmount.toLongOrNull() ?: 0L),
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.Bold,
                                color = if (paidAmount.isEmpty())
                                    MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.4f)
                                else
                                    MaterialTheme.colorScheme.onSurface
                            )
                        }
                        if (paidAmount.isNotEmpty()) {
                            IconButton(
                                onClick = { onPaidAmountChange("") },
                                modifier = Modifier.size(36.dp)
                            ) {
                                Icon(
                                    Icons.Default.Clear,
                                    contentDescription = "Hapus semua",
                                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                        }
                    }
                }

                // Quick amount buttons
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    quickAmounts.take(4).forEach { amount ->
                        val isActive = paidAmount == amount.toString()
                        FilledTonalButton(
                            onClick = { onPaidAmountChange(amount.toString()) },
                            modifier = Modifier.weight(1f),
                            shape = MaterialTheme.shapes.small,
                            contentPadding = PaddingValues(horizontal = 4.dp, vertical = 10.dp),
                            colors = ButtonDefaults.filledTonalButtonColors(
                                containerColor = if (isActive)
                                    MaterialTheme.colorScheme.primaryContainer
                                else
                                    MaterialTheme.colorScheme.surfaceVariant
                            )
                        ) {
                            Text(
                                formatRupiah(amount),
                                style = MaterialTheme.typography.labelSmall,
                                textAlign = TextAlign.Center,
                                maxLines = 1,
                                color = if (isActive)
                                    MaterialTheme.colorScheme.onPrimaryContainer
                                else
                                    MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }

                // Numpad
                PaymentNumpad(
                    onKey = { key ->
                        val current = paidAmount
                        val next = when (key) {
                            "⌫"   -> current.dropLast(1)
                            "000" -> if (current.isEmpty()) current
                                     else (current + "000").take(10)
                            else  -> if (current.isEmpty() && key == "0") current
                                     else (current + key).take(10)
                        }
                        onPaidAmountChange(next)
                    }
                )
            }

            // Proses
            Spacer(Modifier.height(4.dp))
            RancakButton(
                text = "Proses Pembayaran",
                onClick = onProcessPayment,
                isLoading = isProcessing,
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// Custom Numpad
// ─────────────────────────────────────────────────────────────────────────────

@Composable
private fun PaymentNumpad(
    onKey: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val keys = listOf(
        listOf("1", "2", "3"),
        listOf("4", "5", "6"),
        listOf("7", "8", "9"),
        listOf("000", "0", "⌫")
    )

    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        keys.forEach { row ->
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                row.forEach { key ->
                    val isBackspace = key == "⌫"
                    ElevatedButton(
                        onClick = { onKey(key) },
                        modifier = Modifier
                            .weight(1f)
                            .height(56.dp),
                        shape = MaterialTheme.shapes.medium,
                        colors = ButtonDefaults.elevatedButtonColors(
                            containerColor = if (isBackspace)
                                MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.5f)
                            else
                                MaterialTheme.colorScheme.surface,
                            contentColor = if (isBackspace)
                                MaterialTheme.colorScheme.error
                            else
                                MaterialTheme.colorScheme.onSurface
                        ),
                        elevation = ButtonDefaults.elevatedButtonElevation(
                            defaultElevation = 1.dp,
                            pressedElevation = 0.dp
                        ),
                        contentPadding = PaddingValues(0.dp)
                    ) {
                        if (isBackspace) {
                            Icon(
                                Icons.AutoMirrored.Filled.Backspace,
                                contentDescription = "Hapus",
                                modifier = Modifier.size(20.dp)
                            )
                        } else {
                            Text(
                                text = key,
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Medium
                            )
                        }
                    }
                }
            }
        }
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// QRIS Waiting Content — tampilkan QR dan tunggu konfirmasi
// ─────────────────────────────────────────────────────────────────────────────

@Composable
private fun QrisWaitingContent(
    qrString: String,
    amount: Long,
    isPolling: Boolean,
    onCancel: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier.background(MaterialTheme.colorScheme.surface),
        contentAlignment = Alignment.Center
    ) {
        Card(
            modifier = Modifier
                .widthIn(max = 440.dp)
                .fillMaxWidth(0.9f),
            shape = MaterialTheme.shapes.extraLarge,
            elevation = CardDefaults.cardElevation(defaultElevation = 6.dp)
        ) {
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // ── Header hijau QRIS ────────────────────────────────────────
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(MaterialTheme.colorScheme.primary)
                        .padding(vertical = 20.dp, horizontal = 24.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(
                            imageVector = Icons.Default.QrCode2,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onPrimary,
                            modifier = Modifier.size(32.dp)
                        )
                        Spacer(Modifier.height(8.dp))
                        Text(
                            text = "Scan QRIS untuk Bayar",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onPrimary
                        )
                        Spacer(Modifier.height(4.dp))
                        Text(
                            text = formatRupiah(amount),
                            style = MaterialTheme.typography.headlineSmall,
                            fontWeight = FontWeight.ExtraBold,
                            color = MaterialTheme.colorScheme.onPrimary
                        )
                    }
                }

                // ── QR Code ──────────────────────────────────────────────────
                Column(
                    modifier = Modifier.padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    QrisQrCode(
                        qrString = qrString,
                        size     = 240.dp,
                        label    = "Scan dengan aplikasi bank atau e-wallet"
                    )

                    // ── Status polling ───────────────────────────────────────
                    Surface(
                        shape = MaterialTheme.shapes.medium,
                        color = MaterialTheme.colorScheme.secondaryContainer
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 16.dp, vertical = 10.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            if (isPolling) {
                                CircularProgressIndicator(
                                    modifier = Modifier.size(16.dp),
                                    strokeWidth = 2.dp,
                                    color = MaterialTheme.colorScheme.onSecondaryContainer
                                )
                            } else {
                                Icon(
                                    Icons.Default.HourglassTop,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.onSecondaryContainer,
                                    modifier = Modifier.size(16.dp)
                                )
                            }
                            Text(
                                text = if (isPolling) "Menunggu konfirmasi pembayaran..."
                                       else "Memuat QR...",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSecondaryContainer
                            )
                        }
                    }

                    HorizontalDivider()

                    // ── Batalkan ─────────────────────────────────────────────
                    OutlinedButton(
                        onClick = onCancel,
                        modifier = Modifier.fillMaxWidth(),
                        colors = ButtonDefaults.outlinedButtonColors(
                            contentColor = MaterialTheme.colorScheme.error
                        )
                    ) {
                        Icon(Icons.Default.Close, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(Modifier.width(6.dp))
                        Text("Batalkan Pembayaran")
                    }
                }
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
    settingsStore: SettingsStore,
    onNewTransaction: () -> Unit,
    modifier: Modifier = Modifier
) {
    var showPrintDialog by remember { mutableStateOf(false) }

    // Centered card — rapi di tengah layar, responsif untuk phone & tablet
    Box(
        modifier = modifier,
        contentAlignment = Alignment.Center
    ) {
        Card(
            modifier = Modifier
                .widthIn(min = 300.dp, max = 520.dp)
                .fillMaxWidth(0.88f),          // 88% lebar layar, capped 520dp
            shape = MaterialTheme.shapes.extraLarge,
            elevation = CardDefaults.cardElevation(defaultElevation = 3.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surface
            )
        ) {
            Column(
                modifier = Modifier.verticalScroll(rememberScrollState())  // scroll jika layar kecil
            ) {

                // ── Header hijau ─────────────────────────────────────────────
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(MaterialTheme.colorScheme.primary)
                        .padding(vertical = 28.dp, horizontal = 24.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        // Lingkaran putih dengan centang hijau
                        Surface(
                            shape = androidx.compose.foundation.shape.CircleShape,
                            color = MaterialTheme.colorScheme.onPrimary,
                            modifier = Modifier.size(64.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(
                                    Icons.Default.Check,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.size(34.dp)
                                )
                            }
                        }
                        Text(
                            "Transaksi Berhasil!",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onPrimary
                        )
                        if (sale.invoiceNo != null) {
                            Text(
                                sale.invoiceNo,
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.7f)
                            )
                        }
                    }
                }

                // ── Detail transaksi ─────────────────────────────────────────
                Column(
                    modifier = Modifier.padding(24.dp),
                    verticalArrangement = Arrangement.spacedBy(0.dp)
                ) {

                    // Total
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 10.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            "Total Pembayaran",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            formatRupiah(sale.total),
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }

                    // Kembalian (jika ada)
                    if (sale.changeAmount > 0) {
                        HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 10.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(5.dp)
                            ) {
                                Icon(
                                    Icons.Default.SwapHoriz,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.secondary,
                                    modifier = Modifier.size(15.dp)
                                )
                                Text(
                                    "Kembalian",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                            Text(
                                formatRupiah(sale.changeAmount),
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.SemiBold,
                                color = MaterialTheme.colorScheme.secondary
                            )
                        }
                    }

                    HorizontalDivider(
                        modifier = Modifier.padding(bottom = 20.dp),
                        color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)
                    )

                    // ── Dua tombol sejajar ───────────────────────────────────
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        OutlinedButton(
                            onClick = { showPrintDialog = true },
                            modifier = Modifier.weight(1f),
                            shape = MaterialTheme.shapes.medium
                        ) {
                            Icon(
                                Icons.Default.Print,
                                contentDescription = null,
                                modifier = Modifier.size(15.dp)
                            )
                            Spacer(Modifier.width(6.dp))
                            Text("Cetak Struk")
                        }

                        RancakButton(
                            text = "Transaksi Baru",
                            onClick = onNewTransaction,
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
            }
        }
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
// Print Dialog — delegated to shared PrintComponents.kt
// ─────────────────────────────────────────────────────────────────────────────

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
    Box(
        modifier = modifier,
        contentAlignment = Alignment.Center
    ) {
        Card(
            modifier = Modifier
                .widthIn(min = 300.dp, max = 520.dp)
                .fillMaxWidth(0.88f),
            shape = MaterialTheme.shapes.extraLarge,
            elevation = CardDefaults.cardElevation(defaultElevation = 3.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
        ) {
            Column(
                modifier = Modifier.verticalScroll(rememberScrollState())
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(MaterialTheme.colorScheme.primary)
                        .padding(vertical = 28.dp, horizontal = 24.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Surface(
                            shape = androidx.compose.foundation.shape.CircleShape,
                            color = MaterialTheme.colorScheme.onPrimary,
                            modifier = Modifier.size(64.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(
                                    Icons.Default.Check,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.size(34.dp)
                                )
                            }
                        }
                        Text(
                            "Transaksi Berhasil!",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onPrimary
                        )
                        Text(
                            "INV-2024-0001",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.7f)
                        )
                    }
                }
                Column(
                    modifier = Modifier.padding(24.dp),
                    verticalArrangement = Arrangement.spacedBy(0.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(vertical = 10.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("Total Pembayaran", style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant)
                        Text(formatRupiah(75000), style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                    }
                    HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(vertical = 10.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(5.dp)) {
                            Icon(Icons.Default.SwapHoriz, contentDescription = null,
                                tint = MaterialTheme.colorScheme.secondary, modifier = Modifier.size(15.dp))
                            Text("Kembalian", style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                        Text(formatRupiah(25000), style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.SemiBold, color = MaterialTheme.colorScheme.secondary)
                    }
                    HorizontalDivider(modifier = Modifier.padding(bottom = 20.dp),
                        color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                        OutlinedButton(onClick = {}, modifier = Modifier.weight(1f),
                            shape = MaterialTheme.shapes.medium) {
                            Icon(Icons.Default.Print, contentDescription = null, modifier = Modifier.size(15.dp))
                            Spacer(Modifier.width(6.dp))
                            Text("Cetak Struk")
                        }
                        RancakButton(text = "Transaksi Baru", onClick = {}, modifier = Modifier.weight(1f))
                    }
                }
            }
        }
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

package id.rancak.app.presentation.ui.payment

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CallSplit
import androidx.compose.material.icons.filled.Payments
import androidx.compose.material.icons.filled.PointOfSale
import androidx.compose.material3.AssistChip
import androidx.compose.material3.AssistChipDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.zIndex
import id.rancak.app.data.local.SettingsStore
import id.rancak.app.data.printing.PrinterManager
import id.rancak.app.domain.model.PaymentMethod
import id.rancak.app.presentation.components.ErrorBanner
import id.rancak.app.presentation.components.RancakTopBar
import id.rancak.app.presentation.ui.payment.components.PaymentFormContent
import id.rancak.app.presentation.ui.payment.components.PaymentSuccessContent
import id.rancak.app.presentation.ui.payment.components.QrisWaitingContent
import id.rancak.app.presentation.ui.payment.components.SplitPaymentPanel
import id.rancak.app.presentation.viewmodel.CartViewModel
import id.rancak.app.presentation.viewmodel.PaymentViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import org.koin.compose.koinInject
import org.koin.compose.viewmodel.koinViewModel

/**
 * Entry point layar pembayaran. Menentukan state besar (form / QRIS waiting /
 * success) lalu mendelegasikan rendering ke komponen di
 * [id.rancak.app.presentation.ui.payment.components].
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PaymentScreen(
    onBack: () -> Unit,
    onPaymentComplete: () -> Unit,
    cartViewModel: CartViewModel,
    paymentViewModel: PaymentViewModel = koinViewModel()
) {
    val cartState    by cartViewModel.uiState.collectAsStateWithLifecycle()
    val paymentState by paymentViewModel.uiState.collectAsStateWithLifecycle()
    val printerManager: PrinterManager = koinInject()
    val settingsStore: SettingsStore   = koinInject()

    Scaffold(
        topBar = {
            RancakTopBar(
                title    = "Pembayaran",
                icon     = Icons.Default.PointOfSale,
                subtitle = if (paymentState.isSplitPayment) "Bayar terpisah dengan beberapa metode"
                           else "Pilih metode & masukkan jumlah bayar",
                onBack   = onBack
            )
        }
    ) { padding ->
        Box(modifier = Modifier.fillMaxSize()) {
            ErrorBanner(
                error     = paymentState.error,
                onDismiss = paymentViewModel::clearError,
                modifier  = Modifier
                    .fillMaxWidth()
                    .align(Alignment.TopCenter)
                    .zIndex(10f)
            )

            when {
                paymentState.completedSale != null -> PaymentSuccessContent(
                    sale            = paymentState.completedSale!!,
                    printerManager  = printerManager,
                    settingsStore   = settingsStore,
                    onNewTransaction = {
                        onPaymentComplete()
                        cartViewModel.clearCart()
                    },
                    modifier = Modifier.fillMaxSize().padding(padding)
                )

                paymentState.isQrisWaiting -> QrisWaitingContent(
                    qrString  = paymentState.qrisQrString!!,
                    amount    = paymentState.qrisAmount,
                    isPolling = paymentState.isQrisPolling,
                    onCancel  = paymentViewModel::cancelQrisPayment,
                    modifier  = Modifier.fillMaxSize().padding(padding)
                )

                else -> Column(modifier = Modifier.fillMaxSize().padding(padding)) {
                    // Toggle split vs single payment
                    PaymentModeToggle(
                        isSplit = paymentState.isSplitPayment,
                        onToggle = paymentViewModel::toggleSplitPayment
                    )

                    if (paymentState.isSplitPayment) {
                        SplitPaymentPanel(
                            itemCount       = cartState.itemCount,
                            orderTotal      = cartState.subtotal,
                            splitPayments   = paymentState.splitPayments,
                            isProcessing    = paymentState.isProcessing,
                            onAddPayment    = paymentViewModel::addSplitPaymentEntry,
                            onRemovePayment = paymentViewModel::removeSplitPaymentEntry,
                            onProcess       = {
                                paymentViewModel.processPaymentWithSplit(
                                    items        = cartState.items,
                                    orderTotal   = cartState.subtotal,
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
                            modifier = Modifier.fillMaxSize()
                        )
                    } else {
                        PaymentFormContent(
                            itemCount          = cartState.itemCount,
                            subtotal           = cartState.subtotal,
                            selectedMethod     = paymentState.selectedMethod,
                            onSelectMethod     = paymentViewModel::selectMethod,
                            paidAmount         = paymentState.paidAmount,
                            onPaidAmountChange = paymentViewModel::setPaidAmount,
                            isCashSelected     = paymentState.selectedMethod == PaymentMethod.CASH,
                            isProcessing       = paymentState.isProcessing,
                            onProcessPayment   = {
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
                            modifier = Modifier.fillMaxSize()
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun PaymentModeToggle(
    isSplit: Boolean,
    onToggle: () -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 8.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        AssistChip(
            onClick = { if (isSplit) onToggle() },
            label = { Text("Bayar Tunggal") },
            leadingIcon = { Icon(Icons.Default.Payments, null) },
            colors = if (!isSplit) AssistChipDefaults.assistChipColors(
                containerColor = androidx.compose.material3.MaterialTheme.colorScheme.primaryContainer
            ) else AssistChipDefaults.assistChipColors()
        )
        AssistChip(
            onClick = { if (!isSplit) onToggle() },
            label = { Text("Bayar Terpisah") },
            leadingIcon = { Icon(Icons.Default.CallSplit, null) },
            colors = if (isSplit) AssistChipDefaults.assistChipColors(
                containerColor = androidx.compose.material3.MaterialTheme.colorScheme.primaryContainer
            ) else AssistChipDefaults.assistChipColors()
        )
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// Preview — memanggil PaymentFormContent (state default) di dalam Scaffold
// yang identik dengan PaymentScreen. Tidak menduplikasi logika "when".
// ─────────────────────────────────────────────────────────────────────────────

@OptIn(ExperimentalMaterial3Api::class)
@androidx.compose.ui.tooling.preview.Preview(name = "Payment – Form", widthDp = 390, heightDp = 844)
@Composable
private fun PaymentScreenFormPreview() {
    id.rancak.app.presentation.designsystem.RancakTheme {
        Scaffold(
            topBar = {
                RancakTopBar(
                    title    = "Pembayaran",
                    icon     = Icons.Default.PointOfSale,
                    subtitle = "Pilih metode & masukkan jumlah bayar",
                    onBack   = {}
                )
            }
        ) { padding ->
            PaymentFormContent(
                itemCount          = 3,
                subtotal           = 55_000,
                selectedMethod     = PaymentMethod.CASH,
                onSelectMethod     = {},
                paidAmount         = "60000",
                onPaidAmountChange = {},
                isCashSelected     = true,
                isProcessing       = false,
                onProcessPayment   = {},
                modifier           = Modifier.fillMaxSize().padding(padding)
            )
        }
    }
}

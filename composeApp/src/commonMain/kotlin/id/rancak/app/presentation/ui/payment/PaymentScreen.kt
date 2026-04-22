package id.rancak.app.presentation.ui.payment

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.PointOfSale
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.zIndex
import id.rancak.app.data.local.SettingsStore
import id.rancak.app.data.printing.PrinterManager
import id.rancak.app.domain.model.PaymentMethod
import id.rancak.app.presentation.components.ErrorBanner
import id.rancak.app.presentation.components.RancakTopBar
import id.rancak.app.presentation.ui.payment.components.PaymentFormContent
import id.rancak.app.presentation.ui.payment.components.PaymentSuccessContent
import id.rancak.app.presentation.ui.payment.components.QrisWaitingContent
import id.rancak.app.presentation.viewmodel.CartViewModel
import id.rancak.app.presentation.viewmodel.PaymentViewModel
import androidx.compose.runtime.collectAsState
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
    val cartState    by cartViewModel.uiState.collectAsState()
    val paymentState by paymentViewModel.uiState.collectAsState()
    val printerManager: PrinterManager = koinInject()
    val settingsStore: SettingsStore   = koinInject()

    Scaffold(
        topBar = {
            RancakTopBar(
                title    = "Pembayaran",
                icon     = Icons.Default.PointOfSale,
                subtitle = "Pilih metode & masukkan jumlah bayar",
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

                else -> PaymentFormContent(
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
                    modifier = Modifier.fillMaxSize().padding(padding)
                )
            }
        }
    }
}

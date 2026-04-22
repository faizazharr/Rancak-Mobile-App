package id.rancak.app.presentation.ui.payment

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
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
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.zIndex
import id.rancak.app.data.local.SettingsStore
import id.rancak.app.data.printing.PrinterManager
import id.rancak.app.domain.model.PaymentMethod
import id.rancak.app.domain.model.Resource
import id.rancak.app.domain.model.Sale
import id.rancak.app.domain.repository.SaleRepository
import id.rancak.app.presentation.components.ErrorBanner
import id.rancak.app.presentation.components.LoadingScreen
import id.rancak.app.presentation.components.RancakTopBar
import id.rancak.app.presentation.ui.payment.components.PaymentFormContent
import id.rancak.app.presentation.ui.payment.components.PaymentSuccessContent
import id.rancak.app.presentation.ui.payment.components.SplitPaymentPanel
import id.rancak.app.presentation.viewmodel.PaymentViewModel
import org.koin.compose.koinInject
import org.koin.compose.viewmodel.koinViewModel

/**
 * Layar pembayaran untuk held order — mendukung single dan split payment.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PayHeldOrderScreen(
    saleUuid: String,
    onBack: () -> Unit,
    onPaymentComplete: () -> Unit,
    paymentViewModel: PaymentViewModel = koinViewModel()
) {
    val saleRepository: SaleRepository = koinInject()
    val printerManager: PrinterManager = koinInject()
    val settingsStore: SettingsStore   = koinInject()
    val paymentState by paymentViewModel.uiState.collectAsStateWithLifecycle()

    var sale by remember { mutableStateOf<Sale?>(null) }
    var loadError by remember { mutableStateOf<String?>(null) }

    LaunchedEffect(saleUuid) {
        when (val result = saleRepository.getSaleDetail(saleUuid)) {
            is Resource.Success -> sale = result.data
            is Resource.Error   -> loadError = result.message
            is Resource.Loading -> {}
        }
    }

    Scaffold(
        topBar = {
            RancakTopBar(
                title    = "Bayar Pesanan",
                icon     = Icons.Default.PointOfSale,
                subtitle = sale?.invoiceNo ?: "Memuat...",
                onBack   = onBack
            )
        }
    ) { padding ->
        Box(modifier = Modifier.fillMaxSize()) {
            ErrorBanner(
                error     = paymentState.error ?: loadError,
                onDismiss = paymentViewModel::clearError,
                modifier  = Modifier.fillMaxWidth().align(Alignment.TopCenter).zIndex(10f)
            )

            val loadedSale = sale
            when {
                paymentState.completedSale != null -> PaymentSuccessContent(
                    sale             = paymentState.completedSale!!,
                    printerManager   = printerManager,
                    settingsStore    = settingsStore,
                    onNewTransaction = {
                        paymentViewModel.reset()
                        onPaymentComplete()
                    },
                    modifier = Modifier.fillMaxSize().padding(padding)
                )

                loadedSale == null -> LoadingScreen(modifier = Modifier.fillMaxSize().padding(padding))

                else -> Column(modifier = Modifier.fillMaxSize().padding(padding)) {
                    PayHeldOrderModeToggle(
                        isSplit  = paymentState.isSplitPayment,
                        onToggle = paymentViewModel::toggleSplitPayment
                    )

                    if (paymentState.isSplitPayment) {
                        SplitPaymentPanel(
                            itemCount       = loadedSale.items.size,
                            orderTotal      = loadedSale.total,
                            splitPayments   = paymentState.splitPayments,
                            isProcessing    = paymentState.isProcessing,
                            onAddPayment    = paymentViewModel::addSplitPaymentEntry,
                            onRemovePayment = paymentViewModel::removeSplitPaymentEntry,
                            onProcess       = {
                                paymentViewModel.processHeldOrderPaymentWithSplit(
                                    saleUuid   = loadedSale.uuid,
                                    orderTotal = loadedSale.total
                                )
                            },
                            modifier = Modifier.fillMaxSize()
                        )
                    } else {
                        PaymentFormContent(
                            itemCount          = loadedSale.items.size,
                            subtotal           = loadedSale.total,
                            selectedMethod     = paymentState.selectedMethod,
                            onSelectMethod     = paymentViewModel::selectMethod,
                            paidAmount         = paymentState.paidAmount,
                            onPaidAmountChange = paymentViewModel::setPaidAmount,
                            isCashSelected     = paymentState.selectedMethod == PaymentMethod.CASH,
                            isProcessing       = paymentState.isProcessing,
                            onProcessPayment   = {
                                paymentViewModel.processHeldOrderPayment(loadedSale.uuid)
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
private fun PayHeldOrderModeToggle(
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
                containerColor = MaterialTheme.colorScheme.primaryContainer
            ) else AssistChipDefaults.assistChipColors()
        )
        AssistChip(
            onClick = { if (!isSplit) onToggle() },
            label = { Text("Bayar Terpisah") },
            leadingIcon = { Icon(Icons.Default.CallSplit, null) },
            colors = if (isSplit) AssistChipDefaults.assistChipColors(
                containerColor = MaterialTheme.colorScheme.primaryContainer
            ) else AssistChipDefaults.assistChipColors()
        )
    }
}

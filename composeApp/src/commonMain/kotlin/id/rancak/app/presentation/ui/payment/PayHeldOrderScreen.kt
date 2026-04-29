package id.rancak.app.presentation.ui.payment

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.PointOfSale
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
import id.rancak.app.presentation.viewmodel.SplitableItem
import org.koin.compose.koinInject
import org.koin.compose.viewmodel.koinViewModel

/**
 * Layar pembayaran untuk held order — mendukung single dan split payment.
 */
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
        Box(modifier = Modifier.fillMaxSize().padding(padding)) {
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
                    modifier = Modifier.fillMaxSize()
                )

                loadedSale == null -> LoadingScreen(modifier = Modifier.fillMaxSize())

                else -> Column(modifier = Modifier.fillMaxSize()) {

                    if (paymentState.isSplitPayment) {
                        LaunchedEffect(Unit) {
                            paymentViewModel.initSplitItems(
                                loadedSale.items.mapIndexed { idx, saleItem ->
                                    SplitableItem(
                                        index       = idx,
                                        name        = saleItem.productName,
                                        qty         = saleItem.qty.toIntOrNull() ?: 1,
                                        price       = saleItem.price,
                                        variantName = saleItem.variantName
                                    )
                                }
                            )
                        }
                        SplitPaymentPanel(
                            items           = paymentState.splitableItems,
                            splitGroups     = paymentState.splitGroups,
                            currentItemQtys = paymentState.currentSplitItemQtys,
                            currentMethod   = paymentState.currentSplitMethod,
                            currentCashInput = paymentState.currentSplitCashInput,
                            orderTotal      = loadedSale.total,
                            isProcessing    = paymentState.isProcessing,
                            onSetItemQty    = paymentViewModel::setCurrentSplitItemQty,
                            onSetMethod     = paymentViewModel::setCurrentSplitMethod,
                            onSetCashInput  = paymentViewModel::setCurrentSplitCashInput,
                            onConfirmGroup  = paymentViewModel::confirmCurrentSplitGroup,
                            onRemoveGroup   = paymentViewModel::removeSplitGroup,
                            isSplit         = paymentState.isSplitPayment,
                            onToggleMode    = paymentViewModel::toggleSplitPayment,
                            onProcess            = {
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
                                paymentViewModel.processHeldOrderPayment(loadedSale.uuid, loadedSale.total)
                            },
                            onQrisSelected = {
                                paymentViewModel.processHeldOrderPayment(loadedSale.uuid, loadedSale.total)
                            },
                            isQrisWaiting      = paymentState.isQrisWaiting,
                            qrisQrString       = paymentState.qrisQrString,
                            qrisAmount         = paymentState.qrisAmount,
                            isQrisPolling      = paymentState.isQrisPolling,
                            onCancelQris       = paymentViewModel::cancelQrisPayment,
                            isSplit            = paymentState.isSplitPayment,
                            onToggleMode       = paymentViewModel::toggleSplitPayment,
                            modifier = Modifier.fillMaxSize()
                        )
                    }
                }
            }
        }
    }
}



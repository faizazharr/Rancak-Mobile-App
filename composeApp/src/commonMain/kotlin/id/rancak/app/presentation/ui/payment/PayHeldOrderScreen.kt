package id.rancak.app.presentation.ui.payment

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.PointOfSale
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
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
import id.rancak.app.data.local.OpenBillStore
import id.rancak.app.data.local.SettingsStore
import id.rancak.app.data.printing.PrinterManager
import id.rancak.app.data.printing.ReceiptData
import id.rancak.app.data.printing.ReceiptItem
import id.rancak.app.domain.model.PaymentMethod
import id.rancak.app.presentation.components.ErrorBanner
import id.rancak.app.presentation.components.LoadingScreen
import id.rancak.app.presentation.components.PartialReceiptPrintDialog
import id.rancak.app.presentation.components.RancakTopBar
import id.rancak.app.presentation.ui.payment.components.OrderLineItem
import id.rancak.app.presentation.ui.payment.components.PaymentFormContent
import id.rancak.app.presentation.ui.payment.components.PaymentSuccessContent
import id.rancak.app.presentation.ui.payment.components.QrisWaitingContent
import id.rancak.app.presentation.ui.payment.components.SplitPaymentPanel
import id.rancak.app.presentation.viewmodel.PaymentViewModel
import id.rancak.app.presentation.viewmodel.SplitableItem
import kotlin.time.Clock
import kotlinx.collections.immutable.toImmutableList
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import org.koin.compose.koinInject
import org.koin.compose.viewmodel.koinViewModel

/**
 * Layar pembayaran untuk held order — mendukung single dan split payment.
 */
@Composable
fun PayHeldOrderScreen(
    saleUuid: String,
    onBack: () -> Unit,
    onPaymentComplete: () -> Unit
) {
    val paymentViewModel: PaymentViewModel = koinViewModel()
    val printerManager: PrinterManager = koinInject()
    val settingsStore: SettingsStore   = koinInject()
    val openBillStore: OpenBillStore   = koinInject()
    val paymentState by paymentViewModel.uiState.collectAsStateWithLifecycle()

    // Per-customer receipt printing (split payment)
    var pendingGroupReceiptData by remember { mutableStateOf<ReceiptData?>(null) }
    var pendingGroupLabel       by remember { mutableStateOf("") }

    val onConfirmAndPrint: (Long) -> Unit = { groupActualTotal ->
        val state = paymentViewModel.uiState.value
        val currentItemQtys = state.currentSplitItemQtys
        val currentMethod   = state.currentSplitMethod
        val cashPaid        = state.currentSplitCashInput.toLongOrNull() ?: 0L
        val groupId         = (state.splitGroups.maxOfOrNull { it.id } ?: 0) + 1

        val receiptItems = state.splitableItems.mapNotNull { item ->
            val qty = currentItemQtys[item.index] ?: return@mapNotNull null
            if (qty <= 0) return@mapNotNull null
            ReceiptItem(
                name        = item.name,
                variantName = item.variantName,
                qty         = qty,
                price       = item.price,
                subtotal    = item.price * qty
            )
        }
        val subtotal = receiptItems.sumOf { it.subtotal }
        val paid     = if (currentMethod == PaymentMethod.CASH) cashPaid else subtotal
        val change   = if (currentMethod == PaymentMethod.CASH) maxOf(0L, cashPaid - subtotal) else 0L

        val now = Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault())
        val createdAt = "${now.date} ${now.hour.toString().padStart(2,'0')}:${now.minute.toString().padStart(2,'0')}"

        pendingGroupLabel       = "Pelanggan $groupId"
        pendingGroupReceiptData = ReceiptData(
            storeName     = settingsStore.receiptStoreName.ifBlank { "Rancak" },
            storeAddress  = settingsStore.receiptStoreAddress.ifBlank { null },
            storePhone    = settingsStore.receiptStorePhone.ifBlank { null },
            invoiceNo     = "Pelanggan $groupId",
            orderType     = "Bayar Terpisah",
            createdAt     = createdAt,
            items         = receiptItems,
            subtotal      = subtotal,
            total         = subtotal,
            paymentMethod = currentMethod.value,
            paidAmount    = paid,
            changeAmount  = change,
            footerText    = settingsStore.receiptFooter.ifBlank { null }
        )

        paymentViewModel.confirmCurrentSplitGroup(groupActualTotal)
    }

    LaunchedEffect(saleUuid) { paymentViewModel.loadHeldSale(saleUuid) }

    // Dialog: order sudah dibayar sebelumnya (stale local bill)
    if (paymentState.saleAlreadyPaid) {
        AlertDialog(
            onDismissRequest = paymentViewModel::clearAlreadyPaid,
            title   = { Text("Pesanan Sudah Dibayar") },
            text    = { Text("Pesanan ini sudah lunas sebelumnya. Hapus dari daftar Open Bill?") },
            confirmButton = {
                TextButton(onClick = {
                    openBillStore.getAll()
                        .find { it.remoteSaleUuid == saleUuid }
                        ?.let { openBillStore.remove(it.id) }
                    paymentViewModel.clearAlreadyPaid()
                    onPaymentComplete()
                }) { Text("Hapus dari Daftar") }
            },
            dismissButton = {
                TextButton(onClick = paymentViewModel::clearAlreadyPaid) { Text("Tutup") }
            }
        )
    }

    Scaffold(
        topBar = {
            RancakTopBar(
                title    = "Bayar Pesanan",
                icon     = Icons.Default.PointOfSale,
                subtitle = paymentState.heldSale?.invoiceNo ?: "Memuat...",
                onBack   = onBack
            )
        }
    ) { padding ->
        Box(modifier = Modifier.fillMaxSize().padding(padding)) {
            ErrorBanner(
                error     = paymentState.error ?: paymentState.heldSaleError,
                onDismiss = paymentViewModel::clearError,
                modifier  = Modifier.fillMaxWidth().align(Alignment.TopCenter).zIndex(10f)
            )

            val loadedSale = paymentState.heldSale
            when {
                paymentState.completedSale != null -> PaymentSuccessContent(
                    sale             = paymentState.completedSale!!,
                    printerManager   = printerManager,
                    settingsStore    = settingsStore,
                    onNewTransaction = {
                        // Hapus open bill lokal setelah pembayaran berhasil
                        openBillStore.getAll()
                            .find { it.remoteSaleUuid == saleUuid }
                            ?.let { openBillStore.remove(it.id) }
                        paymentViewModel.reset()
                        onPaymentComplete()
                    },
                    modifier = Modifier.fillMaxSize()
                )

                // QRIS waiting dalam split mode — tampil sebagai full-screen overlay
                paymentState.isQrisWaiting && paymentState.isSplitPayment -> {
                    QrisWaitingContent(
                        qrString  = paymentState.qrisQrString!!,
                        amount    = paymentState.qrisAmount,
                        isPolling = paymentState.isQrisPolling,
                        onCancel  = paymentViewModel::cancelQrisPayment,
                        modifier  = Modifier.fillMaxSize()
                    )
                }

                loadedSale == null -> LoadingScreen(modifier = Modifier.fillMaxSize())

                else -> Column(modifier = Modifier.fillMaxSize()) {

                    if (paymentState.isSplitPayment) {
                        LaunchedEffect(Unit) {
                            paymentViewModel.initSplitItems(
                                loadedSale.items.mapIndexed { idx, saleItem ->
                                    SplitableItem(
                                        index       = idx,
                                        name        = saleItem.productName,
                                        qty         = saleItem.qty.toDoubleOrNull()?.toInt() ?: 1,
                                        price       = saleItem.price,
                                        variantName = saleItem.variantName
                                    )
                                }
                            )
                        }
                        SplitPaymentPanel(
                            items           = paymentState.splitableItems.toImmutableList(),
                            splitGroups     = paymentState.splitGroups.toImmutableList(),
                            currentItemQtys = paymentState.currentSplitItemQtys,
                            currentMethod   = paymentState.currentSplitMethod,
                            currentCashInput = paymentState.currentSplitCashInput,
                            orderTotal      = loadedSale.total,
                            isProcessing    = paymentState.isProcessing,
                            merchantQrisString = settingsStore.merchantQrisString,
                            onSetItemQty    = paymentViewModel::setCurrentSplitItemQty,
                            onSetMethod     = paymentViewModel::setCurrentSplitMethod,
                            onSetCashInput  = paymentViewModel::setCurrentSplitCashInput,
                            onConfirmGroup  = { groupActualTotal -> paymentViewModel.confirmCurrentSplitGroup(groupActualTotal) },
                            onConfirmAndPrint = onConfirmAndPrint,
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
                            itemCount          = loadedSale.items.sumOf { it.qty.toDoubleOrNull()?.toInt() ?: 1 },
                            subtotal           = loadedSale.subtotal,
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
                            discount           = loadedSale.discount + loadedSale.voucherDiscount + loadedSale.autoDiscount,
                            tax                = loadedSale.tax,
                            adminFee           = loadedSale.adminFee,
                            deliveryFee        = loadedSale.deliveryFee,
                            tip                = loadedSale.tip,
                            orderItems         = loadedSale.items.map { saleItem ->
                                val qty = saleItem.qty.toDoubleOrNull()?.toInt() ?: 1
                                OrderLineItem(
                                    name        = saleItem.productName,
                                    variantName = saleItem.variantName,
                                    qty         = qty,
                                    price       = saleItem.price,
                                    subtotal    = saleItem.subtotal
                                )
                            }.toImmutableList(),
                            modifier = Modifier.fillMaxSize()
                        )
                    }
                }
            }
        }
    }

    // Dialog cetak struk per-pelanggan (split payment)
    val receiptData = pendingGroupReceiptData
    if (receiptData != null) {
        PartialReceiptPrintDialog(
            groupLabel     = pendingGroupLabel,
            receiptData    = receiptData,
            printerManager = printerManager,
            settingsStore  = settingsStore,
            onDismiss      = { pendingGroupReceiptData = null }
        )
    }
}



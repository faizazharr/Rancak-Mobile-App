package id.rancak.app.presentation.ui.payment

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Payments
import androidx.compose.material.icons.filled.PointOfSale
import androidx.compose.material3.AssistChip
import androidx.compose.material3.AssistChipDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import id.rancak.app.data.local.SettingsStore
import id.rancak.app.data.printing.PrinterManager
import id.rancak.app.data.printing.ReceiptData
import id.rancak.app.data.printing.ReceiptItem
import id.rancak.app.domain.model.PaymentMethod
import id.rancak.app.domain.model.OrderType
import id.rancak.app.presentation.ui.payment.components.NamedAmount
import id.rancak.app.presentation.components.PartialReceiptPrintDialog
import id.rancak.app.presentation.components.RancakTopBar
import id.rancak.app.presentation.ui.payment.components.OrderLineItem
import id.rancak.app.presentation.ui.payment.components.PaymentFormContent
import id.rancak.app.presentation.ui.payment.components.PaymentSuccessContent
import id.rancak.app.presentation.ui.payment.components.QrisWaitingContent
import id.rancak.app.presentation.ui.payment.components.SplitPaymentPanel
import id.rancak.app.presentation.viewmodel.CartViewModel
import id.rancak.app.presentation.viewmodel.PaymentViewModel
import id.rancak.app.presentation.viewmodel.SplitableItem
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import kotlin.time.Clock
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
    val snackbarHostState = remember { SnackbarHostState() }

    // Tampilkan error via Snackbar (tidak tertutup oleh top bar dan tidak hilang
    // saat user menekan numpad — lebih mudah terlihat daripada banner di atas).
    LaunchedEffect(paymentState.error) {
        val msg = paymentState.error
        if (!msg.isNullOrBlank()) {
            snackbarHostState.showSnackbar(msg)
            paymentViewModel.clearError()
        }
    }

    // ── Breakdown pajak & surcharge per-line agar ringkasan pembayaran
    //    persis match dengan tampilan kasir (per-konfigurasi, bukan agregat). ──
    val taxLines: List<NamedAmount> = remember(cartState) {
        buildList {
            if (cartState.tax > 0) add(NamedAmount("Pajak", cartState.tax))
            cartState.activeTaxConfigs.forEach { cfg ->
                val basis = if (cfg.applyTo == "subtotal") cartState.subtotal
                            else (cartState.subtotal - cartState.discount).coerceAtLeast(0L)
                val amt = ((basis * (cfg.rate * 100).toLong()) / 10_000L).coerceAtLeast(0L)
                if (amt > 0) add(NamedAmount("${cfg.name} (${cfg.rate}%)", amt))
            }
        }
    }
    val surchargeLines: List<NamedAmount> = remember(cartState) {
        buildList {
            if (cartState.adminFee > 0) add(NamedAmount("Biaya Admin", cartState.adminFee))
            cartState.activeSurcharges.forEach { sc ->
                val raw = if (sc.isPercentage) {
                    val basis = (cartState.subtotal - cartState.discount).coerceAtLeast(0L)
                    (basis * sc.amount / 100L).coerceAtLeast(0L)
                } else sc.amount
                val amt = sc.maxAmount?.let { cap -> raw.coerceAtMost(cap) } ?: raw
                val label = sc.name + if (sc.isPercentage) " (${sc.amount}%)" else ""
                if (amt > 0) add(NamedAmount(label, amt))
            }
        }
    }
    val orderTypeLabel = when (cartState.orderType) {
        OrderType.DINE_IN  -> "Dine In"
        OrderType.TAKEAWAY -> "Take Away"
        OrderType.DELIVERY -> "Delivery"
    }

    // Per-customer receipt printing (split payment)
    var pendingGroupReceiptData by remember { mutableStateOf<ReceiptData?>(null) }
    var pendingGroupLabel       by remember { mutableStateOf("") }

    // Builds ReceiptData from the current in-progress group and triggers print dialog
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

    Scaffold(
        topBar = {
            RancakTopBar(
                title    = "Pembayaran",
                icon     = Icons.Default.PointOfSale,
                subtitle = if (paymentState.isSplitPayment) "Bayar terpisah dengan beberapa metode"
                           else "Pilih metode & masukkan jumlah bayar",
                onBack   = onBack
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { padding ->
        Box(modifier = Modifier.fillMaxSize()) {

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

                // QRIS waiting dalam split mode — tampil sebagai full-screen overlay
                paymentState.isQrisWaiting && paymentState.isSplitPayment -> {
                    QrisWaitingContent(
                        qrString  = paymentState.qrisQrString!!,
                        amount    = paymentState.qrisAmount,
                        isPolling = paymentState.isQrisPolling,
                        onCancel  = paymentViewModel::cancelQrisPayment,
                        modifier  = Modifier.fillMaxSize().padding(padding)
                    )
                }

                else -> Column(modifier = Modifier.fillMaxSize().padding(padding)) {
                    if (paymentState.isSplitPayment) {
                        // Init split items whenever entering split mode
                        LaunchedEffect(Unit) {
                            paymentViewModel.initSplitItems(
                                cartState.items.mapIndexed { idx, item ->
                                    SplitableItem(
                                        index       = idx,
                                        name        = item.productName,
                                        qty         = item.qty,
                                        price       = item.price,
                                        variantName = item.variantName
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
                            orderTotal      = cartState.total,
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
                                val heldUuid = cartState.activeOpenBillSaleUuid
                                if (heldUuid != null) {
                                    paymentViewModel.processHeldOrderPaymentWithSplit(
                                        saleUuid   = heldUuid,
                                        orderTotal = cartState.total
                                    )
                                } else {
                                    paymentViewModel.processPaymentWithSplit(
                                        items        = cartState.items,
                                        orderTotal   = cartState.total,
                                        orderType    = cartState.orderType,
                                        tableUuid    = cartState.tableUuid,
                                        customerName = cartState.customerName,
                                        note         = cartState.note,
                                        pax          = cartState.pax,
                                        discount     = cartState.discount,
                                        tax          = cartState.totalTax,
                                        adminFee     = cartState.totalSurcharge,
                                        deliveryFee  = cartState.deliveryFee,
                                        tip          = cartState.tip,
                                        voucherCode  = cartState.voucherCode.takeIf { it.isNotBlank() }
                                    )
                                }
                            },
                            modifier = Modifier.fillMaxSize()
                        )
                    } else {
                        val processPaymentArgs = {
                            val heldUuid = cartState.activeOpenBillSaleUuid
                            if (heldUuid != null) {
                                paymentViewModel.processHeldOrderPayment(
                                    saleUuid  = heldUuid,
                                    saleTotal = cartState.total
                                )
                            } else {
                                paymentViewModel.processPayment(
                                    items        = cartState.items,
                                    orderType    = cartState.orderType,
                                    tableUuid    = cartState.tableUuid,
                                    customerName = cartState.customerName,
                                    note         = cartState.note,
                                    pax          = cartState.pax,
                                    discount     = cartState.discount,
                                    tax          = cartState.totalTax,
                                    adminFee     = cartState.totalSurcharge,
                                    deliveryFee  = cartState.deliveryFee,
                                    tip          = cartState.tip,
                                    voucherCode  = cartState.voucherCode.takeIf { it.isNotBlank() }
                                )
                            }
                        }
                        PaymentFormContent(
                            itemCount          = cartState.itemCount,
                            subtotal           = cartState.subtotal,
                            selectedMethod     = paymentState.selectedMethod,
                            onSelectMethod     = paymentViewModel::selectMethod,
                            paidAmount         = paymentState.paidAmount,
                            onPaidAmountChange = paymentViewModel::setPaidAmount,
                            isCashSelected     = paymentState.selectedMethod == PaymentMethod.CASH,
                            isProcessing       = paymentState.isProcessing,
                            isSplit            = paymentState.isSplitPayment,
                            onToggleMode       = paymentViewModel::toggleSplitPayment,
                            onProcessPayment   = processPaymentArgs,
                            onQrisSelected     = processPaymentArgs,
                            isQrisWaiting      = paymentState.isQrisWaiting,
                            qrisQrString       = paymentState.qrisQrString,
                            qrisAmount         = paymentState.qrisAmount,
                            isQrisPolling      = paymentState.isQrisPolling,
                            onCancelQris       = paymentViewModel::cancelQrisPayment,
                            discount           = cartState.discount,
                            tax                = cartState.totalTax,
                            adminFee           = cartState.totalSurcharge,
                            deliveryFee        = cartState.deliveryFee,
                            tip                = cartState.tip,
                            orderItems         = cartState.items.map { item ->
                                OrderLineItem(
                                    name        = item.productName,
                                    variantName = item.variantName,
                                    qty         = item.qty,
                                    price       = item.price,
                                    subtotal    = item.subtotal
                                )
                            },
                            taxLines       = taxLines,
                            surchargeLines = surchargeLines,
                            orderTypeLabel = orderTypeLabel,
                            customerName   = cartState.customerName,
                            pax            = cartState.pax,
                            voucherCode    = cartState.voucherCode,
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

package id.rancak.app.presentation.ui.billing

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CreditCard
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import id.rancak.app.presentation.components.RancakTopBar
import id.rancak.app.presentation.ui.billing.components.BillingContent
import id.rancak.app.presentation.ui.billing.components.BillingQrPaymentDialog
import id.rancak.app.presentation.ui.billing.components.CancelInvoiceDialog
import id.rancak.app.presentation.ui.billing.components.SubscribeConfirmDialog
import id.rancak.app.presentation.viewmodel.BillingViewModel
import kotlinx.collections.immutable.toImmutableList
import org.koin.compose.viewmodel.koinViewModel

// ─────────────────────────────────────────────────────────────────────────────
// Entry point
// ─────────────────────────────────────────────────────────────────────────────

@Composable
fun BillingScreen(
    onBack: (() -> Unit)? = null,
    onNavigateUp: (() -> Unit)? = null,
    /** Dipanggil setelah pembayaran subscription dikonfirmasi — navigasi ke POS. */
    onPaymentComplete: () -> Unit = {}
) {
    val viewModel: BillingViewModel = koinViewModel()
    val state by viewModel.uiState.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(state.error) {
        state.error?.let {
            snackbarHostState.showSnackbar(it)
            viewModel.clearError()
        }
    }
    LaunchedEffect(state.successMessage) {
        state.successMessage?.let {
            snackbarHostState.showSnackbar(it)
            viewModel.clearSuccessMessage()
        }
    }

    // Saat polling mendeteksi status "paid" → bersihkan flag lalu arahkan ke POS.
    LaunchedEffect(state.isPaymentComplete) {
        if (state.isPaymentComplete) {
            viewModel.clearPaymentComplete()
            onPaymentComplete()
        }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            RancakTopBar(
                title = "Billing & Langganan",
                icon = Icons.Default.CreditCard,
                onMenu = onBack,
                onBack = onNavigateUp
            )
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            when {
                state.isLoading -> {
                    CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
                }
                else -> {
                    BillingContent(
                        subscription = state.subscription,
                        plans = state.plans.toImmutableList(),
                        invoices = state.invoices.toImmutableList(),
                        onSubscribe = { viewModel.openSubscribeDialog(it) },
                        onCancelInvoice = { viewModel.openCancelDialog(it) },
                        onRefresh = { viewModel.loadAll() }
                    )
                }
            }
        }
    }

    // ── Dialogs ───────────────────────────────────────────────────────────────

    // 1. Konfirmasi berlangganan (tampilkan detail paket + harga sebelum buat invoice)
    if (state.showSubscribeDialog && state.selectedPlan != null) {
        SubscribeConfirmDialog(
            plan = state.selectedPlan!!,
            isSubmitting = state.isSubmitting,
            onConfirm = { viewModel.subscribe() },
            onDismiss = { viewModel.closeSubscribeDialog() }
        )
    }

    // 2. QR pembayaran — muncul langsung setelah invoice dibuat dan QR string tersedia.
    //    Polling berjalan di ViewModel; dialog ditutup otomatis saat status = "paid".
    if (state.qrInvoice != null) {
        BillingQrPaymentDialog(
            invoice   = state.qrInvoice!!,
            isPolling = state.isPolling,
            onDismiss = { viewModel.dismissQrPayment() }
        )
    }

    // 3. Konfirmasi pembatalan invoice
    if (state.showCancelDialog && state.cancelTargetInvoice != null) {
        CancelInvoiceDialog(
            invoice = state.cancelTargetInvoice!!,
            isSubmitting = state.isSubmitting,
            onConfirm = { viewModel.cancelInvoice() },
            onDismiss = { viewModel.closeCancelDialog() }
        )
    }
}





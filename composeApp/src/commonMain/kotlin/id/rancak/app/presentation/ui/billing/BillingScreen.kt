package id.rancak.app.presentation.ui.billing

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CreditCard
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import id.rancak.app.presentation.components.RancakTopBar
import id.rancak.app.presentation.ui.billing.components.*
import id.rancak.app.presentation.viewmodel.BillingViewModel
import org.koin.compose.viewmodel.koinViewModel

// ─────────────────────────────────────────────────────────────────────────────
// Entry point
// ─────────────────────────────────────────────────────────────────────────────

@Composable
fun BillingScreen(onBack: () -> Unit) {
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

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            RancakTopBar(
                title = "Billing & Langganan",
                icon = Icons.Default.CreditCard,
                onBack = onBack
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
                        plans = state.plans,
                        invoices = state.invoices,
                        onSubscribe = { viewModel.openSubscribeDialog(it) },
                        onCancelInvoice = { viewModel.openCancelDialog(it) },
                        onRefresh = { viewModel.loadAll() }
                    )
                }
            }
        }
    }

    // ── Dialogs ───────────────────────────────────────────────────────────────
    if (state.showSubscribeDialog && state.selectedPlan != null) {
        SubscribeConfirmDialog(
            plan = state.selectedPlan!!,
            isSubmitting = state.isSubmitting,
            onConfirm = { viewModel.subscribe() },
            onDismiss = { viewModel.closeSubscribeDialog() }
        )
    }

    if (state.showCancelDialog && state.cancelTargetInvoice != null) {
        CancelInvoiceDialog(
            invoice = state.cancelTargetInvoice!!,
            isSubmitting = state.isSubmitting,
            onConfirm = { viewModel.cancelInvoice() },
            onDismiss = { viewModel.closeCancelDialog() }
        )
    }
}




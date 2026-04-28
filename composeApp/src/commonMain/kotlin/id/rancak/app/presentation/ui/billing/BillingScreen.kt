package id.rancak.app.presentation.ui.billing

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import id.rancak.app.domain.model.Invoice
import id.rancak.app.domain.model.Plan
import id.rancak.app.domain.model.SubscriptionState
import id.rancak.app.presentation.components.RancakTopBar
import id.rancak.app.presentation.designsystem.*
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

// ─────────────────────────────────────────────────────────────────────────────
// Main content
// ─────────────────────────────────────────────────────────────────────────────

@Composable
private fun BillingContent(
    subscription: SubscriptionState?,
    plans: List<Plan>,
    invoices: List<Invoice>,
    onSubscribe: (Plan) -> Unit,
    onCancelInvoice: (Invoice) -> Unit,
    onRefresh: () -> Unit
) {
    BoxWithConstraints(modifier = Modifier.fillMaxSize()) {
        val isTablet = maxWidth >= 600.dp
        val hPad = if (isTablet) 24.dp else 16.dp
        val planCols = if (isTablet) 2 else 1
        val planRows = plans.chunked(planCols)

        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(horizontal = hPad, vertical = 14.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            item {
                SubscriptionCard(subscription = subscription, isTablet = isTablet)
            }

            if (plans.isNotEmpty()) {
                item { SectionLabel(Icons.Default.Stars, "Paket Langganan") }
                items(planRows) { row ->
                    Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                        row.forEach { plan ->
                            PlanCard(
                                plan = plan,
                                isCurrentPlan = subscription?.plan == plan.code,
                                onSubscribe = { onSubscribe(plan) },
                                modifier = Modifier.weight(1f)
                            )
                        }
                        if (row.size < planCols) Spacer(Modifier.weight(1f))
                    }
                }
            }

            if (invoices.isNotEmpty()) {
                item { SectionLabel(Icons.Default.Receipt, "Riwayat Invoice (${invoices.size})") }
                items(invoices, key = { it.uuid }) { invoice ->
                    InvoiceCard(invoice = invoice, onCancel = { onCancelInvoice(invoice) })
                }
            }

            if (invoices.isEmpty() && plans.isEmpty() && subscription == null) {
                item {
                    Column(
                        modifier = Modifier.fillMaxWidth().padding(vertical = 40.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Icon(Icons.Default.CreditCard, null,
                            modifier = Modifier.size(48.dp),
                            tint = MaterialTheme.colorScheme.outlineVariant)
                        Text("Belum ada data billing",
                            color = MaterialTheme.colorScheme.onSurfaceVariant)
                        OutlinedButton(onClick = onRefresh) { Text("Muat Ulang") }
                    }
                }
            }

            item { Spacer(Modifier.height(12.dp)) }
        }
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// Subscription hero card
// ─────────────────────────────────────────────────────────────────────────────

@Composable
private fun SubscriptionCard(subscription: SubscriptionState?, isTablet: Boolean) {
    val (gradientStart, gradientEnd, statusLabel, statusIcon) = when (subscription?.status) {
        "active"  -> Quadruple(Primary, Color(0xFF1DB88A), "Aktif", Icons.Default.CheckCircle)
        "trial"   -> Quadruple(Tertiary, Color(0xFF5588EE), "Trial", Icons.Default.HourglassTop)
        "expired" -> Quadruple(Color(0xFF9E9E9E), Color(0xFF757575), "Kedaluwarsa", Icons.Default.ErrorOutline)
        else      -> Quadruple(Color(0xFF9E9E9E), Color(0xFF757575), "Tidak Aktif", Icons.Default.Block)
    }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(Brush.linearGradientBrush(listOf(gradientStart, gradientEnd)))
            .padding(horizontal = 16.dp, vertical = 14.dp)
    ) {
        if (isTablet) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Icon(statusIcon, null, tint = Color.White, modifier = Modifier.size(14.dp))
                        Text("Status Langganan", style = MaterialTheme.typography.labelSmall,
                            color = Color.White.copy(alpha = 0.75f))
                        SubStatusPill(statusLabel)
                    }
                    Text(
                        subscription?.plan?.uppercase() ?: "TIDAK ADA PAKET",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.ExtraBold, color = Color.White
                    )
                }
                if (subscription != null) {
                    Row(horizontalArrangement = Arrangement.spacedBy(24.dp)) {
                        SubInfoItem("Mulai", subscription.startedAt?.take(10) ?: "-")
                        SubInfoItem("Berakhir", subscription.expiresAt?.take(10) ?: "-", Alignment.CenterHorizontally)
                        SubInfoItem("Maks. User", subscription.maxUsers?.toString() ?: "∞", Alignment.End)
                    }
                }
            }
        } else {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Icon(statusIcon, null, tint = Color.White, modifier = Modifier.size(14.dp))
                    Text("Status Langganan", style = MaterialTheme.typography.labelSmall,
                        color = Color.White.copy(alpha = 0.75f))
                    Spacer(Modifier.weight(1f))
                    SubStatusPill(statusLabel)
                }
                Text(
                    subscription?.plan?.uppercase() ?: "TIDAK ADA PAKET",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.ExtraBold, color = Color.White
                )
                if (subscription != null) {
                    HorizontalDivider(color = Color.White.copy(alpha = 0.2f))
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        SubInfoItem("Mulai", subscription.startedAt?.take(10) ?: "-")
                        SubInfoItem("Berakhir", subscription.expiresAt?.take(10) ?: "-", Alignment.CenterHorizontally)
                        SubInfoItem("Maks. User", subscription.maxUsers?.toString() ?: "∞", Alignment.End)
                    }
                }
            }
        }
    }
}

@Composable
private fun SubStatusPill(label: String) {
    Surface(shape = RoundedCornerShape(50), color = Color.White.copy(alpha = 0.2f)) {
        Text(label, style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold,
            color = Color.White, modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp))
    }
}

@Composable
private fun SubInfoItem(label: String, value: String, align: Alignment.Horizontal = Alignment.Start) {
    Column(horizontalAlignment = align, verticalArrangement = Arrangement.spacedBy(2.dp)) {
        Text(label, style = MaterialTheme.typography.labelSmall, color = Color.White.copy(alpha = 0.65f))
        Text(value, style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.SemiBold, color = Color.White)
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// Plan card
// ─────────────────────────────────────────────────────────────────────────────

@Composable
private fun PlanCard(
    plan: Plan,
    isCurrentPlan: Boolean,
    onSubscribe: () -> Unit,
    modifier: Modifier = Modifier
) {
    val shape = RoundedCornerShape(14.dp)

    if (isCurrentPlan) {
        // ── Active plan: gradient header + white body ─────────────────────────
        Card(
            modifier = modifier,
            shape = shape,
            border = androidx.compose.foundation.BorderStroke(2.dp, Primary),
            elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
        ) {
            Column {
                // Gradient header
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(
                            Brush.linearGradientBrush(listOf(Primary, Color(0xFF1DB88A))),
                            shape = RoundedCornerShape(topStart = 12.dp, topEnd = 12.dp)
                        )
                        .padding(horizontal = 14.dp, vertical = 12.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            plan.name,
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold,
                            color = Color.White,
                            modifier = Modifier.weight(1f),
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                            if (plan.isTrial) {
                                Surface(shape = RoundedCornerShape(50), color = Color.White.copy(alpha = 0.22f)) {
                                    Text("TRIAL", style = MaterialTheme.typography.labelSmall,
                                        fontWeight = FontWeight.Bold, color = Color.White,
                                        modifier = Modifier.padding(horizontal = 7.dp, vertical = 2.dp))
                                }
                            }
                            Surface(shape = RoundedCornerShape(50), color = Color.White.copy(alpha = 0.22f)) {
                                Text("AKTIF", style = MaterialTheme.typography.labelSmall,
                                    fontWeight = FontWeight.Bold, color = Color.White,
                                    modifier = Modifier.padding(horizontal = 7.dp, vertical = 2.dp))
                            }
                        }
                    }
                }

                // White body
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 14.dp, vertical = 12.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    if (plan.description != null) {
                        Text(
                            plan.description,
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            maxLines = 2,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                    HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.6f))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                            Text(
                                formatPlanPrice(plan.totalPrice),
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.ExtraBold,
                                color = Primary
                            )
                            Text(
                                "${plan.durationDays} hari" + if (plan.maxUsers != null) " · ${plan.maxUsers} user" else "",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Icon(Icons.Default.CheckCircle, null,
                                tint = Primary, modifier = Modifier.size(18.dp))
                            Text("Aktif",
                                style = MaterialTheme.typography.labelLarge,
                                fontWeight = FontWeight.SemiBold,
                                color = Primary)
                        }
                    }
                }
            }
        }
    } else {
        // ── Inactive plan: clean surface card ─────────────────────────────────
        Card(
            modifier = modifier,
            shape = shape,
            border = androidx.compose.foundation.BorderStroke(
                1.dp, MaterialTheme.colorScheme.outlineVariant
            ),
            elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 14.dp, vertical = 12.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        plan.name,
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.weight(1f),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    if (plan.isTrial) SmallBadge("TRIAL", Secondary)
                }
                if (plan.description != null) {
                    Text(
                        plan.description,
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis
                    )
                }
                HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.6f))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                        Text(
                            formatPlanPrice(plan.totalPrice),
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.ExtraBold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            "${plan.durationDays} hari" + if (plan.maxUsers != null) " · ${plan.maxUsers} user" else "",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    Button(
                        onClick = onSubscribe,
                        shape = RoundedCornerShape(10.dp),
                        contentPadding = PaddingValues(horizontal = 14.dp, vertical = 8.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = Primary)
                    ) {
                        Text(
                            if (plan.isTrial) "Coba" else "Langganan",
                            style = MaterialTheme.typography.labelMedium
                        )
                    }
                }
            }
        }
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// Invoice card
// ─────────────────────────────────────────────────────────────────────────────

@Composable
private fun InvoiceCard(
    invoice: Invoice,
    onCancel: () -> Unit
) {
    val (statusColor, statusLabel, statusIcon) = when (invoice.status) {
        "paid"      -> Triple(Success,            "Lunas",       Icons.Default.CheckCircle)
        "pending"   -> Triple(Warning,            "Menunggu",    Icons.Default.HourglassTop)
        "cancelled" -> Triple(Color(0xFF9E9E9E),  "Dibatalkan",  Icons.Default.Cancel)
        "expired"   -> Triple(Error,              "Kedaluwarsa", Icons.Default.ErrorOutline)
        else        -> Triple(Color(0xFF9E9E9E),  invoice.status, Icons.Default.Info)
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 14.dp, vertical = 12.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = statusColor.copy(alpha = 0.12f),
                    modifier = Modifier.size(36.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(statusIcon, null, tint = statusColor, modifier = Modifier.size(18.dp))
                    }
                }
                Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(1.dp)) {
                    Text(invoice.invoiceNo, style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Bold, maxLines = 1, overflow = TextOverflow.Ellipsis)
                    Text(invoice.planName, style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
                SmallBadge(statusLabel, statusColor)
            }

            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Bottom
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(3.dp)) {
                    Surface(shape = RoundedCornerShape(6.dp),
                        color = MaterialTheme.colorScheme.surfaceVariant) {
                        Text("${invoice.durationDays} hari", style = MaterialTheme.typography.labelSmall,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp),
                            color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                    if (invoice.issuedAt != null) {
                        Text("Terbit: ${invoice.issuedAt.take(10)}",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                    if (invoice.dueAt != null) {
                        Text("Jatuh tempo: ${invoice.dueAt.take(10)}",
                            style = MaterialTheme.typography.labelSmall,
                            color = if (invoice.status == "pending") Warning
                                    else MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }
                Column(horizontalAlignment = Alignment.End, verticalArrangement = Arrangement.spacedBy(2.dp)) {
                    if (invoice.taxAmount > 0) {
                        Text(formatPlanPrice(invoice.baseAmount),
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant)
                        Text("+${(invoice.taxRate * 100).toInt()}% pajak",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                    Text(formatPlanPrice(invoice.totalAmount),
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.ExtraBold, color = Primary)
                }
            }

            if (invoice.status == "pending" && invoice.qrString != null) {
                Surface(shape = RoundedCornerShape(8.dp), color = MaterialTheme.colorScheme.surfaceVariant) {
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(horizontal = 10.dp, vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Icon(Icons.Default.QrCode, null, modifier = Modifier.size(14.dp),
                            tint = MaterialTheme.colorScheme.onSurfaceVariant)
                        Text("QRIS tersedia — selesaikan pembayaran via e-wallet",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }
            }

            if (invoice.status == "pending") {
                OutlinedButton(
                    onClick = onCancel,
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(10.dp),
                    contentPadding = PaddingValues(vertical = 8.dp),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = Error),
                    border = androidx.compose.foundation.BorderStroke(1.dp, Error.copy(alpha = 0.4f))
                ) {
                    Icon(Icons.Default.Cancel, null, modifier = Modifier.size(14.dp))
                    Spacer(Modifier.width(6.dp))
                    Text("Batalkan Invoice", style = MaterialTheme.typography.labelMedium)
                }
            }
        }
    }
}

@Composable
private fun SmallBadge(label: String, color: Color) {
    Surface(shape = RoundedCornerShape(50), color = color.copy(alpha = 0.12f)) {
        Text(label, style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold,
            color = color, modifier = Modifier.padding(horizontal = 7.dp, vertical = 2.dp))
    }
}

@Composable
private fun SectionLabel(icon: ImageVector, title: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 4.dp, bottom = 2.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        Icon(icon, null, modifier = Modifier.size(14.dp), tint = Primary)
        Text(title, style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.SemiBold,
            color = MaterialTheme.colorScheme.onSurfaceVariant)
        HorizontalDivider(modifier = Modifier.weight(1f),
            color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// Dialogs
// ─────────────────────────────────────────────────────────────────────────────

@Composable
private fun SubscribeConfirmDialog(
    plan: Plan,
    isSubmitting: Boolean,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = { if (!isSubmitting) onDismiss() },
        icon = { Icon(Icons.Default.Stars, contentDescription = null, tint = Primary) },
        title = { Text("Berlangganan Paket", fontWeight = FontWeight.Bold) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text("Anda akan membuat invoice untuk:")
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = MaterialTheme.colorScheme.surfaceVariant
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Text(plan.name, fontWeight = FontWeight.SemiBold)
                        Text(formatPlanPrice(plan.totalPrice), style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = Primary)
                        Text("${plan.durationDays} hari", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }
                Text(
                    "Invoice akan dikirim. Selesaikan pembayaran sebelum jatuh tempo.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        },
        confirmButton = {
            Button(
                onClick = onConfirm,
                enabled = !isSubmitting,
                colors = ButtonDefaults.buttonColors(containerColor = Primary)
            ) {
                if (isSubmitting) {
                    CircularProgressIndicator(modifier = Modifier.size(16.dp), strokeWidth = 2.dp, color = Color.White)
                } else {
                    Text("Buat Invoice")
                }
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss, enabled = !isSubmitting) {
                Text("Batal")
            }
        }
    )
}

@Composable
private fun CancelInvoiceDialog(
    invoice: Invoice,
    isSubmitting: Boolean,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = { if (!isSubmitting) onDismiss() },
        icon = { Icon(Icons.Default.Cancel, contentDescription = null, tint = Error) },
        title = { Text("Batalkan Invoice", fontWeight = FontWeight.Bold) },
        text = {
            Text("Apakah Anda yakin ingin membatalkan invoice ${invoice.invoiceNo}? Tindakan ini tidak dapat dibatalkan.")
        },
        confirmButton = {
            Button(
                onClick = onConfirm,
                enabled = !isSubmitting,
                colors = ButtonDefaults.buttonColors(containerColor = Error)
            ) {
                if (isSubmitting) {
                    CircularProgressIndicator(modifier = Modifier.size(16.dp), strokeWidth = 2.dp, color = Color.White)
                } else {
                    Text("Ya, Batalkan")
                }
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss, enabled = !isSubmitting) {
                Text("Tidak")
            }
        }
    )
}

// ─────────────────────────────────────────────────────────────────────────────
// Helpers
// ─────────────────────────────────────────────────────────────────────────────

private fun formatPlanPrice(amount: Double): String {
    val long = amount.toLong()
    val formatted = buildString {
        val s = long.toString()
        s.reversed().chunked(3).joinTo(this, ".") { it }
        // the string is built reversed, so reverse again
    }
    // simpler approach
    val s = long.toString().reversed()
    val parts = s.chunked(3).joinToString(".")
    return "Rp ${parts.reversed()}"
}

private fun Brush.Companion.linearGradientBrush(colors: List<Color>) =
    linearGradient(colors)

// Helper data class to avoid destructuring ambiguity
private data class Quadruple<A, B, C, D>(val first: A, val second: B, val third: C, val fourth: D)

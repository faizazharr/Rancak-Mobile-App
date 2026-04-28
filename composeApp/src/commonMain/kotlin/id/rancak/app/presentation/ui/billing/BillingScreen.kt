package id.rancak.app.presentation.ui.billing

import androidx.compose.animation.animateColorAsState
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
import androidx.compose.ui.text.font.FontWeight
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
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(horizontal = 20.dp, vertical = 24.dp),
        verticalArrangement = Arrangement.spacedBy(24.dp)
    ) {
        // ── Subscription card ─────────────────────────────────────────────
        item {
            SubscriptionCard(subscription = subscription)
        }

        // ── Available plans ───────────────────────────────────────────────
        if (plans.isNotEmpty()) {
            item {
                SectionHeader(
                    icon = Icons.Default.Stars,
                    title = "Paket Langganan",
                    subtitle = "Pilih paket yang sesuai kebutuhan bisnis Anda"
                )
            }
            items(plans) { plan ->
                PlanCard(
                    plan = plan,
                    isCurrentPlan = subscription?.plan == plan.code,
                    onSubscribe = { onSubscribe(plan) }
                )
            }
        }

        // ── Invoice history ───────────────────────────────────────────────
        if (invoices.isNotEmpty()) {
            item {
                SectionHeader(
                    icon = Icons.Default.Receipt,
                    title = "Riwayat Invoice",
                    subtitle = "${invoices.size} invoice tercatat"
                )
            }
            items(invoices, key = { it.uuid }) { invoice ->
                InvoiceCard(
                    invoice = invoice,
                    onCancel = { onCancelInvoice(invoice) }
                )
            }
        }

        // ── Empty invoices placeholder ────────────────────────────────────
        if (invoices.isEmpty() && plans.isEmpty() && subscription == null) {
            item {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 48.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Icon(
                        Icons.Default.CreditCard,
                        contentDescription = null,
                        modifier = Modifier.size(56.dp),
                        tint = MaterialTheme.colorScheme.outlineVariant
                    )
                    Text(
                        "Belum ada data billing",
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    OutlinedButton(onClick = onRefresh) {
                        Text("Muat Ulang")
                    }
                }
            }
        }

        item { Spacer(Modifier.height(16.dp)) }
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// Subscription hero card
// ─────────────────────────────────────────────────────────────────────────────

@Composable
private fun SubscriptionCard(subscription: SubscriptionState?) {
    val (gradientStart, gradientEnd, statusLabel, statusIcon) = when (subscription?.status) {
        "active" -> Quadruple(Primary, Color(0xFF1DB88A), "Aktif", Icons.Default.CheckCircle)
        "trial"  -> Quadruple(Tertiary, Color(0xFF5588EE), "Trial", Icons.Default.HourglassTop)
        "expired" -> Quadruple(Color(0xFF9E9E9E), Color(0xFF757575), "Kedaluwarsa", Icons.Default.ErrorOutline)
        else     -> Quadruple(Color(0xFF9E9E9E), Color(0xFF757575), "Tidak Aktif", Icons.Default.Block)
    }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(20.dp))
            .background(Brush.linearGradientBrush(listOf(gradientStart, gradientEnd)))
            .padding(24.dp)
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Icon(
                    statusIcon,
                    contentDescription = null,
                    tint = Color.White,
                    modifier = Modifier.size(20.dp)
                )
                Text(
                    "Status Langganan",
                    style = MaterialTheme.typography.labelMedium,
                    color = Color.White.copy(alpha = 0.8f)
                )
                Spacer(Modifier.weight(1f))
                Surface(
                    shape = RoundedCornerShape(50),
                    color = Color.White.copy(alpha = 0.2f)
                ) {
                    Text(
                        statusLabel,
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = Color.White,
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                    )
                }
            }

            Text(
                text = subscription?.plan?.uppercase() ?: "TIDAK ADA PAKET",
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.ExtraBold,
                color = Color.White
            )

            if (subscription != null) {
                HorizontalDivider(color = Color.White.copy(alpha = 0.2f))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    SubscriptionInfoItem(
                        label = "Mulai",
                        value = subscription.startedAt?.take(10) ?: "-"
                    )
                    SubscriptionInfoItem(
                        label = "Berakhir",
                        value = subscription.expiresAt?.take(10) ?: "-",
                        align = Alignment.CenterHorizontally
                    )
                    SubscriptionInfoItem(
                        label = "Maks. User",
                        value = subscription.maxUsers?.toString() ?: "∞",
                        align = Alignment.End
                    )
                }
            }
        }
    }
}

@Composable
private fun SubscriptionInfoItem(
    label: String,
    value: String,
    align: Alignment.Horizontal = Alignment.Start
) {
    Column(horizontalAlignment = align) {
        Text(label, style = MaterialTheme.typography.labelSmall, color = Color.White.copy(alpha = 0.7f))
        Text(value, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.SemiBold, color = Color.White)
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// Plan card
// ─────────────────────────────────────────────────────────────────────────────

@Composable
private fun PlanCard(
    plan: Plan,
    isCurrentPlan: Boolean,
    onSubscribe: () -> Unit
) {
    val borderColor by animateColorAsState(
        if (isCurrentPlan) Primary else MaterialTheme.colorScheme.outlineVariant
    )

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        border = androidx.compose.foundation.BorderStroke(
            width = if (isCurrentPlan) 2.dp else 1.dp,
            color = borderColor
        ),
        colors = CardDefaults.cardColors(
            containerColor = if (isCurrentPlan)
                MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f)
            else
                MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = if (isCurrentPlan) 4.dp else 1.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 18.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // ── Plan header ──
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(2.dp)) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text(
                            plan.name,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        if (plan.isTrial) {
                            Surface(
                                shape = RoundedCornerShape(50),
                                color = Secondary.copy(alpha = 0.15f)
                            ) {
                                Text(
                                    "TRIAL",
                                    style = MaterialTheme.typography.labelSmall,
                                    fontWeight = FontWeight.Bold,
                                    color = Secondary,
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
                                )
                            }
                        }
                        if (isCurrentPlan) {
                            Surface(
                                shape = RoundedCornerShape(50),
                                color = Primary.copy(alpha = 0.15f)
                            ) {
                                Text(
                                    "PAKET ANDA",
                                    style = MaterialTheme.typography.labelSmall,
                                    fontWeight = FontWeight.Bold,
                                    color = Primary,
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
                                )
                            }
                        }
                    }
                    if (plan.description != null) {
                        Text(
                            plan.description,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }

            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)

            // ── Pricing & details ──
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Bottom
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Text(
                        formatPlanPrice(plan.totalPrice),
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.ExtraBold,
                        color = if (isCurrentPlan) Primary else MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        "${plan.durationDays} hari" + if (plan.maxUsers != null) " · Maks. ${plan.maxUsers} user" else "",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    if (plan.taxRate > 0) {
                        Text(
                            "Sudah termasuk pajak ${(plan.taxRate * 100).toInt()}%",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                        )
                    }
                }

                Button(
                    onClick = onSubscribe,
                    enabled = !isCurrentPlan,
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Primary,
                        disabledContainerColor = MaterialTheme.colorScheme.surfaceVariant
                    )
                ) {
                    if (isCurrentPlan) {
                        Icon(Icons.Default.Check, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(Modifier.width(4.dp))
                        Text("Aktif")
                    } else {
                        Text(if (plan.isTrial) "Coba Gratis" else "Berlangganan")
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
        "paid"      -> Triple(Success,          "Lunas",       Icons.Default.CheckCircle)
        "pending"   -> Triple(Warning,          "Menunggu",    Icons.Default.HourglassTop)
        "cancelled" -> Triple(Color(0xFF9E9E9E), "Dibatalkan",  Icons.Default.Cancel)
        "expired"   -> Triple(Error,            "Kedaluwarsa", Icons.Default.ErrorOutline)
        else        -> Triple(Color(0xFF9E9E9E), invoice.status, Icons.Default.Info)
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 18.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // ── Invoice header ──
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Surface(
                    shape = RoundedCornerShape(10.dp),
                    color = statusColor.copy(alpha = 0.12f),
                    modifier = Modifier.size(40.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            statusIcon,
                            contentDescription = null,
                            tint = statusColor,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        invoice.invoiceNo,
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        invoice.planName,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                Surface(
                    shape = RoundedCornerShape(50),
                    color = statusColor.copy(alpha = 0.12f)
                ) {
                    Text(
                        statusLabel,
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.SemiBold,
                        color = statusColor,
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                    )
                }
            }

            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)

            // ── Invoice detail rows ──
            InvoiceDetailRow("Durasi", "${invoice.durationDays} hari")
            InvoiceDetailRow("Subtotal", formatPlanPrice(invoice.baseAmount))
            if (invoice.taxAmount > 0) {
                InvoiceDetailRow("Pajak (${(invoice.taxRate * 100).toInt()}%)", formatPlanPrice(invoice.taxAmount))
            }
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    "Total",
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.SemiBold
                )
                Text(
                    formatPlanPrice(invoice.totalAmount),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.ExtraBold,
                    color = Primary
                )
            }

            // ── Dates ──
            if (invoice.issuedAt != null || invoice.dueAt != null) {
                HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    if (invoice.issuedAt != null) {
                        Text(
                            "Diterbitkan: ${invoice.issuedAt.take(10)}",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    if (invoice.dueAt != null) {
                        Text(
                            "Jatuh tempo: ${invoice.dueAt.take(10)}",
                            style = MaterialTheme.typography.labelSmall,
                            color = if (invoice.status == "pending") Warning else MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }

            // ── QR info + cancel button (pending only) ──
            if (invoice.status == "pending") {
                if (invoice.qrString != null) {
                    Surface(
                        shape = RoundedCornerShape(10.dp),
                        color = MaterialTheme.colorScheme.surfaceVariant
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Icon(
                                Icons.Default.QrCode,
                                contentDescription = null,
                                modifier = Modifier.size(16.dp),
                                tint = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Text(
                                "QRIS tersedia — selesaikan pembayaran via aplikasi e-wallet",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
                OutlinedButton(
                    onClick = onCancel,
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(10.dp),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = Error),
                    border = androidx.compose.foundation.BorderStroke(1.dp, Error.copy(alpha = 0.5f))
                ) {
                    Icon(
                        Icons.Default.Cancel,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(Modifier.width(6.dp))
                    Text("Batalkan Invoice")
                }
            }
        }
    }
}

@Composable
private fun InvoiceDetailRow(label: String, value: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            label,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Text(
            value,
            style = MaterialTheme.typography.bodySmall,
            fontWeight = FontWeight.Medium
        )
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// Section header
// ─────────────────────────────────────────────────────────────────────────────

@Composable
private fun SectionHeader(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    title: String,
    subtitle: String
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Surface(
            shape = RoundedCornerShape(10.dp),
            color = MaterialTheme.colorScheme.primaryContainer
        ) {
            Icon(
                icon,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onPrimaryContainer,
                modifier = Modifier
                    .padding(8.dp)
                    .size(20.dp)
            )
        }
        Column {
            Text(
                title,
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold
            )
            Text(
                subtitle,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
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

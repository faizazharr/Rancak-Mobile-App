package id.rancak.app.presentation.ui.billing.components

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import id.rancak.app.domain.model.Invoice
import id.rancak.app.domain.model.Plan
import id.rancak.app.domain.model.SubscriptionState
import id.rancak.app.presentation.designsystem.RancakTheme
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.persistentListOf
import kotlinx.collections.immutable.toImmutableList

@Composable
fun BillingContent(
    subscription: SubscriptionState?,
    plans: ImmutableList<Plan>,
    invoices: ImmutableList<Invoice>,
    onSubscribe: (Plan) -> Unit,
    onCancelInvoice: (Invoice) -> Unit,
    onRefresh: () -> Unit,
    modifier: Modifier = Modifier
) {
    BoxWithConstraints(modifier = modifier.fillMaxSize()) {
        val isTablet = maxWidth >= 600.dp
        val leftPaneWidth = maxOf(360.dp, minOf(460.dp, maxWidth * 0.44f))

        if (isTablet) {
            Row(modifier = Modifier.fillMaxSize()) {
                Column(
                    modifier = Modifier
                        .width(leftPaneWidth)
                        .fillMaxHeight()
                        .verticalScroll(rememberScrollState())
                        .padding(start = 16.dp, end = 12.dp, top = 20.dp, bottom = 24.dp),
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    SubscriptionCard(subscription = subscription, isTablet = true)
                    if (plans.isNotEmpty()) {
                        SectionLabel(Icons.Default.Stars, "Paket Langganan")
                        plans.forEach { plan ->
                            PlanCard(
                                plan = plan,
                                isCurrentPlan = subscription?.plan == plan.code,
                                onSubscribe = { onSubscribe(plan) },
                                modifier = Modifier.fillMaxWidth()
                            )
                        }
                    }
                    Spacer(Modifier.height(8.dp))
                }

                VerticalDivider(
                    modifier = Modifier.fillMaxHeight(),
                    color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)
                )

                Column(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxHeight()
                        .verticalScroll(rememberScrollState())
                        .padding(start = 16.dp, end = 24.dp, top = 20.dp, bottom = 24.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    SectionLabel(Icons.Default.Receipt, "Riwayat Invoice")
                    if (invoices.isEmpty()) {
                        Box(modifier = Modifier.fillMaxWidth().padding(vertical = 48.dp),
                            contentAlignment = Alignment.Center) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.spacedBy(12.dp)) {
                                Icon(Icons.Default.Receipt, null, modifier = Modifier.size(48.dp),
                                    tint = MaterialTheme.colorScheme.outlineVariant)
                                Text("Belum ada invoice",
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    style = MaterialTheme.typography.bodyLarge)
                            }
                        }
                    } else {
                        invoices.forEach { invoice ->
                            InvoiceCard(invoice = invoice, onCancel = { onCancelInvoice(invoice) })
                        }
                    }
                    Spacer(Modifier.height(8.dp))
                }
            }
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 14.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                item { SubscriptionCard(subscription = subscription, isTablet = false) }

                if (plans.isNotEmpty()) {
                    item { SectionLabel(Icons.Default.Stars, "Paket Langganan") }
                    items(plans, key = { it.uuid }) { plan ->
                        PlanCard(
                            plan = plan,
                            isCurrentPlan = subscription?.plan == plan.code,
                            onSubscribe = { onSubscribe(plan) },
                            modifier = Modifier.fillMaxWidth()
                        )
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
                            Icon(Icons.Default.CreditCard, null, modifier = Modifier.size(48.dp),
                                tint = MaterialTheme.colorScheme.outlineVariant)
                            Text("Belum ada data billing", color = MaterialTheme.colorScheme.onSurfaceVariant)
                            OutlinedButton(onClick = onRefresh) { Text("Muat Ulang") }
                        }
                    }
                }
                item { Spacer(Modifier.height(12.dp)) }
            }
        }
    }
}

// ── Preview ───────────────────────────────────────────────────────────────────

@Preview
@Composable
private fun BillingContentPreview() {
    RancakTheme {
        BillingContent(
            subscription = SubscriptionState("active", "pro", "2024-01-01", "2025-01-01", 5, false),
            plans = listOf(Plan("1", "pro", "Pro Plan", "Paket terbaik", 100000.0, 0.11, 30, 5, false, 111000.0)).toImmutableList(),
            invoices = persistentListOf(),
            onSubscribe = {},
            onCancelInvoice = {},
            onRefresh = {}
        )
    }
}

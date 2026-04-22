package id.rancak.app.presentation.ui.sales.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountBalance
import androidx.compose.material.icons.filled.BarChart
import androidx.compose.material.icons.filled.Cancel
import androidx.compose.material.icons.filled.CreditCard
import androidx.compose.material.icons.filled.MoreHoriz
import androidx.compose.material.icons.filled.Payments
import androidx.compose.material.icons.filled.QrCode2
import androidx.compose.material.icons.filled.Receipt
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.SubdirectoryArrowLeft
import androidx.compose.material.icons.filled.TrendingUp
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import id.rancak.app.domain.model.OrderType
import id.rancak.app.domain.model.PaymentMethod
import id.rancak.app.domain.model.Sale
import id.rancak.app.domain.model.SaleItem
import id.rancak.app.domain.model.SaleStatus
import id.rancak.app.presentation.designsystem.RancakColors
import id.rancak.app.presentation.designsystem.RancakTheme
import id.rancak.app.presentation.util.formatRupiah

/**
 * Right-hand summary panel (tablet layout) shown when no transaction is
 * selected. Aggregates totals, average, held count, payment-method breakdown
 * and void info from the currently filtered [sales] list.
 */
@Composable
internal fun SalesSummaryPanel(sales: List<Sale>, modifier: Modifier = Modifier) {
    val semantic = RancakColors.semantic

    val paidSales    = sales.filter { it.status == SaleStatus.PAID }
    val heldCount    = sales.count  { it.status == SaleStatus.HELD }
    val voidCount    = sales.count  { it.status == SaleStatus.VOID || it.status == SaleStatus.CANCELLED }
    val totalRevenue = paidSales.sumOf { it.total }
    val avgRevenue   = if (paidSales.isNotEmpty()) totalRevenue / paidSales.size else 0L

    val byMethod = paidSales
        .groupBy { it.paymentMethod?.value?.uppercase() ?: "LAINNYA" }
        .mapValues { (_, list) -> list.sumOf { it.total } }
        .entries.sortedByDescending { it.value }

    Column(
        modifier = modifier
            .verticalScroll(rememberScrollState())
            .padding(20.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Row(
            verticalAlignment     = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Icon(
                Icons.Default.BarChart, contentDescription = null,
                tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(22.dp)
            )
            Text(
                "Ringkasan Transaksi",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
        }

        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            SummaryStatCard(
                modifier  = Modifier.weight(1f),
                icon      = Icons.Default.Receipt,
                iconColor = MaterialTheme.colorScheme.primary,
                label     = "Total Transaksi",
                value     = "${sales.size}",
                bgColor   = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f)
            )
            SummaryStatCard(
                modifier  = Modifier.weight(1f),
                icon      = Icons.Default.Payments,
                iconColor = semantic.success,
                label     = "Total Pendapatan",
                value     = formatRupiah(totalRevenue),
                bgColor   = semantic.success.copy(alpha = 0.08f)
            )
        }
        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            SummaryStatCard(
                modifier  = Modifier.weight(1f),
                icon      = Icons.Default.TrendingUp,
                iconColor = semantic.info,
                label     = "Rata-rata / Transaksi",
                value     = formatRupiah(avgRevenue),
                bgColor   = semantic.info.copy(alpha = 0.08f)
            )
            SummaryStatCard(
                modifier  = Modifier.weight(1f),
                icon      = Icons.Default.Schedule,
                iconColor = semantic.warning,
                label     = "Belum Bayar",
                value     = "$heldCount transaksi",
                bgColor   = semantic.warning.copy(alpha = 0.08f)
            )
        }

        if (byMethod.isNotEmpty()) {
            PaymentMethodBreakdown(byMethod = byMethod)
        }

        if (voidCount > 0) {
            VoidInfoBanner(voidCount = voidCount)
        }

        SelectHint()
    }
}

@Composable
private fun PaymentMethodBreakdown(byMethod: List<Map.Entry<String, Long>>) {
    Card(
        shape     = MaterialTheme.shapes.medium,
        colors    = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Column(
            modifier = Modifier.padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Text(
                "Metode Pembayaran",
                style         = MaterialTheme.typography.labelMedium,
                fontWeight    = FontWeight.Bold,
                letterSpacing = 0.5.sp,
                color         = MaterialTheme.colorScheme.onSurfaceVariant
            )

            byMethod.forEach { (method, amount) ->
                val pmIcon = when (method) {
                    "CASH"     -> Icons.Default.Payments
                    "QRIS"     -> Icons.Default.QrCode2
                    "CARD"     -> Icons.Default.CreditCard
                    "TRANSFER" -> Icons.Default.AccountBalance
                    else       -> Icons.Default.MoreHoriz
                }
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment     = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment     = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(
                            pmIcon, contentDescription = null,
                            modifier = Modifier.size(16.dp),
                            tint     = MaterialTheme.colorScheme.primary
                        )
                        Text(
                            method.lowercase().replaceFirstChar { it.uppercase() },
                            style = MaterialTheme.typography.bodySmall
                        )
                    }
                    Text(
                        formatRupiah(amount),
                        style      = MaterialTheme.typography.bodySmall,
                        fontWeight = FontWeight.SemiBold,
                        fontFamily = FontFamily.Monospace
                    )
                }
            }
        }
    }
}

@Composable
private fun VoidInfoBanner(voidCount: Int) {
    Surface(
        shape  = MaterialTheme.shapes.medium,
        color  = MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.25f),
        border = BorderStroke(0.8.dp, MaterialTheme.colorScheme.error.copy(alpha = 0.3f))
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Icon(
                Icons.Default.Cancel, contentDescription = null,
                tint = MaterialTheme.colorScheme.error, modifier = Modifier.size(18.dp)
            )
            Column {
                Text(
                    "$voidCount transaksi di-void/batal",
                    style      = MaterialTheme.typography.bodySmall,
                    fontWeight = FontWeight.SemiBold,
                    color      = MaterialTheme.colorScheme.error
                )
                Text(
                    "Tidak dihitung dalam total pendapatan",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@Composable
private fun SelectHint() {
    Box(
        modifier = Modifier.fillMaxWidth().padding(top = 8.dp),
        contentAlignment = Alignment.Center
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Icon(
                Icons.Default.SubdirectoryArrowLeft, contentDescription = null,
                modifier = Modifier.size(14.dp),
                tint     = MaterialTheme.colorScheme.outlineVariant
            )
            Text(
                "Pilih transaksi di kiri untuk melihat detail",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.outlineVariant
            )
        }
    }
}

/** Single statistic tile inside the summary panel. */
@Composable
private fun SummaryStatCard(
    modifier:  Modifier,
    icon:      ImageVector,
    iconColor: Color,
    label:     String,
    value:     String,
    bgColor:   Color
) {
    Surface(shape = MaterialTheme.shapes.medium, color = bgColor, modifier = modifier) {
        Column(
            modifier = Modifier.padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Icon(
                icon, contentDescription = null,
                tint = iconColor, modifier = Modifier.size(20.dp)
            )
            Text(
                value,
                style      = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.ExtraBold,
                fontFamily = FontFamily.Monospace,
                maxLines   = 1
            )
            Text(
                label,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Preview
@Composable
private fun SalesSummaryPanelPreview_WithSales() {
    RancakTheme {
        SalesSummaryPanel(
            sales    = previewSales(),
            modifier = Modifier.fillMaxWidth()
        )
    }
}

@Preview
@Composable
private fun SalesSummaryPanelPreview_Empty() {
    RancakTheme {
        SalesSummaryPanel(
            sales    = emptyList(),
            modifier = Modifier.fillMaxWidth()
        )
    }
}

private fun previewSales(): List<Sale> {
    fun s(status: SaleStatus, total: Long, pm: PaymentMethod?) = Sale(
        uuid = "s-$total-$status", invoiceNo = "INV-$total",
        orderType = OrderType.DINE_IN, queueNumber = null, status = status,
        customerName = null, subtotal = total, discount = 0L, surcharge = 0L,
        tax = 0L, total = total, paymentMethod = pm,
        paidAmount = total, changeAmount = 0L,
        items = listOf(
            SaleItem(
                uuid = "i", productUuid = "p", productName = "Menu",
                qty = "1", price = total, subtotal = total,
                variantName = null, note = null
            )
        ),
        createdAt = null
    )
    return listOf(
        s(SaleStatus.PAID, 30_000L, PaymentMethod.CASH),
        s(SaleStatus.PAID, 55_000L, PaymentMethod.QRIS),
        s(SaleStatus.PAID, 22_000L, PaymentMethod.CASH),
        s(SaleStatus.HELD, 18_000L, null),
        s(SaleStatus.VOID, 15_000L, null)
    )
}

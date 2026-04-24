package id.rancak.app.presentation.ui.sales.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountBalance
import androidx.compose.material.icons.filled.CreditCard
import androidx.compose.material.icons.filled.MoreHoriz
import androidx.compose.material.icons.filled.Payments
import androidx.compose.material.icons.filled.Print
import androidx.compose.material.icons.filled.QrCode2
import androidx.compose.material.icons.filled.Store
import androidx.compose.material.icons.filled.TableBar
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import id.rancak.app.data.local.SettingsStore
import id.rancak.app.data.printing.PrinterManager
import id.rancak.app.domain.model.OrderType
import id.rancak.app.domain.model.PaymentMethod
import id.rancak.app.domain.model.Sale
import id.rancak.app.domain.model.SaleItem
import id.rancak.app.domain.model.SaleStatus
import id.rancak.app.presentation.components.PrintDialog
import id.rancak.app.presentation.designsystem.RancakColors
import id.rancak.app.presentation.designsystem.RancakTheme
import id.rancak.app.presentation.util.formatRupiah
import org.koin.compose.koinInject

/**
 * Receipt-styled detail pane for a single [Sale]. Renders outlet header,
 * items, totals, and a "Print Ulang" action that opens a [PrintDialog].
 */
@Composable
internal fun SaleDetailPanel(
    sale: Sale,
    onPayHeldOrder: (String) -> Unit = {},
    onSplitBill: (String) -> Unit = {},
    onAddItems: (String) -> Unit = {},
    modifier: Modifier = Modifier
) {
    val printerManager: PrinterManager = koinInject()
    val settingsStore: SettingsStore   = koinInject()
    var showPrintDialog by remember { mutableStateOf(false) }

    val outletName    = settingsStore.receiptStoreName.ifBlank { "Rancak POS" }
    val outletAddress = settingsStore.receiptStoreAddress.ifBlank { null }
    val outletPhone   = settingsStore.receiptStorePhone.ifBlank { null }

    SaleDetailBody(
        sale          = sale,
        outletName    = outletName,
        outletAddress = outletAddress,
        outletPhone   = outletPhone,
        onRequestPrint = { showPrintDialog = true },
        onPayHeldOrder = onPayHeldOrder,
        onSplitBill    = onSplitBill,
        onAddItems     = onAddItems,
        modifier      = modifier
    )

    if (showPrintDialog) {
        PrintDialog(
            sale           = sale,
            printerManager = printerManager,
            settingsStore  = settingsStore,
            onDismiss      = { showPrintDialog = false }
        )
    }
}

/** Pure-UI body extracted so previews can render without Koin. */
@Composable
private fun SaleDetailBody(
    sale: Sale,
    outletName: String,
    outletAddress: String?,
    outletPhone: String?,
    onRequestPrint: () -> Unit,
    onPayHeldOrder: (String) -> Unit = {},
    onSplitBill: (String) -> Unit = {},
    onAddItems: (String) -> Unit = {},
    modifier: Modifier = Modifier
) {
    val semantic = RancakColors.semantic
    val statusColor = when (sale.status) {
        SaleStatus.PAID                       -> semantic.success
        SaleStatus.HELD                       -> semantic.warning
        SaleStatus.VOID, SaleStatus.CANCELLED -> MaterialTheme.colorScheme.error
        SaleStatus.REFUNDED                   -> semantic.info
    }
    val statusLabel = when (sale.status) {
        SaleStatus.PAID      -> "✓ LUNAS"
        SaleStatus.HELD      -> "⏳ BELUM BAYAR"
        SaleStatus.VOID      -> "✗ VOID"
        SaleStatus.CANCELLED -> "✗ BATAL"
        SaleStatus.REFUNDED  -> "↩ REFUND"
    }

    Box(
        modifier = modifier
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 16.dp, vertical = 12.dp),
        contentAlignment = Alignment.TopCenter
    ) {
        Card(
            modifier = Modifier
                .widthIn(max = 520.dp)
                .fillMaxWidth(),
            shape     = RoundedCornerShape(12.dp),
            colors    = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            elevation = CardDefaults.cardElevation(defaultElevation = 3.dp)
        ) {
            Column(modifier = Modifier.fillMaxWidth()) {
                ReceiptHeader(
                    outletName    = outletName,
                    outletAddress = outletAddress,
                    outletPhone   = outletPhone
                )
                ReceiptNotch(atTop = true)

                Column(
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    ReceiptInvoiceHeader(sale, statusColor, statusLabel)
                    ReceiptInfoChips(sale)
                    DashedDivider()

                    Text(
                        "PESANAN",
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.ExtraBold,
                        letterSpacing = 2.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    ReceiptItems(sale.items)

                    DashedDivider()
                    ReceiptPricingSummary(sale)
                    ReceiptTotalRow(sale)

                    if (sale.paidAmount > 0 || sale.changeAmount > 0) {
                        DashedDivider()
                        ReceiptPaymentInfo(sale)
                    }

                    DashedDivider()
                    ReceiptFooter()
                }

                ReceiptNotch(atTop = false)

                val canPrint = sale.status != SaleStatus.VOID && sale.status != SaleStatus.CANCELLED
                if (canPrint) {
                    ReprintButton(onClick = onRequestPrint)
                }

                if (sale.status == SaleStatus.HELD) {
                    HeldOrderActions(
                        onPay      = { onPayHeldOrder(sale.uuid) },
                        onSplit    = { onSplitBill(sale.uuid) },
                        onAddItems = { onAddItems(sale.uuid) }
                    )
                }
            }
        }
    }
}

@Composable
private fun HeldOrderActions(
    onPay: () -> Unit,
    onSplit: () -> Unit,
    onAddItems: () -> Unit
) {
    Column(
        modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 8.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Button(
            onClick = onPay,
            modifier = Modifier.fillMaxWidth()
        ) { Text("Bayar Sekarang") }
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            OutlinedButton(
                onClick = onAddItems,
                modifier = Modifier.weight(1f)
            ) { Text("Tambah Item") }
            OutlinedButton(
                onClick = onSplit,
                modifier = Modifier.weight(1f)
            ) { Text("Pisah Tagihan") }
        }
    }
}

// ── Sub-sections ────────────────────────────────────────────────────────────

@Composable
private fun ReceiptHeader(outletName: String, outletAddress: String?, outletPhone: String?) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .background(MaterialTheme.colorScheme.primary),
        contentAlignment = Alignment.Center
    ) {
        Column(
            modifier = Modifier.padding(vertical = 14.dp, horizontal = 16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(3.dp)
        ) {
            Icon(
                Icons.Default.Store, contentDescription = null,
                tint = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.8f),
                modifier = Modifier.size(20.dp)
            )
            Text(
                outletName.uppercase(),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.ExtraBold,
                letterSpacing = 3.sp,
                color = MaterialTheme.colorScheme.onPrimary,
                textAlign = TextAlign.Center
            )
            if (outletAddress != null) {
                Text(
                    outletAddress,
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.75f),
                    letterSpacing = 0.sp,
                    textAlign = TextAlign.Center
                )
            }
            if (outletPhone != null) {
                Text(
                    outletPhone,
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.7f),
                    textAlign = TextAlign.Center
                )
            }
        }
    }
}

@Composable
private fun ReceiptNotch(atTop: Boolean) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        val leftShape = if (atTop)
            RoundedCornerShape(bottomEnd = 100.dp, topEnd = 0.dp)
        else
            RoundedCornerShape(topEnd = 100.dp)
        val rightShape = if (atTop)
            RoundedCornerShape(bottomStart = 100.dp)
        else
            RoundedCornerShape(topStart = 100.dp)
        Box(
            modifier = Modifier
                .size(width = 16.dp, height = 14.dp)
                .clip(leftShape)
                .background(MaterialTheme.colorScheme.background)
        )
        Box(
            modifier = Modifier
                .size(width = 16.dp, height = 14.dp)
                .clip(rightShape)
                .background(MaterialTheme.colorScheme.background)
        )
    }
}

@Composable
private fun ReceiptInvoiceHeader(sale: Sale, statusColor: Color, statusLabel: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment     = Alignment.CenterVertically
    ) {
        Column {
            Text(
                sale.invoiceNo ?: "-",
                style      = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                fontFamily = FontFamily.Monospace
            )
            sale.createdAt?.take(16)?.replace("T", " ")?.let { time ->
                Text(
                    time,
                    style      = MaterialTheme.typography.labelSmall,
                    color      = MaterialTheme.colorScheme.outline,
                    fontFamily = FontFamily.Monospace
                )
            }
        }
        Surface(
            shape  = RoundedCornerShape(6.dp),
            color  = statusColor.copy(alpha = 0.12f),
            border = BorderStroke(1.dp, statusColor.copy(alpha = 0.4f))
        ) {
            Text(
                statusLabel,
                modifier   = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                style      = MaterialTheme.typography.labelSmall,
                fontWeight = FontWeight.Bold,
                color      = statusColor
            )
        }
    }
}

@Composable
private fun ReceiptInfoChips(sale: Sale) {
    Row(
        horizontalArrangement = Arrangement.spacedBy(6.dp),
        verticalAlignment     = Alignment.CenterVertically
    ) {
        InfoChip(
            Icons.Default.TableBar,
            sale.orderType.value.replace("_", " ").replaceFirstChar { it.uppercase() }
        )
        sale.paymentMethod?.let { method ->
            val pmIcon = when (method) {
                PaymentMethod.CASH     -> Icons.Default.Payments
                PaymentMethod.QRIS     -> Icons.Default.QrCode2
                PaymentMethod.CARD     -> Icons.Default.CreditCard
                PaymentMethod.TRANSFER -> Icons.Default.AccountBalance
                else                   -> Icons.Default.MoreHoriz
            }
            InfoChip(pmIcon, method.value.replaceFirstChar { it.uppercase() })
        }
        sale.queueNumber?.let { num ->
            Surface(
                shape = MaterialTheme.shapes.extraSmall,
                color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f)
            ) {
                Text(
                    "#$num",
                    modifier   = Modifier.padding(horizontal = 8.dp, vertical = 3.dp),
                    style      = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.Bold,
                    color      = MaterialTheme.colorScheme.primary
                )
            }
        }
    }
}

@Composable
private fun ReceiptItems(items: List<SaleItem>) {
    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
        items.forEach { item ->
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment     = Alignment.Top
            ) {
                Column(modifier = Modifier.weight(1f).padding(end = 8.dp)) {
                    Text(
                        item.productName,
                        style      = MaterialTheme.typography.bodySmall,
                        fontWeight = FontWeight.SemiBold
                    )
                    val sub = buildList {
                        val variant = item.variantName
                        if (!variant.isNullOrBlank()) add(variant)
                        add("${item.qty}x @ ${formatRupiah(item.price)}")
                    }.joinToString("  •  ")
                    Text(
                        sub,
                        style      = MaterialTheme.typography.labelSmall,
                        color      = MaterialTheme.colorScheme.onSurfaceVariant,
                        fontFamily = FontFamily.Monospace
                    )
                    if (!item.note.isNullOrBlank()) {
                        Text(
                            "📝 ${item.note}",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.outline
                        )
                    }
                }
                Text(
                    formatRupiah(item.subtotal),
                    style      = MaterialTheme.typography.bodySmall,
                    fontWeight = FontWeight.SemiBold,
                    fontFamily = FontFamily.Monospace
                )
            }
        }
    }
}

@Composable
private fun ReceiptPricingSummary(sale: Sale) {
    Column(verticalArrangement = Arrangement.spacedBy(5.dp)) {
        ReceiptRow("Subtotal", formatRupiah(sale.subtotal))
        if (sale.discount > 0) {
            ReceiptRow(
                "Diskon",
                "- ${formatRupiah(sale.discount)}",
                valueColor = MaterialTheme.colorScheme.error
            )
        }
        if (sale.surcharge > 0) ReceiptRow("Biaya Tambahan", formatRupiah(sale.surcharge))
        if (sale.tax > 0)       ReceiptRow("Pajak",          formatRupiah(sale.tax))
    }
}

@Composable
private fun ReceiptTotalRow(sale: Sale) {
    Surface(
        shape  = RoundedCornerShape(8.dp),
        color  = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.35f),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.3f)),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 14.dp, vertical = 10.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment     = Alignment.CenterVertically
        ) {
            Text(
                "TOTAL",
                style         = MaterialTheme.typography.titleSmall,
                fontWeight    = FontWeight.ExtraBold,
                letterSpacing = 1.sp
            )
            Text(
                formatRupiah(sale.total),
                style      = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.ExtraBold,
                color      = MaterialTheme.colorScheme.primary,
                fontFamily = FontFamily.Monospace
            )
        }
    }
}

@Composable
private fun ReceiptPaymentInfo(sale: Sale) {
    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
        if (sale.paidAmount > 0) {
            ReceiptRow("Dibayar", formatRupiah(sale.paidAmount))
        }
        if (sale.changeAmount > 0) {
            ReceiptRow(
                "Kembalian",
                formatRupiah(sale.changeAmount),
                valueColor = MaterialTheme.colorScheme.secondary
            )
        }
    }
}

@Composable
private fun ReceiptFooter() {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 6.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(2.dp)
    ) {
        Text(
            "Terima kasih!",
            style      = MaterialTheme.typography.labelMedium,
            fontWeight = FontWeight.Bold,
            color      = MaterialTheme.colorScheme.primary
        )
        Text(
            "Kami menunggu kunjungan Anda",
            style     = MaterialTheme.typography.labelSmall,
            color     = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center
        )
    }
}

@Composable
private fun ReprintButton(onClick: () -> Unit) {
    Button(
        onClick = onClick,
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 10.dp),
        shape = MaterialTheme.shapes.medium,
        colors = ButtonDefaults.buttonColors(
            containerColor = MaterialTheme.colorScheme.primary
        )
    ) {
        Icon(Icons.Default.Print, contentDescription = null, modifier = Modifier.size(18.dp))
        Spacer(Modifier.width(8.dp))
        Text(
            "Print Ulang Struk",
            style      = MaterialTheme.typography.labelLarge,
            fontWeight = FontWeight.SemiBold
        )
    }
}

// ── Shared pieces ───────────────────────────────────────────────────────────

/** Small label chip used in the receipt detail. */
@Composable
internal fun InfoChip(icon: ImageVector, label: String) {
    Surface(
        shape = MaterialTheme.shapes.extraSmall,
        color = MaterialTheme.colorScheme.surfaceVariant
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 7.dp, vertical = 3.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(3.dp)
        ) {
            Icon(
                icon, contentDescription = null,
                modifier = Modifier.size(11.dp),
                tint = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Text(
                label,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

/** Receipt-style label/value row. */
@Composable
internal fun ReceiptRow(
    label: String,
    value: String,
    valueColor: Color = MaterialTheme.colorScheme.onSurface
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment     = Alignment.CenterVertically
    ) {
        Text(
            label,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Text(
            value,
            style      = MaterialTheme.typography.bodySmall,
            fontWeight = FontWeight.SemiBold,
            color      = valueColor,
            fontFamily = FontFamily.Monospace
        )
    }
}

// ── Previews (use pure body to avoid Koin deps) ─────────────────────────────

@Preview
@Composable
private fun SaleDetailPreview_Paid() {
    RancakTheme {
        SaleDetailBody(
            sale          = previewSale(SaleStatus.PAID),
            outletName    = "Warung Kopi Sinar",
            outletAddress = "Jl. Merdeka 10, Bandung",
            outletPhone   = "0812-3456-7890",
            onRequestPrint = {},
            modifier      = Modifier.fillMaxWidth()
        )
    }
}

@Preview
@Composable
private fun SaleDetailPreview_Held() {
    RancakTheme {
        SaleDetailBody(
            sale          = previewSale(SaleStatus.HELD),
            outletName    = "Warung Kopi Sinar",
            outletAddress = null,
            outletPhone   = null,
            onRequestPrint = {},
            modifier      = Modifier.fillMaxWidth()
        )
    }
}

private fun previewSale(status: SaleStatus) = Sale(
    uuid          = "s1",
    invoiceNo     = "INV-20260422-0001",
    orderType     = OrderType.DINE_IN,
    queueNumber   = 7,
    status        = status,
    customerName  = "Budi",
    subtotal      = 40_000L,
    discount      = 5_000L,
    surcharge     = 0L,
    tax           = 3_000L,
    total         = 38_000L,
    paymentMethod = if (status == SaleStatus.PAID) PaymentMethod.CASH else null,
    paidAmount    = if (status == SaleStatus.PAID) 40_000L else 0L,
    changeAmount  = if (status == SaleStatus.PAID) 2_000L else 0L,
    items = listOf(
        SaleItem(
            uuid = "i1", productUuid = "p1", productName = "Kopi Susu Gula Aren",
            qty = "2", price = 18_000L, subtotal = 36_000L,
            variantName = "Large", note = "less sugar"
        ),
        SaleItem(
            uuid = "i2", productUuid = "p2", productName = "Roti Bakar",
            qty = "1", price = 4_000L, subtotal = 4_000L,
            variantName = null, note = null
        )
    ),
    createdAt = "2026-04-22T10:15:00"
)

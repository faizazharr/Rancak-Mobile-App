package id.rancak.app.presentation.ui.payment.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.CallSplit
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import id.rancak.app.domain.model.PaymentMethod
import id.rancak.app.presentation.designsystem.RancakTheme
import id.rancak.app.presentation.util.formatRupiah
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.persistentListOf

/**
 * Sisi kiri layar pembayaran — pratinjau pesanan bergaya struk (receipt).
 * Menampilkan ringkasan item, breakdown biaya, total, dan kembalian.
 * Tidak memiliki state atau efek — pure UI.
 */
@Composable
internal fun PaymentReceiptPanel(
    itemCount:       Int,
    subtotal:        Long,
    total:           Long,
    discount:        Long,
    tax:             Long,
    adminFee:        Long,
    deliveryFee:     Long,
    tip:             Long,
    changeAmount:    Long,
    isCashSelected:  Boolean,
    paidAmount:      String,
    orderItems:      ImmutableList<OrderLineItem>  = persistentListOf(),
    taxLines:        ImmutableList<NamedAmount>    = persistentListOf(),
    surchargeLines:  ImmutableList<NamedAmount>    = persistentListOf(),
    orderTypeLabel:  String?                       = null,
    customerName:    String?                       = null,
    tableLabel:      String?                       = null,
    pax:             Int                           = 0,
    voucherCode:     String?                       = null,
    selectedMethod:  PaymentMethod?                = null,
    storeName:       String                        = "RANCAK POS",
    isSplit:         Boolean                       = false,
    onToggleMode:    () -> Unit                    = {},
    modifier:        Modifier                      = Modifier
) {
    // Panel mengisi tinggi kolom penuh — hanya bagian items yang scroll,
    // header/total/pricing/toggle selalu terlihat.
    Box(
        modifier         = modifier,
        contentAlignment = Alignment.TopCenter
    ) {
        Card(
            modifier  = Modifier
                .widthIn(max = 480.dp)
                .fillMaxWidth()
                .fillMaxHeight(),
            shape     = RoundedCornerShape(12.dp),
            colors    = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
        ) {
            Column(modifier = Modifier.fillMaxSize()) {
                // ── Konten tetap atas ────────────────────────────────────────
                Column(
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    // Brand header
                    Text(
                        storeName.uppercase(),
                        modifier  = Modifier.fillMaxWidth().padding(top = 6.dp, bottom = 2.dp),
                        style     = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.ExtraBold,
                        letterSpacing = 3.sp,
                        color     = MaterialTheme.colorScheme.primary,
                        textAlign = TextAlign.Center
                    )
                    ReceiptOrderChips(
                        orderTypeLabel = orderTypeLabel,
                        customerName   = customerName,
                        tableLabel     = tableLabel,
                        pax            = pax,
                        selectedMethod = selectedMethod
                    )
                    DashedDivider()
                }

                // ── Bagian items — hanya ini yang bisa scroll ────────────────
                if (orderItems.isNotEmpty()) {
                    Column(
                        modifier = Modifier
                            .weight(1f)
                            .verticalScroll(rememberScrollState())
                            .padding(horizontal = 16.dp)
                    ) {
                        Text(
                            "PESANAN",
                            style         = MaterialTheme.typography.labelSmall,
                            fontWeight    = FontWeight.ExtraBold,
                            letterSpacing = 2.sp,
                            color         = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(Modifier.height(6.dp))
                        ReceiptLineItems(
                            items     = orderItems,
                            itemCount = itemCount,
                            subtotal  = subtotal
                        )
                        Spacer(Modifier.height(8.dp))
                    }
                } else {
                    Spacer(modifier = Modifier.weight(1f))
                }

                // ── Konten tetap bawah (selalu terlihat) ─────────────────────
                Column(
                    modifier = Modifier.padding(horizontal = 16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    DashedDivider()
                    ReceiptPricingSection(
                        subtotal       = subtotal,
                        discount       = discount,
                        tax            = tax,
                        adminFee       = adminFee,
                        deliveryFee    = deliveryFee,
                        tip            = tip,
                        taxLines       = taxLines,
                        surchargeLines = surchargeLines,
                        itemCount      = itemCount,
                        hasItemDetail  = orderItems.isNotEmpty()
                    )
                    ReceiptTotalCard(total = total)

                    val paid = paidAmount.toLongOrNull() ?: 0L
                    if (isCashSelected && paid > 0) {
                        DashedDivider()
                        ReceiptPaymentRows(
                            paidAmount   = paid,
                            changeAmount = changeAmount
                        )
                    }

                    if (!voucherCode.isNullOrBlank()) {
                        DashedDivider()
                        Row(
                            verticalAlignment     = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(5.dp)
                        ) {
                            Icon(Icons.Default.LocalOffer, null, Modifier.size(11.dp),
                                tint = MaterialTheme.colorScheme.primary)
                            Text(
                                "Voucher: $voucherCode",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }
                    }
                    Spacer(Modifier.height(4.dp))
                }

                // ── Mode pembayaran toggle — selalu di bawah ─────────────────
                PaymentModeToggle(
                    isSplit   = isSplit,
                    onToggle  = onToggleMode,
                    modifier  = Modifier.padding(horizontal = 16.dp, vertical = 10.dp)
                )
            }
        }
    }
}

// ── Store Header ──────────────────────────────────────────────────────────────

@Composable
private fun ReceiptStoreHeader(storeName: String) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .background(MaterialTheme.colorScheme.primary),
        contentAlignment = Alignment.Center
    ) {
        Column(
            modifier            = Modifier.padding(vertical = 16.dp, horizontal = 16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(3.dp)
        ) {
            Icon(
                Icons.Default.Store,
                contentDescription = null,
                tint     = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.85f),
                modifier = Modifier.size(22.dp)
            )
            Text(
                storeName.uppercase(),
                style         = MaterialTheme.typography.titleMedium,
                fontWeight    = FontWeight.ExtraBold,
                letterSpacing = 3.sp,
                color         = MaterialTheme.colorScheme.onPrimary,
                textAlign     = TextAlign.Center
            )
        }
    }
}

// ── Notch decoration ─────────────────────────────────────────────────────────

@Composable
private fun ReceiptNotch(atTop: Boolean) {
    Row(
        modifier              = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        val leftShape = if (atTop)
            RoundedCornerShape(bottomEnd = 100.dp)
        else
            RoundedCornerShape(topEnd = 100.dp)
        val rightShape = if (atTop)
            RoundedCornerShape(bottomStart = 100.dp)
        else
            RoundedCornerShape(topStart = 100.dp)
        Box(
            Modifier
                .size(width = 16.dp, height = 14.dp)
                .clip(leftShape)
                .background(MaterialTheme.colorScheme.background)
        )
        Box(
            Modifier
                .size(width = 16.dp, height = 14.dp)
                .clip(rightShape)
                .background(MaterialTheme.colorScheme.background)
        )
    }
}

// ── Dashed divider ────────────────────────────────────────────────────────────

@Composable
private fun DashedDivider(color: Color = MaterialTheme.colorScheme.outlineVariant) {
    val c = color
    Canvas(modifier = Modifier.fillMaxWidth().height(1.dp)) {
        drawLine(
            color       = c,
            start       = Offset(0f, size.height / 2),
            end         = Offset(size.width, size.height / 2),
            strokeWidth = 1.dp.toPx(),
            pathEffect  = PathEffect.dashPathEffect(floatArrayOf(6.dp.toPx(), 4.dp.toPx()), 0f)
        )
    }
}

// ── Order chips ───────────────────────────────────────────────────────────────

@Composable
private fun ReceiptOrderChips(
    orderTypeLabel: String?,
    customerName:   String?,
    tableLabel:     String?,
    pax:            Int,
    selectedMethod: PaymentMethod?
) {
    val hasAny = !orderTypeLabel.isNullOrBlank() || !customerName.isNullOrBlank() ||
                 !tableLabel.isNullOrBlank() || pax > 0 || selectedMethod != null
    if (!hasAny) return

    Row(
        modifier              = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(6.dp),
        verticalAlignment     = Alignment.CenterVertically
    ) {
        orderTypeLabel?.takeIf { it.isNotBlank() }?.let {
            InfoChipSmall(Icons.Default.TableBar, it)
        }
        selectedMethod?.let { method ->
            val (icon, label) = when (method) {
                PaymentMethod.CASH     -> Icons.Default.Payments   to "Cash"
                PaymentMethod.QRIS     -> Icons.Default.QrCode2    to "QRIS"
                PaymentMethod.CARD     -> Icons.Default.CreditCard  to "Card"
                PaymentMethod.TRANSFER -> Icons.Default.AccountBalance to "Transfer"
                else                   -> Icons.Default.MoreHoriz  to method.value
            }
            InfoChipSmall(icon, label)
        }
        if (!customerName.isNullOrBlank()) InfoChipSmall(Icons.Default.Person, customerName)
        if (!tableLabel.isNullOrBlank())   InfoChipSmall(Icons.Default.TableBar, tableLabel)
        if (pax > 0)                        InfoChipSmall(Icons.Default.People, "$pax tamu")
    }
}

@Composable
private fun InfoChipSmall(icon: ImageVector, label: String) {
    Surface(
        shape = MaterialTheme.shapes.extraSmall,
        color = MaterialTheme.colorScheme.surfaceVariant
    ) {
        Row(
            modifier              = Modifier.padding(horizontal = 7.dp, vertical = 3.dp),
            verticalAlignment     = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(3.dp)
        ) {
            Icon(icon, null, Modifier.size(10.dp), tint = MaterialTheme.colorScheme.onSurfaceVariant)
            Text(
                label,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

// ── Items section ─────────────────────────────────────────────────────────────

@Composable
private fun ReceiptLineItems(
    items:     ImmutableList<OrderLineItem>,
    itemCount: Int,
    subtotal:  Long
) {
    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
        items.forEach { line ->
            Row(
                modifier              = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment     = Alignment.Top
            ) {
                Column(modifier = Modifier.weight(1f).padding(end = 8.dp)) {
                    val label = if (line.variantName != null)
                        "${line.name} (${line.variantName})" else line.name
                    Text(
                        label,
                        style      = MaterialTheme.typography.bodySmall,
                        fontWeight = FontWeight.SemiBold
                    )
                    Text(
                        "${line.qty}× @ ${formatRupiah(line.price)}",
                        style      = MaterialTheme.typography.labelSmall,
                        color      = MaterialTheme.colorScheme.onSurfaceVariant,
                        fontFamily = FontFamily.Monospace
                    )
                }
                Text(
                    formatRupiah(line.subtotal),
                    style      = MaterialTheme.typography.bodySmall,
                    fontWeight = FontWeight.SemiBold,
                    fontFamily = FontFamily.Monospace
                )
            }
        }
    }
}

// ── Pricing breakdown ─────────────────────────────────────────────────────────

@Composable
private fun ReceiptPricingSection(
    subtotal:       Long,
    discount:       Long,
    tax:            Long,
    adminFee:       Long,
    deliveryFee:    Long,
    tip:            Long,
    taxLines:       ImmutableList<NamedAmount>,
    surchargeLines: ImmutableList<NamedAmount>,
    itemCount:      Int,
    hasItemDetail:  Boolean
) {
    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
        val subtotalLabel = if (hasItemDetail) "Subtotal ($itemCount item)" else "$itemCount item"
        ReceiptAmountRow(subtotalLabel, formatRupiah(subtotal))

        if (discount > 0) ReceiptAmountRow(
            "Diskon",
            "− ${formatRupiah(discount)}",
            valueColor = MaterialTheme.colorScheme.error
        )
        if (taxLines.isNotEmpty()) {
            taxLines.filter { it.amount > 0 }.forEach {
                ReceiptAmountRow(it.label, formatRupiah(it.amount))
            }
        } else if (tax > 0) {
            ReceiptAmountRow("Pajak", formatRupiah(tax))
        }
        if (surchargeLines.isNotEmpty()) {
            surchargeLines.filter { it.amount > 0 }.forEach {
                ReceiptAmountRow(it.label, formatRupiah(it.amount))
            }
        } else if (adminFee > 0) {
            ReceiptAmountRow("Biaya Admin", formatRupiah(adminFee))
        }
        if (deliveryFee > 0) ReceiptAmountRow("Ongkir", formatRupiah(deliveryFee))
        if (tip > 0)         ReceiptAmountRow("Tip",    formatRupiah(tip))
    }
}

// ── TOTAL card ────────────────────────────────────────────────────────────────

@Composable
private fun ReceiptTotalCard(total: Long) {
    Surface(
        shape    = RoundedCornerShape(8.dp),
        color    = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f),
        border   = BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.35f)),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier              = Modifier
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
                formatRupiah(total),
                style      = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.ExtraBold,
                color      = MaterialTheme.colorScheme.primary,
                fontFamily = FontFamily.Monospace
            )
        }
    }
}

// ── Cash payment rows ─────────────────────────────────────────────────────────

@Composable
private fun ReceiptPaymentRows(paidAmount: Long, changeAmount: Long) {
    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
        ReceiptAmountRow("Dibayar", formatRupiah(paidAmount))
        if (changeAmount > 0) ReceiptAmountRow(
            "Kembalian",
            formatRupiah(changeAmount),
            valueColor = MaterialTheme.colorScheme.secondary
        )
    }
}

// ── Mode pembayaran toggle ────────────────────────────────────────────────────

@Composable
private fun PaymentModeToggle(
    isSplit:  Boolean,
    onToggle: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier            = modifier,
        verticalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        Text(
            "Mode Pembayaran",
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
            AssistChip(
                onClick  = { if (isSplit) onToggle() },
                label    = { Text("Tunggal") },
                leadingIcon = { Icon(Icons.Default.Payments, null, Modifier.size(16.dp)) },
                modifier = Modifier.weight(1f),
                colors   = if (!isSplit) AssistChipDefaults.assistChipColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer
                ) else AssistChipDefaults.assistChipColors()
            )
            AssistChip(
                onClick  = { if (!isSplit) onToggle() },
                label    = { Text("Terpisah") },
                leadingIcon = { Icon(Icons.AutoMirrored.Filled.CallSplit, null, Modifier.size(16.dp)) },
                modifier = Modifier.weight(1f),
                colors   = if (isSplit) AssistChipDefaults.assistChipColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer
                ) else AssistChipDefaults.assistChipColors()
            )
        }
    }
}

// ── Shared row helper ─────────────────────────────────────────────────────────

@Composable
private fun ReceiptAmountRow(
    label:      String,
    value:      String,
    valueColor: Color = MaterialTheme.colorScheme.onSurface
) {
    Row(
        modifier              = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment     = Alignment.CenterVertically
    ) {
        Text(label, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        Text(
            value,
            style      = MaterialTheme.typography.bodySmall,
            fontWeight = FontWeight.SemiBold,
            color      = valueColor,
            fontFamily = FontFamily.Monospace
        )
    }
}

// ── Preview ───────────────────────────────────────────────────────────────────

@Preview(showBackground = true, heightDp = 700, widthDp = 360)
@Composable
private fun PaymentReceiptPanelPreview_Cash() {
    RancakTheme {
        PaymentReceiptPanel(
            itemCount      = 3,
            subtotal       = 75_000L,
            total          = 70_000L,
            discount       = 5_000L,
            tax            = 0L,
            adminFee       = 0L,
            deliveryFee    = 0L,
            tip            = 0L,
            changeAmount   = 30_000L,
            isCashSelected = true,
            paidAmount     = "100000",
            orderItems     = persistentListOf(
                OrderLineItem("Kopi Susu Gula Aren", null, 2, 18_000L, 36_000L),
                OrderLineItem("Croissant",           null, 1, 22_000L, 22_000L),
                OrderLineItem("Es Teh",              null, 1, 12_000L, 12_000L)
            ),
            orderTypeLabel = "Dine In",
            customerName   = "Budi",
            pax            = 2,
            selectedMethod = PaymentMethod.CASH,
            modifier       = Modifier.padding(12.dp)
        )
    }
}

@Preview(showBackground = true, heightDp = 500, widthDp = 360)
@Composable
private fun PaymentReceiptPanelPreview_Empty() {
    RancakTheme {
        PaymentReceiptPanel(
            itemCount      = 0,
            subtotal       = 0L,
            total          = 0L,
            discount       = 0L,
            tax            = 0L,
            adminFee       = 0L,
            deliveryFee    = 0L,
            tip            = 0L,
            changeAmount   = 0L,
            isCashSelected = false,
            paidAmount     = "",
            orderTypeLabel = "Take Away",
            selectedMethod = PaymentMethod.QRIS,
            modifier       = Modifier.padding(12.dp)
        )
    }
}

package id.rancak.app.presentation.ui.sales.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import id.rancak.app.domain.model.OrderType
import id.rancak.app.domain.model.Sale
import id.rancak.app.domain.model.SaleItem
import id.rancak.app.domain.model.SaleStatus
import id.rancak.app.presentation.designsystem.RancakColors
import id.rancak.app.presentation.designsystem.RancakTheme
import id.rancak.app.presentation.util.formatRupiah

/**
 * Compact sale summary card used in the left list. Left-edge strip is tinted
 * by status. Highlights selected state for the tablet detail pane.
 */
@Composable
internal fun SaleCard(
    sale: Sale,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    val semantic = RancakColors.semantic
    val statusColor = when (sale.status) {
        SaleStatus.PAID                       -> semantic.success
        SaleStatus.HELD                       -> semantic.warning
        SaleStatus.VOID, SaleStatus.CANCELLED -> MaterialTheme.colorScheme.error
        SaleStatus.REFUNDED                   -> semantic.info
    }
    val statusLabel = when (sale.status) {
        SaleStatus.PAID      -> "LUNAS"
        SaleStatus.HELD      -> "BELUM BAYAR"
        SaleStatus.VOID      -> "VOID"
        SaleStatus.CANCELLED -> "BATAL"
        SaleStatus.REFUNDED  -> "REFUND"
    }

    Surface(
        shape = MaterialTheme.shapes.medium,
        color = if (isSelected)
            MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.25f)
        else
            MaterialTheme.colorScheme.surface,
        tonalElevation  = if (isSelected) 2.dp else 0.dp,
        shadowElevation = if (isSelected) 0.dp else 0.5.dp,
        modifier = Modifier
            .fillMaxWidth()
            .clip(MaterialTheme.shapes.medium)
            .clickable(onClick = onClick)
    ) {
        Row(modifier = Modifier.fillMaxWidth().height(IntrinsicSize.Min)) {
            Box(
                modifier = Modifier
                    .width(4.dp)
                    .fillMaxHeight()
                    .background(statusColor)
            )
            Column(
                modifier = Modifier
                    .weight(1f)
                    .padding(horizontal = 10.dp, vertical = 8.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment     = Alignment.CenterVertically
                ) {
                    Text(
                        sale.invoiceNo ?: "-",
                        style      = MaterialTheme.typography.labelLarge,
                        fontWeight = FontWeight.Bold,
                        maxLines   = 1,
                        overflow   = TextOverflow.Ellipsis,
                        modifier   = Modifier.weight(1f)
                    )
                    Text(
                        formatRupiah(sale.total),
                        style      = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                        color      = MaterialTheme.colorScheme.primary
                    )
                }

                Spacer(Modifier.height(3.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment     = Alignment.CenterVertically
                ) {
                    Text(
                        "${sale.items.size} item  •  ${sale.orderType.value.replace("_", " ")}",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Surface(
                        shape = MaterialTheme.shapes.extraSmall,
                        color = statusColor.copy(alpha = 0.12f)
                    ) {
                        Text(
                            statusLabel,
                            modifier   = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                            style      = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.SemiBold,
                            color      = statusColor
                        )
                    }
                }

                sale.createdAt?.take(16)?.replace("T", "  ")?.let { time ->
                    Text(
                        time,
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.outline.copy(alpha = 0.7f)
                    )
                }
            }
        }
    }
}

/** Dashed 1-dp horizontal divider, used inside the receipt detail panel. */
@Composable
internal fun DashedDivider(
    modifier: Modifier = Modifier,
    color: Color = MaterialTheme.colorScheme.outlineVariant
) {
    val c = color
    Canvas(modifier = modifier.fillMaxWidth().height(1.dp)) {
        drawLine(
            color       = c,
            start       = Offset(0f, size.height / 2),
            end         = Offset(size.width, size.height / 2),
            strokeWidth = 1.dp.toPx(),
            pathEffect  = PathEffect.dashPathEffect(floatArrayOf(6.dp.toPx(), 4.dp.toPx()), 0f)
        )
    }
}

@Preview
@Composable
private fun SaleCardPreview_Paid() {
    RancakTheme {
        Box(Modifier.padding(8.dp)) {
            SaleCard(
                sale        = previewSale(SaleStatus.PAID),
                isSelected  = false,
                onClick     = {}
            )
        }
    }
}

@Preview
@Composable
private fun SaleCardPreview_Held_Selected() {
    RancakTheme {
        Box(Modifier.padding(8.dp)) {
            SaleCard(
                sale        = previewSale(SaleStatus.HELD),
                isSelected  = true,
                onClick     = {}
            )
        }
    }
}

private fun previewSale(status: SaleStatus) = Sale(
    uuid          = "s1",
    invoiceNo     = "INV-20260422-0001",
    orderType     = OrderType.DINE_IN,
    queueNumber   = 4,
    status        = status,
    customerName  = "Budi",
    subtotal      = 36_000L,
    discount      = 0L,
    surcharge     = 0L,
    tax           = 0L,
    total         = 36_000L,
    paymentMethod = null,
    paidAmount    = 0L,
    changeAmount  = 0L,
    items = listOf(
        SaleItem(
            uuid = "i1", productUuid = "p1", productName = "Kopi Susu",
            qty = "2", price = 18_000L, subtotal = 36_000L,
            variantName = null, note = null
        )
    ),
    createdAt = "2026-04-22T10:15:00"
)

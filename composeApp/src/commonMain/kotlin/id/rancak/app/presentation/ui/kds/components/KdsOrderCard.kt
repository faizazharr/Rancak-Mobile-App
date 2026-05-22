package id.rancak.app.presentation.ui.kds.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import id.rancak.app.domain.model.KdsItem
import id.rancak.app.domain.model.KdsItemStatus
import id.rancak.app.domain.model.KdsOrder
import id.rancak.app.domain.model.KdsStatus
import id.rancak.app.domain.model.OrderType
import id.rancak.app.presentation.designsystem.RancakTheme
import kotlinx.collections.immutable.persistentListOf

// ── Helpers (package-private) ─────────────────────────────────────────────────

internal fun headerColorForStatus(status: KdsStatus): Color = when (status) {
    KdsStatus.NEW     -> Color(0xFF1565C0)
    KdsStatus.COOKING -> Color(0xFFE65100)
    KdsStatus.READY   -> Color(0xFF2E7D32)
    KdsStatus.DONE    -> Color(0xFF757575)
}

internal fun orderTime(createdAt: String?): String {
    if (createdAt.isNullOrBlank()) return ""
    return try {
        val t = createdAt.replace("T", " ")
        if (t.length >= 16) t.substring(11, 16) else ""
    } catch (_: Exception) { "" }
}

// ── Component ─────────────────────────────────────────────────────────────────

@Composable
fun KdsOrderCard(
    order: KdsOrder,
    onAdvance: (KdsStatus) -> Unit,
    modifier: Modifier = Modifier
) {
    val nextStatus = when (order.status) {
        KdsStatus.NEW     -> KdsStatus.COOKING
        KdsStatus.COOKING -> KdsStatus.READY
        KdsStatus.READY   -> KdsStatus.DONE
        KdsStatus.DONE    -> null
    }
    val headerColor    = headerColorForStatus(order.status)
    val time           = orderTime(order.createdAt)
    val orderTypeLabel = when (order.orderType) {
        OrderType.DINE_IN  -> "Dine In"
        OrderType.TAKEAWAY -> "Take Away"
        OrderType.DELIVERY -> "Delivery"
    }

    Card(
        modifier = modifier.fillMaxWidth().then(
            if (nextStatus != null) Modifier.clickable { onAdvance(nextStatus) } else Modifier
        ),
        shape  = MaterialTheme.shapes.medium,
        colors = CardDefaults.cardColors(containerColor = Color.White)
    ) {
        Column {
            Box(
                modifier = Modifier.fillMaxWidth().background(headerColor).padding(horizontal = 12.dp, vertical = 8.dp)
            ) {
                Row(modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text("#${order.queueNumber ?: "-"}", color = Color.White, fontSize = 22.sp, fontWeight = FontWeight.ExtraBold)
                        if (!order.invoiceNo.isNullOrBlank()) {
                            Text(order.invoiceNo, color = Color.White.copy(alpha = 0.8f), fontSize = 10.sp,
                                maxLines = 1, overflow = TextOverflow.Ellipsis)
                        }
                        Text("$orderTypeLabel${order.tableName?.let { " · $it" } ?: ""}",
                            color = Color.White.copy(alpha = 0.9f), fontSize = 13.sp, fontWeight = FontWeight.Medium,
                            maxLines = 1, overflow = TextOverflow.Ellipsis)
                        if (!order.customerName.isNullOrBlank()) {
                            Text(
                                "a/n ${order.customerName}",
                                color = Color.White.copy(alpha = 0.95f),
                                fontSize = 12.sp,
                                fontWeight = FontWeight.SemiBold,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                    }
                    if (time.isNotBlank()) {
                        Column(horizontalAlignment = Alignment.End) {
                            Text(time, color = Color.White, fontSize = 18.sp, fontWeight = FontWeight.Bold)
                            Text(when (order.status) {
                                KdsStatus.NEW     -> "BARU"
                                KdsStatus.COOKING -> "MASAK"
                                KdsStatus.READY   -> "SIAP"
                                KdsStatus.DONE    -> "SELESAI"
                            }, color = Color.White.copy(alpha = 0.9f), fontSize = 10.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }

            Column(modifier = Modifier.padding(horizontal = 12.dp, vertical = 10.dp),
                verticalArrangement = Arrangement.spacedBy(6.dp)) {
                order.items.forEach { item ->
                    Row(modifier = Modifier.fillMaxWidth()) {
                        Text(item.qty, fontSize = 16.sp, fontWeight = FontWeight.Bold,
                            color = Color.Black, modifier = Modifier.width(28.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(item.productName, fontSize = 16.sp, fontWeight = FontWeight.SemiBold, color = Color.Black)
                            item.variantName?.let { Text("- $it", fontSize = 14.sp, color = Color.DarkGray) }
                            item.note?.let { Text("- $it", fontSize = 14.sp, color = Color.DarkGray) }
                        }
                    }
                }
                if (nextStatus != null) {
                    HorizontalDivider(modifier = Modifier.padding(top = 4.dp))
                    Text(
                        when (nextStatus) {
                            KdsStatus.COOKING -> "Ketuk untuk mulai masak ▶"
                            KdsStatus.READY   -> "Ketuk jika siap antar ✓"
                            KdsStatus.DONE    -> "Ketuk untuk selesaikan ✓✓"
                            else              -> ""
                        },
                        fontSize = 12.sp, color = headerColor, fontWeight = FontWeight.SemiBold,
                        modifier = Modifier.fillMaxWidth().padding(top = 6.dp)
                    )
                }
            }
        }
    }
}

// ── Preview ───────────────────────────────────────────────────────────────────

@Preview
@Composable
private fun KdsOrderCardNewPreview() {
    RancakTheme {
        KdsOrderCard(
            order = KdsOrder(
                uuid = "1", invoiceNo = "ORD-001", orderType = OrderType.DINE_IN,
                tableName = "Meja 3", queueNumber = 1, customerName = "Andi", note = null,
                status = KdsStatus.NEW,
                items = persistentListOf(KdsItem("i1", "Nasi Goreng", "2", null, null, KdsItemStatus.PENDING)),
                createdAt = "2026-01-01T10:15:00"
            ),
            onAdvance = {}
        )
    }
}

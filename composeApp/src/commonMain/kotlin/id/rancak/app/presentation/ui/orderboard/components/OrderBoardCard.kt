package id.rancak.app.presentation.ui.orderboard.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import id.rancak.app.domain.model.OrderBoardItem
import id.rancak.app.domain.model.OrderBoardOrder
import id.rancak.app.domain.model.OrderType
import id.rancak.app.domain.model.SaleStatus
import id.rancak.app.presentation.designsystem.RancakTheme
import kotlinx.collections.immutable.persistentListOf
import kotlin.time.Clock

// ── Helpers (package-private) ─────────────────────────────────────────────────

internal fun headerColorForStatus(status: SaleStatus): Color = when (status) {
    SaleStatus.HELD -> Color(0xFFF57C00)
    SaleStatus.PAID -> Color(0xFF2E7D32)
    else            -> Color(0xFF757575)
}

@Composable
internal fun rememberElapsed(createdAt: String?): Pair<String, Long> {
    val nowMillis = remember { Clock.System.now().toEpochMilliseconds() }
    if (createdAt.isNullOrBlank()) return "" to 0L
    return remember(createdAt) {
        try {
            val cleaned  = createdAt.replace("T", " ").take(19)
            val parts    = cleaned.split(" ")
            val dateParts = parts[0].split("-").map { it.toInt() }
            val timeParts = parts[1].split(":").map { it.toInt() }
            val y = dateParts[0]; val m = dateParts[1]; val d = dateParts[2]
            val h = timeParts[0]; val min = timeParts[1]; val s = timeParts[2]
            val daysFromYear  = (y - 1970) * 365L + ((y - 1969) / 4) - ((y - 1901) / 100) + ((y - 1601) / 400)
            val daysInMonth   = intArrayOf(0, 31, 28, 31, 30, 31, 30, 31, 31, 30, 31, 30, 31)
            val isLeap        = (y % 4 == 0 && y % 100 != 0) || (y % 400 == 0)
            if (isLeap) daysInMonth[2] = 29
            val daysFromMonth = (1 until m).sumOf { daysInMonth[it].toLong() }
            val totalDays     = daysFromYear + daysFromMonth + (d - 1)
            val createdMs     = (totalDays * 86400L + h * 3600L + min * 60L + s) * 1000L
            val diffSec       = ((nowMillis - createdMs) / 1000L).coerceAtLeast(0)
            val totalMins = diffSec / 60
            val days  = diffSec / 86_400
            val hours = (diffSec % 86_400) / 3600
            val mins  = (diffSec % 3600) / 60
            val secs  = diffSec % 60
            // Format yang mudah dibaca:
            //   < 1 jam   → "MM:SS"      (mis. "05:23")
            //   < 1 hari  → "Hj MMm"     (mis. "2j 15m")
            //   ≥ 1 hari  → "Dh Hj"      (mis. "1h 18j")
            val label = when {
                days >= 1L  -> "${days}h ${hours}j"
                hours >= 1L -> "${hours}j ${mins}m"
                else        -> "${mins.toString().padStart(2, '0')}:${secs.toString().padStart(2, '0')}"
            }
            label to totalMins
        } catch (_: Exception) { "" to 0L }
    }
}

// ── Component ─────────────────────────────────────────────────────────────────

@Composable
fun OrderBoardCard(
    order: OrderBoardOrder,
    onServe: () -> Unit,
    modifier: Modifier = Modifier
) {
    val (elapsed, _)   = rememberElapsed(order.createdAt)
    val headerColor    = headerColorForStatus(order.status)
    val orderTypeLabel = when (order.orderType) {
        OrderType.DINE_IN  -> "Dine In"
        OrderType.TAKEAWAY -> "Take Away"
        OrderType.DELIVERY -> "Delivery"
    }

    Card(
        modifier = modifier.fillMaxWidth().then(
            if (order.status == SaleStatus.HELD) Modifier.clickable(onClick = onServe) else Modifier
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
                        Text("#${order.queueNumber ?: "-"}", color = Color.White,
                            fontSize = 20.sp, fontWeight = FontWeight.ExtraBold)
                        Text(orderTypeLabel, color = Color.White.copy(alpha = 0.9f),
                            fontSize = 13.sp, fontWeight = FontWeight.Medium,
                            maxLines = 1, overflow = TextOverflow.Ellipsis)
                    }
                    if (elapsed.isNotBlank()) {
                        // Pill background agar timer kontras & mudah dibaca,
                        // walau header berwarna terang.
                        Box(
                            modifier = Modifier
                                .background(
                                    color = Color.Black.copy(alpha = 0.22f),
                                    shape = MaterialTheme.shapes.small
                                )
                                .padding(horizontal = 10.dp, vertical = 4.dp)
                        ) {
                            Text(
                                elapsed,
                                color = Color.White,
                                fontSize = 18.sp,
                                fontWeight = FontWeight.ExtraBold,
                                maxLines = 1
                            )
                        }
                    }
                }
            }

            Column(modifier = Modifier.padding(horizontal = 12.dp, vertical = 10.dp),
                verticalArrangement = Arrangement.spacedBy(6.dp)) {
                order.items.forEach { item ->
                    Row(modifier = Modifier.fillMaxWidth()) {
                        Text(item.qty.toString(), fontSize = 16.sp, fontWeight = FontWeight.Bold,
                            color = Color.Black, modifier = Modifier.width(28.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(item.productName, fontSize = 16.sp, fontWeight = FontWeight.SemiBold, color = Color.Black)
                            item.note?.let { Text("- $it", fontSize = 14.sp, color = Color.DarkGray) }
                        }
                    }
                }
                HorizontalDivider(modifier = Modifier.padding(top = 4.dp))
                Row(modifier = Modifier.fillMaxWidth().padding(top = 4.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically) {
                    Text(order.customerName ?: order.invoiceNo ?: "",
                        fontSize = 15.sp, fontWeight = FontWeight.Bold, color = Color.Black)
                    if (order.status == SaleStatus.HELD) {
                        Text("Ketuk untuk antar ✓", fontSize = 12.sp,
                            color = headerColor, fontWeight = FontWeight.SemiBold)
                    }
                }
            }
        }
    }
}

// ── Preview ───────────────────────────────────────────────────────────────────

@Preview
@Composable
private fun OrderBoardCardHeldPreview() {
    RancakTheme {
        OrderBoardCard(
            order = OrderBoardOrder(
                uuid = "1", invoiceNo = "INV-001", queueNumber = 1,
                orderType = OrderType.DINE_IN, customerName = "Andi",
                status = SaleStatus.HELD, createdAt = "2026-01-01T10:15:00",
                servedAt = null,
                items = persistentListOf(OrderBoardItem("Nasi Goreng", 2, null))
            ),
            onServe = {}
        )
    }
}

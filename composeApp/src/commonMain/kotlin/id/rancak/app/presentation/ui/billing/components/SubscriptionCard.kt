package id.rancak.app.presentation.ui.billing.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import id.rancak.app.domain.model.SubscriptionState
import id.rancak.app.presentation.designsystem.Primary
import id.rancak.app.presentation.designsystem.RancakTheme
import id.rancak.app.presentation.designsystem.Tertiary
import id.rancak.app.presentation.ui.billing.Quadruple
import id.rancak.app.presentation.ui.billing.linearGradientBrush

@Composable
fun SubscriptionCard(
    subscription: SubscriptionState?,
    isTablet: Boolean,
    modifier: Modifier = Modifier
) {
    val (gradientStart, gradientEnd, statusLabel, statusIcon) = when (subscription?.status) {
        "active"  -> Quadruple(Primary, Color(0xFF1DB88A), "Aktif", Icons.Default.CheckCircle)
        "trial"   -> Quadruple(Tertiary, Color(0xFF5588EE), "Trial", Icons.Default.HourglassTop)
        "expired" -> Quadruple(Color(0xFF9E9E9E), Color(0xFF757575), "Kedaluwarsa", Icons.Default.ErrorOutline)
        else      -> Quadruple(Color(0xFF9E9E9E), Color(0xFF757575), "Tidak Aktif", Icons.Default.Block)
    }

    Box(
        modifier = modifier
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
                // Kiri: status + nama paket — bisa mengambil ruang lebih banyak
                Column(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Icon(statusIcon, null, tint = Color.White, modifier = Modifier.size(14.dp))
                        Text(
                            "Status Langganan",
                            style = MaterialTheme.typography.labelSmall,
                            color = Color.White.copy(alpha = 0.75f)
                        )
                        SubStatusPill(statusLabel)
                    }
                    Text(
                        subscription?.plan?.uppercase() ?: "TIDAK ADA PAKET",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.ExtraBold,
                        color = Color.White,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
                // Kanan: info detail — setiap kolom punya lebar tetap agar tidak kolaps
                if (subscription != null) {
                    Spacer(Modifier.width(16.dp))
                    Row(horizontalArrangement = Arrangement.spacedBy(20.dp)) {
                        SubInfoItem(
                            label = "Mulai",
                            value = subscription.startedAt?.take(10) ?: "-",
                            align = Alignment.Start,
                            minWidth = 90.dp
                        )
                        SubInfoItem(
                            label = "Berakhir",
                            value = subscription.expiresAt?.take(10) ?: "-",
                            align = Alignment.Start,
                            minWidth = 90.dp
                        )
                        SubInfoItem(
                            label = "Maks. Pengguna",
                            value = subscription.maxUsers?.toString() ?: "Tidak terbatas",
                            align = Alignment.End,
                            minWidth = 100.dp
                        )
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
                    Text(
                        "Status Langganan",
                        style = MaterialTheme.typography.labelSmall,
                        color = Color.White.copy(alpha = 0.75f)
                    )
                    Spacer(Modifier.weight(1f))
                    SubStatusPill(statusLabel)
                }
                Text(
                    subscription?.plan?.uppercase() ?: "TIDAK ADA PAKET",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.ExtraBold,
                    color = Color.White,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                if (subscription != null) {
                    HorizontalDivider(color = Color.White.copy(alpha = 0.2f))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        SubInfoItem(
                            label = "Mulai",
                            value = subscription.startedAt?.take(10) ?: "-"
                        )
                        SubInfoItem(
                            label = "Berakhir",
                            value = subscription.expiresAt?.take(10) ?: "-",
                            align = Alignment.CenterHorizontally
                        )
                        SubInfoItem(
                            label = "Maks. Pengguna",
                            value = subscription.maxUsers?.toString() ?: "∞",
                            align = Alignment.End
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun SubStatusPill(label: String) {
    Surface(shape = RoundedCornerShape(50), color = Color.White.copy(alpha = 0.2f)) {
        Text(
            label,
            style = MaterialTheme.typography.labelSmall,
            fontWeight = FontWeight.Bold,
            color = Color.White,
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
        )
    }
}

@Composable
private fun SubInfoItem(
    label: String,
    value: String,
    align: Alignment.Horizontal = Alignment.Start,
    minWidth: androidx.compose.ui.unit.Dp = androidx.compose.ui.unit.Dp.Unspecified
) {
    val mod = if (minWidth != androidx.compose.ui.unit.Dp.Unspecified)
        Modifier.widthIn(min = minWidth)
    else
        Modifier
    Column(
        modifier = mod,
        horizontalAlignment = align,
        verticalArrangement = Arrangement.spacedBy(2.dp)
    ) {
        Text(
            label,
            style = MaterialTheme.typography.labelSmall,
            color = Color.White.copy(alpha = 0.65f),
            maxLines = 1,
            overflow = TextOverflow.Clip
        )
        Text(
            value,
            style = MaterialTheme.typography.bodySmall,
            fontWeight = FontWeight.SemiBold,
            color = Color.White,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
    }
}

// ── Preview ───────────────────────────────────────────────────────────────────

@Preview
@Composable
private fun SubscriptionCardActivePreview() {
    RancakTheme {
        SubscriptionCard(
            subscription = SubscriptionState("active", "pro", "2024-01-01", "2025-01-01", 5, false),
            isTablet = false
        )
    }
}

@Preview
@Composable
private fun SubscriptionCardNullPreview() {
    RancakTheme {
        SubscriptionCard(subscription = null, isTablet = false)
    }
}

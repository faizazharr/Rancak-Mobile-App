package id.rancak.app.presentation.ui.pos.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.BookmarkBorder
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import kotlinx.collections.immutable.persistentListOf
import id.rancak.app.domain.model.OrderType
import id.rancak.app.domain.model.CartItem
import id.rancak.app.presentation.designsystem.RancakColors
import id.rancak.app.presentation.designsystem.RancakTheme
import id.rancak.app.presentation.util.formatRupiah
import id.rancak.app.presentation.viewmodel.CartUiState

/**
 * Floating cart bar used on phone layouts. Tapping it opens the full cart.
 */
@Composable
internal fun CartBar(
    cartState: CartUiState,
    primary: Color,
    onCartClick: () -> Unit,
    onOpenBillClick: () -> Unit = {}
) {
    // Single unified floating card — Open Bill strip on top, cart row below
    Surface(
        modifier        = Modifier
            .fillMaxWidth()
            .navigationBarsPadding()
            .padding(horizontal = 10.dp, vertical = 8.dp),
        shape           = MaterialTheme.shapes.extraLarge,
        shadowElevation = 12.dp,
        color           = primary
    ) {
        Column {
            // ── Open Bill strip ───────────────────────────────────────────
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(RancakColors.semantic.warning.copy(alpha = 0.18f))
                    .clickable(onClick = onOpenBillClick)
                    .padding(horizontal = 14.dp, vertical = 8.dp),
                verticalAlignment     = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    verticalAlignment     = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(5.dp)
                ) {
                    Icon(
                        Icons.Default.BookmarkBorder, null,
                        Modifier.size(13.dp),
                        tint = RancakColors.semantic.warning
                    )
                    Text(
                        "Open Bill",
                        style      = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Bold,
                        color      = RancakColors.semantic.warning
                    )
                }
                Row(
                    verticalAlignment     = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(2.dp)
                ) {
                    Text(
                        "Lihat semua",
                        style = MaterialTheme.typography.labelSmall,
                        color = RancakColors.semantic.warning.copy(alpha = 0.82f)
                    )
                    Icon(
                        Icons.AutoMirrored.Filled.ArrowForward, null,
                        Modifier.size(11.dp),
                        tint = RancakColors.semantic.warning.copy(alpha = 0.82f)
                    )
                }
            }

            // ── Cart row ──────────────────────────────────────────────────
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable(onClick = onCartClick)
                    .padding(horizontal = 14.dp, vertical = 12.dp),
                verticalAlignment     = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    verticalAlignment     = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Box(
                        Modifier
                            .size(32.dp)
                            .clip(CircleShape)
                            .background(MaterialTheme.colorScheme.onPrimary.copy(0.22f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            "${cartState.itemCount}",
                            style      = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.ExtraBold,
                            color      = MaterialTheme.colorScheme.onPrimary
                        )
                    }
                    Column {
                        Text(
                            "${cartState.itemCount} item",
                            style      = MaterialTheme.typography.labelSmall,
                            color      = MaterialTheme.colorScheme.onPrimary.copy(0.72f)
                        )
                        Text(
                            formatRupiah(cartState.subtotal),
                            style      = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.ExtraBold,
                            color      = MaterialTheme.colorScheme.onPrimary
                        )
                    }
                }
                Row(
                    verticalAlignment     = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Text(
                        "Keranjang",
                        style      = MaterialTheme.typography.labelLarge,
                        fontWeight = FontWeight.Bold,
                        color      = MaterialTheme.colorScheme.onPrimary
                    )
                    Icon(
                        Icons.AutoMirrored.Filled.ArrowForward, null,
                        Modifier.size(16.dp),
                        tint = MaterialTheme.colorScheme.onPrimary
                    )
                }
            }
        }
    }
}

@Preview
@Composable
private fun CartBarPreview() {
    RancakTheme {
        CartBar(
            cartState = CartUiState(
                items = persistentListOf(
                    CartItem(
                        productUuid = "p1",
                        productName = "Kopi Susu",
                        qty         = 2,
                        price       = 18_000L
                    )
                ),
                orderType = OrderType.DINE_IN
            ),
            primary        = MaterialTheme.colorScheme.primary,
            onCartClick    = {},
            onOpenBillClick = {}
        )
    }
}

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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import id.rancak.app.domain.model.OrderType
import id.rancak.app.domain.repository.CartItem
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
    onCartClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .navigationBarsPadding()
            .padding(horizontal = 10.dp, vertical = 8.dp)
            .clip(RoundedCornerShape(14.dp))
            .background(primary)
            .clickable(onClick = onCartClick)
            .padding(horizontal = 14.dp, vertical = 10.dp)
    ) {
        Row(
            modifier              = Modifier.fillMaxWidth(),
            verticalAlignment     = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                verticalAlignment     = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Box(
                    Modifier
                        .size(30.dp)
                        .clip(CircleShape)
                        .background(Color.White.copy(0.22f)),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        "${cartState.itemCount}",
                        style      = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.ExtraBold,
                        color      = Color.White
                    )
                }
                Column {
                    Text(
                        "${cartState.itemCount} item",
                        style      = MaterialTheme.typography.labelSmall,
                        color      = Color.White.copy(0.72f),
                        lineHeight = 14.sp
                    )
                    Text(
                        formatRupiah(cartState.subtotal),
                        style      = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.ExtraBold,
                        color      = Color.White
                    )
                }
            }
            Row(
                verticalAlignment     = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(3.dp)
            ) {
                Text(
                    "Keranjang",
                    style      = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.Bold,
                    color      = Color.White
                )
                Icon(
                    Icons.AutoMirrored.Filled.ArrowForward, null,
                    Modifier.size(16.dp),
                    tint = Color.White
                )
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
                items = listOf(
                    CartItem(
                        productUuid = "p1",
                        productName = "Kopi Susu",
                        qty         = 2,
                        price       = 18_000L
                    )
                ),
                orderType = OrderType.DINE_IN
            ),
            primary     = MaterialTheme.colorScheme.primary,
            onCartClick = {}
        )
    }
}

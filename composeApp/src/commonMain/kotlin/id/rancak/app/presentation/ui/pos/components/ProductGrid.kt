package id.rancak.app.presentation.ui.pos.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.SearchOff
import androidx.compose.material.icons.filled.WifiOff
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import id.rancak.app.domain.model.Category
import id.rancak.app.domain.model.Product
import id.rancak.app.presentation.designsystem.RancakTheme
import id.rancak.app.presentation.util.formatRupiah
import id.rancak.app.presentation.viewmodel.PosUiState

/**
 * Product grid body. Renders loading, error, empty, and populated states.
 */
@Composable
internal fun ProductGridContent(
    uiState: PosUiState,
    cartQtyMap: Map<String, Int>,
    bottomPad: Dp,
    onRefresh: () -> Unit,
    onAdd: (Product) -> Unit,
    minCellDp: Int = 110
) {
    val onSurfaceVariant = MaterialTheme.colorScheme.onSurfaceVariant

    when {
        uiState.isLoading -> Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            CircularProgressIndicator(strokeWidth = 2.dp)
        }

        uiState.error != null -> Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier            = Modifier.padding(16.dp)
            ) {
                Icon(Icons.Default.WifiOff, null, Modifier.size(44.dp), tint = onSurfaceVariant.copy(0.4f))
                Spacer(Modifier.height(8.dp))
                Text(
                    uiState.error,
                    color     = onSurfaceVariant.copy(0.65f),
                    style     = MaterialTheme.typography.bodySmall,
                    textAlign = TextAlign.Center
                )
                Spacer(Modifier.height(12.dp))
                OutlinedButton(onClick = onRefresh) { Text("Coba Lagi") }
            }
        }

        uiState.filteredProducts.isEmpty() -> Box(
            Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Icon(Icons.Default.SearchOff, null, Modifier.size(44.dp), tint = onSurfaceVariant.copy(0.3f))
                Spacer(Modifier.height(8.dp))
                Text(
                    "Produk tidak ditemukan",
                    style = MaterialTheme.typography.bodySmall,
                    color = onSurfaceVariant.copy(0.5f)
                )
            }
        }

        else -> LazyVerticalGrid(
            columns               = GridCells.Adaptive(minSize = minCellDp.dp),
            contentPadding        = PaddingValues(
                start  = 8.dp, end = 8.dp,
                top    = 4.dp, bottom = bottomPad
            ),
            horizontalArrangement = Arrangement.spacedBy(7.dp),
            verticalArrangement   = Arrangement.spacedBy(7.dp)
        ) {
            items(uiState.filteredProducts, key = { it.uuid }) { product ->
                val qty = cartQtyMap[product.uuid] ?: 0
                PosProductCard(
                    product = product,
                    qty     = qty,
                    onAdd   = { onAdd(product) }
                )
            }
        }
    }
}

/**
 * Single product tile. Highlights when in cart with an animated qty badge.
 */
@Composable
internal fun PosProductCard(
    product: Product,
    qty: Int,
    onAdd: () -> Unit
) {
    val inCart           = qty > 0
    val accent           = accentFor(product.category?.name ?: product.name)
    val onSurface        = MaterialTheme.colorScheme.onSurface
    val onSurfaceVariant = MaterialTheme.colorScheme.onSurfaceVariant
    val surfaceVariant   = MaterialTheme.colorScheme.surfaceVariant

    val cardBg = when {
        !product.isActive -> surfaceVariant.copy(0.35f)
        inCart            -> accent.copy(0.05f)
        else              -> MaterialTheme.colorScheme.surface
    }
    val borderColor = when {
        !product.isActive -> MaterialTheme.colorScheme.outlineVariant.copy(0.3f)
        inCart            -> accent.copy(0.65f)
        else              -> MaterialTheme.colorScheme.outlineVariant.copy(0.55f)
    }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(10.dp))
            .background(cardBg)
            .border(
                width  = if (inCart) 2.dp else 1.dp,
                color  = borderColor,
                shape  = RoundedCornerShape(10.dp)
            )
            .clickable(enabled = product.isActive, onClick = onAdd)
            .padding(horizontal = 10.dp, vertical = 10.dp)
    ) {
        Column(Modifier.fillMaxWidth()) {
            Row(
                Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment     = Alignment.Top
            ) {
                Text(
                    product.name,
                    style      = MaterialTheme.typography.bodySmall,
                    fontWeight = FontWeight.ExtraBold,
                    fontSize   = 13.sp,
                    color      = if (product.isActive) onSurface else onSurfaceVariant.copy(0.4f),
                    maxLines   = 3,
                    overflow   = TextOverflow.Ellipsis,
                    lineHeight = 17.sp,
                    modifier   = Modifier
                        .weight(1f)
                        .heightIn(min = 44.dp)
                )

                Spacer(Modifier.width(4.dp))

                AnimatedVisibility(
                    visible = inCart,
                    enter   = scaleIn(tween(150)) + fadeIn(tween(150)),
                    exit    = scaleOut(tween(110)) + fadeOut(tween(110))
                ) {
                    Box(
                        Modifier
                            .size(22.dp)
                            .clip(CircleShape)
                            .background(accent),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            "$qty",
                            style      = MaterialTheme.typography.labelSmall,
                            fontSize   = 10.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color      = Color.White
                        )
                    }
                }
            }

            Spacer(Modifier.height(8.dp))

            Row(
                Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment     = Alignment.CenterVertically
            ) {
                Text(
                    formatRupiah(product.price),
                    style      = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.Bold,
                    fontSize   = 11.sp,
                    color      = if (product.isActive) accent.copy(0.85f)
                                 else onSurfaceVariant.copy(0.3f),
                    maxLines   = 1
                )

                if (!product.isActive) {
                    Box(
                        Modifier
                            .clip(RoundedCornerShape(4.dp))
                            .background(surfaceVariant)
                            .padding(horizontal = 6.dp, vertical = 2.dp)
                    ) {
                        Text(
                            "Habis",
                            style    = MaterialTheme.typography.labelSmall,
                            fontSize = 9.sp,
                            color    = onSurfaceVariant.copy(0.5f)
                        )
                    }
                }
            }
        }
    }
}

@Preview
@Composable
private fun PosProductCardPreview_Available() {
    RancakTheme {
        Box(Modifier.padding(8.dp).size(width = 160.dp, height = 120.dp)) {
            PosProductCard(
                product = previewProduct(isActive = true),
                qty     = 0,
                onAdd   = {}
            )
        }
    }
}

@Preview
@Composable
private fun PosProductCardPreview_InCart() {
    RancakTheme {
        Box(Modifier.padding(8.dp).size(width = 160.dp, height = 120.dp)) {
            PosProductCard(
                product = previewProduct(isActive = true),
                qty     = 3,
                onAdd   = {}
            )
        }
    }
}

@Preview
@Composable
private fun PosProductCardPreview_OutOfStock() {
    RancakTheme {
        Box(Modifier.padding(8.dp).size(width = 160.dp, height = 120.dp)) {
            PosProductCard(
                product = previewProduct(isActive = false),
                qty     = 0,
                onAdd   = {}
            )
        }
    }
}

private fun previewProduct(isActive: Boolean) = Product(
    uuid        = "p-1",
    sku         = "SKU-001",
    barcode     = null,
    name        = "Kopi Susu Gula Aren",
    description = null,
    category    = Category(uuid = "c1", name = "Minuman", description = null),
    price       = 18_000L,
    stock       = 12.0,
    unit        = "cup",
    imageUrl    = null,
    isActive    = isActive,
    updatedAt   = null
)

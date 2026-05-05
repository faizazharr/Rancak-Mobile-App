package id.rancak.app.presentation.ui.pos.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import id.rancak.app.domain.model.CartItem
import id.rancak.app.domain.model.Modifier as DomainModifier
import id.rancak.app.domain.model.OrderType
import id.rancak.app.presentation.designsystem.RancakTheme
import id.rancak.app.presentation.viewmodel.CartUiState
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.ImmutableMap
import kotlinx.collections.immutable.persistentMapOf

/**
 * Detailed order / cart panel used on tablet & landscape (right pane).
 * Contains header, customer & pax entry, order-type selector, item list, fee
 * breakdown, total, and save / pay actions.
 */
@Composable
internal fun OrderPanel(
    cartState: CartUiState,
    hasOpenShift: Boolean = true,
    onUpdateQty: (CartItem, Int) -> Unit,
    onUpdateNote: (CartItem, String) -> Unit,
    /** Cache modifier per-produk dari PosViewModel — key = productUuid. */
    modifierCache: ImmutableMap<String, ImmutableList<DomainModifier>> = persistentMapOf(),
    /** Dipanggil saat note dialog dibuka agar ViewModel load modifier secara lazy. */
    onLoadModifiers: (productUuid: String) -> Unit = {},
    onClearCart: () -> Unit,
    onOrderType: (OrderType) -> Unit,
    onCustomerName: (String) -> Unit,
    onPax: (Int) -> Unit,
    onDiscount: (Long, Boolean) -> Unit,
    onTax: (Long, Boolean) -> Unit,
    onAdminFee: (Long, Boolean) -> Unit,
    onDeliveryFee: (Long) -> Unit,
    onTip: (Long) -> Unit,
    onVoucherCode: (String) -> Unit,
    onSaveClick: () -> Unit,
    onCheckoutClick: () -> Unit,
    isHolding: Boolean = false,
    holdError: String? = null,
    onHoldErrorDismiss: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    val primary          = MaterialTheme.colorScheme.primary
    val surface          = MaterialTheme.colorScheme.surface
    val onSurface        = MaterialTheme.colorScheme.onSurface
    val onSurfaceVariant = MaterialTheme.colorScheme.onSurfaceVariant
    val hasItems         = cartState.items.isNotEmpty()

    Column(
        modifier = modifier
            .background(MaterialTheme.colorScheme.surface)
            .systemBarsPadding()
    ) {
        OrderPanelHeader(
            hasItems    = hasItems,
            itemCount   = cartState.itemCount,
            surface     = surface,
            primary     = primary,
            onSurface   = onSurface,
            onClearCart = onClearCart
        )

        HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(0.5f))

        OrderCustomerRow(
            cartState        = cartState,
            surface          = surface,
            primary          = primary,
            onSurface        = onSurface,
            onSurfaceVariant = onSurfaceVariant,
            onCustomerName   = onCustomerName,
            onPax            = onPax
        )

        HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(0.4f))

        OrderTypeSelector(
            selected         = cartState.orderType,
            surface          = surface,
            primary          = primary,
            onSurfaceVariant = onSurfaceVariant,
            onSelect         = onOrderType
        )

        HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(0.4f))

        OrderCartItemList(
            modifier         = Modifier.weight(1f),
            cartState        = cartState,
            primary          = primary,
            onSurfaceVariant = onSurfaceVariant,
            onUpdateQty      = onUpdateQty,
            onUpdateNote     = onUpdateNote,
            modifierCache    = modifierCache,
            onLoadModifiers  = onLoadModifiers
        )

        OrderSummaryActions(
            cartState          = cartState,
            surface            = surface,
            primary            = primary,
            onSurface          = onSurface,
            onSurfaceVariant   = onSurfaceVariant,
            hasItems           = hasItems,
            hasOpenShift       = hasOpenShift,
            isHolding          = isHolding,
            holdError          = holdError,
            onHoldErrorDismiss = onHoldErrorDismiss,
            onDiscount         = onDiscount,
            onTax              = onTax,
            onAdminFee         = onAdminFee,
            onDeliveryFee      = onDeliveryFee,
            onTip              = onTip,
            onVoucherCode      = onVoucherCode,
            onSaveClick        = onSaveClick,
            onCheckoutClick    = onCheckoutClick
        )
    }
}

// ── Previews ─────────────────────────────────────────────────────────────────

@Preview
@Composable
private fun OrderPanelPreview_Empty() {
    RancakTheme {
        OrderPanel(
            cartState       = CartUiState(),
            onUpdateQty     = { _, _ -> },
            onUpdateNote    = { _, _ -> },
            onClearCart     = {},
            onOrderType     = {},
            onCustomerName  = {},
            onPax           = {},
            onDiscount      = { _, _ -> },
            onTax           = { _, _ -> },
            onAdminFee      = { _, _ -> },
            onDeliveryFee   = {},
            onTip           = {},
            onVoucherCode   = {},
            onSaveClick     = {},
            onCheckoutClick = {}
        )
    }
}

@Preview
@Composable
private fun OrderPanelPreview_WithItems() {
    RancakTheme {
        OrderPanel(
            cartState = CartUiState(
                items = listOf(
                    CartItem(productUuid = "p1", productName = "Kopi Susu Gula Aren", qty = 2, price = 18_000L),
                    CartItem(productUuid = "p2", productName = "Croissant Cokelat",  qty = 1, price = 22_000L, note = "dihangatkan")
                ),
                customerName  = "Budi",
                pax           = 2,
                discountInput = 5_000L,
                taxInput      = 6_000L
            ),
            onUpdateQty     = { _, _ -> },
            onUpdateNote    = { _, _ -> },
            onClearCart     = {},
            onOrderType     = {},
            onCustomerName  = {},
            onPax           = {},
            onDiscount      = { _, _ -> },
            onTax           = { _, _ -> },
            onAdminFee      = { _, _ -> },
            onDeliveryFee   = {},
            onTip           = {},
            onVoucherCode   = {},
            onSaveClick     = {},
            onCheckoutClick = {}
        )
    }
}

// ─── (private helpers have been extracted to separate files) ─────────────────
// OrderPanelHeader   → OrderPanelHeader.kt
// OrderCustomerRow   → OrderCustomerRow.kt
// OrderTypeSelector  → OrderTypeSelector.kt
// OrderCartItemList  → OrderCartItemList.kt  (+ OrderItemRow, SmallQtyButton)
// OrderSummaryActions→ OrderSummaryActions.kt (+ FeeCellItem, FeeInputRow,
//                       AutoFeeRow, VoucherInputRow)


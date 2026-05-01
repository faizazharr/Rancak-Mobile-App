package id.rancak.app.presentation.ui.pos.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.NoteAdd
import androidx.compose.material.icons.automirrored.filled.Notes
import androidx.compose.material.icons.filled.AccountBalance
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.BookmarkBorder
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.DeliveryDining
import androidx.compose.material.icons.filled.Discount
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Payment
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Receipt
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material.icons.filled.Restaurant
import androidx.compose.material.icons.filled.ShoppingBag
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material.icons.filled.LocalOffer
import androidx.compose.material.icons.filled.ShoppingCartCheckout
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import id.rancak.app.domain.model.Modifier as DomainModifier
import id.rancak.app.domain.model.OrderType
import id.rancak.app.domain.model.CartItem
import id.rancak.app.presentation.components.RancakButton
import id.rancak.app.presentation.designsystem.RancakTheme
import id.rancak.app.presentation.ui.pos.FeeInputDialog
import id.rancak.app.presentation.ui.pos.feeFormatNumber
import id.rancak.app.presentation.util.formatRupiah
import id.rancak.app.presentation.viewmodel.CartUiState

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
    modifierCache: Map<String, List<DomainModifier>> = emptyMap(),
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
            .background(MaterialTheme.colorScheme.surfaceVariant.copy(0.25f))
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

        CustomerAndPaxRow(
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

        CartItemList(
            modifier         = Modifier.weight(1f),
            cartState        = cartState,
            primary          = primary,
            onSurfaceVariant = onSurfaceVariant,
            onUpdateQty      = onUpdateQty,
            onUpdateNote     = onUpdateNote,
            modifierCache    = modifierCache,
            onLoadModifiers  = onLoadModifiers
        )

        SummaryAndActions(
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

// ── Header ──────────────────────────────────────────────────────────────────

@Composable
private fun OrderPanelHeader(
    hasItems: Boolean,
    itemCount: Int,
    surface: Color,
    primary: Color,
    onSurface: Color,
    onClearCart: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(surface)
            .padding(horizontal = 14.dp, vertical = 10.dp),
        verticalAlignment     = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Row(
            verticalAlignment     = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Icon(Icons.Default.Receipt, null, Modifier.size(18.dp), tint = primary)
            Text(
                "Pesanan",
                style      = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.ExtraBold,
                color      = onSurface
            )
            if (hasItems) {
                Box(
                    Modifier
                        .clip(CircleShape)
                        .background(primary)
                        .padding(horizontal = 7.dp, vertical = 2.dp)
                ) {
                    Text(
                        "$itemCount",
                        style      = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color      = Color.White
                    )
                }
            }
        }
        if (hasItems) {
            Box(
                Modifier
                    .clip(RoundedCornerShape(6.dp))
                    .border(1.dp, MaterialTheme.colorScheme.outlineVariant, RoundedCornerShape(6.dp))
                    .clickable(onClick = onClearCart)
                    .padding(horizontal = 8.dp, vertical = 4.dp)
            ) {
                Text(
                    "Hapus Semua",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.error
                )
            }
        }
    }
}

// ── Customer + Pax ──────────────────────────────────────────────────────────

@Composable
private fun CustomerAndPaxRow(
    cartState: CartUiState,
    surface: Color,
    primary: Color,
    onSurface: Color,
    onSurfaceVariant: Color,
    onCustomerName: (String) -> Unit,
    onPax: (Int) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(surface)
            .padding(horizontal = 12.dp, vertical = 8.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment     = Alignment.CenterVertically
    ) {
        BasicTextField(
            value         = cartState.customerName,
            onValueChange = onCustomerName,
            singleLine    = true,
            textStyle     = MaterialTheme.typography.bodySmall.copy(
                color    = onSurface,
                fontSize = 13.sp
            ),
            cursorBrush = SolidColor(primary),
            modifier    = Modifier.weight(1f),
            decorationBox = { inner ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(8.dp))
                        .background(MaterialTheme.colorScheme.surfaceVariant.copy(0.5f))
                        .border(
                            1.dp,
                            MaterialTheme.colorScheme.outlineVariant.copy(0.5f),
                            RoundedCornerShape(8.dp)
                        )
                        .padding(horizontal = 10.dp, vertical = 8.dp),
                    verticalAlignment     = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Icon(Icons.Default.Person, null, Modifier.size(14.dp), tint = onSurfaceVariant)
                    Box(Modifier.weight(1f)) {
                        if (cartState.customerName.isEmpty()) {
                            Text(
                                "Nama customer",
                                style    = MaterialTheme.typography.bodySmall,
                                fontSize = 13.sp,
                                color    = onSurfaceVariant.copy(0.5f)
                            )
                        }
                        inner()
                    }
                }
            }
        )

        Row(
            modifier = Modifier
                .clip(RoundedCornerShape(8.dp))
                .background(MaterialTheme.colorScheme.surfaceVariant.copy(0.5f))
                .border(
                    1.dp,
                    MaterialTheme.colorScheme.outlineVariant.copy(0.5f),
                    RoundedCornerShape(8.dp)
                )
                .padding(horizontal = 6.dp, vertical = 6.dp),
            verticalAlignment     = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Box(
                Modifier
                    .size(22.dp)
                    .clip(CircleShape)
                    .background(
                        if (cartState.pax > 1)
                            MaterialTheme.colorScheme.outlineVariant.copy(0.35f)
                        else Color.Transparent
                    )
                    .clickable(enabled = cartState.pax > 1) { onPax(cartState.pax - 1) },
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    Icons.Default.Remove, null,
                    Modifier.size(12.dp),
                    tint = if (cartState.pax > 1) onSurface else onSurfaceVariant.copy(0.3f)
                )
            }
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    "${cartState.pax}",
                    style      = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.Bold,
                    color      = onSurface,
                    modifier   = Modifier.widthIn(min = 18.dp),
                    textAlign  = TextAlign.Center
                )
                Text(
                    "tamu",
                    style    = MaterialTheme.typography.labelSmall,
                    fontSize = 9.sp,
                    color    = onSurfaceVariant.copy(0.6f)
                )
            }
            Box(
                Modifier
                    .size(22.dp)
                    .clip(CircleShape)
                    .background(primary)
                    .clickable { onPax(cartState.pax + 1) },
                contentAlignment = Alignment.Center
            ) {
                Icon(Icons.Default.Add, null, Modifier.size(12.dp), tint = Color.White)
            }
        }
    }
}

// ── Order-type selector ─────────────────────────────────────────────────────

@Composable
private fun OrderTypeSelector(
    selected: OrderType,
    surface: Color,
    primary: Color,
    onSurfaceVariant: Color,
    onSelect: (OrderType) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(surface)
            .padding(horizontal = 10.dp, vertical = 8.dp),
        horizontalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        OrderType.entries.forEach { type ->
            val isSelected = selected == type
            val (icon, label) = when (type) {
                OrderType.DINE_IN  -> Icons.Default.Restaurant   to "Dine In"
                OrderType.TAKEAWAY -> Icons.Default.ShoppingBag  to "Takeaway"
                OrderType.DELIVERY -> Icons.Default.DeliveryDining to "Delivery"
            }
            Box(
                modifier = Modifier
                    .weight(1f)
                    .clip(RoundedCornerShape(8.dp))
                    .background(if (isSelected) primary else surface)
                    .border(
                        1.dp,
                        if (isSelected) Color.Transparent
                        else MaterialTheme.colorScheme.outlineVariant,
                        RoundedCornerShape(8.dp)
                    )
                    .clickable { onSelect(type) }
                    .padding(vertical = 7.dp),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(2.dp)
                ) {
                    Icon(
                        icon, null,
                        Modifier.size(16.dp),
                        tint = if (isSelected) Color.White else onSurfaceVariant
                    )
                    Text(
                        label,
                        style      = MaterialTheme.typography.labelSmall,
                        fontSize   = 10.sp,
                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                        color      = if (isSelected) Color.White else onSurfaceVariant
                    )
                }
            }
        }
    }
}

// ── Cart item list ──────────────────────────────────────────────────────────

@Composable
private fun CartItemList(
    modifier: Modifier,
    cartState: CartUiState,
    primary: Color,
    onSurfaceVariant: Color,
    onUpdateQty: (CartItem, Int) -> Unit,
    onUpdateNote: (CartItem, String) -> Unit,
    modifierCache: Map<String, List<DomainModifier>> = emptyMap(),
    onLoadModifiers: (productUuid: String) -> Unit = {}
) {
    if (cartState.items.isEmpty()) {
        Box(
            modifier.fillMaxWidth(),
            contentAlignment = Alignment.Center
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Icon(
                    Icons.Default.ShoppingCartCheckout, null,
                    Modifier.size(44.dp),
                    tint = MaterialTheme.colorScheme.outlineVariant
                )
                Text(
                    "Belum ada pesanan",
                    style = MaterialTheme.typography.bodySmall,
                    color = onSurfaceVariant
                )
                Text(
                    "Tap produk untuk menambahkan",
                    style = MaterialTheme.typography.labelSmall,
                    color = onSurfaceVariant.copy(0.6f)
                )
            }
        }
    } else {
        LazyColumn(
            modifier       = modifier,
            contentPadding = PaddingValues(vertical = 4.dp)
        ) {
            items(
                cartState.items,
                key = { "${it.productUuid}_${it.variantUuid}" }
            ) { item ->
                OrderItemRow(
                    item            = item,
                    primary         = primary,
                    modifiers       = modifierCache[item.productUuid] ?: emptyList(),
                    onLoadModifiers = { onLoadModifiers(item.productUuid) },
                    onIncrease      = { onUpdateQty(item, item.qty + 1) },
                    onDecrease      = { onUpdateQty(item, item.qty - 1) },
                    onSetQty        = { onUpdateQty(item, it) },
                    onSetNote       = { onUpdateNote(item, it) }
                )
                HorizontalDivider(
                    modifier = Modifier.padding(horizontal = 12.dp),
                    color    = MaterialTheme.colorScheme.outlineVariant.copy(0.3f)
                )
            }
        }
    }
}

// ── Summary & Actions ───────────────────────────────────────────────────────

@Composable
private fun SummaryAndActions(
    cartState: CartUiState,
    surface: Color,
    primary: Color,
    onSurface: Color,
    onSurfaceVariant: Color,
    hasItems: Boolean,
    hasOpenShift: Boolean,
    isHolding: Boolean = false,
    holdError: String? = null,
    onHoldErrorDismiss: () -> Unit = {},
    onDiscount: (Long, Boolean) -> Unit,
    onTax: (Long, Boolean) -> Unit,
    onAdminFee: (Long, Boolean) -> Unit,
    onDeliveryFee: (Long) -> Unit,
    onTip: (Long) -> Unit,
    onVoucherCode: (String) -> Unit,
    onSaveClick: () -> Unit,
    onCheckoutClick: () -> Unit
) {
    Surface(shadowElevation = 8.dp, color = surface) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 14.dp, vertical = 10.dp)
        ) {
            Row(
                Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text("Subtotal", style = MaterialTheme.typography.bodySmall, color = onSurfaceVariant)
                Text(
                    formatRupiah(cartState.subtotal),
                    style      = MaterialTheme.typography.bodySmall,
                    fontWeight = FontWeight.SemiBold,
                    color      = onSurface
                )
            }

            Spacer(Modifier.height(6.dp))

            FeeInputRow(
                label             = "Diskon",
                icon              = Icons.Default.Discount,
                value             = cartState.discountInput,
                onValue           = onDiscount,
                isNegative        = true,
                showPercentToggle = true,
                valueIsPercent    = cartState.discountIsPercent,
                computedAmount    = cartState.discount
            )
            FeeInputRow(
                label             = "Pajak",
                icon              = Icons.Default.AccountBalance,
                value             = cartState.taxInput,
                onValue           = onTax,
                showPercentToggle = true,
                valueIsPercent    = cartState.taxIsPercent,
                computedAmount    = cartState.tax
            )
            // Pajak otomatis dari konfigurasi Pricing (read-only).
            cartState.activeTaxConfigs.forEach { cfg ->
                AutoFeeRow(
                    label   = "${cfg.name} (${cfg.rate}%)",
                    amount  = run {
                        val basis = if (cfg.applyTo == "subtotal") cartState.subtotal
                                    else (cartState.subtotal - cartState.discount).coerceAtLeast(0L)
                        ((basis * (cfg.rate * 100).toLong()) / 10_000L).coerceAtLeast(0L)
                    },
                    onSurfaceVariant = onSurfaceVariant,
                    onSurface = onSurface
                )
            }
            FeeInputRow(
                label             = "Biaya Admin",
                icon              = Icons.Default.Receipt,
                value             = cartState.adminFeeInput,
                onValue           = onAdminFee,
                showPercentToggle = true,
                valueIsPercent    = cartState.adminFeeIsPercent,
                computedAmount    = cartState.adminFee
            )
            // Surcharge otomatis dari konfigurasi Pricing (read-only).
            cartState.activeSurcharges.forEach { sc ->
                val raw = if (sc.isPercentage) {
                    val basis = (cartState.subtotal - cartState.discount).coerceAtLeast(0L)
                    (basis * sc.amount / 100L).coerceAtLeast(0L)
                } else sc.amount
                val amt = sc.maxAmount?.let { cap -> raw.coerceAtMost(cap) } ?: raw
                AutoFeeRow(
                    label   = sc.name + if (sc.isPercentage) " (${sc.amount}%)" else "",
                    amount  = amt,
                    onSurfaceVariant = onSurfaceVariant,
                    onSurface = onSurface
                )
            }
            if (cartState.orderType == OrderType.DELIVERY) {
                FeeInputRow(
                    label   = "Ongkir",
                    icon    = Icons.Default.DeliveryDining,
                    value   = cartState.deliveryFee,
                    onValue = { v, _ -> onDeliveryFee(v) }
                )
            }
            FeeInputRow(
                label   = "Tip",
                icon    = Icons.Default.Favorite,
                value   = cartState.tip,
                onValue = { v, _ -> onTip(v) }
            )

            VoucherInputRow(
                value   = cartState.voucherCode,
                primary = primary,
                onApply = onVoucherCode
            )

            Spacer(Modifier.height(4.dp))
            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(0.5f))
            Spacer(Modifier.height(8.dp))

            // ── Total card ────────────────────────────────────────────────
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(10.dp))
                    .background(primary.copy(alpha = 0.07f))
                    .padding(horizontal = 12.dp, vertical = 10.dp)
            ) {
                Row(
                    Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment     = Alignment.CenterVertically
                ) {
                    Text(
                        "TOTAL",
                        style      = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.SemiBold,
                        color      = onSurface.copy(alpha = 0.55f)
                    )
                    Text(
                        formatRupiah(cartState.total),
                        style      = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.ExtraBold,
                        color      = primary
                    )
                }
            }

            Spacer(Modifier.height(10.dp))

            // ── Hold error chip ───────────────────────────────────────────
            if (holdError != null) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(8.dp))
                        .background(MaterialTheme.colorScheme.errorContainer)
                        .clickable(onClick = onHoldErrorDismiss)
                        .padding(horizontal = 10.dp, vertical = 7.dp)
                ) {
                    Row(
                        modifier              = Modifier.fillMaxWidth(),
                        verticalAlignment     = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            holdError,
                            style    = MaterialTheme.typography.labelSmall,
                            color    = MaterialTheme.colorScheme.onErrorContainer,
                            modifier = Modifier.weight(1f)
                        )
                        Text(
                            "✕",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onErrorContainer
                        )
                    }
                }
                Spacer(Modifier.height(6.dp))
            }

            Row(
                Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // ── Open Bill button ──────────────────────────────────────
                Box(
                    modifier = Modifier
                        .weight(0.38f)
                        .clip(RoundedCornerShape(10.dp))
                        .background(
                            if (hasItems) Color(0xFFF59E0B).copy(alpha = 0.15f)
                            else MaterialTheme.colorScheme.surfaceVariant.copy(0.4f)
                        )
                        .border(
                            1.5.dp,
                            if (hasItems && !isHolding) Color(0xFFF59E0B)
                            else MaterialTheme.colorScheme.outlineVariant,
                            RoundedCornerShape(10.dp)
                        )
                        .clickable(enabled = hasItems && !isHolding, onClick = onSaveClick)
                        .padding(vertical = 12.dp),
                    contentAlignment = Alignment.Center
                ) {
                    if (isHolding) {
                        CircularProgressIndicator(
                            modifier  = Modifier.size(16.dp),
                            color     = Color(0xFFF59E0B),
                            strokeWidth = 2.dp
                        )
                    } else {
                        val isUpdate = cartState.activeOpenBillId != null
                        Row(
                            verticalAlignment     = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(5.dp)
                        ) {
                            Icon(
                                Icons.Default.BookmarkBorder, null,
                                Modifier.size(15.dp),
                                tint = if (hasItems) Color(0xFFF59E0B) else onSurfaceVariant.copy(0.4f)
                            )
                            Text(
                                if (isUpdate) "Perbarui" else "Open Bill",
                                style      = MaterialTheme.typography.labelLarge,
                                fontWeight = FontWeight.Bold,
                                color      = if (hasItems) Color(0xFFF59E0B) else onSurfaceVariant.copy(0.4f)
                            )
                        }
                    }
                }

                Box(
                    modifier = Modifier
                        .weight(0.62f)
                        .clip(RoundedCornerShape(10.dp))
                        .background(
                            when {
                                !hasItems         -> MaterialTheme.colorScheme.outlineVariant.copy(0.4f)
                                !hasOpenShift     -> MaterialTheme.colorScheme.errorContainer
                                else              -> primary
                            }
                        )
                        .clickable(enabled = hasItems && hasOpenShift, onClick = onCheckoutClick)
                        .padding(vertical = 12.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Row(
                        verticalAlignment     = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(5.dp)
                    ) {
                        Icon(
                            Icons.Default.Payment, null,
                            Modifier.size(16.dp),
                            tint = when {
                                !hasItems     -> onSurfaceVariant
                                !hasOpenShift -> MaterialTheme.colorScheme.onErrorContainer
                                else          -> Color.White
                            }
                        )
                        Text(
                            when {
                                !hasItems     -> "Pilih Produk"
                                !hasOpenShift -> "Buka Shift Dulu"
                                else          -> "Bayar ${formatRupiah(cartState.total)}"
                            },
                            style      = MaterialTheme.typography.labelLarge,
                            fontWeight = FontWeight.ExtraBold,
                            color      = when {
                                !hasItems     -> onSurfaceVariant
                                !hasOpenShift -> MaterialTheme.colorScheme.onErrorContainer
                                else          -> Color.White
                            },
                            maxLines   = 1,
                            overflow   = TextOverflow.Ellipsis
                        )
                    }
                }
            }
        }
    }
}

// ── VoucherInputRow ──────────────────────────────────────────────────────────

@Composable
private fun VoucherInputRow(
    value: String,
    primary: Color,
    onApply: (String) -> Unit
) {
    var text by remember(value) { mutableStateOf(value) }
    val keyboard = LocalSoftwareKeyboardController.current
    val applied  = value.isNotBlank() && value == text

    Spacer(Modifier.height(4.dp))
    Row(
        modifier              = Modifier.fillMaxWidth(),
        verticalAlignment     = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        Icon(
            Icons.Default.LocalOffer, null,
            Modifier.size(15.dp),
            tint = if (applied) primary else MaterialTheme.colorScheme.onSurfaceVariant
        )
        // Text field
        Box(
            modifier = Modifier
                .weight(1f)
                .clip(RoundedCornerShape(8.dp))
                .background(MaterialTheme.colorScheme.surfaceVariant.copy(0.6f))
                .border(
                    1.dp,
                    if (applied) primary.copy(0.6f) else MaterialTheme.colorScheme.outlineVariant,
                    RoundedCornerShape(8.dp)
                )
                .padding(horizontal = 10.dp, vertical = 7.dp)
        ) {
            if (text.isEmpty()) {
                Text(
                    "Kode voucher",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(0.45f)
                )
            }
            androidx.compose.foundation.text.BasicTextField(
                value         = text,
                onValueChange = {
                    text = it.uppercase().trim()
                    if (it.isBlank()) onApply("")   // clear immediately when user erases
                },
                singleLine    = true,
                textStyle     = MaterialTheme.typography.bodySmall.copy(
                    color      = if (applied) primary else MaterialTheme.colorScheme.onSurface,
                    fontWeight = if (applied) FontWeight.Bold else FontWeight.Normal
                ),
                cursorBrush   = SolidColor(primary),
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
                keyboardActions = KeyboardActions(onDone = {
                    if (text.isNotBlank()) onApply(text)
                    keyboard?.hide()
                })
            )
        }
        // Apply / Clear button
        Box(
            modifier = Modifier
                .clip(RoundedCornerShape(8.dp))
                .background(
                    if (applied) MaterialTheme.colorScheme.errorContainer
                    else if (text.isNotBlank()) primary
                    else MaterialTheme.colorScheme.outlineVariant.copy(0.4f)
                )
                .clickable(enabled = text.isNotBlank()) {
                    if (applied) {
                        text = ""
                        onApply("")
                    } else {
                        onApply(text)
                        keyboard?.hide()
                    }
                }
                .padding(horizontal = 10.dp, vertical = 7.dp)
        ) {
            Text(
                if (applied) "Hapus" else "Pakai",
                style      = MaterialTheme.typography.labelSmall,
                fontWeight = FontWeight.Bold,
                color      = when {
                    applied          -> MaterialTheme.colorScheme.onErrorContainer
                    text.isNotBlank() -> Color.White
                    else             -> MaterialTheme.colorScheme.onSurfaceVariant.copy(0.4f)
                }
            )
        }
    }
    if (applied) {
        Spacer(Modifier.height(2.dp))
        Row(
            horizontalArrangement = Arrangement.spacedBy(3.dp),
            verticalAlignment     = Alignment.CenterVertically,
            modifier              = Modifier.padding(start = 21.dp)
        ) {
            Icon(
                Icons.Default.LocalOffer, null,
                Modifier.size(10.dp),
                tint = primary.copy(0.7f)
            )
            Text(
                "Voucher \"$value\" diterapkan",
                style = MaterialTheme.typography.labelSmall,
                color = primary.copy(0.85f)
            )
        }
    }
    Spacer(Modifier.height(4.dp))
}

// ── AutoFeeRow ──────────────────────────────────────────────────────────────
// Baris read-only untuk pajak/surcharge yang otomatis diaplikasikan dari
// konfigurasi Pricing.

@Composable
private fun AutoFeeRow(
    label: String,
    amount: Long,
    onSurfaceVariant: androidx.compose.ui.graphics.Color,
    onSurface: androidx.compose.ui.graphics.Color
) {
    Row(
        Modifier.fillMaxWidth().padding(vertical = 2.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment     = Alignment.CenterVertically
    ) {
        Text(
            label,
            style = MaterialTheme.typography.bodySmall,
            color = onSurfaceVariant
        )
        Text(
            formatRupiah(amount),
            style      = MaterialTheme.typography.bodySmall,
            fontWeight = FontWeight.Medium,
            color      = onSurface
        )
    }
}

// ── FeeInputRow ─────────────────────────────────────────────────────────────

@Composable
private fun FeeInputRow(
    label:             String,
    icon:              ImageVector,
    value:             Long,
    onValue:           (Long, Boolean) -> Unit,
    isNegative:        Boolean = false,
    showPercentToggle: Boolean = false,
    valueIsPercent:    Boolean = false,
    /** Nominal Rp hasil hitung (dipakai saat valueIsPercent = true untuk tampilkan Rp di samping %). */
    computedAmount:    Long    = 0L
) {
    val onSurfaceVariant = MaterialTheme.colorScheme.onSurfaceVariant
    val primary          = MaterialTheme.colorScheme.primary
    val error            = MaterialTheme.colorScheme.error

    var showDialog by remember { mutableStateOf(false) }

    if (showDialog) {
        FeeInputDialog(
            title             = label,
            icon              = icon,
            initialValue      = value,
            isNegative        = isNegative,
            showPercentToggle = showPercentToggle,
            initialIsPercent  = valueIsPercent,
            onDismiss         = { showDialog = false },
            onConfirm         = { amount, isPercent ->
                onValue(amount, isPercent)
                showDialog = false
            }
        )
    }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(8.dp))
            .clickable { showDialog = true }
            .padding(vertical = 5.dp, horizontal = 2.dp),
        verticalAlignment     = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Row(
            verticalAlignment     = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(4.dp),
            modifier              = Modifier.weight(1f)
        ) {
            Icon(icon, null, Modifier.size(12.dp), tint = onSurfaceVariant.copy(0.6f))
            Text(label, style = MaterialTheme.typography.labelSmall, color = onSurfaceVariant)
        }

        Row(
            verticalAlignment     = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(2.dp)
        ) {
            if (value > 0L) {
                if (isNegative) {
                    Text(
                        "−", style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.SemiBold, color = error
                    )
                }
                val valueColor = if (isNegative) error else primary
                if (valueIsPercent) {
                    // Tampilkan "%  ·  Rp nominal"
                    Text(
                        "${value}%",
                        style      = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.SemiBold,
                        color      = valueColor
                    )
                    Text(
                        "·",
                        style = MaterialTheme.typography.labelSmall,
                        color = onSurfaceVariant.copy(0.4f)
                    )
                    Text(
                        "Rp ${feeFormatNumber(computedAmount)}",
                        style = MaterialTheme.typography.labelSmall,
                        color = onSurfaceVariant.copy(0.75f)
                    )
                } else {
                    Text(
                        "Rp ${feeFormatNumber(value)}",
                        style      = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.SemiBold,
                        color      = valueColor
                    )
                }
            } else {
                Text(
                    "Ketuk untuk isi",
                    style = MaterialTheme.typography.labelSmall,
                    color = onSurfaceVariant.copy(0.35f)
                )
            }
            Icon(
                Icons.Default.ChevronRight, null,
                Modifier.size(12.dp),
                tint = onSurfaceVariant.copy(0.30f)
            )
        }
    }
}

// ── OrderItemRow + SmallQtyButton ───────────────────────────────────────────

@Composable
private fun OrderItemRow(
    item: CartItem,
    primary: Color,
    modifiers: List<DomainModifier> = emptyList(),
    onLoadModifiers: () -> Unit = {},
    onIncrease: () -> Unit,
    onDecrease: () -> Unit,
    onSetQty: (Int) -> Unit,
    onSetNote: (String) -> Unit
) {
    val accent           = accentFor(item.productName)
    val onSurface        = MaterialTheme.colorScheme.onSurface
    val onSurfaceVariant = MaterialTheme.colorScheme.onSurfaceVariant

    var showQtyDialog  by remember { mutableStateOf(false) }
    var showNoteDialog by remember { mutableStateOf(false) }
    var noteText       by remember(item.note) { mutableStateOf(item.note ?: "") }

    // Load modifier saat dialog note akan dibuka — lazy, sekali per produk
    LaunchedEffect(showNoteDialog) {
        if (showNoteDialog) onLoadModifiers()
    }

    if (showQtyDialog) {
        FeeInputDialog(
            title        = item.productName,
            icon         = Icons.Default.ShoppingCart,
            initialValue = item.qty.toLong(),
            prefix       = "",
            onDismiss    = { showQtyDialog = false },
            onConfirm    = { qty, _ ->
                onSetQty(qty.toInt().coerceAtLeast(0))
                showQtyDialog = false
            }
        )
    }

    if (showNoteDialog) {
        AlertDialog(
            onDismissRequest = { showNoteDialog = false },
            title = {
                Text(
                    "Catatan — ${item.productName}",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold
                )
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    // Modifier chips — hanya tampil jika ada modifier aktif
                    val activeModifiers = modifiers.filter { it.isActive }
                    if (activeModifiers.isNotEmpty()) {
                        Text(
                            "Preset cepat",
                            style = MaterialTheme.typography.labelSmall,
                            color = onSurfaceVariant
                        )
                        // Bungkus chips dalam flow-row manual menggunakan FlowRow-style wrap
                        // via wrapping Row di dalam Column
                        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                            activeModifiers.chunked(3).forEach { rowModifiers ->
                                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                    rowModifiers.forEach { mod ->
                                        // Toggle: jika modifier sudah ada di note, hapus; jika belum, append
                                        val isSelected = noteText.split(", ")
                                            .map { it.trim() }
                                            .contains(mod.name)
                                        Surface(
                                            onClick = {
                                                noteText = if (isSelected) {
                                                    noteText.split(", ")
                                                        .map { it.trim() }
                                                        .filter { it.isNotBlank() && it != mod.name }
                                                        .joinToString(", ")
                                                } else {
                                                    val parts = noteText.split(", ")
                                                        .map { it.trim() }
                                                        .filter { it.isNotBlank() }
                                                    (parts + mod.name).joinToString(", ")
                                                }
                                            },
                                            shape = RoundedCornerShape(20.dp),
                                            color = if (isSelected) primary
                                                    else MaterialTheme.colorScheme.surfaceVariant,
                                            tonalElevation = if (isSelected) 0.dp else 1.dp
                                        ) {
                                            Text(
                                                text = mod.name,
                                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp),
                                                style = MaterialTheme.typography.labelSmall,
                                                color = if (isSelected) MaterialTheme.colorScheme.onPrimary
                                                        else onSurfaceVariant,
                                                fontWeight = if (isSelected) FontWeight.SemiBold
                                                             else FontWeight.Normal
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                    OutlinedTextField(
                        value         = noteText,
                        onValueChange = { noteText = it },
                        label         = { Text("Contoh: gula sedikit, tambah es") },
                        shape         = RoundedCornerShape(12.dp),
                        modifier      = Modifier.fillMaxWidth(),
                        maxLines      = 3
                    )
                }
            },
            confirmButton = {
                TextButton(onClick = {
                    onSetNote(noteText.trim())
                    showNoteDialog = false
                }) { Text("Simpan", fontWeight = FontWeight.Bold) }
            },
            dismissButton = {
                TextButton(onClick = { showNoteDialog = false }) { Text("Batal") }
            }
        )
    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 12.dp, vertical = 8.dp)
    ) {
        Row(
            verticalAlignment     = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(32.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(accent.copy(0.14f))
                    .border(1.dp, accent.copy(0.32f), RoundedCornerShape(8.dp)),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    item.productName.firstOrNull()?.uppercaseChar()?.toString() ?: "?",
                    style      = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.ExtraBold,
                    color      = accent
                )
            }

            Column(Modifier.weight(1f)) {
                Text(
                    item.productName + (item.variantName?.let { " · $it" } ?: ""),
                    style      = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.SemiBold,
                    color      = onSurface,
                    maxLines   = 1,
                    overflow   = TextOverflow.Ellipsis
                )
                Text(
                    formatRupiah(item.price),
                    style = MaterialTheme.typography.labelSmall,
                    color = onSurfaceVariant
                )
            }

            Row(
                verticalAlignment     = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                SmallQtyButton(Icons.Default.Remove, onClick = onDecrease)

                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(6.dp))
                        .background(MaterialTheme.colorScheme.surfaceVariant)
                        .clickable { showQtyDialog = true }
                        .padding(horizontal = 8.dp, vertical = 3.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        "${item.qty}",
                        style      = MaterialTheme.typography.labelLarge,
                        fontWeight = FontWeight.ExtraBold,
                        color      = primary,
                        textAlign  = TextAlign.Center
                    )
                }

                SmallQtyButton(Icons.Default.Add, tint = primary, onClick = onIncrease)
            }

            Column(horizontalAlignment = Alignment.End) {
                Text(
                    formatRupiah(item.subtotal),
                    style      = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.Bold,
                    color      = primary
                )
                Icon(
                    if (item.note.isNullOrBlank()) Icons.AutoMirrored.Filled.NoteAdd
                    else Icons.Default.Edit,
                    contentDescription = "Catatan",
                    modifier = Modifier
                        .size(14.dp)
                        .clickable { showNoteDialog = true },
                    tint = if (item.note.isNullOrBlank())
                        onSurfaceVariant.copy(0.35f)
                    else primary.copy(0.75f)
                )
            }
        }

        if (!item.note.isNullOrBlank()) {
            Row(
                modifier              = Modifier.padding(start = 42.dp, top = 3.dp),
                verticalAlignment     = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Icon(
                    Icons.AutoMirrored.Filled.Notes, null,
                    Modifier.size(11.dp),
                    tint = primary.copy(0.55f)
                )
                Text(
                    item.note,
                    style    = MaterialTheme.typography.labelSmall,
                    color    = onSurfaceVariant.copy(0.75f),
                    fontStyle = FontStyle.Italic,
                    maxLines  = 1,
                    overflow  = TextOverflow.Ellipsis,
                    fontSize  = 10.sp
                )
            }
        }
    }
}

@Composable
private fun SmallQtyButton(
    icon: ImageVector,
    tint: Color = MaterialTheme.colorScheme.onSurfaceVariant,
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .size(24.dp)
            .clip(CircleShape)
            .border(1.dp, MaterialTheme.colorScheme.outlineVariant, CircleShape)
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        Icon(icon, null, Modifier.size(13.dp), tint = tint)
    }
}

// ── Previews ────────────────────────────────────────────────────────────────

@Preview
@Composable
private fun OrderPanelPreview_Empty() {
    RancakTheme {
        OrderPanel(
            cartState      = CartUiState(),
            onUpdateQty    = { _, _ -> },
            onUpdateNote   = { _, _ -> },
            onClearCart    = {},
            onOrderType    = {},
            onCustomerName = {},
            onPax          = {},
            onDiscount     = { _, _ -> },
            onTax          = { _, _ -> },
            onAdminFee     = { _, _ -> },
            onDeliveryFee  = {},
            onTip          = {},
            onVoucherCode  = {},
            onSaveClick    = {},
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
                    CartItem(
                        productUuid = "p1",
                        productName = "Kopi Susu Gula Aren",
                        qty         = 2,
                        price       = 18_000L
                    ),
                    CartItem(
                        productUuid = "p2",
                        productName = "Croissant Cokelat",
                        qty         = 1,
                        price       = 22_000L,
                        note        = "dihangatkan"
                    )
                ),
                customerName  = "Budi",
                pax           = 2,
                discountInput = 5_000L,
                taxInput      = 6_000L
            ),
            onUpdateQty    = { _, _ -> },
            onUpdateNote   = { _, _ -> },
            onClearCart    = {},
            onOrderType    = {},
            onCustomerName = {},
            onPax          = {},
            onDiscount     = { _, _ -> },
            onTax          = { _, _ -> },
            onAdminFee     = { _, _ -> },
            onDeliveryFee  = {},
            onTip          = {},
            onVoucherCode  = {},
            onSaveClick    = {},
            onCheckoutClick = {}
        )
    }
}

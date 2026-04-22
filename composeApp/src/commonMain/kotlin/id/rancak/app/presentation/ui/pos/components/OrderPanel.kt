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
import androidx.compose.material.icons.filled.ShoppingCartCheckout
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
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
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import id.rancak.app.domain.model.OrderType
import id.rancak.app.domain.repository.CartItem
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
    onUpdateQty: (CartItem, Int) -> Unit,
    onUpdateNote: (CartItem, String) -> Unit,
    onClearCart: () -> Unit,
    onOrderType: (OrderType) -> Unit,
    onCustomerName: (String) -> Unit,
    onPax: (Int) -> Unit,
    onDiscount: (Long) -> Unit,
    onTax: (Long) -> Unit,
    onAdminFee: (Long) -> Unit,
    onDeliveryFee: (Long) -> Unit,
    onTip: (Long) -> Unit,
    @Suppress("UNUSED_PARAMETER") onVoucherCode: (String) -> Unit,
    onSaveClick: () -> Unit,
    onCheckoutClick: () -> Unit,
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
            onUpdateNote     = onUpdateNote
        )

        SummaryAndActions(
            cartState      = cartState,
            surface        = surface,
            primary        = primary,
            onSurface      = onSurface,
            onSurfaceVariant = onSurfaceVariant,
            hasItems       = hasItems,
            onDiscount     = onDiscount,
            onTax          = onTax,
            onAdminFee     = onAdminFee,
            onDeliveryFee  = onDeliveryFee,
            onTip          = onTip,
            onSaveClick    = onSaveClick,
            onCheckoutClick = onCheckoutClick
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
    onUpdateNote: (CartItem, String) -> Unit
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
                    item       = item,
                    primary    = primary,
                    onIncrease = { onUpdateQty(item, item.qty + 1) },
                    onDecrease = { onUpdateQty(item, item.qty - 1) },
                    onSetQty   = { onUpdateQty(item, it) },
                    onSetNote  = { onUpdateNote(item, it) }
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
    onDiscount: (Long) -> Unit,
    onTax: (Long) -> Unit,
    onAdminFee: (Long) -> Unit,
    onDeliveryFee: (Long) -> Unit,
    onTip: (Long) -> Unit,
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
                label      = "Diskon",
                icon       = Icons.Default.Discount,
                value      = cartState.discount,
                onValue    = onDiscount,
                isNegative = true
            )
            FeeInputRow(
                label   = "Pajak",
                icon    = Icons.Default.AccountBalance,
                value   = cartState.tax,
                onValue = onTax
            )
            FeeInputRow(
                label   = "Biaya Admin",
                icon    = Icons.Default.Receipt,
                value   = cartState.adminFee,
                onValue = onAdminFee
            )
            if (cartState.orderType == OrderType.DELIVERY) {
                FeeInputRow(
                    label   = "Ongkir",
                    icon    = Icons.Default.DeliveryDining,
                    value   = cartState.deliveryFee,
                    onValue = onDeliveryFee
                )
            }
            FeeInputRow(
                label   = "Tip",
                icon    = Icons.Default.Favorite,
                value   = cartState.tip,
                onValue = onTip
            )

            Spacer(Modifier.height(4.dp))
            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(0.5f))
            Spacer(Modifier.height(8.dp))

            Row(
                Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment     = Alignment.CenterVertically
            ) {
                Text(
                    "TOTAL",
                    style      = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.ExtraBold,
                    color      = onSurface
                )
                Text(
                    formatRupiah(cartState.total),
                    style      = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.ExtraBold,
                    color      = primary
                )
            }

            Spacer(Modifier.height(10.dp))

            Row(
                Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Box(
                    modifier = Modifier
                        .weight(0.38f)
                        .clip(RoundedCornerShape(10.dp))
                        .background(surface)
                        .border(
                            1.5.dp,
                            if (hasItems) primary
                            else MaterialTheme.colorScheme.outlineVariant,
                            RoundedCornerShape(10.dp)
                        )
                        .clickable(enabled = hasItems, onClick = onSaveClick)
                        .padding(vertical = 12.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Row(
                        verticalAlignment     = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(5.dp)
                    ) {
                        Icon(
                            Icons.Default.BookmarkBorder, null,
                            Modifier.size(15.dp),
                            tint = if (hasItems) primary else onSurfaceVariant.copy(0.4f)
                        )
                        Text(
                            "Simpan",
                            style      = MaterialTheme.typography.labelLarge,
                            fontWeight = FontWeight.Bold,
                            color      = if (hasItems) primary else onSurfaceVariant.copy(0.4f)
                        )
                    }
                }

                Box(
                    modifier = Modifier
                        .weight(0.62f)
                        .clip(RoundedCornerShape(10.dp))
                        .background(
                            if (hasItems) primary
                            else MaterialTheme.colorScheme.outlineVariant.copy(0.4f)
                        )
                        .clickable(enabled = hasItems, onClick = onCheckoutClick)
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
                            tint = if (hasItems) Color.White else onSurfaceVariant
                        )
                        Text(
                            if (hasItems) "Bayar ${formatRupiah(cartState.total)}"
                            else "Pilih Produk",
                            style      = MaterialTheme.typography.labelLarge,
                            fontWeight = FontWeight.ExtraBold,
                            color      = if (hasItems) Color.White else onSurfaceVariant,
                            maxLines   = 1,
                            overflow   = TextOverflow.Ellipsis
                        )
                    }
                }
            }
        }
    }
}

// ── FeeInputRow ─────────────────────────────────────────────────────────────

@Composable
private fun FeeInputRow(
    label:      String,
    icon:       ImageVector,
    value:      Long,
    onValue:    (Long) -> Unit,
    isNegative: Boolean = false
) {
    val onSurfaceVariant = MaterialTheme.colorScheme.onSurfaceVariant
    val primary          = MaterialTheme.colorScheme.primary
    val error            = MaterialTheme.colorScheme.error

    var showDialog by remember { mutableStateOf(false) }

    if (showDialog) {
        FeeInputDialog(
            title        = label,
            icon         = icon,
            initialValue = value,
            isNegative   = isNegative,
            onDismiss    = { showDialog = false },
            onConfirm    = { amount ->
                onValue(amount)
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
                Text(
                    "Rp ${feeFormatNumber(value)}",
                    style      = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.SemiBold,
                    color      = if (isNegative) error else primary
                )
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

    if (showQtyDialog) {
        FeeInputDialog(
            title        = item.productName,
            icon         = Icons.Default.ShoppingCart,
            initialValue = item.qty.toLong(),
            prefix       = "",
            onDismiss    = { showQtyDialog = false },
            onConfirm    = { qty ->
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
                OutlinedTextField(
                    value         = noteText,
                    onValueChange = { noteText = it },
                    label         = { Text("Contoh: gula sedikit, tambah es") },
                    shape         = RoundedCornerShape(12.dp),
                    modifier      = Modifier.fillMaxWidth(),
                    maxLines      = 3
                )
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
            onDiscount     = {},
            onTax          = {},
            onAdminFee     = {},
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
                customerName = "Budi",
                pax          = 2,
                discount     = 5_000L,
                tax          = 6_000L
            ),
            onUpdateQty    = { _, _ -> },
            onUpdateNote   = { _, _ -> },
            onClearCart    = {},
            onOrderType    = {},
            onCustomerName = {},
            onPax          = {},
            onDiscount     = {},
            onTax          = {},
            onAdminFee     = {},
            onDeliveryFee  = {},
            onTip          = {},
            onVoucherCode  = {},
            onSaveClick    = {},
            onCheckoutClick = {}
        )
    }
}

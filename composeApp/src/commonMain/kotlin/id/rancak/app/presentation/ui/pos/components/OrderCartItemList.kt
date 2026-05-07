package id.rancak.app.presentation.ui.pos.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.NoteAdd
import androidx.compose.material.icons.automirrored.filled.Notes
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Remove
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
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import id.rancak.app.domain.model.CartItem
import id.rancak.app.domain.model.Modifier as DomainModifier
import id.rancak.app.presentation.designsystem.RancakTheme
import id.rancak.app.presentation.ui.pos.FeeInputDialog
import id.rancak.app.presentation.util.formatRupiah
import id.rancak.app.presentation.viewmodel.CartUiState
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.ImmutableMap
import kotlinx.collections.immutable.persistentListOf
import kotlinx.collections.immutable.persistentMapOf
import kotlinx.collections.immutable.toImmutableList

// ── CartItemList ──────────────────────────────────────────────────────────────

@Composable
internal fun OrderCartItemList(
    modifier:        Modifier,
    cartState:       CartUiState,
    primary:         Color,
    onSurfaceVariant: Color,
    onUpdateQty:     (CartItem, Int) -> Unit,
    onUpdateNote:    (CartItem, String) -> Unit,
    modifierCache:   ImmutableMap<String, ImmutableList<DomainModifier>> = persistentMapOf(),
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
                    modifiers       = (modifierCache[item.productUuid] ?: emptyList()).toImmutableList(),
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

// ── OrderItemRow ─────────────────────────────────────────────────────────────

@Composable
private fun OrderItemRow(
    item:            CartItem,
    primary:         Color,
    modifiers:       ImmutableList<DomainModifier> = persistentListOf(),
    onLoadModifiers: () -> Unit = {},
    onIncrease:      () -> Unit,
    onDecrease:      () -> Unit,
    onSetQty:        (Int) -> Unit,
    onSetNote:       (String) -> Unit
) {
    val accent           = accentFor(item.productName)
    val onSurface        = MaterialTheme.colorScheme.onSurface
    val onSurfaceVariant = MaterialTheme.colorScheme.onSurfaceVariant

    var showQtyDialog  by remember { mutableStateOf(false) }
    var showNoteDialog by remember { mutableStateOf(false) }
    var noteText       by remember(item.note) { mutableStateOf(item.note ?: "") }

    LaunchedEffect(showNoteDialog) {
        if (showNoteDialog) onLoadModifiers()
    }

    // ── Qty dialog ────────────────────────────────────────────────────────
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

    // ── Note dialog ───────────────────────────────────────────────────────
    if (showNoteDialog) {
        AlertDialog(
            onDismissRequest = { showNoteDialog = false },
            title = {
                Text(
                    "Catatan — ${item.productName}",
                    style      = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold
                )
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    val activeModifiers = modifiers.filter { it.isActive }
                    if (activeModifiers.isNotEmpty()) {
                        Text(
                            "Preset cepat",
                            style = MaterialTheme.typography.labelSmall,
                            color = onSurfaceVariant
                        )
                        // Memoize parsed set: split+trim+filter hanya jalan ulang
                        // saat noteText berubah, bukan pada setiap rekomposisi.
                        val selectedNames by remember(noteText) {
                            derivedStateOf {
                                noteText.split(", ")
                                    .mapTo(mutableSetOf()) { it.trim() }
                                    .filter { it.isNotBlank() }
                                    .toSet()
                            }
                        }
                        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                            activeModifiers.chunked(3).forEach { rowModifiers ->
                                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                    rowModifiers.forEach { mod ->
                                        val isSelected = mod.name in selectedNames
                                        Surface(
                                            onClick = {
                                                // Gunakan selectedNames yang sudah diparse — tidak perlu split ulang
                                                val updated = if (isSelected) {
                                                    selectedNames - mod.name
                                                } else {
                                                    selectedNames + mod.name
                                                }
                                                noteText = updated.filter { it.isNotBlank() }.joinToString(", ")
                                            },
                                            shape          = RoundedCornerShape(20.dp),
                                            color          = if (isSelected) primary else MaterialTheme.colorScheme.surfaceVariant,
                                            tonalElevation = if (isSelected) 0.dp else 1.dp
                                        ) {
                                            Text(
                                                text       = mod.name,
                                                modifier   = Modifier.padding(horizontal = 10.dp, vertical = 5.dp),
                                                style      = MaterialTheme.typography.labelSmall,
                                                color      = if (isSelected) MaterialTheme.colorScheme.onPrimary else onSurfaceVariant,
                                                fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Normal
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

    // ── Row content ───────────────────────────────────────────────────────
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 12.dp, vertical = 6.dp)
    ) {
        Row(
            verticalAlignment     = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            // Avatar
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(RoundedCornerShape(10.dp))
                    .background(accent.copy(0.14f))
                    .border(1.dp, accent.copy(0.28f), RoundedCornerShape(10.dp)),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    item.productName.firstOrNull()?.uppercaseChar()?.toString() ?: "?",
                    style      = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.ExtraBold,
                    color      = accent
                )
            }

            // Nama + harga satuan
            Column(Modifier.weight(1f)) {
                Text(
                    item.productName + (item.variantName?.let { " · $it" } ?: ""),
                    style      = MaterialTheme.typography.bodySmall,
                    fontWeight = FontWeight.SemiBold,
                    color      = onSurface,
                    maxLines   = 1,
                    overflow   = TextOverflow.Ellipsis
                )
                Text(
                    "${formatRupiah(item.price)} / item",
                    style = MaterialTheme.typography.labelSmall,
                    color = onSurfaceVariant.copy(0.7f)
                )
            }

            // Qty controls
            Row(
                verticalAlignment     = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(2.dp)
            ) {
                SmallQtyButton(Icons.Default.Remove, onClick = onDecrease)
                Text(
                    "${item.qty}",
                    style      = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.ExtraBold,
                    color      = primary,
                    textAlign  = TextAlign.Center,
                    modifier   = Modifier
                        .widthIn(min = 28.dp)
                        .clickable { showQtyDialog = true }
                )
                SmallQtyButton(Icons.Default.Add, tint = primary, onClick = onIncrease)
            }

            // Subtotal
            Text(
                formatRupiah(item.subtotal),
                style      = MaterialTheme.typography.bodySmall,
                fontWeight = FontWeight.ExtraBold,
                color      = primary,
                textAlign  = TextAlign.End,
                modifier   = Modifier.width(72.dp)
            )

            // Tombol catatan
            Box(
                modifier = Modifier
                    .size(28.dp)
                    .clip(RoundedCornerShape(6.dp))
                    .background(
                        if (item.note.isNullOrBlank()) MaterialTheme.colorScheme.surfaceVariant.copy(0.5f)
                        else primary.copy(0.1f)
                    )
                    .clickable { showNoteDialog = true },
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    if (item.note.isNullOrBlank()) Icons.AutoMirrored.Filled.NoteAdd
                    else Icons.Default.Edit,
                    contentDescription = "Catatan",
                    modifier = Modifier.size(14.dp),
                    tint = if (item.note.isNullOrBlank()) onSurfaceVariant.copy(0.45f)
                           else primary.copy(0.8f)
                )
            }
        }

        // Note preview
        if (!item.note.isNullOrBlank()) {
            Row(
                modifier              = Modifier.padding(start = 50.dp, top = 3.dp, end = 4.dp),
                verticalAlignment     = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Icon(
                    Icons.AutoMirrored.Filled.Notes, null,
                    Modifier.size(10.dp),
                    tint = primary.copy(0.5f)
                )
                Text(
                    item.note,
                    style     = MaterialTheme.typography.labelSmall,
                    color     = onSurfaceVariant.copy(0.65f),
                    fontStyle = FontStyle.Italic,
                    maxLines  = 1,
                    overflow  = TextOverflow.Ellipsis,
                    fontSize  = 10.sp
                )
            }
        }
    }
}

// ── SmallQtyButton ────────────────────────────────────────────────────────────

@Composable
private fun SmallQtyButton(
    icon:    ImageVector,
    tint:    Color = MaterialTheme.colorScheme.onSurfaceVariant,
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .size(36.dp)
            .clip(CircleShape)
            .border(1.dp, MaterialTheme.colorScheme.outlineVariant, CircleShape)
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        Icon(icon, null, Modifier.size(15.dp), tint = tint)
    }
}

// ── Preview ───────────────────────────────────────────────────────────────────

@Preview(showBackground = true, heightDp = 300)
@Composable
private fun OrderCartItemListPreview_Empty() {
    RancakTheme {
        OrderCartItemList(
            modifier         = Modifier.fillMaxSize(),
            cartState        = CartUiState(),
            primary          = MaterialTheme.colorScheme.primary,
            onSurfaceVariant = MaterialTheme.colorScheme.onSurfaceVariant,
            onUpdateQty      = { _, _ -> },
            onUpdateNote     = { _, _ -> }
        )
    }
}

@Preview(showBackground = true, heightDp = 300)
@Composable
private fun OrderCartItemListPreview_WithItems() {
    RancakTheme {
        OrderCartItemList(
            modifier = Modifier.fillMaxSize(),
            cartState = CartUiState(
                items = listOf(
                    CartItem(productUuid = "p1", productName = "Kopi Susu", qty = 2, price = 18_000L),
                    CartItem(productUuid = "p2", productName = "Croissant", qty = 1, price = 22_000L, note = "dihangatkan")
                )
            ),
            primary          = MaterialTheme.colorScheme.primary,
            onSurfaceVariant = MaterialTheme.colorScheme.onSurfaceVariant,
            onUpdateQty      = { _, _ -> },
            onUpdateNote     = { _, _ -> }
        )
    }
}

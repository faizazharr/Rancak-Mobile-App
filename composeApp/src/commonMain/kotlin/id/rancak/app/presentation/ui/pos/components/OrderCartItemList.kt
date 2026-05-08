package id.rancak.app.presentation.ui.pos.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
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
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
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
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import id.rancak.app.domain.model.CartItem
import id.rancak.app.domain.model.Modifier as DomainModifier
import id.rancak.app.presentation.designsystem.Primary
import id.rancak.app.presentation.designsystem.RancakTheme
import id.rancak.app.presentation.ui.pos.FeeInputDialog
import id.rancak.app.presentation.util.formatRupiah
import id.rancak.app.presentation.viewmodel.CartUiState
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.ImmutableMap
import kotlinx.collections.immutable.persistentListOf
import kotlinx.collections.immutable.persistentMapOf
import kotlinx.collections.immutable.toImmutableList

private val NoteGradientEnd = Color(0xFF0B7A60)

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
        ItemNoteDialog(
            productName     = item.productName,
            initialNote     = item.note ?: "",
            modifiers       = modifiers,
            primary         = primary,
            onDismiss       = { showNoteDialog = false },
            onSave          = { note ->
                onSetNote(note)
                showNoteDialog = false
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

// ── ItemNoteDialog ────────────────────────────────────────────────────────────

@Composable
private fun ItemNoteDialog(
    productName: String,
    initialNote: String,
    modifiers:   ImmutableList<DomainModifier>,
    primary:     Color,
    onDismiss:   () -> Unit,
    onSave:      (String) -> Unit
) {
    var noteText by remember(initialNote) { mutableStateOf(initialNote) }
    val activeModifiers = remember(modifiers) { modifiers.filter { it.isActive } }
    val selectedNames by remember(noteText) {
        derivedStateOf {
            noteText.split(", ").mapTo(mutableSetOf()) { it.trim() }.filter { it.isNotBlank() }.toSet()
        }
    }
    val onSurfaceVariant = MaterialTheme.colorScheme.onSurfaceVariant
    val surfaceColor     = MaterialTheme.colorScheme.surface
    val canSave          = noteText.isNotBlank()

    Dialog(
        onDismissRequest = onDismiss,
        properties       = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            shape  = RoundedCornerShape(20.dp),
            color  = surfaceColor,
            tonalElevation = 3.dp,
            modifier = Modifier
                .widthIn(max = 480.dp)
                .fillMaxWidth(0.92f)
                .wrapContentHeight()
        ) {
            Column {
                // ── Teal header ─────────────────────────────────────────────
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(
                            Brush.horizontalGradient(listOf(Primary, NoteGradientEnd)),
                            RoundedCornerShape(topStart = 20.dp, topEnd = 20.dp)
                        )
                        .padding(horizontal = 20.dp, vertical = 16.dp)
                ) {
                    Row(
                        verticalAlignment     = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Box(
                            Modifier
                                .size(38.dp)
                                .background(Color.White.copy(0.18f), RoundedCornerShape(10.dp)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                Icons.AutoMirrored.Filled.NoteAdd, null,
                                Modifier.size(20.dp),
                                tint = Color.White
                            )
                        }
                        Column {
                            Text(
                                "Catatan",
                                style      = MaterialTheme.typography.labelSmall,
                                color      = Color.White.copy(0.75f),
                                fontWeight = FontWeight.Medium
                            )
                            Text(
                                productName,
                                style      = MaterialTheme.typography.titleSmall,
                                color      = Color.White,
                                fontWeight = FontWeight.Bold,
                                maxLines   = 1,
                                overflow   = TextOverflow.Ellipsis
                            )
                        }
                    }
                }

                // ── Body ────────────────────────────────────────────────────
                Column(
                    modifier            = Modifier.padding(20.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    // Preset chips
                    if (activeModifiers.isNotEmpty()) {
                        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            Row(
                                verticalAlignment     = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(5.dp)
                            ) {
                                Icon(
                                    Icons.Default.Bolt, null,
                                    Modifier.size(13.dp),
                                    tint = primary.copy(0.8f)
                                )
                                Text(
                                    "Preset cepat",
                                    style      = MaterialTheme.typography.labelSmall,
                                    fontWeight = FontWeight.SemiBold,
                                    color      = onSurfaceVariant
                                )
                            }
                            // Wrap chips in flexible rows
                            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                                activeModifiers.chunked(3).forEach { row ->
                                    Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                        row.forEach { mod ->
                                            val isSelected = mod.name in selectedNames
                                            val chipBg by animateColorAsState(
                                                if (isSelected) primary else MaterialTheme.colorScheme.surfaceVariant,
                                                tween(200), label = "ChipBg_${mod.name}"
                                            )
                                            val chipBorder by animateColorAsState(
                                                if (isSelected) primary else MaterialTheme.colorScheme.outlineVariant.copy(0.5f),
                                                tween(200), label = "ChipBorder_${mod.name}"
                                            )
                                            val chipText by animateColorAsState(
                                                if (isSelected) Color.White else onSurfaceVariant,
                                                tween(200), label = "ChipText_${mod.name}"
                                            )
                                            Row(
                                                modifier = Modifier
                                                    .clip(RoundedCornerShape(50.dp))
                                                    .background(chipBg)
                                                    .border(1.dp, chipBorder, RoundedCornerShape(50.dp))
                                                    .clickable {
                                                        val updated = if (isSelected) selectedNames - mod.name
                                                                      else selectedNames + mod.name
                                                        noteText = updated.filter { it.isNotBlank() }.joinToString(", ")
                                                    }
                                                    .padding(horizontal = 11.dp, vertical = 5.dp),
                                                verticalAlignment = Alignment.CenterVertically,
                                                horizontalArrangement = Arrangement.spacedBy(4.dp)
                                            ) {
                                                if (isSelected) {
                                                    Icon(Icons.Default.Check, null, Modifier.size(11.dp), tint = Color.White)
                                                }
                                                Text(
                                                    mod.name,
                                                    style      = MaterialTheme.typography.labelSmall,
                                                    color      = chipText,
                                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                                                )
                                            }
                                        }
                                    }
                                }
                            }
                        }
                        HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(0.4f))
                    }

                    // Text input
                    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        Row(
                            modifier              = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment     = Alignment.CenterVertically
                        ) {
                            Row(
                                verticalAlignment     = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(5.dp)
                            ) {
                                Icon(Icons.AutoMirrored.Filled.Notes, null, Modifier.size(13.dp), tint = primary.copy(0.8f))
                                Text(
                                    "Catatan bebas",
                                    style      = MaterialTheme.typography.labelSmall,
                                    fontWeight = FontWeight.SemiBold,
                                    color      = onSurfaceVariant
                                )
                            }
                            Text(
                                "${noteText.length}/120",
                                style = MaterialTheme.typography.labelSmall,
                                color = if (noteText.length > 100) MaterialTheme.colorScheme.error.copy(0.8f)
                                        else onSurfaceVariant.copy(0.5f)
                            )
                        }
                        val fieldBorder by animateColorAsState(
                            if (noteText.isNotBlank()) primary.copy(0.6f) else MaterialTheme.colorScheme.outlineVariant,
                            tween(200), label = "NoteFieldBorder"
                        )
                        val fieldBg by animateColorAsState(
                            if (noteText.isNotBlank()) primary.copy(0.04f) else MaterialTheme.colorScheme.surfaceVariant.copy(0.5f),
                            tween(200), label = "NoteFieldBg"
                        )
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(12.dp))
                                .background(fieldBg)
                                .border(1.dp, fieldBorder, RoundedCornerShape(12.dp))
                                .padding(12.dp)
                        ) {
                            if (noteText.isEmpty()) {
                                Text(
                                    "Contoh: gula sedikit, tambah es…",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = onSurfaceVariant.copy(0.4f),
                                    fontStyle = FontStyle.Italic
                                )
                            }
                            BasicTextField(
                                value         = noteText,
                                onValueChange = { if (it.length <= 120) noteText = it },
                                textStyle     = MaterialTheme.typography.bodySmall.copy(
                                    color      = MaterialTheme.colorScheme.onSurface,
                                    lineHeight = 20.sp
                                ),
                                cursorBrush  = SolidColor(primary),
                                maxLines     = 4,
                                modifier     = Modifier.fillMaxWidth()
                            )
                        }
                    }

                    // Action buttons
                    Row(
                        modifier              = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp),
                        verticalAlignment     = Alignment.CenterVertically
                    ) {
                        TextButton(
                            onClick  = onDismiss,
                            modifier = Modifier.weight(1f)
                        ) {
                            Text(
                                "Batal",
                                style = MaterialTheme.typography.labelLarge,
                                color = onSurfaceVariant
                            )
                        }
                        // Gradient Simpan button
                        val saveStart by animateColorAsState(
                            if (canSave) Primary else MaterialTheme.colorScheme.outlineVariant.copy(0.4f),
                            tween(220), label = "SaveGradStart"
                        )
                        val saveEnd by animateColorAsState(
                            if (canSave) NoteGradientEnd else MaterialTheme.colorScheme.outlineVariant.copy(0.4f),
                            tween(220), label = "SaveGradEnd"
                        )
                        val saveTextColor by animateColorAsState(
                            if (canSave) Color.White else onSurfaceVariant.copy(0.5f),
                            tween(200), label = "SaveTextColor"
                        )
                        Box(
                            modifier = Modifier
                                .weight(1.6f)
                                .clip(RoundedCornerShape(10.dp))
                                .background(Brush.horizontalGradient(listOf(saveStart, saveEnd)))
                                .clickable(enabled = canSave) { onSave(noteText.trim()) }
                                .padding(vertical = 12.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Row(
                                verticalAlignment     = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                Icon(Icons.Default.Check, null, Modifier.size(15.dp), tint = saveTextColor)
                                Text(
                                    "Simpan",
                                    style      = MaterialTheme.typography.labelLarge,
                                    fontWeight = FontWeight.Bold,
                                    color      = saveTextColor
                                )
                            }
                        }
                    }
                }
            }
        }
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

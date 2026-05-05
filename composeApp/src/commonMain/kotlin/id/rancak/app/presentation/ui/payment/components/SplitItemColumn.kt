package id.rancak.app.presentation.ui.payment.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.CallSplit
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import id.rancak.app.domain.model.PaymentMethod
import id.rancak.app.presentation.designsystem.RancakTheme
import id.rancak.app.presentation.util.formatRupiah
import id.rancak.app.presentation.viewmodel.SplitGroup
import id.rancak.app.presentation.viewmodel.SplitableItem
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.ImmutableMap
import kotlinx.collections.immutable.persistentListOf
import kotlinx.collections.immutable.persistentMapOf

// Warna aksen per grup — diurutkan agar grup 1 selalu hijau (teal brand)
internal val groupAccentColors = listOf(
    Color(0xFF0D9373),
    Color(0xFFE8772E),
    Color(0xFF9C27B0),
    Color(0xFF2196F3),
    Color(0xFFFF5722),
    Color(0xFF009688),
)

/**
 * Kolom kiri panel pembayaran terpisah.
 *
 * Card putih berisi:
 * - Brand header RANCAK POS
 * - Surface total keseluruhan
 * - Stepper item (scrollable, fills remaining space)
 * - Daftar grup yang sudah dikonfirmasi
 * - Toggle mode pembayaran (Tunggal / Terpisah)
 */
@Composable
internal fun SplitItemColumn(
    orderTotal:      Long,
    items:           ImmutableList<SplitableItem>,
    confirmedQtyMap: ImmutableMap<Int, Int>,
    currentItemQtys: ImmutableMap<Int, Int>,
    splitGroups:     ImmutableList<SplitGroup>,
    isSplit:         Boolean,
    onSetItemQty:    (Int, Int) -> Unit,
    onToggleMode:    () -> Unit,
    onRemoveGroup:   (Int) -> Unit,
    modifier:        Modifier = Modifier
) {
    Box(modifier = modifier, contentAlignment = Alignment.TopCenter) {
        Card(
            modifier  = Modifier.widthIn(max = 480.dp).fillMaxWidth().fillMaxHeight(),
            shape     = RoundedCornerShape(12.dp),
            colors    = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
        ) {
            Column(modifier = Modifier.fillMaxSize().padding(horizontal = 16.dp)) {

                SplitBrandHeader()
                SplitTotalSurface(total = orderTotal)

                Spacer(Modifier.height(10.dp))

                Text(
                    "Pilih Item untuk Pelanggan",
                    style      = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.SemiBold,
                    color      = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(Modifier.height(6.dp))

                if (items.isEmpty()) {
                    Box(Modifier.weight(1f), contentAlignment = Alignment.Center) {
                        Text(
                            "Memuat item...",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                } else {
                    LazyColumn(
                        modifier            = Modifier.weight(1f),
                        verticalArrangement = Arrangement.spacedBy(8.dp),
                        contentPadding      = PaddingValues(bottom = 4.dp)
                    ) {
                        items(items, key = { it.index }) { item ->
                            val confirmedQty = confirmedQtyMap[item.index] ?: 0
                            val remainingQty = item.qty - confirmedQty
                            val selectedQty  = currentItemQtys[item.index] ?: 0
                            ItemQtyStepper(
                                item         = item,
                                selectedQty  = selectedQty,
                                confirmedQty = confirmedQty,
                                remainingQty = remainingQty,
                                onSetQty     = { qty -> onSetItemQty(item.index, qty) }
                            )
                        }
                    }
                }

                if (splitGroups.isNotEmpty()) {
                    ConfirmedGroupsSection(
                        items         = items,
                        splitGroups   = splitGroups,
                        onRemoveGroup = onRemoveGroup
                    )
                }

                SplitModeToggle(
                    isSplit  = isSplit,
                    onToggle = onToggleMode,
                    modifier = Modifier.padding(vertical = 10.dp)
                )
            }
        }
    }
}

// ── Brand header ──────────────────────────────────────────────────────────────

@Composable
private fun SplitBrandHeader() {
    Text(
        "RANCAK POS",
        modifier      = Modifier.fillMaxWidth().padding(top = 8.dp, bottom = 6.dp),
        style         = MaterialTheme.typography.titleMedium,
        fontWeight    = FontWeight.ExtraBold,
        letterSpacing = 3.sp,
        color         = MaterialTheme.colorScheme.primary,
        textAlign     = TextAlign.Center
    )
}

// ── Total surface ─────────────────────────────────────────────────────────────

@Composable
private fun SplitTotalSurface(total: Long) {
    Surface(
        shape    = RoundedCornerShape(8.dp),
        color    = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f),
        border   = BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.35f)),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier              = Modifier.fillMaxWidth().padding(horizontal = 14.dp, vertical = 10.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment     = Alignment.CenterVertically
        ) {
            Text(
                "TOTAL",
                style         = MaterialTheme.typography.titleSmall,
                fontWeight    = FontWeight.ExtraBold,
                letterSpacing = 1.sp
            )
            Text(
                formatRupiah(total),
                style      = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.ExtraBold,
                color      = MaterialTheme.colorScheme.primary,
                fontFamily = FontFamily.Monospace
            )
        }
    }
}

// ── Confirmed groups section ──────────────────────────────────────────────────

@Composable
private fun ConfirmedGroupsSection(
    items:         ImmutableList<SplitableItem>,
    splitGroups:   ImmutableList<SplitGroup>,
    onRemoveGroup: (Int) -> Unit
) {
    HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
    Spacer(Modifier.height(4.dp))
    Text(
        "Sudah Dikonfirmasi",
        style      = MaterialTheme.typography.labelMedium,
        fontWeight = FontWeight.SemiBold,
        color      = MaterialTheme.colorScheme.onSurfaceVariant
    )
    Spacer(Modifier.height(4.dp))
    LazyColumn(
        modifier            = Modifier.heightIn(max = 180.dp),
        verticalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        itemsIndexed(splitGroups) { index, group ->
            val groupSubtotal = items.sumOf { item -> (group.itemQtys[item.index] ?: 0) * item.price }
            val accentColor   = groupAccentColors.getOrElse(index) { groupAccentColors.first() }
            ConfirmedGroupRow(
                group         = group,
                label         = "Pelanggan ${group.id}",
                groupSubtotal = groupSubtotal,
                accentColor   = accentColor,
                onRemove      = { onRemoveGroup(group.id) }
            )
        }
    }
}

// ── Mode toggle ───────────────────────────────────────────────────────────────

@Composable
private fun SplitModeToggle(
    isSplit:  Boolean,
    onToggle: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier, verticalArrangement = Arrangement.spacedBy(6.dp)) {
        Text(
            "Mode Pembayaran",
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
            AssistChip(
                onClick     = { if (isSplit) onToggle() },
                label       = { Text("Tunggal") },
                leadingIcon = { Icon(Icons.Default.Payments, null, Modifier.size(16.dp)) },
                modifier    = Modifier.weight(1f),
                colors      = if (!isSplit) AssistChipDefaults.assistChipColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer
                ) else AssistChipDefaults.assistChipColors()
            )
            AssistChip(
                onClick     = { if (!isSplit) onToggle() },
                label       = { Text("Terpisah") },
                leadingIcon = { Icon(Icons.AutoMirrored.Filled.CallSplit, null, Modifier.size(16.dp)) },
                modifier    = Modifier.weight(1f),
                colors      = if (isSplit) AssistChipDefaults.assistChipColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer
                ) else AssistChipDefaults.assistChipColors()
            )
        }
    }
}

// ── Item qty stepper ──────────────────────────────────────────────────────────

@Composable
private fun ItemQtyStepper(
    item:        SplitableItem,
    selectedQty: Int,
    confirmedQty: Int,
    remainingQty: Int,
    onSetQty:    (Int) -> Unit
) {
    val isFull = remainingQty <= 0
    val containerColor = when {
        isFull          -> MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)
        selectedQty > 0 -> MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.35f)
        else            -> MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
    }
    val borderColor = if (selectedQty > 0)
        MaterialTheme.colorScheme.primary.copy(alpha = 0.6f)
    else MaterialTheme.colorScheme.outlineVariant

    Card(
        colors   = CardDefaults.cardColors(containerColor = containerColor),
        border   = BorderStroke(if (selectedQty > 0) 1.5.dp else 0.5.dp, borderColor),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier              = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
            verticalAlignment     = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            // Item info
            Column(modifier = Modifier.weight(1f)) {
                val label = if (item.variantName != null)
                    "${item.name} (${item.variantName})" else item.name
                Text(
                    label,
                    style      = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Medium,
                    maxLines   = 1
                )
                Row(
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalAlignment     = Alignment.CenterVertically
                ) {
                    Text(
                        "${formatRupiah(item.price)} / pcs",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    if (confirmedQty > 0) {
                        Text(
                            "• $confirmedQty terbayar",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                }
            }

            // Stepper or "Lunas" badge
            if (isFull) {
                Row(
                    verticalAlignment     = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Icon(
                        Icons.Default.CheckCircle, null,
                        Modifier.size(16.dp),
                        tint = MaterialTheme.colorScheme.primary
                    )
                    Text(
                        "Lunas",
                        style      = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.SemiBold,
                        color      = MaterialTheme.colorScheme.primary
                    )
                }
            } else {
                Row(
                    verticalAlignment     = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(2.dp)
                ) {
                    FilledIconButton(
                        onClick  = { if (selectedQty > 0) onSetQty(selectedQty - 1) },
                        enabled  = selectedQty > 0,
                        modifier = Modifier.size(30.dp),
                        colors   = IconButtonDefaults.filledIconButtonColors(
                            containerColor = MaterialTheme.colorScheme.surfaceVariant
                        )
                    ) { Icon(Icons.Default.Remove, null, Modifier.size(16.dp)) }

                    Text(
                        "$selectedQty / $remainingQty",
                        style      = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Bold,
                        textAlign  = TextAlign.Center,
                        color      = if (selectedQty > 0) MaterialTheme.colorScheme.primary
                                     else MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier   = Modifier.widthIn(min = 38.dp)
                    )

                    FilledIconButton(
                        onClick  = { if (selectedQty < remainingQty) onSetQty(selectedQty + 1) },
                        enabled  = selectedQty < remainingQty,
                        modifier = Modifier.size(30.dp),
                        colors   = IconButtonDefaults.filledIconButtonColors(
                            containerColor = MaterialTheme.colorScheme.primaryContainer
                        )
                    ) { Icon(Icons.Default.Add, null, Modifier.size(16.dp)) }
                }

                if (selectedQty > 0) {
                    Text(
                        formatRupiah(item.price * selectedQty),
                        style      = MaterialTheme.typography.bodySmall,
                        fontWeight = FontWeight.SemiBold,
                        color      = MaterialTheme.colorScheme.primary,
                        textAlign  = TextAlign.End,
                        modifier   = Modifier.widthIn(min = 64.dp)
                    )
                }
            }
        }
    }
}

// ── Confirmed group row ───────────────────────────────────────────────────────

@Composable
private fun ConfirmedGroupRow(
    group:         SplitGroup,
    label:         String,
    groupSubtotal: Long,
    accentColor:   Color,
    onRemove:      () -> Unit
) {
    val expectedAmount = group.groupActualTotal.takeIf { it > 0 } ?: groupSubtotal
    val displayAmount  = if (group.method == PaymentMethod.CASH && group.cashPaid > 0)
        group.cashPaid else expectedAmount
    val changeAmount   = if (group.method == PaymentMethod.CASH && group.cashPaid > 0)
        group.cashPaid - expectedAmount else 0L
    val totalQty       = group.itemQtys.values.sum()

    Card(
        colors   = CardDefaults.cardColors(containerColor = accentColor.copy(alpha = 0.07f)),
        border   = BorderStroke(1.dp, accentColor.copy(alpha = 0.4f)),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier              = Modifier.padding(horizontal = 10.dp, vertical = 8.dp),
            verticalAlignment     = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Box(
                Modifier
                    .width(4.dp)
                    .height(44.dp)
                    .background(accentColor, MaterialTheme.shapes.small)
            )
            Column(modifier = Modifier.weight(1f)) {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalAlignment     = Alignment.CenterVertically
                ) {
                    Icon(Icons.Default.CheckCircle, null, Modifier.size(14.dp), tint = accentColor)
                    Text(
                        label,
                        style      = MaterialTheme.typography.bodySmall,
                        fontWeight = FontWeight.Bold,
                        color      = accentColor
                    )
                }
                Text(
                    "${group.method.value.uppercase()} • ${formatRupiah(displayAmount)}",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                if (group.method == PaymentMethod.CASH && changeAmount > 0) {
                    Text(
                        "Kembalian: ${formatRupiah(changeAmount)}",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
                Text(
                    "$totalQty item",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            IconButton(onClick = onRemove, modifier = Modifier.size(32.dp)) {
                Icon(
                    Icons.Default.Delete, "Hapus grup",
                    Modifier.size(18.dp),
                    tint = MaterialTheme.colorScheme.error
                )
            }
        }
    }
}

// ── Previews ──────────────────────────────────────────────────────────────────

@Preview(showBackground = true, widthDp = 300)
@Composable
private fun ItemQtyStepperPreview() {
    RancakTheme {
        Column(Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            // Belum dipilih
            ItemQtyStepper(
                item = SplitableItem(0, "Kopi Susu Gula Aren", 3, 18_000L),
                selectedQty = 0, confirmedQty = 0, remainingQty = 3, onSetQty = {}
            )
            // Sudah dipilih sebagian
            ItemQtyStepper(
                item = SplitableItem(1, "Croissant Keju", 2, 22_000L),
                selectedQty = 1, confirmedQty = 0, remainingQty = 2, onSetQty = {}
            )
            // Lunas (semua sudah terbayar grup sebelumnya)
            ItemQtyStepper(
                item = SplitableItem(2, "Es Teh", 1, 8_000L),
                selectedQty = 0, confirmedQty = 1, remainingQty = 0, onSetQty = {}
            )
        }
    }
}

@Preview(showBackground = true, widthDp = 300)
@Composable
private fun ConfirmedGroupRowPreview() {
    RancakTheme {
        Column(Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            ConfirmedGroupRow(
                group = SplitGroup(
                    id = 1, itemQtys = mapOf(0 to 2),
                    method = PaymentMethod.CASH, cashPaid = 40_000L, groupActualTotal = 36_000L
                ),
                label = "Pelanggan 1", groupSubtotal = 36_000L,
                accentColor = groupAccentColors[0], onRemove = {}
            )
            ConfirmedGroupRow(
                group = SplitGroup(
                    id = 2, itemQtys = mapOf(1 to 1),
                    method = PaymentMethod.QRIS, cashPaid = 0L, groupActualTotal = 22_000L
                ),
                label = "Pelanggan 2", groupSubtotal = 22_000L,
                accentColor = groupAccentColors[1], onRemove = {}
            )
        }
    }
}

@Preview(showBackground = true, widthDp = 380, heightDp = 600)
@Composable
private fun SplitItemColumnPreview() {
    RancakTheme {
        SplitItemColumn(
            orderTotal      = 70_000L,
            items           = persistentListOf(
                SplitableItem(0, "Kopi Susu",  2, 18_000L),
                SplitableItem(1, "Croissant",  1, 22_000L),
                SplitableItem(2, "Es Teh",     1, 12_000L)
            ),
            confirmedQtyMap = persistentMapOf(0 to 1),
            currentItemQtys = persistentMapOf(1 to 1),
            splitGroups     = persistentListOf(
                SplitGroup(1, mapOf(0 to 1), PaymentMethod.CASH, 20_000L, 18_000L)
            ),
            isSplit         = true,
            onSetItemQty    = { _, _ -> },
            onToggleMode    = {},
            onRemoveGroup   = {},
            modifier        = Modifier.fillMaxHeight().padding(12.dp)
        )
    }
}

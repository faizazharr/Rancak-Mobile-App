package id.rancak.app.presentation.ui.payment.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CallSplit
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Payments
import androidx.compose.material.icons.filled.QrCode2
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import id.rancak.app.domain.model.PaymentMethod
import id.rancak.app.presentation.components.PaymentMethodChip
import id.rancak.app.presentation.components.RancakButton
import id.rancak.app.presentation.components.SummaryRow
import id.rancak.app.presentation.util.formatRupiah
import id.rancak.app.presentation.viewmodel.SplitGroup
import id.rancak.app.presentation.viewmodel.SplitableItem

private val splitMethods = listOf(PaymentMethod.CASH, PaymentMethod.QRIS)

private val groupAccentColors = listOf(
    Color(0xFF0D9373),
    Color(0xFFE8772E),
    Color(0xFF9C27B0),
    Color(0xFF2196F3),
    Color(0xFFFF5722),
    Color(0xFF009688),
)

/**
 * Panel split payment — layout konsisten dengan [PaymentFormContent].
 *
 * - Kiri  : hero total card + daftar grup terkonfirmasi + mode toggle
 * - Kanan : qty stepper per item + pilih metode + input cash/QRIS + tombol konfirmasi + proses
 */
@Composable
internal fun SplitPaymentPanel(
    items: List<SplitableItem>,
    splitGroups: List<SplitGroup>,
    currentItemQtys: Map<Int, Int>,
    currentMethod: PaymentMethod,
    currentCashInput: String,
    orderTotal: Long,
    isProcessing: Boolean,
    onSetItemQty: (index: Int, qty: Int) -> Unit,
    onSetMethod: (PaymentMethod) -> Unit,
    onSetCashInput: (String) -> Unit,
    onConfirmGroup: () -> Unit,
    onRemoveGroup: (Int) -> Unit,
    onProcess: () -> Unit,
    isSplit: Boolean,
    onToggleMode: () -> Unit,
    modifier: Modifier = Modifier
) {
    // Confirmed qty map: itemIndex -> total qty already in confirmed groups
    val confirmedQtyMap: Map<Int, Int> = remember(splitGroups) {
        buildMap {
            splitGroups.forEach { g ->
                g.itemQtys.forEach { (idx, qty) -> merge(idx, qty, Int::plus) }
            }
        }
    }
    val allAssigned = items.isNotEmpty() && items.all { (confirmedQtyMap[it.index] ?: 0) >= it.qty }
    val currentSubtotal: Long = remember(currentItemQtys, items) {
        val priceMap = items.associate { it.index to it.price }
        currentItemQtys.entries.sumOf { (idx, qty) -> (priceMap[idx] ?: 0L) * qty }
    }
    val cashPaid = currentCashInput.toLongOrNull() ?: 0L
    val change = if (currentMethod == PaymentMethod.CASH && cashPaid >= currentSubtotal)
        cashPaid - currentSubtotal else 0L
    val canConfirm = currentItemQtys.isNotEmpty() &&
        currentItemQtys.values.any { it > 0 } &&
        (currentMethod != PaymentMethod.CASH || cashPaid >= currentSubtotal)
    val groupNumber = splitGroups.size + 1

    Row(
        modifier = modifier.padding(16.dp),
        horizontalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // ── LEFT: summary + confirmed groups + mode toggle ────────────────────
        SplitSummaryColumn(
            orderTotal     = orderTotal,
            totalItems     = items.size,
            splitGroups    = splitGroups,
            items          = items,
            allAssigned    = allAssigned,
            isSplit        = isSplit,
            onToggleMode   = onToggleMode,
            onRemoveGroup  = onRemoveGroup,
            modifier       = Modifier.weight(0.42f).fillMaxHeight()
        )

        // ── RIGHT: group builder ──────────────────────────────────────────────
        SplitBuilderColumn(
            items             = items,
            confirmedQtyMap   = confirmedQtyMap,
            currentItemQtys   = currentItemQtys,
            currentSubtotal   = currentSubtotal,
            currentMethod     = currentMethod,
            currentCashInput  = currentCashInput,
            change            = change,
            groupNumber       = groupNumber,
            canConfirm        = canConfirm,
            allAssigned       = allAssigned,
            hasConfirmedGroups = splitGroups.isNotEmpty(),
            isProcessing      = isProcessing,
            onSetItemQty      = onSetItemQty,
            onSetMethod       = onSetMethod,
            onSetCashInput    = onSetCashInput,
            onConfirmGroup    = onConfirmGroup,
            onProcess         = onProcess,
            modifier          = Modifier.weight(0.58f).fillMaxHeight()
        )
    }
}

// ── Left column ───────────────────────────────────────────────────────────────

@Composable
private fun SplitSummaryColumn(
    orderTotal: Long,
    totalItems: Int,
    splitGroups: List<SplitGroup>,
    items: List<SplitableItem>,
    allAssigned: Boolean,
    isSplit: Boolean,
    onToggleMode: () -> Unit,
    onRemoveGroup: (Int) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // Hero card — sama persis dengan PaymentFormContent
        Card(
            shape  = MaterialTheme.shapes.large,
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primary),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 24.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(5.dp)
                ) {
                    Icon(
                        Icons.Default.ShoppingCart, contentDescription = null,
                        tint = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.65f),
                        modifier = Modifier.size(13.dp)
                    )
                    Text(
                        "$totalItems item",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.65f)
                    )
                }
                Text(
                    "Total Pembayaran",
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.85f)
                )
                Text(
                    formatRupiah(orderTotal),
                    style      = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Bold,
                    color      = MaterialTheme.colorScheme.onPrimary
                )
            }
        }

        // Ringkasan split
        Card(
            shape  = MaterialTheme.shapes.medium,
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.45f)
            ),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    "Ringkasan",
                    style      = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.SemiBold,
                    color      = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(Modifier.height(10.dp))
                SummaryRow(label = "$totalItems item", value = formatRupiah(orderTotal))
                HorizontalDivider(
                    modifier = Modifier.padding(vertical = 8.dp),
                    color    = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)
                )
                SummaryRow(
                    label      = "Total",
                    value      = formatRupiah(orderTotal),
                    isBold     = true,
                    valueColor = MaterialTheme.colorScheme.primary
                )
            }
        }

        // Daftar grup terkonfirmasi
        if (splitGroups.isNotEmpty()) {
            Text(
                "Sudah Dikonfirmasi",
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            LazyColumn(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                itemsIndexed(splitGroups) { index, group ->
                    val groupSubtotal = items
                        .sumOf { item -> (group.itemQtys[item.index] ?: 0) * item.price }
                    val accentColor = groupAccentColors.getOrElse(index) { groupAccentColors.first() }
                    ConfirmedGroupRow(
                        group         = group,
                        label         = "Pelanggan ${group.id}",
                        groupSubtotal = groupSubtotal,
                        accentColor   = accentColor,
                        onRemove      = { onRemoveGroup(group.id) }
                    )
                }
            }
        } else {
            Spacer(Modifier.weight(1f))
        }

        // Mode toggle — sama persis dengan PaymentFormContent
        Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
            Text(
                "Mode Pembayaran",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                AssistChip(
                    onClick = { if (isSplit) onToggleMode() },
                    label = { Text("Tunggal") },
                    leadingIcon = { Icon(Icons.Default.Payments, null, modifier = Modifier.size(16.dp)) },
                    modifier = Modifier.weight(1f),
                    colors = if (!isSplit) AssistChipDefaults.assistChipColors(
                        containerColor = MaterialTheme.colorScheme.primaryContainer
                    ) else AssistChipDefaults.assistChipColors()
                )
                AssistChip(
                    onClick = { if (!isSplit) onToggleMode() },
                    label = { Text("Terpisah") },
                    leadingIcon = { Icon(Icons.Default.CallSplit, null, modifier = Modifier.size(16.dp)) },
                    modifier = Modifier.weight(1f),
                    colors = if (isSplit) AssistChipDefaults.assistChipColors(
                        containerColor = MaterialTheme.colorScheme.primaryContainer
                    ) else AssistChipDefaults.assistChipColors()
                )
            }
        }
    }
}

// ── Right column ──────────────────────────────────────────────────────────────

@Composable
private fun SplitBuilderColumn(
    items: List<SplitableItem>,
    confirmedQtyMap: Map<Int, Int>,
    currentItemQtys: Map<Int, Int>,
    currentSubtotal: Long,
    currentMethod: PaymentMethod,
    currentCashInput: String,
    change: Long,
    groupNumber: Int,
    canConfirm: Boolean,
    allAssigned: Boolean,
    hasConfirmedGroups: Boolean,
    isProcessing: Boolean,
    onSetItemQty: (Int, Int) -> Unit,
    onSetMethod: (PaymentMethod) -> Unit,
    onSetCashInput: (String) -> Unit,
    onConfirmGroup: () -> Unit,
    onProcess: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // Header pelanggan N
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                "Pelanggan $groupNumber",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            if (currentItemQtys.values.any { it > 0 }) {
                Text(
                    formatRupiah(currentSubtotal),
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
            }
        }

        // Item qty steppers
        Column(
            modifier = Modifier
                .weight(1f)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            if (items.isEmpty()) {
                Text(
                    "Memuat item...",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            } else {
                items.forEach { item ->
                    val confirmedQty = confirmedQtyMap[item.index] ?: 0
                    val remainingQty = item.qty - confirmedQty
                    val selectedQty = currentItemQtys[item.index] ?: 0
                    ItemQtyStepper(
                        item         = item,
                        selectedQty  = selectedQty,
                        confirmedQty = confirmedQty,
                        remainingQty = remainingQty,
                        onSetQty     = { qty -> onSetItemQty(item.index, qty) }
                    )
                }
            }

            // Method chips
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                splitMethods.forEach { m ->
                    PaymentMethodChip(
                        method     = m.value,
                        isSelected = currentMethod == m,
                        onClick    = { onSetMethod(m) },
                        modifier   = Modifier.weight(1f)
                    )
                }
            }

            // Cash / QRIS input
            if (currentItemQtys.values.any { it > 0 }) {
                when (currentMethod) {
                    PaymentMethod.CASH -> {
                        // Amount display
                        Surface(
                            shape    = MaterialTheme.shapes.medium,
                            color    = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 16.dp, vertical = 14.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment     = Alignment.CenterVertically
                            ) {
                                Column {
                                    Text(
                                        "Uang Diterima",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                    Spacer(Modifier.height(2.dp))
                                    Text(
                                        if (currentCashInput.isEmpty()) "Rp 0"
                                        else formatRupiah(currentCashInput.toLongOrNull() ?: 0L),
                                        style      = MaterialTheme.typography.titleLarge,
                                        fontWeight = FontWeight.Bold,
                                        color      = if (currentCashInput.isEmpty())
                                            MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.4f)
                                        else MaterialTheme.colorScheme.onSurface
                                    )
                                }
                                if (currentCashInput.isNotEmpty()) {
                                    Column(horizontalAlignment = Alignment.End) {
                                        Text(
                                            "Kembalian",
                                            style = MaterialTheme.typography.labelSmall,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                        Spacer(Modifier.height(2.dp))
                                        Text(
                                            formatRupiah(change),
                                            style      = MaterialTheme.typography.titleMedium,
                                            fontWeight = FontWeight.Bold,
                                            color = if (change >= 0) MaterialTheme.colorScheme.primary
                                                    else MaterialTheme.colorScheme.error
                                        )
                                    }
                                }
                            }
                        }

                        // Quick fill — same as PaymentFormContent
                        val quickAmounts = remember(currentSubtotal) {
                            listOf(
                                currentSubtotal,
                                ((currentSubtotal / 10_000) + 1) * 10_000,
                                ((currentSubtotal / 50_000) + 1) * 50_000
                            ).distinct().sorted()
                        }
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            quickAmounts.forEach { amount ->
                                OutlinedButton(
                                    onClick   = { onSetCashInput(amount.toString()) },
                                    modifier  = Modifier.weight(1f),
                                    contentPadding = PaddingValues(horizontal = 4.dp, vertical = 6.dp)
                                ) {
                                    Text(
                                        formatRupiah(amount),
                                        style = MaterialTheme.typography.labelSmall,
                                        maxLines = 1
                                    )
                                }
                            }
                        }

                        PaymentNumpad(
                            onKey = { key ->
                                val current = currentCashInput
                                val next = when (key) {
                                    "\u232b" -> current.dropLast(1)
                                    "000"   -> if (current.isEmpty()) current
                                               else (current + "000").take(10)
                                    else    -> if (current.isEmpty() && key == "0") current
                                               else (current + key).take(10)
                                }
                                onSetCashInput(next)
                            }
                        )
                    }

                    PaymentMethod.QRIS -> {
                        Card(
                            colors = CardDefaults.cardColors(
                                containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.4f)
                            ),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(16.dp),
                                verticalAlignment     = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                Icon(
                                    Icons.Default.QrCode2, contentDescription = null,
                                    tint     = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.size(32.dp)
                                )
                                Column {
                                    Text(
                                        "Total QRIS",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                    Text(
                                        formatRupiah(currentSubtotal),
                                        style      = MaterialTheme.typography.titleLarge,
                                        fontWeight = FontWeight.Bold,
                                        color      = MaterialTheme.colorScheme.primary
                                    )
                                    Text(
                                        "Nominal otomatis dari item yang dipilih",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                                    )
                                }
                            }
                        }
                    }

                    else -> {}
                }
            }

            // Confirm group button
            RancakButton(
                text     = "Tambah Pelanggan $groupNumber",
                onClick  = onConfirmGroup,
                enabled  = canConfirm,
                modifier = Modifier.fillMaxWidth()
            )
        }

        // Process button — sticky at bottom
        RancakButton(
            text      = "Proses Pembayaran",
            onClick   = onProcess,
            isLoading = isProcessing,
            enabled   = allAssigned && hasConfirmedGroups && !isProcessing,
            modifier  = Modifier.fillMaxWidth()
        )
    }
}

// ── Item qty stepper ──────────────────────────────────────────────────────────

@Composable
private fun ItemQtyStepper(
    item: SplitableItem,
    selectedQty: Int,
    confirmedQty: Int,
    remainingQty: Int,
    onSetQty: (Int) -> Unit
) {
    val isFull = remainingQty <= 0
    val containerColor = when {
        isFull      -> MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)
        selectedQty > 0 -> MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.35f)
        else        -> MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
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
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Column(modifier = Modifier.weight(1f)) {
                val label = if (item.variantName != null) "${item.name} (${item.variantName})"
                            else item.name
                Text(
                    label,
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Medium,
                    maxLines = 1
                )
                Row(
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        "${formatRupiah(item.price)} / pcs",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    if (confirmedQty > 0) {
                        Text(
                            "\u2022 $confirmedQty terbayar",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                }
            }

            // Stepper
            if (isFull) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Icon(
                        Icons.Default.CheckCircle,
                        contentDescription = null,
                        tint     = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(16.dp)
                    )
                    Text(
                        "Lunas",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.primary,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            } else {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(2.dp)
                ) {
                    FilledIconButton(
                        onClick  = { if (selectedQty > 0) onSetQty(selectedQty - 1) },
                        enabled  = selectedQty > 0,
                        modifier = Modifier.size(30.dp),
                        colors   = IconButtonDefaults.filledIconButtonColors(
                            containerColor = MaterialTheme.colorScheme.surfaceVariant
                        )
                    ) {
                        Icon(Icons.Default.Remove, null, modifier = Modifier.size(16.dp))
                    }

                    Text(
                        "$selectedQty / $remainingQty",
                        style      = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Bold,
                        color      = if (selectedQty > 0) MaterialTheme.colorScheme.primary
                                     else MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier   = Modifier.widthIn(min = 38.dp),
                        textAlign  = androidx.compose.ui.text.style.TextAlign.Center
                    )

                    FilledIconButton(
                        onClick  = { if (selectedQty < remainingQty) onSetQty(selectedQty + 1) },
                        enabled  = selectedQty < remainingQty,
                        modifier = Modifier.size(30.dp),
                        colors   = IconButtonDefaults.filledIconButtonColors(
                            containerColor = MaterialTheme.colorScheme.primaryContainer
                        )
                    ) {
                        Icon(Icons.Default.Add, null, modifier = Modifier.size(16.dp))
                    }
                }

                // Subtotal for selected qty
                if (selectedQty > 0) {
                    Text(
                        formatRupiah(item.price * selectedQty),
                        style      = MaterialTheme.typography.bodySmall,
                        fontWeight = FontWeight.SemiBold,
                        color      = MaterialTheme.colorScheme.primary,
                        modifier   = Modifier.widthIn(min = 64.dp),
                        textAlign  = androidx.compose.ui.text.style.TextAlign.End
                    )
                }
            }
        }
    }
}

// ── Confirmed group row ───────────────────────────────────────────────────────

@Composable
private fun ConfirmedGroupRow(
    group: SplitGroup,
    label: String,
    groupSubtotal: Long,
    accentColor: Color,
    onRemove: () -> Unit
) {
    val displayAmount = if (group.method == PaymentMethod.CASH && group.cashPaid > 0)
        group.cashPaid else groupSubtotal
    val changeAmount = if (group.method == PaymentMethod.CASH) group.cashPaid - groupSubtotal else 0L

    Card(
        colors   = CardDefaults.cardColors(containerColor = accentColor.copy(alpha = 0.07f)),
        border   = BorderStroke(1.dp, accentColor.copy(alpha = 0.4f)),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Box(
                modifier = Modifier
                    .width(4.dp)
                    .height(44.dp)
                    .background(accentColor, MaterialTheme.shapes.small)
            )
            Column(modifier = Modifier.weight(1f)) {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalAlignment     = Alignment.CenterVertically
                ) {
                    Icon(
                        Icons.Default.CheckCircle, contentDescription = null,
                        tint = accentColor, modifier = Modifier.size(14.dp)
                    )
                    Text(
                        label,
                        style      = MaterialTheme.typography.bodySmall,
                        fontWeight = FontWeight.Bold,
                        color      = accentColor
                    )
                }
                Text(
                    "${group.method.value.uppercase()} \u2022 ${formatRupiah(displayAmount)}",
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
                val totalQty = group.itemQtys.values.sum()
                Text(
                    "$totalQty item",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            IconButton(onClick = onRemove, modifier = Modifier.size(32.dp)) {
                Icon(
                    Icons.Default.Delete, contentDescription = "Hapus grup",
                    tint     = MaterialTheme.colorScheme.error,
                    modifier = Modifier.size(18.dp)
                )
            }
        }
    }
}

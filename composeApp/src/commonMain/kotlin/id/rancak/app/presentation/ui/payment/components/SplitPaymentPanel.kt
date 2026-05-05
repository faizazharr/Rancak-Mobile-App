package id.rancak.app.presentation.ui.payment.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.automirrored.filled.CallSplit
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Payments
import androidx.compose.material.icons.filled.Print
import androidx.compose.material.icons.filled.QrCode2
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import id.rancak.app.domain.model.PaymentMethod
import id.rancak.app.presentation.components.PaymentMethodChip
import id.rancak.app.presentation.components.QrisQrCode
import id.rancak.app.presentation.components.RancakButton
import id.rancak.app.presentation.components.SummaryRow
import id.rancak.app.presentation.util.formatRupiah
import id.rancak.app.presentation.viewmodel.SplitGroup
import id.rancak.app.presentation.viewmodel.SplitableItem
import androidx.compose.ui.tooling.preview.Preview
import id.rancak.app.presentation.designsystem.RancakTheme
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.ImmutableMap
import kotlinx.collections.immutable.persistentListOf
import kotlinx.collections.immutable.persistentMapOf
import kotlinx.collections.immutable.toImmutableMap
import kotlinx.coroutines.flow.merge

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
 * Panel split payment.
 *
 * - Kiri  : hero total + pilih item (stepper scrollable) + grup terkonfirmasi + mode toggle
 * - Kanan : header pelanggan N + input pembayaran (metode / numpad / QRIS) + tombol aksi
 */
@Composable
internal fun SplitPaymentPanel(
    items: ImmutableList<SplitableItem>,
    splitGroups: ImmutableList<SplitGroup>,
    currentItemQtys: ImmutableMap<Int, Int>,
    currentMethod: PaymentMethod,
    currentCashInput: String,
    orderTotal: Long,
    isProcessing: Boolean,
    onSetItemQty: (index: Int, qty: Int) -> Unit,
    onSetMethod: (PaymentMethod) -> Unit,
    onSetCashInput: (String) -> Unit,
    /** Called with the group's actual total (item subtotal + proportional fees). */
    onConfirmGroup: (Long) -> Unit,
    /** Called with the group's actual total — confirms group AND triggers print. */
    onConfirmAndPrint: (Long) -> Unit,
    onRemoveGroup: (Int) -> Unit,
    onProcess: () -> Unit,
    isSplit: Boolean,
    onToggleMode: () -> Unit,
    /** QRIS string statis merchant; bila kosong dialog QRIS jadi info‐only. */
    merchantQrisString: String = "",
    modifier: Modifier = Modifier
) {
    val confirmedQtyMap: ImmutableMap<Int, Int> = remember(splitGroups) {
        buildMap<Int, Int> {
            splitGroups.forEach { g ->
                g.itemQtys.forEach { (idx, qty) -> merge(idx, qty, Int::plus) }
            }
        }.toImmutableMap()
    }
    val allAssigned = items.isNotEmpty() && items.all { (confirmedQtyMap[it.index] ?: 0) >= it.qty }

    // Sum of ALL items' (price × qty) — used to compute each group's fee proportion
    val orderItemSubtotal: Long = remember(items) { items.sumOf { it.price * it.qty } }

    val currentItemSubtotal: Long = remember(currentItemQtys, items) {
        val priceMap = items.associate { it.index to it.price }
        currentItemQtys.entries.sumOf { (idx, qty) -> (priceMap[idx] ?: 0L) * qty }
    }

    // Proportional total: this group pays (its item share / all items) × orderTotal
    // If there are no surcharges, groupActualTotal == currentItemSubtotal
    val groupActualTotal: Long = remember(currentItemSubtotal, orderItemSubtotal, orderTotal) {
        if (orderItemSubtotal > 0L)
            (currentItemSubtotal.toDouble() / orderItemSubtotal.toDouble() * orderTotal.toDouble()).toLong()
        else currentItemSubtotal
    }

    val cashPaid  = currentCashInput.toLongOrNull() ?: 0L
    val change    = if (currentMethod == PaymentMethod.CASH && cashPaid >= groupActualTotal)
        cashPaid - groupActualTotal else 0L
    val canConfirm = currentItemQtys.values.any { it > 0 } &&
        (currentMethod != PaymentMethod.CASH || cashPaid >= groupActualTotal)
    val groupNumber = splitGroups.size + 1

    Row(
        modifier = modifier.padding(16.dp),
        horizontalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // ── LEFT: item selection + confirmed groups + mode toggle ─────────────
        SplitItemColumn(
            orderTotal      = orderTotal,
            items           = items,
            confirmedQtyMap = confirmedQtyMap,
            currentItemQtys = currentItemQtys,
            splitGroups     = splitGroups,
            isSplit         = isSplit,
            onSetItemQty    = onSetItemQty,
            onToggleMode    = onToggleMode,
            onRemoveGroup   = onRemoveGroup,
            modifier        = Modifier.weight(0.45f).fillMaxHeight()
        )

        // ── RIGHT: payment input ──────────────────────────────────────────────
        SplitPaymentColumn(
            groupNumber          = groupNumber,
            currentItemSubtotal  = currentItemSubtotal,
            groupActualTotal     = groupActualTotal,
            hasFeeShare          = groupActualTotal != currentItemSubtotal && currentItemSubtotal > 0,
            currentMethod        = currentMethod,
            currentCashInput     = currentCashInput,
            change               = change,
            canConfirm           = canConfirm,
            allAssigned          = allAssigned,
            hasConfirmedGroups   = splitGroups.isNotEmpty(),
            isProcessing         = isProcessing,
            anyItemSelected      = currentItemQtys.values.any { it > 0 },
            merchantQrisString   = merchantQrisString,
            onSetMethod          = onSetMethod,
            onSetCashInput       = onSetCashInput,
            onConfirmGroup       = { onConfirmGroup(groupActualTotal) },
            onConfirmAndPrint    = { onConfirmAndPrint(groupActualTotal) },
            onProcess            = onProcess,
            modifier             = Modifier.weight(0.55f).fillMaxHeight()
        )
    }
}

// ── Left column — item selection ──────────────────────────────────────────────

@Composable
private fun SplitItemColumn(
    orderTotal: Long,
    items: ImmutableList<SplitableItem>,
    confirmedQtyMap: ImmutableMap<Int, Int>,
    currentItemQtys: ImmutableMap<Int, Int>,
    splitGroups: ImmutableList<SplitGroup>,
    isSplit: Boolean,
    onSetItemQty: (Int, Int) -> Unit,
    onToggleMode: () -> Unit,
    onRemoveGroup: (Int) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        // Hero total card
        Card(
            shape  = MaterialTheme.shapes.large,
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primary),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 18.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(2.dp)
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
                        "${items.size} item",
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

        // Item steppers — scrollable, fills remaining space
        Text(
            "Pilih Item untuk Pelanggan",
            style      = MaterialTheme.typography.labelMedium,
            fontWeight = FontWeight.SemiBold,
            color      = MaterialTheme.colorScheme.onSurfaceVariant
        )
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
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(8.dp),
                contentPadding = PaddingValues(bottom = 4.dp)
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

        // Confirmed groups
        if (splitGroups.isNotEmpty()) {
            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
            Text(
                "Sudah Dikonfirmasi",
                style      = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.SemiBold,
                color      = MaterialTheme.colorScheme.onSurfaceVariant
            )
            LazyColumn(
                modifier = Modifier.heightIn(max = 180.dp),
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                itemsIndexed(splitGroups) { index, group ->
                    val groupSubtotal = items.sumOf { item ->
                        (group.itemQtys[item.index] ?: 0) * item.price
                    }
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
        }

        // Mode toggle
        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
            Text(
                "Mode Pembayaran",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                AssistChip(
                    onClick = { if (isSplit) onToggleMode() },
                    label   = { Text("Tunggal") },
                    leadingIcon = { Icon(Icons.Default.Payments, null, modifier = Modifier.size(16.dp)) },
                    modifier = Modifier.weight(1f),
                    colors = if (!isSplit) AssistChipDefaults.assistChipColors(
                        containerColor = MaterialTheme.colorScheme.primaryContainer
                    ) else AssistChipDefaults.assistChipColors()
                )
                AssistChip(
                    onClick = { if (!isSplit) onToggleMode() },
                    label   = { Text("Terpisah") },
                    leadingIcon = { Icon(Icons.AutoMirrored.Filled.CallSplit, null, modifier = Modifier.size(16.dp)) },
                    modifier = Modifier.weight(1f),
                    colors = if (isSplit) AssistChipDefaults.assistChipColors(
                        containerColor = MaterialTheme.colorScheme.primaryContainer
                    ) else AssistChipDefaults.assistChipColors()
                )
            }
        }
    }
}

// ── Right column — payment input ──────────────────────────────────────────────

@Composable
private fun SplitPaymentColumn(
    groupNumber: Int,
    currentItemSubtotal: Long,
    groupActualTotal: Long,
    hasFeeShare: Boolean,
    currentMethod: PaymentMethod,
    currentCashInput: String,
    change: Long,
    canConfirm: Boolean,
    allAssigned: Boolean,
    hasConfirmedGroups: Boolean,
    isProcessing: Boolean,
    anyItemSelected: Boolean,
    merchantQrisString: String,
    onSetMethod: (PaymentMethod) -> Unit,
    onSetCashInput: (String) -> Unit,
    onConfirmGroup: () -> Unit,
    onConfirmAndPrint: () -> Unit,
    onProcess: () -> Unit,
    modifier: Modifier = Modifier
) {
    var showQrisConfirmDialog by remember { mutableStateOf(false) }

    // ── QRIS confirmation dialog — menampilkan QR statis merchant + nominal grup ─
    if (showQrisConfirmDialog) {
        AlertDialog(
            onDismissRequest = { showQrisConfirmDialog = false },
            title = {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        Icons.Default.QrCode2,
                        contentDescription = null,
                        tint     = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(24.dp)
                    )
                    Spacer(Modifier.width(8.dp))
                    Text("QRIS — Pelanggan $groupNumber")
                }
            },
            text  = {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    // Nominal yang harus dibayar (paling menonjol)
                    Surface(
                        color    = MaterialTheme.colorScheme.primaryContainer,
                        shape    = MaterialTheme.shapes.medium,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(
                            modifier            = Modifier.fillMaxWidth().padding(vertical = 12.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(
                                "Nominal yang harus dibayar",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onPrimaryContainer
                            )
                            Text(
                                formatRupiah(groupActualTotal),
                                style      = MaterialTheme.typography.headlineMedium,
                                fontWeight = FontWeight.Bold,
                                color      = MaterialTheme.colorScheme.primary
                            )
                        }
                    }

                    // QR code merchant statis (kalau ada) atau placeholder info
                    if (merchantQrisString.isNotBlank()) {
                        QrisQrCode(
                            qrString = merchantQrisString,
                            size     = 220.dp,
                            label    = "Scan, lalu masukkan nominal di atas"
                        )
                    } else {
                        Surface(
                            color    = MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.4f),
                            shape    = MaterialTheme.shapes.small,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(
                                "QRIS merchant belum diatur. Buka Pengaturan › Informasi Toko " +
                                "untuk menambahkan QRIS statis Anda, atau minta customer transfer manual.",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onErrorContainer,
                                modifier = Modifier.padding(12.dp)
                            )
                        }
                    }

                    Text(
                        "Setelah customer selesai bayar, tekan \"Sudah Bayar & Cetak\" " +
                        "untuk konfirmasi grup ini dan mencetak struk.",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        showQrisConfirmDialog = false
                        onConfirmAndPrint()
                    }
                ) {
                    Icon(Icons.Default.Print, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(Modifier.width(4.dp))
                    Text("Sudah Bayar & Cetak")
                }
            },
            dismissButton = {
                TextButton(onClick = { showQrisConfirmDialog = false }) {
                    Text("Batal")
                }
            }
        )
    }

    Column(modifier = modifier) {
        // ── Header — Pelanggan N + subtotal ───────────────────────────────────
        Row(
            modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                "Pelanggan $groupNumber",
                style      = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            if (anyItemSelected) {
                Surface(
                    shape = MaterialTheme.shapes.small,
                    color = MaterialTheme.colorScheme.primaryContainer
                ) {
                    Text(
                        formatRupiah(groupActualTotal),
                        style      = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Bold,
                        color      = MaterialTheme.colorScheme.primary,
                        modifier   = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                    )
                }
            } else {
                Text(
                    "Pilih item di sebelah kiri",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
                )
            }
        }

        // Fee breakdown (only when there are order-level charges/discounts)
        if (anyItemSelected && hasFeeShare) {
            val feeShare = groupActualTotal - currentItemSubtotal
            Surface(
                shape    = MaterialTheme.shapes.small,
                color    = if (feeShare >= 0)
                    MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.4f)
                else
                    MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.3f),
                modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 10.dp, vertical = 6.dp),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        "Item: ${formatRupiah(currentItemSubtotal)}",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    val feeLabel = if (feeShare >= 0) "+ ${formatRupiah(feeShare)} biaya"
                                   else "− ${formatRupiah(-feeShare)} diskon"
                    Text(
                        feeLabel,
                        style = MaterialTheme.typography.labelSmall,
                        color = if (feeShare >= 0) MaterialTheme.colorScheme.secondary
                                else MaterialTheme.colorScheme.primary
                    )
                }
            }
        }

        // ── Method chips ──────────────────────────────────────────────────────
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

        Spacer(Modifier.height(8.dp))

        // ── Payment input per method ──────────────────────────────────────────
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
                            .padding(horizontal = 16.dp, vertical = 12.dp),
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
                        if (anyItemSelected && currentCashInput.isNotEmpty()) {
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

                // Quick fill based on groupActualTotal (includes fees)
                val quickAmounts = remember(groupActualTotal) {
                    listOf(
                        groupActualTotal,
                        ((groupActualTotal / 10_000) + 1) * 10_000,
                        ((groupActualTotal / 50_000) + 1) * 50_000
                    ).distinct().sorted()
                }
                Spacer(Modifier.height(4.dp))
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
                Spacer(Modifier.height(4.dp))

                // Numpad — weight(1f) fills remaining space
                PaymentNumpad(
                    modifier = Modifier.weight(1f),
                    onKey = { key ->
                        val current = currentCashInput
                        val next = when (key) {
                            "\u232b" -> current.dropLast(1)
                            "000"   -> if (current.isEmpty()) current else (current + "000").take(10)
                            else    -> if (current.isEmpty() && key == "0") current
                                       else (current + key).take(10)
                        }
                        onSetCashInput(next)
                    }
                )
            }

            PaymentMethod.QRIS -> {
                if (anyItemSelected) {
                    Card(
                        colors   = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.4f)
                        ),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth().padding(16.dp),
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
                                    formatRupiah(groupActualTotal),
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
                Spacer(Modifier.weight(1f))
            }

            else -> Spacer(Modifier.weight(1f))
        }

        Spacer(Modifier.height(8.dp))

        // ── Confirm buttons ───────────────────────────────────────────────────
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            OutlinedButton(
                onClick  = onConfirmGroup,
                enabled  = canConfirm,
                modifier = Modifier.weight(1f)
            ) { Text("Tambah Saja") }

            Button(
                onClick  = {
                    if (currentMethod == PaymentMethod.QRIS) showQrisConfirmDialog = true
                    else onConfirmAndPrint()
                },
                enabled  = canConfirm,
                modifier = Modifier.weight(1f),
                colors   = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.primary
                )
            ) {
                Icon(Icons.Default.Print, contentDescription = null, modifier = Modifier.size(16.dp))
                Spacer(Modifier.width(4.dp))
                Text("Bayar & Cetak")
            }
        }

        Spacer(Modifier.height(8.dp))

        // ── Process button ────────────────────────────────────────────────────
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
    // Prefer groupActualTotal (includes proportional fees) over raw item subtotal
    val expectedAmount = group.groupActualTotal.takeIf { it > 0 } ?: groupSubtotal
    val displayAmount  = if (group.method == PaymentMethod.CASH && group.cashPaid > 0)
        group.cashPaid else expectedAmount
    val changeAmount   = if (group.method == PaymentMethod.CASH && group.cashPaid > 0)
        group.cashPaid - expectedAmount else 0L

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
// ── Previews ─────────────────────────────────────────────────────────────────

@Preview(showBackground = true, widthDp = 800, heightDp = 600)
@Composable
private fun SplitPaymentPanelPreview_Empty() {
    RancakTheme {
        SplitPaymentPanel(
            items             = persistentListOf(
                SplitableItem(0, "Kopi Susu",    2, 18_000L),
                SplitableItem(1, "Croissant",    1, 22_000L),
                SplitableItem(2, "Es Teh",       1, 12_000L)
            ),
            splitGroups       = persistentListOf(),
            currentItemQtys   = persistentMapOf(),
            currentMethod     = PaymentMethod.CASH,
            currentCashInput  = "",
            orderTotal        = 70_000L,
            isProcessing      = false,
            onSetItemQty      = { _, _ -> },
            onSetMethod       = {},
            onSetCashInput    = {},
            onConfirmGroup    = {},
            onConfirmAndPrint = {},
            onRemoveGroup     = {},
            onProcess         = {},
            isSplit           = true,
            onToggleMode      = {},
            modifier          = Modifier.fillMaxSize()
        )
    }
}

@Preview(showBackground = true, widthDp = 800, heightDp = 600)
@Composable
private fun SplitPaymentPanelPreview_WithGroup() {
    RancakTheme {
        SplitPaymentPanel(
            items             = persistentListOf(
                SplitableItem(0, "Kopi Susu",    2, 18_000L),
                SplitableItem(1, "Croissant",    1, 22_000L),
                SplitableItem(2, "Es Teh",       1, 12_000L)
            ),
            splitGroups       = persistentListOf(
                SplitGroup(
                    id               = 1,
                    itemQtys         = mapOf(0 to 1, 2 to 1),
                    method           = PaymentMethod.CASH,
                    cashPaid         = 35_000L,
                    groupActualTotal = 30_000L
                )
            ),
            currentItemQtys   = persistentMapOf(1 to 1),
            currentMethod     = PaymentMethod.QRIS,
            currentCashInput  = "",
            orderTotal        = 70_000L,
            isProcessing      = false,
            onSetItemQty      = { _, _ -> },
            onSetMethod       = {},
            onSetCashInput    = {},
            onConfirmGroup    = {},
            onConfirmAndPrint = {},
            onRemoveGroup     = {},
            onProcess         = {},
            isSplit           = true,
            onToggleMode      = {},
            modifier          = Modifier.fillMaxSize()
        )
    }
}
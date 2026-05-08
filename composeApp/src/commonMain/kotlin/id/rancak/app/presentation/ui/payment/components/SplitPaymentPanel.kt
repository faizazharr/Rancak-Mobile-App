package id.rancak.app.presentation.ui.payment.components

import androidx.compose.foundation.layout.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import id.rancak.app.domain.model.PaymentMethod
import id.rancak.app.presentation.designsystem.RancakTheme
import id.rancak.app.presentation.viewmodel.SplitGroup
import id.rancak.app.presentation.viewmodel.SplitableItem
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.ImmutableMap
import kotlinx.collections.immutable.persistentListOf
import kotlinx.collections.immutable.persistentMapOf
import kotlinx.collections.immutable.toImmutableMap

/**
 * Orchestrator panel pembayaran terpisah (split payment).
 *
 * Terdiri dari dua kolom:
 * - Kiri  ([SplitItemColumn])  : pilih item, total, grup terkonfirmasi, mode toggle
 * - Kanan ([SplitPaymentColumn]): header pelanggan, input metode/cash/QRIS, konfirmasi, proses
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
                g.itemQtys.forEach { (idx, qty) -> put(idx, (get(idx) ?: 0) + qty) }
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
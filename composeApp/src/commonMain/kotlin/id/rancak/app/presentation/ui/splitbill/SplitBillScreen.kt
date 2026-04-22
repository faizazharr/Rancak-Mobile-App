package id.rancak.app.presentation.ui.splitbill

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CallSplit
import androidx.compose.material.icons.filled.CheckBox
import androidx.compose.material.icons.filled.CheckBoxOutlineBlank
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import id.rancak.app.domain.model.SaleItem
import id.rancak.app.domain.repository.SplitBillResult
import id.rancak.app.presentation.components.ErrorScreen
import id.rancak.app.presentation.components.LoadingScreen
import id.rancak.app.presentation.components.RancakButton
import id.rancak.app.presentation.components.RancakTopBar
import id.rancak.app.presentation.util.formatRupiah
import id.rancak.app.presentation.viewmodel.SplitBillUiState
import id.rancak.app.presentation.viewmodel.SplitBillViewModel
import org.koin.compose.viewmodel.koinViewModel

/**
 * Layar Split Bill — pilih item mana yang ingin dipindahkan ke tagihan baru.
 *
 * @param saleUuid UUID held order yang akan dipisah.
 * @param onBack kembali tanpa melakukan apa-apa.
 * @param onSplitComplete dipanggil setelah split berhasil dengan (originalUuid, newSaleUuid).
 */
@Composable
fun SplitBillScreen(
    saleUuid: String,
    onBack: () -> Unit,
    onSplitComplete: (originalUuid: String, newSaleUuid: String) -> Unit,
    viewModel: SplitBillViewModel = koinViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    LaunchedEffect(saleUuid) { viewModel.loadSale(saleUuid) }

    // Handle sukses
    LaunchedEffect(uiState.result) {
        uiState.result?.let { result ->
            onSplitComplete(result.original.uuid, result.newSale.uuid)
            viewModel.dismissResult()
        }
    }

    SplitBillContent(
        uiState       = uiState,
        onBack        = onBack,
        onToggle      = viewModel::toggleItem,
        onSelectAll   = viewModel::selectAll,
        onClear       = viewModel::clearSelection,
        onConfirm     = viewModel::confirmSplit,
        onDismissError = viewModel::clearError
    )
}

@Composable
private fun SplitBillContent(
    uiState: SplitBillUiState,
    onBack: () -> Unit,
    onToggle: (String) -> Unit,
    onSelectAll: () -> Unit,
    onClear: () -> Unit,
    onConfirm: () -> Unit,
    onDismissError: () -> Unit
) {
    Scaffold(
        topBar = {
            RancakTopBar(
                title    = "Split Tagihan",
                icon     = Icons.Default.CallSplit,
                subtitle = "Pilih item yang akan dipisahkan",
                onMenu   = onBack
            )
        },
        bottomBar = {
            if (uiState.sale != null) {
                SplitBillBottomBar(
                    uiState   = uiState,
                    onConfirm = onConfirm
                )
            }
        }
    ) { padding ->
        Box(modifier = Modifier.fillMaxSize().padding(padding)) {
            when {
                uiState.isLoading -> LoadingScreen()
                uiState.sale == null && uiState.error != null -> ErrorScreen(
                    message = uiState.error,
                    onRetry = onDismissError
                )
                uiState.sale != null -> SplitBillBody(
                    uiState     = uiState,
                    onToggle    = onToggle,
                    onSelectAll = onSelectAll,
                    onClear     = onClear
                )
            }
            // Error snackbar setelah sale loaded
            if (uiState.error != null && uiState.sale != null) {
                Snackbar(
                    modifier = Modifier.align(Alignment.BottomCenter).padding(16.dp),
                    action = {
                        TextButton(onClick = onDismissError) { Text("OK") }
                    }
                ) { Text(uiState.error) }
            }
        }
    }
}

@Composable
private fun SplitBillBody(
    uiState: SplitBillUiState,
    onToggle: (String) -> Unit,
    onSelectAll: () -> Unit,
    onClear: () -> Unit
) {
    Column(modifier = Modifier.fillMaxSize()) {
        // ── Selection header ──────────────────────────────────────────────────
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                "${uiState.selectedItemIds.size} dari ${uiState.availableItems.size} item dipilih",
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                TextButton(onClick = onSelectAll) { Text("Pilih Semua") }
                TextButton(onClick = onClear) { Text("Kosongkan") }
            }
        }

        HorizontalDivider()

        Row(modifier = Modifier.fillMaxSize()) {
            // ── Item list ─────────────────────────────────────────────────────
            LazyColumn(
                modifier = Modifier.weight(1f),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(uiState.availableItems, key = { it.uuid }) { item ->
                    SplitBillItemCard(
                        item       = item,
                        isSelected = item.uuid in uiState.selectedItemIds,
                        onToggle   = { onToggle(item.uuid) }
                    )
                }
            }

            // ── Summary panel ─────────────────────────────────────────────────
            Card(
                modifier = Modifier
                    .width(200.dp)
                    .fillMaxHeight()
                    .padding(8.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                )
            ) {
                Column(
                    modifier = Modifier.padding(16.dp).fillMaxHeight(),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text(
                        "Ringkasan",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold
                    )
                    HorizontalDivider()
                    SummaryItem(
                        label = "Tagihan baru",
                        amount = uiState.selectedTotal,
                        color = MaterialTheme.colorScheme.primary
                    )
                    SummaryItem(
                        label = "Tagihan sisa",
                        amount = uiState.remainingTotal,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    HorizontalDivider()
                    SummaryItem(
                        label = "Total",
                        amount = uiState.selectedTotal + uiState.remainingTotal,
                        color = MaterialTheme.colorScheme.onSurface,
                        bold = true
                    )
                }
            }
        }
    }
}

@Composable
private fun SummaryItem(label: String, amount: Long, color: androidx.compose.ui.graphics.Color, bold: Boolean = false) {
    Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
        Text(
            label,
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Text(
            formatRupiah(amount),
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = if (bold) FontWeight.Bold else FontWeight.Normal,
            color = color
        )
    }
}

@Composable
private fun SplitBillItemCard(
    item: SaleItem,
    isSelected: Boolean,
    onToggle: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth().clickable { onToggle() },
        border = if (isSelected)
            BorderStroke(2.dp, MaterialTheme.colorScheme.primary)
        else
            BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
        colors = CardDefaults.cardColors(
            containerColor = if (isSelected)
                MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f)
            else
                MaterialTheme.colorScheme.surface
        )
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Icon(
                imageVector = if (isSelected) Icons.Default.CheckBox else Icons.Default.CheckBoxOutlineBlank,
                contentDescription = null,
                tint = if (isSelected) MaterialTheme.colorScheme.primary
                       else MaterialTheme.colorScheme.onSurfaceVariant
            )
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    item.productName,
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Medium
                )
                if (!item.variantName.isNullOrBlank()) {
                    Text(
                        item.variantName,
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                if (!item.note.isNullOrBlank()) {
                    Text(
                        "📝 ${item.note}",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
            Column(horizontalAlignment = Alignment.End) {
                Text(
                    "×${item.qty}",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    formatRupiah(item.subtotal),
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.SemiBold
                )
            }
        }
    }
}

@Composable
private fun SplitBillBottomBar(
    uiState: SplitBillUiState,
    onConfirm: () -> Unit
) {
    Surface(
        shadowElevation = 8.dp,
        tonalElevation = 2.dp
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 12.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            if (uiState.selectedItemIds.isNotEmpty()) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        "Tagihan baru",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        formatRupiah(uiState.selectedTotal),
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            }
            if (!uiState.canSplit && uiState.selectedItemIds.size >= uiState.availableItems.size) {
                Text(
                    "Tidak bisa memindahkan semua item — minimal 1 item harus tersisa.",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.error
                )
            }
            RancakButton(
                text      = if (uiState.isLoading) "Memproses..." else "Pisahkan Tagihan",
                onClick   = onConfirm,
                enabled   = uiState.canSplit && !uiState.isLoading,
                modifier  = Modifier.fillMaxWidth()
            )
        }
    }
}

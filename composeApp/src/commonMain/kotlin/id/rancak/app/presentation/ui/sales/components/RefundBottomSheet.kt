package id.rancak.app.presentation.ui.sales.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import id.rancak.app.domain.model.Sale
import id.rancak.app.presentation.util.formatRupiah
import id.rancak.app.presentation.viewmodel.RefundLine
import id.rancak.app.presentation.viewmodel.RefundUiState
import id.rancak.app.presentation.viewmodel.RefundViewModel
import org.koin.compose.viewmodel.koinViewModel

/**
 * ModalBottomSheet untuk memproses refund — partial atau penuh.
 * Pemanggil cukup menyediakan [sale] dan callback dismiss/sukses.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RefundBottomSheet(
    sale: Sale,
    onDismiss: () -> Unit,
    onRefundSuccess: () -> Unit
) {
    val viewModel: RefundViewModel = koinViewModel()
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    LaunchedEffect(sale.uuid) {
        if (state.saleUuid != sale.uuid) viewModel.openFor(sale)
    }

    LaunchedEffect(state.completed) {
        if (state.completed != null) {
            onRefundSuccess()
            viewModel.reset()
        }
    }

    var showConfirm by remember { mutableStateOf(false) }

    ModalBottomSheet(
        onDismissRequest = {
            viewModel.reset()
            onDismiss()
        },
        sheetState = sheetState
    ) {
        RefundSheetContent(
            state          = state,
            onSetQty       = viewModel::setQty,
            onSetReason    = viewModel::setReason,
            onRefundFull   = viewModel::refundFull,
            onClearQty     = viewModel::clearQty,
            onSubmitClick  = { showConfirm = true },
            onDismissError = viewModel::clearError
        )
    }

    if (showConfirm) {
        AlertDialog(
            onDismissRequest = { showConfirm = false },
            title   = { Text("Konfirmasi Refund") },
            text    = {
                Text(
                    "Refund ${state.totalQty} item senilai " +
                        "${formatRupiah(state.totalRefund)}. " +
                        "Tindakan ini tidak dapat dibatalkan."
                )
            },
            confirmButton = {
                TextButton(onClick = {
                    showConfirm = false
                    viewModel.submit()
                }) { Text("Refund Sekarang") }
            },
            dismissButton = {
                TextButton(onClick = { showConfirm = false }) { Text("Batal") }
            }
        )
    }
}

@Composable
private fun RefundSheetContent(
    state: RefundUiState,
    onSetQty: (String, Int) -> Unit,
    onSetReason: (String) -> Unit,
    onRefundFull: () -> Unit,
    onClearQty: () -> Unit,
    onSubmitClick: () -> Unit,
    onDismissError: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp)
            .padding(bottom = 16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Text(
            "Refund Item",
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold
        )
        if (state.invoiceNo != null) {
            Text(
                "Invoice ${state.invoiceNo}",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        // Quick actions
        Row(
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            AssistChip(
                onClick = onRefundFull,
                label   = { Text("Refund Penuh") }
            )
            AssistChip(
                onClick = onClearQty,
                label   = { Text("Reset") }
            )
        }

        HorizontalDivider()

        // Items list
        LazyColumn(
            modifier = Modifier.heightIn(max = 360.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(state.lines, key = { it.saleItemUuid }) { line ->
                RefundLineRow(line = line, onSetQty = onSetQty)
            }
        }

        HorizontalDivider()

        // Reason
        OutlinedTextField(
            value         = state.reason,
            onValueChange = onSetReason,
            label         = { Text("Alasan refund (opsional)") },
            modifier      = Modifier.fillMaxWidth(),
            maxLines      = 3
        )

        // Total
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment     = Alignment.CenterVertically
        ) {
            Column {
                Text("Total Refund", style = MaterialTheme.typography.bodySmall)
                Text(
                    formatRupiah(state.totalRefund),
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
            }
            Text(
                "${state.totalQty} item",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        if (state.error != null) {
            Card(
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.errorContainer
                )
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth().padding(12.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        state.error,
                        modifier = Modifier.weight(1f),
                        color = MaterialTheme.colorScheme.onErrorContainer
                    )
                    TextButton(onClick = onDismissError) { Text("Tutup") }
                }
            }
        }

        Button(
            onClick  = onSubmitClick,
            enabled  = state.canSubmit,
            modifier = Modifier.fillMaxWidth().heightIn(min = 48.dp)
        ) {
            if (state.isProcessing) {
                CircularProgressIndicator(
                    modifier = Modifier.size(20.dp),
                    strokeWidth = 2.dp,
                    color = MaterialTheme.colorScheme.onPrimary
                )
            } else {
                Icon(Icons.Default.CheckCircle, contentDescription = null)
                Spacer(Modifier.width(8.dp))
                Text("Proses Refund")
            }
        }
    }
}

@Composable
private fun RefundLineRow(
    line: RefundLine,
    onSetQty: (String, Int) -> Unit
) {
    Card(
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Column(
            modifier = Modifier.padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        line.productName,
                        style = MaterialTheme.typography.bodyLarge,
                        fontWeight = FontWeight.SemiBold
                    )
                    if (line.variantName != null) {
                        Text(
                            line.variantName,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    Text(
                        "${formatRupiah(line.unitPrice)} × ${line.maxQty}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                if (line.qtyToRefund > 0) {
                    Text(
                        formatRupiah(line.lineRefund),
                        style = MaterialTheme.typography.bodyLarge,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            }

            // Qty stepper
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment     = Alignment.CenterVertically
            ) {
                Text(
                    "Qty refund",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    IconButton(
                        onClick = { onSetQty(line.saleItemUuid, line.qtyToRefund - 1) },
                        enabled = line.qtyToRefund > 0,
                        modifier = Modifier.size(40.dp)
                    ) { Icon(Icons.Default.Remove, contentDescription = "Kurangi") }

                    Box(
                        modifier = Modifier
                            .widthIn(min = 40.dp)
                            .clip(RoundedCornerShape(8.dp))
                            .background(MaterialTheme.colorScheme.surface)
                            .padding(horizontal = 12.dp, vertical = 6.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            "${line.qtyToRefund}",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    IconButton(
                        onClick = { onSetQty(line.saleItemUuid, line.qtyToRefund + 1) },
                        enabled = line.qtyToRefund < line.maxQty,
                        modifier = Modifier.size(40.dp)
                    ) { Icon(Icons.Default.Add, contentDescription = "Tambah") }
                }
            }
        }
    }
}

package id.rancak.app.presentation.ui.finance.components

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.TrendingUp
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import id.rancak.app.presentation.components.RancakButton
import id.rancak.app.presentation.components.RancakOutlinedButton
import id.rancak.app.presentation.components.RancakTextField
import id.rancak.app.presentation.ui.finance.CashExpenseActions
import id.rancak.app.presentation.viewmodel.CashExpenseUiState

@Composable
fun CashInFormDialog(
    uiState: CashExpenseUiState,
    actions: CashExpenseActions
) {
    if (!uiState.showCashInForm) return

    val amountNum = uiState.formAmount.toLongOrNull()
    val amountError = when {
        uiState.formAmount.isBlank() -> null
        amountNum == null -> "Jumlah tidak valid"
        amountNum <= 0 -> "Jumlah harus lebih dari 0"
        else -> null
    }
    val canSubmit = uiState.formAmount.isNotBlank() && amountError == null &&
        uiState.formSource.isNotBlank()

    AlertDialog(
        onDismissRequest = actions.onToggleCashInForm,
        icon = {
            Icon(
                imageVector = Icons.Default.TrendingUp,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(28.dp)
            )
        },
        title = {
            Text(
                text = "Tambah Kas Masuk",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )
        },
        text = {
            Column(
                modifier = Modifier.verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                RancakTextField(
                    value           = uiState.formAmount,
                    onValueChange   = actions.onAmountChange,
                    label           = "Jumlah (Rp) *",
                    isError         = amountError != null,
                    errorMessage    = amountError,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                )
                RancakTextField(
                    value         = uiState.formSource,
                    onValueChange = actions.onSourceChange,
                    label         = "Sumber *",
                    isError       = uiState.formSource.isBlank() && uiState.formAmount.isNotBlank(),
                    errorMessage  = if (uiState.formSource.isBlank() && uiState.formAmount.isNotBlank()) "Sumber wajib diisi" else null
                )
                RancakTextField(
                    value         = uiState.formDescription,
                    onValueChange = actions.onDescriptionChange,
                    label         = "Keterangan"
                )
                RancakTextField(
                    value         = uiState.formNote,
                    onValueChange = actions.onNoteChange,
                    label         = "Catatan (opsional)"
                )
            }
        },
        confirmButton = {
            RancakButton(text = "Simpan", onClick = actions.onSubmitCashIn, enabled = canSubmit)
        },
        dismissButton = {
            RancakOutlinedButton(text = "Batal", onClick = actions.onToggleCashInForm)
        },
        shape = MaterialTheme.shapes.large
    )
}

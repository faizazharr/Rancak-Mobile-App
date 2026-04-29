package id.rancak.app.presentation.ui.finance.components

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
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
fun ExpenseForm(
    uiState: CashExpenseUiState,
    actions: CashExpenseActions
) {
    val amountNum = uiState.formAmount.toLongOrNull()
    val amountError = when {
        uiState.formAmount.isBlank() -> null
        amountNum == null -> "Jumlah tidak valid"
        amountNum <= 0 -> "Jumlah harus lebih dari 0"
        else -> null
    }
    val canSubmit = uiState.formAmount.isNotBlank() && amountError == null &&
        uiState.formDescription.isNotBlank()

    Card(Modifier.fillMaxWidth().padding(12.dp)) {
        Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Text("Tambah Pengeluaran", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
            RancakTextField(
                value         = uiState.formAmount,
                onValueChange = actions.onAmountChange,
                label         = "Jumlah (Rp) *",
                isError       = amountError != null,
                errorMessage  = amountError,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
            )
            RancakTextField(
                value         = uiState.formDescription,
                onValueChange = actions.onDescriptionChange,
                label         = "Keterangan *",
                isError       = uiState.formDescription.isBlank() && uiState.formAmount.isNotBlank(),
                errorMessage  = if (uiState.formDescription.isBlank() && uiState.formAmount.isNotBlank()) "Keterangan wajib diisi" else null
            )
            RancakTextField(value = uiState.formNote, onValueChange = actions.onNoteChange, label = "Catatan (opsional)")
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                RancakOutlinedButton("Batal",  onClick = actions.onToggleExpenseForm, modifier = Modifier.weight(1f))
                RancakButton       ("Simpan", onClick = actions.onSubmitExpense,     modifier = Modifier.weight(1f), enabled = canSubmit)
            }
        }
    }
}

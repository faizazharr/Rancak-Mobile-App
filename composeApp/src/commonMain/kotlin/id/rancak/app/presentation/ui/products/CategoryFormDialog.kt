package id.rancak.app.presentation.ui.products

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import id.rancak.app.domain.model.Category
import id.rancak.app.presentation.designsystem.RancakTheme

@Composable
fun CategoryFormDialog(
    editingCategory: Category?,
    isSubmitting: Boolean,
    onDismiss: () -> Unit,
    onConfirm: (name: String, description: String?) -> Unit
) {
    var name        by remember(editingCategory) { mutableStateOf(editingCategory?.name ?: "") }
    var description by remember(editingCategory) { mutableStateOf(editingCategory?.description ?: "") }

    val canConfirm = !isSubmitting && name.isNotBlank()

    AlertDialog(
        onDismissRequest = { if (!isSubmitting) onDismiss() },
        title = { Text(if (editingCategory == null) "Tambah Kategori" else "Edit Kategori") },
        text  = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                OutlinedTextField(
                    value         = name,
                    onValueChange = { name = it },
                    label         = { Text("Nama Kategori *") },
                    modifier      = Modifier.fillMaxWidth(),
                    singleLine    = true,
                    isError       = name.isBlank()
                )
                OutlinedTextField(
                    value         = description,
                    onValueChange = { description = it },
                    label         = { Text("Deskripsi") },
                    modifier      = Modifier.fillMaxWidth(),
                    maxLines      = 3
                )
            }
        },
        confirmButton = {
            Button(
                onClick  = { onConfirm(name.trim(), description.ifBlank { null }) },
                enabled  = canConfirm
            ) {
                if (isSubmitting) CircularProgressIndicator(Modifier.size(16.dp), strokeWidth = 2.dp)
                else Text("Simpan")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss, enabled = !isSubmitting) { Text("Batal") }
        }
    )
}

// ── Preview ───────────────────────────────────────────────────────────────────

@Preview
@Composable
private fun CategoryFormDialogAddPreview() {
    RancakTheme {
        CategoryFormDialog(
            editingCategory = null,
            isSubmitting    = false,
            onDismiss       = {},
            onConfirm       = { _, _ -> }
        )
    }
}

@Preview
@Composable
private fun CategoryFormDialogEditPreview() {
    RancakTheme {
        CategoryFormDialog(
            editingCategory = Category("c1", "Makanan", "Aneka makanan berat"),
            isSubmitting    = false,
            onDismiss       = {},
            onConfirm       = { _, _ -> }
        )
    }
}

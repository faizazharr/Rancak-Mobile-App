package id.rancak.app.presentation.ui.products

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Category
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import id.rancak.app.domain.model.Category
import id.rancak.app.presentation.components.RancakFormDialog
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

    RancakFormDialog(
        icon             = Icons.Default.Category,
        title            = if (editingCategory == null) "Tambah Kategori" else "Edit Kategori",
        subtitle         = if (editingCategory == null) "Buat kategori produk baru" else "Perbarui informasi kategori",
        onDismissRequest = onDismiss,
        confirmLabel     = "Simpan",
        onConfirm        = { onConfirm(name.trim(), description.ifBlank { null }) },
        confirmEnabled   = canConfirm,
        isSubmitting     = isSubmitting
    ) {
        OutlinedTextField(
            value         = name,
            onValueChange = { name = it },
            label         = { Text("Nama Kategori *") },
            modifier      = Modifier.fillMaxWidth(),
            singleLine    = true,
            isError       = name.isBlank(),
            shape         = MaterialTheme.shapes.medium
        )
        OutlinedTextField(
            value         = description,
            onValueChange = { description = it },
            label         = { Text("Deskripsi") },
            modifier      = Modifier.fillMaxWidth(),
            maxLines      = 3,
            shape         = MaterialTheme.shapes.medium
        )
    }
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

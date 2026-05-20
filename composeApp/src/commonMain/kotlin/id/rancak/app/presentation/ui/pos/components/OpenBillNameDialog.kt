package id.rancak.app.presentation.ui.pos.components

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.BookmarkAdd
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.text.input.ImeAction
import id.rancak.app.presentation.components.RancakFormDialog

/**
 * Dialog untuk memasukkan nama open bill sebelum menyimpan keranjang.
 *
 * @param initialName  Nama tagihan awal (kosong = buat baru).
 * @param isUpdate     True jika ini memperbarui open bill yang sudah ada.
 * @param onConfirm    Dipanggil dengan nama tagihan saat kasir menekan "Simpan".
 * @param onDismiss    Dipanggil saat kasir membatalkan / menutup dialog.
 */
@Composable
internal fun OpenBillNameDialog(
    initialName: String = "",
    isUpdate: Boolean = false,
    onConfirm: (name: String) -> Unit,
    onDismiss: () -> Unit
) {
    var name           by remember(initialName) { mutableStateOf(initialName) }
    val focusRequester = remember { FocusRequester() }

    LaunchedEffect(Unit) {
        focusRequester.requestFocus()
    }

    RancakFormDialog(
        icon             = Icons.Default.BookmarkAdd,
        title            = if (isUpdate) "Perbarui Open Bill" else "Simpan sebagai Open Bill",
        subtitle         = "Beri nama pada tagihan ini",
        onDismissRequest = onDismiss,
        confirmLabel     = if (isUpdate) "Perbarui" else "Simpan",
        onConfirm        = { onConfirm(name.trim()) },
        confirmEnabled   = name.isNotBlank()
    ) {
        Text(
            "Beri nama pada tagihan ini agar mudah ditemukan kembali.",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        OutlinedTextField(
            value            = name,
            onValueChange    = { name = it },
            label            = { Text("Nama tagihan") },
            placeholder      = { Text("cth. Meja 5 – Andi", color = MaterialTheme.colorScheme.onSurfaceVariant.copy(0.5f)) },
            singleLine       = true,
            shape            = MaterialTheme.shapes.medium,
            modifier         = Modifier
                .fillMaxWidth()
                .focusRequester(focusRequester),
            keyboardOptions  = KeyboardOptions(imeAction = ImeAction.Done),
            keyboardActions  = KeyboardActions(
                onDone = { if (name.isNotBlank()) onConfirm(name.trim()) }
            )
        )
    }
}

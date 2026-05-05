package id.rancak.app.presentation.ui.pos.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.BookmarkAdd
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp

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
    var name         by remember(initialName) { mutableStateOf(initialName) }
    val focusRequester = remember { FocusRequester() }
    val amber = Color(0xFFF59E0B)

    LaunchedEffect(Unit) {
        focusRequester.requestFocus()
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        icon = {
            Icon(
                imageVector = Icons.Default.BookmarkAdd,
                contentDescription = null,
                tint = amber
            )
        },
        title = {
            Text(
                text       = if (isUpdate) "Perbarui Open Bill" else "Simpan sebagai Open Bill",
                fontWeight = FontWeight.Bold
            )
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(
                    "Beri nama pada tagihan ini agar mudah ditemukan kembali.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(Modifier.height(4.dp))
                OutlinedTextField(
                    value            = name,
                    onValueChange    = { name = it },
                    label            = { Text("Nama tagihan") },
                    placeholder      = { Text("cth. Meja 5 – Andi", color = MaterialTheme.colorScheme.onSurfaceVariant.copy(0.5f)) },
                    singleLine       = true,
                    shape            = RoundedCornerShape(10.dp),
                    modifier         = Modifier
                        .fillMaxWidth()
                        .focusRequester(focusRequester),
                    keyboardOptions  = KeyboardOptions(imeAction = ImeAction.Done),
                    keyboardActions  = KeyboardActions(
                        onDone = { if (name.isNotBlank()) onConfirm(name.trim()) }
                    ),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor   = amber,
                        focusedLabelColor    = amber,
                        cursorColor          = amber
                    )
                )
            }
        },
        confirmButton = {
            Button(
                onClick  = { onConfirm(name.trim()) },
                enabled  = name.isNotBlank(),
                colors   = ButtonDefaults.buttonColors(containerColor = amber, contentColor = Color.White),
                shape    = RoundedCornerShape(8.dp)
            ) {
                Text(if (isUpdate) "Perbarui" else "Simpan", fontWeight = FontWeight.SemiBold)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Batal") }
        },
        shape = RoundedCornerShape(16.dp)
    )
}

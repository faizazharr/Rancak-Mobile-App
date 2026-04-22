package id.rancak.app.presentation.components

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable

/**
 * Dialog peringatan saat [id.rancak.app.data.security.DeviceIntegrity]
 * mendeteksi kemungkinan device rooted / jailbroken.
 *
 * Hanya peringatan — user tetap dapat menggunakan app. Tujuannya adalah
 * memberi kesadaran bahwa data transaksi di device tersebut mungkin lebih
 * rentan terhadap akses tidak sah oleh pihak lain yang memiliki akses
 * administratif ke device.
 */
@Composable
fun DeviceIntegrityWarningDialog(onDismiss: () -> Unit) {
    AlertDialog(
        onDismissRequest = onDismiss,
        icon = {
            Icon(
                imageVector = Icons.Default.Warning,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.error
            )
        },
        title = { Text("Perangkat Tidak Aman") },
        text = {
            Text(
                "Perangkat Anda terdeteksi rooted atau jailbroken. Data aplikasi " +
                    "mungkin lebih mudah diakses oleh aplikasi lain. Harap gunakan " +
                    "perangkat resmi perusahaan untuk transaksi produksi."
            )
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text("Saya Mengerti")
            }
        }
    )
}

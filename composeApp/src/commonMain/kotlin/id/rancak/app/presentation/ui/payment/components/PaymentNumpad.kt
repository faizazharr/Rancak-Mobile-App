package id.rancak.app.presentation.ui.payment.components

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Backspace
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ElevatedButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import id.rancak.app.presentation.designsystem.RancakTheme

/**
 * Numpad khusus untuk input jumlah bayar tunai pada halaman pembayaran.
 * Dipakai oleh [PaymentFormContent].
 */
@Composable
internal fun PaymentNumpad(
    onKey: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val keys = listOf(
        listOf("1",   "2", "3"),
        listOf("4",   "5", "6"),
        listOf("7",   "8", "9"),
        listOf("000", "0", "⌫")
    )

    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        keys.forEach { row ->
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                row.forEach { key -> PaymentNumpadKey(key = key, onClick = { onKey(key) }) }
            }
        }
    }
}

/** Single elevated key; renders a backspace icon for "⌫". */
@Composable
private fun RowScope.PaymentNumpadKey(key: String, onClick: () -> Unit) {
    val isBackspace = key == "⌫"
    ElevatedButton(
        onClick  = onClick,
        modifier = Modifier.weight(1f).height(56.dp),
        shape    = MaterialTheme.shapes.medium,
        colors   = ButtonDefaults.elevatedButtonColors(
            containerColor = if (isBackspace)
                MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.5f)
            else MaterialTheme.colorScheme.surface,
            contentColor = if (isBackspace)
                MaterialTheme.colorScheme.error
            else MaterialTheme.colorScheme.onSurface
        ),
        elevation = ButtonDefaults.elevatedButtonElevation(
            defaultElevation = 1.dp,
            pressedElevation = 0.dp
        ),
        contentPadding = PaddingValues(0.dp)
    ) {
        if (isBackspace) {
            Icon(
                Icons.AutoMirrored.Filled.Backspace,
                contentDescription = "Hapus",
                modifier = Modifier.size(20.dp)
            )
        } else {
            Text(
                key,
                style      = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Medium
            )
        }
    }
}

@Preview
@Composable
private fun PaymentNumpadPreview() {
    RancakTheme {
        Box(Modifier.padding(12.dp)) {
            PaymentNumpad(onKey = {})
        }
    }
}

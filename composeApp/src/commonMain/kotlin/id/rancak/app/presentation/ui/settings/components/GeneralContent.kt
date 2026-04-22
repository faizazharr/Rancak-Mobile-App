package id.rancak.app.presentation.ui.settings.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import id.rancak.app.presentation.designsystem.RancakTheme

/**
 * Panel pengaturan umum — mengaktifkan auto-print setelah bayar serta memilih
 * lebar kertas (58 mm vs 80 mm).
 */
@Composable
internal fun GeneralContent(
    autoPrint: Boolean,
    paperWidth: Int,
    onAutoPrint: (Boolean) -> Unit,
    onPaperWidth: (Int) -> Unit
) {
    SettingsCard {
        Row(
            modifier              = Modifier.fillMaxWidth(),
            verticalAlignment     = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    "Auto Print",
                    style      = MaterialTheme.typography.bodySmall,
                    fontWeight = FontWeight.SemiBold
                )
                Text(
                    "Cetak struk otomatis setelah pembayaran berhasil",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            Switch(checked = autoPrint, onCheckedChange = onAutoPrint)
        }
    }

    SettingsCard {
        Text(
            "Lebar Kertas",
            style      = MaterialTheme.typography.labelMedium,
            fontWeight = FontWeight.SemiBold,
            color      = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Spacer(Modifier.height(6.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            PrimaryFilterChip(
                selected = paperWidth == 58,
                onClick  = { onPaperWidth(58) },
                label    = { Text("58 mm", style = MaterialTheme.typography.labelMedium) },
                modifier = Modifier.weight(1f)
            )
            PrimaryFilterChip(
                selected = paperWidth == 80,
                onClick  = { onPaperWidth(80) },
                label    = { Text("80 mm", style = MaterialTheme.typography.labelMedium) },
                modifier = Modifier.weight(1f)
            )
        }
    }
}

@Preview
@Composable
private fun GeneralContentPreview() {
    RancakTheme {
        Column(
            modifier            = Modifier.padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            GeneralContent(
                autoPrint    = true,
                paperWidth   = 58,
                onAutoPrint  = {},
                onPaperWidth = {}
            )
        }
    }
}

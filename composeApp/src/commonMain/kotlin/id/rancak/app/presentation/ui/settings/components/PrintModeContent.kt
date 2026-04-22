package id.rancak.app.presentation.ui.settings.components

import androidx.compose.foundation.layout.*
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import id.rancak.app.data.printing.PrintMode
import id.rancak.app.presentation.designsystem.RancakTheme

/**
 * Panel pemilihan mode cetak — menentukan apakah sistem akan mencetak struk
 * saja, struk + KOT, atau bagaimana urutannya saat printer hanya satu.
 */
@Composable
internal fun PrintModeContent(
    printMode: PrintMode,
    onPrintMode: (PrintMode) -> Unit
) {
    val modes = listOf(
        PrintMode.RECEIPT_ONLY         to ("Struk Kasir Saja"          to "Hanya cetak struk kasir, tanpa KOT dapur"),
        PrintMode.DUAL_PRINTER         to ("Dua Printer"               to "Struk kasir ke printer kasir, KOT ke printer dapur secara bersamaan"),
        PrintMode.SINGLE_KOT_FIRST     to ("Satu Printer — KOT Dulu"   to "Cetak KOT dapur dulu, lalu struk kasir"),
        PrintMode.SINGLE_RECEIPT_FIRST to ("Satu Printer — Struk Dulu" to "Cetak struk kasir dulu, lalu KOT dapur")
    )

    SettingsCard {
        Text(
            "Tentukan bagaimana struk dan tiket dapur (KOT) dicetak setelah pembayaran.",
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Spacer(Modifier.height(8.dp))
        modes.forEachIndexed { idx, (mode, labelDesc) ->
            val (label, desc) = labelDesc
            PrintModeRow(
                mode        = mode,
                label       = label,
                description = desc,
                isSelected  = printMode == mode,
                onSelect    = onPrintMode
            )
            if (idx < modes.lastIndex) Spacer(Modifier.height(4.dp))
        }
    }
}

@Composable
private fun PrintModeRow(
    mode: PrintMode,
    label: String,
    description: String,
    isSelected: Boolean,
    onSelect: (PrintMode) -> Unit
) {
    Surface(
        onClick  = { onSelect(mode) },
        shape    = MaterialTheme.shapes.small,
        color    = if (isSelected)
            MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f)
        else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier.padding(10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            RadioButton(
                selected = isSelected,
                onClick  = { onSelect(mode) },
                modifier = Modifier.size(20.dp)
            )
            Spacer(Modifier.width(10.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    label,
                    style      = MaterialTheme.typography.bodySmall,
                    fontWeight = FontWeight.SemiBold
                )
                Text(
                    description,
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@Preview
@Composable
private fun PrintModeContentPreview() {
    RancakTheme {
        Column(modifier = Modifier.padding(12.dp)) {
            PrintModeContent(
                printMode  = PrintMode.DUAL_PRINTER,
                onPrintMode = {}
            )
        }
    }
}

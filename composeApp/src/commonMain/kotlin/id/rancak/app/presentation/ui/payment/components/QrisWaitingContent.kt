package id.rancak.app.presentation.ui.payment.components

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.HourglassTop
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import id.rancak.app.presentation.components.QrisQrCode
import id.rancak.app.presentation.designsystem.RancakTheme

/**
 * Komponen inline QRIS — tampil langsung di dalam kolom kanan halaman pembayaran,
 * bukan sebagai layar terpisah atau dialog.
 */
@Composable
internal fun QrisWaitingContent(
    qrString: String,
    amount: Long,
    isPolling: Boolean,
    onCancel: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .verticalScroll(rememberScrollState())
            .padding(vertical = 8.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        QrisQrCode(
            qrString = qrString,
            size     = 200.dp,
            label    = "Scan dengan aplikasi bank atau e-wallet"
        )

        Surface(
            shape = MaterialTheme.shapes.medium,
            color = MaterialTheme.colorScheme.secondaryContainer,
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 10.dp),
                verticalAlignment     = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                if (isPolling) {
                    CircularProgressIndicator(
                        modifier    = Modifier.size(16.dp),
                        strokeWidth = 2.dp,
                        color       = MaterialTheme.colorScheme.onSecondaryContainer
                    )
                } else {
                    Icon(
                        Icons.Default.HourglassTop, contentDescription = null,
                        tint     = MaterialTheme.colorScheme.onSecondaryContainer,
                        modifier = Modifier.size(16.dp)
                    )
                }
                Text(
                    if (isPolling) "Menunggu konfirmasi pembayaran..." else "Memuat QR...",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSecondaryContainer
                )
            }
        }

        HorizontalDivider()

        OutlinedButton(
            onClick  = onCancel,
            modifier = Modifier.fillMaxWidth(),
            colors   = ButtonDefaults.outlinedButtonColors(
                contentColor = MaterialTheme.colorScheme.error
            )
        ) {
            Icon(Icons.Default.Close, contentDescription = null, modifier = Modifier.size(16.dp))
            Spacer(Modifier.width(6.dp))
            Text("Batalkan Pembayaran")
        }
    }
}


@Preview
@Composable
private fun QrisWaitingPreview_Polling() {
    RancakTheme {
        QrisWaitingContent(
            qrString  = "00020101021126...preview-qr",
            amount    = 85_000L,
            isPolling = true,
            onCancel  = {},
            modifier  = Modifier.fillMaxSize()
        )
    }
}

@Preview
@Composable
private fun QrisWaitingPreview_Loading() {
    RancakTheme {
        QrisWaitingContent(
            qrString  = "",
            amount    = 42_000L,
            isPolling = false,
            onCancel  = {},
            modifier  = Modifier.fillMaxSize()
        )
    }
}

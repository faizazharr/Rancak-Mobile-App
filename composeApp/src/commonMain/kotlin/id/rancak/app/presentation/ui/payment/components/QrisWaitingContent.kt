package id.rancak.app.presentation.ui.payment.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.HourglassTop
import androidx.compose.material.icons.filled.QrCode2
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import id.rancak.app.presentation.components.QrisQrCode
import id.rancak.app.presentation.designsystem.RancakTheme
import id.rancak.app.presentation.util.formatRupiah

/**
 * Layar tunggu pembayaran QRIS — menampilkan QR code, status polling, dan
 * tombol batal. Dipakai oleh [id.rancak.app.presentation.ui.payment.PaymentScreen]
 * saat state `isQrisWaiting = true`.
 */
@Composable
internal fun QrisWaitingContent(
    qrString: String,
    amount: Long,
    isPolling: Boolean,
    onCancel: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier.background(MaterialTheme.colorScheme.surface),
        contentAlignment = Alignment.Center
    ) {
        Card(
            modifier = Modifier
                .widthIn(max = 440.dp)
                .fillMaxWidth(0.9f),
            shape     = MaterialTheme.shapes.extraLarge,
            elevation = CardDefaults.cardElevation(defaultElevation = 6.dp)
        ) {
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                QrisHeader(amount = amount)
                QrisBody(
                    qrString  = qrString,
                    isPolling = isPolling,
                    onCancel  = onCancel
                )
            }
        }
    }
}

@Composable
private fun QrisHeader(amount: Long) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .background(MaterialTheme.colorScheme.primary)
            .padding(vertical = 20.dp, horizontal = 24.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Icon(
                imageVector        = Icons.Default.QrCode2,
                contentDescription = null,
                tint     = MaterialTheme.colorScheme.onPrimary,
                modifier = Modifier.size(32.dp)
            )
            Spacer(Modifier.height(8.dp))
            Text(
                "Scan QRIS untuk Bayar",
                style      = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color      = MaterialTheme.colorScheme.onPrimary
            )
            Spacer(Modifier.height(4.dp))
            Text(
                formatRupiah(amount),
                style      = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.ExtraBold,
                color      = MaterialTheme.colorScheme.onPrimary
            )
        }
    }
}

@Composable
private fun QrisBody(
    qrString: String,
    isPolling: Boolean,
    onCancel: () -> Unit
) {
    Column(
        modifier = Modifier.padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        QrisQrCode(
            qrString = qrString,
            size     = 240.dp,
            label    = "Scan dengan aplikasi bank atau e-wallet"
        )

        Surface(
            shape = MaterialTheme.shapes.medium,
            color = MaterialTheme.colorScheme.secondaryContainer
        ) {
            Row(
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 10.dp),
                verticalAlignment     = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                if (isPolling) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(16.dp),
                        strokeWidth = 2.dp,
                        color = MaterialTheme.colorScheme.onSecondaryContainer
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
            onClick = onCancel,
            modifier = Modifier.fillMaxWidth(),
            colors = ButtonDefaults.outlinedButtonColors(
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

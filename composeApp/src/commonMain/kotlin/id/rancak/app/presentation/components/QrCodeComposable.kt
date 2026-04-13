package id.rancak.app.presentation.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.foundation.Image
import io.github.alexzhirkevich.qrose.options.*
import io.github.alexzhirkevich.qrose.rememberQrCodePainter

/**
 * Displays a QR code for QRIS / Xendit payment flow.
 *
 * @param qrString The raw QR string returned by the payment gateway.
 * @param size     The display size of the QR code.
 * @param label    Optional label shown below the QR code.
 */
@Composable
fun QrisQrCode(
    qrString: String,
    size: Dp = 240.dp,
    label: String? = null
) {
    val painter = rememberQrCodePainter(data = qrString) {
        shapes {
            ball  = QrBallShape.circle()
            frame = QrFrameShape.roundCorners(0.25f)
            darkPixel = QrPixelShape.roundCorners()
        }
        colors {
            dark  = QrBrush.solid(Color(0xFF1A1A2E))
            light = QrBrush.solid(Color.White)
        }
    }

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .background(Color.White, RoundedCornerShape(12.dp))
            .padding(16.dp)
    ) {
        Image(
            painter = painter,
            contentDescription = "QRIS Payment Code",
            modifier = Modifier.size(size)
        )
        if (label != null) {
            Spacer(Modifier.height(8.dp))
            Text(
                text = label,
                style = MaterialTheme.typography.bodySmall,
                fontWeight = FontWeight.Medium,
                color = Color(0xFF1A1A2E)
            )
        }
    }
}

/**
 * Full QRIS payment dialog panel — shows QR, amount, and status.
 *
 * @param qrString   Raw QR data from Xendit.
 * @param amount     Payment amount in IDR (displayed as Rp formatted).
 * @param onDismiss  Called when the user cancels or payment is confirmed.
 */
@Composable
fun QrisPaymentPanel(
    qrString: String,
    amount: Long,
    onDismiss: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color.White, RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp))
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Text(
            text = "Scan QRIS untuk Bayar",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold
        )

        Text(
            text = "Rp ${"%,d".format(amount).replace(',', '.')}",
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary
        )

        QrisQrCode(
            qrString = qrString,
            size = 260.dp,
            label = "Scan dengan aplikasi bank atau e-wallet"
        )

        Text(
            text = "Menunggu konfirmasi pembayaran...",
            style = MaterialTheme.typography.bodySmall,
            color = Color.Gray
        )

        androidx.compose.material3.TextButton(onClick = onDismiss) {
            Text("Batalkan Pembayaran")
        }
    }
}

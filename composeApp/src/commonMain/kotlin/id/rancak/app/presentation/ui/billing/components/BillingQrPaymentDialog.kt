package id.rancak.app.presentation.ui.billing.components

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.CircleShape

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Cancel
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.QrCode
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import id.rancak.app.domain.model.Invoice
import id.rancak.app.presentation.components.QrisQrCode
import id.rancak.app.presentation.designsystem.Primary
import id.rancak.app.presentation.ui.billing.formatPlanPrice

/**
 * Dialog pembayaran QRIS untuk billing subscription.
 *
 * Ditampilkan segera setelah invoice berhasil dibuat oleh backend dan QR string
 * tersedia. Polling status invoice berjalan di [BillingViewModel] setiap 2 detik;
 * dialog ini hanya menampilkan state terkini.
 *
 * @param invoice     Invoice yang baru dibuat (berisi [Invoice.qrString]).
 * @param isPolling   True saat ViewModel sedang polling — tampilkan indikator.
 * @param onDismiss   Dipanggil saat user menekan "Tutup" (batalkan menunggu).
 */
@Composable
fun BillingQrPaymentDialog(
    invoice: Invoice,
    isPolling: Boolean,
    onDismiss: () -> Unit
) {
    val qrString = invoice.qrString ?: return

    AlertDialog(
        onDismissRequest = { /* tidak bisa tutup dengan tap-luar — harus tekan tombol */ },
        icon = {
            Icon(
                Icons.Default.QrCode,
                contentDescription = null,
                tint = Primary,
                modifier = Modifier.size(28.dp)
            )
        },
        title = {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    "Selesaikan Pembayaran",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.Center
                )
                Spacer(Modifier.height(2.dp))
                Text(
                    invoice.planName,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center
                )
            }
        },
        text = {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(16.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                // ── Jumlah ───────────────────────────────────────────────────
                Surface(
                    shape = MaterialTheme.shapes.extraLarge,
                    color = Primary.copy(alpha = 0.08f),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier.padding(12.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(2.dp)
                    ) {
                        Text(
                            formatPlanPrice(invoice.totalAmount),
                            style = MaterialTheme.typography.headlineSmall,
                            fontWeight = FontWeight.ExtraBold,
                            color = Primary
                        )
                        if (invoice.taxAmount > 0) {
                            Text(
                                "Sudah termasuk pajak ${(invoice.taxRate * 100).toInt()}%",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(4.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Surface(
                                shape = MaterialTheme.shapes.small,
                                color = MaterialTheme.colorScheme.surfaceVariant
                            ) {
                                Text(
                                    "${invoice.durationDays} hari",
                                    style = MaterialTheme.typography.labelSmall,
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                            if (invoice.dueAt != null) {
                                Text(
                                    "· Jatuh tempo: ${invoice.dueAt.take(10)}",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }
                }

                // ── QR Code ──────────────────────────────────────────────────
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier.widthIn(max = 260.dp)
                ) {
                    QrisQrCode(
                        qrString = qrString,
                        size = 220.dp,
                        label = null
                    )
                }

                // ── Status polling ───────────────────────────────────────────
                PollingStatusRow(isPolling = isPolling)

                // ── Instruksi ────────────────────────────────────────────────
                Text(
                    "Scan QR di atas menggunakan aplikasi bank atau e-wallet (GoPay, OVO, Dana, ShopeePay, dll.). " +
                    "Status akan diperbarui otomatis setelah pembayaran dikonfirmasi.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center
                )
            }
        },
        confirmButton = {},
        dismissButton = {
            OutlinedButton(
                onClick = onDismiss,
                colors = ButtonDefaults.outlinedButtonColors(
                    contentColor = MaterialTheme.colorScheme.onSurfaceVariant
                )
            ) {
                Icon(Icons.Default.Cancel, null, modifier = Modifier.size(14.dp))
                Spacer(Modifier.size(6.dp))
                Text("Bayar Nanti")
            }
        }
    )
}

// ─────────────────────────────────────────────────────────────────────────────
// Pulsing dot + label status polling
// ─────────────────────────────────────────────────────────────────────────────

@Composable
private fun PollingStatusRow(isPolling: Boolean) {
    val inf = rememberInfiniteTransition(label = "pulse")
    val alpha by inf.animateFloat(
        initialValue = 0.3f,
        targetValue  = 1f,
        animationSpec = infiniteRepeatable(
            tween(800, easing = LinearEasing),
            RepeatMode.Reverse
        ),
        label = "dotAlpha"
    )

    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        if (isPolling) {
            Box(
                modifier = Modifier
                    .size(8.dp)
                    .alpha(alpha)
                    .background(Primary, CircleShape)
            )
            Text(
                "Menunggu konfirmasi pembayaran...",
                style = MaterialTheme.typography.labelSmall,
                color = Primary
            )
        } else {
            CircularProgressIndicator(modifier = Modifier.size(12.dp), strokeWidth = 1.5.dp, color = Primary)
            Text(
                "Menghubungkan...",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

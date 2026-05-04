package id.rancak.app.presentation.ui.auth.components

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CreditCard
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import id.rancak.app.presentation.viewmodel.BillingIssue

/**
 * Layar peringatan ketika tenant yang dipilih memiliki masalah billing
 * (langganan kedaluwarsa atau belum aktif).
 *
 * Menawarkan dua pilihan:
 * 1. [onPayBilling] — buka layar Billing agar user dapat membayar.
 * 2. [onPickOtherOutlet] — kembali ke daftar tenant untuk memilih outlet lain.
 */
@Composable
fun BillingIssueContent(
    tenantName: String,
    issue: BillingIssue,
    onPayBilling: () -> Unit,
    onPickOtherOutlet: () -> Unit,
    modifier: Modifier = Modifier
) {
    val (icon, title, description) = when (issue) {
        BillingIssue.EXPIRED -> Triple(
            Icons.Default.Warning,
            "Langganan Kedaluwarsa",
            "Masa langganan outlet \"$tenantName\" telah habis. " +
                "Lakukan pembayaran untuk melanjutkan akses ke aplikasi."
        )
        BillingIssue.INACTIVE -> Triple(
            Icons.Default.CreditCard,
            "Langganan Belum Aktif",
            "Outlet \"$tenantName\" belum memiliki langganan aktif. " +
                "Pilih paket dan lakukan pembayaran untuk mulai menggunakan aplikasi."
        )
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 32.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // ── Ikon ──────────────────────────────────────────────────────────────
        Surface(
            shape = MaterialTheme.shapes.extraLarge,
            color = when (issue) {
                BillingIssue.EXPIRED  -> MaterialTheme.colorScheme.errorContainer
                BillingIssue.INACTIVE -> MaterialTheme.colorScheme.secondaryContainer
            },
            modifier = Modifier.size(96.dp)
        ) {
            Box(contentAlignment = Alignment.Center, modifier = Modifier.fillMaxSize()) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    modifier = Modifier.size(48.dp),
                    tint = when (issue) {
                        BillingIssue.EXPIRED  -> MaterialTheme.colorScheme.onErrorContainer
                        BillingIssue.INACTIVE -> MaterialTheme.colorScheme.onSecondaryContainer
                    }
                )
            }
        }

        Spacer(Modifier.height(24.dp))

        // ── Judul ─────────────────────────────────────────────────────────────
        Text(
            text = title,
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.SemiBold,
            textAlign = TextAlign.Center,
            color = MaterialTheme.colorScheme.onSurface
        )

        Spacer(Modifier.height(12.dp))

        // ── Keterangan ────────────────────────────────────────────────────────
        Text(
            text = description,
            style = MaterialTheme.typography.bodyMedium,
            textAlign = TextAlign.Center,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        Spacer(Modifier.height(36.dp))

        // ── Tombol utama — bayar billing ──────────────────────────────────────
        Button(
            onClick = onPayBilling,
            modifier = Modifier
                .widthIn(max = 360.dp)
                .fillMaxWidth()
        ) {
            Icon(
                imageVector = Icons.Default.CreditCard,
                contentDescription = null,
                modifier = Modifier.size(18.dp)
            )
            Spacer(Modifier.width(8.dp))
            Text("Bayar Billing Sekarang")
        }

        Spacer(Modifier.height(12.dp))

        // ── Tombol sekunder — pilih outlet lain ───────────────────────────────
        OutlinedButton(
            onClick = onPickOtherOutlet,
            modifier = Modifier
                .widthIn(max = 360.dp)
                .fillMaxWidth()
        ) {
            Text("Pilih Outlet Lain")
        }
    }
}

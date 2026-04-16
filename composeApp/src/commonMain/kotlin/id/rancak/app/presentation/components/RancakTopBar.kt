package id.rancak.app.presentation.components

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.foundation.layout.statusBarsPadding

/**
 * Header bar kustom Rancak — konsisten di semua screen.
 *
 * Desain: background primary (hijau), back button putih di kiri,
 * ikon + judul di tengah-kiri, ikon dekoratif transparan di kanan.
 *
 * @param title       Judul layar
 * @param icon        Ikon yang mewakili layar (wajib)
 * @param onBack      Lambda tombol kembali (back arrow); null = tidak tampil
 * @param onMenu      Lambda tombol menu sidebar (hamburger); null = tidak tampil.
 *                    Jika keduanya di-set, onMenu diprioritaskan.
 * @param subtitle    Teks kecil opsional di bawah judul
 * @param actions     Konten tambahan di kanan (opsional, misal icon action)
 */
@Composable
fun RancakTopBar(
    title: String,
    icon: ImageVector,
    onBack: (() -> Unit)? = null,
    onMenu: (() -> Unit)? = null,
    subtitle: String? = null,
    actions: @Composable RowScope.() -> Unit = {}
) {
    Surface(
        color = MaterialTheme.colorScheme.primary,
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .statusBarsPadding()          // jaga jarak dari status bar Android
                .height(60.dp)
                .padding(horizontal = 4.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {

            // ── Nav button: hamburger (menu) atau back arrow ─────────────────
            when {
                onMenu != null -> IconButton(onClick = onMenu) {
                    Icon(
                        imageVector        = Icons.Default.Menu,
                        contentDescription = "Menu",
                        tint               = MaterialTheme.colorScheme.onPrimary
                    )
                }
                onBack != null -> IconButton(onClick = onBack) {
                    Icon(
                        imageVector        = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Kembali",
                        tint               = MaterialTheme.colorScheme.onPrimary
                    )
                }
                else -> Spacer(Modifier.width(12.dp))
            }

            // ── Ikon layar ───────────────────────────────────────────────────
            Surface(
                shape = MaterialTheme.shapes.small,
                color = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.15f),
                modifier = Modifier.size(34.dp)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        imageVector = icon,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onPrimary,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }

            Spacer(Modifier.width(10.dp))

            // ── Judul + subtitle ─────────────────────────────────────────────
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onPrimary
                )
                if (subtitle != null) {
                    Text(
                        text = subtitle,
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.6f)
                    )
                }
            }

            // ── Action slot (opsional) ────────────────────────────────────────
            Row(
                verticalAlignment = Alignment.CenterVertically,
                content = actions
            )

            // ── Ikon dekoratif background (kanan) ────────────────────────────
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.10f),
                modifier = Modifier
                    .size(52.dp)
                    .padding(end = 8.dp)
            )
        }
    }
}

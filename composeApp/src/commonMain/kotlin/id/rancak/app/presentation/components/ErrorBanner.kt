package id.rancak.app.presentation.components

import androidx.compose.animation.*
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.zIndex
import id.rancak.app.presentation.designsystem.RancakTheme
import androidx.compose.ui.tooling.preview.Preview
import kotlinx.coroutines.delay

/**
 * Banner error yang slide-down dari atas layar.
 * Auto-dismiss setelah [autoDismissMs] ms (default 3 detik).
 * Tempatkan sebagai child pertama di dalam Box/Scaffold content.
 *
 * @param error  Pesan error; null = tidak tampil
 * @param onDismiss  Callback saat banner ditutup (manual atau auto)
 * @param modifier  Modifier — gunakan .align(Alignment.TopCenter).zIndex(10f) jika di dalam Box
 * @param autoDismissMs  Durasi auto-dismiss dalam ms; 0 = tidak auto-dismiss
 */
@Composable
fun ErrorBanner(
    error: String?,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier
        .fillMaxWidth()
        .zIndex(10f),
    autoDismissMs: Long = 3000L
) {
    // Auto-dismiss
    LaunchedEffect(error) {
        if (error != null && autoDismissMs > 0) {
            delay(autoDismissMs)
            onDismiss()
        }
    }

    AnimatedVisibility(
        visible = error != null,
        enter = slideInVertically(initialOffsetY = { -it }) + fadeIn(),
        exit = slideOutVertically(targetOffsetY = { -it }) + fadeOut(),
        modifier = modifier
    ) {
        Surface(
            color = MaterialTheme.colorScheme.errorContainer,
            shadowElevation = 4.dp
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .statusBarsPadding()
                    .padding(horizontal = 16.dp, vertical = 12.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Icon(
                    Icons.Default.Warning,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.error,
                    modifier = Modifier.size(20.dp)
                )
                Text(
                    text = error ?: "",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onErrorContainer,
                    modifier = Modifier.weight(1f)
                )
                IconButton(
                    onClick = onDismiss,
                    modifier = Modifier.size(24.dp)
                ) {
                    Icon(
                        Icons.Default.Close,
                        contentDescription = "Tutup",
                        tint = MaterialTheme.colorScheme.onErrorContainer,
                        modifier = Modifier.size(16.dp)
                    )
                }
            }
        }
    }
}

// ── Preview ──

@Preview
@Composable
private fun ErrorBannerPreview() {
    RancakTheme {
        Box(Modifier.fillMaxWidth()) {
            ErrorBanner(
                error = "Email dan password wajib diisi",
                onDismiss = {},
                modifier = Modifier.fillMaxWidth().align(Alignment.TopCenter).zIndex(10f)
            )
        }
    }
}

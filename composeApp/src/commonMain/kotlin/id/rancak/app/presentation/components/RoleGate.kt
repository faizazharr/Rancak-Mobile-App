package id.rancak.app.presentation.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import id.rancak.app.domain.model.UserRole
import id.rancak.app.domain.repository.AuthRepository
import org.koin.compose.koinInject

/**
 * Composable guard — hanya me-render [content] jika peran aktif
 * memenuhi [minRole]. Jika tidak, [fallback] ditampilkan (default: kosong).
 *
 * Contoh:
 * ```
 * RoleGate(minRole = UserRole.ADMIN) {
 *     Button(onClick = { /* edit produk */ }) { Text("Edit Produk") }
 * }
 * ```
 */
@Composable
fun RoleGate(
    minRole: UserRole,
    fallback: @Composable () -> Unit = {},
    content: @Composable () -> Unit
) {
    val authRepository: AuthRepository = koinInject()
    val currentRole = authRepository.getUserRole()
    if (currentRole.atLeast(minRole)) {
        content()
    } else {
        fallback()
    }
}

/**
 * Layar pengganti yang menampilkan pesan "akses ditolak" untuk screen
 * yang tidak boleh diakses oleh role saat ini.
 */
@Composable
fun AccessDeniedScreen(
    minRole: UserRole,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier.fillMaxSize().padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            imageVector = Icons.Default.Lock,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.outline
        )
        Text(
            text = "Akses Ditolak",
            style = MaterialTheme.typography.titleLarge,
            modifier = Modifier.padding(top = 12.dp)
        )
        Text(
            text = "Fitur ini hanya tersedia untuk ${minRole.displayName()} ke atas.",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(top = 4.dp)
        )
    }
}

/** Versi screen-level — gate seluruh layar, tampilkan pesan ditolak jika tidak layak. */
@Composable
fun RoleGatedScreen(
    minRole: UserRole,
    content: @Composable () -> Unit
) {
    RoleGate(
        minRole = minRole,
        fallback = { AccessDeniedScreen(minRole = minRole) },
        content = content
    )
}

private fun UserRole.displayName(): String = when (this) {
    UserRole.STAFF -> "Staff"
    UserRole.ADMIN -> "Admin"
    UserRole.OWNER -> "Owner"
}

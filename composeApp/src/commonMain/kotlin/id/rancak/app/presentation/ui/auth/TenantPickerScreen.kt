package id.rancak.app.presentation.ui.auth

import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import id.rancak.app.domain.model.Tenant
import id.rancak.app.presentation.components.ErrorScreen
import id.rancak.app.presentation.components.LoadingScreen
import id.rancak.app.presentation.ui.auth.components.TenantPickerLandscape
import id.rancak.app.presentation.ui.auth.components.TenantPickerPortrait
import id.rancak.app.presentation.viewmodel.TenantPickerViewModel
import org.koin.compose.viewmodel.koinViewModel

/**
 * Entry point layar pemilihan outlet (tenant). Menangani loading/error dari
 * [TenantPickerViewModel] lalu menampilkan layout portrait atau landscape.
 *
 * Tap kartu outlet langsung memilih + konfirmasi (tanpa tombol terpisah).
 */
@Composable
fun TenantPickerScreen(
    onTenantSelected: () -> Unit,
    viewModel: TenantPickerViewModel = koinViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    LaunchedEffect(Unit) { viewModel.loadTenants() }
    LaunchedEffect(uiState.isConfirmed) {
        if (uiState.isConfirmed) onTenantSelected()
    }

    val onSelectAndConfirm: (Tenant) -> Unit = { tenant ->
        viewModel.selectTenant(tenant)
        viewModel.confirm()
    }

    Scaffold { padding ->
        when {
            uiState.isLoading     -> LoadingScreen(Modifier.padding(padding))
            uiState.error != null -> ErrorScreen(
                message  = uiState.error!!,
                onRetry  = viewModel::loadTenants,
                modifier = Modifier.padding(padding)
            )
            else -> BoxWithConstraints(modifier = Modifier.padding(padding).fillMaxSize()) {
                val isWide = maxWidth > maxHeight || maxWidth >= 600.dp
                if (isWide) {
                    TenantPickerLandscape(
                        tenants        = uiState.tenants,
                        selectedTenant = uiState.selectedTenant,
                        onSelectTenant = onSelectAndConfirm
                    )
                } else {
                    TenantPickerPortrait(
                        tenants        = uiState.tenants,
                        selectedTenant = uiState.selectedTenant,
                        onSelectTenant = onSelectAndConfirm
                    )
                }
            }
        }
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// Preview — memanggil layout yang sama dengan yang dipakai TenantPickerScreen
// ─────────────────────────────────────────────────────────────────────────────

@androidx.compose.ui.tooling.preview.Preview(name = "Tenant – Phone",   widthDp = 390, heightDp = 844)
@Composable
private fun TenantPickerScreenPhonePreview() {
    val tenants = listOf(
        id.rancak.app.domain.model.Tenant("1", "Warung Rancak"),
        id.rancak.app.domain.model.Tenant("2", "Cafe Sederhana"),
        id.rancak.app.domain.model.Tenant("3", "Kedai Kopi")
    )
    id.rancak.app.presentation.designsystem.RancakTheme {
        id.rancak.app.presentation.ui.auth.components.TenantPickerPortrait(
            tenants        = tenants,
            selectedTenant = null,
            onSelectTenant = {}
        )
    }
}

@androidx.compose.ui.tooling.preview.Preview(name = "Tenant – Tablet", widthDp = 1024, heightDp = 768)
@Composable
private fun TenantPickerScreenTabletPreview() {
    val tenants = listOf(
        id.rancak.app.domain.model.Tenant("1", "Warung Rancak"),
        id.rancak.app.domain.model.Tenant("2", "Cafe Sederhana"),
        id.rancak.app.domain.model.Tenant("3", "Kedai Kopi"),
        id.rancak.app.domain.model.Tenant("4", "Toko Serba Ada")
    )
    id.rancak.app.presentation.designsystem.RancakTheme {
        id.rancak.app.presentation.ui.auth.components.TenantPickerLandscape(
            tenants        = tenants,
            selectedTenant = null,
            onSelectTenant = {}
        )
    }
}

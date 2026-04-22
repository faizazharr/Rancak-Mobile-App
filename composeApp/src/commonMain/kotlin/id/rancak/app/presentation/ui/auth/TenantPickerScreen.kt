package id.rancak.app.presentation.ui.auth

import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
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
    val uiState by viewModel.uiState.collectAsState()

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

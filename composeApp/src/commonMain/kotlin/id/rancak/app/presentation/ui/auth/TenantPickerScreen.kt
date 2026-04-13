package id.rancak.app.presentation.ui.auth

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Store
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import id.rancak.app.presentation.components.LoadingScreen
import id.rancak.app.presentation.components.ErrorScreen
import id.rancak.app.presentation.components.RancakButton
import id.rancak.app.presentation.designsystem.RancakTheme
import id.rancak.app.presentation.viewmodel.TenantPickerViewModel
import androidx.compose.ui.tooling.preview.Preview
import org.koin.compose.viewmodel.koinViewModel

@Composable
fun TenantPickerScreen(
    onTenantSelected: () -> Unit,
    viewModel: TenantPickerViewModel = koinViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()

    LaunchedEffect(Unit) { viewModel.loadTenants() }
    LaunchedEffect(uiState.isConfirmed) { if (uiState.isConfirmed) onTenantSelected() }

    Scaffold { padding ->
        when {
            uiState.isLoading -> LoadingScreen(Modifier.padding(padding))
            uiState.error != null -> ErrorScreen(uiState.error!!, onRetry = viewModel::loadTenants, modifier = Modifier.padding(padding))
            else -> Column(
            modifier = Modifier.fillMaxSize().padding(padding).padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(Modifier.height(32.dp))
            Icon(Icons.Default.Store, null, Modifier.size(56.dp), MaterialTheme.colorScheme.primary)
            Spacer(Modifier.height(12.dp))
            Text("Pilih Outlet", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
            Text(
                "Pilih outlet untuk mulai kasir",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center
            )
            Spacer(Modifier.height(24.dp))

            LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.weight(1f)) {
                items(uiState.tenants) { tenant ->
                    val selected = uiState.selectedTenant == tenant
                    Card(
                        modifier = Modifier.fillMaxWidth().clickable { viewModel.selectTenant(tenant) },
                        shape = MaterialTheme.shapes.medium,
                        colors = CardDefaults.cardColors(
                            containerColor = if (selected)
                                MaterialTheme.colorScheme.primaryContainer
                            else MaterialTheme.colorScheme.surface
                        ),
                        border = if (selected) null else CardDefaults.outlinedCardBorder()
                    ) {
                        Row(
                            Modifier.padding(16.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                Icons.Default.Store, null,
                                tint = if (selected) MaterialTheme.colorScheme.primary
                                else MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.size(24.dp)
                            )
                            Spacer(Modifier.width(12.dp))
                            Text(tenant.name, style = MaterialTheme.typography.titleSmall, modifier = Modifier.weight(1f))
                            if (selected) Icon(Icons.Default.CheckCircle, null, tint = MaterialTheme.colorScheme.primary)
                        }
                    }
                }
            }

            Spacer(Modifier.height(16.dp))
            RancakButton(
                text = "Lanjutkan",
                onClick = viewModel::confirm,
                enabled = uiState.selectedTenant != null,
                modifier = Modifier.fillMaxWidth()
            )
            }
        }
    }
}

@Preview
@Composable
private fun TenantPickerScreenPreview() {
    RancakTheme {
        Scaffold { padding ->
            Column(
                modifier = Modifier.fillMaxSize().padding(padding).padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Spacer(Modifier.height(32.dp))
                Icon(Icons.Default.Store, null, Modifier.size(56.dp), MaterialTheme.colorScheme.primary)
                Spacer(Modifier.height(12.dp))
                Text("Pilih Outlet", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
                Text("Pilih outlet untuk mulai kasir", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                Spacer(Modifier.height(24.dp))
                listOf("Outlet Utama", "Outlet Cabang 1", "Outlet Cabang 2").forEachIndexed { index, name ->
                    Card(
                        modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                        shape = MaterialTheme.shapes.medium,
                        colors = CardDefaults.cardColors(
                            containerColor = if (index == 0) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surface
                        ),
                        border = if (index == 0) null else CardDefaults.outlinedCardBorder()
                    ) {
                        Row(Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Store, null, tint = if (index == 0) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.size(24.dp))
                            Spacer(Modifier.width(12.dp))
                            Text(name, style = MaterialTheme.typography.titleSmall, modifier = Modifier.weight(1f))
                            if (index == 0) Icon(Icons.Default.CheckCircle, null, tint = MaterialTheme.colorScheme.primary)
                        }
                    }
                }
                Spacer(Modifier.weight(1f))
                RancakButton(text = "Lanjutkan", onClick = {}, modifier = Modifier.fillMaxWidth())
            }
        }
    }
}

package id.rancak.app.presentation.ui.sessions

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Logout
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import id.rancak.app.domain.model.Session
import id.rancak.app.presentation.components.LoadingScreen
import id.rancak.app.presentation.components.RancakTopBar
import id.rancak.app.presentation.designsystem.LocalSizes
import id.rancak.app.presentation.viewmodel.SessionManagementViewModel
import kotlinx.coroutines.launch
import org.koin.compose.viewmodel.koinViewModel

@Composable
fun SessionManagementScreen(onBack: () -> Unit) {
    val viewModel: SessionManagementViewModel = koinViewModel()
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()

    LaunchedEffect(uiState.error) {
        uiState.error?.let {
            scope.launch { snackbarHostState.showSnackbar("⚠ $it") }
            viewModel.clearError()
        }
    }

    if (uiState.showRevokeConfirm) {
        AlertDialog(
            onDismissRequest = viewModel::cancelRevoke,
            title = { Text("Cabut Sesi") },
            text = {
                Text(
                    "Perangkat \"${uiState.sessionToRevoke?.userAgent ?: "tidak dikenal"}\" " +
                        "akan dikeluarkan dan harus login ulang.",
                )
            },
            confirmButton = {
                TextButton(
                    onClick = viewModel::confirmRevoke,
                    colors = ButtonDefaults.textButtonColors(contentColor = MaterialTheme.colorScheme.error),
                ) { Text("Cabut") }
            },
            dismissButton = {
                TextButton(onClick = viewModel::cancelRevoke) { Text("Batal") }
            },
        )
    }

    BoxWithConstraints(Modifier.fillMaxSize()) {
        val sizes = LocalSizes.current
        val isTablet = maxWidth >= sizes.tabletBreakpoint

        Scaffold(
            topBar = {
                RancakTopBar(
                    title = "Sesi Aktif",
                    icon = Icons.Default.DevicesOther,
                    onMenu = onBack,
                    subtitle = "${uiState.sessions.size} perangkat terhubung",
                )
            },
            snackbarHost = { SnackbarHost(snackbarHostState) },
        ) { paddingValues ->
            when {
                uiState.isLoading -> LoadingScreen()
                uiState.sessions.isEmpty() -> {
                    Box(
                        Modifier.fillMaxSize().padding(paddingValues),
                        contentAlignment = Alignment.Center,
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Icon(
                                Icons.Default.DevicesOther,
                                contentDescription = null,
                                modifier = Modifier.size(64.dp),
                                tint = MaterialTheme.colorScheme.outline,
                            )
                            Spacer(Modifier.height(12.dp))
                            Text(
                                "Tidak ada sesi aktif",
                                style = MaterialTheme.typography.bodyLarge,
                                color = MaterialTheme.colorScheme.outline,
                            )
                        }
                    }
                }
                isTablet -> {
                    // ── Tablet: 2-column grid ────────────────────────────────
                    LazyColumn(
                        modifier = Modifier.fillMaxSize().padding(paddingValues),
                        contentPadding = PaddingValues(16.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp),
                    ) {
                        // Current session first — full width
                        val current = uiState.sessions.filter { it.current }
                        val others = uiState.sessions.filter { !it.current }

                        if (current.isNotEmpty()) {
                            item {
                                Text(
                                    "Perangkat Ini",
                                    style = MaterialTheme.typography.labelMedium,
                                    fontWeight = FontWeight.SemiBold,
                                    color = MaterialTheme.colorScheme.outline,
                                    modifier = Modifier.padding(bottom = 4.dp),
                                )
                            }
                            items(current, key = { it.sessionId }) { session ->
                                SessionCard(session, isRevoking = false, onRevoke = {})
                            }
                        }

                        if (others.isNotEmpty()) {
                            item {
                                Text(
                                    "Perangkat Lain",
                                    style = MaterialTheme.typography.labelMedium,
                                    fontWeight = FontWeight.SemiBold,
                                    color = MaterialTheme.colorScheme.outline,
                                    modifier = Modifier.padding(top = 8.dp, bottom = 4.dp),
                                )
                            }
                            // Pair up sessions for 2-column grid on tablet
                            val pairs = others.chunked(2)
                            items(pairs, key = { it.first().sessionId }) { pair ->
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                                ) {
                                    pair.forEach { session ->
                                        Box(Modifier.weight(1f)) {
                                            SessionCard(
                                                session = session,
                                                isRevoking = uiState.revoking == session.sessionId,
                                                onRevoke = { viewModel.requestRevoke(session) },
                                            )
                                        }
                                    }
                                    // Fill remaining space if odd count
                                    if (pair.size == 1) Spacer(Modifier.weight(1f))
                                }
                            }
                        }
                    }
                }
                else -> {
                    // ── Phone: single column list ────────────────────────────
                    LazyColumn(
                        modifier = Modifier.fillMaxSize().padding(paddingValues),
                        contentPadding = PaddingValues(16.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp),
                    ) {
                        items(uiState.sessions, key = { it.sessionId }) { session ->
                            SessionCard(
                                session = session,
                                isRevoking = uiState.revoking == session.sessionId,
                                onRevoke = { viewModel.requestRevoke(session) },
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun SessionCard(
    session: Session,
    isRevoking: Boolean,
    onRevoke: () -> Unit,
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors =
            if (session.current) {
                CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)
            } else {
                CardDefaults.cardColors()
            },
    ) {
        Row(
            modifier =
                Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Icon(
                imageVector = if (session.current) Icons.Default.PhoneAndroid else Icons.Default.Devices,
                contentDescription = null,
                modifier = Modifier.size(40.dp),
                tint =
                    if (session.current) {
                        MaterialTheme.colorScheme.primary
                    } else {
                        MaterialTheme.colorScheme.outline
                    },
            )
            Spacer(Modifier.width(12.dp))
            Column(Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = session.userAgent ?: "Perangkat tidak dikenal",
                        style = MaterialTheme.typography.titleSmall,
                        modifier = Modifier.weight(1f, fill = false),
                    )
                    if (session.current) {
                        Spacer(Modifier.width(6.dp))
                        Badge { Text("Ini") }
                    }
                }
                session.lastUsedAt?.let {
                    Text(
                        text = "Terakhir aktif: $it",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.outline,
                    )
                }
                session.issuedAt?.let {
                    Text(
                        text = "Login: $it",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.outline,
                    )
                }
            }
            if (!session.current) {
                if (isRevoking) {
                    CircularProgressIndicator(Modifier.size(24.dp), strokeWidth = 2.dp)
                } else {
                    IconButton(onClick = onRevoke) {
                        Icon(
                            Icons.AutoMirrored.Filled.Logout,
                            contentDescription = "Cabut sesi",
                            tint = MaterialTheme.colorScheme.error,
                        )
                    }
                }
            }
        }
    }
}

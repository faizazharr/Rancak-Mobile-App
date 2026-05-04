package id.rancak.app.presentation.ui.reservations

import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.EventSeat
import androidx.compose.material.icons.filled.TableBar
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import id.rancak.app.domain.model.Reservation
import id.rancak.app.domain.model.ReservationInput
import id.rancak.app.domain.model.TableStatus
import id.rancak.app.presentation.components.EmptyScreen
import id.rancak.app.presentation.components.ErrorScreen
import id.rancak.app.presentation.components.LoadingScreen
import id.rancak.app.presentation.components.RancakTopBar
import id.rancak.app.presentation.designsystem.RancakColors
import id.rancak.app.presentation.ui.reservations.components.ReservationCard
import id.rancak.app.presentation.ui.reservations.components.ReservationFormContent
import id.rancak.app.presentation.ui.reservations.components.ReservationFormPanel
import id.rancak.app.presentation.viewmodel.ReservationStatusFilter
import id.rancak.app.presentation.viewmodel.ReservationUiState
import id.rancak.app.presentation.viewmodel.ReservationViewModel
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.toImmutableList
import org.koin.compose.viewmodel.koinViewModel

@Composable
fun ReservationScreen(
    onBack: () -> Unit
) {
    val viewModel: ReservationViewModel = koinViewModel()
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    LaunchedEffect(Unit) { viewModel.load() }

    ReservationScreenContent(
        uiState           = uiState,
        onBack            = onBack,
        onRetry           = viewModel::loadReservations,
        onFilterChange    = viewModel::setStatusFilter,
        onAdd             = viewModel::openCreateDialog,
        onEdit            = viewModel::openEditDialog,
        onConfirm         = viewModel::confirm,
        onSeat            = viewModel::requestSeat,
        onComplete        = viewModel::complete,
        onCancel          = viewModel::requestCancel,
        onDismissDialog   = viewModel::dismissDialog,
        onSubmitForm      = viewModel::saveReservation,
        onDismissCancel   = viewModel::dismissCancel,
        onConfirmCancel   = viewModel::confirmCancel,
        onDismissSeat     = viewModel::cancelSeat,
        onConfirmSeat     = viewModel::confirmSeat,
        onConsumeSnackbar = viewModel::consumeSnackbar
    )
}

@Composable
fun ReservationScreenContent(
    uiState: ReservationUiState,
    onBack: () -> Unit,
    onRetry: () -> Unit,
    onFilterChange: (ReservationStatusFilter) -> Unit,
    onAdd: () -> Unit,
    onEdit: (Reservation) -> Unit,
    onConfirm: (Reservation) -> Unit,
    onSeat: (Reservation) -> Unit,
    onComplete: (Reservation) -> Unit,
    onCancel: (Reservation) -> Unit,
    onDismissDialog: () -> Unit,
    onSubmitForm: (ReservationInput) -> Unit,
    onDismissCancel: () -> Unit,
    onConfirmCancel: (String?) -> Unit,
    onDismissSeat: () -> Unit,
    onConfirmSeat: (String) -> Unit,
    onConsumeSnackbar: () -> Unit
) {
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(uiState.snackbarMessage) {
        uiState.snackbarMessage?.let {
            snackbarHostState.showSnackbar(it)
            onConsumeSnackbar()
        }
    }

    BoxWithConstraints(Modifier.fillMaxSize()) {
        val isTablet = maxWidth >= 600.dp

        // ── Phone: form opens full-screen (early return) ──────────────────
        if (!isTablet && uiState.showFormDialog) {
            ReservationFormContent(
                editing      = uiState.editingReservation,
                tables       = uiState.tables.toImmutableList(),
                isSubmitting = uiState.isSubmitting,
                onBack       = onDismissDialog,
                onConfirm    = onSubmitForm
            )
            return@BoxWithConstraints
        }

        Scaffold(
            topBar = {
                RancakTopBar(
                    title    = "Reservasi",
                    icon     = Icons.Default.EventSeat,
                    subtitle = "Manajemen reservasi meja",
                    onMenu   = onBack,
                    actions  = {
                        IconButton(onClick = onAdd) {
                            Icon(
                                Icons.Default.Add,
                                contentDescription = "Tambah Reservasi",
                                tint = MaterialTheme.colorScheme.onPrimary
                            )
                        }
                    }
                )
            },
            snackbarHost = { SnackbarHost(snackbarHostState) }
        ) { padding ->
            if (isTablet) {
                // ── Tablet: split two-pane layout ─────────────────────────
                Row(
                    Modifier
                        .padding(padding)
                        .fillMaxSize()
                ) {
                    // Left: filter + list (42%)
                    Column(
                        Modifier
                            .weight(0.42f)
                            .fillMaxHeight()
                    ) {
                        ReservationListContent(
                            uiState        = uiState,
                            onRetry        = onRetry,
                            onFilterChange = onFilterChange,
                            onConfirm      = onConfirm,
                            onSeat         = onSeat,
                            onComplete     = onComplete,
                            onCancel       = onCancel,
                            onEdit         = onEdit,
                            bottomPadding  = 16.dp
                        )
                    }

                    VerticalDivider(
                        color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)
                    )

                    // Right: form panel or placeholder (58%)
                    Box(
                        Modifier
                            .weight(0.58f)
                            .fillMaxHeight()
                    ) {
                        if (uiState.showFormDialog) {
                            ReservationFormPanel(
                                editing      = uiState.editingReservation,
                                tables       = uiState.tables.toImmutableList(),
                                isSubmitting = uiState.isSubmitting,
                                onClose      = onDismissDialog,
                                onConfirm    = onSubmitForm
                            )
                        } else {
                            Column(
                                Modifier.fillMaxSize(),
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.Center
                            ) {
                                Icon(
                                    Icons.Default.CalendarMonth, null,
                                    Modifier.size(72.dp),
                                    tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.25f)
                                )
                                Spacer(Modifier.height(16.dp))
                                Text(
                                    "Pilih reservasi untuk diedit",
                                    style = MaterialTheme.typography.bodyLarge,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                Spacer(Modifier.height(4.dp))
                                Text(
                                    "atau tekan + untuk membuat baru",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
                                )
                            }
                        }
                    }
                }
            } else {
                // ── Phone: list only ──────────────────────────────────────
                ReservationListContent(
                    uiState        = uiState,
                    onRetry        = onRetry,
                    onFilterChange = onFilterChange,
                    onConfirm      = onConfirm,
                    onSeat         = onSeat,
                    onComplete     = onComplete,
                    onCancel       = onCancel,
                    onEdit         = onEdit,
                    modifier       = Modifier.padding(padding),
                    bottomPadding  = 16.dp
                )
            }
        }

        // ── Cancel reason dialog ──────────────────────────────────────────
        uiState.pendingCancel?.let { target ->
            var reason by remember(target.uuid) { mutableStateOf("") }
            AlertDialog(
                onDismissRequest = { if (!uiState.isSubmitting) onDismissCancel() },
                title = { Text("Batalkan reservasi?") },
                text = {
                    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        Text(
                            "Reservasi atas nama ${target.customerName} akan dibatalkan.",
                            style = MaterialTheme.typography.bodyMedium
                        )
                        OutlinedTextField(
                            value         = reason,
                            onValueChange = { reason = it },
                            label         = { Text("Alasan pembatalan (opsional)") },
                            maxLines      = 3,
                            modifier      = Modifier.fillMaxWidth()
                        )
                    }
                },
                confirmButton = {
                    Button(
                        onClick = { onConfirmCancel(reason.ifBlank { null }) },
                        enabled = !uiState.isSubmitting,
                        colors  = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
                    ) {
                        if (uiState.isSubmitting) CircularProgressIndicator(Modifier.size(16.dp), strokeWidth = 2.dp)
                        else Text("Batalkan Reservasi")
                    }
                },
                dismissButton = {
                    TextButton(onClick = onDismissCancel, enabled = !uiState.isSubmitting) { Text("Tutup") }
                }
            )
        }

        // ── Seat picker dialog ────────────────────────────────────────────
        uiState.pendingSeat?.let { target ->
            val available = uiState.tables.filter { it.status != TableStatus.INACTIVE }
            var selected by remember(target.uuid) { mutableStateOf(target.tableUuid) }

            AlertDialog(
                onDismissRequest = { if (!uiState.isSubmitting) onDismissSeat() },
                title = { Text("Tamu Tiba") },
                text = {
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text(
                            "${target.customerName} (${target.partySize} orang) hadir.",
                            style = MaterialTheme.typography.bodyMedium
                        )
                        HorizontalDivider()
                        if (available.isEmpty()) {
                            Text(
                                "Tidak ada meja tersedia saat ini.",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        } else {
                            Text(
                                "Pilih meja:",
                                style = MaterialTheme.typography.labelMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            available.forEach { t ->
                                val statusColor = when (t.status) {
                                    TableStatus.AVAILABLE -> RancakColors.semantic.statusAvailable
                                    TableStatus.OCCUPIED  -> RancakColors.semantic.statusOccupied
                                    TableStatus.INACTIVE  -> RancakColors.semantic.statusMaintenance
                                }
                                Surface(
                                    shape  = MaterialTheme.shapes.small,
                                    color  = if (selected == t.uuid)
                                        MaterialTheme.colorScheme.primaryContainer
                                    else
                                        MaterialTheme.colorScheme.surface,
                                    tonalElevation = if (selected == t.uuid) 0.dp else 1.dp,
                                    onClick = { selected = t.uuid }
                                ) {
                                    Row(
                                        verticalAlignment     = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(10.dp),
                                        modifier              = Modifier.fillMaxWidth().padding(10.dp)
                                    ) {
                                        RadioButton(selected = selected == t.uuid, onClick = { selected = t.uuid })
                                        Icon(Icons.Default.TableBar, null,
                                            modifier = Modifier.size(16.dp), tint = statusColor)
                                        Column(modifier = Modifier.weight(1f)) {
                                            Text(
                                                "${t.name}${t.area?.let { a -> " · $a" } ?: ""}",
                                                style = MaterialTheme.typography.bodyMedium,
                                                fontWeight = FontWeight.Medium
                                            )
                                            Text(
                                                "${t.capacity} kursi",
                                                style = MaterialTheme.typography.labelSmall,
                                                color = MaterialTheme.colorScheme.onSurfaceVariant
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                },
                confirmButton = {
                    Button(
                        onClick = { selected?.let(onConfirmSeat) },
                        enabled = !uiState.isSubmitting && selected != null
                    ) {
                        if (uiState.isSubmitting) CircularProgressIndicator(Modifier.size(16.dp), strokeWidth = 2.dp)
                        else Text("Dudukkan Tamu")
                    }
                },
                dismissButton = {
                    TextButton(onClick = onDismissSeat, enabled = !uiState.isSubmitting) { Text("Batal") }
                }
            )
        }
    }
}

// ── Shared list content (phone + tablet left panel) ───────────────────────────

@Composable
private fun ReservationListContent(
    uiState: ReservationUiState,
    onRetry: () -> Unit,
    onFilterChange: (ReservationStatusFilter) -> Unit,
    onConfirm: (Reservation) -> Unit,
    onSeat: (Reservation) -> Unit,
    onComplete: (Reservation) -> Unit,
    onCancel: (Reservation) -> Unit,
    onEdit: (Reservation) -> Unit,
    modifier: Modifier = Modifier,
    bottomPadding: androidx.compose.ui.unit.Dp = 16.dp
) {
    Column(modifier.fillMaxSize()) {
        if (uiState.reservations.isNotEmpty()) {
            ReservationSummaryStrip(uiState.reservations.toImmutableList())
        }

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState())
                .padding(horizontal = 12.dp, vertical = 8.dp),
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            ReservationStatusFilter.entries.forEach { filter ->
                val count = when (filter) {
                    ReservationStatusFilter.ALL -> uiState.reservations.size
                    else -> uiState.reservations.count { it.status == filter.apiValue }
                }
                FilterChip(
                    selected = uiState.statusFilter == filter,
                    onClick  = { onFilterChange(filter) },
                    label    = {
                        val countText = if (count > 0) " $count" else ""
                        Text("${filter.label}$countText", style = MaterialTheme.typography.labelMedium)
                    }
                )
            }
        }

        HorizontalDivider()

        Box(Modifier.fillMaxSize()) {
            when {
                uiState.isLoading  -> LoadingScreen()
                uiState.error != null -> ErrorScreen(uiState.error, onRetry = onRetry)
                uiState.reservations.isEmpty() -> EmptyScreen(
                    if (uiState.statusFilter == ReservationStatusFilter.ALL)
                        "Belum ada reservasi"
                    else
                        "Tidak ada reservasi dengan status '${uiState.statusFilter.label}'"
                )
                else -> LazyColumn(
                    contentPadding      = PaddingValues(
                        start  = 12.dp,
                        end    = 12.dp,
                        top    = 10.dp,
                        bottom = bottomPadding
                    ),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(uiState.reservations, key = { it.uuid }) { r ->
                        ReservationCard(
                            reservation = r,
                            onConfirm   = { onConfirm(r) },
                            onSeat      = { onSeat(r) },
                            onComplete  = { onComplete(r) },
                            onCancel    = { onCancel(r) },
                            onEdit      = { onEdit(r) }
                        )
                    }
                }
            }
        }
    }
}

// ── Summary strip ────────────────────────────────────────────────────────────

@Composable
private fun ReservationSummaryStrip(reservations: ImmutableList<Reservation>) {
    val pending   = reservations.count { it.status == "pending" }
    val confirmed = reservations.count { it.status == "confirmed" }
    val seated    = reservations.count { it.status == "seated" }
    val total     = reservations.size

    Surface(
        color = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.4f),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 10.dp),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            SummaryItem("Total", "$total", MaterialTheme.colorScheme.onSurface)
            VerticalDivider(modifier = Modifier.height(30.dp))
            SummaryItem("Menunggu", "$pending", if (pending > 0) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.onSurfaceVariant)
            VerticalDivider(modifier = Modifier.height(30.dp))
            SummaryItem("Konfirm", "$confirmed", if (confirmed > 0) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant)
            VerticalDivider(modifier = Modifier.height(30.dp))
            SummaryItem("Hadir", "$seated", if (seated > 0) RancakColors.semantic.statusAvailable else MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}

@Composable
private fun SummaryItem(label: String, value: String, color: androidx.compose.ui.graphics.Color) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
            value,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = color
        )
        Text(
            label,
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

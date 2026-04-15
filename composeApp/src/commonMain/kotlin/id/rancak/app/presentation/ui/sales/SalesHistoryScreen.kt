package id.rancak.app.presentation.ui.sales

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.sp
import id.rancak.app.domain.model.PaymentMethod
import id.rancak.app.domain.model.Sale
import id.rancak.app.domain.model.SaleStatus
import kotlin.time.Instant
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import id.rancak.app.data.local.SettingsStore
import id.rancak.app.data.printing.PrinterManager
import id.rancak.app.presentation.components.*
import id.rancak.app.presentation.components.RancakTopBar
import id.rancak.app.presentation.designsystem.RancakColors
import id.rancak.app.presentation.util.formatRupiah
import id.rancak.app.presentation.viewmodel.DateFilter
import id.rancak.app.presentation.viewmodel.SalesHistoryViewModel
import org.koin.compose.koinInject
import org.koin.compose.viewmodel.koinViewModel

// ── Utility: format "YYYY-MM-DD" → "15 Apr" ───────────────────────────────────
private fun formatDateShort(dateStr: String): String {
    val p = dateStr.split("-")
    if (p.size != 3) return dateStr
    val day = p[2].trimStart('0').ifEmpty { "0" }
    val month = when (p[1].toIntOrNull()) {
        1 -> "Jan"; 2 -> "Feb"; 3 -> "Mar"; 4 -> "Apr"
        5 -> "Mei"; 6 -> "Jun"; 7 -> "Jul"; 8 -> "Agu"
        9 -> "Sep"; 10 -> "Okt"; 11 -> "Nov"; 12 -> "Des"
        else -> p[1]
    }
    return "$day $month"
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SalesHistoryScreen(
    onBack: () -> Unit,
    viewModel: SalesHistoryViewModel = koinViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()

    LaunchedEffect(Unit) { viewModel.loadSales() }

    Scaffold(
        topBar = {
            RancakTopBar(
                title    = "Riwayat Penjualan",
                icon     = Icons.Default.Receipt,
                subtitle = "Catatan seluruh transaksi",
                onBack   = onBack
            )
        }
    ) { padding ->
        when {
            uiState.isLoading  -> LoadingScreen(Modifier.padding(padding))
            uiState.error != null -> ErrorScreen(
                uiState.error!!, onRetry = viewModel::loadSales,
                modifier = Modifier.padding(padding)
            )
            uiState.allSales.isEmpty() -> EmptyScreen("Belum ada transaksi", Modifier.padding(padding))
            else -> {
                BoxWithConstraints(modifier = Modifier.padding(padding).fillMaxSize()) {
                    val isTablet = maxWidth >= 600.dp

                    if (isTablet) {
                        // ── Tablet: filter+list kiri, detail kanan penuh tinggi ────────
                        Row(Modifier.fillMaxSize()) {

                            // ── Panel kiri ────────────────────────────────────────────
                            Column(
                                modifier = Modifier
                                    .weight(0.38f)
                                    .fillMaxHeight()
                            ) {
                                SearchAndFilterBar(
                                    query           = uiState.searchQuery,
                                    dateFilter      = uiState.dateFilter,
                                    statusFilter    = uiState.statusFilter,
                                    customDateFrom  = uiState.customDateFrom,
                                    customDateTo    = uiState.customDateTo,
                                    onQueryChange   = viewModel::setSearchQuery,
                                    onDateFilter    = viewModel::setDateFilter,
                                    onStatusFilter  = viewModel::setStatusFilter,
                                    onCustomDateRange = viewModel::setCustomDateRange,
                                    onClear         = viewModel::clearFilters,
                                    hasActiveFilter = uiState.searchQuery.isNotBlank() ||
                                        uiState.dateFilter != DateFilter.ALL ||
                                        uiState.statusFilter != null
                                )
                                HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f))

                                if (uiState.sales.isEmpty()) {
                                    Box(
                                        modifier = Modifier.weight(1f).fillMaxWidth(),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Column(
                                            horizontalAlignment = Alignment.CenterHorizontally,
                                            verticalArrangement = Arrangement.spacedBy(6.dp)
                                        ) {
                                            Icon(Icons.Default.SearchOff, contentDescription = null,
                                                modifier = Modifier.size(40.dp),
                                                tint = MaterialTheme.colorScheme.outlineVariant)
                                            Text("Tidak ada hasil",
                                                style = MaterialTheme.typography.titleSmall,
                                                fontWeight = FontWeight.SemiBold)
                                            Text("Coba ubah kata kunci atau filter",
                                                style = MaterialTheme.typography.bodySmall,
                                                color = MaterialTheme.colorScheme.onSurfaceVariant)
                                            TextButton(onClick = viewModel::clearFilters) { Text("Reset Filter") }
                                        }
                                    }
                                } else {
                                    LazyColumn(
                                        modifier = Modifier.weight(1f).fillMaxHeight(),
                                        contentPadding = PaddingValues(8.dp),
                                        verticalArrangement = Arrangement.spacedBy(4.dp)
                                    ) {
                                        items(uiState.sales, key = { it.uuid }) { sale ->
                                            SaleCard(
                                                sale = sale,
                                                isSelected = sale.uuid == uiState.selectedSale?.uuid,
                                                onClick = { viewModel.selectSale(sale) }
                                            )
                                        }
                                    }
                                }
                            }

                            VerticalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))

                            // ── Panel kanan: detail atau ringkasan ────────────────────
                            Box(modifier = Modifier.weight(0.62f).fillMaxHeight()) {
                                val selected = uiState.selectedSale
                                if (selected != null) {
                                    SaleDetailPanel(sale = selected, modifier = Modifier.fillMaxSize())
                                } else {
                                    SalesSummaryPanel(
                                        sales   = uiState.sales,
                                        modifier = Modifier.fillMaxSize()
                                    )
                                }
                            }
                        }
                    } else {
                        // ── Phone: satu kolom, dialog untuk detail ────────────────────
                        Column(Modifier.fillMaxSize()) {
                            SearchAndFilterBar(
                                query           = uiState.searchQuery,
                                dateFilter      = uiState.dateFilter,
                                statusFilter    = uiState.statusFilter,
                                customDateFrom  = uiState.customDateFrom,
                                customDateTo    = uiState.customDateTo,
                                onQueryChange   = viewModel::setSearchQuery,
                                onDateFilter    = viewModel::setDateFilter,
                                onStatusFilter  = viewModel::setStatusFilter,
                                onCustomDateRange = viewModel::setCustomDateRange,
                                onClear         = viewModel::clearFilters,
                                hasActiveFilter = uiState.searchQuery.isNotBlank() ||
                                    uiState.dateFilter != DateFilter.ALL ||
                                    uiState.statusFilter != null
                            )
                            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f))

                            if (uiState.sales.isEmpty()) {
                                Box(
                                    modifier = Modifier.weight(1f).fillMaxWidth(),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Column(
                                        horizontalAlignment = Alignment.CenterHorizontally,
                                        verticalArrangement = Arrangement.spacedBy(6.dp)
                                    ) {
                                        Icon(Icons.Default.SearchOff, contentDescription = null,
                                            modifier = Modifier.size(40.dp),
                                            tint = MaterialTheme.colorScheme.outlineVariant)
                                        Text("Tidak ada hasil",
                                            style = MaterialTheme.typography.titleSmall,
                                            fontWeight = FontWeight.SemiBold)
                                        Text("Coba ubah kata kunci atau filter",
                                            style = MaterialTheme.typography.bodySmall,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant)
                                        TextButton(onClick = viewModel::clearFilters) { Text("Reset Filter") }
                                    }
                                }
                            } else {
                                LazyColumn(
                                    modifier = Modifier.weight(1f).fillMaxSize(),
                                    contentPadding = PaddingValues(8.dp),
                                    verticalArrangement = Arrangement.spacedBy(4.dp)
                                ) {
                                    items(uiState.sales, key = { it.uuid }) { sale ->
                                        SaleCard(
                                            sale = sale,
                                            isSelected = false,
                                            onClick = { viewModel.selectSale(sale) }
                                        )
                                    }
                                }
                            }

                            uiState.selectedSale?.let { sale ->
                                AlertDialog(
                                    onDismissRequest = { viewModel.selectSale(null) },
                                    title = {
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.SpaceBetween,
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Text("Detail Transaksi",
                                                style = MaterialTheme.typography.titleMedium,
                                                fontWeight = FontWeight.Bold)
                                            IconButton(onClick = { viewModel.selectSale(null) }) {
                                                Icon(Icons.Default.Close, "Tutup")
                                            }
                                        }
                                    },
                                    text = {
                                        SaleDetailPanel(sale = sale, modifier = Modifier.fillMaxWidth())
                                    },
                                    confirmButton = {
                                        TextButton(onClick = { viewModel.selectSale(null) }) { Text("Tutup") }
                                    }
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// Search & Filter Bar
// ─────────────────────────────────────────────────────────────────────────────

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun SearchAndFilterBar(
    query: String,
    dateFilter: DateFilter,
    statusFilter: SaleStatus?,
    customDateFrom: String?,
    customDateTo: String?,
    onQueryChange: (String) -> Unit,
    onDateFilter: (DateFilter) -> Unit,
    onStatusFilter: (SaleStatus?) -> Unit,
    onCustomDateRange: (Long, Long) -> Unit,
    onClear: () -> Unit,
    hasActiveFilter: Boolean
) {
    var showDatePicker by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 10.dp, vertical = 8.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        // ── Search field ──────────────────────────────────────────────────────
        OutlinedTextField(
            value = query,
            onValueChange = onQueryChange,
            modifier = Modifier.fillMaxWidth().height(46.dp),
            placeholder = {
                Text("Cari invoice atau nama produk…",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f))
            },
            leadingIcon = {
                Icon(Icons.Default.Search, contentDescription = null, modifier = Modifier.size(18.dp))
            },
            trailingIcon = {
                AnimatedVisibility(visible = query.isNotBlank(), enter = fadeIn(), exit = fadeOut()) {
                    IconButton(onClick = { onQueryChange("") }, modifier = Modifier.size(32.dp)) {
                        Icon(Icons.Default.Close, contentDescription = "Hapus", modifier = Modifier.size(16.dp))
                    }
                }
            },
            singleLine = true,
            shape = MaterialTheme.shapes.medium,
            textStyle = MaterialTheme.typography.bodySmall
        )

        // ── Date filter pills ─────────────────────────────────────────────────
        Row(
            modifier = Modifier.horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(6.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Preset date filters (exclude CUSTOM — that's handled separately)
            DateFilter.entries
                .filter { it != DateFilter.CUSTOM }
                .forEach { filter ->
                    DatePill(
                        label    = filter.label,
                        selected = dateFilter == filter,
                        onClick  = { onDateFilter(filter) }
                    )
                }

            // Custom date pill
            val customLabel = if (customDateFrom != null && customDateTo != null)
                "${formatDateShort(customDateFrom)} – ${formatDateShort(customDateTo)}"
            else null

            CustomDatePill(
                label    = customLabel,
                selected = dateFilter == DateFilter.CUSTOM,
                onClick  = { showDatePicker = true },
                onClear  = {
                    onDateFilter(DateFilter.ALL)
                    onClear()
                }
            )
        }

        // ── Status filter pills ───────────────────────────────────────────────
        val statusOptions: List<Pair<SaleStatus?, String>> = listOf(
            null              to "Semua",
            SaleStatus.HELD   to "Belum Bayar",
            SaleStatus.PAID   to "Lunas",
            SaleStatus.SERVED to "Disajikan",
            SaleStatus.VOID   to "Void"
        )
        Row(
            modifier = Modifier.horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(6.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            statusOptions.forEach { (status, label) ->
                StatusPill(
                    label    = label,
                    status   = status,
                    selected = statusFilter == status,
                    onClick  = { onStatusFilter(status) }
                )
            }

            if (hasActiveFilter) {
                Spacer(Modifier.width(4.dp))
                Surface(
                    shape = MaterialTheme.shapes.extraLarge,
                    color = MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.6f),
                    modifier = Modifier
                        .height(28.dp)
                        .clip(MaterialTheme.shapes.extraLarge)
                        .clickable(onClick = onClear)
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 10.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Icon(Icons.Default.FilterAltOff, contentDescription = null,
                            modifier = Modifier.size(12.dp),
                            tint = MaterialTheme.colorScheme.error)
                        Text("Reset", style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.error,
                            fontWeight = FontWeight.SemiBold)
                    }
                }
            }
        }
    }

    // ── DateRangePicker dialog ────────────────────────────────────────────────
    if (showDatePicker) {
        DateRangePickerDialog(
            onDismiss = { showDatePicker = false },
            onConfirm = { from, to ->
                onCustomDateRange(from, to)
                showDatePicker = false
            }
        )
    }
}

/** Pill tunggal untuk filter tanggal preset */
@Composable
private fun DatePill(
    label: String,
    selected: Boolean,
    onClick: () -> Unit
) {
    val bgColor  = if (selected) MaterialTheme.colorScheme.primary
                   else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f)
    val txtColor = if (selected) MaterialTheme.colorScheme.onPrimary
                   else MaterialTheme.colorScheme.onSurfaceVariant

    Surface(
        shape = MaterialTheme.shapes.extraLarge,
        color = bgColor,
        modifier = Modifier
            .height(30.dp)
            .clip(MaterialTheme.shapes.extraLarge)
            .clickable(onClick = onClick)
            .then(
                if (!selected) Modifier.border(
                    width = 0.8.dp,
                    color = MaterialTheme.colorScheme.outline.copy(alpha = 0.4f),
                    shape = MaterialTheme.shapes.extraLarge
                ) else Modifier
            )
    ) {
        Box(
            modifier = Modifier.padding(horizontal = 12.dp),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = label,
                style = MaterialTheme.typography.labelSmall,
                fontWeight = if (selected) FontWeight.Bold else FontWeight.Normal,
                color = txtColor
            )
        }
    }
}

/** Pill kalender kustom — tampilkan rentang tanggal bila sudah dipilih */
@Composable
private fun CustomDatePill(
    label: String?,          // null = belum dipilih
    selected: Boolean,
    onClick: () -> Unit,
    onClear: () -> Unit
) {
    val bgColor  = if (selected) MaterialTheme.colorScheme.tertiary
                   else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f)
    val txtColor = if (selected) MaterialTheme.colorScheme.onTertiary
                   else MaterialTheme.colorScheme.onSurfaceVariant

    Surface(
        shape = MaterialTheme.shapes.extraLarge,
        color = bgColor,
        modifier = Modifier
            .height(30.dp)
            .clip(MaterialTheme.shapes.extraLarge)
            .clickable(onClick = onClick)
            .then(
                if (!selected) Modifier.border(
                    width = 0.8.dp,
                    color = MaterialTheme.colorScheme.outline.copy(alpha = 0.4f),
                    shape = MaterialTheme.shapes.extraLarge
                ) else Modifier
            )
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 10.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Icon(
                Icons.Default.CalendarMonth, contentDescription = null,
                modifier = Modifier.size(12.dp),
                tint = txtColor
            )
            Text(
                text = label ?: "Pilih Tanggal",
                style = MaterialTheme.typography.labelSmall,
                fontWeight = if (selected) FontWeight.Bold else FontWeight.Normal,
                color = txtColor
            )
            if (selected && label != null) {
                Spacer(Modifier.width(2.dp))
                Icon(
                    Icons.Default.Close, contentDescription = "Hapus rentang",
                    modifier = Modifier
                        .size(12.dp)
                        .clickable(onClick = onClear),
                    tint = txtColor.copy(alpha = 0.8f)
                )
            }
        }
    }
}

/** Pill status dengan warna indikator */
@Composable
private fun StatusPill(
    label: String,
    status: SaleStatus?,
    selected: Boolean,
    onClick: () -> Unit
) {
    val semantic = RancakColors.semantic
    val dotColor = when (status) {
        SaleStatus.PAID      -> semantic.success
        SaleStatus.HELD      -> semantic.warning
        SaleStatus.VOID,
        SaleStatus.CANCELLED -> MaterialTheme.colorScheme.error
        SaleStatus.SERVED    -> semantic.info
        null                 -> MaterialTheme.colorScheme.outline
    }
    val bgColor  = if (selected) dotColor.copy(alpha = 0.15f)
                   else MaterialTheme.colorScheme.surface
    val borderColor = if (selected) dotColor
                      else MaterialTheme.colorScheme.outline.copy(alpha = 0.3f)
    val txtColor = if (selected) dotColor
                   else MaterialTheme.colorScheme.onSurfaceVariant

    Surface(
        shape = MaterialTheme.shapes.extraLarge,
        color = bgColor,
        modifier = Modifier
            .height(28.dp)
            .clip(MaterialTheme.shapes.extraLarge)
            .clickable(onClick = onClick)
            .border(
                width = if (selected) 1.2.dp else 0.6.dp,
                color = borderColor,
                shape = MaterialTheme.shapes.extraLarge
            )
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 10.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(5.dp)
        ) {
            if (status != null) {
                Box(
                    modifier = Modifier
                        .size(6.dp)
                        .clip(RoundedCornerShape(50))
                        .background(dotColor)
                )
            }
            Text(
                text = label,
                style = MaterialTheme.typography.labelSmall,
                fontWeight = if (selected) FontWeight.SemiBold else FontWeight.Normal,
                color = txtColor
            )
        }
    }
}

/** Dialog date range picker (Material3) */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun DateRangePickerDialog(
    onDismiss: () -> Unit,
    onConfirm: (Long, Long) -> Unit
) {
    val state = rememberDateRangePickerState()
    val confirmEnabled = state.selectedStartDateMillis != null && state.selectedEndDateMillis != null

    DatePickerDialog(
        onDismissRequest = onDismiss,
        confirmButton = {
            TextButton(
                onClick = {
                    onConfirm(
                        state.selectedStartDateMillis!!,
                        state.selectedEndDateMillis!!
                    )
                },
                enabled = confirmEnabled
            ) { Text("Terapkan") }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Batal") }
        }
    ) {
        DateRangePicker(
            state = state,
            title = { Text("Pilih Rentang Tanggal", modifier = Modifier.padding(start = 24.dp, top = 16.dp)) },
            headline = {
                Row(
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 24.dp, vertical = 4.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = state.selectedStartDateMillis?.let {
                            formatDateShort(
                                Instant.fromEpochMilliseconds(it)
                                    .toLocalDateTime(TimeZone.UTC).date.toString()
                            )
                        } ?: "Mulai",
                        style = MaterialTheme.typography.titleMedium,
                        color = if (state.selectedStartDateMillis != null)
                            MaterialTheme.colorScheme.primary
                        else MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text("–", style = MaterialTheme.typography.titleMedium)
                    Text(
                        text = state.selectedEndDateMillis?.let {
                            formatDateShort(
                                Instant.fromEpochMilliseconds(it)
                                    .toLocalDateTime(TimeZone.UTC).date.toString()
                            )
                        } ?: "Selesai",
                        style = MaterialTheme.typography.titleMedium,
                        color = if (state.selectedEndDateMillis != null)
                            MaterialTheme.colorScheme.primary
                        else MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            },
            modifier = Modifier.weight(1f, false)
        )
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// Sale Card  —  strip status kiri, compact, info jelas
// ─────────────────────────────────────────────────────────────────────────────

@Composable
private fun SaleCard(
    sale: Sale,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    val semantic = RancakColors.semantic
    val statusColor = when (sale.status) {
        SaleStatus.PAID                       -> semantic.success
        SaleStatus.HELD                       -> semantic.warning
        SaleStatus.VOID, SaleStatus.CANCELLED -> MaterialTheme.colorScheme.error
        SaleStatus.SERVED                     -> semantic.info
    }
    val statusLabel = when (sale.status) {
        SaleStatus.PAID      -> "LUNAS"
        SaleStatus.HELD      -> "BELUM BAYAR"
        SaleStatus.VOID      -> "VOID"
        SaleStatus.CANCELLED -> "BATAL"
        SaleStatus.SERVED    -> "DISAJIKAN"
    }

    Surface(
        shape = MaterialTheme.shapes.medium,
        color = if (isSelected)
            MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.25f)
        else
            MaterialTheme.colorScheme.surface,
        tonalElevation = if (isSelected) 2.dp else 0.dp,
        shadowElevation = if (isSelected) 0.dp else 0.5.dp,
        modifier = Modifier
            .fillMaxWidth()
            .clip(MaterialTheme.shapes.medium)
            .clickable(onClick = onClick)
    ) {
        Row(modifier = Modifier.fillMaxWidth().height(IntrinsicSize.Min)) {
            Box(
                modifier = Modifier
                    .width(4.dp)
                    .fillMaxHeight()
                    .background(statusColor)
            )
            Column(
                modifier = Modifier
                    .weight(1f)
                    .padding(horizontal = 10.dp, vertical = 8.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        sale.invoiceNo ?: "-",
                        style = MaterialTheme.typography.labelLarge,
                        fontWeight = FontWeight.Bold,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.weight(1f)
                    )
                    Text(
                        formatRupiah(sale.total),
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                }

                Spacer(Modifier.height(3.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        "${sale.items.size} item  •  ${sale.orderType.value.replace("_", " ")}",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Surface(
                        shape = MaterialTheme.shapes.extraSmall,
                        color = statusColor.copy(alpha = 0.12f)
                    ) {
                        Text(
                            statusLabel,
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.SemiBold,
                            color = statusColor
                        )
                    }
                }

                sale.createdAt?.take(16)?.replace("T", "  ")?.let { time ->
                    Text(
                        time,
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.outline.copy(alpha = 0.7f)
                    )
                }
            }
        }
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// Dashed divider composable
// ─────────────────────────────────────────────────────────────────────────────

@Composable
private fun DashedDivider(
    modifier: Modifier = Modifier,
    color: Color = MaterialTheme.colorScheme.outlineVariant
) {
    val c = color
    Canvas(modifier = modifier.fillMaxWidth().height(1.dp)) {
        drawLine(
            color = c,
            start = Offset(0f, size.height / 2),
            end = Offset(size.width, size.height / 2),
            strokeWidth = 1.dp.toPx(),
            pathEffect = PathEffect.dashPathEffect(floatArrayOf(6.dp.toPx(), 4.dp.toPx()), 0f)
        )
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// Sale Detail Panel — desain struk/receipt elegan
// ─────────────────────────────────────────────────────────────────────────────

@Composable
private fun SaleDetailPanel(sale: Sale, modifier: Modifier = Modifier) {
    val printerManager: PrinterManager = koinInject()
    val settingsStore: SettingsStore   = koinInject()
    var showPrintDialog by remember { mutableStateOf(false) }

    // Info outlet dari pengaturan
    val outletName    = settingsStore.receiptStoreName.ifBlank { "Rancak POS" }
    val outletAddress = settingsStore.receiptStoreAddress.ifBlank { null }
    val outletPhone   = settingsStore.receiptStorePhone.ifBlank { null }

    val semantic = RancakColors.semantic
    val statusColor = when (sale.status) {
        SaleStatus.PAID                       -> semantic.success
        SaleStatus.HELD                       -> semantic.warning
        SaleStatus.VOID, SaleStatus.CANCELLED -> MaterialTheme.colorScheme.error
        SaleStatus.SERVED                     -> semantic.info
    }
    val statusLabel = when (sale.status) {
        SaleStatus.PAID      -> "✓ LUNAS"
        SaleStatus.HELD      -> "⏳ BELUM BAYAR"
        SaleStatus.VOID      -> "✗ VOID"
        SaleStatus.CANCELLED -> "✗ BATAL"
        SaleStatus.SERVED    -> "✓ DISAJIKAN"
    }

    // ── Receipt wrapper ───────────────────────────────────────────────────────
    Box(
        modifier = modifier
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 16.dp, vertical = 12.dp),
        contentAlignment = Alignment.TopCenter
    ) {
        Card(
            modifier = Modifier
                .widthIn(max = 520.dp)
                .fillMaxWidth(),
            shape = RoundedCornerShape(12.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surface
            ),
            elevation = CardDefaults.cardElevation(defaultElevation = 3.dp)
        ) {
            Column(modifier = Modifier.fillMaxWidth()) {

                // ── Header hijau — brand + status ─────────────────────────────
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(MaterialTheme.colorScheme.primary),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        modifier = Modifier.padding(vertical = 14.dp, horizontal = 16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(3.dp)
                    ) {
                        Icon(
                            Icons.Default.Store, contentDescription = null,
                            tint = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.8f),
                            modifier = Modifier.size(20.dp)
                        )
                        Text(
                            outletName.uppercase(),
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.ExtraBold,
                            letterSpacing = 3.sp,
                            color = MaterialTheme.colorScheme.onPrimary,
                            textAlign = TextAlign.Center
                        )
                        if (outletAddress != null) {
                            Text(
                                outletAddress,
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.75f),
                                letterSpacing = 0.sp,
                                textAlign = TextAlign.Center
                            )
                        }
                        if (outletPhone != null) {
                            Text(
                                outletPhone,
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.7f),
                                textAlign = TextAlign.Center
                            )
                        }
                    }
                }

                // ── Notch effect kiri-kanan ───────────────────────────────────
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Box(
                        modifier = Modifier
                            .size(width = 16.dp, height = 14.dp)
                            .clip(RoundedCornerShape(bottomEnd = 100.dp, topEnd = 0.dp))
                            .background(MaterialTheme.colorScheme.background)
                    )
                    Box(
                        modifier = Modifier
                            .size(width = 16.dp, height = 14.dp)
                            .clip(RoundedCornerShape(bottomStart = 100.dp))
                            .background(MaterialTheme.colorScheme.background)
                    )
                }

                // ── Body struk ────────────────────────────────────────────────
                Column(
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    // Invoice + status badge
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                sale.invoiceNo ?: "-",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                fontFamily = FontFamily.Monospace
                            )
                            sale.createdAt?.take(16)?.replace("T", " ")?.let { time ->
                                Text(
                                    time,
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.outline,
                                    fontFamily = FontFamily.Monospace
                                )
                            }
                        }
                        Surface(
                            shape = RoundedCornerShape(6.dp),
                            color = statusColor.copy(alpha = 0.12f),
                            border = BorderStroke(
                                1.dp, statusColor.copy(alpha = 0.4f)
                            )
                        ) {
                            Text(
                                statusLabel,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold,
                                color = statusColor
                            )
                        }
                    }

                    // Info chips (order type, payment, queue)
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        InfoChip(Icons.Default.TableBar,
                            sale.orderType.value.replace("_", " ").replaceFirstChar { it.uppercase() })
                        sale.paymentMethod?.let { method ->
                            val pmIcon = when (method) {
                                PaymentMethod.CASH     -> Icons.Default.Payments
                                PaymentMethod.QRIS     -> Icons.Default.QrCode2
                                PaymentMethod.CARD     -> Icons.Default.CreditCard
                                PaymentMethod.TRANSFER -> Icons.Default.AccountBalance
                                else                   -> Icons.Default.MoreHoriz
                            }
                            InfoChip(pmIcon, method.value.replaceFirstChar { it.uppercase() })
                        }
                        sale.queueNumber?.let { num ->
                            Surface(
                                shape = MaterialTheme.shapes.extraSmall,
                                color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f)
                            ) {
                                Text(
                                    "#$num",
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp),
                                    style = MaterialTheme.typography.labelSmall,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.primary
                                )
                            }
                        }
                    }

                    DashedDivider()

                    // Daftar item label
                    Text(
                        "PESANAN",
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.ExtraBold,
                        letterSpacing = 2.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    // Items
                    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        sale.items.forEach { item ->
                            Column {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.Top
                                ) {
                                    Column(modifier = Modifier.weight(1f).padding(end = 8.dp)) {
                                        Text(
                                            item.productName,
                                            style = MaterialTheme.typography.bodySmall,
                                            fontWeight = FontWeight.SemiBold
                                        )
                                        val sub = buildList {
                                            if (!item.variantName.isNullOrBlank()) add(item.variantName)
                                            add("${item.qty}x @ ${formatRupiah(item.price)}")
                                        }.joinToString("  •  ")
                                        Text(
                                            sub,
                                            style = MaterialTheme.typography.labelSmall,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                                            fontFamily = FontFamily.Monospace
                                        )
                                        if (!item.note.isNullOrBlank()) {
                                            Text(
                                                "📝 ${item.note}",
                                                style = MaterialTheme.typography.labelSmall,
                                                color = MaterialTheme.colorScheme.outline
                                            )
                                        }
                                    }
                                    Text(
                                        formatRupiah(item.subtotal),
                                        style = MaterialTheme.typography.bodySmall,
                                        fontWeight = FontWeight.SemiBold,
                                        fontFamily = FontFamily.Monospace
                                    )
                                }
                            }
                        }
                    }

                    DashedDivider()

                    // Ringkasan harga
                    Column(verticalArrangement = Arrangement.spacedBy(5.dp)) {
                        ReceiptRow("Subtotal", formatRupiah(sale.subtotal))
                        if (sale.discount > 0) {
                            ReceiptRow("Diskon", "- ${formatRupiah(sale.discount)}",
                                valueColor = MaterialTheme.colorScheme.error)
                        }
                        if (sale.surcharge > 0) {
                            ReceiptRow("Biaya Tambahan", formatRupiah(sale.surcharge))
                        }
                        if (sale.tax > 0) {
                            ReceiptRow("Pajak", formatRupiah(sale.tax))
                        }
                    }

                    // Total — highlight besar
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.35f),
                        border = BorderStroke(
                            1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.3f)
                        ),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 14.dp, vertical = 10.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                "TOTAL",
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.ExtraBold,
                                letterSpacing = 1.sp
                            )
                            Text(
                                formatRupiah(sale.total),
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.ExtraBold,
                                color = MaterialTheme.colorScheme.primary,
                                fontFamily = FontFamily.Monospace
                            )
                        }
                    }

                    // Payment info
                    if (sale.paidAmount > 0 || sale.changeAmount > 0) {
                        DashedDivider()
                        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                            if (sale.paidAmount > 0) {
                                ReceiptRow("Dibayar", formatRupiah(sale.paidAmount))
                            }
                            if (sale.changeAmount > 0) {
                                ReceiptRow("Kembalian", formatRupiah(sale.changeAmount),
                                    valueColor = MaterialTheme.colorScheme.secondary)
                            }
                        }
                    }

                    DashedDivider()

                    // Footer
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 6.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(2.dp)
                    ) {
                        Text(
                            "Terima kasih!",
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )
                        Text(
                            "Kami menunggu kunjungan Anda",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            textAlign = TextAlign.Center
                        )
                    }
                }

                // Bottom notch
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Box(
                        modifier = Modifier
                            .size(width = 16.dp, height = 14.dp)
                            .clip(RoundedCornerShape(topEnd = 100.dp))
                            .background(MaterialTheme.colorScheme.background)
                    )
                    Box(
                        modifier = Modifier
                            .size(width = 16.dp, height = 14.dp)
                            .clip(RoundedCornerShape(topStart = 100.dp))
                            .background(MaterialTheme.colorScheme.background)
                    )
                }

                // ── Tombol Print Ulang (hanya untuk transaksi bukan VOID/CANCELLED) ──
                val canPrint = sale.status != SaleStatus.VOID && sale.status != SaleStatus.CANCELLED
                if (canPrint) {
                    Button(
                        onClick = { showPrintDialog = true },
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 10.dp),
                        shape = MaterialTheme.shapes.medium,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.primary
                        )
                    ) {
                        Icon(
                            Icons.Default.Print,
                            contentDescription = null,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(Modifier.width(8.dp))
                        Text(
                            "Print Ulang Struk",
                            style = MaterialTheme.typography.labelLarge,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }
            }
        }
    }

    // ── Print Dialog ──────────────────────────────────────────────────────────
    if (showPrintDialog) {
        PrintDialog(
            sale           = sale,
            printerManager = printerManager,
            settingsStore  = settingsStore,
            onDismiss      = { showPrintDialog = false }
        )
    }
}

/** Baris info chip kecil di dalam detail panel */
@Composable
private fun InfoChip(icon: ImageVector, label: String) {
    Surface(
        shape = MaterialTheme.shapes.extraSmall,
        color = MaterialTheme.colorScheme.surfaceVariant
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 7.dp, vertical = 3.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(3.dp)
        ) {
            Icon(icon, contentDescription = null,
                modifier = Modifier.size(11.dp),
                tint = MaterialTheme.colorScheme.onSurfaceVariant)
            Text(
                label,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

/** Baris label-nilai style struk */
@Composable
private fun ReceiptRow(
    label: String,
    value: String,
    valueColor: Color = MaterialTheme.colorScheme.onSurface
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            label,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Text(
            value,
            style = MaterialTheme.typography.bodySmall,
            fontWeight = FontWeight.SemiBold,
            color = valueColor,
            fontFamily = FontFamily.Monospace
        )
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// Sales Summary Panel — tampil di panel kanan saat belum ada transaksi dipilih
// ─────────────────────────────────────────────────────────────────────────────

@Composable
private fun SalesSummaryPanel(sales: List<Sale>, modifier: Modifier = Modifier) {
    val semantic = RancakColors.semantic

    // ── Hitung statistik ─────────────────────────────────────────────────────
    val paidSales     = sales.filter { it.status == SaleStatus.PAID || it.status == SaleStatus.SERVED }
    val heldCount     = sales.count  { it.status == SaleStatus.HELD }
    val voidCount     = sales.count  { it.status == SaleStatus.VOID || it.status == SaleStatus.CANCELLED }
    val totalRevenue  = paidSales.sumOf { it.total }
    val avgRevenue    = if (paidSales.isNotEmpty()) totalRevenue / paidSales.size else 0L

    // Breakdown per metode pembayaran
    val byMethod = paidSales
        .groupBy { it.paymentMethod?.value?.uppercase() ?: "LAINNYA" }
        .mapValues { (_, list) -> list.sumOf { it.total } }
        .entries.sortedByDescending { it.value }

    Column(
        modifier = modifier
            .verticalScroll(rememberScrollState())
            .padding(20.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // ── Judul ──────────────────────────────────────────────────────────
        Row(verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            Icon(Icons.Default.BarChart, contentDescription = null,
                tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(22.dp))
            Text("Ringkasan Transaksi",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold)
        }

        // ── Kartu statistik utama ─────────────────────────────────────────
        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            SummaryStatCard(
                modifier    = Modifier.weight(1f),
                icon        = Icons.Default.Receipt,
                iconColor   = MaterialTheme.colorScheme.primary,
                label       = "Total Transaksi",
                value       = "${sales.size}",
                bgColor     = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f)
            )
            SummaryStatCard(
                modifier    = Modifier.weight(1f),
                icon        = Icons.Default.Payments,
                iconColor   = semantic.success,
                label       = "Total Pendapatan",
                value       = formatRupiah(totalRevenue),
                bgColor     = semantic.success.copy(alpha = 0.08f)
            )
        }
        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            SummaryStatCard(
                modifier    = Modifier.weight(1f),
                icon        = Icons.Default.TrendingUp,
                iconColor   = semantic.info,
                label       = "Rata-rata / Transaksi",
                value       = formatRupiah(avgRevenue),
                bgColor     = semantic.info.copy(alpha = 0.08f)
            )
            SummaryStatCard(
                modifier    = Modifier.weight(1f),
                icon        = Icons.Default.Schedule,
                iconColor   = semantic.warning,
                label       = "Belum Bayar",
                value       = "$heldCount transaksi",
                bgColor     = semantic.warning.copy(alpha = 0.08f)
            )
        }

        // ── Breakdown metode pembayaran ────────────────────────────────────
        if (byMethod.isNotEmpty()) {
            Card(
                shape  = MaterialTheme.shapes.medium,
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
            ) {
                Column(modifier = Modifier.padding(14.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text("Metode Pembayaran",
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 0.5.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant)

                    byMethod.forEach { (method, amount) ->
                        val pmIcon = when (method) {
                            "CASH"     -> Icons.Default.Payments
                            "QRIS"     -> Icons.Default.QrCode2
                            "CARD"     -> Icons.Default.CreditCard
                            "TRANSFER" -> Icons.Default.AccountBalance
                            else       -> Icons.Default.MoreHoriz
                        }
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                Icon(pmIcon, contentDescription = null,
                                    modifier = Modifier.size(16.dp),
                                    tint = MaterialTheme.colorScheme.primary)
                                Text(method.lowercase().replaceFirstChar { it.uppercase() },
                                    style = MaterialTheme.typography.bodySmall)
                            }
                            Text(formatRupiah(amount),
                                style = MaterialTheme.typography.bodySmall,
                                fontWeight = FontWeight.SemiBold,
                                fontFamily = FontFamily.Monospace)
                        }
                    }
                }
            }
        }

        // ── Void/Batal info ────────────────────────────────────────────────
        if (voidCount > 0) {
            Surface(
                shape  = MaterialTheme.shapes.medium,
                color  = MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.25f),
                border = BorderStroke(0.8.dp, MaterialTheme.colorScheme.error.copy(alpha = 0.3f))
            ) {
                Row(modifier = Modifier.fillMaxWidth().padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    Icon(Icons.Default.Cancel, contentDescription = null,
                        tint = MaterialTheme.colorScheme.error, modifier = Modifier.size(18.dp))
                    Column {
                        Text("$voidCount transaksi di-void/batal",
                            style = MaterialTheme.typography.bodySmall,
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.error)
                        Text("Tidak dihitung dalam total pendapatan",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }
            }
        }

        // ── Hint pilih transaksi ───────────────────────────────────────────
        Box(
            modifier = Modifier.fillMaxWidth().padding(top = 8.dp),
            contentAlignment = Alignment.Center
        ) {
            Row(verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                Icon(Icons.Default.SubdirectoryArrowLeft, contentDescription = null,
                    modifier = Modifier.size(14.dp),
                    tint = MaterialTheme.colorScheme.outlineVariant)
                Text("Pilih transaksi di kiri untuk melihat detail",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.outlineVariant)
            }
        }
    }
}

/** Kartu statistik kecil dalam ringkasan */
@Composable
private fun SummaryStatCard(
    modifier:  Modifier,
    icon:      ImageVector,
    iconColor: Color,
    label:     String,
    value:     String,
    bgColor:   Color
) {
    Surface(shape = MaterialTheme.shapes.medium, color = bgColor,
        modifier = modifier) {
        Column(modifier = Modifier.padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp)) {
            Icon(icon, contentDescription = null,
                tint = iconColor, modifier = Modifier.size(20.dp))
            Text(value,
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.ExtraBold,
                fontFamily = FontFamily.Monospace,
                maxLines = 1)
            Text(label,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}
